package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.Intent
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.ExportWorkspace
import com.greenezone.gzphotoeditor.editor.GifEncoder
import com.greenezone.gzphotoeditor.editor.GifSize
import com.greenezone.gzphotoeditor.editor.PendingExportJournal
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.GifStore
import com.greenezone.gzphotoeditor.editor.VideoGifSource
import com.greenezone.gzphotoeditor.ui.GifMakerScreenContent
import com.greenezone.gzphotoeditor.ui.GifWorkspaceSession
import com.greenezone.gzphotoeditor.ui.PendingGifRecipe
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.OutputStream
import java.nio.file.Files
import java.util.concurrent.CopyOnWriteArrayList

/** Real Back/save flows: source-supported GIF consistency coverage, not a reported phone exit failure. */
@RunWith(AndroidJUnit4::class)
class AndroidGifDepartureContractTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val target get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val resolver get() = target.contentResolver
    private val media = mutableListOf<Uri>()
    @Volatile private var current = GifDocument()
    @Volatile private var retained = GifWorkspaceSession()
    @Volatile private var departures = 0

    @Test fun dirtyPhotoBackCancelsThenDiscardsOnlyTheUnsavedBranch() = isolated { context ->
        val red = photo("gz-departure-red.png", Color.RED)
        val blue = photo("gz-departure-blue.png", Color.BLUE)
        val original = GifDocument(frames = listOf(red.toString(), blue.toString()))
        val originalBytes = listOf(bytes(red), bytes(blue))
        mount(context.filesDir, original)
        rule.onNodeWithText("Timing").performScrollTo().performClick()
        rule.onNodeWithText("Play once").performClick()
        rule.waitUntil(15_000) { !current.forever }
        val edited = current

        back()
        assertEquals("Dirty Back must wait for a disposition", 0, departures)
        rule.onNodeWithText("Discard", substring = false).assertIsDisplayed()
        rule.onNodeWithText("Cancel", substring = false).performClick()
        rule.runOnIdle {
            assertEquals(edited, current)
            assertEquals(0, departures)
        }

        back()
        rule.onNodeWithText("Discard", substring = false).performClick()
        rule.waitUntil(15_000) { departures == 1 }
        rule.runOnIdle {
            assertEquals(original, current)
            assertTrue("Discard must not retain the abandoned redo branch", retained.history.redo.isEmpty())
            assertFalse("Discarded edit cannot remain as current undo content",
                retained.history.undo.any { it.document == edited })
        }
        assertArrayEquals(originalBytes[0], bytes(red))
        assertArrayEquals(originalBytes[1], bytes(blue))
        assertTrue(GifStore.names(context).isEmpty())
    }

    @Test fun dirtyVideoBackSavesCapturedRecipeBeforeOneDeparture() = isolated { context ->
        val source = publish(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            "gz-departure-video.mp4", "video/mp4") { output ->
            InstrumentationRegistry.getInstrumentation().context.assets
                .open("gif_sample_2sec_h264.mp4").use { it.copyTo(output) }
        }
        val originalBytes = bytes(source)
        val original = VideoGifSource.document(context, source)
        mount(context.filesDir, original)
        rule.onNodeWithText("Reverse").performClick()
        rule.waitUntil(15_000) { current.video?.reverse == true }
        val edited = current

        back()
        assertEquals("Dirty Back must not navigate before saving", 0, departures)
        rule.onNodeWithText("Save", substring = false).performClick()
        rule.onNode(hasSetTextAction()).performTextReplacement("Departure video")
        rule.onNodeWithText("Save", substring = false).performClick()
        rule.waitUntil(15_000) { departures == 1 }
        assertEquals(edited, GifStore.load(context, "Departure video"))
        rule.runOnIdle {
            assertEquals(edited, current)
            assertEquals(1, departures)
        }
        assertArrayEquals(originalBytes, bytes(source))
    }


    @Test fun canceledReplacementPreservesEditsAndAnOldPickerCannotSatisfyTheNextRequest() = isolated { context ->
        val red = photo("gz-picker-red.png", Color.RED)
        val blue = photo("gz-picker-blue.png", Color.BLUE)
        val original = GifDocument(frames = listOf(red.toString(), blue.toString()))
        val originalBytes = listOf(bytes(red), bytes(blue))
        val video = publish(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            "gz-picker-video.mp4", "video/mp4") { output ->
            InstrumentationRegistry.getInstrumentation().context.assets
                .open("gif_sample_2sec_h264.mp4").use { it.copyTo(output) }
        }
        val picker = DelayedPicker()
        mount(context.filesDir, original, picker)
        rule.onNodeWithText("Timing").performScrollTo().performClick()
        rule.onNodeWithText("Play once").performClick()
        rule.waitUntil(15_000) { !current.forever }
        val edited = current

        option("Open video…")
        rule.onNodeWithText("Discard", substring = false).performClick()
        rule.waitUntil(15_000) { picker.sourceCodes.size == 1 }
        rule.runOnIdle {
            assertEquals("Discard is provisional until a usable source exists", edited, current)
            picker.deliver(picker.sourceCodes.first(), null)
        }
        rule.waitForIdle()
        assertEquals(edited, current)
        rule.onNodeWithContentDescription("Undo").performClick()
        rule.runOnIdle { assertEquals(original, current) }
        rule.onNodeWithContentDescription("Redo").performClick()
        rule.runOnIdle { assertEquals(edited, current) }

        option("Open video…")
        rule.onNodeWithText("Discard", substring = false).performClick()
        rule.waitUntil(15_000) { picker.sourceCodes.size == 2 }
        assertNotEquals(picker.sourceCodes[0], picker.sourceCodes[1])
        rule.runOnIdle { picker.deliver(picker.sourceCodes[0], video) }
        rule.waitForIdle()
        rule.runOnIdle {
            assertEquals("Old callback must not consume the newer replacement", edited, current)
            assertNull(current.video)
            picker.deliver(picker.sourceCodes[1], null)
        }
        rule.waitForIdle()
        assertEquals(edited, current)
        assertEquals(0, departures)
        assertArrayEquals(originalBytes[0], bytes(red))
        assertArrayEquals(originalBytes[1], bytes(blue))
    }

    @Test fun discardingRecoveredNativeEditsDoesNotDeleteOrRestoreTheCapturedExport() = isolated { context ->
        val red = photo("gz-discard-recovery-red.png", Color.RED)
        val blue = photo("gz-discard-recovery-blue.png", Color.BLUE)
        val originalBytes = listOf(bytes(red), bytes(blue))
        val recipe = GifDocument(frames = listOf(red.toString(), blue.toString()),
            delayMs = 450, size = GifSize.SMALL, forever = false)
        val journal = PendingExportJournal(context.filesDir)
        val initial = journal.begin(ExportWorkspace.GIF, "image/gif", "retained.gif",
            PendingGifRecipe(recipe, null).encode(), nativeOwnerId = "recovered-native-owner")
        val pending = requireNotNull(journal.failed(initial))
        val stage = File(context.cacheDir, "gz-gif-export/" + pending.id + ".gif")
        stage.parentFile?.mkdirs()
        stage.outputStream().use { output ->
            GifEncoder.write(output, recipe) { index ->
                Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888).apply {
                    eraseColor(if (index == 0) Color.RED else Color.BLUE)
                }
            }
        }
        val stageBytes = stage.readBytes()
        val destination = publish(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "gz-retained-retry.gif", "image/gif") { it.write(byteArrayOf(6, 7, 8)) }
        val picker = DelayedPicker()
        try {
            mount(context.filesDir, GifDocument(), picker)
            rule.waitUntil(15_000) { current == recipe }
            rule.onNodeWithText("Resume export").assertIsDisplayed()
            back() // Dismiss the recovery dialog; retain the independent request.
            back() // Request native departure.
            rule.onNodeWithText("Discard", substring = false).performClick()
            rule.waitUntil(15_000) { departures == 1 }
            val retainedExport = requireNotNull(journal.read(ExportWorkspace.GIF))
            assertFalse(retainedExport.restoreNativeDraft)
            assertEquals("recovered-native-owner", retainedExport.nativeOwnerId)
            assertEquals(pending.recipe, retainedExport.recipe)
            assertArrayEquals(stageBytes, stage.readBytes())
            assertEquals(GifDocument(), current)

            current = GifDocument()
            retained = GifWorkspaceSession()
            rule.activityRule.scenario.recreate()
            rule.activityRule.scenario.onActivity { activity ->
                activity.setContent { Workspace(context.filesDir, GifDocument(), picker) }
            }
            rule.waitForIdle()
            rule.onNodeWithText("Resume export").assertIsDisplayed()
            assertEquals("Native Discard must survive activity recreation", GifDocument(), current)
            assertNotEquals("recovered-native-owner", retained.identity?.ownerId)
            rule.onNodeWithText("Retry export").performClick()
            rule.waitUntil(15_000) { picker.exportCodes.size == 1 }
            val retried = requireNotNull(journal.read(ExportWorkspace.GIF))
            assertFalse(retried.restoreNativeDraft)
            assertEquals(pending.recipe, retried.recipe)
            rule.runOnIdle { picker.deliver(picker.exportCodes.single(), destination) }
            rule.waitUntil(15_000) { journal.read(ExportWorkspace.GIF) == null }
            assertArrayEquals("Retry must preserve the captured output bytes", stageBytes, bytes(destination))
            assertArrayEquals(originalBytes[0], bytes(red))
            assertArrayEquals(originalBytes[1], bytes(blue))
        } finally {
            journal.read(ExportWorkspace.GIF)?.let { journal.discard(it) }
            stage.delete()
        }
    }

    private class DelayedPicker : ActivityResultRegistryOwner {
        val sourceCodes = CopyOnWriteArrayList<Int>()
        val exportCodes = CopyOnWriteArrayList<Int>()
        override val activityResultRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int, contract: ActivityResultContract<I, O>, input: I, options: ActivityOptionsCompat?
            ) {
                when (contract) {
                    is ActivityResultContracts.PickVisualMedia, is ActivityResultContracts.OpenDocument ->
                        sourceCodes += requestCode
                    is ActivityResultContracts.StartActivityForResult -> {
                        check((input as Intent).action == Intent.ACTION_CREATE_DOCUMENT)
                        exportCodes += requestCode
                    }
                    else -> error("Unexpected contract: " + contract)
                }
            }
        }
        fun deliver(code: Int, uri: Uri?) {
            activityResultRegistry.dispatchResult(code,
                if (uri == null) Activity.RESULT_CANCELED else Activity.RESULT_OK,
                uri?.let { Intent().setData(it) })
        }
    }

    private fun option(label: String) {
        rule.onNodeWithText("Options").performClick()
        rule.onNodeWithText(label).performClick()
    }

    private fun back() {
        rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    private fun mount(folder: File, initial: GifDocument, picker: DelayedPicker? = null) {
        current = initial
        departures = 0
        retained = GifWorkspaceSession()
        rule.setContent { Workspace(folder, initial, picker) }
        rule.waitForIdle()
    }

    @Composable
    private fun Workspace(folder: File, initial: GifDocument, picker: DelayedPicker?) {
        val base = LocalContext.current
        val context = remember(base, folder) {
            object : ContextWrapper(base) { override fun getFilesDir(): File = folder }
        }
        val registry = picker ?: requireNotNull(LocalActivityResultRegistryOwner.current)
        CompositionLocalProvider(LocalContext provides context, LocalActivityResultRegistryOwner provides registry) {
            MaterialTheme(colorScheme = darkColorScheme()) {
                var document by remember { mutableStateOf(initial) }
                var session by remember { mutableStateOf(GifWorkspaceSession()) }
                GifMakerScreenContent(document,
                    onDocument = { document = it; current = it },
                    onBack = { departures++ }, onSwitch = { departures++ },
                    session = session, onSession = { session = it; retained = it })
            }
        }
    }

    private fun photo(name: String, color: Int): Uri =
        publish(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, name, "image/png") { output ->
            val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
            try {
                bitmap.eraseColor(color)
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
            } finally { bitmap.recycle() }
        }

    private fun publish(collection: Uri, name: String, mime: String, write: (OutputStream) -> Unit): Uri {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = requireNotNull(resolver.insert(collection, values))
        media.add(uri)
        requireNotNull(resolver.openOutputStream(uri)).use(write)
        resolver.update(uri, ContentValues().apply {
            put(MediaStore.MediaColumns.IS_PENDING, 0)
        }, null, null)
        return uri
    }

    private fun bytes(uri: Uri): ByteArray =
        requireNotNull(resolver.openInputStream(uri)).use { it.readBytes() }

    private fun isolated(test: (Context) -> Unit) {
        val folder = Files.createTempDirectory(target.cacheDir.toPath(), "gz-gif-departure-").toFile()
        val context = object : ContextWrapper(target) { override fun getFilesDir(): File = folder }
        try { test(context) } finally {
            rule.runOnIdle { rule.activity.setContentView(android.widget.FrameLayout(rule.activity)) }
            media.forEach { resolver.delete(it, null, null) }
            folder.deleteRecursively()
        }
    }
}

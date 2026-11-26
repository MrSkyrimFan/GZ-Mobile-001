package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.CollageAspect
import com.greenezone.gzphotoeditor.editor.CollageBackground
import com.greenezone.gzphotoeditor.editor.CollageDocument
import com.greenezone.gzphotoeditor.editor.CollageLayout
import com.greenezone.gzphotoeditor.editor.CollagePhoto
import com.greenezone.gzphotoeditor.editor.CollageStore
import com.greenezone.gzphotoeditor.editor.CollageTemplate
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.ExportSettings
import com.greenezone.gzphotoeditor.editor.ExportWorkspace
import com.greenezone.gzphotoeditor.editor.GifAspect
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.GifEncoder
import com.greenezone.gzphotoeditor.editor.GifFit
import com.greenezone.gzphotoeditor.editor.GifSize
import com.greenezone.gzphotoeditor.editor.GifStore
import com.greenezone.gzphotoeditor.editor.PendingExportJournal
import com.greenezone.gzphotoeditor.editor.PendingExportPhase
import com.greenezone.gzphotoeditor.editor.PendingImageRecipe
import com.greenezone.gzphotoeditor.editor.VideoGifSource
import com.greenezone.gzphotoeditor.ui.CollageWorkspaceScreenContent
import com.greenezone.gzphotoeditor.ui.CollageWorkspaceSession
import com.greenezone.gzphotoeditor.ui.GifMakerScreenContent
import com.greenezone.gzphotoeditor.ui.GifWorkspaceSession
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@U1TargetedCheck
@RunWith(AndroidJUnit4::class)
class AndroidU1CollageGifCommandTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test fun collageResetRetainsTwoSameSourceTilesAndOneUndo() = withCollageTestImage(rule.activity) { source ->
        isolated { storage, mounted ->
            val (neutral, edited) = collage(source)
            val current = mutableStateOf(edited)
            val session = mutableStateOf(CollageWorkspaceSession())
            val original = bytes(source)
            rule.setContent {
                if (mounted.value) Workspace(storage) {
                    CollageWorkspaceScreenContent(current.value, { current.value = it }, {}, {},
                        session.value, { session.value = it })
                }
            }
            awaitCollagePreview()
            rule.onNodeWithTag("workspace-tools-bar").assertIsDisplayed()
            rule.onNodeWithContentDescription("Collapse tools").performTouchInput { click() }
            rule.onNodeWithTag("collage-sections").assertDoesNotExist()
            rule.onNodeWithContentDescription("Expand tools").performTouchInput { click() }
            rule.onNodeWithTag("collage-sections").assertIsDisplayed()

            option("reset")
            awaitDocument(current, neutral)
            assertEquals(neutral, CollageStore.decode(CollageStore.encode(current.value)))
            assertEquals(edited.photos.map { it.id to it.uri }, current.value.photos.map { it.id to it.uri })
            tapHistory("Undo")
            awaitDocument(current, edited)
            rule.onNodeWithContentDescription("Undo").assertIsNotEnabled()
            tapHistory("Redo")
            awaitDocument(current, neutral)
            rule.onNodeWithContentDescription("Redo").assertIsNotEnabled()

            option("reset")
            tapHistory("Undo")
            awaitDocument(current, edited)
            assertArrayEquals(original, bytes(source))
        }
    }

    @Test fun childDeleteCancelsAndFailsSafelyThenClearsWholeCollageAndRejectsLatePicker() =
        withCollageTestImage(rule.activity) { source ->
            isolated { storage, mounted ->
                val (_, edited) = collage(source)
                val context = storageContext(rule.activity, storage)
                assertTrue(CollageStore.save(context, "Protected collage", edited))
                val protected = requireNotNull(CollageStore.loadNamed(context, "Protected collage"))
                val original = bytes(source)
                val owner = UUID.randomUUID().toString()
                val session = mutableStateOf(CollageWorkspaceSession(documentId = owner))
                val current = mutableStateOf(edited)
                val picker = DelayedPicker()
                val journal = PendingExportJournal(storage)
                rule.setContent {
                    if (mounted.value) Workspace(storage, picker) {
                        CollageWorkspaceScreenContent(current.value, { current.value = it }, {}, {},
                            session.value, { session.value = it })
                    }
                }
                awaitCollagePreview()
                val recipe = PendingImageRecipe(ExportSettings(), CollageStore.encode(edited)).encode()
                val request = journal.begin(ExportWorkspace.COLLAGE, "image/jpeg", "work.jpg", recipe, owner)

                option("open_media")
                rule.waitUntil(15_000) { picker.mediaCodes.size == 1 }
                rule.onNodeWithTag("collage-sections").performScrollToNode(hasText("Photos"))
                tapText("Photos")
                rule.onNodeWithTag("collage-tile-actions").performScrollToNode(hasText("Edit selected"))
                tapText("Edit selected")
                waitText("Done")
                option("reset")
                waitText("Reset all edits?")
                tapDialog("Reset")
                rule.onNodeWithContentDescription("Undo").assertIsEnabled()
                assertEquals("Pending child reset must leave the parent unchanged", edited, current.value)

                option("delete")
                waitText("Delete this collage and its pending photo edits?", substring = true)
                tapDialog("Cancel")
                waitText("Done")
                rule.onNodeWithContentDescription("Undo").assertIsEnabled()
                assertEquals(edited, current.value)

                val blocked = File(storage, "pending-exports/native-resolutions.json")
                assertTrue(blocked.mkdir())
                option("delete")
                tapDialog("Delete")
                waitText("Could not update local recovery. Try Delete again.")
                assertEquals(edited, current.value)
                assertTrue(blocked.delete())

                tapDialog("Delete")
                awaitDocument(current, CollageDocument())
                assertEquals(emptyList<CollageDocument>(), session.value.history.undo)
                assertEquals(emptyList<CollageDocument>(), session.value.history.redo)
                assertNotEquals(owner, session.value.documentId)
                rule.onNodeWithContentDescription("Undo").assertIsNotEnabled()
                rule.onNodeWithContentDescription("Redo").assertIsNotEnabled()
                rule.onNodeWithText("Done").assertDoesNotExist()

                rule.runOnIdle { picker.deliver(picker.mediaCodes.single(), source) }
                rule.waitForIdle()
                assertEquals(CollageDocument(), current.value)
                val retained = requireNotNull(journal.read(ExportWorkspace.COLLAGE))
                assertEquals(request.id, retained.id)
                assertEquals(recipe, retained.recipe)
                assertEquals(PendingExportPhase.RETRY, retained.phase)
                assertFalse(retained.restoreNativeDraft)
                assertEquals(protected, CollageStore.loadNamed(context, "Protected collage"))
                assertArrayEquals(original, bytes(source))
            }
        }

    @Test fun photoGifResetKeepsFrameOrderIncludingDuplicateSourcesAndClearsToVideoEntry() =
        withCollageTestImage(rule.activity) { source ->
            isolated { storage, mounted ->
                val sequence = listOf(source.toString(), source.toString())
                val neutral = GifDocument(frames = sequence)
                val edited = neutral.copy(delayMs = 720, size = GifSize.LARGE, fit = GifFit.FILL,
                    aspect = GifAspect.PORTRAIT, forever = false, edits = EditDocument(quarterTurns = 1))
                val context = storageContext(rule.activity, storage)
                assertTrue(GifStore.save(context, "Protected GIF sequence", edited))
                val protected = requireNotNull(GifStore.loadNamed(context, "Protected GIF sequence"))
                val current = mutableStateOf(edited)
                val session = mutableStateOf(GifWorkspaceSession())
                val original = bytes(source)
                rule.setContent {
                    if (mounted.value) Workspace(storage) {
                        GifMakerScreenContent(current.value, { current.value = it }, {}, {},
                            session.value, { session.value = it })
                    }
                }
                rule.onNodeWithTag("workspace-tools-bar").assertIsDisplayed()
                option("reset")
                awaitDocument(current, neutral)
                assertEquals(neutral, GifStore.decode(GifStore.encode(current.value)))
                tapHistory("Undo")
                awaitDocument(current, edited)
                rule.onNodeWithContentDescription("Undo").assertIsNotEnabled()
                tapHistory("Redo")
                awaitDocument(current, neutral)
                option("delete")
                waitText("Delete this GIF work?", substring = true)
                tapDialog("Delete")
                awaitDocument(current, GifDocument())
                waitText("Select one video to turn into a GIF")
                rule.onNodeWithContentDescription("Undo").assertIsNotEnabled()
                rule.onNodeWithContentDescription("Redo").assertIsNotEnabled()
                assertEquals(protected, GifStore.loadNamed(context, "Protected GIF sequence"))
                assertArrayEquals(original, bytes(source))
            }
        }

    @Test fun importedGifResetUsesSourceTimingAndDeleteKeepsStagedExportAndRejectsLateDestination() =
        withCollageTestImage(rule.activity) { destination ->
            val source = animatedGif()
            try {
                isolated { storage, mounted ->
                    val original = VideoGifSource.document(rule.activity, source)
                    val clip = requireNotNull(original.video)
                    assertTrue(clip.isGif)
                    assertTrue(clip.sourceTimesMs.isNotEmpty())
                    val edited = original.copy(video = clip.copy(startMs = 30L,
                        endMs = clip.durationMs - 15, speedPercent = 200, reverse = true),
                        aspect = GifAspect.PORTRAIT, size = GifSize.SMALL, fit = GifFit.FILL,
                        forever = false, repeatCount = null, edits = EditDocument(quarterTurns = 1))
                    val current = mutableStateOf(edited)
                    val session = mutableStateOf(GifWorkspaceSession())
                    val picker = DelayedPicker()
                    val journal = PendingExportJournal(storage)
                    val sourceBytes = bytes(source)
                    val destinationBytes = bytes(destination)
                    rule.setContent {
                        if (mounted.value) Workspace(storage, picker) {
                            GifMakerScreenContent(current.value, { current.value = it }, {}, {},
                                session.value, { session.value = it })
                        }
                    }

                    option("reset")
                    awaitDocument(current, original)
                    assertEquals(clip.sourceTimesMs, current.value.video?.sourceTimesMs)
                    tapHistory("Undo")
                    awaitDocument(current, edited)
                    rule.onNodeWithContentDescription("Undo").assertIsNotEnabled()
                    tapHistory("Redo")
                    awaitDocument(current, original)

                    option("export")
                    rule.waitUntil(25_000) { picker.exportCodes.size == 1 }
                    val request = requireNotNull(journal.read(ExportWorkspace.GIF))
                    assertEquals(PendingExportPhase.PICKING, request.phase)
                    val stage = File(storage, "gz-gif-export/" + request.id + ".gif")
                    val stagedBytes = stage.readBytes()
                    assertTrue(stagedBytes.isNotEmpty())

                    option("delete")
                    waitText("Delete this GIF work?", substring = true)
                    tapDialog("Delete")
                    awaitDocument(current, GifDocument())
                    waitText("Select one video to turn into a GIF")
                    rule.onNodeWithContentDescription("Undo").assertIsNotEnabled()
                    rule.onNodeWithContentDescription("Redo").assertIsNotEnabled()

                    rule.runOnIdle { picker.deliver(picker.exportCodes.single(), destination) }
                    rule.waitForIdle()
                    val retained = requireNotNull(journal.read(ExportWorkspace.GIF))
                    assertEquals(request.id, retained.id)
                    assertEquals(request.recipe, retained.recipe)
                    assertEquals(PendingExportPhase.RETRY, retained.phase)
                    assertFalse(retained.restoreNativeDraft)
                    assertArrayEquals(stagedBytes, stage.readBytes())
                    assertArrayEquals(sourceBytes, bytes(source))
                    assertArrayEquals(destinationBytes, bytes(destination))

                    option("export")
                    waitText("Resume export")
                    rule.runOnIdle { mounted.value = false }
                    rule.waitForIdle()
                    rule.runOnIdle {
                        session.value = GifWorkspaceSession()
                        mounted.value = true
                    }
                    waitText("Resume export")
                    assertEquals("A fresh GIF composition must not restore deleted working media",
                        GifDocument(), current.value)
                }
            } finally {
                rule.activity.contentResolver.delete(source, null, null)
            }
        }

    @Test fun gifDeleteIsDisabledDuringRealProviderCopyAndCopyFinishesWithStagedBytes() {
        val source = animatedGif()
        try {
            isolated { storage, mounted ->
                val imported = VideoGifSource.document(rule.activity, source)
                val current = mutableStateOf(imported)
                val session = mutableStateOf(GifWorkspaceSession())
                val picker = DelayedPicker()
                val destination = HeldGifDestination(rule.activity.contentResolver, File(storage, "held.gif"))
                val originalBytes = bytes(source)
                val sentinel = byteArrayOf(11, 22, 33)
                destination.file.writeBytes(sentinel)
                val journal = PendingExportJournal(storage)
                rule.setContent {
                    if (mounted.value) Workspace(storage, picker, destination.resolver) {
                        GifMakerScreenContent(current.value, { current.value = it }, {}, {},
                            session.value, { session.value = it })
                    }
                }

                try {
                    option("export")
                    rule.waitUntil(25_000) { picker.exportCodes.size == 1 }
                    val request = requireNotNull(journal.read(ExportWorkspace.GIF))
                    assertEquals(PendingExportPhase.PICKING, request.phase)
                    val stage = File(storage, "gz-gif-export/" + request.id + ".gif")
                    val stagedBytes = stage.readBytes()
                    assertTrue(stagedBytes.size > sentinel.size)
                    rule.runOnIdle { picker.deliver(picker.exportCodes.single(), destination.uri) }
                    rule.waitUntil(15_000) { destination.entered.count == 0L }
                    val copying = requireNotNull(journal.read(ExportWorkspace.GIF))
                    assertEquals(request.id, copying.id)
                    assertEquals(PendingExportPhase.COPYING, copying.phase)
                    assertEquals(destination.uri.toString(), copying.destination)
                    assertArrayEquals(sentinel, destination.file.readBytes())

                    rule.onNodeWithTag("workspace-options").performTouchInput { click() }
                    val delete = rule.onNodeWithTag("workspace-command-delete").performScrollTo()
                    delete.assertIsDisplayed().assertIsNotEnabled()
                    rule.onNodeWithText("Wait for the GIF to finish copying to its destination.").assertIsDisplayed()
                    delete.performTouchInput { click() }
                    rule.onNodeWithText("Delete current work?").assertDoesNotExist()
                    rule.onNodeWithText("GIF saved").assertDoesNotExist()
                    assertEquals(imported, current.value)
                    assertArrayEquals(sentinel, destination.file.readBytes())

                    destination.release.countDown()
                    waitText("GIF saved")
                    assertNull(journal.read(ExportWorkspace.GIF))
                    assertFalse(stage.exists())
                    assertArrayEquals(stagedBytes, destination.file.readBytes())
                    assertArrayEquals(originalBytes, bytes(source))
                    assertEquals(imported, current.value)
                } finally {
                    destination.release.countDown()
                }
            }
        } finally {
            rule.activity.contentResolver.delete(source, null, null)
        }
    }

    private fun collage(source: Uri): Pair<CollageDocument, CollageDocument> {
        val tiles = listOf(CollagePhoto(source.toString(), id = "left-tile"),
            CollagePhoto(source.toString(), id = "right-tile"))
        val layout = CollageLayout(template = CollageTemplate.SIDE_BY_SIDE, aspect = CollageAspect.LANDSCAPE)
        val neutral = CollageDocument(tiles, layout)
        val edited = neutral.copy(
            photos = tiles.mapIndexed { index, tile -> tile.copy(edits = EditDocument(quarterTurns = index + 1)) },
            layout = layout.copy(template = CollageTemplate.STACKED, aspect = CollageAspect.PORTRAIT,
                gap = .035f, background = CollageBackground.WHITE)
        )
        return neutral to edited
    }

    private fun option(command: String) {
        rule.onNodeWithTag("workspace-options").assertIsDisplayed().performTouchInput { click() }
        rule.onNodeWithTag("workspace-command-" + command).assertIsEnabled().performTouchInput { click() }
    }

    private fun tapText(label: String) {
        rule.onNode(hasText(label) and hasClickAction()).assertIsDisplayed().performTouchInput { click() }
    }

    private fun tapDialog(label: String) {
        rule.onNode(hasText(label) and hasClickAction() and hasAnyAncestor(isDialog()))
            .assertIsDisplayed().performTouchInput { click() }
    }

    private fun tapHistory(label: String) {
        rule.onNodeWithContentDescription(label).assertIsEnabled().performTouchInput { click() }
    }

    private fun waitText(text: String, substring: Boolean = false) {
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText(text, substring).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun awaitCollagePreview() {
        rule.waitUntil(15_000) {
            rule.onAllNodesWithContentDescription("Photo collage preview").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun <T> awaitDocument(current: MutableState<T>, expected: T) {
        rule.waitUntil(15_000) { current.value == expected }
        rule.waitForIdle()
    }

    private fun isolated(test: (File, MutableState<Boolean>) -> Unit) {
        val storage = Files.createTempDirectory(rule.activity.cacheDir.toPath(), "u1-collage-gif-").toFile()
        val mounted = mutableStateOf(true)
        try {
            test(storage, mounted)
        } finally {
            rule.runOnUiThread { mounted.value = false }
            rule.waitForIdle()
            storage.deleteRecursively()
        }
    }

    @Composable
    private fun Workspace(
        storage: File,
        picker: DelayedPicker? = null,
        resolver: ContentResolver? = null,
        content: @Composable () -> Unit
    ) {
        val base = LocalContext.current
        val context = remember(base, storage, resolver) { storageContext(base, storage, resolver) }
        val registry = picker ?: requireNotNull(LocalActivityResultRegistryOwner.current)
        CompositionLocalProvider(LocalContext provides context, LocalActivityResultRegistryOwner provides registry) {
            MaterialTheme(colorScheme = darkColorScheme(), content = content)
        }
    }

    private fun storageContext(base: Context, storage: File, resolver: ContentResolver? = null): Context =
        object : ContextWrapper(base) {
            override fun getFilesDir(): File = storage
            override fun getCacheDir(): File = storage
            override fun getContentResolver(): ContentResolver = resolver ?: base.contentResolver
        }

    private fun bytes(uri: Uri) =
        requireNotNull(rule.activity.contentResolver.openInputStream(uri)).use { it.readBytes() }

    private fun animatedGif(): Uri {
        val resolver = rule.activity.contentResolver
        val source = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "gz-u1-source-" + System.nanoTime() + ".gif")
                put(MediaStore.Images.Media.MIME_TYPE, "image/gif")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        try {
            val sequence = GifDocument(frames = listOf(source.toString(), source.toString()))
            requireNotNull(resolver.openOutputStream(source, "wt")).use { output ->
                GifEncoder.write(output, sequence) { index ->
                    Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888).apply {
                        eraseColor(if (index == 0) Color.RED else Color.BLUE)
                    }
                }
            }
            resolver.update(source, ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }, null, null)
            return source
        } catch (failure: Throwable) {
            resolver.delete(source, null, null)
            throw failure
        }
    }

    /** Pause the real controller at its destination open, after it has recorded COPYING. */
    private class HeldGifDestination(private val media: ContentResolver, val file: File) : ContentProvider() {
        val uri: Uri = Uri.parse("content://gz-u1-test/held.gif")
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val resolver: ContentResolver = ContentResolver.wrap(this)

        override fun onCreate() = true

        override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
            if (uri != this.uri) return media.openFileDescriptor(uri, mode)
            check(mode == "wt")
            entered.countDown()
            if (!release.await(30, TimeUnit.SECONDS)) throw FileNotFoundException("Test copy was not released")
            return ParcelFileDescriptor.open(file,
                ParcelFileDescriptor.MODE_WRITE_ONLY or ParcelFileDescriptor.MODE_TRUNCATE)
        }

        override fun getType(uri: Uri): String? = if (uri == this.uri) "image/gif" else media.getType(uri)

        override fun query(
            uri: Uri, projection: Array<out String>?, selection: String?,
            selectionArgs: Array<out String>?, sortOrder: String?
        ): Cursor? = media.query(uri, projection, selection, selectionArgs, sortOrder)

        override fun insert(uri: Uri, values: ContentValues?): Uri? = throw UnsupportedOperationException()
        override fun update(uri: Uri, values: ContentValues?, selection: String?,
            selectionArgs: Array<out String>?): Int = throw UnsupportedOperationException()
        override fun delete(uri: Uri, selection: String?,
            selectionArgs: Array<out String>?): Int = throw UnsupportedOperationException()
    }

    private class DelayedPicker : ActivityResultRegistryOwner {
        val mediaCodes = CopyOnWriteArrayList<Int>()
        val exportCodes = CopyOnWriteArrayList<Int>()
        override val activityResultRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                when (contract) {
                    is ActivityResultContracts.PickMultipleVisualMedia -> mediaCodes += requestCode
                    is ActivityResultContracts.StartActivityForResult -> {
                        check((input as Intent).action == Intent.ACTION_CREATE_DOCUMENT)
                        exportCodes += requestCode
                    }
                    else -> error("Unexpected picker contract: " + contract)
                }
            }
        }

        fun deliver(code: Int, uri: Uri) {
            activityResultRegistry.dispatchResult(code, Activity.RESULT_OK, Intent().setData(uri))
        }
    }
}

package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Movie
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.ExportWorkspace
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.GifEncoder
import com.greenezone.gzphotoeditor.editor.GifSize
import com.greenezone.gzphotoeditor.editor.GifStore
import com.greenezone.gzphotoeditor.editor.PendingExportJournal
import com.greenezone.gzphotoeditor.editor.PendingExportPhase
import com.greenezone.gzphotoeditor.ui.PendingGifRecipe
import com.greenezone.gzphotoeditor.ui.PhotoEditorScreen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.OutputStream
import java.util.concurrent.CopyOnWriteArrayList

@RunWith(AndroidJUnit4::class)
class AndroidGifExportRecoveryTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val journal get() = PendingExportJournal(context.filesDir)
    private val resolver get() = context.contentResolver
    private val media = mutableListOf<Uri>()

    private class DelayedPicker(private val gif: Uri? = null) : ActivityResultRegistryOwner {
        val exportCodes = CopyOnWriteArrayList<Int>()
        override val activityResultRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int, contract: ActivityResultContract<I, O>, input: I, options: ActivityOptionsCompat?
            ) {
                when (contract) {
                    is ActivityResultContracts.OpenDocument ->
                        dispatchResult(requestCode, Activity.RESULT_OK, Intent().setData(requireNotNull(gif)))
                    is ActivityResultContracts.StartActivityForResult -> {
                        val intent = input as Intent
                        check(intent.action == Intent.ACTION_CREATE_DOCUMENT && intent.type == "image/gif")
                        exportCodes += requestCode
                    }
                    else -> error("Unexpected activity result contract: $contract")
                }
            }
        }

        fun deliver(code: Int, destination: Uri?) {
            activityResultRegistry.dispatchResult(code,
                if (destination == null) Activity.RESULT_CANCELED else Activity.RESULT_OK,
                destination?.let { Intent().setData(it) })
        }
    }

    @Test fun videoGifRedeliveryRegeneratesTheEditedRecipeAfterStageReclamation() = isolated {
        val original = generatedGif()
        val destination = destination("gz-video-recovered.gif")
        val duplicate = destination("gz-video-duplicate.gif")
        val picker = DelayedPicker(original)
        rule.setContent { Workspace(picker) }
        openGifWorkspace()
        gifOption("Open GIF…")
        waitForText("Play")
        rule.onNodeWithText("Reverse").performClick()
        gifOption("Export GIF…")
        val pending = awaitPicker(picker)
        assertTrue(PendingGifRecipe.decode(pending.recipe)?.document?.video?.reverse == true)

        val stage = File(context.cacheDir, "gz-gif-export/${pending.id}.gif")
        assertTrue("Video must finish staging before the picker launches", stage.length() > 0)
        assertTrue(stage.delete())
        recreate(picker)
        assertEquals(pending.id, journal.read(ExportWorkspace.GIF)?.id)
        rule.runOnIdle {
            val code = picker.exportCodes.single()
            picker.deliver(code, destination)
            picker.deliver(code, duplicate)
        }
        waitForText("GIF saved")
        assertFrames(destination, Color.BLUE, Color.RED)
        assertArrayEquals(SENTINEL, bytes(duplicate))
        assertNull(journal.read(ExportWorkspace.GIF))
        assertFalse(stage.exists())

        gifOption("Export GIF…")
        val canceled = awaitPicker(picker, launches = 2)
        val canceledStage = File(context.cacheDir, "gz-gif-export/${canceled.id}.gif")
        assertTrue(canceledStage.length() > 0)
        rule.runOnIdle { picker.deliver(picker.exportCodes.last(), null) }
        waitForText("GIF export cancelled")
        assertNull(journal.read(ExportWorkspace.GIF))
        assertFalse(canceledStage.exists())
    }

    @Test fun legacyPhotoGifReturnsToTheRightRouteAndDropsCanceledRequest() = isolated {
        val project = "Recovery photos"
        val recipe = photoRecipe()
        assertTrue(GifStore.save(context, project, recipe))
        try {
            val untouched = destination("gz-photo-cancel.gif")
            val destination = destination("gz-photo-recovered.gif")
            val picker = DelayedPicker()
            rule.setContent { Workspace(picker) }
            openGifWorkspace()
            gifOption("Open project…")
            rule.onNodeWithText(project).performScrollTo().performClick()
            waitForText("Photos")
            gifOption("Export GIF…")
            val first = awaitPicker(picker)
            assertEquals(project, PendingGifRecipe.decode(first.recipe)?.savedName)
            assertEquals(recipe, PendingGifRecipe.decode(first.recipe)?.document)

            recreate(picker)
            waitForText("Photos")
            rule.runOnIdle { picker.deliver(picker.exportCodes.single(), null) }
            waitForText("GIF export cancelled")
            assertNull(journal.read(ExportWorkspace.GIF))
            assertArrayEquals(SENTINEL, bytes(untouched))

            gifOption("Export GIF…")
            val second = awaitPicker(picker, launches = 2)
            assertEquals(project, PendingGifRecipe.decode(second.recipe)?.savedName)
            recreate(picker, launches = 2)
            waitForText("Photos")
            rule.runOnIdle { picker.deliver(picker.exportCodes.last(), destination) }
            waitForText("GIF exported", substring = true)
            assertFrames(destination, Color.RED, Color.BLUE)
            assertNull(journal.read(ExportWorkspace.GIF))
        } finally {
            GifStore.deleteProject(context, project)
        }
    }

    @Test fun ambiguousPreviousCopyCanRetryOnlyToANewlyChosenDestination() = isolated {
        val oldDestination = destination("gz-gif-previous-copy.gif")
        val newDestination = destination("gz-gif-new-destination.gif")
        val recipe = photoRecipe()
        val pending = journal.begin(ExportWorkspace.GIF, "image/gif", "retry.gif",
            PendingGifRecipe(recipe, null).encode())
        val claimed = requireNotNull(journal.claim(pending, oldDestination))
        journal.copying(claimed)

        val picker = DelayedPicker()
        rule.setContent { Workspace(picker) }
        waitForText("Resume export")
        rule.onNodeWithText("incomplete or already finished", substring = true).assertIsDisplayed()
        rule.onNodeWithText("Retry export").performClick()
        val replacement = awaitPicker(picker)
        assertNotEquals(pending.id, replacement.id)
        assertNull(replacement.destination)
        rule.runOnIdle { picker.deliver(picker.exportCodes.single(), newDestination) }
        waitForText("GIF exported", substring = true)
        assertFrames(newDestination, Color.RED, Color.BLUE)
        assertArrayEquals(SENTINEL, bytes(oldDestination))
        assertNull(journal.read(ExportWorkspace.GIF))
    }

    @Composable
    private fun Workspace(picker: DelayedPicker) {
        CompositionLocalProvider(LocalActivityResultRegistryOwner provides picker) {
            MaterialTheme(colorScheme = darkColorScheme()) { PhotoEditorScreen() }
        }
    }

    private fun gifOption(label: String) {
        rule.onNodeWithContentDescription("Options").performClick()
        rule.onNodeWithText(label).performClick()
    }

    private fun openGifWorkspace() {
        rule.onNodeWithContentDescription("Choose editing workspace").performClick()
        rule.onNodeWithText("GIF maker").performClick()
    }

    private fun recreate(picker: DelayedPicker, launches: Int = 1) {
        val original = rule.activity
        rule.activityRule.scenario.recreate()
        rule.activityRule.scenario.onActivity { recreated ->
            assertNotSame(original, recreated)
            recreated.setContent { Workspace(picker) }
        }
        rule.waitForIdle()
        rule.runOnIdle { assertEquals(launches, picker.exportCodes.size) }
    }

    private fun awaitPicker(picker: DelayedPicker, launches: Int = 1) =
        run {
            rule.waitUntil(25_000) { picker.exportCodes.size == launches }
            requireNotNull(journal.read(ExportWorkspace.GIF)).also {
                assertEquals(PendingExportPhase.PICKING, it.phase)
            }
        }

    private fun photoRecipe(): GifDocument = GifDocument(
        frames = listOf(image("gz-recover-red.png", Color.RED), image("gz-recover-blue.png", Color.BLUE))
            .map { it.toString() },
        delayMs = 450,
        size = GifSize.SMALL,
        forever = false
    )

    private fun bitmap(color: Int): Bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        .apply { eraseColor(color) }

    private fun image(name: String, color: Int): Uri = publish(name, "image/png") { output ->
        val picture = bitmap(color)
        try { check(picture.compress(Bitmap.CompressFormat.PNG, 100, output)) }
        finally { picture.recycle() }
    }

    private fun generatedGif(): Uri = publish("gz-recovery-two-colors.gif", "image/gif") { output ->
        val doc = GifDocument(frames = listOf("content://test/red", "content://test/blue"),
            delayMs = 450, size = GifSize.SMALL, forever = false)
        GifEncoder.write(output, doc) { bitmap(if (it == 0) Color.RED else Color.BLUE) }
    }

    private fun destination(name: String): Uri = publish(name, "image/gif") { it.write(SENTINEL) }

    private fun publish(name: String, mime: String, write: (OutputStream) -> Unit): Uri {
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, mime)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        media += uri
        requireNotNull(resolver.openOutputStream(uri)).use(write)
        resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
        return uri
    }

    private fun bytes(uri: Uri): ByteArray = requireNotNull(resolver.openInputStream(uri)).use { it.readBytes() }

    private fun assertFrames(uri: Uri, first: Int, last: Int) {
        val encoded = bytes(uri)
        val animation = requireNotNull(Movie.decodeByteArray(encoded, 0, encoded.size))
        assertEquals(900, animation.duration())
        val frame = Bitmap.createBitmap(animation.width(), animation.height(), Bitmap.Config.ARGB_8888)
        try {
            animation.setTime(100)
            animation.draw(Canvas(frame), 0f, 0f)
            assertEquals(first, frame.getPixel(frame.width / 2, frame.height / 2))
            animation.setTime(600)
            animation.draw(Canvas(frame), 0f, 0f)
            assertEquals(last, frame.getPixel(frame.width / 2, frame.height / 2))
        } finally { frame.recycle() }
    }

    private fun waitForText(text: String, substring: Boolean = false) {
        rule.waitUntil(25_000) {
            rule.onAllNodesWithText(text, substring = substring).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun isolated(test: () -> Unit) {
        try {
            journal.read(ExportWorkspace.GIF)?.let { journal.discard(it) }
            test()
        } finally {
            journal.read(ExportWorkspace.GIF)?.let { journal.discard(it) }
            media.forEach { resolver.delete(it, null, null) }
        }
    }

    private companion object {
        val SENTINEL = byteArrayOf(14, 27, 39, 54)
    }
}

package com.greenezone.gzphotoeditor

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.*
import com.greenezone.gzphotoeditor.ui.VideoGifScreen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidGifWorkspacePlaybackTest {
    @get:Rule val rule = createComposeRule()
    private val instrument = InstrumentationRegistry.getInstrumentation()
    private val context = instrument.targetContext
    private val resolver = context.contentResolver

    private fun show(initial: GifDocument, changed: (GifDocument) -> Unit = {}) {
        rule.setContent {
            var doc by remember { mutableStateOf(initial) }
            VideoGifScreen(doc, { doc = it; changed(it) }, {}, {}, {})
        }
    }

    private fun waitFrame(index: Int) {
        rule.waitUntil(15_000) {
            rule.onAllNodesWithContentDescription("GIF preview, frame $index").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun centerColor(): Int {
        val frame = rule.onNodeWithTag("gif-frame").captureToImage().asAndroidBitmap()
        return frame.getPixel(frame.width / 2, frame.height / 2)
    }

    private fun publish(collection: Uri, name: String, mime: String, write: (java.io.OutputStream) -> Unit): Uri {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = requireNotNull(resolver.insert(collection, values))
        try {
            resolver.openOutputStream(uri, "wt")!!.use(write)
            resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
            return uri
        } catch (e: Throwable) {
            resolver.delete(uri, null, null)
            throw e
        }
    }

    @Test fun reverseControlChangesVisiblePixelsAndPlaysToOriginalFirstFrame() {
        val colors = listOf(Color.RED, Color.GREEN, Color.BLUE)
        val uri = publish(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "gz-three-colors.gif", "image/gif") { out ->
            val photos = GifDocument(frames = colors.indices.map { "content://photo/$it" },
                delayMs = 350, size = GifSize.SMALL, forever = false)
            GifEncoder.write(out, photos) { i ->
                Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888).apply { eraseColor(colors[i]) }
            }
        }
        try {
            val original = VideoGifSource.document(context, uri)
            assertEquals(3, GifVideoMath.frameCount(original.video!!))
            var latest = original
            show(original) { latest = it }
            waitFrame(1)
            assertTrue(Color.red(centerColor()) > 200)
            rule.onNodeWithText("Reverse").performClick()
            rule.waitUntil(15_000) {
                Color.blue(centerColor()) > 200
            }
            rule.runOnIdle { assertTrue(latest.video!!.reverse) }
            rule.onNodeWithText("Play").performClick()
            waitFrame(3)
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Play").fetchSemanticsNodes().isNotEmpty()
            }
            assertTrue(Color.red(centerColor()) > 200)
            rule.onNodeWithText("Undo").performClick()
            rule.runOnIdle { assertFalse(latest.video!!.reverse) }
            rule.onNodeWithText("Redo").performClick()
            rule.runOnIdle { assertTrue(latest.video!!.reverse) }

            rule.onNodeWithText("Looks").performScrollTo().performClick()
            rule.onNodeWithText("B&W").performScrollTo().performClick()
            rule.waitUntil(15_000) {
                val gray = centerColor()
                kotlin.math.abs(Color.red(gray) - Color.blue(gray)) < 8 &&
                    kotlin.math.abs(Color.green(gray) - Color.blue(gray)) < 8
            }
            rule.runOnIdle { assertEquals(-1f, latest.edits.grade.saturation, .001f) }
            rule.onNodeWithText("Caption").performScrollTo().performClick()
            rule.onNodeWithText("Add caption").performClick()
            rule.onNode(hasSetTextAction()).performTextInput("Backwards")
            rule.onNodeWithText("Apply").performClick()
            rule.runOnIdle { assertEquals("Backwards", latest.edits.creative.text.single().content) }
        } finally { resolver.delete(uri, null, null) }
    }

    @Test fun mp4PlaysPausesAndSeeksThroughActualEditorControls() {
        val uri = publish(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "gz-editor-preview.mp4", "video/mp4") { out ->
            instrument.context.assets.open("gif_sample_2sec_h264.mp4").use { it.copyTo(out) }
        }
        try {
            val doc = VideoGifSource.document(context, uri)
            show(doc)
            waitFrame(1)
            rule.onNodeWithText("Play").performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithContentDescription("GIF preview, frame 1").fetchSemanticsNodes().isEmpty()
            }
            rule.onNodeWithText("Pause").performClick()
            rule.onNodeWithText("Play").assertIsDisplayed()
            rule.onNodeWithContentDescription("Scrub GIF frames").performSemanticsAction(
                androidx.compose.ui.semantics.SemanticsActions.SetProgress
            ) { it((GifVideoMath.frameCount(doc.video!!) - 1).toFloat()) }
            waitFrame(GifVideoMath.frameCount(doc.video!!))
        } finally { resolver.delete(uri, null, null) }
    }
}

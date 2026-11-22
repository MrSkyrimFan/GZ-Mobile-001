package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.Intent
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Movie
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import android.provider.MediaStore
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsProperties
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

    private fun show(initial: GifDocument, destination: Uri? = null, changed: (GifDocument) -> Unit = {}) {
        val owner = object : ActivityResultRegistryOwner {
            override val activityResultRegistry = object : ActivityResultRegistry() {
                override fun <I, O> onLaunch(requestCode: Int, contract: ActivityResultContract<I, O>,
                    input: I, options: ActivityOptionsCompat?) {
                    check(contract is ActivityResultContracts.CreateDocument && destination != null)
                    dispatchResult(requestCode, Activity.RESULT_OK, Intent().setData(destination))
                }
            }
        }
        rule.setContent {
            var doc by remember { mutableStateOf(initial) }
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides owner) {
                VideoGifScreen(doc, { doc = it; changed(it) }, {}, {}, {})
            }
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
        val destination = publish(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "gz-reverse-output.gif", "image/gif") {}
        try {
            val original = VideoGifSource.document(context, uri)
            assertEquals(3, GifVideoMath.frameCount(original.video!!))
            var latest = original
            show(original, destination) { latest = it }
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
            rule.onNodeWithText("Save GIF").performClick()
            rule.waitUntil(25_000) {
                rule.onAllNodesWithText("Edit saved GIF").fetchSemanticsNodes().isNotEmpty()
            }
            // Verify the actual SAF output before attempting to reopen it.
            // This catches fake success messages and malformed GIF bytes.
            val stored = requireNotNull(resolver.openInputStream(destination)).use {
                it.readBytes()
            }
            assertEquals("GIF89a", String(stored, 0, 6, Charsets.US_ASCII))
            val movie = requireNotNull(Movie.decodeByteArray(stored, 0, stored.size))
            assertTrue(movie.duration() > 0)
            assertEquals(3, GifVideoMath.frameCount(
                requireNotNull(VideoGifSource.document(context, destination).video)))
            // Reopen the bytes written by the actual Save GIF action.
            rule.onNodeWithText("Edit saved GIF").performClick()
            rule.waitUntil(15_000) { latest.video?.uri == destination.toString() }
            rule.runOnIdle {
                assertEquals(EditDocument(), latest.edits)
                assertEquals(3, GifVideoMath.frameCount(latest.video!!))
                assertEquals(original.video!!.durationMs, latest.video!!.durationMs)
            }
        } finally {
            resolver.delete(destination, null, null)
            resolver.delete(uri, null, null)
        }
    }

    @Test fun sixtyFpsBFramesUseContinuousSampledPlaybackAndExportTwoSeconds() {
        val uri = publish(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "gz-phone-60fps.mp4", "video/mp4") { out ->
            instrument.context.assets.open("gif_phone_60fps_bframes.mp4").use { it.copyTo(out) }
        }
        try {
            val doc = VideoGifSource.document(context, uri)
            val clip = requireNotNull(doc.video)
            assertEquals(720 to 1280, clip.width to clip.height)
            assertEquals(120, clip.sourceTimesMs.size)
            val times = GifVideoMath.frameTimesMs(clip)
            assertEquals(80, times.size)
            show(doc)
            waitFrame(1)
            rule.onNodeWithText("Play").performClick()
            val tag = "gif-scheduled-video"
            rule.waitUntil(15_000) {
                val node = rule.onAllNodesWithTag(tag).fetchSemanticsNodes().singleOrNull()
                val label = if (node != null && SemanticsProperties.ContentDescription in node.config)
                    node.config[SemanticsProperties.ContentDescription].singleOrNull() else null
                (label?.substringAfterLast(' ')?.toIntOrNull() ?: 0) >= 5
            }
            // These patches remain cyan and blue through the moving test pattern.
            // Their positions come from independently decoding the rotated MP4 fixture.
            val visible = rule.onNodeWithTag(tag).captureToImage().asAndroidBitmap()
            assertTrue(visible.height > visible.width)
            val cyan = visible.getPixel(visible.width / 5, visible.height / 10)
            val blue = visible.getPixel(visible.width / 5, visible.height * 45 / 100)
            assertTrue("rotated cyan patch = $cyan", Color.red(cyan) < 80 && Color.green(cyan) > 170 && Color.blue(cyan) > 170)
            assertTrue("rotated blue patch = $blue", Color.red(blue) < 80 && Color.green(blue) < 80 && Color.blue(blue) > 170)
            rule.waitUntil(15_000) { rule.onAllNodesWithText("Play").fetchSemanticsNodes().isNotEmpty() }
            waitFrame(times.size)

            val output = java.io.ByteArrayOutputStream()
            VideoGifSource.write(context, doc.copy(size = GifSize.SMALL), output)
            val encoded = GifSourceMetadata.read(output.toByteArray().inputStream(), "content://test/output")
            assertEquals(2000L, encoded.durationMs)
            assertEquals(80, encoded.sourceTimesMs.size)
            assertEquals(180 to 320, encoded.width to encoded.height)
            assertEquals(GifVideoMath.frameDelaysMs(clip), GifVideoMath.frameDelaysMs(encoded))
        } finally { resolver.delete(uri, null, null) }
    }

    @Test fun longerAudioTrackDoesNotExtendVideoExportOrPlayback() {
        // The fixture contains 16 video frames at 8 fps and a 5-second silent AAC track.
        val uri = publish(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "gz-audio-tail.mp4", "video/mp4") { out ->
            instrument.context.assets.open("gif_video_with_audio_tail.mp4").use { it.copyTo(out) }
        }
        try {
            val doc = VideoGifSource.document(context, uri)
            val clip = requireNotNull(doc.video)
            assertEquals(2000L, clip.durationMs)
            val output = java.io.ByteArrayOutputStream()
            VideoGifSource.write(context, doc, output)
            val gif = GifSourceMetadata.read(output.toByteArray().inputStream(), "content://test/output")
            assertEquals(2000L, gif.durationMs)
            assertEquals(16, gif.sourceTimesMs.size)

            // Reopen old projects that persisted the container end, including explicit trims.
            val name = "Audio tail migration"
            val key = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(name.toByteArray())
            val folder = java.io.File(context.filesDir, "gz_gif_projects").apply { mkdirs() }
            val file = java.io.File(folder, "$key.gzgif")
            try {
                for ((start, end, expectedEnd) in listOf(Triple(0L, 5000L, 2000L),
                    Triple(250L, 1250L, 1250L), Triple(3000L, 4000L, 4000L))) {
                    val old = doc.copy(video = clip.copy(durationMs = 5000, startMs = start, endMs = end,
                        speedPercent = 50, reverse = true))
                    file.writeText(org.json.JSONObject(GifStore.encode(old)).put("version", 3).toString())
                    val restored = requireNotNull(GifStore.load(context, name)?.video)
                    assertEquals(start, restored.startMs)
                    assertEquals(expectedEnd, restored.endMs)
                    assertEquals(50, restored.speedPercent)
                    assertTrue(restored.reverse)
                }
            } finally { file.delete() }

            show(doc)
            waitFrame(1)
            rule.onNodeWithText("Play").performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Pause").fetchSemanticsNodes().isNotEmpty() &&
                    rule.onAllNodes(hasTestTag("gif-frame") and
                        !hasContentDescription("GIF preview, frame 1")).fetchSemanticsNodes().isNotEmpty()
            }
            rule.waitUntil(15_000) { rule.onAllNodesWithText("Play").fetchSemanticsNodes().isNotEmpty() }
            waitFrame(16)
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
            val startedAt = SystemClock.elapsedRealtime()
            rule.onNodeWithText("Play").performClick()
            rule.waitUntil(15_000) {
                val native = rule.onAllNodesWithTag("gif-native-video").fetchSemanticsNodes().singleOrNull()
                val label = if (native != null && SemanticsProperties.ContentDescription in native.config)
                    native.config[SemanticsProperties.ContentDescription].singleOrNull() else null
                val frame = label?.substringAfterLast(' ')?.toIntOrNull() ?: 0
                frame >= 5
            }
            Log.i("GzGifTiming", "2 s, 8 fps MP4: Play to frame 5 or later = ${SystemClock.elapsedRealtime() - startedAt} ms")
            rule.onNodeWithText("Pause").performClick()
            rule.onNodeWithText("Play").assertIsDisplayed()
            rule.onNodeWithContentDescription("Scrub GIF frames").performSemanticsAction(
                androidx.compose.ui.semantics.SemanticsActions.SetProgress
            ) { it((GifVideoMath.frameCount(doc.video!!) - 1).toFloat()) }
            waitFrame(GifVideoMath.frameCount(doc.video!!))
        } finally { resolver.delete(uri, null, null) }
    }
}

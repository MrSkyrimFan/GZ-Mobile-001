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

    private fun taggedFrame(tag: String): Int {
        val node = rule.onAllNodesWithTag(tag).fetchSemanticsNodes().singleOrNull() ?: return 0
        val label = if (SemanticsProperties.ContentDescription in node.config)
            node.config[SemanticsProperties.ContentDescription].singleOrNull() else null
        return label?.substringAfterLast(' ')?.toIntOrNull() ?: 0
    }

    private fun waitTaggedFrameAtLeast(tag: String, frame: Int) {
        rule.waitUntil(15_000) { taggedFrame(tag) >= frame }
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

    @Test fun sixtyFpsBFramesAdvanceThroughScheduledOrMeasuredFallbackAndExportTwoSeconds() {
        val uri = publish(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "gz-phone-60fps.mp4", "video/mp4") { out ->
            instrument.context.assets.open("gif_phone_60fps_bframes.mp4").use { it.copyTo(out) }
        }
        try {
            val startingSequence = GifPreviewDiagnostics.snapshot().lastOrNull()?.sequence ?: 0L
            val doc = VideoGifSource.document(context, uri)
            val clip = requireNotNull(doc.video)
            assertEquals(720 to 1280, clip.width to clip.height)
            assertEquals(120, clip.sourceTimesMs.size)
            val times = GifVideoMath.frameTimesMs(clip)
            assertEquals(80, times.size)
            show(doc)
            waitFrame(1)
            var operationId = 0L
            rule.runOnIdle {
                operationId = requireNotNull(GifPreviewDiagnostics.snapshot().lastOrNull {
                    it.sequence > startingSequence &&
                        it.kind == GifDiagnosticKind.OPERATION_STARTED
                }).operationId
            }

            val scheduledTag = "gif-scheduled-video"
            val bitmapTag = "gif-frame"
            rule.onNodeWithText("Play").performClick()
            rule.waitUntil(15_000) {
                taggedFrame(scheduledTag) >= 5 || taggedFrame(bitmapTag) >= 5
            }

            val pausedForCapture =
                rule.onAllNodesWithText("Pause").fetchSemanticsNodes().isNotEmpty()
            if (pausedForCapture) rule.onNodeWithText("Pause").performClick()
            rule.waitUntil(15_000) { taggedFrame(bitmapTag) >= 5 }

            // These patches remain cyan and blue through the moving test pattern.
            // Their positions come from independently decoding the rotated MP4 fixture.
            val visible = rule.onNodeWithTag(bitmapTag).captureToImage().asAndroidBitmap()
            fun verifyPortraitPatches(image: Bitmap) {
                assertTrue(image.height > image.width)
                // captureToImage() includes the whole ContentScale.Fit node, including
                // any letterbox gutters. Map fixture coordinates into the centered 9:16
                // content rectangle so different API insets cannot turn this into a UI
                // background-color assertion.
                val contentWidth = minOf(image.width, image.height * 9 / 16)
                val contentHeight = minOf(image.height, image.width * 16 / 9)
                val left = (image.width - contentWidth) / 2
                val top = (image.height - contentHeight) / 2
                val cyan = image.getPixel(left + contentWidth / 5, top + contentHeight / 10)
                val blue = image.getPixel(left + contentWidth / 5, top + contentHeight * 45 / 100)
                assertTrue("rotated cyan patch = $cyan", Color.red(cyan) < 80 && Color.green(cyan) > 170 && Color.blue(cyan) > 170)
                assertTrue("rotated blue patch = $blue", Color.red(blue) < 80 && Color.green(blue) < 80 && Color.blue(blue) > 170)
            }
            verifyPortraitPatches(visible)

            rule.runOnIdle {
                val events = GifPreviewDiagnostics.snapshot().filter {
                    it.sequence > startingSequence && it.operationId == operationId
                }
                assertTrue(events.any {
                    it.kind == GifDiagnosticKind.PREVIEW_ROUTE_SELECTED &&
                        it.route == GifPreviewRoute.SCHEDULED_CODEC
                })
                assertFalse(events.any { it.kind == GifDiagnosticKind.PREVIEW_FAILED })
                val scheduledAdvanced = events.any {
                    it.kind == GifDiagnosticKind.FRAME_SETTLED &&
                        it.route == GifPreviewRoute.SCHEDULED_CODEC &&
                        (it.frameIndex ?: -1) >= 4
                }
                val fallbackStarted = events.any {
                    it.kind == GifDiagnosticKind.FALLBACK_STARTED &&
                        it.route == GifPreviewRoute.SCHEDULED_CODEC
                }
                val bitmapAdvanced = events.any {
                    it.kind == GifDiagnosticKind.FRAME_SETTLED &&
                        it.route == GifPreviewRoute.BITMAP_RETRIEVER &&
                        (it.frameIndex ?: -1) >= 4
                }
                assertTrue("Preview neither advanced on the scheduled route nor completed its measured fallback",
                    scheduledAdvanced || fallbackStarted && bitmapAdvanced)
                if (fallbackStarted) {
                    assertTrue(events.any {
                        it.kind == GifDiagnosticKind.PREVIEW_ROUTE_SELECTED &&
                            it.route == GifPreviewRoute.BITMAP_RETRIEVER
                    })
                    assertTrue(bitmapAdvanced)
                }
            }

            if (pausedForCapture) rule.onNodeWithText("Play").performClick()
            rule.waitUntil(15_000) { rule.onAllNodesWithText("Play").fetchSemanticsNodes().isNotEmpty() }
            waitFrame(times.size)

            val small = doc.copy(size = GifSize.SMALL)
            val baseline = java.io.ByteArrayOutputStream()
            val baselineStart = SystemClock.elapsedRealtime()
            VideoGifSource.writeVideoFrames(context, small, baseline, allowScaledBatches = false)
            val baselineMs = SystemClock.elapsedRealtime() - baselineStart
            val output = java.io.ByteArrayOutputStream()
            val optimizedStart = SystemClock.elapsedRealtime()
            VideoGifSource.write(context, small, output)
            val optimizedMs = SystemClock.elapsedRealtime() - optimizedStart
            Log.i("GzGifExportPerf", "720p60 long GOP to 180x320, 80 frames: timestamp=${baselineMs}ms batched=${optimizedMs}ms")
            val encoded = GifSourceMetadata.read(output.toByteArray().inputStream(), "content://test/output")
            val reference = GifSourceMetadata.read(baseline.toByteArray().inputStream(), encoded.uri)
            assertEquals(reference, encoded)
            assertEquals(2000L, encoded.durationMs)
            assertEquals(80, encoded.sourceTimesMs.size)
            assertEquals(180 to 320, encoded.width to encoded.height)
            assertEquals(GifVideoMath.frameDelaysMs(clip), GifVideoMath.frameDelaysMs(encoded))
            val bytes = output.toByteArray()
            val movie = requireNotNull(Movie.decodeByteArray(bytes, 0, bytes.size))
            val final = Bitmap.createBitmap(encoded.width, encoded.height, Bitmap.Config.ARGB_8888)
            try {
                movie.setTime(movie.duration() - 5)
                movie.draw(android.graphics.Canvas(final), 0f, 0f)
                verifyPortraitPatches(final)
                val original = baseline.toByteArray()
                val expected = requireNotNull(Movie.decodeByteArray(original, 0, original.size))
                compareDecodedMotion(expected, movie, encoded.sourceTimesMs, GifVideoMath.frameDelaysMs(clip))
            } finally { final.recycle() }
        } finally { resolver.delete(uri, null, null) }
    }

    private fun compareDecodedMotion(expected: Movie, actual: Movie, starts: List<Long>, delays: List<Int>) {
        val a = Bitmap.createBitmap(expected.width(), expected.height(), Bitmap.Config.ARGB_8888)
        val b = Bitmap.createBitmap(actual.width(), actual.height(), Bitmap.Config.ARGB_8888)
        val previous = IntArray(24 * 32)
        var movingSamples = 0
        var movingError = 0L
        fun error(left: Int, right: Int): Int =
            kotlin.math.abs(Color.red(left) - Color.red(right)) +
                kotlin.math.abs(Color.green(left) - Color.green(right)) +
                kotlin.math.abs(Color.blue(left) - Color.blue(right))
        try {
            starts.forEachIndexed { index, start ->
                val t = start.toInt() + delays[index] / 2
                expected.setTime(t)
                actual.setTime(t)
                expected.draw(android.graphics.Canvas(a), 0f, 0f)
                actual.draw(android.graphics.Canvas(b), 0f, 0f)
                var frameError = 0
                for (row in 0 until 32) for (column in 0 until 24) {
                    val x = (2 * column + 1) * a.width / 48
                    val y = (2 * row + 1) * a.height / 64
                    val color = a.getPixel(x, y)
                    val delta = error(color, b.getPixel(x, y))
                    frameError += delta
                    val sample = row * 24 + column
                    if (index > 0 && error(color, previous[sample]) >= 90) {
                        movingSamples++
                        movingError += delta
                    }
                    previous[sample] = color
                }
                assertTrue("Decoded frame $index differs from timestamp export: $frameError",
                    frameError < previous.size * 3 * 10)
            }
            assertTrue("The fixture must exercise moving pixels", movingSamples > 200)
            assertTrue("Color error at $movingSamples moving samples was $movingError",
                movingError < movingSamples * 3L * 18)
        } finally { a.recycle(); b.recycle() }
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
    @Test fun portraitVideoPlaysAgainAfterTrimAndRapidScrub() {
        val uri = publish(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            "gz-portrait-motion.mp4", "video/mp4") { out ->
            instrument.context.assets.open("gif_portrait_motion_fixture.mp4").use { it.copyTo(out) }
        }
        try {
            val source = VideoGifSource.document(context, uri)
            val clip = requireNotNull(source.video)
            assertEquals(1080 to 1920, clip.width to clip.height)
            assertEquals(150, clip.sourceTimesMs.size)
            var latest = source
            show(source) { latest = it }
            waitFrame(1)
            rule.onNodeWithText("Play").performClick()
            rule.waitUntil(15_000) {
                taggedFrame("gif-native-video") >= 12 || taggedFrame("gif-scheduled-video") >= 12
            }
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Play").fetchSemanticsNodes().isNotEmpty()
            }
            waitFrame(GifVideoMath.frameCount(clip))
            rule.onNodeWithContentDescription("Trim start and end").performScrollTo().performTouchInput {
                swipe(androidx.compose.ui.geometry.Offset(width * .04f, center.y),
                    androidx.compose.ui.geometry.Offset(width * .44f, center.y), 450)
            }
            rule.runOnIdle {
                val trimmed = requireNotNull(latest.video)
                assertTrue("Trim thumb did not move to the intended range: ${trimmed.startMs}",
                    trimmed.startMs in 1_700L..2_600L)
                assertEquals(clip.endMs, trimmed.endMs)
            }
            rule.onNodeWithText("Reverse").performClick()
            rule.onNodeWithText("Looks").performScrollTo().performClick()
            rule.onNodeWithText("B&W").performScrollTo().performClick()
            rule.onNodeWithText("Timeline").performScrollTo().performClick()
            val scrub = rule.onNodeWithContentDescription("Scrub GIF frames").performScrollTo()
            scrub.performTouchInput {
                swipe(androidx.compose.ui.geometry.Offset(width * .06f, center.y),
                    androidx.compose.ui.geometry.Offset(width * .77f, center.y), 260)
                swipe(androidx.compose.ui.geometry.Offset(width * .77f, center.y),
                    androidx.compose.ui.geometry.Offset(width * .06f, center.y), 260)
            }
            scrub.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.SetProgress) { it(0f) }
            waitFrame(1)
            rule.runOnIdle {
                assertTrue(requireNotNull(latest.video).reverse)
                assertEquals(-1f, latest.edits.grade.saturation, .001f)
            }
            fun sampleMotion(): List<Int> {
                val image = rule.onNodeWithTag("gif-frame").captureToImage().asAndroidBitmap()
                return (1..13).flatMap { row ->
                    (1..7).map { column ->
                        val x = image.width / 4 + image.width * column / 16
                        val y = image.height / 12 + image.height * row / 17
                        Color.red(image.getPixel(x, y)) / 24
                    }
                }
            }
            val samples = mutableSetOf(sampleMotion())
            val playAt = SystemClock.elapsedRealtime()
            rule.onNodeWithText("Play").performClick()
            try {
                rule.waitUntil(6_000) {
                    if (taggedFrame("gif-frame") > 0) samples.add(sampleMotion())
                    samples.size >= 5
                }
            } finally {
                Log.i("GzGifTiming", "Trim + reverse + B&W, 1080x1920: ${samples.size} distinct visible samples in ${SystemClock.elapsedRealtime() - playAt}ms")
            }
        } finally {
            resolver.delete(uri, null, null)
        }
    }

    @Test fun editedVideoKeepsAdvancingAcrossReverseLookAndSpeedChanges() {
        val uri = publish(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            "gz-edited-preview.mp4", "video/mp4") { out ->
            instrument.context.assets.open("gif_sample_2sec_h264.mp4").use { it.copyTo(out) }
        }
        try {
            val startingSequence = GifPreviewDiagnostics.snapshot().lastOrNull()?.sequence ?: 0L
            val doc = VideoGifSource.document(context, uri)
            show(doc)
            waitFrame(1)
            var operationId = 0L
            rule.runOnIdle {
                operationId = requireNotNull(GifPreviewDiagnostics.snapshot().lastOrNull {
                    it.sequence > startingSequence &&
                        it.kind == GifDiagnosticKind.OPERATION_STARTED
                }).operationId
            }

            rule.onNodeWithText("Play").performClick()
            waitTaggedFrameAtLeast("gif-native-video", 4)
            rule.onNodeWithText("Pause").performClick()
            rule.onNodeWithContentDescription("Scrub GIF frames").performSemanticsAction(
                androidx.compose.ui.semantics.SemanticsActions.SetProgress
            ) { it(0f) }

            rule.onNodeWithText("Reverse").performClick()
            waitFrame(1)
            rule.onNodeWithText("Play").performClick()
            waitTaggedFrameAtLeast("gif-frame", 4)
            rule.onNodeWithText("Pause").performClick()

            rule.onNodeWithText("Looks").performScrollTo().performClick()
            rule.onNodeWithText("B&W").performScrollTo().performClick()
            rule.onNodeWithText("Timeline").performScrollTo().performClick()
            rule.onNodeWithContentDescription("Scrub GIF frames").performSemanticsAction(
                androidx.compose.ui.semantics.SemanticsActions.SetProgress
            ) { it(0f) }
            rule.waitUntil(15_000) {
                val gray = centerColor()
                kotlin.math.abs(Color.red(gray) - Color.green(gray)) < 12 &&
                    kotlin.math.abs(Color.green(gray) - Color.blue(gray)) < 12
            }
            rule.onNodeWithText("Play").performClick()
            waitTaggedFrameAtLeast("gif-frame", 4)
            rule.onNodeWithText("Pause").performClick()

            rule.onNodeWithText("Playback").performScrollTo().performClick()
            rule.onNodeWithText("200%").performScrollTo().performClick()
            rule.onNodeWithText("Timeline").performScrollTo().performClick()
            rule.onNodeWithContentDescription("Scrub GIF frames").performSemanticsAction(
                androidx.compose.ui.semantics.SemanticsActions.SetProgress
            ) { it(0f) }
            waitFrame(1)
            rule.onNodeWithText("Play").performClick()
            waitTaggedFrameAtLeast("gif-frame", 4)
            rule.onNodeWithText("Pause").performClick()

            rule.runOnIdle {
                val events = GifPreviewDiagnostics.snapshot().filter {
                    it.sequence > startingSequence && it.operationId == operationId
                }
                assertTrue(events.any {
                    it.kind == GifDiagnosticKind.PREVIEW_ROUTE_SELECTED &&
                        it.route == GifPreviewRoute.NATIVE_SOURCE
                })
                assertTrue(events.any {
                    it.kind == GifDiagnosticKind.PREVIEW_ROUTE_SELECTED &&
                        (it.route == GifPreviewRoute.BITMAP_CODEC || it.route == GifPreviewRoute.BITMAP_RETRIEVER)
                })
                val editedFrames = events.filter {
                    it.kind == GifDiagnosticKind.FRAME_SETTLED &&
                        (it.route == GifPreviewRoute.BITMAP_CODEC || it.route == GifPreviewRoute.BITMAP_RETRIEVER)
                }.mapNotNull { it.frameIndex }.distinct()
                assertTrue("Edited preview did not settle three distinct frames: $editedFrames",
                    editedFrames.size >= 3)
                assertFalse(events.any { it.kind == GifDiagnosticKind.PREVIEW_FAILED })
            }
        } finally {
            resolver.delete(uri, null, null)
        }
    }

}

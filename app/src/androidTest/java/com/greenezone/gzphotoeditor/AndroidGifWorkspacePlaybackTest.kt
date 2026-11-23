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

            // Playback can reach the end between a Pause query and a click.
            // These color patches are invariant, so capture the stable final still.
            rule.waitUntil(15_000) { rule.onAllNodesWithText("Play").fetchSemanticsNodes().isNotEmpty() }
            waitFrame(times.size)

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

            val small = doc.copy(size = GifSize.SMALL)
            val baseline = java.io.ByteArrayOutputStream()
            val baselineStart = SystemClock.elapsedRealtime()
            VideoGifSource.writeVideoFrames(context, small, baseline, allowScaledBatches = false)
            val baselineMs = SystemClock.elapsedRealtime() - baselineStart
            val output = java.io.ByteArrayOutputStream()
            val optimizedStart = SystemClock.elapsedRealtime()
            VideoGifSource.write(context, small, output)
            val optimizedMs = SystemClock.elapsedRealtime() - optimizedStart
            Log.i("GzGifExportPerf", "720p60 long GOP to 180x320, 80 frames: timestamp=${baselineMs}ms production=${optimizedMs}ms")
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
            assertTrue("Production export took ${optimizedMs}ms versus ${baselineMs}ms with per-frame seeks",
                optimizedMs * 2 <= baselineMs)
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
        val destination = publish(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "gz-portrait-edited.gif", "image/gif") {}
        try {
            val source = VideoGifSource.document(context, uri)
            val clip = requireNotNull(source.video)
            assertEquals(1080 to 1920, clip.width to clip.height)
            assertEquals(150, clip.sourceTimesMs.size)
            var latest = source
            show(source, destination) { latest = it }
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
            val reverseSequence = GifPreviewDiagnostics.snapshot().lastOrNull()?.sequence ?: 0L
            rule.onNodeWithText("Reverse").performClick()
            rule.onNodeWithText("Looks").performScrollTo().performClick()
            rule.onNodeWithText("B&W").performScrollTo().performClick()
            rule.onNodeWithText("Adjust").performScrollTo().performClick()
            rule.onNodeWithContentDescription("Adjust Exposure").performScrollTo().performTouchInput {
                swipe(center, center + androidx.compose.ui.geometry.Offset(width * .07f, 0f), 300)
            }
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
            val edited = rule.runOnIdle { latest }
            val reversed = requireNotNull(edited.video)
            assertTrue(reversed.reverse)
            assertEquals(-1f, edited.edits.grade.saturation, .001f)
            assertTrue("The Adjust slider did not apply exposure: ${edited.edits.grade.exposure}",
                edited.edits.grade.exposure in 0.1f..0.6f)

            // Evaluate the marker positions from the fixture's generating geometry,
            // independent of the reverse preview/export schedule.
            val forward = GifVideoMath.frameTimesMs(reversed.copy(reverse = false))
            val early = forward.first()
            val late = forward.last()
            fun markerAt(image: Bitmap, sourceMs: Long): Int {
                val width = minOf(image.width, image.height * 9 / 16)
                val height = minOf(image.height, image.width * 16 / 9)
                val x = (210.0 + 125.0 * sourceMs / 1_000) / 1080
                val y = (430.0 + 190.0 * sourceMs / 1_000) / 1920
                return image.getPixel((image.width - width) / 2 + (width * x).toInt(),
                    (image.height - height) / 2 + (height * y).toInt())
            }
            fun showsMarker(image: Bitmap, visibleAt: Long, clearAt: Long): Boolean {
                val marker = markerAt(image, visibleAt)
                val background = markerAt(image, clearAt)
                val channels = listOf(Color.red(marker), Color.green(marker), Color.blue(marker))
                return channels.min() > 175 && channels.max() - channels.min() <= 12 &&
                    Color.red(background) < 100
            }
            fun previewShowsMarker(visibleAt: Long, clearAt: Long): Boolean {
                val image = rule.onNodeWithTag("gif-frame").captureToImage().asAndroidBitmap()
                return try { showsMarker(image, visibleAt, clearAt) } finally { image.recycle() }
            }
            rule.waitUntil(15_000) { previewShowsMarker(late, early) }
            assertTrue("The paused reverse preview must prepare the first source frame before Play",
                GifPreviewDiagnostics.snapshot().any {
                    it.sequence > reverseSequence && it.kind == GifDiagnosticKind.FRAME_SETTLED &&
                        it.stage == GifDiagnosticStage.G4_SEEK && it.frameIndex == 0 &&
                        it.route == GifPreviewRoute.BITMAP_CODEC
                })

            fun sampleMotion(): List<Int> {
                val image = rule.onNodeWithTag("gif-frame").captureToImage().asAndroidBitmap()
                return try {
                    (1..13).flatMap { row ->
                        (1..7).map { column ->
                            val x = image.width / 4 + image.width * column / 16
                            val y = image.height / 12 + image.height * row / 17
                            Color.red(image.getPixel(x, y)) / 24
                        }
                    }
                } finally { image.recycle() }
            }
            val samples = mutableSetOf(sampleMotion())
            val startingSequence = GifPreviewDiagnostics.snapshot().lastOrNull()?.sequence ?: 0L
            val playStartedNanos = System.nanoTime()
            val playAt = SystemClock.elapsedRealtime()
            rule.onNodeWithText("Play").performClick()
            try {
                rule.waitUntil(6_000) {
                    if (taggedFrame("gif-frame") > 0) samples.add(sampleMotion())
                    samples.size >= 5
                }
            } finally {
                Log.i("GzGifTiming", "Trim + reverse + B&W, 1080x1920: ${samples.size} distinct visible samples in ${SystemClock.elapsedRealtime() - playAt}ms")
                val frames = GifPreviewDiagnostics.snapshot().filter {
                    it.sequence > startingSequence && it.stage == GifDiagnosticStage.G3_CONTINUOUS_PLAY &&
                        it.kind == GifDiagnosticKind.FRAME_SETTLED && it.route in setOf(
                            GifPreviewRoute.BITMAP_CODEC, GifPreviewRoute.BITMAP_RETRIEVER)
                }
                val timeline = frames.take(30).joinToString { frame ->
                    val elapsedMs = (frame.timestampNanos - playStartedNanos) / 1_000_000
                    val decodeMs = (frame.latencyNanos ?: 0L) / 1_000_000
                    "${frame.frameIndex}@${elapsedMs}ms/${decodeMs}ms"
                }
                Log.i("GzGifTiming", "Edited reverse decode (${frames.firstOrNull()?.route}): $timeline")
            }
            val count = GifVideoMath.frameCount(reversed)
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Play").fetchSemanticsNodes().isNotEmpty()
            }
            waitFrame(count)
            rule.waitUntil(15_000) { previewShowsMarker(early, late) }
            val playbackEnded = GifPreviewDiagnostics.snapshot().any {
                it.sequence > startingSequence && it.kind == GifDiagnosticKind.PLAYBACK_ENDED &&
                    it.frameIndex == count - 1 && it.route in setOf(
                        GifPreviewRoute.BITMAP_CODEC, GifPreviewRoute.BITMAP_RETRIEVER)
            }
            assertTrue("Edited reverse playback did not reach the original trim start", playbackEnded)

            rule.onNodeWithText("Canvas").performScrollTo().performClick()
            rule.onNodeWithText(GifSize.SMALL.label).performScrollTo().performClick()
            val exportSequence = GifPreviewDiagnostics.snapshot().lastOrNull()?.sequence ?: 0L
            val exportAt = SystemClock.elapsedRealtime()
            rule.onNodeWithText("Save GIF").performClick()
            rule.waitUntil(120_000) {
                rule.onAllNodesWithText("Edit saved GIF").fetchSemanticsNodes().isNotEmpty()
            }
            val exportMs = SystemClock.elapsedRealtime() - exportAt
            val exportEvents = GifPreviewDiagnostics.snapshot().filter { it.sequence > exportSequence }
            val started = requireNotNull(exportEvents.firstOrNull { it.kind == GifDiagnosticKind.EXPORT_STARTED })
            val staged = requireNotNull(exportEvents.firstOrNull { it.kind == GifDiagnosticKind.EXPORT_STAGED })
            assertTrue(exportEvents.any { it.kind == GifDiagnosticKind.EXPORT_COMPLETED })
            val stagedMs = (staged.timestampNanos - started.timestampNanos) / 1_000_000
            val bytes = requireNotNull(resolver.openInputStream(destination)).use { it.readBytes() }
            val exported = GifSourceMetadata.read(bytes.inputStream(), destination.toString())
            assertEquals(180 to 320, exported.width to exported.height)
            assertEquals(count, exported.sourceTimesMs.size)
            val expectedDelays = GifVideoMath.frameDelaysMs(reversed)
            assertEquals(expectedDelays.sum().toLong(), exported.durationMs)
            assertEquals(expectedDelays, GifVideoMath.frameDelaysMs(exported))
            Log.i("GzGifTiming", "Edited export: ${exported.width}x${exported.height}, $count frames, ${exported.durationMs}ms duration, saved in ${exportMs}ms, staged in ${stagedMs}ms")
            val movie = requireNotNull(Movie.decodeByteArray(bytes, 0, bytes.size))
            val pixels = Bitmap.createBitmap(exported.width, exported.height, Bitmap.Config.ARGB_8888)
            try {
                val canvas = android.graphics.Canvas(pixels)
                movie.setTime(0)
                movie.draw(canvas, 0f, 0f)
                assertTrue("Saved GIF must begin at the late, B&W marker",
                    showsMarker(pixels, late, early))
                movie.setTime(movie.duration() - 5)
                movie.draw(canvas, 0f, 0f)
                assertTrue("Saved GIF must end at the early, B&W marker",
                    showsMarker(pixels, early, late))
            } finally { pixels.recycle() }
            rule.onNodeWithText("Edit saved GIF").performClick()
            rule.waitUntil(15_000) { latest.video?.uri == destination.toString() }
            rule.runOnIdle {
                assertTrue(requireNotNull(latest.video).isGif)
                assertEquals(count, GifVideoMath.frameCount(requireNotNull(latest.video)))
            }
            waitFrame(1)
            rule.waitUntil(15_000) { previewShowsMarker(late, early) }
            rule.onNodeWithText("Play").performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Play").fetchSemanticsNodes().isNotEmpty() &&
                    taggedFrame("gif-frame") == count
            }
            rule.waitUntil(15_000) { previewShowsMarker(early, late) }
            assertTrue("Saving the edited portrait GIF took ${exportMs}ms",
                exportMs <= 15_000L)
        } finally {
            resolver.delete(destination, null, null)
            resolver.delete(uri, null, null)
        }
    }

    @Test fun editedBitmapCodecMatchesRetrieverOnMetadataRotatedVideo() {
        val uri = publish(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "gz-edited-rotated.mp4", "video/mp4") { out ->
            instrument.context.assets.open("gif_phone_60fps_bframes.mp4").use { it.copyTo(out) }
        }
        try {
            val source = requireNotNull(VideoGifSource.document(context, uri).video)
            assertEquals(720 to 1280, source.width to source.height)
            val edited = source.copy(startMs = source.sourceTimesMs[10],
                endMs = source.sourceTimesMs[95], reverse = true)
            val at = GifVideoMath.frameTimesMs(edited).first()
            val actual = EditedVideoPreviewReader.open(context, edited) {}.use { it.frame(at, 480) }
            try {
                val expected = VideoGifSource.open(context, edited).use { it.frame(at, 480) }
                try {
                    assertEquals(270 to 480, actual.width to actual.height)
                    assertEquals(expected.width to expected.height, actual.width to actual.height)
                    fun rgb(color: Int) = listOf(Color.red(color), Color.green(color), Color.blue(color))
                    for (y in listOf(actual.height / 10, actual.height * 45 / 100)) {
                        val x = actual.width / 5
                        val delta = rgb(actual.getPixel(x, y)).zip(rgb(expected.getPixel(x, y)))
                            .sumOf { (a, b) -> kotlin.math.abs(a - b) }
                        assertTrue("Edited codec color/orientation differs at ($x, $y): $delta", delta < 60)
                    }
                    val cyan = actual.getPixel(actual.width / 5, actual.height / 10)
                    assertTrue(Color.red(cyan) < 80 && Color.green(cyan) > 170 && Color.blue(cyan) > 170)
                    val blue = actual.getPixel(actual.width / 5, actual.height * 45 / 100)
                    assertTrue(Color.red(blue) < 80 && Color.green(blue) < 80 && Color.blue(blue) > 170)
                } finally { expected.recycle() }
            } finally { actual.recycle() }

            val shortReverse = edited.copy(startMs = source.sourceTimesMs[75])
            val reverseTimes = GifVideoMath.frameTimesMs(shortReverse)
            val window = EditedVideoPreviewReader.ReversePreviewWindow()
            val paused = EditedVideoPreviewReader.open(context, shortReverse, reversePreviewWindow = window) {}
                .use { it.frame(reverseTimes[0], 480) }
            try {
                assertTrue("The prepared first frame must survive reader closure",
                    window.contains(source.sourceTimesMs.indexOf(reverseTimes[0])))
                assertTrue("The next reverse frame must also be prepared",
                    window.contains(source.sourceTimesMs.indexOf(reverseTimes[1])))
                EditedVideoPreviewReader.open(context, shortReverse, prefetchReverseFrames = true,
                    reversePreviewWindow = window) {}.use { reader ->
                    val resumed = reader.frame(reverseTimes[0], 480)
                    try {
                        assertTrue("The prepared first frame must preserve the paused pixels", paused.sameAs(resumed))
                    } finally { resumed.recycle() }
                }
            } finally { paused.recycle() }
        } finally { resolver.delete(uri, null, null) }
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

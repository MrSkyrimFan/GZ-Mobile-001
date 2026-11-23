package com.greenezone.gzphotoeditor

import android.content.ContentValues
import android.graphics.Movie
import android.graphics.Bitmap
import android.graphics.Canvas
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream

/**
 * Genuine H.264/MP4 32x24, 16-frame, 2-second synthetic fixture.
 * Built from FFmpeg testsrc (not a bundled media decoder or third-party runtime library).
 * Validates Android's actual MediaMetadataRetriever media URI path.
 */
@RunWith(AndroidJUnit4::class)
class AndroidVideoGifDecodeTest {
    @Test fun actualNativeVideoUriDecodesTrimsAndExportsAnimatedGif() {
        val instrument = InstrumentationRegistry.getInstrumentation()
        val context = instrument.targetContext
        val resolver = context.contentResolver
        val input = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, "GZ_video_gif_instrumentation_test.mp4")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, input)
        assertNotNull("Android media store must accept the owned test file", uri)
        try {
            instrument.context.assets.open("gif_sample_2sec_h264.mp4").use { asset ->
                resolver.openOutputStream(uri!!, "wt")!!.use { out ->
                    asset.copyTo(out)
                }
            }
            resolver.update(uri!!, ContentValues().apply {
                put(MediaStore.Video.Media.IS_PENDING, 0)
            }, null, null)
            val untouched = VideoGifSource.document(context, uri)
            val clip = requireNotNull(untouched.video)
            assertTrue(GifVideoMath.valid(clip))
            assertTrue("Fixture duration must match actual video", clip.durationMs in 1800L..2200L)
            assertEquals(32 to 24, GifMath.dimensions(untouched))
            assertEquals(clip.durationMs, clip.endMs)
            assertEquals(16, GifVideoMath.frameCount(clip))
            assertEquals(-1, untouched.loopCount)
            val legacy = untouched.copy(size = GifSize.SMALL, aspect = GifAspect.SQUARE,
                video = clip.copy(startMs = 250, endMs = 1500, fps = 8, speedPercent = 50, reverse = true))
            val name = "Legacy GIF migration"
            // Write a v2 record with precisely the metadata fields that release saved.
            val json = org.json.JSONObject(GifStore.encode(legacy)).apply {
                put("version", 2)
                val old = getJSONObject("video")
                listOf("width", "height", "times", "isGif", "loopCount").forEach { old.remove(it) }
            }
            val key = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(name.toByteArray())
            val folder = java.io.File(context.filesDir, "gz_gif_projects").apply { mkdirs() }
            val projectFile = java.io.File(folder, "$key.gzgif")
            try {
                projectFile.writeText(json.toString())
                assertEquals(legacy, GifStore.load(context, name))
            } finally { projectFile.delete() }
            val times = GifVideoMath.frameTimesMs(clip)
            val holds = GifVideoMath.frameDelaysMs(clip)
            val forwardBytes = ByteArrayOutputStream().also {
                VideoGifSource.write(context, untouched, it)
            }.toByteArray()
            val reverseBytes = ByteArrayOutputStream().also {
                VideoGifSource.write(context, untouched.copy(video = clip.copy(reverse = true)), it)
            }.toByteArray()
            val forward = requireNotNull(Movie.decodeByteArray(forwardBytes, 0, forwardBytes.size))
            val reverse = requireNotNull(Movie.decodeByteArray(reverseBytes, 0, reverseBytes.size))
            assertEquals(clip.durationMs.toInt(), forward.duration())
            assertEquals(forward.duration(), reverse.duration())
            val a = Bitmap.createBitmap(32, 24, Bitmap.Config.ARGB_8888)
            val b = Bitmap.createBitmap(32, 24, Bitmap.Config.ARGB_8888)
            try {
                var elapsed = 0
                VideoGifSource.open(context, clip).use { reader ->
                    for (i in times.indices) {
                        val midpoint = elapsed + holds[i] / 2
                        forward.setTime(midpoint)
                        forward.draw(Canvas(a), 0f, 0f)
                        reverse.setTime(forward.duration() - midpoint)
                        reverse.draw(Canvas(b), 0f, 0f)
                        assertTrue("Reverse must match forward frame $i exactly", a.sameAs(b))
                        val source = reader.frame(times[i], 32)
                        try {
                            var error = 0L
                            for (y in 0 until 24) for (x in 0 until 32) {
                                val src = source.getPixel(x, y)
                                val dst = a.getPixel(x, y)
                                for (shift in listOf(0, 8, 16)) {
                                    error += kotlin.math.abs((src ushr shift and 255) - (dst ushr shift and 255))
                                }
                            }
                            assertTrue("Color error in frame $i was $error", error < 32 * 24 * 3 * 9)
                        } finally { source.recycle() }
                        elapsed += holds[i]
                    }
                }
            } finally { a.recycle(); b.recycle() }
            val frame = VideoGifSource.preview(context, clip, 750L, 320)
            try {
                assertTrue(frame.width > 0)
                assertTrue(frame.height > 0)
            } finally { frame.recycle() }
            val early = VideoGifSource.preview(context, clip, 150L, 320)
            val late = VideoGifSource.preview(context, clip, 1_350L, 320)
            try {
                var changed = 0
                val w = minOf(early.width, late.width)
                val h = minOf(early.height, late.height)
                for (y in 0 until h step 4) {
                    for (x in 0 until w step 4) {
                        if (early.getPixel(x,y) != late.getPixel(x,y)) changed++
                    }
                }
                assertTrue("Video must supply genuinely different frames", changed > 5)
            } finally {
                early.recycle()
                late.recycle()
            }
            val doc = GifDocument(video = clip.copy(fps = 4), size = GifSize.SMALL)
            val out = ByteArrayOutputStream()
            var progress = 0
            VideoGifSource.write(context, doc, out,
                onProgress = { index, _ -> progress = index })
            assertEquals(GifVideoMath.frameCount(doc.video!!), progress)
            val bytes = out.toByteArray()
            assertEquals("GIF89a", String(bytes, 0, 6, Charsets.US_ASCII))
            val decoded = Movie.decodeByteArray(bytes, 0, bytes.size)
            assertNotNull("Native MP4 frames must produce a decodable GIF", decoded)
            assertTrue(decoded!!.duration() >= 1000)
            val first = Bitmap.createBitmap(decoded.width(), decoded.height(),
                Bitmap.Config.ARGB_8888)
            val later = Bitmap.createBitmap(decoded.width(), decoded.height(),
                Bitmap.Config.ARGB_8888)
            try {
                decoded.setTime(0)
                decoded.draw(Canvas(first),0f,0f)
                decoded.setTime(1_000)
                decoded.draw(Canvas(later),0f,0f)
                var differences = 0
                for (y in 0 until first.height step 12) {
                    for (x in 0 until first.width step 12) {
                        if (first.getPixel(x,y) != later.getPixel(x,y)) differences++
                    }
                }
                assertTrue("Exported GIF must have different visual frames",differences > 5)
            } finally { first.recycle(); later.recycle() }

            val repeatedClip = clip.copy(fps = 30, reverse = true)
            val repeatedTimes = GifVideoMath.frameTimesMs(repeatedClip)
            val repeatedHolds = GifVideoMath.frameDelaysMs(repeatedClip)
            fun sourceIndex(time: Long): Int = clip.sourceTimesMs.indexOfLast { it <= time }.coerceAtLeast(0)
            val repeatedAt = (0 until repeatedTimes.lastIndex).first {
                sourceIndex(repeatedTimes[it]) == sourceIndex(repeatedTimes[it + 1])
            }
            val repeatedBytes = ByteArrayOutputStream().also {
                VideoGifSource.write(context, untouched.copy(video = repeatedClip), it)
            }.toByteArray()
            val repeatedExport = GifSourceMetadata.read(repeatedBytes.inputStream(), "content://test/repeated")
            assertEquals(repeatedHolds, GifVideoMath.frameDelaysMs(repeatedExport))
            val repeatedMovie = requireNotNull(Movie.decodeByteArray(repeatedBytes, 0, repeatedBytes.size))
            val originalHold = Bitmap.createBitmap(32, 24, Bitmap.Config.ARGB_8888)
            val nextHold = Bitmap.createBitmap(32, 24, Bitmap.Config.ARGB_8888)
            try {
                val start = repeatedHolds.take(repeatedAt).sum()
                repeatedMovie.setTime(start + repeatedHolds[repeatedAt] / 2)
                repeatedMovie.draw(Canvas(originalHold), 0f, 0f)
                repeatedMovie.setTime(start + repeatedHolds[repeatedAt] + repeatedHolds[repeatedAt + 1] / 2)
                repeatedMovie.draw(Canvas(nextHold), 0f, 0f)
                assertTrue("Consecutive GIF holds of the same source frame must have identical pixels",
                    originalHold.sameAs(nextHold))
            } finally {
                originalHold.recycle()
                nextHold.recycle()
            }
        } finally {
            if (uri != null) resolver.delete(uri, null, null)
        }
    }
}

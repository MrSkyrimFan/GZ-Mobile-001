package com.greenezone.gzphotoeditor

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Movie
import android.net.Uri
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.GifEncoder
import com.greenezone.gzphotoeditor.editor.GifMath
import com.greenezone.gzphotoeditor.editor.GifSourceMetadata
import com.greenezone.gzphotoeditor.editor.GifStore
import com.greenezone.gzphotoeditor.editor.GifVideoClip
import com.greenezone.gzphotoeditor.editor.GifVideoMath
import com.greenezone.gzphotoeditor.editor.VideoGifSource
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.util.Base64

@RunWith(AndroidJUnit4::class)
class AndroidGifFidelityTest {
    @Test fun existingGifPassesThroughExactlyAndReverseMovesItsColorsAndFrameHolds() {
        val instrument = InstrumentationRegistry.getInstrumentation()
        val context = instrument.targetContext
        val resolver = context.contentResolver
        val bytes = instrument.context.assets.open("gif_source_timing.gif").use { it.readBytes() }
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "gz_animated_source_test.gif")
            put(MediaStore.Images.Media.MIME_TYPE, "image/gif")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }))
        try {
            resolver.openOutputStream(uri, "wt")!!.use { it.write(bytes) }
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            val original = VideoGifSource.document(context, uri)
            val clip = requireNotNull(original.video)
            assertEquals(18 to 12, GifMath.dimensions(original))
            assertEquals(listOf(70, 130, 310), GifVideoMath.frameDelaysMs(clip))
            assertEquals(2, original.loopCount)
            val copy = ByteArrayOutputStream()
            VideoGifSource.write(context, original, copy)
            assertArrayEquals("Untouched GIF must be byte-identical", bytes, copy.toByteArray())

            val reverse = original.copy(video = clip.copy(reverse = true))
            val rendered = ByteArrayOutputStream()
            VideoGifSource.write(context, reverse, rendered)
            val result = rendered.toByteArray()
            val movie = requireNotNull(Movie.decodeByteArray(result, 0, result.size))
            assertEquals(510, movie.duration())
            val canvas = Bitmap.createBitmap(movie.width(), movie.height(), Bitmap.Config.ARGB_8888)
            try {
                fun verify(time: Int, x: Int, color: Int) {
                    movie.setTime(time)
                    movie.draw(Canvas(canvas), 0f, 0f)
                    assertEquals("Color at playback time ${time}ms", color, canvas.getPixel(x, 5))
                }
                verify(35, 12, Color.rgb(29, 163, 137))
                verify(280, 12, Color.rgb(29, 163, 137))
                verify(360, 7, Color.rgb(181, 91, 39))
                verify(475, 2, Color.rgb(71, 103, 141))
            } finally { canvas.recycle() }
            val rotated = reverse.copy(edits = EditDocument(quarterTurns = 1))
            assertEquals(rotated, GifStore.decode(GifStore.encode(rotated)))
            assertEquals(12 to 18, GifMath.dimensions(rotated))
        } finally { resolver.delete(uri, null, null) }
    }

    @Test fun androidDecoderSelectsTheExactFrameEvenWithZeroAndOneTickDelays() {
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        val data = assets.open("gif_source_timing.gif").use { it.readBytes() }
        val controls = data.indices.filter { i ->
            i + 7 < data.size && data[i] == 0x21.toByte() && data[i+1] == 0xf9.toByte() && data[i+2] == 4.toByte()
        }
        assertEquals(3, controls.size)
        data[controls[0] + 4] = 0
        data[controls[1] + 4] = 1
        val bytes = GifSourceMetadata.indexedStream(data.inputStream()).readBytes()
        val movie = requireNotNull(Movie.decodeByteArray(bytes, 0, bytes.size))
        assertEquals(300, movie.duration())
        val frame = Bitmap.createBitmap(18, 12, Bitmap.Config.ARGB_8888)
        try {
            for ((index, expected) in listOf(2 to Color.rgb(71,103,141), 7 to Color.rgb(181,91,39),
                12 to Color.rgb(29,163,137)).withIndex()) {
                movie.setTime(GifSourceMetadata.decoderTime(index))
                movie.draw(Canvas(frame), 0f, 0f)
                assertEquals(expected.second, frame.getPixel(expected.first, 5))
            }
        } finally { frame.recycle() }
    }

    @Test fun slowedLongHoldReopensWithTheSameDurationAndColorBoundary() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val resolver = context.contentResolver
        // Audit 2's 66-byte red-then-blue source with original holds of 327680 ms and 100 ms.
        val input = Base64.getDecoder().decode(
            "R0lGODlhAQABAIAAAP8AAAAA/yH5BAQAgAAALAAAAAABAAEAAAICRAEAIfkEBAoAAAAsAAAAAAEAAQAAAgJMAQA7"
        )
        val media = mutableListOf<Uri>()
        fun publish(name: String, bytes: ByteArray): Uri {
            val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, name)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/gif")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }))
            media.add(uri)
            requireNotNull(resolver.openOutputStream(uri, "wt")).use { it.write(bytes) }
            resolver.update(uri, ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }, null, null)
            return uri
        }
        try {
            val source = VideoGifSource.document(context, publish("gz-long-hold-source.gif", input))
            val clip = requireNotNull(source.video)
            assertEquals(listOf(327680, 100), GifVideoMath.frameDelaysMs(clip))
            val edited = source.copy(video = clip.copy(speedPercent = 50))
            val output = ByteArrayOutputStream().also { VideoGifSource.write(context, edited, it) }.toByteArray()

            val reopened = VideoGifSource.document(context, publish("gz-long-hold-output.gif", output))
            val reopenedClip = requireNotNull(reopened.video)
            val colorBoundaryMs = 655360
            assertEquals(655560L, reopenedClip.durationMs)
            val delays = GifVideoMath.frameDelaysMs(reopenedClip)
            assertEquals(colorBoundaryMs, delays.dropLast(1).sum())
            assertEquals(200, delays.last())

            val decoded = requireNotNull(Movie.decodeByteArray(output, 0, output.size))
            assertEquals(655560, decoded.duration())
            val image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            try {
                // Android Movie selects the outgoing frame at an exact cumulative delay.
                decoded.setTime(colorBoundaryMs - 1)
                decoded.draw(Canvas(image), 0f, 0f)
                assertEquals(Color.RED, image.getPixel(0, 0))
                decoded.setTime(colorBoundaryMs + 1)
                decoded.draw(Canvas(image), 0f, 0f)
                assertEquals(Color.BLUE, image.getPixel(0, 0))
            } finally {
                image.recycle()
            }
            VideoGifSource.open(context, reopenedClip).use { reader ->
                for ((time, color) in listOf(colorBoundaryMs - 1 to Color.RED, colorBoundaryMs to Color.BLUE)) {
                    val frame = reader.frame(time.toLong(), edge = 1)
                    try {
                        assertEquals("Reopened frame at ${time} ms", color, frame.getPixel(0, 0))
                    } finally {
                        frame.recycle()
                    }
                }
            }
        } finally {
            media.forEach { resolver.delete(it, null, null) }
        }
    }

    @Test fun delaySplitsAtUnsignedSixteenBitBoundariesKeepTheirTotal() {
        val uri = "content://gz-test/long-hold"
        val document = GifDocument(video = GifVideoClip(uri, durationMs = 200, width = 1, height = 1, isGif = true))
        for (ticks in listOf(65534, 65535, 65536, 65537, 131069, 131070, 131071, 131072)) {
            val output = ByteArrayOutputStream()
            GifEncoder.writeFrames(output, document, 1, ticks * 10) {
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.RED) }
            }
            val clip = GifSourceMetadata.read(output.toByteArray().inputStream(), uri)
            assertEquals("Delay total for $ticks ticks", ticks * 10L, clip.durationMs)
            assertTrue(GifVideoMath.frameDelaysMs(clip).all { it in 20..655350 })
        }
    }
}

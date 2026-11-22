package com.greenezone.gzphotoeditor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Movie
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import kotlin.math.abs

/**
 * Preview/export tests with real opaque continuous tones and moving transparency,
 * not only a small three-color GIF asset. No network and no bundled media codecs.
 */
@RunWith(AndroidJUnit4::class)
class AndroidGifColorQualityTest {
    private fun document(transparent: Boolean): GifDocument = GifDocument(
        video = GifVideoClip(
            uri = "content://qa/synthetic-gif-source",
            durationMs = 400L, width = 64, height = 32,
            isGif = transparent
        ),
        size = GifSize.SOURCE,
        aspect = GifAspect.SOURCE,
        forever = false
    )

    @Test fun opaqueMultiColorGradientStaysWithinPaletteErrorBudget() {
        fun source(): Bitmap = Bitmap.createBitmap(64, 32, Bitmap.Config.ARGB_8888).apply {
            for (y in 0 until 32) for (x in 0 until 64) {
                setPixel(x, y, Color.rgb(
                    x * 255 / 63, y * 255 / 31,
                    (x + y) * 255 / 94
                ))
            }
        }
        val out = ByteArrayOutputStream()
        GifEncoder.writeFrames(out, document(false), 2, 200, loader = { source() })
        val raw = out.toByteArray()
        val movie = requireNotNull(Movie.decodeByteArray(raw, 0, raw.size))
        assertEquals(64, movie.width())
        assertEquals(32, movie.height())
        assertEquals(400, movie.duration())
        val bitmap = Bitmap.createBitmap(64, 32, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap)
            movie.setTime(50)
            movie.draw(canvas, 0f, 0f)
            var totalError = 0
            var samples = 0
            for (y in 2 until 32 step 5) for (x in 2 until 64 step 7) {
                val actual = bitmap.getPixel(x, y)
                totalError += abs(Color.red(actual) - x * 255 / 63)
                totalError += abs(Color.green(actual) - y * 255 / 31)
                totalError += abs(Color.blue(actual) - (x + y) * 255 / 94)
                samples += 3
            }
            assertTrue("GIF RGB error ${totalError.toDouble() / samples} too large",
                totalError.toDouble() / samples < 25.0)

            // A held video frame must not flicker simply because it repeats in the timeline.
            val firstPixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(firstPixels, 0, 64, 0, 0, 64, 32)
            movie.setTime(250)
            movie.draw(canvas, 0f, 0f)
            val secondPixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(secondPixels, 0, 64, 0, 0, 64, 32)
            assertArrayEquals("Repeated source frames must remain visually identical",
                firstPixels, secondPixels)
        } finally { bitmap.recycle() }
    }

    @Test fun changingColorCountsAndLateTransparencyKeepThePaletteValid() {
        val palette = GifPalette(257, null, reserveTransparency = false)
        val indices = ByteArray(257)
        // Alternate sparse and quantized frames on the same encoder instance.
        for (distinct in listOf(256, 257, 255, 256, 1)) {
            for (lateAlpha in listOf(false, true)) {
                val colors = IntArray(257) { index ->
                    val value = index % distinct
                    Color.rgb(value and 255, (value * 17) and 255, value ushr 8)
                }
                if (lateAlpha) colors[256] = Color.TRANSPARENT
                val bitmap = Bitmap.createBitmap(colors, 257, 1, Bitmap.Config.ARGB_8888)
                try {
                    palette.index(bitmap, indices)
                    assertEquals(lateAlpha, palette.transparent)
                    for (i in colors.indices) {
                        val index = indices[i].toInt() and 255
                        if (colors[i] == Color.TRANSPARENT) assertEquals(0, index)
                        else {
                            if (lateAlpha) assertTrue(index != 0)
                            val rgb = colors[i] and 0xffffff
                            val actual = palette.colors[index]
                            if (distinct <= (if (lateAlpha) 255 else 256)) assertEquals(rgb, actual)
                            else {
                                assertTrue(abs(Color.red(rgb) - Color.red(actual)) <= 8)
                                assertTrue(abs(Color.green(rgb) - Color.green(actual)) <= 8)
                                assertTrue(abs(Color.blue(rgb) - Color.blue(actual)) <= 8)
                            }
                        }
                    }
                } finally { bitmap.recycle() }
            }
        }
    }

    @Test fun sameSizeTransparentPhotoExportsWithItsPreviewBackground() {
        val doc = GifDocument(frames = listOf("content://qa/first", "content://qa/second"),
            size = GifSize.SMALL)
        val (w, h) = GifMath.dimensions(doc)
        fun photo() = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            setPixel(w / 2, h / 2, Color.RED)
        }
        val out = ByteArrayOutputStream()
        GifEncoder.write(out, doc) { photo() }
        val raw = out.toByteArray()
        val movie = requireNotNull(Movie.decodeByteArray(raw, 0, raw.size))
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        try {
            movie.setTime(50)
            movie.draw(Canvas(bitmap), 0f, 0f)
            assertEquals(Color.BLACK, bitmap.getPixel(0, 0))
            assertEquals(Color.RED, bitmap.getPixel(w / 2, h / 2))
        } finally { bitmap.recycle() }
    }

    @Test fun movingObjectOverTransparentBackgroundLeavesNoGhostedPreviousFrame() {
        val out = ByteArrayOutputStream()
        GifEncoder.writeFrames(out, document(true), 2, 200, loader = { position ->
            Bitmap.createBitmap(64, 32, Bitmap.Config.ARGB_8888).apply {
                eraseColor(Color.TRANSPARENT)
                val left = if (position == 0) 6 else 38
                for (y in 8 until 24) for (x in left until left + 16) {
                    setPixel(x, y, Color.RED)
                }
            }
        })
        val bytes = out.toByteArray()
        val movie = requireNotNull(Movie.decodeByteArray(bytes, 0, bytes.size))
        assertEquals(400, movie.duration())
        val result = Bitmap.createBitmap(64, 32, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(result)
            movie.setTime(50)
            movie.draw(canvas, 0f, 0f)
            assertEquals(Color.RED, result.getPixel(14, 16))
            assertEquals(0, Color.alpha(result.getPixel(46, 16)))
            // Movie.draw() blends the decoded frame over its caller-owned canvas.
            // Clear that *external* canvas first; otherwise our previous painted
            // output remains even if GIF disposal correctly cleared decoder pixels.
            result.eraseColor(Color.TRANSPARENT)
            movie.setTime(250)
            movie.draw(canvas, 0f, 0f)
            assertEquals(Color.RED, result.getPixel(46, 16))
            assertEquals("Old position must return to transparency, not black or red",
                0, Color.alpha(result.getPixel(14, 16)))
        } finally { result.recycle() }
    }
}

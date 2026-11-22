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
            val clip = VideoGifSource.inspect(context, uri)
            assertTrue(GifVideoMath.valid(clip))
            assertTrue("Fixture duration must match actual video", clip.durationMs in 1800L..2200L)
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
        } finally {
            if (uri != null) resolver.delete(uri, null, null)
        }
    }
}

package com.greenezone.gzphotoeditor

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Movie
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream

@RunWith(AndroidJUnit4::class)
class AndroidProGifProductionTest {
    private fun layer(kind: ProLayerKind, creative: CreativeDocument = CreativeDocument(),
        grade: Grade = Grade()) = ProLayer(
            uri = "", name = kind.label, kind = kind,
            edits = EditDocument(grade = grade, creative = creative)
        )

    @Test fun vectorShapeAndDrawingRenderAsRealSeparateLayers() {
        val rectangle = layer(ProLayerKind.SHAPE, CreativeDocument(shapes = listOf(
            ShapeOverlay(kind = ShapeKind.RECTANGLE,
                x = .5f, y = .5f, width = .6f, height = .6f,
                filled = true, color = Color.RED))))
        val paint = layer(ProLayerKind.DRAWING, CreativeDocument(strokes = listOf(
            InkStroke(points = listOf(BrushPoint(.1f, .1f), BrushPoint(.9f, .1f)),
                color = Color.BLUE, thickness = .05f))))
        val project = ProDocument(listOf(rectangle, paint))
        val image = ProRaster.renderWithLoader(project, 200, 200, false,
            loader = { _, _, _ -> error("Vector layers must not open image files") })
        try {
            assertEquals(Color.RED, image.getPixel(100, 100))
            assertEquals(Color.BLUE, image.getPixel(100, 20))
            assertEquals(Color.WHITE, image.getPixel(5, 100))
        } finally { image.recycle() }
    }

    @Test fun textAndAdjustmentLayersKeepIndependentPayloadsAfterSerialization() {
        val caption = layer(ProLayerKind.TEXT, CreativeDocument(
            text = listOf(TextOverlay(content = "One caption", color = Color.BLACK))))
        val adjustment = layer(ProLayerKind.ADJUSTMENT, grade = Grade(exposure = .2f))
        val original = ProDocument(listOf(caption, adjustment),
            aspect = ProAspect.LANDSCAPE, background = ProBackground.TRANSPARENT)
        val encoded = ProStore.encode(original)
        assertEquals(original, ProStore.decode(encoded))
        val image = ProRaster.renderWithLoader(original, 240, 140, false,
            loader = { _, _, _ -> error("No bitmap layer exists") })
        try {
            assertEquals(240, image.width)
            assertEquals(140, image.height)
        } finally { image.recycle() }
    }

    @Test fun projectsFromVersionOneRemainReadableWithoutLayerKinds() {
        val oldProject = ProDocument(listOf(ProLayer(
            uri = "content://legacy/old", name = "Previous Pro image")))
        val saved = JSONObject(ProStore.encode(oldProject)).apply {
            put("version", 1)
            val layer = getJSONArray("layers").getJSONObject(0)
            layer.remove("kind")
            layer.remove("aspect")
        }.toString()
        assertEquals(oldProject, ProStore.decode(saved))
    }

    @Test fun streamingVideoFramesCreateAnimatedGifWithoutImageArrayCache() {
        val video = GifDocument(video = GifVideoClip(
            uri = "content://media/example", durationMs = 2_000L,
            startMs = 0, endMs = 2_000L, fps = 8, speedPercent = 100))
        val count = GifVideoMath.frameCount(video.video!!)
        assertEquals(16, count)
        val buffer = ByteArrayOutputStream()
        GifEncoder.writeFrames(buffer, video, count,
            GifVideoMath.gifDelayMs(video.video), loader = { index ->
                Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888).apply {
                    eraseColor(if (index % 2 == 0) Color.RED else Color.BLUE)
                }
            })
        val bytes = buffer.toByteArray()
        assertEquals("GIF89a", String(bytes, 0, 6, Charsets.US_ASCII))
        assertEquals(0x3B, bytes.last().toInt() and 255)
        val decoded = Movie.decodeByteArray(bytes, 0, bytes.size)
        assertNotNull("Android must decode the new GIF", decoded)
        assertTrue(decoded!!.duration() >= 1_000)
    }

    @Test fun videoGifProjectsRoundTripAndLegacyPhotoAnimationSurvives() {
        val clip = GifVideoClip(
            uri = "content://media/video", durationMs = 24_000L,
            startMs = 3_000L, endMs = 8_000L,
            fps = 10, speedPercent = 200, reverse = true)
        val project = GifDocument(video = clip, size = GifSize.SMALL,
            aspect = GifAspect.LANDSCAPE, fit = GifFit.FILL, forever = true)
        assertEquals(project, GifStore.decode(GifStore.encode(project)))
        val old = GifDocument(frames = listOf("content://a", "content://b"))
        val previous = JSONObject(GifStore.encode(old)).apply { put("version", 1) }.toString()
        assertEquals(old, GifStore.decode(previous))
    }
}

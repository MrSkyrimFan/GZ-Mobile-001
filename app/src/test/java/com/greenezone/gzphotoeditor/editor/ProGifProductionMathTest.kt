package com.greenezone.gzphotoeditor.editor

import org.junit.Assert.*
import org.junit.Test

class ProGifProductionMathTest {
    private val first = "content://example/photo-1"

    @Test fun fiveIndependentProLayerKindsRemainValidAndOrdered() {
        val image = ProLayer(first, name = "Original")
        val text = ProLayer(uri = "", name = "Caption", kind = ProLayerKind.TEXT,
            edits = EditDocument(creative = CreativeDocument(
                text = listOf(TextOverlay(content = "Hello")))))
        val shape = ProLayer(uri = "", name = "Shape", kind = ProLayerKind.SHAPE,
            edits = EditDocument(creative = CreativeDocument(
                shapes = listOf(ShapeOverlay()))))
        val brush = ProLayer(uri = "", name = "Brush", kind = ProLayerKind.DRAWING)
        val adjustment = ProLayer(uri = "", name = "Adjustment", kind = ProLayerKind.ADJUSTMENT,
            edits = EditDocument(grade = Grade(exposure = .3f)))
        val doc = ProDocument(listOf(image, text, shape, brush, adjustment))
        assertTrue(ProMath.valid(doc))
        assertEquals(ProLayerKind.TEXT, doc.layers[1].kind)
        assertEquals(ProLayerKind.ADJUSTMENT, doc.layers.last().kind)
        val next = doc.copy(layers = ProMath.move(doc.layers, 0, 4))
        assertEquals(image, next.layers.last())
        assertEquals(doc, ProHistory().checkpoint(doc, next).undo(next)!!.first)
    }

    @Test fun proModelRejectsMalformedLayerKindsAndOversizedBrushes() {
        assertFalse(ProMath.valid(ProDocument(listOf(
            ProLayer(uri = "", name = "Missing bitmap", kind = ProLayerKind.IMAGE)))))
        assertFalse(ProMath.valid(ProDocument(listOf(
            ProLayer(uri = first, name = "Not local", kind = ProLayerKind.TEXT)))))
        assertFalse(ProMath.valid(ProDocument(listOf(
            ProLayer(uri = "", name = "No shape", kind = ProLayerKind.SHAPE)))))
        assertFalse(ProMath.valid(ProDocument(listOf(
            ProLayer(uri = "", name = "Brush", kind = ProLayerKind.DRAWING,
                edits = EditDocument(creative = CreativeDocument(
                    strokes = listOf(InkStroke(List(501) { BrushPoint(.5f, .5f) })))))))))
        assertFalse(ProMath.valid(ProDocument(List(13) {
            ProLayer(first, name = "Image $it")
        })))
        assertNull(ProMath.insert(ProDocument(List(12) {
            ProLayer(first, name = "Image $it")
        }), ProLayer(first, name = "Overflow")))
    }

    @Test fun videoGifTrimmingFramesSpeedAndReverseStayBounded() {
        val clip = GifVideoMath.defaultClip("content://example/video-1", 18_000L)
        assertTrue(GifVideoMath.valid(clip))
        assertEquals(5_000L, clip.endMs)
        assertEquals(40, GifVideoMath.frameCount(clip))
        assertEquals(125, GifVideoMath.gifDelayMs(clip) + 5) // Rounded to 120ms.
        val times = GifVideoMath.frameTimesMs(clip)
        assertEquals(40, times.size)
        assertEquals(0L, times.first())
        assertTrue(times.last() < clip.endMs)
        assertEquals(times.reversed(), GifVideoMath.frameTimesMs(clip.copy(reverse = true)))
        assertTrue(GifVideoMath.valid(clip.copy(endMs = 12_000L, fps = 10)))
        assertFalse(GifVideoMath.valid(clip.copy(endMs = 13_000L)))
        assertFalse(GifVideoMath.valid(clip.copy(endMs = 500L, startMs = 250L)))
        assertFalse(GifVideoMath.valid(clip.copy(fps = 60)))
        assertFalse(GifVideoMath.valid(clip.copy(uri = "file:///tmp/bad.mp4")))
        val video = GifDocument(video = clip)
        assertTrue(GifMath.valid(video))
        assertFalse(GifMath.valid(video.copy(frames = listOf(first))))
    }
}

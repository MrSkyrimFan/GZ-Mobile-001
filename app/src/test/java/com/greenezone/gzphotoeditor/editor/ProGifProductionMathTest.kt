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

    @Test fun longClipNeverSilentlyLowersRequestedFps() {
        val clip = GifVideoClip(
            uri = "content://qa/long-video", durationMs = 12_000_000L,
            width = 1920, height = 1080,
            sourceTimesMs = listOf(0L, 1000L, 2000L)
        )
        assertTrue("Source timing is still allowed", GifVideoMath.valid(clip))
        assertFalse("30fps on a long video cannot be silently reduced to 150K frames",
            GifVideoMath.fitsFrameBudget(clip, 30))
        assertFalse(GifVideoMath.valid(clip.copy(fps = 30)))
        assertTrue(GifVideoMath.valid(clip.copy(fps = 10)))
    }

    @Test fun sourceDurationAndVariableFrameHoldsSurviveTrimReverseAndSpeed() {
        val clip = GifVideoClip("content://example/video-1", 18_000L, width = 360, height = 640,
            sourceTimesMs = listOf(0L, 33L, 67L, 100L, 170L, 200L))
        assertEquals(18_000L, GifVideoMath.defaultClip(clip.uri, clip.durationMs).endMs)
        assertEquals(18_000, GifVideoMath.frameDelaysMs(clip).sum())
        assertEquals(360 to 640, GifMath.dimensions(GifDocument(video = clip)))
        val segment = clip.copy(startMs = 10, endMs = 170)
        assertEquals(listOf(10L, 33L, 67L, 100L), GifVideoMath.frameTimesMs(segment))
        assertEquals(listOf(20, 40, 30, 70), GifVideoMath.frameDelaysMs(segment))
        val reverse = segment.copy(reverse = true)
        assertEquals(GifVideoMath.frameTimesMs(segment).reversed(), GifVideoMath.frameTimesMs(reverse))
        assertEquals(GifVideoMath.frameDelaysMs(segment).reversed(), GifVideoMath.frameDelaysMs(reverse))
        assertEquals(80, GifVideoMath.frameDelaysMs(reverse.copy(speedPercent = 200)).sum())
        assertEquals(320, GifVideoMath.frameDelaysMs(reverse.copy(speedPercent = 50)).sum())
        val output = GifDocument(video = clip.copy(fps = 8))
        assertEquals(144, GifVideoMath.frameCount(output.video!!))
        assertEquals(18_000, GifVideoMath.frameDelaysMs(output.video).sum())
        assertFalse(GifVideoMath.valid(clip.copy(endMs = clip.durationMs + 1)))
        assertFalse(GifVideoMath.valid(clip.copy(startMs = clip.endMs)))
        assertFalse(GifVideoMath.valid(clip.copy(uri = "file:///tmp/bad.mp4")))
        assertFalse(GifMath.valid(output.copy(frames = listOf(first))))
    }
}

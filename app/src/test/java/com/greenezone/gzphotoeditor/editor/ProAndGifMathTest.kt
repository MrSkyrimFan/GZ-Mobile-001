package com.greenezone.gzphotoeditor.editor

import org.junit.Assert.*
import org.junit.Test

class ProAndGifMathTest {
    private fun layer(id: String) = ProLayer("content://photos/" + id, name = id)
    @Test fun independentLayerOrderVisibilityAndUndo() {
        val original = ProDocument(listOf(layer("red"), layer("blue").copy(opacity = .3f)))
        assertTrue(ProMath.valid(original))
        val moved = original.copy(layers = ProMath.move(original.layers, 0, 1))
        assertEquals("blue", moved.layers[0].name)
        assertEquals("red", moved.layers[1].name)
        val history = ProHistory().checkpoint(original, moved)
        val (previous, undone) = history.undo(moved)!!
        assertEquals(original, previous)
        assertEquals(moved, undone.redo(previous)!!.first)
    }
    @Test fun proImageGeometryHasStrictBudgets() {
        val rect = ProMath.bounds(layer("one"), 200, 100, 100, 100)
        assertEquals(50f, rect.left, .001f)
        assertEquals(150f, rect.right, .001f)
        val (w,h) = ProMath.dimensions(ProAspect.PORTRAIT, 1000, 1_000_000)
        assertEquals(800, w); assertEquals(1000, h)
        assertFalse(ProMath.valid(ProDocument()))
        assertFalse(ProMath.valid(ProDocument(List(13) { layer(it.toString()) })))
        assertFalse(ProMath.valid(ProDocument(listOf(layer("bad").copy(opacity = Float.NaN)))))
    }
    @Test fun gifFramesTimingAndHistoryAreBounded() {
        val a = "content://a"; val b = "content://b"
        val doc = GifDocument(listOf(a,b))
        assertTrue(GifMath.valid(doc))
        assertEquals(480 to 480, GifMath.dimensions(doc))
        assertEquals(480 to 270, GifMath.dimensions(doc.copy(aspect = GifAspect.LANDSCAPE)))
        assertEquals(listOf(b,a), GifMath.move(doc.frames, 0, 1))
        assertFalse(GifMath.valid(doc.copy(frames = List(21) { a })))
        assertFalse(GifMath.valid(doc.copy(frames = listOf(a))))
        assertFalse(GifMath.valid(doc.copy(delayMs = 15)))
        assertEquals(doc, GifHistory().checkpoint(doc, doc.copy(delayMs = 400))
            .undo(doc.copy(delayMs = 400))!!.first)
    }
}

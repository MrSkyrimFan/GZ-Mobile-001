package com.greenezone.gzphotoeditor.editor

import org.junit.Assert.*
import org.junit.Test

class ProHitMathTest {
    private val backdrop = ProLayer(
        uri = "content://images/base",
        name = "Backdrop",
        aspect = 1f
    )
    private val text = ProLayer(
        uri = "", name = "Title", kind = ProLayerKind.TEXT,
        x = .8f, y = .5f,
        edits = EditDocument(creative = CreativeDocument(
            text = listOf(TextOverlay(content = "Title",
                width = .35f, height = .25f))
        ))
    )

    @Test fun tappingVisibleTopmostObjectPicksItBeforeBackground() {
        val doc = ProDocument(listOf(backdrop, text))
        assertTrue(ProMath.valid(doc))
        assertEquals(1, ProHitMath.pick(doc, .8f, .5f))
        assertEquals(0, ProHitMath.pick(doc, .1f, .5f))
        assertEquals(0, ProHitMath.pick(doc.copy(
            layers = listOf(backdrop, text.copy(visible = false))
        ), .8f, .5f))
        assertNull(ProHitMath.pick(doc, Float.NaN, .2f))
    }

    @Test fun masksAndInversionAffectHitTesting() {
        val rectangle = ProSelection(ProSelectionKind.RECTANGLE,
            centerX = .3f, centerY = .5f, width = .4f, height = .8f)
        assertTrue(ProHitMath.maskContains(rectangle, .3f, .5f))
        assertFalse(ProHitMath.maskContains(rectangle, .8f, .5f))
        assertTrue(ProHitMath.maskContains(rectangle.copy(inverted = true), .8f, .5f))
        val oval = ProSelection(ProSelectionKind.ELLIPSE,
            centerX = .5f, centerY = .5f, width = .5f, height = .5f)
        assertTrue(ProHitMath.maskContains(oval, .5f, .5f))
        assertFalse(ProHitMath.maskContains(oval, .1f, .1f))
        val lasso = ProSelection(ProSelectionKind.LASSO, points = listOf(
            BrushPoint(.2f,.2f), BrushPoint(.8f,.2f),
            BrushPoint(.8f,.8f), BrushPoint(.2f,.8f)))
        assertTrue(ProHitMath.maskContains(lasso, .5f, .5f))
        assertFalse(ProHitMath.maskContains(lasso, .1f, .5f))
        val masked = backdrop.copy(mask = rectangle)
        assertEquals(0, ProHitMath.pick(ProDocument(listOf(masked)), .2f, .5f))
        assertNull(ProHitMath.pick(ProDocument(listOf(masked)), .8f, .5f))
    }

    @Test fun topmostStrokeCanBeSelectedByTappingItsRenderedLine() {
        val stroke = ProLayer(
            uri = "", name = "Line", kind = ProLayerKind.DRAWING,
            edits = EditDocument(creative = CreativeDocument(strokes = listOf(
                InkStroke(listOf(BrushPoint(.1f,.5f),
                    BrushPoint(.9f,.5f)), thickness = .05f)
            )))
        )
        assertEquals(1, ProHitMath.pick(ProDocument(listOf(backdrop,stroke)),.5f,.51f))
        assertEquals(0, ProHitMath.pick(ProDocument(listOf(backdrop,stroke)),.5f,.85f))
    }

    @Test fun selectionPassesThroughErasedPixelsUntilTheyAreRepainted() {
        val paint = InkStroke(listOf(BrushPoint(.1f, .5f), BrushPoint(.9f, .5f)), thickness = .08f)
        val erase = paint.copy(points = listOf(BrushPoint(.5f, .2f), BrushPoint(.5f, .8f)), erase = true)
        val drawing = ProLayer("", kind = ProLayerKind.DRAWING)
        fun pick(x: Float, y: Float, vararg strokes: InkStroke): Int? {
            val creative = CreativeDocument(strokes = strokes.toList())
            val document = ProDocument(listOf(backdrop, drawing.copy(edits = EditDocument(creative = creative))))
            return ProHitMath.pick(document, x, y, 1000, 500)
        }
        assertEquals(0, pick(.5f, .5f, paint, erase))
        assertEquals(0, pick(.5f, .5f, paint, erase, paint.copy(opacity = 0f)))
        assertEquals(1, pick(.5f, .5f, paint, erase, paint))
        // At this aspect ratio the eraser is 40 pixels wide. This point is 30 pixels to its right.
        assertEquals(1, pick(.53f, .5f, paint, erase))
    }
}

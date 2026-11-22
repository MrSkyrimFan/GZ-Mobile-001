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
}

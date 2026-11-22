package com.greenezone.gzphotoeditor.editor

import org.junit.Assert.*
import org.junit.Test

class ProWorkspaceToolsTest {
    @Test fun rectanglesAndEllipsesFollowActualDragGeometry() {
        val rect = ProToolMath.rectangle(BrushPoint(.10f, .2f),
            BrushPoint(.90f, .8f), ProSelectionKind.RECTANGLE)
        assertEquals(.5f, rect.centerX, .001f)
        assertEquals(.5f, rect.centerY, .001f)
        assertEquals(.8f, rect.width, .001f)
        assertEquals(.6f, rect.height, .001f)
        assertTrue(ProToolMath.valid(rect))
        val ellipse = ProToolMath.rectangle(BrushPoint(.8f, .8f),
            BrushPoint(.2f, .2f), ProSelectionKind.ELLIPSE)
        assertTrue(ProToolMath.valid(ellipse))
        assertEquals(.6f, ellipse.height, .001f)
    }

    @Test fun lassoAndMaskInvalidInputsFailClosed() {
        val lasso = ProSelection(ProSelectionKind.LASSO,
            points = listOf(BrushPoint(.1f,.1f), BrushPoint(.8f,.1f),
                BrushPoint(.8f,.8f), BrushPoint(.1f,.8f)))
        assertTrue(ProToolMath.valid(lasso))
        assertFalse(ProToolMath.valid(lasso.copy(points = lasso.points.take(2))))
        assertFalse(ProToolMath.valid(lasso.copy(points = List(451) { BrushPoint(.5f,.5f) })))
        assertFalse(ProToolMath.valid(lasso.copy(points = lasso.points +
            BrushPoint(Float.NaN,.5f))))
        assertFalse(ProToolMath.valid(ProSelection(ProSelectionKind.RECTANGLE, width = 0f)))
    }

    @Test fun proMatsHaveRealInputRulesAndHistory() {
        val layer = ProLayer("content://images/one", name = "Original",
            mask = ProSelection(ProSelectionKind.RECTANGLE),
            retouches = listOf(ProRetouchMark(
                ProRetouchKind.CLONE,.1f,.1f,.8f,.8f)),
            rotationDegrees = 40f)
        val doc = ProDocument(listOf(layer))
        assertTrue(ProMath.valid(doc))
        assertFalse(ProMath.valid(doc.copy(layers = listOf(layer.copy(
            rotationDegrees = Float.NaN)))))
        assertFalse(ProMath.valid(doc.copy(layers = listOf(layer.copy(
            mask = ProSelection(ProSelectionKind.LASSO, points = emptyList())))))
        assertFalse(ProMath.valid(doc.copy(layers = listOf(layer.copy(
            retouches = List(81) { ProRetouchMark(
                ProRetouchKind.CLONE,.1f,.1f,.8f,.8f) })))))
        assertEquals(doc, ProHistory().checkpoint(doc, ProDocument()).undo(ProDocument())!!.first)
        assertEquals(ProLayerKind.IMAGE, layer.kind)
    }

    @Test fun proEffectLayersCannotBeFakeImageLayers() {
        val fx = ProLayer(uri = "", name = "Invert",
            kind = ProLayerKind.ADJUSTMENT, effect = ProPixelEffect.INVERT)
        assertTrue(ProMath.valid(ProDocument(listOf(fx))))
        assertFalse(ProMath.valid(ProDocument(listOf(fx.copy(kind = ProLayerKind.TEXT)))))
        assertFalse(ProToolMath.valid(ProRetouchMark(ProRetouchKind.CLONE,
            .5f,.5f,.5f,.5f,radius = -.3f)))
        assertFalse(ProMath.valid(ProDocument(listOf(fx.copy(effectAmount = Float.NaN)))))
    }
}

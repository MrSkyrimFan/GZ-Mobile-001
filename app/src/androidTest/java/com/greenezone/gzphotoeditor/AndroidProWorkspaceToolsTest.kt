package com.greenezone.gzphotoeditor

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidProWorkspaceToolsTest {
    private fun solid(color: Int) =
        Bitmap.createBitmap(30, 30, Bitmap.Config.ARGB_8888).apply {
            eraseColor(color)
        }
    private fun image(mask: ProSelection? = null,
        retouches: List<ProRetouchMark> = emptyList()) = ProLayer(
        uri = "content://fake-source", name = "Image",
        mask = mask, retouches = retouches
    )

    @Test fun rectangleMaskReallyHidesPixelsAndInvertRevealsOppositeSide() {
        val m = ProSelection(ProSelectionKind.RECTANGLE,
            centerX = .25f, centerY = .5f, width = .5f, height = 1f)
        fun render(mask: ProSelection): Bitmap =
            ProRaster.renderWithLoader(
                ProDocument(listOf(image(mask))), 100, 100, false,
                loader = { _,_,_ -> solid(Color.RED) })
        val left = render(m)
        val right = render(m.copy(inverted = true))
        try {
            assertEquals(Color.RED, left.getPixel(20,50))
            assertEquals(Color.WHITE, left.getPixel(80,50))
            assertEquals(Color.WHITE, right.getPixel(20,50))
            assertEquals(Color.RED, right.getPixel(80,50))
        } finally {
            left.recycle(); right.recycle()
        }
    }

    @Test fun ellipseAndLassoMasksProduceRealGeometry() {
        val ellipse = ProSelection(ProSelectionKind.ELLIPSE,
            centerX = .5f, centerY = .5f, width = .5f, height = .5f)
        val free = ProSelection(ProSelectionKind.LASSO, points = listOf(
            BrushPoint(.1f,.1f), BrushPoint(.75f,.1f),
            BrushPoint(.75f,.75f), BrushPoint(.1f,.75f)))
        for (mask in listOf(ellipse, free)) {
            val bitmap = ProRaster.renderWithLoader(
                ProDocument(listOf(image(mask))), 100, 100, false,
                loader = {_,_,_-> solid(Color.BLUE) })
            try {
                assertEquals(Color.BLUE, bitmap.getPixel(50,50))
                assertEquals(Color.WHITE, bitmap.getPixel(95,95))
            } finally { bitmap.recycle() }
        }
    }

    @Test fun cloneCopiesPixelsAndSoftRepairBlendsSelectedArea() {
        val source = Bitmap.createBitmap(100,100,Bitmap.Config.ARGB_8888)
        for (y in 0 until 100) {
            for (x in 0 until 100) {
                source.setPixel(x,y,if (x >= 50) Color.RED else Color.BLUE)
            }
        }
        val mark = ProRetouchMark(ProRetouchKind.CLONE,
            sourceX = .75f, sourceY = .5f,
            targetX = .25f, targetY = .5f, radius = .10f)
        val clone = ProRetouchRaster.apply(source,listOf(mark))
        val soft = ProRetouchRaster.apply(source,
            listOf(mark.copy(kind = ProRetouchKind.SOFT_REPAIR)))
        try {
            assertEquals(Color.BLUE, source.getPixel(25,50))
            assertEquals(Color.RED, clone.getPixel(25,50))
            assertNotEquals(Color.BLUE, soft.getPixel(25,50))
            assertNotEquals(Color.RED, soft.getPixel(25,50))
        } finally {
            clone.recycle(); soft.recycle(); source.recycle()
        }
    }

    @Test fun pixelEffectChangesActualCompositeNotOriginalSource() {
        val fx = ProLayer(uri = "", name = "Invert", kind = ProLayerKind.ADJUSTMENT,
            effect = ProPixelEffect.INVERT)
        val doc = ProDocument(listOf(image(), fx))
        val bitmap = ProRaster.renderWithLoader(doc,80,80,false,
            loader = { _,_,_ -> solid(Color.RED) })
        try {
            assertEquals(Color.CYAN, bitmap.getPixel(40,40))
        } finally { bitmap.recycle() }
        val threshold = ProEffectRaster.render(solid(Color.RED),
            ProPixelEffect.THRESHOLD, amount = .8f)
        try {
            assertEquals(Color.BLACK,threshold.getPixel(10,10))
        } finally { threshold.recycle() }
    }

    @Test fun projectV3PersistsMaskCloneRotationAndEffectAndLoadsLegacy() {
        val mask = ProSelection(ProSelectionKind.LASSO, points = listOf(
            BrushPoint(.1f,.1f), BrushPoint(.7f,.1f), BrushPoint(.7f,.7f)))
        val source = image(mask,listOf(
            ProRetouchMark(ProRetouchKind.SOFT_REPAIR,
                .1f,.2f,.8f,.7f,.06f,.8f))).copy(rotationDegrees = -37f)
        val layer = ProLayer(uri = "", name = "Grayscale",
            kind = ProLayerKind.ADJUSTMENT,
            effect = ProPixelEffect.GRAYSCALE)
        val v3 = ProDocument(listOf(source,layer),aspect=ProAspect.PORTRAIT)
        assertEquals(v3, ProStore.decode(ProStore.encode(v3)))
        val legacy = JSONObject(ProStore.encode(ProDocument(listOf(image())))).apply {
            put("version",2)
            val old = getJSONArray("layers").getJSONObject(0)
            old.remove("retouches"); old.remove("mask")
            old.remove("effect"); old.remove("effectAmount"); old.remove("rotate")
        }.toString()
        assertEquals(ProDocument(listOf(image())),ProStore.decode(legacy))
        assertNull(ProStore.decode(JSONObject(ProStore.encode(v3)).apply {
            getJSONArray("layers").getJSONObject(0).getJSONObject("mask")
                .put("kind", "LASSO")
                .put("points", org.json.JSONArray())
        }.toString()))
    }
}

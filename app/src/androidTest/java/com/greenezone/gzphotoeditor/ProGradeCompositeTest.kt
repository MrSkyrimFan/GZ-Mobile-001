package com.greenezone.gzphotoeditor

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProGradeCompositeTest {
    private val image = ProLayer("content://test/layer")
    private val adjustment = ProLayer("", kind = ProLayerKind.ADJUSTMENT,
        name = "Sky correction", edits = EditDocument(grade = Grade(saturation = -1f)),
        mask = ProSelection(ProSelectionKind.RECTANGLE, centerX = .25f, width = .5f, height = 1f))

    private fun render(layer: ProLayer, color: Int): Bitmap {
        val project = ProDocument(listOf(image, layer), background = ProBackground.TRANSPARENT)
        assertEquals(project, ProStore.decode(ProStore.encode(project)))
        return ProRaster.renderWithLoader(project, 32, 32, false) { _, _, _ ->
            Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
        }
    }

    @Test fun adjustmentChangesOnlyTheSelectionAndPreservesTranslucentEdges() {
        val original = Color.argb(112, 255, 0, 0)
        val bitmap = render(adjustment, original)
        try {
            val inside = bitmap.getPixel(6, 12)
            assertEquals(112, Color.alpha(inside))
            assertEquals(Color.red(inside), Color.green(inside))
            assertEquals(Color.green(inside), Color.blue(inside))
            assertEquals(original, bitmap.getPixel(25, 12))
        } finally { bitmap.recycle() }
    }

    @Test fun featherBlendsAcrossBothSidesOfTheSelectionBoundary() {
        val softMask = adjustment.mask!!.copy(feather = .14f)
        val layer = adjustment.copy(mask = softMask, edits = EditDocument(), effect = ProPixelEffect.INVERT)
        val bitmap = render(layer, Color.BLACK)
        try {
            val samples = listOf(5, 13, 17, 26).map { x -> Color.red(bitmap.getPixel(x, 16)) }
            assertTrue("Expected a gradual edge, saw $samples", samples.zipWithNext().all { (a, b) -> a > b })
            assertTrue(samples[1] < 255 && samples[2] > 0)
        } finally { bitmap.recycle() }

        val cutout = ProRaster.renderWithLoader(ProDocument(listOf(image.copy(mask = softMask)),
            background = ProBackground.TRANSPARENT), 32, 32, false) { _, _, _ ->
            Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.RED) }
        }
        try {
            assertTrue(Color.alpha(cutout.getPixel(15, 16)) in 1..254)
            assertTrue(Color.alpha(cutout.getPixel(8, 16)) > Color.alpha(cutout.getPixel(21, 16)))
        } finally { cutout.recycle() }
    }

    @Test fun invertedSelectionCombinesGradeWithEffectAndBlendPreservesAlpha() {
        val original = Color.argb(160, 255, 0, 0)
        val layer = adjustment.copy(effect = ProPixelEffect.INVERT,
            mask = adjustment.mask!!.copy(inverted = true))
        val bitmap = render(layer, original)
        try {
            assertEquals(original, bitmap.getPixel(5, 12))
            val right = bitmap.getPixel(25, 12)
            assertEquals(160, Color.alpha(right))
            assertEquals(Color.red(right), Color.blue(right))
            assertTrue(Color.red(right) in 1..254)
        } finally { bitmap.recycle() }

        val blended = render(layer.copy(blend = ProBlend.DIFFERENCE, opacity = .5f), original)
        try {
            val right = blended.getPixel(25, 12)
            assertEquals(160, Color.alpha(right))
            assertTrue(Color.green(right) in 1..160)
            assertTrue(Color.red(right) > Color.green(right))
        } finally { blended.recycle() }
    }
}

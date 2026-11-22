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
class ProLinkedMaskTest {
    private val original = ProLayer("content://test/landmark", x = .4f, y = .5f,
        scale = .8f, rotationDegrees = 90f)
    private val draft = ProSelection(ProSelectionKind.RECTANGLE, centerX = .4f, centerY = .7f,
        width = .22f, height = .12f)
    private val linked = original.copy(mask = ProToolMath.applySelection(draft, original, 1f))
    private fun moved(layer: ProLayer) =
        layer.copy(x = .62f, y = .5f, scale = .72f, rotationDegrees = 0f)

    private fun render(layer: ProLayer, width: Int = 100): Bitmap =
        ProRaster.renderWithLoader(ProDocument(listOf(layer), background = ProBackground.TRANSPARENT),
            width, 100, false) { _, _, _ ->
            Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
                eraseColor(Color.BLUE)
                val red = IntArray(5000) { Color.RED }
                setPixels(red, 0, 50, 50, 0, 50, 100)
            }
        }

    @Test fun movedRotatedScaledPhotoKeepsItsCutoutAtTheSameSourcePixels() {
        val initial = render(linked)
        try {
            assertEquals(Color.RED, initial.getPixel(40, 70))
            assertEquals(Color.TRANSPARENT, initial.getPixel(40, 50))
        } finally { initial.recycle() }
        val project = ProDocument(listOf(moved(linked)), background = ProBackground.TRANSPARENT)
        val restored = requireNotNull(ProStore.decode(ProStore.encode(project)))
        assertEquals(project, restored)
        val adjusted = render(restored.layers.single())
        try {
            assertEquals(Color.TRANSPARENT, adjusted.getPixel(40, 70))
            assertEquals(Color.RED, adjusted.getPixel(80, 50))
            assertEquals(Color.RED, adjusted.getPixel(80, 58))
            assertEquals(Color.TRANSPARENT, adjusted.getPixel(88, 50))
            assertEquals(0, ProHitMath.pick(restored, .80f, .58f, 100, 100))
            assertNull(ProHitMath.pick(restored, .88f, .50f, 100, 100))
        } finally { adjusted.recycle() }

        val wide = render(moved(linked), width = 160)
        try {
            assertEquals(Color.RED, wide.getPixel(117, 50))
            assertEquals(Color.TRANSPARENT, wide.getPixel(125, 50))
        } finally { wide.recycle() }
    }

    @Test fun olderImageMasksAndNewAdjustmentSelectionsRemainInArtboardSpace() {
        val json = JSONObject(ProStore.encode(ProDocument(listOf(linked)))).apply {
            put("version", 4)
            getJSONArray("layers").getJSONObject(0).getJSONObject("mask").remove("imageAnchor")
        }
        val legacy = requireNotNull(ProStore.decode(json.toString())).layers.single()
        assertNull(legacy.mask?.anchor)
        val output = render(moved(legacy))
        try {
            assertEquals(Color.BLUE, output.getPixel(40, 70))
            assertEquals(Color.TRANSPARENT, output.getPixel(80, 50))
        } finally { output.recycle() }
        val adjustment = ProLayer("", kind = ProLayerKind.ADJUSTMENT)
        assertNull(ProToolMath.applySelection(draft, adjustment, 1f).anchor)
        assertFalse(ProMath.valid(ProDocument(listOf(adjustment.copy(mask = linked.mask)))))
    }
}

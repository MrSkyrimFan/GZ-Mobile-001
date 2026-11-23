package com.greenezone.gzphotoeditor

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.BrushPoint
import com.greenezone.gzphotoeditor.editor.ProAspect
import com.greenezone.gzphotoeditor.editor.ProBackground
import com.greenezone.gzphotoeditor.editor.ProDocument
import com.greenezone.gzphotoeditor.editor.ProHitMath
import com.greenezone.gzphotoeditor.editor.ProLayer
import com.greenezone.gzphotoeditor.editor.ProMath
import com.greenezone.gzphotoeditor.editor.ProRaster
import com.greenezone.gzphotoeditor.editor.ProSelection
import com.greenezone.gzphotoeditor.editor.ProSelectionKind
import com.greenezone.gzphotoeditor.editor.ProStore
import com.greenezone.gzphotoeditor.editor.ProToolMath
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProAspectRatioTest {
    @Test fun wideSourceMaskRemainsEditableAndRenderableAfterSwitchToPresetCanvas() {
        val source = ProLayer("content://fixture/panorama", name = "Wide image", aspect = 12f)
        val mask = ProToolMath.applySelection(
            ProSelection(kind = ProSelectionKind.RECTANGLE, width = .5f, height = .5f),
            source, 12f)
        assertEquals(12f, requireNotNull(mask.anchor).canvasAspect, 0f)
        assertEquals(12f, requireNotNull(mask.anchor).imageAspect, 0f)
        val original = ProDocument(
            layers = listOf(source.copy(mask = mask)),
            background = ProBackground.TRANSPARENT,
            sourceAspectRatio = 12f
        )
        val preset = original.copy(aspect = ProAspect.LANDSCAPE, sourceAspectRatio = null)
        val layerOnly = preset.copy(layers = listOf(source))
        val anchorOnly = preset.copy(layers = listOf(source.copy(aspect = 1f, mask = mask)))
        for (document in listOf(original, preset, layerOnly, anchorOnly)) {
            val encoded = ProStore.encode(document)
            assertEquals(6, JSONObject(encoded).getInt("version"))
            assertEquals(document, ProStore.decode(encoded))
            assertNull(ProStore.decode(JSONObject(encoded).put("version", 5).toString()))
        }
        val legacyCompatible = preset.copy(layers = listOf(source.copy(aspect = 1f)))
        assertEquals(5, JSONObject(ProStore.encode(legacyCompatible)).getInt("version"))

        for (document in listOf(original, preset)) {
            val restored = requireNotNull(ProStore.decode(ProStore.encode(document)))
            val (width, height) = ProMath.dimensions(restored, 120, 24_000_000)
            val pixels = ProRaster.renderWithLoader(restored, width, height, false) { _, _, _ ->
                Bitmap.createBitmap(120, 10, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.GREEN) }
            }
            try {
                assertEquals(Color.GREEN, pixels.getPixel(width / 2, height / 2))
                assertEquals(0, Color.alpha(pixels.getPixel(4, height / 2)))
                assertEquals(0, ProHitMath.pick(restored, .5f, .5f, width, height))
                assertNull(ProHitMath.pick(restored, .04f, .5f, width, height))
            } finally { pixels.recycle() }
        }
    }

    @Test fun tallSourceFitDoesNotRoundTheLayerWidthToZero() {
        val layer = ProLayer("content://fixture/tall", aspect = 1f / 2000)
        val rect = ProMath.bounds(layer, 1, 2000)
        assertEquals(1f, rect.right - rect.left, .0001f)
        assertEquals(2000f, rect.bottom - rect.top, .001f)
        val center = requireNotNull(ProMath.canvasToImageNormalized(layer, 1, 2000, BrushPoint(.5f, .5f)))
        assertEquals(.5f, center.x, .0001f)
        assertEquals(.5f, center.y, .0001f)
    }
}

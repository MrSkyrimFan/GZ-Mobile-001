package com.greenezone.gzphotoeditor

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.*
import com.greenezone.gzphotoeditor.ui.ProWorkspaceScreen
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@U1TargetedCheck
@RunWith(AndroidJUnit4::class)
class ProBlendRasterTest {
    @get:Rule val rule = createComposeRule()

    private val topColor = Color.rgb(67, 188, 220)
    private val lowerColor = Color.rgb(173, 56, 118)
    private val modes = listOf(ProBlend.NORMAL, ProBlend.SCREEN, ProBlend.OVERLAY,
        ProBlend.HARD_LIGHT, ProBlend.DARKEN, ProBlend.MULTIPLY)
    private val opacities = listOf(0f, .04f, .76f, .98f, 1f)

    private fun channelBlend(mode: ProBlend, top: Double, lower: Double): Double = when (mode) {
        ProBlend.NORMAL -> top
        ProBlend.SCREEN -> 1 - (1 - top) * (1 - lower)
        ProBlend.MULTIPLY -> top * lower
        ProBlend.DARKEN -> minOf(top, lower)
        ProBlend.OVERLAY -> if (lower < .5) 2 * lower * top else 1 - 2 * (1 - lower) * (1 - top)
        ProBlend.HARD_LIGHT -> if (top < .5) 2 * top * lower else 1 - 2 * (1 - top) * (1 - lower)
        ProBlend.SOFT_LIGHT -> if (top <= .5) lower - (1 - 2 * top) * lower * (1 - lower)
            else {
                val curve = if (lower <= .25) ((16 * lower - 12) * lower + 4) * lower else sqrt(lower)
                lower + (2 * top - 1) * (curve - lower)
            }
        ProBlend.LIGHTEN -> maxOf(top, lower)
        ProBlend.COLOR_DODGE -> when {
            lower == 0.0 -> 0.0
            top == 1.0 -> 1.0
            else -> minOf(1.0, lower / (1 - top))
        }
        ProBlend.COLOR_BURN -> when {
            lower == 1.0 -> 1.0
            top == 0.0 -> 0.0
            else -> 1 - minOf(1.0, (1 - lower) / top)
        }
        ProBlend.DIFFERENCE -> abs(lower - top)
        ProBlend.EXCLUSION -> lower + top - 2 * lower * top
        ProBlend.HUE, ProBlend.SATURATION, ProBlend.COLOR, ProBlend.LUMINOSITY ->
            error("Nonseparable mode requires the RGB oracle")
    }

    private fun expectedOpaque(mode: ProBlend, top: Int, lower: Int, opacity: Float): Int {
        val coverage = (opacity * 255).roundToInt() / 255.0
        fun result(s: Int, d: Int): Int =
            ((1 - coverage) * d + coverage * 255 * channelBlend(mode, s / 255.0, d / 255.0))
                .roundToInt()
        return Color.rgb(result(Color.red(top), Color.red(lower)),
            result(Color.green(top), Color.green(lower)), result(Color.blue(top), Color.blue(lower)))
    }

    private fun assertNear(message: String, expected: Int, actual: Int) {
        assertTrue("$message expected #${expected.toUInt().toString(16)}, got #${actual.toUInt().toString(16)}",
            abs(Color.alpha(expected) - Color.alpha(actual)) <= 3 &&
                abs(Color.red(expected) - Color.red(actual)) <= 3 &&
                abs(Color.green(expected) - Color.green(actual)) <= 3 &&
                abs(Color.blue(expected) - Color.blue(actual)) <= 3)
    }

    @Test fun imageBlendsMatchIndependentPixelEquationsOnCanvasAndAnotherLayer() {
        val lower = ProLayer(uri = "content://test/lower", name = "Bottom")
        val top = ProLayer(uri = "content://test/top", name = "Top")
        fun verify(document: ProDocument, expected: Int, expectedOutside: Int? = null) {
            for (preview in listOf(true, false)) {
                val bitmap = ProRaster.renderWithLoader(document, 32, 32, preview) { layer, _, _ ->
                    Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888).apply {
                        eraseColor(if (layer.uri == top.uri) topColor else lowerColor)
                    }
                }
                try {
                    assertNear("Mode ${document.layers.last().blend}, preview $preview", expected,
                        bitmap.getPixel(16, 16))
                    expectedOutside?.let {
                        assertNear("Pixel outside selection", it, bitmap.getPixel(1, 1))
                    }
                } finally { bitmap.recycle() }
            }
        }
        for (mode in modes) {
            for (opacity in opacities) {
                val paint = top.copy(blend = mode, opacity = opacity)
                verify(ProDocument(listOf(paint), background = ProBackground.WHITE),
                    expectedOpaque(mode, topColor, Color.WHITE, opacity))
                verify(ProDocument(listOf(paint), background = ProBackground.BLACK),
                    expectedOpaque(mode, topColor, Color.BLACK, opacity))
                verify(ProDocument(listOf(lower, paint), background = ProBackground.TRANSPARENT),
                    expectedOpaque(mode, topColor, lowerColor, opacity))
            }
            verify(ProDocument(listOf(top.copy(blend = mode, opacity = .76f)),
                background = ProBackground.TRANSPARENT),
                Color.argb((.76f * 255).roundToInt(), Color.red(topColor), Color.green(topColor),
                    Color.blue(topColor)))
            val masked = top.copy(blend = mode, opacity = .76f,
                mask = ProSelection(kind = ProSelectionKind.RECTANGLE, width = .4f, height = .4f))
            verify(ProDocument(listOf(lower, masked), background = ProBackground.TRANSPARENT),
                expectedOpaque(mode, topColor, lowerColor, .76f), lowerColor)
        }
    }

    @Test fun allModesMatchIndependentSrgbBlendWithPartialBackdropAndTwoPatches() {
        val lowerPixels = intArrayOf(Color.argb(173, 66, 39, 109), Color.argb(217, 186, 205, 141))
        val topPixels = intArrayOf(Color.rgb(166, 131, 46), Color.rgb(53, 147, 226))
        val lower = ProLayer(uri = "content://test/lower", name = "Bottom")
        val upper = ProLayer(uri = "content://test/upper", name = "Top", opacity = .76f)
        ProBlend.entries.forEach { mode ->
            for (preview in listOf(true, false)) {
                val composition = ProDocument(listOf(lower, upper.copy(blend = mode)),
                    background = ProBackground.TRANSPARENT)
                val image = ProRaster.renderWithLoader(composition, 48, 48, preview) { layer, _, _ ->
                    val patches = if (layer.uri == upper.uri) topPixels else lowerPixels
                    Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888).apply {
                        for (y in 0 until height) for (x in 0 until width) {
                            setPixel(x, y, patches[if (x < width / 2) 0 else 1])
                        }
                    }
                }
                try {
                    for (patch in lowerPixels.indices) {
                        val expected = referenceComposite(mode, topPixels[patch], lowerPixels[patch], upper.opacity)
                        assertNear("$mode patch $patch preview=$preview", expected,
                            image.getPixel(if (patch == 0) 12 else 36, 24))
                    }
                } finally { image.recycle() }
            }
        }
    }

    // Blend straight sRGB component values, then apply source-over coverage. The
    // production implementation delegates both operations to Android Canvas.
    private fun referenceComposite(mode: ProBlend, top: Int, lower: Int, opacity: Float): Int {
        fun rgb(pixel: Int) = doubleArrayOf(Color.red(pixel) / 255.0,
            Color.green(pixel) / 255.0, Color.blue(pixel) / 255.0)
        val source = rgb(top)
        val backdrop = rgb(lower)
        val mixed = when (mode) {
            ProBlend.HUE -> setLuminosity(setSaturation(source, saturation(backdrop)), luminosity(backdrop))
            ProBlend.SATURATION -> setLuminosity(setSaturation(backdrop, saturation(source)), luminosity(backdrop))
            ProBlend.COLOR -> setLuminosity(source, luminosity(backdrop))
            ProBlend.LUMINOSITY -> setLuminosity(backdrop, luminosity(source))
            else -> DoubleArray(3) { channelBlend(mode, source[it], backdrop[it]) }
        }
        val a = Color.alpha(top) * (opacity * 255).roundToInt() / (255.0 * 255)
        val b = Color.alpha(lower) / 255.0
        val outAlpha = a + b * (1 - a)
        if (outAlpha == 0.0) return Color.TRANSPARENT
        fun component(i: Int): Int {
            val premultiplied = source[i] * a * (1 - b) + mixed[i] * a * b + backdrop[i] * b * (1 - a)
            return (255 * premultiplied / outAlpha).roundToInt().coerceIn(0, 255)
        }
        return Color.argb((outAlpha * 255).roundToInt(), component(0), component(1), component(2))
    }

    private fun luminosity(rgb: DoubleArray) = .3 * rgb[0] + .59 * rgb[1] + .11 * rgb[2]
    private fun saturation(rgb: DoubleArray) = rgb.max() - rgb.min()

    private fun setLuminosity(rgb: DoubleArray, target: Double): DoubleArray {
        val shifted = DoubleArray(3) { rgb[it] + target - luminosity(rgb) }
        val low = shifted.min()
        if (low < 0) {
            shifted.indices.forEach { i -> shifted[i] = target + (shifted[i] - target) * target / (target - low) }
        }
        val high = shifted.max()
        if (high > 1) {
            shifted.indices.forEach { i ->
                shifted[i] = target + (shifted[i] - target) * (1 - target) / (high - target)
            }
        }
        return shifted
    }

    private fun setSaturation(rgb: DoubleArray, target: Double): DoubleArray {
        val (low, middle, high) = rgb.indices.sortedBy { rgb[it] }
        val spread = rgb[high] - rgb[low]
        val result = DoubleArray(3)
        if (spread > 0) {
            result[middle] = (rgb[middle] - rgb[low]) * target / spread
            result[high] = target
        }
        return result
    }

    @Test fun blendPanelExplainsWhiteCanvasAndOneTapRestoresVisibleColors() {
        val shape = ProLayer(uri = "", name = "Blue shape", kind = ProLayerKind.SHAPE,
            edits = EditDocument(creative = CreativeDocument(shapes = listOf(
                ShapeOverlay(color = Color.BLUE, filled = true)))))
        lateinit var current: ProDocument
        rule.setContent {
            var document by remember { mutableStateOf(ProDocument(listOf(shape))) }
            current = document
            MaterialTheme(colorScheme = darkColorScheme()) {
                ProWorkspaceScreen(document, { document = it }, {}, {})
            }
        }
        rule.onNodeWithTag("workspace-tools").performTouchInput { click(center) }
        rule.onNodeWithText("Blend").performTouchInput { click(center) }
        rule.onNodeWithText("Backdrop: White canvas").assertIsDisplayed()
        rule.onNodeWithText("Screen and Overlay produce white over this white canvas.").assertIsDisplayed()
        rule.onNodeWithText("Blend mode: Normal").performScrollTo().performTouchInput { click(center) }
        rule.onNodeWithText("Screen").performScrollTo().performTouchInput { click(center) }
        fun centerIs(expected: Int): Boolean {
            val image = rule.onNodeWithTag("pro-canvas").captureToImage().asAndroidBitmap()
            return try { image.getPixel(image.width / 2, image.height / 2) == expected }
            finally { image.recycle() }
        }
        rule.waitUntil(15_000) { centerIs(Color.WHITE) }
        rule.runOnIdle { assertEquals(ProBlend.SCREEN, current.layers.single().blend) }
        rule.onNodeWithText("Restore Normal").performScrollTo().performTouchInput { click(center) }
        rule.waitUntil(15_000) { centerIs(Color.BLUE) }
        rule.runOnIdle { assertEquals(ProBlend.NORMAL, current.layers.single().blend) }
        rule.onNodeWithContentDescription("Undo").assertIsEnabled()
    }
}

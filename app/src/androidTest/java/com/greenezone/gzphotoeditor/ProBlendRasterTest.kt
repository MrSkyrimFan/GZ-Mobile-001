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
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.*
import com.greenezone.gzphotoeditor.ui.ProWorkspaceScreen
import kotlin.math.abs
import kotlin.math.roundToInt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
        else -> error("No fixture formula configured for $mode")
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
        rule.onNodeWithText("Tools ▾").performClick()
        rule.onNodeWithText("Blend").performClick()
        rule.onNodeWithText("Backdrop: White canvas").assertIsDisplayed()
        rule.onNodeWithText("Screen and Overlay produce white over this white canvas.").assertIsDisplayed()
        rule.onNodeWithText("Screen").performScrollTo().performClick()
        fun centerIs(expected: Int): Boolean {
            val image = rule.onNodeWithTag("pro-canvas").captureToImage().asAndroidBitmap()
            val actual = image.getPixel(image.width / 2, image.height / 2)
            return actual == expected
        }
        rule.waitUntil(15_000) { centerIs(Color.WHITE) }
        rule.runOnIdle { assertEquals(ProBlend.SCREEN, current.layers.single().blend) }
        rule.onNodeWithText("Restore Normal").performScrollTo().performClick()
        rule.waitUntil(15_000) { centerIs(Color.BLUE) }
        rule.runOnIdle { assertEquals(ProBlend.NORMAL, current.layers.single().blend) }
        rule.onNodeWithText("Undo").assertIsEnabled()
    }
}

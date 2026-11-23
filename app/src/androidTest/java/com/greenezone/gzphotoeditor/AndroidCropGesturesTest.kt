package com.greenezone.gzphotoeditor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.CropFrame
import com.greenezone.gzphotoeditor.editor.CropRatio
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.ExportSettings
import com.greenezone.gzphotoeditor.editor.ImageExportFormat
import com.greenezone.gzphotoeditor.editor.ImageExportWriter
import com.greenezone.gzphotoeditor.editor.ImagePipeline
import com.greenezone.gzphotoeditor.ui.CropFrameOverlay
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import kotlin.math.min

@RunWith(AndroidJUnit4::class)
class AndroidCropGesturesTest {
    @get:Rule val rule = createComposeRule()
    private val frame = mutableStateOf(CropFrame(.2f, .2f, .8f, .8f))
    private var gestureStart = frame.value
    private var finished = 0
    private var canceled = 0
    private var tapped = 0

    private fun show(imageWidth: Int, imageHeight: Int, density: Float, ratio: Float? = null) {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density)) {
                Box(Modifier.fillMaxSize()) {
                    CropFrameOverlay(
                        imageWidth = imageWidth,
                        imageHeight = imageHeight,
                        frame = frame.value,
                        aspectRatio = ratio,
                        showGuides = true,
                        onChange = { frame.value = it },
                        onFinished = { finished++ },
                        onCanceled = {
                            canceled++
                            frame.value = gestureStart
                        },
                        onTap = { tapped++ }
                    )
                }
            }
        }
    }

    private data class CanvasImage(val left: Float, val top: Float, val w: Float, val h: Float) {
        fun screen(x: Float, y: Float) = Offset(left + w * x, top + h * y)
    }

    private fun image(width: Int, height: Int): CanvasImage {
        val node = rule.onNodeWithTag("crop-frame").fetchSemanticsNode()
        val scale = min(node.size.width.toFloat() / width, node.size.height.toFloat() / height)
        val w = width * scale
        val h = height * scale
        return CanvasImage((node.size.width - w) / 2f, (node.size.height - h) / 2f, w, h)
    }

    private fun assertFrame(want: CropFrame) {
        rule.runOnIdle {
            val now = frame.value
            assertEquals(want.left, now.left, .002f)
            assertEquals(want.top, now.top, .002f)
            assertEquals(want.right, now.right, .002f)
            assertEquals(want.bottom, now.bottom, .002f)
        }
    }

    private data class Probe(
        val x: Float,
        val y: Float,
        val towardGutter: Offset,
        val dx: Float,
        val dy: Float,
        val expected: CropFrame
    )

    private fun allTargets(width: Int, height: Int, density: Float) {
        show(width, height, density)
        val photo = image(width, height)
        val original = CropFrame(.2f, .2f, .8f, .8f)
        val probes = listOf(
            Probe(.2f, .2f, Offset(-1f, -1f), .08f, .08f, CropFrame(.28f, .28f, .8f, .8f)),
            Probe(.8f, .2f, Offset(1f, -1f), -.08f, .08f, CropFrame(.2f, .28f, .72f, .8f)),
            Probe(.2f, .8f, Offset(-1f, 1f), .08f, -.08f, CropFrame(.28f, .2f, .8f, .72f)),
            Probe(.8f, .8f, Offset(1f, 1f), -.08f, -.08f, CropFrame(.2f, .2f, .72f, .72f)),
            Probe(.2f, .5f, Offset(-1f, 0f), .08f, 0f, CropFrame(.28f, .2f, .8f, .8f)),
            Probe(.5f, .2f, Offset(0f, -1f), 0f, .08f, CropFrame(.2f, .28f, .8f, .8f)),
            Probe(.8f, .5f, Offset(1f, 0f), -.08f, 0f, CropFrame(.2f, .2f, .72f, .8f)),
            Probe(.5f, .8f, Offset(0f, 1f), 0f, -.08f, CropFrame(.2f, .2f, .8f, .72f)),
            Probe(.5f, .5f, Offset.Zero, .08f, .08f, CropFrame(.28f, .28f, .88f, .88f))
        )
        for (offsetDp in listOf(16f, 24f, 32f)) {
            for (probe in probes) {
                rule.runOnIdle {
                    frame.value = original
                    finished = 0
                    canceled = 0
                    tapped = 0
                }
                val start = photo.screen(probe.x, probe.y) +
                    probe.towardGutter * (offsetDp * density - .25f)
                val end = start + Offset(photo.w * probe.dx, photo.h * probe.dy)
                rule.onNodeWithTag("crop-frame").performTouchInput {
                    down(start)
                    moveTo(end)
                    up()
                }
                assertFrame(probe.expected)
                rule.runOnIdle {
                    assertEquals(1, finished)
                    assertEquals(0, canceled)
                    assertEquals(0, tapped)
                }
            }
        }
    }

    @Test fun eightHandlesAndMoveAcquireAtOneTimesDensityOnLandscape() =
        allTargets(300, 200, 1f)

    @Test fun eightHandlesAndMoveAcquireAtTwoTimesDensityOnPortrait() =
        allTargets(200, 300, 2f)

    @Test fun fixedRatioEdgeKeepsItsOppositeEdgeAndRollsBackAPointerCancellation() {
        val original = CropFrame(.3f, .2f, .7f, .8f)
        frame.value = original
        show(300, 200, 2f, ratio = 1f)
        val photo = image(300, 200)
        val start = photo.screen(.7f, .5f) + Offset(48f, 0f)
        val end = start - Offset(photo.w / 10f, 0f)
        rule.onNodeWithTag("crop-frame").performTouchInput {
            down(start)
            moveTo(end)
            up()
        }
        val resized = CropFrame(.3f, .275f, .6f, .725f)
        assertFrame(resized)
        rule.runOnIdle { gestureStart = resized }
        rule.onNodeWithTag("crop-frame").performTouchInput {
            val edge = photo.screen(.6f, .5f)
            down(0, edge)
            moveTo(0, edge - Offset(photo.w / 10f, 0f))
            down(1, photo.screen(.45f, .5f))
            up(1)
            up(0)
        }
        assertFrame(resized)
        rule.runOnIdle {
            assertEquals(1, finished)
            assertEquals(1, canceled)
            assertEquals(0, tapped)
        }
    }

    @Test fun edgeDragControlsTheExactPixelsInLosslessOutput() {
        show(20, 10, 1f)
        val photo = image(20, 10)
        val start = photo.screen(.8f, .5f) + Offset(24f, 0f)
        rule.onNodeWithTag("crop-frame").performTouchInput {
            down(start)
            moveTo(start - Offset(photo.w / 10f, 0f))
            up()
        }
        val crop = rule.runOnIdle { frame.value }
        assertFrame(CropFrame(.2f, .2f, .7f, .8f))
        val pixels = IntArray(200) { i -> Color.rgb(i % 20 * 11, i / 20 * 23, i) }
        val source = Bitmap.createBitmap(pixels, 20, 10, Bitmap.Config.ARGB_8888)
        try {
            val rendered = ImagePipeline.render(source, EditDocument(crop = CropRatio.FREE, cropFrame = crop))
            try {
                val buffer = ByteArrayOutputStream()
                check(ImageExportWriter.write(rendered, ExportSettings(format = ImageExportFormat.PNG), buffer))
                val bytes = buffer.toByteArray()
                val exported = requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                try {
                    assertEquals(10 to 6, exported.width to exported.height)
                    for (y in 0 until exported.height) for (x in 0 until exported.width) {
                        assertEquals(pixels[(y + 2) * 20 + x + 4], exported.getPixel(x, y))
                    }
                } finally {
                    exported.recycle()
                }
            } finally {
                if (rendered !== source) rendered.recycle()
            }
        } finally {
            source.recycle()
        }
    }
}

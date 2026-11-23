package com.greenezone.gzphotoeditor

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.*
import com.greenezone.gzphotoeditor.ui.ProWorkspaceScreen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProCanvasInteractionTest {
    @get:Rule val rule = createComposeRule()
    private lateinit var result: ProDocument
    private val canvas get() = rule.onNodeWithContentDescription("Pro editable composition")

    private fun open(document: ProDocument) {
        result = document
        rule.setContent {
            var current by remember { mutableStateOf(document) }
            MaterialTheme {
                ProWorkspaceScreen(current, { result = it; current = it }, {}, {})
            }
        }
        rule.waitUntil(15_000) {
            rule.onAllNodesWithContentDescription("Pro editable composition")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun tool(name: String) {
        rule.onNodeWithText("Tools ▾").performClick()
        rule.onNodeWithText(name).performScrollTo().performClick()
    }

    @Test fun hiddenBrushDrawsAndPinchNeverPaintsOrRestoresTheControls() {
        open(ProDocument(listOf(ProLayer(uri = "", name = "Ink", kind = ProLayerKind.DRAWING))))
        tool("Brush")
        canvas.performTouchInput { click(center) }
        rule.onNodeWithText("Tools ▾").assertDoesNotExist()
        canvas.performTouchInput {
            val r = minOf(width, height) * .13f
            down(0, center - Offset(r, 0f))
            down(1, center + Offset(r, 0f))
            updatePointerTo(0, center - Offset(2 * r, 0f))
            updatePointerTo(1, center + Offset(2 * r, 0f))
            move()
            up(0)
            up(1)
        }
        rule.runOnIdle { assertTrue(result.layers.single().edits.creative.strokes.isEmpty()) }
        rule.onNodeWithText("Tools ▾").assertDoesNotExist()
        canvas.performTouchInput {
            val r = minOf(width, height) * .15f
            swipe(center - Offset(r, 0f), center + Offset(r, 0f))
        }
        rule.runOnIdle {
            val stroke = result.layers.single().edits.creative.strokes.single()
            assertTrue(stroke.points.size > 1)
            val span = stroke.points.last().x - stroke.points.first().x
            // A 30%-of-fit drag covers only 15% of a canvas zoomed to 2×.
            assertTrue("Zoomed brush mapped to source coordinates: $span", span in .12f.. .18f)
        }
        rule.waitUntil(15_000) {
            val frame = canvas.captureToImage()
            val color = frame.toPixelMap()[frame.width / 2, frame.height / 2]
            color.green > color.red + .15f && color.green > color.blue
        }
        rule.onNodeWithText("Tools ▾").assertDoesNotExist()
        canvas.performTouchInput { click(center) }
        rule.onNodeWithText("Undo").performClick()
        rule.runOnIdle { assertTrue(result.layers.single().edits.creative.strokes.isEmpty()) }
        rule.onNodeWithText("Redo").performClick()
        rule.runOnIdle { assertEquals(1, result.layers.single().edits.creative.strokes.size) }
    }

    @Test fun brushIsCompositedUnderTheUpperPhotoDuringDragAndSecondPointerCancelsIt() {
        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "gz-pro-live-occlusion.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        try {
            val photo = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
            photo.eraseColor(Color.BLUE)
            try {
                resolver.openOutputStream(uri)!!.use { assertTrue(photo.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            } finally { photo.recycle() }
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)

            val lower = ProLayer(uri = "", name = "Lower brush", kind = ProLayerKind.DRAWING,
                opacity = .8f)
            val upper = ProLayer(uri = uri.toString(), name = "Upper blue photo", scale = .32f,
                mask = ProSelection(ProSelectionKind.RECTANGLE, width = .22f, height = .5f))
            val original = ProDocument(listOf(lower, upper))
            open(original)
            tool("Brush")
            val startedAt = SystemClock.elapsedRealtime()
            canvas.performTouchInput {
                val radius = minOf(width, height) * .36f
                down(0, center - Offset(radius, 0f))
                moveTo(0, center + Offset(radius, 0f))
            }

            rule.waitUntil(10_000) {
                val frame = canvas.captureToImage()
                val pixels = frame.toPixelMap()
                val middle = pixels[frame.width / 2, frame.height / 2]
                val left = pixels[frame.width / 2 - (minOf(frame.width, frame.height) * .28f).toInt(),
                    frame.height / 2]
                val exposed = pixels[frame.width / 2 - (minOf(frame.width, frame.height) * .14f).toInt(),
                    frame.height / 2]
                middle.blue > .9f && middle.red < .1f && middle.green < .1f &&
                    left.red in .63f.. .72f && left.green in .87f.. .96f &&
                    exposed.red in .63f.. .72f && exposed.green in .87f.. .96f
            }
            Log.i("GzProPaintTiming", "Live masked brush pixels observed in ${SystemClock.elapsedRealtime() - startedAt}ms")
            rule.runOnIdle { assertEquals("The active brush must stay out of the saved recipe", original, result) }
            canvas.performTouchInput {
                down(1, center + Offset(0f, 24f))
                up(1)
                up(0)
            }
            rule.waitUntil(10_000) {
                val frame = canvas.captureToImage()
                val pixels = frame.toPixelMap()
                val left = pixels[frame.width / 2 - (minOf(frame.width, frame.height) * .28f).toInt(),
                    frame.height / 2]
                left.red > .95f && left.green > .95f && left.blue > .95f
            }
            rule.runOnIdle { assertEquals(original, result) }
            rule.onNodeWithText("Undo").assertIsNotEnabled()
        } finally { resolver.delete(uri, null, null) }
    }

    @Test fun eraserRevealsUnderlyingPixelsBeforeReleaseAndCommitsOneEditableStroke() {
        val ink = InkStroke(listOf(BrushPoint(.10f, .5f), BrushPoint(.90f, .5f)),
            color = Color.BLUE, thickness = .075f)
        open(ProDocument(listOf(ProLayer(uri = "", name = "Ink", kind = ProLayerKind.DRAWING,
            edits = EditDocument(creative = CreativeDocument(strokes = listOf(ink)))))))
        tool("Brush")
        rule.onNodeWithText("Eraser").performClick()
        rule.waitUntil(10_000) {
            val prior = canvas.captureToImage()
            val previousCenter = prior.toPixelMap()[prior.width / 2, prior.height / 2]
            previousCenter.blue > .9f && previousCenter.red < .1f
        }

        val startedAt = SystemClock.elapsedRealtime()
        canvas.performTouchInput {
            val radius = minOf(width, height) * .2f
            down(center - Offset(radius, 0f))
            moveTo(center + Offset(radius, 0f))
        }
        rule.waitUntil(10_000) {
            val frame = canvas.captureToImage()
            val pixels = frame.toPixelMap()
            val middle = pixels[frame.width / 2, frame.height / 2]
            val left = pixels[frame.width / 2 - (minOf(frame.width, frame.height) * .32f).toInt(),
                frame.height / 2]
            middle.red > .95f && middle.green > .95f && middle.blue > .95f &&
                left.blue > .9f && left.red < .1f && left.green < .1f
        }
        Log.i("GzProPaintTiming", "Live erased pixels observed in ${SystemClock.elapsedRealtime() - startedAt}ms")
        rule.runOnIdle { assertEquals(listOf(ink), result.layers.single().edits.creative.strokes) }
        canvas.performTouchInput { up() }
        rule.runOnIdle {
            val saved = result.layers.single().edits.creative.strokes
            assertEquals(2, saved.size)
            assertTrue(saved.last().erase)
            val alpha = ProRaster.renderWithLoader(result.copy(background = ProBackground.TRANSPARENT),
                100, 100, false) { _, _, _ -> error("A drawing-only project needs no media source") }
            try {
                assertEquals(0, Color.alpha(alpha.getPixel(50, 50)))
                assertEquals(Color.BLUE, alpha.getPixel(18, 50))
            } finally { alpha.recycle() }
        }
        rule.onNodeWithText("Undo").performClick()
        rule.runOnIdle { assertEquals(listOf(ink), result.layers.single().edits.creative.strokes) }
        rule.onNodeWithText("Redo").performClick()
        rule.runOnIdle { assertTrue(result.layers.single().edits.creative.strokes.last().erase) }
    }

    @Test fun maskDraftSurvivesHiddenCanvasAndCommitsOnTheSelectedAdjustmentLayer() {
        open(ProDocument(listOf(ProLayer(uri = "", name = "Color grade", kind = ProLayerKind.ADJUSTMENT))))
        tool("Select & mask")
        canvas.performTouchInput { click(center) }
        canvas.performTouchInput {
            val r = minOf(width, height) * .2f
            swipe(center - Offset(r, r), center + Offset(r, r))
        }
        rule.runOnIdle { assertNull(result.layers.single().mask) }
        canvas.performTouchInput { click(center) }
        rule.onNodeWithText("Apply layer mask").performClick()
        rule.runOnIdle {
            val mask = requireNotNull(result.layers.single().mask)
            assertEquals(.4f, mask.width, .025f)
            assertEquals(.4f, mask.height, .025f)
        }
        rule.onNodeWithText("File ▾").performClick()
        rule.onNodeWithText("Pro help").performClick()
        rule.onNodeWithText("Photo Pro help").assertIsDisplayed()
    }

    @Test fun shortTapNeverStampsAndTwoHoldsRepairWhileControlsStayHidden() {
        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "gz-pro-gesture-fixture.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        try {
            val source = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
            source.eraseColor(Color.BLUE)
            for (y in 0 until 48) for (x in 24 until 48) source.setPixel(x, y, Color.RED)
            try {
                resolver.openOutputStream(uri)!!.use { source.compress(Bitmap.CompressFormat.PNG, 100, it) }
            } finally { source.recycle() }
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            open(ProDocument(listOf(ProLayer(uri = uri.toString(), name = "Test photo"))))
            tool("Clone & repair")
            canvas.performTouchInput { click(center) }
            canvas.performTouchInput { longClick(center + Offset(minOf(width, height) * .22f, 0f)) }
            rule.runOnIdle { assertTrue(result.layers.single().retouches.isEmpty()) }
            canvas.performTouchInput { longClick(center - Offset(minOf(width, height) * .22f, 0f)) }
            rule.onNodeWithText("Tools ▾").assertDoesNotExist()
            rule.runOnIdle {
                val mark = result.layers.single().retouches.single()
                assertEquals(.72f, mark.sourceX, .025f)
                assertEquals(.28f, mark.targetX, .025f)
            }
            canvas.performTouchInput { click(center) }
            rule.onNodeWithText("Reset source").assertIsEnabled()
            rule.runOnIdle { assertEquals(1, result.layers.single().retouches.size) }
        } finally { resolver.delete(uri, null, null) }
    }
}

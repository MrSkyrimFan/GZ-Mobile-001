package com.greenezone.gzphotoeditor

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.provider.MediaStore
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@RunWith(AndroidJUnit4::class)
class AndroidProLassoGestureTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val resolver get() = context.contentResolver
    private val canvas get() = rule.onNodeWithContentDescription("Pro editable composition")

    private fun click(text: String) = rule.onNodeWithText(text).performClick()

    private fun switchToPro() {
        rule.onNodeWithContentDescription("Choose editing workspace").performClick()
        click("Photo editing Pro")
    }

    private fun openProject(name: String) {
        rule.onNodeWithText("Options").performClick()
        click("Open project…")
        rule.waitUntil(15_000) { rule.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText(name).performScrollTo().performClick()
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText("Saved Pro projects").fetchSemanticsNodes().isEmpty() &&
                rule.onAllNodesWithContentDescription("Pro editable composition").fetchSemanticsNodes().isNotEmpty() &&
                rule.onAllNodes(hasContentDescription("Choose editing workspace") and isEnabled())
                    .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun selectFreehand() {
        click("Tools ▾")
        rule.onNodeWithText("Select & mask").performScrollTo().performClick()
        rule.onNodeWithText("Freehand").performScrollTo().performClick()
    }

    private fun saveProject(name: String) {
        val previous = requireNotNull(ProStore.loadNamed(context, name)).project
        rule.onNodeWithText("Options").performClick()
        click("Save project")
        rule.waitUntil(15_000) {
            val saved = ProStore.loadNamed(context, name)?.project
            saved != null && saved.id == previous.id && saved.version != previous.version &&
                rule.onAllNodes(hasContentDescription("Choose editing workspace") and isEnabled())
                    .fetchSemanticsNodes().isNotEmpty()
        }
        rule.onAllNodesWithText("Save Pro project").assertCountEquals(0)
    }

    private fun withPhoto(body: (String) -> Unit) {
        val suffix = System.nanoTime().toString().takeLast(10)
        val name = "Lasso $suffix"
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "gz-lasso-$suffix.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        try {
            val photo = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
            photo.eraseColor(Color.BLUE)
            try {
                resolver.openOutputStream(uri)!!.use {
                    assertTrue(photo.compress(Bitmap.CompressFormat.PNG, 100, it))
                }
            } finally { photo.recycle() }
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            assertTrue(ProStore.save(context, name,
                ProDocument(listOf(ProLayer(uri = uri.toString(), name = "Blue source")))))
            switchToPro()
            openProject(name)
            rule.waitUntil(10_000) {
                rule.onAllNodesWithText("Pro project opened").fetchSemanticsNodes().isEmpty()
            }
            body(name)
        } finally {
            ProStore.delete(context, name)
            resolver.delete(uri, null, null)
        }
    }

    private fun assertPreview(masked: Boolean, lateQuadrantBlue: Boolean = true) {
        rule.waitUntil(15_000) {
            val frame = canvas.captureToImage()
            val pixels = frame.toPixelMap()
            val edge = minOf(frame.width, frame.height)
            fun sample(x: Float, y: Float) = pixels[
                (frame.width / 2f + (x - .5f) * edge).toInt(),
                (frame.height / 2f + (y - .5f) * edge).toInt()]
            fun blue(x: Float, y: Float): Boolean {
                val p = sample(x, y)
                return p.blue > .95f && p.red < .05f && p.green < .05f
            }
            fun white(x: Float, y: Float): Boolean {
                val p = sample(x, y)
                return p.red > .95f && p.green > .95f && p.blue > .95f
            }
            blue(.4f, .4f) &&
                (if (masked) white(.1f, .1f) else blue(.1f, .1f)) &&
                (if (lateQuadrantBlue) blue(.7f, .3f) else white(.7f, .3f))
        }
    }

    private fun drawLongContour(release: Boolean) {
        // Two calls keep the pointer held while the real UI recomposes.
        canvas.performTouchInput {
            val edge = minOf(width, height).toFloat()
            down(center + Offset(.3f * edge, 0f))
            // Cross touch slop before the densely sampled contour begins.
            moveTo(center + Offset(.29f * edge, .06f * edge))
            for (i in 1..300) {
                val angle = 2 * PI * i / 620
                moveTo(center + Offset((.3 * cos(angle)).toFloat() * edge,
                    (.3 * sin(angle)).toFloat() * edge))
            }
        }
        rule.waitForIdle()
        canvas.performTouchInput {
            val edge = minOf(width, height).toFloat()
            for (i in 301..620) {
                val angle = 2 * PI * i / 620
                moveTo(center + Offset((.3 * cos(angle)).toFloat() * edge,
                    (.3 * sin(angle)).toFloat() * edge))
            }
            if (release) up()
        }
    }

    @Test fun completeLongContourKeepsLatePixelsThroughMaskHistorySaveAndRecreation() = withPhoto { name ->
        selectFreehand()
        drawLongContour(release = true)
        rule.onNodeWithText("Apply layer mask").performScrollTo().performClick()
        // The old 450-point cutoff closes the polygon before this late quadrant.
        assertPreview(masked = true)
        rule.onNodeWithContentDescription("Undo").performClick()
        assertPreview(masked = false)
        rule.onNodeWithContentDescription("Redo").performClick()
        assertPreview(masked = true)
        saveProject(name)

        val saved = requireNotNull(ProStore.load(context, name))
        val mask = requireNotNull(saved.layers.single().mask)
        assertEquals(ProSelectionKind.LASSO, mask.kind)
        assertTrue(mask.points.size in 3..450)
        assertEquals(.8f, mask.points.last().x, .015f)
        assertEquals(.5f, mask.points.last().y, .015f)
        val rendered = ProRaster.render(resolver, saved, 256, 256, false)
        try {
            assertEquals(Color.BLUE, rendered.getPixel(179, 77))
            assertEquals(Color.WHITE, rendered.getPixel(25, 25))
        } finally { rendered.recycle() }

        rule.activityRule.scenario.recreate()
        switchToPro()
        openProject(name)
        assertPreview(masked = true)
        assertEquals(saved, ProStore.load(context, name))
    }

    @Test fun canceledReplacementContourRestoresTheEarlierUnappliedSelection() = withPhoto { name ->
        selectFreehand()
        canvas.performTouchInput {
            val edge = minOf(width, height).toFloat()
            fun at(x: Float, y: Float) = center + Offset((x - .5f) * edge, (y - .5f) * edge)
            down(at(.2f, .2f))
            moveTo(at(.6f, .2f))
            moveTo(at(.6f, .6f))
            moveTo(at(.2f, .6f))
            moveTo(at(.2f, .2f))
            up()
        }
        rule.onNodeWithText("Apply layer mask").assertIsEnabled()
        drawLongContour(release = false)
        canvas.performTouchInput {
            down(1, center)
            up(1)
            up(0)
        }
        // Second-pointer navigation cancels the replacement, not the earlier draft.
        rule.onNodeWithText("Apply layer mask").assertIsEnabled().performScrollTo().performClick()
        assertPreview(masked = true, lateQuadrantBlue = false)
        rule.onNodeWithContentDescription("Undo").performClick()
        assertPreview(masked = false)
        rule.onNodeWithContentDescription("Redo").performClick()
        assertPreview(masked = true, lateQuadrantBlue = false)
        saveProject(name)
        val mask = requireNotNull(ProStore.load(context, name)?.layers?.single()?.mask)
        assertTrue(mask.points.all { it.x <= .615f && it.y <= .615f })
    }
}

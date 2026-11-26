package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.CropFrame
import com.greenezone.gzphotoeditor.editor.CropRatio
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.Grade
import com.greenezone.gzphotoeditor.editor.ProDocument
import com.greenezone.gzphotoeditor.editor.ProLayer
import com.greenezone.gzphotoeditor.editor.ProLayerKind
import com.greenezone.gzphotoeditor.editor.ProPixelEffect
import com.greenezone.gzphotoeditor.editor.ProSelection
import com.greenezone.gzphotoeditor.editor.ProSelectionKind
import com.greenezone.gzphotoeditor.ui.ProWorkspaceScreen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProToolOwnershipTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private lateinit var current: ProDocument
    private val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver

    private fun open(initial: ProDocument, replacement: Uri? = null) {
        val owner = object : ActivityResultRegistryOwner {
            override val activityResultRegistry = object : ActivityResultRegistry() {
                override fun <I, O> onLaunch(
                    requestCode: Int, contract: ActivityResultContract<I, O>,
                    input: I, options: ActivityOptionsCompat?
                ) {
                    check(contract is ActivityResultContracts.PickVisualMedia)
                    dispatchResult(requestCode, Activity.RESULT_OK, Intent().setData(replacement))
                }
            }
        }
        rule.setContent {
            var document by remember { mutableStateOf(initial) }
            current = document
            MaterialTheme {
                if (replacement == null) {
                    ProWorkspaceScreen(document, { document = it }, {}, {})
                } else CompositionLocalProvider(LocalActivityResultRegistryOwner provides owner) {
                    ProWorkspaceScreen(document, { document = it }, {}, {})
                }
            }
        }
    }

    private fun tool(name: String) {
        rule.onNodeWithTag("workspace-tools").performClick()
        rule.onNodeWithText(name).performScrollTo().performClick()
    }

    @Test fun deletingSelectedLayerDoesNotApplyItsDraftToTheNextLayer() {
        val first = ProLayer("", name = "First adjustment", kind = ProLayerKind.ADJUSTMENT)
        val second = ProLayer("", name = "Second adjustment", kind = ProLayerKind.ADJUSTMENT)
        open(ProDocument(listOf(first, second)))
        tool("Select & mask")
        rule.onNodeWithTag("pro-canvas").performTouchInput {
            val distance = minOf(width, height) * .2f
            swipe(center - Offset(distance, distance), center + Offset(distance, distance))
        }
        rule.onNodeWithText("Apply layer mask").assertIsEnabled()
        tool("Layers")
        rule.onNodeWithText("Remove layer").performScrollTo().performClick()
        tool("Select & mask")
        rule.onNodeWithText("Second adjustment · Adjustment").assertIsDisplayed()
        rule.onNodeWithText("Apply layer mask").assertIsNotEnabled()
        rule.runOnIdle { assertEquals(second, current.layers.single()) }
    }

    @Test fun reorderUndoAndRedoKeepTheSameSelectedLayer() {
        val first = ProLayer("", name = "First adjustment", kind = ProLayerKind.ADJUSTMENT)
        val second = ProLayer("", name = "Second adjustment", kind = ProLayerKind.ADJUSTMENT)
        open(ProDocument(listOf(first, second)))
        rule.onNodeWithText("Stack (2)").performClick()
        rule.onNodeWithText(second.name).performScrollTo().performClick()
        rule.onNodeWithText("↓ Behind").performScrollTo().performClick()
        rule.runOnIdle { assertEquals(listOf(second, first), current.layers) }

        rule.onNodeWithContentDescription("Undo").performClick()
        rule.runOnIdle { assertEquals(listOf(first, second), current.layers) }
        rule.onNodeWithText("Second adjustment · Adjustment").assertIsDisplayed()

        rule.onNodeWithContentDescription("Redo").performClick()
        rule.runOnIdle { assertEquals(listOf(second, first), current.layers) }
        rule.onNodeWithText("Second adjustment · Adjustment").assertIsDisplayed()

        rule.onNodeWithContentDescription("Undo").performClick()
        rule.onNodeWithText("Hide").performScrollTo().performClick()
        rule.runOnIdle { assertEquals(listOf(first, second.copy(visible = false)), current.layers) }
    }

    @Test fun missingSourceCannotRedirectAnotherLayersUnappliedMask() {
        val uri = photo(48, 48)
        val owner = ProLayer("", name = "Mask owner", kind = ProLayerKind.ADJUSTMENT)
        val source = ProLayer(uri.toString(), name = "Imported photo")
        var removedSource = false
        try {
            open(ProDocument(listOf(owner, source)))
            rule.waitUntil(15_000) {
                rule.onAllNodesWithContentDescription("Pro editable composition").fetchSemanticsNodes().isNotEmpty()
            }
            tool("Select & mask")
            rule.onNodeWithTag("pro-canvas").performTouchInput {
                val distance = minOf(width, height) * .2f
                swipe(center - Offset(distance, distance), center + Offset(distance, distance))
            }
            rule.onNodeWithText("Apply layer mask").assertIsEnabled()
            removedSource = resolver.delete(uri, null, null) == 1
            assertTrue(removedSource)

            // Changing the adjustment schedules a new preview while the unapplied mask still exists.
            tool("Layer effects")
            rule.onNodeWithText(ProPixelEffect.INVERT.label).performScrollTo().performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Source for image layer 2 is unavailable.")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            tool("Select & mask")
            rule.onNodeWithText("Mask owner · Adjustment").assertIsDisplayed()
            rule.onNodeWithText("Apply layer mask").assertIsNotEnabled()
            rule.runOnIdle { assertTrue(current.layers.all { it.mask == null }) }
        } finally {
            if (!removedSource) resolver.delete(uri, null, null)
        }
    }

    @Test fun clearingEffectLeavesGradeAndMaskOnThatAdjustmentLayer() {
        val grade = Grade(exposure = -.2f, contrast = .25f)
        val mask = ProSelection(ProSelectionKind.RECTANGLE, width = .4f, height = .4f)
        val layer = ProLayer("", name = "Graded effect", kind = ProLayerKind.ADJUSTMENT,
            edits = EditDocument(grade = grade), effect = ProPixelEffect.INVERT, mask = mask)
        open(ProDocument(listOf(layer)))
        tool("Layer effects")
        rule.onNodeWithText("Unchanged").performScrollTo().performClick()
        rule.runOnIdle { assertEquals(layer.copy(effect = ProPixelEffect.NONE), current.layers.single()) }
        rule.onNodeWithContentDescription("Undo").performClick()
        rule.runOnIdle { assertEquals(layer, current.layers.single()) }
        rule.onNodeWithContentDescription("Redo").performClick()
        rule.runOnIdle { assertEquals(layer.copy(effect = ProPixelEffect.NONE), current.layers.single()) }
    }

    @Test fun replacingTheSourceUsesItsPreservedRotationAndCropForLayerGeometry() {
        val original = photo(48, 48)
        val replacement = photo(84, 28)
        try {
            val edits = EditDocument(quarterTurns = 1, crop = CropRatio.FREE,
                cropFrame = CropFrame(.25f, 0f, .75f, 1f))
            val layer = ProLayer(original.toString(), name = "Rotated crop", edits = edits, aspect = .5f)
            open(ProDocument(listOf(layer)), replacement)
            rule.onNodeWithText("Replace image").performScrollTo().performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Source replaced. Layer edits preserved.").fetchSemanticsNodes().isNotEmpty()
            }
            rule.runOnIdle {
                val replaced = current.layers.single()
                assertEquals(replacement.toString(), replaced.uri)
                assertEquals(edits, replaced.edits)
                // 84×28 rotates to 28×84, then loses the outer quarters of its width.
                assertEquals(14f / 84f, replaced.aspect, .0001f)
            }
            rule.onNodeWithContentDescription("Undo").performClick()
            rule.runOnIdle { assertEquals(layer, current.layers.single()) }
        } finally {
            resolver.delete(replacement, null, null)
            resolver.delete(original, null, null)
        }
    }

    private fun photo(width: Int, height: Int): Uri {
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "gz-pro-replace-${System.nanoTime()}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        try {
            bitmap.eraseColor(Color.RED)
            requireNotNull(resolver.openOutputStream(uri)).use {
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                null, null)
            return uri
        } catch (problem: Throwable) {
            resolver.delete(uri, null, null)
            throw problem
        } finally {
            bitmap.recycle()
        }
    }
}

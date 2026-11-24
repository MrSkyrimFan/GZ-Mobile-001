package com.greenezone.gzphotoeditor

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.CreativeDocument
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.ProDocument
import com.greenezone.gzphotoeditor.editor.ProLayer
import com.greenezone.gzphotoeditor.editor.ProLayerKind
import com.greenezone.gzphotoeditor.editor.ProRaster
import com.greenezone.gzphotoeditor.editor.ProStore
import com.greenezone.gzphotoeditor.editor.TextOverlay
import com.greenezone.gzphotoeditor.ui.ProWorkspaceScreenContent
import com.greenezone.gzphotoeditor.ui.ProWorkspaceSession
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Base64

@RunWith(AndroidJUnit4::class)
class ProWorkingDocumentTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var current: ProDocument
    private var backCount = 0

    private fun open(initial: ProDocument, reset: ProDocument? = null) {
        rule.setContent {
            var document by remember { mutableStateOf(initial) }
            var session by remember { mutableStateOf(ProWorkspaceSession(resetDocument = reset)) }
            current = document
            MaterialTheme(colorScheme = darkColorScheme()) {
                ProWorkspaceScreenContent(document, { document = it }, { backCount++ }, {}, session,
                    { session = it })
            }
        }
    }

    private fun openReset() {
        rule.onNodeWithText("File ▾").performClick()
        rule.onNodeWithText("Delete current work").performClick()
        rule.onNodeWithText("Delete current Pro work?").assertIsDisplayed()
    }

    @Test fun deleteCurrentWorkCancelsOrRestoresOriginalWithoutDeletingSave() {
        val resolver = context.contentResolver
        val name = "Reset fixture " + System.nanoTime()
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "gz-pro-reset-${System.nanoTime()}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        try {
            val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
            try {
                bitmap.eraseColor(Color.GREEN)
                requireNotNull(resolver.openOutputStream(uri)).use {
                    assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
                }
            } finally {
                bitmap.recycle()
            }
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                null, null)
            val originalBytes = requireNotNull(resolver.openInputStream(uri)).use { it.readBytes() }
            val original = ProDocument(layers = listOf(ProLayer(uri.toString(), name = "Original")))
            val saved = original.copy(layers = listOf(original.layers.single().copy(opacity = .5f)))
            assertTrue(ProStore.save(context, name, saved))
            open(original, reset = original)
            rule.onNodeWithText("Hide").performScrollTo().performClick()
            rule.runOnIdle { assertFalse(current.layers.single().visible) }
            rule.onNodeWithText("Undo").assertIsEnabled()
            openReset()
            rule.onNodeWithText("Cancel").performClick()
            rule.runOnIdle { assertFalse(current.layers.single().visible) }
            rule.onNodeWithText("Undo").assertIsEnabled()
            openReset()
            rule.onNodeWithText("Delete work").performClick()
            rule.runOnIdle { assertEquals(original, current) }
            rule.onNodeWithText("Undo").assertIsNotEnabled()
            rule.onNodeWithText("Redo").assertIsNotEnabled()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithContentDescription("Pro editable composition")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            val image = rule.onNodeWithTag("pro-canvas").captureToImage().asAndroidBitmap()
            val center = image.getPixel(image.width / 2, image.height / 2)
            assertTrue(Color.green(center) > 220 && Color.red(center) < 30 && Color.blue(center) < 30)
            assertEquals(saved, ProStore.load(context, name))
            assertArrayEquals(originalBytes, requireNotNull(resolver.openInputStream(uri)).use { it.readBytes() })
        } finally {
            ProStore.delete(context, name)
            resolver.delete(uri, null, null)
        }
    }

    private fun adjustment(name: String) =
        ProDocument(layers = listOf(ProLayer("", name = name, kind = ProLayerKind.ADJUSTMENT)))

    private fun openSavedList() {
        rule.onNodeWithText("File ▾").performClick()
        rule.onNodeWithText("Open project").performClick()
        rule.onNodeWithText("Saved Pro projects").assertIsDisplayed()
    }

    private fun openSaved(name: String) {
        openSavedList()
        rule.onNodeWithText(name).performScrollTo().performClick()
        rule.waitUntil(10_000) {
            rule.onAllNodesWithText("Saved Pro projects").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test fun undoOpenRestoresThePriorPhotosResetBaseline() {
        val photo = adjustment("Prior original")
        val editedPhoto = photo.copy(layers = listOf(photo.layers.single().copy(opacity = .7f)))
        val saved = adjustment("Different saved composition")
        val name = "Undo open " + System.nanoTime().toString().takeLast(12)
        try {
            assertTrue(ProStore.save(context, name, saved))
            open(editedPhoto, reset = photo)
            openSaved(name)
            rule.runOnIdle { assertEquals(saved, current) }
            rule.onNodeWithText("Undo").performClick()
            rule.runOnIdle { assertEquals(editedPhoto, current) }
            openReset()
            rule.onNodeWithText("Delete work").performClick()
            rule.runOnIdle { assertEquals(photo, current) }
            assertEquals(saved, ProStore.load(context, name))
        } finally {
            ProStore.delete(context, name)
        }
    }

    @Test fun saveOpenUndoRedoPreservesTheNamedResetBaseline() {
        val baseline = adjustment("Original baseline")
        val edited = baseline.copy(layers = listOf(baseline.layers.single().copy(opacity = .6f)))
        val hidden = edited.copy(layers = listOf(edited.layers.single().copy(visible = false)))
        val name = "Reopen baseline " + System.nanoTime().toString().takeLast(12)
        try {
            open(edited, reset = baseline)
            rule.onNodeWithText("File ▾").performClick()
            rule.onNodeWithText("Save project").performClick()
            rule.onNode(hasSetTextAction()).performTextInput(name)
            rule.onNodeWithText("Save").performClick()
            rule.waitUntil(10_000) {
                rule.onAllNodesWithText("Save Pro project").fetchSemanticsNodes().isEmpty() &&
                    ProStore.load(context, name) == edited
            }

            rule.onNodeWithText("Hide").performScrollTo().performClick()
            rule.runOnIdle { assertEquals(hidden, current) }
            openSaved(name)
            rule.runOnIdle { assertEquals(edited, current) }
            rule.onNodeWithText("Undo").performClick()
            rule.runOnIdle { assertEquals(hidden, current) }
            rule.onNodeWithText("Redo").performClick()
            rule.runOnIdle { assertEquals(edited, current) }
            openReset()
            rule.onNodeWithText("Delete work").performClick()
            rule.runOnIdle { assertEquals(baseline, current) }
            assertEquals(edited, ProStore.load(context, name))
        } finally {
            ProStore.delete(context, name)
        }
    }

    @Test fun legacySavedProjectResetsToItsSavedVersion() {
        val saved = adjustment("Legacy adjustment")
        val name = "Legacy reset " + System.nanoTime().toString().takeLast(12)
        try {
            assertTrue(ProStore.save(context, name, saved))
            open(ProDocument())
            openSaved(name)
            rule.onNodeWithText("Hide").performScrollTo().performClick()
            rule.runOnIdle { assertFalse(current.layers.single().visible) }
            openReset()
            rule.onNodeWithText("Delete work").performClick()
            rule.runOnIdle { assertEquals(saved, current) }
        } finally {
            ProStore.delete(context, name)
        }
    }

    @Test fun deletingNamedProjectNeedsConfirmationAndKeepsTheOpenWork() {
        val suffix = System.nanoTime().toString().takeLast(12)
        val targetName = "Delete target " + suffix
        val keepName = "Keep project " + suffix
        val target = adjustment("Target saved work")
        val keep = adjustment("Other saved work")
        val openWork = adjustment("Unsaved open work")
        try {
            assertTrue(ProStore.save(context, targetName, target))
            assertTrue(ProStore.save(context, keepName, keep))
            open(openWork)
            openSavedList()
            val delete = rule.onNodeWithContentDescription("Delete saved project $targetName")
            delete.performScrollTo().performClick()
            rule.onNodeWithText("Delete saved Pro project?").assertIsDisplayed()
            assertEquals(target, ProStore.load(context, targetName))
            rule.onNodeWithText("Cancel").performClick()
            assertEquals(target, ProStore.load(context, targetName))

            delete.performScrollTo().performClick()
            rule.onNodeWithText("Delete saved project").performClick()
            rule.onNodeWithText(targetName).assertDoesNotExist()
            assertNull(ProStore.load(context, targetName))
            assertEquals(keep, ProStore.load(context, keepName))
            rule.runOnIdle { assertEquals(openWork, current) }
        } finally {
            ProStore.delete(context, targetName)
            ProStore.delete(context, keepName)
        }
    }

    @Test fun androidBackShowsControlsThenClearsToolDraftBeforeLeaving() {
        open(ProDocument(layers = listOf(ProLayer(uri = "", name = "Grade",
            kind = ProLayerKind.ADJUSTMENT))))
        val canvas = rule.onNodeWithTag("pro-canvas")
        rule.onNodeWithText("Tools ▾").performClick()
        rule.onNodeWithText("Select & mask").performClick()
        canvas.performTouchInput { click(center) }
        rule.onNodeWithText("Tools ▾").assertDoesNotExist()
        canvas.performTouchInput {
            val distance = minOf(width, height) * .2f
            swipe(center - Offset(distance, distance), center + Offset(distance, distance))
        }
        rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.onNodeWithText("Tools ▾").assertIsDisplayed()
        rule.onNodeWithText("Apply layer mask").assertIsEnabled()
        rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.onNodeWithText("Apply layer mask").assertIsNotEnabled()
        rule.runOnIdle {
            assertEquals(0, backCount)
            assertNull(current.layers.single().mask)
        }
        rule.onNodeWithText("‹ Back").performClick()
        rule.runOnIdle { assertEquals(1, backCount) }
    }

    @Test fun oldProTextCanReopenWithoutTextAuthoringActions() {
        val suffix = System.nanoTime().toString().takeLast(12)
        val name = "Legacy text " + suffix
        val resavedName = "Resaved text " + suffix
        val contents = "Keep these words"
        val legacy = ProDocument(layers = listOf(ProLayer(uri = "", name = "Old caption",
            kind = ProLayerKind.TEXT, edits = EditDocument(
                creative = CreativeDocument(text = listOf(TextOverlay(content = contents, color = Color.BLACK)))))))
        val base64 = Base64.getUrlEncoder().withoutPadding()
        val olderEdits = listOf(
            "version=1",
            "creative.text.count=1",
            "creative.text.0.content=" + base64.encodeToString(contents.toByteArray(Charsets.UTF_8)),
            "creative.text.0.color=" + Color.BLACK
        ).joinToString("\n")
        val olderFile = JSONObject().put("version", 3).put("layers", JSONArray()
            .put(JSONObject().put("name", "Old caption").put("kind", "TEXT").put("edits", olderEdits)))
        val directory = File(context.filesDir, "gz_pro_projects").apply { mkdirs() }
        val filename = base64.encodeToString(name.toByteArray(Charsets.UTF_8)) + ".gzpro"
        try {
            File(directory, filename).writeText(olderFile.toString())
            open(ProDocument())
            rule.onNodeWithText("File ▾").performClick()
            rule.onNodeWithText("Open project").performClick()
            rule.onNodeWithText(name).performScrollTo().performClick()
            rule.onNodeWithText("Old caption · Text").assertIsDisplayed()
            rule.onNodeWithText("Edit text").assertDoesNotExist()
            rule.onNodeWithText("Duplicate").assertDoesNotExist()
            rule.runOnIdle { assertEquals(legacy, current) }
            rule.onNodeWithText("+ Add layer").performClick()
            rule.onNodeWithText("Words / Text").assertDoesNotExist()
            rule.onNodeWithText("Shape").assertIsDisplayed()
            rule.onNodeWithText("Close").performClick()
            rule.onNodeWithText("File ▾").performClick()
            rule.onNodeWithText("Save project").performClick()
            rule.onNode(hasSetTextAction()).performTextInput(resavedName)
            rule.onNodeWithText("Save").performClick()
            val reopened = requireNotNull(ProStore.load(context, resavedName))
            assertEquals(legacy, reopened)
            val image = ProRaster.render(context.contentResolver, reopened, 256, 256, false)
            try {
                val pixels = IntArray(image.width * image.height)
                image.getPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
                assertTrue(pixels.any { it == Color.BLACK })
            } finally { image.recycle() }
        } finally {
            ProStore.delete(context, resavedName)
            ProStore.delete(context, name)
        }
    }
}

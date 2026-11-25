package com.greenezone.gzphotoeditor

import android.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.CreativeDocument
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.ProDocument
import com.greenezone.gzphotoeditor.editor.ProLayer
import com.greenezone.gzphotoeditor.editor.ProLayerKind
import com.greenezone.gzphotoeditor.editor.ProRaster
import com.greenezone.gzphotoeditor.editor.ProStore
import com.greenezone.gzphotoeditor.editor.ShapeOverlay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProNamedSaveTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun switchTo(mode: String) {
        rule.onNodeWithContentDescription("Choose editing workspace").performClick()
        rule.onNodeWithText(mode).performClick()
    }

    private fun shape(name: String, color: Int, x: Float = .5f) = ProLayer(
        uri = "", name = name, kind = ProLayerKind.SHAPE, x = x,
        edits = EditDocument(creative = CreativeDocument(shapes = listOf(
            ShapeOverlay(width = .2f, height = .2f, color = color, filled = true)
        )))
    )

    private fun click(text: String) {
        rule.onNodeWithText(text).performClick()
    }

    private fun waitForDialogToClose(title: String) {
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText(title).fetchSemanticsNodes().isEmpty()
        }
    }

    private fun openProject(name: String) {
        val saved = requireNotNull(ProStore.load(context, name))
        rule.onNodeWithText("Options").performClick()
        click("Open project…")
        rule.waitUntil(15_000) { rule.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText(name).performScrollTo().performClick()
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText("Saved Pro projects").fetchSemanticsNodes().isEmpty() &&
                rule.onAllNodesWithText(saved.layers.first().name + " ·", substring = true)
                    .fetchSemanticsNodes().isNotEmpty() &&
                rule.onAllNodes(hasContentDescription("Choose editing workspace") and isEnabled())
                    .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun openSave() {
        rule.onNodeWithText("Options").performClick()
        click("Save project")
    }

    private fun openSaveAs() {
        rule.onNodeWithText("Options").performClick()
        click("Save project as…")
    }

    private fun saveAs(name: String) {
        rule.onNode(hasSetTextAction()).performTextReplacement(name)
        click("Save")
    }

    private fun assertSaveTarget(name: String) {
        rule.onNodeWithText("Options").performClick()
        rule.onNodeWithText("Save project").assertIsNotEnabled()
        click("Save project as…")
        rule.onNode(hasSetTextAction()).assert(hasText("$name copy"))
        click("Cancel")
    }

    private fun assertProject(name: String, expected: ProDocument, centerColor: Int) {
        val reopened = requireNotNull(ProStore.load(context, name))
        assertEquals(expected.layers.map { it.name }, reopened.layers.map { it.name })
        assertEquals(expected, reopened)
        val pixels = ProRaster.render(context.contentResolver, reopened, 96, 96, false)
        try {
            assertEquals(centerColor, pixels.getPixel(pixels.width / 2, pixels.height / 2))
        } finally {
            pixels.recycle()
        }
    }

    @Test fun openingAnotherProjectBindsDefaultSaveAndBothProjectsSurviveActivityRestart() {
        val suffix = System.nanoTime().toString().takeLast(8)
        val nameA = "Red $suffix"
        val nameB = "Blue $suffix"
        val a = ProDocument(listOf(shape("A red layer", Color.RED)))
        val b = ProDocument(listOf(shape("B blue layer", Color.BLUE), shape("B yellow layer", Color.YELLOW, .15f)))
        val editedB = b.copy(layers = listOf(b.layers.first().copy(visible = false), b.layers.last()))
        try {
            assertTrue(ProStore.save(context, nameA, a))
            assertTrue(ProStore.save(context, nameB, b))
            switchTo("Photo editing Pro")
            openProject(nameA)
            assertSaveTarget(nameA)

            openProject(nameB)
            rule.onNodeWithText("Hide").performScrollTo().performClick()
            openSave()
            rule.waitUntil(15_000) { ProStore.load(context, nameB) == editedB }
            rule.onAllNodesWithText("Save Pro project").assertCountEquals(0)
            assertProject(nameA, a, Color.RED)
            assertProject(nameB, editedB, Color.WHITE)

            rule.activityRule.scenario.recreate()
            switchTo("Photo editing Pro")
            openProject(nameA)
            rule.onNodeWithText("A red layer · Shape").assertExists()
            assertProject(nameA, a, Color.RED)
            openProject(nameB)
            rule.onNodeWithText("B blue layer · Shape").assertExists()
            click("Stack (2)")
            click("B yellow layer")
            rule.onNodeWithText("B yellow layer · Shape").assertExists()
            assertProject(nameB, editedB, Color.WHITE)
        } finally {
            ProStore.delete(context, nameA)
            ProStore.delete(context, nameB)
        }
    }

    @Test fun undoOpenCanonicalCollisionAndNamedDeletionKeepTheSaveTargetExplicit() {
        val suffix = System.nanoTime().toString().takeLast(8)
        val nameA = "Red $suffix"
        val nameB = "Blue $suffix"
        val nameC = "Copy $suffix"
        val a = ProDocument(listOf(shape("A red layer", Color.RED)))
        val b = ProDocument(listOf(shape("B blue layer", Color.BLUE)))
        try {
            assertTrue(ProStore.save(context, nameA, a))
            assertTrue(ProStore.save(context, nameB, b))
            switchTo("Photo editing Pro")
            openProject(nameA)
            openProject(nameB)
            rule.onNodeWithContentDescription("Undo").performClick()
            assertSaveTarget(nameA)
            rule.onNodeWithContentDescription("Redo").performClick()
            assertSaveTarget(nameB)

            openSaveAs()
            saveAs("  Red   $suffix  ")
            rule.waitUntil(10_000) { rule.onAllNodesWithText("Replace saved Pro project?").fetchSemanticsNodes().isNotEmpty() }
            assertProject(nameA, a, Color.RED)
            click("Cancel")
            waitForDialogToClose("Save Pro project")
            assertSaveTarget(nameB)

            openSaveAs()
            saveAs(nameC)
            waitForDialogToClose("Save Pro project")
            assertProject(nameC, b, Color.BLUE)

            switchTo("Photo editing")
            switchTo("Photo editing Pro")
            assertSaveTarget(nameC)

            openSaveAs()
            saveAs("Red $suffix")
            rule.waitUntil(10_000) { rule.onAllNodesWithText("Replace saved Pro project?").fetchSemanticsNodes().isNotEmpty() }
            click("Replace project")
            waitForDialogToClose("Save Pro project")
            assertProject(nameA, b, Color.BLUE)

            rule.onNodeWithText("Options").performClick()
            click("Open project…")
            rule.waitUntil(15_000) {
                rule.onAllNodesWithContentDescription("Delete saved project $nameA").fetchSemanticsNodes().isNotEmpty()
            }
            rule.onNodeWithContentDescription("Delete saved project $nameA").performScrollTo().performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Delete saved Pro project?").fetchSemanticsNodes().isNotEmpty()
            }
            click("Delete saved project")
            rule.waitUntil(15_000) {
                ProStore.load(context, nameA) == null &&
                    rule.onAllNodesWithText("Delete saved Pro project?").fetchSemanticsNodes().isEmpty()
            }
            click("Close")
            assertNull(ProStore.load(context, nameA))
            openSave()
            rule.onNodeWithText("Save").assertIsNotEnabled()
        } finally {
            ProStore.delete(context, nameA)
            ProStore.delete(context, nameB)
            ProStore.delete(context, nameC)
        }
    }
}


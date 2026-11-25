package com.greenezone.gzphotoeditor

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.ProStore
import com.greenezone.gzphotoeditor.editor.ProBackground
import com.greenezone.gzphotoeditor.ui.isDirty
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.ProDocument
import com.greenezone.gzphotoeditor.editor.ProLayer
import com.greenezone.gzphotoeditor.editor.ProLayerKind
import com.greenezone.gzphotoeditor.ui.ProWorkspaceScreenContent
import com.greenezone.gzphotoeditor.ui.ProWorkspaceSession
import com.greenezone.gzphotoeditor.ui.WorkspaceMode
import com.greenezone.gzphotoeditor.ui.saved
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Owner F02: actual Pro callbacks must wait for a resolved dirty departure. */
@RunWith(AndroidJUnit4::class)
class AndroidProDepartureContractTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val initial = ProDocument(layers = listOf(
        ProLayer(uri = "", name = "Independent adjustment", kind = ProLayerKind.ADJUSTMENT)
    ))
    private lateinit var current: ProDocument
    private lateinit var currentSession: ProWorkspaceSession
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private var backCalls = 0
    private val switches = mutableListOf<WorkspaceMode>()

    private fun mount(
        start: ProDocument = initial,
        startSession: ProWorkspaceSession = ProWorkspaceSession(resetDocument = start)
    ) {
        rule.setContent {
            var document by remember { mutableStateOf(start) }
            var session by remember { mutableStateOf(startSession) }
            current = document
            currentSession = session
            MaterialTheme {
                ProWorkspaceScreenContent(
                    document = document,
                    onDocument = { document = it },
                    onBack = { backCalls++ },
                    onSwitch = { switches += it },
                    session = session,
                    onSession = { session = it }
                )
            }
        }
    }

    private fun openAndEdit() {
        mount()
        rule.onNodeWithText("Hide").performScrollTo().performClick()
        rule.runOnIdle { assertFalse(current.layers.single().visible) }
    }

    @Test fun dirtyBackWaitsForCancelOrDiscardAndNeverFallsThrough() {
        openAndEdit()
        rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.runOnIdle { assertEquals("Dirty Back navigated without resolving the work", 0, backCalls) }
        rule.onNodeWithText("Save").assertIsDisplayed()
        rule.onNodeWithText("Discard").assertIsDisplayed()
        rule.onNodeWithText("Cancel").performClick()
        rule.runOnIdle {
            assertEquals(0, backCalls)
            assertFalse(current.layers.single().visible)
        }
        rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.onNodeWithText("Discard").performClick()
        rule.waitUntil(15_000) { backCalls == 1 }
        rule.runOnIdle {
            assertEquals(1, backCalls)
            assertEquals(initial, current)
        }
    }

    @Test fun dirtyWorkspaceSwitchWaitsForCancelOrDiscard() {
        openAndEdit()
        rule.onNodeWithContentDescription("Choose editing workspace").performClick()
        rule.onNodeWithText("Collage maker").performClick()
        rule.runOnIdle {
            assertEquals("Dirty mode switch bypassed the departure guard", emptyList<WorkspaceMode>(), switches)
        }
        rule.onNodeWithText("Cancel").performClick()
        rule.runOnIdle { assertFalse(current.layers.single().visible) }
        rule.onNodeWithContentDescription("Choose editing workspace").performClick()
        rule.onNodeWithText("Collage maker").performClick()
        rule.onNodeWithText("Discard").performClick()
        rule.waitUntil(15_000) { switches.size == 1 }
        rule.runOnIdle {
            assertEquals(listOf(WorkspaceMode.COLLAGE), switches)
            assertEquals(initial, current)
        }
    }

    @Test fun canceledSaveNamingCannotReviveOldBackWhenAnotherSaveCompletes() {
        val name = "Pro continuation " + System.nanoTime().toString().takeLast(10)
        try {
            openAndEdit()
            rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
            rule.onNodeWithText("Save").performClick()
            rule.onNodeWithText("Save Pro project").assertIsDisplayed()
            rule.onNodeWithText("Cancel").performClick()
            rule.runOnIdle {
                assertEquals(0, backCalls)
                assertFalse(current.layers.single().visible)
            }
            rule.onNodeWithText("Options").performClick()
            rule.onNodeWithText("Save project").performClick()
            rule.onNode(hasSetTextAction()).performTextInput(name)
            rule.onNodeWithText("Save").performClick()
            rule.waitUntil(15_000) { ProStore.load(context, name) != null }
            rule.waitForIdle()
            rule.runOnIdle {
                assertEquals(0, backCalls)
                assertEquals(current, ProStore.load(context, name))
                assertFalse(currentSession.isDirty(current))
            }
        } finally { ProStore.delete(context, name) }
    }

    @Test fun unchangedAndUndoToBaselineLeaveWithoutFalseDirtyPrompt() {
        mount()
        rule.onNodeWithText("Hide").performScrollTo().performClick()
        rule.onNodeWithContentDescription("Undo").performClick()
        rule.runOnIdle {
            assertEquals(initial, current)
            assertFalse(currentSession.isDirty(current))
            rule.activity.onBackPressedDispatcher.onBackPressed()
        }
        rule.runOnIdle { assertEquals(1, backCalls) }
        assertEquals(0, rule.onAllNodesWithText("Unsaved Pro changes").fetchSemanticsNodes().size)
    }

    @Test fun zeroLayerSetupCancelKeepsSetupAndDiscardRestoresEntry() {
        mount(ProDocument())
        rule.onNodeWithText("Transparent").performClick()
        rule.runOnIdle {
            assertEquals(ProBackground.TRANSPARENT, current.background)
            rule.activity.onBackPressedDispatcher.onBackPressed()
        }
        rule.onNodeWithText("Unsaved canvas setup").assertIsDisplayed()
        rule.onNodeWithText("Save").assertIsNotEnabled()
        rule.onNodeWithText("Cancel").performClick()
        rule.runOnIdle {
            assertEquals(0, backCalls)
            assertEquals(ProBackground.TRANSPARENT, current.background)
            rule.activity.onBackPressedDispatcher.onBackPressed()
        }
        rule.onNodeWithText("Discard setup").performClick()
        rule.waitUntil(15_000) { backCalls == 1 }
        rule.runOnIdle {
            assertEquals(1, backCalls)
            assertEquals(ProDocument(), current)
            assertEquals(0, current.layers.size)
        }
    }

    @Test fun namedDiscardUsesSavedContentWhileDeleteCurrentWorkKeepsOriginalReset() {
        val name = "Pro baseline " + System.nanoTime().toString().takeLast(10)
        try {
            openAndEdit()
            val saved = initial.copy(layers = listOf(initial.layers.single().copy(visible = false)))
            rule.onNodeWithText("Options").performClick()
            rule.onNodeWithText("Save project").performClick()
            rule.onNode(hasSetTextAction()).performTextInput(name)
            rule.onNodeWithText("Save").performClick()
            rule.waitUntil(15_000) { ProStore.load(context, name) == saved }
            rule.waitForIdle()
            rule.onNodeWithText("Show").performScrollTo().performClick()
            rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
            rule.onNodeWithText("Discard").performClick()
            rule.waitUntil(15_000) { backCalls == 1 }
            rule.runOnIdle {
                assertEquals(1, backCalls)
                assertEquals(saved, current)
                assertEquals(saved, ProStore.load(context, name))
            }
            // This harness records departure without unmounting: the explicit reset
            // still has a separate original baseline and cannot delete the named save.
            rule.onNodeWithText("Options").performClick()
            rule.onNodeWithText("Delete current work…").performClick()
            rule.onNodeWithText("Delete work").performClick()
            rule.waitUntil(15_000) { current == initial }
            rule.runOnIdle {
                assertEquals(initial, current)
                assertEquals(saved, ProStore.load(context, name))
            }
        } finally { ProStore.delete(context, name) }
    }

    @Test fun layerCommandsPreserveOwnerAndHistoryWhileNamedSaveIsInFlight() {
        val name = "Pro blocked edit " + System.nanoTime().toString().takeLast(10)
        val bottom = ProLayer("", name = "Bottom", kind = ProLayerKind.ADJUSTMENT)
        val middle = ProLayer("", name = "Middle", kind = ProLayerKind.ADJUSTMENT)
        val top = ProLayer("", name = "Top", kind = ProLayerKind.ADJUSTMENT)
        val clean = ProDocument(layers = listOf(bottom, middle, top))
        val edited = middle.copy(visible = false)
        val dirty = clean.copy(layers = listOf(bottom, edited, top))
        val outcomes = listOf(
            Triple("↓ Behind", listOf(edited, bottom, top), 0),
            Triple("↑ Above", listOf(bottom, top, edited), 2),
            Triple("Duplicate", listOf(bottom, edited, edited.copy(name = "Middle copy"), top), 2),
            Triple("Delete layer", listOf(bottom, top), 1)
        )
        try {
            check(ProStore.save(context, name, clean))
            val ref = requireNotNull(ProStore.loadNamed(context, name)).project
            mount(clean, ProWorkspaceSession(resetDocument = clean, selectedLayer = 1).saved(clean, ref))
            rule.onNodeWithText("Hide").performScrollTo().performClick()
            val busyBaseline = rule.runOnIdle {
                assertEquals(dirty, current)
                Triple(current, currentSession.selectedLayer, currentSession.history)
            }
            val queuedClicks = outcomes.associate { (label, _, _) ->
                val node = rule.onNodeWithText(label).performScrollTo().fetchSemanticsNode()
                label to requireNotNull(node.config[SemanticsActions.OnClick].action)
            }

            // Hold the store's synchronized publication so the real named Save stays in flight.
            val (blockedStates, disabledWhileSaving) = synchronized(ProStore) {
                rule.onNodeWithText("Options").performClick()
                rule.onNodeWithText("Save project").performClick()
                rule.onNodeWithText("Opening images…").assertIsDisplayed()
                val states = queuedClicks.mapValues { (_, click) ->
                    rule.runOnIdle { click() }
                    rule.runOnIdle { Triple(current, currentSession.selectedLayer, currentSession.history) }
                }
                states to queuedClicks.keys.all { label ->
                    SemanticsProperties.Disabled in rule.onNodeWithText(label).fetchSemanticsNode().config
                }
            }
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Editable Pro project saved").fetchSemanticsNodes().isNotEmpty()
            }
            blockedStates.forEach { (label, state) ->
                assertEquals("$label must preserve document, selection and history during Save",
                    busyBaseline, state)
            }
            assertTrue("Layer actions must be unavailable while Save owns the document", disabledWhileSaving)
            assertEquals(dirty, ProStore.load(context, name))

            for ((label, expectedLayers, selectedLayer) in outcomes) {
                rule.onNodeWithText("Stack (3)").performClick()
                rule.onNodeWithText(middle.name).performClick()
                val historySize = rule.runOnIdle { currentSession.history.undo.size }
                rule.onNodeWithText(label).performScrollTo().performClick()
                rule.runOnIdle {
                    assertEquals(expectedLayers, current.layers)
                    assertEquals(selectedLayer, currentSession.selectedLayer)
                    assertEquals(historySize + 1, currentSession.history.undo.size)
                }
                rule.onNodeWithContentDescription("Undo").performClick()
                rule.runOnIdle {
                    assertEquals(dirty, current)
                    assertEquals(historySize, currentSession.history.undo.size)
                }
                rule.onNodeWithContentDescription("Redo").performClick()
                rule.runOnIdle {
                    assertEquals(expectedLayers, current.layers)
                    assertEquals(historySize + 1, currentSession.history.undo.size)
                }
                rule.onNodeWithContentDescription("Undo").performClick()
            }
        } finally {
            ProStore.delete(context, name)
        }
    }

}

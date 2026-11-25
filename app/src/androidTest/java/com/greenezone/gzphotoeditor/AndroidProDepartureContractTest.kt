package com.greenezone.gzphotoeditor

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    private fun mount(start: ProDocument = initial) {
        rule.setContent {
            var document by remember { mutableStateOf(start) }
            var session by remember { mutableStateOf(ProWorkspaceSession(resetDocument = start)) }
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
            rule.onNodeWithContentDescription("Options").performClick()
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
            rule.onNodeWithContentDescription("Options").performClick()
            rule.onNodeWithText("Save project").performClick()
            rule.onNode(hasSetTextAction()).performTextInput(name)
            rule.onNodeWithText("Save").performClick()
            rule.waitUntil(15_000) { ProStore.load(context, name) == saved }
            rule.waitForIdle()
            rule.onNodeWithText("Show").performScrollTo().performClick()
            rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
            rule.onNodeWithText("Discard").performClick()
            rule.runOnIdle {
                assertEquals(1, backCalls)
                assertEquals(saved, current)
                assertEquals(saved, ProStore.load(context, name))
            }
            // This harness records departure without unmounting: the explicit reset
            // still has a separate original baseline and cannot delete the named save.
            rule.onNodeWithContentDescription("Options").performClick()
            rule.onNodeWithText("Delete current work…").performClick()
            rule.onNodeWithText("Delete work").performClick()
            rule.runOnIdle {
                assertEquals(initial, current)
                assertEquals(saved, ProStore.load(context, name))
            }
        } finally { ProStore.delete(context, name) }
    }

}

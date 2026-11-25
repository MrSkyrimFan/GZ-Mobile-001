package com.greenezone.gzphotoeditor

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
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
    private var backCalls = 0
    private val switches = mutableListOf<WorkspaceMode>()

    private fun openAndEdit() {
        rule.setContent {
            var document by remember { mutableStateOf(initial) }
            var session by remember { mutableStateOf(ProWorkspaceSession(resetDocument = initial)) }
            current = document
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
}

package com.greenezone.gzphotoeditor

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.test.swipe
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.ProDocument
import com.greenezone.gzphotoeditor.editor.ProLayer
import com.greenezone.gzphotoeditor.editor.ProLayerKind
import com.greenezone.gzphotoeditor.ui.ProWorkspaceScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Regression for a Compose slot-table teardown crash after hiding Pro controls. */
@RunWith(AndroidJUnit4::class)
class ProCanvasLifecycleTest {
    @get:Rule val rule = createComposeRule()

    @Test fun repeatedlyMountHideDrawAndUnMountProDoesNotCrashOrLoseTheLayer() {
        var showing by mutableStateOf(true)
        var current by mutableStateOf(
            ProDocument(layers = listOf(ProLayer(uri = "", name = "Independent ink",
                kind = ProLayerKind.DRAWING)))
        )
        rule.setContent {
            MaterialTheme {
                if (showing) {
                    ProWorkspaceScreen(
                        document = current, onDocument = { current = it },
                        onBack = {}, onSwitch = {}
                    )
                }
            }
        }
        repeat(4) {
            val canvas = rule.onNodeWithContentDescription("Pro editable composition")
            rule.waitUntil(20_000) {
                runCatching { canvas.fetchSemanticsNode() }.isSuccess
            }
            rule.onNodeWithText("Tools ▾").performClick()
            rule.onNodeWithText("Brush").performScrollTo().performClick()
            canvas.performTouchInput { click(center) }
            canvas.performTouchInput {
                val length = minOf(width, height) * .1f
                swipe(center - Offset(length, 0f), center + Offset(length, 0f))
            }
            rule.waitForIdle()
            rule.runOnIdle { showing = false }
            rule.waitForIdle() // Explicit composition disposal, not just test teardown.
            rule.runOnIdle { showing = true }
            rule.waitForIdle()
        }
        rule.runOnIdle {
            assertTrue(current.layers.single().edits.creative.strokes.isNotEmpty())
            showing = false
        }
        rule.waitForIdle()
    }
}

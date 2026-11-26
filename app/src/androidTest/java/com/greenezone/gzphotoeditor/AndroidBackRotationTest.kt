package com.greenezone.gzphotoeditor

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.ui.ProViewport
import com.greenezone.gzphotoeditor.ui.ProViewportSemanticsKey
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidBackRotationTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun rotationRetainsProSelectedToolViewportAndUndo() {
        val initialOrientation = rule.activity.resources.configuration.orientation
        val requested = if (initialOrientation == Configuration.ORIENTATION_PORTRAIT) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        val expected = if (initialOrientation == Configuration.ORIENTATION_PORTRAIT) {
            Configuration.ORIENTATION_LANDSCAPE
        } else {
            Configuration.ORIENTATION_PORTRAIT
        }

        try {
            rule.onNodeWithContentDescription("Choose editing workspace").performClick()
            rule.onNodeWithText("Photo editing Pro").performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Photo Pro ▾").fetchSemanticsNodes().isNotEmpty()
            }
            rule.onNodeWithText("Options").performClick()
            rule.onNodeWithText("New / Open media…").performClick()
            rule.onNodeWithText("New canvas…").performClick()
            rule.onNodeWithText("+ Add layer").performClick()
            rule.onNodeWithText("Paint layer", substring = false).performClick()
            rule.onNodeWithTag("workspace-tools").performClick()
            rule.onNodeWithText("Layers", substring = false).performClick()
            rule.onNodeWithText("+ Add layer").performClick()
            rule.onNodeWithText("Shape layer", substring = false).performClick()
            rule.onNodeWithText("Shape layer 2 · Shape").assertIsDisplayed()
            rule.onNodeWithTag("workspace-tools").performClick()
            rule.onNodeWithText("Transform").performClick()
            rule.onNodeWithText("Transform · Shape layer 2", substring = false).assertIsDisplayed()
            rule.onNodeWithContentDescription("Undo").assertIsEnabled()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithContentDescription("Pro editable composition")
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }

            val canvas = rule.onNodeWithTag("pro-canvas")
            canvas.performTouchInput {
                val radius = minOf(width, height) * .13f
                down(0, center - Offset(radius, 0f))
                down(1, center + Offset(radius, 0f))
                updatePointerTo(0, center - Offset(2 * radius, 0f))
                updatePointerTo(1, center + Offset(2 * radius, 0f))
                move()
                up(0)
                up(1)
            }
            rule.waitUntil(15_000) {
                canvas.fetchSemanticsNode().config[ProViewportSemanticsKey] !=
                    ProViewport()
            }
            val viewport =
                canvas.fetchSemanticsNode().config[ProViewportSemanticsKey]

            rule.runOnIdle { rule.activity.requestedOrientation = requested }
            rule.waitUntil(15_000) {
                rule.activity.resources.configuration.orientation == expected
            }

            rule.onNodeWithText("Photo Pro ▾").assertIsDisplayed()
            rule.onNodeWithText("Shape layer 2 · Shape").assertIsDisplayed()
            rule.onNodeWithText("Transform · Shape layer 2", substring = false).assertIsDisplayed()
            rule.onNodeWithContentDescription("Undo").assertIsEnabled()
            assertEquals(
                viewport,
                rule.onNodeWithTag("pro-canvas").fetchSemanticsNode()
                    .config[ProViewportSemanticsKey]
            )
        } finally {
            val restore = if (initialOrientation == Configuration.ORIENTATION_PORTRAIT) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            rule.runOnIdle { rule.activity.requestedOrientation = restore }
            rule.waitUntil(15_000) {
                rule.activity.resources.configuration.orientation == initialOrientation
            }
        }
    }
}

package com.greenezone.gzphotoeditor

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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
            rule.onNodeWithText("Or start with a blank canvas").performClick()
            rule.onNodeWithText("+ Add layer").performClick()
            rule.onNodeWithText("Shape").performClick()
            rule.onNodeWithText("Shape 2 · Shape").assertIsDisplayed()
            rule.onNodeWithText("Tools ▾").performClick()
            rule.onNodeWithText("Transform").performClick()
            rule.onNodeWithText("Transform ▴").assertIsDisplayed()
            rule.onNodeWithText("Undo").assertIsEnabled()

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
            rule.waitUntil(5_000) {
                canvas.fetchSemanticsNode().config[SemanticsProperties.StateDescription] !=
                    "z=1.0000;x=0.5000;y=0.5000"
            }
            val viewport =
                canvas.fetchSemanticsNode().config[SemanticsProperties.StateDescription]

            rule.runOnIdle { rule.activity.requestedOrientation = requested }
            rule.waitUntil(15_000) {
                rule.activity.resources.configuration.orientation == expected
            }

            rule.onNodeWithText("Photo Pro ▾").assertIsDisplayed()
            rule.onNodeWithText("Shape 2 · Shape").assertIsDisplayed()
            rule.onNodeWithText("Transform ▴").assertIsDisplayed()
            rule.onNodeWithText("Undo").assertIsEnabled()
            assertEquals(
                viewport,
                rule.onNodeWithTag("pro-canvas").fetchSemanticsNode()
                    .config[SemanticsProperties.StateDescription]
            )
        } finally {
            val restore = if (initialOrientation == Configuration.ORIENTATION_PORTRAIT) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            rule.runOnIdle { rule.activity.requestedOrientation = restore }
        }
    }
}

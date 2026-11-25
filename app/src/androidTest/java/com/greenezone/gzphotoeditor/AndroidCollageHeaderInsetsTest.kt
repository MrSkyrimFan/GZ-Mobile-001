package com.greenezone.gzphotoeditor

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.CollageDocument
import com.greenezone.gzphotoeditor.editor.CollagePhoto
import com.greenezone.gzphotoeditor.editor.CollageStore
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidCollageHeaderInsetsTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun emptyAndPopulatedCollageKeepOptionsOutsideSystemBars() =
        withCollageTestImage(rule.activity) { source ->
            val activity = rule.activity
            val originalRequest = activity.requestedOrientation
            val originalOrientation = activity.resources.configuration.orientation
            val name = "insets-" + System.nanoTime()
            val fixture = CollageDocument(listOf(CollagePhoto(source.toString())))
            assertTrue("Could not seed the named Collage fixture", CollageStore.save(activity, name, fixture))
            try {
                rotate(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, Configuration.ORIENTATION_PORTRAIT)
                rule.onNodeWithContentDescription("Choose editing workspace").performClick()
                rule.onNodeWithText("Collage maker", substring = false).performClick()
                rule.onNodeWithText("Combine 1–20 photos and edit each one.").assertIsDisplayed()
                assertHeaderInsideInsets(requireTopInset = true)
                rule.onNodeWithTag("workspace-options").performTouchInput { click() }
                rule.onNodeWithText("Open project…").performClick()
                waitForSavedCollages(name)
                rule.onNodeWithText(name, substring = false).performScrollTo().performClick()
                rule.onNodeWithText("Open", substring = false).performClick()
                rule.waitUntil(15_000) {
                    rule.onAllNodesWithContentDescription("Photo collage preview")
                        .fetchSemanticsNodes().isNotEmpty()
                }
                assertHeaderInsideInsets(requireTopInset = true)

                rotate(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, Configuration.ORIENTATION_LANDSCAPE)
                rule.onNodeWithContentDescription("Photo collage preview").assertExists()
                assertHeaderInsideInsets(requireTopInset = false)
                rule.onNodeWithTag("workspace-options").performTouchInput { click() }
                rule.onNodeWithText("Open project…").assertIsDisplayed()
                rule.onNodeWithText("Open project…").performClick()
                waitForSavedCollages()
                rule.onNodeWithText("Cancel", substring = false).performClick()
            } finally {
                CollageStore.delete(activity, name)
                val restore = if (originalOrientation == Configuration.ORIENTATION_LANDSCAPE)
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                rotate(restore, originalOrientation)
                rule.runOnIdle { activity.requestedOrientation = originalRequest }
            }
        }

    private fun rotate(request: Int, expected: Int) {
        rule.runOnIdle { rule.activity.requestedOrientation = request }
        rule.waitUntil(15_000) {
            rule.runOnIdle {
                val activity = rule.activity
                val decor = activity.window.decorView
                val layoutMatches = if (expected == Configuration.ORIENTATION_LANDSCAPE) {
                    decor.width > decor.height
                } else {
                    decor.height >= decor.width
                }
                activity.resources.configuration.orientation == expected &&
                    decor.isLaidOut && !decor.isLayoutRequested && layoutMatches &&
                    ViewCompat.getRootWindowInsets(decor) != null
            }
        }
        rule.runOnIdle {
            val window = rule.activity.window
            WindowCompat.getInsetsController(window, window.decorView).show(WindowInsetsCompat.Type.statusBars())
        }
    }

    private fun waitForSavedCollages(name: String? = null) {
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText("Saved collages", substring = false).fetchSemanticsNodes().isNotEmpty() &&
                (name == null || rule.onAllNodesWithText(name, substring = false)
                    .fetchSemanticsNodes().isNotEmpty())
        }
    }

    private fun assertHeaderInsideInsets(requireTopInset: Boolean) {
        val window = rule.activity.window.decorView
        val insetTypes = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        fun windowInsets(): Insets? = rule.runOnIdle {
            ViewCompat.getRootWindowInsets(window)?.getInsets(insetTypes)
        }
        rule.waitUntil(5_000) { windowInsets()?.let { !requireTopInset || it.top > 0 } == true }
        val unsafe = requireNotNull(windowInsets())
        val windowWidth = rule.runOnIdle { window.width }
        val header = rule.onNodeWithTag("workspace-header").assertIsDisplayed()
            .fetchSemanticsNode().boundsInWindow
        assertTrue("Header overlaps the top system inset: " + header + " vs " + unsafe, header.top + 1f >= unsafe.top)
        assertTrue("Header begins in the left system inset: " + header + " vs " + unsafe, header.left + 1f >= unsafe.left)
        assertTrue("Header ends in the right system inset: " + header + " vs " + unsafe,
            header.right - 1f <= windowWidth - unsafe.right)
    }
}

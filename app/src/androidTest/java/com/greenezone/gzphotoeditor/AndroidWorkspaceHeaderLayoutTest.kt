package com.greenezone.gzphotoeditor

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.ui.EditorWorkspaceHeader
import com.greenezone.gzphotoeditor.ui.WorkspaceCommand
import com.greenezone.gzphotoeditor.ui.WorkspaceCommandId
import com.greenezone.gzphotoeditor.ui.WorkspaceMode
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/** Measures the production common header, including its real popup, at a narrow large-text size. */
@RunWith(AndroidJUnit4::class)
class AndroidWorkspaceHeaderLayoutTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private val modes = listOf(
        WorkspaceMode.PHOTO, WorkspaceMode.PRO, WorkspaceMode.COLLAGE, WorkspaceMode.GIF
    )

    private data class HeaderHost(
        val workspace: androidx.compose.runtime.MutableState<WorkspaceMode>,
        val density: Float,
        val commands: List<Pair<WorkspaceCommandId, String>>,
        val invoked: MutableList<WorkspaceCommandId>,
        val historyInvoked: MutableList<String>
    )

    private fun mount(): HeaderHost {
        val workspace = mutableStateOf(WorkspaceMode.PHOTO)
        val expanded = mutableStateOf(false)
        val density = rule.activity.resources.displayMetrics.density
        val invoked = mutableListOf<WorkspaceCommandId>()
        val historyInvoked = mutableListOf<String>()
        // These commands are fixture inputs to the real shared presentation.
        // Native workspace ownership/availability is covered by their integration tests.
        val commands = listOf(
            WorkspaceCommandId.NEW to "New project…",
            WorkspaceCommandId.OPEN_PROJECT to "Open project…",
            WorkspaceCommandId.SAVE to "Save project",
            WorkspaceCommandId.SAVE_AS to "Save project as…",
            WorkspaceCommandId.EXPORT to "Export image…",
            WorkspaceCommandId.RESET to "Reset all edits…",
            WorkspaceCommandId.HELP to "Help"
        )
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale = 2f)) {
                MaterialTheme(colorScheme = darkColorScheme()) {
                    Box(Modifier.fillMaxSize().background(Color.Black)) {
                        Box(Modifier.requiredWidth(320.dp)) {
                            EditorWorkspaceHeader(
                                workspace = workspace.value,
                                commands = commands.map { (id, label) ->
                                    WorkspaceCommand(id, label) { invoked += id }
                                },
                                canUndo = false,
                                canRedo = false,
                                onUndo = { historyInvoked += "Undo" },
                                onRedo = { historyInvoked += "Redo" },
                                onChooseWorkspace = { workspace.value = it },
                                menuExpanded = expanded.value,
                                onMenuExpandedChange = { expanded.value = it },
                                onBack = {}
                            )
                        }
                    }
                }
            }
        }
        return HeaderHost(workspace, density, commands, invoked, historyInvoked)
    }

    private fun bounds(node: SemanticsNodeInteraction): Rect = node.fetchSemanticsNode().boundsInRoot

    private fun assertContains(label: String, outer: Rect, inner: Rect) {
        val tolerance = 1f
        assertTrue("$label begins outside the header: $inner vs $outer", inner.left >= outer.left - tolerance)
        assertTrue("$label ends outside the header: $inner vs $outer", inner.right <= outer.right + tolerance)
        assertTrue("$label starts above the header: $inner vs $outer", inner.top >= outer.top - tolerance)
        assertTrue("$label ends below the header: $inner vs $outer", inner.bottom <= outer.bottom + tolerance)
    }

    private fun assertNoOverlap(first: Pair<String, Rect>, second: Pair<String, Rect>) {
        val a = first.second
        val b = second.second
        val horizontal = minOf(a.right, b.right) - maxOf(a.left, b.left)
        val vertical = minOf(a.bottom, b.bottom) - maxOf(a.top, b.top)
        assertFalse("${first.first} overlaps ${second.first}: $a vs $b",
            horizontal > 1f && vertical > 1f)
    }

    @Test fun narrowDoubleFontHeaderKeepsAllControlsVisibleSeparateAndAtLeast48dp() {
        val host = mount()
        for (mode in modes) {
            rule.runOnIdle { host.workspace.value = mode }
            rule.onNodeWithText("GZ Editor Pro", substring = false).assertIsDisplayed()
            val header = bounds(rule.onNodeWithTag("workspace-header"))
            assertEquals("The fixture must actually measure 320dp", 320f * host.density, header.width, 1f)
            val nodes = listOf(
                "Back" to rule.onNodeWithContentDescription("Back"),
                "Workspace" to rule.onNodeWithContentDescription("Choose editing workspace"),
                "Undo" to rule.onNodeWithContentDescription("Undo"),
                "Redo" to rule.onNodeWithContentDescription("Redo"),
                "Options" to rule.onNodeWithTag("workspace-options")
            )
            val controls = nodes.map { (name, node) ->
                node.assertIsDisplayed()
                val measured = bounds(node)
                assertContains("$mode $name", header, measured)
                assertTrue("$mode $name has a narrow touch target", measured.width + 1f >= 48f * host.density)
                assertTrue("$mode $name has a short touch target", measured.height + 1f >= 48f * host.density)
                name to measured
            }
            controls.forEachIndexed { index, first ->
                controls.drop(index + 1).forEach { second -> assertNoOverlap(first, second) }
            }
            val options = controls.single { it.first == "Options" }.second
            assertTrue("$mode Options must stay at the header's trailing edge",
                abs(options.right - header.right) <= 1f)
            val title = bounds(rule.onNodeWithText("GZ Editor Pro", substring = false))
            assertNoOverlap("Title" to title, "Options" to options)
            rule.onNodeWithContentDescription("Undo").assertIsNotEnabled()
            rule.onNodeWithContentDescription("Redo").assertIsNotEnabled()
            rule.onNodeWithContentDescription("Undo").performTouchInput { click() }
            rule.onNodeWithContentDescription("Redo").performTouchInput { click() }
            rule.runOnIdle { assertTrue("Disabled history controls invoked actions", host.historyInvoked.isEmpty()) }
        }
    }

    @Test fun narrowDoubleFontOptionsExposeEachMajorCommandOnceAndInvokeTheSelectedAction() {
        val host = mount()
        for (mode in modes) {
            rule.runOnIdle { host.workspace.value = mode }
            for ((id, label) in host.commands) {
                // Major commands are owned by Options, not duplicated in the closed header.
                rule.onAllNodesWithText(label, substring = false).assertCountEquals(0)
                rule.onNodeWithTag("workspace-options").assertIsEnabled().performClick()
                host.commands.forEach { (commandId, _) ->
                    rule.onAllNodesWithTag("workspace-command-" + commandId.name.lowercase()).assertCountEquals(1)
                }
                val selected = rule.onNodeWithTag("workspace-command-" + id.name.lowercase())
                selected.performScrollTo().assertIsDisplayed().assertIsEnabled()
                val before = host.invoked.size
                selected.performClick()
                rule.runOnIdle {
                    assertEquals(before + 1, host.invoked.size)
                    assertEquals(id, host.invoked.last())
                }
                rule.onAllNodesWithTag("workspace-command-" + id.name.lowercase()).assertCountEquals(0)
                rule.onNodeWithTag("workspace-options").assertIsDisplayed()
            }
        }
    }
}

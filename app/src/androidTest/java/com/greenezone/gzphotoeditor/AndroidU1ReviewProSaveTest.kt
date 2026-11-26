package com.greenezone.gzphotoeditor

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.NativeWorkStore
import com.greenezone.gzphotoeditor.editor.ProLayerKind
import com.greenezone.gzphotoeditor.editor.ProStore
import com.greenezone.gzphotoeditor.editor.ProjectSaveIntent
import com.greenezone.gzphotoeditor.ui.PhotoEditorScreen
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidU1ReviewProSaveTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test fun canceledInlineProSaveAcknowledgesOnlyItsCapturedRevisionAndReusesTheNamedTarget() {
        val root = Files.createTempDirectory(rule.activity.cacheDir.toPath(), "u1r-held-pro-save-").toFile()
        val files = File(root, "files").apply { check(mkdir()) }
        val data = File(root, "data").apply { check(mkdir()) }
        val context = object : ContextWrapper(rule.activity) {
            override fun getFilesDir(): File = files
            override fun getDataDir(): File = data
        }
        val nativeStore = NativeWorkStore(context)
        val mounted = mutableStateOf(true)
        val entered = CompletableDeferred<ProjectSaveIntent>()
        val release = CompletableDeferred<Unit>()
        val name = "Pro held " + System.nanoTime().toString().takeLast(10)
        try {
            rule.setContent {
                if (mounted.value) {
                    CompositionLocalProvider(LocalContext provides context) {
                        MaterialTheme(colorScheme = darkColorScheme()) {
                            PhotoEditorScreen(beforeInlineWorkspaceSave = { intent ->
                                entered.complete(intent)
                                release.await()
                            })
                        }
                    }
                }
            }
            chooseWorkspace("Photo editing Pro")
            waitForText("Photo Pro ▾")
            tapText("Options")
            tapText("New / Open media…")
            tapText("New canvas…")
            waitForText("+ Add layer")
            tapText("+ Add layer", scroll = true)
            tapText("Shape layer")
            rule.onNodeWithTag("workspace-tools").performTouchInput { click(center) }
            rule.onNode(hasText("Layers", substring = false) and hasAnyAncestor(isDialog()))
                .performScrollTo().performTouchInput { click(center) }
            rule.onNodeWithText("Shape layer 1 · Shape", substring = false).assertIsDisplayed()

            val leasesBefore = runBlocking { nativeStore.outstandingStageLeasesForTest() }
            val switchTitle = "Switch to Photo editing?"
            chooseWorkspace("Photo editing")
            rule.onNodeWithText(switchTitle, substring = false).assertIsDisplayed()
            tapText("Save current work")
            rule.onNode(hasSetTextAction() and hasAnyAncestor(isDialog())).performTextReplacement(name)
            tapText("Save current work")
            rule.waitUntil(15_000) { entered.isCompleted }
            assertEquals(ProjectSaveIntent.Create(name), runBlocking { entered.await() })
            assertEquals(leasesBefore + 1, runBlocking { nativeStore.outstandingStageLeasesForTest() })
            assertNull("Publication must still be held", ProStore.loadNamed(context, name))

            tapText("Cancel")
            rule.onNodeWithText(switchTitle, substring = false).assertDoesNotExist()
            rule.onNodeWithText("Photo Pro ▾", substring = false).assertIsDisplayed()
            tapText("Hide", scroll = true)
            rule.onNodeWithText("Show", substring = false).assertIsDisplayed()
            rule.onNodeWithContentDescription("Undo").assertIsEnabled()

            release.complete(Unit)
            rule.waitUntil(15_000) {
                ProStore.loadNamed(context, name) != null &&
                    runBlocking { nativeStore.outstandingStageLeasesForTest() } == leasesBefore
            }
            val firstSave = requireNotNull(ProStore.loadNamed(context, name))
            val captured = firstSave.content.document
            assertEquals(name, firstSave.project.name)
            assertEquals(listOf(name), ProStore.names(context))
            assertEquals(ProLayerKind.SHAPE, captured.layers.single().kind)
            assertTrue("The inline Save must publish the earlier visible revision", captured.layers.single().visible)
            rule.onNodeWithText("Photo Pro ▾", substring = false).assertIsDisplayed()
            rule.onNodeWithText("Show", substring = false).assertIsDisplayed()
            rule.onNodeWithText(switchTitle, substring = false).assertDoesNotExist()

            // The later edit remains live. Undo reaches exactly the clean saved revision.
            rule.onNodeWithContentDescription("Undo").performTouchInput { click(center) }
            rule.onNodeWithText("Hide", substring = false).assertIsDisplayed()
            tapText("Options")
            rule.onNodeWithText("Save project", substring = false).assertIsNotEnabled()
            rule.onNodeWithText("Already saved.", substring = false).assertIsDisplayed()
            tapText("Save project as…")
            rule.onNode(hasSetTextAction() and hasAnyAncestor(isDialog())).assert(hasText("$name copy"))
            tapText("Cancel")

            rule.onNodeWithContentDescription("Redo").performTouchInput { click(center) }
            rule.onNodeWithText("Show", substring = false).assertIsDisplayed()
            tapText("Options")
            rule.onNodeWithText("Save project", substring = false).assertIsEnabled()
            tapText("Save project")
            rule.onNodeWithText("Save Pro project", substring = false).assertDoesNotExist()
            waitForText("Editable Pro project saved")
            val updated = requireNotNull(ProStore.loadNamed(context, name))
            assertEquals(firstSave.project.id, updated.project.id)
            assertNotEquals(firstSave.project.version, updated.project.version)
            assertEquals(captured.copy(layers = listOf(captured.layers.single().copy(visible = false))),
                updated.content.document)
            assertEquals(listOf(name), ProStore.names(context))
            rule.onNodeWithText("Photo Pro ▾", substring = false).assertIsDisplayed()
        } finally {
            release.complete(Unit)
            rule.runOnIdle { mounted.value = false }
            rule.waitForIdle()
            root.deleteRecursively()
        }
    }

    private fun chooseWorkspace(label: String) {
        rule.onNodeWithContentDescription("Choose editing workspace").performTouchInput { click(center) }
        tapText(label)
    }

    private fun waitForText(label: String) {
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText(label, substring = false).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun tapText(label: String, scroll: Boolean = false) {
        val node = rule.onNodeWithText(label, substring = false)
        if (scroll) node.performScrollTo()
        node.assertIsDisplayed().performTouchInput { click(center) }
    }
}

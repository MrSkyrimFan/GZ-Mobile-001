package com.greenezone.gzphotoeditor

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.Grade
import com.greenezone.gzphotoeditor.editor.Look
import com.greenezone.gzphotoeditor.editor.MaskKind
import com.greenezone.gzphotoeditor.ui.EditTab
import com.greenezone.gzphotoeditor.ui.EditorDock
import com.greenezone.gzphotoeditor.ui.PhotoEditorScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhotoFirstUiTest {
    @get:Rule val rule = createComposeRule()

    @Test fun firstOpenExplainsHowToStartWithoutTinyPreview() {
        rule.setContent { MaterialTheme(colorScheme = darkColorScheme()) { PhotoEditorScreen() } }
        rule.onNodeWithText("Your photo deserves the whole screen.").assertIsDisplayed()
        rule.onNodeWithText("Open photo").assertIsDisplayed()
        rule.onNodeWithText("Export").assertExists()
    }

    @Test fun swipableLooksStartAtNoneAndSelectingGoldenChangesGrade() {
        var selected = ""
        var result = Grade()
        rule.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                EditorDock(
                    tab = EditTab.LOOKS,
                    onTab = {},
                    document = EditDocument(),
                    original = null,
                    thumbnails = emptyList(),
                    savedLooks = emptyList(),
                    activeLook = "none",
                    busy = false,
                    selectedMaskIndex = 0,
                    onSelectMask = {},
                    onCommit = {},
                    onSlide = {},
                    onSlideFinished = {},
                    onSelectLook = { key, grade -> selected = key; result = grade },
                    onAddMask = {},
                    onDeleteMask = {},
                    onUndoBrush = {},
                    onClearBrush = {},
                    onAutoTone = {},
                    onAutoWB = {},
                    onSaveLook = {},
                    onSaveProject = {},
                    onProjects = {},
                    onOpen = {}
                )
            }
        }
        rule.onNodeWithText("None").assertIsDisplayed()
        rule.onNodeWithText("Golden").performClick()
        rule.runOnIdle {
            assertEquals("preset-1", selected)
            assertNotEquals(Grade(), result)
        }
    }

    @Test fun selectingLightToolShowsOneSliderAtATime() {
        rule.setContent {
            var tab by remember { mutableStateOf(EditTab.LOOKS) }
            var edits by remember { mutableStateOf(EditDocument()) }
            MaterialTheme(colorScheme = darkColorScheme()) {
                EditorDock(
                    tab = tab,
                    onTab = { tab = it },
                    document = edits,
                    original = null,
                    thumbnails = emptyList(),
                    savedLooks = emptyList(),
                    activeLook = "none",
                    busy = false,
                    selectedMaskIndex = 0,
                    onSelectMask = {},
                    onCommit = { edits = it },
                    onSlide = { edits = it },
                    onSlideFinished = {},
                    onSelectLook = { _, grade -> edits = edits.copy(grade = grade) },
                    onAddMask = {},
                    onDeleteMask = {},
                    onUndoBrush = {},
                    onClearBrush = {},
                    onAutoTone = {},
                    onAutoWB = {},
                    onSaveLook = {},
                    onSaveProject = {},
                    onProjects = {},
                    onOpen = {}
                )
            }
        }
        rule.onNodeWithText("Light").performClick()
        rule.onNodeWithContentDescription("Adjust Exposure").assertExists()
        rule.onNodeWithContentDescription("Adjust Contrast").assertDoesNotExist()
        rule.onAllNodesWithText("Contrast")[0].performClick()
        rule.onNodeWithContentDescription("Adjust Contrast").assertExists()
        rule.onNodeWithContentDescription("Adjust Exposure").assertDoesNotExist()
    }
}

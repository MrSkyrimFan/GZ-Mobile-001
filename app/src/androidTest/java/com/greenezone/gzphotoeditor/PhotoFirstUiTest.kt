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
import com.greenezone.gzphotoeditor.editor.CreativeDocument
import com.greenezone.gzphotoeditor.editor.TextOverlay
import com.greenezone.gzphotoeditor.editor.CropRatio
import com.greenezone.gzphotoeditor.editor.ExportSettings
import com.greenezone.gzphotoeditor.editor.ImageExportFormat
import com.greenezone.gzphotoeditor.editor.Grade
import com.greenezone.gzphotoeditor.editor.Look
import com.greenezone.gzphotoeditor.editor.MaskKind
import com.greenezone.gzphotoeditor.ui.EditTab
import com.greenezone.gzphotoeditor.ui.EditorDock
import com.greenezone.gzphotoeditor.ui.ExportDialog
import com.greenezone.gzphotoeditor.ui.PhotoEditorScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhotoFirstUiTest {
    @get:Rule val rule = createComposeRule()

    @Test fun workspaceMenuSeparatesModesAndKeepsVideoDisabled() {
        rule.setContent { MaterialTheme(colorScheme = darkColorScheme()) { PhotoEditorScreen() } }
        rule.onNodeWithContentDescription("Choose editing workspace").performClick()
        rule.onNodeWithText("Photo editing Pro").assertExists()
        rule.onNodeWithText("Collage maker").assertExists()
        rule.onNodeWithText("GIF maker").assertExists()
        rule.onNodeWithText("Video editing · Coming soon").assertExists()
        rule.onNodeWithText("Photo editing Pro").performClick()
        rule.onNodeWithText("Add first photo").assertExists()
    }

    @Test fun proLayerCreationIsReachableFromCleanCanvas() {
        var lastDocument = com.greenezone.gzphotoeditor.editor.ProDocument()
        rule.setContent {
            var editable by remember {
                mutableStateOf(com.greenezone.gzphotoeditor.editor.ProDocument())
            }
            lastDocument = editable
            MaterialTheme(colorScheme = darkColorScheme()) {
                com.greenezone.gzphotoeditor.ui.ProWorkspaceScreen(
                    document = editable,
                    onDocument = { editable = it },
                    onBack = {},
                    onSwitch = {}
                )
            }
        }
        rule.onNodeWithText("Or start with a blank canvas").performClick()
        rule.onNodeWithText("+ Add layer").performClick()
        rule.onNodeWithText("Words / Text").performClick()
        rule.onNodeWithText("Apply").performClick()
        rule.runOnIdle {
            assertEquals(2, lastDocument.layers.size)
            assertEquals(com.greenezone.gzphotoeditor.editor.ProLayerKind.DRAWING,
                lastDocument.layers[0].kind)
            assertEquals(com.greenezone.gzphotoeditor.editor.ProLayerKind.TEXT,
                lastDocument.layers[1].kind)
        }
    }

    @Test fun proUsesItsOwnPhotoshopToolStripAndCenterActuallyMovesALayer() {
        var last = com.greenezone.gzphotoeditor.editor.ProDocument()
        rule.setContent {
            var doc by remember {
                mutableStateOf(com.greenezone.gzphotoeditor.editor.ProDocument(
                    listOf(com.greenezone.gzphotoeditor.editor.ProLayer(
                        uri = "", name = "Shape",
                        kind = com.greenezone.gzphotoeditor.editor.ProLayerKind.SHAPE,
                        x = .2f, y = .8f,
                        edits = com.greenezone.gzphotoeditor.editor.EditDocument(
                            creative = com.greenezone.gzphotoeditor.editor.CreativeDocument(
                                shapes = listOf(
                                    com.greenezone.gzphotoeditor.editor.ShapeOverlay()
                                )
                            )
                        )
                    ))
                ))
            }
            last = doc
            MaterialTheme(colorScheme = darkColorScheme()) {
                com.greenezone.gzphotoeditor.ui.ProWorkspaceScreen(
                    document = doc,
                    onDocument = { doc = it },
                    onBack = {},
                    onSwitch = {}
                )
            }
        }
        rule.onNodeWithText("Tools ▾").performClick()
        rule.onNodeWithText("Select & mask").assertExists()
        rule.onNodeWithText("Clone & repair").assertExists()
        rule.onNodeWithText("Transform").performClick()
        rule.onNodeWithText("Center on canvas").performClick()
        rule.runOnIdle {
            assertEquals(.5f, last.layers[0].x, .001f)
            assertEquals(.5f, last.layers[0].y, .001f)
        }
        rule.onNodeWithText("Looks").assertDoesNotExist()
        rule.onNodeWithText("Exposure").assertDoesNotExist()
        rule.onNodeWithText("Edit photo").assertDoesNotExist()
    }

    @Test fun videoGifLandingAsksOnlyForOneVideo() {
        rule.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                com.greenezone.gzphotoeditor.ui.VideoGifScreen(
                    document = com.greenezone.gzphotoeditor.editor.GifDocument(),
                    onDocument = {},
                    onBack = {},
                    onSwitch = {},
                    onOpenLegacyPhotoProject = {}
                )
            }
        }
        rule.onNodeWithText("Select one video to turn into a GIF").assertIsDisplayed()
        rule.onNodeWithText("Choose video").assertIsDisplayed()
        rule.onNodeWithText("Open saved GIF project").assertExists()
        rule.onNodeWithText("Instead: animate multiple photos").assertDoesNotExist()
    }

    @Test fun firstOpenExplainsHowToStartWithoutTinyPreview() {
        rule.setContent { MaterialTheme(colorScheme = darkColorScheme()) { PhotoEditorScreen() } }
        rule.onNodeWithText("Open media").assertIsDisplayed()
        rule.onNodeWithText("© 2026 Greenezone Software LLC").assertExists()
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
                    selectedPrivacyIndex = 0,
                    onSelectPrivacy = {},
                    onAddPrivacy = {},
                    onDeletePrivacy = {},
                    onUndoPrivacyStroke = {},
                    onClearPrivacy = {},
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

    @Test fun privacyPanelShowsBlurToolWithoutOpeningLongSliderList() {
        rule.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                EditorDock(
                    tab = EditTab.PRIVACY,
                    onTab = {},
                    document = EditDocument(),
                    original = null,
                    thumbnails = emptyList(),
                    savedLooks = emptyList(),
                    activeLook = "none",
                    busy = false,
                    selectedMaskIndex = 0,
                    onSelectMask = {},
                    selectedPrivacyIndex = 0,
                    onSelectPrivacy = {},
                    onAddPrivacy = {},
                    onDeletePrivacy = {},
                    onUndoPrivacyStroke = {},
                    onClearPrivacy = {},
                    onCommit = {},
                    onSlide = {},
                    onSlideFinished = {},
                    onSelectLook = { _, _ -> },
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
        rule.onNodeWithText("+ Blur").assertIsDisplayed()
    }

    @Composable
    private fun Fixture(
        tab: EditTab,
        document: EditDocument = EditDocument(),
        onCommit: (EditDocument) -> Unit = {},
        onAddWords: () -> Unit = {}
    ) {
        EditorDock(
            tab = tab,
            onTab = {},
            document = document,
            original = null,
            thumbnails = emptyList(),
            savedLooks = emptyList(),
            activeLook = "none",
            busy = false,
            selectedMaskIndex = 0,
            onSelectMask = {},
            selectedPrivacyIndex = 0,
            onSelectPrivacy = {},
            onAddPrivacy = {},
            onDeletePrivacy = {},
            onUndoPrivacyStroke = {},
            onClearPrivacy = {},
            onCommit = onCommit,
            onSlide = {},
            onSlideFinished = {},
            onSelectLook = { _, _ -> },
            onAddMask = {},
            onDeleteMask = {},
            onUndoBrush = {},
            onClearBrush = {},
            onAutoTone = {},
            onAutoWB = {},
            onSaveLook = {},
            onSaveProject = {},
            onProjects = {},
            onOpen = {},
            onNewText = onAddWords,
            cropImageWidth = 1200,
            cropImageHeight = 900
        )
    }

    @Test fun textToolsExposeEditableFontsAndAddText() {
        var created = false
        rule.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Fixture(
                    EditTab.TEXT,
                    EditDocument(creative = CreativeDocument(
                        text = listOf(TextOverlay(content = "Test caption"))
                    )),
                    onAddWords = { created = true }
                )
            }
        }
        rule.onNodeWithText("+ Add text").performClick()
        rule.onNodeWithText("Modern").assertIsDisplayed()
        rule.runOnIdle { assertEquals(true, created) }
    }

    @Test fun drawingShowsBrushEraserAndShapeSelection() {
        rule.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Fixture(EditTab.DRAW)
            }
        }
        rule.onNodeWithText("Brush").assertIsDisplayed()
        rule.onNodeWithText("Eraser").performClick()
        rule.onNodeWithText("Brush thickness").assertExists()
    }

    @Test fun manualCropPresetCreatesEditableAspectFrame() {
        var value: EditDocument? = null
        rule.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Fixture(EditTab.CROP, onCommit = { value = it })
            }
        }
        rule.onNodeWithText("1:1").performClick()
        rule.runOnIdle {
            val changed = value
            assertEquals(CropRatio.SQUARE, changed?.crop)
            assertEquals(true, changed?.cropFrame != null)
        }
    }

    @Test fun exportHasSeparateFormatAndPlatformDropdowns() {
        rule.setContent {
            var settings by remember { mutableStateOf(ExportSettings()) }
            MaterialTheme(colorScheme = darkColorScheme()) {
                ExportDialog(settings,
                    onSettings = { settings = it },
                    onSave = {},
                    onDismiss = {})
            }
        }
        rule.onNodeWithText("FILE FORMAT").assertIsDisplayed()
        rule.onNodeWithText("SOCIAL PLATFORM / DIMENSIONS").assertIsDisplayed()
        rule.onNodeWithText("JPEG · JPG  ▾").performClick()
        rule.onNodeWithText("PNG · Lossless").performClick()
        rule.onNodeWithText("Save PNG").assertExists()
    }

    @Test fun homeScreenOpensAndClosesCollageWithoutChangingPhotoEditor() {
        rule.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                PhotoEditorScreen()
            }
        }
        rule.onNodeWithText("Open media").assertIsDisplayed()
        rule.onNodeWithText("Create collage").performClick()
        rule.onNodeWithText("Collage").assertIsDisplayed()
        rule.onNodeWithText("Back").performClick()
        rule.onNodeWithText("Open media").assertIsDisplayed()
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
                    selectedPrivacyIndex = 0,
                    onSelectPrivacy = {},
                    onAddPrivacy = {},
                    onDeletePrivacy = {},
                    onUndoPrivacyStroke = {},
                    onClearPrivacy = {},
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
        rule.onNodeWithText("Reset").assertIsDisplayed()
        rule.onNodeWithContentDescription("Adjust Contrast").assertDoesNotExist()
        rule.onAllNodesWithText("Contrast")[0].performClick()
        rule.onNodeWithContentDescription("Adjust Contrast").assertExists()
        rule.onNodeWithContentDescription("Adjust Exposure").assertDoesNotExist()
    }
}

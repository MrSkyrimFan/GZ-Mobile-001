package com.greenezone.gzphotoeditor.ui

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.Grade
import com.greenezone.gzphotoeditor.editor.Look
import com.greenezone.gzphotoeditor.editor.PrivacyKind

internal class RegularDockState(
    val tab: EditTab,
    val document: EditDocument,
    val original: Bitmap?,
    val thumbnails: List<Bitmap>,
    val savedLooks: List<Look>,
    val activeLook: String,
    val busy: Boolean,
    val privacyIndex: Int,
    val isStandalone: Boolean,
    val showHistogram: Boolean,
    val creativeTool: CreativeToolState,
    val cropSize: Pair<Int, Int>?
)

internal class RegularDockActions(
    val onTab: (EditTab) -> Unit,
    val onSelectPrivacy: (Int) -> Unit,
    val onAddPrivacy: (PrivacyKind) -> Unit,
    val onDeletePrivacy: () -> Unit,
    val onUndoPrivacyStroke: () -> Unit,
    val onClearPrivacy: () -> Unit,
    val onCommit: (EditDocument) -> Unit,
    val onSlide: (EditDocument) -> Unit,
    val onSlideFinished: () -> Unit,
    val onSelectLook: (String, Grade) -> Unit,
    val onAutoTone: () -> Unit,
    val onAutoWB: () -> Unit,
    val onSaveLook: () -> Unit,
    val onSaveProject: () -> Unit,
    val onProjects: () -> Unit,
    val onOpen: () -> Unit,
    val onCollage: () -> Unit,
    val onCreativeTool: (CreativeToolState) -> Unit,
    val onNewText: () -> Unit,
    val onEditText: () -> Unit,
    val onHistogram: () -> Unit,
    val onCommitAndSelect: (EditDocument, CreativeToolState) -> Unit
)

@Composable
internal fun RegularPhotoDock(state: RegularDockState, actions: RegularDockActions) {
    val (cropWidth, cropHeight) = state.cropSize ?: (1 to 1)
    val histogram = actions.onHistogram.takeIf { state.showHistogram }
    EditorDock(
        tab = state.tab,
        onTab = actions.onTab,
        document = state.document,
        original = state.original,
        thumbnails = state.thumbnails,
        savedLooks = state.savedLooks,
        activeLook = state.activeLook,
        busy = state.busy,
        selectedPrivacyIndex = state.privacyIndex,
        onSelectPrivacy = actions.onSelectPrivacy,
        onAddPrivacy = actions.onAddPrivacy,
        onDeletePrivacy = actions.onDeletePrivacy,
        onUndoPrivacyStroke = actions.onUndoPrivacyStroke,
        onClearPrivacy = actions.onClearPrivacy,
        onCommit = actions.onCommit,
        onSlide = actions.onSlide,
        onSlideFinished = actions.onSlideFinished,
        onSelectLook = actions.onSelectLook,
        onAutoTone = actions.onAutoTone,
        onAutoWB = actions.onAutoWB,
        onSaveLook = actions.onSaveLook,
        onSaveProject = actions.onSaveProject,
        onProjects = actions.onProjects,
        onOpen = actions.onOpen,
        onCollage = actions.onCollage,
        showCollage = state.isStandalone,
        creativeTool = state.creativeTool,
        onCreativeTool = actions.onCreativeTool,
        onNewText = actions.onNewText,
        onEditText = actions.onEditText,
        cropImageWidth = cropWidth,
        cropImageHeight = cropHeight,
        onHistogram = histogram,
        onCommitAndSelect = actions.onCommitAndSelect
    )
}

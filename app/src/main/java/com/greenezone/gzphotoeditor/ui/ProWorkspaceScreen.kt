package com.greenezone.gzphotoeditor.ui

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenezone.gzphotoeditor.editor.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal enum class ProTransform(val title: String) {
    SIZE("Size"), OPACITY("Opacity"), HORIZONTAL("X"), VERTICAL("Y"), ROTATE("Rotate")
}

private data class ProBrushDraft(val id: Long, val layer: Int, val stroke: InkStroke)
private data class ProBrushFrame(val draftId: Long, val bitmap: Bitmap)
private data class ProSaveRequest(
    val name: String,
    val document: ProDocument,
    val session: ProWorkspaceSession,
    val token: WorkspaceOperationToken,
    val intent: ProjectSaveIntent
)

private sealed interface ProDestination {
    data object Back : ProDestination
    data class Switch(val mode: WorkspaceMode) : ProDestination
    data object NewCanvas : ProDestination
    data object OpenPhoto : ProDestination
    data class OpenProject(val name: String) : ProDestination
    data class ReplaceImage(val index: Int, val layer: ProLayer) : ProDestination
}
private enum class ProDepartureStage { PROMPT, READY, PICKER, LOADING, SAVING }
private data class ProDepartureRequest(
    val token: WorkspaceOperationToken,
    val document: ProDocument,
    val session: ProWorkspaceSession,
    val destination: ProDestination,
    val discard: Boolean = false
)

internal data class ProWorkspaceSession(
    val documentEstablished: Boolean = false,
    val selectedLayer: Int = 0,
    val section: ProSection = ProSection.LAYERS,
    val transform: ProTransform = ProTransform.SIZE,
    val viewport: ProViewport = ProViewport(),
    val viewportAspect: Float = 1f,
    val history: ProHistory = ProHistory(),
    val resetDocument: ProDocument? = null,
    val chromeVisible: Boolean = true,
    val panelExpanded: Boolean = true,
    val brushColor: Int = 0xFF96E5C2.toInt(),
    val brushSize: Float = .012f,
    val brushOpacity: Float = 1f,
    val eraserMode: Boolean = false,
    val selectionKind: ProSelectionKind = ProSelectionKind.RECTANGLE,
    val retouchKind: ProRetouchKind = ProRetouchKind.CLONE,
    val retouchRadius: Float = .04f,
    val retouchStrength: Float = 1f,
    val saveTarget: ProSaveTarget = ProSaveTarget(),
    val nextWorkId: Long = 1,
    val entryDocument: ProDocument? = null,
    val baselines: Map<Long, ProWorkBaseline> = emptyMap(),
    val ownerId: String = java.util.UUID.randomUUID().toString(),
    val contentRevision: Long = 0,
    val sourceRevision: Long = 0
)

internal data class ProInlineSaveAcknowledgement(
    val ownerKey: String,
    val previousTarget: NamedProjectRef?,
    val capturedDocument: ProDocument,
    val saved: NamedProjectRef
)

internal fun ProWorkspaceSession.nativeOwnerKey(): String =
    saveTarget.nativeOwnerId ?: (ownerId + "/" + saveTarget.workId)

/** Used by the parent coordinator while this workspace is inactive. */
internal fun ProWorkspaceSession.baseline(document: ProDocument): ProDocument =
    baselines[saveTarget.workId]?.cleanDocument ?: entryDocument ?: document

internal fun ProWorkspaceSession.isDirty(document: ProDocument): Boolean = document != baseline(document)

internal fun ProWorkspaceSession.discarded(document: ProDocument): Pair<ProDocument, ProWorkspaceSession> {
    val clean = baseline(document)
    return clean to copy(
        history = ProHistory(),
        selectedLayer = selectedLayer.coerceIn(0, clean.layers.lastIndex.coerceAtLeast(0)),
        contentRevision = contentRevision + if (clean != document) 1 else 0,
        sourceRevision = sourceRevision + if (clean.layers.map { it.uri } != document.layers.map { it.uri }) 1 else 0
    )
}

internal fun ProWorkspaceSession.saved(document: ProDocument, ref: NamedProjectRef): ProWorkspaceSession {
    require(ref.kind == ProjectKind.PRO)
    val target = saveTarget.copy(name = ref.name, savedRef = ref)
    val matchingWork = (history.undo + history.redo).filter {
        it.saveTarget.savedRef?.id == ref.id
    }.map { it.saveTarget.workId }.toSet() + saveTarget.workId
    val nextBaselines = baselines.toMutableMap()
    matchingWork.forEach { work ->
        val previous = nextBaselines[work]
        nextBaselines[work] = ProWorkBaseline(
            previous?.entryDocument ?: entryDocument ?: document, document
        )
    }
    return copy(saveTarget = target, baselines = nextBaselines,
        history = history.bindSavedProject(target), entryDocument = entryDocument ?: document)
}

/** True independently editable bitmap/vector/adjustment layer workspace. */
@Composable
fun ProWorkspaceScreen(
    document: ProDocument,
    onDocument: (ProDocument) -> Unit,
    onBack: () -> Unit,
    onSwitch: (WorkspaceMode) -> Unit
) {
    var session by remember { mutableStateOf(ProWorkspaceSession()) }
    ProWorkspaceScreenContent(
        document = document,
        onDocument = onDocument,
        onBack = onBack,
        onSwitch = onSwitch,
        session = session,
        onSession = { session = it }
    )
}

@Composable
internal fun ProWorkspaceScreenContent(
    document: ProDocument,
    onDocument: (ProDocument) -> Unit,
    onBack: () -> Unit,
    onSwitch: (WorkspaceMode) -> Unit,
    session: ProWorkspaceSession,
    onSession: (ProWorkspaceSession) -> Unit,
    onRequestRegularTransfer: ((ProDocument, ProWorkspaceSession) -> Unit)? = null,
    onRevokeWorkingSession: (suspend (String) -> Boolean)? = null,
    saveCapturedProject: (suspend (ProjectSaveIntent, ProDocument, ProWorkspaceSession) -> ProjectSaveResult)? = null,
    prepareOpenedNamedProject: (suspend (NamedProject<ProStore.SavedProject>) -> Boolean)? = null,
    onOpenedNamedProject: ((NamedProject<ProStore.SavedProject>, ProWorkspaceSession) -> Unit)? = null,
    workingDrafts: List<Pair<String, String>> = emptyList(),
    onOpenWorkingDraft: ((String, ProDocument, ProWorkspaceSession) -> Unit)? = null,
    linkedDraftDirty: Boolean = false,
    inlineSaveAcknowledgement: ProInlineSaveAcknowledgement? = null,
    onInlineSaveAcknowledged: (ProInlineSaveAcknowledgement) -> Unit = {}
) {
    val context = LocalContext.current
    val resolver = context.contentResolver
    val scope = rememberCoroutineScope()
    var selected by remember {
        mutableIntStateOf(session.selectedLayer.coerceIn(
            0, document.layers.lastIndex.coerceAtLeast(0)
        ))
    }
    var section by remember { mutableStateOf(session.section) }
    var transform by remember { mutableStateOf(session.transform) }
    var chrome by remember { mutableStateOf(session.chromeVisible) }
    var expanded by remember { mutableStateOf(session.panelExpanded) }
    var finishedOutput by remember { mutableStateOf(document.linkedFinishes.isNotEmpty()) }
    var showTools by remember { mutableStateOf(false) }
    var showStack by remember { mutableStateOf(false) }
    var showFile by remember { mutableStateOf(false) }
    var rename by remember { mutableStateOf<String?>(null) }
    var resetGeneration by remember { mutableIntStateOf(0) }
    var viewport by remember(document.canvasAspectRatio, resetGeneration) {
        mutableStateOf(
            if (resetGeneration == 0 && document.canvasAspectRatio == session.viewportAspect) session.viewport
            else ProViewport()
        )
    }
    var history by remember { mutableStateOf(session.history) }
    var documentEstablished by remember { mutableStateOf(session.documentEstablished) }
    // An untouched landing screen can acquire its first source.
    var resetDocument by remember {
        mutableStateOf(session.resetDocument ?: document.takeIf { it.layers.isNotEmpty() })
    }
    var saveTarget by remember { mutableStateOf(session.saveTarget) }
    var nextWorkId by remember { mutableLongStateOf(session.nextWorkId) }
    var entryDocument by remember { mutableStateOf(session.entryDocument ?: document) }
    var baselines by remember {
        mutableStateOf(session.baselines.ifEmpty {
            mapOf(session.saveTarget.workId to ProWorkBaseline(session.entryDocument ?: document))
        })
    }
    val ownerId = remember { session.ownerId }
    var contentRevision by remember { mutableLongStateOf(session.contentRevision) }
    var sourceRevision by remember { mutableLongStateOf(session.sourceRevision) }
    var pendingDeparture by remember { mutableStateOf<ProDepartureRequest?>(null) }
    var departureStage by remember { mutableStateOf(ProDepartureStage.PROMPT) }
    var retrySave by remember { mutableStateOf<ProSaveRequest?>(null) }
    var sliderStart by remember { mutableStateOf<ProHistoryEntry?>(null) }
    var lastSliderValue by remember { mutableStateOf<ProDocument?>(null) }
    var lastDragValue by remember { mutableStateOf<ProDocument?>(null) }
    var dragStart by remember { mutableStateOf<ProHistoryEntry?>(null) }
    var activeStroke by remember { mutableStateOf<ProBrushDraft?>(null) }
    var nextBrushId by remember { mutableLongStateOf(0L) }
    var brushFrame by remember { mutableStateOf<ProBrushFrame?>(null) }
    var brushColor by remember { mutableIntStateOf(session.brushColor) }
    var brushSize by remember { mutableFloatStateOf(session.brushSize) }
    var brushOpacity by remember { mutableFloatStateOf(session.brushOpacity) }
    var eraserMode by remember { mutableStateOf(session.eraserMode) }
    var selectionKind by remember { mutableStateOf(session.selectionKind) }
    var selectionStart by remember { mutableStateOf<BrushPoint?>(null) }
    var draftSelection by remember { mutableStateOf<ProSelection?>(null) }
    var selectionBeforeDrag by remember { mutableStateOf<ProSelection?>(null) }
    var retouchSource by remember { mutableStateOf<BrushPoint?>(null) }
    var retouchSourceImage by remember { mutableStateOf<BrushPoint?>(null) }
    var retouchKind by remember { mutableStateOf(session.retouchKind) }
    var retouchRadius by remember { mutableFloatStateOf(session.retouchRadius) }
    var retouchStrength by remember { mutableFloatStateOf(session.retouchStrength) }
    val latestDocument by rememberUpdatedState(document)
    var showAdd by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    val showingFinishedOutput = finishedOutput && document.linkedFinishes.isNotEmpty()
    // Keep each mode's raster and pending render separate from the other mode's canvas.
    var preview by remember(showingFinishedOutput) { mutableStateOf<Bitmap?>(null) }
    var rendering by remember(showingFinishedOutput) { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf("") }
    var showExport by remember { mutableStateOf(false) }
    var exportSettings by remember { mutableStateOf(ExportSettings()) }
    val exportJournal = remember(context) { PendingExportJournal(context.filesDir) }
    var exportJournalReady by remember { mutableStateOf(false) }
    var pendingExport by remember { mutableStateOf<PendingExport?>(null) }
    var exportLaunchId by remember { mutableStateOf<String?>(null) }
    var showExportRecovery by remember { mutableStateOf(false) }
    var exportFailure by remember { mutableStateOf<String?>(null) }
    var exportRecoveryUnavailable by remember { mutableStateOf<String?>(null) }
    var showSave by remember { mutableStateOf(false) }
    var showProjects by remember { mutableStateOf(false) }
    var pendingSavedDelete by remember { mutableStateOf<NamedProjectRef?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    var draftName by remember { mutableStateOf("") }
    var pendingReplace by remember { mutableStateOf<ProSaveRequest?>(null) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var openError by remember { mutableStateOf<String?>(null) }
    var savedNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var addPickerOwner by remember { mutableStateOf<Pair<WorkspaceOperationToken, ProDocument>?>(null) }

    val active = document.layers.getOrNull(selected)

    fun clearToolDrafts() {
        selectionBeforeDrag = null
        selectionStart = null
        draftSelection = null
        retouchSource = null
        retouchSourceImage = null
        activeStroke = null
        brushFrame = null
    }
    fun selectLayer(index: Int) {
        if (index != selected) clearToolDrafts()
        selected = index
    }
    fun selectTool(tool: ProSection) {
        if (busy || exporting || pendingDeparture != null) return
        if (tool != section) {
            selectionBeforeDrag = null
            selectionStart = null
            retouchSource = null
            retouchSourceImage = null
            activeStroke = null
            brushFrame = null
        }
        section = tool
        if (tool != ProSection.LAYERS && tool != ProSection.CANVAS) finishedOutput = false
        expanded = true
        showTools = false
    }
    fun publishDocument(next: ProDocument) {
        if (next != latestDocument) {
            if (next != ProDocument()) documentEstablished = true
            contentRevision++
            if (next.layers.map { it.uri } != latestDocument.layers.map { it.uri }) sourceRevision++
        }
        val retainedWork = (history.undo + history.redo).map { it.saveTarget.workId }.toSet() + saveTarget.workId
        baselines = baselines.filterKeys { it in retainedWork }
        onDocument(next)
    }
    fun historyEntry(current: ProDocument = document) =
        ProHistoryEntry(current, resetDocument, selected, saveTarget)

    fun canEditDocument() = !busy && !exporting && pendingDeparture == null

    fun commit(
        next: ProDocument,
        nextReset: ProDocument? = resetDocument,
        nextTarget: ProSaveTarget = saveTarget,
        allowBusy: Boolean = false
    ): Boolean {
        if (!allowBusy && !canEditDocument()) return false
        history = history.checkpoint(historyEntry(), ProHistoryEntry(next, nextReset, selected, nextTarget))
        resetDocument = nextReset
        saveTarget = nextTarget
        sliderStart = null
        lastSliderValue = null
        publishDocument(next)
        return true
    }

    fun moveSelectedLayer(offset: Int) {
        val index = selected
        val destination = index + offset
        if (index !in document.layers.indices || destination !in document.layers.indices) return
        val layers = ProMath.move(document.layers, index, destination)
        if (commit(document.copy(layers = layers))) selected = destination
    }

    fun duplicateSelectedLayer() {
        val index = selected
        val layer = document.layers.getOrNull(index) ?: return
        if (layer.kind == ProLayerKind.TEXT || document.layers.size >= ProMath.MAX_LAYERS) return
        val renamed = layer.copy(name = layer.name.take(32) + " copy")
        val source = layer.sourceSize
        val copy = if (layer.kind == ProLayerKind.IMAGE && source != null) {
            ProImageGeometry.recordImport(renamed, source, placement = layer.sourcePlacement)
        } else renamed
        val layers = document.layers.toMutableList().also { it.add(index + 1, copy) }
        if (commit(document.copy(layers = layers))) selectLayer(index + 1)
    }

    fun deleteSelectedLayer() {
        val index = selected
        if (index !in document.layers.indices) return
        val layers = document.layers.filterIndexed { layerIndex, _ -> layerIndex != index }
        if (commit(document.copy(layers = layers))) {
            clearToolDrafts()
            selected = index.coerceAtMost(layers.lastIndex).coerceAtLeast(0)
        }
    }

    fun toggleLayerVisibility(index: Int) {
        val layer = document.layers.getOrNull(index) ?: return
        val layers = document.layers.toMutableList().also {
            it[index] = layer.copy(visible = !layer.visible)
        }
        if (commit(document.copy(layers = layers)) && index == selected && layer.visible) {
            clearToolDrafts()
        }
    }

    fun changeLayer(transformer: (ProLayer) -> ProLayer, sliding: Boolean = false) {
        if (!canEditDocument() || selected !in document.layers.indices) return
        val next = document.copy(layers = document.layers.toMutableList().also {
            it[selected] = transformer(it[selected])
        })
        if (sliding) {
            if (sliderStart == null) sliderStart = historyEntry()
            lastSliderValue = next
            publishDocument(next)
        } else commit(next)
    }
    /** Trailing lambda convenience preserves one straightforward tool action per click. */
    fun changeLayer(transformer: (ProLayer) -> ProLayer) {
        changeLayer(transformer, false)
    }
    fun finishSlider() {
        sliderStart?.let { history = history.checkpoint(it, historyEntry(lastSliderValue ?: document)) }
        lastSliderValue = null
        sliderStart = null
    }
    fun addLayer(layer: ProLayer) {
        if (!canEditDocument()) return
        val next = ProMath.insert(document, layer)
        if (next == null) { notice = "A project supports up to 12 layers."; return }
        val creationSetup = document.sourceCanvasSetup ?: ProCanvasSetup.from(document)
        commit(next.copy(sourceCanvasSetup = creationSetup), resetDocument ?: ProDocument())
        finishedOutput = false
        selectLayer(next.layers.lastIndex)
        expanded = true
        section = when (layer.kind) {
            ProLayerKind.SHAPE -> ProSection.STYLE
            ProLayerKind.DRAWING -> ProSection.PAINT
            ProLayerKind.ADJUSTMENT -> ProSection.GRADE
            else -> ProSection.LAYERS
        }
    }
    fun addCreative(kind: ProLayerKind) {
        val label = when (kind) {
            ProLayerKind.SHAPE -> "Shape layer"
            ProLayerKind.DRAWING -> "Paint layer"
            ProLayerKind.ADJUSTMENT -> "Adjustment layer"
            else -> error("Expected an editable composition layer")
        } + " " + (document.layers.size + 1)
        val edits = if (kind == ProLayerKind.SHAPE) {
            EditDocument(creative = CreativeDocument(
                shapes = listOf(ShapeOverlay(kind = ShapeKind.RECTANGLE, color = 0xFF111111.toInt()))))
        } else EditDocument()
        addLayer(ProLayer(uri = "", kind = kind, edits = edits, name = label))
        showAdd = false
    }

    fun workspaceSnapshot(): ProWorkspaceSession = ProWorkspaceSession(
        documentEstablished = documentEstablished || resetDocument != null ||
            document.layers.isNotEmpty() || history.undo.isNotEmpty() || history.redo.isNotEmpty(),
        selectedLayer = selected.coerceIn(0, document.layers.lastIndex.coerceAtLeast(0)),
        section = section, transform = transform, viewport = viewport,
        viewportAspect = document.canvasAspectRatio, history = history, resetDocument = resetDocument,
        chromeVisible = chrome, panelExpanded = expanded,
        brushColor = brushColor, brushSize = brushSize, brushOpacity = brushOpacity,
        eraserMode = eraserMode, selectionKind = selectionKind, retouchKind = retouchKind,
        retouchRadius = retouchRadius, retouchStrength = retouchStrength,
        saveTarget = saveTarget, nextWorkId = nextWorkId, entryDocument = entryDocument,
        baselines = baselines, ownerId = ownerId, contentRevision = contentRevision,
        sourceRevision = sourceRevision
    )
    fun persistSession() { onSession(workspaceSnapshot()) }

    fun installMetadata(next: ProWorkspaceSession) {
        history = next.history
        resetDocument = next.resetDocument
        saveTarget = next.saveTarget
        entryDocument = next.entryDocument ?: entryDocument
        baselines = next.baselines
        nextWorkId = next.nextWorkId
        selected = next.selectedLayer
    }

    fun cancelGesture(): Boolean {
        val before = dragStart ?: sliderStart
        val hadDraft = before != null || activeStroke != null || selectionStart != null
        if (!hadDraft) return false
        before?.let { publishDocument(it.document) }
        dragStart = null
        lastDragValue = null
        sliderStart = null
        lastSliderValue = null
        activeStroke = null
        brushFrame = null
        if (selectionStart != null) draftSelection = selectionBeforeDrag
        selectionStart = null
        selectionBeforeDrag = null
        return true
    }

    fun finishPendingHistory() {
        finishSlider()
        dragStart?.let { before ->
            history = history.checkpoint(before, historyEntry(lastDragValue ?: document))
        }
        dragStart = null
        lastDragValue = null
    }

    fun restoreHistory(undo: Boolean) {
        val current = historyEntry(lastDragValue ?: lastSliderValue ?: document)
        finishPendingHistory()
        val (restored, nextHistory) = (if (undo) history.undo(current) else history.redo(current))
            ?: return
        clearToolDrafts()
        history = nextHistory
        resetDocument = restored.resetDocument
        saveTarget = restored.saveTarget
        selected = restored.selectionAfter(current)
        entryDocument = baselines[saveTarget.workId]?.entryDocument ?: restored.document
        publishDocument(restored.document)
    }

    fun hasUnresolvedToolDraft(): Boolean {
        if (activeStroke != null || selectionStart != null) {
            notice = "Finish the current Pro gesture before leaving."
            return true
        }
        if (draftSelection != null) {
            notice = "Apply or clear the current selection before leaving Photo Pro."
            return true
        }
        if (retouchSource != null || retouchSourceImage != null) {
            notice = "Finish or clear the current clone source before leaving Photo Pro."
            return true
        }
        return false
    }

    fun operationToken() = WorkspaceOperationToken(
        ownerId = "$ownerId:${saveTarget.workId}:$sourceRevision",
        contentRevision = contentRevision
    )
    fun owns(token: WorkspaceOperationToken): Boolean =
        token.ownerId == "$ownerId:${saveTarget.workId}:$sourceRevision" &&
            token.contentRevision == contentRevision

    fun cancelDeparture() {
        pendingDeparture = null
        departureStage = ProDepartureStage.PROMPT
    }
    fun cancelSave() {
        if (busy) return
        showSave = false
        pendingReplace = null
        retrySave = null
        saveError = null
        cancelDeparture()
    }
    fun requestDeparture(destination: ProDestination) {
        if (busy || exporting || pendingDeparture != null) {
            notice = "Finish the current Pro operation before leaving."
            return
        }
        if (cancelGesture()) {
            notice = "Unfinished gesture canceled. Your completed edits are kept."
            return
        }
        if (hasUnresolvedToolDraft()) return
        val snapshot = workspaceSnapshot()
        pendingDeparture = ProDepartureRequest(operationToken(), document, snapshot, destination)
        departureStage = if (snapshot.isDirty(document)) ProDepartureStage.PROMPT else ProDepartureStage.READY
    }
    fun requestBack() {
        when {
            busy || exporting -> notice = "Wait for the current Pro operation to finish."
            pendingReplace != null || showSave -> cancelSave()
            pendingDeparture != null -> cancelDeparture()
            showFile -> showFile = false
            showHelp -> showHelp = false
            showAdd -> showAdd = false
            showStack -> showStack = false
            showTools -> showTools = false
            rename != null -> rename = null
            confirmDelete -> confirmDelete = false
            pendingSavedDelete != null -> pendingSavedDelete = null
            showProjects -> showProjects = false
            showExport -> showExport = false
            showExportRecovery -> showExportRecovery = false
            !chrome -> chrome = true
            cancelGesture() -> notice = "Unfinished gesture canceled."
            draftSelection != null || retouchSource != null || retouchSourceImage != null -> {
                clearToolDrafts()
                notice = "Unapplied tool draft cleared."
            }
            else -> requestDeparture(ProDestination.Back)
        }
    }
    fun requestSwitch(mode: WorkspaceMode) {
        if (mode == WorkspaceMode.PRO) return
        val transfer = onRequestRegularTransfer
        if (mode == WorkspaceMode.PHOTO && transfer != null) {
            if (!canEditDocument() || confirmDelete) return
            if (cancelGesture()) {
                notice = "Unfinished gesture canceled. Your completed edits are kept."
                return
            }
            if (hasUnresolvedToolDraft()) return
            showFile = false
            showTools = false
            showStack = false
            transfer(latestDocument, workspaceSnapshot())
        } else requestDeparture(ProDestination.Switch(mode))
    }

    fun openWorkingDraft(key: String) {
        if (!canEditDocument() || cancelGesture() || hasUnresolvedToolDraft()) return
        showProjects = false
        onOpenWorkingDraft?.invoke(key, latestDocument, workspaceSnapshot())
    }

    suspend fun revokeNativeRestoration(nativeOwner: String): Boolean {
        return try {
            val changed = withContext(Dispatchers.IO) {
                // Read and resolve the current durable record under the journal lock;
                // the startup UI read may still be pending or have become stale.
                exportJournal.suppressNativeRestores(listOf(ExportWorkspace.PRO to nativeOwner))
            }
            changed.firstOrNull()?.let { pendingExport = it }
            true
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) {
            notice = "Could not record Discard. Your Pro work remains open."
            false
        }
    }

    fun deleteCurrentWork() {
        if (busy || exporting || pendingDeparture != null) return
        val token = operationToken()
        val before = latestDocument
        val beforeSession = workspaceSnapshot()
        busy = true
        scope.launch {
            try {
                deleteError = null
                val revoke = onRevokeWorkingSession
                val revoked = if (revoke != null) revoke(beforeSession.nativeOwnerKey())
                    else revokeNativeRestoration(beforeSession.nativeOwnerKey())
                if (!revoked) {
                    deleteError = "Could not record Delete. Your Pro work remains open."
                    return@launch
                }
                if (!owns(token) || latestDocument != before) {
                    deleteError = "The work changed. Choose Delete again."
                    return@launch
                }
                val clean = ProDocument()
                resetDocument = null
                documentEstablished = false
                finishedOutput = false
                addPickerOwner = null
                cancelDeparture()
                clearToolDrafts()
                sliderStart = null
                lastSliderValue = null
                dragStart = null
                lastDragValue = null
                history = ProHistory()
                saveTarget = ProSaveTarget(nextWorkId++)
                entryDocument = clean
                baselines = mapOf(saveTarget.workId to ProWorkBaseline(clean))
                selected = 0
                section = ProSection.LAYERS
                transform = ProTransform.SIZE
                val defaults = ProWorkspaceSession()
                selectionKind = defaults.selectionKind
                retouchKind = defaults.retouchKind
                brushColor = defaults.brushColor
                brushSize = defaults.brushSize
                brushOpacity = defaults.brushOpacity
                eraserMode = defaults.eraserMode
                retouchRadius = defaults.retouchRadius
                retouchStrength = defaults.retouchStrength
                chrome = true
                expanded = true
                preview = null
                resetGeneration++
                confirmDelete = false
                publishDocument(clean)
                notice = "Current Pro work deleted."
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                deleteError = "Could not record Delete. Your Pro work remains open."
            } finally { busy = false }
        }
    }

    fun resetAllEdits() {
        if (!canEditDocument() || cancelGesture() || hasUnresolvedToolDraft()) return
        val token = operationToken()
        val before = latestDocument
        val legacy = resetDocument
        val selectedImage = before.layers.getOrNull(selected)?.let { it.kind == ProLayerKind.IMAGE } == true
        val imageIndex = if (selectedImage) before.layers.take(selected).count { it.kind == ProLayerKind.IMAGE } else 0
        busy = true
        scope.launch {
            try {
                val neutral = withContext(Dispatchers.IO) {
                    val geometry = ProImageGeometry.recover(resolver, ProStore.SavedProject(before, legacy ?: before))
                    ProImageGeometry.reset(geometry.document, geometry.resetDocument)
                }
                if (!owns(token) || latestDocument != before) return@launch
                if (neutral == null) {
                    notice = "Could not verify original layer placement. Your edits are kept."
                    return@launch
                }
                if (neutral == before) {
                    notice = "The Pro work is already reset."
                    return@launch
                }
                if (commit(neutral, allowBusy = true)) {
                    clearToolDrafts()
                    selected = imageIndex.coerceAtMost(neutral.layers.lastIndex).coerceAtLeast(0)
                    if (neutral.layers.isEmpty()) section = ProSection.CANVAS
                    notice = "Pro edits reset. Undo restores the previous work."
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                notice = "Could not reset all edits. Your Pro work is kept."
            } finally { busy = false }
        }
    }

    fun saveFailure(reason: ProjectSaveFailure): String = when (reason) {
        ProjectSaveFailure.INVALID_NAME -> "Choose a valid project name."
        ProjectSaveFailure.INVALID_DOCUMENT -> "Add a supported layer before saving."
        ProjectSaveFailure.FULL -> "The saved project limit is reached. Choose an existing project explicitly or delete a saved project."
        ProjectSaveFailure.TOO_LARGE -> "This project exceeds the saved edit limit. Your current work and previous save are kept."
        ProjectSaveFailure.TARGET_CHANGED -> "The saved project changed. Cancel and use Save project as, or reopen it before replacing."
        ProjectSaveFailure.STORAGE_FAILURE -> "Could not save. Check available storage and retry."
    }
    fun saveProject(request: ProSaveRequest) {
        if (busy || exporting) return
        if (!owns(request.token) || latestDocument != request.document) {
            cancelSave()
            notice = "The work changed. Choose Save again for the current document."
            return
        }
        busy = true
        saveError = null
        retrySave = request
        if (pendingDeparture != null) departureStage = ProDepartureStage.SAVING
        draftName = request.name
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val save = saveCapturedProject
                    if (save != null) save(request.intent, request.document, request.session)
                    else ProStore.saveNamed(context, request.intent, request.document, request.session.resetDocument)
                }
                pendingReplace = null
                when (result) {
                    is ProjectSaveResult.Saved -> {
                        val sameOwner = request.token.ownerId == operationToken().ownerId
                        val exact = sameOwner && owns(request.token) && latestDocument == request.document
                        // An update of this same target can advance its persisted baseline even
                        // when newer edits exist. A stale Save as must not hijack their target.
                        if (sameOwner && (exact || saveTarget.savedRef?.id == result.project.id)) {
                            installMetadata(workspaceSnapshot().saved(request.document, result.project))
                        }
                        showSave = false
                        retrySave = null
                        val departure = pendingDeparture
                        if (exact && departure?.token?.operationId == request.token.operationId) {
                            pendingDeparture = departure.copy(session = workspaceSnapshot())
                            departureStage = ProDepartureStage.READY
                        } else if (departure != null) {
                            cancelDeparture()
                        }
                        notice = if (exact) "Editable Pro project saved"
                            else "Captured Pro revision saved. Newer work remains unsaved."
                    }
                    is ProjectSaveResult.Exists -> {
                        pendingReplace = request.copy(
                            name = result.project.name,
                            intent = ProjectSaveIntent.Replace(result.project)
                        )
                        showSave = true
                    }
                    is ProjectSaveResult.Rejected -> {
                        saveError = saveFailure(result.reason)
                        showSave = true
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                saveError = saveFailure(ProjectSaveFailure.STORAGE_FAILURE)
                showSave = true
            } finally {
                busy = false
            }
        }
    }
    fun startSave(asCopy: Boolean = false) {
        if (busy || exporting) return
        if (cancelGesture()) {
            notice = "Unfinished gesture canceled. Save the completed result."
            return
        }
        if (hasUnresolvedToolDraft()) return
        if (!ProMath.valid(document)) {
            notice = "Add a layer before saving this setup."
            return
        }
        val departure = pendingDeparture
        if (departure != null && (!owns(departure.token) || document != departure.document)) {
            cancelDeparture()
            notice = "The work changed. Choose the departure again."
            return
        }
        saveError = null
        retrySave = null
        val ref = saveTarget.savedRef
        val token = departure?.token ?: operationToken()
        if (!asCopy && ref != null) {
            if (!linkedDraftDirty && !workspaceSnapshot().isDirty(document) && departure == null) {
                notice = "Already saved."
                return
            }
            saveProject(ProSaveRequest(ref.name, document, workspaceSnapshot(), token,
                ProjectSaveIntent.Update(ref)))
        } else {
            draftName = if (asCopy) saveTarget.name?.let { "$it copy" }.orEmpty() else ""
            showSave = true
            if (departure != null) departureStage = ProDepartureStage.SAVING
        }
    }
    fun openProjects() {
        if (busy || exporting || pendingDeparture != null) return
        openError = null
        showProjects = true
        scope.launch {
            savedNames = withContext(Dispatchers.IO) { ProStore.names(context) }
        }
    }
    fun openProject(name: String) {
        showProjects = false
        requestDeparture(ProDestination.OpenProject(name))
    }

    fun resolved(request: ProDepartureRequest): Pair<ProDocument, ProWorkspaceSession> =
        if (request.discard) request.session.discarded(request.document)
        else request.document to request.session

    suspend fun finishDeparture(
        request: ProDepartureRequest,
        prepared: ProDocument? = null,
        preparedReset: ProDocument? = null,
        project: NamedProjectRef? = null,
        replacementOnly: Boolean = false,
        preparedSelection: Int? = null,
        openedNamedProject: NamedProject<ProStore.SavedProject>? = null,
        priorRecoverySuperseded: Boolean = false
    ) {
        if (pendingDeparture?.token?.operationId != request.token.operationId ||
            !owns(request.token) || latestDocument != request.document) {
            cancelDeparture()
            notice = "The work changed. The pending action was canceled."
            return
        }
        if (request.discard && !priorRecoverySuperseded) {
            val wasBusy = busy
            busy = true
            val recorded = try { revokeNativeRestoration(request.session.nativeOwnerKey()) }
                finally { busy = wasBusy }
            if (!recorded) {
                departureStage = ProDepartureStage.PROMPT
                return
            }
            if (pendingDeparture?.token?.operationId != request.token.operationId ||
                !owns(request.token) || latestDocument != request.document) {
                cancelDeparture()
                notice = "The work changed. The pending action was canceled."
                return
            }
        }
        val (retained, retainedSession) = resolved(request)
        clearToolDrafts()
        installMetadata(retainedSession)
        if (prepared == null) {
            publishDocument(retained)
            onSession(workspaceSnapshot().copy(
                viewportAspect = retained.canvasAspectRatio,
                contentRevision = contentRevision, sourceRevision = sourceRevision
            ))
            cancelDeparture()
            when (val destination = request.destination) {
                ProDestination.Back -> onBack()
                is ProDestination.Switch -> onSwitch(destination.mode)
                else -> Unit
            }
            return
        }
        val nextReset = if (replacementOnly) retainedSession.resetDocument else preparedReset
        val nextTarget = if (replacementOnly) retainedSession.saveTarget
            else ProSaveTarget(nextWorkId++, project?.name, project)
        val before = ProHistoryEntry(retained, retainedSession.resetDocument,
            retainedSession.selectedLayer, retainedSession.saveTarget)
        val nextSelection = if (replacementOnly) preparedSelection ?: retainedSession.selectedLayer else 0
        val after = ProHistoryEntry(prepared, nextReset, nextSelection, nextTarget)
        history = retainedSession.history.checkpoint(before, after)
        saveTarget = nextTarget
        resetDocument = nextReset
        selected = nextSelection
        if (!replacementOnly) {
            documentEstablished = true
            finishedOutput = prepared.linkedFinishes.isNotEmpty()
            entryDocument = prepared
            baselines = baselines + (nextTarget.workId to ProWorkBaseline(
                prepared, prepared.takeIf { project != null }
            ))
            selected = 0
            viewport = ProViewport()
            resetGeneration++
            section = if (prepared.layers.isEmpty()) ProSection.CANVAS else ProSection.LAYERS
        }
        chrome = true
        expanded = true
        preview = null
        publishDocument(prepared)
        cancelDeparture()
        if (openedNamedProject != null) {
            onOpenedNamedProject?.invoke(openedNamedProject, workspaceSnapshot().copy(
                documentEstablished = true, viewportAspect = prepared.canvasAspectRatio
            ))
        }
        notice = when {
            replacementOnly -> "Source replaced. Layer edits preserved."
            project != null -> "Pro project opened"
            prepared.layers.isEmpty() -> "New canvas. Add a layer before saving."
            else -> "Photo opened as a new Pro document."
        }
    }

    // A tracked gesture still holds a history entry with the previous save target.
    val inlineSaveReady = inlineSaveAcknowledgement != null && dragStart == null && sliderStart == null
    SideEffect {
        val acknowledgement = inlineSaveAcknowledgement
        var settled = false
        if (acknowledgement != null) {
            val current = workspaceSnapshot()
            val ownsSave = current.nativeOwnerKey() == acknowledgement.ownerKey &&
                current.saveTarget.savedRef == acknowledgement.previousTarget
            if (ownsSave && inlineSaveReady) {
                val saved = current.saved(acknowledgement.capturedDocument, acknowledgement.saved)
                saveTarget = saved.saveTarget
                history = saved.history
                baselines = saved.baselines
                entryDocument = saved.entryDocument ?: entryDocument
            }
            settled = !ownsSave || inlineSaveReady
        }
        persistSession()
        if (settled && acknowledgement != null) onInlineSaveAcknowledged(acknowledgement)
    }
    DisposableEffect(ownerId) { onDispose { cancelGesture() } }
    BackHandler { requestBack() }

    val launchedAddOwner = addPickerOwner
    val picker = key(launchedAddOwner?.first?.operationId) { rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(ProMath.MAX_LAYERS)
    ) addResult@{ uris ->
        val owner = launchedAddOwner ?: return@addResult
        if (addPickerOwner?.first?.operationId != owner.first.operationId) return@addResult
        addPickerOwner = null
        if (!owns(owner.first) || latestDocument != owner.second || pendingDeparture != null) {
            notice = "The document changed. Images were not added."
            return@addResult
        }
        val remaining = ProMath.MAX_LAYERS - owner.second.layers.size
        if (uris.isNotEmpty() && remaining > 0) {
            scope.launch {
                busy = true
                try {
                    val layers = withContext(Dispatchers.IO) {
                        uris.take(remaining).mapIndexed { index, uri ->
                            try {
                                resolver.takePersistableUriPermission(
                                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            } catch (_: SecurityException) {}
                            ProImageGeometry.recordImport(ProLayer(
                                uri = uri.toString(), name = "Image " + (document.layers.size + index + 1)
                            ), ProImageGeometry.sourceSize(resolver, uri))
                        }
                    }
                    if (!owns(owner.first) || latestDocument != owner.second) {
                        notice = "The document changed. Images were not added."
                        return@launch
                    }
                    if (layers.isNotEmpty()) {
                        val original = resetDocument ?: document.copy(layers = listOf(layers.first()))
                        commit(document.copy(
                            layers = document.layers + layers,
                            sourceCanvasSetup = document.sourceCanvasSetup ?: ProCanvasSetup.from(document)
                        ), original, allowBusy = true)
                        selectLayer(document.layers.size + layers.lastIndex)
                        section = ProSection.LAYERS
                        showAdd = false
                    }
                    if (uris.size > remaining) notice = "Only the first 12 layers can be added."
                } catch (cancelled: CancellationException) { throw cancelled
                } catch (failure: Exception) {
                    notice = "Cannot open image: " + (failure.message ?: "permission missing")
                } finally { busy = false }
            }
        }
    } }
    fun openImagePicker() {
        if (!busy && !exporting && pendingDeparture == null && document.layers.size < ProMath.MAX_LAYERS) {
            if (cancelGesture() || hasUnresolvedToolDraft()) return
            addPickerOwner = operationToken() to document
        } else notice = "Maximum of 12 layers reached."
    }
    LaunchedEffect(addPickerOwner?.first?.operationId) {
        if (addPickerOwner != null) {
            try {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } catch (_: Exception) {
                addPickerOwner = null
                notice = "Could not open the image picker."
            }
        }
    }
    val launchedDeparture = pendingDeparture
    val departurePhotoPicker = key(launchedDeparture?.token?.operationId) { rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val request = launchedDeparture
        if (request != null && pendingDeparture?.token?.operationId == request.token.operationId &&
            departureStage == ProDepartureStage.PICKER) {
            if (uri == null) {
                cancelDeparture()
                notice = "Photo opening canceled. Current work kept."
            } else {
                departureStage = ProDepartureStage.LOADING
                busy = true
                scope.launch {
                    try {
                        val prepared = withContext(Dispatchers.IO) {
                            try {
                                resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            } catch (_: SecurityException) {}
                            val size = ProImageGeometry.sourceSize(resolver, uri)
                            val (retained, _) = resolved(request)
                            when (val action = request.destination) {
                                is ProDestination.ReplaceImage -> {
                                    val same = retained.layers.indexOfFirst { it === action.layer }
                                    val candidates = retained.layers.withIndex().filter {
                                        it.value.uri == action.layer.uri && it.value.name == action.layer.name &&
                                            it.value.kind == action.layer.kind
                                    }
                                    val index = when {
                                        retained == request.document -> action.index
                                        same >= 0 -> same
                                        candidates.size == 1 -> candidates.single().index
                                        else -> -1
                                    }
                                    require(index in retained.layers.indices &&
                                        retained.layers[index].kind == ProLayerKind.IMAGE) {
                                        "The selected layer changed. Current work is kept; choose the layer again."
                                    }
                                    val previousLayer = retained.layers[index]
                                    val layer = ProImageGeometry.recordImport(
                                        previousLayer.copy(uri = uri.toString()), size,
                                        placement = previousLayer.sourcePlacement)
                                    retained.copy(layers = retained.layers.toMutableList().also { it[index] = layer }) to index
                                }
                                ProDestination.OpenPhoto -> {
                                    val layer = ProImageGeometry.recordImport(
                                        ProLayer(uri.toString(), name = "Original photo"), size)
                                    val opened = ProDocument(layers = listOf(layer), background = ProBackground.TRANSPARENT,
                                        sourceAspectRatio = size.aspect, canvasSize = size)
                                    opened.copy(sourceCanvasSetup = ProCanvasSetup.from(opened)) to 0
                                }
                                else -> error("The photo request no longer owns this operation.")
                            }
                        }
                        finishDeparture(request, prepared.first, prepared.first,
                            replacementOnly = request.destination is ProDestination.ReplaceImage,
                            preparedSelection = prepared.second)
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: OutOfMemoryError) {
                        cancelDeparture()
                        notice = "Not enough memory to open this photo. Current work kept."
                    } catch (error: Exception) {
                        cancelDeparture()
                        notice = error.message ?: "Could not open the photo. Current work kept."
                    } finally { busy = false }
                }
            }
        }
    } }

    LaunchedEffect(pendingDeparture?.token?.operationId, departureStage) {
        val request = pendingDeparture ?: return@LaunchedEffect
        if (departureStage != ProDepartureStage.READY) return@LaunchedEffect
        if (!owns(request.token) || document != request.document) {
            cancelDeparture()
            notice = "The work changed. Choose the action again."
            return@LaunchedEffect
        }
        when (val action = request.destination) {
            ProDestination.Back, is ProDestination.Switch -> finishDeparture(request)
            ProDestination.NewCanvas -> {
                val blank = ProDocument()
                val established = blank.copy(sourceCanvasSetup = ProCanvasSetup.from(blank))
                finishDeparture(request, established, established)
            }
            ProDestination.OpenPhoto, is ProDestination.ReplaceImage -> {
                departureStage = ProDepartureStage.PICKER
                try {
                    departurePhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } catch (error: Exception) {
                    cancelDeparture()
                    notice = "Could not open the photo picker. Current work kept."
                }
            }
            is ProDestination.OpenProject -> {
                departureStage = ProDepartureStage.LOADING
                busy = true
                // A child job outlives this effect's READY -> LOADING recomposition.
                scope.launch {
                    try {
                        val loaded = withContext(Dispatchers.IO) {
                            ProStore.loadNamed(context, action.name)?.let {
                                NamedProject(it.project, ProImageGeometry.recover(resolver, it.content))
                            }
                        }
                        if (loaded == null) {
                            cancelDeparture()
                            openError = "Project unreadable or unavailable. Current work kept."
                            showProjects = true
                        } else if (pendingDeparture?.token != request.token || !owns(request.token) ||
                            latestDocument != request.document) {
                            cancelDeparture()
                            openError = "The work changed. Choose Open project again."
                            showProjects = true
                        } else if (prepareOpenedNamedProject?.invoke(loaded) == false) {
                            cancelDeparture()
                            openError = "Could not reopen the project's working history. Current work kept."
                            showProjects = true
                        } else {
                            finishDeparture(
                                request, loaded.content.document, loaded.content.resetDocument,
                                loaded.project, openedNamedProject = loaded,
                                priorRecoverySuperseded = loaded.content.linkedWork != null &&
                                    prepareOpenedNamedProject != null
                            )
                        }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: OutOfMemoryError) {
                        cancelDeparture()
                        openError = "Not enough memory to open this project. Current work kept."
                        showProjects = true
                    } catch (error: Exception) {
                        cancelDeparture()
                        openError = "Could not open this project. Current work kept."
                        showProjects = true
                    } finally { busy = false }
                }
            }
        }
    }

    val exportPixelBudget = remember(context) {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        GeometryMath.safeExportPixels(manager.memoryClass)
    }
    val intendedExportSize = ProMath.intendedCanvasSize(document)
    val safeExportSize = intendedExportSize.within(ProMath.MAX_EXPORT_EDGE, exportPixelBudget)
    var exportOutputSize by remember(document, safeExportSize) { mutableStateOf<ProPixelSize?>(null) }
    LaunchedEffect(showExport, document, safeExportSize) {
        if (showExport && document.linkedFinishes.isNotEmpty()) {
            exportOutputSize = withContext(Dispatchers.IO) {
                runCatching { ProRaster.outputSize(context, document, safeExportSize) }.getOrNull()
            }
        }
    }

    suspend fun retainExportForRetry(pending: PendingExport, message: String) {
        notice = message
        exportFailure = message
        pendingExport = withContext(Dispatchers.IO) {
            runCatching { exportJournal.failed(pending) }.getOrElse {
                runCatching { exportJournal.read(ExportWorkspace.PRO) }.getOrNull()
            }
        }
        showExportRecovery = pendingExport != null
    }

    suspend fun export(destination: Uri, request: PendingProExportRecipe, pending: PendingExport) {
        val protectedLayers = request.document.layers + request.resetDocument.layers
        if (protectedLayers.any { it.uri.isNotEmpty() && it.uri == destination.toString() }) {
            retainExportForRetry(pending, "Export cannot overwrite an original image.")
            return
        }
        showExportRecovery = false
        notice = "Exporting editable composition…"
        val dimensions = withContext(Dispatchers.Default) {
            val coroutine = currentCoroutineContext()
            val image = ProRaster.renderOutput(context, request.document, request.size, preview = false) {
                coroutine.ensureActive()
            }
            try {
                val sized = ExportSizing.layout(image, request.settings)
                try {
                    withContext(Dispatchers.IO) {
                        writeStagedImage(context.cacheDir,
                            openDestination = {
                                exportJournal.copying(pending)
                                resolver.openOutputStream(destination, "wt")
                            },
                            checkActive = { ensureActive() }) { out ->
                            ImageExportWriter.write(sized, request.settings, out)
                        }
                    }
                    "${sized.width} × ${sized.height}"
                } finally {
                    if (sized !== image) sized.recycle()
                }
            } finally {
                image.recycle()
            }
        }
        withContext(Dispatchers.IO) { exportJournal.discard(pending) }
        pendingExport = null
        notice = "Exported $dimensions. Original media unchanged."
    }

    LaunchedEffect(exportJournal) {
        val restored = withContext(Dispatchers.IO) {
            runCatching { exportJournal.read(ExportWorkspace.PRO) }.getOrNull()
        }
        pendingExport = restored
        showExportRecovery = restored != null
        exportJournalReady = true
    }

    RecoverableExportPicker(
        pending = pendingExport,
        launchId = exportLaunchId,
        onLaunch = { if (exportLaunchId == it) exportLaunchId = null },
        onLaunchFailed = { pending, error ->
            scope.launch { retainExportForRetry(pending, error.message ?: "Could not open the export destination.") }
        },
        onResult = { pending, response ->
            if (pendingExport?.id == pending.id && !exporting) {
                exporting = true
                scope.launch {
                    try {
                        if (response.resultCode != Activity.RESULT_OK) {
                            if (withContext(Dispatchers.IO) { exportJournal.cancelPicker(pending) }) {
                                pendingExport = null
                                showExportRecovery = false
                                notice = "Export canceled."
                            }
                        } else {
                            val destination = response.data?.data
                            val request = withContext(Dispatchers.IO) { PendingProExportRecipe.decode(pending) }
                            if (destination == null || request == null) {
                                if (request == null) exportRecoveryUnavailable =
                                    "Discard this unreadable request to start a new Pro export."
                                retainExportForRetry(pending, "The export destination or Pro edits could not be restored.")
                            } else {
                                val claimed = withContext(Dispatchers.IO) { exportJournal.claim(pending, destination) }
                                if (claimed != null) {
                                    pendingExport = claimed
                                    export(destination, request, claimed)
                                }
                            }
                        }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: OutOfMemoryError) {
                        retainExportForRetry(pending, "Not enough device memory to export this composition.")
                    } catch (error: Exception) {
                        retainExportForRetry(pending, "Export failed: " + (error.message ?: "source unavailable"))
                    } finally {
                        exporting = false
                    }
                }
            }
        }
    )

    fun startExport() {
        if (busy || exporting || !exportJournalReady || !ProMath.valid(document)) return
        showExport = false
        if (pendingExport != null) {
            showExportRecovery = true
            return
        }
        exportFailure = null
        exportRecoveryUnavailable = null
        val request = PendingProExportRecipe(document, resetDocument ?: document, exportSettings, safeExportSize)
        val format = request.settings.format
        val nativeExportOwner = workspaceSnapshot().nativeOwnerKey()
        busy = true
        scope.launch {
            try {
                val pending = withContext(Dispatchers.IO) {
                    exportJournal.begin(ExportWorkspace.PRO, format.mime,
                        "GZEditorPro_Pro." + format.extension, request.encode(), nativeExportOwner)
                }
                pendingExport = pending
                exportLaunchId = pending.id
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: OutOfMemoryError) {
                notice = "Not enough device memory to keep this export request."
            } catch (error: Exception) {
                notice = "Could not keep the Pro export request. " +
                    (error.message ?: "Try reducing the complexity of the project.")
            } finally {
                busy = false
            }
        }
    }

    fun resolveExportRecovery(retry: Boolean) {
        val pending = pendingExport ?: return
        if (busy || exporting) return
        busy = true
        scope.launch {
            try {
                if (retry) {
                    val next = withContext(Dispatchers.IO) { exportJournal.retry(pending) }
                    pendingExport = next
                    exportLaunchId = next.id
                } else {
                    withContext(Dispatchers.IO) { exportJournal.discard(pending) }
                    pendingExport = null
                }
                exportFailure = null
                exportRecoveryUnavailable = null
                showExportRecovery = false
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                notice = error.message ?: "Could not resolve the pending Pro export."
            } finally {
                busy = false
            }
        }
    }

    // A hardware display list can retain the previous preview after recomposition.
    // Displayed bitmaps are framework/GC owned; only unpublished scratch images are recycled.
    var previewEdge by remember { mutableIntStateOf(940) }
    val memoryClass = remember(context) {
        (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).memoryClass
    }
    val maxPreviewPixels = when {
        memoryClass >= 256 -> 3_000_000L
        memoryClass >= 128 -> 2_000_000L
        else -> 1_200_000L
    }
    LaunchedEffect(memoryClass, document.canvasAspectRatio, resetGeneration) {
        snapshotFlow { viewport.zoom }.collectLatest { zoom ->
            delay(220)
            previewEdge = when {
                zoom >= 2.5f && memoryClass >= 256 -> 2048
                zoom >= 1.5f && memoryClass >= 128 -> 1600
                zoom >= 1.5f -> 1200
                else -> 940
            }
        }
    }
    val pristine = remember(document) {
        document.layers.none { layer ->
            layer.visible && layer.opacity > 0f && when (layer.kind) {
                ProLayerKind.DRAWING -> layer.edits.creative.strokes.isNotEmpty()
                ProLayerKind.ADJUSTMENT -> layer.effect != ProPixelEffect.NONE || layer.edits.grade != Grade()
                else -> true
            }
        }
    }
    val artboardSize = remember(document.canvasAspectRatio, previewEdge, maxPreviewPixels) {
        ProMath.dimensions(document, previewEdge, maxPreviewPixels)
    }
    fun reportMissingSource(missing: ProRaster.MissingLayer) {
        // A failed layer must not inherit another layer's selection or pending tool.
        clearToolDrafts()
        preview = null
        notice = "Source for image layer " + (missing.position + 1) + " is unavailable."
    }
    LaunchedEffect(document, artboardSize, resolver, resetGeneration, showingFinishedOutput) {
        if (pristine && !showingFinishedOutput) {
            preview = null
            rendering = false
            if (document.layers.isEmpty()) chrome = true
            return@LaunchedEffect
        }
        rendering = true
        try {
            val (w, h) = artboardSize
            val bitmap = withContext(Dispatchers.Default) {
                val job = currentCoroutineContext()
                val rendered = if (showingFinishedOutput) {
                    ProRaster.renderOutput(context, document, ProPixelSize(w, h), preview = true) { job.ensureActive() }
                } else {
                    ProRaster.render(resolver, document, w, h, true) { job.ensureActive() }
                }
                try { job.ensureActive(); rendered }
                catch (cancelled: CancellationException) {
                    if (!rendered.isRecycled) rendered.recycle()
                    throw cancelled
                }
            }
            preview = bitmap
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (missing: ProRaster.MissingLayer) {
            reportMissingSource(missing)
        } catch (_: OutOfMemoryError) {
            notice = "Preview has too many pixels for available memory."
        } catch (failure: Exception) {
            notice = "Preview failed: " + (failure.message ?: "unknown problem")
        } finally { rendering = false }
    }
    val brushLayer = selected.takeIf {
        !showingFinishedOutput && section == ProSection.PAINT && active?.kind == ProLayerKind.DRAWING &&
            active.visible && active.opacity > 0f
    }
    val brushSizePixels = remember(document.canvasAspectRatio, maxPreviewPixels) {
        ProMath.dimensions(document, 640, minOf(maxPreviewPixels, 409_600L))
    }
    LaunchedEffect(document, brushLayer, brushSizePixels, resolver, resetGeneration) {
        if (brushLayer == null) return@LaunchedEffect
        val (width, height) = brushSizePixels
        val renderer = ProRaster.BrushPreview(resolver, document, brushLayer, width, height)
        fun ownsDraft(draft: ProBrushDraft): Boolean =
            activeStroke?.id == draft.id && draft.layer == brushLayer && selected == brushLayer &&
                section == ProSection.PAINT && latestDocument == document
        try {
            // Let the current frame finish when the pointer moves again. A long drag
            // still shows frames continuously and the next render uses the newest sample.
            snapshotFlow { activeStroke }.conflate().collect { draft ->
                if (draft == null || draft.stroke.points.size < 2 || !ownsDraft(draft)) return@collect
                try {
                    val bitmap = withContext(Dispatchers.Default) {
                        val job = currentCoroutineContext()
                        val image = renderer.render(draft.stroke) { job.ensureActive() }
                        try {
                            job.ensureActive()
                            image
                        } catch (cancelled: CancellationException) {
                            image.recycle()
                            throw cancelled
                        }
                    }
                    if (ownsDraft(draft)) {
                        brushFrame = ProBrushFrame(draft.id, bitmap)
                        // Avoid allocating a full preview for every high-rate touch sample.
                        delay(16L)
                    } else bitmap.recycle()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (missing: ProRaster.MissingLayer) {
                    if (ownsDraft(draft)) reportMissingSource(missing)
                } catch (_: OutOfMemoryError) {
                    if (ownsDraft(draft)) {
                        clearToolDrafts()
                        notice = "Preview has too many pixels for available memory."
                    }
                } catch (failure: Exception) {
                    if (ownsDraft(draft)) {
                        clearToolDrafts()
                        notice = "Preview failed: " + (failure.message ?: "unknown problem")
                    }
                }
            }
        } finally { renderer.close() }
    }
    LaunchedEffect(notice) {
        if (notice.isNotBlank()) {
            delay(5000L); notice = ""
        }
    }

    val available = canEditDocument()
    val hasUnsavedWork = linkedDraftDirty || workspaceSnapshot().isDirty(document)
    val hasWork = documentEstablished || resetDocument != null || document != ProDocument() ||
        history.undo.isNotEmpty() || history.redo.isNotEmpty()
    val operationUnavailable = "Finish the current operation first."
    // The original BoxWithConstraints used a SubcomposeLayout for the entire
    // Pro workspace. Its disposable nested slot table could fail during Android
    // activity teardown after rapid layer/brush changes. The workspace needs
    // no subcomposition: a normal Box and configuration-aware panel bound
    // preserve usable canvas space without a nested composition lifecycle.
    val panelHeight = LocalConfiguration.current.screenHeightDp.dp * .39f
    Box(Modifier.fillMaxSize().background(Color(0xFF080B10))) {
        val safeArea = if (chrome) Modifier.windowInsetsPadding(WindowInsets.safeDrawing) else Modifier
        Column(Modifier.fillMaxSize().then(safeArea)) {
            if (chrome) {
                EditorWorkspaceHeader(
                    workspace = WorkspaceMode.PRO,
                    commands = listOf(
                        WorkspaceCommand(WorkspaceCommandId.OPEN_MEDIA, "New / Open media…", available,
                            if (!available) operationUnavailable else null) { },
                        WorkspaceCommand(WorkspaceCommandId.OPEN_PROJECT, "Open project…", available,
                            if (!available) operationUnavailable else null, onInvoke = ::openProjects),
                        WorkspaceCommand(WorkspaceCommandId.SAVE, "Save project",
                            available && ProMath.valid(document) &&
                                (saveTarget.savedRef == null || hasUnsavedWork),
                            when {
                                !ProMath.valid(document) -> "Add a layer before saving this setup."
                                saveTarget.savedRef != null && !hasUnsavedWork -> "Already saved."
                                !available -> "Finish the current operation first."
                                else -> null
                            }) { startSave() },
                        WorkspaceCommand(WorkspaceCommandId.SAVE_AS, "Save project as…",
                            available && ProMath.valid(document),
                            if (!ProMath.valid(document)) "Add a layer before saving this setup." else null) {
                            startSave(asCopy = true)
                        },
                        WorkspaceCommand(WorkspaceCommandId.EXPORT, "Export image…",
                            available && exportJournalReady && (ProMath.valid(document) || pendingExport != null),
                            if (!ProMath.valid(document) && pendingExport == null) "Add a layer before exporting." else null) {
                            if (!cancelGesture() && !hasUnresolvedToolDraft()) {
                                showExportRecovery = pendingExport != null
                                showExport = !showExportRecovery
                            }
                        },
                        WorkspaceCommand(WorkspaceCommandId.RESET, "Reset all edits", available && hasWork,
                            if (!hasWork) "There is no Pro work to reset." else if (!available) operationUnavailable else null,
                            onInvoke = ::resetAllEdits),
                        WorkspaceCommand(WorkspaceCommandId.DELETE, "Delete", available && hasWork,
                            if (!hasWork) "There is no Pro work to delete." else if (!available) operationUnavailable else null) {
                            deleteError = null
                            confirmDelete = true
                        },
                        WorkspaceCommand(WorkspaceCommandId.HELP, "Help", available) { showHelp = true }
                    ),
                    mediaChoices = listOf(
                        WorkspaceCommand(WorkspaceCommandId.NEW, "New canvas…", available) {
                            requestDeparture(ProDestination.NewCanvas)
                        },
                        WorkspaceCommand(WorkspaceCommandId.OPEN_MEDIA, "Open photo as new document…", available) {
                            requestDeparture(ProDestination.OpenPhoto)
                        }
                    ),
                    canUndo = available && history.undo.isNotEmpty(),
                    canRedo = available && history.redo.isNotEmpty(),
                    onUndo = { restoreHistory(undo = true) },
                    onRedo = { restoreHistory(undo = false) },
                    onChooseWorkspace = ::requestSwitch,
                    menuExpanded = showFile, onMenuExpandedChange = { showFile = it },
                    enabled = available, onBack = ::requestBack
                )
                val status = notice.ifBlank { if (linkedDraftDirty) "Unsaved linked work" else "" }
                if (status.isNotBlank()) Text(status, color = EditorMint, fontSize = 12.sp,
                    maxLines = 2, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp))
            }
            Box(Modifier.weight(1f).fillMaxWidth().clipToBounds(), contentAlignment = Alignment.Center) {
                if (document.layers.isEmpty() && !hasWork) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 44.dp)
                            .verticalScroll(rememberScrollState())) {
                        Text("Open a photo or create a canvas from Options.",
                            color = EditorWhite, fontSize = 16.sp)
                    }
                    Text("© 2026 Greenezone Software LLC", color = EditorMuted, fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.BottomStart).padding(12.dp))
                } else {
                    val bitmap = if (pristine && !showingFinishedOutput) null else preview
                    val live = brushFrame?.takeIf {
                        it.draftId == activeStroke?.id && selected == brushLayer &&
                            dragStart?.document == document
                    }?.bitmap
                    val (canvasW, canvasH) = artboardSize
                    val canEdit = available && !showingFinishedOutput && (pristine || bitmap != null ||
                        document.layers.none { it.kind == ProLayerKind.IMAGE && it.visible })
                    val displaySize = if (showingFinishedOutput && bitmap != null) {
                        bitmap.width to bitmap.height
                    } else artboardSize
                    ProCanvasSurface(live ?: bitmap, displaySize, document.background.argb,
                        section, active.takeUnless { showingFinishedOutput }, viewport, chrome, canEdit,
                        draftSelection.takeUnless { showingFinishedOutput },
                        retouchSource.takeUnless { showingFinishedOutput },
                        actions = ProCanvasActions(
                            toggleControls = { chrome = !chrome },
                            hold = { point ->
                                if (section == ProSection.LAYERS) {
                                    ProHitMath.pick(document, point.x, point.y, canvasW, canvasH)
                                        ?.let(::selectLayer)
                                } else if (section == ProSection.PAINT && active?.kind == ProLayerKind.DRAWING &&
                                    active.visible && active.opacity > 0f && !eraserMode) {
                                    if (pristine || bitmap != null) {
                                        brushColor = (bitmap?.let {
                                            val x = (point.x * it.width).toInt().coerceIn(0, it.width - 1)
                                            val y = (point.y * it.height).toInt().coerceIn(0, it.height - 1)
                                            it.getPixel(x, y)
                                        } ?: document.background.argb) or -0x1000000
                                        notice = "Brush color sampled from canvas."
                                    }
                                } else if (section == ProSection.RETOUCH && active?.kind == ProLayerKind.IMAGE &&
                                    active.visible && active.opacity > 0f) {
                                    val target = ProMath.canvasToImageNormalized(active, canvasW, canvasH, point)
                                    val source = retouchSourceImage
                                    if (target == null) notice = "Press inside the selected image."
                                    else if (source == null) {
                                        retouchSource = point
                                        retouchSourceImage = target
                                        notice = "Source set. Press and hold a destination to stamp."
                                    } else if (active.retouches.size < ProToolMath.MAX_RETOUCH) {
                                        val mark = ProRetouchMark(retouchKind, source.x, source.y,
                                            target.x, target.y, retouchRadius, retouchStrength)
                                        changeLayer { it.copy(retouches = it.retouches + mark) }
                                    } else notice = "This image already has 80 retouch marks."
                                }
                            },
                            dragStart = { point ->
                                lastDragValue = null
                                dragStart = historyEntry()
                                if (section == ProSection.PAINT && active?.kind == ProLayerKind.DRAWING &&
                                    active.visible && active.opacity > 0f) {
                                    if (active.edits.creative.strokes.size >= CreativeRaster.MAX_INK) {
                                        notice = "This brush layer is full. Add another brush layer or undo a stroke."
                                    } else {
                                        nextBrushId += 1
                                        activeStroke = ProBrushDraft(nextBrushId, selected,
                                            InkStroke(listOf(point), color = brushColor,
                                                thickness = brushSize, opacity = brushOpacity, erase = eraserMode))
                                    }
                                }
                                if (section == ProSection.SELECT && active?.visible == true && active.opacity > 0f &&
                                    (active.kind == ProLayerKind.IMAGE || active.kind == ProLayerKind.ADJUSTMENT)) {
                                    selectionBeforeDrag = draftSelection
                                    selectionStart = point
                                    draftSelection = if (selectionKind == ProSelectionKind.LASSO)
                                        ProSelection(selectionKind, points = listOf(point))
                                    else ProToolMath.rectangle(point, point, selectionKind)
                                }
                            },
                            drag = { start, point ->
                                val before = dragStart?.document
                                val item = before?.layers?.getOrNull(selected)
                                if (before != null && item != null) when (section) {
                                    ProSection.MOVE -> if (item.visible && item.opacity > 0f && item.kind in listOf(
                                        ProLayerKind.IMAGE, ProLayerKind.TEXT, ProLayerKind.SHAPE)) {
                                        val moved = item.copy(x = (item.x + point.x - start.x).coerceIn(0f, 1f),
                                            y = (item.y + point.y - start.y).coerceIn(0f, 1f))
                                        val next = before.copy(layers = before.layers.toMutableList().also {
                                            it[selected] = moved
                                        })
                                        lastDragValue = next
                                        publishDocument(next)
                                    }
                                    ProSection.PAINT -> activeStroke?.let { draft ->
                                        val stroke = draft.stroke
                                        if (draft.layer == selected && stroke.points.last() != point) {
                                            val points = ProStrokeSampling.append(stroke.points, point, before.canvasAspectRatio)
                                            activeStroke = draft.copy(stroke = stroke.copy(points = points))
                                        }
                                    }
                                    ProSection.SELECT -> if (selectionStart != null) {
                                        draftSelection = if (selectionKind == ProSelectionKind.LASSO) {
                                            val points = draftSelection?.points.orEmpty()
                                            ProSelection(selectionKind, points = ProStrokeSampling.append(
                                                points, point, before.canvasAspectRatio, ProToolMath.MAX_LASSO_POINTS))
                                        } else ProToolMath.rectangle(start, point, selectionKind)
                                    }
                                    else -> Unit
                                }
                            },
                            dragEnd = { cancel ->
                                val before = dragStart
                                if (cancel) {
                                    if (section == ProSection.MOVE && before != null) publishDocument(before.document)
                                    if (selectionStart != null) draftSelection = selectionBeforeDrag
                                } else if (before != null && section == ProSection.MOVE) {
                                    history = history.checkpoint(before, historyEntry(lastDragValue ?: before.document))
                                } else if (section == ProSection.PAINT && before?.document == document) {
                                    activeStroke?.let { draft ->
                                        if (draft.layer == selected && draft.stroke.points.size > 1) {
                                            // Keep the last real draft pixels visible until the
                                            // committed preview at full resolution is ready.
                                            brushFrame?.takeIf { it.draftId == draft.id }
                                                ?.let { preview = it.bitmap }
                                            changeLayer { it.withBrushStroke(draft.stroke) }
                                        }
                                    }
                                }
                                lastDragValue = null
                                dragStart = null
                                activeStroke = null
                                brushFrame = null
                                selectionStart = null
                                selectionBeforeDrag = null
                            },
                            changeView = { viewport = it }
                        ), modifier = Modifier.fillMaxSize())
                }
            }
            if (chrome) {
                if (hasWork && expanded) {
                    Column(Modifier.fillMaxWidth().heightIn(max = panelHeight)
                        .background(EditorSurface).verticalScroll(rememberScrollState())
                        .padding(bottom = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (active != null) Text(active.name + " · " + active.kind.label,
                            color = EditorMuted, fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 13.dp))
                        if (section == ProSection.LAYERS || active == null) {
                            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { showAdd = true },
                                    enabled = available && document.layers.size < ProMath.MAX_LAYERS) {
                                    Text("+ Add layer", color = EditorMint)
                                }
                                if (document.layers.isNotEmpty()) {
                                    TextButton(onClick = { showStack = true }, enabled = available) {
                                        Text("Stack (" + document.layers.size + ")")
                                    }
                                    TextButton(onClick = { viewport = ProViewport() }, enabled = available) {
                                        Text("Fit canvas")
                                    }
                                    if (document.linkedFinishes.isNotEmpty()) {
                                        TextButton(enabled = available, onClick = {
                                            if (!cancelGesture() && !hasUnresolvedToolDraft()) {
                                                finishedOutput = !showingFinishedOutput
                                                viewport = ProViewport()
                                            }
                                        }) {
                                            Text(if (showingFinishedOutput) "Edit native layers" else "Preview finished output")
                                        }
                                    }
                                }
                            }
                        }
                        if (section == ProSection.CANVAS || active == null) {
                            ProCanvasProperties(document, available) { updated ->
                                if (document.linkedFinishes.isNotEmpty() &&
                                    updated.canvasAspectRatio != document.canvasAspectRatio) {
                                    notice = "Canvas proportions are kept with linked Regular edits."
                                    return@ProCanvasProperties
                                }
                                val creating = document.layers.isEmpty() &&
                                    (history.undo + history.redo).none { it.document.layers.isNotEmpty() }
                                commit(if (creating) updated.copy(sourceCanvasSetup = ProCanvasSetup.from(updated))
                                    else updated)
                            }
                        }
                        if (active != null) when (section) {
                            ProSection.LAYERS -> {
                                Text("Tap the canvas to hide controls. Hold an object to select its layer.",
                                    color = EditorMuted, fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp))
                                Row(Modifier.horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    TextButton(onClick = { rename = active.name }) { Text("Rename") }
                                    if (active.kind == ProLayerKind.IMAGE) {
                                        TextButton(enabled = available, onClick = {
                                            selectTool(ProSection.SELECT)
                                        }) { Text("Select / mask", color = EditorMint) }
                                        TextButton(enabled = available, onClick = {
                                            selectTool(ProSection.RETOUCH)
                                        }) { Text("Clone / repair", color = EditorMint) }
                                        TextButton(enabled = available, onClick = {
                                            requestDeparture(ProDestination.ReplaceImage(selected, active))
                                        }) { Text("Replace image", color = EditorWhite) }
                                    }
                                    TextButton(enabled = available, onClick = { toggleLayerVisibility(selected) }) {
                                        Text(if (active.visible) "Hide" else "Show", color = EditorWhite)
                                    }
                                    TextButton(enabled = available && selected > 0,
                                        onClick = { moveSelectedLayer(-1) }) {
                                        Text("↓ Behind", color = EditorWhite)
                                    }
                                    TextButton(enabled = available && selected < document.layers.lastIndex,
                                        onClick = { moveSelectedLayer(1) }) {
                                        Text("↑ Above", color = EditorWhite)
                                    }
                                    if (active.kind != ProLayerKind.TEXT) TextButton(
                                        enabled = available && document.layers.size < ProMath.MAX_LAYERS,
                                        onClick = ::duplicateSelectedLayer) {
                                        Text("Duplicate", color = EditorWhite)
                                    }
                                    TextButton(enabled = available, onClick = ::deleteSelectedLayer) {
                                        Text("Remove layer", color = Color(0xFFFF9D9D))
                                    }
                                }
                            }
                            ProSection.MOVE -> {
                                if (active.kind == ProLayerKind.DRAWING ||
                                    active.kind == ProLayerKind.ADJUSTMENT) {
                                    Text("Use Transform on image or shape layers. Paint directly on a paint layer.",
                                        color = EditorMuted, fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp))
                                } else if (!active.visible) {
                                    Text("Show this layer in Layers before transforming it.",
                                        color = EditorMuted, fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp))
                                } else {
                                    if (active.opacity == 0f) {
                                        Text("Raise opacity above zero to see this layer move.",
                                            color = EditorMuted, fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 14.dp))
                                    }
                                    Row(Modifier.horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        ProTransform.entries.forEach { option ->
                                            DockChip(option.title, transform == option,
                                                enabled = active.opacity > 0f || option == ProTransform.OPACITY) {
                                                transform = option
                                            }
                                        }
                                        DockChip("Center on canvas", false, enabled = active.opacity > 0f) {
                                            val already = active.x == .5f && active.y == .5f
                                            changeLayer { it.copy(x = .5f, y = .5f) }
                                            notice = if (already) "Layer already centered."
                                                else "Layer centered on canvas."
                                        }
                                    }
                                    val value = when (transform) {
                                        ProTransform.SIZE -> active.scale
                                        ProTransform.OPACITY -> active.opacity
                                        ProTransform.HORIZONTAL -> active.x
                                        ProTransform.VERTICAL -> active.y
                                        ProTransform.ROTATE -> active.rotationDegrees
                                    }
                                    val range = when (transform) {
                                        ProTransform.SIZE -> .1f..2f
                                        ProTransform.ROTATE -> -180f..180f
                                        else -> 0f..1f
                                    }
                                    Slider(value = value.coerceIn(range.start, range.endInclusive),
                                        enabled = active.opacity > 0f || transform == ProTransform.OPACITY,
                                        valueRange = range, onValueChange = { x ->
                                            changeLayer({ layer -> when (transform) {
                                                ProTransform.SIZE -> layer.copy(scale = x)
                                                ProTransform.OPACITY -> layer.copy(opacity = x)
                                                ProTransform.HORIZONTAL -> layer.copy(x = x)
                                                ProTransform.VERTICAL -> layer.copy(y = x)
                                                ProTransform.ROTATE -> layer.copy(rotationDegrees = x)
                                            } }, sliding = true)
                                        }, onValueChangeFinished = ::finishSlider,
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
                                }
                            }
                            ProSection.STYLE -> {
                                val shape = active.edits.creative.shapes.firstOrNull()
                                    ?.takeIf { active.kind == ProLayerKind.SHAPE }
                                fun editShape(update: (ShapeOverlay) -> ShapeOverlay) {
                                    if (shape == null) return
                                    changeLayer { item -> item.copy(edits = item.edits.copy(
                                        creative = item.edits.creative.copy(shapes = listOf(update(shape)))
                                    )) }
                                }
                                if (shape != null) {
                                    Row(Modifier.horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        ShapeKind.entries.forEach { kind ->
                                            DockChip(kind.label, shape.kind == kind) {
                                                editShape { it.copy(kind = kind) }
                                            }
                                        }
                                        DockChip("Filled", shape.filled) {
                                            editShape { it.copy(filled = !it.filled) }
                                        }
                                    }
                                    ProColorPalette(shape.color) { color -> editShape { it.copy(color = color) } }
                                } else {
                                    TextButton(enabled = available && document.layers.size < ProMath.MAX_LAYERS,
                                        onClick = { addCreative(ProLayerKind.SHAPE) }) {
                                        Text("Add shape layer", color = EditorMint)
                                    }
                                }
                            }
                            ProSection.PAINT -> {
                                if (active.kind == ProLayerKind.DRAWING && (!active.visible || active.opacity <= 0f)) {
                                    Text("Show this paint layer and raise its opacity to paint or erase.",
                                        color = EditorMuted, fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 13.dp))
                                } else if (active.kind == ProLayerKind.DRAWING) {
                                    Row(Modifier.horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 9.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        DockChip("Paint", !eraserMode) { eraserMode = false }
                                        DockChip("Eraser", eraserMode) { eraserMode = true }
                                    }
                                    Text(if (eraserMode)
                                        "Erase pixels from " + active.name + "."
                                        else "Paint on " + active.name + ". Hold a canvas color to sample it.",
                                        color = EditorMuted, fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 13.dp))
                                    if (!eraserMode) ProColorPalette(brushColor) { brushColor = it }
                                    SingleAdjustment("Brush size", brushSize * 100, .3f, 8f, true,
                                        onReset = { brushSize = .012f }, onChange = { brushSize = it / 100 },
                                        onFinished = {}, valueSuffix = "%")
                                    if (!eraserMode) SingleAdjustment("Brush opacity", brushOpacity * 100, 1f, 100f, true,
                                        onReset = { brushOpacity = 1f }, onChange = { brushOpacity = it / 100 },
                                        onFinished = {}, valueSuffix = "%")
                                    Row(Modifier.horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 9.dp)) {
                                        DockChip("Undo brush stroke", false,
                                            enabled = active.edits.creative.strokes.isNotEmpty()) {
                                            changeLayer { item -> item.copy(edits = item.edits.copy(
                                                creative = item.edits.creative.copy(strokes =
                                                    item.edits.creative.strokes.dropLast(1)))) }
                                        }
                                        DockChip("Clear brush", false,
                                            enabled = active.edits.creative.strokes.isNotEmpty()) {
                                            changeLayer { item -> item.copy(edits = item.edits.copy(
                                                creative = item.edits.creative.copy(strokes = emptyList()))) }
                                        }
                                    }
                                } else TextButton(
                                    enabled = available && document.layers.size < ProMath.MAX_LAYERS,
                                    onClick = { addCreative(ProLayerKind.DRAWING) }
                                ) { Text("Add paint layer", color = EditorMint) }
                            }
                            ProSection.SELECT -> ProSelectionPanel(
                                layer = active,
                                selectedKind = selectionKind,
                                onKind = {
                                    selectionKind = it
                                    draftSelection = null
                                    selectionStart = null
                                    selectionBeforeDrag = null
                                },
                                draft = draftSelection,
                                onApply = {
                                    val selectedMask = draftSelection
                                    if (selectedMask != null && ProToolMath.valid(selectedMask)) {
                                        changeLayer { layer -> layer.copy(mask = ProToolMath.applySelection(
                                            selectedMask, layer, document.canvasAspectRatio)) }
                                        draftSelection = null
                                        notice = "Layer mask applied. Your image remains editable."
                                    }
                                },
                                onInvert = {
                                    changeLayer { layer -> layer.copy(mask =
                                        layer.mask?.copy(inverted = !layer.mask.inverted)) }
                                    notice = "Mask inverted."
                                },
                                onRemove = {
                                    changeLayer { it.copy(mask = null) }
                                    notice = "Mask removed."
                                },
                                onDiscard = { draftSelection = null },
                                onFeather = { value -> changeLayer({ layer ->
                                    layer.copy(mask = layer.mask?.copy(feather = value))
                                }, true) },
                                onResetFeather = { changeLayer { it.copy(mask = it.mask?.copy(feather = 0f)) } },
                                onFinish = ::finishSlider
                            )
                            ProSection.RETOUCH -> ProRetouchPanel(
                                layer = active,
                                mode = retouchKind,
                                onMode = {
                                    retouchKind = it
                                    retouchSource = null
                                    retouchSourceImage = null
                                },
                                radius = retouchRadius,
                                onRadius = { retouchRadius = it },
                                strength = retouchStrength,
                                onStrength = { retouchStrength = it },
                                sourceChosen = retouchSource != null,
                                onClearSource = { retouchSource = null; retouchSourceImage = null },
                                onUndoMark = {
                                    changeLayer { it.copy(retouches = it.retouches.dropLast(1)) }
                                },
                                onClearMarks = {
                                    changeLayer { it.copy(retouches = emptyList()) }
                                }
                            )
                            ProSection.PIXEL -> ProPixelEffectPanel(
                                layer = active,
                                canAdd = document.layers.size < ProMath.MAX_LAYERS,
                                enabled = available,
                                onAdd = {
                                    addCreative(ProLayerKind.ADJUSTMENT)
                                    selectTool(ProSection.PIXEL)
                                },
                                onEffect = { effect ->
                                    changeLayer { it.copy(effect = effect) }
                                },
                                onAmount = { amount ->
                                    changeLayer({ it.copy(effectAmount = amount) }, true)
                                },
                                onAmountDone = ::finishSlider
                            )
                            ProSection.GRADE -> ProGradePanel(
                                layer = active,
                                enabled = available,
                                canAdd = document.layers.size < ProMath.MAX_LAYERS,
                                onAdd = { addCreative(ProLayerKind.ADJUSTMENT) },
                                onGrade = { grade ->
                                    if (latestDocument.layers.getOrNull(selected) === active) {
                                        changeLayer({ it.copy(edits = it.edits.copy(grade = grade)) }, true)
                                    }
                                },
                                onCommitGrade = { grade ->
                                    if (latestDocument.layers.getOrNull(selected) === active) {
                                        changeLayer { it.copy(edits = it.edits.copy(grade = grade)) }
                                    }
                                },
                                onFinish = ::finishSlider
                            )
                            ProSection.BLEND -> {
                                val layersBelow = document.layers.take(selected).filter { layer ->
                                    layer.visible && layer.opacity > 0f && when (layer.kind) {
                                        ProLayerKind.DRAWING -> layer.edits.creative.strokes.isNotEmpty()
                                        ProLayerKind.ADJUSTMENT ->
                                            layer.effect != ProPixelEffect.NONE || layer.edits.grade != Grade()
                                        else -> true
                                    }
                                }
                                val below = layersBelow.lastOrNull()
                                val emptyBackdrop = document.background == ProBackground.TRANSPARENT &&
                                    layersBelow.none { it.kind != ProLayerKind.ADJUSTMENT }
                                val backdrop = if (below == null || emptyBackdrop) {
                                    "Backdrop: " + document.background.label + " canvas"
                                } else {
                                    "Layer below: " + below.name + " · " + document.background.label + " canvas"
                                }
                                val guidance = when {
                                    !active.visible -> "Show this layer to preview its blend and opacity."
                                    active.opacity <= 0f -> "Raise this layer's opacity to preview blending."
                                    emptyBackdrop && active.kind == ProLayerKind.ADJUSTMENT ->
                                        "Add visible content below this adjustment or choose an opaque canvas."
                                    emptyBackdrop ->
                                        "Add a visible layer below to see blending where they overlap."
                                    below == null && document.background == ProBackground.WHITE ->
                                        "Screen and Overlay produce white over this white canvas."
                                    else -> "Blending changes the selected layer where it overlaps the backdrop."
                                }
                                val target = selected
                                ProBlendPanel(
                                    layer = active,
                                    backdrop = backdrop,
                                    guidance = guidance,
                                    enabled = available,
                                    onMode = { mode ->
                                        if (selected == target && latestDocument.layers.getOrNull(target) === active &&
                                            active.blend != mode) changeLayer { it.copy(blend = mode) }
                                    },
                                    onOpacity = { opacity ->
                                        if (selected == target && latestDocument.layers.getOrNull(target) === active) {
                                            changeLayer({ it.copy(opacity = opacity) }, true)
                                        }
                                    },
                                    onOpacityDone = {
                                        if (canEditDocument() && selected == target) finishSlider()
                                    }
                                )
                            }
                            ProSection.CANVAS -> Unit
                        }
                    }
                }
                WorkspaceToolsBar(
                    expanded = expanded && hasWork,
                    onOpenTools = { showTools = true },
                    onToggleExpanded = { expanded = !expanded },
                    target = if (hasWork) listOfNotNull(section.title, active?.name).joinToString(" · ")
                        else "No Pro work loaded",
                    enabled = available && hasWork
                )
            }
        }
        if (busy || exporting || (rendering && (!pristine || showingFinishedOutput) && preview == null)) {
            Row(Modifier.align(Alignment.Center)
                .background(Color(0xE0212630), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = EditorMint,
                    modifier = Modifier.padding(end = 10.dp))
                Text(when {
                    exporting -> "Exporting locally…"
                    busy -> "Opening images…"
                    else -> "Preparing preview…"
                }, color = EditorWhite)
            }
        }
    }

    if (showTools) ProToolChooser(section, ::selectTool) { showTools = false }
    if (showStack) ProLayerStack(document, selected, enabled = available, onSelect = {
        if (canEditDocument()) {
            selectLayer(it)
            showStack = false
            selectTool(ProSection.LAYERS)
        }
    }, onVisibility = ::toggleLayerVisibility, onDismiss = { showStack = false })
    rename?.let { name ->
        SaveNameDialog("Rename layer", "Choose a name for this layer.", name,
            onName = { rename = it }, onSave = {
                if (name.isNotBlank()) changeLayer { it.copy(name = name.trim()) }
                rename = null
            }, onDismiss = { rename = null })
    }
    if (showAdd) AlertDialog(
        onDismissRequest = { showAdd = false },
        title = { Text("Add a layer", color = EditorWhite) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Choose what you want to create. Each item stays separately editable.",
                    color = EditorMuted, fontSize = 12.sp)
                TextButton(onClick = { showAdd = false; openImagePicker() }) {
                    Text("Photo or image", color = EditorMint)
                }
                TextButton(onClick = { addCreative(ProLayerKind.SHAPE) }) {
                    Text("Shape layer", color = EditorWhite)
                }
                TextButton(onClick = { addCreative(ProLayerKind.DRAWING) }) {
                    Text("Paint layer", color = EditorWhite)
                }
                TextButton(onClick = { addCreative(ProLayerKind.ADJUSTMENT) }) {
                    Text("Adjustment layer", color = EditorWhite)
                }
            }
        },
        confirmButton = { TextButton(onClick = { showAdd = false }) {
            Text("Close", color = EditorMuted)
        } }
    )
    val recoverable = pendingExport
    if (showExportRecovery && recoverable != null && !busy && !exporting) RecoverableExportDialog(
        request = recoverable,
        onRetry = { resolveExportRecovery(retry = true) },
        onDiscard = { resolveExportRecovery(retry = false) },
        onDismiss = { showExportRecovery = false },
        unavailable = exportRecoveryUnavailable,
        failure = exportFailure
    )
    if (showExport) ExportDialog(
        settings = exportSettings, onSettings = { exportSettings = it },
        onSave = ::startExport, onDismiss = { showExport = false },
        originalOutput = if (document.linkedFinishes.isNotEmpty()) {
            exportOutputSize?.let { "${it.width} × ${it.height} pixels with linked edits" }
                ?: "Output dimensions will follow the linked edits."
        } else if (safeExportSize == intendedExportSize) {
            "${safeExportSize.width} × ${safeExportSize.height} pixels"
        } else {
            "${safeExportSize.width} × ${safeExportSize.height} pixels on this device. " +
                "Project canvas: ${intendedExportSize.width} × ${intendedExportSize.height} pixels."
        }
    )
    pendingDeparture?.takeIf { departureStage == ProDepartureStage.PROMPT }?.let { request ->
        val emptySetup = request.document.layers.isEmpty()
        WorkspaceDepartureDialog(
            title = if (emptySetup) "Unsaved canvas setup" else "Unsaved Pro changes",
            message = if (emptySetup)
                "This canvas setup has changed. Add a layer before saving it, or discard the setup to continue."
                else "Save the current Pro changes before leaving? Discard restores the saved or entry version.",
            saveEnabled = !emptySetup && ProMath.valid(request.document),
            saveDisabledReason = if (emptySetup) "Add a layer before saving this setup." else null,
            onSave = { startSave() },
            onDiscard = {
                if (owns(request.token) && document == request.document) {
                    pendingDeparture = request.copy(discard = true)
                    departureStage = ProDepartureStage.READY
                } else cancelDeparture()
            },
            onCancel = ::cancelDeparture,
            discardLabel = if (emptySetup) "Discard setup" else "Discard"
        )
    }
    if (showSave && pendingReplace == null) SaveNameDialog(
        title = "Save Pro project",
        description = when {
            busy -> "Saving project…"
            saveError != null -> saveError.orEmpty()
            else -> "Preserves individual layers, order, adjustments and local image references."
        },
        name = draftName,
        onName = {
            if (!busy && (retrySave == null || retrySave?.intent is ProjectSaveIntent.Create)) {
                draftName = it
                retrySave = null
                saveError = null
            }
        },
        onSave = {
            val request = retrySave ?: ProSaveRequest(draftName, document, workspaceSnapshot(),
                pendingDeparture?.token ?: operationToken(), ProjectSaveIntent.Create(draftName))
            saveProject(request)
        },
        onDismiss = ::cancelSave
    )
    pendingReplace?.let { request ->
        AlertDialog(
            onDismissRequest = ::cancelSave,
            title = { Text("Replace saved Pro project?") },
            text = { Text("Replace “${request.name}” with the current Pro work?") },
            confirmButton = {
                TextButton(onClick = { saveProject(request) }, enabled = !busy) {
                    Text("Replace project", color = EditorMint)
                }
            },
            dismissButton = {
                TextButton(onClick = ::cancelSave, enabled = !busy) { Text("Cancel") }
            }
        )
    }
    if (showProjects && pendingSavedDelete == null) AlertDialog(
        onDismissRequest = { if (!busy) showProjects = false },
        title = { Text("Saved Pro projects") },
        text = {
            Column(Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                openError?.let { Text(it, color = EditorMuted) }
                if (busy) Text("Opening project…", color = EditorMuted)
                if (workingDrafts.isNotEmpty()) {
                    Text("Working drafts", color = EditorWhite)
                    workingDrafts.forEach { (key, title) ->
                        TextButton(onClick = { openWorkingDraft(key) }, enabled = !busy) {
                            Text(title, color = EditorMint)
                        }
                    }
                    Text("Named projects", color = EditorWhite)
                }
                if (savedNames.isEmpty()) Text("No saved Pro projects yet.", color = EditorMuted)
                savedNames.forEach { name ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { openProject(name) }, enabled = !busy) {
                            Text(name, color = EditorMint)
                        }
                        Spacer(Modifier.weight(1f))
                        TextButton(
                            onClick = prepareDelete@{
                                if (busy || !showProjects || pendingSavedDelete != null) return@prepareDelete
                                busy = true
                                openError = null
                                scope.launch {
                                    try {
                                        val target = withContext(Dispatchers.IO) {
                                            ProStore.deletionTarget(context, name)
                                        }
                                        if (showProjects) {
                                            pendingSavedDelete = target
                                            if (target == null) {
                                                openError = "Saved project changed or could not be checked for deletion."
                                            }
                                        }
                                    } finally { busy = false }
                                }
                            },
                            enabled = !busy,
                            modifier = Modifier.semantics { contentDescription = "Delete saved project $name" }
                        ) { Text("Delete", color = Color(0xFFFF9D9D)) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { showProjects = false }, enabled = !busy) {
            Text("Close")
        } }
    )
    pendingSavedDelete?.let { ref ->
        AlertDialog(
            onDismissRequest = { if (!busy) pendingSavedDelete = null },
            title = { Text("Delete saved Pro project?") },
            text = { Text("Delete the saved project “${ref.name}” permanently? Your open work and source photos will be kept.") },
            confirmButton = {
                TextButton(enabled = !busy, onClick = confirmSavedDelete@{
                    if (busy || pendingSavedDelete != ref) return@confirmSavedDelete
                    busy = true
                    scope.launch {
                        try {
                            val deleted = withContext(Dispatchers.IO) {
                                ProStore.deleteNamed(context, ref)
                            }
                            if (deleted) {
                                val matching = (history.undo + history.redo).filter {
                                    it.saveTarget.savedRef?.id == ref.id
                                }.map { it.saveTarget.workId }.toSet() +
                                    listOfNotNull(saveTarget.workId.takeIf { saveTarget.savedRef?.id == ref.id })
                                baselines = baselines.mapValues { (work, baseline) ->
                                    if (work in matching) ProWorkBaseline(baseline.cleanDocument) else baseline
                                }
                                if (saveTarget.savedRef?.id == ref.id) {
                                    saveTarget = saveTarget.copy(name = null, savedRef = null)
                                }
                                history = history.forgetSavedProject(ref.id)
                            }
                            savedNames = withContext(Dispatchers.IO) { ProStore.names(context) }
                            pendingSavedDelete = null
                            if (deleted) notice = "Saved Pro project deleted."
                            else openError = "Saved project changed or could not be deleted."
                        } finally { busy = false }
                    }
                }) { Text("Delete saved project", color = Color(0xFFFF9D9D)) }
            },
            dismissButton = {
                TextButton(enabled = !busy, onClick = { if (!busy) pendingSavedDelete = null }) { Text("Cancel") }
            }
        )
    }
    if (confirmDelete) WorkspaceDeleteDialog(
        message = "Delete this Pro composition and its working history?",
        onConfirm = ::deleteCurrentWork,
        onCancel = { confirmDelete = false; deleteError = null },
        busy = busy,
        error = deleteError
    )
    if (showHelp) ProHelpDialog(onDismiss = { showHelp = false })
}

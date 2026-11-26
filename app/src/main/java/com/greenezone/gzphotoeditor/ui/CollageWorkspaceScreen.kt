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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenezone.gzphotoeditor.editor.CollageAspect
import com.greenezone.gzphotoeditor.editor.CollageBackground
import com.greenezone.gzphotoeditor.editor.CollageCellMode
import com.greenezone.gzphotoeditor.editor.CollageDocument
import com.greenezone.gzphotoeditor.editor.CollageHistory
import com.greenezone.gzphotoeditor.editor.CollageLayout
import com.greenezone.gzphotoeditor.editor.CollageMath
import com.greenezone.gzphotoeditor.editor.CollagePhoto
import com.greenezone.gzphotoeditor.editor.CollageQuality
import com.greenezone.gzphotoeditor.editor.CollageRaster
import com.greenezone.gzphotoeditor.editor.CollageStore
import com.greenezone.gzphotoeditor.editor.CollageTemplate
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.ExportSettings
import com.greenezone.gzphotoeditor.editor.ExportWorkspace
import com.greenezone.gzphotoeditor.editor.PendingExport
import com.greenezone.gzphotoeditor.editor.PendingExportJournal
import com.greenezone.gzphotoeditor.editor.PendingExportPhase
import com.greenezone.gzphotoeditor.editor.PendingImageRecipe
import com.greenezone.gzphotoeditor.editor.ExportSizing
import com.greenezone.gzphotoeditor.editor.GeometryMath
import com.greenezone.gzphotoeditor.editor.writeStagedImage
import com.greenezone.gzphotoeditor.editor.ImageExportWriter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

import com.greenezone.gzphotoeditor.editor.NamedProject
import com.greenezone.gzphotoeditor.editor.NamedProjectRef
import com.greenezone.gzphotoeditor.editor.ProjectSaveIntent
import com.greenezone.gzphotoeditor.editor.ProjectSaveResult
import com.greenezone.gzphotoeditor.editor.ProjectSaveFailure
import com.greenezone.gzphotoeditor.editor.ImagePipeline
import java.util.UUID

internal enum class CollageSection(val label: String) {
    TEMPLATES("Layouts"), ASPECT("Aspect"), FRAME("Spacing"),
    BACKGROUND("Style"), TILES("Photos"), QUALITY("Quality")
}

internal enum class FrameControl(val title: String, val low: Float, val high: Float) {
    GAP("Spacing", 0f, .08f),
    MARGIN("Outside margin", 0f, .10f),
    ROUND("Corner roundness", 0f, .10f),
    BORDER("Border width", 0f, .025f)
}

internal data class CollageWorkspaceSession(
    val selectedPhoto: Int = 0,
    val history: CollageHistory = CollageHistory(),
    val section: CollageSection = CollageSection.TEMPLATES,
    val control: FrameControl = FrameControl.GAP,
    val toolsVisible: Boolean = true,
    val savedDocument: CollageDocument = CollageDocument(),
    val documentId: String = UUID.randomUUID().toString(),
    val entryDocument: CollageDocument? = null,
    val savedProject: NamedProjectRef? = null,
    val savedHistory: CollageHistory = CollageHistory(),
    val entryHistory: CollageHistory = CollageHistory(),
    val contentRevision: Long = 0,
    val tileRevisions: Map<String, Long> = emptyMap(),
    val toolsExpanded: Boolean = true
)

private data class CollageTileEntry(
    val parentId: String, val tile: CollagePhoto, val sourceRevision: Long
)
private data class TileSourceChoice(
    val entry: CollageTileEntry, val keepEdits: Boolean,
    val operationId: String = UUID.randomUUID().toString()
)

/** The registry key and callback both belong to one immutable picker launch. */
@Composable
private fun CollageAddPhotoPicker(
    token: WorkspaceOperationToken?,
    onResult: (WorkspaceOperationToken, List<Uri>) -> Unit
) {
    if (token == null) return
    key(token.operationId) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(CollageMath.MAX_PHOTOS)
        ) { onResult(token, it) }
        LaunchedEffect(token.operationId) {
            try {
                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } catch (_: Exception) { onResult(token, emptyList()) }
        }
    }
}

@Composable
private fun CollageTileSourcePicker(choice: TileSourceChoice?,
    onResult: (TileSourceChoice, Uri?) -> Unit) {
    if (choice == null) return
    key(choice.operationId) {
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            onResult(choice, it)
        }
        LaunchedEffect(choice.operationId) {
            try {
                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } catch (_: Exception) { onResult(choice, null) }
        }
    }
}
private data class PreparedTileSource(val choice: TileSourceChoice, val photo: CollagePhoto)

private sealed interface CollageDestination {
    data object Back : CollageDestination
    data class Switch(val mode: WorkspaceMode) : CollageDestination
    data class Open(val project: NamedProject<CollageDocument>) : CollageDestination
}
private data class CollageDeparture(
    val token: WorkspaceOperationToken,
    val original: CollageDocument,
    val candidate: CollageDocument,
    val candidateHistory: CollageHistory,
    val destination: CollageDestination,
    val child: CollageTileEntry? = null
)
private data class CollageDeleteRequest(
    val token: WorkspaceOperationToken,
    val document: CollageDocument,
    val child: CollageTileEntry?
)
private data class CollageSaveAttempt(
    val ownerId: String,
    val content: CollageDocument,
    val history: CollageHistory,
    val target: NamedProjectRef?,
    val asCopy: Boolean,
    val departureToken: String?
)

private data class CollageExport(
    val collage: CollageDocument,
    val settings: ExportSettings
) {
    fun encode(): String = PendingImageRecipe(settings, CollageStore.encode(collage)).encode()

    companion object {
        fun decode(pending: PendingExport): CollageExport? {
            val image = PendingImageRecipe.decode(pending.recipe) ?: return null
            return CollageStore.decode(image.payload)?.let { CollageExport(it, image.settings) }
        }
    }
}

@Composable
fun CollageWorkspaceScreen(
    document: CollageDocument,
    onDocument: (CollageDocument) -> Unit,
    onBack: () -> Unit,
    onSwitch: (WorkspaceMode) -> Unit = {}
) {
    var session by remember { mutableStateOf(CollageWorkspaceSession()) }
    CollageWorkspaceScreenContent(document, onDocument, onBack, onSwitch,
        session, { session = it })
}

@Composable
internal fun CollageWorkspaceScreenContent(
    document: CollageDocument,
    onDocument: (CollageDocument) -> Unit,
    onBack: () -> Unit,
    onSwitch: (WorkspaceMode) -> Unit,
    session: CollageWorkspaceSession,
    onSession: (CollageWorkspaceSession) -> Unit,
    onTrySwitch: ((WorkspaceMode, () -> Boolean) -> Boolean)? = null
) {
    val context = LocalContext.current
    val resolver = context.contentResolver
    val scope = rememberCoroutineScope()
    val latestDocument by rememberUpdatedState(document)
    var documentId by remember { mutableStateOf(session.documentId) }
    var contentRevision by remember { mutableStateOf(session.contentRevision) }
    var tileRevisions by remember { mutableStateOf(session.tileRevisions) }
    var selected by remember { mutableIntStateOf(session.selectedPhoto.coerceIn(
        0, document.photos.lastIndex.coerceAtLeast(0))) }
    var history by remember { mutableStateOf(session.history) }
    var entryDocument by remember { mutableStateOf(session.entryDocument
        ?: session.savedDocument.takeIf { it.photos.isNotEmpty() } ?: document) }
    var entryHistory by remember { mutableStateOf(session.entryHistory) }
    var savedProject by remember { mutableStateOf(session.savedProject) }
    var savedDocument by remember { mutableStateOf(session.savedDocument) }
    var savedHistory by remember { mutableStateOf(session.savedHistory) }
    var layoutGestureStart by remember { mutableStateOf<CollageDocument?>(null) }
    var layoutGestureLatest by remember { mutableStateOf<CollageDocument?>(null) }
    var section by remember { mutableStateOf(session.section) }
    var control by remember { mutableStateOf(session.control) }
    var toolsVisible by remember { mutableStateOf(session.toolsVisible) }
    var toolsExpanded by remember { mutableStateOf(session.toolsExpanded) }
    var toolsMenuOpen by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    val latestPreview by rememberUpdatedState(preview)
    var missingTileId by remember { mutableStateOf<String?>(null) }
    var tileSourceChoice by remember { mutableStateOf<TileSourceChoice?>(null) }
    var replaceConfirmation by remember { mutableStateOf<TileSourceChoice?>(null) }
    var preparedRelink by remember { mutableStateOf<PreparedTileSource?>(null) }
    var notice by remember { mutableStateOf("") }
    var conflictNotice by remember { mutableStateOf<String?>(null) }
    var rendering by remember { mutableStateOf(false) }
    var renderGeneration by remember { mutableIntStateOf(0) }
    var exporting by remember { mutableStateOf(false) }
    var loadingProject by remember { mutableStateOf(false) }
    var preparingSources by remember { mutableStateOf(false) }
    var writingProject by remember { mutableStateOf(false) }
    var deletingWork by remember { mutableStateOf(false) }
    var deleteWorkRequest by remember { mutableStateOf<CollageDeleteRequest?>(null) }
    var deleteWorkError by remember { mutableStateOf<String?>(null) }
    var editingTile by remember { mutableStateOf<CollageTileEntry?>(null) }
    var pendingDeparture by remember { mutableStateOf<CollageDeparture?>(null) }
    var showDeparture by remember { mutableStateOf(false) }
    var saveAttempt by remember { mutableStateOf<CollageSaveAttempt?>(null) }
    var saveIntent by remember { mutableStateOf<ProjectSaveIntent?>(null) }
    var saveConfirmation by remember { mutableStateOf(false) }
    var saveCollision by remember { mutableStateOf<NamedProjectRef?>(null) }
    var naming by remember { mutableStateOf("") }
    var saveError by remember { mutableStateOf<String?>(null) }
    var savedList by remember { mutableStateOf(false) }
    var savedNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var savedChoice by remember { mutableStateOf<String?>(null) }
    var projectOpenError by remember { mutableStateOf<String?>(null) }
    var deleteChoice by remember { mutableStateOf<NamedProjectRef?>(null) }
    var mediaToken by remember { mutableStateOf<WorkspaceOperationToken?>(null) }
    var showExport by remember { mutableStateOf(false) }
    val exportJournal = remember(context) { PendingExportJournal(context.filesDir) }
    val recoveredExport = remember { exportJournal.read(ExportWorkspace.COLLAGE) }
    var exportSettings by remember {
        mutableStateOf(recoveredExport?.let(CollageExport::decode)?.settings ?: ExportSettings())
    }
    var pendingExport by remember { mutableStateOf(recoveredExport) }
    var exportLaunchId by remember { mutableStateOf<String?>(null) }
    var showExportRecovery by remember { mutableStateOf(recoveredExport != null) }
    var exportRecoveryError by remember { mutableStateOf<String?>(null) }

    fun busy() = exporting || loadingProject || preparingSources || writingProject || deletingWork
    fun baseline() = if (savedProject != null) savedDocument else entryDocument
    fun baselineHistory() = (if (savedProject != null) savedHistory else entryHistory).copy(redo = emptyList())
    fun dirty(value: CollageDocument = latestDocument) = value != baseline()

    fun persistSession(value: CollageDocument = latestDocument) {
        onSession(CollageWorkspaceSession(
            selectedPhoto = selected.coerceIn(0, value.photos.lastIndex.coerceAtLeast(0)),
            history = history, section = section, control = control, toolsVisible = toolsVisible,
            savedDocument = savedDocument, documentId = documentId, entryDocument = entryDocument,
            savedProject = savedProject, savedHistory = savedHistory, entryHistory = entryHistory,
            contentRevision = contentRevision, tileRevisions = tileRevisions, toolsExpanded = toolsExpanded
        ))
    }

    fun recordTileChanges(before: CollageDocument, after: CollageDocument, forceId: String? = null) {
        val beforeById = before.photos.associateBy { it.id }
        val afterById = after.photos.associateBy { it.id }
        val changed = (beforeById.keys + afterById.keys).filter {
            beforeById[it] != afterById[it] || it == forceId
        }
        if (changed.isNotEmpty()) {
            val next = tileRevisions.toMutableMap()
            changed.forEach { next[it] = (next[it] ?: 0L) + 1L }
            tileRevisions = next
        }
    }

    fun commit(next: CollageDocument, forceSourceId: String? = null) {
        if (busy()) return
        val before = latestDocument
        history = history.checkpoint(before, next)
        if (before != next || forceSourceId != null) contentRevision++
        recordTileChanges(before, next, forceSourceId)
        onDocument(next)
        layoutGestureStart = null
        layoutGestureLatest = null
    }

    fun changeLayout(layout: CollageLayout) { commit(latestDocument.copy(layout = layout)) }
    fun slideLayout(layout: CollageLayout) {
        if (busy()) return
        if (layoutGestureStart == null) layoutGestureStart = latestDocument
        val next = latestDocument.copy(layout = layout)
        layoutGestureLatest = next
        onDocument(next)
    }
    fun finishLayout() {
        layoutGestureStart?.let { start ->
            val next = layoutGestureLatest ?: latestDocument
            history = history.checkpoint(start, next)
            if (start != next) contentRevision++
        }
        layoutGestureStart = null
        layoutGestureLatest = null
    }
    fun cancelLayout(): Boolean {
        val start = layoutGestureStart ?: return false
        onDocument(start)
        layoutGestureStart = null
        layoutGestureLatest = null
        notice = "Unfinished adjustment canceled."
        return true
    }

    fun moveSelectionTo(next: CollageDocument) {
        val selectedId = latestDocument.photos.getOrNull(selected)?.id
        val index = next.photos.indexOfFirst { it.id == selectedId }
        selected = if (index >= 0) index else selected.coerceIn(0, next.photos.lastIndex.coerceAtLeast(0))
    }
    fun undoCollage() {
        if (busy() || cancelLayout()) return
        history.undo(latestDocument)?.let { (previous, nextHistory) ->
            recordTileChanges(latestDocument, previous)
            contentRevision++
            history = nextHistory
            moveSelectionTo(previous)
            onDocument(previous)
        }
    }
    fun redoCollage() {
        if (busy() || cancelLayout()) return
        history.redo(latestDocument)?.let { (next, nextHistory) ->
            recordTileChanges(latestDocument, next)
            contentRevision++
            history = nextHistory
            moveSelectionTo(next)
            onDocument(next)
        }
    }

    fun tileEntry(): CollageTileEntry? = latestDocument.photos.getOrNull(selected)?.let {
        CollageTileEntry(documentId, it, tileRevisions[it.id] ?: 0L)
    }
    fun resolveTile(entry: CollageTileEntry, edited: CollagePhoto?): CollageDocument? {
        val current = latestDocument
        val index = current.photos.indexOfFirst { it.id == entry.tile.id }
        if (entry.parentId != documentId || index < 0 || current.photos[index] != entry.tile ||
            (tileRevisions[entry.tile.id] ?: 0L) != entry.sourceRevision) return null
        if (edited == null) return current
        return current.copy(photos = current.photos.toMutableList().apply {
            this[index] = edited.copy(id = entry.tile.id)
        })
    }

    fun valid(departure: CollageDeparture): Boolean =
        departure.token.ownerId == documentId &&
            departure.token.contentRevision == contentRevision &&
            departure.original == latestDocument &&
            (departure.child == null || departure.child == editingTile)

    fun clearDeparture() {
        pendingDeparture = null
        showDeparture = false
    }

    fun adoptProject(next: CollageDocument, reference: NamedProjectRef? = null) {
        recordTileChanges(latestDocument, next)
        documentId = UUID.randomUUID().toString()
        contentRevision = 0
        tileRevisions = emptyMap()
        entryDocument = next
        entryHistory = CollageHistory()
        savedProject = reference
        savedDocument = if (reference != null) next else CollageDocument()
        savedHistory = CollageHistory()
        history = CollageHistory()
        selected = 0
        editingTile = null
        layoutGestureStart = null
        layoutGestureLatest = null
        preview = null
        missingTileId = null
        savedList = false
        toolsVisible = true
        toolsExpanded = true
        toolsMenuOpen = false
        section = CollageSection.TEMPLATES
        control = FrameControl.GAP
        onDocument(next)
        persistSession(next)
    }

    fun completeDeparture(departure: CollageDeparture, resolved: CollageDocument,
        resolvedHistory: CollageHistory, discard: Boolean = false) {
        val destination = departure.destination
        var failureMessage: String? = null
        val commitPrepared: () -> Boolean = commitPrepared@ {
            if (pendingDeparture?.token != departure.token || !valid(departure)) {
                failureMessage = "The collage changed while this action was pending. Your current work is still open."
                return@commitPrepared false
            }
            if (discard || destination is CollageDestination.Open) {
                try {
                    // Export retry owns its immutable recipe independently. Only
                    // suppress native restoration of this departing document.
                    val pending = exportJournal.read(ExportWorkspace.COLLAGE)
                    if (pending != null && (pending.nativeOwnerId ?: pending.id) == documentId) {
                        val retained = exportJournal.suppressNativeRestore(pending, documentId)
                        if (retained == null) {
                            failureMessage = "The recovery request changed. Your collage and unapplied tile edits are still open."
                            return@commitPrepared false
                        }
                        pendingExport = retained
                    }
                } catch (_: Exception) {
                    failureMessage = "Could not update local recovery. Your collage and unapplied tile edits are still open."
                    return@commitPrepared false
                }
            }
            clearDeparture()
            when (destination) {
                is CollageDestination.Open -> {
                    adoptProject(destination.project.content, destination.project.project)
                    notice = "Saved collage opened"
                }
                CollageDestination.Back, is CollageDestination.Switch -> {
                    recordTileChanges(latestDocument, resolved)
                    if (latestDocument != resolved) contentRevision++
                    history = resolvedHistory
                    moveSelectionTo(resolved)
                    onDocument(resolved)
                    editingTile = null
                    persistSession(resolved)
                }
            }
            true
        }
        val accepted = if (destination is CollageDestination.Switch) {
            try {
                onTrySwitch?.invoke(destination.mode, commitPrepared) ?: run {
                    if (commitPrepared()) { onSwitch(destination.mode); true } else false
                }
            } catch (_: Exception) { false }
        } else commitPrepared()
        if (!accepted) {
            clearDeparture()
            conflictNotice = failureMessage ?:
                "The workspace could not be opened. Your collage and unapplied tile edits are still open."
        } else if (destination == CollageDestination.Back) onBack()
    }

    fun requestDeparture(destination: CollageDestination, candidate: CollageDocument = latestDocument,
        child: CollageTileEntry? = null) {
        if (busy() || pendingDeparture != null || saveAttempt != null) return
        if (cancelLayout()) return
        val departure = CollageDeparture(WorkspaceOperationToken(documentId, contentRevision),
            latestDocument, candidate, history.checkpoint(latestDocument, candidate), destination, child)
        pendingDeparture = departure
        menuExpanded = false
        if (dirty(candidate)) showDeparture = true
        else completeDeparture(departure, candidate, departure.candidateHistory)
    }

    fun validDelete(request: CollageDeleteRequest): Boolean =
        request.token.ownerId == documentId && request.token.contentRevision == contentRevision &&
            request.document == latestDocument && request.child == editingTile &&
            (request.child == null || resolveTile(request.child, null) != null)

    fun requestDeleteWork(child: CollageTileEntry? = null) {
        if (busy() || pendingDeparture != null || saveAttempt != null || deleteWorkRequest != null ||
            latestDocument.photos.isEmpty() || child != editingTile || cancelLayout()) return
        val request = CollageDeleteRequest(WorkspaceOperationToken(documentId, contentRevision),
            latestDocument, child)
        if (!validDelete(request)) return
        deleteWorkError = null
        deleteWorkRequest = request
    }

    fun confirmDeleteWork(request: CollageDeleteRequest) {
        if (busy() || deleteWorkRequest != request) return
        if (!validDelete(request)) {
            deleteWorkError = "The collage changed. Cancel and choose Delete again."
            return
        }
        deletingWork = true
        val ownerId = request.token.ownerId
        val visibleRequest = pendingExport
        scope.launch {
            try {
                val retained = withContext(Dispatchers.IO) {
                    val current = exportJournal.read(ExportWorkspace.COLLAGE)
                    check(current != null || visibleRequest == null) { "The export request is unavailable." }
                    if (current == null || (current.nativeOwnerId ?: current.id) != ownerId) current
                    else {
                        val settled = if (current.phase == PendingExportPhase.PICKING) {
                            checkNotNull(exportJournal.failed(current))
                        } else current
                        checkNotNull(exportJournal.suppressNativeRestore(settled, ownerId))
                    }
                }
                pendingExport = retained
                if (!validDelete(request)) {
                    deleteWorkError = "The collage changed. Cancel and choose Delete again."
                    return@launch
                }
                mediaToken = null
                tileSourceChoice = null
                replaceConfirmation = null
                preparedRelink = null
                exportLaunchId = null
                showExport = false
                showExportRecovery = false
                clearDeparture()
                deleteWorkRequest = null
                adoptProject(CollageDocument())
                notice = "Collage deleted."
            } catch (canceled: CancellationException) { throw canceled }
            catch (_: Exception) {
                deleteWorkError = "Could not update local recovery. Try Delete again."
            } finally {
                deletingWork = false
            }
        }
    }

    fun resetAllEdits() {
        if (busy() || latestDocument.photos.isEmpty() || cancelLayout()) return
        val next = latestDocument.resetEdits()
        if (next != latestDocument) commit(next)
    }

    fun requestBack() {
        if (menuExpanded) { menuExpanded = false; return }
        if (cancelLayout()) return
        if (!toolsVisible && latestDocument.photos.isNotEmpty()) { toolsVisible = true; return }
        if (busy()) { notice = "Finish the current operation before leaving."; return }
        requestDeparture(CollageDestination.Back)
    }
    fun requestSwitch(mode: WorkspaceMode) {
        if (mode != WorkspaceMode.COLLAGE) requestDeparture(CollageDestination.Switch(mode))
    }

    fun cancelSave() {
        if (writingProject) return
        saveAttempt = null
        saveIntent = null
        saveConfirmation = false
        saveCollision = null
        saveError = null
        clearDeparture()
    }

    fun performSave(intent: ProjectSaveIntent) {
        val attempt = saveAttempt ?: return
        if (writingProject || attempt.ownerId != documentId) return
        val targetToOpen = (pendingDeparture?.destination as? CollageDestination.Open)?.project?.project
        if (intent is ProjectSaveIntent.Replace && targetToOpen != null &&
            intent.expected.id == targetToOpen.id && attempt.target?.id != targetToOpen.id) {
            saveCollision = null
            saveIntent = null
            saveError = "Choose a different name from the collage you are opening."
            return
        }
        saveIntent = intent
        saveConfirmation = false
        saveCollision = null
        saveError = null
        writingProject = true
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    CollageStore.saveNamed(context, intent, attempt.content)
                }
                if (attempt.ownerId != documentId || saveAttempt !== attempt) return@launch
                when (result) {
                    is ProjectSaveResult.Saved -> {
                        savedProject = result.project
                        savedDocument = attempt.content
                        savedHistory = attempt.history
                        saveAttempt = null
                        saveIntent = null
                        notice = "Collage project saved"
                        persistSession()
                        val pending = pendingDeparture
                        if (pending != null && pending.token.operationId == attempt.departureToken) {
                            val target = pending.destination as? CollageDestination.Open
                            val refreshed = if (target?.project?.project?.id == result.project.id)
                                pending.copy(destination = CollageDestination.Open(
                                    NamedProject(result.project, attempt.content))) else pending
                            if (refreshed !== pending) pendingDeparture = refreshed
                            completeDeparture(refreshed, attempt.content, attempt.history)
                        }
                    }
                    is ProjectSaveResult.Exists -> {
                        if (attempt.asCopy && result.project.id == attempt.target?.id) {
                            saveIntent = null
                            saveError = "Save as needs a different name from this project's current name."
                        } else saveCollision = result.project
                    }
                    is ProjectSaveResult.Rejected -> {
                        if (result.reason == ProjectSaveFailure.INVALID_NAME) saveIntent = null
                        saveError = when (result.reason) {
                        ProjectSaveFailure.FULL -> "Twelve collages are already saved. Delete a saved project or replace one explicitly."
                        ProjectSaveFailure.TOO_LARGE -> "This collage has too many saved edit details. Your current work is still open."
                        ProjectSaveFailure.TARGET_CHANGED -> "That saved project changed. Cancel and reopen it, or use Save project as."
                        ProjectSaveFailure.INVALID_NAME -> "Enter a project name."
                        ProjectSaveFailure.INVALID_DOCUMENT -> "Add at least one photo before saving this collage."
                        ProjectSaveFailure.STORAGE_FAILURE -> "Could not save the collage. Your edits are still open. Check local storage and retry."
                        }
                    }
                }
            } catch (canceled: CancellationException) {
                throw canceled
            } catch (_: Exception) {
                if (attempt.ownerId == documentId) saveError = "Could not save the collage. Your edits are still open."
            } finally { writingProject = false }
        }
    }

    fun beginSave(asCopy: Boolean = false, departure: CollageDeparture? = null) {
        if (busy() || saveAttempt != null || cancelLayout()) return
        val content = departure?.candidate ?: latestDocument
        if (!CollageMath.validCount(content.photos.size)) {
            notice = "Add at least one photo before saving this collage."
            return
        }
        showDeparture = false
        naming = if (asCopy) "" else savedProject?.name.orEmpty()
        saveError = null
        val attempt = CollageSaveAttempt(documentId, content,
            departure?.candidateHistory ?: history, savedProject, asCopy, departure?.token?.operationId)
        saveAttempt = attempt
        saveIntent = if (!asCopy && attempt.target != null) ProjectSaveIntent.Update(attempt.target) else null
        if (saveIntent != null) {
            if (departure != null) performSave(saveIntent!!) else saveConfirmation = true
        }
    }

    fun requestProjects() {
        if (busy()) return
        loadingProject = true
        val owner = documentId
        scope.launch {
            try {
                val names = withContext(Dispatchers.IO) { CollageStore.names(context) }
                if (owner == documentId) {
                    savedNames = names
                    savedChoice = null
                    projectOpenError = null
                    savedList = true
                }
            } catch (canceled: CancellationException) { throw canceled }
            catch (_: Exception) { notice = "Saved collages could not be listed. Your work is unchanged." }
            finally { loadingProject = false }
        }
    }
    fun requestOpen(name: String) {
        if (busy()) return
        val token = WorkspaceOperationToken(documentId, contentRevision)
        loadingProject = true
        scope.launch {
            val candidate = try {
                withContext(Dispatchers.IO) { CollageStore.loadNamed(context, name) }
            } catch (canceled: CancellationException) { throw canceled }
            catch (_: Exception) { null }
            loadingProject = false
            if (token.ownerId != documentId || token.contentRevision != contentRevision) return@launch
            if (candidate == null) projectOpenError = "This saved collage could not be opened. Your current work is unchanged."
            else {
                savedList = false
                requestDeparture(CollageDestination.Open(candidate))
            }
        }
    }
    fun requestDelete(name: String) {
        if (busy()) return
        loadingProject = true
        val owner = documentId
        scope.launch {
            try {
                val target = withContext(Dispatchers.IO) { CollageStore.loadNamed(context, name)?.project }
                if (owner != documentId) return@launch
                if (target == null) projectOpenError = "The saved collage could not be identified safely."
                else { savedList = false; deleteChoice = target }
            } catch (canceled: CancellationException) { throw canceled }
            catch (_: Exception) { projectOpenError = "The saved collage could not be identified safely." }
            finally { loadingProject = false }
        }
    }

    fun retainReadPermission(uri: Uri) {
        try { resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        catch (_: SecurityException) { }
    }

    fun addPhotos(uris: List<Uri>, token: WorkspaceOperationToken) {
        if (uris.isEmpty() || token.ownerId != documentId || token.contentRevision != contentRevision) return
        val remaining = CollageMath.MAX_PHOTOS - latestDocument.photos.size
        if (remaining <= 0) { notice = "A collage supports up to twenty photos."; return }
        preparingSources = true
        scope.launch {
            try {
                val chosen = uris.take(remaining)
                chosen.forEach { retainReadPermission(it) }
                withContext(Dispatchers.IO) {
                    chosen.forEach { uri ->
                        ImagePipeline.decode(resolver, uri, 256, 65_536L).recycle()
                    }
                }
                preparingSources = false
                if (token.ownerId != documentId || token.contentRevision != contentRevision) return@launch
                val before = latestDocument
                val added = chosen.map { CollagePhoto(it.toString()) }
                commit(before.copy(photos = before.photos + added))
                selected = before.photos.size
                notice = if (uris.size > remaining) "Only the first twenty photos were added."
                    else (before.photos.size + added.size).toString() + " photos in collage"
            } catch (canceled: CancellationException) { throw canceled }
            catch (_: OutOfMemoryError) { notice = "Not enough memory to open these photos. Your collage is unchanged." }
            catch (_: Exception) { notice = "A selected photo could not be opened. Your collage is unchanged." }
            finally { preparingSources = false }
        }
    }

    CollageAddPhotoPicker(mediaToken) { token, uris ->
        if (mediaToken?.operationId == token.operationId) {
            mediaToken = null
            addPhotos(uris, token)
        }
    }
    CollageTileSourcePicker(tileSourceChoice) { choice, uri ->
        // An obsolete null, duplicate or late result cannot consume a newer choice.
        if (tileSourceChoice?.operationId == choice.operationId) {
            tileSourceChoice = null
            if (uri != null && resolveTile(choice.entry, null) != null) {
                preparingSources = true
                scope.launch {
                    try {
                        retainReadPermission(uri)
                        withContext(Dispatchers.IO) { ImagePipeline.decode(resolver, uri, 256, 65_536L).recycle() }
                        preparingSources = false
                        if (resolveTile(choice.entry, null) == null) {
                            conflictNotice = "The selected tile changed. Its current source and edits were preserved."
                            return@launch
                        }
                        val next = choice.entry.tile.copy(uri = uri.toString(),
                            edits = if (choice.keepEdits) choice.entry.tile.edits else EditDocument())
                        if (choice.keepEdits) preparedRelink = PreparedTileSource(choice, next)
                        else resolveTile(choice.entry, next)?.let {
                            commit(it, forceSourceId = next.id)
                            notice = "Photo replaced; its old adjustments were cleared."
                        }
                    } catch (canceled: CancellationException) { throw canceled }
                    catch (_: OutOfMemoryError) { notice = "Not enough memory to open that photo. The tile is unchanged." }
                    catch (_: Exception) { notice = "That photo could not be opened. The original tile and edits are unchanged." }
                    finally { preparingSources = false }
                }
            }
        }
    }
    fun launchTileSource(choice: TileSourceChoice) {
        if (busy() || tileSourceChoice != null || resolveTile(choice.entry, null) == null) return
        tileSourceChoice = choice
    }
    fun chooseTileSource(keepEdits: Boolean) {
        val entry = tileEntry() ?: return
        if (busy()) return
        val choice = TileSourceChoice(entry, keepEdits)
        if (keepEdits) launchTileSource(choice) else replaceConfirmation = choice
    }
    fun openMulti() {
        if (busy() || mediaToken != null || cancelLayout()) return
        if (latestDocument.photos.size >= CollageMath.MAX_PHOTOS) {
            notice = "Maximum of twenty photos reached."
            return
        }
        mediaToken = WorkspaceOperationToken(documentId, contentRevision)
    }

    editingTile?.let { entry ->
        PhotoEditorScreen(
            initialCollagePhoto = entry.tile,
            onDoneCollagePhoto = { edited ->
                if (editingTile == entry && !busy() && pendingDeparture == null) {
                    val next = resolveTile(entry, edited)
                    if (next == null) conflictNotice = "This tile changed elsewhere. Your unapplied photo edits remain open; no other tile was changed."
                    else {
                        commit(next)
                        selected = next.photos.indexOfFirst { it.id == entry.tile.id }.coerceAtLeast(0)
                        editingTile = null
                    }
                }
            },
            onCancelCollagePhoto = { if (!busy() && pendingDeparture == null) editingTile = null },
            onDeleteCollageWork = { requestDeleteWork(entry) },
            onRequestCollageWorkspace = { mode, edited ->
                if (editingTile == entry && !busy() && pendingDeparture == null) {
                    val next = resolveTile(entry, edited)
                    if (next == null) conflictNotice = "This tile changed elsewhere. Your unapplied photo edits remain open."
                    else if (mode == WorkspaceMode.COLLAGE) { commit(next); editingTile = null }
                    else requestDeparture(CollageDestination.Switch(mode), next, entry)
                }
            }
        )
    }
    BackHandler(enabled = editingTile == null) { requestBack() }

    fun retainExportForRetry(pending: PendingExport, message: String) {
        notice = message
        exportRecoveryError = message
        try {
            pendingExport = exportJournal.failed(pending)
        } catch (_: Exception) {
            pendingExport = exportJournal.read(ExportWorkspace.COLLAGE)
        }
        showExportRecovery = pendingExport != null
    }

    fun runExport(uri: Uri, request: CollageExport, pending: PendingExport) {
        if (request.collage.photos.any { it.uri == uri.toString() }) {
            retainExportForRetry(pending, "Export destination cannot overwrite a source photo.")
            return
        }
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val budget = GeometryMath.safeExportPixels(manager.memoryClass)
        scope.launch {
            exporting = true
            showExportRecovery = false
            exportRecoveryError = null
            notice = "Exporting collage…"
            try {
                val size = withContext(Dispatchers.Default) {
                    var image: Bitmap? = null
                    var composed: Bitmap? = null
                    try {
                        val (width, height) = CollageMath.outputSize(
                            request.collage.layout,
                            request.collage.layout.quality.maxEdge,
                            budget
                        )
                        val frame = currentCoroutineContext()
                        val rendered = CollageRaster.render(resolver, request.collage,
                            width, height, false) { frame.ensureActive() }
                        image = rendered
                        val target = ExportSizing.layout(rendered, request.settings)
                        composed = target
                        withContext(Dispatchers.IO) {
                            writeStagedImage(context.cacheDir,
                                openDestination = {
                                    exportJournal.copying(pending)
                                    resolver.openOutputStream(uri, "wt")
                                },
                                checkActive = { ensureActive() }) { stream -> ImageExportWriter.write(target, request.settings, stream) }
                        }
                        target.width.toString() + " × " + target.height
                    } finally {
                        composed?.let { if (it !== image && !it.isRecycled) it.recycle() }
                        image?.let { if (!it.isRecycled) it.recycle() }
                    }
                }
                exportJournal.discard(pending)
                pendingExport = null
                notice = "Collage saved • " + size + " • No watermark"
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                retainExportForRetry(pending, "Export failed: " + (error.message ?: "unavailable photo"))
            } catch (_: OutOfMemoryError) {
                retainExportForRetry(pending, "Not enough memory. Try Standard or High quality.")
            } finally {
                exporting = false
            }
        }
    }

    RecoverableExportPicker(
        pending = pendingExport,
        launchId = exportLaunchId,
        onLaunch = { if (exportLaunchId == it) exportLaunchId = null },
        onLaunchFailed = { pending, error ->
            retainExportForRetry(pending, error.message ?: "Could not open the destination picker")
        },
        onResult = resultReceived@ { pending, result ->
            if (deletingWork) return@resultReceived
            try {
                val active = exportJournal.read(ExportWorkspace.COLLAGE)
                if (active == null || active.id != pending.id || active.phase != PendingExportPhase.PICKING) {
                    return@resultReceived
                }
                if (result.resultCode != Activity.RESULT_OK) {
                    if (exportJournal.cancelPicker(pending)) {
                        pendingExport = null
                        showExportRecovery = false
                        notice = "Collage export canceled"
                    }
                } else {
                    val uri = result.data?.data
                    val request = CollageExport.decode(pending)
                    if (uri == null || request == null) {
                        retainExportForRetry(pending, "The export destination or collage could not be restored.")
                    } else {
                        val claimed = exportJournal.claim(pending, uri)
                        if (claimed != null) {
                            pendingExport = claimed
                            runExport(uri, request, claimed)
                        }
                    }
                }
            } catch (error: Exception) {
                retainExportForRetry(pending, error.message ?: "Could not start the collage export")
            }
        }
    )
    fun askToSave() {
        if (busy() || cancelLayout()) return
        val snapshot = latestDocument
        if (!CollageMath.validCount(snapshot.photos.size)) return
        showExport = false
        if (pendingExport != null) {
            showExportRecovery = true
            return
        }
        val settings = exportSettings
        try {
            val pending = exportJournal.begin(ExportWorkspace.COLLAGE, settings.format.mime,
                "GZEditorPro_Collage." + settings.format.extension, CollageExport(snapshot, settings).encode(),
                nativeOwnerId = documentId)
            pendingExport = pending
            exportLaunchId = pending.id
        } catch (error: Exception) {
            notice = "Could not prepare collage export: " + (error.message ?: "local storage failed")
        }
    }

    LaunchedEffect(document) {
        renderGeneration += 1
        val generation = renderGeneration
        if (document.photos.isEmpty()) {
            preview = null
            missingTileId = null
            rendering = false
            savedNames = withContext(Dispatchers.IO) { CollageStore.names(context) }
            return@LaunchedEffect
        }
        rendering = true
        try {
            delay(125L)
            val (width, height) = CollageMath.outputSize(document.layout, 960, 1_300_000L)
            val value = withContext(Dispatchers.Default) {
                val job = currentCoroutineContext()
                CollageRaster.render(resolver, document, width, height, true) {
                    job.ensureActive()
                }
            }
            preview = value
            missingTileId = null
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: CollageRaster.MissingPhoto) {
            preview = null
            missingTileId = document.photos.getOrNull(error.position)?.id
            selected = error.position
            section = CollageSection.TILES
            toolsVisible = true
            notice = "Photo " + (error.position + 1) +
                " is inaccessible. Relink its source or replace the tile."
        } catch (error: Exception) {
            preview = null
            missingTileId = null
            toolsVisible = true
            notice = "Cannot render collage: " + (error.message ?: "missing source photo")
        } finally {
            if (renderGeneration == generation) rendering = false
        }
    }

    if (editingTile == null) {
        Box(Modifier.fillMaxSize().background(Color(0xFF080B10)), contentAlignment = Alignment.Center) {
            val shown = preview
            if (document.photos.isEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    modifier = Modifier.padding(horizontal = 28.dp)) {
                    Text("Collage", color = EditorWhite, fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
                    Text("Combine 1–20 photos and edit each one.", color = EditorMuted, fontSize = 13.sp)
                    Text("Use Options to add photos or open a saved project.", color = EditorMuted, fontSize = 13.sp)
                }
            } else if (shown != null) {
                Image(bitmap = shown.asImageBitmap(), contentDescription = "Photo collage preview",
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                Canvas(Modifier.fillMaxSize()
                    .semantics {
                        contentDescription = "Collage canvas. Tap to hide or show tools."
                        onClick(label = "Hide or show editing controls") {
                            toolsVisible = !toolsVisible
                            true
                        }
                    }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            if (currentEvent.changes.size != 1) return@awaitEachGesture

                            val startSize = size
                            val startDocument = latestDocument
                            val startOwner = documentId
                            val bitmap = latestPreview ?: return@awaitEachGesture
                            val factor = min(startSize.width.toFloat() / bitmap.width,
                                startSize.height.toFloat() / bitmap.height)
                            val width = bitmap.width * factor
                            val height = bitmap.height * factor
                            val left = (startSize.width - width) / 2f
                            val top = (startSize.height - height) / 2f
                            val hit = CollageMath.select((down.position.x - left) / width,
                                (down.position.y - top) / height, startDocument.photos.size, startDocument.layout)
                            down.consume()

                            do {
                                val pointer = awaitPointerEvent().changes.singleOrNull()
                                    ?: return@awaitEachGesture
                                if (pointer.id != down.id || pointer.isConsumed ||
                                    size != startSize || documentId != startOwner ||
                                    latestDocument != startDocument || latestPreview !== bitmap ||
                                    pointer.position.x !in 0f..startSize.width.toFloat() ||
                                    pointer.position.y !in 0f..startSize.height.toFloat() ||
                                    (pointer.position - down.position).getDistance() > viewConfiguration.touchSlop
                                ) return@awaitEachGesture
                                pointer.consume()
                            } while (pointer.pressed)

                            if (hit >= 0) selected = hit
                            toolsVisible = !toolsVisible
                        }
                    }) {
                    if (selected in document.photos.indices) {
                        val factor = min(size.width / shown.width, size.height / shown.height)
                        val width = shown.width * factor
                        val height = shown.height * factor
                        val left = (size.width - width) / 2f
                        val top = (size.height - height) / 2f
                        val cell = CollageMath.innerFrames(document.photos.size, document.layout)[selected]
                        drawRect(EditorMint, Offset(left + cell.left * width, top + cell.top * height),
                            Size(cell.width * width, cell.height * height), style = Stroke(3f))
                    }
                }
            }
            val chrome = toolsVisible || document.photos.isEmpty()
            if (chrome) {
                val canSave = CollageMath.validCount(document.photos.size)
                val commands = listOf(
                    WorkspaceCommand(WorkspaceCommandId.OPEN_MEDIA, "Add photos…",
                        !busy() && document.photos.size < CollageMath.MAX_PHOTOS,
                        if (document.photos.size >= CollageMath.MAX_PHOTOS) "Maximum of twenty photos reached." else null,
                        ::openMulti),
                    WorkspaceCommand(WorkspaceCommandId.OPEN_PROJECT, "Open project…", !busy(),
                        onInvoke = ::requestProjects),
                    WorkspaceCommand(WorkspaceCommandId.SAVE, "Save project",
                        !busy() && canSave && (savedProject == null || dirty()),
                        if (!canSave) "Add at least one photo before saving." else if (savedProject != null && !dirty())
                            "Already saved." else null) { beginSave() },
                    WorkspaceCommand(WorkspaceCommandId.SAVE_AS, "Save project as…", !busy() && canSave,
                        if (!canSave) "Add at least one photo before saving." else null) { beginSave(asCopy = true) },
                    WorkspaceCommand(WorkspaceCommandId.EXPORT, "Export image…",
                        !busy() && (canSave || pendingExport != null),
                        if (!canSave && pendingExport == null) "Add at least one photo before exporting." else null) {
                        if (!cancelLayout()) {
                            if (pendingExport != null) showExportRecovery = true else showExport = true
                        }
                    },
                    WorkspaceCommand(WorkspaceCommandId.RESET, "Reset all edits", !busy() && canSave,
                        if (!canSave) "Add photos to reset edits." else null, ::resetAllEdits),
                    WorkspaceCommand(WorkspaceCommandId.DELETE, "Delete", !busy() && canSave,
                        if (!canSave) "No active collage to delete." else null) { requestDeleteWork() },
                    WorkspaceCommand(WorkspaceCommandId.HELP, "Help", !busy()) { showHelp = true }
                )
                Column(Modifier.align(Alignment.TopCenter).fillMaxWidth().windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                )) {
                    EditorWorkspaceHeader(
                        workspace = WorkspaceMode.COLLAGE, commands = commands,
                        canUndo = history.undo.isNotEmpty() && !busy(),
                        canRedo = history.redo.isNotEmpty() && !busy(),
                        onUndo = ::undoCollage, onRedo = ::redoCollage,
                        onChooseWorkspace = ::requestSwitch,
                        menuExpanded = menuExpanded, onMenuExpandedChange = { menuExpanded = it },
                        enabled = !busy(), onBack = ::requestBack
                    )
                    if (notice.isNotBlank()) Text(notice, color = EditorMuted, fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth().background(EditorSurface).padding(horizontal = 12.dp))
                }
                val hasPhotos = document.photos.isNotEmpty()
                Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(EditorSurface)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (hasPhotos && toolsExpanded) {
                        CollageControls(
                            document = document, selected = selected, section = section, control = control,
                            enabled = !busy(),
                            onLayout = ::changeLayout, onSlideLayout = ::slideLayout,
                            onLayoutFinished = ::finishLayout,
                            onSection = { section = it }, onControl = { control = it },
                            onSelect = { selected = it },
                            onReplace = { chooseTileSource(keepEdits = false) },
                            onRelink = if (missingTileId != null &&
                                document.photos.getOrNull(selected)?.id == missingTileId)
                                ({ chooseTileSource(keepEdits = true) }) else null,
                            onEdit = { if (!busy() && !cancelLayout()) editingTile = tileEntry() },
                            onLeft = {
                                val id = latestDocument.photos.getOrNull(selected)?.id
                                val next = latestDocument.copy(photos =
                                    CollageMath.move(latestDocument.photos, selected, selected - 1))
                                commit(next)
                                selected = next.photos.indexOfFirst { it.id == id }.coerceAtLeast(0)
                            },
                            onRight = {
                                val id = latestDocument.photos.getOrNull(selected)?.id
                                val next = latestDocument.copy(photos =
                                    CollageMath.move(latestDocument.photos, selected, selected + 1))
                                commit(next)
                                selected = next.photos.indexOfFirst { it.id == id }.coerceAtLeast(0)
                            },
                            onRemove = {
                                val next = latestDocument.copy(photos = latestDocument.photos.filterIndexed { index, _ ->
                                    index != selected
                                })
                                commit(next)
                                selected = selected.coerceIn(0, next.photos.lastIndex.coerceAtLeast(0))
                            }
                        )
                    }
                    if (!hasPhotos) Text("© 2026 Greenezone Software LLC", color = EditorMuted,
                        fontSize = 10.sp, modifier = Modifier.padding(start = 16.dp, top = 7.dp))
                    Box {
                        WorkspaceToolsBar(expanded = hasPhotos && toolsExpanded,
                            onOpenTools = { toolsMenuOpen = true },
                            onToggleExpanded = { toolsExpanded = !toolsExpanded },
                            target = if (hasPhotos) section.label else "Add photos to edit",
                            enabled = hasPhotos && !busy())
                        DropdownMenu(expanded = toolsMenuOpen,
                            onDismissRequest = { toolsMenuOpen = false }) {
                            CollageSection.entries.forEach { choice ->
                                DropdownMenuItem(text = { Text(choice.label) }, onClick = {
                                    section = choice
                                    toolsExpanded = true
                                    toolsMenuOpen = false
                                })
                            }
                        }
                    }
                }
            } else {
                TextButton(onClick = { toolsVisible = true },
                    modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding()) {
                    Text("Show tools", color = EditorMint)
                }
            }
            if (rendering || busy()) {
                Row(Modifier.align(Alignment.Center).background(Color(0xDB19222B), RoundedCornerShape(14.dp))
                    .padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(21.dp), color = EditorMint, strokeWidth = 2.dp)
                    Text(if (exporting) " Exporting…" else if (writingProject) " Saving project…"
                        else if (preparingSources || loadingProject) " Opening…" else " Updating…",
                        color = EditorWhite, fontSize = 12.sp)
                }
            }
        }
    }

    deleteWorkRequest?.let { request ->
        WorkspaceDeleteDialog(
            message = if (request.child != null)
                "Delete this collage and its pending photo edits? Your original photos and saved projects stay on your phone."
            else "Delete this collage? Your original photos and saved projects stay on your phone.",
            onConfirm = { confirmDeleteWork(request) },
            onCancel = { if (!deletingWork) deleteWorkRequest = null },
            busy = deletingWork,
            error = deleteWorkError
        )
    }

    val departure = pendingDeparture
    if (showDeparture && departure != null && saveAttempt == null) {
        WorkspaceDepartureDialog(
            title = "Keep your collage edits?",
            message = if (savedProject != null)
                "Save changes to “${savedProject!!.name}”, discard only the unsaved changes, or cancel."
                else "Save an editable collage, discard the unsaved changes, or cancel.",
            saveEnabled = CollageMath.validCount(departure.candidate.photos.size),
            saveDisabledReason = if (!CollageMath.validCount(departure.candidate.photos.size))
                "Add at least one photo before saving this collage." else null,
            onSave = { beginSave(departure = departure) },
            onDiscard = { completeDeparture(departure, baseline(), baselineHistory(), discard = true) },
            onCancel = ::clearDeparture
        )
    }

    val attempt = saveAttempt
    val collision = saveCollision
    if (writingProject && attempt != null) {
        AlertDialog(onDismissRequest = {}, title = { Text("Saving collage…") },
            text = { Text("Publishing the editable project. Your current work stays open until saving finishes.") },
            confirmButton = {})
    } else if (attempt != null && collision != null) {
        AlertDialog(onDismissRequest = ::cancelSave,
            title = { Text("Replace saved collage?") },
            text = { Text("Replace “${collision.name}” with this collage? Other saved projects and source photos stay unchanged.") },
            confirmButton = { TextButton(onClick = { performSave(ProjectSaveIntent.Replace(collision)) }) { Text("Replace") } },
            dismissButton = { Row {
                TextButton(onClick = ::cancelSave) { Text("Cancel") }
                TextButton(onClick = {
                    saveCollision = null; saveIntent = null; saveError = null
                }) { Text("Choose another name") }
            } })
    } else if (attempt != null && saveConfirmation) {
        AlertDialog(onDismissRequest = ::cancelSave,
            title = { Text("Save collage changes?") },
            text = { Text("Update the editable project “${attempt.target?.name.orEmpty()}” with the current collage.") },
            confirmButton = { TextButton(onClick = { saveIntent?.let(::performSave) }) { Text("Save") } },
            dismissButton = { TextButton(onClick = ::cancelSave) { Text("Cancel") } })
    } else if (attempt != null && saveIntent != null && saveError != null) {
        AlertDialog(onDismissRequest = ::cancelSave, title = { Text("Collage not saved") },
            text = { Text(saveError.orEmpty()) },
            confirmButton = { TextButton(onClick = { saveIntent?.let(::performSave) }) { Text("Retry") } },
            dismissButton = { TextButton(onClick = ::cancelSave) { Text("Cancel") } })
    } else if (attempt != null) {
        SaveNameDialog(
            title = if (attempt.asCopy) "Save collage as" else "Save editable collage",
            description = "All tile edits and photo references stay on this device. Keep the original photos available.",
            name = naming, onName = { naming = it; saveError = null; saveIntent = null },
            error = saveError, onSave = { performSave(ProjectSaveIntent.Create(naming)) },
            onDismiss = ::cancelSave
        )
    }

    if (savedList) {
        AlertDialog(onDismissRequest = { if (!loadingProject) savedList = false },
            title = { Text("Saved collages", color = EditorWhite) },
            text = {
                Column(Modifier.heightIn(max = 350.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    projectOpenError?.let { Text(it, color = EditorWhite) }
                    if (savedNames.isEmpty()) Text("No collages saved yet.", color = EditorMuted)
                    savedNames.forEach { name ->
                        DockChip(name, savedChoice == name, !loadingProject) { savedChoice = name }
                    }
                }
            },
            confirmButton = { TextButton(enabled = savedChoice != null && !loadingProject,
                onClick = { savedChoice?.let(::requestOpen) }) { Text("Open", color = EditorMint) } },
            dismissButton = { Row {
                TextButton(enabled = savedChoice != null && !loadingProject,
                    onClick = { savedChoice?.let(::requestDelete) }) { Text("Delete saved project", color = EditorMuted) }
                TextButton(enabled = !loadingProject, onClick = { savedList = false }) { Text("Cancel", color = EditorMuted) }
            } })
    }

    deleteChoice?.let { target ->
        fun returnToProjects() { deleteChoice = null; savedList = true }
        AlertDialog(onDismissRequest = { if (!writingProject) returnToProjects() },
            title = { Text("Delete saved collage?") },
            text = { Text("Delete “${target.name}” from saved collages. Your open collage and original photos stay on your phone.") },
            confirmButton = { TextButton(enabled = !writingProject, onClick = {
                if (!writingProject) {
                    writingProject = true
                    scope.launch {
                        try {
                            val removed = withContext(Dispatchers.IO) { CollageStore.deleteNamed(context, target) }
                            if (removed) {
                                if (savedProject?.id == target.id) savedProject = null
                                savedNames = withContext(Dispatchers.IO) { CollageStore.names(context) }
                                savedChoice = null
                                notice = "Saved collage deleted."
                            } else projectOpenError = "The saved collage changed or could not be deleted."
                            returnToProjects()
                        } catch (canceled: CancellationException) { throw canceled }
                        catch (_: Exception) {
                            projectOpenError = "The saved collage could not be deleted."
                            returnToProjects()
                        } finally { writingProject = false }
                    }
                }
            }) { Text("Delete collage") } },
            dismissButton = { TextButton(enabled = !writingProject, onClick = ::returnToProjects) { Text("Cancel") } })
    }

    replaceConfirmation?.let { choice ->
        AlertDialog(onDismissRequest = { replaceConfirmation = null },
            title = { Text("Replace selected photo?") },
            text = { Text("Choose a new source for this tile. Its current photo edits will reset; Undo can restore the complete old tile.") },
            confirmButton = { TextButton(onClick = {
                replaceConfirmation = null
                launchTileSource(choice)
            }) { Text("Choose photo") } },
            dismissButton = { TextButton(onClick = { replaceConfirmation = null }) { Text("Cancel") } })
    }
    preparedRelink?.let { prepared ->
        AlertDialog(onDismissRequest = { preparedRelink = null },
            title = { Text("Confirm original source") },
            text = { Text("The selected file is readable. Confirm it is the same original photo with matching framing; existing tile edits will be kept. If the original is unavailable, its content cannot be independently compared.") },
            confirmButton = { TextButton(onClick = {
                val next = resolveTile(prepared.choice.entry, prepared.photo)
                if (next == null) conflictNotice = "The tile changed. Its existing source and edits were preserved."
                else {
                    commit(next, forceSourceId = prepared.photo.id)
                    notice = "Source relinked with its adjustments. Check that the photo and edit alignment match."
                }
                preparedRelink = null
            }) { Text("Use this source") } },
            dismissButton = { TextButton(onClick = { preparedRelink = null }) { Text("Cancel") } })
    }
    conflictNotice?.let { message ->
        AlertDialog(onDismissRequest = { conflictNotice = null }, title = { Text("Work preserved") },
            text = { Text(message) }, confirmButton = {
                TextButton(onClick = { conflictNotice = null }) { Text("Keep editing") }
            })
    }
    if (showHelp) {
        AlertDialog(onDismissRequest = { showHelp = false }, title = { Text("Collage help") },
            text = { Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Use Options to add photos, open, save or export a collage. Save keeps an editable project; Export creates a finished image. Keep source photos available.")
                Text("A free tap selects the photo under it and hides or shows the controls. Tapping a margin only toggles the controls. Editing guides remain visible.")
                Text("Choose a photo in Photos, then Edit selected. Done applies only that tile; Cancel keeps the parent's tile unchanged. A separately saved photo project does not apply the tile.")
                Text("Relink restores a missing original while keeping edits. Replace adopts a new photo and resets that tile's edits. Other tiles stay independent.")
            } }, confirmButton = { TextButton(onClick = { showHelp = false }) { Text("Close") } })
    }

    val recovery = pendingExport
    if (editingTile == null && showExportRecovery && recovery != null && !exporting) {
        val canRecover = remember(recovery.id) { CollageExport.decode(recovery) != null }
        RecoverableExportDialog(
            request = recovery,
            onRetry = {
                try {
                    val retry = exportJournal.retry(recovery)
                    pendingExport = retry
                    showExportRecovery = false
                    exportLaunchId = retry.id
                } catch (error: Exception) {
                    notice = error.message ?: "Could not retry collage export"
                }
            },
            onDiscard = {
                try {
                    exportJournal.discard(recovery)
                    pendingExport = null
                    showExportRecovery = false
                } catch (error: Exception) {
                    notice = error.message ?: "Could not clear the collage export request"
                }
            },
            onDismiss = { showExportRecovery = false },
            unavailable = if (canRecover) null
                else "The saved collage export recipe could not be read. Discard this request to export the current collage.",
            failure = exportRecoveryError
        )
    }
    if (editingTile == null && showExport) {
        ExportDialog(
            settings = exportSettings,
            onSettings = { exportSettings = it },
            onSave = ::askToSave,
            onDismiss = { showExport = false }
        )
    }
}

@Composable
private fun CollageControls(
    document: CollageDocument,
    selected: Int,
    section: CollageSection,
    control: FrameControl,
    enabled: Boolean,
    onLayout: (CollageLayout) -> Unit,
    onSlideLayout: (CollageLayout) -> Unit,
    onLayoutFinished: () -> Unit,
    onSection: (CollageSection) -> Unit,
    onControl: (FrameControl) -> Unit,
    onSelect: (Int) -> Unit,
    onReplace: () -> Unit,
    onRelink: (() -> Unit)?,
    onEdit: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onRemove: () -> Unit
) {
    val layout = document.layout
    when (section) {
        CollageSection.TEMPLATES -> {
            LazyRow(Modifier.fillMaxWidth().height(46.dp),
                contentPadding = PaddingValues(horizontal = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(CollageTemplate.entries) { kind ->
                    DockChip(kind.label, kind == layout.template, enabled) {
                        onLayout(layout.copy(template = kind))
                    }
                }
            }
            Text("Tap once to select a photo and hide or show tools.",
                color = EditorMuted, fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 5.dp))
        }
        CollageSection.ASPECT -> {
            LazyRow(Modifier.fillMaxWidth().height(46.dp),
                contentPadding = PaddingValues(horizontal = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(CollageAspect.entries) { aspect ->
                    DockChip(aspect.label, aspect == layout.aspect, enabled) {
                        onLayout(layout.copy(aspect = aspect))
                    }
                }
            }
            Text("Choose your collage's final canvas shape.",
                color = EditorMuted, fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 5.dp))
        }
        CollageSection.FRAME -> {
            LazyRow(Modifier.fillMaxWidth().height(46.dp),
                contentPadding = PaddingValues(horizontal = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(FrameControl.entries) { item ->
                    DockChip(item.title, control == item, enabled) { onControl(item) }
                }
            }
            val value = when (control) {
                FrameControl.GAP -> layout.gap
                FrameControl.MARGIN -> layout.margin
                FrameControl.ROUND -> layout.cornerRadius
                FrameControl.BORDER -> layout.border
            }
            fun update(v: Float) = when (control) {
                FrameControl.GAP -> layout.copy(gap = v)
                FrameControl.MARGIN -> layout.copy(margin = v)
                FrameControl.ROUND -> layout.copy(cornerRadius = v)
                FrameControl.BORDER -> layout.copy(border = v)
            }
            SingleAdjustment(
                title = control.title,
                value = value,
                low = control.low,
                high = control.high,
                enabled = enabled,
                onReset = { onLayout(update(0f)) },
                onChange = { onSlideLayout(update(it)) },
                onFinished = onLayoutFinished
            )
        }
        CollageSection.BACKGROUND -> {
            LazyRow(Modifier.fillMaxWidth().height(46.dp),
                contentPadding = PaddingValues(horizontal = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(CollageBackground.entries) { bg ->
                    DockChip(bg.label, bg == layout.background, enabled) {
                        onLayout(layout.copy(background = bg))
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CollageCellMode.entries.forEach { mode ->
                    DockChip(mode.label, mode == layout.cellMode, enabled) {
                        onLayout(layout.copy(cellMode = mode))
                    }
                }
            }
        }
        CollageSection.TILES -> {
            LazyRow(Modifier.fillMaxWidth().height(46.dp),
                contentPadding = PaddingValues(horizontal = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                document.photos.forEachIndexed { index, _ ->
                    item(key = "tile-$index") {
                        DockChip("Photo " + (index + 1), selected == index, enabled) {
                            onSelect(index)
                        }
                    }
                }
            }
            LazyRow(Modifier.fillMaxWidth().height(46.dp).testTag("collage-tile-actions"),
                contentPadding = PaddingValues(horizontal = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item { DockChip("Edit selected", false, enabled, onEdit) }
                if (onRelink != null) {
                    item { DockChip("Relink source", false, enabled, onRelink) }
                }
                item { DockChip("Replace", false, enabled, onReplace) }
                item { DockChip("Move left", false, enabled && selected > 0, onLeft) }
                item { DockChip("Move right", false, enabled && selected < document.photos.lastIndex, onRight) }
                item { DockChip("Remove", false, enabled, onRemove) }
            }
        }
        CollageSection.QUALITY -> {
            LazyRow(Modifier.fillMaxWidth().height(46.dp),
                contentPadding = PaddingValues(horizontal = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(CollageQuality.entries) { q ->
                    DockChip(q.label, q == layout.quality, enabled) {
                        onLayout(layout.copy(quality = q))
                    }
                }
            }
            Text("Large exports automatically respect phone memory limits.",
                color = EditorMuted, fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
        }
    }

    LazyRow(Modifier.fillMaxWidth().height(47.dp).testTag("collage-sections"),
        contentPadding = PaddingValues(horizontal = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        items(CollageSection.entries) { item ->
            DockChip(item.label, item == section, enabled) { onSection(item) }
        }
    }
}

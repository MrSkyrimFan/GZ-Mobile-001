package com.greenezone.gzphotoeditor.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenezone.gzphotoeditor.editor.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt
import kotlin.math.roundToLong

internal enum class VideoGifTab(val title: String) {
    TIMELINE("Timeline"), PLAYBACK("Playback"), ADJUST("Adjust"), LOOKS("Looks"),
    CAPTION("Caption"), CANVAS("Canvas")
}

private fun timeLabel(ms: Long): String = String.format(Locale.US, "%d:%02d.%02d",
    ms / 60_000, (ms / 1000) % 60, (ms % 1000) / 10)

@Composable
fun VideoGifScreen(
    document: GifDocument,
    onDocument: (GifDocument) -> Unit,
    onBack: () -> Unit,
    onSwitch: (WorkspaceMode) -> Unit,
    onOpenLegacyPhotoProject: (GifDocument) -> Unit
) {
    var session by remember { mutableStateOf(GifWorkspaceSession()) }
    VideoGifScreenContent(
        document = document,
        onDocument = onDocument,
        onBack = onBack,
        onSwitch = onSwitch,
        onOpenLegacyPhotoProject = onOpenLegacyPhotoProject,
        session = session,
        onSession = { session = it }
    )
}

@Composable
internal fun VideoGifScreenContent(
    document: GifDocument,
    onDocument: (GifDocument) -> Unit,
    onBack: () -> Unit,
    onSwitch: (WorkspaceMode) -> Unit,
    onOpenLegacyPhotoProject: (GifDocument) -> Unit,
    session: GifWorkspaceSession,
    onSession: (GifWorkspaceSession) -> Unit
) {
    val context = LocalContext.current
    val resolver = context.contentResolver
    val scope = rememberCoroutineScope()
    val clip = document.video
    val initialDiagnosticOperation = remember {
        GifPreviewDiagnostics.nextOperationId()
    }
    var diagnosticOperation by remember {
        mutableLongStateOf(initialDiagnosticOperation)
    }
    LaunchedEffect(initialDiagnosticOperation) {
        GifPreviewDiagnostics.record(initialDiagnosticOperation,
            stage = GifDiagnosticStage.G0_SOURCE_PICKED,
            kind = GifDiagnosticKind.OPERATION_STARTED)
    }
    // Preview and export share decoder ownership. Export first unmounts preview.
    val videoReaderOwner = remember { Mutex() }
    val editedCodecFailed = remember(diagnosticOperation, clip?.uri) { AtomicBoolean() }
    val latestDiagnosticOperation by rememberUpdatedState(diagnosticOperation)
    DisposableEffect(Unit) {
        onDispose {
            GifPreviewDiagnostics.record(latestDiagnosticOperation,
                stage = GifDiagnosticStage.G7_LIFECYCLE,
                kind = GifDiagnosticKind.SCREEN_CLOSED)
        }
    }
    var tab by remember { mutableStateOf(session.videoTab) }
    var playing by remember { mutableStateOf(false) }
    var seek by remember { mutableIntStateOf(session.videoSeek) }
    var position by remember { mutableIntStateOf(session.videoPosition) }
    var busy by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf("") }
    var lastExport by remember { mutableStateOf<Uri?>(null) }
    val gifExport = rememberGifExport(video = true, videoReaderOwner = videoReaderOwner,
        onNotice = { notice = it }, onSaved = { uri, _ ->
            lastExport = uri
            notice = "GIF saved"
        })
    LaunchedEffect(gifExport.uiAcknowledgement) {
        if (gifExport.uiAcknowledgement > 0) {
            withFrameNanos { }
            gifExport.acknowledgeVisibleState()
        }
    }
    LaunchedEffect(gifExport.visibleCompletion) {
        if (gifExport.visibleCompletion > 0) {
            withFrameNanos { }
            gifExport.acknowledgeCompletion()
        }
    }
    val exporting = gifExport.active
    val committingExport = gifExport.committing
    val custody = remember { GifDocumentCustody(document, session) }
    val departure = remember { GifDepartureGate(custody) }
    var saveCapture by remember { mutableStateOf<GifSaveCapture?>(null) }
    var mediaPick by remember { mutableStateOf<GifMediaPickRequest?>(null) }
    val enabled = !busy && !exporting && !departure.pending && saveCapture == null
    var history by remember { mutableStateOf(session.history) }
    val savedName = custody.savedName
    var dragStart by remember { mutableStateOf<GifProjectSnapshot?>(null) }
    var dragLatest by remember { mutableStateOf<GifDocument?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var showSaved by remember { mutableStateOf(false) }
    var savedNames by remember { mutableStateOf(emptyList<String>()) }
    var collapsed by remember { mutableStateOf(session.videoCollapsed) }
    var chromeVisible by remember { mutableStateOf(session.videoChromeVisible) }

    fun pauseAt(frame: Int) {
        playing = false
        seek = frame
        position = frame
    }
    fun snapshot(
        current: GifDocument = custody.current,
        name: String? = custody.savedName,
        route: GifWorkspaceRoute = if (current.frames.isNotEmpty() && current.video == null)
            GifWorkspaceRoute.PHOTOS else GifWorkspaceRoute.VIDEO
    ) = GifProjectSnapshot(current, name, route, custody.identity)

    fun persistVideoSession(route: GifWorkspaceRoute = GifWorkspaceRoute.VIDEO) {
        onSession(custody.retain(session.copy(
            route = route,
            videoTab = tab,
            videoSeek = seek,
            videoPosition = position,
            history = history,
            savedName = custody.savedName,
            videoCollapsed = collapsed,
            videoChromeVisible = chromeVisible
        ), history))
    }

    fun applySnapshot(next: GifProjectSnapshot, frame: Int) {
        pauseAt(frame)
        dragStart = null
        dragLatest = null
        custody.restore(next)
        if (next.route == GifWorkspaceRoute.PHOTOS) {
            persistVideoSession(next.route)
            onOpenLegacyPhotoProject(next.document)
        } else onDocument(next.document)
    }

    fun commit(next: GifDocument, frame: Int = position, nextName: String? = custody.savedName,
               identity: GifDocumentIdentity = custody.identity) {
        val updated = snapshot(next, nextName).copy(identity = identity)
        history = history.checkpoint(snapshot(), updated)
        GifPreviewDiagnostics.record(diagnosticOperation,
            stage = GifDiagnosticStage.G5_EDIT,
            kind = GifDiagnosticKind.DOCUMENT_COMMITTED,
            frameIndex = frame.coerceAtLeast(0),
            frameCount = next.video?.let { GifVideoMath.frameCount(it) })
        applySnapshot(updated, frame)
    }

    fun slide(next: GifDocument, frame: Int = position) {
        if (dragStart == null) dragStart = snapshot()
        dragLatest = next
        custody.record(next)
        pauseAt(frame)
        GifPreviewDiagnostics.record(diagnosticOperation,
            stage = GifDiagnosticStage.G5_EDIT,
            kind = GifDiagnosticKind.DOCUMENT_SLID,
            frameIndex = frame.coerceAtLeast(0),
            frameCount = next.video?.let { GifVideoMath.frameCount(it) })
        onDocument(next)
    }
    fun finishSlide() {
        val start = dragStart
        val end = dragLatest
        if (start != null && end != null) {
            history = history.checkpoint(start, snapshot(end))
            GifPreviewDiagnostics.record(diagnosticOperation,
                stage = GifDiagnosticStage.G5_EDIT,
                kind = GifDiagnosticKind.SLIDE_FINISHED)
        }
        dragStart = null
        dragLatest = null
    }
    fun restore(entry: Pair<GifProjectSnapshot, GifUndoStack<GifProjectSnapshot>>?) {
        if (entry == null) return
        history = entry.second
        applySnapshot(entry.first, 0)
    }
    fun discardWorking(): Boolean {
        if (!gifExport.suppressNativeRestore(custody.identity.ownerId)) return false
        val baseline = custody.identity.baseline
        history = GifUndoStack()
        applySnapshot(snapshot(baseline), 0)
        return true
    }
    fun beginSave(saveAs: Boolean = false) {
        if (!GifMath.valid(custody.current) || busy || exporting || saveCapture != null) return
        pauseAt(position)
        finishSlide()
        saveCapture = GifSaveCapture(departure.saveToken(), custody.current, custody.identity.project, saveAs)
        departure.beginSave()
    }
    fun requestCommand(action: () -> Unit) {
        if (busy || exporting || saveCapture != null || departure.pending || mediaPick != null) return
        finishSlide()
        pauseAt(position)
        departure.request(action)
    }

    fun openSource(uri: Uri) {
        if (busy || exporting) return
        val openingOperation = GifPreviewDiagnostics.nextOperationId()
        diagnosticOperation = openingOperation
        GifPreviewDiagnostics.record(openingOperation,
            stage = GifDiagnosticStage.G0_SOURCE_PICKED,
            kind = GifDiagnosticKind.OPERATION_STARTED)
        GifPreviewDiagnostics.record(openingOperation,
            stage = GifDiagnosticStage.G0_SOURCE_PICKED,
            kind = GifDiagnosticKind.SOURCE_OPEN_STARTED)
        busy = true
        pauseAt(position)
        notice = ""
        scope.launch {
            try {
                try {
                    resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: SecurityException) {
                    // Some providers grant access only for this session.
                }
                val fresh = withContext(Dispatchers.IO) { VideoGifSource.document(context, uri) }
                val openedClip = requireNotNull(fresh.video)
                GifPreviewDiagnostics.record(openingOperation,
                    stage = GifDiagnosticStage.G1_SOURCE_OPEN,
                    kind = GifDiagnosticKind.SOURCE_OPEN_SUCCEEDED,
                    frameCount = GifVideoMath.frameCount(openedClip),
                    width = openedClip.width,
                    height = openedClip.height,
                    durationMs = openedClip.durationMs)
                if (!departure.complete(::discardWorking) {
                    commit(fresh, 0, nextName = null, identity = custody.fresh(fresh))
                    lastExport = null
                    collapsed = false
                    chromeVisible = true
                    tab = VideoGifTab.TIMELINE
                    persistVideoSession()
                } && notice.isBlank()) notice = "The source was not opened because this operation is no longer current."
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                GifPreviewDiagnostics.record(openingOperation,
                    stage = GifDiagnosticStage.G1_SOURCE_OPEN,
                    kind = GifDiagnosticKind.SOURCE_OPEN_FAILED,
                    failure = error)
                notice = error.message ?: "Cannot open this source."
            } catch (error: OutOfMemoryError) {
                GifPreviewDiagnostics.record(openingOperation,
                    stage = GifDiagnosticStage.G1_SOURCE_OPEN,
                    kind = GifDiagnosticKind.SOURCE_OPEN_FAILED,
                    failure = error)
                notice = "There is not enough memory to open this source."
            } finally { busy = false; departure.cancel() }
        }
    }
    fun chooseVideo() { mediaPick = GifMediaPickRequest(departure.saveToken(), GifMediaPickKind.VIDEO) }
    fun chooseGif() { mediaPick = GifMediaPickRequest(departure.saveToken(), GifMediaPickKind.GIF) }
    fun openProjects() {
        savedNames = GifStore.names(context)
        showSaved = true
    }

    fun startExport() {
        if (!enabled || !GifMath.valid(document)) return
        pauseAt(position)
        gifExport.start(PendingGifRecipe(document, savedName), diagnosticOperation, custody.identity.ownerId)
    }

    fun finishVideoRouteExit() {
        finishSlide()
        val frameCount = document.video?.let { GifVideoMath.frameCount(it) } ?: 0
        pauseAt(position.coerceIn(0, (frameCount - 1).coerceAtLeast(0)))
        GifPreviewDiagnostics.record(diagnosticOperation,
            stage = GifDiagnosticStage.G7_LIFECYCLE,
            kind = GifDiagnosticKind.ROUTE_CLOSED,
            frameIndex = position.coerceAtLeast(0),
            frameCount = frameCount)
    }

    fun leave(action: () -> Unit) {
        departure.complete(::discardWorking) {
            finishVideoRouteExit()
            persistVideoSession()
            action()
        }
    }
    fun requestBack() {
        if (showMenu) { showMenu = false; return }
        if (dragStart != null) {
            val before = requireNotNull(dragStart)
            applySnapshot(before, position)
            return
        }
        if (committingExport) { notice = "Saving GIF; keep this workspace open until copying finishes."; return }
        if (exporting) { notice = "Finish or cancel the GIF export before leaving."; return }
        if (busy) { notice = "Wait for the source to finish opening."; return }
        requestCommand { leave(onBack) }
    }
    fun requestSwitch(mode: WorkspaceMode) {
        if (mode == WorkspaceMode.GIF) return
        requestCommand { leave { onSwitch(mode) } }
    }

    BackHandler { requestBack() }

    val count = remember(clip) { clip?.let { GifVideoMath.frameCount(it) } ?: 0 }
    val time = remember(clip) { clip?.let { GifVideoMath.frameTimesMs(it) }.orEmpty() }
    val activeFrame = position.coerceIn(0, (count - 1).coerceAtLeast(0))
    val setEdits: (EditDocument) -> Unit = { commit(document.copy(edits = it)) }
    val slideEdits: (EditDocument) -> Unit = { slide(document.copy(edits = it)) }

    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFF080B10))
        .statusBarsPadding().navigationBarsPadding()) {
        val deckHeight = maxHeight * .38f
        Column(Modifier.fillMaxSize()) {
            if (chromeVisible || clip == null) EditorWorkspaceHeader(
                workspace = WorkspaceMode.GIF,
                commands = listOf(
                    WorkspaceCommand(WorkspaceCommandId.OPEN_MEDIA, "Open video…", enabled,
                        if (enabled) null else "Finish the current operation first") { requestCommand(::chooseVideo) },
                    WorkspaceCommand(WorkspaceCommandId.OPEN_SECONDARY_MEDIA, "Open GIF…", enabled,
                        if (enabled) null else "Finish the current operation first") { requestCommand(::chooseGif) },
                    WorkspaceCommand(WorkspaceCommandId.OPEN_PROJECT, "Open project…", enabled,
                        if (enabled) null else "Finish the current operation first") { openProjects() },
                    WorkspaceCommand(WorkspaceCommandId.SAVE, "Save project", enabled && GifMath.valid(document),
                        if (!GifMath.valid(document)) "Open a supported source before saving" else null) { beginSave() },
                    WorkspaceCommand(WorkspaceCommandId.SAVE_AS, "Save project as…", enabled && GifMath.valid(document),
                        if (!GifMath.valid(document)) "Open a supported source before saving" else null) { beginSave(true) },
                    WorkspaceCommand(WorkspaceCommandId.EXPORT, "Export GIF…", enabled && GifMath.valid(document),
                        if (!GifMath.valid(document)) "Open a supported source before exporting" else null, ::startExport),
                    WorkspaceCommand(WorkspaceCommandId.HELP, "Help") { pauseAt(position); showHelp = true }
                ),
                canUndo = enabled && history.undo.isNotEmpty(), canRedo = enabled && history.redo.isNotEmpty(),
                onUndo = { restore(history.undo(snapshot())) }, onRedo = { restore(history.redo(snapshot())) },
                onChooseWorkspace = ::requestSwitch, menuExpanded = showMenu,
                onMenuExpandedChange = { showMenu = it }, enabled = enabled, onBack = ::requestBack)

            if (notice.isNotBlank()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(notice, color = EditorWhite, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    // The success text is published before finally clears exporting.
                    // Do not expose a clickable link until openSource() can accept it.
                    if (enabled && lastExport != null && notice == "GIF saved") TextButton(onClick = {
                        lastExport?.let { uri -> requestCommand { openSource(uri) } }
                    }) { Text("Edit saved GIF", color = EditorMint) }
                    TextButton(onClick = { notice = "" }) { Text("Dismiss", color = EditorMuted) }
                }
            }
            if (clip == null) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Select one video to turn into a GIF", color = EditorWhite,
                        fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text("Use Options to open a video, GIF or saved project.", color = EditorMuted)
                    Text("© 2026 Greenezone Software LLC", color = EditorMuted, fontSize = 12.sp)
                }
            } else {
                if (busy || exporting) {
                    // Suspend decoding, frame caches and thumbnail bitmaps while
                    // replacing the source or encoding output, particularly on
                    // low-memory Android 10 devices.
                    Box(Modifier.weight(1f).fillMaxWidth())
                } else {
                    GifFramePreview(document, diagnosticOperation, playing, seek,
                        editedCodecFailed = editedCodecFailed, readerOwner = videoReaderOwner,
                        onFrame = { position = it },
                        onEnd = { pauseAt(it) },
                        onError = { playing = false; notice = "Preview unavailable. $it" },
                        modifier = Modifier.weight(1f).fillMaxWidth()
                            .pointerInput(Unit) { detectTapGestures(onTap = { chromeVisible = !chromeVisible }) }
                            .semantics {
                                customActions = listOf(CustomAccessibilityAction("Show editing controls") {
                                    chromeVisible = true; true
                                })
                            })
                }
                if (chromeVisible) Column(Modifier.fillMaxWidth().background(EditorSurface),
                    verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        TextButton(enabled = enabled && count > 1, onClick = {
                            if (playing) pauseAt(position) else {
                                seek = if (activeFrame == count - 1) 0 else activeFrame
                                playing = true
                            }
                        }) { Text(if (playing) "Pause" else "Play", color = EditorMint) }
                        FilterChip(selected = clip.reverse, enabled = enabled && count > 1,
                            onClick = {
                                val resume = playing
                                val index = if (resume && activeFrame > 0) count - 1 - activeFrame else 0
                                commit(document.copy(video = clip.copy(reverse = !clip.reverse)), index)
                                playing = resume
                            },
                            label = { Text("Reverse") })
                        TextButton(enabled = enabled, onClick = { collapsed = !collapsed }) {
                            Text(if (collapsed) "Tools ▴" else "Hide ▾", color = EditorWhite)
                        }
                    }
                    if (!collapsed) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            val (w, h) = GifMath.dimensions(document)
                            Text("${w} × ${h}  ·  ${timeLabel(time.getOrElse(activeFrame) { 0L })}",
                                fontSize = 12.sp, color = EditorMuted, modifier = Modifier.weight(1f))

                        }
                        GifOptions {
                            VideoGifTab.entries.forEach { item ->
                                DockChip(item.title, tab == item, enabled) { tab = item }
                            }
                        }
                        Column(Modifier.fillMaxWidth().heightIn(max = deckHeight)
                            .verticalScroll(rememberScrollState()).padding(vertical = 5.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            when (tab) {
                                VideoGifTab.TIMELINE -> GifTimeline(clip, activeFrame, count, enabled,
                                    onSeek = ::pauseAt, onTrim = { next, end ->
                                        val index = if (end != next.reverse) GifVideoMath.frameCount(next) - 1 else 0
                                        slide(document.copy(video = next), index)
                                    }, onFinish = ::finishSlide)
                                VideoGifTab.PLAYBACK -> {
                                    GifOptions {
                                        GifVideoMath.FRAME_RATES.forEach { fps ->
                                            DockChip(
                                                if (fps == 0) "Source timing" else "$fps FPS",
                                                clip.fps == fps,
                                                enabled && GifVideoMath.fitsFrameBudget(clip, fps)
                                            ) {
                                                commit(document.copy(video = clip.copy(fps = fps)), 0)
                                            }
                                        }
                                    }
                                    GifOptions {
                                        GifVideoMath.SPEEDS.forEach { speed ->
                                            DockChip("${speed}%", clip.speedPercent == speed, enabled) {
                                                commit(document.copy(video = clip.copy(speedPercent = speed)))
                                            }
                                        }
                                    }
                                    GifOptions {
                                        DockChip("Play once", document.loopCount == -1, enabled) {
                                            commit(document.copy(forever = false, repeatCount = null))
                                        }
                                        DockChip("Loop forever", document.loopCount == 0, enabled) {
                                            commit(document.copy(forever = true, repeatCount = null))
                                        }
                                        if (clip.loopCount > 0) DockChip("Repeat ${clip.loopCount}×",
                                            document.loopCount == clip.loopCount, enabled) {
                                            commit(document.copy(forever = false, repeatCount = clip.loopCount))
                                        }
                                    }
                                }
                                VideoGifTab.ADJUST -> GradeAdjustmentDeck(document.edits, enabled, setEdits, slideEdits, ::finishSlide)
                                VideoGifTab.LOOKS -> GifLooksDeck(document.edits, enabled, setEdits)
                                VideoGifTab.CAPTION -> GifCaptionDeck(document.edits, enabled, setEdits, slideEdits, ::finishSlide)
                                VideoGifTab.CANVAS -> {
                                    GifOptions {
                                        GifSize.entries.forEach { size ->
                                            DockChip(size.label, document.size == size, enabled) { commit(document.copy(size = size)) }
                                        }
                                    }
                                    GifOptions {
                                        GifAspect.entries.forEach { aspect ->
                                            DockChip(aspect.label, document.aspect == aspect, enabled) { commit(document.copy(aspect = aspect)) }
                                        }
                                    }
                                    GifOptions {
                                        GifFit.entries.forEach { fit ->
                                            DockChip(fit.label, document.fit == fit, enabled) { commit(document.copy(fit = fit)) }
                                        }
                                    }
                                    val edits = document.edits
                                    GifOptions {
                                        DockChip("Rotate 90°", false, enabled) {
                                            setEdits(edits.copy(quarterTurns = (edits.quarterTurns + 1) % 4))
                                        }
                                        DockChip("Mirror", edits.mirrored, enabled) { setEdits(edits.copy(mirrored = !edits.mirrored)) }
                                        DockChip("Flip vertical", edits.flipVertical, enabled) { setEdits(edits.copy(flipVertical = !edits.flipVertical)) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!enabled) Column(Modifier.align(Alignment.Center).background(EditorSurface).padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = EditorMint)
            Text(if (exporting) gifExport.progress else "Opening source…", color = EditorWhite)
            if (exporting) {
                TextButton(enabled = !committingExport, onClick = {
                    if (!committingExport) gifExport.cancel()
                }) {
                    Text("Cancel export", color = if (committingExport) EditorMuted else EditorMint)
                }
            }
        }
    }
    GifMediaPicker(mediaPick) { expected, selected ->
        if (mediaPick?.token?.operationId == expected.token.operationId && custody.matches(expected.token)) {
            mediaPick = null
            if (selected.isEmpty()) departure.cancel() else openSource(selected.first())
        }
    }
    GifExportRecovery(gifExport)
    if (showHelp) GifHelpDialog { showHelp = false }
    if (departure.dialogOpen) WorkspaceDepartureDialog(
        message = "Save the current GIF project before leaving these edits?",
        saveEnabled = GifMath.valid(custody.current),
        saveDisabledReason = if (GifMath.valid(custody.current)) null else "Open a supported source before saving",
        onSave = { beginSave() }, onDiscard = departure::discard, onCancel = departure::cancel)
    saveCapture?.let { capture ->
        GifProjectSaveDialog(capture, onSaved = { project ->
            val currentRevision = custody.saved(capture.token, project, capture.document)
            saveCapture = null
            persistVideoSession()
            notice = "Project saved"
            if (currentRevision) departure.saved(capture.token)
            else { departure.cancel(); notice = "The captured project was saved; newer edits are still here." }
        }, onDismiss = { saveCapture = null; departure.cancel() })
    }
    if (showSaved) GifSavedProjectsDialog(
        names = savedNames,
        enabled = !busy && !exporting && saveCapture == null,
        onOpen = { name ->
            showSaved = false
            requestCommand {
            busy = true
            scope.launch {
                try {
                    val saved = withContext(Dispatchers.IO) { GifStore.loadNamed(context, name) }
                    if (saved == null) notice = "Could not read the saved project or its source file."
                    else departure.complete(::discardWorking) {
                        if (saved.content.video == null) finishVideoRouteExit() else finishSlide()
                        commit(saved.content, 0, nextName = saved.project.name,
                            identity = custody.fresh(saved.content, saved.project))
                        lastExport = null
                        persistVideoSession(if (saved.content.video == null) GifWorkspaceRoute.PHOTOS else GifWorkspaceRoute.VIDEO)
                    }
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (failed: Exception) { notice = "Could not open this GIF project. " + failed.message.orEmpty() }
                finally { busy = false; departure.cancel() }
            }
            }
        },
        onDelete = { name ->
            val saved = GifStore.loadNamed(context, name)
            (saved != null && GifStore.deleteNamed(context, saved.project)).also { deleted ->
                if (deleted) {
                    finishSlide()
                    custody.detach(name)
                    history = history.withoutSavedProject(name)
                    savedNames = GifStore.names(context)
                    persistVideoSession()
                }
            }
        },
        onDismiss = { showSaved = false; departure.cancel() }
    )

}

@Composable
private fun GifTimeline(clip: GifVideoClip, index: Int, count: Int, enabled: Boolean,
    onSeek: (Int) -> Unit, onTrim: (GifVideoClip, Boolean) -> Unit, onFinish: () -> Unit) {
    fun label(value: String): Modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
        .semantics { contentDescription = value }
    Text("Frame ${index + 1} of $count", Modifier.padding(start = 14.dp), color = EditorWhite, fontSize = 12.sp)
    Slider(value = index.toFloat(), valueRange = 0f..(count - 1).coerceAtLeast(1).toFloat(),
        enabled = enabled && count > 1, modifier = label("Scrub GIF frames"),
        onValueChange = { onSeek(it.roundToInt().coerceIn(0, count - 1)) })
    Text("Trim  ${timeLabel(clip.startMs)} – ${timeLabel(clip.endMs)}", Modifier.padding(start = 14.dp),
        color = EditorWhite, fontSize = 12.sp)
    val maxSpan = GifVideoMath.maxTrimDurationMs(clip)
    if (maxSpan < clip.durationMs) {
        Text("Maximum trim at this frame rate: ${timeLabel(maxSpan)}",
            Modifier.padding(start = 14.dp), color = EditorMuted, fontSize = 12.sp)
    }
    RangeSlider(value = clip.startMs.toFloat()..clip.endMs.toFloat(),
        onValueChange = { range ->
            val movingEnd = range.start == clip.startMs.toFloat()
            val requested = if (movingEnd) range.endInclusive else range.start
            val trimmed = GifVideoMath.trimWithinFrameBudget(clip, requested.roundToLong(), movingEnd)
            if (trimmed != clip) onTrim(trimmed, movingEnd)
        }, onValueChangeFinished = onFinish, valueRange = 0f..clip.durationMs.toFloat(),
        enabled = enabled, modifier = label("Trim start and end"))
}

package com.greenezone.gzphotoeditor.ui

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.key
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenezone.gzphotoeditor.editor.ExportWorkspace
import com.greenezone.gzphotoeditor.editor.GifAspect
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.GifEncoder
import com.greenezone.gzphotoeditor.editor.GifFit
import com.greenezone.gzphotoeditor.editor.GifMath
import com.greenezone.gzphotoeditor.editor.GifSize
import com.greenezone.gzphotoeditor.editor.GifVideoMath
import com.greenezone.gzphotoeditor.editor.GifStore
import com.greenezone.gzphotoeditor.editor.NamedProjectRef
import com.greenezone.gzphotoeditor.editor.VideoGifSource
import com.greenezone.gzphotoeditor.editor.GifUndoStack
import com.greenezone.gzphotoeditor.editor.ImagePipeline
import com.greenezone.gzphotoeditor.editor.PendingExportJournal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal enum class GifTool(val label: String) {
    FRAMES("Photos"), TIMING("Timing"), SIZE("Canvas")
}

internal enum class GifWorkspaceRoute { VIDEO, PHOTOS }

internal data class GifDocumentIdentity(
    val ownerId: String = java.util.UUID.randomUUID().toString(),
    val entryDocument: GifDocument,
    val project: NamedProjectRef? = null,
    val savedDocument: GifDocument? = null
) {
    val baseline: GifDocument get() = savedDocument ?: entryDocument
}

internal data class GifProjectSnapshot(
    val document: GifDocument,
    val savedName: String?,
    val route: GifWorkspaceRoute,
    val identity: GifDocumentIdentity? = null
)

internal fun GifUndoStack<GifProjectSnapshot>.withoutSavedProject(name: String): GifUndoStack<GifProjectSnapshot> {
    fun detach(snapshot: GifProjectSnapshot): GifProjectSnapshot =
        if (snapshot.savedName == name) snapshot.copy(savedName = null,
            identity = snapshot.identity?.copy(project = null, savedDocument = null)) else snapshot
    return copy(undo = undo.map(::detach), redo = redo.map(::detach))
}

internal data class GifWorkspaceSession(
    val route: GifWorkspaceRoute? = null,
    val photoTool: GifTool = GifTool.FRAMES,
    val photoIndex: Int = 0,
    val history: GifUndoStack<GifProjectSnapshot> = GifUndoStack(),
    val savedName: String? = null,
    val videoTab: VideoGifTab = VideoGifTab.TIMELINE,
    val videoSeek: Int = 0,
    val videoPosition: Int = 0,
    val videoCollapsed: Boolean = false,
    val identity: GifDocumentIdentity? = null,
    val identities: Map<String, GifDocumentIdentity> = emptyMap(),
    val photoChromeVisible: Boolean = true,
    val videoChromeVisible: Boolean = true,
    val photoToolsExpanded: Boolean = true,
    val offerExportRecoveryOnEntry: Boolean = true
)

/** Native save state is outside Undo: undoing an edit cannot undo a completed publication. */
internal class GifDocumentCustody(initial: GifDocument, session: GifWorkspaceSession) {
    var current by mutableStateOf(initial)
        private set
    var identity by mutableStateOf(session.identity ?: GifDocumentIdentity(entryDocument = initial))
        private set
    private var identities = session.identities + (identity.ownerId to identity)
    var revision by mutableStateOf(0L)
        private set
    val dirty get() = current != identity.baseline
    val savedName get() = identity.project?.name
    fun token() = WorkspaceOperationToken(identity.ownerId, revision)
    fun matches(token: WorkspaceOperationToken) =
        token.ownerId == identity.ownerId && token.contentRevision == revision
    fun record(document: GifDocument) {
        if (current != document) { current = document; revision++ }
    }
    fun restore(snapshot: GifProjectSnapshot) {
        val next = snapshot.identity ?: GifDocumentIdentity(entryDocument = snapshot.document)
        identity = identities[next.ownerId] ?: next
        identities = identities + (identity.ownerId to identity)
        record(snapshot.document)
    }
    fun fresh(document: GifDocument, project: NamedProjectRef? = null): GifDocumentIdentity =
        GifDocumentIdentity(entryDocument = document, project = project,
            savedDocument = document.takeIf { project != null })
    fun saved(token: WorkspaceOperationToken, project: NamedProjectRef, document: GifDocument): Boolean {
        val prior = identities[token.ownerId] ?: return false
        val updated = prior.copy(project = project, savedDocument = document)
        // Several native work owners may have opened the same named project.
        // Publication advances that project's baseline/version for all of them,
        // while preserving each owner's entry state and current editable content.
        identities = identities.mapValues { (owner, item) ->
            when {
                owner == updated.ownerId -> updated
                item.project?.id == project.id -> item.copy(project = project, savedDocument = document)
                else -> item
            }
        }
        identity = identities.getValue(identity.ownerId)
        return matches(token)
    }
    fun detach(name: String) {
        identities = identities.mapValues { (_, item) ->
            if (item.project?.name == name) item.copy(project = null, savedDocument = null) else item
        }
        identity = identities.getValue(identity.ownerId)
    }
    fun retain(session: GifWorkspaceSession, history: GifUndoStack<GifProjectSnapshot>): GifWorkspaceSession {
        val retainedIds = (history.undo + history.redo).mapNotNull { it.identity?.ownerId }.toSet() + identity.ownerId
        identities = identities.filterKeys { it in retainedIds }
        return session.copy(identity = identity, identities = identities, savedName = savedName)
    }
}

internal fun GifDocument.resetGifEdits(): GifDocument? {
    val clip = video ?: return GifDocument(frames = frames)
    val sourceClip = clip.copy(startMs = 0, endMs = clip.durationMs, fps = 0,
        speedPercent = 100, reverse = false)
    if (!GifVideoMath.valid(sourceClip)) return null
    return GifDocument(video = sourceClip, forever = sourceClip.loopCount == 0,
        repeatCount = sourceClip.loopCount.takeIf { it > 0 })
}

internal class GifWorkDeletion(
    private val custody: GifDocumentCustody,
    private val export: GifExportOperation
) {
    var target by mutableStateOf<WorkspaceOperationToken?>(null)
        private set
    var working by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun request() {
        if (target != null || export.deleteUnavailableReason(custody.identity.ownerId) != null) return
        error = null
        target = custody.token()
    }

    fun cancel() {
        if (!working) target = null
    }

    suspend fun confirm(expected: WorkspaceOperationToken, onDeleted: () -> Unit) {
        if (working || target != expected) return
        if (!custody.matches(expected)) {
            error = "The GIF work changed. Cancel and choose Delete again."
            return
        }
        working = true
        try {
            when {
                !export.stopForNativeDelete(expected.ownerId) ->
                    error = export.deleteUnavailableReason(expected.ownerId)
                        ?: "Could not update local GIF recovery. Try Delete again."
                !custody.matches(expected) ->
                    error = "The GIF work changed. Cancel and choose Delete again."
                else -> {
                    target = null
                    onDeleted()
                }
            }
        } finally {
            working = false
        }
    }
}

/** A replacement's discard stays provisional until its target is usable. */
internal class GifDepartureGate(private val custody: GifDocumentCustody) {
    private data class Request(val token: WorkspaceOperationToken, val action: () -> Unit, val discard: Boolean = false)
    private var request by mutableStateOf<Request?>(null)
    var dialogOpen by mutableStateOf(false)
        private set
    val pending get() = request != null
    fun request(action: () -> Unit) {
        if (request != null) return
        request = Request(custody.token(), action)
        if (custody.dirty) dialogOpen = true else action()
    }
    fun discard() {
        val prior = request ?: return
        request = prior.copy(discard = true)
        dialogOpen = false
        prior.action()
    }
    fun beginSave() { dialogOpen = false }
    fun saved(token: WorkspaceOperationToken) {
        val next = request ?: return
        if (next.token.operationId == token.operationId && custody.matches(token)) next.action()
        else cancel()
    }
    fun saveToken(): WorkspaceOperationToken = request?.token ?: custody.token()
    fun cancel() { request = null; dialogOpen = false }
    fun complete(discard: () -> Boolean, action: () -> Unit): Boolean {
        val pending = request ?: return false
        if (!custody.matches(pending.token)) { cancel(); return false }
        if (pending.discard && !discard()) { cancel(); return false }
        cancel()
        action()
        return true
    }
}

internal enum class GifMediaPickKind { VIDEO, GIF, PHOTOS }
internal data class GifMediaPickRequest(val token: WorkspaceOperationToken, val kind: GifMediaPickKind)

/** Each request has a distinct registry key, so a late old picker result cannot satisfy a newer request. */
@Composable
internal fun GifMediaPicker(request: GifMediaPickRequest?, onResult: (GifMediaPickRequest, List<Uri>) -> Unit) {
    if (request == null) return
    key(request.token.operationId) {
        val video = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            onResult(request, listOfNotNull(it))
        }
        val gif = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
            onResult(request, listOfNotNull(it))
        }
        val photos = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(GifMath.MAX_FRAMES)) {
            onResult(request, it)
        }
        LaunchedEffect(request.token.operationId) {
            try {
                when (request.kind) {
                    GifMediaPickKind.VIDEO -> video.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                    GifMediaPickKind.GIF -> gif.launch(arrayOf("image/gif"))
                    GifMediaPickKind.PHOTOS -> photos.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            } catch (_: Exception) { onResult(request, emptyList()) }
        }
    }
}

@Composable
fun GifMakerScreen(
    document: GifDocument,
    onDocument: (GifDocument) -> Unit,
    onBack: () -> Unit,
    onSwitch: (WorkspaceMode) -> Unit
) {
    var session by remember { mutableStateOf(GifWorkspaceSession()) }
    GifMakerScreenContent(
        document = document,
        onDocument = onDocument,
        onBack = onBack,
        onSwitch = onSwitch,
        session = session,
        onSession = { session = it }
    )
}

@Composable
internal fun GifMakerScreenContent(
    document: GifDocument,
    onDocument: (GifDocument) -> Unit,
    onBack: () -> Unit,
    onSwitch: (WorkspaceMode) -> Unit,
    session: GifWorkspaceSession,
    onSession: (GifWorkspaceSession) -> Unit
) {
    val context = LocalContext.current
    val recoveredExport = remember(context) {
        PendingExportJournal(context.filesDir).read(ExportWorkspace.GIF)?.takeIf { it.restoreNativeDraft }
    }
    val recovery = remember(recoveredExport) { recoveredExport?.let { PendingGifRecipe.decode(it.recipe) } }
    var restored by remember { mutableStateOf(recovery == null || session.route != null) }
    if (!restored && recovery != null) {
        LaunchedEffect(recovery) {
            val route = if (recovery.document.video == null) GifWorkspaceRoute.PHOTOS else GifWorkspaceRoute.VIDEO
            val pending = requireNotNull(recoveredExport)
            // A display name cannot identify a saved target. Only the exact
            // captured kind/UUID/name/version may restore its Update association.
            val named = recovery.savedProject?.let { expected ->
                withContext(Dispatchers.IO) {
                    GifStore.loadNamed(context, expected.name)?.takeIf { it.project == expected }
                }
            }
            val identity = GifDocumentIdentity(ownerId = pending.nativeOwnerId ?: pending.id,
                entryDocument = named?.content ?: GifDocument(),
                project = named?.project, savedDocument = named?.content)
            onDocument(recovery.document)
            onSession(session.copy(route = route, savedName = named?.project?.name,
                identity = identity, identities = mapOf(identity.ownerId to identity)))
            restored = true
        }
        return
    }

    var photoSequenceOnly by remember {
        mutableStateOf(when (session.route) {
            GifWorkspaceRoute.PHOTOS -> true
            GifWorkspaceRoute.VIDEO -> false
            null -> document.frames.isNotEmpty() && document.video == null
        })
    }
    if (!photoSequenceOnly) {
        VideoGifScreenContent(
            document = document,
            onDocument = onDocument,
            onBack = onBack,
            onSwitch = onSwitch,
            onOpenLegacyPhotoProject = { saved ->
                onDocument(saved)
                photoSequenceOnly = true
            },
            session = session,
            onSession = onSession
        )
        return
    }

    val resolver = context.contentResolver
    val scope = rememberCoroutineScope()
    var tool by remember { mutableStateOf(session.photoTool) }
    var index by remember {
        mutableIntStateOf(session.photoIndex.coerceIn(
            0, document.frames.lastIndex.coerceAtLeast(0)
        ))
    }
    var playing by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var notice by remember { mutableStateOf("") }
    val gifExport = rememberGifExport(video = false, onNotice = { notice = it },
        onSaved = { _, doc ->
            notice = "GIF exported · " + (doc.video?.let { GifVideoMath.frameCount(it) } ?: doc.frames.size) + " frames · no watermark"
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
    var history by remember { mutableStateOf(session.history) }
    val custody = remember { GifDocumentCustody(document, session) }
    val departure = remember { GifDepartureGate(custody) }
    val deletion = remember(custody, gifExport) { GifWorkDeletion(custody, gifExport) }
    val savedName = custody.savedName
    var saveCapture by remember { mutableStateOf<GifSaveCapture?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var chromeVisible by remember { mutableStateOf(session.photoChromeVisible) }
    var toolsExpanded by remember { mutableStateOf(session.photoToolsExpanded) }
    var toolsMenuOpen by remember { mutableStateOf(false) }
    var mediaPick by remember { mutableStateOf<GifMediaPickRequest?>(null) }
    var timingStart by remember { mutableStateOf<GifProjectSnapshot?>(null) }
    var timingLatest by remember { mutableStateOf<GifDocument?>(null) }
    var helpOpen by remember { mutableStateOf(false) }
    var projectsOpen by remember { mutableStateOf(false) }
    var openingProject by remember { mutableStateOf(false) }
    var savedNames by remember { mutableStateOf<List<String>>(emptyList()) }
    val editingAvailable = !openingProject && saveCapture == null && !departure.pending &&
        mediaPick == null && deletion.target == null && !deletion.working
    val enabled = !exporting && editingAvailable
    val deleteUnavailable = when {
        document.frames.isEmpty() && history.undo.isEmpty() && history.redo.isEmpty() && savedName == null ->
            "No active GIF work to delete."
        !editingAvailable -> "Wait for the current operation to finish."
        else -> gifExport.deleteUnavailableReason(custody.identity.ownerId)
    }

    fun snapshot(
        current: GifDocument = custody.current,
        name: String? = custody.savedName
    ) = GifProjectSnapshot(current, name,
        if (current.video == null) GifWorkspaceRoute.PHOTOS else GifWorkspaceRoute.VIDEO, custody.identity)

    fun persistPhotoSession(route: GifWorkspaceRoute = GifWorkspaceRoute.PHOTOS) {
        onSession(custody.retain(session.copy(
            route = route,
            photoTool = tool,
            photoIndex = index.coerceIn(0, document.frames.lastIndex.coerceAtLeast(0)),
            history = history,
            savedName = custody.savedName,
            photoChromeVisible = chromeVisible,
            photoToolsExpanded = toolsExpanded
        ), history))
    }

    fun applySnapshot(next: GifProjectSnapshot) {
        timingStart = null
        timingLatest = null
        custody.restore(next)
        onDocument(next.document)
        index = index.coerceAtMost(next.document.frames.lastIndex).coerceAtLeast(0)
        if (next.route == GifWorkspaceRoute.VIDEO) {
            playing = false
            persistPhotoSession(GifWorkspaceRoute.VIDEO)
            photoSequenceOnly = false
        }
    }

    fun commit(next: GifDocument, nextName: String? = custody.savedName,
               identity: GifDocumentIdentity = custody.identity) {
        val updated = snapshot(next, nextName).copy(identity = identity)
        history = history.checkpoint(snapshot(), updated)
        applySnapshot(updated)
    }

    fun restore(entry: Pair<GifProjectSnapshot, GifUndoStack<GifProjectSnapshot>>?) {
        if (entry == null) return
        playing = false
        history = entry.second
        applySnapshot(entry.first)
    }
    fun add(uris: List<Uri>) {
        val remaining = GifMath.MAX_FRAMES - document.frames.size
        if (remaining <= 0) { notice = "Twenty photos maximum."; return }
        val accepted = uris.take(remaining).map { uri ->
            try {
                resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
                // Some providers only grant temporary URI access.
            }
            uri.toString()
        }
        if (accepted.isNotEmpty()) {
            commit(document.copy(frames = document.frames + accepted))
            notice = if (uris.size > remaining) "Only the first 20 photos were added." else
                "Added " + accepted.size + " photos"
        }
    }

    fun finishPhotoTiming() {
        timingStart?.let { start ->
            history = history.checkpoint(start, snapshot(timingLatest ?: document))
        }
        timingStart = null
        timingLatest = null
    }

    fun discardWorking(): Boolean {
        if (!gifExport.suppressNativeRestore(custody.identity.ownerId)) return false
        history = GifUndoStack()
        applySnapshot(snapshot(custody.identity.baseline))
        return true
    }
    fun beginSave(saveAs: Boolean = false) {
        if (!GifMath.valid(custody.current) || exporting || openingProject || saveCapture != null) return
        playing = false
        finishPhotoTiming()
        saveCapture = GifSaveCapture(departure.saveToken(), custody.current, custody.identity.project, saveAs)
        departure.beginSave()
    }
    fun requestCommand(action: () -> Unit) {
        if (!enabled) return
        playing = false
        finishPhotoTiming()
        departure.request(action)
    }
    fun leave(action: () -> Unit) {
        departure.complete(::discardWorking) {
            playing = false
            persistPhotoSession()
            action()
        }
    }
    fun requestPhotoBack() {
        if (menuExpanded) { menuExpanded = false; return }
        if (toolsMenuOpen) { toolsMenuOpen = false; return }
        if (timingStart != null) { applySnapshot(requireNotNull(timingStart)); return }
        if (exporting) { notice = "Finish or cancel the GIF export before leaving."; return }
        requestCommand { leave(onBack) }
    }
    fun requestSwitch(mode: WorkspaceMode) {
        if (mode != WorkspaceMode.GIF) requestCommand { leave { onSwitch(mode) } }
    }
    BackHandler { requestPhotoBack() }
    fun importPhotos() {
        if (!enabled || mediaPick != null) return
        if (document.frames.size == GifMath.MAX_FRAMES) notice = "Maximum of 20 frames."
        else mediaPick = GifMediaPickRequest(custody.token(), GifMediaPickKind.PHOTOS)
    }
    fun openSource(uri: Uri) {
        openingProject = true
        scope.launch {
            try {
                try { resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                catch (_: SecurityException) { /* Some providers grant only session access. */ }
                val fresh = withContext(Dispatchers.IO) { VideoGifSource.document(context, uri) }
                departure.complete(::discardWorking) {
                    commit(fresh, null, custody.fresh(fresh))
                    persistPhotoSession(GifWorkspaceRoute.VIDEO)
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (failed: Exception) { notice = "Could not open this source. " + failed.message.orEmpty() }
            catch (_: OutOfMemoryError) { notice = "There is not enough memory to open this source." }
            finally { openingProject = false; departure.cancel() }
        }
    }
    fun chooseVideo() { mediaPick = GifMediaPickRequest(departure.saveToken(), GifMediaPickKind.VIDEO) }
    fun chooseGif() { mediaPick = GifMediaPickRequest(departure.saveToken(), GifMediaPickKind.GIF) }
    fun openProjects() { savedNames = GifStore.names(context); projectsOpen = true }
    fun resetAllEdits() {
        if (!enabled || custody.current.frames.isEmpty()) return
        playing = false
        finishPhotoTiming()
        val next = custody.current.resetGifEdits()
        if (next != null && next != custody.current) commit(next)
    }

    fun deleteWork() {
        if (deleteUnavailable != null) return
        playing = false
        finishPhotoTiming()
        deletion.request()
    }

    fun finishDelete() {
        val empty = GifDocument()
        history = GifUndoStack()
        val fresh = GifProjectSnapshot(empty, null, GifWorkspaceRoute.VIDEO, custody.fresh(empty))
        custody.restore(fresh)
        mediaPick = null
        departure.cancel()
        onDocument(empty)
        onSession(custody.retain(GifWorkspaceSession(route = GifWorkspaceRoute.VIDEO,
            offerExportRecoveryOnEntry = false), history))
        photoSequenceOnly = false
    }

    fun startExport() {
        if (gifExport.showRetainedExport()) return
        if (!GifMath.valid(document)) {
            notice = "Choose at least two photos before exporting."
            return
        }
        playing = false
        gifExport.start(PendingGifRecipe(document, savedName, custody.identity.project),
            nativeOwnerId = custody.identity.ownerId)
    }

    LaunchedEffect(document.frames, index, document.fit, document.aspect, document.size) {
        val uri = document.frames.getOrNull(index)
        if (uri == null) { preview = null; return@LaunchedEffect }
        try {
            val frame = withContext(Dispatchers.Default) {
                val source = ImagePipeline.decode(
                    resolver, Uri.parse(uri), document.size.maxEdge * 2, 1_100_000L)
                try {
                    GifEncoder.renderFrame(source, document)
                } finally {
                    if (!source.isRecycled) source.recycle()
                }
            }
            android.util.Log.i("GzU1Process", "GIF photo decoded/rendered index=$index dimensions=${frame.width}x${frame.height}")
            preview = frame
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            android.util.Log.i("GzU1Process", "GIF photo preview failed index=$index ${failure.javaClass.simpleName}")
            preview = null
            playing = false
            notice = "Photo " + (index + 1) + " unavailable. Remove or replace it."
        }
    }
    LaunchedEffect(playing, document.frames, document.delayMs, document.forever) {
        if (!playing || document.frames.size < 2) return@LaunchedEffect
        while (playing) {
            delay(document.delayMs.toLong())
            if (index >= document.frames.lastIndex) {
                if (document.forever) index = 0 else {
                    playing = false
                    break
                }
            } else index++
        }
    }
    LaunchedEffect(notice) {
        if (notice.isNotBlank()) {
            delay(5500L)
            notice = ""
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFF080B10)),
        contentAlignment = Alignment.Center) {
        val deckHeight = maxHeight * .38f
        if (preview != null) {
            Image(bitmap = preview!!.asImageBitmap(),
                contentDescription = "Animated GIF frame preview",
                modifier = Modifier.fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures(onTap = { chromeVisible = !chromeVisible }) }
                    .semantics {
                        customActions = listOf(CustomAccessibilityAction("Show editing controls") {
                            chromeVisible = true; true
                        })
                    }, contentScale = ContentScale.Fit)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Photo sequence", color = EditorWhite, fontSize = 26.sp,
                    fontWeight = FontWeight.Bold)
                Text("Use Options to add photos or open a source or saved project.", color = EditorMuted,
                    fontSize = 13.sp)
                Text("© 2026 Greenezone Software LLC", color = EditorMuted, fontSize = 12.sp)
            }
        }

        if (chromeVisible || document.frames.isEmpty()) Column(Modifier.align(Alignment.TopCenter).fillMaxWidth()
            .background(Color(0xEA121820)).windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))) {
            EditorWorkspaceHeader(
                workspace = WorkspaceMode.GIF,
                commands = listOf(
                    WorkspaceCommand(WorkspaceCommandId.OPEN_MEDIA, "New / Open media…", enabled) {},
                    WorkspaceCommand(WorkspaceCommandId.OPEN_PROJECT, "Open project…", enabled,
                        if (enabled) null else "Finish the current operation first") { openProjects() },
                    WorkspaceCommand(WorkspaceCommandId.SAVE, "Save project", enabled && GifMath.valid(document),
                        if (!GifMath.valid(document)) "Choose at least two photos before saving" else null) { beginSave() },
                    WorkspaceCommand(WorkspaceCommandId.SAVE_AS, "Save project as…", enabled && GifMath.valid(document),
                        if (!GifMath.valid(document)) "Choose at least two photos before saving" else null) { beginSave(true) },
                    WorkspaceCommand(WorkspaceCommandId.EXPORT, "Export GIF…",
                        enabled && (GifMath.valid(document) || gifExport.request != null),
                        if (!GifMath.valid(document) && gifExport.request == null)
                            "Choose at least two photos before exporting." else null, ::startExport),
                    WorkspaceCommand(WorkspaceCommandId.RESET, "Reset all edits", enabled && document.frames.isNotEmpty(),
                        if (document.frames.isEmpty()) "Add photos to reset edits." else null, ::resetAllEdits),
                    WorkspaceCommand(WorkspaceCommandId.DELETE, "Delete", deleteUnavailable == null,
                        deleteUnavailable, ::deleteWork),
                    WorkspaceCommand(WorkspaceCommandId.HELP, "Help") { playing = false; helpOpen = true }
                ),
                mediaChoices = listOf(
                    WorkspaceCommand(WorkspaceCommandId.OPEN_MEDIA, "Open video…", enabled) { requestCommand(::chooseVideo) },
                    WorkspaceCommand(WorkspaceCommandId.OPEN_SECONDARY_MEDIA, "Open GIF…", enabled) { requestCommand(::chooseGif) },
                    WorkspaceCommand(WorkspaceCommandId.ADD_PHOTOS, "Add photos…",
                        enabled && document.frames.size < GifMath.MAX_FRAMES,
                        if (document.frames.size >= GifMath.MAX_FRAMES) "Twenty photos maximum." else null, ::importPhotos)
                ),
                allowWhileBusy = if (deleteUnavailable == null) setOf(WorkspaceCommandId.DELETE) else emptySet(),
                canUndo = enabled && history.undo.isNotEmpty(), canRedo = enabled && history.redo.isNotEmpty(),
                onUndo = { restore(history.undo(snapshot())) }, onRedo = { restore(history.redo(snapshot())) },
                onChooseWorkspace = ::requestSwitch, menuExpanded = menuExpanded,
                onMenuExpandedChange = { menuExpanded = it }, enabled = enabled, onBack = ::requestPhotoBack)
            if (notice.isNotBlank()) Text(notice, color = EditorMuted, fontSize = 12.sp,
                modifier = Modifier.padding(start = 9.dp, bottom = 5.dp), maxLines = 2)
        }

        if (chromeVisible || document.frames.isEmpty()) {
            val hasPhotos = document.frames.isNotEmpty()
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(EditorSurface)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)),
                verticalArrangement = Arrangement.spacedBy(5.dp)) {
                if (hasPhotos) TextButton(enabled = enabled && document.frames.size >= 2,
                    onClick = { playing = !playing }, modifier = Modifier.padding(start = 6.dp)) {
                    Text(if (playing) "Pause" else "▶ Play", color = EditorMint)
                }
                if (hasPhotos && toolsExpanded) Column(Modifier.fillMaxWidth()
                    .heightIn(max = deckHeight).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.horizontalScroll(rememberScrollState())
                        .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        GifTool.entries.forEach { option ->
                            DockChip(option.label, tool == option, enabled = enabled) { tool = option }
                        }
                    }
                    when (tool) {
                        GifTool.FRAMES -> {
                            Row(Modifier.horizontalScroll(rememberScrollState())
                                .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                document.frames.forEachIndexed { n, _ ->
                                    DockChip("Photo " + (n + 1), n == index, enabled = enabled) {
                                        playing = false; index = n
                                    }
                                }
                            }
                            Row(Modifier.horizontalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                TextButton(enabled = index > 0 && enabled, onClick = {
                                    playing = false
                                    commit(document.copy(frames = GifMath.move(document.frames, index, index - 1)))
                                    index--
                                }) { Text("← Earlier", color = EditorWhite) }
                                TextButton(enabled = index < document.frames.lastIndex && enabled,
                                    onClick = {
                                        playing = false
                                        commit(document.copy(frames = GifMath.move(document.frames, index, index + 1)))
                                        index++
                                    }) { Text("Later →", color = EditorWhite) }
                                TextButton(enabled = enabled, onClick = {
                                    playing = false
                                    commit(document.copy(frames = document.frames.filterIndexed { i, _ -> i != index }))
                                }) { Text("Remove photo", color = Color(0xFFFFA2A2)) }
                            }
                        }
                        GifTool.TIMING -> {
                            Text("Frame duration: " + document.delayMs + " ms", color = EditorWhite,
                                modifier = Modifier.padding(start = 16.dp), fontSize = 13.sp)
                            Slider(value = document.delayMs.toFloat(), valueRange = 100f..1500f,
                                enabled = enabled,
                                onValueChange = { next ->
                                    if (timingStart == null) timingStart = snapshot()
                                    val changed = document.copy(
                                        delayMs = (next.toInt() / 10) * 10
                                    )
                                    timingLatest = changed
                                    custody.record(changed)
                                    onDocument(changed)
                                },
                                onValueChangeFinished = ::finishPhotoTiming,
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 14.dp)
                                    .semantics {
                                        contentDescription = "Photo GIF frame duration"
                                    })
                            Row(Modifier.padding(start = 10.dp)) {
                                DockChip("Loop forever", document.forever, enabled = enabled) {
                                    commit(document.copy(forever = true))
                                }
                                DockChip("Play once", !document.forever, enabled = enabled) {
                                    commit(document.copy(forever = false))
                                }
                            }
                        }
                        GifTool.SIZE -> {
                            Row(Modifier.horizontalScroll(rememberScrollState())
                                .padding(horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                GifSize.entries.filter { it != GifSize.SOURCE }.forEach {
                                    DockChip(it.label, document.size == it, enabled = enabled) {
                                        commit(document.copy(size = it))
                                    }
                                }
                            }
                            Row(Modifier.horizontalScroll(rememberScrollState())
                                .padding(horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                GifAspect.entries.filter { it != GifAspect.SOURCE }.forEach {
                                    DockChip(it.label, document.aspect == it, enabled = enabled) {
                                        commit(document.copy(aspect = it))
                                    }
                                }
                            }
                            Row(Modifier.horizontalScroll(rememberScrollState())
                                .padding(horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                GifFit.entries.forEach {
                                    DockChip(it.label, document.fit == it, enabled = enabled) {
                                        commit(document.copy(fit = it))
                                    }
                                }
                            }
                        }

                    }
                }
                Box {
                    WorkspaceToolsBar(expanded = hasPhotos && toolsExpanded,
                        onOpenTools = { toolsMenuOpen = true },
                        onToggleExpanded = { toolsExpanded = !toolsExpanded },
                        target = if (hasPhotos) tool.label else "Add photos to edit",
                        enabled = enabled && hasPhotos)
                    DropdownMenu(expanded = toolsMenuOpen, onDismissRequest = { toolsMenuOpen = false }) {
                        GifTool.entries.forEach { choice ->
                            DropdownMenuItem(text = { Text(choice.label) }, onClick = {
                                tool = choice
                                toolsExpanded = true
                                toolsMenuOpen = false
                            })
                        }
                    }
                }
            }
        }
        if (exporting) {
            Row(Modifier.align(Alignment.Center)
                .background(Color(0xDF20252E), RoundedCornerShape(14.dp))
                .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = EditorMint, modifier = Modifier.padding(end = 12.dp))
                Column {
                    Text(gifExport.progress.ifBlank { "Preparing GIF…" }, color = EditorWhite)
                    TextButton(onClick = gifExport::cancel, enabled = !gifExport.committing) {
                        Text("Cancel export", color = EditorMint)
                    }
                }
            }
        }
    }

    GifMediaPicker(mediaPick) { expected, selected ->
        if (mediaPick?.token?.operationId == expected.token.operationId && custody.matches(expected.token)) {
            mediaPick = null
            if (expected.kind == GifMediaPickKind.PHOTOS) add(selected)
            else if (selected.isEmpty()) departure.cancel()
            else openSource(selected.first())
        }
    }
    GifExportRecovery(gifExport)
    deletion.target?.let { expected ->
        WorkspaceDeleteDialog(
            message = "Delete this GIF work? Your source media and saved projects stay on your phone.",
            onConfirm = { scope.launch { deletion.confirm(expected, ::finishDelete) } },
            onCancel = deletion::cancel,
            busy = deletion.working,
            error = deletion.error
        )
    }
    if (helpOpen) GifHelpDialog { helpOpen = false }
    if (departure.dialogOpen) WorkspaceDepartureDialog(
        message = "Save the current photo GIF project before leaving these edits?",
        saveEnabled = GifMath.valid(custody.current),
        saveDisabledReason = if (GifMath.valid(custody.current)) null else "Choose at least two photos before saving",
        onSave = { beginSave() }, onDiscard = departure::discard, onCancel = departure::cancel)
    saveCapture?.let { capture ->
        GifProjectSaveDialog(capture, onSaved = { project ->
            val currentRevision = custody.saved(capture.token, project, capture.document)
            saveCapture = null
            persistPhotoSession()
            notice = "GIF project saved"
            if (currentRevision) departure.saved(capture.token)
            else { departure.cancel(); notice = "The captured project was saved; newer edits are still here." }
        }, onDismiss = { saveCapture = null; departure.cancel() })
    }
    if (projectsOpen) GifSavedProjectsDialog(
        names = savedNames,
        enabled = enabled,
        onOpen = { name ->
            projectsOpen = false
            requestCommand {
            openingProject = true
            scope.launch {
                try {
                    val saved = withContext(Dispatchers.IO) { GifStore.loadNamed(context, name) }
                    projectsOpen = false
                    if (saved == null) notice = "Could not read the saved project or its source file."
                    else departure.complete(::discardWorking) {
                        playing = false
                        index = 0
                        commit(saved.content, saved.project.name, custody.fresh(saved.content, saved.project))
                        persistPhotoSession(if (saved.content.video == null) GifWorkspaceRoute.PHOTOS else GifWorkspaceRoute.VIDEO)
                        notice = "GIF project opened"
                    }
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (failed: Exception) { notice = "Could not open this GIF project. " + failed.message.orEmpty() }
                finally { openingProject = false; departure.cancel() }
            }
            }
        },
        onIdentifyDelete = { name ->
            withContext(Dispatchers.IO) { GifStore.loadNamed(context, name)?.project }
        },
        onDelete = { expected ->
            val deleted = withContext(Dispatchers.IO) { GifStore.deleteNamed(context, expected) }
            deleted.also {
                if (deleted) {
                    finishPhotoTiming()
                    custody.detach(expected.name)
                    history = history.withoutSavedProject(expected.name)
                    savedNames = GifStore.names(context)
                    persistPhotoSession()
                }
            }
        },
        onDismiss = { projectsOpen = false; departure.cancel() }
    )

}

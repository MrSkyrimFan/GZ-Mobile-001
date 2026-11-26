package com.greenezone.gzphotoeditor.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenezone.gzphotoeditor.editor.AutoEnhanceMath
import com.greenezone.gzphotoeditor.editor.BrushPoint
import com.greenezone.gzphotoeditor.editor.BrushStroke
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.CollageStore
import com.greenezone.gzphotoeditor.editor.CropFrame
import com.greenezone.gzphotoeditor.editor.CollageDocument
import com.greenezone.gzphotoeditor.editor.CollagePhoto
import com.greenezone.gzphotoeditor.editor.ProBackground
import com.greenezone.gzphotoeditor.editor.ProDocument
import com.greenezone.gzphotoeditor.editor.ProImageGeometry
import com.greenezone.gzphotoeditor.editor.ProLayer
import com.greenezone.gzphotoeditor.editor.ProPixelSize
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.CropMath
import com.greenezone.gzphotoeditor.editor.CropRatio
import com.greenezone.gzphotoeditor.editor.InkMath
import com.greenezone.gzphotoeditor.editor.InkStroke
import com.greenezone.gzphotoeditor.editor.ProStrokeSampling
import com.greenezone.gzphotoeditor.editor.CreativeRaster
import com.greenezone.gzphotoeditor.editor.TextOverlay
import com.greenezone.gzphotoeditor.editor.EditHistory
import com.greenezone.gzphotoeditor.editor.ExportWorkspace
import com.greenezone.gzphotoeditor.editor.GeometryMath
import com.greenezone.gzphotoeditor.editor.Grade
import com.greenezone.gzphotoeditor.editor.ImagePipeline
import com.greenezone.gzphotoeditor.editor.LocalMask
import com.greenezone.gzphotoeditor.editor.Look
import com.greenezone.gzphotoeditor.editor.Looks
import com.greenezone.gzphotoeditor.editor.MaskKind
import com.greenezone.gzphotoeditor.editor.NamedProjectRef
import com.greenezone.gzphotoeditor.editor.ProjectSaveIntent
import com.greenezone.gzphotoeditor.editor.ProjectSaveResult
import com.greenezone.gzphotoeditor.editor.ProjectSaveFailure
import com.greenezone.gzphotoeditor.editor.ProStore
import com.greenezone.gzphotoeditor.editor.ProSaveTarget
import com.greenezone.gzphotoeditor.editor.ProjectStore
import com.greenezone.gzphotoeditor.editor.PrivacyEdit
import com.greenezone.gzphotoeditor.editor.PrivacyKind
import com.greenezone.gzphotoeditor.editor.PhotoGeometry
import com.greenezone.gzphotoeditor.editor.PendingExportJournal
import com.greenezone.gzphotoeditor.editor.PendingImageRecipe
import com.greenezone.gzphotoeditor.editor.PendingProExportRecipe
import com.greenezone.gzphotoeditor.editor.PrivacyMath
import com.greenezone.gzphotoeditor.editor.PrivacyRaster
import com.greenezone.gzphotoeditor.editor.SavedLooks
import com.greenezone.gzphotoeditor.editor.SavedPhotoProjectMetadata
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

private data class PhotoReplacement(
    val uri: Uri,
    val edits: EditDocument? = null,
    val projectName: String? = null,
    val projectRef: NamedProjectRef? = null
)

private enum class PhotoDepartureKind { CLOSE, BACK, WORKSPACE, PICK_PHOTO, OPEN_PROJECT, REPLACE_PHOTO, PRO }
private data class PhotoDeparture(
    val token: WorkspaceOperationToken,
    val kind: PhotoDepartureKind,
    val workspace: WorkspaceMode? = null,
    val replacement: PhotoReplacement? = null,
    val discard: Boolean = false
)



/**
 * Image-first editor. The image covers the entire canvas behind lightweight chrome.
 * One tap hides the controls; a second tap restores them. Do not show a vertical
 * adjustment list or constrain the image to a short preview box.
 */
@Composable
fun PhotoEditorScreen(
    initialCollagePhoto: CollagePhoto? = null,
    onDoneCollagePhoto: ((CollagePhoto) -> Unit)? = null,
    onCancelCollagePhoto: (() -> Unit)? = null,
    onRequestCollageWorkspace: ((WorkspaceMode, CollagePhoto?) -> Unit)? = null,
    savePhotoProject: (Context, ProjectSaveIntent, Uri, EditDocument) -> ProjectSaveResult = ProjectStore::saveNamed,
    onDeleteCollageWork: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val resolver = context.contentResolver
    val scope = rememberCoroutineScope()
    // The parent Collage record changes only through Apply or Done. Keep this
    // entry snapshot immutable while the nested editor loads/saves other media.
    val tileEntryBaseline = remember(initialCollagePhoto) { initialCollagePhoto }

    val startupJournal = remember(context) { PendingExportJournal(context.filesDir) }
    val startupWorkspace = remember(startupJournal, tileEntryBaseline) {
        if (tileEntryBaseline == null) startupJournal.latestWorkspace() else null
    }
    val coldExport = remember(startupWorkspace) {
        startupWorkspace?.takeIf { it == ExportWorkspace.PRO || it == ExportWorkspace.COLLAGE }
            ?.let(startupJournal::read)
    }
    val recoveredWorkspace = when (startupWorkspace) {
        ExportWorkspace.PRO -> WorkspaceMode.PRO
        ExportWorkspace.COLLAGE -> WorkspaceMode.COLLAGE
        ExportWorkspace.GIF -> WorkspaceMode.GIF
        else -> WorkspaceMode.PHOTO
    }
    var workspaceNavigation by remember {
        mutableStateOf(WorkspaceNavigation(current = recoveredWorkspace))
    }
    var collageDocument by remember {
        val restored = coldExport?.takeIf { it.workspace == ExportWorkspace.COLLAGE && it.restoreNativeDraft }
            ?.let { PendingImageRecipe.decode(it.recipe) }
            ?.payload?.let(CollageStore::decode)
        mutableStateOf(restored ?: CollageDocument())
    }
    var collageSession by remember {
        val recovered = coldExport?.takeIf { it.workspace == ExportWorkspace.COLLAGE && it.restoreNativeDraft }
        mutableStateOf(if (recovered == null) CollageWorkspaceSession() else CollageWorkspaceSession(
            documentId = recovered.nativeOwnerId ?: recovered.id, entryDocument = CollageDocument()))
    }
    val recoveredPro = remember(coldExport) {
        coldExport?.takeIf { it.workspace == ExportWorkspace.PRO && it.restoreNativeDraft }
            ?.let(PendingProExportRecipe::decode)
    }
    var proDocument by remember { mutableStateOf(recoveredPro?.document ?: ProDocument()) }
    var proSession by remember {
        val restored = recoveredPro?.let {
            ProWorkspaceSession(documentEstablished = true, resetDocument = it.resetDocument,
                entryDocument = ProDocument(), saveTarget = ProSaveTarget(
                    nativeOwnerId = coldExport?.let { pending -> pending.nativeOwnerId ?: pending.id }))
        }
        mutableStateOf(restored ?: ProWorkspaceSession())
    }
    var gifDocument by remember { mutableStateOf(GifDocument()) }
    var gifSession by remember { mutableStateOf(GifWorkspaceSession()) }

    var sourceUri by remember { mutableStateOf<Uri?>(null) }
    var original by remember { mutableStateOf<Bitmap?>(null) }
    var sourceSize by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val previewState = remember { RegularPhotoPreviewState() }
    var liveGesture by remember { mutableStateOf(false) }
    var painting by remember { mutableStateOf(false) }
    var document by remember { mutableStateOf(EditDocument()) }
    var history by remember { mutableStateOf(EditHistory()) }
    var gestureStart by remember { mutableStateOf<EditDocument?>(null) }

    var chromeVisible by remember { mutableStateOf(true) }
    var topChromeHeight by remember { mutableIntStateOf(0) }
    var bottomChromeHeight by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableStateOf(EditTab.LOOKS) }
    var activeLook by remember { mutableStateOf("none") }
    var selectedPrivacy by remember { mutableIntStateOf(0) }
    var compare by remember { mutableStateOf(false) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }
    var notice by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val photoExport = rememberPhotoExportController(startupJournal, restorePending = tileEntryBaseline == null) {
        notice = it
    }
    val exporting by photoExport.inProgress
    var loadingToken by remember { mutableIntStateOf(0) }
    var creativeTools by remember { mutableStateOf(CreativeToolState()) }
    var currentInk by remember { mutableStateOf<InkStroke?>(null) }
    var previousInkErasePoint by remember { mutableStateOf<BrushPoint?>(null) }
    var showTextDialog by remember { mutableStateOf(false) }
    var textDraft by remember { mutableStateOf("") }
    var editingTextIndex by remember { mutableIntStateOf(-1) }
    var baselineUri by remember { mutableStateOf<String?>(null) }
    var baselineDocument by remember { mutableStateOf(EditDocument()) }
    var showRegularExit by remember { mutableStateOf(false) }
    var replacement by remember { mutableStateOf<PhotoReplacement?>(null) }
    var replacementAfterSave by remember { mutableStateOf(false) }
    var projectDeleteRef by remember { mutableStateOf<NamedProjectRef?>(null) }
    var showProHandoff by remember { mutableStateOf(false) }
    var showTileExit by remember { mutableStateOf(false) }
    var closeAfterProjectSave by remember { mutableStateOf(false) }

    var photoOwnerId by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var contentRevision by remember { mutableStateOf(0L) }
    var activePhotoProject by remember { mutableStateOf<NamedProjectRef?>(null) }
    var pendingDeparture by remember { mutableStateOf<PhotoDeparture?>(null) }
    var photoPickerToken by remember { mutableStateOf<WorkspaceOperationToken?>(null) }
    var optionsExpanded by remember { mutableStateOf(false) }
    var showPhotoHelp by remember { mutableStateOf(false) }
    var showPhotoReset by remember { mutableStateOf(false) }
    var showPhotoDelete by remember { mutableStateOf(false) }
    var photoDeleteError by remember { mutableStateOf<String?>(null) }
    var photoToolsExpanded by remember { mutableStateOf(true) }
    var photoUiEpoch by remember { mutableStateOf(0L) }
    var photoRetryIntent by remember { mutableStateOf<ProjectSaveIntent?>(null) }
    var photoRetryAllowed by remember { mutableStateOf(true) }
    var proRetryIntent by remember { mutableStateOf<ProjectSaveIntent?>(null) }
    var proRetryAllowed by remember { mutableStateOf(true) }
    var photoSaveAs by remember { mutableStateOf(false) }
    var photoCollision by remember { mutableStateOf<NamedProjectRef?>(null) }
    var tileWorkspace by remember { mutableStateOf<WorkspaceMode?>(null) }
    var proHandoffStage by remember { mutableStateOf("") }
    var proHandoffDiscard by remember { mutableStateOf(false) }
    var proHandoffImport by remember { mutableStateOf(false) }
    var expectedProDocument by remember { mutableStateOf<ProDocument?>(null) }
    var expectedProOwner by remember { mutableStateOf<String?>(null) }
    var proSaveName by remember { mutableStateOf("") }
    var proSaveError by remember { mutableStateOf<String?>(null) }
    var proSaveCollision by remember { mutableStateOf<NamedProjectRef?>(null) }
    var gestureEpoch by remember { mutableStateOf(0L) }
    var gestureViewStart by remember { mutableStateOf<Pair<Int, CreativeToolState>?>(null) }
    var gestureLookStart by remember { mutableStateOf<String?>(null) }
    lateinit var requestWorkspaceChange: (WorkspaceMode) -> Unit
    lateinit var requestDeparture: (PhotoDepartureKind, WorkspaceMode?, PhotoReplacement?) -> Unit
    lateinit var continueDeparture: () -> Unit
    lateinit var performPhotoSave: (ProjectSaveIntent) -> Unit
    var showSaveLook by remember { mutableStateOf(false) }
    var showSaveProject by remember { mutableStateOf(false) }
    var projectSaveError by remember { mutableStateOf<String?>(null) }
    var showProjects by remember { mutableStateOf(false) }
    var draftName by remember { mutableStateOf("") }
    var selectedProject by remember { mutableStateOf<NamedProjectRef?>(null) }
    var savedLooks by remember { mutableStateOf(SavedLooks.load(context)) }
    var savedProjects by remember { mutableStateOf(emptyList<SavedPhotoProjectMetadata>()) }

    fun cancelDraft(): Boolean {
        val start = gestureStart ?: return false
        document = start
        gestureEpoch += 1
        gestureLookStart?.let { activeLook = it }
        gestureViewStart?.let { (privacy, tools) -> selectedPrivacy = privacy; creativeTools = tools }
        gestureViewStart = null
        gestureStart = null
        gestureLookStart = null
        painting = false
        currentInk = null
        previousInkErasePoint = null
        liveGesture = false
        return true
    }
    fun beginGesture() {
        if (gestureStart == null) {
            gestureStart = document
            gestureLookStart = activeLook
            gestureViewStart = selectedPrivacy to creativeTools
        }
    }

    fun commit(next: EditDocument): Boolean {
        if (cancelDraft()) return false
        val updated = PrivacyMath.retainAuthoringGeometry(document, next)
        history = history.checkpoint(document, updated)
        if (document != updated) contentRevision += 1
        document = updated
        gestureStart = null
        liveGesture = false
        return true
    }
    fun slide(next: EditDocument) {
        beginGesture()
        document = PrivacyMath.retainAuthoringGeometry(document, next)
        liveGesture = true
        activeLook = "custom"
    }
    fun finishSlide() {
        gestureStart?.let { beginning ->
            history = history.checkpoint(beginning, document)
            if (beginning != document) contentRevision += 1
        }
        gestureStart = null
        gestureLookStart = null
        gestureViewStart = null
        liveGesture = false
    }
    fun applyLook(key: String, grade: Grade) {
        if (commit(document.copy(grade = grade))) activeLook = key
    }

    fun loadPhoto(
        uri: Uri,
        restored: EditDocument? = null,
        establishBaseline: Boolean = tileEntryBaseline == null,
        restoringExport: Boolean = false,
        projectRef: NamedProjectRef? = null,
        restoredOwnerId: String? = null,
        beforePublish: () -> Boolean = { true },
        onLoaded: () -> Unit = {}
    ) {
        val request = ++loadingToken
        scope.launch {
            busy = true
            if (!restoringExport) notice = "Opening photo…"
            try {
                try {
                    resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: SecurityException) {
                    // Some pickers grant access only for this session.
                }
                val (bitmap, dimensions) = withContext(Dispatchers.IO) {
                    var header: Pair<Int, Int>? = null
                    val preview = ImagePipeline.decode(
                        resolver, uri, ImagePipeline.PREVIEW_MAX_EDGE, 2_000_000L
                    ) { width, height -> header = width to height }
                    preview to checkNotNull(header)
                }
                val initialPreview = try {
                    if (restored != null && restored.privacy.isNotEmpty()) {
                        withContext(Dispatchers.Default) {
                            val job = currentCoroutineContext()
                            ImagePipeline.render(bitmap, restored) { job.ensureActive() }
                        }
                    } else bitmap
                } catch (error: Throwable) {
                    bitmap.recycle()
                    throw error
                }
                if (request != loadingToken) {
                    if (initialPreview !== bitmap) initialPreview.recycle()
                    bitmap.recycle()
                    return@launch
                }
                if (!beforePublish()) {
                    if (initialPreview !== bitmap) initialPreview.recycle()
                    bitmap.recycle()
                    return@launch
                }
                photoOwnerId = restoredOwnerId ?: UUID.randomUUID().toString()
                contentRevision += 1
                activePhotoProject = projectRef
                sourceUri = uri
                original = bitmap
                sourceSize = dimensions
                previewState.processed = initialPreview
                previewState.processedStamp = PreviewStamp(
                    bitmap, restored?.takeIf { it.privacy.isNotEmpty() } ?: EditDocument()
                )
                previewState.beforeImage = bitmap
                previewState.beforeStamp = bitmap to PhotoGeometry.from(EditDocument())
                previewState.filterThumbs = emptyList()
                previewState.quickSource = null
                painting = false
                liveGesture = false
                val loadedDocument = restored ?: EditDocument()
                document = loadedDocument
                if (establishBaseline) {
                    baselineUri = uri.toString()
                    baselineDocument = if (restoringExport) EditDocument() else loadedDocument
                }
                history = EditHistory()
                gestureStart = null
                selectedPrivacy = 0
                selectedTab = EditTab.LOOKS
                creativeTools = CreativeToolState()
                currentInk = null
                previousInkErasePoint = null
                showTextDialog = false
                activeLook = if (restored == null) "none" else "custom"
                chromeVisible = true
                compare = false
                zoom = 1f
                panX = 0f
                panY = 0f
                if (!restoringExport) {
                    notice = if (restored == null) "Media opened" else "Project restored"
                }
                onLoaded()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (request == loadingToken && (!restoringExport || !exporting)) {
                    notice = "Could not open photo: " +
                        (error.message ?: "file missing or permission revoked")
                }
            } finally {
                if (request == loadingToken) {
                    busy = false
                    if (pendingDeparture?.kind in listOf(PhotoDepartureKind.PICK_PHOTO,
                            PhotoDepartureKind.OPEN_PROJECT, PhotoDepartureKind.REPLACE_PHOTO)) {
                        // Decode failure must not leave a continuation waiting for
                        // an unrelated later Save.
                        pendingDeparture = null
                    }
                }
            }
        }
    }

    fun chooseWorkspace(mode: WorkspaceMode) { requestWorkspaceChange(mode) }

    fun leaveWorkspace() {
        workspaceNavigation.commitBack()?.let { workspaceNavigation = it }
    }

    fun finishCollageTile() {
        val originalTile = initialCollagePhoto ?: return
        if (busy || exporting || cancelDraft()) return
        val image = sourceUri?.toString() ?: originalTile.uri
        showTileExit = false
        onDoneCollagePhoto?.invoke(originalTile.copy(uri = image, edits = document))
    }

    fun isPhotoDirty(): Boolean {
        val tileEntry = tileEntryBaseline
        if (tileEntry != null) {
            val currentUri = sourceUri ?: return false
            return currentUri.toString() != tileEntry.uri || document != tileEntry.edits
        }
        return sourceUri?.toString() != baselineUri || document != baselineDocument
    }

    fun requestPhotoReplacement(candidate: PhotoReplacement) {
        requestDeparture(PhotoDepartureKind.REPLACE_PHOTO, null, candidate)
    }

    fun closeCurrentPhoto(recoveryResolved: Boolean = false) {
        if (!recoveryResolved && !photoExport.resolveNativeDraft(photoOwnerId)) {
            showRegularExit = true
            return
        }
        loadingToken += 1
        photoOwnerId = UUID.randomUUID().toString()
        contentRevision += 1
        activePhotoProject = null
        pendingDeparture = null
        sourceUri = null
        original = null
        sourceSize = null
        previewState.processed = null
        previewState.processedStamp = null
        previewState.beforeImage = null
        previewState.beforeStamp = null
        previewState.filterThumbs = emptyList()
        previewState.quickSource = null
        document = EditDocument()
        baselineUri = null
        baselineDocument = EditDocument()
        history = EditHistory()
        gestureStart = null
        liveGesture = false
        painting = false
        currentInk = null
        previousInkErasePoint = null
        chromeVisible = true
        selectedTab = EditTab.LOOKS
        activeLook = "none"
        selectedPrivacy = 0
        compare = false
        zoom = 1f
        panX = 0f
        panY = 0f
        notice = ""
        photoExport.closeDialog()
        showTextDialog = false
        showRegularExit = false
        showTileExit = false
        showPhotoDelete = false
        photoPickerToken = null
        photoUiEpoch += 1
        photoToolsExpanded = true
        closeAfterProjectSave = false
        replacement = null
        replacementAfterSave = false
        selectedProject = null
        creativeTools = CreativeToolState()
    }

    fun requestTileBack() {
        if (busy || exporting) {
            notice = if (exporting) "Finish or cancel the export before leaving this tile."
                else "Wait for the current photo operation to finish."
        } else if (cancelDraft()) {
            return
        } else if (!chromeVisible) {
            chromeVisible = true
        } else if (isPhotoDirty()) {
            tileWorkspace = null
            showTileExit = true
        } else {
            onCancelCollagePhoto?.invoke()
        }
    }

    fun requestRegularBack() {
        requestDeparture(if (workspaceNavigation.previewBack() != null) PhotoDepartureKind.BACK
            else PhotoDepartureKind.CLOSE, null, null)
    }

    fun autoTone() {
        if (cancelDraft()) return
        val bitmap = original ?: return
        scope.launch {
            busy = true
            try {
                val correction = withContext(Dispatchers.Default) {
                    AutoEnhanceMath.autoTone(ImagePipeline.luminanceHistogram(bitmap))
                }
                commit(document.copy(grade = document.grade.copy(
                    exposure = correction.exposure,
                    contrast = correction.contrast,
                    shadows = correction.shadows,
                    highlights = correction.highlights
                )))
                activeLook = "custom"
                notice = "Auto tone applied"
            } catch (error: Exception) {
                notice = "Auto tone failed: " + (error.message ?: "unknown error")
            } finally { busy = false }
        }
    }

    fun autoWB() {
        if (cancelDraft()) return
        val bitmap = original ?: return
        scope.launch {
            busy = true
            try {
                val correction = withContext(Dispatchers.Default) {
                    AutoEnhanceMath.whiteBalance(bitmap)
                }
                commit(document.copy(grade = document.grade.copy(
                    temperature = correction.temperature,
                    tint = correction.tint
                )))
                activeLook = "custom"
                notice = "Auto white balance applied"
            } catch (error: Exception) {
                notice = "Auto WB failed: " + (error.message ?: "unknown error")
            } finally { busy = false }
        }
    }

    fun addPrivacy(kind: PrivacyKind) {
        if (cancelDraft()) return
        if (document.privacy.size >= PrivacyRaster.MAX_EDITS) {
            notice = "Maximum of twelve privacy layers per photo."
            return
        }
        val item = PrivacyEdit(kind = kind, strength = .75f,
            feather = if (kind == PrivacyKind.BLACK_BRUSH) .01f else .22f,
            brushSize = if (kind.usesBrush) .045f else .055f)
        selectedPrivacy = document.privacy.size
        commit(document.copy(privacy = document.privacy + item))
        zoom = 1f
        panX = 0f
        panY = 0f
        notice = if (kind.usesBrush) "Drag on the photo to paint."
            else "Drag the rectangle to position it."
    }

    fun removePrivacy() {
        if (cancelDraft()) return
        if (selectedPrivacy !in document.privacy.indices) return
        val updated = document.privacy.filterIndexed { i, _ -> i != selectedPrivacy }
        commit(document.copy(privacy = updated))
        selectedPrivacy = selectedPrivacy.coerceAtMost(updated.lastIndex).coerceAtLeast(0)
    }

    fun privacyStrokeEdit(clearAll: Boolean) {
        if (cancelDraft()) return
        val mark = document.privacy.getOrNull(selectedPrivacy) ?: return
        if (!mark.kind.usesBrush) return
        val strokes = if (clearAll) emptyList() else mark.strokes.dropLast(1)
        val updated = document.privacy.toMutableList()
        updated[selectedPrivacy] = mark.copy(strokes = strokes)
        commit(document.copy(privacy = updated))
    }

    fun authoringPoint(mark: PrivacyEdit, point: BrushPoint): BrushPoint {
        val input = original ?: return point
        return PrivacyMath.projection(input.width, input.height, document, mark)
            ?.toAuthoring(point) ?: point
    }

    fun movePrivacy(x: Float, y: Float) {
        val mark = document.privacy.getOrNull(selectedPrivacy) ?: return
        if (mark.kind.usesBrush) return
        val updated = document.privacy.toMutableList()
        val point = authoringPoint(mark, BrushPoint(x, y))
        updated[selectedPrivacy] = PrivacyMath.withCenter(mark, point.x, point.y)
        slide(document.copy(privacy = updated))
    }

    fun startPaint(viewPoint: BrushPoint) {
        if (selectedTab != EditTab.PRIVACY) return
        val mark = document.privacy.getOrNull(selectedPrivacy) ?: return
        if (!mark.kind.usesBrush) return
        val point = authoringPoint(mark, viewPoint)
        if (point.x !in 0f..1f || point.y !in 0f..1f) return
        if (mark.strokes.size >= 40 || mark.strokes.sumOf { it.points.size } >= 639) {
            notice = "Brush limit reached. Undo a stroke or add another brush."
            return
        }
        beginGesture()
        painting = true
        val updated = document.privacy.toMutableList()
        updated[selectedPrivacy] = mark.copy(strokes =
            mark.strokes + BrushStroke(listOf(point)))
        document = document.copy(privacy = updated)
    }

    fun continuePaint(viewPoint: BrushPoint) {
        if (!painting || gestureStart == null) return
        if (selectedTab != EditTab.PRIVACY) return
        val mark = document.privacy.getOrNull(selectedPrivacy) ?: return
        val last = mark.strokes.lastOrNull() ?: return
        val pointLimit = 640 - (mark.strokes.sumOf { it.points.size } - last.points.size)
        val point = authoringPoint(mark, viewPoint)
        if (point.x !in 0f..1f || point.y !in 0f..1f) return
        val previous = last.points.last()
        val dx = point.x - previous.x
        val dy = point.y - previous.y
        if (dx * dx + dy * dy < .000012f) return
        val updated = document.privacy.toMutableList()
        val points = if (last.points.size < pointLimit) last.points
            else last.points.filterIndexed { index, _ -> index % 2 == 0 && index < last.points.lastIndex }
        updated[selectedPrivacy] = mark.copy(strokes =
            mark.strokes.dropLast(1) + last.copy(points = points + point))
        document = document.copy(privacy = updated)
    }

    fun finishPaint() {
        if (!painting) return
        painting = false
        finishSlide()
    }

    fun cancelPaint() {
        if (!painting) return
        gestureStart?.let { document = it }
        painting = false
        gestureStart = null
        liveGesture = false
    }

    fun transformPhoto(pinch: Float, translation: Offset, width: Float, height: Float) {
        zoom = (zoom * pinch).coerceIn(1f, 6f)
        val maxX = width * (zoom - 1f) / 2f
        val maxY = height * (zoom - 1f) / 2f
        panX = (panX + translation.x).coerceIn(-maxX, maxX)
        panY = (panY + translation.y).coerceIn(-maxY, maxY)
    }

    var recoveredPhotoHandled by remember(photoExport.initialRecoveryId) { mutableStateOf(false) }
    LaunchedEffect(photoExport.initialRecoveryId, workspaceNavigation.current) {
        if (photoExport.initialRecoveryId == null || workspaceNavigation.current != WorkspaceMode.PHOTO ||
            recoveredPhotoHandled
        ) return@LaunchedEffect
        recoveredPhotoHandled = true
        if (sourceUri != null || !photoExport.nativeRecoveryExpected) return@LaunchedEffect
        val request = photoExport.recoveredRequest
        if (request != null) {
            loadPhoto(request.source, request.edits, restoringExport = true,
                restoredOwnerId = photoExport.recoveredOwnerId)
        } else {
            notice = "The previous export recipe could not be restored."
        }
    }
    fun openPicker() {
        requestDeparture(PhotoDepartureKind.PICK_PHOTO, null, null)
    }
    fun saveLook() {
        if (cancelDraft()) return
        draftName = ""
        showSaveLook = true
    }
    fun saveProject(saveAs: Boolean = false) {
        if (busy || exporting || cancelDraft()) return
        projectSaveError = null
        photoSaveAs = saveAs
        photoCollision = null
        val active = activePhotoProject
        if (!saveAs && active != null) performPhotoSave(ProjectSaveIntent.Update(active))
        else {
            draftName = ""
            showSaveProject = true
        }
    }
    fun openProjects() {
        requestDeparture(PhotoDepartureKind.OPEN_PROJECT, null, null)
    }

    fun applyInkErase(point: BrushPoint) {
        val image = previewState.beforeImage ?: original
        val aspect = image?.let { it.width.toFloat() / it.height } ?: 1f
        val result = InkMath.eraseBounded(document.creative.strokes, point.x, point.y,
            creativeTools.inkThickness, aspect, previousInkErasePoint)
        if (result.capacityExceeded) {
            // A later sample can overflow after earlier valid cuts. Reject the
            // whole gesture before history or persistence can accept a prefix.
            gestureStart?.let { document = it }
            currentInk = null
            previousInkErasePoint = null
            painting = false
            gestureStart = null
            liveGesture = false
            chromeVisible = true
            notice = "Erasing would exceed the drawing limit. Your drawing is unchanged."
        } else {
            if (result.strokes != document.creative.strokes) {
                document = document.copy(creative = document.creative.copy(strokes = result.strokes))
            }
            previousInkErasePoint = point
        }
    }

    fun startInk(point: BrushPoint) {
        if (selectedTab != EditTab.DRAW) return
        if (!creativeTools.erase &&
            document.creative.strokes.size >= CreativeRaster.MAX_INK) {
            notice = "Drawing layer limit reached. Clear some strokes first."
            return
        }
        beginGesture()
        painting = true
        previousInkErasePoint = null
        if (creativeTools.erase) {
            applyInkErase(point)
        } else {
            currentInk = InkStroke(listOf(point), creativeTools.inkColor,
                creativeTools.inkThickness, creativeTools.inkOpacity)
        }
    }

    fun continueInk(point: BrushPoint) {
        if (!painting || selectedTab != EditTab.DRAW) return
        if (creativeTools.erase) {
            applyInkErase(point)
        } else {
            val ink = currentInk ?: return
            val image = previewState.beforeImage ?: original
            val aspect = image?.let { it.width.toFloat() / it.height } ?: 1f
            currentInk = ink.copy(points = ProStrokeSampling.append(ink.points, point, aspect))
        }
    }

    fun finishInk() {
        if (!painting) return
        if (selectedTab == EditTab.DRAW && !creativeTools.erase) {
            val ink = currentInk
            if (ink != null && ink.points.isNotEmpty()) {
                document = document.copy(creative = document.creative.copy(
                    strokes = document.creative.strokes + ink
                ))
            }
        }
        currentInk = null
        previousInkErasePoint = null
        painting = false
        finishSlide()
    }

    fun cancelInk() {
        if (!painting) return
        gestureStart?.let { document = it }
        currentInk = null
        previousInkErasePoint = null
        painting = false
        gestureStart = null
        liveGesture = false
    }

    fun editWords(newLayer: Boolean) {
        if (cancelDraft()) return
        if (!newLayer && document.creative.text.getOrNull(creativeTools.textIndex) == null) return
        editingTextIndex = if (newLayer) -1 else creativeTools.textIndex
        textDraft = if (newLayer) "" else document.creative.text[editingTextIndex].content
        showTextDialog = true
    }

    fun saveWords() {
        val text = textDraft.trim().take(500)
        if (text.isBlank()) return
        val layers = document.creative.text.toMutableList()
        if (editingTextIndex >= 0 && editingTextIndex in layers.indices) {
            layers[editingTextIndex] = layers[editingTextIndex].copy(content = text)
            creativeTools = creativeTools.copy(textIndex = editingTextIndex)
        } else if (layers.size < CreativeRaster.MAX_TEXT) {
            layers.add(TextOverlay(content = text))
            creativeTools = creativeTools.copy(textIndex = layers.lastIndex)
        } else {
            notice = "Maximum number of text objects reached."
            return
        }
        commit(document.copy(creative = document.creative.copy(text = layers)))
        showTextDialog = false
    }

    fun moveCreative(x: Float, y: Float, w: Float, h: Float) {
        if (selectedTab == EditTab.TEXT) {
            val index = creativeTools.textIndex
            val layer = document.creative.text.getOrNull(index) ?: return
            val copy = document.creative.text.toMutableList()
            copy[index] = layer.copy(x = x, y = y, width = w, height = h)
            slide(document.copy(creative = document.creative.copy(text = copy)))
        } else if (selectedTab == EditTab.SHAPES) {
            val index = creativeTools.shapeIndex
            val layer = document.creative.shapes.getOrNull(index) ?: return
            val copy = document.creative.shapes.toMutableList()
            copy[index] = layer.copy(x = x, y = y, width = w, height = h)
            slide(document.copy(creative = document.creative.copy(shapes = copy)))
        }
    }

    fun moveCrop(frame: CropFrame) {
        beginGesture()
        document = PrivacyMath.retainAuthoringGeometry(document, document.copy(
            crop = if (document.crop == CropRatio.ORIGINAL) CropRatio.FREE else document.crop,
            cropFrame = frame,
            cropFrameRatioLocked = document.crop.ratio != null &&
                (document.cropFrameRatioLocked || document.cropFrame == null)
        ))
    }

    fun cancelCrop() {
        gestureStart?.let { document = it }
        gestureStart = null
        liveGesture = false
    }


    fun departureOwned(pending: PhotoDeparture): Boolean =
        pending.token.ownerId == photoOwnerId && pending.token.contentRevision == contentRevision

    fun cancelDeparture() {
        photoUiEpoch += 1
        photoRetryIntent = null
        proRetryIntent = null
        pendingDeparture = null
        photoPickerToken = null
        showRegularExit = false
        showSaveProject = false
        showProjects = false
        photoCollision = null
        showProHandoff = false
        proHandoffStage = ""
        proSaveCollision = null
        expectedProDocument = null
        expectedProOwner = null
    }

    fun commitDiscard(pending: PhotoDeparture, recoveryResolved: Boolean = false): Boolean {
        if (!pending.discard) return true
        if (!recoveryResolved && !photoExport.resolveNativeDraft(photoOwnerId)) {
            showRegularExit = true
            return false
        }
        document = baselineDocument
        history = EditHistory()
        gestureStart = null
        liveGesture = false
        currentInk = null
        previousInkErasePoint = null
        activeLook = "custom"
        contentRevision += 1
        return true
    }

    fun openPreparedPhoto(candidate: PhotoReplacement) {
        val pending = pendingDeparture ?: return
        if (!departureOwned(pending)) { cancelDeparture(); return }
        // loadPhoto publishes only after decode succeeds. A canceled or failed
        // replacement therefore never commits a provisional Discard.
        loadPhoto(candidate.uri, candidate.edits, establishBaseline = true,
            projectRef = candidate.projectRef,
            beforePublish = {
                pendingDeparture == pending && departureOwned(pending) &&
                    (!pending.discard || photoExport.resolveNativeDraft(photoOwnerId))
            }, onLoaded = {
                pendingDeparture = null
                showRegularExit = false
                showProjects = false
            })
    }

    fun finishProHandoff(importPhoto: Boolean) {
        val pending = pendingDeparture ?: return
        if (!departureOwned(pending) || expectedProDocument != proDocument ||
            expectedProOwner != proSession.ownerId) {
            cancelDeparture()
            notice = "The work changed. Start the transfer again."
            return
        }
        val resolvedEdits = if (pending.discard) baselineDocument else document
        val resolvedUri = if (pending.discard) baselineUri?.let(Uri::parse) else sourceUri
        busy = true
        showProHandoff = false
        scope.launch {
            try {
                val prepared = if (importPhoto) {
                    val uri = checkNotNull(resolvedUri)
                    withContext(Dispatchers.IO) {
                        val pixels = ProImageGeometry.sourceSize(resolver, uri)
                        val editedSize = ProImageGeometry.editedSize(pixels, resolvedEdits)
                        val layer = ProImageGeometry.bind(ProLayer(uri = uri.toString(), name = "Original photo"), pixels)
                        val reset = ProDocument(layers = listOf(layer), background = ProBackground.TRANSPARENT,
                            sourceAspectRatio = pixels.aspect, canvasSize = pixels)
                        reset.copy(layers = listOf(ProImageGeometry.bind(layer.copy(edits = resolvedEdits), pixels)),
                            sourceAspectRatio = editedSize.aspect, canvasSize = editedSize) to reset
                    }
                } else ProDocument() to ProDocument()
                if (pendingDeparture != pending || !departureOwned(pending) ||
                    expectedProDocument != proDocument || expectedProOwner != proSession.ownerId) {
                    cancelDeparture()
                    notice = "The work changed. Start the transfer again."
                    return@launch
                }
                val resolvedRecoveries = startupJournal.suppressNativeRestores(buildList {
                    if (pending.discard) add(ExportWorkspace.PHOTO to photoOwnerId)
                    if (proHandoffDiscard) add(ExportWorkspace.PRO to proSession.nativeOwnerKey())
                })
                photoExport.adoptNativeDisposition(resolvedRecoveries)
                if (!commitDiscard(pending, recoveryResolved = true)) return@launch
                proDocument = prepared.first
                proSession = ProWorkspaceSession(documentEstablished = true,
                    resetDocument = prepared.second, entryDocument = prepared.first)
                cancelDeparture()
                workspaceNavigation = workspaceNavigation.open(WorkspaceMode.PRO)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: OutOfMemoryError) {
                cancelDeparture()
                notice = "There is not enough memory to open this photo in Pro. Both workspaces are unchanged."
            } catch (_: Exception) {
                cancelDeparture()
                notice = "Could not open the photo in Pro. Both workspaces are unchanged."
            } finally { busy = false }
        }
    }

    fun resolveProForHandoff(importPhoto: Boolean) {
        proHandoffImport = importPhoto
        if (proSession.isDirty(proDocument)) {
            proHandoffStage = "resolve"
            showProHandoff = true
        } else finishProHandoff(importPhoto)
    }

    fun saveInactivePro(intent: ProjectSaveIntent) {
        if (busy || exporting) return
        val pending = pendingDeparture ?: return
        val captured = proDocument
        val capturedSession = proSession
        val capturedUiEpoch = photoUiEpoch
        if (!departureOwned(pending) || expectedProDocument != captured ||
            expectedProOwner != capturedSession.ownerId) { cancelDeparture(); return }
        busy = true
        proSaveError = null
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                ProStore.saveNamed(context, intent, captured, capturedSession.resetDocument)
            }
            busy = false
            if (proSession.ownerId != capturedSession.ownerId || proDocument != captured) {
                cancelDeparture()
                notice = "The Pro work changed. Its pending departure was canceled."
                return@launch
            }
            when (result) {
                is ProjectSaveResult.Saved -> {
                    proSession = proSession.saved(captured, result.project)
                    if (photoUiEpoch == capturedUiEpoch && pendingDeparture == pending && departureOwned(pending)) finishProHandoff(proHandoffImport)
                }
                is ProjectSaveResult.Exists -> if (photoUiEpoch == capturedUiEpoch) {
                    proSaveCollision = result.project
                    proHandoffStage = "collision"
                }
                is ProjectSaveResult.Rejected -> if (photoUiEpoch == capturedUiEpoch) {
                    proSaveError = "Pro could not be saved (" + result.reason.name.lowercase().replace('_', ' ') +
                        "). Both working documents remain open."
                    if (intent is ProjectSaveIntent.Create) proHandoffStage = "name"
                    else {
                        proRetryIntent = intent
                        proRetryAllowed = result.reason != ProjectSaveFailure.TARGET_CHANGED
                        proHandoffStage = "failure"
                    }
                }
            }
        }
    }

    continueDeparture = continuation@{
        val pending = pendingDeparture ?: return@continuation
        if (!departureOwned(pending)) { cancelDeparture(); return@continuation }
        showRegularExit = false
        when (pending.kind) {
            PhotoDepartureKind.CLOSE -> closeCurrentPhoto()
            PhotoDepartureKind.BACK -> {
                if (!commitDiscard(pending)) return@continuation
                cancelDeparture()
                leaveWorkspace()
            }
            PhotoDepartureKind.WORKSPACE -> {
                val target = pending.workspace ?: return@continuation
                if (!commitDiscard(pending)) return@continuation
                cancelDeparture()
                workspaceNavigation = workspaceNavigation.open(target)
            }
            PhotoDepartureKind.PICK_PHOTO -> photoPickerToken = pending.token
            PhotoDepartureKind.OPEN_PROJECT -> {
                val loadingNotice = "Reading saved projects…"
                notice = loadingNotice
                busy = true
                scope.launch {
                    val projects = withContext(Dispatchers.IO) { ProjectStore.listMetadata(context) }
                    busy = false
                    if (notice == loadingNotice) notice = ""
                    if (pendingDeparture != pending || !departureOwned(pending)) return@launch
                    if (projects == null) {
                        cancelDeparture()
                        notice = "Could not read saved projects. Your photo is unchanged."
                    } else {
                        savedProjects = projects
                        selectedProject = null
                        showProjects = true
                    }
                }
            }
            PhotoDepartureKind.REPLACE_PHOTO -> pending.replacement?.let(::openPreparedPhoto)
            PhotoDepartureKind.PRO -> {
                proHandoffDiscard = false
                expectedProDocument = proDocument
                expectedProOwner = proSession.ownerId
                val hasProWork = proSession.documentEstablished || proDocument != ProDocument() ||
                    proSession.history.undo.isNotEmpty() || proSession.history.redo.isNotEmpty()
                proHandoffStage = if (hasProWork) "choose" else "appearance"
                showProHandoff = true
            }
        }
    }

    requestDeparture = request@{ kind, mode, candidate ->
        if (busy || exporting || pendingDeparture != null || cancelDraft()) return@request
        val pending = PhotoDeparture(WorkspaceOperationToken(photoOwnerId, contentRevision),
            kind, mode, candidate)
        pendingDeparture = pending
        if (isPhotoDirty()) showRegularExit = true else continueDeparture()
    }

    requestWorkspaceChange = changeWorkspace@{ mode ->
        if (!mode.available || busy || exporting) return@changeWorkspace
        if (onDoneCollagePhoto != null) {
            if (onRequestCollageWorkspace == null || cancelDraft()) return@changeWorkspace
            if (isPhotoDirty()) {
                tileWorkspace = mode
                showTileExit = true
            } else onRequestCollageWorkspace(mode, null)
            return@changeWorkspace
        }
        if (mode == workspaceNavigation.current) return@changeWorkspace
        if (workspaceNavigation.current != WorkspaceMode.PHOTO) {
            if (mode == WorkspaceMode.PHOTO && workspaceNavigation.previewBack() == WorkspaceMode.PHOTO) {
                leaveWorkspace()
            } else workspaceNavigation = workspaceNavigation.open(mode)
        } else {
            requestDeparture(if (mode == WorkspaceMode.PRO && original != null) PhotoDepartureKind.PRO
                else PhotoDepartureKind.WORKSPACE, mode, null)
        }
    }

    performPhotoSave = save@{ intent ->
        if (busy || exporting || gestureStart != null) return@save
        val uri = sourceUri ?: return@save
        val capturedDocument = document
        val capturedOwner = photoOwnerId
        val capturedRevision = contentRevision
        val capturedDeparture = pendingDeparture
        val capturedUiEpoch = photoUiEpoch
        projectSaveError = null
        busy = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                savePhotoProject(context, intent, uri, capturedDocument)
            }
            busy = false
            if (photoOwnerId != capturedOwner) return@launch
            when (result) {
                is ProjectSaveResult.Saved -> {
                    activePhotoProject = result.project
                    // This native save baseline is deliberately independent of
                    // tileEntryBaseline: standalone Save does not Apply a tile.
                    baselineUri = uri.toString()
                    baselineDocument = capturedDocument
                    if (photoUiEpoch != capturedUiEpoch) return@launch
                    showSaveProject = false
                    photoCollision = null
                    photoRetryIntent = null
                    notice = "Project saved on your phone"
                    if (capturedDeparture != null && pendingDeparture == capturedDeparture &&
                        capturedRevision == contentRevision && document == capturedDocument) {
                        continueDeparture()
                    } else if (capturedDeparture != null) cancelDeparture()
                }
                is ProjectSaveResult.Exists -> if (photoUiEpoch == capturedUiEpoch) photoCollision = result.project
                is ProjectSaveResult.Rejected -> if (photoUiEpoch == capturedUiEpoch) {
                    projectSaveError = "Could not save project (" +
                        result.reason.name.lowercase().replace('_', ' ') +
                        "). Your edits remain open."
                    if (intent is ProjectSaveIntent.Create) showSaveProject = true
                    else {
                        showSaveProject = false
                        photoRetryIntent = intent
                        photoRetryAllowed = result.reason != ProjectSaveFailure.TARGET_CHANGED
                    }
                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val cancelCurrentDraft by rememberUpdatedState({ cancelDraft() })
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) cancelCurrentDraft()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            cancelCurrentDraft()
        }
    }

    photoPickerToken?.let { launchToken ->
        key(launchToken.operationId) {
            val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                val pending = pendingDeparture
                if (photoPickerToken == launchToken && pending?.token == launchToken &&
                    pending.kind == PhotoDepartureKind.PICK_PHOTO && departureOwned(pending)) {
                    photoPickerToken = null
                    if (uri == null) cancelDeparture() else openPreparedPhoto(PhotoReplacement(uri))
                }
            }
            LaunchedEffect(launchToken.operationId) {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }

    LaunchedEffect(initialCollagePhoto?.uri) {
        val photo = initialCollagePhoto
        if (photo != null) loadPhoto(Uri.parse(photo.uri), photo.edits, establishBaseline = true)
    }

    BackHandler(enabled = onDoneCollagePhoto != null) {
        requestTileBack()
    }
    BackHandler(
        enabled = onDoneCollagePhoto == null &&
            workspaceNavigation.current == WorkspaceMode.PHOTO &&
            (workspaceNavigation.previewBack() != null || original != null || busy || exporting)
    ) {
        if (busy || exporting) {
            notice = if (exporting) "Finish the export before leaving this photo."
                else "Wait for the current photo operation to finish."
        } else {
            if (cancelDraft()) Unit
            else if (!chromeVisible) chromeVisible = true
            else requestRegularBack()
        }
    }

    val editingBlurBrush = selectedTab == EditTab.PRIVACY &&
        document.privacy.getOrNull(selectedPrivacy)?.kind == PrivacyKind.BLUR_BRUSH
    val cropTab = selectedTab == EditTab.CROP
    val previewEdits = if (cropTab) {
        // Canonicalize even ORIGINAL -> ORIGINAL before the first pointer move.
        // moveCrop will retain exactly these anchors, so dragging a fresh cover's
        // crop never invalidates the uncropped preview or disposes its overlay.
        // This temporary recipe does not dirty the document or add history.
        val authoringGeometry = PhotoGeometry.from(document)
        document.copy(
            privacy = document.privacy.map { mark ->
                if (mark.authoringGeometry == null) mark.copy(authoringGeometry = authoringGeometry) else mark
            },
            crop = CropRatio.ORIGINAL, cropFrame = null, cropCenterX = .5f,
            cropCenterY = .5f, cropFrameRatioLocked = false
        )
    } else document
    RenderRegularPhotoPreview(
        state = previewState,
        original = original,
        previewEdits = previewEdits,
        cropTab = cropTab,
        liveGesture = liveGesture,
        painting = painting,
        editingBlurBrush = editingBlurBrush,
        currentDocument = { document },
        onError = { notice = it }
    )

    LaunchedEffect(notice) {
        if (notice.isNotEmpty()) {
            delay(4500L)
            notice = ""
        }
    }

    if (workspaceNavigation.current == WorkspaceMode.PRO) {
        ProWorkspaceScreenContent(
            document = proDocument,
            onDocument = { proDocument = it },
            onBack = ::leaveWorkspace,
            onSwitch = ::chooseWorkspace,
            session = proSession,
            onSession = { proSession = it }
        )
        return
    }
    if (workspaceNavigation.current == WorkspaceMode.GIF) {
        GifMakerScreenContent(
            document = gifDocument,
            onDocument = { gifDocument = it },
            onBack = ::leaveWorkspace,
            onSwitch = ::chooseWorkspace,
            session = gifSession,
            onSession = { gifSession = it }
        )
        return
    }
    if (workspaceNavigation.current == WorkspaceMode.COLLAGE) {
        CollageWorkspaceScreenContent(
            document = collageDocument,
            onDocument = { collageDocument = it },
            onBack = ::leaveWorkspace,
            onSwitch = ::chooseWorkspace,
            session = collageSession,
            onSession = { collageSession = it },
            onTrySwitch = { mode, commitPrepared ->
                if (!mode.available || busy || exporting || workspaceNavigation.current != WorkspaceMode.COLLAGE) false
                else if (!commitPrepared()) false
                else {
                    workspaceNavigation = workspaceNavigation.open(mode)
                    true
                }
            }
        )
        return
    }

    // The last processed image may be a cropped preview from another tool.
    // Only attach handles when the matching uncropped image is actually shown.
    val readyCrop = previewState.cropPreview?.takeIf { cropTab && it.source === original && it.edits == previewEdits }
    val selectedImage = if (compare) previewState.beforeImage ?: original
        else readyCrop?.bitmap ?: previewState.processed ?: original
    val cropEditing = readyCrop != null && !compare && !busy && !exporting && zoom <= 1.001f
    val cropInset = if (cropTab) 16.dp else 0.dp
    val cropImageSize = readyCrop?.bitmap?.let { it.width to it.height } ?: original?.let {
        val (width, height) = if (document.quarterTurns % 2 == 0) it.width to it.height else it.height to it.width
        GeometryMath.safeStraightenCrop(width, height, document.straightenDegrees)
    }
    val textEditing = selectedTab == EditTab.TEXT && !compare && !busy && !exporting &&
        zoom <= 1.001f && document.creative.text.getOrNull(creativeTools.textIndex) != null
    val shapeEditing = selectedTab == EditTab.SHAPES && !compare && !busy && !exporting &&
        zoom <= 1.001f && document.creative.shapes.getOrNull(creativeTools.shapeIndex) != null
    val drawingActive = selectedTab == EditTab.DRAW && !compare && !busy && !exporting &&
        zoom <= 1.001f
    val activePrivacy = document.privacy.getOrNull(selectedPrivacy)
    val brushSelected = !compare && !busy && !exporting &&
        selectedTab == EditTab.PRIVACY && activePrivacy?.kind?.usesBrush == true
    val privacyRegionSelected = !compare && !busy && !exporting &&
        selectedTab == EditTab.PRIVACY && activePrivacy != null &&
        !activePrivacy.kind.usesBrush && zoom <= 1.001f

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF080B10)).clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        if (selectedImage != null) {
            val viewportInsets = when {
                selectedTab != EditTab.CROP -> Modifier
                chromeVisible -> with(LocalDensity.current) {
                    Modifier.padding(top = topChromeHeight.toDp(), bottom = bottomChromeHeight.toDp())
                }
                else -> Modifier.statusBarsPadding().navigationBarsPadding()
            }
            // Fit the crop image, Before image, and gesture coordinates inside the
            // same viewport so visible handles stay clear of the header and dock.
            Box(Modifier.fillMaxSize().then(viewportInsets)) {
                val stableCoordinates = previewState.beforeImage ?: selectedImage
                Image(
                    bitmap = selectedImage.asImageBitmap(),
                    contentDescription = "Photo editing canvas. Tap to hide or show tools.",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                        .padding(cropInset)
                        .semantics {
                            onClick(label = "Hide or show editing controls") {
                                chromeVisible = !chromeVisible
                                true
                            }
                        }
                        .pointerInput(selectedImage) {
                            detectTapGestures(onTap = { chromeVisible = !chromeVisible })
                        }
                        .pointerInput(selectedImage, brushSelected) {
                            detectTransformGestures { _, pan, pinch, _ ->
                                if (!brushSelected && (!cropTab || compare) && !textEditing &&
                                    !shapeEditing && !drawingActive) {
                                    transformPhoto(pinch, pan, size.width.toFloat(), size.height.toFloat())
                                }
                            }
                        }
                        .graphicsLayer {
                            scaleX = zoom
                            scaleY = zoom
                            translationX = panX
                            translationY = panY
                        }
                )
                val privacyProjection = if (brushSelected || privacyRegionSelected) {
                    val input = original
                    if (input != null && activePrivacy != null)
                        PrivacyMath.projection(input.width, input.height, document, activePrivacy)
                    else null
                } else null
                if (brushSelected && activePrivacy != null) {
                    val guideStrokes = if (privacyProjection == null) activePrivacy.strokes
                        else activePrivacy.strokes.map { stroke ->
                            stroke.copy(points = stroke.points.map(privacyProjection::toView))
                        }
                    BrushOverlay(
                        bitmapWidth = stableCoordinates.width,
                        bitmapHeight = stableCoordinates.height,
                        mask = LocalMask(
                            kind = MaskKind.BRUSH,
                            strokes = guideStrokes,
                            radius = activePrivacy.brushSize * (privacyProjection?.radiusScale ?: 1f),
                            feather = activePrivacy.feather
                        ),
                        enabled = !busy,
                        onStart = ::startPaint,
                        onContinue = ::continuePaint,
                        onFinish = ::finishPaint,
                        onTap = { chromeVisible = !chromeVisible },
                        showGuides = !editingBlurBrush,
                        onCancel = ::cancelPaint,
                        zoom = zoom,
                        pan = Offset(panX, panY),
                        onTransform = ::transformPhoto
                    )
                }
                if (privacyRegionSelected && activePrivacy != null) {
                    PrivacyRegionOverlay(
                        imageWidth = stableCoordinates.width,
                        imageHeight = stableCoordinates.height,
                        mark = activePrivacy,
                        projection = privacyProjection,
                        canMove = true,
                        showGuides = true,
                        onMove = ::movePrivacy,
                        onFinished = ::finishSlide,
                        onTap = { chromeVisible = !chromeVisible },
                        onCanceled = { cancelDraft() },
                        targetKey = Triple(photoOwnerId, selectedPrivacy, gestureEpoch)
                    )
                }
                if (cropEditing) {
                    CropFrameOverlay(
                        imageWidth = selectedImage.width,
                        imageHeight = selectedImage.height,
                        frame = CropMath.displayFrame(selectedImage.width, selectedImage.height, document),
                        aspectRatio = document.crop.ratio.takeIf {
                            document.cropFrameRatioLocked || document.cropFrame == null
                        },
                        imageInset = cropInset,
                        showGuides = true,
                        onChange = ::moveCrop,
                        onFinished = ::finishSlide,
                        onCanceled = { cancelDraft() },
                        onTap = { chromeVisible = !chromeVisible },
                        targetKey = photoOwnerId to gestureEpoch
                    )
                }
                if (textEditing) {
                    val layer = document.creative.text.getOrNull(creativeTools.textIndex)
                    if (layer != null) EditableObjectOverlay(
                        imageWidth = stableCoordinates.width,
                        imageHeight = stableCoordinates.height,
                        centerX = layer.x, centerY = layer.y,
                        width = layer.width, height = layer.height,
                        showGuides = true,
                        onChanged = ::moveCreative,
                        onFinished = ::finishSlide,
                        onTap = { chromeVisible = !chromeVisible },
                        onCanceled = { cancelDraft() },
                        targetKey = listOf(photoOwnerId, selectedTab, if (textEditing) creativeTools.textIndex else creativeTools.shapeIndex, gestureEpoch)
                    )
                }
                if (shapeEditing) {
                    val layer = document.creative.shapes.getOrNull(creativeTools.shapeIndex)
                    if (layer != null) EditableObjectOverlay(
                        imageWidth = stableCoordinates.width,
                        imageHeight = stableCoordinates.height,
                        centerX = layer.x, centerY = layer.y,
                        width = layer.width, height = layer.height,
                        showGuides = true,
                        onChanged = ::moveCreative,
                        onFinished = ::finishSlide,
                        onTap = { chromeVisible = !chromeVisible },
                        onCanceled = { cancelDraft() },
                        targetKey = listOf(photoOwnerId, selectedTab, if (textEditing) creativeTools.textIndex else creativeTools.shapeIndex, gestureEpoch)
                    )
                }
                if (drawingActive) {
                    DrawTouchOverlay(
                        imageWidth = stableCoordinates.width,
                        imageHeight = stableCoordinates.height,
                        ink = currentInk,
                        onStart = ::startInk,
                        onMove = ::continueInk,
                        onFinish = ::finishInk,
                        onTap = { chromeVisible = !chromeVisible },
                        onCancel = ::cancelInk
                    )
                }
            }
        } else {
            Text("Choose Options → New photo… to begin.", color = EditorMuted,
                modifier = Modifier.padding(28.dp))
        }

        if (original == null) {
            Text("© 2026 Greenezone Software LLC", color = EditorMuted, fontSize = 10.sp,
                modifier = Modifier.align(Alignment.BottomStart).navigationBarsPadding()
                    .padding(start = 16.dp, bottom = 60.dp))
        }

        if (chromeVisible || original == null) {
            Column(Modifier.align(Alignment.TopCenter).fillMaxWidth()
                .onSizeChanged { topChromeHeight = it.height }
                .background(Brush.verticalGradient(
                    listOf(Color(0xE5090C13), Color(0xC5090C13), Color.Transparent)))
                .statusBarsPadding().padding(horizontal = 7.dp, vertical = 3.dp)) {
                val hasPhoto = original != null && sourceUri != null
                val photoReason = "Open a photo first."
                val child = onDoneCollagePhoto != null
                EditorWorkspaceHeader(
                    workspace = WorkspaceMode.PHOTO,
                    commands = buildList {
                        add(WorkspaceCommand(WorkspaceCommandId.NEW, "New photo…", onInvoke = ::openPicker))
                        add(WorkspaceCommand(WorkspaceCommandId.OPEN_PROJECT,
                            if (child) "Open photo project…" else "Open project…", onInvoke = ::openProjects))
                        add(WorkspaceCommand(WorkspaceCommandId.SAVE,
                            if (child) "Save photo project" else "Save project", hasPhoto,
                            if (hasPhoto) null else photoReason, onInvoke = { saveProject() }))
                        add(WorkspaceCommand(WorkspaceCommandId.SAVE_AS,
                            if (child) "Save photo project as…" else "Save project as…", hasPhoto,
                            if (hasPhoto) null else photoReason, onInvoke = { saveProject(true) }))
                        add(WorkspaceCommand(WorkspaceCommandId.EXPORT, "Export image…",
                            hasPhoto && !child, when {
                                child -> "Choose Done and export from the collage."
                                !hasPhoto -> photoReason
                                else -> null
                            }, onInvoke = { photoExport.openDialog() }))
                        add(WorkspaceCommand(WorkspaceCommandId.RESET, "Reset all edits…", hasPhoto,
                            if (hasPhoto) null else photoReason, onInvoke = { showPhotoReset = true }))
                        add(WorkspaceCommand(WorkspaceCommandId.DELETE, "Delete",
                            if (child) onDeleteCollageWork != null else hasPhoto,
                            if (child) "Return to the collage to delete it." else "There is no working photo to delete.",
                            onInvoke = {
                                if (child) onDeleteCollageWork?.invoke() else { photoDeleteError = null; showPhotoDelete = true }
                            }))
                        add(WorkspaceCommand(WorkspaceCommandId.HELP, "Help", onInvoke = { showPhotoHelp = true }))
                    },
                    canUndo = history.undo.isNotEmpty(), canRedo = history.redo.isNotEmpty(),
                    onUndo = {
                        history.undo(document)?.let { (previous, changes) ->
                            document = previous; history = changes
                            contentRevision += 1; activeLook = "custom"
                        }
                    },
                    onRedo = {
                        history.redo(document)?.let { (next, changes) ->
                            document = next; history = changes
                            contentRevision += 1; activeLook = "custom"
                        }
                    },
                    onChooseWorkspace = ::chooseWorkspace,
                    menuExpanded = optionsExpanded, onMenuExpandedChange = { optionsExpanded = it },
                    enabled = !busy && !exporting && gestureStart == null,
                    onBack = if (child) ::requestTileBack else if (hasPhoto ||
                        workspaceNavigation.previewBack() != null) ::requestRegularBack else null,
                    workspaceSelectionEnabled = !child || onRequestCollageWorkspace != null
                )
                if (notice.isNotEmpty()) Text(notice, color = EditorWhite, fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp))
            }

            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .then(if (selectedTab == EditTab.CROP) {
                        Modifier.onSizeChanged { bottomChromeHeight = it.height }
                    } else Modifier)
                    .navigationBarsPadding()
                    .background(Color(0xE5090C13))
            ) {
                if (original != null && photoToolsExpanded) {
                    RegularPhotoDock(
                        state = RegularDockState(
                            tab = selectedTab,
                            document = document,
                            original = original,
                            thumbnails = previewState.filterThumbs,
                            savedLooks = savedLooks,
                            activeLook = activeLook,
                            busy = busy || exporting,
                            privacyIndex = selectedPrivacy,
                            isStandalone = onDoneCollagePhoto == null,
                            showHistogram = false,
                            creativeTool = creativeTools,
                            cropSize = cropImageSize
                        ),
                        actions = RegularDockActions(
                            onTab = {
                                cancelDraft()
                                selectedTab = it
                                if (it == EditTab.PRIVACY || it == EditTab.TEXT || it == EditTab.DRAW ||
                                    it == EditTab.SHAPES || it == EditTab.CROP) {
                                    zoom = 1f
                                    panX = 0f
                                    panY = 0f
                                }
                            },
                            onSelectPrivacy = { cancelDraft(); selectedPrivacy = it },
                            onAddPrivacy = ::addPrivacy,
                            onDeletePrivacy = ::removePrivacy,
                            onUndoPrivacyStroke = { privacyStrokeEdit(false) },
                            onClearPrivacy = { privacyStrokeEdit(true) },
                            onCommit = {
                                if (commit(it)) activeLook = "custom"
                            },
                            onSlide = ::slide,
                            onSlideFinished = ::finishSlide,
                            onSelectLook = ::applyLook,
                            onAutoTone = ::autoTone,
                            onAutoWB = ::autoWB,
                            onSaveLook = ::saveLook,
                            onSaveProject = { saveProject() },
                            onProjects = ::openProjects,
                            onOpen = ::openPicker,
                            onCollage = { chooseWorkspace(WorkspaceMode.COLLAGE) },
                            onCreativeTool = { cancelDraft(); creativeTools = it },
                            onNewText = { editWords(true) },
                            onEditText = { editWords(false) },
                            onHistogram = {},
                            onCommitAndSelect = { next, selected ->
                                if (commit(next)) {
                                    activeLook = "custom"
                                    creativeTools = selected
                                }
                            }
                        )
                    )
                }
                if (original != null) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { compare = !compare }, enabled = !busy && !exporting) {
                            Text(if (compare) "Edited" else "Before", color = EditorWhite)
                        }
                        if (zoom > 1.01f) TextButton(onClick = { zoom = 1f; panX = 0f; panY = 0f }) {
                            Text("Fit canvas", color = EditorMint)
                        }
                        if (onDoneCollagePhoto != null) {
                            TextButton(onClick = { if (!busy && !exporting) {
                                cancelDraft(); onCancelCollagePhoto?.invoke()
                            } }, enabled = !busy && !exporting) { Text("Cancel", color = EditorWhite) }
                            TextButton(onClick = ::finishCollageTile, enabled = !busy && !exporting) {
                                Text("Done", color = EditorMint)
                            }
                        }
                    }
                }
                WorkspaceToolsBar(
                    expanded = photoToolsExpanded && original != null,
                    onOpenTools = { photoToolsExpanded = true },
                    onToggleExpanded = { photoToolsExpanded = !photoToolsExpanded },
                    target = if (original == null) "Open a photo to use tools."
                        else if (onDoneCollagePhoto != null) "Selected collage photo" else "Current photo",
                    enabled = original != null && !busy && !exporting
                )
            }
        }

        if (busy || exporting) {
            Row(
                Modifier.align(Alignment.Center)
                    .background(Color(0xD9212930), RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = EditorMint, strokeWidth = 2.dp
                )
                Text(if (exporting) "Exporting locally…" else "Processing…",
                    color = EditorWhite, fontSize = 13.sp)
            }
        }
    }

    if (onDoneCollagePhoto == null) PhotoExportDialogs(photoExport, sourceUri, sourceSize, document, busy, notice, photoOwnerId)
    if (showPhotoHelp) PhotoHelpDialog { showPhotoHelp = false }
    if (showPhotoDelete) WorkspaceDeleteDialog(
        message = "Clear the current photo and its editing history?",
        onConfirm = {
            if (!busy && !exporting && sourceUri != null) {
                if (photoExport.resolveNativeDraft(photoOwnerId)) closeCurrentPhoto(recoveryResolved = true)
                else photoDeleteError = "Could not clear this photo's recovery record. Current work is still open."
            }
        },
        onCancel = { showPhotoDelete = false; photoDeleteError = null },
        busy = busy || exporting,
        error = photoDeleteError
    )
    if (showPhotoReset) AlertDialog(
        onDismissRequest = { showPhotoReset = false },
        title = { Text("Reset all edits?") },
        text = { Text("Reset edits on the current photo input. The input stays available and you can undo the reset.") },
        confirmButton = { TextButton(onClick = {
            commit(EditDocument()); activeLook = "none"; showPhotoReset = false
        }) { Text("Reset") } },
        dismissButton = { TextButton(onClick = { showPhotoReset = false }) { Text("Cancel") } }
    )
    if (showTextDialog) {
        TextEditDialog(
            content = textDraft,
            onContent = { textDraft = it },
            onSave = ::saveWords,
            onDismiss = { showTextDialog = false }
        )
    }
    if (showSaveLook) {
        SaveNameDialog(
            title = "Save my look",
            description = "Save your color and curve settings for other photos. " +
                "Your original remains unchanged.",
            name = draftName,
            onName = { draftName = it },
            onSave = {
                if (SavedLooks.save(context, draftName, document.grade)) {
                    savedLooks = SavedLooks.load(context)
                    showSaveLook = false
                    notice = "Custom look saved"
                } else notice = "Give the look a name (up to twenty saved)."
            },
            onDismiss = { showSaveLook = false }
        )
    }

    if (showSaveProject && photoCollision == null) {
        SaveNameDialog(
            title = if (onDoneCollagePhoto != null) "Save photo project" else "Save editable project",
            description = "Stores your edits and a local reference to this photo. Keep the original photo available.",
            name = draftName,
            onName = { draftName = it; projectSaveError = null },
            error = projectSaveError,
            onSave = {
                val target = activePhotoProject
                performPhotoSave(if (!photoSaveAs && target != null)
                    ProjectSaveIntent.Update(target) else ProjectSaveIntent.Create(draftName))
            },
            onDismiss = ::cancelDeparture
        )
    }
    photoRetryIntent?.let { retry ->
        AlertDialog(
            onDismissRequest = ::cancelDeparture,
            title = { Text("Project was not saved") },
            text = { Text(projectSaveError ?: "Your working edits remain open.") },
            confirmButton = { Column {
                if (photoRetryAllowed) TextButton(onClick = { performPhotoSave(retry) }, enabled = !busy) {
                    Text("Retry")
                }
                TextButton(onClick = {
                    photoRetryIntent = null; photoSaveAs = true; draftName = ""
                    projectSaveError = null; showSaveProject = true
                }, enabled = !busy) { Text("Save as…") }
            } },
            dismissButton = { TextButton(onClick = ::cancelDeparture) { Text("Cancel") } }
        )
    }
    photoCollision?.let { collision ->
        AlertDialog(
            onDismissRequest = ::cancelDeparture,
            title = { Text("Replace saved project?") },
            text = { Text("Replace “" + collision.name + "” with this photo project? Its original media stays unchanged.") },
            confirmButton = { TextButton(onClick = {
                photoCollision = null
                performPhotoSave(ProjectSaveIntent.Replace(collision))
            }, enabled = !busy) { Text("Replace") } },
            dismissButton = { TextButton(onClick = ::cancelDeparture) { Text("Cancel") } }
        )
    }
    if (showProjects) {
        ProjectsDialog(
            projects = savedProjects,
            selectedProject = selectedProject,
            enabled = !busy,
            onSelect = { if (!busy) selectedProject = it },
            onOpen = {
                val expected = selectedProject
                val captured = pendingDeparture
                val readable = savedProjects.any { it.project == expected && it.readable }
                if (expected != null && readable && captured != null && departureOwned(captured) && !busy) {
                    busy = true
                    scope.launch {
                        val loaded = withContext(Dispatchers.IO) { ProjectStore.loadNamed(context, expected) }
                        busy = false
                        if (pendingDeparture != captured || !departureOwned(captured)) return@launch
                        if (loaded == null) {
                            cancelDeparture()
                            notice = "Could not open that project. Your photo is unchanged."
                        } else {
                            showProjects = false
                            openPreparedPhoto(PhotoReplacement(loaded.content.image, loaded.content.document,
                                loaded.project.name, loaded.project))
                        }
                    }
                }
            },
            onDelete = {
                val expected = selectedProject
                val captured = pendingDeparture
                if (expected != null && captured != null && departureOwned(captured) && !busy) {
                    projectDeleteRef = expected
                    showProjects = false
                }
            },
            onDismiss = ::cancelDeparture
        )
    }
    projectDeleteRef?.let { expected ->
        val closeConfirmation = {
            if (!busy) {
                projectDeleteRef = null
                showProjects = true
            }
        }
        AlertDialog(
            onDismissRequest = closeConfirmation,
            title = { Text("Delete saved project?") },
            text = { Text("Delete “" + expected.name + "” from saved projects. Your original photo stays on your phone.") },
            confirmButton = {
                TextButton(onClick = {
                    val pending = pendingDeparture
                    if (!busy && projectDeleteRef == expected && pending != null && departureOwned(pending)) {
                        val deleteEpoch = photoUiEpoch
                        busy = true
                        scope.launch {
                            val (deleted, refreshed) = withContext(Dispatchers.IO) {
                                val result = ProjectStore.deleteNamed(context, expected)
                                result to ProjectStore.listMetadata(context)
                            }
                            busy = false
                            if (deleted && activePhotoProject?.id == expected.id) activePhotoProject = null
                            if (photoUiEpoch != deleteEpoch || pendingDeparture != pending ||
                                !departureOwned(pending)) return@launch
                            projectDeleteRef = null
                            if (refreshed == null) {
                                cancelDeparture()
                                notice = if (deleted) "Project reference removed. Could not refresh saved projects."
                                    else "Could not read saved projects. Your photo is unchanged."
                            } else {
                                savedProjects = refreshed
                                selectedProject = null
                                showProjects = true
                                notice = if (deleted) "Project reference removed."
                                    else "The saved project changed or could not be deleted."
                            }
                        }
                    }
                }, enabled = !busy) { Text("Delete saved project") }
            },
            dismissButton = {
                TextButton(onClick = closeConfirmation, enabled = !busy) { Text("Cancel") }
            }
        )
    }
    if (showRegularExit) WorkspaceDepartureDialog(
        message = "Save this photo project, discard the unsaved changes, or cancel.",
        onSave = { showRegularExit = false; saveProject() },
        onDiscard = {
            pendingDeparture = pendingDeparture?.copy(discard = true)
            continueDeparture()
        },
        onCancel = ::cancelDeparture
    )
    if (showProHandoff) {
        when (proHandoffStage) {
            "choose" -> AlertDialog(
                onDismissRequest = ::cancelDeparture,
                title = { Text("Existing Pro work") },
                text = { Text("Resume the existing Pro work or start another Pro document.") },
                confirmButton = {
                    Column {
                        TextButton(onClick = {
                            val pending = pendingDeparture
                            if (pending != null && departureOwned(pending) && commitDiscard(pending)) {
                                cancelDeparture()
                                workspaceNavigation = workspaceNavigation.open(WorkspaceMode.PRO)
                            }
                        }) { Text("Resume existing Pro") }
                        TextButton(onClick = { proHandoffStage = "appearance" }) { Text("Start another Pro") }
                    }
                },
                dismissButton = { TextButton(onClick = ::cancelDeparture) { Text("Cancel") } }
            )
            "appearance" -> AlertDialog(
                onDismissRequest = ::cancelDeparture,
                title = { Text("Bring this photo into Pro?") },
                text = { Text(if (pendingDeparture?.discard == true)
                    "Use the saved or entry appearance selected by Discard. The abandoned edits will not be transferred."
                    else "Use this photo with its current Regular edits, or start a blank Pro canvas.") },
                confirmButton = { TextButton(onClick = { resolveProForHandoff(true) }) { Text("Yes, bring photo") } },
                dismissButton = {
                    Column {
                        TextButton(onClick = { resolveProForHandoff(false) }) { Text("No, start blank") }
                        TextButton(onClick = ::cancelDeparture) { Text("Cancel") }
                    }
                }
            )
            "resolve" -> WorkspaceDepartureDialog(
                title = "Unsaved Pro changes",
                message = "Resolve the existing Pro work before replacing it. A canceled or failed transfer keeps both workspaces open.",
                saveEnabled = proDocument.layers.isNotEmpty(),
                saveDisabledReason = if (proDocument.layers.isEmpty())
                    "This empty canvas setup cannot be saved as a project yet." else null,
                discardLabel = if (proDocument.layers.isEmpty()) "Discard setup" else "Discard",
                onSave = {
                    val target = proSession.saveTarget.savedRef
                    if (target != null) saveInactivePro(ProjectSaveIntent.Update(target))
                    else { proSaveName = ""; proSaveError = null; proHandoffStage = "name" }
                },
                onDiscard = { proHandoffDiscard = true; finishProHandoff(proHandoffImport) },
                onCancel = ::cancelDeparture
            )
            "name" -> SaveNameDialog(
                title = "Save Pro project",
                description = "Save the existing Pro document before replacing it.",
                name = proSaveName, onName = { proSaveName = it; proSaveError = null },
                error = proSaveError,
                onSave = { saveInactivePro(ProjectSaveIntent.Create(proSaveName)) },
                onDismiss = ::cancelDeparture
            )
            "failure" -> AlertDialog(
                onDismissRequest = ::cancelDeparture,
                title = { Text("Pro project was not saved") },
                text = { Text(proSaveError ?: "The existing Pro work remains open.") },
                confirmButton = { Column {
                    if (proRetryAllowed) TextButton(onClick = {
                        proRetryIntent?.let(::saveInactivePro)
                    }, enabled = !busy) { Text("Retry") }
                    TextButton(onClick = {
                        proRetryIntent = null; proSaveName = ""; proSaveError = null
                        proHandoffStage = "name"
                    }, enabled = !busy) { Text("Save as…") }
                } },
                dismissButton = { TextButton(onClick = ::cancelDeparture) { Text("Cancel") } }
            )
            "collision" -> proSaveCollision?.let { collision ->
                AlertDialog(
                    onDismissRequest = ::cancelDeparture,
                    title = { Text("Replace saved Pro project?") },
                    text = { Text("Replace “" + collision.name + "” with the existing Pro work?") },
                    confirmButton = { TextButton(onClick = {
                        saveInactivePro(ProjectSaveIntent.Replace(collision))
                    }, enabled = !busy) { Text("Replace") } },
                    dismissButton = { TextButton(onClick = ::cancelDeparture) { Text("Cancel") } }
                )
            }
        }
    }
    if (showTileExit) WorkspaceDepartureDialog(
        title = "Apply tile changes?",
        message = if (tileWorkspace == null) "Apply the changes to this tile or discard them and return to the collage."
            else "Resolve this tile first. The collage will remain open until its own departure is resolved.",
        saveLabel = "Apply to tile",
        discardLabel = "Discard tile changes",
        onSave = {
            val mode = tileWorkspace
            if (mode == null) finishCollageTile()
            else {
                val entry = initialCollagePhoto
                if (entry != null) {
                    showTileExit = false
                    onRequestCollageWorkspace?.invoke(mode,
                        entry.copy(uri = sourceUri?.toString() ?: entry.uri, edits = document))
                }
            }
        },
        onDiscard = {
            showTileExit = false
            val mode = tileWorkspace
            if (mode == null) onCancelCollagePhoto?.invoke()
            else onRequestCollageWorkspace?.invoke(mode, null)
        },
        onCancel = { showTileExit = false; tileWorkspace = null }
    )
}

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

private enum class PhotoDepartureKind { CLOSE, BACK, WORKSPACE, PICK_PHOTO, OPEN_PROJECT, REPLACE_PHOTO }
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
    val nativeStore = remember(context) { NativeWorkStore(context) }
    val needsNativeRestore = remember(nativeStore, tileEntryBaseline) {
        tileEntryBaseline == null && nativeStore.hasRecoveryRecord()
    }
    var nativeReady by remember(context, tileEntryBaseline) { mutableStateOf(!needsNativeRestore) }
    var nativeCatalog by remember(context) { mutableStateOf<NativeWorkCatalog?>(null) }
    var nativeActive by remember(context) { mutableStateOf<NativeWorkLoaded?>(null) }
    var stagedNamedPro by remember(context) { mutableStateOf<PreparedNamedWorkspace?>(null) }
    var nativeOpenError by remember(context) { mutableStateOf<String?>(null) }
    val latestNamedPreparation by rememberUpdatedState(stagedNamedPro)
    DisposableEffect(context) {
        onDispose { latestNamedPreparation?.photo?.discard() }
    }
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
    var switchRequest by remember { mutableStateOf<RegularProSwitchRequest?>(null) }
    var switchName by remember { mutableStateOf<String?>(null) }
    var switchCollision by remember { mutableStateOf<NamedProjectRef?>(null) }
    var switchError by remember { mutableStateOf<String?>(null) }
    var switchBusy by remember { mutableStateOf(false) }
    var switchCommitting by remember { mutableStateOf(false) }
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
    var photoSaveAs by remember { mutableStateOf(false) }
    var photoCollision by remember { mutableStateOf<NamedProjectRef?>(null) }
    var tileWorkspace by remember { mutableStateOf<WorkspaceMode?>(null) }
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

    fun installPreparedPhoto(
        photo: PreparedRegularPhoto,
        uri: Uri,
        restored: EditDocument?,
        establishBaseline: Boolean,
        restoringExport: Boolean = false,
        projectRef: NamedProjectRef? = null,
        restoredOwnerId: String? = null
    ) {
        val bitmap = photo.bitmap
        val dimensions = photo.dimensions
        val initialPreview = photo.preview
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
                val prepared = prepareRegularPhoto(resolver, uri, restored)
                if (request != loadingToken || !beforePublish()) {
                    prepared.discard()
                    return@launch
                }
                installPreparedPhoto(prepared, uri, restored, establishBaseline, restoringExport,
                    projectRef, restoredOwnerId)
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
    LaunchedEffect(photoExport.initialRecoveryId, workspaceNavigation.current, nativeReady) {
        if (!nativeReady || nativeCatalog?.route != null || photoExport.initialRecoveryId == null ||
            workspaceNavigation.current != WorkspaceMode.PHOTO || recoveredPhotoHandled
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
        pendingDeparture = null
        photoPickerToken = null
        showRegularExit = false
        showSaveProject = false
        showProjects = false
        photoCollision = null
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

    fun captureRegularWork(): NativeRegularState? {
        val uri = sourceUri ?: return null
        val prior = nativeActive?.state?.regular?.takeIf { it.ownerId == photoOwnerId && it.uri == uri }
        return NativeRegularState(
            ownerId = photoOwnerId,
            sourceId = prior?.sourceId ?: photoOwnerId,
            sourceRevision = prior?.sourceRevision ?: 0,
            contentRevision = contentRevision,
            uri = uri,
            document = document,
            entry = prior?.entry ?: baselineDocument,
            clean = baselineDocument,
            reset = prior?.reset ?: EditDocument(),
            history = history,
            savedRef = activePhotoProject
        )
    }

    fun captureProWork(): NativeProState? {
        if (!proSession.documentEstablished && proDocument.layers.isEmpty() &&
            proSession.history.undo.isEmpty() && proSession.history.redo.isEmpty()) return null
        return NativeProState(
            ownerId = proSession.ownerId,
            document = proDocument,
            documentEstablished = proSession.documentEstablished,
            contentRevision = proSession.contentRevision,
            sourceRevision = proSession.sourceRevision,
            selectedLayer = proSession.selectedLayer,
            saveTarget = proSession.saveTarget,
            nextWorkId = proSession.nextWorkId,
            entry = proSession.entryDocument,
            reset = proSession.resetDocument,
            baselines = proSession.baselines,
            history = proSession.history
        )
    }

    fun captureNativeWork(mode: NativeWorkMode): NativeWorkState {
        val regular = captureRegularWork()
        val pro = captureProWork()
        val current = nativeActive?.state?.takeIf {
            when (mode) {
                NativeWorkMode.REGULAR -> regular != null && it.regular?.ownerId == regular.ownerId
                NativeWorkMode.PRO -> pro != null && it.pro?.ownerId == pro.ownerId
            }
        }
        val activeRegular = regular?.takeIf { mode == NativeWorkMode.REGULAR || current?.regular?.ownerId == it.ownerId }
        val activePro = pro?.takeIf { mode == NativeWorkMode.PRO || current?.pro?.ownerId == it.ownerId }
        return NativeWorkState(
            id = current?.id ?: activeRegular?.ownerId ?: activePro?.ownerId ?: UUID.randomUUID().toString(),
            mode = mode, regular = activeRegular, pro = activePro, link = current?.link
        )
    }

    suspend fun prepareNativePhoto(state: NativeWorkState): PreparedRegularPhoto? {
        val regular = state.regular ?: return null
        if (regular.uri == sourceUri && original != null) return null
        return prepareRegularPhoto(resolver, regular.uri, regular.document)
    }

    fun applyNativeWork(state: NativeWorkState, prepared: PreparedRegularPhoto?) {
        val regular = state.regular
        if (regular == null) {
            if (sourceUri != null) closeCurrentPhoto(recoveryResolved = true)
        } else {
            if (prepared != null) installPreparedPhoto(prepared, regular.uri, regular.document,
                establishBaseline = true, projectRef = regular.savedRef, restoredOwnerId = regular.ownerId)
            photoOwnerId = regular.ownerId
            contentRevision = regular.contentRevision
            activePhotoProject = regular.savedRef
            document = regular.document
            baselineUri = regular.uri.toString()
            baselineDocument = regular.clean
            history = regular.history
        }
        val pro = state.pro
        proDocument = pro?.document ?: ProDocument()
        proSession = if (pro == null) ProWorkspaceSession() else ProWorkspaceSession(
            documentEstablished = pro.documentEstablished, selectedLayer = pro.selectedLayer,
            history = pro.history, resetDocument = pro.reset, saveTarget = pro.saveTarget,
            nextWorkId = pro.nextWorkId, entryDocument = pro.entry, baselines = pro.baselines,
            ownerId = pro.ownerId, contentRevision = pro.contentRevision, sourceRevision = pro.sourceRevision
        )
    }

    fun clearSwitchRequest() {
        switchRequest = null
        switchName = null
        switchCollision = null
        switchError = null
    }

    fun switchOwned(request: RegularProSwitchRequest): Boolean =
        switchRequest === request && photoOwnerId == request.regularOwner &&
            contentRevision == request.regularRevision && proSession.ownerId == request.proOwner &&
            proSession.contentRevision == request.proRevision &&
            workspaceNavigation.current == request.source.mode.workspace()

    fun launchNativeSwitch(request: RegularProSwitchRequest, bring: Boolean, save: ProjectSaveIntent? = null) {
        if (switchBusy || !switchOwned(request)) return
        switchBusy = true
        switchError = null
        scope.launch {
            var preparedPhoto: PreparedRegularPhoto? = null
            try {
                val catalog = when (val recovered = nativeStore.recover()) {
                    is NativeWorkResult.Ok -> recovered.value
                    is NativeWorkResult.Failed -> {
                        switchError = nativeWorkMessage(recovered.reason)
                        return@launch
                    }
                }
                val staged = if (bring) when (val result = prepareBring(nativeStore, resolver, request.source)) {
                    is NativeWorkResult.Ok -> result.value
                    is NativeWorkResult.Failed -> {
                        switchError = nativeWorkMessage(result.reason)
                        return@launch
                    }
                } else null
                preparedPhoto = staged?.let { prepareNativePhoto(it) }
                val displaced = request.displaced?.let { snapshot ->
                    when (val result = nativeStore.prepare(snapshot)) {
                        is NativeWorkResult.Ok -> result.value
                        is NativeWorkResult.Failed -> {
                            switchError = nativeWorkMessage(result.reason)
                            return@launch
                        }
                    }
                }
                val next = staged?.let { snapshot ->
                    when (val result = nativeStore.prepare(snapshot)) {
                        is NativeWorkResult.Ok -> result.value
                        is NativeWorkResult.Failed -> {
                            switchError = nativeWorkMessage(result.reason)
                            return@launch
                        }
                    }
                }
                if (!switchOwned(request)) return@launch
                if (save != null) {
                    val saved = when (val result = nativeStore.prepare(request.source)) {
                        is NativeWorkResult.Ok -> nativeStore.saveNamed(save, result.value)
                        is NativeWorkResult.Failed -> ProjectSaveResult.Rejected(ProjectSaveFailure.STORAGE_FAILURE)
                    }
                    when (saved) {
                        is ProjectSaveResult.Saved -> {
                            // A completed named publication remains a save even if a
                            // later cancellation prevents the promised route change.
                            if (request.source.mode == NativeWorkMode.REGULAR && photoOwnerId == request.regularOwner) {
                                activePhotoProject = saved.project
                                baselineUri = request.source.regular?.uri?.toString()
                                baselineDocument = requireNotNull(request.source.regular).document
                            } else if (request.source.mode == NativeWorkMode.PRO && proSession.ownerId == request.proOwner) {
                                request.source.pro?.let { proSession = proSession.saved(it.document, saved.project) }
                            }
                        }
                        is ProjectSaveResult.Exists -> {
                            if (switchOwned(request)) switchCollision = saved.project
                            return@launch
                        }
                        is ProjectSaveResult.Rejected -> {
                            if (switchOwned(request)) switchError = "Could not save the project. Current work is still open."
                            return@launch
                        }
                    }
                }
                if (!switchOwned(request)) return@launch
                switchCommitting = true
                val committed = nativeStore.acknowledge(catalog.revision, next, displaced, route = request.destination)
                when (committed) {
                    is NativeWorkResult.Failed -> switchError = nativeWorkMessage(committed.reason)
                    is NativeWorkResult.Ok -> {
                        nativeCatalog = committed.value
                        nativeActive = next?.let { work ->
                            committed.value.active?.let { NativeWorkLoaded(it.ref, work.state) }
                        }
                        if (staged == null) {
                            closeCurrentPhoto(recoveryResolved = true)
                            proDocument = ProDocument()
                            proSession = ProWorkspaceSession()
                        } else {
                            applyNativeWork(staged, preparedPhoto)
                            preparedPhoto = null
                        }
                        clearSwitchRequest()
                        workspaceNavigation = workspaceNavigation.open(request.destination.workspace())
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: OutOfMemoryError) {
                if (switchOwned(request)) switchError = "There is not enough memory to prepare the other workspace."
            } catch (_: Exception) {
                if (switchOwned(request)) switchError = "Could not prepare the other workspace. Current work is still open."
            } finally {
                preparedPhoto?.discard()
                if (switchRequest == null) switchError = null
                switchCommitting = false
                switchBusy = false
            }
        }
    }

    fun requestNativeSwitch(destination: NativeWorkMode) {
        if (!nativeReady || switchBusy || switchRequest != null || busy || exporting ||
            pendingDeparture != null || cancelDraft()) return
        val mode = when (workspaceNavigation.current) {
            WorkspaceMode.PHOTO -> NativeWorkMode.REGULAR
            WorkspaceMode.PRO -> NativeWorkMode.PRO
            else -> return
        }
        if (destination == mode) return
        val source = captureNativeWork(mode)
        val target = captureNativeWork(destination)
        val targetHasWork = if (destination == NativeWorkMode.PRO) target.pro != null else target.regular != null
        val request = RegularProSwitchRequest(source, destination,
            target.takeIf { targetHasWork && it.id != source.id }, photoOwnerId, contentRevision,
            proSession.ownerId, proSession.contentRevision)
        switchRequest = request
        if (!request.hasWork) launchNativeSwitch(request, bring = false)
    }

    suspend fun revokeWorkingOwner(ownerId: String): Boolean {
        val current = nativeActive ?: return true
        val regularOwns = current.state.regular?.ownerId == ownerId
        val proOwns = current.state.pro?.let {
            (it.saveTarget.nativeOwnerId ?: (it.ownerId + "/" + it.saveTarget.workId)) == ownerId
        } == true
        if (!regularOwns && !proOwns) return true
        val catalog = when (val recovered = nativeStore.recover()) {
            is NativeWorkResult.Ok -> recovered.value
            is NativeWorkResult.Failed -> return false
        }
        val currentRef = catalog.active?.ref?.takeIf { it.id == current.ref.id } ?: return false
        val result = nativeStore.delete(catalog.revision, currentRef)
        if (result !is NativeWorkResult.Ok) return false
        nativeCatalog = result.value
        nativeActive = null
        if (regularOwns && current.state.pro != null) {
            proDocument = ProDocument()
            proSession = ProWorkspaceSession()
        } else if (proOwns && current.state.regular != null) {
            closeCurrentPhoto(recoveryResolved = true)
        }
        return true
    }

    fun workingDrafts(mode: NativeWorkMode): List<Pair<String, String>> =
        nativeCatalog?.drafts.orEmpty().filter { mode in it.recoverableModes }.mapIndexed { index, draft ->
            val label = if (mode == NativeWorkMode.REGULAR) "Photo working draft" else "Pro working draft"
            draft.ref.id to (if (index == 0) label else "$label ${index + 1}")
        }

    suspend fun publishPreparedWork(work: NativeWorkPrepared, photo: PreparedRegularPhoto?,
        mode: NativeWorkMode, stillOwned: () -> Boolean,
        displaced: NativeWorkPrepared? = null, resume: NativeWorkRef? = null
    ): Boolean {
        val catalog = when (val recovered = nativeStore.recover()) {
            is NativeWorkResult.Ok -> recovered.value
            is NativeWorkResult.Failed -> {
                nativeOpenError = nativeWorkMessage(recovered.reason)
                return false
            }
        }
        if (!stillOwned()) return false
        return when (val saved = nativeStore.acknowledge(catalog.revision, work, displaced, resume, mode)) {
            is NativeWorkResult.Failed -> {
                nativeOpenError = nativeWorkMessage(saved.reason)
                false
            }
            is NativeWorkResult.Ok -> {
                nativeCatalog = saved.value
                nativeActive = saved.value.active?.let { NativeWorkLoaded(it.ref, work.state) }
                applyNativeWork(work.state, photo)
                workspaceNavigation = workspaceNavigation.open(mode.workspace())
                true
            }
        }
    }

    suspend fun prepareNamedWorkspace(manifest: NativeManifestRef, named: NamedProjectRef): PreparedNamedWorkspace? {
        val recovered = when (val loaded = nativeStore.loadNamed(manifest, named)) {
            is NativeWorkResult.Ok -> loaded.value
            is NativeWorkResult.Failed -> {
                nativeOpenError = nativeWorkMessage(loaded.reason)
                return null
            }
        }
        val prepared = when (val result = nativeStore.prepare(recovered)) {
            is NativeWorkResult.Ok -> result.value
            is NativeWorkResult.Failed -> {
                nativeOpenError = nativeWorkMessage(result.reason)
                return null
            }
        }
        val photo = try { prepareNativePhoto(prepared.state) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: OutOfMemoryError) {
            nativeOpenError = "There is not enough memory to open this saved work."
            return null
        } catch (_: Exception) {
            nativeOpenError = "A source for this saved work is unavailable."
            return null
        }
        return PreparedNamedWorkspace(named, prepared, photo)
    }

    fun openWorkingDraft(id: String, mode: NativeWorkMode, pending: PhotoDeparture? = null) {
        if (busy || switchBusy || switchRequest != null) return
        val selected = nativeCatalog?.drafts?.firstOrNull { it.ref.id == id && mode in it.recoverableModes }
            ?: return
        val current = captureNativeWork(mode)
        val previousOwner = photoOwnerId
        val previousRevision = contentRevision
        val previousPro = proSession
        busy = true
        scope.launch {
            var photo: PreparedRegularPhoto? = null
            try {
                fun ownsRequest() = workspaceNavigation.current == mode.workspace() &&
                    previousOwner == photoOwnerId && previousRevision == contentRevision &&
                    previousPro.ownerId == proSession.ownerId &&
                    previousPro.contentRevision == proSession.contentRevision &&
                    (pending == null || pendingDeparture == pending && departureOwned(pending))
                val loaded = when (val result = nativeStore.load(selected.ref)) {
                    is NativeWorkResult.Ok -> result.value
                    is NativeWorkResult.Failed -> {
                        nativeOpenError = nativeWorkMessage(result.reason)
                        return@launch
                    }
                }
                val prepared = when (val result = nativeStore.prepare(loaded.state.copy(mode = mode))) {
                    is NativeWorkResult.Ok -> result.value
                    is NativeWorkResult.Failed -> {
                        nativeOpenError = nativeWorkMessage(result.reason)
                        return@launch
                    }
                }
                photo = prepareNativePhoto(prepared.state)
                val displaced = if (pending?.discard == true || current.id == selected.ref.id ||
                    current.regular == null && current.pro == null) null
                else when (val result = nativeStore.prepare(current)) {
                    is NativeWorkResult.Ok -> result.value
                    is NativeWorkResult.Failed -> {
                        nativeOpenError = nativeWorkMessage(result.reason)
                        return@launch
                    }
                }
                if (publishPreparedWork(prepared, photo, mode, ::ownsRequest, displaced, selected.ref)) {
                    photo = null
                    cancelDeparture()
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: OutOfMemoryError) {
                nativeOpenError = "There is not enough memory to open this working draft."
            } catch (_: Exception) {
                nativeOpenError = "A source for this working draft is unavailable."
            } finally {
                photo?.discard()
                busy = false
            }
        }
    }

    suspend fun saveCapturedPro(
        intent: ProjectSaveIntent,
        captured: ProDocument,
        session: ProWorkspaceSession
    ): ProjectSaveResult {
        val linked = withContext(Dispatchers.Main.immediate) {
            val previous = nativeActive?.state?.takeIf { it.pro?.ownerId == session.ownerId }
            previous?.let {
                val state = captureNativeWork(NativeWorkMode.PRO)
                state.pro?.takeIf { current ->
                    current.ownerId == session.ownerId && current.contentRevision == session.contentRevision
                }?.let { current ->
                    state.copy(pro = current.copy(document = captured, reset = session.resetDocument,
                        selectedLayer = session.selectedLayer, saveTarget = session.saveTarget,
                        baselines = session.baselines, history = session.history))
                }
            }
        }
        if (linked == null) return withContext(Dispatchers.IO) {
            ProStore.saveNamed(context, intent, captured, session.resetDocument)
        }
        return when (val prepared = nativeStore.prepare(linked)) {
            is NativeWorkResult.Ok -> nativeStore.saveNamed(intent, prepared.value)
            is NativeWorkResult.Failed -> ProjectSaveResult.Rejected(ProjectSaveFailure.STORAGE_FAILURE)
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
        }
    }

    requestDeparture = request@{ kind, mode, candidate ->
        if (busy || exporting || pendingDeparture != null || switchRequest != null || cancelDraft()) return@request
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
        if (mode == WorkspaceMode.PRO && workspaceNavigation.current == WorkspaceMode.PHOTO) {
            requestNativeSwitch(NativeWorkMode.PRO)
        } else if (mode == WorkspaceMode.PHOTO && workspaceNavigation.current == WorkspaceMode.PRO) {
            requestNativeSwitch(NativeWorkMode.REGULAR)
        } else if (workspaceNavigation.current != WorkspaceMode.PHOTO) {
            if (mode == WorkspaceMode.PHOTO && workspaceNavigation.previewBack() == WorkspaceMode.PHOTO) {
                leaveWorkspace()
            } else workspaceNavigation = workspaceNavigation.open(mode)
        } else {
            requestDeparture(PhotoDepartureKind.WORKSPACE, mode, null)
        }
    }

    performPhotoSave = save@{ intent ->
        if (busy || exporting || gestureStart != null || switchRequest != null) return@save
        val uri = sourceUri ?: return@save
        val linked = nativeActive?.state?.takeIf { it.regular?.ownerId == photoOwnerId }
            ?.let { captureNativeWork(NativeWorkMode.REGULAR) }
        val capturedDocument = document
        val capturedOwner = photoOwnerId
        val capturedRevision = contentRevision
        val capturedDeparture = pendingDeparture
        val capturedUiEpoch = photoUiEpoch
        projectSaveError = null
        busy = true
        scope.launch {
            val result = if (linked == null) withContext(Dispatchers.IO) {
                savePhotoProject(context, intent, uri, capturedDocument)
            } else when (val prepared = nativeStore.prepare(linked)) {
                is NativeWorkResult.Ok -> nativeStore.saveNamed(intent, prepared.value)
                is NativeWorkResult.Failed -> ProjectSaveResult.Rejected(ProjectSaveFailure.STORAGE_FAILURE)
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

    LaunchedEffect(nativeStore, needsNativeRestore) {
        if (!needsNativeRestore) return@LaunchedEffect
        var prepared: PreparedRegularPhoto? = null
        try {
            val catalog = when (val restored = nativeStore.recover()) {
                is NativeWorkResult.Ok -> restored.value
                is NativeWorkResult.Failed -> {
                    proDocument = ProDocument()
                    proSession = ProWorkspaceSession()
                    workspaceNavigation = WorkspaceNavigation()
                    nativeOpenError = nativeWorkMessage(restored.reason)
                    return@LaunchedEffect
                }
            }
            nativeCatalog = catalog
            val route = catalog.route
            val active = catalog.active
            if (active == null) {
                if (route != null) {
                    proDocument = ProDocument()
                    proSession = ProWorkspaceSession()
                    closeCurrentPhoto(recoveryResolved = true)
                    workspaceNavigation = WorkspaceNavigation(current = route.workspace())
                }
                return@LaunchedEffect
            }
            when (val loaded = nativeStore.load(active.ref)) {
                is NativeWorkResult.Failed -> {
                    proDocument = ProDocument()
                    proSession = ProWorkspaceSession()
                    workspaceNavigation = WorkspaceNavigation(current = (route ?: active.mode).workspace())
                    nativeOpenError = nativeWorkMessage(loaded.reason)
                }
                is NativeWorkResult.Ok -> {
                    prepared = prepareNativePhoto(loaded.value.state)
                    applyNativeWork(loaded.value.state, prepared)
                    prepared = null
                    nativeActive = loaded.value
                    workspaceNavigation = WorkspaceNavigation(current = (route ?: loaded.value.state.mode).workspace())
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: OutOfMemoryError) {
            proDocument = ProDocument()
            proSession = ProWorkspaceSession()
            nativeOpenError = "There is not enough memory to restore the previous editing session."
        } catch (_: Exception) {
            proDocument = ProDocument()
            proSession = ProWorkspaceSession()
            nativeOpenError = "The previous editing session needs its source media before it can reopen."
        } finally {
            prepared?.discard()
            nativeReady = true
        }
    }

    LaunchedEffect(nativeActive?.ref?.id, workspaceNavigation.current, contentRevision,
        proSession.contentRevision, activePhotoProject, proSession.saveTarget.savedRef, switchBusy) {
        val previous = nativeActive ?: return@LaunchedEffect
        if (!nativeReady || switchRequest != null || switchBusy || busy || exporting ||
            liveGesture || painting) return@LaunchedEffect
        val mode = when (workspaceNavigation.current) {
            WorkspaceMode.PHOTO -> NativeWorkMode.REGULAR
            WorkspaceMode.PRO -> NativeWorkMode.PRO
            else -> return@LaunchedEffect
        }
        val current = captureNativeWork(mode)
        if (current.id != previous.ref.id || previous.state == current) return@LaunchedEffect
        val expectedRegular = contentRevision
        val expectedPro = proSession.contentRevision
        val catalog = when (val loaded = nativeStore.recover()) {
            is NativeWorkResult.Ok -> loaded.value
            is NativeWorkResult.Failed -> { notice = nativeWorkMessage(loaded.reason); return@LaunchedEffect }
        }
        if (catalog.active?.ref?.id != current.id) return@LaunchedEffect
        val prepared = when (val result = nativeStore.prepare(current)) {
            is NativeWorkResult.Ok -> result.value
            is NativeWorkResult.Failed -> {
                notice = nativeWorkMessage(result.reason)
                return@LaunchedEffect
            }
        }
        if (switchRequest != null || expectedRegular != contentRevision ||
            expectedPro != proSession.contentRevision || current.mode.workspace() != workspaceNavigation.current
        ) return@LaunchedEffect
        when (val result = nativeStore.acknowledge(catalog.revision, prepared)) {
            is NativeWorkResult.Ok -> {
                nativeCatalog = result.value
                nativeActive = result.value.active?.let { NativeWorkLoaded(it.ref, prepared.state) }
            }
            is NativeWorkResult.Failed -> if (result.reason != NativeWorkFailure.STALE) {
                notice = nativeWorkMessage(result.reason)
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

    if (!nativeReady) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Restoring work…", color = EditorWhite)
        }
        return
    }

    switchRequest?.takeIf { it.hasWork || switchError != null }?.let { request ->
        val proCanSave = request.source.pro?.document?.layers?.isNotEmpty() == true
        val proUnavailable = if (request.source.mode == NativeWorkMode.PRO && !proCanSave)
            "An empty Pro canvas cannot be saved or brought to Regular." else null
        WorkspaceTransferDialog(
            state = WorkspaceTransferPresentation(
                destination = request.destination.workspace(),
                saveUnavailable = if (request.hasWork) proUnavailable else "No working media to save.",
                bringUnavailable = if (request.hasWork) proUnavailable else "No working media to bring.",
                name = switchName, collisionName = switchCollision?.name, error = switchError,
                working = switchBusy, cancelEnabled = !switchCommitting
            ),
            onDiscard = { launchNativeSwitch(request, bring = false) },
            onSave = {
                val named = if (request.source.mode == NativeWorkMode.REGULAR)
                    activePhotoProject else proSession.saveTarget.savedRef
                val name = switchName
                if (named != null) launchNativeSwitch(request, bring = false, save = ProjectSaveIntent.Update(named))
                else if (name == null) switchName = ""
                else if (name.isBlank()) switchError = "Enter a project name."
                else {
                    switchCollision = null
                    launchNativeSwitch(request, bring = false, save = ProjectSaveIntent.Create(name))
                }
            },
            onBring = { launchNativeSwitch(request, bring = true) },
            onCancel = { if (!switchCommitting) clearSwitchRequest() },
            onNameChange = { switchName = it; switchCollision = null; switchError = null },
            onReplace = { switchCollision?.let {
                launchNativeSwitch(request, bring = false, save = ProjectSaveIntent.Replace(it))
            } }
        )
    }

    nativeOpenError?.let { message ->
        AlertDialog(onDismissRequest = { nativeOpenError = null },
            title = { Text("Could not open the work") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { nativeOpenError = null }) { Text("OK") } })
    }

    if (workspaceNavigation.current == WorkspaceMode.PRO) {
        ProWorkspaceScreenContent(
            document = proDocument,
            onDocument = { proDocument = it },
            onBack = ::leaveWorkspace,
            onSwitch = ::chooseWorkspace,
            session = proSession,
            onSession = { proSession = it },
            onRequestRegularTransfer = { captured, session ->
                proDocument = captured
                proSession = session
                requestNativeSwitch(NativeWorkMode.REGULAR)
            },
            onRevokeWorkingSession = ::revokeWorkingOwner,
            saveCapturedProject = ::saveCapturedPro,
            workingDrafts = workingDrafts(NativeWorkMode.PRO),
            onOpenWorkingDraft = { id, captured, session ->
                proDocument = captured
                proSession = session
                openWorkingDraft(id, NativeWorkMode.PRO)
            },
            prepareOpenedNamedProject = { loaded ->
                stagedNamedPro?.photo?.discard()
                stagedNamedPro = loaded.content.linkedWork?.let { prepareNamedWorkspace(it, loaded.project) }
                loaded.content.linkedWork == null || stagedNamedPro != null
            },
            onOpenedNamedProject = { loaded, session ->
                val staged = stagedNamedPro?.takeIf { it.ref == loaded.project }
                stagedNamedPro = null
                if (staged != null) scope.launch {
                    var photo = staged.photo
                    try {
                        if (publishPreparedWork(staged.work, photo, NativeWorkMode.PRO, stillOwned = {
                                workspaceNavigation.current == WorkspaceMode.PRO &&
                                    proSession.ownerId == session.ownerId && proSession.saveTarget.savedRef == staged.ref
                            })) photo = null
                    } finally { photo?.discard() }
                }
            }
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
                val owner = photoOwnerId
                if (nativeActive?.state?.regular?.ownerId == owner) {
                    busy = true
                    scope.launch {
                        try {
                            if (revokeWorkingOwner(owner)) closeCurrentPhoto(recoveryResolved = true)
                            else photoDeleteError = "Could not clear the working session. Current work is still open."
                        } finally { busy = false }
                    }
                } else if (photoExport.resolveNativeDraft(owner)) closeCurrentPhoto(recoveryResolved = true)
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
                            val manifest = loaded.content.linkedWork
                            if (manifest == null) {
                                showProjects = false
                                openPreparedPhoto(PhotoReplacement(loaded.content.image, loaded.content.document,
                                    loaded.project.name, loaded.project))
                            } else {
                                var photo: PreparedRegularPhoto? = null
                                busy = true
                                try {
                                    val staged = prepareNamedWorkspace(manifest, loaded.project)
                                    photo = staged?.photo
                                    if (staged != null && publishPreparedWork(staged.work, photo, NativeWorkMode.REGULAR,
                                            stillOwned = { pendingDeparture == captured && departureOwned(captured) })) {
                                        photo = null
                                        cancelDeparture()
                                    }
                                } finally { photo?.discard(); busy = false }
                            }
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
            onDismiss = ::cancelDeparture,
            workingDrafts = if (onDoneCollagePhoto == null) workingDrafts(NativeWorkMode.REGULAR) else emptyList(),
            onOpenWorkingDraft = { id ->
                pendingDeparture?.takeIf { departureOwned(it) }?.let {
                    openWorkingDraft(id, NativeWorkMode.REGULAR, it)
                }
            }
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

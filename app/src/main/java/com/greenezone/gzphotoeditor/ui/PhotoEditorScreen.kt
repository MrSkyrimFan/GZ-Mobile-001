package com.greenezone.gzphotoeditor.ui

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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt

private data class PhotoReplacement(
    val uri: Uri,
    val edits: EditDocument? = null,
    val projectName: String? = null
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
    onCancelCollagePhoto: (() -> Unit)? = null
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
        val restored = coldExport?.takeIf { it.workspace == ExportWorkspace.COLLAGE }
            ?.let { PendingImageRecipe.decode(it.recipe) }
            ?.payload?.let(CollageStore::decode)
        mutableStateOf(restored ?: CollageDocument())
    }
    var collageSession by remember { mutableStateOf(CollageWorkspaceSession()) }
    val recoveredPro = remember(coldExport) {
        coldExport?.takeIf { it.workspace == ExportWorkspace.PRO }
            ?.let(PendingProExportRecipe::decode)
    }
    var proDocument by remember { mutableStateOf(recoveredPro?.document ?: ProDocument()) }
    var proSession by remember {
        val restored = recoveredPro?.let {
            ProWorkspaceSession(documentEstablished = true, resetDocument = it.resetDocument)
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
    var showTextDialog by remember { mutableStateOf(false) }
    var textDraft by remember { mutableStateOf("") }
    var editingTextIndex by remember { mutableIntStateOf(-1) }
    var baselineUri by remember { mutableStateOf<String?>(null) }
    var baselineDocument by remember { mutableStateOf(EditDocument()) }
    var showRegularExit by remember { mutableStateOf(false) }
    var replacement by remember { mutableStateOf<PhotoReplacement?>(null) }
    var replacementAfterSave by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<String?>(null) }
    var showProHandoff by remember { mutableStateOf(false) }
    var showTileExit by remember { mutableStateOf(false) }
    var closeAfterProjectSave by remember { mutableStateOf(false) }

    var showHistogram by remember { mutableStateOf(false) }
    var showSaveLook by remember { mutableStateOf(false) }
    var showSaveProject by remember { mutableStateOf(false) }
    var projectSaveError by remember { mutableStateOf<String?>(null) }
    var showProjects by remember { mutableStateOf(false) }
    var draftName by remember { mutableStateOf("") }
    var selectedProject by remember { mutableStateOf<String?>(null) }
    var savedLooks by remember { mutableStateOf(SavedLooks.load(context)) }
    var savedProjects by remember { mutableStateOf(ProjectStore.load(context)) }

    fun commit(next: EditDocument) {
        val updated = PrivacyMath.retainAuthoringGeometry(document, next)
        history = history.checkpoint(document, updated)
        document = updated
        gestureStart = null
        liveGesture = false
    }
    fun slide(next: EditDocument) {
        if (gestureStart == null) gestureStart = document
        document = PrivacyMath.retainAuthoringGeometry(document, next)
        liveGesture = true
        activeLook = "custom"
    }
    fun finishSlide() {
        gestureStart?.let { beginning ->
            history = history.checkpoint(beginning, document)
        }
        gestureStart = null
        liveGesture = false
    }
    fun applyLook(key: String, grade: Grade) {
        commit(document.copy(grade = grade))
        activeLook = key
    }

    fun loadPhoto(
        uri: Uri,
        restored: EditDocument? = null,
        establishBaseline: Boolean = tileEntryBaseline == null,
        restoringExport: Boolean = false
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
                    baselineDocument = loadedDocument
                }
                history = EditHistory()
                gestureStart = null
                selectedPrivacy = 0
                selectedTab = EditTab.LOOKS
                creativeTools = CreativeToolState()
                currentInk = null
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
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (request == loadingToken && (!restoringExport || !exporting)) {
                    notice = "Could not open photo: " +
                        (error.message ?: "file missing or permission revoked")
                }
            } finally {
                if (request == loadingToken) busy = false
            }
        }
    }

    fun chooseWorkspace(mode: WorkspaceMode) {
        if (onDoneCollagePhoto != null || busy || exporting) return
        if (mode == WorkspaceMode.VIDEO) {
            notice = "Video editing is coming later, after the photo and GIF tools."
            return
        }
        if (mode == WorkspaceMode.PHOTO && workspaceNavigation.current == WorkspaceMode.PRO &&
            workspaceNavigation.previewBack() == WorkspaceMode.PHOTO) {
            workspaceNavigation.commitBack()?.let { workspaceNavigation = it }
            return
        }
        if (mode == WorkspaceMode.PRO && workspaceNavigation.current == WorkspaceMode.PHOTO &&
            sourceUri != null && original != null) {
            showProHandoff = true
            return
        }
        workspaceNavigation = workspaceNavigation.open(mode)
    }

    fun openProWithRegularEdits() {
        val uri = sourceUri ?: return
        if (original == null || busy || exporting) return
        showProHandoff = false
        finishSlide()
        val edits = document
        busy = true
        scope.launch {
            try {
                val sourcePixels = sourceSize?.let { (width, height) -> ProPixelSize(width, height) }
                    ?: withContext(Dispatchers.IO) { ProImageGeometry.sourceSize(resolver, uri) }
                val editedSize = ProImageGeometry.editedSize(sourcePixels, edits)
                val source = ProImageGeometry.bind(ProLayer(uri = uri.toString(), name = "Original photo"), sourcePixels)
                val resetDocument = ProDocument(
                    layers = listOf(source),
                    background = ProBackground.TRANSPARENT,
                    sourceAspectRatio = sourcePixels.aspect,
                    canvasSize = sourcePixels
                )
                proDocument = resetDocument.copy(
                    layers = listOf(ProImageGeometry.bind(source.copy(edits = edits), sourcePixels)),
                    sourceAspectRatio = editedSize.aspect,
                    canvasSize = editedSize
                )
                proSession = ProWorkspaceSession(documentEstablished = true, resetDocument = resetDocument)
                workspaceNavigation = workspaceNavigation.open(WorkspaceMode.PRO)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: OutOfMemoryError) {
                notice = "There is not enough memory to open this photo in Pro."
            } catch (_: Exception) {
                notice = "Could not open the edited photo in Pro."
            } finally {
                busy = false
            }
        }
    }

    fun leaveWorkspace() {
        workspaceNavigation.commitBack()?.let { workspaceNavigation = it }
    }

    fun finishCollageTile() {
        val originalTile = initialCollagePhoto ?: return
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
        if (busy || exporting) return
        finishSlide()
        if (isPhotoDirty()) {
            replacement = candidate
            showRegularExit = true
        } else {
            loadPhoto(candidate.uri, candidate.edits)
        }
    }

    fun completeReplacement() {
        val candidate = replacement ?: return
        replacement = null
        showRegularExit = false
        loadPhoto(candidate.uri, candidate.edits)
    }

    fun closeCurrentPhoto() {
        loadingToken += 1
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
        } else if (isPhotoDirty()) {
            showTileExit = true
        } else {
            onCancelCollagePhoto?.invoke()
        }
    }

    fun requestRegularBack() {
        val previous = workspaceNavigation.commitBack()
        if (previous != null) {
            workspaceNavigation = previous
        } else if (original != null) {
            if (isPhotoDirty()) showRegularExit = true else closeCurrentPhoto()
        }
    }

    fun autoTone() {
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
        if (selectedPrivacy !in document.privacy.indices) return
        val updated = document.privacy.filterIndexed { i, _ -> i != selectedPrivacy }
        commit(document.copy(privacy = updated))
        selectedPrivacy = selectedPrivacy.coerceAtMost(updated.lastIndex).coerceAtLeast(0)
    }

    fun privacyStrokeEdit(clearAll: Boolean) {
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
        gestureStart = document
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

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        if (it != null) requestPhotoReplacement(PhotoReplacement(it))
    }
    var recoveredPhotoHandled by remember(photoExport.initialRecoveryId) { mutableStateOf(false) }
    LaunchedEffect(photoExport.initialRecoveryId, workspaceNavigation.current) {
        if (photoExport.initialRecoveryId == null || workspaceNavigation.current != WorkspaceMode.PHOTO ||
            recoveredPhotoHandled
        ) return@LaunchedEffect
        recoveredPhotoHandled = true
        if (sourceUri != null) return@LaunchedEffect
        val request = photoExport.recoveredRequest
        if (request != null) {
            loadPhoto(request.source, request.edits, restoringExport = true)
        } else {
            notice = "The previous export recipe could not be restored."
        }
    }
    fun openPicker() {
        if (!busy && !exporting) {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
    fun saveLook() {
        draftName = ""
        showSaveLook = true
    }
    fun saveProject() {
        projectSaveError = null
        closeAfterProjectSave = false
        replacementAfterSave = false
        draftName = ""
        showSaveProject = true
    }
    fun saveProjectAndClose() {
        projectSaveError = null
        replacementAfterSave = replacement != null
        closeAfterProjectSave = !replacementAfterSave
        draftName = ""
        showSaveProject = true
    }
    fun openProjects() {
        savedProjects = ProjectStore.load(context)
        selectedProject = null
        showProjects = true
    }

    fun startInk(point: BrushPoint) {
        if (selectedTab != EditTab.DRAW) return
        if (!creativeTools.erase &&
            document.creative.strokes.size >= CreativeRaster.MAX_INK) {
            notice = "Drawing layer limit reached. Clear some strokes first."
            return
        }
        gestureStart = document
        painting = true
        if (creativeTools.erase) {
            val erased = InkMath.erase(document.creative.strokes, point.x, point.y,
                creativeTools.inkThickness)
            document = document.copy(creative = document.creative.copy(strokes = erased))
        } else {
            currentInk = InkStroke(listOf(point), creativeTools.inkColor,
                creativeTools.inkThickness, creativeTools.inkOpacity)
        }
    }

    fun continueInk(point: BrushPoint) {
        if (!painting || selectedTab != EditTab.DRAW) return
        if (creativeTools.erase) {
            val erased = InkMath.erase(document.creative.strokes, point.x, point.y,
                creativeTools.inkThickness)
            if (erased.size <= CreativeRaster.MAX_INK) {
                document = document.copy(creative = document.creative.copy(strokes = erased))
            }
        } else {
            val ink = currentInk ?: return
            if (ink.points.size >= 500) return
            val last = ink.points.last()
            val dx = last.x - point.x
            val dy = last.y - point.y
            if (dx * dx + dy * dy >= .000008f) {
                currentInk = ink.copy(points = ink.points + point)
            }
        }
    }

    fun finishInk() {
        if (selectedTab == EditTab.DRAW && !creativeTools.erase) {
            val ink = currentInk
            if (ink != null && ink.points.isNotEmpty()) {
                document = document.copy(creative = document.creative.copy(
                    strokes = document.creative.strokes + ink
                ))
            }
        }
        currentInk = null
        painting = false
        finishSlide()
    }

    fun editWords(newLayer: Boolean) {
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
        if (gestureStart == null) gestureStart = document
        document = document.copy(
            crop = if (document.crop == CropRatio.ORIGINAL) CropRatio.FREE else document.crop,
            cropFrame = frame,
            cropFrameRatioLocked = document.crop.ratio != null &&
                (document.cropFrameRatioLocked || document.cropFrame == null)
        )
    }

    fun cancelCrop() {
        gestureStart?.let { document = it }
        gestureStart = null
        liveGesture = false
    }

    LaunchedEffect(initialCollagePhoto?.uri) {
        val photo = initialCollagePhoto
        if (photo != null) loadPhoto(Uri.parse(photo.uri), photo.edits)
    }

    BackHandler(enabled = onDoneCollagePhoto != null) {
        requestTileBack()
    }
    BackHandler(
        enabled = onDoneCollagePhoto == null && !showHistogram &&
            workspaceNavigation.current == WorkspaceMode.PHOTO &&
            (workspaceNavigation.previewBack() != null || original != null || busy || exporting)
    ) {
        if (busy || exporting) {
            notice = if (exporting) "Finish the export before leaving this photo."
                else "Wait for the current photo operation to finish."
        } else {
            requestRegularBack()
        }
    }

    val editingBlurBrush = selectedTab == EditTab.PRIVACY &&
        document.privacy.getOrNull(selectedPrivacy)?.kind == PrivacyKind.BLUR_BRUSH
    val cropTab = selectedTab == EditTab.CROP
    val previewEdits = if (cropTab) {
        PrivacyMath.retainAuthoringGeometry(document,
            document.copy(crop = CropRatio.ORIGINAL, cropFrame = null, cropCenterX = .5f,
                cropCenterY = .5f, cropFrameRatioLocked = false))
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
            onSession = { collageSession = it }
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
                        onTap = { chromeVisible = !chromeVisible }
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
                        onCanceled = ::cancelCrop,
                        onTap = { chromeVisible = !chromeVisible }
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
                        onTap = { chromeVisible = !chromeVisible }
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
                        onTap = { chromeVisible = !chromeVisible }
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
                        onTap = { chromeVisible = !chromeVisible }
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.padding(horizontal = 28.dp)
            ) {
                Button(
                    onClick = ::openPicker,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EditorMint, contentColor = Color(0xFF0B1E18)
                    )
                ) { Text("Open media", fontWeight = FontWeight.Bold) }
                if (onDoneCollagePhoto == null) {
                    Button(
                        onClick = { chooseWorkspace(WorkspaceMode.COLLAGE) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF26333C),
                            contentColor = EditorWhite
                        )
                    ) { Text("Create collage") }
                }
            }
        }

        if (original == null && chromeVisible) {
            Row(
                modifier = Modifier.align(Alignment.TopCenter)
                    .fillMaxWidth().statusBarsPadding()
                    .padding(start = 18.dp, end = 7.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("GZ Editor Pro", color = EditorWhite, fontSize = 21.sp,
                    fontWeight = FontWeight.Bold)
                if (onDoneCollagePhoto == null) {
                    WorkspaceModeMenu(WorkspaceMode.PHOTO, !busy && !exporting, ::chooseWorkspace)
                }
            }
            Text(
                "© 2026 Greenezone Software LLC",
                color = EditorMuted,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.BottomStart)
                    .navigationBarsPadding().padding(start = 16.dp, bottom = 12.dp)
            )
        }

        if (chromeVisible && original != null) {
            Column(
                Modifier.align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .then(if (selectedTab == EditTab.CROP) {
                        Modifier.onSizeChanged { topChromeHeight = it.height }
                    } else Modifier)
                    .background(Brush.verticalGradient(
                        listOf(Color(0xE5090C13), Color(0xC5090C13), Color.Transparent)
                    ))
                    .statusBarsPadding()
                    .padding(start = 11.dp, end = 7.dp, top = 3.dp, bottom = 12.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().height(39.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("GZ Editor Pro", color = EditorWhite,
                        fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    if (onDoneCollagePhoto == null) {
                        WorkspaceModeMenu(WorkspaceMode.PHOTO, !busy && !exporting, ::chooseWorkspace)
                    }
                    TextButton(
                        onClick = {
                            if (onDoneCollagePhoto != null) finishCollageTile()
                            else photoExport.openDialog()
                        },
                        enabled = !busy && !exporting
                    ) {
                        Text(if (onDoneCollagePhoto != null) "Done" else "Export",
                            color = EditorMint,
                            fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Row(
                    Modifier.fillMaxWidth().height(42.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = ::openPicker,
                        enabled = !busy && !exporting
                    ) { Text("Open media", color = EditorWhite, fontSize = 13.sp) }
                    IconButton(
                        modifier = Modifier.size(42.dp).semantics { contentDescription = "Undo" },
                        onClick = {
                            history.undo(document)?.let { (previous, changes) ->
                                document = previous
                                history = changes
                                activeLook = "custom"
                            }
                        },
                        enabled = history.undo.isNotEmpty() && !busy && !exporting
                    ) { Text("↶", color = EditorWhite, fontSize = 27.sp) }
                    IconButton(
                        modifier = Modifier.size(42.dp).semantics { contentDescription = "Redo" },
                        onClick = {
                            history.redo(document)?.let { (next, changes) ->
                                document = next
                                history = changes
                                activeLook = "custom"
                            }
                        },
                        enabled = history.redo.isNotEmpty() && !busy && !exporting
                    ) { Text("↷", color = EditorWhite, fontSize = 27.sp) }
                    TextButton(
                        onClick = { compare = !compare },
                        enabled = !busy && !exporting
                    ) {
                        Text(if (compare) "Edited" else "Before",
                            color = EditorWhite, fontSize = 12.sp)
                    }
                }
                if (notice.isNotEmpty()) {
                    Text(
                        notice,
                        color = EditorWhite,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp)
                    )
                }
            }

            if (zoom > 1.01f && original != null) {
                TextButton(
                    onClick = {
                        zoom = 1f; panX = 0f; panY = 0f
                    },
                    modifier = Modifier.align(Alignment.TopEnd)
                        .statusBarsPadding().padding(top = 64.dp, end = 10.dp)
                        .background(Color(0xC9151D24), RoundedCornerShape(12.dp))
                ) {
                    Text("Fit • " + String.format(Locale.US, "%.1f×", zoom),
                        color = EditorMint, fontSize = 12.sp)
                }
            }

            if (original != null) {
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .then(if (selectedTab == EditTab.CROP) {
                            Modifier.onSizeChanged { bottomChromeHeight = it.height }
                        } else Modifier)
                        .navigationBarsPadding()
                ) {
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
                            showHistogram = tileEntryBaseline == null && onDoneCollagePhoto == null,
                            creativeTool = creativeTools,
                            cropSize = cropImageSize
                        ),
                        actions = RegularDockActions(
                            onTab = {
                                selectedTab = it
                                if (it == EditTab.PRIVACY || it == EditTab.TEXT || it == EditTab.DRAW ||
                                    it == EditTab.SHAPES || it == EditTab.CROP) {
                                    zoom = 1f
                                    panX = 0f
                                    panY = 0f
                                }
                            },
                            onSelectPrivacy = { selectedPrivacy = it },
                            onAddPrivacy = ::addPrivacy,
                            onDeletePrivacy = ::removePrivacy,
                            onUndoPrivacyStroke = { privacyStrokeEdit(false) },
                            onClearPrivacy = { privacyStrokeEdit(true) },
                            onCommit = {
                                commit(it)
                                activeLook = "custom"
                            },
                            onSlide = ::slide,
                            onSlideFinished = ::finishSlide,
                            onSelectLook = ::applyLook,
                            onAutoTone = ::autoTone,
                            onAutoWB = ::autoWB,
                            onSaveLook = ::saveLook,
                            onSaveProject = ::saveProject,
                            onProjects = ::openProjects,
                            onOpen = ::openPicker,
                            onCollage = { chooseWorkspace(WorkspaceMode.COLLAGE) },
                            onCreativeTool = { creativeTools = it },
                            onNewText = { editWords(true) },
                            onEditText = { editWords(false) },
                            onHistogram = { showHistogram = true }
                        )
                    )
                }
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

    if (showHistogram && selectedImage != null) {
        val image = original?.let { input ->
            if (compare) previewState.beforeImage.takeIf { previewState.beforeStamp == (input to PhotoGeometry.from(previewEdits)) }
            else selectedImage.takeIf { previewState.processedStamp == PreviewStamp(input, previewEdits) }
        }
        PhotoHistogramDialog(
            image = image,
            showingBefore = compare,
            sourceGeneration = loadingToken,
            exposure = previewEdits.grade.exposure
        ) { showHistogram = false }
    }
    PhotoExportDialogs(photoExport, sourceUri, sourceSize, document, busy, notice)
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
    if (showSaveProject) {
        SaveNameDialog(
            title = "Save editable project",
            description = "Stores your edits and a local reference to this photo. " +
                "Keep the original photo available.",
            name = draftName,
            onName = { draftName = it; projectSaveError = null },
            error = projectSaveError,
            onSave = {
                val uri = sourceUri
                if (replacementAfterSave && ProjectStore.normalizedName(draftName) == replacement?.projectName) {
                    projectSaveError = "Choose a different name from the project you are opening."
                } else if (uri != null && ProjectStore.save(context, draftName, uri, document)) {
                    savedProjects = ProjectStore.load(context)
                    if (tileEntryBaseline == null) {
                        baselineUri = uri.toString()
                        baselineDocument = document
                    }
                    val shouldClose = closeAfterProjectSave
                    val shouldReplace = replacementAfterSave
                    closeAfterProjectSave = false
                    replacementAfterSave = false
                    showSaveProject = false
                    when {
                        shouldReplace -> completeReplacement()
                        shouldClose -> closeCurrentPhoto()
                        else -> notice = "Project saved on your phone"
                    }
                } else projectSaveError = "Could not save project (twelve maximum)."
            },
            onDismiss = {
                closeAfterProjectSave = false
                if (replacementAfterSave) replacement = null
                replacementAfterSave = false
                showSaveProject = false
            }
        )
    }
    if (showProjects) {
        ProjectsDialog(
            projects = savedProjects,
            selectedName = selectedProject,
            onSelect = { selectedProject = it },
            onOpen = {
                savedProjects.firstOrNull { it.name == selectedProject }?.let {
                    requestPhotoReplacement(PhotoReplacement(it.image, it.document, it.name))
                }
                showProjects = false
            },
            onDelete = {
                selectedProject?.let {
                    projectToDelete = it
                    showProjects = false
                }
            },
            onDismiss = { showProjects = false }
        )
    }
    projectToDelete?.let { name ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null; showProjects = true },
            title = { Text("Delete saved project?") },
            text = { Text("Delete \"$name\" from saved projects. Your original photo stays on your phone.") },
            confirmButton = {
                TextButton(onClick = {
                    if (ProjectStore.delete(context, name)) {
                        savedProjects = ProjectStore.load(context)
                        selectedProject = null
                        notice = "Project reference removed."
                    } else {
                        notice = "Could not delete project."
                    }
                    projectToDelete = null
                    showProjects = true
                }) { Text("Delete project") }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null; showProjects = true }) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showRegularExit) {
        val keepEditing = {
            showRegularExit = false
            replacement = null
        }
        AlertDialog(
            onDismissRequest = keepEditing,
            title = { Text("Keep your photo edits?") },
            text = {
                Text(if (replacement == null) {
                    "Save an editable project, discard these changes, or keep editing."
                } else {
                    "Save or discard your current edits before opening the other photo."
                })
            },
            confirmButton = {
                Row {
                    TextButton(onClick = keepEditing) {
                        Text("Keep editing")
                    }
                    TextButton(onClick = {
                        showRegularExit = false
                        saveProjectAndClose()
                    }) {
                        Text("Save project")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    if (replacement == null) closeCurrentPhoto() else completeReplacement()
                }) {
                    Text("Discard")
                }
            }
        )
    }
    if (showProHandoff) {
        val hasProWork = proDocument.layers.isNotEmpty() ||
            proSession.history.undo.isNotEmpty() || proSession.history.redo.isNotEmpty()
        AlertDialog(
            onDismissRequest = { showProHandoff = false },
            title = { Text("Continue editing in Photo Pro?") },
            text = {
                Column {
                    Text("Bring this photo and its current edits into Pro. Your Regular photo stays open.")
                    if (hasProWork) {
                        Text("Starting a new session replaces the current Pro work. Saved projects stay available.")
                        TextButton(onClick = {
                            showProHandoff = false
                            finishSlide()
                            workspaceNavigation = workspaceNavigation.open(WorkspaceMode.PRO)
                        }) { Text("Resume Pro work") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = ::openProWithRegularEdits) { Text("Yes, continue") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { showProHandoff = false }) { Text("Cancel") }
                    TextButton(onClick = {
                        showProHandoff = false
                        finishSlide()
                        proDocument = ProDocument()
                        proSession = ProWorkspaceSession(documentEstablished = true)
                        workspaceNavigation = workspaceNavigation.open(WorkspaceMode.PRO)
                    }) { Text("No, open blank") }
                }
            }
        )
    }
    if (showTileExit) {
        AlertDialog(
            onDismissRequest = { showTileExit = false },
            title = { Text("Apply tile changes?") },
            text = { Text("Only Apply or Done changes this collage tile.") },
            confirmButton = {
                Row {
                    TextButton(onClick = { showTileExit = false }) {
                        Text("Keep editing")
                    }
                    TextButton(onClick = { finishCollageTile() }) {
                        Text("Apply")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showTileExit = false
                    onCancelCollagePhoto?.invoke()
                }) {
                    Text("Discard")
                }
            }
        )
    }
}

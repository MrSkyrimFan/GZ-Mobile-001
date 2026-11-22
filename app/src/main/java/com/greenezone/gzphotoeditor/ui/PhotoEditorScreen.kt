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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.greenezone.gzphotoeditor.editor.CropFrame
import com.greenezone.gzphotoeditor.editor.CollageDocument
import com.greenezone.gzphotoeditor.editor.CollagePhoto
import com.greenezone.gzphotoeditor.editor.ProDocument
import com.greenezone.gzphotoeditor.editor.ProLayer
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.CropMath
import com.greenezone.gzphotoeditor.editor.CropRatio
import com.greenezone.gzphotoeditor.editor.ExportSettings
import com.greenezone.gzphotoeditor.editor.writeStagedImage
import com.greenezone.gzphotoeditor.editor.ImageExportWriter
import com.greenezone.gzphotoeditor.editor.ExportSizing
import com.greenezone.gzphotoeditor.editor.InkMath
import com.greenezone.gzphotoeditor.editor.InkStroke
import com.greenezone.gzphotoeditor.editor.CreativeRaster
import com.greenezone.gzphotoeditor.editor.TextOverlay
import com.greenezone.gzphotoeditor.editor.EditHistory
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
import com.greenezone.gzphotoeditor.editor.PrivacyMath
import com.greenezone.gzphotoeditor.editor.PrivacyRaster
import com.greenezone.gzphotoeditor.editor.SavedLooks
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt

private data class ExportRequest(val settings: ExportSettings, val edits: EditDocument)

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

    var workspaceNavigation by remember { mutableStateOf(WorkspaceNavigation()) }
    var collageDocument by remember { mutableStateOf(CollageDocument()) }
    var collageSession by remember { mutableStateOf(CollageWorkspaceSession()) }
    var proDocument by remember { mutableStateOf(ProDocument()) }
    var proSession by remember { mutableStateOf(ProWorkspaceSession()) }
    var gifDocument by remember { mutableStateOf(GifDocument()) }
    var gifSession by remember { mutableStateOf(GifWorkspaceSession()) }

    var sourceUri by remember { mutableStateOf<Uri?>(null) }
    var original by remember { mutableStateOf<Bitmap?>(null) }
    var processed by remember { mutableStateOf<Bitmap?>(null) }
    var beforeImage by remember { mutableStateOf<Bitmap?>(null) }
    var filterThumbs by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var quickSource by remember { mutableStateOf<Bitmap?>(null) }
    var liveGesture by remember { mutableStateOf(false) }
    var painting by remember { mutableStateOf(false) }
    var document by remember { mutableStateOf(EditDocument()) }
    var history by remember { mutableStateOf(EditHistory()) }
    var gestureStart by remember { mutableStateOf<EditDocument?>(null) }

    var chromeVisible by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(EditTab.LOOKS) }
    var activeLook by remember { mutableStateOf("none") }
    var selectedMask by remember { mutableIntStateOf(0) }
    var selectedPrivacy by remember { mutableIntStateOf(0) }
    var compare by remember { mutableStateOf(false) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }
    var notice by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    var loadingToken by remember { mutableIntStateOf(0) }
    var exportSettings by remember { mutableStateOf(ExportSettings()) }
    var pendingExport by remember { mutableStateOf<ExportRequest?>(null) }
    var creativeTools by remember { mutableStateOf(CreativeToolState()) }
    var currentInk by remember { mutableStateOf<InkStroke?>(null) }
    var showTextDialog by remember { mutableStateOf(false) }
    var textDraft by remember { mutableStateOf("") }
    var editingTextIndex by remember { mutableIntStateOf(-1) }
    var baselineUri by remember { mutableStateOf<String?>(null) }
    var baselineDocument by remember { mutableStateOf(EditDocument()) }
    var showRegularExit by remember { mutableStateOf(false) }
    var showTileExit by remember { mutableStateOf(false) }
    var closeAfterProjectSave by remember { mutableStateOf(false) }

    var showExport by remember { mutableStateOf(false) }
    var showSaveLook by remember { mutableStateOf(false) }
    var showSaveProject by remember { mutableStateOf(false) }
    var showProjects by remember { mutableStateOf(false) }
    var draftName by remember { mutableStateOf("") }
    var selectedProject by remember { mutableStateOf<String?>(null) }
    var savedLooks by remember { mutableStateOf(SavedLooks.load(context)) }
    var savedProjects by remember { mutableStateOf(ProjectStore.load(context)) }

    fun commit(next: EditDocument) {
        history = history.checkpoint(document, next)
        document = next
        gestureStart = null
        liveGesture = false
    }
    fun slide(next: EditDocument) {
        if (gestureStart == null) gestureStart = document
        document = next
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
        establishBaseline: Boolean = tileEntryBaseline == null
    ) {
        val request = ++loadingToken
        scope.launch {
            busy = true
            notice = "Opening photo…"
            try {
                try {
                    resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: SecurityException) {
                    // Some pickers grant access only for this session.
                }
                val bitmap = withContext(Dispatchers.IO) {
                    ImagePipeline.decode(resolver, uri, ImagePipeline.PREVIEW_MAX_EDGE, 2_000_000L)
                }
                if (request != loadingToken) {
                    bitmap.recycle()
                    return@launch
                }
                sourceUri = uri
                original = bitmap
                processed = bitmap
                beforeImage = bitmap
                filterThumbs = emptyList()
                quickSource = null
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
                selectedMask = 0
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
                notice = if (restored == null) "Media opened" else "Project restored"
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (request == loadingToken) {
                    notice = "Could not open photo: " +
                        (error.message ?: "file missing or permission revoked")
                }
            } finally {
                if (request == loadingToken) busy = false
            }
        }
    }

    fun exportImage(output: Uri, request: ExportRequest) {
        val source = sourceUri ?: return
        if (source == output) {
            notice = "Export destination cannot overwrite the original."
            return
        }
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val maxPixels = GeometryMath.safeExportPixels(manager.memoryClass)
        scope.launch {
            exporting = true
            showExport = false
            notice = "Exporting image…"
            try {
                val dimensions = withContext(Dispatchers.Default) {
                    var decoded: Bitmap? = null
                    var rendered: Bitmap? = null
                    var sized: Bitmap? = null
                    try {
                        val originalFull = ImagePipeline.decode(resolver, source, 12_000, maxPixels)
                        decoded = originalFull
                        val full = ImagePipeline.render(originalFull, request.edits) { ensureActive() }
                        rendered = full
                        val photo = ExportSizing.layout(full, request.settings)
                        sized = photo
                        withContext(Dispatchers.IO) {
                            writeStagedImage(context.cacheDir,
                                openDestination = { resolver.openOutputStream(output, "wt") },
                                checkActive = { ensureActive() }) { stream -> ImageExportWriter.write(photo, request.settings, stream) }
                        }
                        photo.width.toString() + " × " + photo.height
                    } finally {
                        sized?.let { if (it !== rendered && !it.isRecycled) it.recycle() }
                        rendered?.let { if (it !== decoded && !it.isRecycled) it.recycle() }
                        decoded?.let { if (!it.isRecycled) it.recycle() }
                    }
                }
                notice = "Saved " + dimensions + " • Original unchanged"
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: OutOfMemoryError) {
                notice = "Not enough memory to export. Choose a smaller size."
            } catch (error: Exception) {
                notice = "Export failed: " + (error.message ?: "unknown error")
            } finally {
                exporting = false
            }
        }
    }


    fun chooseWorkspace(mode: WorkspaceMode) {
        if (onDoneCollagePhoto != null || busy || exporting) return
        if (mode == WorkspaceMode.VIDEO) {
            notice = "Video editing is coming later, after the photo and GIF tools."
            return
        }
        if (mode == WorkspaceMode.PRO &&
            workspaceNavigation.current != WorkspaceMode.PRO &&
            !proSession.documentEstablished) {
            val current = sourceUri
            if (proDocument.layers.isEmpty() && current != null) {
                proDocument = ProDocument(layers = listOf(ProLayer(
                    uri = current.toString(), edits = document, name = "Original photo",
                    aspect = (processed ?: original)?.let { frame ->
                        frame.width.toFloat() / frame.height.coerceAtLeast(1)
                    }?.coerceIn(.1f, 10f) ?: 1f
                )))
                proSession = ProWorkspaceSession(documentEstablished = true)
            } else {
                proSession = proSession.copy(documentEstablished = true)
            }
        }
        workspaceNavigation = workspaceNavigation.open(mode)
    }

    fun leaveWorkspace() {
        workspaceNavigation.commitBack()?.let { workspaceNavigation = it }
    }

    fun finishCollageTile() {
        val originalTile = initialCollagePhoto ?: return
        val image = sourceUri?.toString() ?: originalTile.uri
        showTileExit = false
        onDoneCollagePhoto?.invoke(CollagePhoto(image, document))
    }

    fun isPhotoDirty(): Boolean {
        val tileEntry = tileEntryBaseline
        if (tileEntry != null) {
            val currentUri = sourceUri ?: return false
            return currentUri.toString() != tileEntry.uri || document != tileEntry.edits
        }
        return sourceUri?.toString() != baselineUri || document != baselineDocument
    }

    fun closeCurrentPhoto() {
        loadingToken += 1
        sourceUri = null
        original = null
        processed = null
        beforeImage = null
        filterThumbs = emptyList()
        quickSource = null
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
        selectedMask = 0
        selectedPrivacy = 0
        compare = false
        zoom = 1f
        panX = 0f
        panY = 0f
        notice = ""
        showExport = false
        pendingExport = null
        showTextDialog = false
        showRegularExit = false
        showTileExit = false
        closeAfterProjectSave = false
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

    fun addMask(kind: MaskKind) {
        if (document.masks.size >= 4) {
            notice = "Maximum of four masks per photo."
            return
        }
        val mask = if (kind == MaskKind.BRUSH) {
            LocalMask(kind = kind, radius = .045f, feather = .45f, exposure = .55f)
        } else LocalMask(kind = kind)
        selectedMask = document.masks.size
        commit(document.copy(masks = document.masks + mask))
        if (kind == MaskKind.BRUSH) {
            zoom = 1f
            panX = 0f
            panY = 0f
            notice = "Brush selected. Drag on your photo to paint."
        }
    }
    fun removeMask() {
        if (selectedMask !in document.masks.indices) return
        val updated = document.masks.filterIndexed { i, _ -> i != selectedMask }
        commit(document.copy(masks = updated))
        selectedMask = selectedMask.coerceAtMost(updated.lastIndex).coerceAtLeast(0)
    }
    fun brushEdit(clearAll: Boolean) {
        val mask = document.masks.getOrNull(selectedMask) ?: return
        if (mask.kind != MaskKind.BRUSH) return
        val updated = mask.copy(strokes = if (clearAll) emptyList() else mask.strokes.dropLast(1))
        val values = document.masks.toMutableList()
        values[selectedMask] = updated
        commit(document.copy(masks = values))
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

    fun movePrivacy(x: Float, y: Float) {
        val mark = document.privacy.getOrNull(selectedPrivacy) ?: return
        if (mark.kind.usesBrush) return
        val updated = document.privacy.toMutableList()
        updated[selectedPrivacy] = PrivacyMath.withCenter(mark, x, y)
        slide(document.copy(privacy = updated))
    }

    fun startPaint(point: BrushPoint) {
        if (selectedTab == EditTab.PRIVACY) {
            val mark = document.privacy.getOrNull(selectedPrivacy) ?: return
            if (!mark.kind.usesBrush) return
            if (mark.strokes.size >= 40 || mark.strokes.sumOf { it.points.size } >= 640) {
                notice = "Brush limit reached. Undo a stroke or add another brush."
                return
            }
            gestureStart = document
            painting = true
            val updated = document.privacy.toMutableList()
            updated[selectedPrivacy] = mark.copy(strokes =
                mark.strokes + BrushStroke(listOf(point)))
            document = document.copy(privacy = updated)
        } else {
            val mask = document.masks.getOrNull(selectedMask) ?: return
            if (mask.kind != MaskKind.BRUSH) return
            if (mask.strokes.size >= 40 || mask.strokes.sumOf { it.points.size } >= 640) {
                notice = "Brush limit reached. Undo a stroke or add another mask."
                return
            }
            gestureStart = document
            painting = true
            val updated = document.masks.toMutableList()
            updated[selectedMask] = mask.copy(strokes =
                mask.strokes + BrushStroke(listOf(point)))
            document = document.copy(masks = updated)
        }
    }

    fun continuePaint(point: BrushPoint) {
        if (!painting || gestureStart == null) return
        if (selectedTab == EditTab.PRIVACY) {
            val mark = document.privacy.getOrNull(selectedPrivacy) ?: return
            val last = mark.strokes.lastOrNull() ?: return
            if (mark.strokes.sumOf { it.points.size } >= 640) return
            val previous = last.points.last()
            val dx = point.x - previous.x
            val dy = point.y - previous.y
            if (dx * dx + dy * dy < .000012f) return
            val updated = document.privacy.toMutableList()
            updated[selectedPrivacy] = mark.copy(strokes =
                mark.strokes.dropLast(1) + last.copy(points = last.points + point))
            document = document.copy(privacy = updated)
        } else {
            val mask = document.masks.getOrNull(selectedMask) ?: return
            val last = mask.strokes.lastOrNull() ?: return
            if (mask.strokes.sumOf { it.points.size } >= 640) return
            val previous = last.points.last()
            val dx = point.x - previous.x
            val dy = point.y - previous.y
            if (dx * dx + dy * dy < .000012f) return
            val updated = document.masks.toMutableList()
            updated[selectedMask] = mask.copy(strokes =
                mask.strokes.dropLast(1) + last.copy(points = last.points + point))
            document = document.copy(masks = updated)
        }
    }

    fun finishPaint() {
        painting = false
        finishSlide()
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        if (it != null) loadPhoto(it)
    }
    val exporter = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val request = pendingExport
        pendingExport = null
        val output = result.data?.data
        if (result.resultCode == Activity.RESULT_OK && output != null && request != null) {
            exportImage(output, request)
        }
    }
    fun requestExport() {
        val settings = exportSettings
        pendingExport = ExportRequest(settings, document)
        showExport = false
        val suffix = settings.format.extension
        val target = if (settings.preset == com.greenezone.gzphotoeditor.editor.SocialPreset.ORIGINAL)
            "GZEditorPro_Edit" else "GZEditorPro_" + settings.preset.name
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = settings.format.mime
            putExtra(Intent.EXTRA_TITLE, target + "." + suffix)
        }
        exporter.launch(intent)
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
        closeAfterProjectSave = false
        draftName = ""
        showSaveProject = true
    }
    fun saveProjectAndClose() {
        closeAfterProjectSave = true
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
        slide(document.copy(
            crop = if (document.crop == CropRatio.ORIGINAL) CropRatio.FREE else document.crop,
            cropFrame = frame
        ))
    }

    LaunchedEffect(initialCollagePhoto?.uri) {
        val photo = initialCollagePhoto
        if (photo != null) loadPhoto(Uri.parse(photo.uri), photo.edits)
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
            requestRegularBack()
        }
    }

    LaunchedEffect(original) {
        val input = original ?: run { quickSource = null; return@LaunchedEffect }
        val small = withContext(Dispatchers.Default) {
            val scale = minOf(1f, 660f / maxOf(input.width, input.height))
            if (scale >= 1f) input
            else Bitmap.createScaledBitmap(input,
                (input.width * scale).roundToInt().coerceAtLeast(1),
                (input.height * scale).roundToInt().coerceAtLeast(1), true)
        }
        quickSource = small
    }

    val editingBlurBrush = selectedTab == EditTab.PRIVACY &&
        document.privacy.getOrNull(selectedPrivacy)?.kind == PrivacyKind.BLUR_BRUSH
    LaunchedEffect(quickSource, painting, editingBlurBrush) {
        val fast = quickSource ?: return@LaunchedEffect
        if (!painting || !editingBlurBrush) return@LaunchedEffect
        try {
            // Complete the current frame, then render the newest pointer position.
            snapshotFlow { document }.conflate().collect { edits ->
                processed = withContext(Dispatchers.Default) {
                    val running = currentCoroutineContext()
                    ImagePipeline.render(fast, edits) { running.ensureActive() }
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            notice = "Preview error: " + (error.message ?: "unexpected error")
        }
    }

    LaunchedEffect(original, if (painting) null else document, liveGesture, quickSource, painting, selectedTab) {
        val input = original ?: run { processed = null; return@LaunchedEffect }
        if (painting) return@LaunchedEffect
        val imageDocument = if (selectedTab == EditTab.CROP)
            document.copy(crop = CropRatio.ORIGINAL, cropFrame = null) else document
        delay(if (liveGesture) 20L else 45L)
        try {
            val fast = quickSource
            if (fast != null && fast !== input) {
                val preview = withContext(Dispatchers.Default) {
                    val running = currentCoroutineContext()
                    ImagePipeline.render(fast, imageDocument) { running.ensureActive() }
                }
                processed = preview
            }
            if (!liveGesture || fast == null || fast === input) {
                if (fast != null && fast !== input) delay(140L)
                val quality = withContext(Dispatchers.Default) {
                    val running = currentCoroutineContext()
                    ImagePipeline.render(input, imageDocument) { running.ensureActive() }
                }
                processed = quality
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            notice = "Preview error: " + (error.message ?: "unexpected error")
        }
    }

    // Keep before/after geometry identical; compute outside the UI thread.
    LaunchedEffect(original, document.quarterTurns, document.mirrored,
        document.flipVertical, document.crop, document.cropFrame, document.cropCenterX,
        document.cropCenterY, document.straightenDegrees, selectedTab) {
        val input = original ?: run { beforeImage = null; return@LaunchedEffect }
        try {
            val result = withContext(Dispatchers.Default) {
                ImagePipeline.geometry(input, if (selectedTab == EditTab.CROP)
                    document.copy(crop = CropRatio.ORIGINAL, cropFrame = null) else document)
            }
            beforeImage = result
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            beforeImage = input
        }
    }

    LaunchedEffect(original) {
        val input = original ?: run { filterThumbs = emptyList(); return@LaunchedEffect }
        try {
            val created = withContext(Dispatchers.Default) {
                val working = currentCoroutineContext()
                val longest = maxOf(input.width, input.height)
                val sizeFactor = 128f / longest.coerceAtLeast(1)
                val width = (input.width * sizeFactor).roundToInt().coerceAtLeast(1)
                val height = (input.height * sizeFactor).roundToInt().coerceAtLeast(1)
                val thumbnail = Bitmap.createScaledBitmap(input, width, height, true)
                listOf(thumbnail) + Looks.all.map { look ->
                    working.ensureActive()
                    ImagePipeline.render(thumbnail, EditDocument(grade = look.grade)) {
                        working.ensureActive()
                    }
                }
            }
            filterThumbs = created
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            filterThumbs = emptyList()
        }
    }

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

    val selectedImage = if (compare) beforeImage ?: original else processed ?: original
    val cropEditing = selectedTab == EditTab.CROP && !compare && !busy && !exporting &&
        zoom <= 1.001f
    val textEditing = selectedTab == EditTab.TEXT && !compare && !busy && !exporting &&
        zoom <= 1.001f && document.creative.text.getOrNull(creativeTools.textIndex) != null
    val shapeEditing = selectedTab == EditTab.SHAPES && !compare && !busy && !exporting &&
        zoom <= 1.001f && document.creative.shapes.getOrNull(creativeTools.shapeIndex) != null
    val drawingActive = selectedTab == EditTab.DRAW && !compare && !busy && !exporting &&
        zoom <= 1.001f
    val activePrivacy = document.privacy.getOrNull(selectedPrivacy)
    val maskBrushActive = selectedTab == EditTab.MASKS &&
        document.masks.getOrNull(selectedMask)?.kind == MaskKind.BRUSH
    val privacyBrushActive = selectedTab == EditTab.PRIVACY &&
        activePrivacy?.kind?.usesBrush == true
    val brushSelected = !compare && !busy && !exporting && zoom <= 1.001f &&
        (maskBrushActive || privacyBrushActive)
    val privacyRegionSelected = !compare && !busy && !exporting &&
        selectedTab == EditTab.PRIVACY && activePrivacy != null &&
        !activePrivacy.kind.usesBrush && zoom <= 1.001f

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF080B10)).clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        if (selectedImage != null) {
            val stableCoordinates = beforeImage ?: selectedImage
            Image(
                bitmap = selectedImage.asImageBitmap(),
                contentDescription = "Photo editing canvas. Tap to hide or show tools.",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
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
                            if (!brushSelected && !cropEditing && !textEditing &&
                                !shapeEditing && !drawingActive) {
                                zoom = (zoom * pinch).coerceIn(1f, 6f)
                                val maxX = size.width * (zoom - 1f) / 2f
                                val maxY = size.height * (zoom - 1f) / 2f
                                panX = (panX + pan.x).coerceIn(-maxX, maxX)
                                panY = (panY + pan.y).coerceIn(-maxY, maxY)
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
            if (brushSelected) {
                val active = if (privacyBrushActive) activePrivacy?.let {
                    LocalMask(
                        kind = MaskKind.BRUSH,
                        strokes = it.strokes,
                        radius = it.brushSize,
                        feather = it.feather
                    )
                } else document.masks.getOrNull(selectedMask)
                if (active != null) {
                    BrushOverlay(
                        bitmapWidth = stableCoordinates.width,
                        bitmapHeight = stableCoordinates.height,
                        mask = active,
                        enabled = !busy,
                        onStart = ::startPaint,
                        onContinue = ::continuePaint,
                        onFinish = ::finishPaint,
                        onTap = { chromeVisible = !chromeVisible },
                        showGuides = !editingBlurBrush
                    )
                }
            }
            if (privacyRegionSelected && activePrivacy != null) {
                PrivacyRegionOverlay(
                    imageWidth = stableCoordinates.width,
                    imageHeight = stableCoordinates.height,
                    mark = activePrivacy,
                    canMove = true,
                    showGuides = true,
                    onMove = ::movePrivacy,
                    onFinished = ::finishSlide,
                    onTap = { chromeVisible = !chromeVisible }
                )
            }
            if (cropEditing) {
                CropFrameOverlay(
                    imageWidth = stableCoordinates.width,
                    imageHeight = stableCoordinates.height,
                    frame = document.cropFrame ?: CropMath.initial(
                        stableCoordinates.width, stableCoordinates.height, document.crop
                    ),
                    aspectRatio = document.crop.ratio,
                    showGuides = true,
                    onChange = ::moveCrop,
                    onFinished = ::finishSlide,
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
                            else showExport = true
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
                        .navigationBarsPadding()
                ) {
                    EditorDock(
                        tab = selectedTab,
                        onTab = {
                            selectedTab = it
                            if (it == EditTab.MASKS || it == EditTab.PRIVACY ||
                                it == EditTab.TEXT || it == EditTab.DRAW ||
                                it == EditTab.SHAPES || it == EditTab.CROP) {
                                zoom = 1f; panX = 0f; panY = 0f
                            }
                        },
                        document = document,
                        original = original,
                        thumbnails = filterThumbs,
                        savedLooks = savedLooks,
                        activeLook = activeLook,
                        busy = busy || exporting,
                        selectedMaskIndex = selectedMask,
                        onSelectMask = { selectedMask = it },
                        selectedPrivacyIndex = selectedPrivacy,
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
                        onAddMask = ::addMask,
                        onDeleteMask = ::removeMask,
                        onUndoBrush = { brushEdit(false) },
                        onClearBrush = { brushEdit(true) },
                        onAutoTone = ::autoTone,
                        onAutoWB = ::autoWB,
                        onSaveLook = ::saveLook,
                        onSaveProject = ::saveProject,
                        onProjects = ::openProjects,
                        onOpen = ::openPicker,
                        onCollage = { chooseWorkspace(WorkspaceMode.COLLAGE) },
                        showCollage = onDoneCollagePhoto == null,
                        creativeTool = creativeTools,
                        onCreativeTool = { creativeTools = it },
                        onNewText = { editWords(true) },
                        onEditText = { editWords(false) },
                        cropImageWidth = if (document.quarterTurns % 2 == 0)
                            (original?.width ?: 1) else (original?.height ?: 1),
                        cropImageHeight = if (document.quarterTurns % 2 == 0)
                            (original?.height ?: 1) else (original?.width ?: 1)
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

    if (showExport) {
        ExportDialog(
            settings = exportSettings,
            onSettings = { exportSettings = it },
            onSave = ::requestExport,
            onDismiss = { showExport = false }
        )
    }
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
            onName = { draftName = it },
            onSave = {
                val uri = sourceUri
                if (uri != null && ProjectStore.save(context, draftName, uri, document)) {
                    savedProjects = ProjectStore.load(context)
                    if (tileEntryBaseline == null) {
                        baselineUri = uri.toString()
                        baselineDocument = document
                    }
                    val shouldClose = closeAfterProjectSave
                    closeAfterProjectSave = false
                    showSaveProject = false
                    if (shouldClose) closeCurrentPhoto()
                    else notice = "Project saved on your phone"
                } else notice = "Could not save project (twelve maximum)."
            },
            onDismiss = {
                closeAfterProjectSave = false
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
                    loadPhoto(it.image, it.document)
                }
                showProjects = false
            },
            onDelete = {
                selectedProject?.let { ProjectStore.delete(context, it) }
                savedProjects = ProjectStore.load(context)
                selectedProject = null
                notice = "Project reference removed. Original photo is untouched."
            },
            onDismiss = { showProjects = false }
        )
    }
    if (showRegularExit) {
        AlertDialog(
            onDismissRequest = { showRegularExit = false },
            title = { Text("Keep your photo edits?") },
            text = { Text("Save an editable project, discard these changes, or keep editing.") },
            confirmButton = {
                Row {
                    TextButton(onClick = { showRegularExit = false }) {
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
                TextButton(onClick = { closeCurrentPhoto() }) {
                    Text("Discard")
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

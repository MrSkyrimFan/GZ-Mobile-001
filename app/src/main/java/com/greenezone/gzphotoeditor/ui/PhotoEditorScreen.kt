package com.greenezone.gzphotoeditor.ui

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
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
import com.greenezone.gzphotoeditor.editor.EditHistory
import com.greenezone.gzphotoeditor.editor.GeometryMath
import com.greenezone.gzphotoeditor.editor.Grade
import com.greenezone.gzphotoeditor.editor.ImagePipeline
import com.greenezone.gzphotoeditor.editor.LocalMask
import com.greenezone.gzphotoeditor.editor.Look
import com.greenezone.gzphotoeditor.editor.Looks
import com.greenezone.gzphotoeditor.editor.MaskKind
import com.greenezone.gzphotoeditor.editor.ProjectStore
import com.greenezone.gzphotoeditor.editor.SavedLooks
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Image-first editor. The image covers the entire canvas behind lightweight chrome.
 * One tap hides the controls; a second tap restores them. Do not show a vertical
 * adjustment list or constrain the image to a short preview box.
 */
@Composable
fun PhotoEditorScreen() {
    val context = LocalContext.current
    val resolver = context.contentResolver
    val scope = rememberCoroutineScope()

    var sourceUri by remember { mutableStateOf<Uri?>(null) }
    var original by remember { mutableStateOf<Bitmap?>(null) }
    var processed by remember { mutableStateOf<Bitmap?>(null) }
    var beforeImage by remember { mutableStateOf<Bitmap?>(null) }
    var filterThumbs by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var document by remember { mutableStateOf(EditDocument()) }
    var history by remember { mutableStateOf(EditHistory()) }
    var gestureStart by remember { mutableStateOf<EditDocument?>(null) }

    var chromeVisible by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(EditTab.LOOKS) }
    var activeLook by remember { mutableStateOf("none") }
    var selectedMask by remember { mutableIntStateOf(0) }
    var compare by remember { mutableStateOf(false) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }
    var notice by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    var loadingToken by remember { mutableIntStateOf(0) }
    var jpegQuality by remember { mutableIntStateOf(95) }

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
    }
    fun slide(next: EditDocument) {
        if (gestureStart == null) gestureStart = document
        document = next
        activeLook = "custom"
    }
    fun finishSlide() {
        gestureStart?.let { beginning ->
            history = history.checkpoint(beginning, document)
        }
        gestureStart = null
    }
    fun applyLook(key: String, grade: Grade) {
        commit(document.copy(grade = grade))
        activeLook = key
    }

    fun loadPhoto(uri: Uri, restored: EditDocument? = null) {
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
                document = restored ?: EditDocument()
                history = EditHistory()
                gestureStart = null
                selectedMask = 0
                selectedTab = EditTab.LOOKS
                activeLook = if (restored == null) "none" else "custom"
                chromeVisible = true
                compare = false
                zoom = 1f
                panX = 0f
                panY = 0f
                notice = if (restored == null) "Tap the photo to hide or show tools" else "Editable project restored"
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

    fun exportImage(output: Uri, format: Bitmap.CompressFormat, quality: Int) {
        val source = sourceUri ?: return
        if (source == output) {
            notice = "Export destination cannot overwrite the original."
            return
        }
        val edits = document
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val maxPixels = GeometryMath.safeExportPixels(manager.memoryClass)
        scope.launch {
            exporting = true
            showExport = false
            notice = "Exporting photo…"
            try {
                val dimensions = withContext(Dispatchers.Default) {
                    var decoded: Bitmap? = null
                    var rendered: Bitmap? = null
                    var flattened: Bitmap? = null
                    try {
                        val full = ImagePipeline.decode(resolver, source, 12_000, maxPixels)
                        decoded = full
                        val result = ImagePipeline.render(full, edits) { ensureActive() }
                        rendered = result
                        val writeBitmap = if (format == Bitmap.CompressFormat.JPEG && result.hasAlpha()) {
                            ImagePipeline.compositeWhite(result).also { flattened = it }
                        } else result
                        val done = withContext(Dispatchers.IO) {
                            resolver.openOutputStream(output, "wt")?.use { stream ->
                                writeBitmap.compress(format, quality, stream)
                            } ?: throw IOException("Save destination is inaccessible")
                        }
                        if (!done) throw IOException("Image encoder failed")
                        writeBitmap.width.toString() + " × " + writeBitmap.height
                    } finally {
                        flattened?.let { if (it !== rendered && !it.isRecycled) it.recycle() }
                        rendered?.let { if (it !== decoded && !it.isRecycled) it.recycle() }
                        decoded?.let { if (!it.isRecycled) it.recycle() }
                    }
                }
                notice = "Saved " + dimensions + " • Original preserved"
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: OutOfMemoryError) {
                notice = "Not enough memory to export. Close other apps and retry."
                try { resolver.delete(output, null, null) } catch (_: Exception) {}
            } catch (error: Exception) {
                notice = "Export failed: " + (error.message ?: "unknown error")
                try { resolver.delete(output, null, null) } catch (_: Exception) {}
            } finally {
                exporting = false
            }
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
    fun startPaint(point: BrushPoint) {
        val mask = document.masks.getOrNull(selectedMask) ?: return
        if (mask.kind != MaskKind.BRUSH) return
        if (mask.strokes.size >= 40 || mask.strokes.sumOf { it.points.size } >= 640) {
            notice = "Brush limit reached. Undo a stroke or add another mask."
            return
        }
        gestureStart = document
        val updated = document.masks.toMutableList()
        updated[selectedMask] = mask.copy(strokes = mask.strokes + BrushStroke(listOf(point)))
        document = document.copy(masks = updated)
    }
    fun continuePaint(point: BrushPoint) {
        val mask = document.masks.getOrNull(selectedMask) ?: return
        val last = mask.strokes.lastOrNull() ?: return
        if (gestureStart == null || mask.strokes.sumOf { it.points.size } >= 640) return
        val previous = last.points.last()
        val dx = point.x - previous.x
        val dy = point.y - previous.y
        if (dx * dx + dy * dy < .000012f) return
        val newStroke = last.copy(points = last.points + point)
        val updated = document.masks.toMutableList()
        updated[selectedMask] = mask.copy(strokes = mask.strokes.dropLast(1) + newStroke)
        document = document.copy(masks = updated)
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        if (it != null) loadPhoto(it)
    }
    val jpeg = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/jpeg")) {
        if (it != null) exportImage(it, Bitmap.CompressFormat.JPEG, jpegQuality)
    }
    val png = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) {
        if (it != null) exportImage(it, Bitmap.CompressFormat.PNG, 100)
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
        draftName = ""
        showSaveProject = true
    }
    fun openProjects() {
        savedProjects = ProjectStore.load(context)
        selectedProject = null
        showProjects = true
    }

    LaunchedEffect(original, document) {
        val input = original ?: run { processed = null; return@LaunchedEffect }
        delay(100L)
        try {
            val edited = withContext(Dispatchers.Default) {
                val running = currentCoroutineContext()
                ImagePipeline.render(input, document) { running.ensureActive() }
            }
            processed = edited
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            notice = "Preview error: " + (error.message ?: "unexpected error")
        }
    }

    // Keep before/after geometry identical; compute outside the UI thread.
    LaunchedEffect(original, document.quarterTurns, document.mirrored,
        document.flipVertical, document.crop, document.cropCenterX,
        document.cropCenterY, document.straightenDegrees) {
        val input = original ?: run { beforeImage = null; return@LaunchedEffect }
        try {
            val result = withContext(Dispatchers.Default) {
                ImagePipeline.geometry(input, document)
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

    val selectedImage = if (compare) beforeImage ?: original else processed ?: original
    val brushSelected = chromeVisible && !compare && !busy && !exporting &&
        selectedTab == EditTab.MASKS && zoom <= 1.001f &&
        document.masks.getOrNull(selectedMask)?.kind == MaskKind.BRUSH

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF080B10)).clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        if (selectedImage != null) {
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
                            if (!brushSelected) {
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
                val active = document.masks.getOrNull(selectedMask)
                if (active != null) {
                    BrushOverlay(
                        bitmapWidth = selectedImage.width,
                        bitmapHeight = selectedImage.height,
                        mask = active,
                        enabled = !busy,
                        onStart = ::startPaint,
                        onContinue = ::continuePaint,
                        onFinish = ::finishSlide,
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
                Text("GZ", color = EditorMint, fontSize = 52.sp, fontWeight = FontWeight.Black)
                Text(
                    "Your photo deserves the whole screen.",
                    color = EditorWhite, fontSize = 23.sp, fontWeight = FontWeight.SemiBold
                )
                Text(
                    "No cloud. No ads. No AI. Just editing.",
                    color = EditorMuted, fontSize = 14.sp
                )
                Button(
                    onClick = ::openPicker,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EditorMint, contentColor = Color(0xFF0B1E18)
                    )
                ) { Text("Open photo", fontWeight = FontWeight.Bold) }
            }
        }

        if (chromeVisible) {
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
                    Modifier.fillMaxWidth().height(52.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("GZ", color = EditorMint, fontSize = 21.sp,
                        fontWeight = FontWeight.Black)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = ::openPicker,
                            enabled = !busy && !exporting
                        ) { Text("Open", color = EditorWhite, fontSize = 13.sp) }
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
                            enabled = original != null && !busy && !exporting
                        ) {
                            Text(if (compare) "Edited" else "Before",
                                color = EditorWhite, fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = { showExport = true },
                            enabled = original != null && !busy && !exporting
                        ) {
                            Text("Export", color = EditorMint, fontWeight = FontWeight.Bold,
                                fontSize = 13.sp)
                        }
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
                            if (it == EditTab.MASKS) {
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
                        onOpen = ::openPicker
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
            jpegQuality = jpegQuality,
            onQuality = { jpegQuality = it },
            onJpeg = {
                showExport = false
                jpeg.launch("GZPhotoEditorPro_Edit.jpg")
            },
            onPng = {
                showExport = false
                png.launch("GZPhotoEditorPro_Edit.png")
            },
            onDismiss = { showExport = false }
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
                    showSaveProject = false
                    notice = "Project saved on your phone"
                } else notice = "Could not save project (twelve maximum)."
            },
            onDismiss = { showSaveProject = false }
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
}

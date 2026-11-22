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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import java.io.IOException

private enum class ProTransform(val title: String) {
    SIZE("Size"), OPACITY("Opacity"), HORIZONTAL("X"), VERTICAL("Y"), ROTATE("Rotate")
}
/** True independently editable bitmap/vector/adjustment layer workspace. */
@Composable
fun ProWorkspaceScreen(
    document: ProDocument,
    onDocument: (ProDocument) -> Unit,
    onBack: () -> Unit,
    onSwitch: (WorkspaceMode) -> Unit
) {
    val context = LocalContext.current
    val resolver = context.contentResolver
    val scope = rememberCoroutineScope()
    var selected by remember { mutableIntStateOf(0) }
    var section by remember { mutableStateOf(ProSection.LAYERS) }
    var transform by remember { mutableStateOf(ProTransform.SIZE) }
    var chrome by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(true) }
    var showTools by remember { mutableStateOf(false) }
    var showStack by remember { mutableStateOf(false) }
    var showFile by remember { mutableStateOf(false) }
    var rename by remember { mutableStateOf<String?>(null) }
    var viewport by remember(document.aspect) { mutableStateOf(ProViewport()) }
    var history by remember { mutableStateOf(ProHistory()) }
    var sliderStart by remember { mutableStateOf<ProDocument?>(null) }
    var lastSliderValue by remember { mutableStateOf<ProDocument?>(null) }
    var lastDragValue by remember { mutableStateOf<ProDocument?>(null) }
    var dragStart by remember { mutableStateOf<ProDocument?>(null) }
    var activeStroke by remember { mutableStateOf<InkStroke?>(null) }
    var brushColor by remember { mutableIntStateOf(0xFF96E5C2.toInt()) }
    var brushSize by remember { mutableFloatStateOf(.012f) }
    var brushOpacity by remember { mutableFloatStateOf(1f) }
    var eraserMode by remember { mutableStateOf(false) }
    var selectionKind by remember { mutableStateOf(ProSelectionKind.RECTANGLE) }
    var selectionStart by remember { mutableStateOf<BrushPoint?>(null) }
    var draftSelection by remember { mutableStateOf<ProSelection?>(null) }
    var retouchSource by remember { mutableStateOf<BrushPoint?>(null) }
    var retouchSourceImage by remember { mutableStateOf<BrushPoint?>(null) }
    var retouchKind by remember { mutableStateOf(ProRetouchKind.CLONE) }
    var retouchRadius by remember { mutableFloatStateOf(.04f) }
    var retouchStrength by remember { mutableFloatStateOf(1f) }
    var replacingImage by remember { mutableIntStateOf(-1) }
    var textOpen by remember { mutableStateOf(false) }
    var textDraft by remember { mutableStateOf("") }
    var showAdd by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var rendering by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf("") }
    var showExport by remember { mutableStateOf(false) }
    var exportSettings by remember { mutableStateOf(ExportSettings()) }
    var pendingExport by remember { mutableStateOf<Pair<ProDocument, ExportSettings>?>(null) }
    var showSave by remember { mutableStateOf(false) }
    var showProjects by remember { mutableStateOf(false) }
    var confirmNew by remember { mutableStateOf(false) }
    var draftName by remember { mutableStateOf("") }
    var savedNames by remember { mutableStateOf<List<String>>(emptyList()) }

    val active = document.layers.getOrNull(selected)

    fun clearToolDrafts() {
        selectionStart = null
        draftSelection = null
        retouchSource = null
        retouchSourceImage = null
        activeStroke = null
    }
    fun selectLayer(index: Int) {
        if (index != selected) clearToolDrafts()
        selected = index
    }
    fun selectTool(tool: ProSection) {
        if (tool != section) {
            selectionStart = null
            retouchSource = null
            retouchSourceImage = null
        }
        section = tool
        expanded = true
        showTools = false
    }
    fun commit(next: ProDocument) {
        history = history.checkpoint(document, next)
        sliderStart = null
        onDocument(next)
    }
    fun changeLayer(transformer: (ProLayer) -> ProLayer, sliding: Boolean = false) {
        if (selected !in document.layers.indices) return
        val next = document.copy(layers = document.layers.toMutableList().also {
            it[selected] = transformer(it[selected])
        })
        if (sliding) {
            if (sliderStart == null) sliderStart = document
            lastSliderValue = next
            onDocument(next)
        } else commit(next)
    }
    /** Trailing lambda convenience preserves one straightforward tool action per click. */
    fun changeLayer(transformer: (ProLayer) -> ProLayer) {
        changeLayer(transformer, false)
    }
    fun finishSlider() {
        sliderStart?.let { history = history.checkpoint(it, lastSliderValue ?: document) }
        lastSliderValue = null
        sliderStart = null
    }
    fun addLayer(layer: ProLayer) {
        val next = ProMath.insert(document, layer)
        if (next == null) { notice = "A project supports up to 12 layers."; return }
        commit(next)
        selectLayer(next.layers.lastIndex)
        expanded = true
        section = when (layer.kind) {
            ProLayerKind.TEXT, ProLayerKind.SHAPE -> ProSection.STYLE
            ProLayerKind.DRAWING -> ProSection.PAINT
            ProLayerKind.ADJUSTMENT -> ProSection.PIXEL
            else -> ProSection.LAYERS
        }
    }
    fun addCreative(kind: ProLayerKind) {
        val label = kind.label + " " + (document.layers.size + 1)
        val edits = when (kind) {
            ProLayerKind.TEXT -> EditDocument(creative = CreativeDocument(
                text = listOf(TextOverlay(content = "Your text", color = 0xFF111111.toInt()))))
            ProLayerKind.SHAPE -> EditDocument(creative = CreativeDocument(
                shapes = listOf(ShapeOverlay(kind = ShapeKind.RECTANGLE, color = 0xFF111111.toInt()))))
            else -> EditDocument()
        }
        addLayer(ProLayer(uri = "", kind = kind, edits = edits, name = label,
            effect = if (kind == ProLayerKind.ADJUSTMENT)
                ProPixelEffect.GRAYSCALE else ProPixelEffect.NONE))
        showAdd = false
        if (kind == ProLayerKind.TEXT) {
            textDraft = "Your text"
            textOpen = true
        }
    }

    BackHandler {
        if (!chrome) chrome = true
        else if (!busy && !exporting) onBack()
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(ProMath.MAX_LAYERS)
    ) { uris ->
        val remaining = ProMath.MAX_LAYERS - document.layers.size
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
                            val small = ImagePipeline.decode(resolver, uri, 200, 50_000L)
                            val aspect = try { small.width.toFloat() / small.height }
                                finally { if (!small.isRecycled) small.recycle() }
                            ProLayer(uri = uri.toString(),
                                name = "Image " + (document.layers.size + index + 1),
                                aspect = aspect.coerceIn(.1f, 10f))
                        }
                    }
                    if (layers.isNotEmpty()) {
                        commit(document.copy(layers = document.layers + layers))
                        selected = document.layers.size + layers.lastIndex
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
    }
    fun openImagePicker() {
        if (!busy && !exporting && document.layers.size < ProMath.MAX_LAYERS) {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else notice = "Maximum of 12 layers reached."
    }
    val replacePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val index = replacingImage
        replacingImage = -1
        if (uri != null && index in document.layers.indices &&
            document.layers[index].kind == ProLayerKind.IMAGE) {
            try {
                resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {}
            scope.launch {
                busy = true
                try {
                    val aspect = withContext(Dispatchers.IO) {
                        val sample = ImagePipeline.decode(resolver, uri, 200, 50_000L)
                        try { (sample.width.toFloat() / sample.height).coerceIn(.1f, 10f) }
                        finally { sample.recycle() }
                    }
                    commit(document.copy(layers = document.layers.toMutableList().also {
                        it[index] = it[index].copy(uri = uri.toString(), aspect = aspect)
                    }))
                    selected = index
                    clearToolDrafts()
                    notice = "Source replaced. Layer edits preserved."
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: Exception) {
                    notice = "Could not read replacement image."
                } finally { busy = false }
            }
        }
    }

    fun export(destination: Uri, request: Pair<ProDocument, ExportSettings>) {
        if (request.first.layers.any { it.uri.isNotEmpty() && it.uri == destination.toString() }) {
            notice = "Export cannot overwrite an original image."
            return
        }
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val budget = GeometryMath.safeExportPixels(manager.memoryClass)
        scope.launch {
            exporting = true
            notice = "Exporting editable composition…"
            try {
                val result = withContext(Dispatchers.Default) {
                    var image: Bitmap? = null
                    var sized: Bitmap? = null
                    try {
                        val (w, h) = ProMath.dimensions(request.first.aspect, 4096, budget)
                        val coroutine = currentCoroutineContext()
                        image = ProRaster.render(resolver, request.first, w, h, false) {
                            coroutine.ensureActive()
                        }
                        sized = ExportSizing.layout(image, request.second)
                        val success = withContext(Dispatchers.IO) {
                            resolver.openOutputStream(destination, "wt")?.use {
                                ImageExportWriter.write(sized, request.second, it)
                            } ?: throw IOException("Output file cannot be opened")
                        }
                        if (!success) throw IOException("Export encoder returned false")
                        sized.width.toString() + " × " + sized.height
                    } finally {
                        sized?.let { if (it !== image && !it.isRecycled) it.recycle() }
                        image?.let { if (!it.isRecycled) it.recycle() }
                    }
                }
                notice = "Exported " + result + ". Original media unchanged."
            } catch (cancelled: CancellationException) {
                try { resolver.delete(destination, null, null) } catch (_: Exception) {}
                throw cancelled
            } catch (_: OutOfMemoryError) {
                notice = "Not enough device memory. Try a smaller image."
                try { resolver.delete(destination, null, null) } catch (_: Exception) {}
            } catch (failure: Exception) {
                notice = "Export failed: " + (failure.message ?: "source unavailable")
                try { resolver.delete(destination, null, null) } catch (_: Exception) {}
            } finally { exporting = false }
        }
    }
    val exporter = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { response ->
        val request = pendingExport
        pendingExport = null
        val uri = response.data?.data
        if (response.resultCode == Activity.RESULT_OK && uri != null && request != null) {
            export(uri, request)
        }
    }
    fun startExport() {
        if (!ProMath.valid(document)) return
        pendingExport = document to exportSettings
        showExport = false
        exporter.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = exportSettings.format.mime
            putExtra(Intent.EXTRA_TITLE, "GZEditorPro_Pro." + exportSettings.format.extension)
        })
    }

    // A hardware display list can retain the previous preview after recomposition.
    // Displayed bitmaps are framework/GC owned; only unpublished scratch images are recycled.
    val latestDocument by rememberUpdatedState(document)
    var previewEdge by remember { mutableIntStateOf(940) }
    val memoryClass = remember(context) {
        (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).memoryClass
    }
    val maxPreviewPixels = when {
        memoryClass >= 256 -> 3_000_000L
        memoryClass >= 128 -> 2_000_000L
        else -> 1_200_000L
    }
    LaunchedEffect(memoryClass, document.aspect) {
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
    LaunchedEffect(resolver, maxPreviewPixels) {
        snapshotFlow { latestDocument to previewEdge }.conflate().collect { (frame, edge) ->
            if (frame.layers.isEmpty()) { preview = null; chrome = true; return@collect }
            rendering = true
            try {
                val (w, h) = ProMath.dimensions(frame.aspect, edge, maxPreviewPixels)
                val bitmap = withContext(Dispatchers.Default) {
                    val job = currentCoroutineContext()
                    val rendered = ProRaster.render(resolver, frame, w, h, true) {
                        job.ensureActive()
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
                selected = missing.position
                notice = "Source for image layer " + (missing.position + 1) + " is unavailable."
            } catch (_: OutOfMemoryError) {
                notice = "Preview has too many pixels for available memory."
            } catch (failure: Exception) {
                notice = "Preview failed: " + (failure.message ?: "unknown problem")
            } finally { rendering = false }
        }
    }
    LaunchedEffect(notice) {
        if (notice.isNotBlank()) {
            delay(5000L); notice = ""
        }
    }

    val available = !busy && !exporting
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFF080B10))) {
        val panelHeight = maxHeight * .39f
        val safeArea = if (chrome) Modifier.statusBarsPadding().navigationBarsPadding() else Modifier
        Column(Modifier.fillMaxSize().then(safeArea)) {
            if (chrome) {
                Row(Modifier.fillMaxWidth().background(EditorSurface), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            TextButton(onClick = { showFile = true }, enabled = available) { Text("File ▾") }
                            DropdownMenu(expanded = showFile, onDismissRequest = { showFile = false }) {
                                DropdownMenuItem(text = { Text("Open project") }, onClick = {
                                    showFile = false; savedNames = ProStore.names(context); showProjects = true
                                })
                                DropdownMenuItem(text = { Text("Save project") }, enabled = ProMath.valid(document),
                                    onClick = { showFile = false; showSave = true })
                                DropdownMenuItem(text = { Text("Export image") }, enabled = ProMath.valid(document),
                                    onClick = { showFile = false; showExport = true })
                                DropdownMenuItem(text = { Text("Fit canvas") }, enabled = document.layers.isNotEmpty(),
                                    onClick = { showFile = false; viewport = ProViewport() })
                                DropdownMenuItem(text = { Text("Pro help") }, onClick = {
                                    showFile = false; showHelp = true
                                })
                                DropdownMenuItem(text = { Text("Back") }, onClick = { showFile = false; onBack() })
                            }
                        }
                        TextButton(onClick = { showAdd = true }, enabled = available &&
                            document.layers.size < ProMath.MAX_LAYERS) { Text("+ Add layer", color = EditorMint) }
                        TextButton(enabled = available && history.undo.isNotEmpty(), onClick = {
                            history.undo(document)?.let { (before, next) ->
                                clearToolDrafts(); history = next; onDocument(before)
                                selected = selected.coerceIn(0, before.layers.lastIndex.coerceAtLeast(0))
                            }
                        }) { Text("Undo") }
                        TextButton(enabled = available && history.redo.isNotEmpty(), onClick = {
                            history.redo(document)?.let { (after, next) ->
                                clearToolDrafts(); history = next; onDocument(after)
                                selected = selected.coerceIn(0, after.layers.lastIndex.coerceAtLeast(0))
                            }
                        }) { Text("Redo") }
                    }
                    WorkspaceModeMenu(WorkspaceMode.PRO, available, onSwitch)
                }
                if (notice.isNotBlank()) Text(notice, color = EditorMint, fontSize = 12.sp,
                    maxLines = 2, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp))
            }
            Box(Modifier.weight(1f).fillMaxWidth().clipToBounds(), contentAlignment = Alignment.Center) {
                if (document.layers.isEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text("Photo editing Pro", color = EditorWhite,
                            fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Text("Create with real image, text, shape, brush and adjustment layers.",
                            color = EditorMuted, fontSize = 13.sp)
                        Button(onClick = ::openImagePicker,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EditorMint, contentColor = Color(0xFF0C2118))) {
                            Text("Add first photo")
                        }
                        TextButton(onClick = { addCreative(ProLayerKind.DRAWING) }) {
                            Text("Or start with a blank canvas", color = EditorWhite)
                        }
                    }
                } else {
                    preview?.let { bitmap ->
                        ProCanvasSurface(bitmap, section, active, viewport, chrome, available,
                            draftSelection, activeStroke, retouchSource,
                            actions = ProCanvasActions(
                                toggleControls = { chrome = !chrome },
                                hold = { point ->
                                    if (section == ProSection.LAYERS) {
                                        ProHitMath.pick(document, point.x, point.y, bitmap.width, bitmap.height)
                                            ?.let(::selectLayer)
                                    } else if (section == ProSection.PAINT && active?.kind == ProLayerKind.DRAWING && !eraserMode) {
                                        val x = (point.x * bitmap.width).toInt().coerceIn(0, bitmap.width - 1)
                                        val y = (point.y * bitmap.height).toInt().coerceIn(0, bitmap.height - 1)
                                        brushColor = bitmap.getPixel(x, y) or -0x1000000
                                        notice = "Brush color sampled from canvas."
                                    } else if (section == ProSection.RETOUCH && active?.kind == ProLayerKind.IMAGE) {
                                        val target = ProMath.canvasToImageNormalized(active, bitmap.width, bitmap.height, point)
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
                                    dragStart = document
                                    if (section == ProSection.PAINT && active?.kind == ProLayerKind.DRAWING &&
                                        active.visible && active.edits.creative.strokes.size < CreativeRaster.MAX_INK)
                                        activeStroke = InkStroke(listOf(point), color = brushColor,
                                            thickness = brushSize, opacity = brushOpacity, erase = eraserMode)
                                    if (section == ProSection.SELECT && (active?.kind == ProLayerKind.IMAGE ||
                                            active?.kind == ProLayerKind.ADJUSTMENT)) {
                                        selectionStart = point
                                        draftSelection = if (selectionKind == ProSelectionKind.LASSO)
                                            ProSelection(selectionKind, points = listOf(point))
                                        else ProToolMath.rectangle(point, point, selectionKind)
                                    }
                                },
                                drag = { start, point ->
                                    val before = dragStart
                                    val item = before?.layers?.getOrNull(selected)
                                    if (before != null && item != null) when (section) {
                                        ProSection.MOVE -> if (item.visible && item.kind in listOf(
                                            ProLayerKind.IMAGE, ProLayerKind.TEXT, ProLayerKind.SHAPE)) {
                                            val moved = item.copy(x = (item.x + point.x - start.x).coerceIn(0f, 1f),
                                                y = (item.y + point.y - start.y).coerceIn(0f, 1f))
                                            val next = before.copy(layers = before.layers.toMutableList().also {
                                                it[selected] = moved
                                            })
                                            lastDragValue = next
                                            onDocument(next)
                                        }
                                        ProSection.PAINT -> activeStroke?.let { stroke ->
                                            if (stroke.points.size < 500 && stroke.points.last() != point)
                                                activeStroke = stroke.copy(points = stroke.points + point)
                                        }
                                        ProSection.SELECT -> if (selectionStart != null) {
                                            draftSelection = if (selectionKind == ProSelectionKind.LASSO) {
                                                val points = draftSelection?.points.orEmpty()
                                                if (points.size < ProToolMath.MAX_LASSO_POINTS)
                                                    ProSelection(selectionKind, points = points + point)
                                                else draftSelection
                                            } else ProToolMath.rectangle(start, point, selectionKind)
                                        }
                                        else -> Unit
                                    }
                                },
                                dragEnd = { cancel ->
                                    val before = dragStart
                                    if (cancel) {
                                        if (section == ProSection.MOVE && before != null) onDocument(before)
                                        if (selectionStart != null) draftSelection = null
                                    } else if (before != null && section == ProSection.MOVE) {
                                        history = history.checkpoint(before, lastDragValue ?: before)
                                    } else if (section == ProSection.PAINT) {
                                        activeStroke?.let { stroke ->
                                            changeLayer { layer -> layer.copy(edits = layer.edits.copy(
                                                creative = layer.edits.creative.copy(strokes =
                                                    layer.edits.creative.strokes + stroke))) }
                                        }
                                    }
                                    lastDragValue = null
                                    dragStart = null
                                    activeStroke = null
                                    selectionStart = null
                                },
                                changeView = { viewport = it }
                            ), modifier = Modifier.fillMaxSize())
                    }
                }
            }
            if (chrome && document.layers.isNotEmpty()) {
                Row(Modifier.fillMaxWidth().background(EditorSurface),
                    verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { showTools = true }, enabled = available) { Text("Tools ▾", color = EditorMint) }
                    TextButton(onClick = { showStack = true }, enabled = available) {
                        Text("Stack (" + document.layers.size + ")")
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { expanded = !expanded }, enabled = available) {
                        Text(section.title + if (expanded) " ▴" else " ▾", maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                    }
                }
                if (expanded) {
                    Column(Modifier.fillMaxWidth().heightIn(max = panelHeight)
                        .background(EditorSurface).verticalScroll(rememberScrollState())
                        .padding(bottom = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(active?.name.orEmpty() + " · " + (active?.kind?.label ?: ""),
                            color = EditorMuted, fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 13.dp))
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
                                        TextButton(onClick = {
                                            section = ProSection.SELECT
                                        }) { Text("Select / mask", color = EditorMint) }
                                        TextButton(onClick = {
                                            section = ProSection.RETOUCH
                                        }) { Text("Clone / repair", color = EditorMint) }
                                        TextButton(onClick = {
                                            replacingImage = selected
                                            replacePicker.launch(PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        }) { Text("Replace image", color = EditorWhite) }
                                    }
                                    if (active.kind == ProLayerKind.TEXT) {
                                        TextButton(onClick = {
                                            textDraft = active.edits.creative.text.firstOrNull()?.content ?: ""
                                            textOpen = true
                                        }) { Text("Edit text", color = EditorMint) }
                                    }
                                    TextButton(onClick = { changeLayer { it.copy(visible = !it.visible) } }) {
                                        Text(if (active.visible) "Hide" else "Show", color = EditorWhite)
                                    }
                                    TextButton(enabled = selected > 0,
                                        onClick = {
                                            commit(document.copy(layers =
                                                ProMath.move(document.layers, selected, selected - 1)))
                                            selected--
                                        }) { Text("↓ Behind", color = EditorWhite) }
                                    TextButton(enabled = selected < document.layers.lastIndex,
                                        onClick = {
                                            commit(document.copy(layers =
                                                ProMath.move(document.layers, selected, selected + 1)))
                                            selected++
                                        }) { Text("↑ Above", color = EditorWhite) }
                                    TextButton(enabled = document.layers.size < ProMath.MAX_LAYERS,
                                        onClick = {
                                            val copy = active.copy(name = (active.name.take(32) + " copy"))
                                            val next = document.layers.toMutableList().also {
                                                it.add(selected + 1, copy)
                                            }
                                            commit(document.copy(layers = next))
                                            selected++
                                        }) { Text("Duplicate", color = EditorWhite) }
                                    TextButton(onClick = {
                                        commit(document.copy(layers =
                                            document.layers.filterIndexed { index, _ -> index != selected }))
                                        selected = selected.coerceAtMost(document.layers.size - 2).coerceAtLeast(0)
                                    }) { Text("Delete", color = Color(0xFFFF9D9D)) }
                                }
                            }
                            ProSection.MOVE -> {
                                if (active.kind == ProLayerKind.DRAWING ||
                                    active.kind == ProLayerKind.ADJUSTMENT) {
                                    Text("Move is for image, text and shape layers. Draw directly in Brush.",
                                        color = EditorMuted, fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp))
                                } else {
                                    Row(Modifier.horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        ProTransform.entries.forEach { option ->
                                            DockChip(option.title, transform == option) {
                                                transform = option
                                            }
                                        }
                                        DockChip("Center on canvas", false) {
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
                                if (active.kind == ProLayerKind.TEXT) {
                                    val textLayer = active.edits.creative.text.firstOrNull()
                                    if (textLayer != null) {
                                        Row(Modifier.horizontalScroll(rememberScrollState())
                                            .padding(horizontal = 10.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            DockChip("Edit words", false) {
                                                textDraft = textLayer.content; textOpen = true
                                            }
                                            TextFont.entries.forEach { font ->
                                                DockChip(font.label, textLayer.font == font) {
                                                    changeLayer { item -> item.copy(edits = item.edits.copy(
                                                        creative = item.edits.creative.copy(text =
                                                            listOf(textLayer.copy(font = font))))) }
                                                }
                                            }
                                            DockChip("Bold", textLayer.bold) {
                                                changeLayer { item -> item.copy(edits = item.edits.copy(
                                                    creative = item.edits.creative.copy(text =
                                                        listOf(textLayer.copy(bold = !textLayer.bold))))) }
                                            }
                                        }
                                        Text("Text size", color = EditorMuted, fontSize = 11.sp,
                                            modifier = Modifier.padding(start = 14.dp))
                                        Slider(value = textLayer.size.coerceIn(.02f, .18f),
                                            valueRange = .02f.. .18f,
                                            onValueChange = { value ->
                                                changeLayer({ item -> item.copy(edits = item.edits.copy(
                                                    creative = item.edits.creative.copy(text =
                                                        listOf(textLayer.copy(size = value))))) }, true)
                                            }, onValueChangeFinished = ::finishSlider,
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp))
                                    }
                                } else if (active.kind == ProLayerKind.SHAPE) {
                                    val shape = active.edits.creative.shapes.firstOrNull()
                                    if (shape != null) {
                                        Row(Modifier.horizontalScroll(rememberScrollState())
                                            .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            ShapeKind.entries.forEach { shapeKind ->
                                                DockChip(shapeKind.label, shape.kind == shapeKind) {
                                                    changeLayer { item -> item.copy(edits = item.edits.copy(
                                                        creative = item.edits.creative.copy(shapes =
                                                            listOf(shape.copy(kind = shapeKind))))) }
                                                }
                                            }
                                            DockChip("Filled", shape.filled) {
                                                changeLayer { item -> item.copy(edits = item.edits.copy(
                                                    creative = item.edits.creative.copy(shapes =
                                                        listOf(shape.copy(filled = !shape.filled))))) }
                                            }
                                        }
                                    }
                                } else {
                                    Row(Modifier.horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(enabled = document.layers.size < ProMath.MAX_LAYERS,
                                            onClick = { addCreative(ProLayerKind.TEXT) }) {
                                            Text("+ Text layer", color = EditorMint)
                                        }
                                        TextButton(enabled = document.layers.size < ProMath.MAX_LAYERS,
                                            onClick = { addCreative(ProLayerKind.SHAPE) }) {
                                            Text("+ Shape layer", color = EditorMint)
                                        }
                                    }
                                }
                                if (active.kind == ProLayerKind.TEXT || active.kind == ProLayerKind.SHAPE) {
                                    val text = active.edits.creative.text.firstOrNull()
                                    val shape = active.edits.creative.shapes.firstOrNull()
                                    ProColorPalette(text?.color ?: shape?.color ?: -1) { color ->
                                        changeLayer { item ->
                                            val creative = item.edits.creative
                                            val updated = when {
                                                text != null -> creative.copy(text = listOf(text.copy(color = color)))
                                                shape != null -> creative.copy(shapes = listOf(shape.copy(color = color)))
                                                else -> creative
                                            }
                                            item.copy(edits = item.edits.copy(creative = updated))
                                        }
                                    }
                                }
                            }
                            ProSection.PAINT -> {
                                if (active.kind == ProLayerKind.DRAWING) {
                                    Row(Modifier.horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 9.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        DockChip("Paint", !eraserMode) { eraserMode = false }
                                        DockChip("Eraser", eraserMode) { eraserMode = true }
                                    }
                                    Text(if (eraserMode)
                                        "Drag to erase pixels from this drawing layer."
                                        else "Drag to paint. Hold a color on the canvas to sample it.",
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
                                    enabled = document.layers.size < ProMath.MAX_LAYERS,
                                    onClick = { addCreative(ProLayerKind.DRAWING) }
                                ) { Text("+ Draw on new layer", color = EditorMint) }
                            }
                            ProSection.SELECT -> ProSelectionPanel(
                                layer = active,
                                selectedKind = selectionKind,
                                onKind = {
                                    selectionKind = it
                                    draftSelection = null
                                    selectionStart = null
                                },
                                draft = draftSelection,
                                onApply = {
                                    val selectedMask = draftSelection
                                    if (selectedMask != null && ProToolMath.valid(selectedMask)) {
                                        changeLayer { layer -> layer.copy(mask = ProToolMath.applySelection(
                                            selectedMask, layer, document.aspect.ratio)) }
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
                                onAdd = { addCreative(ProLayerKind.ADJUSTMENT) },
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
                                canAdd = document.layers.size < ProMath.MAX_LAYERS,
                                onAdd = {
                                    addLayer(ProLayer(uri = "", name = "Tone & color",
                                        kind = ProLayerKind.ADJUSTMENT))
                                    selectTool(ProSection.GRADE)
                                },
                                onGrade = { grade ->
                                    changeLayer({ it.copy(edits = it.edits.copy(grade = grade)) }, true)
                                },
                                onCommitGrade = { grade -> changeLayer { it.copy(edits = it.edits.copy(grade = grade)) } },
                                onFinish = ::finishSlider
                            )
                            ProSection.BLEND -> {
                                Row(Modifier.horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    ProBlend.entries.forEach { blend ->
                                        DockChip(blend.label, active.blend == blend) {
                                            changeLayer { it.copy(blend = blend) }
                                        }
                                    }
                                }
                                Text("Opacity · ${(active.opacity * 100).toInt()}%", color = EditorMuted,
                                    fontSize = 12.sp, modifier = Modifier.padding(horizontal = 13.dp))
                                Slider(value = active.opacity, onValueChange = { opacity ->
                                    changeLayer({ it.copy(opacity = opacity) }, true)
                                }, onValueChangeFinished = ::finishSlider,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp))
                                Text("Blend and opacity change only the selected layer.",
                                    color = EditorMuted, fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp))
                            }
                            ProSection.CANVAS -> {
                                Row(Modifier.horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    ProAspect.entries.forEach { aspect ->
                                        DockChip(aspect.label, document.aspect == aspect) {
                                            commit(document.copy(aspect = aspect))
                                        }
                                    }
                                }
                                Row(Modifier.horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    ProBackground.entries.forEach { background ->
                                        DockChip(background.label, document.background == background) {
                                            commit(document.copy(background = background))
                                        }
                                    }
                                    DockChip("New project", false) { confirmNew = true }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (busy || exporting || (rendering && preview == null)) {
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
    if (showStack) ProLayerStack(document, selected, onSelect = {
        selectLayer(it); showStack = false; selectTool(ProSection.LAYERS)
    }, onVisibility = { index ->
        val layers = document.layers.toMutableList()
        layers[index] = layers[index].copy(visible = !layers[index].visible)
        commit(document.copy(layers = layers))
    }, onAdd = { showStack = false; showAdd = true }, onDismiss = { showStack = false })
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
                TextButton(onClick = { addCreative(ProLayerKind.TEXT) }) {
                    Text("Words / Text", color = EditorWhite)
                }
                TextButton(onClick = { addCreative(ProLayerKind.SHAPE) }) {
                    Text("Shape", color = EditorWhite)
                }
                TextButton(onClick = { addCreative(ProLayerKind.DRAWING) }) {
                    Text("Draw / Brush", color = EditorWhite)
                }
                TextButton(onClick = { addCreative(ProLayerKind.ADJUSTMENT) }) {
                    Text("Color adjustment", color = EditorWhite)
                }
            }
        },
        confirmButton = { TextButton(onClick = { showAdd = false }) {
            Text("Close", color = EditorMuted)
        } }
    )
    if (textOpen) TextEditDialog(
        content = textDraft,
        onContent = { textDraft = it },
        onSave = {
            val value = textDraft.trim().take(500)
            if (value.isNotBlank()) {
                changeLayer { layer ->
                    val current = layer.edits.creative.text.firstOrNull() ?: TextOverlay()
                    layer.copy(edits = layer.edits.copy(
                        creative = layer.edits.creative.copy(
                            text = listOf(current.copy(content = value)))))
                }
                textOpen = false
            }
        },
        onDismiss = { textOpen = false }
    )
    if (showExport) ExportDialog(
        settings = exportSettings, onSettings = { exportSettings = it },
        onSave = ::startExport, onDismiss = { showExport = false }
    )
    if (showSave) SaveNameDialog(
        title = "Save Pro project",
        description = "Preserves individual layers, order, adjustments and local image references.",
        name = draftName, onName = { draftName = it },
        onSave = {
            if (ProStore.save(context, draftName, document)) {
                showSave = false; notice = "Editable Pro project saved"
            } else notice = "Could not save. Name required; 12 projects maximum."
        },
        onDismiss = { showSave = false }
    )
    if (showProjects) AlertDialog(
        onDismissRequest = { showProjects = false },
        title = { Text("Saved Pro projects") },
        text = {
            Column(Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                if (savedNames.isEmpty()) Text("No saved Pro projects yet.", color = EditorMuted)
                savedNames.forEach { name ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = {
                            val restored = ProStore.load(context, name)
                            if (restored == null) notice = "Project unreadable or invalid."
                            else {
                                commit(restored); selected = 0; showProjects = false
                                notice = "Pro project opened"
                            }
                        }) { Text(name, color = EditorMint) }
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = {
                            ProStore.delete(context, name)
                            savedNames = ProStore.names(context)
                        }) { Text("Delete", color = Color(0xFFFF9D9D)) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { showProjects = false }) {
            Text("Close")
        } }
    )
    if (confirmNew) AlertDialog(
        onDismissRequest = { confirmNew = false },
        title = { Text("Start a new Pro project?") },
        text = { Text("Save the current project first if you want it later. Undo may restore unsaved edits.",
            color = EditorMuted) },
        confirmButton = { TextButton(onClick = {
            commit(ProDocument()); selected = 0; confirmNew = false
        }) { Text("Start new", color = EditorMint) } },
        dismissButton = { TextButton(onClick = { confirmNew = false }) { Text("Keep current") } }
    )
    if (showHelp) ProHelpDialog(onDismiss = { showHelp = false })
}

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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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

private enum class ProSection(val title: String) {
    LAYERS("Layers"), MOVE("Transform"), SELECT("Select & mask"),
    RETOUCH("Clone & repair"), PAINT("Brush"), STYLE("Text & shapes"),
    PIXEL("Layer effects"), BLEND("Blend"), CANVAS("Canvas")
}
private enum class ProTransform(val title: String) {
    SIZE("Size"), OPACITY("Opacity"), HORIZONTAL("X"), VERTICAL("Y"), ROTATE("Rotate")
}
private val proColors = listOf(
    "White" to 0xFFFFFFFF.toInt(), "Black" to 0xFF000000.toInt(),
    "Mint" to 0xFF96E5C2.toInt(), "Red" to 0xFFFF5555.toInt(),
    "Blue" to 0xFF368FFF.toInt(), "Yellow" to 0xFFFFD95E.toInt(),
    "Purple" to 0xFFA878FF.toInt()
)
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
    var history by remember { mutableStateOf(ProHistory()) }
    var sliderStart by remember { mutableStateOf<ProDocument?>(null) }
    var dragStart by remember { mutableStateOf<ProDocument?>(null) }
    var activeStroke by remember { mutableStateOf<InkStroke?>(null) }
    var brushColor by remember { mutableIntStateOf(0xFF96E5C2.toInt()) }
    var brushSize by remember { mutableFloatStateOf(.012f) }
    var eraserMode by remember { mutableStateOf(false) }
    var selectionKind by remember { mutableStateOf(ProSelectionKind.RECTANGLE) }
    var selectionStart by remember { mutableStateOf<BrushPoint?>(null) }
    var draftSelection by remember { mutableStateOf<ProSelection?>(null) }
    var retouchSource by remember { mutableStateOf<BrushPoint?>(null) }
    var retouchSourceImage by remember { mutableStateOf<BrushPoint?>(null) }
    var retouchKind by remember { mutableStateOf(ProRetouchKind.CLONE) }
    var retouchRadius by remember { mutableFloatStateOf(.04f) }
    var retouchStrength by remember { mutableFloatStateOf(.85f) }
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
            onDocument(next)
        } else commit(next)
    }
    /** Trailing lambda convenience preserves one straightforward tool action per click. */
    fun changeLayer(transformer: (ProLayer) -> ProLayer) {
        changeLayer(transformer, false)
    }
    fun finishSlider() {
        sliderStart?.let { history = history.checkpoint(it, document) }
        sliderStart = null
    }
    fun addLayer(layer: ProLayer) {
        val next = ProMath.insert(document, layer)
        if (next == null) { notice = "A project supports up to 12 layers."; return }
        commit(next)
        selected = next.layers.lastIndex
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

    BackHandler { if (!busy && !exporting) onBack() }

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
            commit(document.copy(layers = document.layers.toMutableList().also {
                it[index] = it[index].copy(uri = uri.toString(), edits = EditDocument())
            }))
            selected = index
            notice = "Image replaced. Its previous photo adjustments were cleared."
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

    DisposableEffect(preview) {
        val owned = preview
        onDispose { if (owned != null && !owned.isRecycled) owned.recycle() }
    }
    LaunchedEffect(document) {
        if (document.layers.isEmpty()) { preview = null; return@LaunchedEffect }
        delay(if (dragStart != null || sliderStart != null) 55L else 90L)
        rendering = true
        try {
            val (w, h) = ProMath.dimensions(document.aspect, 940, 950_000L)
            val bitmap = withContext(Dispatchers.Default) {
                val job = currentCoroutineContext()
                val rendered = ProRaster.render(resolver, document, w, h, true) {
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
    LaunchedEffect(notice) {
        if (notice.isNotBlank()) {
            delay(5000L); notice = ""
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF080B10)),
        contentAlignment = Alignment.Center) {
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
                Image(bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Pro editable composition",
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                if (section == ProSection.LAYERS && !busy && !exporting) {
                    ProTapToSelect(
                        document = document,
                        selected = selected,
                        canvasWidth = bitmap.width,
                        canvasHeight = bitmap.height,
                        onSelect = { index ->
                            selected = index
                            draftSelection = null
                            retouchSource = null
                            retouchSourceImage = null
                            notice = "Selected " + document.layers[index].name
                        }
                    )
                }
                if ((section == ProSection.MOVE || section == ProSection.PAINT) &&
                    active != null && !busy && !exporting) {
                    val canvasAction = if (section == ProSection.PAINT)
                        ProCanvasAction.BRUSH else ProCanvasAction.MOVE
                    ProCanvasOverlay(
                        canvasWidth = bitmap.width, canvasHeight = bitmap.height,
                        selected = active, action = canvasAction,
                        activeStroke = activeStroke,
                        onBegin = { point ->
                            dragStart = document
                            if (canvasAction == ProCanvasAction.BRUSH &&
                                active.kind == ProLayerKind.DRAWING) {
                                activeStroke = InkStroke(listOf(point),
                                    color = brushColor, thickness = brushSize,
                                    erase = eraserMode)
                            }
                        },
                        onMove = { point ->
                            val before = dragStart ?: document
                            if (selected in before.layers.indices) {
                                val layers = before.layers.toMutableList()
                                val layer = layers[selected]
                                if (canvasAction == ProCanvasAction.MOVE) {
                                    layers[selected] = layer.copy(x = point.x, y = point.y)
                                } else {
                                    val prior = layer.edits.creative.strokes
                                    val stroke = activeStroke
                                    if (stroke != null && prior.size < CreativeRaster.MAX_INK &&
                                        stroke.points.size < 500) {
                                        val update = stroke.copy(points = stroke.points + point)
                                        activeStroke = update
                                        layers[selected] = layer.copy(edits = layer.edits.copy(
                                            creative = layer.edits.creative.copy(
                                                strokes = prior + update)))
                                    }
                                }
                                onDocument(before.copy(layers = layers))
                            }
                        },
                        onFinish = {
                            dragStart?.let { history = history.checkpoint(it, document) }
                            dragStart = null
                            activeStroke = null
                        }
                    )
                }
                if ((section == ProSection.SELECT || section == ProSection.RETOUCH) &&
                    active?.kind == ProLayerKind.IMAGE && !busy && !exporting) {
                    ProPrecisionOverlay(
                        imageWidth = bitmap.width, imageHeight = bitmap.height,
                        action = if (section == ProSection.SELECT)
                            ProPrecisionAction.SELECT else ProPrecisionAction.RETOUCH,
                        kind = selectionKind,
                        draft = draftSelection,
                        committed = active.mask,
                        sourceMarker = retouchSource,
                        onStartSelection = { point ->
                            selectionStart = point
                            draftSelection = if (selectionKind == ProSelectionKind.LASSO)
                                ProSelection(ProSelectionKind.LASSO,
                                    points = listOf(point, point, point))
                            else ProToolMath.rectangle(point, point, selectionKind)
                        },
                        onMoveSelection = { point ->
                            val start = selectionStart ?: point
                            draftSelection = if (selectionKind == ProSelectionKind.LASSO) {
                                val prior = draftSelection?.points ?: listOf(start,start,start)
                                ProSelection(ProSelectionKind.LASSO,
                                    points = (prior + point)
                                        .take(ProToolMath.MAX_LASSO_POINTS))
                            } else ProToolMath.rectangle(start, point, selectionKind)
                        },
                        onEndSelection = { selectionStart = null },
                        onRetouchTap = { point ->
                            val local = ProMath.canvasToImageNormalized(
                                active, bitmap.width, bitmap.height, point)
                            if (local == null) {
                                notice = "Tap inside the selected image to sample/repair."
                            } else if (retouchSourceImage == null) {
                                retouchSource = point // Canvas coordinates for marker.
                                retouchSourceImage = local // Source-image coordinates for edit.
                                notice = "Source set. Tap the image area to clone/repair."
                            } else {
                                if (active.retouches.size >= ProToolMath.MAX_RETOUCH) {
                                    notice = "Maximum 80 retouch marks per image."
                                } else {
                                    val source = retouchSourceImage!!
                                    val mark = ProRetouchMark(
                                        retouchKind, source.x, source.y,
                                        local.x, local.y,
                                        radius = retouchRadius, strength = retouchStrength)
                                    changeLayer { layer ->
                                        layer.copy(retouches = layer.retouches + mark)
                                    }
                                    notice = "Retouch applied. Choose another source."
                                }
                                retouchSource = null
                                retouchSourceImage = null
                            }
                        }
                    )
                }
            }
        }

        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth()
            .background(Color(0xED111820)).statusBarsPadding()
            .padding(horizontal = 8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onBack, enabled = !busy && !exporting) {
                    Text("‹ Back", color = EditorWhite, fontSize = 13.sp)
                }
                Text("Photo Pro", color = EditorWhite,
                    fontWeight = FontWeight.Bold, fontSize = 16.sp)
                WorkspaceModeMenu(WorkspaceMode.PRO, !busy && !exporting, onSwitch)
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically) {
                TextButton(enabled = history.undo.isNotEmpty() && !busy && !exporting,
                    onClick = { history.undo(document)?.let { (before, next) ->
                        history = next; onDocument(before)
                        selected = selected.coerceAtMost(before.layers.lastIndex).coerceAtLeast(0)
                    } }) { Text("Undo", color = EditorWhite) }
                TextButton(enabled = history.redo.isNotEmpty() && !busy && !exporting,
                    onClick = { history.redo(document)?.let { (after, next) ->
                        history = next; onDocument(after)
                        selected = selected.coerceAtMost(after.layers.lastIndex).coerceAtLeast(0)
                    } }) { Text("Redo", color = EditorWhite) }
                TextButton(onClick = { showAdd = true }, enabled = !busy && !exporting &&
                    document.layers.size < ProMath.MAX_LAYERS) {
                    Text("+ Add layer", color = EditorMint, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = { savedNames = ProStore.names(context); showProjects = true },
                    enabled = !busy && !exporting) { Text("Open", color = EditorWhite) }
                TextButton(onClick = { showSave = true },
                    enabled = ProMath.valid(document) && !busy && !exporting) {
                    Text("Save", color = EditorWhite)
                }
                TextButton(onClick = { showExport = true },
                    enabled = ProMath.valid(document) && !busy && !exporting) {
                    Text("Export", color = EditorMint)
                }
                TextButton(onClick = { showHelp = true }, enabled = !exporting) {
                    Text("? Help", color = EditorWhite)
                }
            }
            if (notice.isNotBlank()) Text(notice, color = EditorMuted, fontSize = 11.sp,
                maxLines = 2, modifier = Modifier.padding(start = 8.dp, bottom = 5.dp))
        }

        if (document.layers.isNotEmpty()) {
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(EditorSurface).navigationBarsPadding()
                .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(Modifier.horizontalScroll(rememberScrollState())
                    .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    document.layers.forEachIndexed { i, layer ->
                        DockChip(layer.kind.label + " " + (i + 1) +
                            if (!layer.visible) " · Hidden" else "",
                            i == selected, enabled = !busy && !exporting) {
                            selected = i
                            section = ProSection.LAYERS
                            draftSelection = null
                            selectionStart = null
                            retouchSource = null
                            retouchSourceImage = null
                        }
                    }
                }
                Row(Modifier.horizontalScroll(rememberScrollState())
                    .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ProSection.entries.forEach { mode ->
                        DockChip(mode.title, section == mode, enabled = !busy && !exporting) {
                            section = mode
                            retouchSource = null
                            retouchSourceImage = null
                        }
                    }
                }
                Text("Selected: " + (active?.name ?: "None") + " · " +
                    (active?.kind?.label ?: "") +
                    if (section == ProSection.MOVE) " · Drag the outline on the photo" else "",
                    color = EditorMuted, fontSize = 11.sp,
                    modifier = Modifier.padding(start = 14.dp))
                if (active != null) when (section) {
                    ProSection.LAYERS -> {
                        Text("Tap an object on the canvas or its layer chip to select it.",
                            color = EditorMuted, fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp))
                        Row(Modifier.horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)) {
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
                            Row(Modifier.horizontalScroll(rememberScrollState())
                                .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                proColors.forEach { (label, color) ->
                                    DockChip(label, text?.color == color || shape?.color == color) {
                                        changeLayer { item ->
                                            val creative = item.edits.creative
                                            item.copy(edits = item.edits.copy(
                                                creative = if (text != null) creative.copy(
                                                    text = listOf(text.copy(color = color)))
                                                else if (shape != null) creative.copy(
                                                    shapes = listOf(shape.copy(color = color)))
                                                else creative))
                                        }
                                    }
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
                                else "Drag to paint on this independent drawing layer.",
                                color = EditorMuted, fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 13.dp))
                            Row(Modifier.horizontalScroll(rememberScrollState())
                                .padding(horizontal = 9.dp),
                                horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                proColors.forEach { (name, color) ->
                                    DockChip(name, brushColor == color) { brushColor = color }
                                }
                            }
                            Slider(value = brushSize.coerceIn(.003f,.08f),
                                valueRange = .003f.. .08f, onValueChange = { brushSize = it },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
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
                                changeLayer { it.copy(mask = selectedMask) }
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
                        onDiscard = { draftSelection = null }
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
                    ProSection.BLEND -> {
                        Row(Modifier.horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            ProBlend.entries.forEach { blend ->
                                DockChip(blend.label, active.blend == blend) {
                                    changeLayer { it.copy(blend = blend) }
                                }
                            }
                        }
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
    if (showHelp) AlertDialog(
        onDismissRequest = { showHelp = false },
        title = { Text("Photo Pro · Quick guide", color = EditorWhite) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("1. Regular Photo is for photographic adjustments. Pro is for layered " +
                    "compositing, objects, selections, masks and retouching.",
                    color = EditorWhite)
                Text("2. Add layers, select one, then Transform to move/rotate it. " +
                    "Select & mask supports rectangles, ellipses and freehand paths.",
                    color = EditorWhite)
                Text("3. Apply a mask to hide unwanted image regions. For Clone & repair, " +
                    "tap a clean source and then the destination. Use Layer effects " +
                    "for grayscale, inversion, threshold or posterize.",
                    color = EditorWhite)
                Text("Save a Pro project to keep individual layers editable. Export creates a new image file.",
                    color = EditorMuted)
            }
        },
        confirmButton = { TextButton(onClick = { showHelp = false }) {
            Text("Got it", color = EditorMint)
        } }
    )
}

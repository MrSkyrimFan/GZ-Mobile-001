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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
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
import com.greenezone.gzphotoeditor.editor.CollagePhoto
import com.greenezone.gzphotoeditor.editor.ExportSettings
import com.greenezone.gzphotoeditor.editor.ExportSizing
import com.greenezone.gzphotoeditor.editor.GeometryMath
import com.greenezone.gzphotoeditor.editor.ImageExportWriter
import com.greenezone.gzphotoeditor.editor.ProAspect
import com.greenezone.gzphotoeditor.editor.ProBackground
import com.greenezone.gzphotoeditor.editor.ProBlend
import com.greenezone.gzphotoeditor.editor.ProDocument
import com.greenezone.gzphotoeditor.editor.ProHistory
import com.greenezone.gzphotoeditor.editor.ProLayer
import com.greenezone.gzphotoeditor.editor.ProMath
import com.greenezone.gzphotoeditor.editor.ProRaster
import com.greenezone.gzphotoeditor.editor.ProStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

private enum class ProTool(val label: String) {
    LAYERS("Layers"), MOVE("Position"), SCALE("Size"), OPACITY("Opacity"),
    BLEND("Blend"), CANVAS("Canvas")
}
private enum class MoveAxis(val label: String) { X("Left / right"), Y("Up / down") }

/**
 * The Pro workspace is deliberately separate from regular photo correction:
 * real independent image layers, a single selected control, and no permanent giant panel.
 */
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
    var tool by remember { mutableStateOf(ProTool.LAYERS) }
    var axis by remember { mutableStateOf(MoveAxis.X) }
    var history by remember { mutableStateOf(ProHistory()) }
    var sliderStart by remember { mutableStateOf<ProDocument?>(null) }
    var editing by remember { mutableIntStateOf(-1) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var rendering by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf("") }
    var showExport by remember { mutableStateOf(false) }
    var exportSettings by remember { mutableStateOf(ExportSettings()) }
    var pendingExport by remember { mutableStateOf<Pair<ProDocument, ExportSettings>?>(null) }
    var showSave by remember { mutableStateOf(false) }
    var showProjects by remember { mutableStateOf(false) }
    var confirmNew by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var savedNames by remember { mutableStateOf<List<String>>(emptyList()) }

    fun commit(next: ProDocument) {
        history = history.checkpoint(document, next)
        sliderStart = null
        onDocument(next)
    }
    fun editSelected(transform: (ProLayer) -> ProLayer, sliding: Boolean = false) {
        if (selected !in document.layers.indices) return
        val next = document.copy(layers = document.layers.toMutableList().apply {
            this[selected] = transform(this[selected])
        })
        if (sliding) {
            if (sliderStart == null) sliderStart = document
            onDocument(next)
        } else commit(next)
    }
    fun finishSlider() {
        sliderStart?.let { history = history.checkpoint(it, document) }
        sliderStart = null
    }
    fun addPhotos(uris: List<Uri>) {
        val remaining = ProMath.MAX_LAYERS - document.layers.size
        if (remaining <= 0) {
            notice = "Maximum 12 image layers."
            return
        }
        val added = uris.take(remaining).mapIndexed { index, uri ->
            try {
                resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
                // Some photo providers offer only session access.
            }
            ProLayer(uri.toString(), name = "Image " + (document.layers.size + index + 1))
        }
        if (added.isNotEmpty()) {
            commit(document.copy(layers = document.layers + added))
            selected = document.layers.size + added.lastIndex
            notice = if (uris.size > remaining) "Only 12 layers allowed." else "Image layer added"
        }
    }

    if (editing in document.layers.indices) {
        val idx = editing
        val layer = document.layers[idx]
        PhotoEditorScreen(
            initialCollagePhoto = CollagePhoto(layer.uri, layer.edits),
            onDoneCollagePhoto = { updated ->
                val items = document.layers.toMutableList()
                if (idx in items.indices) {
                    items[idx] = items[idx].copy(uri = updated.uri, edits = updated.edits)
                    commit(document.copy(layers = items))
                }
                editing = -1
            }
        )
        return
    }

    BackHandler { onBack() }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(ProMath.MAX_LAYERS)
    ) { addPhotos(it) }
    fun openImages() {
        if (document.layers.size >= ProMath.MAX_LAYERS) {
            notice = "Maximum 12 image layers."
        } else picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    fun export(uri: Uri, request: Pair<ProDocument, ExportSettings>) {
        if (request.first.layers.any { it.uri == uri.toString() }) {
            notice = "Export cannot overwrite a source photo."
            return
        }
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val budget = GeometryMath.safeExportPixels(manager.memoryClass)
        scope.launch {
            exporting = true
            notice = "Exporting image layers…"
            try {
                val writtenSize = withContext(Dispatchers.Default) {
                    var composed: Bitmap? = null
                    var resized: Bitmap? = null
                    try {
                        val (w, h) = ProMath.dimensions(request.first.aspect, 4096, budget)
                        val job = currentCoroutineContext()
                        composed = ProRaster.render(resolver, request.first, w, h, false) {
                            job.ensureActive()
                        }
                        resized = ExportSizing.layout(composed, request.second)
                        val ok = withContext(Dispatchers.IO) {
                            resolver.openOutputStream(uri, "wt")?.use {
                                ImageExportWriter.write(resized, request.second, it)
                            } ?: throw IOException("Output unavailable")
                        }
                        if (!ok) throw IOException("Encoder failed")
                        resized.width.toString() + " × " + resized.height
                    } finally {
                        resized?.let { if (it !== composed && !it.isRecycled) it.recycle() }
                        composed?.let { if (!it.isRecycled) it.recycle() }
                    }
                }
                notice = "Saved " + writtenSize + " · Original photos unchanged"
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: OutOfMemoryError) {
                notice = "Insufficient device memory for export."
                try { resolver.delete(uri, null, null) } catch (_: Exception) {}
            } catch (failure: Exception) {
                notice = "Export failed: " + (failure.message ?: "source unavailable")
                try { resolver.delete(uri, null, null) } catch (_: Exception) {}
            } finally { exporting = false }
        }
    }
    val exporter = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val request = pendingExport
        pendingExport = null
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null && request != null) {
            export(result.data!!.data!!, request)
        }
    }
    fun startExport() {
        if (!ProMath.valid(document)) return
        pendingExport = document to exportSettings
        showExport = false
        exporter.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = exportSettings.format.mime
            putExtra(Intent.EXTRA_TITLE, "GZEditorPro_Layers." + exportSettings.format.extension)
        })
    }

    LaunchedEffect(document) {
        if (document.layers.isEmpty()) { preview = null; return@LaunchedEffect }
        delay(95L)
        rendering = true
        try {
            val (w, h) = ProMath.dimensions(document.aspect, 920, 950_000L)
            val bitmap = withContext(Dispatchers.Default) {
                val job = currentCoroutineContext()
                ProRaster.render(resolver, document, w, h, true) { job.ensureActive() }
            }
            preview = bitmap
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (missing: ProRaster.MissingLayer) {
            selected = missing.position
            notice = "Layer " + (missing.position + 1) + " missing. Replace it or delete it."
        } catch (_: OutOfMemoryError) {
            notice = "Preview out of memory. Reduce the number of layers."
        } catch (failure: Exception) {
            notice = "Preview failed: " + (failure.message ?: "cannot open layer")
        } finally { rendering = false }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF080B10)), contentAlignment = Alignment.Center) {
        if (document.layers.isEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Photo editing Pro", color = EditorWhite,
                    fontWeight = FontWeight.Bold, fontSize = 23.sp)
                Text("Create a photo using editable image layers.", color = EditorMuted, fontSize = 13.sp)
                Button(onClick = ::openImages, colors = ButtonDefaults.buttonColors(
                    containerColor = EditorMint, contentColor = Color(0xFF0B1E18)
                )) { Text("Add first photo") }
            }
        } else preview?.let {
            Image(it.asImageBitmap(), contentDescription = "Editable photo layers",
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        }

        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth()
            .background(Color(0xE810151D)).statusBarsPadding().padding(horizontal = 8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onBack, enabled = !exporting) {
                    Text("‹ Back", color = EditorWhite, fontSize = 13.sp)
                }
                Text("Photo Pro", color = EditorWhite, fontWeight = FontWeight.Bold,
                    fontSize = 15.sp)
                WorkspaceModeMenu(WorkspaceMode.PRO, !exporting, onSwitch)
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically) {
                TextButton(enabled = history.undo.isNotEmpty() && !exporting,
                    onClick = { history.undo(document)?.let { (d,h) ->
                        history = h; onDocument(d); selected = selected.coerceAtMost(d.layers.lastIndex).coerceAtLeast(0)
                    } }) { Text("Undo", color = EditorWhite) }
                TextButton(enabled = history.redo.isNotEmpty() && !exporting,
                    onClick = { history.redo(document)?.let { (d,h) ->
                        history = h; onDocument(d); selected = selected.coerceAtMost(d.layers.lastIndex).coerceAtLeast(0)
                    } }) { Text("Redo", color = EditorWhite) }
                TextButton(onClick = ::openImages, enabled = !exporting && document.layers.size < 12) {
                    Text("+ Layer", color = EditorMint)
                }
                TextButton(onClick = { savedNames = ProStore.names(context); showProjects = true },
                    enabled = !exporting) { Text("Open", color = EditorWhite) }
                TextButton(onClick = { showSave = true }, enabled = ProMath.valid(document) && !exporting) {
                    Text("Save", color = EditorWhite)
                }
                TextButton(onClick = { showExport = true }, enabled = ProMath.valid(document) && !exporting) {
                    Text("Export", color = EditorMint, fontWeight = FontWeight.Bold)
                }
            }
            if (notice.isNotBlank()) Text(notice, modifier = Modifier.padding(start = 9.dp, bottom = 5.dp),
                color = EditorMuted, fontSize = 12.sp, maxLines = 2)
        }

        if (document.layers.isNotEmpty()) {
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .navigationBarsPadding().background(EditorSurface).padding(top = 7.dp, bottom = 7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    ProTool.entries.forEach { part ->
                        DockChip(part.label, tool == part, enabled = !exporting) { tool = part }
                    }
                }
                val layer = document.layers.getOrNull(selected)
                when (tool) {
                    ProTool.LAYERS -> {
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                            .padding(horizontal = 9.dp),
                            horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            document.layers.forEachIndexed { index, item ->
                                DockChip((index + 1).toString() + " " + item.name.take(14) +
                                    if (!item.visible) " (hidden)" else "",
                                    selected == index, enabled = !exporting) {
                                    selected = index
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            TextButton(enabled = layer != null && !exporting,
                                onClick = { editing = selected }) {
                                Text("Edit layer", color = EditorMint)
                            }
                            TextButton(enabled = layer != null && !exporting,
                                onClick = { editSelected({ it.copy(visible = !it.visible) }) }) {
                                Text(if (layer?.visible == true) "Hide" else "Show", color = EditorWhite)
                            }
                            TextButton(enabled = layer != null && selected < document.layers.lastIndex && !exporting,
                                onClick = { commit(document.copy(layers =
                                    ProMath.move(document.layers, selected, selected + 1))); selected++ }) {
                                Text("Move up", color = EditorWhite)
                            }
                            TextButton(enabled = layer != null && selected > 0 && !exporting,
                                onClick = { commit(document.copy(layers =
                                    ProMath.move(document.layers, selected, selected - 1))); selected-- }) {
                                Text("Move down", color = EditorWhite)
                            }
                            TextButton(enabled = layer != null && document.layers.size < 12 && !exporting,
                                onClick = {
                                    commit(document.copy(layers = document.layers.toMutableList().apply {
                                        add(selected + 1, this[selected].copy(name = this[selected].name + " copy"))
                                    }))
                                    selected++
                                }) { Text("Duplicate", color = EditorWhite) }
                            TextButton(enabled = layer != null && !exporting,
                                onClick = {
                                    commit(document.copy(layers =
                                        document.layers.filterIndexed { i, _ -> i != selected }))
                                    selected = selected.coerceAtMost(document.layers.size - 2).coerceAtLeast(0)
                                }) { Text("Delete", color = Color(0xFFFF9C9C)) }
                        }
                    }
                    ProTool.MOVE, ProTool.SCALE, ProTool.OPACITY -> {
                        if (layer != null) {
                            if (tool == ProTool.MOVE) {
                                Row(Modifier.horizontalScroll(rememberScrollState()).padding(start = 10.dp)) {
                                    MoveAxis.entries.forEach { dir ->
                                        DockChip(dir.label, axis == dir) { axis = dir }
                                    }
                                }
                            }
                            val value = when (tool) {
                                ProTool.MOVE -> if (axis == MoveAxis.X) layer.x else layer.y
                                ProTool.SCALE -> layer.scale
                                else -> layer.opacity
                            }
                            val range = if (tool == ProTool.SCALE) .1f..2f else 0f..1f
                            Text(tool.label + ": " + (value * 100).toInt().toString() +
                                if (tool == ProTool.SCALE) "% size" else "%",
                                color = EditorMuted, modifier = Modifier.padding(start = 16.dp),
                                fontSize = 12.sp)
                            Slider(value = value.coerceIn(range.start, range.endInclusive),
                                valueRange = range,
                                enabled = !exporting,
                                onValueChange = { next ->
                                    editSelected({ item -> when (tool) {
                                        ProTool.MOVE -> if (axis == MoveAxis.X) item.copy(x = next)
                                            else item.copy(y = next)
                                        ProTool.SCALE -> item.copy(scale = next)
                                        else -> item.copy(opacity = next)
                                    } }, sliding = true)
                                },
                                onValueChangeFinished = ::finishSlider,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
                        }
                    }
                    ProTool.BLEND -> Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ProBlend.entries.forEach { blend ->
                            DockChip(blend.label, layer?.blend == blend, enabled = layer != null && !exporting) {
                                editSelected({ it.copy(blend = blend) })
                            }
                        }
                    }
                    ProTool.CANVAS -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ProAspect.entries.forEach { ratio ->
                                DockChip(ratio.label, document.aspect == ratio, enabled = !exporting) {
                                    commit(document.copy(aspect = ratio))
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ProBackground.entries.forEach { color ->
                                DockChip(color.label, document.background == color, enabled = !exporting) {
                                    commit(document.copy(background = color))
                                }
                            }
                            DockChip("New project", false, enabled = !exporting) {
                                confirmNew = true
                            }
                        }
                    }
                }
            }
        }
        if (rendering || exporting) {
            Row(Modifier.align(Alignment.Center).background(Color(0xDA1A222B), RoundedCornerShape(16.dp))
                .padding(14.dp), horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = EditorMint, modifier = Modifier.height(24.dp))
                Text(if (exporting) "Exporting…" else "Rendering…", color = EditorWhite)
            }
        }
    }
    if (showExport) ExportDialog(exportSettings, { exportSettings = it }, ::startExport,
        { showExport = false })
    if (showSave) SaveNameDialog("Save editable Pro project",
        "Image sources remain on your device. Save up to 12 projects.",
        name, { name = it }, {
            if (ProStore.save(context, name, document)) {
                notice = "Pro project saved"; showSave = false
            } else notice = "Could not save. Choose a name (12 maximum)."
        }, { showSave = false })
    if (showProjects) AlertDialog(
        onDismissRequest = { showProjects = false },
        title = { Text("Saved Pro projects") },
        text = {
            Column(Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState())) {
                if (savedNames.isEmpty()) Text("No saved Pro projects yet.", color = EditorMuted)
                savedNames.forEach { saved ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = {
                            val restored = ProStore.load(context, saved)
                            if (restored != null) {
                                commit(restored); selected = 0; showProjects = false
                                notice = "Project opened"
                            } else notice = "Saved file invalid or unavailable"
                        }) { Text(saved, color = EditorMint) }
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = {
                            ProStore.delete(context, saved)
                            savedNames = ProStore.names(context)
                        }) { Text("Delete", color = Color(0xFFFF9C9C)) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { showProjects = false }) { Text("Close") } }
    )
    if (confirmNew) AlertDialog(
        onDismissRequest = { confirmNew = false },
        title = { Text("Start a new Pro project?") },
        text = { Text("The current unsaved layer arrangement will be cleared. Undo can restore it.",
            color = EditorMuted) },
        confirmButton = { TextButton(onClick = {
            commit(ProDocument()); selected = 0; confirmNew = false
        }) { Text("New project", color = EditorMint) } },
        dismissButton = { TextButton(onClick = { confirmNew = false }) { Text("Keep current") } }
    )
}

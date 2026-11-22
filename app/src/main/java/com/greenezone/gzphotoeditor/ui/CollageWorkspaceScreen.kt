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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenezone.gzphotoeditor.editor.CollageAspect
import com.greenezone.gzphotoeditor.editor.CollageBackground
import com.greenezone.gzphotoeditor.editor.CollageCellMode
import com.greenezone.gzphotoeditor.editor.CollageDocument
import com.greenezone.gzphotoeditor.editor.CollageHistory
import com.greenezone.gzphotoeditor.editor.CollageLayout
import com.greenezone.gzphotoeditor.editor.CollageMath
import com.greenezone.gzphotoeditor.editor.CollagePhoto
import com.greenezone.gzphotoeditor.editor.CollageQuality
import com.greenezone.gzphotoeditor.editor.CollageRaster
import com.greenezone.gzphotoeditor.editor.CollageStore
import com.greenezone.gzphotoeditor.editor.CollageTemplate
import com.greenezone.gzphotoeditor.editor.ExportSettings
import com.greenezone.gzphotoeditor.editor.ExportSizing
import com.greenezone.gzphotoeditor.editor.GeometryMath
import com.greenezone.gzphotoeditor.editor.ImageExportWriter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.math.min

private enum class CollageSection(val label: String) {
    TEMPLATES("Layouts"), ASPECT("Aspect"), FRAME("Spacing"),
    BACKGROUND("Style"), TILES("Photos"), QUALITY("Quality")
}

private enum class FrameControl(val title: String, val low: Float, val high: Float) {
    GAP("Spacing", 0f, .08f),
    MARGIN("Outside margin", 0f, .10f),
    ROUND("Corner roundness", 0f, .10f),
    BORDER("Border width", 0f, .025f)
}

private data class CollageExport(
    val collage: CollageDocument,
    val settings: ExportSettings
)

@Composable
fun CollageWorkspaceScreen(
    document: CollageDocument,
    onDocument: (CollageDocument) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val resolver = context.contentResolver
    val scope = rememberCoroutineScope()
    var selected by remember { mutableIntStateOf(0) }
    var history by remember { mutableStateOf(CollageHistory()) }
    var layoutGestureStart by remember { mutableStateOf<CollageDocument?>(null) }
    var section by remember { mutableStateOf(CollageSection.TEMPLATES) }
    var control by remember { mutableStateOf(FrameControl.GAP) }
    var toolsVisible by remember { mutableStateOf(true) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var notice by remember { mutableStateOf("") }
    var rendering by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    var editingTile by remember { mutableIntStateOf(-1) }
    var showExport by remember { mutableStateOf(false) }
    var exportSettings by remember { mutableStateOf(ExportSettings()) }
    var pending by remember { mutableStateOf<CollageExport?>(null) }
    var saving by remember { mutableStateOf(false) }
    var confirmNew by remember { mutableStateOf(false) }
    var naming by remember { mutableStateOf("") }
    var savedList by remember { mutableStateOf(false) }
    var savedNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var savedChoice by remember { mutableStateOf<String?>(null) }

    fun commit(next: CollageDocument) {
        history = history.checkpoint(document, next)
        onDocument(next)
        layoutGestureStart = null
    }

    fun changeLayout(layout: CollageLayout) {
        commit(document.copy(layout = layout))
    }

    fun slideLayout(layout: CollageLayout) {
        if (layoutGestureStart == null) layoutGestureStart = document
        onDocument(document.copy(layout = layout))
    }

    fun finishLayout() {
        layoutGestureStart?.let {
            history = history.checkpoint(it, document)
        }
        layoutGestureStart = null
    }

    fun undoCollage() {
        history.undo(document)?.let { (previous, next) ->
            history = next
            onDocument(previous)
            selected = selected.coerceAtMost(previous.photos.lastIndex).coerceAtLeast(0)
        }
    }

    fun redoCollage() {
        history.redo(document)?.let { (nextDoc, next) ->
            history = next
            onDocument(nextDoc)
            selected = selected.coerceAtMost(nextDoc.photos.lastIndex).coerceAtLeast(0)
        }
    }

    fun retainReadPermission(uri: Uri) {
        try {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) {
            // Source permissions may be temporary on third-party photo pickers.
        }
    }

    fun addPhotos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val remaining = CollageMath.MAX_PHOTOS - document.photos.size
        if (remaining <= 0) {
            notice = "A collage supports up to twenty photos."
            return
        }
        val added = uris.take(remaining).map {
            retainReadPermission(it)
            CollagePhoto(it.toString())
        }
        if (added.isNotEmpty()) {
            commit(document.copy(photos = document.photos + added))
            selected = document.photos.size
            notice = document.photos.size.plus(added.size).toString() + " photos in collage"
        }
        if (uris.size > remaining) notice = "Only the first twenty photos were added."
    }

    if (editingTile in document.photos.indices) {
        val index = editingTile
        PhotoEditorScreen(
            initialCollagePhoto = document.photos[index],
            onDoneCollagePhoto = { item ->
                val copy = document.photos.toMutableList()
                if (index in copy.indices) {
                    copy[index] = item
                    commit(document.copy(photos = copy))
                }
                editingTile = -1
            }
        )
        return
    }

    BackHandler { onBack() }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(CollageMath.MAX_PHOTOS)
    ) { uris -> addPhotos(uris) }
    val replacePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && selected in document.photos.indices) {
            retainReadPermission(uri)
            val copy = document.photos.toMutableList()
            copy[selected] = CollagePhoto(uri.toString())
            commit(document.copy(photos = copy))
            notice = "Photo replaced; its old adjustments were cleared."
        }
    }
    fun openMulti() {
        val slots = CollageMath.MAX_PHOTOS - document.photos.size
        if (slots > 0) picker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        ) else notice = "Maximum of twenty photos reached."
    }

    fun runExport(uri: Uri, request: CollageExport) {
        if (request.collage.photos.any { it.uri == uri.toString() }) {
            notice = "Export destination cannot overwrite a source photo."
            return
        }
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val budget = GeometryMath.safeExportPixels(manager.memoryClass)
        scope.launch {
            exporting = true
            notice = "Exporting collage…"
            try {
                val size = withContext(Dispatchers.Default) {
                    var image: Bitmap? = null
                    var composed: Bitmap? = null
                    try {
                        val (width, height) = CollageMath.outputSize(
                            request.collage.layout,
                            request.collage.layout.quality.maxEdge,
                            budget
                        )
                        val frame = currentCoroutineContext()
                        val rendered = CollageRaster.render(resolver, request.collage,
                            width, height, false) { frame.ensureActive() }
                        image = rendered
                        val target = ExportSizing.layout(rendered, request.settings)
                        composed = target
                        val written = withContext(Dispatchers.IO) {
                            resolver.openOutputStream(uri, "wt")?.use {
                                ImageExportWriter.write(target, request.settings, it)
                            } ?: throw IOException("Could not open output document")
                        }
                        if (!written) throw IOException("The selected encoder failed")
                        target.width.toString() + " × " + target.height
                    } finally {
                        composed?.let { if (it !== image && !it.isRecycled) it.recycle() }
                        image?.let { if (!it.isRecycled) it.recycle() }
                    }
                }
                notice = "Collage saved • " + size + " • No watermark"
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                notice = "Export failed: " + (error.message ?: "unavailable photo")
                try { resolver.delete(uri, null, null) } catch (_: Exception) {}
            } catch (_: OutOfMemoryError) {
                notice = "Not enough memory. Try Standard or High quality."
                try { resolver.delete(uri, null, null) } catch (_: Exception) {}
            } finally {
                exporting = false
            }
        }
    }

    val exporter = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val request = pending
        pending = null
        if (result.resultCode == Activity.RESULT_OK &&
            result.data?.data != null && request != null) {
            runExport(result.data!!.data!!, request)
        }
    }
    fun askToSave() {
        if (!CollageMath.validCount(document.photos.size)) return
        pending = CollageExport(document, exportSettings)
        showExport = false
        exporter.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = exportSettings.format.mime
            putExtra(Intent.EXTRA_TITLE,
                "GZEditorPro_Collage." + exportSettings.format.extension)
        })
    }

    LaunchedEffect(document) {
        if (document.photos.isEmpty()) {
            preview = null
            return@LaunchedEffect
        }
        delay(125L)
        rendering = true
        try {
            val (width, height) = CollageMath.outputSize(document.layout, 960, 1_300_000L)
            val value = withContext(Dispatchers.Default) {
                val job = currentCoroutineContext()
                CollageRaster.render(resolver, document, width, height, true) {
                    job.ensureActive()
                }
            }
            preview = value
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: CollageRaster.MissingPhoto) {
            selected = error.position
            section = CollageSection.TILES
            notice = "Photo " + (error.position + 1) +
                " is inaccessible. Choose Photos → Replace."
        } catch (error: Exception) {
            notice = "Cannot render collage: " + (error.message ?: "missing source photo")
        } finally {
            rendering = false
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF080B10)), contentAlignment = Alignment.Center) {
        val shown = preview
        if (shown == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(15.dp)) {
                Text("Collage", color = EditorWhite, fontSize = 27.sp,
                    fontWeight = FontWeight.SemiBold)
                Text("Combine 2–20 photos and edit each one.",
                    color = EditorMuted, fontSize = 13.sp)
                Button(
                    onClick = ::openMulti,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EditorMint, contentColor = Color(0xFF111E19)
                    )
                ) { Text("Add photos") }
            }
        } else {
            Image(
                bitmap = shown.asImageBitmap(),
                contentDescription = "Photo collage preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            Canvas(Modifier.fillMaxSize().pointerInput(document, shown) {
                detectTapGestures(
                    onDoubleTap = { toolsVisible = !toolsVisible },
                    onTap = { point ->
                        val factor = min(size.width.toFloat() / shown.width,
                            size.height.toFloat() / shown.height)
                        val w = shown.width * factor
                        val h = shown.height * factor
                        val left = (size.width - w) / 2f
                        val top = (size.height - h) / 2f
                        val nx = (point.x - left) / w
                        val ny = (point.y - top) / h
                        val choice = CollageMath.select(nx, ny, document.photos.size, document.layout)
                        if (choice >= 0) selected = choice
                    }
                )
            }) {
                if (selected in document.photos.indices) {
                    val factor = min(size.width / shown.width, size.height / shown.height)
                    val w = shown.width * factor
                    val h = shown.height * factor
                    val left = (size.width - w) / 2f
                    val top = (size.height - h) / 2f
                    val cell = CollageMath.innerFrames(document.photos.size, document.layout)[selected]
                    drawRect(
                        EditorMint,
                        topLeft = Offset(left + cell.left * w, top + cell.top * h),
                        size = Size(cell.width * w, cell.height * h),
                        style = Stroke(3f)
                    )
                }
            }
        }
        if (toolsVisible) {
            Column(Modifier.fillMaxWidth().align(Alignment.TopCenter)
                .background(Color(0xF010141C)).statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 7.dp)) {
                Row(Modifier.fillMaxWidth().height(39.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("GZ Editor Pro", color = EditorWhite, fontSize = 18.sp,
                        fontWeight = FontWeight.Bold)
                    TextButton(onClick = { showExport = true },
                        enabled = document.photos.isNotEmpty() && !exporting) {
                        Text("Export", color = EditorMint, fontWeight = FontWeight.Bold)
                    }
                }
                Row(Modifier.fillMaxWidth().height(42.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onBack) {
                        Text("Back", color = EditorWhite)
                    }
                    TextButton(onClick = ::undoCollage,
                        enabled = history.undo.isNotEmpty()) {
                        Text("↶", color = EditorWhite, fontSize = 24.sp)
                    }
                    TextButton(onClick = ::redoCollage,
                        enabled = history.redo.isNotEmpty()) {
                        Text("↷", color = EditorWhite, fontSize = 24.sp)
                    }
                    if (document.photos.isNotEmpty()) {
                        TextButton(onClick = ::openMulti,
                            enabled = document.photos.size < CollageMath.MAX_PHOTOS) {
                            Text("+ Add", color = EditorMint, fontSize = 13.sp)
                        }
                    }
                }
                if (notice.isNotBlank()) {
                    Text(notice, color = EditorMuted, fontSize = 11.sp)
                }
            }

            if (document.photos.isNotEmpty()) {
                Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .navigationBarsPadding().background(EditorSurface)
                    .padding(top = 9.dp, bottom = 5.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CollageControls(
                        document, selected, section, control,
                        onLayout = ::changeLayout,
                        onSlideLayout = ::slideLayout,
                        onLayoutFinished = ::finishLayout,
                        onSection = { section = it },
                        onControl = { control = it },
                        onSelect = { selected = it },
                        onAdd = ::openMulti,
                        onReplace = {
                            replacePicker.launch(PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onEdit = { if (selected in document.photos.indices) editingTile = selected },
                        onLeft = {
                            commit(document.copy(photos =
                                CollageMath.move(document.photos, selected, selected - 1)))
                            selected = (selected - 1).coerceAtLeast(0)
                        },
                        onRight = {
                            commit(document.copy(photos =
                                CollageMath.move(document.photos, selected, selected + 1)))
                            selected = (selected + 1).coerceAtMost(document.photos.lastIndex)
                        },
                        onRemove = {
                            commit(document.copy(photos =
                                document.photos.filterIndexed { index, _ -> index != selected }))
                            selected = selected.coerceAtMost(document.photos.lastIndex - 1)
                                .coerceAtLeast(0)
                        },
                        onSave = { naming = ""; saving = true },
                        onOpen = { savedNames = CollageStore.names(context); savedList = true },
                        onNew = { confirmNew = true }
                    )
                }
            }
        } else {
            TextButton(
                onClick = { toolsVisible = true },
                modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding()
            ) { Text("Show tools", color = EditorMint) }
        }

        if (rendering || exporting) {
            Row(Modifier.align(Alignment.Center)
                .background(Color(0xDB19222B), RoundedCornerShape(14.dp))
                .padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(21.dp), color = EditorMint, strokeWidth = 2.dp)
                Text(if (exporting) " Exporting…" else " Updating…",
                    color = EditorWhite, fontSize = 12.sp)
            }
        }
    }

    if (confirmNew) {
        AlertDialog(
            onDismissRequest = { confirmNew = false },
            title = { Text("Start another collage?", color = EditorWhite) },
            text = {
                Text("Save your current collage first if you want to reopen it. " +
                    "You can also undo this change.",
                    color = EditorMuted)
            },
            confirmButton = {
                TextButton(onClick = {
                    commit(CollageDocument())
                    selected = 0
                    section = CollageSection.TEMPLATES
                    confirmNew = false
                    notice = "New collage"
                }) { Text("Start new", color = EditorMint) }
            },
            dismissButton = {
                TextButton(onClick = { confirmNew = false }) {
                    Text("Cancel", color = EditorMuted)
                }
            }
        )
    }
    if (showExport) {
        ExportDialog(
            settings = exportSettings,
            onSettings = { exportSettings = it },
            onSave = ::askToSave,
            onDismiss = { showExport = false }
        )
    }
    if (saving) {
        SaveNameDialog(
            title = "Save editable collage",
            description = "All tile edits and photo references stay on this device.",
            name = naming,
            onName = { naming = it },
            onSave = {
                if (CollageStore.save(context, naming, document)) {
                    saving = false
                    notice = "Collage project saved"
                } else notice = "Unable to save collage (limit 12 or project too large)."
            },
            onDismiss = { saving = false }
        )
    }
    if (savedList) {
        AlertDialog(
            onDismissRequest = { savedList = false },
            title = { Text("Saved collages", color = EditorWhite) },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 350.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (savedNames.isEmpty()) Text("No collages saved yet.", color = EditorMuted)
                    savedNames.forEach { name ->
                        DockChip(name, savedChoice == name) { savedChoice = name }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val candidate = savedChoice?.let { CollageStore.load(context, it) }
                    if (candidate != null) {
                        history = CollageHistory()
                        layoutGestureStart = null
                        onDocument(candidate)
                        selected = 0
                        savedList = false
                        notice = "Saved collage opened"
                    } else notice = "Saved collage cannot be opened."
                }, enabled = savedChoice != null) {
                    Text("Open", color = EditorMint)
                }
            },
            dismissButton = {
                Row {
                    TextButton(enabled = savedChoice != null, onClick = {
                        savedChoice?.let { CollageStore.delete(context, it) }
                        savedNames = CollageStore.names(context)
                        savedChoice = null
                    }) { Text("Delete", color = EditorMuted) }
                    TextButton(onClick = { savedList = false }) {
                        Text("Cancel", color = EditorMuted)
                    }
                }
            }
        )
    }
}

@Composable
private fun CollageControls(
    document: CollageDocument,
    selected: Int,
    section: CollageSection,
    control: FrameControl,
    onLayout: (CollageLayout) -> Unit,
    onSlideLayout: (CollageLayout) -> Unit,
    onLayoutFinished: () -> Unit,
    onSection: (CollageSection) -> Unit,
    onControl: (FrameControl) -> Unit,
    onSelect: (Int) -> Unit,
    onAdd: () -> Unit,
    onReplace: () -> Unit,
    onEdit: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onRemove: () -> Unit,
    onSave: () -> Unit,
    onOpen: () -> Unit,
    onNew: () -> Unit
) {
    val layout = document.layout
    when (section) {
        CollageSection.TEMPLATES -> {
            LazyRow(Modifier.fillMaxWidth().height(46.dp),
                contentPadding = PaddingValues(horizontal = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(CollageTemplate.entries) { kind ->
                    DockChip(kind.label, kind == layout.template) {
                        onLayout(layout.copy(template = kind))
                    }
                }
            }
            Text("Tap a photo to select it. Double-tap to hide tools.",
                color = EditorMuted, fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 5.dp))
        }
        CollageSection.ASPECT -> {
            LazyRow(Modifier.fillMaxWidth().height(46.dp),
                contentPadding = PaddingValues(horizontal = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(CollageAspect.entries) { aspect ->
                    DockChip(aspect.label, aspect == layout.aspect) {
                        onLayout(layout.copy(aspect = aspect))
                    }
                }
            }
            Text("Choose your collage's final canvas shape.",
                color = EditorMuted, fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 5.dp))
        }
        CollageSection.FRAME -> {
            LazyRow(Modifier.fillMaxWidth().height(46.dp),
                contentPadding = PaddingValues(horizontal = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(FrameControl.entries) { item ->
                    DockChip(item.title, control == item) { onControl(item) }
                }
            }
            val value = when (control) {
                FrameControl.GAP -> layout.gap
                FrameControl.MARGIN -> layout.margin
                FrameControl.ROUND -> layout.cornerRadius
                FrameControl.BORDER -> layout.border
            }
            fun update(v: Float) = when (control) {
                FrameControl.GAP -> layout.copy(gap = v)
                FrameControl.MARGIN -> layout.copy(margin = v)
                FrameControl.ROUND -> layout.copy(cornerRadius = v)
                FrameControl.BORDER -> layout.copy(border = v)
            }
            SingleAdjustment(
                title = control.title,
                value = value,
                low = control.low,
                high = control.high,
                enabled = true,
                onReset = { onLayout(update(0f)) },
                onChange = { onSlideLayout(update(it)) },
                onFinished = onLayoutFinished
            )
        }
        CollageSection.BACKGROUND -> {
            LazyRow(Modifier.fillMaxWidth().height(46.dp),
                contentPadding = PaddingValues(horizontal = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(CollageBackground.entries) { bg ->
                    DockChip(bg.label, bg == layout.background) {
                        onLayout(layout.copy(background = bg))
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CollageCellMode.entries.forEach { mode ->
                    DockChip(mode.label, mode == layout.cellMode) {
                        onLayout(layout.copy(cellMode = mode))
                    }
                }
            }
        }
        CollageSection.TILES -> {
            LazyRow(Modifier.fillMaxWidth().height(46.dp),
                contentPadding = PaddingValues(horizontal = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                document.photos.forEachIndexed { index, _ ->
                    item(key = "tile-$index") {
                        DockChip("Photo " + (index + 1), selected == index) {
                            onSelect(index)
                        }
                    }
                }
            }
            LazyRow(Modifier.fillMaxWidth().height(46.dp),
                contentPadding = PaddingValues(horizontal = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item { DockChip("+ Add", false,
                    document.photos.size < CollageMath.MAX_PHOTOS, onAdd) }
                item { DockChip("Edit selected", false, true, onEdit) }
                item { DockChip("Replace", false, true, onReplace) }
                item { DockChip("Move left", false, selected > 0, onLeft) }
                item { DockChip("Move right", false, selected < document.photos.lastIndex, onRight) }
                item { DockChip("Remove", false, true, onRemove) }
                item { DockChip("Save project", false, true, onSave) }
                item { DockChip("Saved collages", false, true, onOpen) }
                item { DockChip("New collage", false, true, onNew) }
            }
        }
        CollageSection.QUALITY -> {
            LazyRow(Modifier.fillMaxWidth().height(46.dp),
                contentPadding = PaddingValues(horizontal = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(CollageQuality.entries) { q ->
                    DockChip(q.label, q == layout.quality) {
                        onLayout(layout.copy(quality = q))
                    }
                }
            }
            Text("Large exports automatically respect phone memory limits.",
                color = EditorMuted, fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
        }
    }

    LazyRow(Modifier.fillMaxWidth().height(47.dp),
        contentPadding = PaddingValues(horizontal = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        items(CollageSection.entries) { item ->
            DockChip(item.label, item == section) { onSection(item) }
        }
    }
}

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
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
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
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.ExportSettings
import com.greenezone.gzphotoeditor.editor.ExportWorkspace
import com.greenezone.gzphotoeditor.editor.PendingExport
import com.greenezone.gzphotoeditor.editor.PendingExportJournal
import com.greenezone.gzphotoeditor.editor.PendingExportPhase
import com.greenezone.gzphotoeditor.editor.PendingImageRecipe
import com.greenezone.gzphotoeditor.editor.ExportSizing
import com.greenezone.gzphotoeditor.editor.GeometryMath
import com.greenezone.gzphotoeditor.editor.writeStagedImage
import com.greenezone.gzphotoeditor.editor.ImageExportWriter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

internal enum class CollageSection(val label: String) {
    TEMPLATES("Layouts"), ASPECT("Aspect"), FRAME("Spacing"),
    BACKGROUND("Style"), TILES("Photos"), QUALITY("Quality")
}

internal enum class FrameControl(val title: String, val low: Float, val high: Float) {
    GAP("Spacing", 0f, .08f),
    MARGIN("Outside margin", 0f, .10f),
    ROUND("Corner roundness", 0f, .10f),
    BORDER("Border width", 0f, .025f)
}

internal data class CollageWorkspaceSession(
    val selectedPhoto: Int = 0,
    val history: CollageHistory = CollageHistory(),
    val section: CollageSection = CollageSection.TEMPLATES,
    val control: FrameControl = FrameControl.GAP,
    val toolsVisible: Boolean = true,
    val savedDocument: CollageDocument = CollageDocument()
)

private data class SavedCollageChoice(val name: String, val document: CollageDocument)

private data class TileSourceChoice(val id: String, val keepEdits: Boolean)

private data class CollageExport(
    val collage: CollageDocument,
    val settings: ExportSettings
) {
    fun encode(): String = PendingImageRecipe(settings, CollageStore.encode(collage)).encode()

    companion object {
        fun decode(pending: PendingExport): CollageExport? {
            val image = PendingImageRecipe.decode(pending.recipe) ?: return null
            return CollageStore.decode(image.payload)?.let { CollageExport(it, image.settings) }
        }
    }
}

@Composable
fun CollageWorkspaceScreen(
    document: CollageDocument,
    onDocument: (CollageDocument) -> Unit,
    onBack: () -> Unit,
    onSwitch: (WorkspaceMode) -> Unit = {}
) {
    var session by remember { mutableStateOf(CollageWorkspaceSession()) }
    CollageWorkspaceScreenContent(
        document = document,
        onDocument = onDocument,
        onBack = onBack,
        onSwitch = onSwitch,
        session = session,
        onSession = { session = it }
    )
}

@Composable
internal fun CollageWorkspaceScreenContent(
    document: CollageDocument,
    onDocument: (CollageDocument) -> Unit,
    onBack: () -> Unit,
    onSwitch: (WorkspaceMode) -> Unit,
    session: CollageWorkspaceSession,
    onSession: (CollageWorkspaceSession) -> Unit
) {
    val context = LocalContext.current
    val resolver = context.contentResolver
    val scope = rememberCoroutineScope()
    var selected by remember {
        mutableIntStateOf(session.selectedPhoto.coerceIn(
            0, document.photos.lastIndex.coerceAtLeast(0)
        ))
    }
    var history by remember { mutableStateOf(session.history) }
    var layoutGestureStart by remember { mutableStateOf<CollageDocument?>(null) }
    var layoutGestureLatest by remember { mutableStateOf<CollageDocument?>(null) }
    var section by remember { mutableStateOf(session.section) }
    var control by remember { mutableStateOf(session.control) }
    var toolsVisible by remember { mutableStateOf(session.toolsVisible) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var missingTileId by remember { mutableStateOf<String?>(null) }
    var tileSourceChoice by remember { mutableStateOf<TileSourceChoice?>(null) }
    var notice by remember { mutableStateOf("") }
    var rendering by remember { mutableStateOf(false) }
    var renderGeneration by remember { mutableIntStateOf(0) }
    var exporting by remember { mutableStateOf(false) }
    var editingTile by remember { mutableIntStateOf(-1) }
    var showExport by remember { mutableStateOf(false) }
    val exportJournal = remember(context) { PendingExportJournal(context.filesDir) }
    val recoveredExport = remember { exportJournal.read(ExportWorkspace.COLLAGE) }
    var exportSettings by remember {
        mutableStateOf(recoveredExport?.let(CollageExport::decode)?.settings ?: ExportSettings())
    }
    var pendingExport by remember { mutableStateOf(recoveredExport) }
    var exportLaunchId by remember { mutableStateOf<String?>(null) }
    var showExportRecovery by remember { mutableStateOf(recoveredExport != null) }
    var exportRecoveryError by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var confirmNew by remember { mutableStateOf(false) }
    var naming by remember { mutableStateOf("") }
    var savedList by remember { mutableStateOf(false) }
    var savedNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var savedChoice by remember { mutableStateOf<String?>(null) }
    var savedDocument by remember { mutableStateOf(session.savedDocument) }
    var openAfterDecision by remember { mutableStateOf<SavedCollageChoice?>(null) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var openingAfterSave by remember { mutableStateOf(false) }
    var deleteChoice by remember { mutableStateOf<String?>(null) }

    fun commit(next: CollageDocument) {
        history = history.checkpoint(document, next)
        onDocument(next)
        layoutGestureStart = null
        layoutGestureLatest = null
    }

    fun changeLayout(layout: CollageLayout) {
        commit(document.copy(layout = layout))
    }

    fun slideLayout(layout: CollageLayout) {
        if (layoutGestureStart == null) layoutGestureStart = document
        val next = document.copy(layout = layout)
        layoutGestureLatest = next
        onDocument(next)
    }

    fun finishLayout() {
        layoutGestureStart?.let { start ->
            history = history.checkpoint(start, layoutGestureLatest ?: document)
        }
        layoutGestureStart = null
        layoutGestureLatest = null
    }

    fun moveSelectionTo(next: CollageDocument) {
        val selectedId = document.photos.getOrNull(selected)?.id
        val sameTile = next.photos.indexOfFirst { it.id == selectedId }
        selected = if (sameTile >= 0) sameTile else {
            selected.coerceIn(0, next.photos.lastIndex.coerceAtLeast(0))
        }
    }

    fun openSaved(next: CollageDocument) {
        preview = null
        missingTileId = null
        history = CollageHistory()
        layoutGestureStart = null
        layoutGestureLatest = null
        savedDocument = next
        selected = 0
        openAfterDecision = null
        savedList = false
        onDocument(next)
        notice = "Saved collage opened"
    }

    fun undoCollage() {
        history.undo(document)?.let { (previous, next) ->
            history = next
            moveSelectionTo(previous)
            onDocument(previous)
        }
    }

    fun redoCollage() {
        history.redo(document)?.let { (nextDoc, next) ->
            history = next
            moveSelectionTo(nextDoc)
            onDocument(nextDoc)
        }
    }

    fun persistSession() {
        onSession(CollageWorkspaceSession(
            selectedPhoto = selected.coerceIn(0, document.photos.lastIndex.coerceAtLeast(0)),
            history = history,
            section = section,
            control = control,
            toolsVisible = toolsVisible,
            savedDocument = savedDocument
        ))
    }

    fun requestBack() {
        if (rendering || exporting) {
            notice = if (exporting) "Finish the collage export before leaving."
                else "Wait for the collage preview to finish."
            return
        }
        finishLayout()
        persistSession()
        onBack()
    }

    fun requestSwitch(mode: WorkspaceMode) {
        if (rendering || exporting) {
            notice = if (exporting) "Finish the collage export before switching workspaces."
                else "Wait for the collage preview to finish."
            return
        }
        finishLayout()
        persistSession()
        onSwitch(mode)
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
                    copy[index] = item.copy(id = copy[index].id)
                    commit(document.copy(photos = copy))
                }
                editingTile = -1
            },
            onCancelCollagePhoto = { editingTile = -1 }
        )
        return
    }

    BackHandler { requestBack() }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(CollageMath.MAX_PHOTOS)
    ) { uris -> addPhotos(uris) }
    val replacePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val choice = tileSourceChoice
        tileSourceChoice = null
        if (uri != null && choice != null) {
            val index = document.photos.indexOfFirst { it.id == choice.id }
            if (index >= 0) {
                retainReadPermission(uri)
                val copy = document.photos.toMutableList()
                val tile = copy[index]
                copy[index] = tile.copy(
                    uri = uri.toString(),
                    edits = if (choice.keepEdits) tile.edits else EditDocument()
                )
                commit(document.copy(photos = copy))
                notice = if (choice.keepEdits) "Source relinked with its adjustments."
                    else "Photo replaced; its old adjustments were cleared."
            }
        }
    }
    fun chooseTileSource(keepEdits: Boolean) {
        val tile = document.photos.getOrNull(selected) ?: return
        tileSourceChoice = TileSourceChoice(tile.id, keepEdits)
        replacePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    fun openMulti() {
        val slots = CollageMath.MAX_PHOTOS - document.photos.size
        if (slots > 0) picker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        ) else notice = "Maximum of twenty photos reached."
    }

    fun retainExportForRetry(pending: PendingExport, message: String) {
        notice = message
        exportRecoveryError = message
        try {
            pendingExport = exportJournal.failed(pending)
        } catch (_: Exception) {
            pendingExport = exportJournal.read(ExportWorkspace.COLLAGE)
        }
        showExportRecovery = pendingExport != null
    }

    fun runExport(uri: Uri, request: CollageExport, pending: PendingExport) {
        if (request.collage.photos.any { it.uri == uri.toString() }) {
            retainExportForRetry(pending, "Export destination cannot overwrite a source photo.")
            return
        }
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val budget = GeometryMath.safeExportPixels(manager.memoryClass)
        scope.launch {
            exporting = true
            showExportRecovery = false
            exportRecoveryError = null
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
                        withContext(Dispatchers.IO) {
                            writeStagedImage(context.cacheDir,
                                openDestination = {
                                    exportJournal.copying(pending)
                                    resolver.openOutputStream(uri, "wt")
                                },
                                checkActive = { ensureActive() }) { stream -> ImageExportWriter.write(target, request.settings, stream) }
                        }
                        target.width.toString() + " × " + target.height
                    } finally {
                        composed?.let { if (it !== image && !it.isRecycled) it.recycle() }
                        image?.let { if (!it.isRecycled) it.recycle() }
                    }
                }
                exportJournal.discard(pending)
                pendingExport = null
                notice = "Collage saved • " + size + " • No watermark"
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                retainExportForRetry(pending, "Export failed: " + (error.message ?: "unavailable photo"))
            } catch (_: OutOfMemoryError) {
                retainExportForRetry(pending, "Not enough memory. Try Standard or High quality.")
            } finally {
                exporting = false
            }
        }
    }

    RecoverableExportPicker(
        pending = pendingExport,
        launchId = exportLaunchId,
        onLaunch = { if (exportLaunchId == it) exportLaunchId = null },
        onLaunchFailed = { pending, error ->
            retainExportForRetry(pending, error.message ?: "Could not open the destination picker")
        },
        onResult = resultReceived@ { pending, result ->
            try {
                val active = exportJournal.read(ExportWorkspace.COLLAGE)
                if (active == null || active.id != pending.id || active.phase != PendingExportPhase.PICKING) {
                    return@resultReceived
                }
                if (result.resultCode != Activity.RESULT_OK) {
                    if (exportJournal.cancelPicker(pending)) {
                        pendingExport = null
                        showExportRecovery = false
                        notice = "Collage export canceled"
                    }
                } else {
                    val uri = result.data?.data
                    val request = CollageExport.decode(pending)
                    if (uri == null || request == null) {
                        retainExportForRetry(pending, "The export destination or collage could not be restored.")
                    } else {
                        val claimed = exportJournal.claim(pending, uri)
                        if (claimed != null) {
                            pendingExport = claimed
                            runExport(uri, request, claimed)
                        }
                    }
                }
            } catch (error: Exception) {
                retainExportForRetry(pending, error.message ?: "Could not start the collage export")
            }
        }
    )
    fun askToSave() {
        if (!CollageMath.validCount(document.photos.size)) return
        showExport = false
        if (pendingExport != null) {
            showExportRecovery = true
            return
        }
        val settings = exportSettings
        try {
            val pending = exportJournal.begin(ExportWorkspace.COLLAGE, settings.format.mime,
                "GZEditorPro_Collage." + settings.format.extension, CollageExport(document, settings).encode())
            pendingExport = pending
            exportLaunchId = pending.id
        } catch (error: Exception) {
            notice = "Could not prepare collage export: " + (error.message ?: "local storage failed")
        }
    }

    LaunchedEffect(document) {
        renderGeneration += 1
        val generation = renderGeneration
        if (document.photos.isEmpty()) {
            preview = null
            missingTileId = null
            rendering = false
            savedNames = withContext(Dispatchers.IO) { CollageStore.names(context) }
            return@LaunchedEffect
        }
        rendering = true
        try {
            delay(125L)
            val (width, height) = CollageMath.outputSize(document.layout, 960, 1_300_000L)
            val value = withContext(Dispatchers.Default) {
                val job = currentCoroutineContext()
                CollageRaster.render(resolver, document, width, height, true) {
                    job.ensureActive()
                }
            }
            preview = value
            missingTileId = null
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: CollageRaster.MissingPhoto) {
            preview = null
            missingTileId = document.photos.getOrNull(error.position)?.id
            selected = error.position
            section = CollageSection.TILES
            toolsVisible = true
            notice = "Photo " + (error.position + 1) +
                " is inaccessible. Relink its source or replace the tile."
        } catch (error: Exception) {
            preview = null
            missingTileId = null
            toolsVisible = true
            notice = "Cannot render collage: " + (error.message ?: "missing source photo")
        } finally {
            if (renderGeneration == generation) rendering = false
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF080B10)), contentAlignment = Alignment.Center) {
        val shown = preview
        if (document.photos.isEmpty()) {
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
        } else if (shown != null) {
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
                    WorkspaceModeMenu(
                        WorkspaceMode.COLLAGE,
                        !rendering && !exporting,
                        ::requestSwitch
                    )
                    val openProjects = document.photos.isEmpty() && savedNames.isNotEmpty()
                    TextButton(onClick = {
                        if (openProjects) {
                            savedChoice = null
                            savedList = true
                        } else showExport = true
                    }, enabled = (openProjects || document.photos.isNotEmpty()) && !exporting) {
                        // Measure the original label in both states so the mode menu and hit target stay put.
                        Box(contentAlignment = Alignment.Center) {
                            Text("Export", fontWeight = FontWeight.Bold,
                                color = if (openProjects) Color.Transparent else EditorMint,
                                modifier = if (openProjects) Modifier.clearAndSetSemantics {} else Modifier)
                            if (openProjects) Text("Projects", color = EditorMint, fontWeight = FontWeight.Bold,
                                modifier = Modifier.matchParentSize().wrapContentSize(unbounded = true))
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().height(42.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = ::requestBack) {
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
                        onReplace = { chooseTileSource(keepEdits = false) },
                        onRelink = if (missingTileId != null &&
                            document.photos.getOrNull(selected)?.id == missingTileId
                        ) ({ chooseTileSource(keepEdits = true) }) else null,
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
                        onSave = { naming = ""; saveError = null; saving = true },
                        onOpen = {
                            savedNames = CollageStore.names(context)
                            savedChoice = null
                            savedList = true
                        },
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
    val recovery = pendingExport
    if (showExportRecovery && recovery != null && !exporting) {
        val canRecover = remember(recovery.id) { CollageExport.decode(recovery) != null }
        RecoverableExportDialog(
            request = recovery,
            onRetry = {
                try {
                    val retry = exportJournal.retry(recovery)
                    pendingExport = retry
                    showExportRecovery = false
                    exportLaunchId = retry.id
                } catch (error: Exception) {
                    notice = error.message ?: "Could not retry collage export"
                }
            },
            onDiscard = {
                try {
                    exportJournal.discard(recovery)
                    pendingExport = null
                    showExportRecovery = false
                } catch (error: Exception) {
                    notice = error.message ?: "Could not clear the collage export request"
                }
            },
            onDismiss = { showExportRecovery = false },
            unavailable = if (canRecover) null
                else "The saved collage export recipe could not be read. Discard this request to export the current collage.",
            failure = exportRecoveryError
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
            onName = { naming = it; saveError = null },
            error = saveError,
            onSave = {
                if (openingAfterSave && CollageStore.normalizedName(naming) == openAfterDecision?.name) {
                    saveError = "Choose a different name from the collage you are opening."
                } else if (CollageStore.save(context, naming, document)) {
                    savedDocument = document
                    saving = false
                    val next = openAfterDecision.takeIf { openingAfterSave }
                    openingAfterSave = false
                    if (next != null) openSaved(next.document) else notice = "Collage project saved"
                } else saveError = "Could not save collage. Use two to twenty photos and at most twelve projects."
            },
            onDismiss = {
                saving = false
                openingAfterSave = false
                openAfterDecision = null
            }
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
                    val candidate = savedChoice?.let { name ->
                        CollageStore.load(context, name)?.let { SavedCollageChoice(name, it) }
                    }
                    if (candidate != null) {
                        finishLayout()
                        if (document.photos.isNotEmpty() && document != savedDocument) {
                            openAfterDecision = candidate
                            savedList = false
                        } else {
                            openSaved(candidate.document)
                        }
                    } else notice = "Saved collage cannot be opened."
                }, enabled = savedChoice != null) {
                    Text("Open", color = EditorMint)
                }
            },
            dismissButton = {
                Row {
                    TextButton(enabled = savedChoice != null, onClick = {
                        deleteChoice = savedChoice
                        savedList = false
                    }) { Text("Delete", color = EditorMuted) }
                    TextButton(onClick = { savedList = false }) {
                        Text("Cancel", color = EditorMuted)
                    }
                }
            }
        )
    }
    val candidate = openAfterDecision
    if (candidate != null && !openingAfterSave) {
        val keepEditing = { openAfterDecision = null }
        AlertDialog(
            onDismissRequest = keepEditing,
            title = { Text("Keep your collage edits?") },
            text = { Text("Save or discard your current collage edits before opening another project.") },
            confirmButton = {
                Row {
                    TextButton(onClick = keepEditing) { Text("Keep editing") }
                    TextButton(onClick = {
                        openingAfterSave = true
                        naming = ""
                        saveError = null
                        saving = true
                    }) { Text("Save project") }
                }
            },
            dismissButton = {
                TextButton(onClick = { openSaved(candidate.document) }) { Text("Discard") }
            }
        )
    }
    deleteChoice?.let { name ->
        val returnToProjects = {
            deleteChoice = null
            savedList = true
        }
        AlertDialog(
            onDismissRequest = returnToProjects,
            title = { Text("Delete saved collage?") },
            text = { Text("Delete \"$name\" from saved collages. Your original photos stay on your phone.") },
            confirmButton = {
                TextButton(onClick = {
                    if (CollageStore.delete(context, name)) {
                        savedNames = CollageStore.names(context)
                        savedChoice = null
                        notice = "Saved collage deleted."
                    } else {
                        notice = "Could not delete saved collage."
                    }
                    returnToProjects()
                }) { Text("Delete collage") }
            },
            dismissButton = {
                TextButton(onClick = returnToProjects) { Text("Cancel") }
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
    onRelink: (() -> Unit)?,
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
            LazyRow(Modifier.fillMaxWidth().height(46.dp).testTag("collage-tile-actions"),
                contentPadding = PaddingValues(horizontal = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item { DockChip("+ Add", false,
                    document.photos.size < CollageMath.MAX_PHOTOS, onAdd) }
                item { DockChip("Edit selected", false, true, onEdit) }
                if (onRelink != null) {
                    item { DockChip("Relink source", false, true, onRelink) }
                }
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

    LazyRow(Modifier.fillMaxWidth().height(47.dp).testTag("collage-sections"),
        contentPadding = PaddingValues(horizontal = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        items(CollageSection.entries) { item ->
            DockChip(item.label, item == section) { onSection(item) }
        }
    }
}

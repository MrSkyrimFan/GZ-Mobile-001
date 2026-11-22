package com.greenezone.gzphotoeditor.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenezone.gzphotoeditor.editor.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private enum class VideoGifTab(val title: String) {
    TIMELINE("Timeline"), PLAYBACK("Playback"), ADJUST("Adjust"), LOOKS("Looks"),
    CAPTION("Caption"), CANVAS("Canvas")
}

private fun timeLabel(ms: Long): String = String.format(Locale.US, "%d:%02d.%02d",
    ms / 60_000, (ms / 1000) % 60, (ms % 1000) / 10)

@Composable
fun VideoGifScreen(
    document: GifDocument,
    onDocument: (GifDocument) -> Unit,
    onBack: () -> Unit,
    onSwitch: (WorkspaceMode) -> Unit,
    onOpenLegacyPhotoProject: (GifDocument) -> Unit
) {
    val context = LocalContext.current
    val resolver = context.contentResolver
    val scope = rememberCoroutineScope()
    val clip = document.video
    var tab by rememberSaveable { mutableStateOf(VideoGifTab.TIMELINE) }
    var playing by remember { mutableStateOf(false) }
    var seek by remember { mutableIntStateOf(0) }
    var position by remember { mutableIntStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    val enabled = !busy && !exporting
    var notice by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf("") }
    var history by remember { mutableStateOf(GifHistory()) }
    var dragStart by remember { mutableStateOf<GifDocument?>(null) }
    var dragLatest by remember { mutableStateOf<GifDocument?>(null) }
    var exportJob by remember { mutableStateOf<Job?>(null) }
    var destinationRequest by remember { mutableStateOf<CompletableDeferred<Uri?>?>(null) }
    var lastExport by remember { mutableStateOf<Uri?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var showSave by remember { mutableStateOf(false) }
    var showSaved by remember { mutableStateOf(false) }
    var savedNames by remember { mutableStateOf(emptyList<String>()) }
    var saveName by remember { mutableStateOf("") }
    var collapsed by rememberSaveable { mutableStateOf(false) }

    fun pauseAt(frame: Int) {
        playing = false
        seek = frame
        position = frame
    }
    fun commit(next: GifDocument, frame: Int = position) {
        history = history.checkpoint(document, next)
        pauseAt(frame)
        dragStart = null
        dragLatest = null
        onDocument(next)
    }
    fun slide(next: GifDocument, frame: Int = position) {
        if (dragStart == null) dragStart = document
        dragLatest = next
        pauseAt(frame)
        onDocument(next)
    }
    fun finishSlide() {
        val start = dragStart
        val end = dragLatest
        if (start != null && end != null) history = history.checkpoint(start, end)
        dragStart = null
        dragLatest = null
    }
    fun restore(entry: Pair<GifDocument, GifHistory>?) {
        if (entry == null) return
        pauseAt(0)
        history = entry.second
        onDocument(entry.first)
    }
    fun openSource(uri: Uri) {
        if (!enabled) return
        busy = true
        pauseAt(position)
        notice = ""
        scope.launch {
            try {
                try {
                    resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: SecurityException) {
                    // Some providers grant access only for this session.
                }
                val fresh = withContext(Dispatchers.IO) { VideoGifSource.document(context, uri) }
                commit(fresh, 0)
                lastExport = null
                collapsed = false
                tab = VideoGifTab.TIMELINE
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                notice = error.message ?: "Cannot open this source."
            } catch (_: OutOfMemoryError) {
                notice = "There is not enough memory to open this source."
            } finally { busy = false }
        }
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        if (it != null) openSource(it)
    }
    val gifPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) openSource(it)
    }
    fun chooseVideo() = videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
    fun chooseGif() = gifPicker.launch(arrayOf("image/gif"))
    fun openProjects() {
        savedNames = GifStore.names(context)
        showSaved = true
    }

    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/gif")) {
        destinationRequest?.complete(it)
    }
    fun startExport() {
        if (!enabled || !GifMath.valid(document)) return
        val saved = document
        pauseAt(position)
        exporting = true
        notice = ""
        progress = "Writing GIF…"
        exportJob = scope.launch {
            var temp: File? = null
            try {
                withContext(Dispatchers.IO) {
                    val job = currentCoroutineContext()
                    val file = File.createTempFile("gz-gif-", ".tmp", context.cacheDir)
                    temp = file
                    val quota = GifExportGuard.maxTemporaryBytes(context.cacheDir.usableSpace)
                    if (quota < GifExportGuard.MIN_EXPORT_BYTES) {
                        throw IOException("Not enough free storage to safely create a GIF. Free at least 72 MB.")
                    }
                    GifExportGuard.limit(file.outputStream(), quota).buffered(65536).use { stream ->
                        VideoGifSource.write(context, saved, stream, cancel = { job.ensureActive() },
                            onProgress = { index, count ->
                                if (index == count || index % 8 == 0) {
                                    scope.launch(Dispatchers.Main) { progress = "Writing frame $index of $count…" }
                                }
                            })
                    }
                }
                val request = CompletableDeferred<Uri?>()
                destinationRequest = request
                progress = "Choose where to save your GIF"
                exporter.launch("GZEditorPro.gif")
                val dest = request.await()
                if (dest == null) {
                    notice = "GIF export cancelled"
                    return@launch
                }
                require(dest.toString() != saved.video?.uri) { "Choose a new filename to keep the original." }
                progress = "Saving GIF…"
                withContext(Dispatchers.IO) {
                    val job = currentCoroutineContext()
                    // Android ACTION_CREATE_DOCUMENT creates a new document and does not
                    // overwrite an existing filename. Some providers are write-only until
                    // content is committed, so don't require openInputStream on a new URI.
                    val output = resolver.openOutputStream(dest, "wt")
                        ?: throw IOException("This provider cannot write the destination. Choose Files instead.")
                    try {
                        output.use { stream ->
                            requireNotNull(temp).inputStream().use { input ->
                                val buffer = ByteArray(65536)
                                while (true) {
                                    job.ensureActive()
                                    val n = input.read(buffer)
                                    if (n < 0) break
                                    stream.write(buffer, 0, n)
                                }
                            }
                        }
                    } catch (failed: Throwable) {
                        // ACTION_CREATE_DOCUMENT created a new document for this export.
                        // Remove incomplete output if its provider supports deletion.
                        val removed = runCatching { resolver.delete(dest, null, null) > 0 }
                            .getOrDefault(false)
                        if (!removed) runCatching { resolver.openOutputStream(dest, "wt")?.close() }
                        throw failed
                    }
                }
                lastExport = dest
                notice = "GIF saved"
            } catch (cancelled: CancellationException) {
                notice = "GIF export cancelled"
                throw cancelled
            } catch (_: OutOfMemoryError) {
                notice = "Not enough memory to export. Choose a smaller canvas size and try again."
            } catch (error: Exception) {
                notice = "GIF export failed. ${error.message.orEmpty()}"
            } finally {
                temp?.delete()
                destinationRequest?.cancel()
                destinationRequest = null
                exporting = false
                exportJob = null
            }
        }
    }
    BackHandler { if (enabled) onBack() }

    val count = remember(clip) { clip?.let { GifVideoMath.frameCount(it) } ?: 0 }
    val time = remember(clip) { clip?.let { GifVideoMath.frameTimesMs(it) }.orEmpty() }
    val activeFrame = position.coerceIn(0, (count - 1).coerceAtLeast(0))
    val setEdits: (EditDocument) -> Unit = { commit(document.copy(edits = it)) }
    val slideEdits: (EditDocument) -> Unit = { slide(document.copy(edits = it)) }

    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFF080B10))
        .statusBarsPadding().navigationBarsPadding()) {
        val deckHeight = maxHeight * .38f
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().background(EditorSurface),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onBack, enabled = enabled) { Text("‹ Back", color = EditorWhite) }
                TextButton(onClick = { pauseAt(position); showHelp = true }, enabled = enabled) {
                    Text("Help", color = EditorMint)
                }
                if (clip != null) Box {
                    TextButton(onClick = { pauseAt(position); showMenu = true }, enabled = enabled) {
                        Text("File ▾", color = EditorWhite)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Choose video") }, onClick = { showMenu = false; chooseVideo() })
                        DropdownMenuItem(text = { Text("Edit existing GIF") }, onClick = { showMenu = false; chooseGif() })
                        DropdownMenuItem(text = { Text("Save project") }, onClick = { showMenu = false; showSave = true })
                        DropdownMenuItem(text = { Text("Open saved GIF project") }, onClick = { showMenu = false; openProjects() })
                    }
                }
                WorkspaceModeMenu(WorkspaceMode.GIF, enabled, onSwitch)
            }
            if (notice.isNotBlank()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(notice, color = EditorWhite, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    if (lastExport != null && notice == "GIF saved") TextButton(onClick = {
                        lastExport?.let(::openSource)
                    }) { Text("Edit saved GIF", color = EditorMint) }
                    TextButton(onClick = { notice = "" }) { Text("Dismiss", color = EditorMuted) }
                }
            }
            if (clip == null) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Select one video to turn into a GIF", color = EditorWhite,
                        fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Button(onClick = ::chooseVideo, enabled = enabled) { Text("Choose video") }
                    TextButton(onClick = ::chooseGif, enabled = enabled) { Text("Edit existing GIF", color = EditorMint) }
                    TextButton(onClick = ::openProjects, enabled = enabled) { Text("Open saved GIF project", color = EditorMuted) }
                }
            } else {
                GifFramePreview(document, playing, seek, onFrame = { position = it },
                    onEnd = { pauseAt(it) },
                    onError = { playing = false; notice = "Preview unavailable. $it" },
                    modifier = Modifier.weight(1f).fillMaxWidth())
                Column(Modifier.fillMaxWidth().background(EditorSurface),
                    verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        TextButton(enabled = enabled && count > 1, onClick = {
                            if (playing) pauseAt(position) else {
                                seek = if (activeFrame == count - 1) 0 else activeFrame
                                playing = true
                            }
                        }) { Text(if (playing) "Pause" else "Play", color = EditorMint) }
                        FilterChip(selected = clip.reverse, enabled = enabled && count > 1,
                            onClick = {
                                val resume = playing
                                val index = if (resume && activeFrame > 0) count - 1 - activeFrame else 0
                                commit(document.copy(video = clip.copy(reverse = !clip.reverse)), index)
                                playing = resume
                            },
                            label = { Text("Reverse") })
                        TextButton(enabled = enabled, onClick = { collapsed = !collapsed }) {
                            Text(if (collapsed) "Tools ▴" else "Hide ▾", color = EditorWhite)
                        }
                        Button(onClick = ::startExport, enabled = enabled) { Text("Save GIF") }
                    }
                    if (!collapsed) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            val (w, h) = GifMath.dimensions(document)
                            Text("${w} × ${h}  ·  ${timeLabel(time.getOrElse(activeFrame) { 0L })}",
                                fontSize = 12.sp, color = EditorMuted, modifier = Modifier.weight(1f))
                            TextButton(enabled = enabled && history.undo.isNotEmpty(), onClick = { restore(history.undo(document)) }) {
                                Text("Undo", color = EditorWhite)
                            }
                            TextButton(enabled = enabled && history.redo.isNotEmpty(), onClick = { restore(history.redo(document)) }) {
                                Text("Redo", color = EditorWhite)
                            }
                        }
                        GifOptions {
                            VideoGifTab.entries.forEach { item ->
                                DockChip(item.title, tab == item, enabled) { tab = item }
                            }
                        }
                        Column(Modifier.fillMaxWidth().heightIn(max = deckHeight)
                            .verticalScroll(rememberScrollState()).padding(vertical = 5.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            when (tab) {
                                VideoGifTab.TIMELINE -> GifTimeline(clip, activeFrame, count, enabled,
                                    onSeek = ::pauseAt, onTrim = { next, end ->
                                        val index = if (end != next.reverse) GifVideoMath.frameCount(next) - 1 else 0
                                        slide(document.copy(video = next), index)
                                    }, onFinish = ::finishSlide)
                                VideoGifTab.PLAYBACK -> {
                                    GifOptions {
                                        GifVideoMath.FRAME_RATES.forEach { fps ->
                                            DockChip(if (fps == 0) "Source timing" else "$fps FPS", clip.fps == fps, enabled) {
                                                commit(document.copy(video = clip.copy(fps = fps)), 0)
                                            }
                                        }
                                    }
                                    GifOptions {
                                        GifVideoMath.SPEEDS.forEach { speed ->
                                            DockChip("${speed}%", clip.speedPercent == speed, enabled) {
                                                commit(document.copy(video = clip.copy(speedPercent = speed)))
                                            }
                                        }
                                    }
                                    GifOptions {
                                        DockChip("Play once", document.loopCount == -1, enabled) {
                                            commit(document.copy(forever = false, repeatCount = null))
                                        }
                                        DockChip("Loop forever", document.loopCount == 0, enabled) {
                                            commit(document.copy(forever = true, repeatCount = null))
                                        }
                                        if (clip.loopCount > 0) DockChip("Repeat ${clip.loopCount}×",
                                            document.loopCount == clip.loopCount, enabled) {
                                            commit(document.copy(forever = false, repeatCount = clip.loopCount))
                                        }
                                    }
                                }
                                VideoGifTab.ADJUST -> GradeAdjustmentDeck(document.edits, enabled, setEdits, slideEdits, ::finishSlide)
                                VideoGifTab.LOOKS -> GifLooksDeck(document.edits, enabled, setEdits)
                                VideoGifTab.CAPTION -> GifCaptionDeck(document.edits, enabled, setEdits, slideEdits, ::finishSlide)
                                VideoGifTab.CANVAS -> {
                                    GifOptions {
                                        GifSize.entries.forEach { size ->
                                            DockChip(size.label, document.size == size, enabled) { commit(document.copy(size = size)) }
                                        }
                                    }
                                    GifOptions {
                                        GifAspect.entries.forEach { aspect ->
                                            DockChip(aspect.label, document.aspect == aspect, enabled) { commit(document.copy(aspect = aspect)) }
                                        }
                                    }
                                    GifOptions {
                                        GifFit.entries.forEach { fit ->
                                            DockChip(fit.label, document.fit == fit, enabled) { commit(document.copy(fit = fit)) }
                                        }
                                    }
                                    val edits = document.edits
                                    GifOptions {
                                        DockChip("Rotate 90°", false, enabled) {
                                            setEdits(edits.copy(quarterTurns = (edits.quarterTurns + 1) % 4))
                                        }
                                        DockChip("Mirror", edits.mirrored, enabled) { setEdits(edits.copy(mirrored = !edits.mirrored)) }
                                        DockChip("Flip vertical", edits.flipVertical, enabled) { setEdits(edits.copy(flipVertical = !edits.flipVertical)) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!enabled) Column(Modifier.align(Alignment.Center).background(EditorSurface).padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = EditorMint)
            Text(if (exporting) progress else "Opening source…", color = EditorWhite)
            if (exporting) TextButton(onClick = { exportJob?.cancel() }) { Text("Cancel export", color = EditorMint) }
        }
    }
    if (showHelp) GifHelpDialog { showHelp = false }
    if (showSave) SaveNameDialog("Save GIF project", "Reopen your source with these edits and timing settings.",
        saveName, { saveName = it }, {
            if (GifStore.save(context, saveName, document)) {
                showSave = false
                notice = "Project saved"
            } else notice = "Could not save this project."
        }, { showSave = false })
    if (showSaved) AlertDialog(onDismissRequest = { showSaved = false }, title = { Text("Saved GIF projects") },
        text = {
            Column(Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState())) {
                if (savedNames.isEmpty()) Text("No saved GIF projects yet.")
                savedNames.forEach { name ->
                    TextButton(onClick = {
                        showSaved = false
                        busy = true
                        scope.launch {
                            try {
                                val saved = withContext(Dispatchers.IO) { GifStore.load(context, name) }
                                if (saved == null) notice = "Could not read the saved project or its source file."
                                else if (saved.video != null) {
                                    commit(saved, 0)
                                    lastExport = null
                                } else onOpenLegacyPhotoProject(saved)
                            } finally { busy = false }
                        }
                    }) { Text(name) }
                }
            }
        }, confirmButton = { TextButton(onClick = { showSaved = false }) { Text("Close") } })
}

@Composable
private fun GifTimeline(clip: GifVideoClip, index: Int, count: Int, enabled: Boolean,
    onSeek: (Int) -> Unit, onTrim: (GifVideoClip, Boolean) -> Unit, onFinish: () -> Unit) {
    fun label(value: String): Modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
        .semantics { contentDescription = value }
    Text("Frame ${index + 1} of $count", Modifier.padding(start = 14.dp), color = EditorWhite, fontSize = 12.sp)
    Slider(value = index.toFloat(), valueRange = 0f..(count - 1).coerceAtLeast(1).toFloat(),
        enabled = enabled && count > 1, modifier = label("Scrub GIF frames"),
        onValueChange = { onSeek(it.roundToInt().coerceIn(0, count - 1)) })
    Text("Trim  ${timeLabel(clip.startMs)} – ${timeLabel(clip.endMs)}", Modifier.padding(start = 14.dp),
        color = EditorWhite, fontSize = 12.sp)
    RangeSlider(value = clip.startMs.toFloat()..clip.endMs.toFloat(),
        onValueChange = { range ->
            val startChanged = range.start.roundToLong() != clip.startMs
            val start = range.start.roundToLong().coerceIn(0, clip.durationMs - 1)
            val end = range.endInclusive.roundToLong().coerceIn(start + 1, clip.durationMs)
            onTrim(clip.copy(startMs = start, endMs = end), !startChanged)
        }, onValueChangeFinished = onFinish, valueRange = 0f..clip.durationMs.toFloat(),
        enabled = enabled, modifier = label("Trim start and end"))
}

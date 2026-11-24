package com.greenezone.gzphotoeditor.ui

import android.app.Activity
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenezone.gzphotoeditor.editor.GifAspect
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.writeStagedImage
import com.greenezone.gzphotoeditor.editor.GifEncoder
import com.greenezone.gzphotoeditor.editor.GifFit
import com.greenezone.gzphotoeditor.editor.GifMath
import com.greenezone.gzphotoeditor.editor.GifSize
import com.greenezone.gzphotoeditor.editor.GifStore
import com.greenezone.gzphotoeditor.editor.GifUndoStack
import com.greenezone.gzphotoeditor.editor.ImagePipeline
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal enum class GifTool(val label: String) {
    FRAMES("Photos"), TIMING("Timing"), SIZE("Canvas"), PROJECT("Projects")
}

internal enum class GifWorkspaceRoute { VIDEO, PHOTOS }

internal data class GifProjectSnapshot(
    val document: GifDocument,
    val savedName: String?,
    val route: GifWorkspaceRoute
)

internal fun GifUndoStack<GifProjectSnapshot>.withoutSavedProject(name: String): GifUndoStack<GifProjectSnapshot> {
    fun detach(snapshot: GifProjectSnapshot): GifProjectSnapshot =
        if (snapshot.savedName == name) snapshot.copy(savedName = null) else snapshot

    return copy(undo = undo.map(::detach), redo = redo.map(::detach))
}

internal data class GifWorkspaceSession(
    val route: GifWorkspaceRoute? = null,
    val photoTool: GifTool = GifTool.FRAMES,
    val photoIndex: Int = 0,
    val history: GifUndoStack<GifProjectSnapshot> = GifUndoStack(),
    val savedName: String? = null,
    val videoTab: VideoGifTab = VideoGifTab.TIMELINE,
    val videoSeek: Int = 0,
    val videoPosition: Int = 0,
    val videoCollapsed: Boolean = false
)

@Composable
fun GifMakerScreen(
    document: GifDocument,
    onDocument: (GifDocument) -> Unit,
    onBack: () -> Unit,
    onSwitch: (WorkspaceMode) -> Unit
) {
    var session by remember { mutableStateOf(GifWorkspaceSession()) }
    GifMakerScreenContent(
        document = document,
        onDocument = onDocument,
        onBack = onBack,
        onSwitch = onSwitch,
        session = session,
        onSession = { session = it }
    )
}

@Composable
internal fun GifMakerScreenContent(
    document: GifDocument,
    onDocument: (GifDocument) -> Unit,
    onBack: () -> Unit,
    onSwitch: (WorkspaceMode) -> Unit,
    session: GifWorkspaceSession,
    onSession: (GifWorkspaceSession) -> Unit
) {
    var photoSequenceOnly by remember {
        mutableStateOf(when (session.route) {
            GifWorkspaceRoute.PHOTOS -> true
            GifWorkspaceRoute.VIDEO -> false
            null -> document.frames.isNotEmpty() && document.video == null
        })
    }
    if (!photoSequenceOnly) {
        VideoGifScreenContent(
            document = document,
            onDocument = onDocument,
            onBack = onBack,
            onSwitch = onSwitch,
            onOpenLegacyPhotoProject = { saved ->
                onDocument(saved)
                photoSequenceOnly = true
            },
            session = session,
            onSession = onSession
        )
        return
    }

    val context = LocalContext.current
    val resolver = context.contentResolver
    val scope = rememberCoroutineScope()
    var tool by remember { mutableStateOf(session.photoTool) }
    var index by remember {
        mutableIntStateOf(session.photoIndex.coerceIn(
            0, document.frames.lastIndex.coerceAtLeast(0)
        ))
    }
    var playing by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var exporting by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf("") }
    var history by remember { mutableStateOf(session.history) }
    var savedName by remember { mutableStateOf(session.savedName) }
    var timingStart by remember { mutableStateOf<GifProjectSnapshot?>(null) }
    var timingLatest by remember { mutableStateOf<GifDocument?>(null) }
    var pending by remember { mutableStateOf<GifDocument?>(null) }
    var saveOpen by remember { mutableStateOf(false) }
    var helpOpen by remember { mutableStateOf(false) }
    var projectsOpen by remember { mutableStateOf(false) }
    var openingProject by remember { mutableStateOf(false) }
    var newOpen by remember { mutableStateOf(false) }
    var savedNames by remember { mutableStateOf<List<String>>(emptyList()) }

    fun snapshot(
        current: GifDocument = document,
        name: String? = savedName
    ) = GifProjectSnapshot(current, name,
        if (current.video == null) GifWorkspaceRoute.PHOTOS else GifWorkspaceRoute.VIDEO)

    fun persistPhotoSession(route: GifWorkspaceRoute = GifWorkspaceRoute.PHOTOS) {
        onSession(session.copy(
            route = route,
            photoTool = tool,
            photoIndex = index.coerceIn(0, document.frames.lastIndex.coerceAtLeast(0)),
            history = history,
            savedName = savedName
        ))
    }

    fun applySnapshot(next: GifProjectSnapshot) {
        timingStart = null
        timingLatest = null
        savedName = next.savedName
        onDocument(next.document)
        index = index.coerceAtMost(next.document.frames.lastIndex).coerceAtLeast(0)
        if (next.route == GifWorkspaceRoute.VIDEO) {
            playing = false
            persistPhotoSession(GifWorkspaceRoute.VIDEO)
            photoSequenceOnly = false
        }
    }

    fun commit(next: GifDocument, nextName: String? = savedName) {
        val updated = snapshot(next, nextName)
        history = history.checkpoint(snapshot(), updated)
        applySnapshot(updated)
    }

    fun restore(entry: Pair<GifProjectSnapshot, GifUndoStack<GifProjectSnapshot>>?) {
        if (entry == null) return
        playing = false
        history = entry.second
        applySnapshot(entry.first)
    }
    fun add(uris: List<Uri>) {
        val remaining = GifMath.MAX_FRAMES - document.frames.size
        if (remaining <= 0) { notice = "Twenty photos maximum."; return }
        val accepted = uris.take(remaining).map { uri ->
            try {
                resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
                // Some providers only grant temporary URI access.
            }
            uri.toString()
        }
        if (accepted.isNotEmpty()) {
            commit(document.copy(frames = document.frames + accepted))
            notice = if (uris.size > remaining) "Only the first 20 photos were added." else
                "Added " + accepted.size + " photos"
        }
    }

    fun finishPhotoTiming() {
        timingStart?.let { start ->
            history = history.checkpoint(start, snapshot(timingLatest ?: document))
        }
        timingStart = null
        timingLatest = null
    }

    fun requestPhotoBack() {
        if (exporting) {
            notice = "Finish the photo GIF export before leaving."
            return
        }
        playing = false
        finishPhotoTiming()
        persistPhotoSession()
        onBack()
    }

    BackHandler { requestPhotoBack() }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(GifMath.MAX_FRAMES)
    ) { add(it) }
    fun importPhotos() {
        if (document.frames.size == GifMath.MAX_FRAMES) {
            notice = "Maximum of 20 frames."
        } else picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    fun export(uri: Uri, saved: GifDocument) {
        if (saved.frames.any { it == uri.toString() }) {
            notice = "Export cannot overwrite any original photo."
            return
        }
        scope.launch {
            exporting = true
            playing = false
            notice = "Encoding GIF on your phone…"
            try {
                withContext(Dispatchers.IO) {
                    val job = currentCoroutineContext()
                    writeStagedImage(context.cacheDir,
                        openDestination = { resolver.openOutputStream(uri, "wt") },
                        checkActive = { job.ensureActive() }) { out ->
                        GifEncoder.write(out, saved, cancel = { job.ensureActive() }) { i ->
                            ImagePipeline.decode(resolver, Uri.parse(saved.frames[i]),
                                saved.size.maxEdge * 2, 1_100_000L)
                        }
                        true
                    }
                }
                notice = "GIF exported · " + saved.frames.size + " frames · no watermark"
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: OutOfMemoryError) {
                notice = "Not enough memory to encode GIF. Choose a smaller size."
            } catch (failure: Exception) {
                notice = "GIF export failed: " + (failure.message ?: "source missing")
            } finally { exporting = false }
        }
    }
    val exporter = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val snapshot = pending
        pending = null
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null && snapshot != null) {
            export(result.data!!.data!!, snapshot)
        }
    }
    fun startExport() {
        if (!GifMath.valid(document)) {
            notice = "Choose at least two photos before exporting."
            return
        }
        pending = document
        exporter.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/gif"
            putExtra(Intent.EXTRA_TITLE, "GZEditorPro_Animation.gif")
        })
    }

    LaunchedEffect(document.frames, index, document.fit, document.aspect, document.size) {
        val uri = document.frames.getOrNull(index)
        if (uri == null) { preview = null; return@LaunchedEffect }
        try {
            val frame = withContext(Dispatchers.Default) {
                val source = ImagePipeline.decode(
                    resolver, Uri.parse(uri), document.size.maxEdge * 2, 1_100_000L)
                try {
                    GifEncoder.renderFrame(source, document)
                } finally {
                    if (!source.isRecycled) source.recycle()
                }
            }
            preview = frame
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            preview = null
            playing = false
            notice = "Photo " + (index + 1) + " unavailable. Remove or replace it."
        }
    }
    LaunchedEffect(playing, document.frames, document.delayMs, document.forever) {
        if (!playing || document.frames.size < 2) return@LaunchedEffect
        while (playing) {
            delay(document.delayMs.toLong())
            if (index >= document.frames.lastIndex) {
                if (document.forever) index = 0 else {
                    playing = false
                    break
                }
            } else index++
        }
    }
    LaunchedEffect(notice) {
        if (notice.isNotBlank()) {
            delay(5500L)
            notice = ""
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF080B10)), contentAlignment = Alignment.Center) {
        if (preview != null) {
            Image(bitmap = preview!!.asImageBitmap(),
                contentDescription = "Animated GIF frame preview",
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Photo sequence", color = EditorWhite, fontSize = 26.sp,
                    fontWeight = FontWeight.Bold)
                Text("Pick 2–20 photos to make an animated GIF.", color = EditorMuted,
                    fontSize = 13.sp)
                Button(onClick = ::importPhotos, colors = ButtonDefaults.buttonColors(
                    containerColor = EditorMint, contentColor = Color(0xFF0C1C17)
                )) { Text("Add photos") }
            }
        }

        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth()
            .background(Color(0xEA121820)).statusBarsPadding().padding(horizontal = 8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = ::requestPhotoBack) {
                    Text("‹ Back", color = EditorWhite, fontSize = 13.sp)
                }
                TextButton(enabled = !exporting, onClick = { playing = false; helpOpen = true }) {
                    Text("Help", color = EditorMint)
                }
                TextButton(enabled = !exporting, onClick = {
                    playing = false
                    finishPhotoTiming()
                    persistPhotoSession(GifWorkspaceRoute.VIDEO)
                    photoSequenceOnly = false
                }) {
                    Text("Video → GIF", color = EditorMint, fontSize = 12.sp)
                }
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically) {
                TextButton(enabled = !exporting && document.frames.size >= 2,
                    onClick = { playing = !playing }) {
                    Text(if (playing) "Pause" else "▶ Play", color = EditorMint)
                }
                TextButton(enabled = history.undo.isNotEmpty() && !exporting,
                    onClick = { restore(history.undo(snapshot())) }) { Text("Undo", color = EditorWhite) }
                TextButton(enabled = history.redo.isNotEmpty() && !exporting,
                    onClick = { restore(history.redo(snapshot())) }) { Text("Redo", color = EditorWhite) }
                TextButton(enabled = !exporting && document.frames.size < 20,
                    onClick = ::importPhotos) { Text("+ Photos", color = EditorMint) }
                TextButton(enabled = !exporting,
                    onClick = { savedNames = GifStore.names(context); projectsOpen = true }) {
                    Text("Open saved", color = EditorWhite)
                }
                TextButton(enabled = GifMath.valid(document) && !exporting,
                    onClick = ::startExport) { Text("Export GIF", color = EditorMint) }
            }
            if (notice.isNotBlank()) Text(notice, color = EditorMuted, fontSize = 12.sp,
                modifier = Modifier.padding(start = 9.dp, bottom = 5.dp), maxLines = 2)
        }

        if (document.frames.isNotEmpty()) {
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(EditorSurface).navigationBarsPadding().padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(Modifier.horizontalScroll(rememberScrollState())
                    .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    GifTool.entries.forEach { option ->
                        DockChip(option.label, tool == option, enabled = !exporting) { tool = option }
                    }
                }
                when (tool) {
                    GifTool.FRAMES -> {
                        Row(Modifier.horizontalScroll(rememberScrollState())
                            .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            document.frames.forEachIndexed { n, _ ->
                                DockChip("Photo " + (n + 1), n == index, enabled = !exporting) {
                                    playing = false; index = n
                                }
                            }
                        }
                        Row(Modifier.horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            TextButton(enabled = index > 0 && !exporting, onClick = {
                                playing = false
                                commit(document.copy(frames = GifMath.move(document.frames, index, index - 1)))
                                index--
                            }) { Text("← Earlier", color = EditorWhite) }
                            TextButton(enabled = index < document.frames.lastIndex && !exporting,
                                onClick = {
                                    playing = false
                                    commit(document.copy(frames = GifMath.move(document.frames, index, index + 1)))
                                    index++
                                }) { Text("Later →", color = EditorWhite) }
                            TextButton(enabled = !exporting, onClick = {
                                playing = false
                                commit(document.copy(frames = document.frames.filterIndexed { i, _ -> i != index }))
                            }) { Text("Remove photo", color = Color(0xFFFFA2A2)) }
                        }
                    }
                    GifTool.TIMING -> {
                        Text("Frame duration: " + document.delayMs + " ms", color = EditorWhite,
                            modifier = Modifier.padding(start = 16.dp), fontSize = 13.sp)
                        Slider(value = document.delayMs.toFloat(), valueRange = 100f..1500f,
                            enabled = !exporting,
                            onValueChange = { next ->
                                if (timingStart == null) timingStart = snapshot()
                                val changed = document.copy(
                                    delayMs = (next.toInt() / 10) * 10
                                )
                                timingLatest = changed
                                onDocument(changed)
                            },
                            onValueChangeFinished = ::finishPhotoTiming,
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .semantics {
                                    contentDescription = "Photo GIF frame duration"
                                })
                        Row(Modifier.padding(start = 10.dp)) {
                            DockChip("Loop forever", document.forever, enabled = !exporting) {
                                commit(document.copy(forever = true))
                            }
                            DockChip("Play once", !document.forever, enabled = !exporting) {
                                commit(document.copy(forever = false))
                            }
                        }
                    }
                    GifTool.SIZE -> {
                        Row(Modifier.horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            GifSize.entries.filter { it != GifSize.SOURCE }.forEach {
                                DockChip(it.label, document.size == it, enabled = !exporting) {
                                    commit(document.copy(size = it))
                                }
                            }
                        }
                        Row(Modifier.horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            GifAspect.entries.filter { it != GifAspect.SOURCE }.forEach {
                                DockChip(it.label, document.aspect == it, enabled = !exporting) {
                                    commit(document.copy(aspect = it))
                                }
                            }
                        }
                        Row(Modifier.horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            GifFit.entries.forEach {
                                DockChip(it.label, document.fit == it, enabled = !exporting) {
                                    commit(document.copy(fit = it))
                                }
                            }
                        }
                    }
                    GifTool.PROJECT -> {
                        Row(Modifier.horizontalScroll(rememberScrollState())
                            .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            DockChip("Save project", false,
                                enabled = GifMath.valid(document) && !exporting) { saveOpen = true }
                            DockChip("Open saved", false, enabled = !exporting) {
                                savedNames = GifStore.names(context); projectsOpen = true
                            }
                            DockChip("New GIF", false, enabled = !exporting) { newOpen = true }
                        }
                        Text("GIFs contain no music. For animation with audio, the future video workspace will export MP4.",
                            color = EditorMuted, fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 14.dp))
                    }
                }
            }
        }
        if (exporting) {
            Row(Modifier.align(Alignment.Center)
                .background(Color(0xDF20252E), RoundedCornerShape(14.dp))
                .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = EditorMint, modifier = Modifier.padding(end = 12.dp))
                Text("Exporting animated GIF…", color = EditorWhite)
            }
        }
    }

    if (helpOpen) GifHelpDialog { helpOpen = false }
    if (saveOpen) GifProjectSaveDialog("Save editable GIF project",
        "Save photo order, frame speed and canvas options. Keep the original photos.",
        document, savedName, onSaved = { name ->
            savedName = name
            saveOpen = false
            notice = "GIF project saved"
        }, onDismiss = { saveOpen = false })
    if (projectsOpen) GifSavedProjectsDialog(
        names = savedNames,
        enabled = !openingProject,
        onOpen = { name ->
            openingProject = true
            scope.launch {
                try {
                    val restored = withContext(Dispatchers.IO) { GifStore.load(context, name) }
                    projectsOpen = false
                    if (restored == null) notice = "Could not read the saved project or its source file."
                    else {
                        playing = false
                        index = 0
                        commit(restored, name)
                        notice = "GIF project opened"
                    }
                } finally { openingProject = false }
            }
        },
        onDelete = { name ->
            GifStore.deleteProject(context, name).also { deleted ->
                if (deleted) {
                    finishPhotoTiming()
                    if (savedName == name) savedName = null
                    history = history.withoutSavedProject(name)
                    savedNames = GifStore.names(context)
                }
            }
        },
        onDismiss = { projectsOpen = false }
    )
    if (newOpen) AlertDialog(
        onDismissRequest = { newOpen = false },
        title = { Text("Start a new GIF?") },
        text = { Text("Unsaved edits will be cleared. You can Undo after starting over.",
            color = EditorMuted) },
        confirmButton = { TextButton(onClick = {
            playing = false
            commit(GifDocument(), nextName = null)
            index = 0
            newOpen = false
        }) { Text("New GIF", color = EditorMint) } },
        dismissButton = { TextButton(onClick = { newOpen = false }) { Text("Keep current") } }
    )
}

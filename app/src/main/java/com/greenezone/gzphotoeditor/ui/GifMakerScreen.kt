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
import com.greenezone.gzphotoeditor.editor.GifAspect
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.GifEncoder
import com.greenezone.gzphotoeditor.editor.GifFit
import com.greenezone.gzphotoeditor.editor.GifHistory
import com.greenezone.gzphotoeditor.editor.GifMath
import com.greenezone.gzphotoeditor.editor.GifSize
import com.greenezone.gzphotoeditor.editor.GifStore
import com.greenezone.gzphotoeditor.editor.ImagePipeline
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

private enum class GifTool(val label: String) {
    FRAMES("Photos"), TIMING("Timing"), SIZE("Canvas"), PROJECT("Projects")
}

@Composable
fun GifMakerScreen(
    document: GifDocument,
    onDocument: (GifDocument) -> Unit,
    onBack: () -> Unit,
    onSwitch: (WorkspaceMode) -> Unit
) {
    var photoSequenceOnly by remember { mutableStateOf(false) }
    if (!photoSequenceOnly) {
        VideoGifScreen(
            document = document,
            onDocument = onDocument,
            onBack = onBack,
            onSwitch = onSwitch,
            onAnimatePhotos = {
                if (document.video != null) {
                    onDocument(document.copy(frames = emptyList(), video = null))
                }
                photoSequenceOnly = true
            }
        )
        return
    }

    val context = LocalContext.current
    val resolver = context.contentResolver
    val scope = rememberCoroutineScope()
    var tool by remember { mutableStateOf(GifTool.FRAMES) }
    var index by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var exporting by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf("") }
    var history by remember { mutableStateOf(GifHistory()) }
    var timingStart by remember { mutableStateOf<GifDocument?>(null) }
    var pending by remember { mutableStateOf<GifDocument?>(null) }
    var saveOpen by remember { mutableStateOf(false) }
    var projectsOpen by remember { mutableStateOf(false) }
    var newOpen by remember { mutableStateOf(false) }
    var fileName by remember { mutableStateOf("") }
    var savedNames by remember { mutableStateOf<List<String>>(emptyList()) }

    fun commit(next: GifDocument) {
        history = history.checkpoint(document, next)
        timingStart = null
        onDocument(next)
        index = index.coerceAtMost(next.frames.lastIndex).coerceAtLeast(0)
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

    BackHandler { if (!exporting) onBack() }
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
                    resolver.openOutputStream(uri, "wt")?.use { out ->
                        GifEncoder.write(out, saved, cancel = { job.ensureActive() }) { i ->
                            ImagePipeline.decode(resolver, Uri.parse(saved.frames[i]),
                                saved.size.maxEdge * 2, 1_100_000L)
                        }
                    } ?: throw IOException("Could not open GIF destination")
                }
                notice = "GIF exported · " + saved.frames.size + " frames · no watermark"
            } catch (cancelled: CancellationException) {
                try { resolver.delete(uri, null, null) } catch (_: Exception) {}
                throw cancelled
            } catch (_: OutOfMemoryError) {
                notice = "Not enough memory to encode GIF. Choose a smaller size."
                try { resolver.delete(uri, null, null) } catch (_: Exception) {}
            } catch (failure: Exception) {
                notice = "GIF export failed: " + (failure.message ?: "source missing")
                try { resolver.delete(uri, null, null) } catch (_: Exception) {}
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
                TextButton(enabled = !exporting, onClick = onBack) {
                    Text("‹ Back", color = EditorWhite, fontSize = 13.sp)
                }
                Text("GIF maker", color = EditorWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                TextButton(enabled = !exporting, onClick = { photoSequenceOnly = false }) {
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
                    onClick = { history.undo(document)?.let { (d,h) ->
                        playing = false; history = h; onDocument(d)
                        index = index.coerceAtMost(d.frames.lastIndex).coerceAtLeast(0)
                    } }) { Text("Undo", color = EditorWhite) }
                TextButton(enabled = history.redo.isNotEmpty() && !exporting,
                    onClick = { history.redo(document)?.let { (d,h) ->
                        playing = false; history = h; onDocument(d)
                        index = index.coerceAtMost(d.frames.lastIndex).coerceAtLeast(0)
                    } }) { Text("Redo", color = EditorWhite) }
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
                                if (timingStart == null) timingStart = document
                                onDocument(document.copy(delayMs = (next.toInt() / 10) * 10))
                            },
                            onValueChangeFinished = {
                                timingStart?.let { history = history.checkpoint(it, document) }
                                timingStart = null
                            },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp))
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
                            GifSize.entries.forEach {
                                DockChip(it.label, document.size == it, enabled = !exporting) {
                                    commit(document.copy(size = it))
                                }
                            }
                        }
                        Row(Modifier.horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            GifAspect.entries.forEach {
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

    if (saveOpen) SaveNameDialog("Save editable GIF project",
        "Save photo order, frame speed and canvas options. Keep the original photos.",
        fileName, { fileName = it }, {
            if (GifStore.save(context, fileName, document)) {
                saveOpen = false
                notice = "GIF project saved"
            } else notice = "Could not save. Choose a name (12 maximum)."
        }, { saveOpen = false })
    if (projectsOpen) AlertDialog(
        onDismissRequest = { projectsOpen = false },
        title = { Text("Saved GIF projects") },
        text = {
            Column(Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState())) {
                if (savedNames.isEmpty()) Text("No saved GIF projects yet.", color = EditorMuted)
                savedNames.forEach { name ->
                    TextButton(onClick = {
                        val restored = GifStore.load(context, name)
                        if (restored == null) notice = "Saved GIF project unreadable"
                        else {
                            playing = false; commit(restored); index = 0
                            projectsOpen = false; notice = "GIF project opened"
                            if (restored.video != null) photoSequenceOnly = false
                        }
                    }) { Text(name, color = EditorMint) }
                }
            }
        },
        confirmButton = { TextButton(onClick = { projectsOpen = false }) { Text("Close") } }
    )
    if (newOpen) AlertDialog(
        onDismissRequest = { newOpen = false },
        title = { Text("Start a new GIF?") },
        text = { Text("Unsaved edits will be cleared. You can Undo after starting over.",
            color = EditorMuted) },
        confirmButton = { TextButton(onClick = {
            playing = false; commit(GifDocument()); index = 0; newOpen = false
        }) { Text("New GIF", color = EditorMint) } },
        dismissButton = { TextButton(onClick = { newOpen = false }) { Text("Keep current") } }
    )
}

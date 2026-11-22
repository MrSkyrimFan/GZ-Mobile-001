package com.greenezone.gzphotoeditor.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.VideoView
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
import com.greenezone.gzphotoeditor.editor.GifVideoClip
import com.greenezone.gzphotoeditor.editor.GifVideoMath
import com.greenezone.gzphotoeditor.editor.VideoGifSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.math.min
import kotlin.math.roundToLong

private enum class VideoGifTab(val title: String) {
    TRIM("Trim"), PLAYBACK("Playback"), CANVAS("Canvas"), PROJECTS("Projects")
}
private fun timeLabel(ms: Long): String =
    (ms / 60_000).toString() + ":" + ((ms / 1_000) % 60).toString().padStart(2, '0') +
        "." + ((ms % 1_000) / 100).toString()

/** Real video-to-GIF editing without claiming the separate video editor exists. */
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
    var activeTab by remember { mutableStateOf(VideoGifTab.TRIM) }
    var selectedFrame by remember { mutableIntStateOf(0) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    var playing by remember { mutableStateOf(false) }
    var videoPlayer by remember { mutableStateOf<VideoView?>(null) }
    var playerReady by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf("") }
    var history by remember { mutableStateOf(GifHistory()) }
    var dragStart by remember { mutableStateOf<GifDocument?>(null) }
    var outputSnapshot by remember { mutableStateOf<GifDocument?>(null) }
    var askSave by remember { mutableStateOf(false) }
    var savedOpen by remember { mutableStateOf(false) }
    var askNew by remember { mutableStateOf(false) }
    var saveName by remember { mutableStateOf("") }
    var savedNames by remember { mutableStateOf<List<String>>(emptyList()) }

    fun commit(next: GifDocument) {
        history = history.checkpoint(document, next)
        dragStart = null
        onDocument(next)
        if (next.video?.uri != document.video?.uri) {
            playing = false
            playerReady = false
        }
        val count = next.video?.let { if (GifVideoMath.valid(it)) GifVideoMath.frameCount(it) else 1 } ?: 1
        selectedFrame = selectedFrame.coerceIn(0, count - 1)
    }
    fun slide(next: GifDocument) {
        if (dragStart == null) dragStart = document
        onDocument(next)
        selectedFrame = 0
        playing = false
    }
    fun finishSlide() {
        dragStart?.let { history = history.checkpoint(it, document) }
        dragStart = null
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null && !busy && !exporting) {
            scope.launch {
                busy = true
                playing = false
                notice = "Reading local video…"
                try {
                    try {
                        resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } catch (_: SecurityException) {}
                    val newClip = withContext(Dispatchers.IO) { VideoGifSource.inspect(context, uri) }
                    commit(document.copy(frames = emptyList(), video = newClip))
                    selectedFrame = 0
                    activeTab = VideoGifTab.TRIM
                    notice = "Choose the clip section, then export as GIF."
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: Exception) {
                    notice = "Video unavailable: " + (failure.message ?: "unsupported codec")
                } finally { busy = false }
            }
        }
    }
    fun selectVideo() {
        if (!busy && !exporting) picker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
    }

    BackHandler { if (!exporting && !busy) onBack() }
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        val snapshot = outputSnapshot
        outputSnapshot = null
        val dest = result.data?.data
        if (result.resultCode == Activity.RESULT_OK && dest != null && snapshot?.video != null) {
            if (dest.toString() == snapshot.video.uri) {
                notice = "Refusing to overwrite the original video."
            } else {
                scope.launch {
                    exporting = true
                    playing = false
                    try {
                        withContext(Dispatchers.IO) {
                            val job = currentCoroutineContext()
                            resolver.openOutputStream(dest, "wt")?.use { stream ->
                                VideoGifSource.write(context, snapshot, stream,
                                    cancel = { job.ensureActive() },
                                    onProgress = { index, count ->
                                        if (index == count || index % 8 == 0) {
                                            scope.launch(Dispatchers.Main) {
                                                notice = "Encoding $index / $count frames…"
                                            }
                                        }
                                    })
                            } ?: throw IOException("Output file cannot be opened")
                        }
                        notice = "GIF exported. Your original video was not changed."
                    } catch (cancelled: CancellationException) {
                        try { resolver.delete(dest, null, null) } catch (_: Exception) {}
                        throw cancelled
                    } catch (_: OutOfMemoryError) {
                        notice = "Not enough memory. Choose a smaller GIF."
                        try { resolver.delete(dest, null, null) } catch (_: Exception) {}
                    } catch (error: Exception) {
                        notice = "GIF export failed: " + (error.message ?: "decoder error")
                        try { resolver.delete(dest, null, null) } catch (_: Exception) {}
                    } finally { exporting = false }
                }
            }
        }
    }
    fun startExport() {
        if (!GifMath.valid(document) || exporting || busy) return
        outputSnapshot = document
        exporter.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/gif"
            putExtra(Intent.EXTRA_TITLE, "GZEditorPro_VideoGIF.gif")
        })
    }

    DisposableEffect(preview) {
        val owned = preview
        onDispose { if (owned != null && !owned.isRecycled) owned.recycle() }
    }

    // A paused frame is rendered with actual GIF Fit/Fill/aspect settings.
    // During forward playback, the VideoView surface plays the real source instead.
    LaunchedEffect(clip, selectedFrame, document.size, document.aspect, document.fit,
        playing) {
        if (clip == null || (playing && !clip.reverse)) {
            preview = null
            return@LaunchedEffect
        }
        delay(75L) // Coalesce continuous dragging of trim and scrub controls.
        try {
            val bitmap = withContext(Dispatchers.IO) {
                val times = GifVideoMath.frameTimesMs(clip)
                val index = selectedFrame.coerceIn(0, times.lastIndex)
                val source = VideoGifSource.preview(
                    context, clip, times[index], document.size.maxEdge * 2)
                try {
                    val result = GifEncoder.renderFrame(source, document)
                    try {
                        currentCoroutineContext().ensureActive()
                        result
                    } catch (cancelled: CancellationException) {
                        if (!result.isRecycled) result.recycle()
                        throw cancelled
                    }
                } finally {
                    if (!source.isRecycled) source.recycle()
                }
            }
            preview = bitmap
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            preview = null
            playing = false
            notice = "Frame preview unavailable: " +
                (error.message ?: "unsupported frame")
        }
    }

    // Reverse playback cannot be performed natively by VideoView. Decode its
    // timestamped frames sequentially, waiting for each one to finish first.
    // This prevents the earlier timer from cancelling every in-flight decode.
    LaunchedEffect(playing, clip, document.forever) {
        if (!playing || clip == null || !clip.reverse) return@LaunchedEffect
        val count = GifVideoMath.frameCount(clip)
        while (true) {
            delay(GifVideoMath.gifDelayMs(clip).toLong())
            if (selectedFrame >= count - 1) {
                if (document.forever) selectedFrame = 0
                else {
                    playing = false
                    break
                }
            } else selectedFrame++
            // Wait until the visual frame decode has a chance to finish.
            delay(150L)
        }
    }
    LaunchedEffect(notice) {
        if (notice.isNotBlank()) {
            delay(5500L)
            notice = ""
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF080B10)),
        contentAlignment = Alignment.Center) {
        if (clip == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(13.dp),
                modifier = Modifier.padding(horizontal = 22.dp)) {
                Text("Select one video to turn into a GIF",
                    color = EditorWhite, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text("Choose a video from your phone.",
                    color = EditorMuted, fontSize = 13.sp)
                Button(onClick = ::selectVideo, enabled = !busy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EditorMint, contentColor = Color(0xFF09231A)
                    )) { Text("Choose video") }
                TextButton(
                    enabled = !busy && !exporting,
                    onClick = { savedNames = GifStore.names(context); savedOpen = true }
                ) { Text("Open saved GIF project", color = EditorMuted) }
            }
        } else {
            if (!clip.reverse) {
                NativeGifPlayback(
                    clip = clip,
                    playing = playing,
                    forever = document.forever,
                    onPosition = { ms ->
                        val fraction = ((ms - clip.startMs).toDouble() /
                            (clip.endMs - clip.startMs)).coerceIn(0.0, 0.9999)
                        selectedFrame = (fraction * GifVideoMath.frameCount(clip))
                            .toInt().coerceIn(0, GifVideoMath.frameCount(clip) - 1)
                    },
                    onEnded = { playing = false },
                    onError = { message -> playing = false; notice = message },
                    onReady = { playerReady = true },
                    onPlayer = { videoPlayer = it },
                    modifier = Modifier.fillMaxSize()
                )
            }
            if (!playing || clip.reverse) {
                preview?.let {
                    Image(bitmap = it.asImageBitmap(),
                        contentDescription = "GIF crop and size preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit)
                }
            }
        }

        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth()
            .background(Color(0xEB121820)).statusBarsPadding()
            .padding(horizontal = 8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack, enabled = !busy && !exporting) {
                    Text("‹ Back", color = EditorWhite)
                }
                Text("GIF · Video", color = EditorWhite,
                    fontWeight = FontWeight.Bold, fontSize = 16.sp)
                WorkspaceModeMenu(WorkspaceMode.GIF, !busy && !exporting, onSwitch)
            }
            if (clip != null) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = ::selectVideo, enabled = !busy && !exporting) {
                        Text("Change video", color = EditorWhite, fontSize = 12.sp)
                    }
                    TextButton(enabled = !busy && !exporting &&
                        (clip.reverse || playerReady),
                        onClick = {
                            if (!playing && !clip.reverse) {
                                val ms = GifVideoMath.frameTimesMs(clip)[selectedFrame
                                    .coerceIn(0, GifVideoMath.frameCount(clip) - 1)]
                                videoPlayer?.seekTo(ms.toInt())
                            }
                            playing = !playing
                        }
                    ) { Text(if (playing) "⏸ Pause" else "▶ Play video",
                        color = EditorMint, fontSize = 13.sp) }
                    Button(onClick = ::startExport, enabled = !busy && !exporting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EditorMint,
                            contentColor = Color(0xFF0B1E18))) {
                        Text("Save GIF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    TextButton(enabled = history.undo.isNotEmpty() && !busy && !exporting,
                        onClick = {
                            history.undo(document)?.let { (before, next) ->
                                playing = false
                                history = next
                                onDocument(before)
                                selectedFrame = 0
                            }
                        }) { Text("Undo", color = EditorMuted, fontSize = 12.sp) }
                    TextButton(enabled = history.redo.isNotEmpty() && !busy && !exporting,
                        onClick = {
                            history.redo(document)?.let { (after, next) ->
                                playing = false
                                history = next
                                onDocument(after)
                                selectedFrame = 0
                            }
                        }) { Text("Redo", color = EditorMuted, fontSize = 12.sp) }
                    Text("GIF output: ${GifVideoMath.frameCount(clip)} frames · " +
                        "${document.size.maxEdge}px · ${clip.fps} FPS",
                        color = EditorMuted, fontSize = 11.sp)
                }
            }
            if (notice.isNotBlank()) Text(notice, color = EditorMuted,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                fontSize = 12.sp, maxLines = 2)
        }

        if (clip != null) {
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(EditorSurface).navigationBarsPadding()
                .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    VideoGifTab.entries.forEach { tab ->
                        DockChip(tab.title, tab == activeTab, enabled = !busy && !exporting) {
                            playing = false; activeTab = tab
                        }
                    }
                }
                when (activeTab) {
                    VideoGifTab.TRIM -> {
                        Text("Clip: " + timeLabel(clip.startMs) + " to " +
                            timeLabel(clip.endMs) + "  ·  " +
                            GifVideoMath.frameCount(clip) + " frames",
                            color = EditorWhite, fontSize = 12.sp,
                            modifier = Modifier.padding(start = 15.dp))
                        Text("Start", color = EditorMuted, fontSize = 11.sp,
                            modifier = Modifier.padding(start = 15.dp))
                        Slider(
                            value = clip.startMs.toFloat(),
                            valueRange = 0f..(clip.durationMs - 500L).toFloat(),
                            enabled = !busy && !exporting,
                            onValueChange = { next ->
                                playing = false
                                val start = next.roundToLong()
                                val maxEnd = min(clip.durationMs, start + GifVideoMath.MAX_CLIP_MS)
                                val end = clip.endMs.coerceIn(start + 500L, maxEnd)
                                slide(document.copy(video = clip.copy(startMs = start, endMs = end)))
                                videoPlayer?.seekTo(start.toInt())
                            },
                            onValueChangeFinished = ::finishSlide,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp)
                        )
                        Text("End (maximum 12-second clip)", color = EditorMuted, fontSize = 11.sp,
                            modifier = Modifier.padding(start = 15.dp))
                        val last = min(clip.durationMs, clip.startMs + GifVideoMath.MAX_CLIP_MS)
                        Slider(
                            value = clip.endMs.toFloat(),
                            valueRange = (clip.startMs + 500L).toFloat()..last.toFloat(),
                            enabled = !busy && !exporting && last > clip.startMs + 500L,
                            onValueChange = { next ->
                                playing = false
                                val end = next.roundToLong()
                                    .coerceIn(clip.startMs + 500L, last)
                                slide(document.copy(video = clip.copy(endMs = end)))
                                videoPlayer?.seekTo((end - 1L).toInt())
                            },
                            onValueChangeFinished = ::finishSlide,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp)
                        )
                        Text("Scrub frames", color = EditorMuted, fontSize = 11.sp,
                            modifier = Modifier.padding(start = 15.dp))
                        Slider(
                            value = selectedFrame.toFloat().coerceIn(0f,
                                (GifVideoMath.frameCount(clip) - 1).toFloat()),
                            valueRange = 0f..(GifVideoMath.frameCount(clip) - 1).toFloat(),
                            enabled = !exporting && !busy,
                            onValueChange = {
                                playing = false
                                selectedFrame = it.toInt()
                                if (!clip.reverse) {
                                    val times = GifVideoMath.frameTimesMs(clip)
                                    videoPlayer?.seekTo(times[selectedFrame.coerceIn(
                                        0, times.lastIndex)].toInt())
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 13.dp)
                        )
                    }
                    VideoGifTab.PLAYBACK -> {
                        Text("GIF frame rate (export)", color = EditorMuted, fontSize = 12.sp,
                            modifier = Modifier.padding(start = 15.dp))
                        Row(Modifier.horizontalScroll(rememberScrollState())
                            .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            GifVideoMath.FRAME_RATES.forEach { fps ->
                                DockChip("$fps FPS", clip.fps == fps, enabled = !busy && !exporting) {
                                    selectedFrame = 0
                                    commit(document.copy(video = clip.copy(fps = fps)))
                                }
                            }
                        }
                        Text("Playback speed (video preview and GIF)", color = EditorMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 15.dp))
                        Row(Modifier.horizontalScroll(rememberScrollState())
                            .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            GifVideoMath.SPEEDS.forEach { speed ->
                                DockChip("${speed}%", clip.speedPercent == speed,
                                    enabled = !busy && !exporting) {
                                    commit(document.copy(video = clip.copy(speedPercent = speed)))
                                }
                            }
                            DockChip("Reverse", clip.reverse, enabled = !busy && !exporting) {
                                selectedFrame = 0
                                commit(document.copy(video = clip.copy(reverse = !clip.reverse)))
                            }
                        }
                        Row(Modifier.horizontalScroll(rememberScrollState())
                            .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            DockChip("Loop", document.forever, enabled = !busy && !exporting) {
                                commit(document.copy(forever = true))
                            }
                            DockChip("Play once", !document.forever, enabled = !busy && !exporting) {
                                commit(document.copy(forever = false))
                            }
                        }
                    }
                    VideoGifTab.CANVAS -> {
                        Row(Modifier.horizontalScroll(rememberScrollState())
                            .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            GifSize.entries.forEach {
                                DockChip(it.label, document.size == it, enabled = !busy && !exporting) {
                                    commit(document.copy(size = it))
                                }
                            }
                        }
                        Row(Modifier.horizontalScroll(rememberScrollState())
                            .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            GifAspect.entries.forEach {
                                DockChip(it.label, document.aspect == it, enabled = !busy && !exporting) {
                                    commit(document.copy(aspect = it))
                                }
                            }
                        }
                        Row(Modifier.horizontalScroll(rememberScrollState())
                            .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            GifFit.entries.forEach {
                                DockChip(it.label, document.fit == it, enabled = !busy && !exporting) {
                                    commit(document.copy(fit = it))
                                }
                            }
                        }
                    }
                    VideoGifTab.PROJECTS -> {
                        Row(Modifier.horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            DockChip("Save project", false, enabled = !busy && !exporting) {
                                askSave = true
                            }
                            DockChip("Open saved", false, enabled = !busy && !exporting) {
                                savedNames = GifStore.names(context); savedOpen = true
                            }
                            DockChip("Start over", false, enabled = !busy && !exporting) {
                                askNew = true
                            }
                        }
                        Text("Choose Save GIF above to export an animated GIF. GIF has no audio.",
                            color = EditorMuted, fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 13.dp))
                    }
                }
            }
        }
        if (busy || exporting) {
            Row(Modifier.align(Alignment.Center)
                .background(Color(0xDC1A222A), RoundedCornerShape(12.dp))
                .padding(13.dp),
                verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = EditorMint,
                    modifier = Modifier.padding(end = 12.dp))
                Text(if (exporting) "Writing GIF…" else "Reading video…", color = EditorWhite)
            }
        }
    }

    if (askSave) SaveNameDialog("Save video GIF project",
        "This saves trimming and playback settings, not a copy of the original video.",
        saveName, { saveName = it }, {
            if (GifStore.save(context, saveName, document)) {
                askSave = false; notice = "Project saved locally"
            } else notice = "Could not save (up to 12 projects)."
        }, { askSave = false })

    if (savedOpen) AlertDialog(
        onDismissRequest = { savedOpen = false },
        title = { Text("Saved GIF projects") },
        text = {
            Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                if (savedNames.isEmpty()) Text("No projects saved yet.", color = EditorMuted)
                savedNames.forEach { name ->
                    TextButton(onClick = {
                        val restored = GifStore.load(context, name)
                        if (restored?.video != null) {
                            commit(restored); selectedFrame = 0; savedOpen = false
                        } else if (restored != null && restored.frames.size >= 2) {
                            savedOpen = false
                            onOpenLegacyPhotoProject(restored)
                        } else notice = "This saved GIF project is unreadable."
                    }) { Text(name, color = EditorMint) }
                }
            }
        },
        confirmButton = { TextButton(onClick = { savedOpen = false }) { Text("Close") } }
    )
    if (askNew) AlertDialog(
        onDismissRequest = { askNew = false },
        title = { Text("Leave this video GIF?") },
        text = { Text("Save your edit first if you want to reopen it later. Undo history may restore it.",
            color = EditorMuted) },
        confirmButton = { TextButton(onClick = {
            playing = false
            commit(GifDocument())
            selectedFrame = 0
            askNew = false
        }) { Text("Continue", color = EditorMint) } },
        dismissButton = { TextButton(onClick = {
            askNew = false
        }) { Text("Cancel") } }
    )
}

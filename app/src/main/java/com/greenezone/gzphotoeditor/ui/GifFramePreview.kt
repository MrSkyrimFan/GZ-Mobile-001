package com.greenezone.gzphotoeditor.ui

import android.graphics.Bitmap
import android.os.SystemClock
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.GifEncoder
import com.greenezone.gzphotoeditor.editor.GifFrameReader
import com.greenezone.gzphotoeditor.editor.GifMath
import com.greenezone.gzphotoeditor.editor.GifPreviewClock
import com.greenezone.gzphotoeditor.editor.GifPreviewDiagnostics
import com.greenezone.gzphotoeditor.editor.GifDiagnosticKind
import com.greenezone.gzphotoeditor.editor.GifDiagnosticStage
import com.greenezone.gzphotoeditor.editor.GifPreviewRoute
import com.greenezone.gzphotoeditor.editor.GifVideoMath
import com.greenezone.gzphotoeditor.editor.VideoGifSource
import com.greenezone.gzphotoeditor.editor.previewRoute
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicLong

private data class PreviewFrame(val index: Int, val bitmap: Bitmap)

@Composable
internal fun GifFramePreview(
    document: GifDocument,
    operationId: Long,
    playing: Boolean,
    startFrame: Int,
    onFrame: (Int) -> Unit,
    onEnd: (Int) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clip = document.video ?: return
    val context = LocalContext.current
    var shown by remember(clip.uri) { mutableStateOf<PreviewFrame?>(null) }
    val requestedFrame by rememberUpdatedState(startFrame)
    val reportFrame by rememberUpdatedState(onFrame)
    val reportEnd by rememberUpdatedState(onEnd)
    val reportError by rememberUpdatedState(onError)
    val cache = remember(document) {
        object : LruCache<Int, Bitmap>(12 * 1024 * 1024) {
            override fun sizeOf(key: Int, value: Bitmap) = value.allocationByteCount
        }
    }
    var nativeFailed by remember(clip.uri) { mutableStateOf(false) }
    var resumeFrom by remember(document, playing) { mutableStateOf<Int?>(null) }
    val generationCounter = remember(operationId) { AtomicLong() }
    val activeGeneration = remember(operationId) { AtomicLong() }
    val generation = remember(operationId, document, playing, nativeFailed) {
        generationCounter.incrementAndGet()
    }
    val route = remember(document, playing, nativeFailed) {
        document.previewRoute(playing, nativeFailed)
    }
    DisposableEffect(operationId, generation) {
        activeGeneration.set(generation)
        onDispose {
            activeGeneration.compareAndSet(generation, 0L)
            GifPreviewDiagnostics.record(operationId, generation,
                GifDiagnosticStage.G7_LIFECYCLE, GifDiagnosticKind.PREVIEW_CLOSED, route)
        }
    }
    LaunchedEffect(operationId, generation, route) {
        GifPreviewDiagnostics.record(operationId, generation,
            if (playing) GifDiagnosticStage.G2_PLAY else GifDiagnosticStage.G4_SEEK,
            GifDiagnosticKind.PREVIEW_ROUTE_SELECTED, route)
    }
    if (route == GifPreviewRoute.NATIVE_SOURCE ||
            route == GifPreviewRoute.SCHEDULED_CODEC) {
        val frameCount = GifVideoMath.frameCount(clip)
        val continuousFrame: (Int) -> Unit = { frame ->
            if (activeGeneration.get() == generation) {
                GifPreviewDiagnostics.record(operationId, generation,
                    GifDiagnosticStage.G3_CONTINUOUS_PLAY, GifDiagnosticKind.FRAME_SETTLED,
                    route, frameIndex = frame, frameCount = frameCount)
                reportFrame(frame)
            } else {
                GifPreviewDiagnostics.record(operationId, generation,
                    GifDiagnosticStage.G3_CONTINUOUS_PLAY,
                    GifDiagnosticKind.STALE_FRAME_DISCARDED, route,
                    frameIndex = frame, frameCount = frameCount)
            }
        }
        val continuousEnd: (Int) -> Unit = { frame ->
            if (activeGeneration.get() == generation) {
                GifPreviewDiagnostics.record(operationId, generation,
                    GifDiagnosticStage.G3_CONTINUOUS_PLAY, GifDiagnosticKind.PLAYBACK_ENDED,
                    route, frameIndex = frame, frameCount = frameCount)
                reportEnd(frame)
            }
        }
        val fallback: (Int) -> Unit = { frame ->
            if (activeGeneration.get() == generation) {
                GifPreviewDiagnostics.record(operationId, generation,
                    GifDiagnosticStage.G2_PLAY, GifDiagnosticKind.FALLBACK_STARTED,
                    route, frameIndex = frame, frameCount = frameCount)
                resumeFrom = frame
                nativeFailed = true
            }
        }
        if (route == GifPreviewRoute.NATIVE_SOURCE) {
            NativeGifPlayback(clip, startFrame, document.loopCount,
                continuousFrame, continuousEnd, fallback, modifier)
        } else {
            ScheduledGifPlayback(clip, startFrame, document.loopCount,
                continuousFrame, continuousEnd, fallback, modifier)
        }
        return
    }
    LaunchedEffect(operationId, generation, document, playing) {
        try {
            withContext(Dispatchers.IO) {
                val times = GifVideoMath.frameTimesMs(clip)
                val (width, height) = GifMath.dimensions(document, maxEdge = 480)
                val neutral = document.edits == EditDocument()
                var reader: GifFrameReader? = null
                try {
                    suspend fun decode(index: Int): PreviewFrame {
                        val job = currentCoroutineContext()
                        job.ensureActive()
                        val startedAt = System.nanoTime()
                        val cached = cache.get(index)
                        if (cached != null) {
                            if (activeGeneration.get() != generation) {
                                GifPreviewDiagnostics.record(operationId, generation,
                                    if (playing) GifDiagnosticStage.G3_CONTINUOUS_PLAY
                                    else GifDiagnosticStage.G4_SEEK,
                                    GifDiagnosticKind.STALE_FRAME_DISCARDED, route,
                                    frameIndex = index, frameCount = times.size)
                                throw CancellationException("Superseded GIF preview generation")
                            }
                            GifPreviewDiagnostics.record(operationId, generation,
                                if (playing) GifDiagnosticStage.G3_CONTINUOUS_PLAY
                                else GifDiagnosticStage.G4_SEEK,
                                GifDiagnosticKind.FRAME_SETTLED, route,
                                frameIndex = index, frameCount = times.size,
                                latencyNanos = System.nanoTime() - startedAt)
                            return PreviewFrame(index, cached)
                        }
                        val decoder = reader ?: VideoGifSource.open(context, clip, batchFrames = playing)
                            .also {
                                reader = it
                                GifPreviewDiagnostics.record(operationId, generation,
                                    GifDiagnosticStage.G7_LIFECYCLE,
                                    GifDiagnosticKind.READER_OPENED, route)
                            }
                        val source = decoder.frame(times[index], 480)
                        if (activeGeneration.get() != generation) {
                            if (!source.isRecycled) source.recycle()
                            GifPreviewDiagnostics.record(operationId, generation,
                                if (playing) GifDiagnosticStage.G3_CONTINUOUS_PLAY
                                else GifDiagnosticStage.G4_SEEK,
                                GifDiagnosticKind.STALE_FRAME_DISCARDED, route,
                                frameIndex = index, frameCount = times.size)
                            throw CancellationException("Superseded GIF preview generation")
                        }
                        val result = if (neutral && source.width == width && source.height == height) source
                            else try {
                                GifEncoder.renderFrame(source, document, maxEdge = 480,
                                    cancel = { job.ensureActive() })
                            } finally {
                                source.recycle()
                            }
                        if (activeGeneration.get() != generation) {
                            if (!result.isRecycled) result.recycle()
                            GifPreviewDiagnostics.record(operationId, generation,
                                if (playing) GifDiagnosticStage.G3_CONTINUOUS_PLAY
                                else GifDiagnosticStage.G4_SEEK,
                                GifDiagnosticKind.STALE_FRAME_DISCARDED, route,
                                frameIndex = index, frameCount = times.size)
                            throw CancellationException("Superseded GIF preview generation")
                        }
                        cache.put(index, result)
                        GifPreviewDiagnostics.record(operationId, generation,
                            if (playing) GifDiagnosticStage.G3_CONTINUOUS_PLAY
                            else GifDiagnosticStage.G4_SEEK,
                            GifDiagnosticKind.FRAME_SETTLED, route,
                            frameIndex = index, frameCount = times.size,
                            latencyNanos = System.nanoTime() - startedAt)
                        return PreviewFrame(index, result)
                    }
                    if (!playing) {
                        snapshotFlow { requestedFrame }.conflate().collect { request ->
                            val frame = decode(request.coerceIn(times.indices))
                            withContext(Dispatchers.Main.immediate) {
                                if (requestedFrame == request &&
                                    activeGeneration.get() == generation) {
                                    shown = frame
                                } else {
                                    GifPreviewDiagnostics.record(operationId, generation,
                                        GifDiagnosticStage.G4_SEEK,
                                        GifDiagnosticKind.STALE_FRAME_DISCARDED, route,
                                        frameIndex = frame.index, frameCount = times.size)
                                }
                            }
                        }
                    } else {
                        suspend fun present(frame: PreviewFrame) = withContext(Dispatchers.Main.immediate) {
                            if (activeGeneration.get() == generation) {
                                shown = frame
                                reportFrame(frame.index)
                            } else {
                                GifPreviewDiagnostics.record(operationId, generation,
                                    GifDiagnosticStage.G3_CONTINUOUS_PLAY,
                                    GifDiagnosticKind.STALE_FRAME_DISCARDED, route,
                                    frameIndex = frame.index, frameCount = times.size)
                            }
                        }
                        val clock = GifPreviewClock(GifVideoMath.frameDelaysMs(clip))
                        val first = (resumeFrom ?: requestedFrame).coerceIn(times.indices)
                        present(decode(first))
                        var position = clock.startOf(first)
                        val origin = SystemClock.uptimeMillis() - position
                        val end = clock.endTime(document.loopCount)
                        while (true) {
                            val next = clock.nextBoundary(position)
                            if (next >= end) break
                            // Slow decoders may skip overdue preview samples. Export keeps every frame.
                            position = maxOf(next, SystemClock.uptimeMillis() - origin).coerceAtMost(end - 1)
                            val frame = decode(clock.frameAt(position))
                            delay((origin + position - SystemClock.uptimeMillis()).coerceAtLeast(0))
                            present(frame)
                        }
                        delay((origin + end - SystemClock.uptimeMillis()).coerceAtLeast(0))
                        withContext(Dispatchers.Main.immediate) {
                            if (activeGeneration.get() == generation) {
                                GifPreviewDiagnostics.record(operationId, generation,
                                    GifDiagnosticStage.G3_CONTINUOUS_PLAY,
                                    GifDiagnosticKind.PLAYBACK_ENDED, route,
                                    frameIndex = times.lastIndex, frameCount = times.size)
                                reportEnd(times.lastIndex)
                            }
                        }
                    }
                } finally {
                    reader?.let { opened ->
                        try {
                            opened.close()
                        } finally {
                            GifPreviewDiagnostics.record(operationId, generation,
                                GifDiagnosticStage.G7_LIFECYCLE,
                                GifDiagnosticKind.READER_CLOSED, route)
                        }
                    }
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            GifPreviewDiagnostics.record(operationId, generation,
                GifDiagnosticStage.G2_PLAY, GifDiagnosticKind.PREVIEW_FAILED,
                route, failure = error)
            shown = null
            reportError(error.message ?: "Cannot decode the selected frame")
        } catch (error: OutOfMemoryError) {
            GifPreviewDiagnostics.record(operationId, generation,
                GifDiagnosticStage.G2_PLAY, GifDiagnosticKind.PREVIEW_FAILED,
                route, failure = error)
            cache.evictAll()
            shown = null
            reportError("There is not enough memory to preview this source")
        }
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        val frame = shown
        if (frame != null) {
            Image(frame.bitmap.asImageBitmap(), "GIF preview, frame ${frame.index + 1}",
                modifier = Modifier.matchParentSize().testTag("gif-frame"), contentScale = ContentScale.Fit)
        } else {
            CircularProgressIndicator(color = EditorMint)
        }
    }
}

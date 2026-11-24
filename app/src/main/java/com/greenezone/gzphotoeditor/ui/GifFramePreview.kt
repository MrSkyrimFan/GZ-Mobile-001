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
import com.greenezone.gzphotoeditor.editor.EditedVideoPreviewReader
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
import com.greenezone.gzphotoeditor.editor.afterBitmapCodecFailure
import com.greenezone.gzphotoeditor.editor.previewRoute
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

private data class PreviewFrame(val index: Int, val bitmap: Bitmap)

@Composable
internal fun GifFramePreview(
    document: GifDocument,
    operationId: Long,
    playing: Boolean,
    startFrame: Int,
    editedCodecFailed: AtomicBoolean,
    readerOwner: Mutex,
    onFrame: (Int) -> Unit,
    onEnd: (Int) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clip = document.video ?: return
    val context = LocalContext.current
    var shown by remember(operationId, clip.uri) { mutableStateOf<PreviewFrame?>(null) }
    val requestedFrame by rememberUpdatedState(startFrame)
    val reportFrame by rememberUpdatedState(onFrame)
    val reportEnd by rememberUpdatedState(onEnd)
    val reportError by rememberUpdatedState(onError)
    val cache = remember(document) {
        object : LruCache<Int, Bitmap>(12 * 1024 * 1024) {
            override fun sizeOf(key: Int, value: Bitmap) = value.allocationByteCount
        }
    }
    val reversePreviewWindow = remember(
        operationId, clip.uri, clip.startMs, clip.endMs, clip.reverse, clip.isGif,
        clip.width, clip.height, clip.displayWidth, clip.displayHeight, clip.sourceTimesMs
    ) {
        if (clip.reverse && !clip.isGif) EditedVideoPreviewReader.ReversePreviewWindow() else null
    }
    // A still from a different reverse range cannot supply its neighboring frames.
    val sourceCache = remember(operationId, clip.uri, clip.displayWidth, clip.displayHeight, reversePreviewWindow) {
        object : LruCache<Long, Bitmap>(4 * 1024 * 1024) {
            override fun sizeOf(key: Long, value: Bitmap) = value.allocationByteCount
        }
    }
    var nativeFailed by remember(operationId, clip.uri) { mutableStateOf(false) }
    var resumeFrom by remember(document, playing) { mutableStateOf<Int?>(null) }
    val generationCounter = remember(operationId) { AtomicLong() }
    val activeGeneration = remember(operationId) { AtomicLong() }
    val prepareReverseStill = !playing && startFrame == 0 && reversePreviewWindow != null &&
        clip.sourceTimesMs.isNotEmpty()
    val generation = remember(operationId, clip.uri, document, playing, nativeFailed, prepareReverseStill) {
        generationCounter.incrementAndGet()
    }
    val preferredRoute = remember(document, playing, nativeFailed, prepareReverseStill) {
        if (prepareReverseStill) GifPreviewRoute.BITMAP_CODEC
        else document.previewRoute(playing, nativeFailed)
    }
    val route = remember(operationId, generation) {
        preferredRoute.afterBitmapCodecFailure(editedCodecFailed.get())
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
            // A continuous drag should only spend decoding time on the newest still.
            if (!playing) delay(32)
            withContext(Dispatchers.IO) {
                readerOwner.withLock {
                    val times = GifVideoMath.frameTimesMs(clip)
                    val (width, height) = GifMath.dimensions(document, maxEdge = 480)
                    val neutral = document.edits == EditDocument()
                    var reader: GifFrameReader? = null
                    // Reread after taking ownership in case the previous reader failed
                    // while this effect was queued behind it.
                    var bitmapRoute = route.afterBitmapCodecFailure(editedCodecFailed.get())
                    val playbackJob = currentCoroutineContext()
                    fun checkCurrent() {
                        playbackJob.ensureActive()
                        if (activeGeneration.get() != generation)
                            throw CancellationException("Superseded GIF preview generation")
                    }
                    fun openReader(): GifFrameReader {
                        val opened = if (bitmapRoute == GifPreviewRoute.BITMAP_CODEC) {
                            EditedVideoPreviewReader.open(context, clip, prefetchReverseFrames = playing,
                                reversePreviewWindow = reversePreviewWindow, checkActive = ::checkCurrent)
                        } else {
                            VideoGifSource.open(context, clip, batchFrames = playing,
                                allowScaledBatches = playing && preferredRoute == GifPreviewRoute.BITMAP_CODEC)
                        }
                        reader = opened
                        GifPreviewDiagnostics.record(operationId, generation,
                            GifDiagnosticStage.G7_LIFECYCLE, GifDiagnosticKind.READER_OPENED, bitmapRoute)
                        return opened
                    }
                    fun closeReader() {
                        val opened = reader ?: return
                        reader = null
                        try { opened.close() } finally {
                            GifPreviewDiagnostics.record(operationId, generation,
                                GifDiagnosticStage.G7_LIFECYCLE, GifDiagnosticKind.READER_CLOSED, bitmapRoute)
                        }
                    }
                    fun readFrame(time: Long): Bitmap {
                        checkCurrent()
                        return try {
                            (reader ?: openReader()).frame(time, 480)
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (error: Exception) {
                            if (bitmapRoute != GifPreviewRoute.BITMAP_CODEC) throw error
                            checkCurrent()
                            GifPreviewDiagnostics.record(operationId, generation,
                                GifDiagnosticStage.G2_PLAY, GifDiagnosticKind.FALLBACK_STARTED, bitmapRoute,
                                failure = error)
                            editedCodecFailed.set(true)
                            closeReader()
                            bitmapRoute = GifPreviewRoute.BITMAP_RETRIEVER
                            GifPreviewDiagnostics.record(operationId, generation,
                                GifDiagnosticStage.G2_PLAY, GifDiagnosticKind.PREVIEW_ROUTE_SELECTED, bitmapRoute)
                            openReader().frame(time, 480)
                        }
                    }
                    try {
                        suspend fun decode(index: Int, warmDecoder: Boolean = false): PreviewFrame {
                            checkCurrent()
                            val startedAt = System.nanoTime()
                            val cached = if (warmDecoder) null else cache.get(index)
                            if (cached != null) {
                                if (activeGeneration.get() != generation) {
                                    GifPreviewDiagnostics.record(operationId, generation,
                                        if (playing) GifDiagnosticStage.G3_CONTINUOUS_PLAY
                                        else GifDiagnosticStage.G4_SEEK,
                                        GifDiagnosticKind.STALE_FRAME_DISCARDED, bitmapRoute,
                                        frameIndex = index, frameCount = times.size)
                                    throw CancellationException("Superseded GIF preview generation")
                                }
                                GifPreviewDiagnostics.record(operationId, generation,
                                    if (playing) GifDiagnosticStage.G3_CONTINUOUS_PLAY
                                    else GifDiagnosticStage.G4_SEEK,
                                    GifDiagnosticKind.FRAME_SETTLED, bitmapRoute,
                                    frameIndex = index, frameCount = times.size,
                                    latencyNanos = System.nanoTime() - startedAt)
                                return PreviewFrame(index, cached)
                            }
                            val time = times[index]
                            // A grade slider edits the same source sample. Reuse the decoded
                            // still instead of reopening/seeking the video on every move.
                            val reusable = !playing
                            val source = (if (reusable) sourceCache.get(time) else null) ?: readFrame(time).also {
                                try { checkCurrent() } catch (cancelled: CancellationException) {
                                    it.recycle()
                                    throw cancelled
                                }
                                if (reusable) sourceCache.put(time, it)
                            }
                            val result = if (neutral && source.width == width && source.height == height) source
                                else try {
                                    GifEncoder.renderFrame(source, document, maxEdge = 480,
                                        cancel = ::checkCurrent)
                                } finally {
                                    if (!reusable) source.recycle()
                                }
                            if (activeGeneration.get() != generation) {
                                if ((!reusable || result !== source) && !result.isRecycled) result.recycle()
                                GifPreviewDiagnostics.record(operationId, generation,
                                    if (playing) GifDiagnosticStage.G3_CONTINUOUS_PLAY
                                    else GifDiagnosticStage.G4_SEEK,
                                    GifDiagnosticKind.STALE_FRAME_DISCARDED, bitmapRoute,
                                    frameIndex = index, frameCount = times.size)
                                throw CancellationException("Superseded GIF preview generation")
                            }
                            cache.put(index, result)
                            GifPreviewDiagnostics.record(operationId, generation,
                                if (playing) GifDiagnosticStage.G3_CONTINUOUS_PLAY
                                else GifDiagnosticStage.G4_SEEK,
                                GifDiagnosticKind.FRAME_SETTLED, bitmapRoute,
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
                                            GifDiagnosticKind.STALE_FRAME_DISCARDED, bitmapRoute,
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
                                        GifDiagnosticKind.STALE_FRAME_DISCARDED, bitmapRoute,
                                        frameIndex = frame.index, frameCount = times.size)
                                }
                            }
                            val clock = GifPreviewClock(GifVideoMath.frameDelaysMs(clip))
                            val first = (resumeFrom ?: requestedFrame).coerceIn(times.indices)
                            // A displayed still alone does not prepare a reverse sequence.
                            // The reader can use the paused decoder's prepared pictures.
                            // Start the clock once that sequence can supply moving frames.
                            present(decode(first, warmDecoder = bitmapRoute == GifPreviewRoute.BITMAP_CODEC))
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
                                        GifDiagnosticKind.PLAYBACK_ENDED, bitmapRoute,
                                        frameIndex = times.lastIndex, frameCount = times.size)
                                    reportEnd(times.lastIndex)
                                }
                            }
                        }
                    } finally {
                        closeReader()
                    }
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            if (activeGeneration.get() != generation) return@LaunchedEffect
            GifPreviewDiagnostics.record(operationId, generation,
                GifDiagnosticStage.G2_PLAY, GifDiagnosticKind.PREVIEW_FAILED,
                route, failure = error)
            shown = null
            reportError(error.message ?: "Cannot decode the selected frame")
        } catch (error: OutOfMemoryError) {
            if (activeGeneration.get() != generation) return@LaunchedEffect
            sourceCache.evictAll()
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

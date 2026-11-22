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
import com.greenezone.gzphotoeditor.editor.GifVideoMath
import com.greenezone.gzphotoeditor.editor.VideoGifSource
import com.greenezone.gzphotoeditor.editor.canPlayContinuousVideo
import com.greenezone.gzphotoeditor.editor.canPlaySourceDirectly
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

private data class PreviewFrame(val index: Int, val bitmap: Bitmap)

@Composable
internal fun GifFramePreview(
    document: GifDocument,
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
    if (playing && !nativeFailed && remember(document) { document.canPlayContinuousVideo() }) {
        val fallback: (Int) -> Unit = { resumeFrom = it; nativeFailed = true }
        if (remember(document) { document.canPlaySourceDirectly() }) {
            NativeGifPlayback(clip, startFrame, document.loopCount, onFrame, onEnd, fallback, modifier)
        } else {
            ScheduledGifPlayback(clip, startFrame, document.loopCount, onFrame, onEnd, fallback, modifier)
        }
        return
    }
    LaunchedEffect(document, playing) {
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
                        val bitmap = cache.get(index) ?: run {
                            val decoder = reader ?: VideoGifSource.open(context, clip, batchFrames = playing)
                                .also { reader = it }
                            val source = decoder.frame(times[index], 480)
                            val result = if (neutral && source.width == width && source.height == height) source
                                else try {
                                    GifEncoder.renderFrame(source, document, maxEdge = 480,
                                        cancel = { job.ensureActive() })
                                } finally {
                                    source.recycle()
                                }
                            cache.put(index, result)
                            result
                        }
                        return PreviewFrame(index, bitmap)
                    }
                    if (!playing) {
                        snapshotFlow { requestedFrame }.conflate().collect { request ->
                            val frame = decode(request.coerceIn(times.indices))
                            withContext(Dispatchers.Main.immediate) {
                                if (requestedFrame == request) shown = frame
                            }
                        }
                    } else {
                        suspend fun present(frame: PreviewFrame) = withContext(Dispatchers.Main.immediate) {
                            shown = frame
                            reportFrame(frame.index)
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
                        withContext(Dispatchers.Main.immediate) { reportEnd(times.lastIndex) }
                    }
                } finally {
                    reader?.close()
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            shown = null
            reportError(error.message ?: "Cannot decode the selected frame")
        } catch (_: OutOfMemoryError) {
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

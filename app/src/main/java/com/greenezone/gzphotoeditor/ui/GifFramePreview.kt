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
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.GifEncoder
import com.greenezone.gzphotoeditor.editor.GifVideoMath
import com.greenezone.gzphotoeditor.editor.VideoGifSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

private data class PreviewFrame(val index: Int, val bitmap: Bitmap)

/** Forward, reverse and scrub use the same sample times and edits as the encoder. */
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
    LaunchedEffect(document, playing) {
        try {
            val times = GifVideoMath.frameTimesMs(clip)
            val delays = GifVideoMath.frameDelaysMs(clip)
            var due = 0L
            var lastShown = startFrame.coerceIn(times.indices)
            flow {
                VideoGifSource.open(context, clip).use { reader ->
                    suspend fun decode(index: Int): PreviewFrame {
                        val job = currentCoroutineContext()
                        job.ensureActive()
                        val bitmap = cache.get(index) ?: run {
                            val source = reader.frame(times[index], 480)
                            val rendered = try {
                                GifEncoder.renderFrame(source, document, maxEdge = 480, cancel = { job.ensureActive() })
                            } catch (error: Throwable) {
                                source.recycle()
                                throw error
                            }
                            if (rendered !== source) source.recycle()
                            cache.put(index, rendered)
                            rendered
                        }
                        return PreviewFrame(index, bitmap)
                    }
                    if (!playing) {
                        // Keep one decoder open while scrubbing. Conflate pending positions so a
                        // slow decode finishes and remains visible while the latest seek loads.
                        snapshotFlow { requestedFrame }.conflate().collect { request ->
                            emit(decode(request.coerceIn(times.indices)))
                        }
                        return@use
                    }
                    var index = requestedFrame.coerceIn(times.indices)
                    var repeats = 0
                    while (true) {
                        emit(decode(index))
                        if (index == times.lastIndex) {
                            if (document.loopCount != 0 && repeats >= document.loopCount) break
                            index = 0
                            repeats++
                        } else index++
                    }
                }
            }.buffer(2).flowOn(Dispatchers.IO).collect { frame ->
                delay((due - SystemClock.uptimeMillis()).coerceAtLeast(0))
                shown = frame
                lastShown = frame.index
                reportFrame(frame.index)
                if (playing) due = SystemClock.uptimeMillis() + delays[frame.index]
            }
            if (playing) {
                delay((due - SystemClock.uptimeMillis()).coerceAtLeast(0))
                reportEnd(lastShown)
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

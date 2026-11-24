package com.greenezone.gzphotoeditor.ui

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.ImagePipeline
import com.greenezone.gzphotoeditor.editor.Looks
import com.greenezone.gzphotoeditor.editor.PhotoGeometry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

internal data class PreviewStamp(val source: Bitmap, val edits: EditDocument)

internal data class CropPreview(val source: Bitmap, val edits: EditDocument, val bitmap: Bitmap)

internal class RegularPhotoPreviewState {
    var processed by mutableStateOf<Bitmap?>(null)
    var processedStamp by mutableStateOf<PreviewStamp?>(null)
    var beforeStamp by mutableStateOf<Pair<Bitmap, PhotoGeometry>?>(null)
    var cropPreview by mutableStateOf<CropPreview?>(null)
    var beforeImage by mutableStateOf<Bitmap?>(null)
    var filterThumbs by mutableStateOf<List<Bitmap>>(emptyList())
    var quickSource by mutableStateOf<Bitmap?>(null)
}

@Composable
internal fun RenderRegularPhotoPreview(
    state: RegularPhotoPreviewState,
    original: Bitmap?,
    previewEdits: EditDocument,
    cropTab: Boolean,
    liveGesture: Boolean,
    painting: Boolean,
    editingBlurBrush: Boolean,
    currentDocument: () -> EditDocument,
    onError: (String) -> Unit
) {
    val documentState = rememberUpdatedState(currentDocument)
    val errorHandler by rememberUpdatedState(onError)
    LaunchedEffect(original) {
        val input = original ?: run { state.quickSource = null; return@LaunchedEffect }
        val small = withContext(Dispatchers.Default) {
            val scale = minOf(1f, 660f / maxOf(input.width, input.height))
            if (scale >= 1f) input
            else Bitmap.createScaledBitmap(input,
                (input.width * scale).roundToInt().coerceAtLeast(1),
                (input.height * scale).roundToInt().coerceAtLeast(1), true)
        }
        state.quickSource = small
    }

    LaunchedEffect(original, state.quickSource, painting, editingBlurBrush) {
        val fast = state.quickSource ?: return@LaunchedEffect
        if (!painting || !editingBlurBrush) return@LaunchedEffect
        try {
            // Complete the current frame, then render the newest pointer position.
            snapshotFlow { documentState.value() }.conflate().collect { edits ->
                state.processed = withContext(Dispatchers.Default) {
                    val running = currentCoroutineContext()
                    ImagePipeline.render(fast, edits) { running.ensureActive() }
                }
                state.processedStamp = original?.let { PreviewStamp(it, edits) }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            errorHandler("Preview error: " + (error.message ?: "unexpected error"))
        }
    }

    LaunchedEffect(original, if (painting) null else previewEdits, liveGesture, state.quickSource, painting, cropTab) {
        if (!cropTab) state.cropPreview = null
        val input = original ?: run { state.processed = null; state.cropPreview = null; return@LaunchedEffect }
        if (painting) return@LaunchedEffect
        delay(if (liveGesture) 20L else 45L)
        try {
            val fast = state.quickSource
            if (fast != null && fast !== input && (!cropTab || liveGesture)) {
                val preview = withContext(Dispatchers.Default) {
                    val running = currentCoroutineContext()
                    ImagePipeline.render(fast, previewEdits) { running.ensureActive() }
                }
                state.processed = preview
                state.processedStamp = PreviewStamp(input, previewEdits)
            }
            if (!liveGesture || fast == null || fast === input) {
                if (fast != null && fast !== input && !cropTab) delay(140L)
                val quality = withContext(Dispatchers.Default) {
                    val running = currentCoroutineContext()
                    ImagePipeline.render(input, previewEdits) { running.ensureActive() }
                }
                state.processed = quality
                state.processedStamp = PreviewStamp(input, previewEdits)
                if (cropTab) state.cropPreview = CropPreview(input, previewEdits, quality)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            errorHandler("Preview error: " + (error.message ?: "unexpected error"))
        }
    }

    // Keep before/after geometry identical; compute outside the UI thread.
    LaunchedEffect(original, previewEdits.quarterTurns, previewEdits.mirrored,
        previewEdits.flipVertical, previewEdits.crop, previewEdits.cropFrame, previewEdits.cropCenterX,
        previewEdits.cropCenterY, previewEdits.straightenDegrees, previewEdits.cropFrameRatioLocked) {
        val input = original ?: run { state.beforeImage = null; return@LaunchedEffect }
        try {
            val result = withContext(Dispatchers.Default) {
                ImagePipeline.geometry(input, previewEdits)
            }
            state.beforeImage = result
            state.beforeStamp = input to PhotoGeometry.from(previewEdits)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            state.beforeImage = input
            state.beforeStamp = null
        }
    }

    LaunchedEffect(original) {
        val input = original ?: run { state.filterThumbs = emptyList(); return@LaunchedEffect }
        try {
            val created = withContext(Dispatchers.Default) {
                val working = currentCoroutineContext()
                val longest = maxOf(input.width, input.height)
                val sizeFactor = 128f / longest.coerceAtLeast(1)
                val width = (input.width * sizeFactor).roundToInt().coerceAtLeast(1)
                val height = (input.height * sizeFactor).roundToInt().coerceAtLeast(1)
                val thumbnail = Bitmap.createScaledBitmap(input, width, height, true)
                listOf(thumbnail) + Looks.all.map { look ->
                    working.ensureActive()
                    ImagePipeline.render(thumbnail, EditDocument(grade = look.grade)) {
                        working.ensureActive()
                    }
                }
            }
            state.filterThumbs = created
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            state.filterThumbs = emptyList()
        }
    }
}

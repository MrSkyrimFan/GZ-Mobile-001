package com.greenezone.gzphotoeditor.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.greenezone.gzphotoeditor.editor.BrushPoint
import com.greenezone.gzphotoeditor.editor.ProSelection
import com.greenezone.gzphotoeditor.editor.ProSelectionKind
import com.greenezone.gzphotoeditor.editor.ProToolMath
import kotlin.math.min

enum class ProPrecisionAction { SELECT, RETOUCH }

/**
 * Gesture-only precision overlay. The photograph is not altered until a mask or
 * retouch mark is committed to the editor document.
 */
@Composable
fun ProPrecisionOverlay(
    imageWidth: Int,
    imageHeight: Int,
    action: ProPrecisionAction,
    kind: ProSelectionKind,
    draft: ProSelection?,
    committed: ProSelection?,
    sourceMarker: BrushPoint?,
    onStartSelection: (BrushPoint) -> Unit,
    onMoveSelection: (BrushPoint) -> Unit,
    onEndSelection: () -> Unit,
    onRetouchTap: (BrushPoint) -> Unit
) {
    Canvas(Modifier.fillMaxSize().pointerInput(action, kind, imageWidth, imageHeight) {
        val scale = min(size.width.toFloat() / imageWidth,
            size.height.toFloat() / imageHeight)
        val w = imageWidth * scale
        val h = imageHeight * scale
        val l = (size.width - w) / 2f
        val t = (size.height - h) / 2f
        fun point(at: Offset) = BrushPoint(
            ((at.x - l) / w).coerceIn(0f, 1f),
            ((at.y - t) / h).coerceIn(0f, 1f)
        )
        if (action == ProPrecisionAction.SELECT) {
            detectDragGestures(
                onDragStart = { at -> onStartSelection(point(at)) },
                onDrag = { event, _ ->
                    event.consume()
                    onMoveSelection(point(event.position))
                },
                onDragEnd = onEndSelection,
                onDragCancel = onEndSelection
            )
        } else {
            detectTapGestures(onTap = { at -> onRetouchTap(point(at)) })
        }
    }) {
        val scale = min(size.width / imageWidth, size.height / imageHeight)
        val w = imageWidth * scale
        val h = imageHeight * scale
        val left = (size.width - w) / 2f
        val top = (size.height - h) / 2f
        fun outline(selection: ProSelection, color: Int) {
            if (!ProToolMath.valid(selection)) return
            val androidCanvas = drawContext.canvas.nativeCanvas
            val save = androidCanvas.save()
            try {
                androidCanvas.translate(left, top)
                androidCanvas.scale(w / imageWidth, h / imageHeight)
                androidCanvas.drawPath(
                    ProToolMath.path(selection, imageWidth, imageHeight),
                    Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        this.color = color
                        style = Paint.Style.STROKE
                        strokeWidth = 2f * imageWidth / w
                    }
                )
            } finally { androidCanvas.restoreToCount(save) }
        }
        committed?.let { outline(it, android.graphics.Color.CYAN) }
        draft?.let { outline(it, android.graphics.Color.YELLOW) }
        sourceMarker?.let {
            val center = Offset(left + it.x * w, top + it.y * h)
            drawCircle(Color.Yellow, radius = 8f, center = center,
                style = Stroke(2.5f))
            drawLine(Color.Yellow, Offset(center.x - 10f, center.y),
                Offset(center.x + 10f, center.y), strokeWidth = 2f)
            drawLine(Color.Yellow, Offset(center.x, center.y - 10f),
                Offset(center.x, center.y + 10f), strokeWidth = 2f)
        }
    }
}

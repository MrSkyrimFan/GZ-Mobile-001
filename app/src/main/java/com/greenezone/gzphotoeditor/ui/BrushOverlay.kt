package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.greenezone.gzphotoeditor.editor.BrushPoint
import com.greenezone.gzphotoeditor.editor.LocalMask
import kotlin.math.min

/** Draw actual normalized brush strokes over the fitted preview and capture freehand strokes. */
@Composable
fun BrushOverlay(
    bitmapWidth: Int,
    bitmapHeight: Int,
    mask: LocalMask,
    enabled: Boolean,
    onStart: (BrushPoint) -> Unit,
    onContinue: (BrushPoint) -> Unit,
    onFinish: () -> Unit
) {
    val latestStart by rememberUpdatedState(onStart)
    val latestContinue by rememberUpdatedState(onContinue)
    val latestFinish by rememberUpdatedState(onFinish)

    Canvas(Modifier.fillMaxSize().pointerInput(bitmapWidth, bitmapHeight, enabled) {
        var isPainting = false
        fun coordinates(position: Offset, onlyInside: Boolean): BrushPoint? {
            val width = size.width.toFloat()
            val height = size.height.toFloat()
            val factor = min(width / bitmapWidth, height / bitmapHeight)
            val displayedWidth = bitmapWidth * factor
            val displayedHeight = bitmapHeight * factor
            val left = (width - displayedWidth) / 2f
            val top = (height - displayedHeight) / 2f
            val x = (position.x - left) / displayedWidth
            val y = (position.y - top) / displayedHeight
            if (onlyInside && (x !in 0f..1f || y !in 0f..1f)) return null
            return BrushPoint(x.coerceIn(0f, 1f), y.coerceIn(0f, 1f))
        }
        detectDragGestures(
            onDragStart = { point ->
                isPainting = false
                if (enabled) {
                    coordinates(point, true)?.let {
                        isPainting = true
                        latestStart(it)
                    }
                }
            },
            onDragEnd = {
                if (isPainting) latestFinish()
                isPainting = false
            },
            onDragCancel = {
                if (isPainting) latestFinish()
                isPainting = false
            },
            onDrag = { change, _ ->
                if (isPainting) {
                    coordinates(change.position, false)?.let(latestContinue)
                    change.consume()
                }
            }
        )
    }) {
        val scale = min(size.width / bitmapWidth, size.height / bitmapHeight)
        val displayedWidth = bitmapWidth * scale
        val displayedHeight = bitmapHeight * scale
        val left = (size.width - displayedWidth) / 2f
        val top = (size.height - displayedHeight) / 2f
        val strokeWidth = (mask.radius * 2f * min(displayedWidth, displayedHeight)).coerceAtLeast(2f)
        for (stroke in mask.strokes) {
            val points = stroke.points
            if (points.isEmpty()) continue
            if (points.size == 1) {
                val point = points[0]
                drawCircle(
                    color = Color(0xFF46CDAF).copy(alpha = .30f),
                    radius = strokeWidth / 2f,
                    center = Offset(left + point.x * displayedWidth, top + point.y * displayedHeight)
                )
            } else {
                val path = Path()
                points.forEachIndexed { index, point ->
                    val x = left + point.x * displayedWidth
                    val y = top + point.y * displayedHeight
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, Color(0xFF46CDAF).copy(alpha = .27f),
                    style = Stroke(width = strokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.PathJoin.Round))
            }
        }
    }
}

package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
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
    onFinish: () -> Unit,
    onTap: () -> Unit = {},
    showGuides: Boolean = true,
    onCancel: () -> Unit,
    zoom: Float,
    pan: Offset,
    onTransform: (pinch: Float, translation: Offset, width: Float, height: Float) -> Unit
) {
    val latestStart by rememberUpdatedState(onStart)
    val latestContinue by rememberUpdatedState(onContinue)
    val latestFinish by rememberUpdatedState(onFinish)
    val latestTap by rememberUpdatedState(onTap)
    val latestCancel by rememberUpdatedState(onCancel)
    val latestZoom by rememberUpdatedState(zoom)
    val latestPan by rememberUpdatedState(pan)
    val latestTransform by rememberUpdatedState(onTransform)

    Canvas(
        Modifier.fillMaxSize().pointerInput(bitmapWidth, bitmapHeight, enabled) {
            if (!enabled) return@pointerInput
            fun coordinates(position: Offset, onlyInside: Boolean): BrushPoint? {
                val width = size.width.toFloat()
                val height = size.height.toFloat()
                val factor = min(width / bitmapWidth, height / bitmapHeight) * latestZoom
                val displayedWidth = bitmapWidth * factor
                val displayedHeight = bitmapHeight * factor
                val left = (width - displayedWidth) / 2f + latestPan.x
                val top = (height - displayedHeight) / 2f + latestPan.y
                val x = (position.x - left) / displayedWidth
                val y = (position.y - top) / displayedHeight
                if (onlyInside && (x !in 0f..1f || y !in 0f..1f)) return null
                return BrushPoint(x.coerceIn(0f, 1f), y.coerceIn(0f, 1f))
            }
            awaitEachGesture {
                val down = awaitFirstDown()
                val start = coordinates(down.position, true)
                var isPainting = false
                var navigated = false
                var traveled = false
                try {
                    do {
                        val event = awaitPointerEvent()
                        if (event.changes.any { it.isConsumed }) break
                        val pressed = event.changes.count { it.pressed }
                        val primary = event.changes.firstOrNull { it.id == down.id }
                        if (pressed >= 2) {
                            if (isPainting) latestCancel()
                            isPainting = false
                            navigated = true
                            latestTransform(event.calculateZoom(), event.calculatePan(),
                                size.width.toFloat(), size.height.toFloat())
                            event.changes.forEach { it.consume() }
                        } else if (!navigated && primary != null) {
                            if (primary.pressed) {
                                if ((primary.position - down.position).getDistance() >= viewConfiguration.touchSlop) {
                                    traveled = true
                                    if (!isPainting && start != null) {
                                        isPainting = true
                                        latestStart(start)
                                    }
                                }
                                if (isPainting) {
                                    coordinates(primary.position, false)?.let(latestContinue)
                                    primary.consume()
                                }
                            } else if (isPainting) {
                                coordinates(primary.position, false)?.let(latestContinue)
                                latestFinish()
                                isPainting = false
                                primary.consume()
                            } else if (!traveled) {
                                latestTap()
                                primary.consume()
                            }
                        }
                    } while (pressed > 0)
                } finally {
                    if (isPainting) latestCancel()
                }
            }
        }
    ) {
        if (!showGuides) return@Canvas
        val scale = min(size.width / bitmapWidth, size.height / bitmapHeight) * zoom
        val displayedWidth = bitmapWidth * scale
        val displayedHeight = bitmapHeight * scale
        val left = (size.width - displayedWidth) / 2f + pan.x
        val top = (size.height - displayedHeight) / 2f + pan.y
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
                        join = androidx.compose.ui.graphics.StrokeJoin.Round))
            }
        }
    }
}

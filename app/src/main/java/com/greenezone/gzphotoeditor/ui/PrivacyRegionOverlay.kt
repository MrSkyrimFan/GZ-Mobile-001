package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.greenezone.gzphotoeditor.editor.BrushPoint
import com.greenezone.gzphotoeditor.editor.PrivacyEdit
import com.greenezone.gzphotoeditor.editor.PrivacyProjection
import kotlin.math.min

@Composable
fun PrivacyRegionOverlay(
    imageWidth: Int,
    imageHeight: Int,
    mark: PrivacyEdit,
    projection: PrivacyProjection?,
    canMove: Boolean,
    showGuides: Boolean,
    onMove: (Float, Float) -> Unit,
    onFinished: () -> Unit,
    onTap: () -> Unit,
    onCanceled: () -> Unit = {},
    targetKey: Any? = null
) {
    val latestMove by rememberUpdatedState(onMove)
    val latestFinish by rememberUpdatedState(onFinished)
    val latestTap by rememberUpdatedState(onTap)
    val latestCancel by rememberUpdatedState(onCanceled)

    Canvas(
        Modifier.fillMaxSize()
            .pointerInput(imageWidth, imageHeight, canMove, targetKey) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val startSize = size
                    val factor = min(startSize.width.toFloat() / imageWidth, startSize.height.toFloat() / imageHeight)
                    val w = imageWidth * factor
                    val h = imageHeight * factor
                    val left = (startSize.width - w) / 2f
                    val top = (startSize.height - h) / 2f
                    val firstX = (down.position.x - left) / w
                    val firstY = (down.position.y - top) / h
                    val ownsRegion = canMove && firstX in 0f..1f && firstY in 0f..1f
                    down.consume()
                    var moved = false
                    var changed = false
                    var completed = false
                    try {
                        do {
                            val event = awaitPointerEvent()
                            val pointer = event.changes.singleOrNull() ?: return@awaitEachGesture
                            if (size != startSize || pointer.id != down.id || pointer.isConsumed)
                                return@awaitEachGesture
                            moved = moved || (pointer.position - down.position).getDistance() >
                                viewConfiguration.touchSlop
                            if (moved && ownsRegion) {
                                changed = true
                                latestMove(((pointer.position.x - left) / w).coerceIn(0f, 1f),
                                    ((pointer.position.y - top) / h).coerceIn(0f, 1f))
                            }
                            pointer.consume()
                        } while (pointer.pressed)
                        completed = true
                        if (changed) latestFinish() else if (!moved) latestTap()
                    } finally {
                        if (!completed && changed) latestCancel()
                    }
                }
            }
    ) {
        if (!showGuides) return@Canvas
        val factor = min(size.width / imageWidth, size.height / imageHeight)
        val width = imageWidth * factor
        val height = imageHeight * factor
        val left = (size.width - width) / 2f
        val top = (size.height - height) / 2f
        fun screenPoint(x: Float, y: Float): Offset {
            val point = projection?.toView(BrushPoint(x, y)) ?: BrushPoint(x, y)
            return Offset(left + point.x * width, top + point.y * height)
        }
        val x1 = (mark.centerX - mark.width / 2f).coerceIn(0f, 1f)
        val x2 = (mark.centerX + mark.width / 2f).coerceIn(0f, 1f)
        val y1 = (mark.centerY - mark.height / 2f).coerceIn(0f, 1f)
        val y2 = (mark.centerY + mark.height / 2f).coerceIn(0f, 1f)
        val corners = listOf(screenPoint(x1, y1), screenPoint(x2, y1),
            screenPoint(x2, y2), screenPoint(x1, y2))
        val outline = Path().apply {
            moveTo(corners.first().x, corners.first().y)
            for (corner in corners.drop(1)) lineTo(corner.x, corner.y)
            close()
        }
        clipRect(left = left, top = top, right = left + width, bottom = top + height) {
            drawPath(outline, color = Color(0xFF8FF5D1), style = Stroke(width = 2.5f))
            drawCircle(Color(0xFF8FF5D1), radius = 7f, center = screenPoint(mark.centerX, mark.centerY))
        }
    }
}

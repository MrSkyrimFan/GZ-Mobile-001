package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.greenezone.gzphotoeditor.editor.PrivacyEdit
import kotlin.math.min

@Composable
fun PrivacyRegionOverlay(
    imageWidth: Int,
    imageHeight: Int,
    mark: PrivacyEdit,
    canMove: Boolean,
    showGuides: Boolean,
    onMove: (Float, Float) -> Unit,
    onFinished: () -> Unit,
    onTap: () -> Unit
) {
    val latestMove by rememberUpdatedState(onMove)
    val latestFinish by rememberUpdatedState(onFinished)
    val latestTap by rememberUpdatedState(onTap)

    Canvas(
        Modifier.fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onTap = { latestTap() }) }
            .pointerInput(imageWidth, imageHeight, canMove) {
                var moving = false
                fun imagePoint(offset: Offset): Pair<Float, Float>? {
                    val factor = min(size.width.toFloat() / imageWidth, size.height.toFloat() / imageHeight)
                    val w = imageWidth * factor
                    val h = imageHeight * factor
                    val left = (size.width - w) / 2f
                    val top = (size.height - h) / 2f
                    val x = (offset.x - left) / w
                    val y = (offset.y - top) / h
                    if (x !in 0f..1f || y !in 0f..1f) return null
                    return x to y
                }
                detectDragGestures(
                    onDragStart = { moving = canMove && imagePoint(it) != null },
                    onDragEnd = { if (moving) latestFinish(); moving = false },
                    onDragCancel = { if (moving) latestFinish(); moving = false },
                    onDrag = { change, _ ->
                        if (moving) imagePoint(change.position)?.let {
                            latestMove(it.first, it.second)
                            change.consume()
                        }
                    }
                )
            }
    ) {
        if (!showGuides) return@Canvas
        val factor = min(size.width / imageWidth, size.height / imageHeight)
        val width = imageWidth * factor
        val height = imageHeight * factor
        val left = (size.width - width) / 2f
        val top = (size.height - height) / 2f
        val boxWidth = mark.width * width
        val boxHeight = mark.height * height
        val x = left + mark.centerX * width - boxWidth / 2f
        val y = top + mark.centerY * height - boxHeight / 2f
        drawRect(
            color = Color(0xFF8FF5D1),
            topLeft = Offset(x, y),
            size = Size(boxWidth, boxHeight),
            style = Stroke(width = 2.5f)
        )
        drawCircle(
            color = Color(0xFF8FF5D1),
            radius = 7f,
            center = Offset(left + mark.centerX * width, top + mark.centerY * height)
        )
    }
}

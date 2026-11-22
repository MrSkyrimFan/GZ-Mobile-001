package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.greenezone.gzphotoeditor.editor.BrushPoint
import com.greenezone.gzphotoeditor.editor.InkStroke
import com.greenezone.gzphotoeditor.editor.ProLayer
import com.greenezone.gzphotoeditor.editor.ProLayerKind
import kotlin.math.min

enum class ProCanvasAction { MOVE, BRUSH }

/**
 * Shared direct-manipulation input is expressed as normalized canvas coordinates.
 * Only the image preview surface receives gestures; the toolbars stay independent.
 */
@Composable
fun ProCanvasOverlay(
    canvasWidth: Int,
    canvasHeight: Int,
    selected: ProLayer,
    action: ProCanvasAction,
    activeStroke: InkStroke? = null,
    onBegin: (BrushPoint) -> Unit,
    onMove: (BrushPoint) -> Unit,
    onFinish: () -> Unit
) {
    val interactive = if (action == ProCanvasAction.BRUSH)
        selected.kind == ProLayerKind.DRAWING
        else selected.kind != ProLayerKind.DRAWING &&
            selected.kind != ProLayerKind.ADJUSTMENT

    Canvas(Modifier.fillMaxSize().pointerInput(action, selected.kind, selected.name,
        canvasWidth, canvasHeight, interactive) {
        if (!interactive) return@pointerInput
        val fit = min(size.width.toFloat() / canvasWidth, size.height.toFloat() / canvasHeight)
        val imageW = canvasWidth * fit
        val imageH = canvasHeight * fit
        val startX = (size.width - imageW) / 2f
        val startY = (size.height - imageH) / 2f
        fun normalize(pos: Offset): BrushPoint = BrushPoint(
            ((pos.x - startX) / imageW).coerceIn(0f, 1f),
            ((pos.y - startY) / imageH).coerceIn(0f, 1f)
        )
        var dragX = 0f
        var dragY = 0f
        var anchorX = selected.x
        var anchorY = selected.y
        detectDragGestures(
            onDragStart = { at ->
                dragX = 0f; dragY = 0f
                anchorX = selected.x; anchorY = selected.y
                onBegin(normalize(at))
            },
            onDrag = { change, amount ->
                change.consume()
                if (action == ProCanvasAction.MOVE) {
                    dragX += amount.x / imageW
                    dragY += amount.y / imageH
                    onMove(BrushPoint(
                        (anchorX + dragX).coerceIn(0f, 1f),
                        (anchorY + dragY).coerceIn(0f, 1f)))
                } else {
                    onMove(normalize(change.position))
                }
            },
            onDragEnd = onFinish,
            onDragCancel = onFinish
        )
    }) {
        val factor = min(size.width / canvasWidth, size.height / canvasHeight)
        val frameW = canvasWidth * factor
        val frameH = canvasHeight * factor
        val originX = (size.width - frameW) / 2f
        val originY = (size.height - frameH) / 2f
        if (selected.kind == ProLayerKind.DRAWING) {
            val points = activeStroke?.points ?: emptyList()
            if (points.isNotEmpty()) {
                val path = Path()
                points.forEachIndexed { i, point ->
                    val x = originX + point.x * frameW
                    val y = originY + point.y * frameH
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, Color(activeStroke?.color ?: 0xFFFFFFFF.toInt()),
                    style = Stroke(width = (activeStroke?.thickness ?: .01f) *
                        min(frameW, frameH)))
            }
        } else if (selected.kind != ProLayerKind.ADJUSTMENT) {
            val aspect = selected.aspect.coerceIn(.1f, 10f)
            val fitW = if (aspect > 1f) 1f else aspect
            val fitH = if (aspect > 1f) 1f / aspect else 1f
            val visualW = when (selected.kind) {
                ProLayerKind.TEXT -> selected.edits.creative.text.firstOrNull()?.width ?: .5f
                ProLayerKind.SHAPE -> selected.edits.creative.shapes.firstOrNull()?.width ?: .35f
                else -> fitW
            } * selected.scale
            val visualH = when (selected.kind) {
                ProLayerKind.TEXT -> selected.edits.creative.text.firstOrNull()?.height ?: .2f
                ProLayerKind.SHAPE -> selected.edits.creative.shapes.firstOrNull()?.height ?: .25f
                else -> fitH
            } * selected.scale
            val w = visualW.coerceIn(.02f, 1.6f) * frameW
            val h = visualH.coerceIn(.02f, 1.6f) * frameH
            val centerX = originX + selected.x * frameW
            val centerY = originY + selected.y * frameH
            drawRect(
                color = EditorMint,
                topLeft = Offset(centerX - w / 2f, centerY - h / 2f),
                size = Size(w, h),
                style = Stroke(2f)
            )
            drawCircle(EditorMint, radius = 5f,
                center = Offset(centerX, centerY))
        }
    }
}

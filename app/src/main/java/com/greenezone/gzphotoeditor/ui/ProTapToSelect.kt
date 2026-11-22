package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.greenezone.gzphotoeditor.editor.ProDocument
import com.greenezone.gzphotoeditor.editor.ProHitMath
import com.greenezone.gzphotoeditor.editor.ProLayerKind
import kotlin.math.min

/** Select the topmost visible object by tapping the canvas, not just by chip scrolling. */
@Composable
fun ProTapToSelect(
    document: ProDocument,
    selected: Int,
    canvasWidth: Int,
    canvasHeight: Int,
    onSelect: (Int) -> Unit
) {
    Canvas(Modifier.fillMaxSize().pointerInput(
        document, canvasWidth, canvasHeight
    ) {
        detectTapGestures(onTap = { point ->
            val scale = min(size.width / canvasWidth.toFloat(),
                size.height / canvasHeight.toFloat())
            val w = canvasWidth * scale
            val h = canvasHeight * scale
            val x = ((point.x - (size.width-w)/2f)/w).coerceIn(0f,1f)
            val y = ((point.y - (size.height-h)/2f)/h).coerceIn(0f,1f)
            ProHitMath.pick(document,x,y)?.let(onSelect)
        })
    }) {
        val layer = document.layers.getOrNull(selected)
        if (layer == null || !layer.visible) return@Canvas
        if (layer.kind == ProLayerKind.ADJUSTMENT ||
            layer.kind == ProLayerKind.DRAWING) return@Canvas
        val scale = min(size.width / canvasWidth, size.height / canvasHeight)
        val w = canvasWidth * scale
        val h = canvasHeight * scale
        val l = (size.width-w)/2f
        val t = (size.height-h)/2f
        val width = when (layer.kind) {
            ProLayerKind.IMAGE -> if (layer.aspect >= 1f)
                layer.scale else layer.aspect * layer.scale
            ProLayerKind.TEXT -> (layer.edits.creative.text.firstOrNull()?.width ?: .7f)*layer.scale
            ProLayerKind.SHAPE -> (layer.edits.creative.shapes.firstOrNull()?.width ?: .4f)*layer.scale
            else -> 0f
        }
        val height = when (layer.kind) {
            ProLayerKind.IMAGE -> if (layer.aspect >= 1f)
                layer.scale/layer.aspect else layer.scale
            ProLayerKind.TEXT -> (layer.edits.creative.text.firstOrNull()?.height ?: .2f)*layer.scale
            ProLayerKind.SHAPE -> (layer.edits.creative.shapes.firstOrNull()?.height ?: .3f)*layer.scale
            else -> 0f
        }
        val center = Offset(l+layer.x*w,t+layer.y*h)
        drawRect(EditorMint,
            topLeft = Offset(center.x-width*w/2f,center.y-height*h/2f),
            size = androidx.compose.ui.geometry.Size(width*w,height*h),
            style = Stroke(2.5f))
    }
}

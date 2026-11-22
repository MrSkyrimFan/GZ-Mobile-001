package com.greenezone.gzphotoeditor.editor

import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

/** Deterministic hit-testing for the topmost visible Pro object. */
object ProHitMath {
    fun pick(document: ProDocument, x: Float, y: Float,
        canvasWidth: Int = (1000 * document.aspect.ratio).toInt(), canvasHeight: Int = 1000
    ): Int? {
        if (!x.isFinite() || !y.isFinite() || x !in 0f..1f || y !in 0f..1f)
            return null
        for (index in document.layers.indices.reversed()) {
            val layer = document.layers[index]
            if (!layer.visible || layer.opacity <= 0f) continue
            val mask = layer.mask
            if (mask != null && !maskContains(mask, x, y)) continue
            val inBounds = when (layer.kind) {
                ProLayerKind.IMAGE ->
                    ProMath.canvasToImageNormalized(
                        layer, canvasWidth, canvasHeight, BrushPoint(x, y)) != null
                ProLayerKind.TEXT, ProLayerKind.SHAPE -> {
                    val text = layer.edits.creative.text.firstOrNull()
                    val shape = layer.edits.creative.shapes.firstOrNull()
                    val width = (text?.width ?: shape?.width ?: 0f) * layer.scale
                    val height = (text?.height ?: shape?.height ?: 0f) * layer.scale
                    val angle = -layer.rotationDegrees * kotlin.math.PI / 180.0
                    val dx = (x - layer.x) * canvasWidth
                    val dy = (y - layer.y) * canvasHeight
                    val u = dx * kotlin.math.cos(angle) - dy * kotlin.math.sin(angle)
                    val v = dx * kotlin.math.sin(angle) + dy * kotlin.math.cos(angle)
                    abs(u) <= width.coerceAtMost(1f) * canvasWidth / 2 &&
                        abs(v) <= height.coerceAtMost(1f) * canvasHeight / 2
                }
                ProLayerKind.DRAWING -> layer.edits.creative.strokes.any { stroke ->
                    val radius = (stroke.thickness / 2f).coerceAtLeast(.008f)
                    stroke.points.any { point ->
                        val dx = point.x - x; val dy = point.y - y
                        dx * dx + dy * dy <= radius * radius
                    } || stroke.points.zipWithNext().any { (a, b) ->
                        distanceToSegment(x, y, a.x, a.y, b.x, b.y) <= radius
                    }
                }
                ProLayerKind.ADJUSTMENT -> false
            }
            if (inBounds) return index
        }
        return null
    }

    private fun distanceToSegment(
        x: Float, y: Float, ax: Float, ay: Float, bx: Float, by: Float
    ): Float {
        val dx = bx - ax; val dy = by - ay
        val denom = dx * dx + dy * dy
        if (denom <= 0f) return sqrt((x-ax)*(x-ax)+(y-ay)*(y-ay))
        val f = (((x-ax)*dx+(y-ay)*dy)/denom).coerceIn(0f,1f)
        val px = ax+f*dx; val py = ay+f*dy
        return sqrt((x-px)*(x-px)+(y-py)*(y-py))
    }

    fun maskContains(mask: ProSelection, x: Float, y: Float): Boolean {
        if (!ProToolMath.valid(mask)) return false
        val inside = when (mask.kind) {
            ProSelectionKind.RECTANGLE -> {
                abs(x-mask.centerX) <= mask.width/2f &&
                    abs(y-mask.centerY) <= mask.height/2f
            }
            ProSelectionKind.ELLIPSE -> {
                val dx = (x-mask.centerX)/(mask.width/2f)
                val dy = (y-mask.centerY)/(mask.height/2f)
                dx*dx+dy*dy <= 1f
            }
            ProSelectionKind.LASSO -> {
                var yes = false
                val pts = mask.points
                for (i in pts.indices) {
                    val a = pts[i]
                    val b = pts[(i+1)%pts.size]
                    if ((a.y>y) != (b.y>y)) {
                        val edgeX = a.x+(b.x-a.x)*(y-a.y)/(b.y-a.y)
                        if (x < edgeX) yes = !yes
                    }
                }
                yes
            }
        }
        return if (mask.inverted) !inside else inside
    }
}

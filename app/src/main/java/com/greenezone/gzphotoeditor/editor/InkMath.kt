package com.greenezone.gzphotoeditor.editor

import kotlin.math.sqrt

object InkMath {
    /** Vector eraser; removes only points inside the eraser and preserves surviving segments. */
    fun erase(
        strokes: List<InkStroke>,
        x: Float,
        y: Float,
        radius: Float
    ): List<InkStroke> {
        if (strokes.isEmpty()) return strokes
        val squared = radius.coerceIn(.004f, .18f).let { it * it }
        val result = ArrayList<InkStroke>()
        for (stroke in strokes) {
            var run = ArrayList<BrushPoint>()
            for (p in stroke.points) {
                val dx = p.x - x
                val dy = p.y - y
                if (dx * dx + dy * dy < squared) {
                    if (run.isNotEmpty()) {
                        result.add(stroke.copy(points = run.toList()))
                        run = ArrayList()
                    }
                } else {
                    run.add(p)
                }
            }
            if (run.isNotEmpty()) result.add(stroke.copy(points = run))
        }
        return result
    }
}

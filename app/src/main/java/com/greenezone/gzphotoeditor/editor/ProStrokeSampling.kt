package com.greenezone.gzphotoeditor.editor

/** Bounded stroke capture preserves the start, sharp bends, and newest endpoint. */
internal object ProStrokeSampling {
    const val MAX_POINTS = 500

    fun append(
        points: List<BrushPoint>, point: BrushPoint, aspect: Float, maxPoints: Int = MAX_POINTS
    ): List<BrushPoint> {
        require(maxPoints in 3..MAX_POINTS)
        if (points.lastOrNull() == point) return points
        if (points.size < maxPoints) return points + point

        // Drop the interior point with the smallest visual contribution. Include
        // the previous endpoint as a candidate now that there is a newer one.
        var discard = 1
        var smallestImportance = Float.POSITIVE_INFINITY
        for (index in 1 until points.size) {
            val next = if (index == points.lastIndex) point else points[index + 1]
            val importance = removalImportance(points[index - 1], points[index], next, aspect)
            if (importance < smallestImportance) {
                smallestImportance = importance
                discard = index
            }
        }
        return points.toMutableList().apply {
            removeAt(discard)
            add(point)
        }
    }

    private fun removalImportance(
        start: BrushPoint, sample: BrushPoint, end: BrushPoint, aspect: Float
    ): Float {
        val dx = (end.x - start.x) * aspect
        val dy = end.y - start.y
        val lengthSquared = dx * dx + dy * dy
        val x = (sample.x - start.x) * aspect
        val y = sample.y - start.y
        if (lengthSquared == 0f) return x * x + y * y

        // Area prioritizes a broad bend over densely sampled, narrow zigzags.
        // A collinear out-and-back has no area, but its excursion still matters:
        // retain distance error when the sample lies beyond either neighbour.
        val projection = (x * dx + y * dy) / lengthSquared
        val area = kotlin.math.abs(dx * y - dy * x)
        if (projection in 0f..1f) return area
        val fraction = projection.coerceIn(0f, 1f)
        val offsetX = x - fraction * dx
        val offsetY = y - fraction * dy
        return maxOf(area, offsetX * offsetX + offsetY * offsetY)
    }
}

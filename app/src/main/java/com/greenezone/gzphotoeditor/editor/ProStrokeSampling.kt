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

        // Drop the interior point closest to its neighbours' segment. Include the
        // previous endpoint as a candidate now that there is a newer one.
        var discard = 1
        var smallestError = Float.POSITIVE_INFINITY
        for (index in 1 until points.size) {
            val next = if (index == points.lastIndex) point else points[index + 1]
            val error = distanceToSegmentSquared(points[index - 1], points[index], next, aspect)
            if (error < smallestError) {
                smallestError = error
                discard = index
            }
        }
        return points.toMutableList().apply {
            removeAt(discard)
            add(point)
        }
    }

    private fun distanceToSegmentSquared(
        start: BrushPoint, sample: BrushPoint, end: BrushPoint, aspect: Float
    ): Float {
        val dx = (end.x - start.x) * aspect
        val dy = end.y - start.y
        val lengthSquared = dx * dx + dy * dy
        val x = (sample.x - start.x) * aspect
        val y = sample.y - start.y
        val fraction = if (lengthSquared == 0f) 0f else ((x * dx + y * dy) / lengthSquared).coerceIn(0f, 1f)
        val offsetX = x - fraction * dx
        val offsetY = y - fraction * dy
        return offsetX * offsetX + offsetY * offsetY
    }
}

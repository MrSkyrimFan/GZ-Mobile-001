package com.greenezone.gzphotoeditor.editor

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object InkMath {
    data class EraseResult(val strokes: List<InkStroke>, val capacityExceeded: Boolean = false)

    /** Safe compatibility entry point; a result that cannot be saved leaves its input intact. */
    fun erase(
        strokes: List<InkStroke>,
        x: Float,
        y: Float,
        radius: Float
    ): List<InkStroke> = eraseBounded(strokes, x, y, radius).strokes

    /**
     * Cuts the displayed polyline, including the swept area between pointer samples.
     * Geometry and thickness are measured in units of the image's shorter edge.
     * No fragment is accepted unless the complete result fits the existing recipe.
     * A gesture owner must roll back its earlier samples if capacityExceeded is true.
     */
    fun eraseBounded(
        strokes: List<InkStroke>,
        x: Float,
        y: Float,
        radius: Float,
        aspect: Float = 1f,
        previous: BrushPoint? = null,
        maxStrokes: Int = CreativeRaster.MAX_INK
    ): EraseResult {
        require(maxStrokes in 0..CreativeRaster.MAX_INK)
        if (strokes.isEmpty()) return EraseResult(strokes)
        if (strokes.size > CreativeRaster.MAX_INK ||
            strokes.any { it.points.size > ProStrokeSampling.MAX_POINTS }) {
            return EraseResult(strokes, capacityExceeded = true)
        }
        if (!x.isFinite() || !y.isFinite() || !radius.isFinite() ||
            !aspect.isFinite() || aspect <= 0f) return EraseResult(strokes)
        val from = previous?.takeIf { it.x.isFinite() && it.y.isFinite() } ?: BrushPoint(x, y)
        val capsule = Capsule(from, BrushPoint(x, y), aspect.toDouble())
        val result = ArrayList<InkStroke>(minOf(strokes.size + 4, maxStrokes))
        var changed = false
        for (stroke in strokes) {
            // Round caps extend beyond a retained centerline endpoint. Include the
            // rendered half-width so they cannot paint back into the erased area.
            val reach = radius.coerceIn(.004f, .18f).toDouble() +
                stroke.thickness.coerceIn(.0015f, .16f).toDouble() / 2
            val pieces = clip(stroke, capsule, reach, maxStrokes - result.size)
                ?: return EraseResult(strokes, capacityExceeded = true)
            if (pieces.size != 1 || pieces[0] !== stroke) changed = true
            result.addAll(pieces)
        }
        // Intersections are computed in Double, then stored as Float. Repeating
        // an erase can round back to identical endpoints; preserve a true no-op.
        return EraseResult(if (!changed || result == strokes) strokes else result)
    }

    private fun clip(stroke: InkStroke, capsule: Capsule, radius: Double, available: Int): List<InkStroke>? {
        val points = stroke.points
        if (points.isEmpty()) return if (available > 0) listOf(stroke) else null
        if (points.size == 1) {
            if (capsule.contains(points[0], radius)) return emptyList()
            return if (available > 0) listOf(stroke) else null
        }
        val pieces = ArrayList<InkStroke>()
        var run = ArrayList<BrushPoint>()
        var changed = false
        fun append(point: BrushPoint) {
            if (run.lastOrNull() != point) run.add(point)
        }
        fun finish(): Boolean {
            if (run.isEmpty()) return true
            if (pieces.size >= available || run.size > ProStrokeSampling.MAX_POINTS) return false
            pieces.add(stroke.copy(points = run))
            run = ArrayList()
            return true
        }
        for (index in 1 until points.size) {
            val a = points[index - 1]
            val b = points[index]
            val cut = capsule.interval(a, b, radius)
            if (cut == null) {
                append(a)
                append(b)
            } else {
                changed = true
                if (cut.start > 0.0) {
                    append(a)
                    append(between(a, b, cut.start))
                }
                if (!finish()) return null
                if (cut.end < 1.0) {
                    append(between(a, b, cut.end))
                    append(b)
                }
            }
        }
        if (!changed) return if (available > 0) listOf(stroke) else null
        if (!finish()) return null
        return pieces
    }

    private fun between(a: BrushPoint, b: BrushPoint, fraction: Double): BrushPoint = when {
        fraction <= 0.0 -> a
        fraction >= 1.0 -> b
        else -> BrushPoint((a.x + (b.x.toDouble() - a.x) * fraction).toFloat(),
            (a.y + (b.y.toDouble() - a.y) * fraction).toFloat())
    }

    private data class Interval(val start: Double, val end: Double)

    private class Capsule(from: BrushPoint, to: BrushPoint, aspect: Double) {
        private val scaleX = max(1.0, aspect)
        private val scaleY = max(1.0, 1.0 / aspect)
        private val startX = from.x * scaleX
        private val startY = from.y * scaleY
        private val endX = to.x * scaleX
        private val endY = to.y * scaleY
        private val sweepX = endX - startX
        private val sweepY = endY - startY
        private val sweepSquared = sweepX * sweepX + sweepY * sweepY
        private val length = sqrt(sweepSquared)

        fun contains(point: BrushPoint, radius: Double): Boolean {
            val x = point.x * scaleX - startX
            val y = point.y * scaleY - startY
            val fraction = if (sweepSquared == 0.0) 0.0
                else ((x * sweepX + y * sweepY) / sweepSquared).coerceIn(0.0, 1.0)
            val dx = x - fraction * sweepX
            val dy = y - fraction * sweepY
            val interior = (radius - GEOMETRY_EPSILON).coerceAtLeast(0.0)
            return dx * dx + dy * dy < interior * interior
        }

        fun interval(a: BrushPoint, b: BrushPoint, radius: Double): Interval? {
            val ax = a.x * scaleX
            val ay = a.y * scaleY
            val bx = b.x * scaleX
            val by = b.y * scaleY
            if (max(ax, bx) < min(startX, endX) - radius ||
                min(ax, bx) > max(startX, endX) + radius ||
                max(ay, by) < min(startY, endY) - radius ||
                min(ay, by) > max(startY, endY) + radius) return null
            val dx = bx - ax
            val dy = by - ay
            val squared = dx * dx + dy * dy
            if (squared == 0.0) return if (contains(a, radius)) Interval(0.0, 1.0) else null

            // A capsule is the union of the endpoint disks and its oriented body.
            // Its intersection with a line segment is one interval (it is convex).
            val first = circle(ax, ay, dx, dy, squared, startX, startY, radius)
            if (length == 0.0) return first
            val last = circle(ax, ay, dx, dy, squared, endX, endY, radius)
            val ux = sweepX / length
            val uy = sweepY / length
            val along = slab((ax - startX) * ux + (ay - startY) * uy,
                dx * ux + dy * uy, 0.0, length)
            val across = slab(-(ax - startX) * uy + (ay - startY) * ux,
                -dx * uy + dy * ux, -radius, radius)
            val body = if (along != null && across != null) bounded(
                max(along.start, across.start), min(along.end, across.end)) else null
            var start = first?.start ?: Double.POSITIVE_INFINITY
            var end = first?.end ?: Double.NEGATIVE_INFINITY
            if (last != null) { start = min(start, last.start); end = max(end, last.end) }
            if (body != null) { start = min(start, body.start); end = max(end, body.end) }
            return bounded(start, end)
        }
    }

    private fun circle(
        ax: Double, ay: Double, dx: Double, dy: Double, squared: Double,
        cx: Double, cy: Double, radius: Double
    ): Interval? {
        val x = cx - ax
        val y = cy - ay
        val projection = (x * dx + y * dy) / squared
        val cross = x * dy - y * dx
        val distanceSquared = cross * cross / squared
        val interior = (radius - GEOMETRY_EPSILON).coerceAtLeast(0.0)
        // Touching the outer boundary alone is a no-op, including Float roundoff.
        if (distanceSquared >= interior * interior) return null
        val delta = sqrt(((radius * radius - distanceSquared) / squared).coerceAtLeast(0.0))
        return bounded(projection - delta, projection + delta)
    }

    private fun slab(position: Double, delta: Double, low: Double, high: Double): Interval? {
        if (abs(delta) < 1e-15) {
            return if (position > low + GEOMETRY_EPSILON && position < high - GEOMETRY_EPSILON)
                Interval(0.0, 1.0) else null
        }
        val first = (low - position) / delta
        val last = (high - position) / delta
        return bounded(min(first, last), max(first, last))
    }

    private fun bounded(start: Double, end: Double): Interval? {
        val low = max(0.0, start)
        val high = min(1.0, end)
        return if (high - low > 1e-12) Interval(low, high) else null
    }

    // Below a thousandth of a pixel even on a 4K short edge. Avoid phantom cuts
    // caused solely by converting an exactly tangent coordinate through Float.
    private const val GEOMETRY_EPSILON = 1e-7
}

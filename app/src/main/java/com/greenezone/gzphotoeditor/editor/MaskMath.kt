package com.greenezone.gzphotoeditor.editor

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** CPU-friendly radial, linear, and painted local masks. No runtime AI or external SDKs. */
object MaskMath {
    private const val GRID = 32
    private const val SEGMENT_LIMIT = 640

    data class Segment(val ax: Float, val ay: Float, val bx: Float, val by: Float)

    class BrushGrid private constructor(
        private val segments: List<Segment>,
        private val cells: Array<IntArray>,
        private val radius: Float,
        private val feather: Float
    ) {
        fun coverage(x: Int, y: Int, width: Int, height: Int): Float =
            coverageAt(x + .5f, y + .5f, width, height)

        fun coverageAt(x: Float, y: Float, width: Int, height: Int): Float {
            if (width <= 0 || height <= 0 || segments.isEmpty() ||
                x < 0f || y < 0f || x >= width || y >= height) return 0f
            val nx = x / width
            val ny = y / height
            val gx = (nx * GRID).toInt().coerceIn(0, GRID - 1)
            val gy = (ny * GRID).toInt().coerceIn(0, GRID - 1)
            val indices = cells[gy * GRID + gx]
            if (indices.isEmpty()) return 0f
            val shortSide = min(width, height).toFloat()
            val scaleX = width / shortSide
            val scaleY = height / shortSide
            val inner = radius * (1f - feather)
            var highest = 0f
            for (index in indices) {
                val segment = segments[index]
                val ax = (nx - segment.ax) * scaleX
                val ay = (ny - segment.ay) * scaleY
                val bx = (segment.bx - segment.ax) * scaleX
                val by = (segment.by - segment.ay) * scaleY
                val lengthSquared = bx * bx + by * by
                val t = if (lengthSquared < .00000001f) 0f else
                    ((ax * bx + ay * by) / lengthSquared).coerceIn(0f, 1f)
                val dx = ax - bx * t
                val dy = ay - by * t
                val dist = sqrt(dx * dx + dy * dy)
                if (dist >= radius) continue
                val value = 1f - smooth((dist - inner) / max(.00001f, radius - inner))
                if (value > highest) highest = value
                if (highest >= .9999f) return 1f
            }
            return highest
        }

        companion object {
            fun from(mask: LocalMask): BrushGrid {
                val segments = ArrayList<Segment>()
                for (stroke in mask.strokes.take(64)) {
                    val valid = stroke.points.take(SEGMENT_LIMIT + 1).filter {
                        it.x.isFinite() && it.y.isFinite() && it.x in 0f..1f && it.y in 0f..1f
                    }
                    if (valid.isEmpty()) continue
                    if (valid.size == 1) {
                        val point = valid[0]
                        segments.add(Segment(point.x, point.y, point.x, point.y))
                    } else {
                        for (i in 1 until valid.size) {
                            val a = valid[i - 1]
                            val b = valid[i]
                            segments.add(Segment(a.x, a.y, b.x, b.y))
                            if (segments.size >= SEGMENT_LIMIT) break
                        }
                    }
                    if (segments.size >= SEGMENT_LIMIT) break
                }
                val radius = mask.radius.coerceIn(.005f, .20f)
                val feather = mask.feather.coerceIn(.01f, 1f)
                val buckets = Array(GRID * GRID) { mutableListOf<Int>() }
                segments.forEachIndexed { index, segment ->
                    // Conservative normalized bounding region, covers pixel-aspect corrections.
                    val x0 = (floor((min(segment.ax, segment.bx) - radius) * GRID)).toInt().coerceIn(0, GRID - 1)
                    val x1 = (floor((max(segment.ax, segment.bx) + radius) * GRID)).toInt().coerceIn(0, GRID - 1)
                    val y0 = (floor((min(segment.ay, segment.by) - radius) * GRID)).toInt().coerceIn(0, GRID - 1)
                    val y1 = (floor((max(segment.ay, segment.by) + radius) * GRID)).toInt().coerceIn(0, GRID - 1)
                    for (row in y0..y1) for (column in x0..x1) {
                        buckets[row * GRID + column].add(index)
                    }
                }
                return BrushGrid(segments, Array(buckets.size) { buckets[it].toIntArray() }, radius, feather)
            }
        }
    }

    data class Prepared(
        val mask: LocalMask,
        val exposureFactor: Float,
        val cosTilt: Float,
        val sinTilt: Float,
        val brush: BrushGrid?
    )

    private fun prep(mask: LocalMask): Prepared {
        val radians = (mask.centerX - .5f) * 2.0 * PI / 3.0
        return Prepared(
            mask = mask,
            exposureFactor = 2.0.pow(mask.exposure.toDouble()).toFloat(),
            cosTilt = cos(radians).toFloat(),
            sinTilt = sin(radians).toFloat(),
            brush = if (mask.kind == MaskKind.BRUSH) BrushGrid.from(mask) else null
        )
    }

    fun prepare(masks: List<LocalMask>): List<Prepared> =
        masks.take(4).filter {
            (it.exposure != 0f || it.contrast != 0f || it.saturation != 0f) &&
                (it.kind != MaskKind.BRUSH || it.strokes.isNotEmpty())
        }.map(::prep)

    private fun smooth(value: Float): Float {
        val t = value.coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    /** Useful in deterministic unit tests and UI previews. */
    fun coverage(mask: LocalMask, x: Int, y: Int, width: Int, height: Int): Float =
        coveragePrepared(prep(mask), x, y, width, height)

    fun coverageForPrepared(prepared: Prepared, x: Int, y: Int, width: Int, height: Int): Float =
        coveragePrepared(prepared, x, y, width, height)

    fun prepareCoverage(mask: LocalMask): Prepared = prep(mask)

    private fun coveragePrepared(item: Prepared, x: Int, y: Int, width: Int, height: Int): Float {
        val mask = item.mask
        if (width <= 0 || height <= 0) return 0f
        val nx = (x + .5f) / width
        val ny = (y + .5f) / height
        return when (mask.kind) {
            MaskKind.RADIAL -> {
                val dx = nx - mask.centerX
                val dy = ny - mask.centerY
                val dist = sqrt(dx * dx + dy * dy)
                val radius = mask.radius.coerceIn(.02f, .8f)
                val inner = radius * (1f - mask.feather.coerceIn(.01f, 1f))
                1f - smooth((dist - inner) / max(.0001f, radius - inner))
            }
            MaskKind.LINEAR -> {
                val breadth = mask.radius.coerceIn(.02f, .8f)
                val projection = (ny - mask.centerY) * item.cosTilt +
                    (nx - .5f) * item.sinTilt
                1f - smooth((projection + breadth) / (breadth * 2f))
            }
            MaskKind.BRUSH -> item.brush?.coverage(x, y, width, height) ?: 0f
        }
    }

    fun adjust(argb: Int, masks: List<Prepared>, x: Int, y: Int, width: Int, height: Int): Int {
        if (masks.isEmpty()) return argb
        val a = argb and -0x1000000
        var r = ((argb ushr 16) and 255) / 255f
        var g = ((argb ushr 8) and 255) / 255f
        var b = (argb and 255) / 255f
        var used = false
        for (item in masks) {
            val mask = item.mask
            val weight = coveragePrepared(item, x, y, width, height)
            if (weight <= .0001f) continue
            used = true
            val exposure = 1f + (item.exposureFactor - 1f) * weight
            r *= exposure
            g *= exposure
            b *= exposure
            val contrast = (1f + mask.contrast * .55f * weight).coerceIn(.4f, 1.6f)
            r = (r - .5f) * contrast + .5f
            g = (g - .5f) * contrast + .5f
            b = (b - .5f) * contrast + .5f
            val luma = .2126f * r + .7152f * g + .0722f * b
            val saturation = (1f + mask.saturation * weight).coerceIn(0f, 2f)
            r = luma + (r - luma) * saturation
            g = luma + (g - luma) * saturation
            b = luma + (b - luma) * saturation
        }
        if (!used) return argb
        fun toByte(f: Float) = (f.coerceIn(0f, 1f) * 255f + .5f).toInt()
        return a or (toByte(r) shl 16) or (toByte(g) shl 8) or toByte(b)
    }
}

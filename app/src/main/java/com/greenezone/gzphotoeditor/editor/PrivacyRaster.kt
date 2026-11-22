package com.greenezone.gzphotoeditor.editor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.floor
import kotlin.math.roundToInt

object PrivacyRaster {
    const val MAX_EDITS = 12

    fun apply(
        source: Bitmap,
        edits: List<PrivacyEdit>,
        cancelCheck: (() -> Unit)? = null
    ): Bitmap {
        if (edits.isEmpty()) return source
        val output = source.copy(Bitmap.Config.ARGB_8888, true)
            ?: throw IllegalStateException("Could not allocate privacy output")
        try {
            val bounded = edits.take(MAX_EDITS)
            bounded.filter { it.kind == PrivacyKind.BLUR || it.kind == PrivacyKind.PIXELATE ||
                it.kind == PrivacyKind.BLUR_BRUSH }.forEach {
                if (it.kind == PrivacyKind.BLUR_BRUSH) {
                    applyBlurBrush(output, it, cancelCheck)
                } else {
                    applyFiltered(output, it, cancelCheck)
                }
            }
            bounded.filter { it.kind == PrivacyKind.BLACK_BAR ||
                it.kind == PrivacyKind.COLOR_COVER || it.kind == PrivacyKind.BLACK_BRUSH }.forEach {
                applyCover(output, it, cancelCheck)
            }
            return output
        } catch (error: Throwable) {
            output.recycle()
            throw error
        }
    }

    private fun applyCover(
        output: Bitmap,
        mark: PrivacyEdit,
        cancelCheck: (() -> Unit)?
    ) {
        val bounds = PrivacyMath.bounds(mark, output.width, output.height)
        if (bounds.empty) return
        val color = if (mark.kind == PrivacyKind.COLOR_COVER) {
            mark.color or -0x1000000
        } else Color.BLACK
        if (!mark.kind.usesBrush) {
            Canvas(output).drawRect(
                bounds.left.toFloat(), bounds.top.toFloat(),
                bounds.right.toFloat(), bounds.bottom.toFloat(),
                Paint().apply { this.color = color; isAntiAlias = false }
            )
            return
        }
        val mask = brushMask(mark)
        val prepared = MaskMath.prepareCoverage(mask)
        val line = IntArray(bounds.width)
        for (y in bounds.top until bounds.bottom) {
            if (y % 12 == 0) cancelCheck?.invoke()
            output.getPixels(line, 0, bounds.width, bounds.left, y, bounds.width, 1)
            for (x in bounds.left until bounds.right) {
                val w = MaskMath.coverageForPrepared(prepared, x, y, output.width, output.height)
                if (w > 0f) line[x - bounds.left] = if (w >= .999f) color
                    else blend(line[x - bounds.left], color, w)
            }
            output.setPixels(line, 0, bounds.width, bounds.left, y, bounds.width, 1)
        }
    }

    private fun applyFiltered(
        output: Bitmap,
        mark: PrivacyEdit,
        cancelCheck: (() -> Unit)?
    ) {
        val bounds = PrivacyMath.bounds(mark, output.width, output.height)
        if (bounds.empty || bounds.width < 2 || bounds.height < 2) return
        val pixelate = mark.kind == PrivacyKind.PIXELATE
        val factor = if (pixelate) {
            (2 + mark.strength.coerceIn(0f, 1f) * 65f).roundToInt()
        } else {
            (2 + mark.strength.coerceIn(0f, 1f) * 44f).roundToInt()
        }.coerceAtLeast(2)
        var crop: Bitmap? = null
        var sampled: Bitmap? = null
        try {
            crop = Bitmap.createBitmap(
                output, bounds.left, bounds.top, bounds.width, bounds.height
            )
            sampled = Bitmap.createScaledBitmap(
                crop,
                (bounds.width / factor).coerceAtLeast(1),
                (bounds.height / factor).coerceAtLeast(1),
                !pixelate
            )
            val sw = sampled.width
            val sh = sampled.height
            val pixels = IntArray(sw * sh)
            sampled.getPixels(pixels, 0, sw, 0, 0, sw, sh)
            val line = IntArray(bounds.width)
            for (y in bounds.top until bounds.bottom) {
                if (y % 12 == 0) cancelCheck?.invoke()
                output.getPixels(line, 0, bounds.width, bounds.left, y, bounds.width, 1)
                for (x in bounds.left until bounds.right) {
                    val coverage = PrivacyMath.rectCoverage(mark, x, y, output.width, output.height)
                    if (coverage <= .0001f) continue
                    val nx = ((x - bounds.left + .5f) / bounds.width * sw - .5f)
                        .coerceIn(0f, (sw - 1).toFloat())
                    val ny = ((y - bounds.top + .5f) / bounds.height * sh - .5f)
                        .coerceIn(0f, (sh - 1).toFloat())
                    val target = if (pixelate) {
                        pixels[ny.roundToInt().coerceIn(0, sh - 1) * sw +
                            nx.roundToInt().coerceIn(0, sw - 1)]
                    } else bilinear(pixels, sw, sh, nx, ny)
                    line[x - bounds.left] = if (coverage >= .999f) target
                        else blend(line[x - bounds.left], target, coverage)
                }
                output.setPixels(line, 0, bounds.width, bounds.left, y, bounds.width, 1)
            }
        } finally {
            if (sampled != null && sampled !== output && !sampled.isRecycled) sampled.recycle()
            if (crop != null && crop !== sampled && crop !== output && !crop.isRecycled) crop.recycle()
        }
    }

    private fun applyBlurBrush(output: Bitmap, mark: PrivacyEdit, cancelCheck: (() -> Unit)?) {
        val bounds = PrivacyMath.bounds(mark, output.width, output.height)
        val strength = mark.strength.coerceIn(0f, 1f)
        if (bounds.empty || strength <= 0f) return
        val mask = MaskMath.prepareCoverage(brushMask(mark))
        val spread = minOf(output.width, output.height) * .032f * strength
        val radius = spread.roundToInt().coerceAtLeast(1)
        val diameter = radius * 2 + 1
        val sampleLeft = (bounds.left - radius).coerceAtLeast(0)
        val sampleRight = (bounds.right + radius).coerceAtMost(output.width)
        val samples = IntArray(sampleRight - sampleLeft)
        val rows = Array(diameter) { IntArray(bounds.width) }
        val columns = Array(bounds.width) { ChannelSum() }
        val original = IntArray(bounds.width)

        fun readBlurredRow(sourceY: Int, row: IntArray) {
            val y = sourceY.coerceIn(0, output.height - 1)
            output.getPixels(samples, 0, samples.size, sampleLeft, y, samples.size, 1)
            for (i in samples.indices) samples[i] = premultiply(samples[i])
            fun sample(x: Int) = samples[(x - sampleLeft).coerceIn(0, samples.lastIndex)]
            val horizontal = ChannelSum()
            for (x in bounds.left - radius..bounds.left + radius) horizontal.add(sample(x), 1)
            for (i in row.indices) {
                row[i] = horizontal.average(diameter)
                val x = bounds.left + i
                horizontal.add(sample(x - radius), -1)
                horizontal.add(sample(x + radius + 1), 1)
            }
        }

        // Each source row is read before this rolling window writes back to it.
        // The halo uses original neighboring pixels even beyond the brush boundary.
        for (index in rows.indices) {
            cancelCheck?.invoke()
            val row = rows[index]
            readBlurredRow(bounds.top - radius + index, row)
            for (i in row.indices) columns[i].add(row[i], 1)
        }
        val opacity = spread.coerceAtMost(1f)
        for (y in bounds.top until bounds.bottom) {
            cancelCheck?.invoke()
            output.getPixels(original, 0, bounds.width, bounds.left, y, bounds.width, 1)
            for (i in original.indices) {
                val weight = MaskMath.coverageForPrepared(mask, bounds.left + i, y,
                    output.width, output.height) * opacity
                if (weight > 0f) {
                    val blurred = columns[i].average(diameter)
                    original[i] = unpremultiply(blend(premultiply(original[i]), blurred, weight))
                }
            }
            if (y + 1 < bounds.bottom) {
                val oldest = rows[(y - bounds.top) % diameter]
                for (i in oldest.indices) columns[i].add(oldest[i], -1)
                readBlurredRow(y + radius + 1, oldest)
                for (i in oldest.indices) columns[i].add(oldest[i], 1)
            }
            output.setPixels(original, 0, bounds.width, bounds.left, y, bounds.width, 1)
        }
    }

    private class ChannelSum {
        private var alpha = 0
        private var red = 0
        private var green = 0
        private var blue = 0

        fun add(color: Int, sign: Int) {
            alpha += Color.alpha(color) * sign
            red += Color.red(color) * sign
            green += Color.green(color) * sign
            blue += Color.blue(color) * sign
        }

        fun average(count: Int): Int {
            val half = count / 2
            return Color.argb((alpha + half) / count, (red + half) / count,
                (green + half) / count, (blue + half) / count)
        }
    }

    private fun premultiply(color: Int): Int {
        val alpha = Color.alpha(color)
        if (alpha == 255) return color
        return Color.argb(alpha, (Color.red(color) * alpha + 127) / 255,
            (Color.green(color) * alpha + 127) / 255, (Color.blue(color) * alpha + 127) / 255)
    }

    private fun unpremultiply(color: Int): Int {
        val alpha = Color.alpha(color)
        if (alpha == 255 || alpha == 0) return color
        fun straight(channel: Int) = ((channel * 255 + alpha / 2) / alpha).coerceAtMost(255)
        return Color.argb(alpha, straight(Color.red(color)), straight(Color.green(color)),
            straight(Color.blue(color)))
    }

    private fun brushMask(mark: PrivacyEdit) = LocalMask(
        kind = MaskKind.BRUSH,
        radius = mark.brushSize.coerceIn(.005f, .20f),
        feather = if (mark.kind == PrivacyKind.BLACK_BRUSH) .01f
            else mark.feather.coerceIn(.01f, 1f),
        strokes = mark.strokes
    )

    private fun bilinear(pixels: IntArray, width: Int, height: Int, x: Float, y: Float): Int {
        val x0 = floor(x).toInt().coerceIn(0, width - 1)
        val y0 = floor(y).toInt().coerceIn(0, height - 1)
        val x1 = (x0 + 1).coerceAtMost(width - 1)
        val y1 = (y0 + 1).coerceAtMost(height - 1)
        val wx = (x - x0).coerceIn(0f, 1f)
        val wy = (y - y0).coerceIn(0f, 1f)
        val c00 = pixels[y0 * width + x0]
        val c10 = pixels[y0 * width + x1]
        val c01 = pixels[y1 * width + x0]
        val c11 = pixels[y1 * width + x1]
        fun channel(shift: Int): Int {
            val v0 = ((c00 ushr shift) and 255) * (1f - wx) +
                ((c10 ushr shift) and 255) * wx
            val v1 = ((c01 ushr shift) and 255) * (1f - wx) +
                ((c11 ushr shift) and 255) * wx
            return (v0 * (1f - wy) + v1 * wy + .5f).toInt().coerceIn(0, 255)
        }
        return (channel(24) shl 24) or (channel(16) shl 16) or
            (channel(8) shl 8) or channel(0)
    }

    private fun blend(source: Int, filtered: Int, opacity: Float): Int {
        val w = opacity.coerceIn(0f, 1f)
        if (w <= 0f) return source
        if (w >= 1f) return filtered
        fun channel(shift: Int): Int {
            val a = (source ushr shift) and 255
            val b = (filtered ushr shift) and 255
            return (a * (1f - w) + b * w + .5f).toInt().coerceIn(0, 255)
        }
        return (channel(24) shl 24) or (channel(16) shl 16) or
            (channel(8) shl 8) or channel(0)
    }
}

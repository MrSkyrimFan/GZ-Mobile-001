package com.greenezone.gzphotoeditor.editor

import android.graphics.Bitmap
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class AutoToneSuggestion(
    val exposure: Float,
    val contrast: Float,
    val highlights: Float,
    val shadows: Float
)

data class WhiteBalanceSuggestion(val temperature: Float, val tint: Float)

object AutoEnhanceMath {
    /**
     * Bounded, explainable percentile-based auto tone. Deliberately conservative;
     * does not try to "recover" information absent from blown-out pixels.
     */
    fun autoTone(histogram: IntArray): AutoToneSuggestion {
        if (histogram.size < 16 || histogram.sum() <= 0) {
            return AutoToneSuggestion(0f, 0f, 0f, 0f)
        }
        val total = histogram.sum()
        fun percentile(fraction: Float): Float {
            val threshold = max(1, (total * fraction).toInt())
            var seen = 0
            for (i in histogram.indices) {
                seen += histogram[i]
                if (seen >= threshold) return i.toFloat() / (histogram.size - 1)
            }
            return 1f
        }
        val low = percentile(.03f)
        val mid = percentile(.50f)
        val high = percentile(.97f)
        if (high - low < .045f) return AutoToneSuggestion(0f, 0f, 0f, 0f)
        val factor = (.50f / mid.coerceAtLeast(.06f)).coerceIn(.75f, 1.35f)
        val exposure = (ln(factor.toDouble()) / ln(2.0)).toFloat().coerceIn(-.45f, .45f)
        val range = (high - low).coerceAtLeast(.06f)
        val contrast = ((.78f / range - 1f) / .68f).coerceIn(-.28f, .35f)
        val shadows = if (low < .07f) .11f else 0f
        val highlights = if (high > .94f) -.13f else 0f
        return AutoToneSuggestion(exposure, contrast, highlights, shadows)
    }

    /** Gray-world estimate from near-neutral midtone samples, capped to avoid overcorrecting. */
    fun whiteBalance(image: Bitmap): WhiteBalanceSuggestion {
        val total = image.width.toLong() * image.height
        val step = max(1, sqrt(total / 12_000.0).toInt())
        var n = 0
        var red = 0.0
        var green = 0.0
        var blue = 0.0
        for (y in 0 until image.height step step) for (x in 0 until image.width step step) {
            val p = image.getPixel(x, y)
            val r = ((p ushr 16) and 255) / 255f
            val g = ((p ushr 8) and 255) / 255f
            val b = (p and 255) / 255f
            val luminance = .2126f * r + .7152f * g + .0722f * b
            if (luminance < .16f || luminance > .88f || maxOf(r, g, b) - minOf(r, g, b) > .16f) continue
            red += r; green += g; blue += b; n++
        }
        if (n < 60) return WhiteBalanceSuggestion(0f, 0f)
        val r = red / n
        val g = green / n
        val b = blue / n
        return WhiteBalanceSuggestion(
            temperature = ((b - r) * 2.0).toFloat().coerceIn(-.32f, .32f),
            tint = (((r + b) / 2.0 - g) * 3.0).toFloat().coerceIn(-.32f, .32f)
        )
    }
}

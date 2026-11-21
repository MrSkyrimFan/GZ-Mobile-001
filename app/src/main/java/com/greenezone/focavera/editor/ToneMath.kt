package com.greenezone.focavera.editor

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Deterministic sRGB operations shared by the scaled preview and full-resolution export. */
object ToneMath {
    private val neutral = Grade()
    private fun clip(x: Float): Float = x.coerceIn(0f, 1f)
    private fun smooth(a: Float, b: Float, v: Float): Float {
        val t = ((v - a) / (b - a)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }
    private fun luma(r: Float, g: Float, b: Float): Float =
        .2126f * r + .7152f * g + .0722f * b

    /** Precompute frame-wide values once, never once per pixel. */
    class Prepared internal constructor(
        val grade: Grade,
        val isNeutral: Boolean,
        val exposureFactor: Float,
        val gammaExponent: Double,
        val contrastFactor: Float,
        val hasMixer: Boolean,
        val hasColorGrade: Boolean
    )

    fun prepare(grade: Grade): Prepared = Prepared(
        grade = grade,
        isNeutral = grade == neutral,
        exposureFactor = 2.0.pow(grade.exposure.toDouble()).toFloat(),
        gammaExponent = 2.0.pow((-grade.gamma * .6f).toDouble()),
        contrastFactor = (1f + grade.contrast * .68f).coerceIn(.20f, 1.8f),
        hasMixer = grade.colorMix.any { it.hue != 0f || it.saturation != 0f || it.luminance != 0f },
        hasColorGrade = grade.shadowRed != 0f || grade.shadowGreen != 0f || grade.shadowBlue != 0f ||
            grade.midRed != 0f || grade.midGreen != 0f || grade.midBlue != 0f ||
            grade.highlightRed != 0f || grade.highlightGreen != 0f || grade.highlightBlue != 0f
    )

    /** localDelta is current luminance minus a four-neighbor average, in [-1,+1]. */
    fun adjust(
        argb: Int, grade: Grade, x: Int, y: Int, width: Int, height: Int, localDelta: Float = 0f
    ): Int = adjustPrepared(argb, prepare(grade), x, y, width, height, localDelta)

    fun adjustPrepared(
        argb: Int, prepared: Prepared, x: Int, y: Int, width: Int, height: Int, localDelta: Float = 0f
    ): Int {
        if (prepared.isNeutral) return argb
        val grade = prepared.grade
        val a = (argb ushr 24) and 255
        var r = ((argb ushr 16) and 255) / 255f
        var g = ((argb ushr 8) and 255) / 255f
        var b = (argb and 255) / 255f

        r *= prepared.exposureFactor
        g *= prepared.exposureFactor
        b *= prepared.exposureFactor

        val lum = luma(r, g, b)
        val shadow = 1f - smooth(.10f, .57f, lum)
        val highlight = smooth(.43f, .94f, lum)
        val deepBlack = 1f - smooth(.02f, .34f, lum)
        val nearWhite = smooth(.69f, 1.09f, lum)
        val shift = grade.shadows * shadow * .27f +
            grade.highlights * highlight * .27f +
            grade.blacks * deepBlack * .14f +
            grade.whites * nearWhite * .16f
        r += shift
        g += shift
        b += shift

        r = (r - .5f) * prepared.contrastFactor + .5f
        g = (g - .5f) * prepared.contrastFactor + .5f
        b = (b - .5f) * prepared.contrastFactor + .5f

        r = clip(r).toDouble().pow(prepared.gammaExponent).toFloat()
        g = clip(g).toDouble().pow(prepared.gammaExponent).toFloat()
        b = clip(b).toDouble().pow(prepared.gammaExponent).toFloat()

        r += grade.temperature * .12f
        b -= grade.temperature * .12f
        g -= grade.tint * .08f
        r += grade.tint * .04f
        b += grade.tint * .04f

        if (grade.hue != 0f) {
            val yy = .299f * r + .587f * g + .114f * b
            val ii = .596f * r - .274f * g - .322f * b
            val qq = .211f * r - .523f * g + .312f * b
            val angle = grade.hue * PI
            val rotatedI = (ii * cos(angle) - qq * sin(angle)).toFloat()
            val rotatedQ = (ii * sin(angle) + qq * cos(angle)).toFloat()
            r = yy + .956f * rotatedI + .621f * rotatedQ
            g = yy - .272f * rotatedI - .647f * rotatedQ
            b = yy - 1.106f * rotatedI + 1.703f * rotatedQ
        }

        val baseLum = luma(r, g, b)
        val minChannel = minOf(r, g, b)
        val maxChannel = maxOf(r, g, b)
        val existingSat = (maxChannel - minChannel).coerceIn(0f, 1f)
        val sat = (1f + grade.saturation + grade.vibrance * (1f - existingSat) * .75f)
            .coerceIn(0f, 2.8f)
        r = baseLum + (r - baseLum) * sat
        g = baseLum + (g - baseLum) * sat
        b = baseLum + (b - baseLum) * sat

        r += grade.red * .12f
        g += grade.green * .12f
        b += grade.blue * .12f

        if (prepared.hasColorGrade) {
            val level = clip(luma(r, g, b))
            val sw = 1f - smooth(.12f, .52f, level)
            val mw = smooth(.10f, .44f, level) * (1f - smooth(.56f, .91f, level))
            val hw = smooth(.48f, .88f, level)
            r += .16f * (grade.shadowRed * sw + grade.midRed * mw + grade.highlightRed * hw)
            g += .16f * (grade.shadowGreen * sw + grade.midGreen * mw + grade.highlightGreen * hw)
            b += .16f * (grade.shadowBlue * sw + grade.midBlue * mw + grade.highlightBlue * hw)
        }

        // Weighted 8-band HSV correction. Adjacent hues blend smoothly, no abrupt color boundaries.
        if (prepared.hasMixer) {
            val cr = clip(r)
            val cg = clip(g)
            val cb = clip(b)
            val maximum = maxOf(cr, cg, cb)
            val minimum = minOf(cr, cg, cb)
            val chroma = maximum - minimum
            if (chroma > .001f && maximum > 0f) {
                var hue = when (maximum) {
                    cr -> ((cg - cb) / chroma) % 6f
                    cg -> (cb - cr) / chroma + 2f
                    else -> (cr - cg) / chroma + 4f
                } * 60f
                if (hue < 0f) hue += 360f
                var hueShift = 0f
                var saturationDelta = 0f
                var luminanceDelta = 0f
                for (i in ColorBand.entries.indices) {
                    val distance = abs(hue - ColorBand.entries[i].centerHue)
                    val wrapped = minOf(distance, 360f - distance)
                    if (wrapped >= 67f) continue
                    val proximity = 1f - wrapped / 67f
                    val weight = proximity * proximity * (3f - 2f * proximity)
                    val mix = grade.colorMix[i]
                    hueShift += weight * mix.hue * 30f
                    saturationDelta += weight * mix.saturation
                    luminanceDelta += weight * mix.luminance
                }
                if (hueShift != 0f || saturationDelta != 0f || luminanceDelta != 0f) {
                    hue = ((hue + hueShift) % 360f + 360f) % 360f
                    val updatedSat = ((chroma / maximum) * (1f + saturationDelta)).coerceIn(0f, 1f)
                    val updatedValue = (maximum + luminanceDelta * .23f).coerceIn(0f, 1f)
                    val c = updatedSat * updatedValue
                    val v = hue / 60f
                    val xval = c * (1f - abs(v % 2f - 1f))
                    val m = updatedValue - c
                    when (v.toInt()) {
                        0 -> { r = c + m; g = xval + m; b = m }
                        1 -> { r = xval + m; g = c + m; b = m }
                        2 -> { r = m; g = c + m; b = xval + m }
                        3 -> { r = m; g = xval + m; b = c + m }
                        4 -> { r = xval + m; g = m; b = c + m }
                        else -> { r = c + m; g = m; b = xval + m }
                    }
                }
            }
        }

        val detail = (grade.clarity * .48f + grade.sharpen * .9f) * localDelta
        r += detail
        g += detail
        b += detail

        val fade = grade.fade.coerceIn(0f, 1f) * .19f
        r = r * (1f - fade) + .53f * fade
        g = g * (1f - fade) + .53f * fade
        b = b * (1f - fade) + .53f * fade

        if (grade.vignette > 0f && width > 0 && height > 0) {
            val nx = ((x + .5f) / width - .5f) * 2f
            val ny = ((y + .5f) / height - .5f) * 2f
            val radius = sqrt(nx * nx + ny * ny)
            val shade = 1f - grade.vignette * .58f * smooth(.32f, 1.39f, radius)
            r *= shade
            g *= shade
            b *= shade
        }

        if (grade.grain > 0f) {
            val seed = (x * 374761393) xor (y * 668265263) xor 0x5BD1E995
            val n = ((seed xor (seed ushr 13)) * 1274126177).ushr(16) and 255
            val noise = (n / 255f - .5f) * grade.grain * .075f
            r += noise
            g += noise
            b += noise
        }
        return (a shl 24) or
            ((clip(r) * 255f + .5f).toInt() shl 16) or
            ((clip(g) * 255f + .5f).toInt() shl 8) or
            (clip(b) * 255f + .5f).toInt()
    }
}

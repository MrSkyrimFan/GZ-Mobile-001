package com.greenezone.chromavale.editor

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Deterministic, device-independent sRGB adjustment mathematics for preview and export. */
object ToneMath {
    private val neutral = Grade()
    private fun clip(x: Float): Float = x.coerceIn(0f, 1f)
    private fun smooth(a: Float, b: Float, v: Float): Float {
        val t = ((v - a) / (b - a)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }
    private fun luma(r: Float, g: Float, b: Float): Float =
        .2126f * r + .7152f * g + .0722f * b

    /**
     * localDelta is the signed difference between current and neighborhood luminance
     * in [-1,+1]. For most pixels it is zero, keeping the fast path inexpensive.
     */
    fun adjust(argb: Int, grade: Grade, x: Int, y: Int, width: Int, height: Int, localDelta: Float = 0f): Int {
        if (grade == neutral) return argb
        val a = (argb ushr 24) and 255
        var r = ((argb ushr 16) and 255) / 255f
        var g = ((argb ushr 8) and 255) / 255f
        var b = (argb and 255) / 255f

        val exposure = 2.0.pow(grade.exposure.toDouble()).toFloat()
        r *= exposure
        g *= exposure
        b *= exposure

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

        val contrast = (1f + grade.contrast * .68f).coerceIn(.20f, 1.8f)
        r = (r - .5f) * contrast + .5f
        g = (g - .5f) * contrast + .5f
        b = (b - .5f) * contrast + .5f

        val gamma = 2.0.pow((-grade.gamma * .6f).toDouble())
        r = clip(r).toDouble().pow(gamma).toFloat()
        g = clip(g).toDouble().pow(gamma).toFloat()
        b = clip(b).toDouble().pow(gamma).toFloat()

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

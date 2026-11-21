package com.greenezone.gzphotoeditor.editor

import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

/** Pure deterministic local adjustment engine, tested without Android. */
object MaskMath {
    data class Prepared(
        val mask: LocalMask, val exposureFactor: Float, val cosTilt: Float, val sinTilt: Float
    )

    private fun prep(mask: LocalMask): Prepared {
        val radians = (mask.centerX - .5f) * 2.0 * PI / 3.0
        return Prepared(
            mask, 2.0.pow(mask.exposure.toDouble()).toFloat(),
            kotlin.math.cos(radians).toFloat(), kotlin.math.sin(radians).toFloat()
        )
    }

    fun prepare(masks: List<LocalMask>): List<Prepared> =
        masks.take(4).filter { it.exposure != 0f || it.contrast != 0f || it.saturation != 0f }
            .map(::prep)

    private fun smooth(x: Float): Float {
        val t = x.coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    /** Weight 0..1, smoothly feathered in the final image's normalized space. */
    fun coverage(mask: LocalMask, x: Int, y: Int, width: Int, height: Int): Float =
        coveragePrepared(prep(mask), x, y, width, height)

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
                1f - smooth((dist - inner) / (radius - inner).coerceAtLeast(.0001f))
            }
            MaskKind.LINEAR -> {
                val breadth = mask.radius.coerceIn(.02f, .8f)
                // Left-to-right position controls the gradient tilt (-60..+60 degrees).
                val projection = (ny - mask.centerY) * item.cosTilt +
                    (nx - .5f) * item.sinTilt
                1f - smooth((projection + breadth) / (breadth * 2f))
            }
        }
    }

    fun adjust(argb: Int, masks: List<Prepared>, x: Int, y: Int, width: Int, height: Int): Int {
        if (masks.isEmpty()) return argb
        val a = argb and -0x1000000
        var r = ((argb ushr 16) and 255) / 255f
        var g = ((argb ushr 8) and 255) / 255f
        var b = (argb and 255) / 255f
        for (item in masks) {
            val mask = item.mask
            val weight = coveragePrepared(item, x, y, width, height)
            if (weight <= .0001f) continue
            val exposure = 1f + (item.exposureFactor - 1f) * weight
            r *= exposure; g *= exposure; b *= exposure
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
        fun clipByte(f: Float) = (f.coerceIn(0f, 1f) * 255f + .5f).toInt()
        return a or (clipByte(r) shl 16) or (clipByte(g) shl 8) or clipByte(b)
    }
}

package com.greenezone.gzphotoeditor.editor

import kotlin.math.abs
import kotlin.math.sign

/**
 * Optional edge-aware chroma/luma denoising and independent structure/texture/detail.
 * Reads five source scanlines only, never allocates a full-image intermediate.
 */
object DetailMath {
    fun luminance(color: Int): Float =
        (.2126f * ((color ushr 16) and 255) +
            .7152f * ((color ushr 8) and 255) +
            .0722f * (color and 255)) / 255f

    fun denoise(rows: Array<IntArray>, x: Int, strength: Float): Int {
        val center = rows[2][x]
        if (strength <= 0f) return center
        val centerLum = luminance(center)
        val w = rows[2].size
        val range = .050f + .20f * strength.coerceIn(0f, 1f)
        var r = 0f
        var g = 0f
        var b = 0f
        var weights = 0f
        for (dy in -1..1) {
            val row = rows[dy + 2]
            for (dx in -1..1) {
                val pixel = row[(x + dx).coerceIn(0, w - 1)]
                val diff = abs(luminance(pixel) - centerLum)
                val preserveEdge = (1f - diff / range).coerceIn(0f, 1f)
                val spatial = when {
                    dx == 0 && dy == 0 -> 1.7f
                    dx == 0 || dy == 0 -> 1f
                    else -> .65f
                }
                val weight = if (dx == 0 && dy == 0) spatial else spatial * preserveEdge
                r += ((pixel ushr 16) and 255) * weight
                g += ((pixel ushr 8) and 255) * weight
                b += (pixel and 255) * weight
                weights += weight
            }
        }
        val blend = strength.coerceIn(0f, 1f) * .9f
        fun c(value: Float, original: Int): Int =
            (original * (1f - blend) + value / weights * blend + .5f).toInt().coerceIn(0, 255)
        return (center and -0x1000000) or
            (c(r, (center ushr 16) and 255) shl 16) or
            (c(g, (center ushr 8) and 255) shl 8) or
            c(b, center and 255)
    }

    /**
     * Returns a small luminance shift to add to all channels after the global tone grade.
     * Sharpen uses radius-1 detail, texture uses mid-frequency radius-2, clarity
     * operates on broader radius-2 contrast. All are independently controllable.
     */
    fun detailDelta(rows: Array<IntArray>, x: Int, grade: Grade): Float {
        if (grade.sharpen == 0f && grade.texture == 0f && grade.clarity == 0f) return 0f
        val width = rows[2].size
        val center = luminance(rows[2][x])
        var sum3 = 0f
        var sum5 = 0f
        for (dy in -2..2) {
            val row = rows[dy + 2]
            for (dx in -2..2) {
                val light = luminance(row[(x + dx).coerceIn(0, width - 1)])
                sum5 += light
                if (dy in -1..1 && dx in -1..1) sum3 += light
            }
        }
        val local3 = sum3 / 9f
        val local5 = sum5 / 25f
        val threshold = grade.sharpenThreshold.coerceIn(0f, 1f) * .12f
        val high = center - local3
        val effectiveHigh = if (abs(high) <= threshold) 0f
            else sign(high) * (abs(high) - threshold)
        val midtone = (1f - abs(center - .5f) * 1.4f).coerceIn(.25f, 1f)
        val sharpen = effectiveHigh * grade.sharpen * 1.12f
        val texture = (local3 - local5) * grade.texture * .9f * midtone
        val clarity = (center - local5) * grade.clarity * .4f * midtone
        return (sharpen + texture + clarity).coerceIn(-.33f, .33f)
    }
}

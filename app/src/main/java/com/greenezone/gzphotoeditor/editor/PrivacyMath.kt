package com.greenezone.gzphotoeditor.editor

import kotlin.math.ceil
import kotlin.math.floor

object PrivacyMath {
    data class Bounds(val left: Int, val top: Int, val right: Int, val bottom: Int) {
        val width get() = right - left
        val height get() = bottom - top
        val empty get() = width <= 0 || height <= 0
    }

    fun bounds(mark: PrivacyEdit, imageWidth: Int, imageHeight: Int): Bounds {
        if (imageWidth <= 0 || imageHeight <= 0) return Bounds(0, 0, 0, 0)
        val minSide = minOf(imageWidth, imageHeight).toFloat()
        val spanX: Float
        val spanY: Float
        val left: Float
        val top: Float
        val right: Float
        val bottom: Float
        if (mark.kind.usesBrush) {
            val points = mark.strokes.asSequence().flatMap { it.points.asSequence() }
                .filter { it.x.isFinite() && it.y.isFinite() && it.x in 0f..1f && it.y in 0f..1f }
                .take(650).toList()
            if (points.isEmpty()) return Bounds(0, 0, 0, 0)
            val radiusX = mark.brushSize.coerceIn(.005f, .2f) * minSide / imageWidth
            val radiusY = mark.brushSize.coerceIn(.005f, .2f) * minSide / imageHeight
            left = points.minOf { it.x } - radiusX - .003f
            right = points.maxOf { it.x } + radiusX + .003f
            top = points.minOf { it.y } - radiusY - .003f
            bottom = points.maxOf { it.y } + radiusY + .003f
            spanX = 1f
            spanY = 1f
        } else {
            spanX = mark.width.coerceIn(.02f, 1f)
            spanY = mark.height.coerceIn(.02f, 1f)
            val cx = mark.centerX.coerceIn(0f, 1f)
            val cy = mark.centerY.coerceIn(0f, 1f)
            left = cx - spanX / 2f
            right = cx + spanX / 2f
            top = cy - spanY / 2f
            bottom = cy + spanY / 2f
        }
        return Bounds(
            floor(left * imageWidth).toInt().coerceIn(0, imageWidth),
            floor(top * imageHeight).toInt().coerceIn(0, imageHeight),
            ceil(right * imageWidth).toInt().coerceIn(0, imageWidth),
            ceil(bottom * imageHeight).toInt().coerceIn(0, imageHeight)
        )
    }

    fun rectCoverage(mark: PrivacyEdit, x: Int, y: Int, imageWidth: Int, imageHeight: Int): Float {
        if (imageWidth <= 0 || imageHeight <= 0) return 0f
        val dx = kotlin.math.abs((x + .5f) / imageWidth - mark.centerX)
        val dy = kotlin.math.abs((y + .5f) / imageHeight - mark.centerY)
        val halfW = mark.width.coerceIn(.02f, 1f) / 2f
        val halfH = mark.height.coerceIn(.02f, 1f) / 2f
        if (dx > halfW || dy > halfH) return 0f
        if (mark.kind == PrivacyKind.BLACK_BAR || mark.kind == PrivacyKind.COLOR_COVER) return 1f
        val feather = mark.feather.coerceIn(0f, .8f)
        if (feather <= 0f) return 1f
        val edgeX = ((halfW - dx) / (halfW * feather)).coerceIn(0f, 1f)
        val edgeY = ((halfH - dy) / (halfH * feather)).coerceIn(0f, 1f)
        val a = minOf(edgeX, edgeY)
        return a * a * (3f - 2f * a)
    }

    fun withCenter(mark: PrivacyEdit, x: Float, y: Float) = mark.copy(
        centerX = x.coerceIn(0f, 1f), centerY = y.coerceIn(0f, 1f)
    )
}

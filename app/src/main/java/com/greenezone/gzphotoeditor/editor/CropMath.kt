package com.greenezone.gzphotoeditor.editor

import kotlin.math.abs
import kotlin.math.roundToInt

enum class CropHandle { MOVE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

/** Aspect-safe, clamped, normalized rectangle geometry. */
object CropMath {
    private const val MIN_SIDE = .035f
    fun sanitize(input: CropFrame): CropFrame {
        val left = if (input.left.isFinite()) input.left.coerceIn(0f, 1f) else 0f
        val top = if (input.top.isFinite()) input.top.coerceIn(0f, 1f) else 0f
        val right = if (input.right.isFinite()) input.right.coerceIn(0f, 1f) else 1f
        val bottom = if (input.bottom.isFinite()) input.bottom.coerceIn(0f, 1f) else 1f
        val l = minOf(left, right - MIN_SIDE).coerceIn(0f, 1f - MIN_SIDE)
        val t = minOf(top, bottom - MIN_SIDE).coerceIn(0f, 1f - MIN_SIDE)
        return CropFrame(l, t, maxOf(l + MIN_SIDE, right).coerceAtMost(1f),
            maxOf(t + MIN_SIDE, bottom).coerceAtMost(1f))
    }

    fun initial(width: Int, height: Int, ratio: CropRatio): CropFrame {
        require(width > 0 && height > 0)
        val wanted = ratio.ratio ?: return CropFrame(0f, 0f, 1f, 1f)
        val actual = width.toFloat() / height
        val fractionWidth = if (actual > wanted) wanted / actual else 1f
        val fractionHeight = if (actual < wanted) actual / wanted else 1f
        return CropFrame(
            (1f - fractionWidth) / 2f, (1f - fractionHeight) / 2f,
            (1f + fractionWidth) / 2f, (1f + fractionHeight) / 2f
        )
    }

    fun drag(
        frame: CropFrame,
        handle: CropHandle,
        dx: Float,
        dy: Float,
        imageWidth: Int,
        imageHeight: Int,
        ratio: Float?
    ): CropFrame {
        if (imageWidth <= 0 || imageHeight <= 0) return sanitize(frame)
        val current = sanitize(frame)
        if (handle == CropHandle.MOVE) {
            val width = current.width
            val height = current.height
            val x = (current.left + dx).coerceIn(0f, 1f - width)
            val y = (current.top + dy).coerceIn(0f, 1f - height)
            return CropFrame(x, y, x + width, y + height)
        }

        val isLeft = handle == CropHandle.TOP_LEFT || handle == CropHandle.BOTTOM_LEFT
        val isTop = handle == CropHandle.TOP_LEFT || handle == CropHandle.TOP_RIGHT
        val anchorX = if (isLeft) current.right else current.left
        val anchorY = if (isTop) current.bottom else current.top
        val oldX = if (isLeft) current.left else current.right
        val oldY = if (isTop) current.top else current.bottom
        val projectedX = (oldX + dx).coerceIn(0f, 1f)
        val projectedY = (oldY + dy).coerceIn(0f, 1f)
        val xSign = if (isLeft) -1f else 1f
        val ySign = if (isTop) -1f else 1f
        var newWidth = ((projectedX - anchorX) * xSign).coerceAtLeast(MIN_SIDE)
        var newHeight = ((projectedY - anchorY) * ySign).coerceAtLeast(MIN_SIDE)
        val maximumWidth = if (isLeft) anchorX else 1f - anchorX
        val maximumHeight = if (isTop) anchorY else 1f - anchorY

        if (ratio != null && ratio > 0f) {
            val normalizedRatio = (ratio * imageHeight.toFloat() / imageWidth)
                .coerceAtLeast(.0001f)
            val oldPixelDeltaX = abs(dx) * imageWidth
            val oldPixelDeltaY = abs(dy) * imageHeight
            if (oldPixelDeltaX >= oldPixelDeltaY) {
                newHeight = newWidth / normalizedRatio
            } else {
                newWidth = newHeight * normalizedRatio
            }
            newWidth = minOf(newWidth, maximumWidth, maximumHeight * normalizedRatio)
            newHeight = newWidth / normalizedRatio
            // A very narrow image can make the requested ratio smaller than MIN_SIDE.
            newWidth = newWidth.coerceAtLeast(.003f)
            newHeight = newHeight.coerceAtLeast(.003f)
        } else {
            newWidth = newWidth.coerceIn(MIN_SIDE.coerceAtMost(maximumWidth), maximumWidth)
            newHeight = newHeight.coerceIn(MIN_SIDE.coerceAtMost(maximumHeight), maximumHeight)
        }
        val x = anchorX + xSign * newWidth
        val y = anchorY + ySign * newHeight
        return CropFrame(minOf(x, anchorX).coerceIn(0f, 1f),
            minOf(y, anchorY).coerceIn(0f, 1f),
            maxOf(x, anchorX).coerceIn(0f, 1f),
            maxOf(y, anchorY).coerceIn(0f, 1f))
    }

    fun pixelBounds(
        width: Int,
        height: Int,
        frame: CropFrame
    ): IntArray {
        require(width > 0 && height > 0)
        val f = sanitize(frame)
        val left = (f.left * width).roundToInt().coerceIn(0, width - 1)
        val top = (f.top * height).roundToInt().coerceIn(0, height - 1)
        val right = (f.right * width).roundToInt().coerceIn(left + 1, width)
        val bottom = (f.bottom * height).roundToInt().coerceIn(top + 1, height)
        return intArrayOf(left, top, right - left, bottom - top)
    }
}

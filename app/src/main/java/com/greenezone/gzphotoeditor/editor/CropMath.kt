package com.greenezone.gzphotoeditor.editor

import kotlin.math.abs
import kotlin.math.roundToInt

enum class CropHandle { MOVE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, LEFT, TOP, RIGHT, BOTTOM }

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

    /** x/y are relative to the fitted image in screen pixels and can be outside it. */
    fun hitTest(
        frame: CropFrame, x: Float, y: Float, width: Float, height: Float, radius: Float
    ): CropHandle? {
        if (width <= 0f || height <= 0f || radius <= 0f) return null
        val f = sanitize(frame)
        val left = f.left * width
        val right = f.right * width
        val top = f.top * height
        val bottom = f.bottom * height
        val corners = listOf(
            Triple(CropHandle.TOP_LEFT, abs(x - left), abs(y - top)),
            Triple(CropHandle.TOP_RIGHT, abs(x - right), abs(y - top)),
            Triple(CropHandle.BOTTOM_LEFT, abs(x - left), abs(y - bottom)),
            Triple(CropHandle.BOTTOM_RIGHT, abs(x - right), abs(y - bottom))
        )
        corners.filter { (_, dx, dy) -> dx <= radius && dy <= radius }
            .minByOrNull { (_, dx, dy) -> dx * dx + dy * dy }
            ?.let { return it.first }

        val edges = buildList {
            if (y in top..bottom) {
                add(CropHandle.LEFT to abs(x - left))
                add(CropHandle.RIGHT to abs(x - right))
            }
            if (x in left..right) {
                add(CropHandle.TOP to abs(y - top))
                add(CropHandle.BOTTOM to abs(y - bottom))
            }
        }
        edges.filter { it.second <= radius }.minByOrNull { it.second }
            ?.let { return it.first }
        return CropHandle.MOVE.takeIf { x in left..right && y in top..bottom }
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
        val current = sanitize(frame)
        if (imageWidth <= 0 || imageHeight <= 0 || !dx.isFinite() || !dy.isFinite() ||
            (dx == 0f && dy == 0f)) return current
        if (handle == CropHandle.MOVE) {
            val width = current.width
            val height = current.height
            val x = (current.left + dx).coerceIn(0f, 1f - width)
            val y = (current.top + dy).coerceIn(0f, 1f - height)
            return CropFrame(x, y, x + width, y + height)
        }

        val normalizedRatio = ratio?.takeIf { it.isFinite() && it > 0f }
            ?.let { (it * imageHeight.toFloat() / imageWidth).coerceAtLeast(.0001f) }
        if (handle == CropHandle.LEFT || handle == CropHandle.RIGHT ||
            handle == CropHandle.TOP || handle == CropHandle.BOTTOM) {
            return dragEdge(current, handle, dx, dy, normalizedRatio)
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

        if (normalizedRatio != null) {
            val oldPixelDeltaX = abs(dx) * imageWidth
            val oldPixelDeltaY = abs(dy) * imageHeight
            if (oldPixelDeltaX >= oldPixelDeltaY) {
                newHeight = newWidth / normalizedRatio
            } else {
                newWidth = newHeight * normalizedRatio
            }
            val limit = minOf(maximumWidth, maximumHeight * normalizedRatio)
            val minWidth = maxOf(MIN_SIDE, MIN_SIDE * normalizedRatio).coerceAtMost(limit)
            newWidth = newWidth.coerceIn(minWidth, limit)
            newHeight = newWidth / normalizedRatio
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

    private fun dragEdge(
        frame: CropFrame, handle: CropHandle, dx: Float, dy: Float, ratio: Float?
    ): CropFrame {
        val horizontal = handle == CropHandle.LEFT || handle == CropHandle.RIGHT
        val near = handle == CropHandle.LEFT || handle == CropHandle.TOP
        val start = if (horizontal) frame.left else frame.top
        val end = if (horizontal) frame.right else frame.bottom
        val crossStart = if (horizontal) frame.top else frame.left
        val crossEnd = if (horizontal) frame.bottom else frame.right
        val anchor = if (near) end else start
        val sign = if (near) -1f else 1f
        val old = if (near) start else end
        val delta = if (horizontal) dx else dy
        var maximum = if (near) anchor else 1f - anchor
        var minimum = MIN_SIDE
        val crossCenter = (crossStart + crossEnd) / 2f
        val crossPerAxis = ratio?.let { if (horizontal) 1f / it else it }
        if (crossPerAxis != null) {
            val crossMaximum = 2f * minOf(crossCenter, 1f - crossCenter)
            maximum = minOf(maximum, crossMaximum / crossPerAxis)
            minimum = maxOf(minimum, MIN_SIDE / crossPerAxis)
        }
        val axisLength = ((old + delta - anchor) * sign)
            .coerceIn(minimum.coerceAtMost(maximum), maximum)
        val next = (anchor + axisLength * sign).coerceIn(0f, 1f)
        val low = minOf(anchor, next)
        val high = maxOf(anchor, next)
        val crossHalf = crossPerAxis?.let { axisLength * it / 2f }
        val crossLow = crossHalf?.let { (crossCenter - it).coerceIn(0f, 1f) } ?: crossStart
        val crossHigh = crossHalf?.let { (crossCenter + it).coerceIn(0f, 1f) } ?: crossEnd
        return if (horizontal) CropFrame(low, crossLow, high, crossHigh)
            else CropFrame(crossLow, low, crossHigh, high)
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

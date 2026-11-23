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
        // MIN_SIDE is a gesture constraint. Expanding existing valid bounds here
        // would change locked crops on panoramas and alter saved custom rectangles.
        val l = if (left < right) left else right.coerceAtMost(1f - MIN_SIDE)
        val t = if (top < bottom) top else bottom.coerceAtMost(1f - MIN_SIDE)
        return CropFrame(l, t, if (left < right) right else l + MIN_SIDE,
            if (top < bottom) bottom else t + MIN_SIDE)
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

    fun presetSize(width: Int, height: Int, ratio: CropRatio): Pair<Int, Int> {
        require(width > 0 && height > 0)
        val wanted = ratio.ratio ?: return width to height
        val current = width.toFloat() / height
        return if (current > wanted) {
            (height * wanted).roundToInt().coerceIn(1, width) to height
        } else {
            width to (width / wanted).roundToInt().coerceIn(1, height)
        }
    }

    /** Older recipes place their crop within the room left by the preset. */
    fun presetBounds(
        width: Int, height: Int, ratio: CropRatio, centerX: Float, centerY: Float
    ): IntArray {
        val (cropWidth, cropHeight) = presetSize(width, height, ratio)
        val (left, top) = GeometryMath.cropOrigin(width, height, cropWidth, cropHeight, centerX, centerY)
        return intArrayOf(left, top, cropWidth, cropHeight)
    }

    fun displayFrame(width: Int, height: Int, document: EditDocument): CropFrame {
        val explicit = document.cropFrame
        if (explicit != null) {
            val ratio = document.crop.ratio
            return if (document.cropFrameRatioLocked && ratio != null)
                fitAspect(explicit, ratio * height / width) else sanitize(explicit)
        }
        val (left, top, cropWidth, cropHeight) =
            presetBounds(width, height, document.crop, document.cropCenterX, document.cropCenterY)
        return CropFrame(left.toFloat() / width, top.toFloat() / height,
            (left + cropWidth).toFloat() / width, (top + cropHeight).toFloat() / height)
    }

    private fun fitAspect(frame: CropFrame, normalizedRatio: Float): CropFrame {
        val f = sanitize(frame)
        val width = minOf(f.width, f.height * normalizedRatio)
        val height = minOf(f.height, f.width / normalizedRatio)
        val left = f.left + (f.width - width) / 2f
        val top = f.top + (f.height - height) / 2f
        return CropFrame(left, top, left + width, top + height)
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
        // Keep the generous outer targets, but reserve the central half of even a
        // very small frame for its edge dots and move gesture.
        val innerX = minOf(radius, (right - left) / 4f)
        val innerY = minOf(radius, (bottom - top) / 4f)
        fun inLeft() = x in (left - radius)..(left + innerX)
        fun inRight() = x in (right - innerX)..(right + radius)
        fun inTop() = y in (top - radius)..(top + innerY)
        fun inBottom() = y in (bottom - innerY)..(bottom + radius)
        return when {
            inLeft() && inTop() -> CropHandle.TOP_LEFT
            inRight() && inTop() -> CropHandle.TOP_RIGHT
            inLeft() && inBottom() -> CropHandle.BOTTOM_LEFT
            inRight() && inBottom() -> CropHandle.BOTTOM_RIGHT
            inLeft() && y in top..bottom -> CropHandle.LEFT
            inRight() && y in top..bottom -> CropHandle.RIGHT
            inTop() && x in left..right -> CropHandle.TOP
            inBottom() && x in left..right -> CropHandle.BOTTOM
            x in left..right && y in top..bottom -> CropHandle.MOVE
            else -> null
        }
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
        if (imageWidth <= 0 || imageHeight <= 0 || !dx.isFinite() || !dy.isFinite())
            return sanitize(frame)
        val normalizedRatio = ratio?.let { it * imageHeight.toFloat() / imageWidth }
            ?.takeIf { it.isFinite() && it > 0f }
        val current = if (normalizedRatio == null) sanitize(frame) else fitAspect(frame, normalizedRatio)
        if (dx == 0f && dy == 0f) return current
        val minWidth = MIN_SIDE * minOf(1f, normalizedRatio ?: 1f)
        val minHeight = MIN_SIDE * minOf(1f, 1f / (normalizedRatio ?: 1f))
        if (handle == CropHandle.MOVE) {
            val width = current.width
            val height = current.height
            val x = (current.left + dx).coerceIn(0f, 1f - width)
            val y = (current.top + dy).coerceIn(0f, 1f - height)
            return CropFrame(x, y, x + width, y + height)
        }

        if (handle == CropHandle.LEFT || handle == CropHandle.RIGHT ||
            handle == CropHandle.TOP || handle == CropHandle.BOTTOM) {
            return dragEdge(current, handle, dx, dy, normalizedRatio, minWidth, minHeight)
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
        var newWidth = ((projectedX - anchorX) * xSign).coerceAtLeast(minWidth)
        var newHeight = ((projectedY - anchorY) * ySign).coerceAtLeast(minHeight)
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
            newWidth = newWidth.coerceIn(minWidth.coerceAtMost(limit), limit)
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
        frame: CropFrame, handle: CropHandle, dx: Float, dy: Float, ratio: Float?,
        minWidth: Float, minHeight: Float
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
        val minimum = if (horizontal) minWidth else minHeight
        val crossCenter = (crossStart + crossEnd) / 2f
        val crossPerAxis = ratio?.let { if (horizontal) 1f / it else it }
        if (crossPerAxis != null) {
            val crossMaximum = 2f * minOf(crossCenter, 1f - crossCenter)
            maximum = minOf(maximum, crossMaximum / crossPerAxis)
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
        frame: CropFrame,
        lockedRatio: CropRatio? = null
    ): IntArray {
        require(width > 0 && height > 0)
        val f = sanitize(frame)
        val left = (f.left * width).roundToInt().coerceIn(0, width - 1)
        val top = (f.top * height).roundToInt().coerceIn(0, height - 1)
        val right = (f.right * width).roundToInt().coerceIn(left + 1, width)
        val bottom = (f.bottom * height).roundToInt().coerceIn(top + 1, height)
        val cropWidth = right - left
        val cropHeight = bottom - top
        if (lockedRatio != null && lockedRatio.ratio != null) {
            // Snap inwards to exact aspect units so separately rounded edges or a
            // differently sized export source cannot quietly change the ratio.
            val units = minOf(cropWidth / lockedRatio.unitsWide, cropHeight / lockedRatio.unitsHigh)
            if (units > 0) {
                val exactWidth = units * lockedRatio.unitsWide
                val exactHeight = units * lockedRatio.unitsHigh
                return intArrayOf(left + (cropWidth - exactWidth) / 2,
                    top + (cropHeight - exactHeight) / 2, exactWidth, exactHeight)
            }
        }
        return intArrayOf(left, top, cropWidth, cropHeight)
    }
}

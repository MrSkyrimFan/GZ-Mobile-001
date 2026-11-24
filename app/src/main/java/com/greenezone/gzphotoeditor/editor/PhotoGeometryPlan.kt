package com.greenezone.gzphotoeditor.editor

import android.graphics.Matrix
import android.graphics.RectF
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

internal class PhotoGeometryPlan(width: Int, height: Int, geometry: PhotoGeometry) {
    val orientation = Matrix()
    val straighten = Matrix()
    val safeLeft: Int
    val safeTop: Int
    val safeWidth: Int
    val safeHeight: Int
    val crop: IntArray
    private val sourceToView = Matrix()
    val width: Int get() = crop[2]
    val height: Int get() = crop[3]

    init {
        val quarter = ((geometry.quarterTurns % 4) + 4) % 4
        if (geometry.mirrored || geometry.flipVertical) {
            orientation.postScale(
                if (geometry.mirrored) -1f else 1f,
                if (geometry.flipVertical) -1f else 1f,
                width / 2f, height / 2f
            )
        }
        if (quarter != 0) orientation.postRotate(quarter * 90f, width / 2f, height / 2f)
        val oriented = applyBitmapTransform(orientation, width, height)
        val angle = geometry.straightenDegrees.coerceIn(-15f, 15f)
        if (abs(angle) > .001f) straighten.setRotate(angle)
        val spun = applyBitmapTransform(straighten, oriented.first, oriented.second)
        val safe = if (straighten.isIdentity) oriented
            else GeometryMath.safeStraightenCrop(oriented.first, oriented.second, angle)
        safeWidth = safe.first.coerceAtMost(spun.first)
        safeHeight = safe.second.coerceAtMost(spun.second)
        safeLeft = (spun.first - safeWidth) / 2
        safeTop = (spun.second - safeHeight) / 2
        crop = geometry.cropFrame?.let {
            CropMath.pixelBounds(safeWidth, safeHeight, it,
                geometry.crop.takeIf { geometry.cropFrameRatioLocked })
        } ?: CropMath.presetBounds(safeWidth, safeHeight, geometry.crop,
            geometry.cropCenterX, geometry.cropCenterY)
        sourceToView.postTranslate(-(safeLeft + crop[0]).toFloat(), -(safeTop + crop[1]).toFloat())
    }

    /** Bitmap.createBitmap translates the transformed image to its output bounds. */
    private fun applyBitmapTransform(matrix: Matrix, width: Int, height: Int): Pair<Int, Int> {
        if (matrix.isIdentity) return width to height
        val bounds = RectF(0f, 0f, width.toFloat(), height.toFloat())
        matrix.mapRect(bounds)
        sourceToView.postConcat(matrix)
        sourceToView.postTranslate(-bounds.left, -bounds.top)
        return bounds.width().roundToInt().coerceAtLeast(1) to
            bounds.height().roundToInt().coerceAtLeast(1)
    }

    fun projectFrom(authoring: PhotoGeometryPlan): PrivacyProjection {
        val fromAuthoring = Matrix()
        check(authoring.sourceToView.invert(fromAuthoring))
        fromAuthoring.postConcat(sourceToView)
        return PrivacyProjection(fromAuthoring, authoring.width, authoring.height, width, height)
    }
}

/** A mark's authored raster coordinates and their positions in the current image. */
class PrivacyProjection internal constructor(
    internal val fromAuthoring: Matrix,
    internal val authorWidth: Int,
    internal val authorHeight: Int,
    private val viewWidth: Int,
    private val viewHeight: Int
) {
    private val inverse = Matrix().also { check(fromAuthoring.invert(it)) }
    private val inverseValues = FloatArray(9).also(inverse::getValues)
    internal val radiusScale = minOf(authorWidth, authorHeight).toFloat() / minOf(viewWidth, viewHeight)

    fun toView(point: BrushPoint): BrushPoint {
        val mapped = floatArrayOf(point.x * authorWidth, point.y * authorHeight)
        fromAuthoring.mapPoints(mapped)
        return BrushPoint(mapped[0] / viewWidth, mapped[1] / viewHeight)
    }

    fun toAuthoring(point: BrushPoint): BrushPoint {
        val mapped = floatArrayOf(point.x * viewWidth, point.y * viewHeight)
        inverse.mapPoints(mapped)
        return BrushPoint(mapped[0] / authorWidth, mapped[1] / authorHeight)
    }

    internal fun authorX(x: Float, y: Float) =
        inverseValues[Matrix.MSCALE_X] * x + inverseValues[Matrix.MSKEW_X] * y +
            inverseValues[Matrix.MTRANS_X]

    internal fun authorY(x: Float, y: Float) =
        inverseValues[Matrix.MSKEW_Y] * x + inverseValues[Matrix.MSCALE_Y] * y +
            inverseValues[Matrix.MTRANS_Y]

    internal fun boundsInView(rect: PrivacyMath.Bounds): PrivacyMath.Bounds {
        if (rect.empty) return rect
        val mapped = RectF(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat())
        fromAuthoring.mapRect(mapped)
        return PrivacyMath.Bounds(
            floor(mapped.left).toInt().coerceIn(0, viewWidth),
            floor(mapped.top).toInt().coerceIn(0, viewHeight),
            ceil(mapped.right).toInt().coerceIn(0, viewWidth),
            ceil(mapped.bottom).toInt().coerceIn(0, viewHeight)
        )
    }
}

package com.greenezone.gzphotoeditor.editor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.min
import kotlin.math.roundToInt

enum class ProSelectionKind(val label: String) {
    RECTANGLE("Rectangle"), ELLIPSE("Ellipse"), LASSO("Freehand")
}

/** Normalized project-space geometry. Selection can be applied as a reversible layer mask. */
data class ProSelection(
    val kind: ProSelectionKind,
    val centerX: Float = .5f,
    val centerY: Float = .5f,
    val width: Float = .6f,
    val height: Float = .6f,
    val points: List<BrushPoint> = emptyList(),
    val inverted: Boolean = false
)

enum class ProRetouchKind(val label: String) {
    CLONE("Clone stamp"),
    SOFT_REPAIR("Soft repair")
}

/** Uses an explicit source point and target point; no AI, ML or source-file writes. */
data class ProRetouchMark(
    val kind: ProRetouchKind,
    val sourceX: Float,
    val sourceY: Float,
    val targetX: Float,
    val targetY: Float,
    val radius: Float = .04f,
    val strength: Float = 1f
)

enum class ProPixelEffect(val label: String) {
    NONE("Unchanged"),
    GRAYSCALE("Black & white"),
    INVERT("Invert colors"),
    THRESHOLD("Threshold"),
    POSTERIZE("Posterize")
}

object ProToolMath {
    const val MAX_LASSO_POINTS = 450
    const val MAX_RETOUCH = 80

    fun valid(selection: ProSelection): Boolean {
        fun fraction(v: Float) = v.isFinite() && v in 0f..1f
        if (!fraction(selection.centerX) || !fraction(selection.centerY) ||
            !fraction(selection.width) || !fraction(selection.height)) return false
        return if (selection.kind == ProSelectionKind.LASSO) {
            selection.points.size in 3..MAX_LASSO_POINTS &&
                selection.points.all { fraction(it.x) && fraction(it.y) }
        } else selection.width >= .005f && selection.height >= .005f &&
            selection.points.isEmpty()
    }

    fun valid(retouch: ProRetouchMark): Boolean =
        listOf(retouch.sourceX, retouch.sourceY, retouch.targetX, retouch.targetY)
            .all { it.isFinite() && it in 0f..1f } &&
            retouch.radius.isFinite() && retouch.radius in .005f.. .20f &&
            retouch.strength.isFinite() && retouch.strength in .1f..1f

    fun rectangle(start: BrushPoint, end: BrushPoint, kind: ProSelectionKind): ProSelection {
        require(kind != ProSelectionKind.LASSO)
        val left = minOf(start.x, end.x).coerceIn(0f, 1f)
        val top = minOf(start.y, end.y).coerceIn(0f, 1f)
        val right = maxOf(start.x, end.x).coerceIn(0f, 1f)
        val bottom = maxOf(start.y, end.y).coerceIn(0f, 1f)
        return ProSelection(
            kind = kind,
            centerX = (left + right) / 2f,
            centerY = (top + bottom) / 2f,
            width = (right - left).coerceAtLeast(.005f),
            height = (bottom - top).coerceAtLeast(.005f)
        )
    }

    fun path(mask: ProSelection, width: Int, height: Int): Path {
        require(valid(mask))
        require(width > 0 && height > 0)
        return Path().apply {
            when (mask.kind) {
                ProSelectionKind.RECTANGLE, ProSelectionKind.ELLIPSE -> {
                    val l = (mask.centerX - mask.width / 2f) * width
                    val t = (mask.centerY - mask.height / 2f) * height
                    val r = (mask.centerX + mask.width / 2f) * width
                    val b = (mask.centerY + mask.height / 2f) * height
                    if (mask.kind == ProSelectionKind.RECTANGLE)
                        addRect(RectF(l, t, r, b), Path.Direction.CW)
                    else addOval(RectF(l, t, r, b), Path.Direction.CW)
                }
                ProSelectionKind.LASSO -> {
                    mask.points.forEachIndexed { i, point ->
                        if (i == 0) moveTo(point.x * width, point.y * height)
                        else lineTo(point.x * width, point.y * height)
                    }
                    close()
                }
            }
        }
    }
}

/**
 * Real local copy/heal pixel operations on the selected image layer.
 * Source pixels are read from an independent immutable snapshot. Soft repair
 * blends cloned pixels with the destination; it is not advertised as AI healing.
 */
object ProRetouchRaster {
    fun apply(
        source: Bitmap,
        marks: List<ProRetouchMark>,
        cancel: (() -> Unit)? = null
    ): Bitmap {
        require(marks.size <= ProToolMath.MAX_RETOUCH && marks.all(ProToolMath::valid))
        if (marks.isEmpty()) return source
        val output = source.copy(Bitmap.Config.ARGB_8888, true)
            ?: throw IllegalStateException("Could not allocate the retouch layer")
        try {
            val canvas = Canvas(output)
            val pixels = min(output.width, output.height).toFloat()
            for (mark in marks) {
                cancel?.invoke()
                val radius = mark.radius * pixels
                val x = mark.targetX * output.width
                val y = mark.targetY * output.height
                val sx = mark.sourceX * output.width
                val sy = mark.sourceY * output.height
                val src = android.graphics.Rect(
                    (sx - radius).roundToInt().coerceIn(0, source.width - 1),
                    (sy - radius).roundToInt().coerceIn(0, source.height - 1),
                    (sx + radius).roundToInt().coerceIn(1, source.width),
                    (sy + radius).roundToInt().coerceIn(1, source.height)
                )
                if (src.width() < 1 || src.height() < 1) continue
                val dst = RectF(x - radius, y - radius, x + radius, y + radius)
                val save = canvas.save()
                try {
                    canvas.clipPath(Path().apply {
                        addCircle(x, y, radius, Path.Direction.CW)
                    })
                    val opacity = if (mark.kind == ProRetouchKind.SOFT_REPAIR) .57f else 1f
                    canvas.drawBitmap(source, src, dst,
                        Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                            alpha = (255f * opacity * mark.strength)
                                .roundToInt().coerceIn(0, 255)
                        })
                } finally { canvas.restoreToCount(save) }
            }
            return output
        } catch (error: Throwable) {
            output.recycle()
            throw error
        }
    }
}

/** Photoshop/GIMP-like pixel-effects applied as independent compositing layers. */
object ProEffectRaster {
    fun render(
        source: Bitmap, effect: ProPixelEffect,
        amount: Float = .5f,
        cancel: (() -> Unit)? = null
    ): Bitmap {
        if (effect == ProPixelEffect.NONE) return source
        require(amount.isFinite() && amount in 0f..1f)
        val output = source.copy(Bitmap.Config.ARGB_8888, true)
            ?: throw IllegalStateException("Cannot allocate pixel-effect layer")
        val row = IntArray(source.width)
        try {
            for (y in 0 until source.height) {
                if ((y and 31) == 0) cancel?.invoke()
                source.getPixels(row, 0, source.width, 0, y, source.width, 1)
                for (i in row.indices) {
                    val argb = row[i]
                    val r = Color.red(argb); val g = Color.green(argb); val b = Color.blue(argb)
                    val gray = (r * .299f + g * .587f + b * .114f).roundToInt().coerceIn(0, 255)
                    val out = when (effect) {
                        ProPixelEffect.NONE -> argb
                        ProPixelEffect.GRAYSCALE -> Color.rgb(gray, gray, gray)
                        ProPixelEffect.INVERT -> Color.rgb(255 - r, 255 - g, 255 - b)
                        ProPixelEffect.THRESHOLD -> {
                            val v = if (gray >= (amount * 255f).roundToInt()) 255 else 0
                            Color.rgb(v, v, v)
                        }
                        ProPixelEffect.POSTERIZE -> {
                            val steps = (2 + amount * 6f).roundToInt().coerceIn(2, 8)
                            fun q(v: Int) =
                                ((v.toFloat() / 255f * (steps - 1)).roundToInt() *
                                    255f / (steps - 1)).roundToInt().coerceIn(0, 255)
                            Color.rgb(q(r), q(g), q(b))
                        }
                    }
                    row[i] = (argb and -0x1000000) or (out and 0xFFFFFF)
                }
                output.setPixels(row, 0, source.width, 0, y, source.width, 1)
            }
            return output
        } catch (error: Throwable) {
            output.recycle()
            throw error
        }
    }
}

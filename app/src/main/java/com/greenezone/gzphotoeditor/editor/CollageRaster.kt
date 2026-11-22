package com.greenezone.gzphotoeditor.editor

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.net.Uri
import kotlin.math.roundToInt

/** Builds one raster from individually editable sources. No cloud, AI, or source writes. */
object CollageRaster {
    class MissingPhoto(val position: Int, cause: Throwable) :
        IllegalStateException("Photo " + (position + 1) + " is unavailable; reselect it", cause)

    private fun rect(r: CollageRect, w: Int, h: Int): RectF =
        RectF(r.left * w, r.top * h, r.right * w, r.bottom * h)

    fun render(
        resolver: ContentResolver,
        collage: CollageDocument,
        width: Int,
        height: Int,
        preview: Boolean,
        cancelCheck: (() -> Unit)? = null
    ): Bitmap = renderWithLoader(collage, width, height, preview, cancelCheck) { item, maxEdge, maxPixels ->
        val uri = Uri.parse(item.uri)
        require(uri.scheme == "content") { "Collage entries require a local Android photo URI" }
        ImagePipeline.decode(resolver, uri, maxEdge, maxPixels)
    }

    /** Loader seam allows real bitmap output tests on an Android emulator. */
    fun renderWithLoader(
        collage: CollageDocument,
        width: Int,
        height: Int,
        preview: Boolean,
        cancelCheck: (() -> Unit)? = null,
        loader: (CollagePhoto, Int, Long) -> Bitmap
    ): Bitmap {
        require(CollageMath.validCount(collage.photos.size))
        require(width > 0 && height > 0 && width.toLong() * height <= 24_000_000L)
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val background = collage.layout.background.argb
        output.setHasAlpha(collage.layout.background == CollageBackground.TRANSPARENT)
        val canvas = Canvas(output)
        canvas.drawColor(background)
        val frames = CollageMath.innerFrames(collage.photos.size, collage.layout)
        val stroke = collage.layout.border.coerceIn(0f, .025f) * minOf(width, height)
        val radiusBase = collage.layout.cornerRadius.coerceIn(0f, .1f) * minOf(width, height)
        try {
            for ((index, photo) in collage.photos.withIndex()) {
                cancelCheck?.invoke()
                val cell = rect(frames[index], width, height)
                if (cell.width() < 1f || cell.height() < 1f) continue
                val radius = radiusBase.coerceAtMost(minOf(cell.width(), cell.height()) / 2f)
                val clipPath = Path().apply {
                    addRoundRect(cell, radius, radius, Path.Direction.CW)
                }
                var source: Bitmap? = null
                var edited: Bitmap? = null
                try {
                    val maxEdge = if (preview) {
                        (maxOf(cell.width(), cell.height()) * 1.25f)
                            .roundToInt().coerceIn(72, 1400)
                    } else {
                        (maxOf(cell.width(), cell.height()) * 1.5f)
                            .roundToInt().coerceIn(128, 5600)
                    }
                    val pixelBudget = if (preview) 1_500_000L else
                        (cell.width().toLong() * cell.height().toLong() * 2L)
                            .coerceIn(250_000L, 9_000_000L)
                    val decoded = try {
                        loader(photo, maxEdge, pixelBudget)
                    } catch (failure: Exception) {
                        throw MissingPhoto(index, failure)
                    }
                    source = decoded
                    val result = ImagePipeline.render(decoded, photo.edits, cancelCheck)
                    edited = result
                    val scale = if (collage.layout.cellMode == CollageCellMode.FILL) {
                        maxOf(cell.width() / result.width, cell.height() / result.height)
                    } else {
                        minOf(cell.width() / result.width, cell.height() / result.height)
                    }
                    val shownW = result.width * scale
                    val shownH = result.height * scale
                    val dest = RectF(
                        cell.centerX() - shownW / 2f,
                        cell.centerY() - shownH / 2f,
                        cell.centerX() + shownW / 2f,
                        cell.centerY() + shownH / 2f
                    )
                    canvas.save()
                    try {
                        canvas.clipPath(clipPath)
                        canvas.drawBitmap(result, null, dest, Paint(Paint.FILTER_BITMAP_FLAG))
                    } finally {
                        canvas.restore()
                    }
                    if (stroke > 0f) {
                        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            style = Paint.Style.STROKE
                            strokeWidth = stroke
                            color = if (background == Color.BLACK) Color.WHITE else Color.BLACK
                            alpha = 128
                        }
                        canvas.drawPath(clipPath, line)
                    }
                } finally {
                    edited?.let { if (it !== source && !it.isRecycled) it.recycle() }
                    source?.let { if (!it.isRecycled) it.recycle() }
                }
            }
            return output
        } catch (failure: Throwable) {
            output.recycle()
            throw failure
        }
    }
}

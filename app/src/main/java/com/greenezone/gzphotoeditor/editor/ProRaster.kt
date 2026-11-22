package com.greenezone.gzphotoeditor.editor

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import kotlinx.coroutines.CancellationException
import kotlin.math.roundToInt

/** Own rendering pipeline for stacked, independently editable bitmap layers. */
object ProRaster {
    class MissingLayer(val position: Int, cause: Throwable) :
        IllegalStateException("Layer " + (position + 1) + " cannot access its source photo", cause)

    fun render(
        resolver: ContentResolver,
        document: ProDocument,
        width: Int, height: Int, preview: Boolean,
        cancel: (() -> Unit)? = null
    ): Bitmap = renderWithLoader(document, width, height, preview, cancel) { layer, edge, pixels ->
        val uri = Uri.parse(layer.uri)
        require(uri.scheme == "content")
        ImagePipeline.decode(resolver, uri, edge, pixels)
    }

    fun renderWithLoader(
        document: ProDocument,
        width: Int, height: Int, preview: Boolean,
        cancel: (() -> Unit)? = null,
        loader: (ProLayer, Int, Long) -> Bitmap
    ): Bitmap {
        require(ProMath.valid(document))
        require(width > 0 && height > 0 && width.toLong() * height <= 24_000_000L)
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        output.setHasAlpha(document.background == ProBackground.TRANSPARENT)
        val canvas = Canvas(output)
        canvas.drawColor(document.background.argb)
        try {
            for ((index, layer) in document.layers.withIndex()) {
                cancel?.invoke()
                if (!layer.visible || layer.opacity <= 0f) continue
                var source: Bitmap? = null
                var edited: Bitmap? = null
                try {
                    val edge = (maxOf(width, height) * layer.scale.coerceIn(.1f, 2f) * 1.3f)
                        .roundToInt().coerceIn(96, if (preview) 1400 else 5600)
                    val pixels = if (preview) 1_600_000L else
                        (width.toLong() * height * 2).coerceIn(250_000L, 12_000_000L)
                    source = try {
                        loader(layer, edge, pixels)
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Exception) {
                        throw MissingLayer(index, error)
                    }
                    edited = ImagePipeline.render(source, layer.edits, cancel)
                    val frame = ProMath.bounds(layer, width, height, edited.width, edited.height)
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                        alpha = (layer.opacity * 255).roundToInt().coerceIn(0, 255)
                        blendMode = when (layer.blend) {
                            ProBlend.NORMAL -> BlendMode.SRC_OVER
                            ProBlend.MULTIPLY -> BlendMode.MULTIPLY
                            ProBlend.SCREEN -> BlendMode.SCREEN
                            ProBlend.OVERLAY -> BlendMode.OVERLAY
                        }
                    }
                    canvas.drawBitmap(edited, null,
                        RectF(frame.left, frame.top, frame.right, frame.bottom), paint)
                } finally {
                    edited?.let { if (it !== source && !it.isRecycled) it.recycle() }
                    source?.let { if (!it.isRecycled) it.recycle() }
                }
            }
            return output
        } catch (problem: Throwable) {
            output.recycle()
            throw problem
        }
    }
}

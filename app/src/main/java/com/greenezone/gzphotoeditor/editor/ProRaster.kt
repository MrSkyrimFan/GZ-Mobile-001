package com.greenezone.gzphotoeditor.editor

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.graphics.Rect
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

    private fun blend(mode: ProBlend): BlendMode = when (mode) {
        ProBlend.NORMAL -> BlendMode.SRC_OVER
        ProBlend.MULTIPLY -> BlendMode.MULTIPLY
        ProBlend.SCREEN -> BlendMode.SCREEN
        ProBlend.OVERLAY -> BlendMode.OVERLAY
        ProBlend.SOFT_LIGHT -> BlendMode.SOFT_LIGHT
        ProBlend.HARD_LIGHT -> BlendMode.HARD_LIGHT
        ProBlend.DARKEN -> BlendMode.DARKEN
        ProBlend.LIGHTEN -> BlendMode.LIGHTEN
        ProBlend.COLOR_DODGE -> BlendMode.COLOR_DODGE
        ProBlend.COLOR_BURN -> BlendMode.COLOR_BURN
        ProBlend.DIFFERENCE -> BlendMode.DIFFERENCE
        ProBlend.EXCLUSION -> BlendMode.EXCLUSION
        ProBlend.HUE -> BlendMode.HUE
        ProBlend.SATURATION -> BlendMode.SATURATION
        ProBlend.COLOR -> BlendMode.COLOR
        ProBlend.LUMINOSITY -> BlendMode.LUMINOSITY
    }

    private fun clipMask(canvas: Canvas, layer: ProLayer) {
        val mask = layer.mask ?: return
        if (mask.feather > 0f) return
        val path = ProToolMath.projectedPath(mask, layer, canvas.width, canvas.height)
        if (mask.inverted) canvas.clipOutPath(path) else canvas.clipPath(path)
    }

    private fun featherMask(layer: ProLayer, width: Int, height: Int): Bitmap? {
        val mask = layer.mask ?: return null
        if (mask.feather == 0f) return null
        // Android draws ALPHA_8 bitmaps as paint coverage. DST_IN needs a color
        // image carrying source alpha; otherwise a soft cutout can stay opaque.
        // Adjustment layers only read mask samples and can use the smaller format.
        val format = if (layer.kind == ProLayerKind.IMAGE) Bitmap.Config.ARGB_8888 else Bitmap.Config.ALPHA_8
        val alpha = Bitmap.createBitmap(width, height, format)
        try {
            if (mask.inverted) alpha.eraseColor(Color.WHITE)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                if (mask.inverted) blendMode = BlendMode.DST_OUT
                maskFilter = BlurMaskFilter(ProToolMath.featherRadius(mask, layer, width, height), BlurMaskFilter.Blur.NORMAL)
            }
            Canvas(alpha).drawPath(ProToolMath.projectedPath(mask, layer, width, height), paint)
            return alpha
        } catch (e: Throwable) { alpha.recycle(); throw e }
    }

    private fun renderAdjustment(
        base: Bitmap, canvas: Canvas, layer: ProLayer, cancel: (() -> Unit)?
    ) {
        val graded = ImagePipeline.render(base, EditDocument(grade = layer.edits.grade), cancel)
        try {
            val adjusted = ProEffectRaster.render(graded, layer.effect, layer.effectAmount, cancel)
            try {
                if (adjusted === base) return
                val saved = canvas.save()
                try {
                    clipMask(canvas, layer)
                    if (!base.hasAlpha() && (layer.mask?.feather ?: 0f) == 0f) {
                        val paint = Paint().apply {
                            alpha = (layer.opacity * 255).roundToInt()
                            blendMode = blend(layer.blend)
                        }
                        canvas.drawBitmap(adjusted, null, Rect(0, 0, base.width, base.height), paint)
                    } else {
                        compositeKeepingAlpha(base, canvas, adjusted, layer, cancel)
                    }
                } finally { canvas.restoreToCount(saved) }
            } finally { if (adjusted !== graded) adjusted.recycle() }
        } finally { if (graded !== base) graded.recycle() }
    }

    // Blend the colors independently of coverage. Painting a filtered copy over its
    // original with SRC_OVER would increase alpha at every translucent edge.
    private fun compositeKeepingAlpha(
        base: Bitmap, canvas: Canvas, adjusted: Bitmap, layer: ProLayer, cancel: (() -> Unit)?
    ) {
        val width = base.width
        val old = IntArray(width)
        val changed = IntArray(width)
        val coverage = IntArray(width)
        val bottom = Bitmap.createBitmap(width, 1, Bitmap.Config.ARGB_8888)
        val top = Bitmap.createBitmap(width, 1, Bitmap.Config.ARGB_8888)
        val strip = Canvas(bottom)
        val rowRect = Rect(0, 0, width, 1)
        val dst = Rect()
        val mix = Paint().apply {
            alpha = (layer.opacity * 255).roundToInt()
            blendMode = blend(layer.blend)
        }
        val replace = Paint().apply { blendMode = BlendMode.SRC }
        val feather = featherMask(layer, width, base.height)
        val maskRow = if (feather == null) null else IntArray(width)
        try {
            val region = canvas.clipBounds
            for (y in region.top until region.bottom) {
                if ((y and 31) == 0) cancel?.invoke()
                base.getPixels(old, 0, width, 0, y, width, 1)
                adjusted.getPixels(changed, 0, width, 0, y, width, 1)
                if (feather != null && maskRow != null) feather.getPixels(maskRow, 0, width, 0, y, width, 1)
                for (x in old.indices) {
                    coverage[x] = old[x] and -0x1000000
                    old[x] = old[x] or -0x1000000
                    changed[x] = (changed[x] and 0xffffff) or ((maskRow?.get(x) ?: -1) and -0x1000000)
                }
                bottom.setPixels(old, 0, width, 0, 0, width, 1)
                top.setPixels(changed, 0, width, 0, 0, width, 1)
                strip.drawBitmap(top, null, rowRect, mix)
                bottom.getPixels(changed, 0, width, 0, 0, width, 1)
                for (x in changed.indices) changed[x] = coverage[x] or (changed[x] and 0xffffff)
                bottom.setPixels(changed, 0, width, 0, 0, width, 1)
                dst.set(0, y, width, y + 1)
                canvas.drawBitmap(bottom, null, dst, replace)
            }
        } finally { feather?.recycle(); bottom.recycle(); top.recycle() }
    }

    /**
     * A brush gesture changes only one drawing layer. Reuse immutable, already corrected
     * source images while the same document is live and re-composite in stack order.
     * The cache owns its images and is confined to one sequential preview job.
     */
    internal class BrushPreview(
        private val resolver: ContentResolver,
        private val document: ProDocument,
        private val selectedLayer: Int,
        private val width: Int,
        private val height: Int
    ) : AutoCloseable {
        private val images = PreparedImages(document.layers.count {
            it.kind == ProLayerKind.IMAGE && it.visible && it.opacity > 0f
        })

        fun render(stroke: InkStroke, cancel: () -> Unit): Bitmap =
            renderComposition(document, width, height, preview = true, cancel = cancel,
                images = images, draftLayer = selectedLayer, draftStroke = stroke) { layer, edge, pixels ->
                decodeSource(resolver, layer, edge, pixels)
            }

        override fun close() = images.close()
    }

    private class PreparedImages(layerCount: Int) : AutoCloseable {
        // Budget before decoding as well as when keeping the actual allocations.
        val pixelsPerSource = MAX_BRUSH_SOURCE_BYTES / 4L / layerCount.coerceAtLeast(1)
        private val retained = mutableMapOf<Int, Bitmap>()
        private var bytes = 0L

        operator fun get(index: Int): Bitmap? = retained[index]

        fun keep(index: Int, bitmap: Bitmap): Boolean {
            val size = bitmap.allocationByteCount.toLong()
            if (bytes + size > MAX_BRUSH_SOURCE_BYTES) return false
            retained[index] = bitmap
            bytes += size
            return true
        }

        override fun close() {
            retained.values.forEach { it.recycle() }
            retained.clear()
            bytes = 0L
        }
    }

    private const val MAX_BRUSH_SOURCE_BYTES = 8L * 1024 * 1024

    private fun decodeSource(
        resolver: ContentResolver, layer: ProLayer, edge: Int, pixels: Long
    ): Bitmap {
        val uri = Uri.parse(layer.uri)
        require(uri.scheme == "content")
        return ImagePipeline.decode(resolver, uri, edge, pixels)
    }

    fun render(
        resolver: ContentResolver,
        document: ProDocument,
        width: Int, height: Int, preview: Boolean,
        cancel: (() -> Unit)? = null
    ): Bitmap = renderWithLoader(document, width, height, preview, cancel) { layer, edge, pixels ->
        decodeSource(resolver, layer, edge, pixels)
    }

    fun renderWithLoader(
        document: ProDocument,
        width: Int, height: Int, preview: Boolean,
        cancel: (() -> Unit)? = null,
        loader: (ProLayer, Int, Long) -> Bitmap
    ): Bitmap = renderComposition(document, width, height, preview, cancel, loader = loader)

    private fun renderComposition(
        document: ProDocument,
        width: Int, height: Int, preview: Boolean,
        cancel: (() -> Unit)?,
        images: PreparedImages? = null,
        draftLayer: Int = -1,
        draftStroke: InkStroke? = null,
        loader: (ProLayer, Int, Long) -> Bitmap
    ): Bitmap {
        require(ProMath.valid(document))
        require(width > 0 && height > 0 && width.toLong() * height <= 24_000_000L)
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        output.setHasAlpha(document.background == ProBackground.TRANSPARENT)
        val canvas = Canvas(output)
        canvas.drawColor(document.background.argb)
        try {
            for ((index, committed) in document.layers.withIndex()) {
                cancel?.invoke()
                if (!committed.visible || committed.opacity <= 0f) continue
                val layer = if (index == draftLayer && draftStroke != null &&
                    committed.kind == ProLayerKind.DRAWING) committed.withBrushStroke(draftStroke)
                else committed
                if (layer.kind != ProLayerKind.IMAGE) {
                    if (layer.kind == ProLayerKind.ADJUSTMENT) {
                        renderAdjustment(output, canvas, layer, cancel)
                    } else {
                        val creative = layer.edits.creative
                        val overlay = when (layer.kind) {
                            ProLayerKind.TEXT -> CreativeDocument(text = creative.text.map {
                                it.copy(x = layer.x, y = layer.y,
                                    width = (it.width * layer.scale).coerceIn(.07f, 1f),
                                    height = (it.height * layer.scale).coerceIn(.04f, 1f),
                                    size = (it.size * layer.scale).coerceIn(.012f, .24f),
                                    rotation = layer.rotationDegrees)
                            })
                            ProLayerKind.SHAPE -> CreativeDocument(shapes = creative.shapes.map {
                                it.copy(x = layer.x, y = layer.y,
                                    width = (it.width * layer.scale).coerceIn(.018f, 1f),
                                    height = (it.height * layer.scale).coerceIn(.018f, 1f),
                                    rotation = layer.rotationDegrees)
                            })
                            ProLayerKind.DRAWING -> CreativeDocument(strokes = creative.strokes)
                            else -> CreativeDocument()
                        }
                        val save = canvas.saveLayer(
                            0f, 0f, width.toFloat(), height.toFloat(),
                            Paint().apply {
                                alpha = (layer.opacity * 255).roundToInt().coerceIn(0, 255)
                                blendMode = blend(layer.blend)
                            })
                        try {
                            CreativeRaster.drawIntoCanvas(canvas, overlay, width, height, cancel)
                        } finally { canvas.restoreToCount(save) }
                    }
                    continue
                }
                val prepared = images?.get(index)
                val outputImage = prepared ?: prepareImage(index, layer, width, height, preview,
                    images?.pixelsPerSource, cancel, loader)
                var retained = prepared != null
                try {
                    if (!retained) retained = images?.keep(index, outputImage) == true
                    val frame = ProMath.bounds(layer, width, height,
                        outputImage.width, outputImage.height)
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                        alpha = (layer.opacity * 255).roundToInt().coerceIn(0, 255)
                        blendMode = blend(layer.blend)
                    }
                    val feather = featherMask(layer, width, height)
                    val saved = if (feather == null) canvas.save() else
                        canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), paint)
                    try {
                        clipMask(canvas, layer)
                        val transform = canvas.save()
                        try {
                            if (layer.rotationDegrees != 0f) canvas.rotate(layer.rotationDegrees,
                                layer.x * width, layer.y * height)
                            canvas.drawBitmap(outputImage, null,
                                RectF(frame.left, frame.top, frame.right, frame.bottom),
                                if (feather == null) paint else Paint(Paint.FILTER_BITMAP_FLAG))
                        } finally { canvas.restoreToCount(transform) }
                        if (feather != null) {
                            canvas.drawBitmap(feather, null, Rect(0, 0, width, height),
                                Paint().apply { blendMode = BlendMode.DST_IN })
                        }
                    } finally {
                        canvas.restoreToCount(saved)
                        feather?.recycle()
                    }
                } finally {
                    if (!retained) outputImage.recycle()
                }
            }
            return output
        } catch (problem: Throwable) {
            output.recycle()
            throw problem
        }
    }

    private fun prepareImage(
        index: Int, layer: ProLayer, width: Int, height: Int, preview: Boolean,
        previewSourceLimit: Long?, cancel: (() -> Unit)?,
        loader: (ProLayer, Int, Long) -> Bitmap
    ): Bitmap {
        val edge = (maxOf(width, height) * layer.scale.coerceIn(.1f, 2f) * 1.3f)
            .roundToInt().coerceIn(96, if (preview) 2048 else 5600)
        val pixels = (width.toLong() * height * 2)
            .coerceIn(250_000L, if (preview) 3_000_000L else 12_000_000L)
        val source = try {
            loader(layer, edge, minOf(pixels, previewSourceLimit ?: pixels))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            throw MissingLayer(index, error)
        }
        var edited: Bitmap? = null
        var result: Bitmap? = null
        try {
            val corrected = ImagePipeline.render(source, layer.edits, cancel)
            edited = corrected
            val prepared = if (layer.retouches.isEmpty()) corrected
                else ProRetouchRaster.apply(corrected, layer.retouches, cancel)
            result = prepared
            return prepared
        } finally {
            if (source !== edited && source !== result) source.recycle()
            edited?.let { if (it !== result) it.recycle() }
        }
    }
}

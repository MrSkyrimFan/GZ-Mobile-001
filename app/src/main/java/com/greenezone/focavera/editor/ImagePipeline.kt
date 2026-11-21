package com.greenezone.focavera.editor

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ColorSpace
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Entirely local software sRGB pipeline. No input URI writes.
 * Call decode/render from a background dispatcher, not the UI thread.
 */
object ImagePipeline {
    const val PREVIEW_MAX_EDGE = 1400
    const val MAX_EXPORT_PIXELS = 24_000_000L

    fun decode(resolver: ContentResolver, uri: Uri, maxEdge: Int, maxPixels: Long): Bitmap {
        require(maxEdge > 0 && maxPixels > 0) { "Invalid decode budget" }
        val src = ImageDecoder.createSource(resolver, uri)
        return ImageDecoder.decodeBitmap(src) { decoder, info, _ ->
            val w = info.size.width.toLong()
            val h = info.size.height.toLong()
            require(w > 0 && h > 0) { "Invalid image dimensions." }
            val sideScale = minOf(1.0, maxEdge.toDouble() / maxOf(w, h))
            val pixelScale = minOf(1.0, sqrt(maxPixels.toDouble() / (w * h).toDouble()))
            val scale = minOf(sideScale, pixelScale)
            if (scale < 1.0) {
                decoder.setTargetSize(
                    maxOf(1, (w * scale).roundToInt()),
                    maxOf(1, (h * scale).roundToInt())
                )
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.setTargetColorSpace(ColorSpace.get(ColorSpace.Named.SRGB))
        }
    }

    fun centerCropSize(width: Int, height: Int, ratio: CropRatio): Pair<Int, Int> {
        require(width > 0 && height > 0)
        val wanted = ratio.ratio ?: return width to height
        val current = width.toFloat() / height
        return if (current > wanted) {
            (height * wanted).roundToInt().coerceIn(1, width) to height
        } else {
            width to (width / wanted).roundToInt().coerceIn(1, height)
        }
    }

    fun geometry(input: Bitmap, document: EditDocument): Bitmap {
        val turns = ((document.quarterTurns % 4) + 4) % 4
        val oriented = if (turns == 0 && !document.mirrored && !document.flipVertical) {
            input
        } else {
            val transform = Matrix()
            if (document.mirrored || document.flipVertical) {
                transform.postScale(
                    if (document.mirrored) -1f else 1f,
                    if (document.flipVertical) -1f else 1f,
                    input.width / 2f,
                    input.height / 2f
                )
            }
            if (turns != 0) transform.postRotate(turns * 90f, input.width / 2f, input.height / 2f)
            Bitmap.createBitmap(input, 0, 0, input.width, input.height, transform, true)
        }
        val (w, h) = centerCropSize(oriented.width, oriented.height, document.crop)
        return if (w == oriented.width && h == oriented.height) oriented
        else Bitmap.createBitmap(oriented, (oriented.width - w) / 2, (oriented.height - h) / 2, w, h)
    }

    /**
     * Four small scanline buffers instead of two full-frame IntArrays.
     * Large photo export peak memory is now primarily the input/output Bitmaps.
     */
    fun render(input: Bitmap, document: EditDocument): Bitmap {
        val source = geometry(input, document)
        val prepared = ToneMath.prepare(document.grade)
        if (prepared.isNeutral) return source

        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        var above = IntArray(width)
        var current = IntArray(width)
        var below = IntArray(width)
        val rendered = IntArray(width)

        source.getPixels(current, 0, width, 0, 0, width, 1)
        current.copyInto(above)
        if (height > 1) source.getPixels(below, 0, width, 0, 1, width, 1)
        else current.copyInto(below)

        val hasDetail = document.grade.clarity != 0f || document.grade.sharpen != 0f
        for (y in 0 until height) {
            for (x in 0 until width) {
                var delta = 0f
                if (hasDetail) {
                    val here = luminance(current[x])
                    val left = luminance(current[maxOf(0, x - 1)])
                    val right = luminance(current[minOf(width - 1, x + 1)])
                    val up = luminance(above[x])
                    val down = luminance(below[x])
                    delta = (here - (left + right + up + down) * .25f).coerceIn(-1f, 1f)
                }
                rendered[x] = ToneMath.adjustPrepared(current[x], prepared, x, y, width, height, delta)
            }
            output.setPixels(rendered, 0, width, 0, y, width, 1)
            if (y + 1 < height) {
                val reusable = above
                above = current
                current = below
                below = reusable
                source.getPixels(below, 0, width, 0, minOf(y + 2, height - 1), width, 1)
            }
        }
        return output
    }

    /** Downsampled histogram; uses a bounded number of samples and no frame-sized arrays. */
    fun luminanceHistogram(image: Bitmap, buckets: Int = 64): IntArray {
        val counts = IntArray(buckets)
        val total = image.width.toLong() * image.height.toLong()
        val stride = maxOf(1, sqrt(total / 12000.0).roundToInt())
        for (y in 0 until image.height step stride) {
            for (x in 0 until image.width step stride) {
                val bucket = (luminance(image.getPixel(x, y)) * (buckets - 1)).roundToInt()
                    .coerceIn(0, buckets - 1)
                counts[bucket]++
            }
        }
        return counts
    }

    private fun luminance(c: Int): Float =
        (.2126f * ((c ushr 16) and 255) + .7152f * ((c ushr 8) and 255) + .0722f * (c and 255)) / 255f
}

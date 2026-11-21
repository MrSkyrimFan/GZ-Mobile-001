package com.greenezone.chromavale.editor

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.ColorSpace
import android.net.Uri
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Never writes to the source URI. All bitmap work is CPU-local and synchronous.
 * The Activity runs it on Dispatchers.Default/IO rather than the main thread.
 */
object ImagePipeline {
    const val PREVIEW_MAX_EDGE = 1400
    const val MAX_EXPORT_PIXELS = 24_000_000L

    fun decode(resolver: ContentResolver, uri: Uri, maxEdge: Int, maxPixels: Long): Bitmap {
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
        val oriented = if (turns == 0 && !document.mirrored) {
            input
        } else {
            val transform = Matrix()
            if (document.mirrored) transform.postScale(-1f, 1f, input.width / 2f, input.height / 2f)
            if (turns != 0) transform.postRotate(turns * 90f, input.width / 2f, input.height / 2f)
            Bitmap.createBitmap(input, 0, 0, input.width, input.height, transform, true)
        }
        val (w, h) = centerCropSize(oriented.width, oriented.height, document.crop)
        return if (w == oriented.width && h == oriented.height) oriented
        else Bitmap.createBitmap(oriented, (oriented.width - w) / 2, (oriented.height - h) / 2, w, h)
    }

    fun render(input: Bitmap, document: EditDocument): Bitmap {
        val source = geometry(input, document)
        val grade = document.grade
        if (grade == Grade()) return source
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        val output = IntArray(pixels.size)
        val hasDetail = grade.clarity != 0f || grade.sharpen != 0f
        for (y in 0 until height) {
            for (x in 0 until width) {
                val i = y * width + x
                var delta = 0f
                if (hasDetail) {
                    val current = luminance(pixels[i])
                    val left = luminance(pixels[y * width + maxOf(0, x - 1)])
                    val right = luminance(pixels[y * width + minOf(width - 1, x + 1)])
                    val up = luminance(pixels[maxOf(0, y - 1) * width + x])
                    val down = luminance(pixels[minOf(height - 1, y + 1) * width + x])
                    delta = (current - (left + right + up + down) * .25f).coerceIn(-1f, 1f)
                }
                output[i] = ToneMath.adjust(pixels[i], grade, x, y, width, height, delta)
            }
        }
        return Bitmap.createBitmap(output, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun luminance(c: Int): Float =
        (.2126f * ((c ushr 16) and 255) + .7152f * ((c ushr 8) and 255) + .0722f * (c and 255)) / 255f
}

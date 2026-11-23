package com.greenezone.gzphotoeditor.editor

import android.graphics.Bitmap

internal class PhotoHistogram private constructor(
    val luminance: IntArray,
    val red: IntArray,
    val green: IntArray,
    val blue: IntArray,
    val blackPixels: Int,
    val whitePixels: Int,
    val visiblePixels: Int
) {
    companion object {
        /** Counts every visible pixel of the supplied sRGB preview in one pass. */
        fun fromPreview(image: Bitmap, checkActive: () -> Unit): PhotoHistogram {
            val luminance = IntArray(256)
            val red = IntArray(256)
            val green = IntArray(256)
            val blue = IntArray(256)
            var blackPixels = 0
            var whitePixels = 0
            var visiblePixels = 0
            val width = image.width
            val rows = (16_384 / width).coerceIn(1, 32)
            val pixels = IntArray(width * rows)
            for (top in 0 until image.height step rows) {
                checkActive()
                val height = minOf(rows, image.height - top)
                image.getPixels(pixels, 0, width, 0, top, width, height)
                for (i in 0 until width * height) {
                    val pixel = pixels[i]
                    if (pixel ushr 24 == 0) continue
                    val r = (pixel ushr 16) and 255
                    val g = (pixel ushr 8) and 255
                    val b = pixel and 255
                    red[r]++
                    green[g]++
                    blue[b]++
                    // Rec. 709 luma weights applied to the displayed sRGB values.
                    luminance[(2126 * r + 7152 * g + 722 * b + 5000) / 10_000]++
                    if (r == 0 && g == 0 && b == 0) blackPixels++
                    if (r == 255 && g == 255 && b == 255) whitePixels++
                    visiblePixels++
                }
            }
            return PhotoHistogram(luminance, red, green, blue, blackPixels, whitePixels, visiblePixels)
        }
    }
}

package com.greenezone.gzphotoeditor.editor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.os.Build
import java.io.IOException
import java.io.OutputStream
import kotlin.math.roundToInt

object ExportSizing {
    fun layout(source: Bitmap, settings: ExportSettings): Bitmap {
        val preset = settings.preset
        if (preset == SocialPreset.ORIGINAL) return source
        val width = preset.width
        val height = preset.height
        require(width in 1..5000 && height in 1..5000)
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        output.setHasAlpha(settings.background == ExportBackground.TRANSPARENT)
        val canvas = Canvas(output)
        if (settings.background != ExportBackground.TRANSPARENT) {
            canvas.drawColor(settings.background.color)
        }
        val imageRatio = source.width.toFloat() / source.height
        val frameRatio = width.toFloat() / height
        val factor = if (settings.placement == ExportPlacement.FIT) {
            if (imageRatio > frameRatio) width.toFloat() / source.width
            else height.toFloat() / source.height
        } else {
            if (imageRatio < frameRatio) width.toFloat() / source.width
            else height.toFloat() / source.height
        }
        val displayedW = source.width * factor
        val displayedH = source.height * factor
        val dest = RectF((width - displayedW) / 2f, (height - displayedH) / 2f,
            (width + displayedW) / 2f, (height + displayedH) / 2f)
        canvas.drawBitmap(source, null, dest, Paint(Paint.FILTER_BITMAP_FLAG))
        return output
    }
}

/** The MIME type and extension match the file bytes. No fake formats or remote SDKs. */
object ImageExportWriter {
    fun write(bitmap: Bitmap, settings: ExportSettings, output: OutputStream): Boolean {
        val quality = settings.quality.coerceIn(1, 100)
        return when (settings.format) {
            ImageExportFormat.JPEG -> {
                val opaque = if (bitmap.hasAlpha()) ImagePipeline.compositeWhite(bitmap) else bitmap
                try { opaque.compress(Bitmap.CompressFormat.JPEG, quality, output) }
                finally { if (opaque !== bitmap) opaque.recycle() }
            }
            ImageExportFormat.PNG -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            ImageExportFormat.WEBP -> if (Build.VERSION.SDK_INT >= 30) {
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, output)
            } else {
                @Suppress("DEPRECATION")
                bitmap.compress(Bitmap.CompressFormat.WEBP, quality.coerceAtMost(99), output)
            }
            ImageExportFormat.WEBP_LOSSLESS -> if (Build.VERSION.SDK_INT >= 30) {
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, quality, output)
            } else {
                @Suppress("DEPRECATION")
                bitmap.compress(Bitmap.CompressFormat.WEBP, 100, output)
            }
            ImageExportFormat.BMP -> { writeBmp(bitmap, output); true }
            ImageExportFormat.TIFF -> { writeTiff(bitmap, output); true }
            ImageExportFormat.PDF -> { writePdf(bitmap, output); true }
        }
    }

    private fun OutputStream.u16(v: Int) {
        write(v and 255)
        write((v ushr 8) and 255)
    }
    private fun OutputStream.u32(v: Int) {
        u16(v and 65535)
        u16(v ushr 16)
    }
    private fun rgbOpaque(c: Int): Int {
        val a = (c ushr 24) and 255
        if (a >= 255) return c or -0x1000000
        val inverse = 255 - a
        val r = (((c ushr 16) and 255) * a + 255 * inverse + 127) / 255
        val g = (((c ushr 8) and 255) * a + 255 * inverse + 127) / 255
        val b = ((c and 255) * a + 255 * inverse + 127) / 255
        return -0x1000000 or (r shl 16) or (g shl 8) or b
    }
    private fun writeBmp(bitmap: Bitmap, out: OutputStream) {
        val w = bitmap.width
        val h = bitmap.height
        val rowBytes = ((w * 3 + 3) / 4) * 4
        val size = 54 + rowBytes * h
        if (w <= 0 || h <= 0 || size < 54) throw IOException("BMP size is invalid")
        out.write(0x42); out.write(0x4D)
        out.u32(size); out.u32(0); out.u32(54)
        out.u32(40); out.u32(w); out.u32(h)
        out.u16(1); out.u16(24)
        out.u32(0); out.u32(rowBytes * h)
        out.u32(2835); out.u32(2835); out.u32(0); out.u32(0)
        val pixels = IntArray(w)
        val row = ByteArray(rowBytes)
        for (y in h - 1 downTo 0) {
            bitmap.getPixels(pixels, 0, w, 0, y, w, 1)
            row.fill(0)
            for (x in 0 until w) {
                val c = rgbOpaque(pixels[x])
                row[x * 3] = (c and 255).toByte()
                row[x * 3 + 1] = ((c ushr 8) and 255).toByte()
                row[x * 3 + 2] = ((c ushr 16) and 255).toByte()
            }
            out.write(row)
        }
    }

    private fun writeTiff(bitmap: Bitmap, out: OutputStream) {
        val w = bitmap.width
        val h = bitmap.height
        require(w > 0 && h > 0 && w.toLong() * h <= 30_000_000)
        // Baseline TIFF: RGB, 8 bits/channel, one uncompressed strip, little endian.
        val tags = 14
        val extra = 8 + 2 + tags * 12 + 4
        val bitsOffset = extra
        val xResOffset = bitsOffset + 6
        val yResOffset = xResOffset + 8
        val pixelsOffset = yResOffset + 8
        val byteCount = w * h * 3
        out.write('I'.code); out.write('I'.code)
        out.u16(42); out.u32(8)
        out.u16(tags)
        fun tag(id: Int, type: Int, count: Int, value: Int) {
            out.u16(id); out.u16(type); out.u32(count); out.u32(value)
        }
        tag(256, 4, 1, w)
        tag(257, 4, 1, h)
        tag(258, 3, 3, bitsOffset)
        tag(259, 3, 1, 1)
        tag(262, 3, 1, 2)
        tag(273, 4, 1, pixelsOffset)
        tag(274, 3, 1, 1)
        tag(277, 3, 1, 3)
        tag(278, 4, 1, h)
        tag(279, 4, 1, byteCount)
        tag(282, 5, 1, xResOffset)
        tag(283, 5, 1, yResOffset)
        tag(284, 3, 1, 1)
        tag(296, 3, 1, 2)
        out.u32(0)
        out.u16(8); out.u16(8); out.u16(8)
        out.u32(72); out.u32(1)
        out.u32(72); out.u32(1)
        val pixels = IntArray(w)
        val row = ByteArray(w * 3)
        for (y in 0 until h) {
            bitmap.getPixels(pixels, 0, w, 0, y, w, 1)
            for (x in 0 until w) {
                val c = rgbOpaque(pixels[x])
                row[x * 3] = ((c ushr 16) and 255).toByte()
                row[x * 3 + 1] = ((c ushr 8) and 255).toByte()
                row[x * 3 + 2] = (c and 255).toByte()
            }
            out.write(row)
        }
    }

    private fun writePdf(bitmap: Bitmap, output: OutputStream) {
        val maxSide = 1440f
        val factor = minOf(1f, maxSide / maxOf(bitmap.width, bitmap.height))
        val w = (bitmap.width * factor).roundToInt().coerceAtLeast(1)
        val h = (bitmap.height * factor).roundToInt().coerceAtLeast(1)
        val doc = PdfDocument()
        try {
            val page = doc.startPage(PdfDocument.PageInfo.Builder(w, h, 1).create())
            page.canvas.drawColor(Color.WHITE)
            page.canvas.drawBitmap(bitmap, null, Rect(0, 0, w, h),
                Paint(Paint.FILTER_BITMAP_FLAG))
            doc.finishPage(page)
            doc.writeTo(output)
        } finally {
            doc.close()
        }
    }
}

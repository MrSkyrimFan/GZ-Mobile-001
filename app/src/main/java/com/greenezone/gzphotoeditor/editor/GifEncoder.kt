package com.greenezone.gzphotoeditor.editor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * Independent GIF89a writer with an original 256-color 3-3-2 global palette and LZW
 * compression. Entire GIF is streamed: one source bitmap and one output frame at a time.
 * No external codec, runtime AI, ads or network access.
 */
object GifEncoder {
    private fun OutputStream.byte(value: Int) { write(value and 255) }
    private fun OutputStream.short(value: Int) { byte(value); byte(value shr 8) }
    private fun OutputStream.ascii(value: String) { write(value.toByteArray(Charsets.US_ASCII)) }

    /** 3 red bits / 3 green bits / 2 blue bits; each index maps to one global palette entry. */
    fun quantize(argb: Int): Int =
        ((argb shr 16 and 255) shr 5 shl 5) or
        (((argb shr 8 and 255) shr 5) shl 2) or
        ((argb and 255) shr 6)

    /** GIF's least-significant-bit-first, dynamic 9–12-bit dictionary stream. */
    internal fun lzw(data: ByteArray): ByteArray {
        require(data.isNotEmpty())
        val out = ByteArrayOutputStream()
        var bits = 0
        var used = 0
        var codeWidth = 9
        var nextCode = 258
        val table = HashMap<Int, Int>(4096)
        fun code(n: Int) {
            bits = bits or (n shl used)
            used += codeWidth
            while (used >= 8) {
                out.write(bits and 255)
                bits = bits ushr 8
                used -= 8
            }
        }
        code(256) // Clear table.
        var prefix = data[0].toInt() and 255
        for (i in 1 until data.size) {
            val value = data[i].toInt() and 255
            val joined = (prefix shl 8) or value
            val found = table[joined]
            if (found != null) {
                prefix = found
            } else {
                code(prefix)
                if (nextCode < 4096) {
                    table[joined] = nextCode++
                    // Encoder adds an entry one code earlier than the GIF decoder can see it.
                    // Changing width at exactly 2^codeWidth corrupts longer animations.
                    if (nextCode > (1 shl codeWidth) && codeWidth < 12) codeWidth++
                } else {
                    code(256)
                    table.clear()
                    nextCode = 258
                    codeWidth = 9
                }
                prefix = value
            }
        }
        code(prefix)
        code(257) // End of image.
        if (used > 0) out.write(bits and 255)
        return out.toByteArray()
    }

    private fun writeBlocks(output: OutputStream, bytes: ByteArray) {
        var index = 0
        while (index < bytes.size) {
            val count = min(255, bytes.size - index)
            output.byte(count)
            output.write(bytes, index, count)
            index += count
        }
        output.byte(0)
    }

    /** Caller owns output stream; loader transfers ownership of each temporary bitmap. */
    fun write(
        output: OutputStream,
        doc: GifDocument,
        cancel: (() -> Unit)? = null,
        loader: (position: Int) -> Bitmap
    ) {
        require(GifMath.valid(doc))
        val (w, h) = GifMath.dimensions(doc)
        require(w > 0 && h > 0 && w.toLong() * h <= 640L * 640L)
        output.ascii("GIF89a")
        output.short(w)
        output.short(h)
        output.byte(0xF7)  // 8-bit indexed color, global palette of 256.
        output.byte(0)     // Background palette index.
        output.byte(0)     // Pixel aspect ratio.
        for (idx in 0..255) {
            output.byte(((idx shr 5) and 7) * 255 / 7)
            output.byte(((idx shr 2) and 7) * 255 / 7)
            output.byte((idx and 3) * 255 / 3)
        }
        // NETSCAPE2.0 loop extension: 0 = forever; 1 = one repeat (two plays).
        if (doc.forever) {
            output.byte(0x21); output.byte(0xFF); output.byte(11)
            output.ascii("NETSCAPE2.0")
            output.byte(3); output.byte(1); output.short(0); output.byte(0)
        }
        val line = IntArray(w)
        for (frameIndex in doc.frames.indices) {
            cancel?.invoke()
            var source: Bitmap? = null
            var target: Bitmap? = null
            try {
                source = loader(frameIndex)
                require(source.width > 0 && source.height > 0)
                target = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(target)
                canvas.drawColor(Color.BLACK)
                val scale = if (doc.fit == GifFit.FIT) {
                    min(w.toFloat() / source.width, h.toFloat() / source.height)
                } else max(w.toFloat() / source.width, h.toFloat() / source.height)
                val rw = source.width * scale
                val rh = source.height * scale
                canvas.drawBitmap(source, null,
                    RectF((w - rw) / 2f, (h - rh) / 2f, (w + rw) / 2f, (h + rh) / 2f),
                    Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                val pixels = ByteArray(w * h)
                for (y in 0 until h) {
                    if ((y and 31) == 0) cancel?.invoke()
                    target.getPixels(line, 0, w, 0, y, w, 1)
                    for (x in 0 until w) pixels[y * w + x] = quantize(line[x]).toByte()
                }
                // Graphic Control Extension: disposal 2 (clear canvas for next frame), delay in 1/100s.
                output.byte(0x21); output.byte(0xF9); output.byte(4)
                output.byte(0x08)
                output.short((doc.delayMs / 10).coerceIn(1, 65535))
                output.byte(0); output.byte(0)
                // Image Descriptor, no local color table.
                output.byte(0x2C)
                output.short(0); output.short(0)
                output.short(w); output.short(h)
                output.byte(0)
                output.byte(8) // LZW minimum code size for 256 colors.
                writeBlocks(output, lzw(pixels))
            } finally {
                target?.let { if (!it.isRecycled) it.recycle() }
                source?.let { if (!it.isRecycled) it.recycle() }
            }
        }
        output.byte(0x3B)
        output.flush()
    }
}

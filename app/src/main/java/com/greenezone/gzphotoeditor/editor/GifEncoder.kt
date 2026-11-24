package com.greenezone.gzphotoeditor.editor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Debug
import android.util.Log
import java.io.BufferedOutputStream
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.min

/** Aggregate wall/thread CPU time only; no source identifiers or per-frame records. */
internal class GifStageTiming {
    private var wallNanos = 0L
    private var cpuNanos = 0L
    private var cpuAvailable = true
    val wallMs get() = wallNanos / 1_000_000
    val cpuMs get() = if (cpuAvailable) cpuNanos / 1_000_000 else -1L

    fun <T> measure(block: () -> T): T {
        val wallStart = System.nanoTime()
        val cpuStart = Debug.threadCpuTimeNanos()
        try {
            return block()
        } finally {
            val cpuEnd = Debug.threadCpuTimeNanos()
            wallNanos += System.nanoTime() - wallStart
            if (cpuStart >= 0 && cpuEnd >= cpuStart) cpuNanos += cpuEnd - cpuStart
            else cpuAvailable = false
        }
    }
}

/** Streaming GIF89a writer with per-frame source-adaptive palettes. */
object GifEncoder {
    private const val MAX_DELAY_TICKS = 65535
    private val neutral = EditDocument()
    private fun OutputStream.byte(value: Int) { write(value and 255) }
    private fun OutputStream.short(value: Int) { byte(value); byte(value shr 8) }
    private fun OutputStream.ascii(value: String) { write(value.toByteArray(Charsets.US_ASCII)) }

    /** The dictionary and GIF data sub-block buffer are reused for the whole export. */
    private class LzwWriter(private val out: OutputStream, private val cancel: (() -> Unit)?) {
        private val keys = IntArray(8192)
        private val codes = IntArray(8192)
        private val block = ByteArray(255)
        private var length = 0
        private var bits = 0
        private var used = 0
        private var width = 9
        private fun emitByte(n: Int) {
            block[length++] = n.toByte()
            if (length == 255) flushBlock()
        }
        private fun flushBlock() {
            if (length > 0) {
                out.write(length)
                out.write(block, 0, length)
                length = 0
            }
        }
        private fun code(n: Int) {
            bits = bits or (n shl used)
            used += width
            while (used >= 8) {
                emitByte(bits and 255)
                bits = bits ushr 8
                used -= 8
            }
        }

        fun write(data: ByteArray) {
            bits = 0; used = 0; width = 9
            codes.fill(0)
            code(256)
            var next = 258
            var prefix = data[0].toInt() and 255
            for (i in 1 until data.size) {
                if ((i and 65535) == 0) cancel?.invoke()
                val value = data[i].toInt() and 255
                val joined = (prefix shl 8) or value
                var slot = (joined * -1640531527) ushr 19
                while (codes[slot] != 0 && keys[slot] != joined) slot = (slot + 1) and 8191
                if (codes[slot] != 0) prefix = codes[slot]
                else {
                    code(prefix)
                    if (next < 4096) {
                        keys[slot] = joined
                        codes[slot] = next++
                        // The encoder creates each entry one code before the GIF decoder.
                        if (next > (1 shl width) && width < 12) width++
                    } else {
                        code(256)
                        codes.fill(0)
                        next = 258; width = 9
                    }
                    prefix = value
                }
            }
            code(prefix)
            if (next == (1 shl width) && width < 12) width++
            code(257)
            if (used > 0) emitByte(bits and 255)
            flushBlock()
            out.write(0)
        }
    }

    /** One shared composition recipe for animated preview and final GIF frames. */
    fun renderFrame(source: Bitmap, doc: GifDocument, maxEdge: Int? = null,
                    cancel: (() -> Unit)? = null): Bitmap =
        renderFrame(source, doc, maxEdge, cancel, null)

    private fun renderFrame(
        source: Bitmap, doc: GifDocument, maxEdge: Int?, cancel: (() -> Unit)?,
        colors: ImagePipeline.GifColors?
    ): Bitmap {
        require(source.width > 0 && source.height > 0 && !source.isRecycled)
        val (w, h) = GifMath.dimensions(doc, maxEdge)
        require(w.toLong() * h <= 16_777_216L) { "Choose a smaller export size for this canvas" }
        val target = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        var edited: Bitmap? = null
        try {
            val input = if (doc.edits == neutral) source
                else ImagePipeline.render(source, doc.edits, colors, cancel)
            edited = input
            val canvas = Canvas(target)
            if (doc.video?.isGif != true) canvas.drawColor(Color.BLACK)
            val scale = if (doc.fit == GifFit.FIT) {
                min(w.toFloat() / input.width, h.toFloat() / input.height)
            } else max(w.toFloat() / input.width, h.toFloat() / input.height)
            val rw = input.width * scale
            val rh = input.height * scale
            canvas.drawBitmap(input, null,
                RectF((w - rw) / 2f, (h - rh) / 2f, (w + rw) / 2f, (h + rh) / 2f),
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
            return target
        } catch (failed: Throwable) {
            target.recycle()
            throw failed
        } finally {
            if (edited != null && edited !== source) edited.recycle()
        }
    }

    /** Caller owns output stream; loader transfers ownership of each temporary bitmap. */
    fun write(
        output: OutputStream,
        doc: GifDocument,
        cancel: (() -> Unit)? = null,
        loader: (position: Int) -> Bitmap
    ) {
        require(doc.video == null && GifMath.valid(doc))
        writeFrames(output, doc, doc.frames.size, doc.delayMs, cancel, loader = loader)
    }

    /** Stream frames from either a photo sequence or a timestamped native video decoder. */
    fun writeFrames(
        output: OutputStream,
        doc: GifDocument,
        frameCount: Int,
        frameDelayMs: Int,
        cancel: (() -> Unit)? = null,
        delayAt: (Int) -> Int = { frameDelayMs },
        loader: (position: Int) -> Bitmap
    ) {
        require(frameCount in 1..GifVideoMath.MAX_OUTPUT_FRAMES)
        val (w, h) = GifMath.dimensions(doc)
        require(w.toLong() * h <= 16_777_216L) { "Choose a smaller export size for this canvas" }
        val totalTiming = GifStageTiming()
        val loaderTiming = GifStageTiming()
        val renderTiming = GifStageTiming()
        val paletteTiming = GifStageTiming()
        val encodeTiming = GifStageTiming()
        val colors = totalTiming.measure {
            val colors = ImagePipeline.GifColors.forGrade(doc.edits.grade)
            val stream = BufferedOutputStream(output, 64 * 1024)
            val writer = LzwWriter(stream, cancel)
            val palette = GifPalette(w, cancel, reserveTransparency = doc.video?.isGif == true)
            val pixels = ByteArray(w * h)
            val table = ByteArray(256 * 3)
            encodeTiming.measure {
                stream.ascii("GIF89a")
                stream.short(w); stream.short(h)
                // A two-entry global table supplies the logical background for transparent frames.
                stream.byte(0xf0); stream.byte(0); stream.byte(0)
                stream.write(ByteArray(6))
                if (doc.loopCount >= 0) {
                    stream.byte(0x21); stream.byte(0xff); stream.byte(11)
                    stream.ascii("NETSCAPE2.0")
                    stream.byte(3); stream.byte(1); stream.short(doc.loopCount); stream.byte(0)
                }
            }
            for (i in 0 until frameCount) {
                cancel?.invoke()
                val source = loaderTiming.measure { loader(i) }
                var target: Bitmap? = null
                try {
                    // Photo sequences composite transparent input onto the same black canvas as preview.
                    val needsPhotoBackground = doc.video == null && source.hasAlpha()
                    val image = renderTiming.measure {
                        if (source.width == w && source.height == h && doc.edits == neutral && !needsPhotoBackground)
                            source else renderFrame(source, doc, maxEdge = null, cancel = cancel, colors = colors)
                    }
                    target = image
                    paletteTiming.measure {
                        palette.index(image, pixels)
                        palette.colors.forEachIndexed { idx, color ->
                            table[idx * 3] = (color ushr 16).toByte()
                            table[idx * 3 + 1] = (color ushr 8).toByte()
                            table[idx * 3 + 2] = color.toByte()
                        }
                    }
                    encodeTiming.measure {
                        var ticks = ((delayAt(i) + 5) / 10).coerceAtLeast(1)
                        while (ticks > 0) {
                            // Readers treat a one-tick hold as a fallback. Borrow a tick for the final block.
                            val hold = if (ticks == MAX_DELAY_TICKS + 1) MAX_DELAY_TICKS - 1
                                else min(MAX_DELAY_TICKS, ticks)
                            stream.byte(0x21); stream.byte(0xf9); stream.byte(4)
                            stream.byte(if (palette.transparent) 0x09 else 0x04)
                            stream.short(hold); stream.byte(0); stream.byte(0)
                            stream.byte(0x2c); stream.short(0); stream.short(0)
                            stream.short(w); stream.short(h); stream.byte(0x87)
                            stream.write(table); stream.byte(8)
                            writer.write(pixels)
                            ticks -= hold
                        }
                    }
                } finally {
                    if (target != null && target !== source) target.recycle()
                    source.recycle()
                }
            }
            encodeTiming.measure {
                stream.byte(0x3b)
                stream.flush()
            }
            colors
        }
        Log.d("GzGifEncodeStage",
            "frames=$frameCount canvas=${w}x$h loaderMs=${loaderTiming.wallMs} loaderCpuMs=${loaderTiming.cpuMs} " +
                "renderMs=${renderTiming.wallMs} renderCpuMs=${renderTiming.cpuMs} " +
                "paletteMs=${paletteTiming.wallMs} paletteCpuMs=${paletteTiming.cpuMs} " +
                "lzwOutputMs=${encodeTiming.wallMs} lzwOutputCpuMs=${encodeTiming.cpuMs} " +
                "colorCacheHits=${colors?.hits ?: 0} colorCacheMisses=${colors?.misses ?: 0} " +
                "totalMs=${totalTiming.wallMs} totalCpuMs=${totalTiming.cpuMs}")
    }
}

package com.greenezone.gzphotoeditor.editor

import android.graphics.Bitmap

/** Keep sparse colors exactly. For photographs, choose a local palette from the frame's
 * actual RGB distribution using weighted median cut, including the within-bin means. */
internal class GifPalette(width: Int, private val cancel: (() -> Unit)?,
                          private val reserveTransparency: Boolean) {
    val colors = IntArray(256)
    var transparent = reserveTransparency
        private set
    private val counts = IntArray(32768)
    private val red = IntArray(32768)
    private val green = IntArray(32768)
    private val blue = IntArray(32768)
    private val mapping = ByteArray(32768)
    private val keys = IntArray(1024) { -1 }
    private val indices = IntArray(keys.size)
    private val frequencies = IntArray(256)
    private val stripeRows = (32768 / width).coerceIn(1, 32)
    private val stripe = IntArray(stripeRows * width)
    private val populated = IntArray(counts.size)
    private val partition = IntArray(counts.size)
    private val bands = IntArray(32)

    private fun bin(c: Int) = (c ushr 9 and 31744) or (c ushr 6 and 992) or (c ushr 3 and 31)
    private fun slot(c: Int): Int {
        var i = (c * -1640531527) ushr 22
        while (keys[i] != c && keys[i] >= 0) i = (i + 1) and 1023
        return i
    }

    private inline fun eachPixel(bitmap: Bitmap, visit: (Int, Int) -> Unit) {
        var y = 0
        while (y < bitmap.height) {
            cancel?.invoke()
            val rows = minOf(stripeRows, bitmap.height - y)
            val width = bitmap.width
            bitmap.getPixels(stripe, 0, width, 0, y, width, rows)
            val start = y * width
            for (i in 0 until rows * width) visit(start + i, stripe[i])
            y += rows
        }
    }

    private fun addColor(color: Int, frequency: Int) {
        val b = bin(color)
        counts[b] += frequency
        red[b] += ((color ushr 16) and 7) * frequency
        green[b] += ((color ushr 8) and 7) * frequency
        blue[b] += (color and 7) * frequency
    }

    fun index(bitmap: Bitmap, pixels: ByteArray) {
        transparent = reserveTransparency
        keys.fill(-1)
        var exactCount = 0
        var dense = false
        fun startHistogram() {
            counts.fill(0)
            red.fill(0)
            green.fill(0)
            blue.fill(0)
            for (i in 0 until exactCount) addColor(colors[(i + 1) and 255], frequencies[i])
            dense = true
        }
        eachPixel(bitmap) { position, pixel ->
            if (pixel ushr 24 < 128) {
                transparent = true
                pixels[position] = 0
                if (!dense && exactCount == 256) startHistogram()
            } else {
                val color = pixel and 0xffffff
                if (!dense) {
                    val s = slot(color)
                    if (keys[s] < 0 && exactCount == (if (transparent) 255 else 256)) {
                        startHistogram()
                    } else {
                        if (keys[s] < 0) {
                            keys[s] = color
                            indices[s] = exactCount
                            frequencies[exactCount] = 0
                            // Reserve index zero until all pixels have been checked for alpha.
                            // With 256 opaque colors the last color can use index zero.
                            colors[(exactCount + 1) and 255] = color
                            exactCount++
                        }
                        val ordinal = indices[s]
                        frequencies[ordinal]++
                        pixels[position] = (ordinal + 1).toByte()
                    }
                }
                if (dense) addColor(color, 1)
            }
        }
        if (!dense) return
        build(if (transparent) 1 else 0)
        eachPixel(bitmap) { position, pixel ->
            pixels[position] = if (pixel ushr 24 < 128) 0 else mapping[bin(pixel)]
        }
    }

    private data class Box(val start: Int, val end: Int, val axis: Int, val score: Long)
    private fun box(start: Int, end: Int): Box {
        var r0 = 31; var r1 = 0
        var g0 = 31; var g1 = 0
        var b0 = 31; var b1 = 0
        var weight = 0
        for (i in start until end) {
            val b = populated[i]
            val r = b ushr 10; val g = b ushr 5 and 31; val z = b and 31
            r0 = minOf(r0,r); r1 = maxOf(r1,r)
            g0 = minOf(g0,g); g1 = maxOf(g1,g)
            b0 = minOf(b0,z); b1 = maxOf(b1,z)
            weight += counts[b]
        }
        val range = maxOf(r1 - r0, g1 - g0, b1 - b0)
        val axis = when (range) { g1 - g0 -> 5; r1 - r0 -> 10; else -> 0 }
        return Box(start, end, axis, weight.toLong() * range * range)
    }

    private fun build(first: Int) {
        val boxes = java.util.PriorityQueue<Box>(compareByDescending { it.score })
        var populatedCount = 0
        for (i in counts.indices) if (counts[i] != 0) populated[populatedCount++] = i
        boxes.add(box(0, populatedCount))
        while (boxes.size < 256 - first && boxes.peek()!!.score > 0) {
            cancel?.invoke()
            val parent = boxes.remove()
            bands.fill(0)
            for (i in parent.start until parent.end) {
                val bin = populated[i]
                bands[bin ushr parent.axis and 31] += counts[bin]
            }
            val half = (bands.sum() + 1) / 2
            val last = bands.indexOfLast { it > 0 }
            var boundary = bands.indexOfFirst { it > 0 }
            var total = bands[boundary]
            while (total < half && boundary < last - 1) total += bands[++boundary]
            var left = parent.start
            var other = 0
            for (i in parent.start until parent.end) {
                val b = populated[i]
                if ((b ushr parent.axis and 31) <= boundary) populated[left++] = b
                else partition[other++] = b
            }
            val split = left
            partition.copyInto(populated, split, 0, other)
            boxes.add(box(parent.start, split))
            boxes.add(box(split, parent.end))
        }
        var index = first
        for (box in boxes) {
            var weight = 0; var rs = 0L; var gs = 0L; var bs = 0L
            for (i in box.start until box.end) {
                val b = populated[i]
                val count = counts[b].toLong()
                weight += counts[b]
                rs += (b ushr 10) * 8 * count + red[b]
                gs += (b ushr 5 and 31) * 8 * count + green[b]
                bs += (b and 31) * 8 * count + blue[b]
                mapping[b] = index.toByte()
            }
            fun average(sum: Long) = ((sum + weight / 2) / weight).toInt()
            colors[index++] = (average(rs) shl 16) or (average(gs) shl 8) or average(bs)
        }
    }
}

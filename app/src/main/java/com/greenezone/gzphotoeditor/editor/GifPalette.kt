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
    private val line = IntArray(width)
    private var exactCount = 0

    private fun bin(c: Int) = (c ushr 9 and 31744) or (c ushr 6 and 992) or (c ushr 3 and 31)
    private fun slot(c: Int): Int {
        var i = (c * -1640531527) ushr 22
        while (keys[i] != c && keys[i] >= 0) i = (i + 1) and 1023
        return i
    }

    private inline fun eachPixel(bitmap: Bitmap, visit: (Int, Int) -> Unit) {
        for (y in 0 until bitmap.height) {
            if (y % 32 == 0) cancel?.invoke()
            bitmap.getPixels(line, 0, bitmap.width, 0, y, bitmap.width, 1)
            for (x in line.indices) visit(y * bitmap.width + x, line[x])
        }
    }

    fun index(bitmap: Bitmap, pixels: ByteArray) {
        transparent = reserveTransparency
        exactCount = 0
        keys.fill(-1)
        counts.fill(0); red.fill(0); green.fill(0); blue.fill(0)
        eachPixel(bitmap) { _, pixel ->
            if (pixel ushr 24 < 128) transparent = true
            else {
                val c = pixel and 0xffffff
                if (exactCount <= 256) {
                    val i = slot(c)
                    if (keys[i] < 0) {
                        keys[i] = c
                        indices[i] = exactCount++
                    }
                }
                val b = bin(c)
                counts[b]++
                red[b] += c ushr 16 and 7
                green[b] += c ushr 8 and 7
                blue[b] += c and 7
            }
        }
        val first = if (transparent) 1 else 0
        val exact = exactCount <= 256 - first
        if (exact) {
            for (i in keys.indices) if (keys[i] >= 0) colors[first + indices[i]] = keys[i]
        } else build(first)
        eachPixel(bitmap) { position, pixel ->
            if (pixel ushr 24 >= 128) pixels[position] = if (exact)
                (indices[slot(pixel and 0xffffff)] + first).toByte() else mapping[bin(pixel)]
            else pixels[position] = 0
        }
    }

    private data class Box(val bins: IntArray, val axis: Int, val score: Long)
    private fun box(bins: IntArray): Box {
        var r0 = 31; var r1 = 0
        var g0 = 31; var g1 = 0
        var b0 = 31; var b1 = 0
        var weight = 0
        for (b in bins) {
            val r = b ushr 10; val g = b ushr 5 and 31; val z = b and 31
            r0 = minOf(r0,r); r1 = maxOf(r1,r)
            g0 = minOf(g0,g); g1 = maxOf(g1,g)
            b0 = minOf(b0,z); b1 = maxOf(b1,z)
            weight += counts[b]
        }
        val range = maxOf(r1 - r0, g1 - g0, b1 - b0)
        val axis = when (range) { g1 - g0 -> 5; r1 - r0 -> 10; else -> 0 }
        return Box(bins, axis, weight.toLong() * range * range)
    }

    private fun build(first: Int) {
        val boxes = java.util.PriorityQueue<Box>(compareByDescending { it.score })
        val populated = IntArray(counts.count { it > 0 })
        var n = 0
        for (i in counts.indices) if (counts[i] > 0) populated[n++] = i
        boxes.add(box(populated))
        while (boxes.size < 256 - first && boxes.peek()!!.score > 0) {
            val parent = boxes.remove()
            val bands = IntArray(32)
            for (b in parent.bins) bands[b ushr parent.axis and 31] += counts[b]
            val half = (bands.sum() + 1) / 2
            val last = bands.indexOfLast { it > 0 }
            var boundary = bands.indexOfFirst { it > 0 }
            var total = bands[boundary]
            while (total < half && boundary < last - 1) total += bands[++boundary]
            val left = IntArray(parent.bins.count { (it ushr parent.axis and 31) <= boundary })
            val right = IntArray(parent.bins.size - left.size)
            var l = 0; var r = 0
            for (b in parent.bins) {
                if ((b ushr parent.axis and 31) <= boundary) left[l++] = b else right[r++] = b
            }
            boxes.add(box(left)); boxes.add(box(right))
        }
        var index = first
        for (box in boxes) {
            var weight = 0; var rs = 0L; var gs = 0L; var bs = 0L
            for (b in box.bins) {
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

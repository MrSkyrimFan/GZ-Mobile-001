package com.greenezone.gzphotoeditor.editor

import java.io.DataInputStream
import java.io.InputStream

/** The editor uses the source timeline. Android's Movie decoder gets a private,
 * uniformly timed view of the same compressed frames so a timestamp identifies an
 * exact frame regardless of how a given device interprets tiny or missing delays. */
internal object GifSourceMetadata {
    private const val DECODE_HOLD_MS = 100
    fun decoderTime(index: Int) = index * DECODE_HOLD_MS + DECODE_HOLD_MS / 2

    private class Blocks(input: InputStream, private val inspect: Boolean) {
        private val stream = DataInputStream(input)
        val chunk = ByteArray(1024)
        val times = mutableListOf<Long>()
        var width = 0
        var height = 0
        var duration = 0L
        var repeat = -1
        private var frames = 0
        private var holdMs = DECODE_HOLD_MS
        private var hasControl = false

        private fun unsigned(n: Int) = chunk[n].toInt() and 255
        private fun word(n: Int) = unsigned(n) or (unsigned(n + 1) shl 8)
        private suspend fun SequenceScope<Int>.colorTable(flags: Int) {
            if (flags and 128 != 0) {
                val bytes = 3 * (2 shl (flags and 7))
                stream.readFully(chunk, 0, bytes)
                yield(bytes)
            }
        }
        private suspend fun SequenceScope<Int>.subBlocks(loop: Boolean = false) {
            do {
                val n = stream.readUnsignedByte()
                chunk[0] = n.toByte()
                stream.readFully(chunk, 1, n)
                if (loop && n == 3 && unsigned(1) == 1) repeat = word(2)
                yield(n + 1)
            } while (n != 0)
        }

        val parts = sequence {
            stream.readFully(chunk, 0, 13)
            require(String(chunk, 0, 6, Charsets.US_ASCII) in setOf("GIF87a", "GIF89a"))
            width = word(6); height = word(8)
            require(width > 0 && height > 0 && width.toLong() * height <= 16_777_216L) {
                "This GIF exceeds the decoder's 16-megapixel canvas"
            }
            val flags = unsigned(10)
            yield(13)
            colorTable(flags)
            while (true) {
                when (stream.readUnsignedByte()) {
                    0x21 -> {
                        val label = stream.readUnsignedByte()
                        val n = stream.readUnsignedByte()
                        chunk[0] = 0x21; chunk[1] = label.toByte(); chunk[2] = n.toByte()
                        stream.readFully(chunk, 3, n)
                        val loop = label == 0xff && n == 11 &&
                            String(chunk, 3, n, Charsets.US_ASCII) in setOf("NETSCAPE2.0", "ANIMEXTS1.0")
                        when (label) {
                            0xf9 -> {
                                require(n == 4) { "Invalid GIF frame controls" }
                                val ticks = word(4)
                                holdMs = if (ticks < 2) DECODE_HOLD_MS else ticks * 10
                                hasControl = true
                                chunk[4] = (DECODE_HOLD_MS / 10).toByte()
                                chunk[5] = 0
                            }
                            // A plain-text block consumes the previous graphics controls.
                            0x01 -> { hasControl = false; holdMs = DECODE_HOLD_MS }
                        }
                        yield(n + 3)
                        if (n != 0) subBlocks(loop)
                    }
                    0x2c -> {
                        if (!hasControl) {
                            byteArrayOf(0x21, 0xf9.toByte(), 4, 0, (DECODE_HOLD_MS / 10).toByte(),
                                0, 0, 0).copyInto(chunk)
                            yield(8)
                        }
                        chunk[0] = 0x2c
                        stream.readFully(chunk, 1, 9)
                        val local = unsigned(9)
                        yield(10)
                        colorTable(local)
                        stream.readFully(chunk, 0, 1)
                        yield(1)
                        subBlocks()
                        if (inspect) times.add(duration)
                        duration += holdMs
                        frames++
                        require(frames <= GifVideoMath.MAX_OUTPUT_FRAMES &&
                            duration <= GifVideoMath.MAX_CLIP_MS) { "GIF duration exceeds supported range" }
                        holdMs = DECODE_HOLD_MS
                        hasControl = false
                    }
                    0x3b -> {
                        require(frames > 0) { "The GIF has no image frames" }
                        chunk[0] = 0x3b
                        yield(1)
                        break
                    }
                    else -> error("Invalid GIF image block")
                }
            }
        }
    }

    fun read(input: InputStream, uri: String): GifVideoClip {
        val blocks = Blocks(input, inspect = true)
        for (part in blocks.parts) { /* Walk block headers without decompressing image pixels. */ }
        return GifVideoClip(uri, blocks.duration, width = blocks.width, height = blocks.height,
            sourceTimesMs = blocks.times, isGif = true, loopCount = blocks.repeat)
    }

    /** Reads directly from the document provider, with one small reusable block buffer. */
    fun indexedStream(input: InputStream): InputStream = object : InputStream() {
        private val blocks = Blocks(input, inspect = false)
        private val parts = blocks.parts.iterator()
        private var position = 0
        private var length = 0
        private fun availableBlock(): Boolean {
            if (position < length) return true
            if (!parts.hasNext()) return false
            length = parts.next()
            position = 0
            return true
        }
        override fun read(): Int = if (availableBlock()) blocks.chunk[position++].toInt() and 255 else -1
        override fun read(b: ByteArray, off: Int, len: Int): Int {
            var count = 0
            while (count < len && availableBlock()) {
                val n = minOf(len - count, length - position)
                blocks.chunk.copyInto(b, off + count, position, position + n)
                position += n
                count += n
            }
            return if (count == 0 && len > 0) -1 else count
        }
    }
}

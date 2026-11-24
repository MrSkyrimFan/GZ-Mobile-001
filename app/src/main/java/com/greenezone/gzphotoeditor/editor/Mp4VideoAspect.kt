package com.greenezone.gzphotoeditor.editor

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

/** Reads a VisualSampleEntry's pixel aspect on Android versions that omit MP4 pasp from MediaFormat. */
internal object Mp4VideoAspect {
    private data class Box(val type: String, val payload: Long, val end: Long)

    fun read(context: Context, uri: Uri): Pair<Double, Double>? {
        return try {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { asset ->
                val start = asset.startOffset
                val duplicate = ParcelFileDescriptor.dup(asset.fileDescriptor)
                ParcelFileDescriptor.AutoCloseInputStream(duplicate).use { stream ->
                    val source = stream.channel
                    val length = asset.declaredLength
                    val fileEnd = source.size()
                    val end = if (length in 0..(fileEnd - start)) start + length else fileEnd
                    aspect(source, start, end)
                }
            }
        } catch (_: IOException) { null }
        catch (_: SecurityException) { null }
    }

    private fun aspect(source: FileChannel, start: Long, end: Long): Pair<Double, Double>? {
        val movie = source.boxes(start, end).firstOrNull { it.type == "moov" } ?: return null
        val video = source.boxes(movie.payload, movie.end)
            .filter { it.type == "trak" }
            .mapNotNull { source.child(it, "mdia") }
            .firstOrNull { media ->
                source.child(media, "hdlr")?.let { handler ->
                    (handler.end - handler.payload >= 12) &&
                        source.bytes(handler.payload, 12)?.getInt(8) == 0x76696465 // vide
                } == true
            } ?: return null
        val mediaInfo = source.child(video, "minf") ?: return null
        val sampleTable = source.child(mediaInfo, "stbl") ?: return null
        val sampleDescriptions = source.child(sampleTable, "stsd") ?: return null
        if (sampleDescriptions.end - sampleDescriptions.payload < 8) return null
        // The first visual sample entry describes the track selected by MediaExtractor.
        val entry = source.boxes(sampleDescriptions.payload + 8, sampleDescriptions.end).firstOrNull() ?: return null
        if (entry.end - entry.payload < 78) return null
        val pasp = source.boxes(entry.payload + 78, entry.end).firstOrNull { it.type == "pasp" } ?: return null
        if (pasp.end - pasp.payload < 8) return null
        val spacing = source.bytes(pasp.payload, 8) ?: return null
        val horizontal = spacing.int.toLong() and 0xffff_ffffL
        val vertical = spacing.int.toLong() and 0xffff_ffffL
        return if (horizontal > 0 && vertical > 0) horizontal.toDouble() to vertical.toDouble() else null
    }

    private fun FileChannel.child(parent: Box, type: String): Box? =
        boxes(parent.payload, parent.end).firstOrNull { it.type == type }

    private fun FileChannel.boxes(start: Long, end: Long) = sequence {
        var position = start
        // Only inspect box headers and advance by declared payload size, including over mdat.
        repeat(4096) {
            if (end - position < 8) return@sequence
            val header = bytes(position, 8) ?: return@sequence
            val shortSize = header.int.toLong() and 0xffff_ffffL
            val type = String(header.array(), 4, 4, Charsets.US_ASCII)
            val headerSize = if (shortSize == 1L) 16 else 8
            val total = when (shortSize) {
                0L -> end - position
                1L -> if (end - position >= headerSize) bytes(position + 8, 8)?.long ?: return@sequence
                    else return@sequence
                else -> shortSize
            }
            if (total < headerSize || total > end - position) return@sequence
            yield(Box(type, position + headerSize, position + total))
            position += total
        }
    }

    private fun FileChannel.bytes(offset: Long, size: Int): ByteBuffer? {
        val target = ByteBuffer.allocate(size)
        while (target.hasRemaining()) {
            if (read(target, offset + target.position()) <= 0) return null
        }
        target.flip()
        return target
    }
}

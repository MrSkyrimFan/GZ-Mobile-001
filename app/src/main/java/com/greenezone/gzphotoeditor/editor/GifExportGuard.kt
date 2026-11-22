package com.greenezone.gzphotoeditor.editor

import java.io.FilterOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * Writes GIFs to private temporary storage with a hard ceiling, leaving
 * 64 MiB available for the operating system and other user operations.
 *
 * The ceiling is a device-storage safety bound, not an artificial quality
 * setting. Smaller files retain full requested dimensions and timing.
 */
object GifExportGuard {
    const val SAFETY_MARGIN_BYTES = 64L * 1024L * 1024L
    const val MIN_EXPORT_BYTES = 8L * 1024L * 1024L
    const val MAX_TEMP_BYTES = 2L * 1024L * 1024L * 1024L

    fun maxTemporaryBytes(usableSpace: Long): Long {
        require(usableSpace >= 0L)
        return (usableSpace - SAFETY_MARGIN_BYTES).coerceIn(0L, MAX_TEMP_BYTES)
    }

    fun limit(destination: OutputStream, maxBytes: Long): OutputStream {
        require(maxBytes >= 0)
        return object : FilterOutputStream(destination) {
            private var written = 0L
            private fun budget(bytes: Int) {
                if (bytes.toLong() > maxBytes - written) {
                    throw IOException(
                        "GIF would exceed available safe temporary storage. " +
                        "Reduce video duration, frame rate or dimensions and retry."
                    )
                }
            }
            override fun write(value: Int) {
                budget(1)
                out.write(value)
                written++
            }
            override fun write(bytes: ByteArray, offset: Int, length: Int) {
                if (offset < 0 || length < 0 || offset > bytes.size - length) {
                    throw IndexOutOfBoundsException()
                }
                budget(length)
                out.write(bytes, offset, length)
                written += length
            }
        }
    }
}

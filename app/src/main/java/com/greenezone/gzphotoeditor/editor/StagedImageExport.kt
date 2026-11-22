package com.greenezone.gzphotoeditor.editor

import java.io.File
import java.io.IOException
import java.io.OutputStream

/** Finish encoding locally before asking a document provider for a truncating write stream. */
internal fun writeStagedImage(
    cacheDirectory: File,
    openDestination: () -> OutputStream?,
    checkActive: () -> Unit = {},
    encode: (OutputStream) -> Boolean
) {
    val staged = File.createTempFile("gz-image-", ".tmp", cacheDirectory)
    try {
        checkActive()
        val encoded = staged.outputStream().buffered().use(encode)
        if (!encoded) throw IOException("Image encoder returned no complete image")
        copyStagedImage(staged, openDestination, checkActive)
    } finally {
        staged.delete()
    }
}

/** SAF providers may expose a write-only document. Only caller-owned staging files can be deleted safely. */
internal fun copyStagedImage(
    staged: File,
    openDestination: () -> OutputStream?,
    checkActive: () -> Unit
) {
    staged.inputStream().use { input ->
        val buffer = ByteArray(65_536)
        var count = input.read(buffer)
        if (count < 0) throw IOException("The staged image is empty")
        checkActive()
        // Opening "wt" can truncate an existing document. Finish this copy even if the
        // caller is cancelled after that point. A write-only provider cannot roll it back.
        val destination = openDestination() ?: throw IOException("Export destination unavailable")
        destination.use { output ->
            while (count >= 0) {
                output.write(buffer, 0, count)
                count = input.read(buffer)
            }
        }
    }
}

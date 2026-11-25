package com.greenezone.gzphotoeditor.editor

import android.content.Context
import android.util.AtomicFile
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.FilterInputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.Base64
import java.util.Locale
import java.util.UUID

enum class ProjectKind { PHOTO, COLLAGE, PRO, GIF }

/** Identity is independent of the display name; version is exact stored-byte custody. */
data class NamedProjectRef(
    val kind: ProjectKind,
    val id: String,
    val name: String,
    val version: String
)

sealed interface ProjectSaveIntent {
    data class Create(val name: String) : ProjectSaveIntent
    data class Update(val expected: NamedProjectRef) : ProjectSaveIntent
    data class Replace(val expected: NamedProjectRef) : ProjectSaveIntent
}

sealed interface ProjectSaveResult {
    data class Saved(val project: NamedProjectRef) : ProjectSaveResult
    data class Exists(val project: NamedProjectRef) : ProjectSaveResult
    data class Rejected(val reason: ProjectSaveFailure) : ProjectSaveResult
}

enum class ProjectSaveFailure {
    INVALID_NAME, INVALID_DOCUMENT, FULL, TOO_LARGE, TARGET_CHANGED, STORAGE_FAILURE
}

data class NamedProject<T>(val project: NamedProjectRef, val content: T)

internal val ProjectSaveIntent.requestedName: String
    get() = when (this) {
        is ProjectSaveIntent.Create -> name
        is ProjectSaveIntent.Update -> expected.name
        is ProjectSaveIntent.Replace -> expected.name
    }

internal val ProjectSaveIntent.expectedProject: NamedProjectRef?
    get() = when (this) {
        is ProjectSaveIntent.Create -> null
        is ProjectSaveIntent.Update -> expected
        is ProjectSaveIntent.Replace -> expected
    }

/**
 * B1 bookkeeping only: existing payload formats/quotas/names are unchanged.
 *
 * A UUID is reserved and read back BEFORE a payload is published. A reservation
 * does not establish an active UI target: only a verified payload save returns
 * Saved. Missing payloads always receive a new UUID, even if a failed write or
 * interrupted deletion left a sidecar. Existing payloads retain their UUID.
 *
 * Callers hold their store lock across identity, validation and payload IO.
 */
internal object NamedProjectIdentity {
    private fun file(context: Context, kind: ProjectKind, name: String): File {
        val directory = File(context.filesDir,
            "named-project-identities/" + kind.name.lowercase(Locale.ROOT))
        if (!directory.isDirectory && !directory.mkdirs())
            throw IOException("Project identity directory is unavailable")
        val key = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(name.toByteArray(Charsets.UTF_8))
        return File(directory, "$key.id")
    }

    private fun canonicalId(raw: String): String {
        return try {
            val id = UUID.fromString(raw).toString()
            if (id != raw) throw IOException("Invalid project identity")
            id
        } catch (failure: IllegalArgumentException) {
            throw IOException("Invalid project identity", failure)
        }
    }

    private fun readId(base: File): String {
        return AtomicFile(base).openRead().use { input ->
            if (input.channel.size() !in 1L..64L) throw IOException("Invalid project identity size")
            canonicalId(input.bufferedReader(Charsets.UTF_8).readText())
        }
    }

    fun reserve(context: Context, kind: ProjectKind, name: String, payloadExists: Boolean): String {
        val base = file(context, kind, name)
        if (payloadExists && (base.exists() || File(base.path + ".bak").exists())) return readId(base)
        val id = UUID.randomUUID().toString()
        val atomic = AtomicFile(base)
        val stream = atomic.startWrite()
        try {
            stream.write(id.toByteArray(Charsets.UTF_8))
            stream.fd.sync()
            atomic.finishWrite(stream)
        } catch (failure: Throwable) {
            atomic.failWrite(stream)
            throw failure
        }
        if (readId(base) != id) throw IOException("Project identity publication failed")
        return id
    }

    fun reference(context: Context, kind: ProjectKind, name: String, version: String): NamedProjectRef =
        NamedProjectRef(kind, reserve(context, kind, name, payloadExists = true), name, version)

    /** Existing legacy callers may delete by name; any later create still gets a new UUID. */
    fun forget(context: Context, kind: ProjectKind, name: String) {
        try { AtomicFile(file(context, kind, name)).delete() } catch (_: Exception) { }
    }

    fun authorize(kind: ProjectKind, intent: ProjectSaveIntent, current: NamedProjectRef?): ProjectSaveResult? =
        when (intent) {
            is ProjectSaveIntent.Create -> current?.let { ProjectSaveResult.Exists(it) }
            is ProjectSaveIntent.Update, is ProjectSaveIntent.Replace -> {
                val expected = intent.expectedProject!!
                if (expected.kind != kind || current == null || expected != current)
                    ProjectSaveResult.Rejected(ProjectSaveFailure.TARGET_CHANGED)
                else null
            }
        }

    fun version(bytes: ByteArray): String = hex(MessageDigest.getInstance("SHA-256").digest(bytes))

    private fun hex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it.toInt() and 255) }

    data class ReadPayload<T>(val content: T, val version: String)

    fun <T> readPayload(base: File, maxBytes: Long, parse: (InputStream, Long) -> T?): ReadPayload<T>? {
        recoverPublication(base, maxBytes)
        return readRawPayload(base, maxBytes, parse)
    }

    private fun <T> readRawPayload(base: File, maxBytes: Long,
        parse: (InputStream, Long) -> T?): ReadPayload<T>? {
        val backup = File(base.path + ".bak")
        val authoritative = if (backup.exists()) backup else base
        if (!authoritative.isFile || authoritative.length() !in 1L..maxBytes) return null
        return AtomicFile(base).openRead().use { input ->
            val length = input.channel.size()
            if (length !in 1L..maxBytes) return null
            val digest = MessageDigest.getInstance("SHA-256")
            val hashed = DigestInputStream(input, digest)
            val bounded = object : FilterInputStream(hashed) {
                private var remaining = length
                override fun read(): Int {
                    val next = hashed.read()
                    if (remaining == 0L) {
                        if (next != -1) throw IOException("Project grew while reading")
                        return -1
                    }
                    if (next == -1) throw IOException("Truncated project")
                    remaining--
                    return next
                }
                override fun read(buffer: ByteArray, offset: Int, count: Int): Int {
                    if (count == 0) return 0
                    if (remaining == 0L) return read()
                    val amount = hashed.read(buffer, offset, minOf(count.toLong(), remaining).toInt())
                    if (amount <= 0) throw IOException("Truncated project")
                    remaining -= amount
                    return amount
                }
                // The outer use owns the stream, even when a parser closes its reader.
                override fun close() = Unit
            }
            val content = parse(bounded, length) ?: return null
            // A parser may stop early. Complete the bounded digest without retaining bytes.
            val buffer = ByteArray(8192)
            while (bounded.read(buffer) >= 0) { }
            if (input.channel.size() != length) throw IOException("Project changed while reading")
            ReadPayload(content, hex(digest.digest()))
        }
    }

    fun fileVersion(base: File, maxBytes: Long): String? {
        recoverPublication(base, maxBytes)
        return rawFileVersion(base, maxBytes)
    }

    private fun rawFileVersion(base: File, maxBytes: Long): String? =
        readRawPayload(base, maxBytes) { input, length ->
            val buffer = ByteArray(8192)
            var remaining = length
            while (remaining > 0) {
                val count = input.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
                if (count <= 0) throw IOException("Truncated project")
                remaining -= count
            }
            if (input.read() != -1) throw IOException("Project grew while reading")
            true
        }?.version

    fun verify(base: File, bytes: ByteArray, maxBytes: Long) {
        if (rawFileVersion(base, maxBytes) != version(bytes))
            throw IOException("Project publication could not be verified")
    }

    private const val PENDING_SUFFIX = ".save-pending"
    private const val PREVIOUS_SUFFIX = ".save-previous"
    private data class Publication(val previous: String?, val next: String)

    fun hasPendingPublication(base: File): Boolean {
        val marker = File(base.path + PENDING_SUFFIX)
        return marker.exists() || File(marker.path + ".bak").exists()
    }

    private fun removeAtomic(base: File) {
        AtomicFile(base).delete()
        if (base.exists() || File(base.path + ".bak").exists() || File(base.path + ".new").exists())
            throw IOException("Could not remove project transaction metadata")
    }

    private fun writeAtomic(base: File, bytes: ByteArray) {
        val atomic = AtomicFile(base)
        val stream = atomic.startWrite()
        try {
            stream.write(bytes)
            stream.fd.sync()
            atomic.finishWrite(stream)
        } catch (failure: Throwable) {
            atomic.failWrite(stream)
            throw failure
        }
    }

    private fun writeMarker(base: File, publication: Publication) {
        val marker = File(base.path + PENDING_SUFFIX)
        val bytes = JSONObject().put("version", 1)
            .put("previous", publication.previous ?: "")
            .put("next", publication.next).toString().toByteArray(Charsets.UTF_8)
        writeAtomic(marker, bytes)
        verify(marker, bytes, 1024)
    }

    private fun readMarker(base: File): Publication {
        val marker = File(base.path + PENDING_SUFFIX)
        val loaded = readRawPayload(marker, 1024) { input, _ ->
            JSONObject(input.bufferedReader(Charsets.UTF_8).readText())
        }?.content ?: throw IOException("Project transaction marker is unavailable")
        if (loaded.optInt("version", -1) != 1) throw IOException("Unknown project transaction")
        val previous = loaded.getString("previous").takeIf { it.isNotEmpty() }
        val next = loaded.getString("next")
        val hash = Regex("[0-9a-f]{64}")
        if ((previous != null && !hash.matches(previous)) || !hash.matches(next))
            throw IOException("Invalid project transaction hashes")
        return Publication(previous, next)
    }

    private fun copyVerified(source: File, destination: File, maxBytes: Long): String {
        val expected = rawFileVersion(source, maxBytes)
            ?: throw IOException("Healthy project recovery bytes are unavailable")
        val atomic = AtomicFile(destination)
        val output = atomic.startWrite()
        try {
            val copied = readRawPayload(source, maxBytes) { input, _ ->
                val buffer = ByteArray(8192)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    output.write(buffer, 0, count)
                }
                true
            } ?: throw IOException("Could not read project recovery bytes")
            if (copied.version != expected) throw IOException("Project changed while preserving recovery")
            output.fd.sync()
            atomic.finishWrite(output)
        } catch (failure: Throwable) {
            atomic.failWrite(output)
            throw failure
        }
        if (rawFileVersion(destination, maxBytes) != expected)
            throw IOException("Project recovery copy could not be verified")
        return expected
    }

    private fun rollbackPublication(base: File, publication: Publication, maxBytes: Long) {
        val previous = File(base.path + PREVIOUS_SUFFIX)
        // Re-establish the marker if commit-marker removal failed ambiguously.
        // Loads must not expose the replacement until rollback is completed.
        if (!hasPendingPublication(base)) writeMarker(base, publication)
        if (publication.previous == null) {
            removeAtomic(base)
        } else {
            if (rawFileVersion(previous, maxBytes) != publication.previous)
                throw IOException("The previous healthy project is not available for recovery")
            val restored = copyVerified(previous, base, maxBytes)
            if (restored != publication.previous) throw IOException("Project rollback did not match")
        }
        removeAtomic(File(base.path + PENDING_SUFFIX))
        // After marker removal the restored old state is authoritative. Cleanup
        // failure cannot change that outcome or turn it into a failed publication.
        try { removeAtomic(previous) } catch (_: Exception) { }
    }

    /**
     * The existing native format stays at base. The bounded previous-byte copy
     * remains recoverable until the new bytes have passed validation AND the
     * durable pending marker has been removed. A failed validation restores old
     * bytes; an interrupted rollback remains marked and is retried before reads.
     *
     * verifyPublished is injectable only for a bounded publication-failure test.
     */
    fun publish(base: File, bytes: ByteArray, maxBytes: Long,
        verifyPublished: (File, ByteArray, Long) -> Unit = ::verify): String {
        if (bytes.isEmpty() || bytes.size.toLong() > maxBytes)
            throw IOException("Project exceeds its publication bound")
        recoverPublication(base, maxBytes)
        val previous = File(base.path + PREVIOUS_SUFFIX)
        val exists = base.exists() || File(base.path + ".bak").exists()
        val previousVersion = if (exists) copyVerified(base, previous, maxBytes) else {
            try { removeAtomic(previous) } catch (_: Exception) { }
            null
        }
        val publication = Publication(previousVersion, version(bytes))
        try {
            writeMarker(base, publication)
            writeAtomic(base, bytes)
            verifyPublished(base, bytes, maxBytes)
            // This is the success commit point. No fallible payload/identity
            // reads are performed after it to create an acknowledgement.
            removeAtomic(File(base.path + PENDING_SUFFIX))
        } catch (failure: Throwable) {
            try {
                rollbackPublication(base, publication, maxBytes)
            } catch (rollbackFailure: Throwable) {
                failure.addSuppressed(rollbackFailure)
                // Keep both the marker and verified previous copy for recovery.
            }
            throw failure
        }
        try { removeAtomic(previous) } catch (_: Exception) { }
        return publication.next
    }

    fun recoverPublication(base: File, maxBytes: Long) {
        if (hasPendingPublication(base)) {
            rollbackPublication(base, readMarker(base), maxBytes)
        } else {
            // A crash before marker publication never touched the native file.
            try { removeAtomic(File(base.path + PREVIOUS_SUFFIX)) } catch (_: Exception) { }
            try { removeAtomic(File(base.path + PENDING_SUFFIX)) } catch (_: Exception) { }
        }
    }

    /** Listing remains metadata-only; an unresolved record stays counted/listed. */
    fun recoverDirectory(directory: File, extension: String, maxBytes: Long) {
        directory.listFiles()?.mapNotNull { file ->
            val marker = file.name.removeSuffix(".bak")
            if (!marker.endsWith(extension + PENDING_SUFFIX)) null
            else File(directory, marker.removeSuffix(PENDING_SUFFIX))
        }?.distinctBy { it.path }?.forEach { base ->
            try { recoverPublication(base, maxBytes) } catch (_: Exception) { }
        }
    }
}

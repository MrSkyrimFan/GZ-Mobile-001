package com.greenezone.gzphotoeditor.editor

import android.content.Context
import android.net.Uri
import android.system.Os
import android.system.OsConstants
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

internal class NativeWorkLimit : IOException("Native work exceeds its storage bound")
internal class NativeWorkUnavailable : IOException("Native work bytes are unavailable")

internal class NativeWorkFiles(private val context: Context) {
    val root = File(context.filesDir, "native-work-v1")
    val catalog = File(root, "catalog.json")
    private val records = File(root, "records")
    private val manifests = File(root, "manifests")
    private val derivatives = File(root, "derivatives")

    fun manifest(sha: String) = owned(manifests, sha, ".json")
    fun record(sha: String) = owned(records, sha, ".raw")
    fun derivative(sha: String) = owned(derivatives, sha, ".png")

    fun derivativeUri(sha: String) =
        FileProvider.getUriForFile(context, context.packageName + ".nativework", derivative(sha))

    fun derivativeSha(uri: Uri): String? {
        if (uri.authority != context.packageName + ".nativework") return null
        val segments = uri.pathSegments
        require(segments.size == 2 && segments[0] == "derivatives" && segments[1].endsWith(".png"))
        return segments[1].removeSuffix(".png").also { require(validSha(it)) }
    }

    fun requireCapacity(extra: Long) {
        val live = listOf(records, manifests, derivatives).sumOf { directory ->
            files(directory).sumOf { it.length() }
        }
        if (extra < 0 || extra > MAX_STORE_BYTES || live > MAX_STORE_BYTES - extra)
            throw NativeWorkLimit()
    }

    fun put(file: File, expected: ByteArray, max: Long) {
        if (expected.size.toLong() !in 1..max) throw NativeWorkLimit()
        if (file.exists()) {
            verify(file, expected.size.toLong(), file.nameWithoutExtension, max)
            return
        }
        mkdir(file.parentFile!!)
        requireCapacity(expected.size.toLong())
        val stage = File.createTempFile("stage-", ".tmp", file.parentFile!!)
        try {
            FileOutputStream(stage).use { it.write(expected); it.fd.sync() }
            publish(stage, file, expected.size.toLong(), file.nameWithoutExtension, max)
        } finally {
            stage.delete()
        }
    }

    /** Move only a fully written, verified same-filesystem stage under the store writer lock. */
    fun publish(stage: File, output: File, bytes: Long, sha: String, max: Long) {
        verify(stage, bytes, sha, max)
        mkdir(output.parentFile!!)
        if (output.exists()) verify(output, bytes, sha, max)
        else {
            Files.move(stage.toPath(), output.toPath(), StandardCopyOption.ATOMIC_MOVE)
            syncDirectory(output.parentFile!!)
            verify(output, bytes, sha, max)
        }
    }

    fun newDerivativeStage(): File {
        mkdir(root)
        return File.createTempFile("bridge-", ".tmp", root)
    }

    fun verify(file: File, bytes: Long, sha: String, max: Long) {
        if (bytes !in 1..max) throw NativeWorkLimit()
        if (!regular(file) || file.length() != bytes || digest(file, max) != sha)
            throw NativeWorkUnavailable()
    }

    fun read(file: File, bytes: Long, max: Long): ByteArray {
        if (bytes !in 1..max) throw NativeWorkLimit()
        if (!regular(file) || file.length() != bytes) throw NativeWorkUnavailable()
        return file.inputStream().use { input ->
            val result = ByteArray(bytes.toInt())
            var at = 0
            while (at < result.size) {
                val count = input.read(result, at, result.size - at)
                if (count <= 0) throw NativeWorkUnavailable()
                at += count
            }
            if (input.read() != -1 || digest(result) != file.nameWithoutExtension)
                throw NativeWorkUnavailable()
            result
        }
    }

    fun collect(keepManifests: Set<String>, keepRecords: Set<String>, keepDerivatives: Set<String>, cold: Boolean) {
        fun clean(directory: File, keep: Set<String>, extension: String) {
            files(directory).forEach { file ->
                val name = file.name
                val orphan = file.extension == extension && validSha(file.nameWithoutExtension) &&
                    file.nameWithoutExtension !in keep
                if (orphan || (cold && name.startsWith("stage-") && name.endsWith(".tmp"))) file.delete()
            }
        }
        clean(manifests, keepManifests, "json")
        clean(records, keepRecords, "raw")
        clean(derivatives, keepDerivatives, "png")
        if (cold) files(root).filter { it.name.startsWith("bridge-") && it.extension == "tmp" }.forEach { it.delete() }
    }

    fun deleteStageFilesOnly() {
        listOf(records, manifests, derivatives).flatMap(::files)
            .filter { it.name.startsWith("stage-") && it.extension == "tmp" }.forEach { it.delete() }
        files(root).filter { it.name.startsWith("bridge-") && it.extension == "tmp" }.forEach { it.delete() }
    }

    private fun files(directory: File) = directory.listFiles()?.filter(::regular).orEmpty()

    private fun owned(directory: File, sha: String, extension: String): File {
        require(validSha(sha))
        return File(directory, sha + extension)
    }

    companion object {
        const val MAX_INDEX_BYTES = 256L * 1024
        const val MAX_STORE_BYTES = 512L * 1024 * 1024
        const val MAX_DERIVATIVE_BYTES = 56L * 1024 * 1024
        const val MAX_PRO_BYTES = 3_000_000L
        const val MAX_REGULAR_BYTES = 2_000_000L
        const val MAX_DERIVATIVE_PIXELS = 12_000_000L
        const val MAX_DERIVATIVE_EDGE = 4096
        const val MAX_HISTORY = 35
        const val MAX_DRAFTS = 24
        private val shaPattern = Regex("[0-9a-f]{64}")

        fun validSha(value: String) = shaPattern.matches(value)
        fun regular(file: File) = Files.isRegularFile(file.toPath(), LinkOption.NOFOLLOW_LINKS)

        fun mkdir(file: File) {
            if (!file.isDirectory && !file.mkdirs()) throw IOException("Native work folder is unavailable")
        }

        fun syncDirectory(file: File) {
            if (!Files.isDirectory(file.toPath(), LinkOption.NOFOLLOW_LINKS))
                throw IOException("Native work publication directory is unavailable")
            val descriptor = Os.open(file.path, OsConstants.O_RDONLY, 0)
            try { Os.fsync(descriptor) } finally { Os.close(descriptor) }
        }

        fun digest(bytes: ByteArray) = NamedProjectIdentity.version(bytes)

        fun digest(file: File, max: Long): String {
            if (!regular(file) || file.length() !in 1..max) throw NativeWorkUnavailable()
            val md = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(16_384)
                var length = 0L
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    length += count
                    if (length > max) throw NativeWorkLimit()
                    md.update(buffer, 0, count)
                }
                if (length != file.length()) throw NativeWorkUnavailable()
            }
            return md.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
        }
    }
}

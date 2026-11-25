package com.greenezone.gzphotoeditor.editor

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.util.UUID

/** Disk representation for Regular projects. Calls are serialized by ProjectStore. */
internal object PhotoProjectFiles {
    const val LIMIT = 12
    // This is the existing Regular rollback bound. Larger legacy values stay in
    // the original XML, with a counted, unavailable index entry.
    const val MAX_RECORD_BYTES = 12_000_000L
    private const val MAX_INDEX_BYTES = 64_000L
    private const val INDEX_VERSION = 1
    private val VERSION_PATTERN = Regex("[0-9a-f]{64}")
    private val cutoverBytes = "gzphotoeditor.projects.v2\n".toByteArray(Charsets.UTF_8)

    data class Record(
        val project: NamedProjectRef,
        val byteCount: Long,
        val readable: Boolean,
        val generation: String?
    )

    private fun directory(context: Context) = File(context.filesDir, "gz-photo-projects-v2")
    private fun recordDirectory(context: Context) = File(directory(context), "records")
    private fun cutoverFile(context: Context) = File(directory(context), "cutover")
    fun indexFile(context: Context) = File(directory(context), "index.json")

    private fun isCanonicalUuid(value: String): Boolean =
        try { UUID.fromString(value).toString() == value } catch (_: IllegalArgumentException) { false }

    fun payloadFile(context: Context, generation: String): File {
        require(isCanonicalUuid(generation)) { "Invalid Regular project generation" }
        return File(recordDirectory(context), "$generation.raw")
    }

    private fun ensureDirectory(dir: File) {
        if (!dir.isDirectory && !dir.mkdirs()) throw IOException("Regular project storage is unavailable")
    }

    fun createStage(context: Context): File {
        val dir = recordDirectory(context)
        ensureDirectory(dir)
        return File.createTempFile("stage-", ".tmp", dir)
    }

    fun finishRecord(context: Context, staged: File, byteCount: Long, version: String): String {
        require(byteCount in 1L..MAX_RECORD_BYTES)
        val generation = UUID.randomUUID().toString()
        val target = payloadFile(context, generation)
        if (target.exists() || !staged.renameTo(target))
            throw IOException("Regular project record could not be published")
        if (target.length() != byteCount ||
            NamedProjectIdentity.fileVersion(target, MAX_RECORD_BYTES) != version) {
            target.delete()
            throw IOException("Regular project record could not be verified")
        }
        return generation
    }

    fun writeRecord(context: Context, bytes: ByteArray, version: String): String {
        val staged = createStage(context)
        try {
            FileOutputStream(staged).use { output ->
                output.write(bytes)
                output.fd.sync()
            }
            return finishRecord(context, staged, bytes.size.toLong(), version)
        } finally { staged.delete() }
    }

    fun metadata(context: Context, row: Record): SavedPhotoProjectMetadata {
        val payload = row.generation?.let { payloadFile(context, it) }
        return SavedPhotoProjectMetadata(row.project, row.byteCount,
            row.readable && payload?.let { it.isFile && it.length() == row.byteCount } == true)
    }

    fun readRecord(context: Context, row: Record, onDecode: (() -> Unit)? = null): SavedProject? {
        val generation = row.generation ?: return null
        val current = NamedProjectIdentity.readPayload(payloadFile(context, generation), MAX_RECORD_BYTES) {
                input, length ->
            if (length != row.byteCount) return@readPayload null
            val value = input.bufferedReader(Charsets.UTF_8).use { it.readText() }
            ProjectStore.decodeRecord(row.project.name, value, onDecode)
        }
        return current?.takeIf { it.version == row.project.version }?.content
    }

    fun cleanUnreferenced(context: Context, rows: List<Record>) {
        val dir = recordDirectory(context)
        if (!dir.isDirectory) return
        val retained = rows.mapNotNull { it.generation }.toSet()
        Files.newDirectoryStream(dir.toPath()).use { files ->
            for (path in files) {
                val child = path.toFile()
                val name = child.name
                val abandonedStage = name.startsWith("stage-") && name.endsWith(".tmp")
                val generation = name.removeSuffix(".raw")
                val abandonedRecord = name.endsWith(".raw") &&
                    generation !in retained && isCanonicalUuid(generation)
                if ((abandonedStage || abandonedRecord) && child.isFile) child.delete()
            }
        }
    }

    fun readIndex(context: Context): List<Record>? {
        val path = indexFile(context)
        val result = NamedProjectIdentity.readPayload(path, MAX_INDEX_BYTES) { input, _ ->
            decodeIndex(input.bufferedReader(Charsets.UTF_8).use { it.readText() })
        }
        if (result == null && (path.exists() || File(path.path + ".bak").exists()))
            throw IOException("The Regular project index is unreadable")
        return result?.content
    }

    fun publishIndex(
        context: Context,
        rows: List<Record>,
        verifyIndex: (File, ByteArray, Long) -> Unit
    ) {
        val path = indexFile(context)
        ensureDirectory(path.parentFile ?: throw IOException("The Regular project index has no directory"))
        val items = JSONArray()
        rows.forEach { row ->
            items.put(JSONObject().put("id", row.project.id).put("name", row.project.name)
                .put("version", row.project.version).put("byteCount", row.byteCount)
                .put("readable", row.readable).put("generation", row.generation ?: JSONObject.NULL))
        }
        val bytes = JSONObject().put("version", INDEX_VERSION).put("rows", items)
            .toString().toByteArray(Charsets.UTF_8)
        require(decodeIndex(String(bytes, Charsets.UTF_8)) == rows) { "Invalid Regular project index" }
        NamedProjectIdentity.publish(path, bytes, MAX_INDEX_BYTES, verifyIndex)
    }

    private fun decodeIndex(text: String): List<Record> {
        val json = JSONObject(text)
        require(json.getInt("version") == INDEX_VERSION) { "Unsupported Regular project index" }
        val items = json.getJSONArray("rows")
        require(items.length() <= LIMIT) { "The Regular project index exceeds its slot limit" }
        val rows = List(items.length()) { index ->
            val row = items.getJSONObject(index)
            val id = row.getString("id")
            val name = row.getString("name")
            val version = row.getString("version")
            val byteCount = row.getLong("byteCount")
            val readable = row.getBoolean("readable")
            val generation = if (row.isNull("generation")) null else row.getString("generation")
            require(isCanonicalUuid(id) && name.isNotBlank() && VERSION_PATTERN.matches(version))
            require(byteCount >= 0 && (!readable || generation != null))
            require(generation == null || (isCanonicalUuid(generation) && byteCount in 1L..MAX_RECORD_BYTES))
            Record(NamedProjectRef(ProjectKind.PHOTO, id, name, version), byteCount, readable, generation)
        }
        require(rows.map { it.project.id }.distinct().size == rows.size)
        require(rows.map { it.project.name }.distinct().size == rows.size)
        val generations = rows.mapNotNull { it.generation }
        require(generations.distinct().size == generations.size)
        return rows
    }

    fun cutoverEstablished(context: Context): Boolean {
        val marker = cutoverFile(context)
        val version = NamedProjectIdentity.fileVersion(marker, cutoverBytes.size.toLong())
        if (version == null && !marker.exists() && !File(marker.path + ".bak").exists()) return false
        if (version != NamedProjectIdentity.version(cutoverBytes))
            throw IOException("The Regular project cutover marker is unreadable")
        return true
    }

    fun publishCutover(context: Context) {
        NamedProjectIdentity.publish(cutoverFile(context), cutoverBytes, cutoverBytes.size.toLong())
    }
}

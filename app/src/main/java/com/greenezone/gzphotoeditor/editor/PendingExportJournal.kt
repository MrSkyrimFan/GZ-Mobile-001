package com.greenezone.gzphotoeditor.editor

import android.net.Uri
import androidx.core.util.AtomicFile
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.UUID

internal enum class ExportWorkspace { PHOTO, COLLAGE, PRO, GIF }

internal enum class PendingExportPhase {
    PICKING, PREPARING, COPYING, RETRY, DESTINATION_UNCERTAIN
}

internal data class PendingExport(
    val workspace: ExportWorkspace,
    val id: String,
    val phase: PendingExportPhase,
    val mime: String,
    val filename: String,
    val recipe: String,
    val destination: String? = null
)

/** One immutable recipe per workspace. Only the matching picker can claim its result. */
internal class PendingExportJournal(filesDirectory: File) {
    private val directory = File(filesDirectory, "pending-exports")

    private fun file(workspace: ExportWorkspace) =
        AtomicFile(File(directory, workspace.name + ".json"))

    /** Root routing only needs the newest intact header, not every stored recipe. */
    fun latestWorkspace(): ExportWorkspace? = synchronized(lock) {
        ExportWorkspace.entries.mapNotNull { workspace ->
            val atomic = file(workspace)
            val base = atomic.baseFile
            val backup = File(base.path + ".bak")
            val authoritative = if (backup.isFile) backup else base
            if (!authoritative.isFile || authoritative.length() !in 1L..MAX_RECORD_BYTES) {
                return@mapNotNull null
            }
            val modified = authoritative.lastModified()
            val header = try {
                atomic.openRead().use { input ->
                    val bytes = ByteArray(192)
                    var offset = 0
                    while (offset < bytes.size) {
                        val count = input.read(bytes, offset, bytes.size - offset)
                        if (count <= 0) break
                        offset += count
                    }
                    String(bytes, 0, offset, Charsets.UTF_8)
                }
            } catch (_: IOException) { return@mapNotNull null }
            val match = HEADER.find(header) ?: return@mapNotNull null
            val (_, storedWorkspace, id, phase) = match.groupValues
            if (storedWorkspace != workspace.name || !isCanonicalId(id) ||
                PendingExportPhase.entries.none { it.name == phase }) return@mapNotNull null
            workspace to modified
        }.maxByOrNull { it.second }?.first
    }

    fun read(workspace: ExportWorkspace): PendingExport? = synchronized(lock) {
        val file = file(workspace)
        val source = try {
            file.openRead()
        } catch (_: IOException) { return@synchronized null }
        try {
            val data = source.use { input ->
                if (input.channel.size() > MAX_RECORD_BYTES) return@synchronized null
                input.readBytes()
            }
            val json = JSONObject(String(data, Charsets.UTF_8))
            if (json.getInt("version") != 1 || json.getString("workspace") != workspace.name) {
                return@synchronized null
            }
            val id = json.getString("id")
            if (!isCanonicalId(id)) return@synchronized null
            PendingExport(
                workspace, id, PendingExportPhase.valueOf(json.getString("phase")),
                json.getString("mime"), json.getString("filename"), json.getString("recipe"),
                json.optString("destination").takeIf { it.isNotEmpty() }
            )
        } catch (_: Exception) { null }
    }

    fun begin(workspace: ExportWorkspace, mime: String, filename: String, recipe: String): PendingExport =
        synchronized(lock) {
            check(read(workspace) == null) { "Finish or discard the previous export request first" }
            write(newRecord(workspace, mime, filename, recipe))
        }

    fun retry(prior: PendingExport): PendingExport = synchronized(lock) {
        val current = matching(prior) ?: throw IOException("Export request is no longer available")
        write(newRecord(current.workspace, current.mime, current.filename, current.recipe))
    }

    fun claim(expected: PendingExport, destination: Uri): PendingExport? = synchronized(lock) {
        val current = matching(expected)?.takeIf { it.phase == PendingExportPhase.PICKING }
            ?: return@synchronized null
        require(destination.scheme == "content") { "The picker returned an invalid export destination" }
        write(current.copy(phase = PendingExportPhase.PREPARING, destination = destination.toString()))
    }

    fun copying(expected: PendingExport) = synchronized(lock) {
        val current = matching(expected)?.takeIf { it.phase == PendingExportPhase.PREPARING }
            ?: throw IOException("Export request is no longer active")
        write(current.copy(phase = PendingExportPhase.COPYING))
    }

    fun failed(expected: PendingExport): PendingExport? = synchronized(lock) {
        val current = matching(expected) ?: return@synchronized null
        val phase = when (current.phase) {
            PendingExportPhase.COPYING, PendingExportPhase.DESTINATION_UNCERTAIN ->
                PendingExportPhase.DESTINATION_UNCERTAIN
            else -> PendingExportPhase.RETRY
        }
        write(current.copy(phase = phase))
    }

    fun cancelPicker(expected: PendingExport): Boolean = synchronized(lock) {
        if (matching(expected)?.phase != PendingExportPhase.PICKING) return@synchronized false
        discard(expected)
    }

    fun discard(expected: PendingExport): Boolean = synchronized(lock) {
        if (matching(expected) == null) return@synchronized false
        file(expected.workspace).delete()
        if (read(expected.workspace)?.id == expected.id) throw IOException("Export request could not be cleared")
        true
    }

    private fun matching(expected: PendingExport): PendingExport? =
        read(expected.workspace)?.takeIf { it.id == expected.id }

    private fun newRecord(workspace: ExportWorkspace, mime: String, filename: String, recipe: String) =
        PendingExport(workspace, UUID.randomUUID().toString(), PendingExportPhase.PICKING, mime, filename, recipe)

    private fun write(record: PendingExport): PendingExport {
        val bytes = JSONObject().apply {
            put("version", 1)
            put("workspace", record.workspace.name)
            put("id", record.id)
            put("phase", record.phase.name)
            put("mime", record.mime)
            put("filename", record.filename)
            put("recipe", record.recipe)
            if (record.destination != null) put("destination", record.destination)
        }.toString().toByteArray(Charsets.UTF_8)
        if (bytes.size > MAX_RECORD_BYTES) throw IOException("Export recipe is too large to keep for recovery")
        val file = file(record.workspace)
        val output = file.startWrite()
        try {
            output.write(bytes)
            output.fd.sync()
            file.finishWrite(output)
        } catch (error: Throwable) {
            file.failWrite(output)
            throw error
        }
        // AtomicFile can log a failed publish without throwing. The picker must
        // never be launched until its exact recovery record can be read back.
        file.openRead().use { input ->
            val buffer = ByteArray(8_192)
            var offset = 0
            while (offset < bytes.size) {
                val count = input.read(buffer, 0, minOf(buffer.size, bytes.size - offset))
                if (count <= 0 || (0 until count).any { buffer[it] != bytes[offset + it] }) {
                    throw IOException("Export recovery record could not be verified")
                }
                offset += count
            }
            if (input.read() != -1) throw IOException("Export recovery record could not be verified")
        }
        return record
    }

    private companion object {
        val lock = Any()
        const val MAX_RECORD_BYTES = 12_000_000L
        // write() uses the stable insertion order of Android's JSONObject.
        val HEADER = Regex("""^\{"version":1,"workspace":"([A-Z]+)","id":"([0-9a-f-]{36})","phase":"([A-Z_]+)","mime":""")

        fun isCanonicalId(id: String): Boolean = try {
            UUID.fromString(id).toString() == id
        } catch (_: IllegalArgumentException) { false }
    }
}

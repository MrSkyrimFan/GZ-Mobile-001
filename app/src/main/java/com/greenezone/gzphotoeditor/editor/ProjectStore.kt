package com.greenezone.gzphotoeditor.editor

import android.content.Context
import android.net.Uri
import android.content.SharedPreferences
import android.util.AtomicFile
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Base64

data class SavedProject(val name: String, val image: Uri, val document: EditDocument)

/** Up to twelve named local projects; original photos stay in the system media store. */
object ProjectStore {
    private const val PREFS = "gzphotoeditor.projects.v1"
    private const val LIMIT = 12
    private const val MAX_ROLLBACK_BYTES = 12_000_000L
    private data class PendingSave(val storageKey: String, val previous: String?)

    private fun pendingFile(context: Context) =
        File(context.filesDir, "gz-photo-project-save.pending")

    private fun hasPending(base: File) = base.exists() || File(base.path + ".bak").exists()

    private fun pendingBytes(pending: PendingSave): ByteArray =
        JSONObject().put("version", 1).put("key", pending.storageKey)
            .put("previous", pending.previous ?: JSONObject.NULL)
            .toString().toByteArray(StandardCharsets.UTF_8).also {
                if (it.size.toLong() > MAX_ROLLBACK_BYTES)
                    throw IOException("Project rollback exceeds its storage bound")
            }

    private fun writePending(base: File, bytes: ByteArray) {
        val atomic = AtomicFile(base)
        val output = atomic.startWrite()
        try {
            output.write(bytes)
            output.fd.sync()
            atomic.finishWrite(output)
        } catch (failure: Throwable) {
            atomic.failWrite(output)
            throw failure
        }
        atomic.openRead().use { input ->
            if (input.channel.size() != bytes.size.toLong())
                throw IOException("Project rollback record could not be verified")
            val buffer = ByteArray(8192)
            var offset = 0
            while (offset < bytes.size) {
                val count = input.read(buffer, 0, minOf(buffer.size, bytes.size - offset))
                if (count <= 0 || (0 until count).any { buffer[it] != bytes[offset + it] })
                    throw IOException("Project rollback record could not be verified")
                offset += count
            }
            if (input.read() != -1) throw IOException("Project rollback record changed")
        }
    }

    private fun clearPending(base: File) {
        AtomicFile(base).delete()
        if (hasPending(base) || File(base.path + ".new").exists())
            throw IOException("Project rollback record could not be cleared")
    }

    private fun readPending(base: File): PendingSave = AtomicFile(base).openRead().use { input ->
        if (input.channel.size() !in 1L..MAX_ROLLBACK_BYTES)
            throw IOException("Invalid project rollback record size")
        val json = JSONObject(String(input.readBytes(), StandardCharsets.UTF_8))
        if (json.getInt("version") != 1) throw IOException("Unknown project rollback record")
        val storageKey = json.getString("key")
        if (storageKey.isEmpty()) throw IOException("Invalid project rollback key")
        PendingSave(storageKey, if (json.isNull("previous")) null else json.getString("previous"))
    }

    private fun rollback(context: Context, prefs: SharedPreferences, pending: PendingSave) {
        val editor = prefs.edit()
        if (pending.previous == null) editor.remove(pending.storageKey)
        else editor.putString(pending.storageKey, pending.previous)
        if (!editor.commit() || prefs.getString(pending.storageKey, null) != pending.previous)
            throw IOException("Previous project is retained for recovery")
        clearPending(pendingFile(context))
        if (pending.previous == null) {
            val name = String(Base64.getUrlDecoder().decode(pending.storageKey), StandardCharsets.UTF_8)
            NamedProjectIdentity.forget(context, ProjectKind.PHOTO, name)
        }
    }

    /** A rejected publication is never exposed by a later list/load/save/delete. */
    private fun recoverPending(context: Context, prefs: SharedPreferences) {
        val base = pendingFile(context)
        if (hasPending(base)) rollback(context, prefs, readPending(base))
    }

    private fun key(name: String): String = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(name.toByteArray(StandardCharsets.UTF_8))

    private fun storedName(context: Context, input: String): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return if (input.isNotBlank() && prefs.contains(key(input))) input else normalizedName(input)
    }

    private fun decode(name: String, value: String): SavedProject? = try {
        val split = value.indexOf('\n')
        if (split <= 0) null else {
            val uri = Uri.parse(String(Base64.getUrlDecoder().decode(value.substring(0, split)),
                StandardCharsets.UTF_8))
            val document = EditDocumentCodec.decode(value.substring(split + 1))
            if (uri.scheme != "content" || document == null) null
            else SavedProject(name, uri, document)
        }
    } catch (_: Exception) { null }

    @Synchronized fun load(context: Context): List<SavedProject> = try {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        recoverPending(context, prefs)
        prefs.all.mapNotNull { (key, raw) ->
            val value = raw as? String ?: return@mapNotNull null
            try {
                val name = String(Base64.getUrlDecoder().decode(key), StandardCharsets.UTF_8)
                decode(name, value)
            } catch (_: Exception) { null }
        }.sortedBy { it.name.lowercase() }
    } catch (_: OutOfMemoryError) { emptyList() } catch (_: Exception) { emptyList() }

    fun normalizedName(input: String): String = input.trim().replace(Regex("\\s+"), " ").take(28)

    @Synchronized fun loadNamed(context: Context, name: String): NamedProject<SavedProject>? = try {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        recoverPending(context, prefs)
        val canonical = storedName(context, name)
        val raw = prefs.getString(key(canonical), null)
        if (raw == null) null else decode(canonical, raw)?.let { content ->
            NamedProject(NamedProjectIdentity.reference(context, ProjectKind.PHOTO, canonical,
                NamedProjectIdentity.version(raw.toByteArray(StandardCharsets.UTF_8))), content)
        }
    } catch (_: OutOfMemoryError) { null } catch (_: Exception) { null }

    fun save(context: Context, nameInput: String, image: Uri, document: EditDocument): Boolean =
        write(context, nameInput, image, document, null) is ProjectSaveResult.Saved

    fun saveNamed(context: Context, intent: ProjectSaveIntent, image: Uri,
        document: EditDocument): ProjectSaveResult =
        write(context, intent.requestedName, image, document, intent)

    @Synchronized private fun write(context: Context, nameInput: String, image: Uri,
        document: EditDocument, intent: ProjectSaveIntent?): ProjectSaveResult {
        if (image.scheme != "content")
            return ProjectSaveResult.Rejected(ProjectSaveFailure.INVALID_DOCUMENT)
        if (intent?.expectedProject?.kind?.let { it != ProjectKind.PHOTO } == true)
            return ProjectSaveResult.Rejected(ProjectSaveFailure.TARGET_CHANGED)
        return try {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            recoverPending(context, prefs)
            val name = storedName(context, nameInput)
            if (name.isBlank()) return ProjectSaveResult.Rejected(ProjectSaveFailure.INVALID_NAME)
            // Build the bounded payload before touching any named save or identity.
            val value = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(image.toString().toByteArray(StandardCharsets.UTF_8)) +
                "\n" + EditDocumentCodec.encodeBounded(document)
            val storageKey = key(name)
            val previous = prefs.getString(storageKey, null)
            val current = previous?.let {
                NamedProjectIdentity.reference(context, ProjectKind.PHOTO, name,
                    NamedProjectIdentity.version(it.toByteArray(StandardCharsets.UTF_8)))
            }
            if (intent != null) NamedProjectIdentity.authorize(ProjectKind.PHOTO, intent, current)
                ?.let { return it }
            if (previous == null && prefs.all.size >= LIMIT)
                return ProjectSaveResult.Rejected(ProjectSaveFailure.FULL)
            val id = current?.id ?: NamedProjectIdentity.reserve(context, ProjectKind.PHOTO,
                name, payloadExists = false)
            // Prepare the entire acknowledgement before publication; hashing can allocate.
            val saved = ProjectSaveResult.Saved(NamedProjectRef(ProjectKind.PHOTO, id, name,
                NamedProjectIdentity.version(value.toByteArray(StandardCharsets.UTF_8))))
            val pending = PendingSave(storageKey, previous)
            val recoveryBytes = pendingBytes(pending)
            val recoveryFile = pendingFile(context)
            writePending(recoveryFile, recoveryBytes)
            try {
                if (!prefs.edit().putString(storageKey, value).commit() ||
                    prefs.getString(storageKey, null) != value)
                    throw IOException("Project publication could not be verified")
                // This is the commit point. Only infallible reference return follows.
                clearPending(recoveryFile)
                saved
            } catch (failure: Throwable) {
                // A failed commit can still update the in-memory map. Keep a durable
                // previous value until both rollback commit and readback succeed.
                if (!hasPending(recoveryFile)) {
                    try { writePending(recoveryFile, recoveryBytes) }
                    catch (recordFailure: Throwable) { failure.addSuppressed(recordFailure) }
                }
                try { rollback(context, prefs, pending) }
                catch (rollbackFailure: Throwable) { failure.addSuppressed(rollbackFailure) }
                throw failure
            }
        } catch (_: EditDocumentCodec.RecipeTooLarge) {
            ProjectSaveResult.Rejected(ProjectSaveFailure.TOO_LARGE)
        } catch (_: OutOfMemoryError) {
            ProjectSaveResult.Rejected(ProjectSaveFailure.TOO_LARGE)
        } catch (_: Exception) {
            ProjectSaveResult.Rejected(ProjectSaveFailure.STORAGE_FAILURE)
        }
    }

    @Synchronized fun deleteNamed(context: Context, expected: NamedProjectRef): Boolean {
        if (expected.kind != ProjectKind.PHOTO) return false
        val current = loadNamed(context, expected.name)?.project ?: return false
        return current == expected && delete(context, current.name)
    }

    @Synchronized fun delete(context: Context, name: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        try { recoverPending(context, prefs) } catch (_: Exception) { return false }
        val canonical = storedName(context, name)
        val storageKey = key(canonical)
        val previous = prefs.getString(storageKey, null)
        val deleted = prefs.edit().remove(storageKey).commit()
        if (deleted) NamedProjectIdentity.forget(context, ProjectKind.PHOTO, canonical)
        else if (previous != null) prefs.edit().putString(storageKey, previous).commit()
        return deleted
    }
}

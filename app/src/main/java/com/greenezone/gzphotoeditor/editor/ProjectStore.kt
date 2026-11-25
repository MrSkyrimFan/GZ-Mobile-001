package com.greenezone.gzphotoeditor.editor

import android.content.Context
import android.net.Uri
import java.nio.charset.StandardCharsets
import java.util.Base64

data class SavedProject(val name: String, val image: Uri, val document: EditDocument)

/** Up to twelve named local projects; original photos stay in the system media store. */
object ProjectStore {
    private const val PREFS = "gzphotoeditor.projects.v1"
    private const val LIMIT = 12

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

    @Synchronized fun load(context: Context): List<SavedProject> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).all.mapNotNull { (key, raw) ->
            val value = raw as? String ?: return@mapNotNull null
            try {
                val name = String(Base64.getUrlDecoder().decode(key), StandardCharsets.UTF_8)
                decode(name, value)
            } catch (_: Exception) { null }
        }.sortedBy { it.name.lowercase() }

    fun normalizedName(input: String): String = input.trim().replace(Regex("\\s+"), " ").take(28)

    @Synchronized fun loadNamed(context: Context, name: String): NamedProject<SavedProject>? = try {
        val canonical = storedName(context, name)
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(key(canonical), null)
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
        val name = storedName(context, nameInput)
        if (name.isBlank()) return ProjectSaveResult.Rejected(ProjectSaveFailure.INVALID_NAME)
        if (intent?.expectedProject?.kind?.let { it != ProjectKind.PHOTO } == true)
            return ProjectSaveResult.Rejected(ProjectSaveFailure.TARGET_CHANGED)
        return try {
            // Build the bounded payload before touching any named save or identity.
            val value = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(image.toString().toByteArray(StandardCharsets.UTF_8)) +
                "\n" + EditDocumentCodec.encodeBounded(document)
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
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
            if (!prefs.edit().putString(storageKey, value).commit() ||
                prefs.getString(storageKey, null) != value) {
                // A failed commit can still change SharedPreferences' in-memory map.
                val rollback = prefs.edit()
                if (previous == null) rollback.remove(storageKey) else rollback.putString(storageKey, previous)
                rollback.commit()
                if (previous == null) NamedProjectIdentity.forget(context, ProjectKind.PHOTO, name)
                ProjectSaveResult.Rejected(ProjectSaveFailure.STORAGE_FAILURE)
            } else ProjectSaveResult.Saved(NamedProjectRef(ProjectKind.PHOTO, id, name,
                NamedProjectIdentity.version(value.toByteArray(StandardCharsets.UTF_8))))
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
        val canonical = storedName(context, name)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val storageKey = key(canonical)
        val previous = prefs.getString(storageKey, null)
        val deleted = prefs.edit().remove(storageKey).commit()
        if (deleted) NamedProjectIdentity.forget(context, ProjectKind.PHOTO, canonical)
        else if (previous != null) prefs.edit().putString(storageKey, previous).commit()
        return deleted
    }
}

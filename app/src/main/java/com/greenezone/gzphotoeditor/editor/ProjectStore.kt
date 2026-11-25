package com.greenezone.gzphotoeditor.editor

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException
import java.util.Base64

data class SavedProject(val name: String, val image: Uri, val document: EditDocument)

internal data class SavedPhotoProjectMetadata(
    val project: NamedProjectRef,
    val byteCount: Long,
    val readable: Boolean
)

/** Up to twelve named local projects; original photos stay in the system media store. */
object ProjectStore {
    private val names = compareBy<PhotoProjectFiles.Record> { it.project.name.lowercase() }

    private fun index(
        context: Context,
        verifyIndex: (File, ByteArray, Long) -> Unit = NamedProjectIdentity::verify,
        onDecode: (() -> Unit)? = null
    ): List<PhotoProjectFiles.Record> {
        val cutover = PhotoProjectFiles.cutoverEstablished(context)
        val existing = PhotoProjectFiles.readIndex(context)
        if (cutover) return existing ?: throw IOException("The Regular project index is unavailable")
        if (existing != null) {
            // An interruption after the full index committed leaves it complete.
            // No writes through the new store are allowed until this marker is verified.
            PhotoProjectFiles.publishCutover(context)
            return existing
        }
        PhotoProjectFiles.cleanUnreferenced(context, emptyList())
        val migrated = LegacyPhotoProjects.migrate(context, onDecode).sortedWith(names)
        PhotoProjectFiles.publishIndex(context, migrated, verifyIndex)
        PhotoProjectFiles.publishCutover(context)
        return migrated
    }

    @Synchronized
    internal fun listMetadata(
        context: Context,
        verifyIndex: (File, ByteArray, Long) -> Unit = NamedProjectIdentity::verify,
        onDecode: (() -> Unit)? = null
    ): List<SavedPhotoProjectMetadata>? = try {
        index(context, verifyIndex, onDecode).map { PhotoProjectFiles.metadata(context, it) }
    } catch (_: OutOfMemoryError) { null } catch (_: Exception) { null }

    /** Compatibility for older callers that explicitly request every editable recipe. */
    @Synchronized fun load(context: Context): List<SavedProject> = try {
        index(context).mapNotNull { row ->
            try { loadRecord(context, row)?.content } catch (_: Exception) { null }
        }
    } catch (_: OutOfMemoryError) { emptyList() } catch (_: Exception) { emptyList() }

    fun normalizedName(input: String): String = input.trim().replace(Regex("\\s+"), " ").take(28)

    private fun storedName(rows: List<PhotoProjectFiles.Record>, input: String): String =
        if (input.isNotBlank() && rows.any { it.project.name == input }) input else normalizedName(input)

    internal fun decodeRecord(name: String, value: String, onDecode: (() -> Unit)?): SavedProject? {
        val split = value.indexOf('\n')
        if (split <= 0 || value.length - split - 1 > EditDocumentCodec.MAX_ENCODED_CHARS) return null
        val uri = Uri.parse(String(Base64.getUrlDecoder().decode(value.substring(0, split)), Charsets.UTF_8))
        if (uri.scheme != "content") return null
        onDecode?.invoke()
        val document = EditDocumentCodec.decode(value.substring(split + 1)) ?: return null
        return SavedProject(name, uri, document)
    }

    private fun loadRecord(
        context: Context,
        row: PhotoProjectFiles.Record,
        onDecode: (() -> Unit)? = null
    ): NamedProject<SavedProject>? {
        if (!PhotoProjectFiles.metadata(context, row).readable) return null
        val content = PhotoProjectFiles.readRecord(context, row, onDecode) ?: return null
        return NamedProject(row.project, content)
    }

    @Synchronized fun loadNamed(context: Context, name: String): NamedProject<SavedProject>? = try {
        val rows = index(context)
        val canonical = storedName(rows, name)
        rows.firstOrNull { it.project.name == canonical }?.let { loadRecord(context, it) }
    } catch (_: OutOfMemoryError) { null } catch (_: Exception) { null }

    @Synchronized
    internal fun loadNamed(
        context: Context,
        expected: NamedProjectRef,
        onDecode: (() -> Unit)? = null
    ): NamedProject<SavedProject>? = try {
        index(context).firstOrNull { it.project == expected }
            ?.let { loadRecord(context, it, onDecode) }
    } catch (_: OutOfMemoryError) { null } catch (_: Exception) { null }

    fun save(context: Context, nameInput: String, image: Uri, document: EditDocument): Boolean =
        write(context, nameInput, image, document, null, NamedProjectIdentity::verify) is ProjectSaveResult.Saved

    fun saveNamed(context: Context, intent: ProjectSaveIntent, image: Uri,
        document: EditDocument): ProjectSaveResult =
        saveNamed(context, intent, image, document, NamedProjectIdentity::verify)

    internal fun saveNamed(
        context: Context,
        intent: ProjectSaveIntent,
        image: Uri,
        document: EditDocument,
        verifyIndex: (File, ByteArray, Long) -> Unit
    ): ProjectSaveResult = write(context, intent.requestedName, image, document, intent, verifyIndex)

    @Synchronized
    private fun write(
        context: Context,
        nameInput: String,
        image: Uri,
        document: EditDocument,
        intent: ProjectSaveIntent?,
        verifyIndex: (File, ByteArray, Long) -> Unit
    ): ProjectSaveResult {
        if (image.scheme != "content")
            return ProjectSaveResult.Rejected(ProjectSaveFailure.INVALID_DOCUMENT)
        if (intent?.expectedProject?.kind?.let { it != ProjectKind.PHOTO } == true)
            return ProjectSaveResult.Rejected(ProjectSaveFailure.TARGET_CHANGED)
        return try {
            val rows = index(context)
            val name = storedName(rows, nameInput)
            if (name.isBlank()) return ProjectSaveResult.Rejected(ProjectSaveFailure.INVALID_NAME)
            val current = rows.firstOrNull { it.project.name == name }
            if (intent != null) NamedProjectIdentity.authorize(ProjectKind.PHOTO, intent, current?.project)
                ?.let { return it }
            if (current == null && rows.size >= PhotoProjectFiles.LIMIT)
                return ProjectSaveResult.Rejected(ProjectSaveFailure.FULL)

            val value = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(image.toString().toByteArray(Charsets.UTF_8)) +
                "\n" + EditDocumentCodec.encodeBounded(document)
            val bytes = value.toByteArray(Charsets.UTF_8)
            if (bytes.size.toLong() > PhotoProjectFiles.MAX_RECORD_BYTES)
                return ProjectSaveResult.Rejected(ProjectSaveFailure.TOO_LARGE)
            val version = NamedProjectIdentity.version(bytes)
            val id = current?.project?.id ?:
                NamedProjectIdentity.reserve(context, ProjectKind.PHOTO, name, payloadExists = false)
            val project = NamedProjectRef(ProjectKind.PHOTO, id, name, version)
            val result = ProjectSaveResult.Saved(project)
            val generation = PhotoProjectFiles.writeRecord(context, bytes, version)
            val row = PhotoProjectFiles.Record(project, bytes.size.toLong(), true, generation)
            val revised = if (current == null) rows + row else rows.map { if (it === current) row else it }
            val nextRows = revised.sortedWith(names)
            PhotoProjectFiles.publishIndex(context, nextRows, verifyIndex)
            // After commit, cleanup is independent of the user-visible acknowledgement.
            cleanObsoleteRecords(context, nextRows)
            result
        } catch (_: EditDocumentCodec.RecipeTooLarge) {
            ProjectSaveResult.Rejected(ProjectSaveFailure.TOO_LARGE)
        } catch (_: OutOfMemoryError) {
            ProjectSaveResult.Rejected(ProjectSaveFailure.TOO_LARGE)
        } catch (_: Exception) {
            ProjectSaveResult.Rejected(ProjectSaveFailure.STORAGE_FAILURE)
        }
    }

    fun deleteNamed(context: Context, expected: NamedProjectRef): Boolean =
        deleteNamed(context, expected, NamedProjectIdentity::verify)

    @Synchronized
    internal fun deleteNamed(
        context: Context,
        expected: NamedProjectRef,
        verifyIndex: (File, ByteArray, Long) -> Unit
    ): Boolean = try {
        val rows = index(context)
        val current = rows.firstOrNull { it.project == expected }
        if (current == null) false else {
            deleteRecord(context, rows, current, verifyIndex)
            true
        }
    } catch (_: OutOfMemoryError) { false } catch (_: Exception) { false }

    @Synchronized
    fun delete(context: Context, name: String): Boolean = try {
        val rows = index(context)
        val canonical = storedName(rows, name)
        rows.firstOrNull { it.project.name == canonical }?.let {
            deleteRecord(context, rows, it, NamedProjectIdentity::verify)
        }
        true
    } catch (_: OutOfMemoryError) { false } catch (_: Exception) { false }

    private fun deleteRecord(
        context: Context,
        rows: List<PhotoProjectFiles.Record>,
        current: PhotoProjectFiles.Record,
        verifyIndex: (File, ByteArray, Long) -> Unit
    ) {
        val nextRows = rows.filterNot { it === current }
        PhotoProjectFiles.publishIndex(context, nextRows, verifyIndex)
        cleanObsoleteRecords(context, nextRows)
    }

    private fun cleanObsoleteRecords(context: Context, retained: List<PhotoProjectFiles.Record>) {
        try { PhotoProjectFiles.cleanUnreferenced(context, retained) }
        catch (_: OutOfMemoryError) { }
        catch (_: Exception) { }
    }
}

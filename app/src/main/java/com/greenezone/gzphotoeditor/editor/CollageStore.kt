package com.greenezone.gzphotoeditor.editor

import android.content.Context
import androidx.core.util.AtomicFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.UUID

/** Private on-device custody. Saves references and editing parameters, not full photo bytes. */
object CollageStore {
    private const val VERSION = 1
    private const val MAX_SAVED = 12
    private const val MAX_BYTES = 3_000_000
    private const val DIR = "gz_collages"

    fun encode(document: CollageDocument): String {
        require(CollageMath.validCount(document.photos.size))
        val layout = document.layout
        val obj = JSONObject().apply {
            put("version", VERSION)
            put("template", layout.template.name)
            put("aspect", layout.aspect.name)
            put("gap", layout.gap.toDouble())
            put("margin", layout.margin.toDouble())
            put("radius", layout.cornerRadius.toDouble())
            put("border", layout.border.toDouble())
            put("background", layout.background.name)
            put("mode", layout.cellMode.name)
            put("quality", layout.quality.name)
        }
        val photos = JSONArray()
        for (photo in document.photos) {
            require(photo.uri.startsWith("content://") && photo.uri.length <= 4096) {
                "Only local photos may be stored"
            }
            val edits = EditDocumentCodec.encodeBounded(photo.edits)
            photos.put(JSONObject().apply {
                put("uri", photo.uri)
                put("id", photo.id)
                put("edits", edits)
            })
        }
        obj.put("photos", photos)
        return obj.toString().also {
            require(it.toByteArray(StandardCharsets.UTF_8).size <= MAX_BYTES)
        }
    }

    private inline fun <reified T: Enum<T>> enumValue(value: String?, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == value } ?: fallback

    fun decode(data: String): CollageDocument? {
        if (data.length > MAX_BYTES) return null
        return try {
            val obj = JSONObject(data)
            if (obj.optInt("version", -1) != VERSION) return null
            val images = obj.optJSONArray("photos") ?: return null
            if (!CollageMath.validCount(images.length())) return null
            val tileIds = mutableSetOf<String>()
            val photos = (0 until images.length()).map { i ->
                val image = images.optJSONObject(i) ?: return null
                val uri = image.optString("uri", "")
                if (!uri.startsWith("content://") || uri.length > 4096) return null
                val edits = EditDocumentCodec.decode(image.optString("edits", ""))
                    ?: return null
                val savedId = image.optString("id", "")
                val id = if (savedId.length in 1..64 && tileIds.add(savedId)) savedId
                    else UUID.randomUUID().toString().also { tileIds.add(it) }
                CollagePhoto(uri, edits, id)
            }
            fun number(name: String, fallback: Float, low: Float, high: Float): Float {
                val n = obj.optDouble(name, fallback.toDouble()).toFloat()
                return if (n.isFinite()) n.coerceIn(low, high) else fallback
            }
            CollageDocument(
                photos,
                CollageLayout(
                    template = enumValue(obj.optString("template"), CollageTemplate.GRID),
                    aspect = enumValue(obj.optString("aspect"), CollageAspect.SQUARE),
                    gap = number("gap", .012f, 0f, .08f),
                    margin = number("margin", .025f, 0f, .10f),
                    cornerRadius = number("radius", .015f, 0f, .10f),
                    border = number("border", 0f, 0f, .025f),
                    background = enumValue(obj.optString("background"), CollageBackground.BLACK),
                    cellMode = enumValue(obj.optString("mode"), CollageCellMode.FILL),
                    quality = enumValue(obj.optString("quality"), CollageQuality.HIGH)
                )
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun directory(context: Context): File =
        File(context.filesDir, DIR).also { it.mkdirs() }

    fun normalizedName(input: String): String = input.trim().replace(Regex("\\s+"), " ").take(36)

    private fun key(name: String): String? {
        if (name.isBlank()) return null
        return Base64.getUrlEncoder().withoutPadding()
            .encodeToString(name.toByteArray(StandardCharsets.UTF_8))
    }

    private fun storedName(context: Context, input: String): String {
        val exact = key(input)?.let { File(directory(context), "$it.gzc") }
        return if (exact != null && (hasSavedFile(exact) || NamedProjectIdentity.hasPendingPublication(exact))) input else normalizedName(input)
    }

    @Synchronized
    fun names(context: Context): List<String> {
        NamedProjectIdentity.recoverDirectory(directory(context), ".gzc", MAX_BYTES.toLong())
        return directory(context).listFiles()?.mapNotNull { file ->
            val name = file.name.removeSuffix(".bak").removeSuffix(".save-pending")
            if (!file.isFile || !name.endsWith(".gzc")) return@mapNotNull null
            try {
                String(Base64.getUrlDecoder().decode(name.removeSuffix(".gzc")),
                    StandardCharsets.UTF_8)
            } catch (_: Exception) { null }
        }?.distinct()?.sortedBy { it.lowercase() } ?: emptyList()
    }

    fun save(context: Context, name: String, document: CollageDocument): Boolean =
        write(context, name, document, null) is ProjectSaveResult.Saved

    fun saveNamed(context: Context, intent: ProjectSaveIntent,
        document: CollageDocument): ProjectSaveResult =
        write(context, intent.requestedName, document, intent)

    @Synchronized private fun write(context: Context, input: String, document: CollageDocument,
        intent: ProjectSaveIntent?): ProjectSaveResult {
        if (!CollageMath.validCount(document.photos.size) ||
            document.photos.any { !it.uri.startsWith("content://") || it.uri.length > 4096 })
            return ProjectSaveResult.Rejected(ProjectSaveFailure.INVALID_DOCUMENT)
        if (intent?.expectedProject?.kind?.let { it != ProjectKind.COLLAGE } == true)
            return ProjectSaveResult.Rejected(ProjectSaveFailure.TARGET_CHANGED)
        return try {
            val name = storedName(context, input)
            val id = key(name) ?: return ProjectSaveResult.Rejected(ProjectSaveFailure.INVALID_NAME)
            val file = File(directory(context), "$id.gzc")
            val encoded = encode(document)
            val bytes = encoded.toByteArray(StandardCharsets.UTF_8)
            NamedProjectIdentity.recoverPublication(file, MAX_BYTES.toLong())
            val exists = hasSavedFile(file)
            val current = if (exists) NamedProjectIdentity.reference(context, ProjectKind.COLLAGE,
                name, NamedProjectIdentity.fileVersion(file, MAX_BYTES.toLong())
                    ?: return ProjectSaveResult.Rejected(ProjectSaveFailure.STORAGE_FAILURE)) else null
            if (intent != null) NamedProjectIdentity.authorize(ProjectKind.COLLAGE, intent, current)
                ?.let { return it }
            if (!exists && names(context).size >= MAX_SAVED)
                return ProjectSaveResult.Rejected(ProjectSaveFailure.FULL)
            val identity = current?.id ?: NamedProjectIdentity.reserve(context,
                ProjectKind.COLLAGE, name, payloadExists = false)
            val version = NamedProjectIdentity.publish(file, bytes, MAX_BYTES.toLong())
            ProjectSaveResult.Saved(NamedProjectRef(ProjectKind.COLLAGE, identity, name, version))
        } catch (_: EditDocumentCodec.RecipeTooLarge) {
            ProjectSaveResult.Rejected(ProjectSaveFailure.TOO_LARGE)
        } catch (_: OutOfMemoryError) {
            ProjectSaveResult.Rejected(ProjectSaveFailure.TOO_LARGE)
        } catch (_: IllegalArgumentException) {
            ProjectSaveResult.Rejected(ProjectSaveFailure.TOO_LARGE)
        } catch (_: Exception) {
            ProjectSaveResult.Rejected(ProjectSaveFailure.STORAGE_FAILURE)
        }
    }

    @Synchronized fun loadNamed(context: Context, input: String): NamedProject<CollageDocument>? = try {
        val name = storedName(context, input)
        val id = key(name)
        if (id == null) null else {
            val file = File(directory(context), "$id.gzc")
            NamedProjectIdentity.readPayload(file, MAX_BYTES.toLong()) { stream, _ ->
                decode(stream.bufferedReader(Charsets.UTF_8).readText())
            }?.let { loaded ->
                NamedProject(NamedProjectIdentity.reference(context, ProjectKind.COLLAGE,
                    name, loaded.version), loaded.content)
            }
        }
    } catch (_: OutOfMemoryError) { null } catch (_: Exception) { null }

    @Synchronized fun deleteNamed(context: Context, expected: NamedProjectRef): Boolean {
        if (expected.kind != ProjectKind.COLLAGE) return false
        val current = loadNamed(context, expected.name)?.project ?: return false
        return current == expected && delete(context, current.name)
    }

    private fun hasSavedFile(file: File): Boolean =
        file.isFile || File(file.path + ".bak").isFile

    private fun readRecipe(file: File): String? =
        AtomicFile(file).openRead().use { stream ->
            if (stream.channel.size() > MAX_BYTES) null
            else stream.bufferedReader(Charsets.UTF_8).readText()
        }

    @Synchronized
    fun load(context: Context, name: String): CollageDocument? {
        val id = key(storedName(context, name)) ?: return null
        val file = File(directory(context), "$id.gzc")
        return try {
            NamedProjectIdentity.recoverPublication(file, MAX_BYTES.toLong())
            if (!hasSavedFile(file)) null else readRecipe(file)?.let(::decode)
        } catch (_: Exception) { null }
    }

    @Synchronized
    fun delete(context: Context, name: String): Boolean {
        val canonical = storedName(context, name)
        val id = key(canonical) ?: return false
        val file = File(directory(context), "$id.gzc")
        try { NamedProjectIdentity.recoverPublication(file, MAX_BYTES.toLong()) }
        catch (_: Exception) { return false }
        if (!hasSavedFile(file)) return false
        AtomicFile(file).delete()
        val deleted = !hasSavedFile(file)
        if (deleted) NamedProjectIdentity.forget(context, ProjectKind.COLLAGE, canonical)
        return deleted
    }
}

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
        val clean = normalizedName(name)
        if (clean.isBlank()) return null
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
            clean.toByteArray(StandardCharsets.UTF_8)
        )
    }

    @Synchronized
    fun names(context: Context): List<String> {
        return directory(context).listFiles()?.mapNotNull { file ->
            val name = file.name.removeSuffix(".bak")
            if (!file.isFile || !name.endsWith(".gzc")) return@mapNotNull null
            try {
                String(Base64.getUrlDecoder().decode(name.removeSuffix(".gzc")),
                    StandardCharsets.UTF_8)
            } catch (_: Exception) { null }
        }?.distinct()?.sortedBy { it.lowercase() } ?: emptyList()
    }

    @Synchronized
    fun save(context: Context, name: String, document: CollageDocument): Boolean {
        val id = key(name) ?: return false
        val folder = directory(context)
        val file = File(folder, "$id.gzc")
        if (!hasSavedFile(file) && names(context).size >= MAX_SAVED) return false
        return try {
            val encoded = encode(document)
            val atomic = AtomicFile(file)
            val stream = atomic.startWrite()
            try {
                stream.write(encoded.toByteArray(StandardCharsets.UTF_8))
                stream.fd.sync()
                atomic.finishWrite(stream)
            } catch (failure: Exception) {
                atomic.failWrite(stream)
                throw failure
            }
            readRecipe(file) == encoded
        } catch (_: Exception) { false }
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
        val id = key(name) ?: return null
        val file = File(directory(context), "$id.gzc")
        if (!hasSavedFile(file)) return null
        return try { readRecipe(file)?.let(::decode) } catch (_: Exception) { null }
    }

    @Synchronized
    fun delete(context: Context, name: String): Boolean {
        val id = key(name) ?: return false
        val file = File(directory(context), "$id.gzc")
        if (!hasSavedFile(file)) return false
        AtomicFile(file).delete()
        return !hasSavedFile(file)
    }
}

package com.greenezone.gzphotoeditor.editor

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Base64

/** Local-only GIF projects: source references plus order, tempo and output options. */
object GifStore {
    private const val MAX_FILES = 12
    private const val MAX_BYTES = 90_000
    private fun folder(context: Context) = File(context.filesDir, "gz_gif_projects").also { it.mkdirs() }
    private fun id(name: String): String? {
        val clean = name.trim().replace(Regex("\\s+"), " ").take(32)
        if (clean.isBlank()) return null
        return Base64.getUrlEncoder().withoutPadding().encodeToString(clean.toByteArray(Charsets.UTF_8))
    }
    fun encode(doc: GifDocument): String {
        require(GifMath.valid(doc))
        return JSONObject().apply {
            put("version", 1)
            put("delayMs", doc.delayMs)
            put("size", doc.size.name)
            put("fit", doc.fit.name)
            put("aspect", doc.aspect.name)
            put("forever", doc.forever)
            put("frames", JSONArray(doc.frames))
        }.toString().also { require(it.length <= MAX_BYTES) }
    }
    fun decode(data: String): GifDocument? {
        if (data.length > MAX_BYTES) return null
        return try {
            val obj = JSONObject(data)
            if (obj.optInt("version", 0) != 1) return null
            val array = obj.optJSONArray("frames") ?: return null
            if (array.length() !in 2..GifMath.MAX_FRAMES) return null
            val list = (0 until array.length()).map { array.optString(it, "") }
            fun <T : Enum<T>> find(items: Array<T>, key: String, fallback: T): T =
                items.firstOrNull { it.name == obj.optString(key) } ?: fallback
            GifDocument(
                frames = list, delayMs = obj.optInt("delayMs", 350),
                size = find(GifSize.entries.toTypedArray(), "size", GifSize.MEDIUM),
                fit = find(GifFit.entries.toTypedArray(), "fit", GifFit.FIT),
                aspect = find(GifAspect.entries.toTypedArray(), "aspect", GifAspect.SQUARE),
                forever = obj.optBoolean("forever", true)
            ).takeIf { GifMath.valid(it) }
        } catch (_: Exception) { null }
    }
    fun names(context: Context): List<String> = folder(context).listFiles()?.mapNotNull {
        if (!it.isFile || !it.name.endsWith(".gzgif")) return@mapNotNull null
        try { String(Base64.getUrlDecoder().decode(it.name.removeSuffix(".gzgif")),
            Charsets.UTF_8) } catch (_: Exception) { null }
    }?.sortedBy { it.lowercase() } ?: emptyList()

    fun save(context: Context, name: String, doc: GifDocument): Boolean {
        val id = id(name) ?: return false
        val dir = folder(context)
        val file = File(dir, "$id.gzgif")
        if (!file.exists() && names(context).size >= MAX_FILES) return false
        return try {
            val temp = File(dir, "$id.tmp")
            temp.writeText(encode(doc), Charsets.UTF_8)
            if (!temp.renameTo(file)) {
                temp.copyTo(file, overwrite = true)
                temp.delete()
            }
            true
        } catch (_: Exception) { false }
    }
    fun load(context: Context, name: String): GifDocument? {
        val id = id(name) ?: return null
        val file = File(folder(context), "$id.gzgif")
        if (!file.isFile || file.length() > MAX_BYTES) return null
        return try { decode(file.readText(Charsets.UTF_8)) } catch (_: Exception) { null }
    }
}

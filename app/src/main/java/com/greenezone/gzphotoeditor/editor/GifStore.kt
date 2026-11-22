package com.greenezone.gzphotoeditor.editor

import android.content.Context
import android.util.AtomicFile
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Base64

/** Local-only GIF projects: source references plus order, tempo and output options. */
object GifStore {
    private const val MAX_FILES = 12
    private const val MAX_BYTES = 2_400_000
    private fun folder(context: Context) = File(context.filesDir, "gz_gif_projects").also { it.mkdirs() }
    private fun id(name: String): String? {
        val clean = name.trim().replace(Regex("\\s+"), " ").take(32)
        if (clean.isBlank()) return null
        return Base64.getUrlEncoder().withoutPadding().encodeToString(clean.toByteArray(Charsets.UTF_8))
    }
    fun encode(doc: GifDocument): String {
        require(GifMath.valid(doc))
        return JSONObject().apply {
            put("version", 3)
            put("delayMs", doc.delayMs)
            put("size", doc.size.name)
            put("fit", doc.fit.name)
            put("aspect", doc.aspect.name)
            put("forever", doc.forever)
            doc.repeatCount?.let { put("repeatCount", it) }
            if (doc.edits != EditDocument()) put("edits", EditDocumentCodec.encode(doc.edits))
            put("frames", JSONArray(doc.frames))
            doc.video?.let { clip ->
                put("video", JSONObject().apply {
                    put("uri", clip.uri); put("durationMs", clip.durationMs)
                    put("startMs", clip.startMs); put("endMs", clip.endMs)
                    put("fps", clip.fps); put("speed", clip.speedPercent)
                    put("reverse", clip.reverse)
                    put("width", clip.width); put("height", clip.height)
                    put("isGif", clip.isGif); put("loopCount", clip.loopCount)
                    put("times", JSONArray(clip.sourceTimesMs))
                })
            }
        }.toString().also { require(it.length <= MAX_BYTES) }
    }
    fun decode(data: String): GifDocument? {
        if (data.length > MAX_BYTES) return null
        return try {
            val obj = JSONObject(data)
            if (obj.optInt("version", 0) !in 1..3) return null
            val array = obj.optJSONArray("frames") ?: return null
            if (array.length() > GifMath.MAX_FRAMES) return null
            val list = (0 until array.length()).map { array.optString(it, "") }
            val v = obj.optJSONObject("video")
            val clip = if (v != null && obj.optInt("version") >= 2) {
                val times = v.optJSONArray("times")
                if (times != null && times.length() > GifVideoMath.MAX_OUTPUT_FRAMES) return null
                val duration = v.optLong("durationMs", 0)
                val stamps = if (times == null) emptyList() else (0 until times.length()).map { times.getLong(it) }
                if (stamps.any { it !in 0 until duration } || stamps.indices.any { it > 0 && stamps[it] <= stamps[it - 1] }) return null
                GifVideoClip(
                    uri = v.optString("uri", ""),
                    durationMs = duration,
                    startMs = v.optLong("startMs", 0),
                    endMs = v.optLong("endMs", 0),
                    fps = v.optInt("fps", 8),
                    speedPercent = v.optInt("speed", 100),
                    reverse = v.optBoolean("reverse", false),
                    width = v.optInt("width", 480), height = v.optInt("height", 480),
                    sourceTimesMs = stamps, isGif = v.optBoolean("isGif"),
                    loopCount = v.optInt("loopCount", -1)
                )
            } else null
            fun <T : Enum<T>> find(items: Array<T>, key: String, fallback: T): T =
                items.firstOrNull { it.name == obj.optString(key) } ?: fallback
            GifDocument(
                frames = list, delayMs = obj.optInt("delayMs", 350),
                size = find(GifSize.entries.toTypedArray(), "size", GifSize.MEDIUM),
                fit = find(GifFit.entries.toTypedArray(), "fit", GifFit.FIT),
                aspect = find(GifAspect.entries.toTypedArray(), "aspect", GifAspect.SQUARE),
                forever = obj.optBoolean("forever", true),
                repeatCount = if (obj.has("repeatCount")) obj.getInt("repeatCount").also {
                    if (it !in 1..65535) return null
                } else null,
                edits = if (obj.has("edits")) EditDocumentCodec.decode(obj.getString("edits")) ?: return null
                    else EditDocument(),
                video = clip
            ).takeIf { GifMath.valid(it) }
        } catch (_: Exception) { null }
    }
    fun names(context: Context): List<String> = folder(context).listFiles()?.mapNotNull {
        if (!it.isFile || !it.name.endsWith(".gzgif")) return@mapNotNull null
        try { String(Base64.getUrlDecoder().decode(it.name.removeSuffix(".gzgif")),
            Charsets.UTF_8) } catch (_: Exception) { null }
    }?.sortedBy { it.lowercase() } ?: emptyList()

    @Synchronized fun save(context: Context, name: String, doc: GifDocument): Boolean {
        val id = id(name) ?: return false
        val dir = folder(context)
        val file = File(dir, "$id.gzgif")
        if (!file.exists() && names(context).size >= MAX_FILES) return false
        return try {
            val data = encode(doc).toByteArray(Charsets.UTF_8)
            val atomic = AtomicFile(file)
            val stream = atomic.startWrite()
            try {
                stream.write(data)
                atomic.finishWrite(stream)
            } catch (e: Throwable) {
                atomic.failWrite(stream)
                throw e
            }
            true
        } catch (_: Exception) { false }
    }
    @Synchronized fun load(context: Context, name: String): GifDocument? {
        val id = id(name) ?: return null
        val file = File(folder(context), "$id.gzgif")
        if (!file.isFile || file.length() > MAX_BYTES) return null
        return try {
            val doc = AtomicFile(file).openRead().bufferedReader().use { decode(it.readText()) } ?: return null
            val previous = doc.video
            if (previous == null || previous.sourceTimesMs.isNotEmpty()) return doc
            // v2 saved only the edit parameters. Obtain actual geometry and frame
            // timestamps before offering the new Original options on these projects.
            val source = VideoGifSource.inspect(context, Uri.parse(previous.uri))
            doc.copy(video = source.copy(startMs = previous.startMs, endMs = previous.endMs,
                fps = previous.fps, speedPercent = previous.speedPercent, reverse = previous.reverse))
                .takeIf(GifMath::valid)
        } catch (_: Exception) { null }
    }
}

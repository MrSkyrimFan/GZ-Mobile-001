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
    internal fun projectName(name: String): String? =
        name.trim().replace(Regex("\\s+"), " ").take(32).takeUnless(String::isBlank)

    private fun id(name: String): String? = projectName(name)?.let {
        Base64.getUrlEncoder().withoutPadding().encodeToString(it.toByteArray(Charsets.UTF_8))
    }

    internal enum class SaveResult { SAVED, NAME_IN_USE, FULL, FAILED }
    fun encode(doc: GifDocument): String {
        require(GifMath.valid(doc))
        return JSONObject().apply {
            put("version", 4)
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
    fun decode(data: String): GifDocument? = readProject(data, null)

    private fun readProject(data: String, context: Context?): GifDocument? {
        if (data.length > MAX_BYTES) return null
        return try {
            val obj = JSONObject(data)
            val version = obj.optInt("version", 0)
            if (version !in 1..4) return null
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
            val document = GifDocument(
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
            ).takeIf { GifMath.valid(it) } ?: return null
            if (context == null || clip == null) document else restoreTimeline(context, document, clip, version)
        } catch (_: Exception) { null }
    }
    private fun restoreTimeline(context: Context, document: GifDocument, clip: GifVideoClip,
        version: Int): GifDocument? {
        val uri = Uri.parse(clip.uri)
        val source = when {
            // v2 saved edit parameters without frame timestamps or geometry.
            clip.sourceTimesMs.isEmpty() -> VideoGifSource.inspect(context, uri)
            version < 4 && !clip.isGif -> {
                // v3 used container duration. Keep existing projects readable offline.
                val visualEnd = try { VideoGifSource.visualDurationMs(context, uri) }
                    catch (_: Exception) { null }
                if (visualEnd == null || visualEnd >= clip.durationMs) return document
                clip.copy(durationMs = visualEnd, sourceTimesMs = clip.sourceTimesMs.takeWhile { it < visualEnd })
            }
            else -> return document
        }
        // A saved selection ending at the source boundary follows the corrected end.
        // Preserve explicit selections, even a deliberate hold inside the old audio tail.
        val end = if (clip.endMs == clip.durationMs && clip.startMs < source.durationMs)
            minOf(clip.endMs, source.durationMs) else clip.endMs
        val restored = source.copy(durationMs = maxOf(source.durationMs, end),
            startMs = clip.startMs, endMs = end, fps = clip.fps,
            speedPercent = clip.speedPercent, reverse = clip.reverse)
        return document.copy(video = restored).takeIf(GifMath::valid)
    }

    @Synchronized fun names(context: Context): List<String> = folder(context).listFiles()?.mapNotNull {
        val projectName = it.name.removeSuffix(".bak")
        if (!it.isFile || !projectName.endsWith(".gzgif")) return@mapNotNull null
        try { String(Base64.getUrlDecoder().decode(projectName.removeSuffix(".gzgif")),
            Charsets.UTF_8) } catch (_: Exception) { null }
    }?.distinct()?.sortedBy { it.lowercase() } ?: emptyList()

    fun save(context: Context, name: String, doc: GifDocument): Boolean =
        saveProject(context, name, doc, replaceExisting = true) == SaveResult.SAVED

    @Synchronized internal fun saveProject(
        context: Context,
        name: String,
        doc: GifDocument,
        replaceExisting: Boolean
    ): SaveResult {
        val id = id(name) ?: return SaveResult.FAILED
        val file = File(folder(context), "$id.gzgif")
        val exists = file.exists() || File(file.path + ".bak").isFile
        if (exists && !replaceExisting) return SaveResult.NAME_IN_USE
        if (!exists && names(context).size >= MAX_FILES) return SaveResult.FULL
        return try {
            val data = encode(doc).toByteArray(Charsets.UTF_8)
            require(data.size <= MAX_BYTES)
            val atomic = AtomicFile(file)
            val stream = atomic.startWrite()
            try {
                stream.write(data)
                atomic.finishWrite(stream)
            } catch (e: Throwable) {
                atomic.failWrite(stream)
                throw e
            }
            SaveResult.SAVED
        } catch (_: Exception) { SaveResult.FAILED }
    }

    @Synchronized internal fun deleteProject(context: Context, name: String): Boolean {
        val id = id(name) ?: return false
        val file = File(folder(context), "$id.gzgif")
        val backup = File(file.path + ".bak")
        val staged = File(file.path + ".new")
        if (!file.isFile && !backup.isFile) return false
        val files = listOf(staged, file, backup)
        if (files.any { it.exists() && !it.isFile }) return false
        // A legacy backup is authoritative until recovery. Remove it last.
        return try {
            files.all { !it.exists() || it.delete() }
        } catch (_: SecurityException) { false }
    }

    @Synchronized fun load(context: Context, name: String): GifDocument? {
        val id = id(name) ?: return null
        val file = File(folder(context), "$id.gzgif")
        // Legacy AtomicFile restores the backup first, including when there is no base file.
        val backup = File(file.path + ".bak")
        val recoverable = if (backup.exists()) backup else file
        if (!recoverable.isFile || recoverable.length() > MAX_BYTES) return null
        return try {
            AtomicFile(file).openRead().bufferedReader().use { readProject(it.readText(), context) }
        } catch (_: Exception) { null }
    }
}

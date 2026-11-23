package com.greenezone.gzphotoeditor.editor

import android.content.Context
import android.util.AtomicFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Base64

/** Versioned private JSON holding photo URIs and editable layer settings, never source image bytes. */
object ProStore {
    private const val VERSION = 6
    private const val LEGACY_ASPECT_MIN = .1f
    private const val LEGACY_ASPECT_MAX = 10f
    private const val MAX_BYTES = 3_000_000
    // A named save can carry one bounded document and one bounded reset baseline.
    private const val MAX_PROJECT_BYTES = MAX_BYTES * 2 + 512
    private const val RESET_DOCUMENT = "resetDocument"
    private const val MAX_SAVED = 12
    private const val DIR = "gz_pro_projects"

    private fun projectReadLimit(): Long =
        (Runtime.getRuntime().maxMemory() / 8L).coerceAtMost(MAX_PROJECT_BYTES.toLong())

    fun encode(document: ProDocument): String {
        require(ProMath.valid(document))
        val layers = JSONArray()
        document.layers.forEach { layer ->
            layers.put(JSONObject().apply {
                put("uri", layer.uri); put("name", layer.name)
                put("x", layer.x.toDouble()); put("y", layer.y.toDouble())
                put("scale", layer.scale.toDouble()); put("opacity", layer.opacity.toDouble())
                put("visible", layer.visible); put("blend", layer.blend.name)
                put("kind", layer.kind.name); put("aspect", layer.aspect.toDouble())
                put("rotate", layer.rotationDegrees.toDouble())
                put("effect", layer.effect.name); put("effectAmount", layer.effectAmount.toDouble())
                layer.mask?.let { mask ->
                    val obj = JSONObject().apply {
                        put("kind", mask.kind.name)
                        put("x", mask.centerX.toDouble()); put("y", mask.centerY.toDouble())
                        put("width", mask.width.toDouble()); put("height", mask.height.toDouble())
                        put("invert", mask.inverted)
                        put("feather", mask.feather.toDouble())
                        mask.anchor?.let { anchor ->
                            put("imageAnchor", JSONObject().apply {
                                put("x", anchor.x.toDouble())
                                put("y", anchor.y.toDouble())
                                put("scale", anchor.scale.toDouble())
                                put("rotation", anchor.rotation.toDouble())
                                put("canvasAspect", anchor.canvasAspect.toDouble())
                                put("imageAspect", anchor.imageAspect.toDouble())
                            })
                        }
                        put("points", JSONArray().also { arr ->
                            mask.points.forEach { point ->
                                arr.put(JSONArray().put(point.x.toDouble()).put(point.y.toDouble()))
                            }
                        })
                    }
                    put("mask", obj)
                }
                put("retouches", JSONArray().apply {
                    layer.retouches.forEach { mark ->
                        put(JSONObject().apply {
                            put("kind", mark.kind.name)
                            put("sx", mark.sourceX.toDouble()); put("sy", mark.sourceY.toDouble())
                            put("tx", mark.targetX.toDouble()); put("ty", mark.targetY.toDouble())
                            put("radius", mark.radius.toDouble())
                            put("strength", mark.strength.toDouble())
                        })
                    }
                })
                put("edits", EditDocumentCodec.encode(layer.edits))
            })
        }
        val legacyAspectRange = LEGACY_ASPECT_MIN..LEGACY_ASPECT_MAX
        val extendedAspect = document.sourceAspectRatio != null || document.layers.any { layer ->
            val anchor = layer.mask?.anchor
            layer.aspect !in legacyAspectRange ||
                (anchor != null && (anchor.canvasAspect !in legacyAspectRange ||
                    anchor.imageAspect !in legacyAspectRange))
        }
        return JSONObject().apply {
            put("version", if (extendedAspect) VERSION else 5)
            put("aspect", document.aspect.name)
            document.sourceAspectRatio?.let { put("sourceAspectRatio", it.toDouble()) }
            put("background", document.background.name)
            put("layers", layers)
        }.toString().also { require(it.toByteArray(StandardCharsets.UTF_8).size <= MAX_BYTES) }
    }
    private inline fun <reified T: Enum<T>> value(name: String, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: fallback

    fun decode(data: String): ProDocument? {
        if (data.length > MAX_BYTES) return null
        return try {
            val obj = JSONObject(data)
            val version = obj.optInt("version", -1)
            if (version !in 1..VERSION) return null
            val array = obj.optJSONArray("layers") ?: return null
            if (array.length() !in 1..ProMath.MAX_LAYERS) return null
            fun unit(o: JSONObject, key: String, default: Double, lo: Float, hi: Float): Float {
                val raw = o.optDouble(key, default).toFloat()
                if (!raw.isFinite() || raw < lo || raw > hi) throw IllegalArgumentException(key)
                return raw
            }
            val aspectMin = if (version >= 6) ProMath.MIN_SOURCE_ASPECT else LEGACY_ASPECT_MIN
            val aspectMax = if (version >= 6) ProMath.MAX_SOURCE_ASPECT else LEGACY_ASPECT_MAX
            val layers = (0 until array.length()).map { idx ->
                val o = array.optJSONObject(idx) ?: return null
                val uri = o.optString("uri", "")
                if (uri.length > 4096) return null
                val edits = EditDocumentCodec.decode(o.optString("edits", "")) ?: return null
                val mask = o.optJSONObject("mask")?.let { json ->
                    val raw = json.optJSONArray("points") ?: JSONArray()
                    if (raw.length() > ProToolMath.MAX_LASSO_POINTS) return null
                    val points = (0 until raw.length()).map { k ->
                        val p = raw.optJSONArray(k) ?: return null
                        if (p.length() != 2) return null
                        BrushPoint(p.optDouble(0, Double.NaN).toFloat(),
                            p.optDouble(1, Double.NaN).toFloat())
                    }
                    ProSelection(
                        kind = value(json.optString("kind", ""), ProSelectionKind.RECTANGLE),
                        centerX = unit(json, "x", .5, 0f, 1f),
                        centerY = unit(json, "y", .5, 0f, 1f),
                        width = unit(json, "width", .6, 0f, 1f),
                        height = unit(json, "height", .6, 0f, 1f),
                        points = points, inverted = json.optBoolean("invert", false),
                        feather = unit(json, "feather", 0.0, 0f, .2f),
                        anchor = if (version >= 5 && json.has("imageAnchor")) {
                            val anchor = json.getJSONObject("imageAnchor")
                            ProMaskAnchor(
                                unit(anchor, "x", Double.NaN, 0f, 1f),
                                unit(anchor, "y", Double.NaN, 0f, 1f),
                                unit(anchor, "scale", Double.NaN, .1f, 2f),
                                unit(anchor, "rotation", Double.NaN, -180f, 180f),
                                unit(anchor, "canvasAspect", Double.NaN, aspectMin, aspectMax),
                                unit(anchor, "imageAspect", Double.NaN, aspectMin, aspectMax)
                            )
                        } else null
                    ).takeIf(ProToolMath::valid) ?: return null
                }
                val rawRetouch = o.optJSONArray("retouches") ?: JSONArray()
                if (rawRetouch.length() > ProToolMath.MAX_RETOUCH) return null
                val retouches = (0 until rawRetouch.length()).map { k ->
                    val item = rawRetouch.optJSONObject(k) ?: return null
                    ProRetouchMark(
                        kind = value(item.optString("kind", ""), ProRetouchKind.CLONE),
                        sourceX = unit(item, "sx", .5, 0f, 1f),
                        sourceY = unit(item, "sy", .5, 0f, 1f),
                        targetX = unit(item, "tx", .5, 0f, 1f),
                        targetY = unit(item, "ty", .5, 0f, 1f),
                        radius = unit(item, "radius", .04, .005f, .2f),
                        strength = unit(item, "strength", .85, .1f, 1f)
                    ).takeIf(ProToolMath::valid) ?: return null
                }
                ProLayer(
                    uri = uri, edits = edits, name = o.optString("name", "").take(42),
                    x = unit(o, "x", .5, 0f, 1f), y = unit(o, "y", .5, 0f, 1f),
                    scale = unit(o, "scale", 1.0, .1f, 2f),
                    opacity = unit(o, "opacity", 1.0, 0f, 1f),
                    visible = o.optBoolean("visible", true),
                    blend = value(o.optString("blend", ""), ProBlend.NORMAL),
                    kind = value(o.optString("kind", ""), ProLayerKind.IMAGE),
                    aspect = unit(o, "aspect", 1.0, aspectMin, aspectMax),
                    rotationDegrees = unit(o, "rotate", 0.0, -180f, 180f),
                    mask = mask, retouches = retouches,
                    effect = value(o.optString("effect", ""), ProPixelEffect.NONE),
                    effectAmount = unit(o, "effectAmount", .5, 0f, 1f)
                )
            }
            val sourceAspectRatio = if (version >= 6 && obj.has("sourceAspectRatio")) {
                unit(obj, "sourceAspectRatio", Double.NaN, ProMath.MIN_SOURCE_ASPECT, ProMath.MAX_SOURCE_ASPECT)
            } else null
            ProDocument(layers,
                value(obj.optString("aspect", ""), ProAspect.SQUARE),
                value(obj.optString("background", ""), ProBackground.WHITE),
                sourceAspectRatio)
                .takeIf { ProMath.valid(it) }
        } catch (_: Exception) { null }
    }

    internal data class SavedProject(val document: ProDocument, val resetDocument: ProDocument)

    private fun encodeProject(document: ProDocument, resetDocument: ProDocument?): ByteArray {
        val encoded = encode(document)
        if (resetDocument == null || resetDocument == document) return encoded.toByteArray(Charsets.UTF_8)
        val baseline = if (resetDocument.layers.isEmpty()) {
            JSONObject().apply {
                put("empty", true)
                put("aspect", resetDocument.aspect.name)
                put("background", resetDocument.background.name)
                resetDocument.sourceAspectRatio?.let { put("sourceAspectRatio", it.toDouble()) }
            }
        } else JSONObject(encode(resetDocument))
        // The optional field leaves the existing root document and codec version intact.
        return JSONObject(encoded).put(RESET_DOCUMENT, baseline).toString().toByteArray(Charsets.UTF_8)
            .also { require(it.size <= MAX_PROJECT_BYTES) }
    }

    private fun decodeProject(data: String): SavedProject? {
        val json = JSONObject(data)
        val reset = json.remove(RESET_DOCUMENT) as? JSONObject
        val document = decode(if (reset == null) data else json.toString()) ?: return null
        val baseline = reset?.let {
            if (it.optBoolean("empty")) {
                ProDocument(
                    aspect = value(it.optString("aspect"), ProAspect.SQUARE),
                    background = value(it.optString("background"), ProBackground.WHITE),
                    sourceAspectRatio = it.optDouble("sourceAspectRatio").toFloat()
                        .takeIf(ProMath::validSourceAspect)
                )
            } else decode(it.toString())
        }
        // A legacy or unreadable optional baseline must never clear the user's saved work.
        return SavedProject(document, baseline ?: document)
    }

    private fun dir(context: Context): File = File(context.filesDir, DIR).also { it.mkdirs() }
    private fun key(name: String): String? {
        val cleaned = name.trim().replace(Regex("\\s+"), " ").take(32)
        if (cleaned.isBlank()) return null
        return Base64.getUrlEncoder().withoutPadding()
            .encodeToString(cleaned.toByteArray(StandardCharsets.UTF_8))
    }
    @Synchronized fun names(context: Context): List<String> =
        dir(context).listFiles()?.mapNotNull {
            if (!it.isFile) return@mapNotNull null
            // AtomicFile can leave the legacy .bak file after an interrupted write.
            val stem = when {
                it.name.endsWith(".gzpro") -> it.name.removeSuffix(".gzpro")
                it.name.endsWith(".gzpro.bak") -> it.name.removeSuffix(".gzpro.bak")
                else -> return@mapNotNull null
            }
            try {
                String(Base64.getUrlDecoder().decode(stem), Charsets.UTF_8)
            } catch (_: Exception) { null }
        }?.distinct()?.sortedBy { it.lowercase() } ?: emptyList()

    @Synchronized fun save(
        context: Context,
        name: String,
        document: ProDocument,
        resetDocument: ProDocument? = null
    ): Boolean {
        val id = key(name) ?: return false
        val file = File(dir(context), "$id.gzpro")
        val recoverableBackup = File(file.path + ".bak")
        if (!file.exists() && !recoverableBackup.isFile && names(context).size >= MAX_SAVED) return false
        return try {
            val payload = encodeProject(document, resetDocument)
            if (payload.size.toLong() > projectReadLimit()) return false
            val atomic = AtomicFile(file)
            val stream = atomic.startWrite()
            try {
                stream.write(payload)
                atomic.finishWrite(stream)
            } catch (failed: Throwable) {
                atomic.failWrite(stream)
                throw failed
            }
            true
        } catch (_: Exception) { false }
    }

    fun load(context: Context, name: String): ProDocument? = loadProject(context, name)?.document

    @Synchronized internal fun loadProject(context: Context, name: String): SavedProject? {
        val id = key(name) ?: return null
        val base = File(dir(context), "$id.gzpro")
        val backup = File(base.path + ".bak")
        // Fail before allocating strings or reading hostile oversize projects.
        // On legacy backup-only recovery AtomicFile will rename the backup to base.
        val recoverable = if (backup.exists()) backup else base
        if (!recoverable.isFile || recoverable.length() > projectReadLimit()) return null
        val atomic = AtomicFile(base)
        return try {
            val data = atomic.openRead().bufferedReader(Charsets.UTF_8).use { reader ->
                val text = StringBuilder()
                val buf = CharArray(4096)
                while (true) {
                    val n = reader.read(buf)
                    if (n < 0) break
                    if (text.length + n > MAX_PROJECT_BYTES) return null
                    text.append(buf, 0, n)
                }
                text.toString()
            }
            decodeProject(data)
        } catch (_: Exception) { null }
    }

    @Synchronized fun delete(context: Context, name: String): Boolean {
        val id = key(name) ?: return false
        val file = File(dir(context), "$id.gzpro")
        val projectFiles = listOf(file, File(file.path + ".bak"), File(file.path + ".new"))
        if (projectFiles.none { it.exists() }) return false
        AtomicFile(file).delete()
        return projectFiles.none { it.exists() }
    }
}

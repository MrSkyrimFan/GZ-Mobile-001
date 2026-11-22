package com.greenezone.gzphotoeditor.editor

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Base64

/** Versioned private JSON holding photo URIs and editable layer settings, never source image bytes. */
object ProStore {
    private const val VERSION = 3
    private const val MAX_BYTES = 3_000_000
    private const val MAX_SAVED = 12
    private const val DIR = "gz_pro_projects"

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
        return JSONObject().apply {
            put("version", VERSION)
            put("aspect", document.aspect.name)
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
            if (obj.optInt("version", -1) !in 1..VERSION) return null
            val array = obj.optJSONArray("layers") ?: return null
            if (array.length() !in 1..ProMath.MAX_LAYERS) return null
            fun unit(o: JSONObject, key: String, default: Double, lo: Float, hi: Float): Float {
                val raw = o.optDouble(key, default).toFloat()
                if (!raw.isFinite() || raw < lo || raw > hi) throw IllegalArgumentException(key)
                return raw
            }
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
                        points = points, inverted = json.optBoolean("invert", false)
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
                    aspect = unit(o, "aspect", 1.0, .1f, 10f),
                    rotationDegrees = unit(o, "rotate", 0.0, -180f, 180f),
                    mask = mask, retouches = retouches,
                    effect = value(o.optString("effect", ""), ProPixelEffect.NONE),
                    effectAmount = unit(o, "effectAmount", .5, 0f, 1f)
                )
            }
            ProDocument(layers,
                value(obj.optString("aspect", ""), ProAspect.SQUARE),
                value(obj.optString("background", ""), ProBackground.WHITE))
                .takeIf { ProMath.valid(it) }
        } catch (_: Exception) { null }
    }

    private fun dir(context: Context): File = File(context.filesDir, DIR).also { it.mkdirs() }
    private fun key(name: String): String? {
        val cleaned = name.trim().replace(Regex("\\s+"), " ").take(32)
        if (cleaned.isBlank()) return null
        return Base64.getUrlEncoder().withoutPadding()
            .encodeToString(cleaned.toByteArray(StandardCharsets.UTF_8))
    }
    fun names(context: Context): List<String> =
        dir(context).listFiles()?.mapNotNull {
            if (!it.isFile || !it.name.endsWith(".gzpro")) return@mapNotNull null
            try {
                String(Base64.getUrlDecoder().decode(it.name.removeSuffix(".gzpro")), Charsets.UTF_8)
            } catch (_: Exception) { null }
        }?.sortedBy { it.lowercase() } ?: emptyList()

    fun save(context: Context, name: String, document: ProDocument): Boolean {
        val id = key(name) ?: return false
        val folder = dir(context)
        val file = File(folder, "$id.gzpro")
        if (!file.exists() && names(context).size >= MAX_SAVED) return false
        return try {
            val payload = encode(document)
            val tmp = File(folder, "$id.new")
            tmp.writeText(payload, Charsets.UTF_8)
            if (!tmp.renameTo(file)) {
                tmp.copyTo(file, overwrite = true)
                tmp.delete()
            }
            true
        } catch (_: Exception) { false }
    }
    fun load(context: Context, name: String): ProDocument? {
        val id = key(name) ?: return null
        val file = File(dir(context), "$id.gzpro")
        if (!file.isFile || file.length() > MAX_BYTES) return null
        return try { decode(file.readText(Charsets.UTF_8)) } catch (_: Exception) { null }
    }
    fun delete(context: Context, name: String): Boolean {
        val id = key(name) ?: return false
        return File(dir(context), "$id.gzpro").delete()
    }
}

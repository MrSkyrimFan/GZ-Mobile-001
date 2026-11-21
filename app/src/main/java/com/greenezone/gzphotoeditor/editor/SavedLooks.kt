package com.greenezone.gzphotoeditor.editor

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * User-created looks live in app-private preferences. Only grade parameters
 * are saved, never image files, URI paths, EXIF data, or account identifiers.
 */
object SavedLooks {
    private const val PREFS = "gzphotoeditor.saved_looks.v1"
    private const val LIMIT = 20

    fun load(context: Context): List<Look> {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).all.mapNotNull { (name, payload) ->
            val encoded = payload as? String ?: return@mapNotNull null
            try {
                val data = JSONObject(encoded)
                val grade = ControlCatalog.all.fold(Grade()) { current, control ->
                    val raw = data.optDouble(control.name, 0.0).toFloat()
                    val value = if (raw.isFinite()) raw.coerceIn(control.min, control.max) else 0f
                    control.write(current, value)
                }
                var restored = grade
                for (channel in CurveChannel.entries) {
                    val array = data.optJSONArray("curve." + channel.name) ?: continue
                    if (array.length() != 5) continue
                    val values = (0..4).map { i -> array.optDouble(i, Double.NaN).toFloat() }
                    if (values.all { it.isFinite() && it in 0f..1f }) {
                        restored = CurveMath.put(restored, channel, CurvePoints(values))
                    }
                }
                Look(name, restored)
            } catch (_: Exception) {
                null
            }
        }.sortedBy { it.name.lowercase() }
    }

    fun save(context: Context, requestedName: String, grade: Grade): Boolean {
        val name = requestedName.trim().replace(Regex("\\s+"), " ").take(28)
        if (name.isBlank()) return false
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.contains(name) && prefs.all.size >= LIMIT) return false
        val data = JSONObject()
        for (control in ControlCatalog.all) data.put(control.name, control.read(grade).toDouble())
        for (channel in CurveChannel.entries) {
            val arr = JSONArray()
            CurveMath.get(grade, channel).values.forEach { arr.put(it.toDouble()) }
            data.put("curve." + channel.name, arr)
        }
        prefs.edit().putString(name, data.toString()).apply()
        return true
    }

    fun remove(context: Context, name: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(name).apply()
    }
}

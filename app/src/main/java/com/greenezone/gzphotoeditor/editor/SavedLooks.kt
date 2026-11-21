package com.greenezone.gzphotoeditor.editor

import android.content.Context
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
                Look(name, grade)
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
        prefs.edit().putString(name, data.toString()).apply()
        return true
    }

    fun remove(context: Context, name: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(name).apply()
    }
}

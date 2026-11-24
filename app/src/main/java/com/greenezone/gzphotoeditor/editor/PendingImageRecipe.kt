package com.greenezone.gzphotoeditor.editor

import org.json.JSONObject

internal data class PendingImageRecipe(val settings: ExportSettings, val payload: String) {
    fun encode(): String = JSONObject().apply {
        put("format", settings.format.name)
        put("preset", settings.preset.name)
        put("placement", settings.placement.name)
        put("background", settings.background.name)
        put("quality", settings.quality)
        put("payload", payload)
    }.toString()

    companion object {
        fun decode(encoded: String): PendingImageRecipe? = try {
            val fields = JSONObject(encoded)
            val settings = ExportSettings(
                ImageExportFormat.valueOf(fields.getString("format")),
                SocialPreset.valueOf(fields.getString("preset")),
                ExportPlacement.valueOf(fields.getString("placement")),
                ExportBackground.valueOf(fields.getString("background")),
                fields.getInt("quality").coerceIn(1, 100)
            )
            PendingImageRecipe(settings, fields.getString("payload"))
        } catch (_: Exception) { null }
    }
}

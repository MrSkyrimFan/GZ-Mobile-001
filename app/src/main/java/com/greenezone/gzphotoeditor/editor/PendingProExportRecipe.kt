package com.greenezone.gzphotoeditor.editor

import org.json.JSONObject

/** Immutable layered project and export dimensions recorded before Android opens its picker. */
internal data class PendingProExportRecipe(
    val document: ProDocument,
    val resetDocument: ProDocument,
    val settings: ExportSettings,
    val size: ProPixelSize
) {
    fun encode(): String {
        val content = JSONObject()
            .put("version", 1)
            .put("width", size.width)
            .put("height", size.height)
            .put("project", String(ProStore.encodeProject(document, resetDocument), Charsets.UTF_8))
        return PendingImageRecipe(settings, content.toString()).encode()
    }

    companion object {
        fun decode(pending: PendingExport): PendingProExportRecipe? {
            if (pending.workspace != ExportWorkspace.PRO) return null
            return try {
                val recipe = PendingImageRecipe.decode(pending.recipe) ?: return null
                if (recipe.settings.format.mime != pending.mime) return null
                val content = JSONObject(recipe.payload)
                if (content.getInt("version") != 1) return null
                val size = ProPixelSize(content.getInt("width"), content.getInt("height"))
                if (size.edge > ProMath.MAX_EXPORT_EDGE || size.pixelCount > ImagePipeline.MAX_EXPORT_PIXELS) return null
                val project = ProStore.decodeProject(content.getString("project")) ?: return null
                PendingProExportRecipe(project.document, project.resetDocument, recipe.settings, size)
            } catch (_: OutOfMemoryError) {
                null
            } catch (_: Exception) {
                null
            }
        }
    }
}

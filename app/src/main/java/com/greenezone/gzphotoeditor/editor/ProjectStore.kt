package com.greenezone.gzphotoeditor.editor

import android.content.Context
import android.net.Uri
import java.nio.charset.StandardCharsets
import java.util.Base64

data class SavedProject(val name: String, val image: Uri, val document: EditDocument)

/** Up to twelve named local projects; original photos stay in the system media store. */
object ProjectStore {
    private const val PREFS = "gzphotoeditor.projects.v1"
    private const val LIMIT = 12

    fun load(context: Context): List<SavedProject> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).all.mapNotNull { (key, raw) ->
            val value = raw as? String ?: return@mapNotNull null
            try {
                val name = String(Base64.getUrlDecoder().decode(key), StandardCharsets.UTF_8)
                val split = value.indexOf('\n')
                if (split <= 0) return@mapNotNull null
                val uriText = String(
                    Base64.getUrlDecoder().decode(value.substring(0, split)),
                    StandardCharsets.UTF_8
                )
                val uri = Uri.parse(uriText)
                if (uri.scheme != "content") return@mapNotNull null
                val doc = EditDocumentCodec.decode(value.substring(split + 1)) ?: return@mapNotNull null
                SavedProject(name, uri, doc)
            } catch (_: Exception) { null }
        }.sortedBy { it.name.lowercase() }

    fun save(context: Context, nameInput: String, image: Uri, document: EditDocument): Boolean {
        if (image.scheme != "content") return false
        val name = nameInput.trim().replace(Regex("\\s+"), " ").take(28)
        if (name.isBlank()) return false
        val key = Base64.getUrlEncoder().withoutPadding().encodeToString(
            name.toByteArray(StandardCharsets.UTF_8)
        )
        val value = Base64.getUrlEncoder().withoutPadding().encodeToString(
            image.toString().toByteArray(StandardCharsets.UTF_8)
        ) + "\n" + EditDocumentCodec.encode(document)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.contains(key) && prefs.all.size >= LIMIT) return false
        return prefs.edit().putString(key, value).commit()
    }

    fun delete(context: Context, name: String) {
        val key = Base64.getUrlEncoder().withoutPadding().encodeToString(
            name.toByteArray(StandardCharsets.UTF_8)
        )
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(key).apply()
    }
}

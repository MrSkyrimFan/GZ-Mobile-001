package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenezone.gzphotoeditor.editor.*

@Composable
internal fun GifOptions(content: @Composable RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp), content = content)
}

@Composable
internal fun GifLooksDeck(edits: EditDocument, enabled: Boolean, commit: (EditDocument) -> Unit) {
    GifOptions {
        DockChip("None", edits.grade == Grade(), enabled) { commit(edits.copy(grade = Grade())) }
        Looks.all.forEach { look ->
            DockChip(look.name, edits.grade == look.grade, enabled) {
                commit(edits.copy(grade = look.grade))
            }
        }
    }
}

@Composable
internal fun GifCaptionDeck(
    edits: EditDocument, enabled: Boolean, commit: (EditDocument) -> Unit,
    slide: (EditDocument) -> Unit, finish: () -> Unit
) {
    val caption = edits.creative.text.firstOrNull()
    var input by remember { mutableStateOf<String?>(null) }
    fun replace(value: TextOverlay?): EditDocument {
        val text = listOfNotNull(value) + edits.creative.text.drop(1)
        return edits.copy(creative = edits.creative.copy(text = text))
    }
    GifOptions {
        DockChip(if (caption == null) "Add caption" else "Edit caption", false, enabled) {
            input = caption?.content.orEmpty()
        }
        if (caption != null) {
            DockChip("Remove caption", false, enabled) { commit(replace(null)) }
            listOf("Top" to .16f, "Center" to .5f, "Bottom" to .84f).forEach { (label, y) ->
                DockChip(label, caption.y == y, enabled) { commit(replace(caption.copy(y = y))) }
            }
        }
    }
    if (caption != null) {
        SingleAdjustment("Text size", caption.size, .025f, .18f, enabled,
            { commit(replace(caption.copy(size = .075f))) },
            { slide(replace(caption.copy(size = it))) }, finish)
        GifOptions {
            TextFont.entries.forEach { font ->
                DockChip(font.label, caption.font == font, enabled) { commit(replace(caption.copy(font = font))) }
            }
        }
    }
    if (input != null) AlertDialog(
        onDismissRequest = { input = null },
        title = { Text("Caption") },
        text = { OutlinedTextField(input.orEmpty(), { input = it.take(160) }, label = { Text("Text on every frame") }) },
        confirmButton = { TextButton(enabled = !input.isNullOrBlank(), onClick = {
            val item = caption ?: TextOverlay(y = .84f, width = .9f, bold = true, outline = .14f)
            commit(replace(item.copy(content = input.orEmpty().trim())))
            input = null
        }) { Text("Apply") } },
        dismissButton = { TextButton(onClick = { input = null }) { Text("Cancel") } }
    )
}

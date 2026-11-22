package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenezone.gzphotoeditor.editor.ProDocument

internal enum class ProSection(val title: String, val group: String) {
    LAYERS("Layers", "Composition"), MOVE("Transform", "Composition"),
    BLEND("Blend", "Composition"), CANVAS("Canvas", "Composition"),
    SELECT("Select & mask", "Selection and retouch"), RETOUCH("Clone & repair", "Selection and retouch"),
    PAINT("Brush", "Create"), STYLE("Text & shapes", "Create"),
    GRADE("Tone & color", "Adjust"), PIXEL("Layer effects", "Adjust")
}

@Composable
internal fun ProToolChooser(selected: ProSection, onTool: (ProSection) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tools") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            ProSection.entries.groupBy { it.group }.forEach { (group, tools) ->
                Text(group, fontSize = 12.sp, color = EditorMuted,
                    modifier = Modifier.padding(top = 10.dp, bottom = 3.dp))
                tools.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth()) {
                        pair.forEach { tool ->
                            Box(Modifier.weight(1f).clickable { onTool(tool) }.padding(vertical = 12.dp)) {
                                Text(tool.title, fontSize = 14.sp,
                                    color = if (tool == selected) EditorMint else EditorWhite)
                            }
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } })
}

@Composable
internal fun ProLayerStack(
    document: ProDocument, selected: Int, onSelect: (Int) -> Unit,
    onVisibility: (Int) -> Unit, onAdd: () -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Layer stack") }, text = {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            document.layers.indices.reversed().forEach { index ->
                val layer = document.layers[index]
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).clickable { onSelect(index) }.padding(vertical = 8.dp)) {
                        Text(layer.name, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.SemiBold,
                            color = if (index == selected) EditorMint else EditorWhite)
                        Text(listOfNotNull(layer.kind.label, layer.blend.label,
                            "${(100 * layer.opacity).toInt()}%", if (layer.mask != null) "Mask" else null)
                            .joinToString(" · "), fontSize = 11.sp, color = EditorMuted)
                    }
                    TextButton(onClick = { onVisibility(index) }, modifier = Modifier.semantics {
                        contentDescription = (if (layer.visible) "Hide " else "Show ") + layer.name
                    }) { Text(if (layer.visible) "Visible" else "Hidden", color = EditorMuted) }
                }
                HorizontalDivider()
            }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
       dismissButton = { TextButton(onClick = onAdd, enabled = document.layers.size <
           com.greenezone.gzphotoeditor.editor.ProMath.MAX_LAYERS) { Text("+ Add layer") } })
}

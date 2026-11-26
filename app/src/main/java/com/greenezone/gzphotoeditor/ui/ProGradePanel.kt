package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenezone.gzphotoeditor.editor.ControlCatalog
import com.greenezone.gzphotoeditor.editor.Grade
import com.greenezone.gzphotoeditor.editor.ProLayer
import com.greenezone.gzphotoeditor.editor.ProLayerKind

/** A property inspector for the selected image or adjustment layer. */
@Composable
internal fun ProGradePanel(
    layer: ProLayer,
    enabled: Boolean,
    canAdd: Boolean,
    onAdd: () -> Unit,
    onGrade: (Grade) -> Unit,
    onFinish: () -> Unit,
    onCommitGrade: (Grade) -> Unit
) {
    if (layer.kind != ProLayerKind.IMAGE && layer.kind != ProLayerKind.ADJUSTMENT) {
        Text("Choose an image or adjustment layer to edit its properties.",
            color = EditorMuted, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 13.dp))
        TextButton(enabled = enabled && canAdd, onClick = onAdd) { Text("Add adjustment layer") }
        return
    }

    val scope = if (layer.kind == ProLayerKind.IMAGE) "Changes apply to this image layer."
        else if (layer.mask != null) "Changes apply to the masked area of the composition below."
        else "Changes apply to the visible composition below this layer."
    Text(scope, color = EditorMuted, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 13.dp))

    val controls = ControlCatalog.all
    var property by remember { mutableStateOf(controls.first().name) }
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.padding(horizontal = 13.dp)) {
        TextButton(onClick = { expanded = true }, enabled = enabled,
            modifier = Modifier.heightIn(min = 48.dp)) { Text("Property: " + property) }
        DropdownMenu(expanded = enabled && expanded, onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 320.dp)) {
            (controls.map { it.name } + "Curve points").forEach { name ->
                DropdownMenuItem(text = { Text(name) }, onClick = {
                    expanded = false
                    property = name
                })
            }
        }
    }
    if (property == "Curve points") {
        CurveDeck(layer.edits, enabled, onCommit = { onCommitGrade(it.grade) },
            onSlide = { onGrade(it.grade) }, onFinished = onFinish)
    } else {
        val control = controls.first { it.name == property }
        SingleAdjustment(
            title = control.name,
            value = control.read(layer.edits.grade),
            low = control.min,
            high = control.max,
            enabled = enabled,
            onReset = { onCommitGrade(control.write(layer.edits.grade, 0f)) },
            onChange = { onGrade(control.write(layer.edits.grade, it)) },
            onFinished = onFinish
        )
    }
}

package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenezone.gzphotoeditor.editor.Grade
import com.greenezone.gzphotoeditor.editor.ProLayer
import com.greenezone.gzphotoeditor.editor.ProLayerKind

@Composable
internal fun ProGradePanel(
    layer: ProLayer,
    canAdd: Boolean,
    onAdd: () -> Unit,
    onGrade: (Grade) -> Unit,
    onFinish: () -> Unit,
    onCommitGrade: (Grade) -> Unit
) {
    TextButton(enabled = canAdd, onClick = onAdd) {
        Text("+ Add adjustment layer", color = EditorMint)
    }
    if (layer.kind != ProLayerKind.IMAGE && layer.kind != ProLayerKind.ADJUSTMENT) {
        Text("Add an adjustment layer above this object to change its tones or colors.",
            color = EditorMuted, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp))
        return
    }
    GradeAdjustmentDeck(layer.edits, true,
        commit = { onCommitGrade(it.grade) },
        slide = { onGrade(it.grade) },
        finish = onFinish)
}

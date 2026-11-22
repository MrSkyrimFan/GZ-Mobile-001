package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenezone.gzphotoeditor.editor.ProLayer
import com.greenezone.gzphotoeditor.editor.ProLayerKind
import com.greenezone.gzphotoeditor.editor.ProPixelEffect
import com.greenezone.gzphotoeditor.editor.ProRetouchKind
import com.greenezone.gzphotoeditor.editor.ProSelection
import com.greenezone.gzphotoeditor.editor.ProSelectionKind
import com.greenezone.gzphotoeditor.editor.ProToolMath

/** Actual selection-mask controls. No photographic Light/Color sliders in Pro. */
@Composable
internal fun ProSelectionPanel(
    layer: ProLayer,
    selectedKind: ProSelectionKind,
    onKind: (ProSelectionKind) -> Unit,
    draft: ProSelection?,
    onApply: () -> Unit,
    onInvert: () -> Unit,
    onRemove: () -> Unit,
    onDiscard: () -> Unit,
    onFeather: (Float) -> Unit,
    onResetFeather: () -> Unit,
    onFinish: () -> Unit
) {
    if (layer.kind != ProLayerKind.IMAGE && layer.kind != ProLayerKind.ADJUSTMENT) {
        Text("Choose an image or adjustment layer to create an editable mask.",
            color = EditorMuted, fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 13.dp))
        return
    }
    Text("Drag on the canvas to select an area. Tap Apply mask to keep it.",
        color = EditorWhite, fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = 13.dp))
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
        .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ProSelectionKind.entries.forEach { option ->
            DockChip(option.label, option == selectedKind) { onKind(option) }
        }
    }
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
        .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        DockChip("Apply layer mask", false,
            enabled = draft?.let(ProToolMath::valid) == true, onClick = onApply)
        DockChip("Invert mask", layer.mask?.inverted == true,
            enabled = layer.mask != null, onClick = onInvert)
        DockChip("Remove mask", false,
            enabled = layer.mask != null, onClick = onRemove)
        DockChip("Discard selection", false,
            enabled = draft != null, onClick = onDiscard)
    }
    layer.mask?.let { mask ->
        SingleAdjustment("Feather", mask.feather, 0f, .2f, true,
            onReset = onResetFeather, onChange = onFeather, onFinished = onFinish)
    }
}

@Composable
internal fun ProRetouchPanel(
    layer: ProLayer,
    mode: ProRetouchKind,
    onMode: (ProRetouchKind) -> Unit,
    radius: Float,
    onRadius: (Float) -> Unit,
    strength: Float,
    onStrength: (Float) -> Unit,
    sourceChosen: Boolean,
    onClearSource: () -> Unit,
    onUndoMark: () -> Unit,
    onClearMarks: () -> Unit
) {
    if (layer.kind != ProLayerKind.IMAGE) {
        Text("Choose an image layer to use Clone stamp or Soft repair.",
            color = EditorMuted, fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 13.dp))
        return
    }
    Text(if (sourceChosen) "Press and hold a destination to stamp. Reset source to sample again."
        else "Press and hold a clean source, then hold each area to repair. A short tap hides the controls.",
        color = EditorWhite, fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = 13.dp))
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
        .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ProRetouchKind.entries.forEach { option ->
            DockChip(option.label, mode == option) { onMode(option) }
        }
        DockChip("Reset source", false, enabled = sourceChosen, onClick = onClearSource)
    }
    Text("Repair radius", color = EditorMuted, fontSize = 11.sp,
        modifier = Modifier.padding(start = 14.dp))
    Slider(value = radius, valueRange = .005f.. .20f,
        onValueChange = onRadius,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
    Text("Brush strength", color = EditorMuted, fontSize = 11.sp,
        modifier = Modifier.padding(start = 14.dp))
    Slider(value = strength, valueRange = .1f..1f,
        onValueChange = onStrength,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
        .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        DockChip("Undo last repair", false,
            enabled = layer.retouches.isNotEmpty(), onClick = onUndoMark)
        DockChip("Clear repairs", false,
            enabled = layer.retouches.isNotEmpty(), onClick = onClearMarks)
    }
}

@Composable
internal fun ProPixelEffectPanel(
    layer: ProLayer,
    canAdd: Boolean,
    onAdd: () -> Unit,
    onEffect: (ProPixelEffect) -> Unit,
    onAmount: (Float) -> Unit,
    onAmountDone: () -> Unit
) {
    if (layer.kind != ProLayerKind.ADJUSTMENT) {
        TextButton(enabled = canAdd, onClick = onAdd) {
            Text("+ Add a compositing effect layer", color = EditorMint)
        }
        return
    }
    Text("Independent layer effect. Change its order to affect underlying layers.",
        color = EditorMuted, fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = 13.dp))
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
        .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ProPixelEffect.entries.filter { it != ProPixelEffect.NONE }.forEach { effect ->
            DockChip(effect.label, layer.effect == effect) { onEffect(effect) }
        }
    }
    if (layer.effect == ProPixelEffect.THRESHOLD ||
        layer.effect == ProPixelEffect.POSTERIZE) {
        Text("Effect amount", color = EditorMuted, fontSize = 11.sp,
            modifier = Modifier.padding(start = 14.dp))
        Slider(value = layer.effectAmount.coerceIn(0f, 1f), valueRange = 0f..1f,
            onValueChange = onAmount, onValueChangeFinished = onAmountDone,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
    }
}

package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenezone.gzphotoeditor.editor.ProAspect
import com.greenezone.gzphotoeditor.editor.ProBackground
import com.greenezone.gzphotoeditor.editor.ProBlend
import com.greenezone.gzphotoeditor.editor.ProDocument
import com.greenezone.gzphotoeditor.editor.ProMath
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
    if (!layer.visible || layer.opacity <= 0f) {
        Text("Show this layer and raise its opacity to preview the mask.",
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
    if (!layer.visible || layer.opacity <= 0f) {
        Text("Show this image and raise its opacity to preview retouching.",
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
    enabled: Boolean,
    onAdd: () -> Unit,
    onEffect: (ProPixelEffect) -> Unit,
    onAmount: (Float) -> Unit,
    onAmountDone: () -> Unit
) {
    if (layer.kind != ProLayerKind.ADJUSTMENT) {
        TextButton(enabled = canAdd && enabled, onClick = onAdd) {
            Text("Add adjustment layer", color = EditorMint)
        }
        return
    }
    Text(if (layer.mask == null) "Applies to the visible composition below this layer."
        else "Applies to the masked area of the composition below.",
        color = EditorMuted, fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = 13.dp))
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
        .padding(horizontal = 9.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ProPixelEffect.entries.forEach { effect ->
            val label = if (effect == ProPixelEffect.GRAYSCALE) "Black & white" else effect.label
            DockChip(label, layer.effect == effect, enabled = enabled) { onEffect(effect) }
        }
    }
    if (layer.effect == ProPixelEffect.THRESHOLD ||
        layer.effect == ProPixelEffect.POSTERIZE) {
        Text("Effect amount", color = EditorMuted, fontSize = 11.sp,
            modifier = Modifier.padding(start = 14.dp))
        Slider(value = layer.effectAmount.coerceIn(0f, 1f), valueRange = 0f..1f,
            enabled = enabled, onValueChange = onAmount, onValueChangeFinished = onAmountDone,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
    }
}

/** The selected layer owns its blend property even when it contributes no visible pixels. */
@Composable
internal fun ProBlendPanel(
    layer: ProLayer,
    backdrop: String,
    guidance: String,
    enabled: Boolean,
    onMode: (ProBlend) -> Unit,
    onOpacity: (Float) -> Unit,
    onOpacityDone: () -> Unit
) {
    var choicesOpen by remember { mutableStateOf(false) }
    LaunchedEffect(enabled) {
        if (!enabled) choicesOpen = false
    }
    Column(Modifier.fillMaxWidth().padding(horizontal = 13.dp)) {
        Text(backdrop, color = EditorWhite, fontSize = 12.sp)
        Text(guidance, color = EditorMuted, fontSize = 12.sp)
        if (!enabled) {
            Text("Finish the current operation to change this layer.", color = EditorMuted, fontSize = 12.sp)
        }
        Box {
            TextButton(
                onClick = { choicesOpen = true },
                enabled = enabled,
                modifier = Modifier.heightIn(min = 48.dp)
            ) { Text("Blend mode: " + layer.blend.label) }
            DropdownMenu(
                expanded = choicesOpen && enabled,
                onDismissRequest = { choicesOpen = false },
                modifier = Modifier.heightIn(max = 320.dp).widthIn(min = 176.dp).testTag("pro-blend-menu")
            ) {
                ProBlend.entries.forEach { mode ->
                    val active = mode == layer.blend
                    DropdownMenuItem(
                        text = { Text(mode.label, color = if (active) EditorMint else EditorWhite) },
                        modifier = Modifier.heightIn(min = 48.dp).semantics { selected = active },
                        onClick = {
                            choicesOpen = false
                            onMode(mode)
                        }
                    )
                }
            }
        }
        TextButton(
            onClick = { onMode(ProBlend.NORMAL) },
            enabled = enabled && layer.blend != ProBlend.NORMAL,
            modifier = Modifier.heightIn(min = 48.dp)
        ) { Text("Restore Normal") }
        Text("Opacity · " + (layer.opacity * 100).toInt() + "%",
            color = EditorMuted, fontSize = 12.sp)
        Slider(value = layer.opacity, enabled = enabled, onValueChange = onOpacity,
            onValueChangeFinished = onOpacityDone, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
internal fun ProCanvasProperties(document: ProDocument, enabled: Boolean, onEdit: (ProDocument) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        if (document.sourceAspectRatio != null) {
            Text("Source aspect", color = EditorMint, modifier = Modifier.align(Alignment.CenterVertically))
        }
        ProAspect.entries.forEach { aspect ->
            DockChip(aspect.label, document.sourceAspectRatio == null && document.aspect == aspect, enabled) {
                onEdit(ProMath.withAspect(document, aspect))
            }
        }
    }
    Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        ProBackground.entries.forEach { background ->
            DockChip(background.label, document.background == background, enabled) {
                onEdit(document.copy(background = background))
            }
        }
    }
}

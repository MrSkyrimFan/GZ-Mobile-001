package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.PrivacyEdit
import com.greenezone.gzphotoeditor.editor.PrivacyKind
import com.greenezone.gzphotoeditor.editor.PrivacyRaster

private data class PrivacySetting(
    val title: String,
    val low: Float,
    val high: Float,
    val read: (PrivacyEdit) -> Float,
    val change: (PrivacyEdit, Float) -> PrivacyEdit,
    val reset: Float
)

private fun privacySettings(mark: PrivacyEdit): List<PrivacySetting> {
    val x = PrivacySetting("Position X", 0f, 1f, { it.centerX }, { m, v -> m.copy(centerX = v) }, .5f)
    val y = PrivacySetting("Position Y", 0f, 1f, { it.centerY }, { m, v -> m.copy(centerY = v) }, .5f)
    val width = PrivacySetting("Width", .02f, 1f, { it.width }, { m, v -> m.copy(width = v) }, .45f)
    val height = PrivacySetting("Height", .02f, 1f, { it.height }, { m, v -> m.copy(height = v) }, .16f)
    val strength = PrivacySetting("Strength", 0f, 1f, { it.strength },
        { m, v -> m.copy(strength = v) }, .65f)
    val feather = PrivacySetting("Soft edge", 0f, .8f, { it.feather },
        { m, v -> m.copy(feather = v) }, .20f)
    val brushSize = PrivacySetting("Brush size", .005f, .2f, { it.brushSize },
        { m, v -> m.copy(brushSize = v) }, .055f)
    return when (mark.kind) {
        PrivacyKind.BLUR, PrivacyKind.PIXELATE -> listOf(strength, width, height, feather, x, y)
        PrivacyKind.BLACK_BAR, PrivacyKind.COLOR_COVER -> listOf(width, height, x, y)
        PrivacyKind.BLUR_BRUSH -> listOf(strength, brushSize, feather)
        PrivacyKind.BLACK_BRUSH -> listOf(brushSize)
    }
}

@Composable
fun PrivacyDeck(
    document: EditDocument,
    selected: Int,
    enabled: Boolean,
    onSelect: (Int) -> Unit,
    onAdd: (PrivacyKind) -> Unit,
    onDelete: () -> Unit,
    onUndoStroke: () -> Unit,
    onClearStrokes: () -> Unit,
    onCommit: (EditDocument) -> Unit,
    onSlide: (EditDocument) -> Unit,
    onFinished: () -> Unit
) {
    val current = document.privacy.getOrNull(selected)
    var chosen by remember(selected, current?.kind) { mutableIntStateOf(0) }
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(45.dp).testTag("privacy-edits"),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        itemsIndexed(document.privacy, key = { index, _ -> "privacy-$index" }) { index, mark ->
            DockChip(mark.kind.title + " " + (index + 1), index == selected, enabled) {
                onSelect(index)
            }
        }
        if (document.privacy.size < PrivacyRaster.MAX_EDITS) {
            item { DockChip("+ Blur", false, enabled) { onAdd(PrivacyKind.BLUR) } }
            item { DockChip("+ Blur brush", false, enabled) { onAdd(PrivacyKind.BLUR_BRUSH) } }
            item { DockChip("+ Pixelate", false, enabled) { onAdd(PrivacyKind.PIXELATE) } }
            item { DockChip("+ Black bar", false, enabled) { onAdd(PrivacyKind.BLACK_BAR) } }
            item { DockChip("+ Black brush", false, enabled) { onAdd(PrivacyKind.BLACK_BRUSH) } }
            item { DockChip("+ Cover", false, enabled) { onAdd(PrivacyKind.COLOR_COVER) } }
        }
        if (current != null) {
            item { DockChip("Remove", false, enabled, onDelete) }
            if (current.kind.usesBrush) {
                item { DockChip("Undo stroke", false,
                    enabled && current.strokes.isNotEmpty(), onUndoStroke) }
                item { DockChip("Clear paint", false,
                    enabled && current.strokes.isNotEmpty(), onClearStrokes) }
            }
        }
    }

    if (current == null) {
        Text("Add blur, pixelation, an opaque bar, or a paintable cover.",
            color = EditorWhite, fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp))
        return
    }
    Text(
        when (current.kind) {
            PrivacyKind.BLACK_BAR -> "Opaque bar • Drag the rectangle on the photo to place it"
            PrivacyKind.COLOR_COVER -> "Solid covering • Drag the rectangle to position it"
            PrivacyKind.BLUR_BRUSH -> "Blur brush • Swipe on the photo, even with controls hidden"
            PrivacyKind.BLACK_BRUSH -> "Black brush • Swipe on the photo, even with controls hidden"
            else -> "Drag the rectangle to position it. Blur is not secure redaction."
        },
        color = EditorMint, fontSize = 11.sp,
        modifier = Modifier.padding(horizontal = 14.dp)
    )

    if (current.kind == PrivacyKind.COLOR_COVER) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            listOf(
                "Black" to 0xFF000000.toInt(),
                "White" to 0xFFFFFFFF.toInt(),
                "Gray" to 0xFF777777.toInt(),
                "Purple" to 0xFF6544A3.toInt()
            ).forEach { (title, color) ->
                DockChip(title, current.color == color, enabled) {
                    val changed = document.privacy.toMutableList()
                    changed[selected] = current.copy(color = color)
                    onCommit(document.copy(privacy = changed))
                }
            }
        }
    }

    val controls = privacySettings(current)
    val setting = controls[chosen.coerceIn(0, controls.lastIndex)]
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(45.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        itemsIndexed(controls, key = { _, item -> item.title }) { i, item ->
            DockChip(item.title, i == chosen, enabled) { chosen = i }
        }
    }

    fun update(value: Float, finish: Boolean) {
        if (selected !in document.privacy.indices) return
        val changed = document.privacy.toMutableList()
        changed[selected] = setting.change(current, value)
        val next = document.copy(privacy = changed)
        if (finish) onCommit(next) else onSlide(next)
    }
    SingleAdjustment(
        title = setting.title,
        value = setting.read(current),
        low = setting.low,
        high = setting.high,
        enabled = enabled,
        onReset = { update(setting.reset, true) },
        onChange = { update(it, false) },
        onFinished = onFinished
    )
}

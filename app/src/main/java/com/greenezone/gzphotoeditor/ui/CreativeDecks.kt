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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenezone.gzphotoeditor.editor.CreativeRaster
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.ShapeKind
import com.greenezone.gzphotoeditor.editor.ShapeOverlay
import com.greenezone.gzphotoeditor.editor.TextAlignment
import com.greenezone.gzphotoeditor.editor.TextFont
import com.greenezone.gzphotoeditor.editor.TextOverlay

data class CreativeToolState(
    val textIndex: Int = 0,
    val shapeIndex: Int = 0,
    val inkColor: Int = -1,
    val inkThickness: Float = .012f,
    val inkOpacity: Float = 1f,
    val erase: Boolean = false
)

internal val EditingColors = listOf(
    "White" to 0xFFFFFFFF.toInt(),
    "Black" to 0xFF000000.toInt(),
    "Red" to 0xFFFF4F5A.toInt(),
    "Yellow" to 0xFFFFD34E.toInt(),
    "Mint" to 0xFF96E5C2.toInt(),
    "Blue" to 0xFF56A6FF.toInt(),
    "Pink" to 0xFFFF7EBB.toInt(),
    "Orange" to 0xFFFF9856.toInt(),
    "Purple" to 0xFFB58CFF.toInt(),
    "Gray" to 0xFFADB2BF.toInt(),
    "Cyan" to 0xFF40E5EA.toInt()
)

@Composable
private fun PaletteBar(selectedColor: Int, onChoose: (Int) -> Unit, enabled: Boolean) {
    LazyRow(Modifier.fillMaxWidth().height(46.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        itemsIndexed(EditingColors, key = { i, _ -> i }) { _, (name, color) ->
            DockChip(name, selectedColor == color, enabled) { onChoose(color) }
        }
    }
}

@Composable
fun TextDeck(
    document: EditDocument,
    tool: CreativeToolState,
    onTool: (CreativeToolState) -> Unit,
    enabled: Boolean,
    onNew: () -> Unit,
    onEdit: () -> Unit,
    onCommit: (EditDocument) -> Unit,
    onSlide: (EditDocument) -> Unit,
    onFinished: () -> Unit,
    onCommitAndSelect: (EditDocument, CreativeToolState) -> Unit = { next, selected ->
        onCommit(next); onTool(selected)
    }
) {
    val layers = document.creative.text
    val index = tool.textIndex.coerceIn(0, layers.lastIndex.coerceAtLeast(0))
    val current = layers.getOrNull(index)
    var setting by remember { mutableIntStateOf(0) }
    val controls = listOf("Font", "Color", "Style", "Size", "Width", "Height",
        "Rotation", "Opacity", "Outline", "Shadow", "Spacing", "Outline color")
    fun change(next: TextOverlay, slide: Boolean = false) {
        if (current == null) return
        val list = layers.toMutableList()
        list[index] = next
        val document2 = document.copy(creative = document.creative.copy(text = list))
        if (slide) onSlide(document2) else onCommit(document2)
    }

    LazyRow(Modifier.fillMaxWidth().height(46.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        item { DockChip("+ Add text", false, enabled && layers.size < CreativeRaster.MAX_TEXT, onNew) }
        itemsIndexed(layers, key = { index, _ -> index }) { i, item ->
            DockChip("Text " + (i + 1), index == i, enabled) {
                onTool(tool.copy(textIndex = i))
            }
        }
        if (current != null) {
            item { DockChip("Edit words", false, enabled, onEdit) }
            item { DockChip("Duplicate", false, enabled && layers.size < CreativeRaster.MAX_TEXT) {
                onCommitAndSelect(document.copy(creative = document.creative.copy(
                    text = layers + current.copy(x = (current.x + .06f).coerceAtMost(1f),
                        y = (current.y + .06f).coerceAtMost(1f))
                )), tool.copy(textIndex = layers.size))
            } }
            item { DockChip("Delete", false, enabled) {
                onCommitAndSelect(document.copy(creative = document.creative.copy(
                    text = layers.filterIndexed { i, _ -> i != index }
                )), tool.copy(textIndex = index.coerceAtMost(layers.lastIndex - 1).coerceAtLeast(0)))
            } }
        }
    }
    if (current == null) {
        Text("Add text to place an editable caption on the photo.",
            color = EditorWhite, fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp))
        return
    }
    LazyRow(Modifier.fillMaxWidth().height(46.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        itemsIndexed(controls) { i, title ->
            DockChip(title, setting == i, enabled) { setting = i }
        }
    }
    when (controls[setting]) {
        "Font" -> LazyRow(Modifier.fillMaxWidth().height(47.dp),
            contentPadding = PaddingValues(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            itemsIndexed(TextFont.entries) { _, font ->
                DockChip(font.label, current.font == font, enabled) {
                    change(current.copy(font = font))
                }
            }
        }
        "Color" -> PaletteBar(current.color, { change(current.copy(color = it)) }, enabled)
        "Outline color" -> PaletteBar(current.outlineColor,
            { change(current.copy(outlineColor = it)) }, enabled)
        "Style" -> LazyRow(Modifier.fillMaxWidth().height(47.dp),
            contentPadding = PaddingValues(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            item { DockChip("Bold", current.bold, enabled) { change(current.copy(bold = !current.bold)) } }
            item { DockChip("Italic", current.italic, enabled) { change(current.copy(italic = !current.italic)) } }
            itemsIndexed(TextAlignment.entries) { _, align ->
                DockChip(align.label, current.align == align, enabled) {
                    change(current.copy(align = align))
                }
            }
        }
        else -> {
            val v: Float
            val low: Float
            val high: Float
            val neutral: Float
            val update: (Float) -> TextOverlay
            when (controls[setting]) {
                "Size" -> { v = current.size; low = .012f; high = .24f; neutral = .075f; update = { current.copy(size = it) } }
                "Width" -> { v = current.width; low = .07f; high = 1f; neutral = .7f; update = { current.copy(width = it) } }
                "Height" -> { v = current.height; low = .04f; high = 1f; neutral = .24f; update = { current.copy(height = it) } }
                "Rotation" -> { v = current.rotation; low = -180f; high = 180f; neutral = 0f; update = { current.copy(rotation = it) } }
                "Opacity" -> { v = current.opacity; low = 0f; high = 1f; neutral = 1f; update = { current.copy(opacity = it) } }
                "Outline" -> { v = current.outline; low = 0f; high = 1f; neutral = 0f; update = { current.copy(outline = it) } }
                "Shadow" -> { v = current.shadow; low = 0f; high = 1f; neutral = 0f; update = { current.copy(shadow = it) } }
                else -> { v = current.letterSpacing; low = 0f; high = .3f; neutral = 0f; update = { current.copy(letterSpacing = it) } }
            }
            SingleAdjustment(
                title = controls[setting], value = v, low = low, high = high,
                enabled = enabled,
                onReset = { change(update(neutral)) },
                onChange = { change(update(it), slide = true) },
                onFinished = onFinished
            )
        }
    }
}

@Composable
fun DrawDeck(
    document: EditDocument,
    tool: CreativeToolState,
    onTool: (CreativeToolState) -> Unit,
    enabled: Boolean,
    onCommit: (EditDocument) -> Unit
) {
    var setting by remember { mutableIntStateOf(0) }
    LazyRow(Modifier.fillMaxWidth().height(46.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        item { DockChip("Brush", !tool.erase, enabled) { onTool(tool.copy(erase = false)) } }
        item { DockChip("Eraser", tool.erase, enabled) { onTool(tool.copy(erase = true)) } }
        item { DockChip("Undo stroke", false,
            enabled && document.creative.strokes.isNotEmpty()) {
            onCommit(document.copy(creative = document.creative.copy(
                strokes = document.creative.strokes.dropLast(1)
            )))
        } }
        item { DockChip("Clear drawing", false,
            enabled && document.creative.strokes.isNotEmpty()) {
            onCommit(document.copy(creative = document.creative.copy(strokes = emptyList())))
        } }
        item { DockChip("Thickness", setting == 0, enabled) { setting = 0 } }
        item { DockChip("Opacity", setting == 1, enabled) { setting = 1 } }
    }
    PaletteBar(tool.inkColor, { onTool(tool.copy(inkColor = it, erase = false)) }, enabled)
    SingleAdjustment(
        title = if (setting == 0) "Brush thickness" else "Brush opacity",
        value = if (setting == 0) tool.inkThickness else tool.inkOpacity,
        low = if (setting == 0) .0015f else 0f,
        high = if (setting == 0) .16f else 1f,
        enabled = enabled,
        onReset = {
            onTool(if (setting == 0) tool.copy(inkThickness = .012f)
                else tool.copy(inkOpacity = 1f))
        },
        onChange = {
            onTool(if (setting == 0) tool.copy(inkThickness = it)
                else tool.copy(inkOpacity = it))
        },
        onFinished = {}
    )
}

@Composable
fun ShapesDeck(
    document: EditDocument,
    tool: CreativeToolState,
    onTool: (CreativeToolState) -> Unit,
    enabled: Boolean,
    onCommit: (EditDocument) -> Unit,
    onSlide: (EditDocument) -> Unit,
    onFinished: () -> Unit,
    onCommitAndSelect: (EditDocument, CreativeToolState) -> Unit = { next, selected ->
        onCommit(next); onTool(selected)
    }
) {
    val layers = document.creative.shapes
    val index = tool.shapeIndex.coerceIn(0, layers.lastIndex.coerceAtLeast(0))
    val current = layers.getOrNull(index)
    var setting by remember { mutableIntStateOf(0) }
    val settings = listOf("Color", "Width", "Height", "Rotation", "Thickness", "Opacity")
    fun change(next: ShapeOverlay, slide: Boolean = false) {
        if (current == null) return
        val copy = layers.toMutableList()
        copy[index] = next
        val nextDoc = document.copy(creative = document.creative.copy(shapes = copy))
        if (slide) onSlide(nextDoc) else onCommit(nextDoc)
    }
    LazyRow(Modifier.fillMaxWidth().height(46.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        itemsIndexed(layers, key = { i, _ -> i }) { i, shape ->
            DockChip(shape.kind.label + " " + (i + 1), index == i, enabled) {
                onTool(tool.copy(shapeIndex = i))
            }
        }
        if (current != null) {
            item { DockChip("Fill", current.filled, enabled) {
                change(current.copy(filled = !current.filled))
            } }
            item { DockChip("Delete", false, enabled) {
                onCommitAndSelect(document.copy(creative = document.creative.copy(
                    shapes = layers.filterIndexed { i, _ -> i != index }
                )), tool.copy(shapeIndex = index.coerceAtMost(layers.lastIndex - 1).coerceAtLeast(0)))
            } }
        }
    }
    LazyRow(Modifier.fillMaxWidth().height(46.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        itemsIndexed(ShapeKind.entries) { _, kind ->
            DockChip("+ " + kind.label, false, enabled && layers.size < CreativeRaster.MAX_SHAPES) {
                onCommitAndSelect(document.copy(creative = document.creative.copy(
                    shapes = layers + ShapeOverlay(kind = kind, color = tool.inkColor)
                )), tool.copy(shapeIndex = layers.size))
            }
        }
    }
    if (current == null) {
        Text("Add a shape, then drag to position and resize on the photo.",
            color = EditorWhite, fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
        return
    }
    LazyRow(Modifier.fillMaxWidth().height(46.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        itemsIndexed(settings) { i, name ->
            DockChip(name, setting == i, enabled) { setting = i }
        }
    }
    if (setting == 0) {
        PaletteBar(current.color, { change(current.copy(color = it)) }, enabled)
    } else {
        val v: Float
        val low: Float
        val high: Float
        val neutral: Float
        val update: (Float) -> ShapeOverlay
        when (setting) {
            1 -> { v = current.width; low = .018f; high = 1f; neutral = .38f; update = { current.copy(width = it) } }
            2 -> { v = current.height; low = .018f; high = 1f; neutral = .25f; update = { current.copy(height = it) } }
            3 -> { v = current.rotation; low = -180f; high = 180f; neutral = 0f; update = { current.copy(rotation = it) } }
            4 -> { v = current.thickness; low = .002f; high = .1f; neutral = .009f; update = { current.copy(thickness = it) } }
            else -> { v = current.opacity; low = 0f; high = 1f; neutral = 1f; update = { current.copy(opacity = it) } }
        }
        SingleAdjustment(
            title = settings[setting], value = v, low = low, high = high, enabled = enabled,
            onReset = { change(update(neutral)) },
            onChange = { change(update(it), true) },
            onFinished = onFinished
        )
    }
}

package com.greenezone.gzphotoeditor.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenezone.gzphotoeditor.editor.ColorBand
import com.greenezone.gzphotoeditor.editor.Control
import com.greenezone.gzphotoeditor.editor.ControlCatalog
import com.greenezone.gzphotoeditor.editor.CropRatio
import com.greenezone.gzphotoeditor.editor.CropMath
import com.greenezone.gzphotoeditor.editor.CropFrame
import com.greenezone.gzphotoeditor.editor.CurveChannel
import com.greenezone.gzphotoeditor.editor.CurveMath
import com.greenezone.gzphotoeditor.editor.CurvePoints
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.Grade
import com.greenezone.gzphotoeditor.editor.LocalMask
import com.greenezone.gzphotoeditor.editor.Look
import com.greenezone.gzphotoeditor.editor.Looks
import com.greenezone.gzphotoeditor.editor.MaskKind
import com.greenezone.gzphotoeditor.editor.Panel
import com.greenezone.gzphotoeditor.editor.PrivacyKind
import java.util.Locale

internal val EditorMint = Color(0xFF96E5C2)
internal val EditorWhite = Color(0xFFF2F5F6)
internal val EditorMuted = Color(0xFFB1BBC4)
internal val EditorSurface = Color(0xE6171C25)
private val ChipIdle = Color(0xFF292F3C)
private val ChipSelected = Color(0xFF345548)

enum class EditTab(val title: String) {
    LOOKS("Looks"),
    LIGHT("Light"),
    COLOR("Color"),
    MIXER("Mix"),
    GRADE("Grading"),
    DETAIL("Detail"),
    EFFECTS("Effects"),
    CURVES("Curves"),
    MASKS("Masks"),
    PRIVACY("Blur & cover"),
    TEXT("Text"),
    DRAW("Draw"),
    SHAPES("Shapes"),
    CROP("Crop"),
    MORE("More")
}

/**
 * One active tool at a time. The editor is a canvas with a shallow bottom dock,
 * not a vertically scrolling list of fifty-six controls.
 */
@Composable
fun EditorDock(
    tab: EditTab,
    onTab: (EditTab) -> Unit,
    document: EditDocument,
    original: Bitmap?,
    thumbnails: List<Bitmap>,
    savedLooks: List<Look>,
    activeLook: String,
    busy: Boolean,
    selectedMaskIndex: Int,
    onSelectMask: (Int) -> Unit,
    selectedPrivacyIndex: Int,
    onSelectPrivacy: (Int) -> Unit,
    onAddPrivacy: (PrivacyKind) -> Unit,
    onDeletePrivacy: () -> Unit,
    onUndoPrivacyStroke: () -> Unit,
    onClearPrivacy: () -> Unit,
    onCommit: (EditDocument) -> Unit,
    onSlide: (EditDocument) -> Unit,
    onSlideFinished: () -> Unit,
    onSelectLook: (String, Grade) -> Unit,
    onAddMask: (MaskKind) -> Unit,
    onDeleteMask: () -> Unit,
    onUndoBrush: () -> Unit,
    onClearBrush: () -> Unit,
    onAutoTone: () -> Unit,
    onAutoWB: () -> Unit,
    onSaveLook: () -> Unit,
    onSaveProject: () -> Unit,
    onProjects: () -> Unit,
    onOpen: () -> Unit,
    creativeTool: CreativeToolState = CreativeToolState(),
    onCreativeTool: (CreativeToolState) -> Unit = {},
    onNewText: () -> Unit = {},
    onEditText: () -> Unit = {},
    cropImageWidth: Int = 1,
    cropImageHeight: Int = 1
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(EditorSurface)
            .padding(top = 10.dp, bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        when (tab) {
            EditTab.LOOKS -> LooksDeck(
                original, thumbnails, savedLooks, activeLook, !busy,
                onSelectLook, onSaveLook
            )
            EditTab.LIGHT, EditTab.COLOR, EditTab.DETAIL, EditTab.EFFECTS, EditTab.GRADE ->
                GlobalDeck(tab, document, !busy, onCommit, onSlide, onSlideFinished)
            EditTab.MIXER -> MixerDeck(
                document, !busy, onCommit, onSlide, onSlideFinished
            )
            EditTab.CURVES -> CurveDeck(
                document, !busy, onCommit, onSlide, onSlideFinished
            )
            EditTab.MASKS -> MaskDeck(
                document, !busy, selectedMaskIndex, onSelectMask, onCommit,
                onSlide, onSlideFinished, onAddMask, onDeleteMask, onUndoBrush, onClearBrush
            )
            EditTab.PRIVACY -> PrivacyDeck(
                document, selectedPrivacyIndex, !busy, onSelectPrivacy, onAddPrivacy,
                onDeletePrivacy, onUndoPrivacyStroke, onClearPrivacy,
                onCommit, onSlide, onSlideFinished
            )
            EditTab.TEXT -> TextDeck(
                document, creativeTool, onCreativeTool, !busy, onNewText, onEditText,
                onCommit, onSlide, onSlideFinished
            )
            EditTab.DRAW -> DrawDeck(
                document, creativeTool, onCreativeTool, !busy, onCommit
            )
            EditTab.SHAPES -> ShapesDeck(
                document, creativeTool, onCreativeTool, !busy,
                onCommit, onSlide, onSlideFinished
            )
            EditTab.CROP -> CropDeck(
                document, !busy, onCommit, onSlide, onSlideFinished,
                cropImageWidth, cropImageHeight
            )
            EditTab.MORE -> MoreDeck(
                !busy, onAutoTone, onAutoWB, onSaveLook, onSaveProject, onProjects, onOpen,
                onCommit, document
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier.fillMaxWidth().height(47.dp)
        ) {
            items(EditTab.entries) { item ->
                DockChip(
                    title = item.title,
                    selected = tab == item,
                    enabled = !busy,
                    onClick = { onTab(item) }
                )
            }
        }
    }
}

@Composable
internal fun DockChip(
    title: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) ChipSelected else ChipIdle)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) EditorMint else Color(0xFF414B5B),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            title,
            color = if (selected) EditorWhite else EditorMuted,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun LooksDeck(
    original: Bitmap?,
    thumbnails: List<Bitmap>,
    savedLooks: List<Look>,
    activeLook: String,
    enabled: Boolean,
    onSelect: (String, Grade) -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "SWIPE LOOKS  •  TAP TO APPLY",
            color = EditorMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
        TextButton(onClick = onSave, enabled = enabled) {
            Text("Save my look", color = EditorMint, fontSize = 12.sp)
        }
    }
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(107.dp),
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "no-filter") {
            LookTile("None", thumbnails.getOrNull(0) ?: original, activeLook == "none", enabled) {
                onSelect("none", Grade())
            }
        }
        itemsIndexed(Looks.all, key = { index, _ -> "preset-$index" }) { index, look ->
            LookTile(
                look.name, thumbnails.getOrNull(index + 1),
                activeLook == "preset-$index", enabled
            ) { onSelect("preset-$index", look.grade) }
        }
        items(savedLooks, key = { "saved-" + it.name }) { look ->
            LookTile(
                look.name, null, activeLook == "saved-" + look.name, enabled
            ) { onSelect("saved-" + look.name, look.grade) }
        }
    }
}

@Composable
private fun LookTile(title: String, bitmap: Bitmap?, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(84.dp).clickable(enabled = enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier.size(width = 82.dp, height = 74.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(ChipIdle)
                .border(if (selected) 2.dp else 1.dp,
                    if (selected) EditorMint else Color(0xFF555E68),
                    RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = title + " filter preview",
                    modifier = Modifier.matchParentSize().clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("★", color = EditorMint, fontSize = 23.sp)
            }
            if (selected) Box(
                modifier = Modifier.align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(9.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(EditorMint)
            )
        }
        Text(
            title,
            color = if (selected) EditorWhite else EditorMuted,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GlobalDeck(
    tab: EditTab,
    document: EditDocument,
    enabled: Boolean,
    onCommit: (EditDocument) -> Unit,
    onSlide: (EditDocument) -> Unit,
    onFinished: () -> Unit
) {
    val panel = when (tab) {
        EditTab.LIGHT -> Panel.LIGHT
        EditTab.COLOR -> Panel.COLOR
        EditTab.DETAIL -> Panel.DETAIL
        EditTab.EFFECTS -> Panel.FILM
        else -> Panel.GRADE
    }
    val controls = ControlCatalog.groups.getValue(panel)
    var selected by remember(tab) { mutableIntStateOf(0) }
    val index = selected.coerceIn(0, controls.lastIndex)
    val control = controls[index]
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(44.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        itemsIndexed(controls, key = { _, item -> item.name }) { i, item ->
            DockChip(
                title = item.name,
                selected = index == i,
                enabled = enabled,
                onClick = { selected = i }
            )
        }
    }
    SingleAdjustment(
        title = control.name,
        value = control.read(document.grade),
        low = control.min,
        high = control.max,
        enabled = enabled,
        onReset = {
            onCommit(document.copy(grade = control.write(document.grade, 0f)))
        },
        onChange = {
            onSlide(document.copy(grade = control.write(document.grade, it)))
        },
        onFinished = onFinished
    )
}

@Composable
private fun MixerDeck(
    document: EditDocument,
    enabled: Boolean,
    onCommit: (EditDocument) -> Unit,
    onSlide: (EditDocument) -> Unit,
    onFinished: () -> Unit
) {
    var band by remember { mutableStateOf(ColorBand.RED) }
    var index by remember { mutableIntStateOf(0) }
    val controls = ControlCatalog.mixer.getValue(band)
    val control = controls[index.coerceIn(0, 2)]
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ColorBand.entries.forEach { item ->
            DockChip(item.label, band == item, enabled) { band = item }
        }
    }
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        listOf("Hue", "Saturation", "Luminance").forEachIndexed { i, name ->
            DockChip(name, index == i, enabled) { index = i }
        }
    }
    SingleAdjustment(
        control.name, control.read(document.grade), control.min, control.max, enabled,
        onReset = { onCommit(document.copy(grade = control.write(document.grade, 0f))) },
        onChange = { onSlide(document.copy(grade = control.write(document.grade, it))) },
        onFinished = onFinished
    )
}

@Composable
private fun CurveDeck(
    document: EditDocument,
    enabled: Boolean,
    onCommit: (EditDocument) -> Unit,
    onSlide: (EditDocument) -> Unit,
    onFinished: () -> Unit
) {
    var channel by remember { mutableStateOf(CurveChannel.MASTER) }
    var knot by remember { mutableIntStateOf(2) }
    val points = CurveMath.get(document.grade, channel)
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        CurveChannel.entries.forEach { item ->
            DockChip(item.title, channel == item, enabled) { channel = item }
        }
        DockChip("Reset", false, enabled) {
            onCommit(document.copy(grade = CurveMath.put(document.grade, channel, CurveMath.identity)))
        }
    }
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        listOf("Black", "Shadows", "Midtone", "Highlights", "White").forEachIndexed { i, name ->
            DockChip(name, knot == i, enabled) { knot = i }
        }
    }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CurveThumb(points, Modifier.size(72.dp))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            SingleAdjustment(
                title = channel.title + " • " + listOf("Black", "Shadows", "Mid", "Highlights", "White")[knot],
                value = points.values[knot],
                low = 0f,
                high = 1f,
                enabled = enabled,
                onReset = {
                    val point = CurveMath.identity.values[knot]
                    onCommit(document.copy(grade = CurveMath.put(
                        document.grade, channel, points.change(knot, point)
                    )))
                },
                onChange = {
                    onSlide(document.copy(grade = CurveMath.put(
                        document.grade, channel, points.change(knot, it)
                    )))
                },
                onFinished = onFinished,
                horizontalPadding = 0
            )
        }
    }
}

@Composable
private fun CurveThumb(points: CurvePoints, modifier: Modifier = Modifier) {
    Canvas(modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF262E39))) {
        val path = Path()
        for (i in 0..64) {
            val x = i / 64f
            val y = CurveMath.evaluate(points, x)
            if (i == 0) path.moveTo(x * size.width, (1f - y) * size.height)
            else path.lineTo(x * size.width, (1f - y) * size.height)
        }
        drawLine(Color(0xFF515D68), Offset(0f, size.height), Offset(size.width, 0f), 1f)
        drawPath(path, EditorMint, style = Stroke(width = 2.5f))
    }
}

private data class MaskSetting(
    val label: String,
    val min: Float,
    val max: Float,
    val read: (LocalMask) -> Float,
    val write: (LocalMask, Float) -> LocalMask,
    val neutral: Float = 0f
)

private fun settingsFor(mask: LocalMask): List<MaskSetting> {
    val common = listOf(
        MaskSetting("Exposure", -1.5f, 1.5f, { it.exposure }, { m, v -> m.copy(exposure = v) }),
        MaskSetting("Contrast", -1f, 1f, { it.contrast }, { m, v -> m.copy(contrast = v) }),
        MaskSetting("Saturation", -1f, 1f, { it.saturation }, { m, v -> m.copy(saturation = v) })
    )
    val feather = MaskSetting("Soft edge", .01f, 1f, { it.feather },
        { m, v -> m.copy(feather = v) }, .35f)
    val size = MaskSetting("Brush size", .005f, .20f, { it.radius },
        { m, v -> m.copy(radius = v) }, .045f)
    val radius = MaskSetting("Size", .02f, .8f, { it.radius },
        { m, v -> m.copy(radius = v) }, .35f)
    val x = MaskSetting("Center X", 0f, 1f, { it.centerX },
        { m, v -> m.copy(centerX = v) }, .5f)
    val y = MaskSetting("Center Y", 0f, 1f, { it.centerY },
        { m, v -> m.copy(centerY = v) }, .5f)
    return common + when (mask.kind) {
        MaskKind.BRUSH -> listOf(size, feather)
        MaskKind.RADIAL -> listOf(radius, feather, x, y)
        MaskKind.LINEAR -> listOf(
            radius.copy(label = "Spread"),
            x.copy(label = "Tilt"),
            y
        )
    }
}

@Composable
private fun MaskDeck(
    document: EditDocument,
    enabled: Boolean,
    selectedMaskIndex: Int,
    onSelectMask: (Int) -> Unit,
    onCommit: (EditDocument) -> Unit,
    onSlide: (EditDocument) -> Unit,
    onFinished: () -> Unit,
    onAdd: (MaskKind) -> Unit,
    onDelete: () -> Unit,
    onUndoBrush: () -> Unit,
    onClearBrush: () -> Unit
) {
    val current = document.masks.getOrNull(selectedMaskIndex)
    var settingIndex by remember(selectedMaskIndex, current?.kind) { mutableIntStateOf(0) }
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(44.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        itemsIndexed(document.masks, key = { index, _ -> "mask-$index" }) { index, item ->
            DockChip(item.kind.title + " " + (index + 1), selectedMaskIndex == index, enabled) {
                onSelectMask(index)
            }
        }
        if (document.masks.size < 4) {
            item { DockChip("+ Brush", false, enabled) { onAdd(MaskKind.BRUSH) } }
            item { DockChip("+ Radial", false, enabled) { onAdd(MaskKind.RADIAL) } }
            item { DockChip("+ Linear", false, enabled) { onAdd(MaskKind.LINEAR) } }
        }
        if (current != null) {
            item { DockChip("Remove mask", false, enabled, onDelete) }
            if (current.kind == MaskKind.BRUSH) {
                item { DockChip("Undo stroke", false, enabled && current.strokes.isNotEmpty(), onUndoBrush) }
                item { DockChip("Clear paint", false, enabled && current.strokes.isNotEmpty(), onClearBrush) }
            }
        }
    }
    if (current == null) {
        Text(
            "Add a mask to adjust just part of the image.",
            color = EditorWhite, fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp)
        )
        return
    }
    if (current.kind == MaskKind.BRUSH) {
        Text("BRUSH  •  Paint directly on the photo at 1×",
            color = EditorMint, fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 15.dp))
    }
    val settings = settingsFor(current)
    val setting = settings[settingIndex.coerceIn(0, settings.lastIndex)]
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(44.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        itemsIndexed(settings) { index, item ->
            DockChip(item.label, settingIndex == index, enabled) { settingIndex = index }
        }
    }
    fun update(v: Float) {
        val edited = document.masks.toMutableList()
        edited[selectedMaskIndex] = setting.write(current, v)
        onSlide(document.copy(masks = edited))
    }
    SingleAdjustment(
        title = setting.label, value = setting.read(current),
        low = setting.min, high = setting.max, enabled = enabled,
        onReset = {
            val edited = document.masks.toMutableList()
            edited[selectedMaskIndex] = setting.write(current, setting.neutral)
            onCommit(document.copy(masks = edited))
        },
        onChange = ::update,
        onFinished = onFinished
    )
}

@Composable
private fun CropDeck(
    document: EditDocument,
    enabled: Boolean,
    onCommit: (EditDocument) -> Unit,
    onSlide: (EditDocument) -> Unit,
    onFinished: () -> Unit,
    imageWidth: Int,
    imageHeight: Int
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(44.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        item { DockChip("Rotate left", false, enabled) {
            val quarter = (document.quarterTurns + 3) % 4
            onCommit(document.copy(quarterTurns = quarter,
                cropFrame = if (document.cropFrame != null)
                    CropMath.initial(imageHeight, imageWidth, document.crop)
                    else null))
        } }
        item { DockChip("Rotate right", false, enabled) {
            val quarter = (document.quarterTurns + 1) % 4
            onCommit(document.copy(quarterTurns = quarter,
                cropFrame = if (document.cropFrame != null)
                    CropMath.initial(imageHeight, imageWidth, document.crop)
                    else null))
        } }
        item { DockChip("Flip horizontal", document.mirrored, enabled) {
            onCommit(document.copy(mirrored = !document.mirrored))
        } }
        item { DockChip("Flip vertical", document.flipVertical, enabled) {
            onCommit(document.copy(flipVertical = !document.flipVertical))
        } }
        item { DockChip("Reset crop", false, enabled) {
            onCommit(document.copy(crop = CropRatio.ORIGINAL, cropFrame = null,
                cropCenterX = .5f, cropCenterY = .5f, straightenDegrees = 0f))
        } }
    }
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(44.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        items(CropRatio.entries) { ratio ->
            DockChip(ratio.label, document.crop == ratio, enabled) {
                val frame = when (ratio) {
                    CropRatio.ORIGINAL -> null
                    CropRatio.FREE -> CropFrame(.08f, .08f, .92f, .92f)
                    else -> CropMath.initial(imageWidth.coerceAtLeast(1),
                        imageHeight.coerceAtLeast(1), ratio)
                }
                onCommit(document.copy(crop = ratio, cropFrame = frame,
                    cropCenterX = .5f, cropCenterY = .5f))
            }
        }
    }
    Text(
        "Drag inside to move. Drag the corner dots to resize.",
        color = EditorMuted, fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = 13.dp)
    )
    SingleAdjustment(
        title = "Straighten",
        value = document.straightenDegrees,
        low = -15f,
        high = 15f,
        enabled = enabled,
        valueSuffix = "°",
        onReset = { onCommit(document.copy(straightenDegrees = 0f)) },
        onChange = { onSlide(document.copy(straightenDegrees = it)) },
        onFinished = onFinished
    )
}
@Composable
private fun MoreDeck(
    enabled: Boolean,
    onAutoTone: () -> Unit,
    onAutoWB: () -> Unit,
    onSaveLook: () -> Unit,
    onSaveProject: () -> Unit,
    onProjects: () -> Unit,
    onOpen: () -> Unit,
    onCommit: (EditDocument) -> Unit,
    document: EditDocument
) {
    Text(
        "QUICK ACTIONS",
        color = EditorMuted,
        fontSize = 10.sp,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 15.dp, vertical = 4.dp)
    )
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(47.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        item { DockChip("Auto tone", false, enabled, onAutoTone) }
        item { DockChip("Auto white balance", false, enabled, onAutoWB) }
        item { DockChip("Save my look", false, enabled, onSaveLook) }
        item { DockChip("Save project", false, enabled, onSaveProject) }
        item { DockChip("Open projects", false, enabled, onProjects) }
        item { DockChip("New photo", false, enabled, onOpen) }
        item { DockChip("Reset all edits", false, enabled) { onCommit(EditDocument()) } }
    }
    Text(
        "Edits are non-destructive. Your original photo stays untouched.",
        color = EditorMuted, fontSize = 11.sp,
        modifier = Modifier.padding(horizontal = 15.dp, vertical = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SingleAdjustment(
    title: String,
    value: Float,
    low: Float,
    high: Float,
    enabled: Boolean,
    onReset: () -> Unit,
    onChange: (Float) -> Unit,
    onFinished: () -> Unit,
    valueSuffix: String = "",
    horizontalPadding: Int = 14
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(46.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = EditorWhite,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                String.format(Locale.US, "%+.2f", value) + valueSuffix,
                color = EditorMint, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 5.dp)
            )
            TextButton(
                onClick = onReset,
                enabled = enabled,
                modifier = Modifier.width(70.dp).height(44.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Reset", color = EditorMint, fontSize = 12.sp, maxLines = 1)
            }
        }
        Slider(
            modifier = Modifier.fillMaxWidth().height(48.dp)
                .semantics { contentDescription = "Adjust " + title },
            value = value.coerceIn(low, high),
            valueRange = low..high,
            enabled = enabled,
            onValueChange = onChange,
            onValueChangeFinished = onFinished,
            colors = SliderDefaults.colors(
                thumbColor = EditorMint,
                activeTrackColor = EditorMint,
                inactiveTrackColor = Color(0xFF505967)
            ),
            thumb = {
                Box(
                    Modifier.size(19.dp)
                        .clip(CircleShape)
                        .background(if (enabled) EditorMint else EditorMuted)
                        .border(2.dp, Color(0xFF1C2530), CircleShape)
                )
            },
            track = { sliderState ->
                Canvas(Modifier.fillMaxWidth().height(6.dp)) {
                    val fraction = ((sliderState.value - low) / (high - low)).coerceIn(0f, 1f)
                    val neutral = if (low < 0f && high > 0f) (-low / (high - low)) else 0f
                    val start = minOf(fraction, neutral)
                    val end = maxOf(fraction, neutral)
                    val round = CornerRadius(size.height / 2f, size.height / 2f)
                    drawRoundRect(color = Color(0xFF56606F), cornerRadius = round)
                    if (end - start > .001f) {
                        drawRoundRect(
                            color = EditorMint,
                            topLeft = Offset(size.width * start, 0f),
                            size = Size(size.width * (end - start), size.height),
                            cornerRadius = round
                        )
                    }
                    if (low < 0f && high > 0f) {
                        val x = neutral * size.width
                        drawLine(
                            color = Color(0xFFF1F7F6).copy(alpha = .65f),
                            start = Offset(x, -5f),
                            end = Offset(x, size.height + 5f),
                            strokeWidth = 2f
                        )
                    }
                }
            }
        )
    }
}

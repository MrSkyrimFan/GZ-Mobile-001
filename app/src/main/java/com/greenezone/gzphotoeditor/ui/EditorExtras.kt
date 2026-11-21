package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.greenezone.gzphotoeditor.editor.CurveChannel
import com.greenezone.gzphotoeditor.editor.CurveMath
import com.greenezone.gzphotoeditor.editor.CurvePoints
import com.greenezone.gzphotoeditor.editor.LocalMask
import com.greenezone.gzphotoeditor.editor.MaskKind
import java.util.Locale

@Composable
fun CurvePanel(
    selectedChannel: CurveChannel,
    onSelectChannel: (CurveChannel) -> Unit,
    points: CurvePoints,
    onValueChange: (Int, Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    onReset: () -> Unit
) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        CurveChannel.entries.forEach { channel ->
            FilterChip(
                selected = channel == selectedChannel,
                onClick = { onSelectChannel(channel) },
                label = { Text(channel.title) }
            )
        }
        TextButton(onClick = onReset) { Text("Reset curve") }
    }
    val color = when (selectedChannel) {
        CurveChannel.MASTER -> Color(0xFFECF1F2)
        CurveChannel.RED -> Color(0xFFFF7575)
        CurveChannel.GREEN -> Color(0xFF76DFA4)
        CurveChannel.BLUE -> Color(0xFF86A7FA)
    }
    Canvas(Modifier.fillMaxWidth().height(132.dp).background(Color(0xFF11171C))) {
        for (i in 1..3) {
            val offset = i / 4f
            drawLine(Color(0xFF354047), Offset(size.width * offset, 0f),
                Offset(size.width * offset, size.height), 1f)
            drawLine(Color(0xFF354047), Offset(0f, size.height * offset),
                Offset(size.width, size.height * offset), 1f)
        }
        val path = Path()
        for (i in 0..128) {
            val x = i / 128f
            val y = CurveMath.evaluate(points, x)
            if (i == 0) path.moveTo(x * size.width, (1f - y) * size.height)
            else path.lineTo(x * size.width, (1f - y) * size.height)
        }
        drawPath(path, color, style = Stroke(width = 3f))
        points.values.forEachIndexed { index, y ->
            drawCircle(color, radius = 5f,
                center = Offset(index * size.width / 4f, (1f - y) * size.height))
        }
    }
    val labels = listOf("Black", "Shadow", "Midtone", "Highlight", "White")
    points.values.forEachIndexed { index, value ->
        Text(labels[index] + "  " + String.format(Locale.US, "%.2f", value))
        Slider(
            value = value, valueRange = 0f..1f,
            onValueChange = { onValueChange(index, it) },
            onValueChangeFinished = onValueChangeFinished
        )
    }
}

@Composable
fun MaskPanel(
    masks: List<LocalMask>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onAdd: (MaskKind) -> Unit,
    onDelete: () -> Unit,
    onMaskChange: (LocalMask) -> Unit,
    onChangeFinished: () -> Unit
) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { onAdd(MaskKind.RADIAL) }, enabled = masks.size < 4) {
            Text("+ Radial")
        }
        OutlinedButton(onClick = { onAdd(MaskKind.LINEAR) }, enabled = masks.size < 4) {
            Text("+ Linear")
        }
        OutlinedButton(onClick = { onAdd(MaskKind.BRUSH) }, enabled = masks.size < 4) {
            Text("+ Brush")
        }
        if (masks.isNotEmpty()) TextButton(onClick = onDelete) { Text("Delete mask") }
    }
    Text("Local masks stay editable and work on the full-resolution export.")
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        masks.forEachIndexed { index, mask ->
            FilterChip(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                label = { Text(mask.kind.title + " " + (index + 1)) }
            )
        }
    }
    val mask = masks.getOrNull(selectedIndex)
    if (mask == null) {
        Text("Add a radial mask for a local adjustment, or a linear graduated mask.")
        return
    }
    when (mask.kind) {
        MaskKind.RADIAL -> {
            MaskSlider("Center X", mask.centerX, 0f, 1f,
                { onMaskChange(mask.copy(centerX = it)) }, onChangeFinished)
            MaskSlider("Center Y", mask.centerY, 0f, 1f,
                { onMaskChange(mask.copy(centerY = it)) }, onChangeFinished)
            MaskSlider("Radius", mask.radius, .02f, .8f,
                { onMaskChange(mask.copy(radius = it)) }, onChangeFinished)
            MaskSlider("Feather", mask.feather, .01f, 1f,
                { onMaskChange(mask.copy(feather = it)) }, onChangeFinished)
        }
        MaskKind.LINEAR -> {
            MaskSlider("Tilt", mask.centerX, 0f, 1f,
                { onMaskChange(mask.copy(centerX = it)) }, onChangeFinished)
            MaskSlider("Center Y", mask.centerY, 0f, 1f,
                { onMaskChange(mask.copy(centerY = it)) }, onChangeFinished)
            MaskSlider("Spread", mask.radius, .02f, .8f,
                { onMaskChange(mask.copy(radius = it)) }, onChangeFinished)
        }
        MaskKind.BRUSH -> {
            Text("Drag a finger over the photo to paint. Fit zoom first. Up to 640 points per mask.")
            MaskSlider("Brush size", mask.radius, .005f, .20f,
                { onMaskChange(mask.copy(radius = it)) }, onChangeFinished)
            MaskSlider("Soft edge", mask.feather, .01f, 1f,
                { onMaskChange(mask.copy(feather = it)) }, onChangeFinished)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(enabled = mask.strokes.isNotEmpty(), onClick = {
                    onMaskChange(mask.copy(strokes = mask.strokes.dropLast(1)))
                    onChangeFinished()
                }) { Text("Undo brush stroke") }
                TextButton(enabled = mask.strokes.isNotEmpty(), onClick = {
                    onMaskChange(mask.copy(strokes = emptyList()))
                    onChangeFinished()
                }) { Text("Clear brush") }
            }
            Text("${mask.strokes.size} strokes")
        }
    }
    MaskSlider("Exposure", mask.exposure, -1.5f, 1.5f,
        { onMaskChange(mask.copy(exposure = it)) }, onChangeFinished)
    MaskSlider("Contrast", mask.contrast, -1f, 1f,
        { onMaskChange(mask.copy(contrast = it)) }, onChangeFinished)
    MaskSlider("Saturation", mask.saturation, -1f, 1f,
        { onMaskChange(mask.copy(saturation = it)) }, onChangeFinished)
}

@Composable
private fun MaskSlider(
    label: String, value: Float, low: Float, high: Float,
    onChange: (Float) -> Unit, onFinished: () -> Unit
) {
    Text(label + "  " + String.format(Locale.US, "%+.2f", value))
    Slider(value = value, valueRange = low..high,
        onValueChange = onChange, onValueChangeFinished = onFinished)
}

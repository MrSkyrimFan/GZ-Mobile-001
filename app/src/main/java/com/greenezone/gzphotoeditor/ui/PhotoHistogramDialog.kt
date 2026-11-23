package com.greenezone.gzphotoeditor.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenezone.gzphotoeditor.editor.PhotoHistogram
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
internal fun PhotoHistogramDialog(
    image: Bitmap,
    showingBefore: Boolean,
    onDismiss: () -> Unit
) {
    var rgb by remember { mutableStateOf(true) }
    var histogram by remember(image) { mutableStateOf<PhotoHistogram?>(null) }
    LaunchedEffect(image) {
        histogram = withContext(Dispatchers.Default) {
            val job = currentCoroutineContext()
            PhotoHistogram.fromPreview(image) { job.ensureActive() }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Histogram") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(if (showingBefore) "Before preview" else "Edited preview")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DockChip("RGB", rgb) { rgb = true }
                    DockChip("Luminance", !rgb) { rgb = false }
                }
                val current = histogram
                when {
                    current == null -> Text("Measuring photo…")
                    current.visiblePixels == 0 -> Text("No visible pixels")
                    else -> HistogramReadout(current, rgb)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun HistogramReadout(histogram: PhotoHistogram, rgb: Boolean) {
    val series = if (rgb) listOf(
        histogram.red to Color(0xFFFF7777),
        histogram.green to Color(0xFF7FE29F),
        histogram.blue to Color(0xFF7EB2FF)
    ) else listOf(histogram.luminance to EditorWhite)
    val peak = series.maxOf { (values, _) -> values.maxOrNull() ?: 0 }.coerceAtLeast(1)
    val chartDescription = if (rgb) "RGB photo histogram" else "Luminance photo histogram"
    Canvas(
        modifier = Modifier.fillMaxWidth().height(104.dp)
            .background(Color(0xFF1B222B))
            .semantics { contentDescription = chartDescription }
    ) {
        val step = size.width / 256f
        for ((counts, color) in series) {
            val trace = Path()
            trace.moveTo(0f, size.height)
            for (bin in counts.indices) {
                val x = bin * step
                val y = size.height * (1f - counts[bin].toFloat() / peak)
                trace.lineTo(x, y)
                trace.lineTo(x + step, y)
            }
            trace.lineTo(size.width, size.height)
            trace.close()
            drawPath(trace, color.copy(alpha = .24f))
            drawPath(trace, color.copy(alpha = .88f), style = Stroke(width = 1.dp.toPx()))
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("0", fontSize = 11.sp)
        Text("255", fontSize = 11.sp)
    }
    fun percent(count: Int): String =
        String.format(Locale.getDefault(), "%.1f%%", 100.0 * count / histogram.visiblePixels)
    Text("Black ${percent(histogram.blackPixels)}  ·  White ${percent(histogram.whitePixels)}")
    Text("Channel clipping at 0 / 255", fontSize = 12.sp)
    Text("R  ${percent(histogram.red[0])} / ${percent(histogram.red[255])}")
    Text("G  ${percent(histogram.green[0])} / ${percent(histogram.green[255])}")
    Text("B  ${percent(histogram.blue[0])} / ${percent(histogram.blue[255])}")
}

package com.greenezone.chromavale.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenezone.chromavale.editor.ControlCatalog
import com.greenezone.chromavale.editor.CropRatio
import com.greenezone.chromavale.editor.EditDocument
import com.greenezone.chromavale.editor.EditHistory
import com.greenezone.chromavale.editor.ImagePipeline
import com.greenezone.chromavale.editor.Looks
import com.greenezone.chromavale.editor.Panel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

private val Muted = Color(0xFF9DAFB3)
private val Frame = Color(0xFF29353D)

@Composable
fun PhotoEditorScreen() {
    val context = LocalContext.current
    val resolver = context.contentResolver
    val scope = rememberCoroutineScope()

    var sourceUri by remember { mutableStateOf<Uri?>(null) }
    var original by remember { mutableStateOf<Bitmap?>(null) }
    var processed by remember { mutableStateOf<Bitmap?>(null) }
    var document by remember { mutableStateOf(EditDocument()) }
    var history by remember { mutableStateOf(EditHistory()) }
    var dragStart by remember { mutableStateOf<EditDocument?>(null) }
    var selectedPanel by remember { mutableStateOf(Panel.LIGHT) }
    var compare by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Open a photo to begin editing.") }
    var busy by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    var showExport by remember { mutableStateOf(false) }

    fun commit(next: EditDocument) {
        history = history.checkpoint(document, next)
        document = next
        dragStart = null
    }

    fun loadPhoto(uri: Uri) {
        scope.launch {
            busy = true
            message = "Loading photo…"
            try {
                val loaded = withContext(Dispatchers.IO) {
                    ImagePipeline.decode(resolver, uri, ImagePipeline.PREVIEW_MAX_EDGE, 2_000_000L)
                }
                sourceUri = uri
                original = loaded
                document = EditDocument()
                history = EditHistory()
                processed = loaded
                dragStart = null
                compare = false
                message = "Photo loaded • " + loaded.width + " × " + loaded.height + " preview"
            } catch (error: Exception) {
                message = "Could not open photo: " + (error.message ?: "unsupported format")
            } finally {
                busy = false
            }
        }
    }

    fun exportImage(output: Uri, format: Bitmap.CompressFormat) {
        val source = sourceUri ?: return
        val edits = document
        scope.launch {
            exporting = true
            showExport = false
            message = "Rendering full-quality image…"
            try {
                val size = withContext(Dispatchers.Default) {
                    val originalFull = ImagePipeline.decode(
                        resolver, source, 12_000, ImagePipeline.MAX_EXPORT_PIXELS
                    )
                    val image = ImagePipeline.render(originalFull, edits)
                    val written = withContext(Dispatchers.IO) {
                        resolver.openOutputStream(output, "w")?.use { stream ->
                            image.compress(format, if (format == Bitmap.CompressFormat.JPEG) 95 else 100, stream)
                        } ?: throw IOException("Cannot open chosen output file")
                    }
                    if (!written) throw IOException("Image encoder reported a failure")
                    image.width.toString() + " × " + image.height
                }
                message = "Exported " + size + " • original left untouched"
            } catch (error: Exception) {
                message = "Export failed: " + (error.message ?: "unknown error")
            } finally {
                exporting = false
            }
        }
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) loadPhoto(uri) }
    val jpeg = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("image/jpeg")
    ) { uri -> if (uri != null) exportImage(uri, Bitmap.CompressFormat.JPEG) }
    val png = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("image/png")
    ) { uri -> if (uri != null) exportImage(uri, Bitmap.CompressFormat.PNG) }

    LaunchedEffect(original, document) {
        val src = original
        if (src == null) {
            processed = null
        } else {
            delay(125L)
            try {
                val result = withContext(Dispatchers.Default) {
                    ImagePipeline.render(src, document)
                }
                processed = result
            } catch (error: Exception) {
                message = "Preview error: " + (error.message ?: "unknown error")
            }
        }
    }

    val compared = remember(original, document.quarterTurns, document.mirrored, document.crop) {
        original?.let { ImagePipeline.geometry(it, document) }
    }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 14.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("CHROMAVALE", letterSpacing = 2.sp, fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text("PHOTO STUDIO  •  OFFLINE", color = Muted, fontSize = 10.sp, letterSpacing = 1.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    enabled = original != null && history.undo.isNotEmpty() && !busy && !exporting,
                    onClick = { history.undo(document)?.let { (d, h) -> document = d; history = h } }
                ) { Text("↶", fontSize = 24.sp) }
                TextButton(
                    enabled = original != null && history.redo.isNotEmpty() && !busy && !exporting,
                    onClick = { history.redo(document)?.let { (d, h) -> document = d; history = h } }
                ) { Text("↷", fontSize = 24.sp) }
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                enabled = !busy && !exporting,
                onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            ) { Text("Open photo") }
            OutlinedButton(
                enabled = original != null && !busy && !exporting,
                onClick = { compare = !compare }
            ) { Text(if (compare) "Show edit" else "Compare") }
            OutlinedButton(
                enabled = original != null && !busy && !exporting,
                onClick = { commit(EditDocument()) }
            ) { Text("Reset") }
        }

        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f)
                .background(Color(0xFF0A0E12)).border(1.dp, Frame),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = if (compare) compared else processed
            if (bitmap == null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text("◈", color = MaterialTheme.colorScheme.primary, fontSize = 48.sp)
                    Text("Your photo, your vision.", fontWeight = FontWeight.Medium, fontSize = 18.sp)
                    Text("20 real adjustments. Seven looks.\nNo ads. No AI. No cloud.",
                        color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            } else {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = if (compare) "Original photo comparison" else "Edited photo preview",
                    contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize()
                )
            }
        }

        Text(message, fontSize = 11.sp, color = Muted, maxLines = 2,
            modifier = Modifier.padding(top = 7.dp, bottom = 7.dp))
        HorizontalDivider(color = Frame)

        if (original != null) {
            Text("LOOKS", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 8.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Looks.all.forEach { look ->
                    AssistChip(onClick = { commit(document.copy(grade = look.grade)) },
                        label = { Text(look.name) }, enabled = !busy && !exporting)
                }
            }

            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Panel.entries.forEach { panel ->
                    FilterChip(selected = panel == selectedPanel,
                        onClick = { selectedPanel = panel }, label = { Text(panel.title) })
                }
            }

            Column(Modifier.heightIn(max = 230.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(3.dp)) {
                ControlCatalog.groups.getValue(selectedPanel).forEach { control ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(control.name, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text(String.format(Locale.US, "%+.2f", control.read(document.grade)),
                            color = Muted, fontSize = 11.sp)
                    }
                    Slider(
                        value = control.read(document.grade),
                        valueRange = control.min..control.max,
                        enabled = !busy && !exporting,
                        onValueChange = { value ->
                            if (dragStart == null) dragStart = document
                            document = document.copy(grade = control.write(document.grade, value))
                        },
                        onValueChangeFinished = {
                            dragStart?.let { start -> history = history.checkpoint(start, document) }
                            dragStart = null
                        },
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    )
                }
                HorizontalDivider(color = Frame, modifier = Modifier.padding(vertical = 8.dp))
                Text("TRANSFORM", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        commit(document.copy(quarterTurns = (document.quarterTurns + 1) % 4))
                    }) { Text("Rotate 90°") }
                    OutlinedButton(onClick = {
                        commit(document.copy(mirrored = !document.mirrored))
                    }) { Text("Mirror") }
                }
                Text("CENTER CROP", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CropRatio.entries.forEach { ratio ->
                        FilterChip(selected = document.crop == ratio,
                            onClick = { commit(document.copy(crop = ratio)) },
                            label = { Text(ratio.label) })
                    }
                }
            }

            if (showExport) {
                Card(Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Save a NEW image", fontWeight = FontWeight.Bold)
                        Text("Never overwrites the original. Maximum export 24 MP.",
                            color = Muted, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { jpeg.launch("ChromaVale_Edit.jpg") }) { Text("JPEG 95") }
                            OutlinedButton(onClick = { png.launch("ChromaVale_Edit.png") }) { Text("PNG") }
                            TextButton(onClick = { showExport = false }) { Text("Cancel") }
                        }
                    }
                }
            } else {
                Button(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    enabled = !busy && !exporting, onClick = { showExport = true }) {
                    Text(if (exporting) "Exporting…" else "EXPORT PHOTO")
                }
            }
        } else {
            Spacer(Modifier.height(12.dp))
            Text("Greenezone Software LLC • One-time purchase • Your images stay on your phone",
                fontSize = 11.sp, color = Muted, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp))
        }
    }
}

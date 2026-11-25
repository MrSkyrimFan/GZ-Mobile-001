package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenezone.gzphotoeditor.editor.NamedProjectRef
import com.greenezone.gzphotoeditor.editor.SavedPhotoProjectMetadata
import com.greenezone.gzphotoeditor.editor.ExportSettings
import com.greenezone.gzphotoeditor.editor.ImageExportFormat
import com.greenezone.gzphotoeditor.editor.SocialPreset
import com.greenezone.gzphotoeditor.editor.ExportPlacement
import com.greenezone.gzphotoeditor.editor.ExportBackground

@Composable
private fun <T> PickerLine(
    label: String,
    value: T,
    choices: List<T>,
    name: (T) -> String,
    onChoose: (T) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    Column {
        Text(label, color = EditorMuted, fontSize = 12.sp)
        Box(Modifier.fillMaxWidth()) {
            TextButton(onClick = { open = true }) {
                Text(name(value) + "  ▾", color = EditorWhite,
                    fontSize = 15.sp, modifier = Modifier.fillMaxWidth())
            }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                choices.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(name(item), color = EditorWhite) },
                        onClick = { onChoose(item); open = false }
                    )
                }
            }
        }
    }
}

@Composable
fun ExportDialog(
    settings: ExportSettings,
    onSettings: (ExportSettings) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    originalOutput: String = "Original edited image dimensions"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export image", color = EditorWhite) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                PickerLine(
                    "FILE FORMAT", settings.format, ImageExportFormat.entries,
                    { it.label }, { onSettings(settings.copy(format = it)) }
                )
                PickerLine(
                    "SOCIAL PLATFORM / DIMENSIONS", settings.preset, SocialPreset.entries,
                    { it.label }, { onSettings(settings.copy(preset = it)) }
                )
                if (settings.preset != SocialPreset.ORIGINAL) {
                    Text(
                        "Output: " + settings.preset.width + " × " + settings.preset.height + " pixels",
                        color = EditorMint, fontSize = 13.sp
                    )
                    PickerLine(
                        "FRAME CONTENT", settings.placement, ExportPlacement.entries,
                        { it.label }, { onSettings(settings.copy(placement = it)) }
                    )
                    if (settings.placement == ExportPlacement.FIT) {
                        PickerLine(
                            "SPACE AROUND PHOTO", settings.background,
                            ExportBackground.entries,
                            { it.label }, { onSettings(settings.copy(background = it)) }
                        )
                    } else {
                        Text("Fill may crop the image's outer edges.",
                            color = EditorMuted, fontSize = 12.sp)
                    }
                } else {
                    Text("Output: " + originalOutput,
                        color = EditorMuted, fontSize = 12.sp)
                }
                val qualityApplicable = settings.format == ImageExportFormat.JPEG ||
                    settings.format == ImageExportFormat.WEBP ||
                    settings.format == ImageExportFormat.WEBP_LOSSLESS
                if (qualityApplicable) {
                    Text(if (settings.format == ImageExportFormat.WEBP_LOSSLESS)
                        "WebP compression effort" else "Image quality",
                        color = EditorWhite, fontSize = 12.sp)
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        listOf(80, 90, 95, 100).forEach { quality ->
                            DockChip(quality.toString(), settings.quality == quality) {
                                onSettings(settings.copy(quality = quality))
                            }
                        }
                    }
                }
                Text(
                    "New file only. No watermark. JPEG, BMP, TIFF and PDF flatten " +
                        "transparency over white. Some photo apps cannot display TIFF or BMP.",
                    color = EditorMuted, fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("Save " + settings.format.extension.uppercase(), color = EditorMint)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = EditorMuted) }
        }
    )
}

@Composable
fun TextEditDialog(
    content: String,
    onContent: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit text", color = EditorWhite) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Write one or more lines. Position and style them in Text tools.",
                    color = EditorMuted, fontSize = 12.sp)
                TextField(
                    value = content,
                    onValueChange = { onContent(it.take(500)) },
                    minLines = 3,
                    maxLines = 7,
                    label = { Text("Your text") }
                )
                Text(content.length.toString() + " / 500",
                    color = EditorMuted, fontSize = 11.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = content.isNotBlank()) {
                Text("Apply", color = EditorMint)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = EditorMuted) }
        }
    )
}

@Composable
fun SaveNameDialog(
    title: String,
    description: String,
    name: String,
    onName: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    error: String? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = EditorWhite) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(description, color = EditorMuted, fontSize = 13.sp)
                TextField(
                    value = name,
                    onValueChange = { onName(it.take(28)) },
                    singleLine = true,
                    label = { Text("Name") },
                    isError = error != null,
                    supportingText = error?.let { message -> { Text(message) } }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = name.isNotBlank()) {
                Text("Save", color = EditorMint)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = EditorMuted) }
        }
    )
}

@Composable
internal fun ProjectsDialog(
    projects: List<SavedPhotoProjectMetadata>,
    selectedProject: NamedProjectRef?,
    enabled: Boolean,
    onSelect: (NamedProjectRef) -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("My editable projects", color = EditorWhite) },
        text = {
            Column(
                Modifier.heightIn(max = 350.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Text(
                    "Projects save your adjustments and a reference to the original photo. " +
                        "Keep that original available on your phone.",
                    color = EditorMuted,
                    fontSize = 12.sp
                )
                if (projects.isEmpty()) {
                    Text("No saved projects yet.", color = EditorWhite)
                }
                projects.forEach { item ->
                    Column {
                        DockChip(
                            item.project.name,
                            selected = selectedProject == item.project
                        ) { onSelect(item.project) }
                        if (!item.readable) Text("Saved project unavailable", color = EditorMuted, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = enabled && projects.any { it.project == selectedProject && it.readable },
                onClick = onOpen
            ) { Text("Open project", color = EditorMint) }
        },
        dismissButton = {
            Row {
                TextButton(
                    enabled = enabled && projects.any { it.project == selectedProject },
                    onClick = onDelete
                ) { Text("Delete", color = EditorMuted) }
                TextButton(onClick = onDismiss) { Text("Close", color = EditorMuted) }
            }
        }
    )
}

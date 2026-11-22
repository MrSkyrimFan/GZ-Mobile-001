package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenezone.gzphotoeditor.editor.SavedProject

@Composable
fun ExportDialog(
    jpegQuality: Int,
    onQuality: (Int) -> Unit,
    onJpeg: () -> Unit,
    onPng: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export photo", color = EditorWhite) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Save a NEW file. Your original will not be overwritten.",
                    color = EditorMuted,
                    fontSize = 13.sp
                )
                Text("JPEG quality", color = EditorWhite, fontSize = 14.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    listOf(85, 90, 95, 100).forEach { quality ->
                        DockChip(quality.toString(), quality == jpegQuality) { onQuality(quality) }
                    }
                }
                Text(
                    "JPEG is great for sharing. PNG supports transparency. " +
                        "Export resolution adapts to your device's memory.",
                    color = EditorMuted, fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onJpeg) { Text("Save JPEG", color = EditorMint) }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onPng) { Text("Save PNG", color = EditorMint) }
                TextButton(onClick = onDismiss) { Text("Cancel", color = EditorMuted) }
            }
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
    onDismiss: () -> Unit
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
                    label = { Text("Name") }
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
fun ProjectsDialog(
    projects: List<SavedProject>,
    selectedName: String?,
    onSelect: (String) -> Unit,
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
                    DockChip(
                        item.name,
                        selected = selectedName == item.name
                    ) { onSelect(item.name) }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = projects.any { it.name == selectedName },
                onClick = onOpen
            ) { Text("Open project", color = EditorMint) }
        },
        dismissButton = {
            Row {
                TextButton(
                    enabled = projects.any { it.name == selectedName },
                    onClick = onDelete
                ) { Text("Delete", color = EditorMuted) }
                TextButton(onClick = onDismiss) { Text("Close", color = EditorMuted) }
            }
        }
    )
}

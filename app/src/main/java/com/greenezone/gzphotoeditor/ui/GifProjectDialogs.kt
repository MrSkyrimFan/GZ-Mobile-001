package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.GifStore

@Composable
internal fun GifProjectSaveDialog(
    title: String,
    description: String,
    document: GifDocument,
    savedName: String?,
    onSaved: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var draft by remember(savedName) { mutableStateOf(savedName.orEmpty()) }
    var conflict by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf("") }

    fun save(allowReplacement: Boolean = false) {
        val name = GifStore.projectName(draft) ?: return
        val result = GifStore.saveProject(context, draft, document,
            replaceExisting = allowReplacement || name == savedName)
        when (result) {
            GifStore.SaveResult.SAVED -> onSaved(name)
            GifStore.SaveResult.NAME_IN_USE -> conflict = name
            GifStore.SaveResult.FULL -> error = "The saved GIF project list is full (12 projects). Delete a saved project to free a slot."
            GifStore.SaveResult.FAILED -> error = "Could not save this GIF project."
        }
    }

    val existingName = conflict
    if (existingName == null) {
        SaveNameDialog(
            title, if (error.isBlank()) description else "$description\n$error",
            draft, { name -> draft = name; error = "" },
            { save() }, onDismiss
        )
    } else {
        AlertDialog(
            onDismissRequest = { conflict = null },
            title = { Text("Replace GIF project?") },
            text = { Text("Replace the saved project “$existingName” with the current GIF edits?") },
            confirmButton = {
                TextButton(onClick = { conflict = null; save(allowReplacement = true) }) {
                    Text("Replace", color = EditorMint)
                }
            },
            dismissButton = {
                TextButton(onClick = { conflict = null }) { Text("Cancel", color = EditorMuted) }
            }
        )
    }
}

@Composable
internal fun GifSavedProjectsDialog(
    names: List<String>,
    enabled: Boolean,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Boolean,
    onDismiss: () -> Unit
) {
    var deleteName by remember { mutableStateOf<String?>(null) }
    var deleteFailed by remember { mutableStateOf(false) }
    val pending = deleteName
    if (pending == null) {
        AlertDialog(
            onDismissRequest = { if (enabled) onDismiss() },
            title = { Text("Saved GIF projects") },
            text = {
                Column(Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState())) {
                    if (deleteFailed) Text("Could not delete this GIF project.", color = EditorMuted)
                    if (names.isEmpty()) Text("No saved GIF projects yet.", color = EditorMuted)
                    names.forEach { name ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                modifier = Modifier.weight(1f),
                                enabled = enabled,
                                onClick = { onOpen(name) }
                            ) { Text(name, color = EditorMint) }
                            TextButton(
                                modifier = Modifier.semantics { contentDescription = "Delete $name" },
                                enabled = enabled,
                                onClick = {
                                    deleteFailed = false
                                    deleteName = name
                                }
                            ) { Text("Delete", color = EditorMuted) }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = enabled, onClick = onDismiss) { Text("Close") }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = { deleteName = null },
            title = { Text("Delete GIF project?") },
            text = { Text("Delete “$pending”? Your source media and current edits stay available.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteFailed = !onDelete(pending)
                    deleteName = null
                }) { Text("Delete", color = EditorMint) }
            },
            dismissButton = {
                TextButton(onClick = { deleteName = null }) { Text("Cancel", color = EditorMuted) }
            }
        )
    }
}

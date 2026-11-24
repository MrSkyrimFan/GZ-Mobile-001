package com.greenezone.gzphotoeditor.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
            GifStore.SaveResult.FULL -> error = "The saved GIF project list is full (12 projects)."
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

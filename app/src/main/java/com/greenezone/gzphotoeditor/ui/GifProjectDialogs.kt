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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
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
import com.greenezone.gzphotoeditor.editor.NamedProjectRef
import com.greenezone.gzphotoeditor.editor.ProjectSaveIntent
import com.greenezone.gzphotoeditor.editor.ProjectSaveResult
import com.greenezone.gzphotoeditor.editor.ProjectSaveFailure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal data class GifSaveCapture(
    val token: WorkspaceOperationToken,
    val document: GifDocument,
    val project: NamedProjectRef?,
    val saveAs: Boolean
)

@Composable
internal fun GifProjectSaveDialog(
    capture: GifSaveCapture,
    onSaved: (NamedProjectRef) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var draft by remember(capture) { mutableStateOf(if (capture.saveAs) "" else capture.project?.name.orEmpty()) }
    var conflict by remember(capture) { mutableStateOf<NamedProjectRef?>(null) }
    var error by remember(capture) { mutableStateOf("") }
    var saving by remember(capture) { mutableStateOf(false) }

    fun save(intent: ProjectSaveIntent) {
        if (saving) return
        saving = true
        error = ""
        scope.launch {
            val result = withContext(Dispatchers.IO) { GifStore.saveNamed(context, intent, capture.document) }
            saving = false
            when (result) {
                is ProjectSaveResult.Saved -> onSaved(result.project)
                is ProjectSaveResult.Exists -> conflict = result.project
                is ProjectSaveResult.Rejected -> error = when (result.reason) {
                    ProjectSaveFailure.FULL -> "The saved GIF project list is full (12 projects). Delete a saved project to free a slot."
                    ProjectSaveFailure.TARGET_CHANGED -> "This saved project changed. Cancel and reopen it, or save the current edits as a new project."
                    ProjectSaveFailure.TOO_LARGE -> "This GIF project exceeds the supported saved-project size."
                    ProjectSaveFailure.INVALID_DOCUMENT -> "This GIF project cannot be saved with its current sources or timing."
                    ProjectSaveFailure.INVALID_NAME -> "Enter a name for this GIF project."
                    ProjectSaveFailure.STORAGE_FAILURE -> "Could not save this GIF project. Retry or cancel; your edits remain here."
                }
            }
        }
    }
    val current = capture.project
    LaunchedEffect(capture.token.operationId) {
        if (current != null && !capture.saveAs) save(ProjectSaveIntent.Update(current))
    }
    val existing = conflict
    when {
        saving -> AlertDialog(
            onDismissRequest = {},
            title = { Text("Saving GIF project") },
            text = { Text("Your current edits stay here until saving finishes.") },
            confirmButton = {}
        )
        existing != null -> AlertDialog(
            onDismissRequest = { conflict = null },
            title = { Text("Replace GIF project?") },
            text = { Text("Replace the saved project “" + existing.name + "” with the captured GIF edits?") },
            confirmButton = {
                TextButton(onClick = { conflict = null; save(ProjectSaveIntent.Replace(existing)) }) {
                    Text("Replace", color = EditorMint)
                }
            },
            dismissButton = { TextButton(onClick = { conflict = null }) { Text("Cancel", color = EditorMuted) } }
        )
        current != null && !capture.saveAs -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Save GIF project") },
            text = { Text(error.ifBlank { "Preparing the captured GIF project." }) },
            confirmButton = {
                TextButton(onClick = { save(ProjectSaveIntent.Update(current)) }) { Text("Retry", color = EditorMint) }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = EditorMuted) } }
        )
        else -> SaveNameDialog(
            if (capture.saveAs) "Save GIF project as" else "Save GIF project",
            error.ifBlank { "Reopen your sources with these edits and timing settings." },
            draft, { draft = it; error = "" },
            {
                val name = GifStore.projectName(draft)
                if (name == null) error = "Enter a name for this GIF project."
                else save(ProjectSaveIntent.Create(name))
            }, onDismiss
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

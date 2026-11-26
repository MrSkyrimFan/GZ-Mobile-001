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
import kotlinx.coroutines.CancellationException
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
    var retryIntent by remember(capture) { mutableStateOf<ProjectSaveIntent?>(null) }

    fun save(intent: ProjectSaveIntent) {
        if (saving) return
        saving = true
        error = ""
        retryIntent = intent
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) { GifStore.saveNamed(context, intent, capture.document) }
                when (result) {
                    is ProjectSaveResult.Saved -> onSaved(result.project)
                    is ProjectSaveResult.Exists -> { retryIntent = null; conflict = result.project }
                    is ProjectSaveResult.Rejected -> {
                        if (result.reason == ProjectSaveFailure.INVALID_NAME) retryIntent = null
                        error = when (result.reason) {
                            ProjectSaveFailure.FULL -> "The saved GIF project list is full (12 projects). Delete a saved project to free a slot."
                            ProjectSaveFailure.TARGET_CHANGED -> "This saved project changed. Cancel and reopen it, or save the current edits as a new project."
                            ProjectSaveFailure.TOO_LARGE -> "This GIF project exceeds the supported saved-project size."
                            ProjectSaveFailure.INVALID_DOCUMENT -> "This GIF project cannot be saved with its current sources or timing."
                            ProjectSaveFailure.INVALID_NAME -> "Enter a name for this GIF project."
                            ProjectSaveFailure.STORAGE_FAILURE -> "Could not save this GIF project. Retry or cancel; your edits remain here."
                        }
                    }
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { error = "Could not save this GIF project. Retry or cancel; your edits remain here." }
            finally { saving = false }
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
            onDismissRequest = onDismiss,
            title = { Text("Replace GIF project?") },
            text = { Text("Replace the saved project “" + existing.name + "” with the captured GIF edits?") },
            confirmButton = {
                TextButton(onClick = { conflict = null; save(ProjectSaveIntent.Replace(existing)) }) {
                    Text("Replace", color = EditorMint)
                }
            },
            dismissButton = { Row {
                TextButton(onClick = onDismiss) { Text("Cancel", color = EditorMuted) }
                TextButton(onClick = { conflict = null; retryIntent = null; error = "" }) {
                    Text("Choose another name", color = EditorMint)
                }
            } }
        )
        retryIntent != null && error.isNotBlank() -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("GIF project not saved") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { retryIntent?.let(::save) }) { Text("Retry", color = EditorMint) }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = EditorMuted) } }
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
    onIdentifyDelete: suspend (String) -> NamedProjectRef?,
    onDelete: suspend (NamedProjectRef) -> Boolean,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var deleteTarget by remember { mutableStateOf<NamedProjectRef?>(null) }
    var deleteFailed by remember { mutableStateOf(false) }
    var working by remember { mutableStateOf(false) }
    val interactive = enabled && !working
    val pending = deleteTarget
    if (pending == null) {
        AlertDialog(
            onDismissRequest = { if (interactive) onDismiss() },
            title = { Text("Saved GIF projects") },
            text = {
                Column(Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState())) {
                    if (deleteFailed) Text("Could not delete this GIF project.", color = EditorMuted)
                    if (names.isEmpty()) Text("No saved GIF projects yet.", color = EditorMuted)
                    names.forEach { name ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                modifier = Modifier.weight(1f),
                                enabled = interactive,
                                onClick = { onOpen(name) }
                            ) { Text(name, color = EditorMint) }
                            TextButton(
                                modifier = Modifier.semantics { contentDescription = "Delete $name" },
                                enabled = interactive,
                                onClick = {
                                    deleteFailed = false
                                    working = true
                                    scope.launch {
                                        try {
                                            deleteTarget = onIdentifyDelete(name)
                                            deleteFailed = deleteTarget == null
                                        } finally {
                                            working = false
                                        }
                                    }
                                }
                            ) { Text("Delete saved project", color = EditorMuted) }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = interactive, onClick = onDismiss) { Text("Close") }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = { if (interactive) deleteTarget = null },
            title = { Text("Delete GIF project?") },
            text = { Text("Delete “${pending.name}”? Your source media and current edits stay available.") },
            confirmButton = {
                TextButton(enabled = interactive, onClick = {
                    working = true
                    scope.launch {
                        try {
                            deleteFailed = !onDelete(pending)
                            deleteTarget = null
                        } finally {
                            working = false
                        }
                    }
                }) { Text("Delete", color = EditorMint) }
            },
            dismissButton = {
                TextButton(enabled = interactive, onClick = { deleteTarget = null }) {
                    Text("Cancel", color = EditorMuted)
                }
            }
        )
    }
}

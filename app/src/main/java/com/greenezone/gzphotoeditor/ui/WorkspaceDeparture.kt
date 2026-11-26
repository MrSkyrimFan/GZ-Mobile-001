package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.util.UUID

/** Immutable receipt for one native owner and captured semantic revision. */
data class WorkspaceOperationToken(
    val ownerId: String,
    val contentRevision: Long,
    val operationId: String = UUID.randomUUID().toString()
)

/** The document adapter owns snapshots/targets; this gate only prevents stale or duplicate completion. */
class WorkspaceOperationGate {
    private var pending: WorkspaceOperationToken? = null
    fun begin(ownerId: String, contentRevision: Long): WorkspaceOperationToken? {
        if (pending != null) return null
        return WorkspaceOperationToken(ownerId, contentRevision).also { pending = it }
    }
    fun owns(token: WorkspaceOperationToken, ownerId: String, contentRevision: Long): Boolean =
        pending == token && token.ownerId == ownerId && token.contentRevision == contentRevision
    fun cancel() { pending = null }
    fun consume(token: WorkspaceOperationToken, ownerId: String, contentRevision: Long): Boolean {
        if (!owns(token, ownerId, contentRevision)) return false
        pending = null
        return true
    }
}

@Composable
fun WorkspaceDepartureDialog(
    title: String = "Unsaved changes",
    message: String,
    saveEnabled: Boolean = true,
    saveDisabledReason: String? = null,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onCancel: () -> Unit,
    saveLabel: String = "Save",
    discardLabel: String = "Discard",
    cancelLabel: String = "Cancel"
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(title) },
        text = {
            Column {
                Text(message)
                if (!saveEnabled && saveDisabledReason != null) Text(saveDisabledReason)
            }
        },
        confirmButton = {
            Column {
                TextButton(enabled = saveEnabled, onClick = onSave) { Text(saveLabel) }
                TextButton(onClick = onCancel) { Text(cancelLabel) }
            }
        },
        dismissButton = { TextButton(onClick = onDiscard) { Text(discardLabel) } }
    )
}

@Composable
fun WorkspaceDeleteDialog(
    message: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    busy: Boolean = false,
    error: String? = null
) {
    AlertDialog(onDismissRequest = { if (!busy) onCancel() },
        title = { Text("Delete current work?") },
        text = { Column {
            Text(message)
            Text("Your original media and named saved projects will be kept.")
            error?.let { Text(it) }
        } },
        confirmButton = { TextButton(enabled = !busy, onClick = onConfirm) { Text("Delete") } },
        dismissButton = { TextButton(enabled = !busy, onClick = onCancel) { Text("Cancel") } })
}

internal data class WorkspaceTransferPresentation(
    val destination: WorkspaceMode,
    val saveUnavailable: String? = null,
    val bringUnavailable: String? = null,
    val name: String? = null,
    val collisionName: String? = null,
    val error: String? = null,
    val working: Boolean = false,
    val cancelEnabled: Boolean = true
)

@Composable
internal fun WorkspaceTransferDialog(
    state: WorkspaceTransferPresentation,
    onDiscard: () -> Unit,
    onSave: () -> Unit,
    onBring: () -> Unit,
    onCancel: () -> Unit,
    onNameChange: (String) -> Unit,
    onReplace: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (state.cancelEnabled) onCancel() },
        title = { Text("Switch to " + state.destination.label + "?") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("Discard or save the current work to open a blank workspace. Bring current work to continue editing it there.")
                state.saveUnavailable?.let { Text(it) }
                state.bringUnavailable?.let { Text(it) }
                state.name?.let { name ->
                    OutlinedTextField(value = name, onValueChange = onNameChange,
                        enabled = !state.working, singleLine = true, label = { Text("Project name") },
                        modifier = Modifier.fillMaxWidth())
                }
                state.collisionName?.let { name ->
                    Text("A saved project named “" + name + "” already exists.")
                    TextButton(enabled = !state.working, onClick = onReplace) {
                        Text("Replace saved project")
                    }
                }
                state.error?.let { Text(it) }
                if (state.working) Text("Preparing the workspace…")
                TextButton(enabled = !state.working, onClick = onDiscard) { Text("Discard") }
                TextButton(enabled = !state.working && state.saveUnavailable == null, onClick = onSave) {
                    Text("Save current work")
                }
                TextButton(enabled = !state.working && state.bringUnavailable == null, onClick = onBring) {
                    Text("Bring current work")
                }
            }
        },
        confirmButton = { TextButton(enabled = state.cancelEnabled, onClick = onCancel) { Text("Cancel") } }
    )
}

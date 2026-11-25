package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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

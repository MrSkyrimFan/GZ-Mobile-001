package com.greenezone.gzphotoeditor.ui

import android.content.Intent
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import com.greenezone.gzphotoeditor.editor.PendingExport
import com.greenezone.gzphotoeditor.editor.PendingExportPhase

/**
 * Use a stable key for the operation so recreation can receive Android's delayed
 * result. A retry uses a new key and cannot consume a result from the old picker.
 */
@Composable
internal fun RecoverableExportPicker(
    pending: PendingExport?,
    launchId: String?,
    onLaunch: (String) -> Unit,
    onLaunchFailed: (PendingExport, Exception) -> Unit,
    onResult: (PendingExport, ActivityResult) -> Unit
) {
    val registry = LocalActivityResultRegistryOwner.current?.activityResultRegistry
    val latestLaunch by rememberUpdatedState(onLaunch)
    val latestFailure by rememberUpdatedState(onLaunchFailed)
    val latestResult by rememberUpdatedState(onResult)
    val picking = pending?.takeIf { it.phase == PendingExportPhase.PICKING }
    var launcher by remember(registry, picking?.id) {
        mutableStateOf<ActivityResultLauncher<Intent>?>(null)
    }
    DisposableEffect(registry, picking?.id) {
        if (registry == null || picking == null) return@DisposableEffect onDispose {}
        val registration = registry.register("gz-export:" + picking.id,
            ActivityResultContracts.StartActivityForResult()) { latestResult(picking, it) }
        launcher = registration
        onDispose { registration.unregister() }
    }
    // Video GIF can finish staging after its stable result key is registered.
    LaunchedEffect(launcher, launchId, picking?.id) {
        val request = picking ?: return@LaunchedEffect
        val registration = launcher ?: return@LaunchedEffect
        if (request.id != launchId) return@LaunchedEffect
        latestLaunch(request.id)
        try {
            registration.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = request.mime
                putExtra(Intent.EXTRA_TITLE, request.filename)
            })
        } catch (error: Exception) {
            latestFailure(request, error)
        }
    }
}

@Composable
internal fun RecoverableExportDialog(
    request: PendingExport,
    onRetry: () -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit,
    unavailable: String? = null,
    failure: String? = null
) {
    val message = unavailable ?: when (request.phase) {
        PendingExportPhase.COPYING, PendingExportPhase.DESTINATION_UNCERTAIN ->
            "The previous destination may contain an incomplete or already finished file. Choose a new destination to retry with the saved edits."
        else -> "Your export settings and edits were kept. Choose a destination to try again."
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (unavailable == null) "Resume export" else "Export recovery unavailable") },
        text = { Text(if (failure == null) message else failure + "\n\n" + message) },
        confirmButton = {
            if (unavailable == null) TextButton(onClick = onRetry) { Text("Retry export") }
        },
        dismissButton = { TextButton(onClick = onDiscard) { Text("Discard request") } }
    )
}

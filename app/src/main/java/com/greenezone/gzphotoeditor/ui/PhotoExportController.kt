package com.greenezone.gzphotoeditor.ui

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.EditDocumentCodec
import com.greenezone.gzphotoeditor.editor.ExportSettings
import com.greenezone.gzphotoeditor.editor.ExportSizing
import com.greenezone.gzphotoeditor.editor.ExportWorkspace
import com.greenezone.gzphotoeditor.editor.GeometryMath
import com.greenezone.gzphotoeditor.editor.ImageExportWriter
import com.greenezone.gzphotoeditor.editor.ImagePipeline
import com.greenezone.gzphotoeditor.editor.PendingExport
import com.greenezone.gzphotoeditor.editor.PendingExportJournal
import com.greenezone.gzphotoeditor.editor.PendingExportPhase
import com.greenezone.gzphotoeditor.editor.PendingImageRecipe
import com.greenezone.gzphotoeditor.editor.PhotoGeometry
import com.greenezone.gzphotoeditor.editor.PhotoGeometryPlan
import com.greenezone.gzphotoeditor.editor.SocialPreset
import com.greenezone.gzphotoeditor.editor.writeStagedImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

internal data class PhotoExportRequest(
    val source: Uri,
    val settings: ExportSettings,
    val edits: EditDocument
) {
    fun encode(): String {
        val photo = JSONObject().apply {
            put("source", source.toString())
            put("edits", EditDocumentCodec.encodeBounded(edits))
        }
        return PendingImageRecipe(settings, photo.toString()).encode()
    }

    companion object {
        fun decode(pending: PendingExport): PhotoExportRequest? {
            if (pending.workspace != ExportWorkspace.PHOTO) return null
            val image = PendingImageRecipe.decode(pending.recipe) ?: return null
            if (image.settings.format.mime != pending.mime) return null
            return try {
                val photo = JSONObject(image.payload)
                val source = Uri.parse(photo.getString("source"))
                val edits = EditDocumentCodec.decode(photo.getString("edits")) ?: return null
                source.takeIf { it.scheme == "content" }?.let { PhotoExportRequest(it, image.settings, edits) }
            } catch (_: Exception) { null }
        }
    }
}

internal class PhotoExportController(
    private val context: Context,
    private val scope: CoroutineScope,
    private val journal: PendingExportJournal,
    restorePending: Boolean,
    private val report: (String) -> Unit
) {
    private val resolver = context.contentResolver
    private val exportMaxPixels = GeometryMath.safeExportPixels(
        (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).memoryClass
    )
    private val initial = if (restorePending) journal.read(ExportWorkspace.PHOTO) else null
    val initialRecoveryId: String? = initial?.id
    val nativeRecoveryExpected: Boolean = initial?.restoreNativeDraft == true
    val recoveredRequest = initial?.takeIf { it.restoreNativeDraft }?.let(PhotoExportRequest::decode)
    val recoveredOwnerId: String? = initial?.let { it.nativeOwnerId ?: it.id }
    private val running = mutableStateOf(false)
    val inProgress: State<Boolean> = running

    var pending by mutableStateOf(initial)
        private set
    var launchId by mutableStateOf<String?>(null)
        private set
    var recoveryVisible by mutableStateOf(initial != null)
        private set
    var dialogVisible by mutableStateOf(false)
        private set
    var settings by mutableStateOf(recoveredRequest?.settings ?: ExportSettings())
        private set

    fun openDialog() { dialogVisible = true }
    fun closeDialog() { dialogVisible = false }
    fun updateSettings(next: ExportSettings) { settings = next }
    fun hideRecovery() { recoveryVisible = false }

    fun consumeLaunch(id: String) {
        if (launchId == id) launchId = null
    }

    fun onLaunchFailed(record: PendingExport, error: Exception) {
        retainForRetry(record, error.message ?: "Could not open the destination picker")
    }

    fun onResult(record: PendingExport, result: ActivityResult) {
        try {
            val active = journal.read(ExportWorkspace.PHOTO)
            if (active == null || active.id != record.id || active.phase != PendingExportPhase.PICKING) return
            if (result.resultCode != Activity.RESULT_OK) {
                if (journal.cancelPicker(record)) {
                    pending = null
                    recoveryVisible = false
                    report("Export canceled")
                }
                return
            }
            val output = result.data?.data
            val request = PhotoExportRequest.decode(record)
            if (output == null || request == null) {
                retainForRetry(record, "The export destination or recipe could not be restored.")
                return
            }
            val claimed = journal.claim(record, output) ?: return
            pending = claimed
            write(output, request, claimed)
        } catch (error: Exception) {
            retainForRetry(record, error.message ?: "Could not start the export")
        }
    }

    fun request(source: Uri?, document: EditDocument, nativeOwnerId: String? = null) {
        if (source == null) return
        closeDialog()
        if (pending != null) {
            recoveryVisible = true
            return
        }
        val selectedSettings = settings
        val target = if (selectedSettings.preset == SocialPreset.ORIGINAL) "GZEditorPro_Edit"
            else "GZEditorPro_" + selectedSettings.preset.name
        try {
            val record = journal.begin(ExportWorkspace.PHOTO, selectedSettings.format.mime,
                target + "." + selectedSettings.format.extension,
                PhotoExportRequest(source, selectedSettings, document).encode(), nativeOwnerId)
            pending = record
            launchId = record.id
        } catch (_: EditDocumentCodec.RecipeTooLarge) {
            report("Could not prepare export: too many edit details. Remove some drawing or brush details and try again.")
        } catch (_: OutOfMemoryError) {
            report("Not enough memory to prepare this export. Your current edits are still open.")
        } catch (error: Exception) {
            report("Could not prepare export: " + (error.message ?: "local storage failed"))
        }
    }

    fun adoptNativeDisposition(records: List<PendingExport>) {
        val current = pending ?: return
        records.firstOrNull { it.workspace == ExportWorkspace.PHOTO && it.id == current.id }
            ?.let { pending = it }
    }

    fun resolveNativeDraft(ownerId: String): Boolean = try {
        pending?.let { record ->
            journal.suppressNativeRestore(record, ownerId)?.let { pending = it }
        }
        true
    } catch (_: Exception) {
        report("Could not record Discard. Your working photo is still open.")
        false
    }

    fun retry(record: PendingExport) {
        try {
            val next = journal.retry(record)
            pending = next
            recoveryVisible = false
            launchId = next.id
        } catch (error: Exception) {
            report(error.message ?: "Could not retry the export")
        }
    }

    fun discard(record: PendingExport) {
        try {
            journal.discard(record)
            pending = null
            recoveryVisible = false
        } catch (error: Exception) {
            report(error.message ?: "Could not clear the export request")
        }
    }

    fun originalOutput(sourceSize: Pair<Int, Int>?, document: EditDocument): String {
        val (width, height) = sourceSize ?: return "Available after opening a photo"
        val (decodeWidth, decodeHeight) = ImagePipeline.decodeSize(
            width, height, ImagePipeline.EXPORT_MAX_EDGE, exportMaxPixels
        )
        val geometry = PhotoGeometryPlan(decodeWidth, decodeHeight, PhotoGeometry.from(document))
        val result = "${geometry.width} × ${geometry.height} pixels"
        return if (width != decodeWidth || height != decodeHeight) {
            "$result (limited by device memory or maximum image size)"
        } else result
    }

    private fun retainForRetry(record: PendingExport, message: String) {
        report(message)
        try {
            pending = journal.failed(record)
        } catch (_: Exception) {
            // The last persisted phase still prevents another write on redelivery.
            pending = journal.read(ExportWorkspace.PHOTO)
        }
        recoveryVisible = pending != null
    }

    private fun write(output: Uri, request: PhotoExportRequest, record: PendingExport) {
        if (request.source == output) {
            retainForRetry(record, "Export destination cannot overwrite the original.")
            return
        }
        scope.launch {
            running.value = true
            closeDialog()
            recoveryVisible = false
            report("Exporting image…")
            try {
                val dimensions = export(output, request, record)
                journal.discard(record)
                pending = null
                report("Saved " + dimensions + " • Original unchanged")
            } catch (cancelled: CancellationException) {
                // Keep the recorded phase so a new host can offer a new destination.
                throw cancelled
            } catch (_: OutOfMemoryError) {
                retainForRetry(record, "Not enough memory to export. Choose a smaller size.")
            } catch (error: Exception) {
                retainForRetry(record, "Export failed: " + (error.message ?: "unknown error"))
            } finally {
                running.value = false
            }
        }
    }

    private suspend fun export(output: Uri, request: PhotoExportRequest, record: PendingExport): String =
        withContext(Dispatchers.Default) {
            var decoded: Bitmap? = null
            var rendered: Bitmap? = null
            var sized: Bitmap? = null
            try {
                val source = ImagePipeline.decode(resolver, request.source,
                    ImagePipeline.EXPORT_MAX_EDGE, exportMaxPixels)
                decoded = source
                val full = ImagePipeline.render(source, request.edits) { ensureActive() }
                rendered = full
                val photo = ExportSizing.layout(full, request.settings)
                sized = photo
                withContext(Dispatchers.IO) {
                    writeStagedImage(context.cacheDir,
                        openDestination = {
                            journal.copying(record)
                            resolver.openOutputStream(output, "wt")
                        },
                        checkActive = { ensureActive() }) { stream ->
                        ImageExportWriter.write(photo, request.settings, stream)
                    }
                }
                photo.width.toString() + " × " + photo.height
            } finally {
                sized?.let { if (it !== rendered && !it.isRecycled) it.recycle() }
                rendered?.let { if (it !== decoded && !it.isRecycled) it.recycle() }
                decoded?.let { if (!it.isRecycled) it.recycle() }
            }
        }
}

@Composable
internal fun rememberPhotoExportController(
    journal: PendingExportJournal,
    restorePending: Boolean,
    report: (String) -> Unit
): PhotoExportController {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val latestReport by rememberUpdatedState(report)
    val owner = remember(context, journal, restorePending) {
        PhotoExportController(context, scope, journal, restorePending) { latestReport(it) }
    }
    RecoverableExportPicker(
        pending = owner.pending,
        launchId = owner.launchId,
        onLaunch = owner::consumeLaunch,
        onLaunchFailed = owner::onLaunchFailed,
        onResult = owner::onResult
    )
    return owner
}

@Composable
internal fun PhotoExportDialogs(
    owner: PhotoExportController,
    source: Uri?,
    sourceSize: Pair<Int, Int>?,
    document: EditDocument,
    busy: Boolean,
    notice: String,
    nativeOwnerId: String? = null
) {
    val recovery = owner.pending
    if (owner.recoveryVisible && recovery != null && !busy && !owner.inProgress.value) {
        val recoverable = remember(recovery.id) { PhotoExportRequest.decode(recovery) != null }
        RecoverableExportDialog(
            request = recovery,
            unavailable = if (recoverable) null else "The previous export recipe could not be restored.",
            failure = notice.takeIf { it.startsWith("Export failed:") },
            onRetry = { owner.retry(recovery) },
            onDiscard = { owner.discard(recovery) },
            onDismiss = owner::hideRecovery
        )
    }
    if (owner.dialogVisible) {
        ExportDialog(
            settings = owner.settings,
            onSettings = owner::updateSettings,
            onSave = { owner.request(source, document, nativeOwnerId) },
            onDismiss = owner::closeDialog,
            originalOutput = owner.originalOutput(sourceSize, document)
        )
    }
}

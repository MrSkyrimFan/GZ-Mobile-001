package com.greenezone.gzphotoeditor.ui

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.greenezone.gzphotoeditor.editor.ExportWorkspace
import com.greenezone.gzphotoeditor.editor.GifDiagnosticKind
import com.greenezone.gzphotoeditor.editor.GifDiagnosticStage
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.GifEncoder
import com.greenezone.gzphotoeditor.editor.GifExportGuard
import com.greenezone.gzphotoeditor.editor.GifPreviewDiagnostics
import com.greenezone.gzphotoeditor.editor.GifStore
import com.greenezone.gzphotoeditor.editor.GifVideoMath
import com.greenezone.gzphotoeditor.editor.ImagePipeline
import com.greenezone.gzphotoeditor.editor.PendingExport
import com.greenezone.gzphotoeditor.editor.PendingExportJournal
import com.greenezone.gzphotoeditor.editor.PendingExportPhase
import com.greenezone.gzphotoeditor.editor.VideoGifSource
import com.greenezone.gzphotoeditor.editor.copyStagedImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException

internal data class PendingGifRecipe(val document: GifDocument, val savedName: String?) {
    fun encode(): String = JSONObject().apply {
        put("document", GifStore.encode(document))
        if (savedName != null) put("savedName", savedName)
    }.toString()

    companion object {
        fun decode(value: String): PendingGifRecipe? = try {
            val json = JSONObject(value)
            GifStore.decode(json.getString("document"))?.let { document ->
                PendingGifRecipe(document, json.optString("savedName").takeIf { it.isNotBlank() })
            }
        } catch (_: Exception) { null }
    }
}

@Composable
internal fun rememberGifExport(
    video: Boolean,
    videoReaderOwner: Mutex? = null,
    onNotice: (String) -> Unit,
    onSaved: (Uri, GifDocument) -> Unit
): GifExportOperation {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val latestNotice by rememberUpdatedState(onNotice)
    val latestSaved by rememberUpdatedState(onSaved)
    return remember(context, video, videoReaderOwner, scope) {
        GifExportOperation(context, scope, video, videoReaderOwner,
            { latestNotice(it) }, { uri, document -> latestSaved(uri, document) })
    }
}

@Composable
internal fun GifExportRecovery(operation: GifExportOperation) {
    RecoverableExportPicker(operation.request, operation.launchId, operation::launched,
        operation::launchFailed, operation::receive)
    val request = operation.request
    if (request != null && operation.recoveryOpen) {
        val recoverable = remember(request.id) { PendingGifRecipe.decode(request.recipe) != null }
        RecoverableExportDialog(request, operation::retry, operation::discard,
            onDismiss = { operation.recoveryOpen = false },
            unavailable = if (recoverable) null else "The previous GIF recipe could not be restored.",
            failure = operation.recoveryFailure)
    }
}

internal class GifExportOperation(
    private val context: Context,
    private val scope: CoroutineScope,
    private val video: Boolean,
    private val videoReaderOwner: Mutex?,
    private val onNotice: (String) -> Unit,
    private val onSaved: (Uri, GifDocument) -> Unit
) {
    private val journal = PendingExportJournal(context.filesDir)
    private val stages = GifExportStages(context.cacheDir)
    private val resolver = context.contentResolver
    private var job: Job? = null
    private var diagnosticId: Long? = null

    var request by mutableStateOf(readRequest())
        private set
    var launchId by mutableStateOf<String?>(null)
        private set
    var recoveryOpen by mutableStateOf(request != null)
    var recoveryFailure by mutableStateOf<String?>(null)
        private set
    var active by mutableStateOf(false)
        private set
    var committing by mutableStateOf(false)
        private set
    var progress by mutableStateOf("")
        private set

    init {
        stages.reconcile(journal.read(ExportWorkspace.GIF)?.id)
    }

    private fun readRequest(): PendingExport? =
        journal.read(ExportWorkspace.GIF)?.takeIf {
            val recipe = PendingGifRecipe.decode(it.recipe)
            if (recipe == null) video else (recipe.document.video != null) == video
        }

    fun start(recipe: PendingGifRecipe, operationId: Long? = null) {
        if (active) return
        val prior = readRequest()
        if (prior != null) {
            request = prior
            recoveryOpen = true
            return
        }
        diagnosticId = operationId
        try {
            val filename = if (video) "GZEditorPro.gif" else "GZEditorPro_Animation.gif"
            val pending = journal.begin(ExportWorkspace.GIF, "image/gif", filename, recipe.encode())
            request = pending
            beginPicker(pending, recipe.document)
        } catch (error: Exception) {
            onNotice("GIF export failed. ${error.message.orEmpty()}")
        }
    }

    fun retry() {
        val prior = request ?: return
        if (active) return
        try {
            val recipe = requireNotNull(PendingGifRecipe.decode(prior.recipe)) { "Saved GIF edits are unavailable." }
            val next = journal.retry(prior)
            stages.transfer(prior, next)
            request = next
            recoveryOpen = false
            beginPicker(next, recipe.document)
        } catch (error: Exception) {
            onNotice("GIF export failed. ${error.message.orEmpty()}")
        }
    }

    private fun beginPicker(pending: PendingExport, document: GifDocument) {
        active = true
        onNotice("")
        trace(GifDiagnosticKind.EXPORT_STARTED, document)
        if (!video) {
            askDestination(pending)
            return
        }
        progress = "Writing GIF…"
        job = scope.launch {
            try {
                prepareStage(pending, document)
                trace(GifDiagnosticKind.EXPORT_STAGED, document)
                askDestination(pending)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                fail(pending, error)
            } catch (error: OutOfMemoryError) {
                fail(pending, error)
            } finally {
                if (job === coroutineContext[Job]) job = null
            }
        }
    }

    private fun askDestination(pending: PendingExport) {
        progress = "Choose where to save your GIF"
        launchId = pending.id
    }

    fun launched(id: String) {
        if (launchId == id) launchId = null
    }

    fun launchFailed(expected: PendingExport, error: Exception) = fail(expected, error)

    fun receive(expected: PendingExport, result: ActivityResult) {
        val destination = result.data?.data?.takeIf { result.resultCode == Activity.RESULT_OK }
        val claimed = try {
            if (destination == null) {
                if (result.resultCode == Activity.RESULT_OK) {
                    fail(expected, IOException("The picker did not return a destination."))
                    return
                }
                if (journal.cancelPicker(expected)) {
                    clearRequest(expected)
                    trace(GifDiagnosticKind.EXPORT_DESTINATION_CANCELLED)
                    onNotice("GIF export cancelled")
                }
                return
            }
            journal.claim(expected, destination) ?: return
        } catch (error: Exception) {
            fail(expected, error)
            return
        }
        request = claimed
        recoveryOpen = false
        active = true
        progress = if (video) "Saving GIF…" else "Encoding GIF on your phone…"
        job = scope.launch {
            try {
                val doc = requireNotNull(PendingGifRecipe.decode(claimed.recipe)) {
                    "Saved GIF edits are unavailable."
                }.document
                require(destination.toString() != doc.video?.uri && destination.toString() !in doc.frames) {
                    "Choose a new filename to keep the original."
                }
                val staged = prepareStage(claimed, doc)
                committing = true
                progress = "Saving GIF…"
                withContext(Dispatchers.IO) {
                    val copy = currentCoroutineContext()
                    copyStagedImage(staged, openDestination = {
                        journal.copying(claimed)
                        resolver.openOutputStream(destination, "wt")
                            ?: throw IOException("This provider cannot write the destination. Choose Files instead.")
                    }, checkActive = { copy.ensureActive() })
                    journal.discard(claimed)
                    stages.delete(claimed)
                }
                clearRequest(claimed)
                trace(GifDiagnosticKind.EXPORT_COMPLETED, doc)
                onSaved(destination, doc)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                fail(claimed, error)
            } catch (error: OutOfMemoryError) {
                fail(claimed, error)
            } finally {
                if (job === coroutineContext[Job]) job = null
            }
        }
    }

    fun cancel() {
        if (committing) return
        val pending = request ?: return
        try {
            if (!journal.discard(pending)) return
            job?.cancel()
            clearRequest(pending)
            trace(GifDiagnosticKind.EXPORT_CANCELLED)
            onNotice("GIF export cancelled")
        } catch (error: Exception) {
            onNotice("Could not cancel the GIF export. ${error.message.orEmpty()}")
        }
    }

    fun discard() {
        val pending = request ?: return
        try {
            if (journal.discard(pending)) clearRequest(pending)
        } catch (error: Exception) {
            onNotice("Could not discard the GIF export. ${error.message.orEmpty()}")
        }
    }

    private fun clearRequest(previous: PendingExport) {
        stages.delete(previous)
        request = null
        launchId = null
        recoveryOpen = false
        recoveryFailure = null
        committing = false
        active = false
    }

    private fun fail(previous: PendingExport, error: Throwable) {
        if (request?.id != previous.id) return
        request = try {
            journal.failed(previous) ?: readRequest()
        } catch (_: Exception) {
            readRequest()
        }
        trace(GifDiagnosticKind.EXPORT_FAILED, error = error)
        val copyMayBeIncomplete = request?.phase == PendingExportPhase.DESTINATION_UNCERTAIN
        val explanation = when {
            copyMayBeIncomplete -> "The destination may contain an incomplete GIF. Retry with a new destination."
            error is OutOfMemoryError -> "Not enough memory to export. Choose a smaller canvas size and try again."
            else -> error.message.orEmpty()
        }
        recoveryFailure = "GIF export failed. $explanation"
        onNotice(requireNotNull(recoveryFailure))
        active = false
        committing = false
        launchId = null
        recoveryOpen = request != null
    }

    private suspend fun prepareStage(pending: PendingExport, document: GifDocument): File =
        withContext(Dispatchers.IO) {
            val encode = currentCoroutineContext()
            stages.prepare(pending) { file ->
                if (video) {
                    val quota = GifExportGuard.maxTemporaryBytes(context.cacheDir.usableSpace)
                    if (quota < GifExportGuard.MIN_EXPORT_BYTES) {
                        throw IOException("Not enough free storage to safely create a GIF. Free at least 72 MB.")
                    }
                    requireNotNull(videoReaderOwner).withLock {
                        GifExportGuard.limit(file.outputStream(), quota).buffered(65_536).use { stream ->
                            VideoGifSource.write(context, document, stream, cancel = { encode.ensureActive() },
                                onProgress = { index, count ->
                                    if (index == count || index % 8 == 0) {
                                        scope.launch {
                                            if (request?.id == pending.id && active && !committing) {
                                                progress = "Writing frame $index of $count…"
                                            }
                                        }
                                    }
                                })
                        }
                    }
                } else {
                    file.outputStream().buffered(65_536).use { stream ->
                        GifEncoder.write(stream, document, cancel = { encode.ensureActive() }) { index ->
                            ImagePipeline.decode(resolver, Uri.parse(document.frames[index]),
                                document.size.maxEdge * 2, 1_100_000L)
                        }
                    }
                }
            }
        }

    private fun trace(kind: GifDiagnosticKind, doc: GifDocument? = null, error: Throwable? = null) {
        diagnosticId?.let { id ->
            GifPreviewDiagnostics.record(id, stage = GifDiagnosticStage.G6_EXPORT, kind = kind,
                frameCount = doc?.video?.let(GifVideoMath::frameCount), failure = error)
        }
    }
}

private class GifExportStages(cacheDirectory: File) {
    private val folder = File(cacheDirectory, "gz-gif-export").also { it.mkdirs() }
    private val owned = Regex("[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}\\.(gif|part)")

    private fun complete(request: PendingExport) = File(folder, request.id + ".gif")
    private fun partial(request: PendingExport) = File(folder, request.id + ".part")

    suspend fun prepare(request: PendingExport, encode: suspend (File) -> Unit): File {
        val completed = complete(request)
        if (completed.isFile && completed.length() > 0) return completed
        val staged = partial(request)
        try {
            encode(staged)
            currentCoroutineContext().ensureActive()
            if (staged.length() == 0L || !staged.renameTo(completed)) {
                throw IOException("Could not prepare the GIF for saving")
            }
            return completed
        } finally {
            staged.delete()
        }
    }

    fun transfer(previous: PendingExport, next: PendingExport) {
        complete(previous).renameTo(complete(next))
        delete(previous)
    }

    fun delete(request: PendingExport) {
        partial(request).delete()
        complete(request).delete()
    }

    fun reconcile(active: String?) {
        folder.listFiles()?.forEach { file ->
            if (file.isFile && owned.matches(file.name) && file.name.substringBefore('.') != active) file.delete()
        }
    }
}

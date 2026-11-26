package com.greenezone.gzphotoeditor.ui

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import com.greenezone.gzphotoeditor.editor.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.util.UUID

internal class PreparedRegularPhoto(
    val bitmap: Bitmap,
    val dimensions: Pair<Int, Int>,
    val preview: Bitmap
) {
    fun discard() {
        if (preview !== bitmap) preview.recycle()
        bitmap.recycle()
    }
}

/** Prepare a usable photo before a route or the old work can be replaced. */
internal suspend fun prepareRegularPhoto(
    resolver: ContentResolver,
    uri: Uri,
    edits: EditDocument?
): PreparedRegularPhoto {
    val (bitmap, dimensions) = withContext(Dispatchers.IO) {
        var header: Pair<Int, Int>? = null
        val input = ImagePipeline.decode(resolver, uri, ImagePipeline.PREVIEW_MAX_EDGE, 2_000_000L) { w, h ->
            header = w to h
        }
        input to checkNotNull(header)
    }
    return try {
        val preview = if (edits != null && edits.privacy.isNotEmpty()) {
            withContext(Dispatchers.Default) {
                val job = currentCoroutineContext()
                ImagePipeline.render(bitmap, edits) { job.ensureActive() }
            }
        } else bitmap
        PreparedRegularPhoto(bitmap, dimensions, preview)
    } catch (failure: Throwable) {
        bitmap.recycle()
        throw failure
    }
}

/** Only stages immutable inputs. The host acknowledges the route after rechecking both owners. */
internal suspend fun prepareBring(
    store: NativeWorkStore,
    resolver: ContentResolver,
    from: NativeWorkState
): NativeWorkResult<NativeWorkState> {
    if (from.mode == NativeWorkMode.PRO) {
        val pro = from.pro ?: return NativeWorkResult.Failed(NativeWorkFailure.UNAVAILABLE)
        val derivative = when (val staged = store.stageDerivative(pro.document)) {
            is NativeWorkResult.Ok -> staged.value
            is NativeWorkResult.Failed -> return staged
        }
        val prior = from.regular
        val regular = prior?.takeIf { it.uri == derivative.uri } ?: NativeRegularState(
            ownerId = UUID.randomUUID().toString(),
            sourceId = derivative.sha,
            sourceRevision = 0,
            contentRevision = 0,
            uri = derivative.uri,
            document = EditDocument(),
            entry = EditDocument(),
            clean = EditDocument(),
            reset = EditDocument(),
            history = EditHistory(),
            savedRef = null
        )
        val link = (from.link ?: NativeWorkLink()).copy(
            derivative = derivative, regularInput = derivative.size
        )
        return NativeWorkResult.Ok(from.copy(mode = NativeWorkMode.REGULAR, regular = regular, link = link))
    }

    val regular = from.regular ?: return NativeWorkResult.Failed(NativeWorkFailure.UNAVAILABLE)
    val pro = from.pro
    val derivative = from.link?.derivative
    if (pro != null && derivative != null && regular.uri == derivative.uri) {
        val activePro = if (regular.document == EditDocument()) pro else {
            val finish = when (val staged = store.stageFinish(pro.document, derivative, regular.document)) {
                is NativeWorkResult.Ok -> staged.value
                is NativeWorkResult.Failed -> return staged
            }
            // The active Regular finish is now a native revision in Pro. A later
            // unedited return has a new neutral Regular recipe over its rendering.
            val updated = pro.document.copy(linkedFinishes = pro.document.linkedFinishes + finish)
            pro.copy(document = updated, contentRevision = pro.contentRevision + 1,
                history = pro.history.checkpoint(
                    ProHistoryEntry(pro.document, pro.reset, pro.selectedLayer, pro.saveTarget),
                    ProHistoryEntry(updated, pro.reset, pro.selectedLayer, pro.saveTarget)))
        }
        return NativeWorkResult.Ok(from.copy(mode = NativeWorkMode.PRO, pro = activePro))
    }

    val initial = withContext(Dispatchers.IO) {
        val source = ProImageGeometry.sourceSize(resolver, regular.uri)
        val editedSize = ProImageGeometry.editedSize(source, regular.document)
        val input = ProImageGeometry.recordImport(ProLayer(regular.uri.toString(), name = "Original photo"), source)
        val placed = ProImageGeometry.bind(input.copy(edits = regular.document), source)
        val canvas = ProDocument(layers = listOf(placed), background = ProBackground.TRANSPARENT,
            sourceAspectRatio = editedSize.aspect, canvasSize = editedSize)
        val created = canvas.copy(sourceCanvasSetup = ProCanvasSetup.from(canvas))
        val reset = created.copy(layers = listOf(input))
        Triple(created, reset, input.nativePlacementId)
    }
    val ownerId = UUID.randomUUID().toString()
    val saveTarget = ProSaveTarget(nativeOwnerId = ownerId)
    val handoff = NativeProState(
        ownerId = ownerId,
        document = initial.first,
        documentEstablished = true,
        contentRevision = 0,
        sourceRevision = 0,
        selectedLayer = 0,
        saveTarget = saveTarget,
        nextWorkId = 1,
        entry = initial.second,
        reset = initial.second,
        baselines = mapOf(0L to ProWorkBaseline(initial.second)),
        history = ProHistory()
    )
    val link = (from.link ?: NativeWorkLink()).copy(ownedPlacementId = initial.third)
    return NativeWorkResult.Ok(from.copy(mode = NativeWorkMode.PRO, pro = handoff, link = link))
}

internal data class RegularProSwitchRequest(
    val source: NativeWorkState,
    val destination: NativeWorkMode,
    val displaced: NativeWorkState?,
    val regularOwner: String,
    val regularRevision: Long,
    val proOwner: String,
    val proRevision: Long
) {
    val hasWork: Boolean get() = if (source.mode == NativeWorkMode.REGULAR)
        source.regular != null else source.pro != null
}

internal fun NativeWorkMode.workspace(): WorkspaceMode =
    if (this == NativeWorkMode.PRO) WorkspaceMode.PRO else WorkspaceMode.PHOTO

internal fun nativeWorkMessage(reason: NativeWorkFailure): String = when (reason) {
    NativeWorkFailure.STALE -> "The work changed. Start the action again."
    NativeWorkFailure.UNAVAILABLE -> "A required source or saved revision is unavailable."
    NativeWorkFailure.INVALID -> "The working session could not be verified."
    NativeWorkFailure.LIMIT -> "This work needs more local storage or memory than the workspace allows."
    NativeWorkFailure.STORAGE -> "Could not record this work on the phone. Current work is still open."
}

internal data class PreparedNamedWorkspace(
    val ref: NamedProjectRef,
    val work: NativeWorkPrepared,
    val photo: PreparedRegularPhoto?
)

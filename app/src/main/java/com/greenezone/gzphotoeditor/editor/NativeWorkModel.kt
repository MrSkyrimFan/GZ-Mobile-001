package com.greenezone.gzphotoeditor.editor

import android.net.Uri

/** Immutable content pointer embedded in a named project record. */
data class NativeManifestRef(val sha: String)

/** The native recipe and exact bridge frame contributing one Regular finish. */
data class ProLinkedFinishRef(
    val sha: String,
    val input: ProPixelSize,
    val output: ProPixelSize,
    val proParentSha: String,
    val derivativeSha: String
)

internal enum class NativeWorkMode { REGULAR, PRO }

internal data class NativeWorkRef(val id: String, val revision: Long)

internal data class NativeRevisionRef(val mode: NativeWorkMode, val sha: String, val bytes: Long)

internal data class NativeDerivativeRef(
    val uri: Uri,
    val size: ProPixelSize,
    val sha: String,
    val parent: NativeRevisionRef,
    val bytes: Long,
    val hasAlpha: Boolean
)

internal data class NativeRegularState(
    val ownerId: String,
    val sourceId: String,
    val sourceRevision: Long,
    val contentRevision: Long,
    val uri: Uri,
    val document: EditDocument,
    val entry: EditDocument,
    val clean: EditDocument,
    val reset: EditDocument,
    val history: EditHistory,
    val savedRef: NamedProjectRef?
)

internal data class NativeProState(
    val ownerId: String,
    val document: ProDocument,
    val documentEstablished: Boolean,
    val contentRevision: Long,
    val sourceRevision: Long,
    val selectedLayer: Int,
    val saveTarget: ProSaveTarget,
    val nextWorkId: Long,
    val entry: ProDocument?,
    val reset: ProDocument?,
    val baselines: Map<Long, ProWorkBaseline>,
    val history: ProHistory
)

internal data class NativeWorkLink(
    val ownedPlacementId: String? = null,
    val regularOrigin: NativeRevisionRef? = null,
    val proOrigin: NativeRevisionRef? = null,
    val derivative: NativeDerivativeRef? = null,
    val regularInput: ProPixelSize? = null
)

internal data class NativeWorkState(
    val id: String,
    val mode: NativeWorkMode,
    val regular: NativeRegularState?,
    val pro: NativeProState?,
    val link: NativeWorkLink?
)

internal data class NativeWorkPrepared(
    val manifest: NativeManifestRef,
    val state: NativeWorkState
)

internal data class NativeWorkSummary(
    val ref: NativeWorkRef,
    val mode: NativeWorkMode,
    val recoverableModes: Set<NativeWorkMode>
)

internal data class NativeWorkCatalog(
    val revision: Long = 0,
    val active: NativeWorkSummary? = null,
    val drafts: List<NativeWorkSummary> = emptyList(),
    val retiredOwnerIds: Set<String> = emptySet(),
    val route: NativeWorkMode? = null
)

internal data class NativeWorkLoaded(val ref: NativeWorkRef, val state: NativeWorkState)

internal sealed interface NativeWorkResult<out T> {
    data class Ok<T>(val value: T) : NativeWorkResult<T>
    data class Failed(val reason: NativeWorkFailure) : NativeWorkResult<Nothing>
}

internal enum class NativeWorkFailure { STALE, UNAVAILABLE, INVALID, LIMIT, STORAGE }

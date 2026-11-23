package com.greenezone.gzphotoeditor.editor

import java.util.ArrayDeque
import java.util.concurrent.atomic.AtomicLong

/**
 * Bounded, process-local GIF diagnostics. Events intentionally contain no URI, path,
 * filename, title, media bytes, pixels, free-form message, disk write, or network behavior.
 */
internal enum class GifDiagnosticStage {
    G0_SOURCE_PICKED,
    G1_SOURCE_OPEN,
    G2_PLAY,
    G3_CONTINUOUS_PLAY,
    G4_SEEK,
    G5_EDIT,
    G6_EXPORT,
    G7_LIFECYCLE
}

internal enum class GifDiagnosticKind {
    OPERATION_STARTED,
    SOURCE_OPEN_STARTED,
    SOURCE_OPEN_SUCCEEDED,
    SOURCE_OPEN_FAILED,
    PREVIEW_ROUTE_SELECTED,
    FRAME_SETTLED,
    STALE_FRAME_DISCARDED,
    PLAYBACK_ENDED,
    FALLBACK_STARTED,
    PREVIEW_FAILED,
    READER_OPENED,
    READER_CLOSED,
    DOCUMENT_COMMITTED,
    DOCUMENT_SLID,
    SLIDE_FINISHED,
    EXPORT_STARTED,
    EXPORT_STAGED,
    EXPORT_COMPLETED,
    EXPORT_DESTINATION_CANCELLED,
    EXPORT_CANCELLED,
    EXPORT_FAILED,
    PREVIEW_CLOSED,
    ROUTE_CLOSED,
    SCREEN_CLOSED
}

internal data class GifDiagnosticEvent(
    val sequence: Long,
    val operationId: Long,
    val generation: Long,
    val stage: GifDiagnosticStage,
    val kind: GifDiagnosticKind,
    val route: GifPreviewRoute? = null,
    val frameIndex: Int? = null,
    val frameCount: Int? = null,
    val width: Int? = null,
    val height: Int? = null,
    val durationMs: Long? = null,
    val latencyNanos: Long? = null,
    val timestampNanos: Long,
    val failureClass: String? = null
)

internal class GifDiagnosticBuffer(
    private val capacity: Int,
    private val clockNanos: () -> Long = { System.nanoTime() }
) {
    private val sequence = AtomicLong()
    private val events = ArrayDeque<GifDiagnosticEvent>(capacity.coerceAtLeast(1))

    @Synchronized
    fun append(
        operationId: Long,
        generation: Long = 0,
        stage: GifDiagnosticStage,
        kind: GifDiagnosticKind,
        route: GifPreviewRoute? = null,
        frameIndex: Int? = null,
        frameCount: Int? = null,
        width: Int? = null,
        height: Int? = null,
        durationMs: Long? = null,
        latencyNanos: Long? = null,
        failureClass: String? = null
    ): GifDiagnosticEvent {
        val event = GifDiagnosticEvent(
            sequence = sequence.incrementAndGet(),
            operationId = operationId.coerceAtLeast(0),
            generation = generation.coerceAtLeast(0),
            stage = stage,
            kind = kind,
            route = route,
            frameIndex = frameIndex?.takeIf { it >= 0 },
            frameCount = frameCount?.takeIf { it >= 0 },
            width = width?.takeIf { it > 0 },
            height = height?.takeIf { it > 0 },
            durationMs = durationMs?.takeIf { it >= 0 },
            latencyNanos = latencyNanos?.takeIf { it >= 0 },
            timestampNanos = clockNanos(),
            failureClass = failureClass
                ?.substringAfterLast('.')
                ?.substringAfterLast('/')
                ?.substringAfterLast('\\')
                ?.filter { it.isLetterOrDigit() || it == '_' || it == '$' }
                ?.take(80)
                ?.takeIf { it.isNotBlank() }
        )
        while (events.size >= capacity.coerceAtLeast(1)) events.removeFirst()
        events.addLast(event)
        return event
    }

    @Synchronized
    fun snapshot(): List<GifDiagnosticEvent> = events.toList()
}

internal object GifPreviewDiagnostics {
    internal const val MAX_EVENTS = 1024

    private val operationCounter = AtomicLong()
    private val buffer = GifDiagnosticBuffer(MAX_EVENTS)

    fun nextOperationId(): Long = operationCounter.incrementAndGet()

    fun record(
        operationId: Long,
        generation: Long = 0,
        stage: GifDiagnosticStage,
        kind: GifDiagnosticKind,
        route: GifPreviewRoute? = null,
        frameIndex: Int? = null,
        frameCount: Int? = null,
        width: Int? = null,
        height: Int? = null,
        durationMs: Long? = null,
        latencyNanos: Long? = null,
        failure: Throwable? = null
    ): GifDiagnosticEvent = buffer.append(
        operationId = operationId,
        generation = generation,
        stage = stage,
        kind = kind,
        route = route,
        frameIndex = frameIndex,
        frameCount = frameCount,
        width = width,
        height = height,
        durationMs = durationMs,
        latencyNanos = latencyNanos,
        failureClass = failure?.javaClass?.simpleName
    )

    fun snapshot(): List<GifDiagnosticEvent> = buffer.snapshot()
}

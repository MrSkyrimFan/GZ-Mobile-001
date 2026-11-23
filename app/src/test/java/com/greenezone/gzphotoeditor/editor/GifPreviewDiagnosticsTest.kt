package com.greenezone.gzphotoeditor.editor

import kotlin.concurrent.thread
import org.junit.Assert.*
import org.junit.Test

class GifPreviewDiagnosticsTest {
    @Test fun operationIdsIncreaseMonotonically() {
        val first = GifPreviewDiagnostics.nextOperationId()
        val second = GifPreviewDiagnostics.nextOperationId()
        assertTrue(second > first)
    }

    @Test fun bufferIsBoundedOrderedAndSnapshotDoesNotOwnInternalStorage() {
        var now = 0L
        val buffer = GifDiagnosticBuffer(capacity = 8) { ++now }
        repeat(12) { frame ->
            buffer.append(
                operationId = 1,
                generation = 2,
                stage = GifDiagnosticStage.G4_SEEK,
                kind = GifDiagnosticKind.FRAME_SETTLED,
                route = GifPreviewRoute.BITMAP_RETRIEVER,
                frameIndex = frame,
                frameCount = 12,
                latencyNanos = frame.toLong()
            )
        }

        val snapshot = buffer.snapshot()
        assertEquals(8, snapshot.size)
        assertEquals(4, snapshot.first().frameIndex)
        assertEquals(11, snapshot.last().frameIndex)
        assertTrue(snapshot.zipWithNext().all { (left, right) ->
            left.sequence < right.sequence && left.timestampNanos < right.timestampNanos
        })
        (snapshot as? MutableList<GifDiagnosticEvent>)?.clear()
        assertEquals(8, buffer.snapshot().size)
    }

    @Test fun failureClassIsSanitizedAndInvalidOptionalMetadataIsDropped() {
        val buffer = GifDiagnosticBuffer(capacity = 4) { 44L }
        val event = buffer.append(
            operationId = -5,
            generation = -9,
            stage = GifDiagnosticStage.G1_SOURCE_OPEN,
            kind = GifDiagnosticKind.SOURCE_OPEN_FAILED,
            frameIndex = -1,
            frameCount = -1,
            width = 0,
            height = -2,
            durationMs = -3,
            latencyNanos = -4,
            failureClass = "C:\\private\\path/Illegal State!Exception"
        )

        assertEquals(0, event.operationId)
        assertEquals(0, event.generation)
        assertNull(event.frameIndex)
        assertNull(event.frameCount)
        assertNull(event.width)
        assertNull(event.height)
        assertNull(event.durationMs)
        assertNull(event.latencyNanos)
        assertEquals("IllegalStateException", event.failureClass)
        assertFalse(event.toString().contains("private"))
        assertFalse(event.toString().contains("path"))
    }

    @Test fun concurrentAppendsRetainUniqueMonotonicSequences() {
        val buffer = GifDiagnosticBuffer(capacity = 400)
        val workers = List(4) { worker ->
            thread(start = true) {
                repeat(100) { frame ->
                    buffer.append(
                        operationId = worker.toLong() + 1,
                        stage = GifDiagnosticStage.G3_CONTINUOUS_PLAY,
                        kind = GifDiagnosticKind.FRAME_SETTLED,
                        frameIndex = frame
                    )
                }
            }
        }
        workers.forEach { it.join() }

        val events = buffer.snapshot()
        assertEquals(400, events.size)
        assertEquals(400, events.map { it.sequence }.toSet().size)
        assertTrue(events.zipWithNext().all { (left, right) -> left.sequence < right.sequence })
    }
}

package com.greenezone.gzphotoeditor.editor

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class GifPausedFrameRequestsTest {
    @Test fun failedOlderReadResetsReaderAndDeliversNewestPausedFrame() = runBlocking {
        withTimeout(5_000) {
            val older = 3
            val intermediate = 8
            val newest = 15
            val olderDecodeStarted = CompletableDeferred<Unit>()
            val newerFramesQueued = CompletableDeferred<Unit>()
            val olderFailure = IOException("Older paused read failed")
            val decoded = mutableListOf<Int>()
            val failures = mutableListOf<Pair<Int, Exception>>()
            val presented = mutableListOf<Pair<Int, String>>()
            var readerResets = 0

            val requests = flow {
                emit(older)
                olderDecodeStarted.await()
                emit(intermediate)
                emit(newest)
                newerFramesQueued.complete(Unit)
            }
            requests.collectPausedGifFrameRequests(
                decode = { request ->
                    decoded.add(request)
                    if (request == older) {
                        olderDecodeStarted.complete(Unit)
                        newerFramesQueued.await()
                        throw olderFailure
                    }
                    assertEquals("Reset the failed reader before decoding a newer request", 1, readerResets)
                    "Frame $request"
                },
                closeReader = { readerResets++ },
                onFailure = { request, failure -> failures.add(request to failure) },
                onFrame = { request, frame -> presented.add(request to frame) }
            )

            assertEquals(listOf(older, newest), decoded)
            assertEquals(listOf(older to olderFailure), failures)
            assertEquals(listOf(newest to "Frame $newest"), presented)
            assertEquals(1, readerResets)
        }
    }

    @Test fun cancellationAndErrorsDoNotBecomePausedReadFailures() = runBlocking {
        val failures = listOf(CancellationException("Preview was superseded"), AssertionError("Decoder invariant"))
        for (expected in failures) {
            val escaped = runCatching {
                flowOf(3).collectPausedGifFrameRequests<Int, Int>(
                    decode = { throw expected },
                    closeReader = { fail("Unexpected reader reset") },
                    onFailure = { _, _ -> fail("Failure must escape the paused request collector") },
                    onFrame = { _, _ -> fail("No decoded frame can be presented") }
                )
            }.exceptionOrNull()
            assertSame(expected, escaped)
        }
    }
}

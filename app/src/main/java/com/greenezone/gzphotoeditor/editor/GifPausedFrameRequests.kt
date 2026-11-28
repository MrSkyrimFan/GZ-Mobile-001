package com.greenezone.gzphotoeditor.editor

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate

/** A failed paused read must leave the latest queued seek available to the collector. */
internal suspend fun <Request, Frame> Flow<Request>.collectPausedGifFrameRequests(
    decode: suspend (Request) -> Frame,
    closeReader: () -> Unit,
    onFailure: suspend (Request, Exception) -> Unit,
    onFrame: suspend (Request, Frame) -> Unit
) {
    conflate().collect { request ->
        val frame = try {
            decode(request)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            try {
                closeReader()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (closeError: Exception) {
                if (closeError !== error) error.addSuppressed(closeError)
            }
            onFailure(request, error)
            return@collect
        }
        onFrame(request, frame)
    }
}

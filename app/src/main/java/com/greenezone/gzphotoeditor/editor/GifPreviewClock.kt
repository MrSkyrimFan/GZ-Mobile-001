package com.greenezone.gzphotoeditor.editor

/** Absolute presentation boundaries shared by every repetition of a sampled preview. */
internal class GifPreviewClock(delaysMs: List<Int>) {
    private val starts = LongArray(delaysMs.size + 1)

    init {
        require(delaysMs.isNotEmpty())
        delaysMs.forEachIndexed { index, delay ->
            require(delay > 0)
            starts[index + 1] = starts[index] + delay
        }
    }

    private val duration get() = starts.last()
    fun startOf(index: Int): Long = starts[index]

    fun frameAt(timeMs: Long): Int {
        val found = starts.binarySearch(timeMs % duration)
        return if (found >= 0) found else -found - 2
    }

    fun nextBoundary(timeMs: Long): Long =
        timeMs - timeMs % duration + starts[frameAt(timeMs) + 1]

    fun endTime(loopCount: Int): Long =
        if (loopCount == 0) Long.MAX_VALUE else duration * (loopCount.coerceAtLeast(0) + 1L)
}

/** Untouched forward video can use a decoder surface without bypassing any visual edits. */
internal fun GifDocument.canPlayContinuousVideo(): Boolean {
    val clip = video ?: return false
    return !clip.isGif && !clip.hasCorrectedGeometry && !clip.reverse &&
        clip.startMs == 0L && clip.endMs == clip.durationMs &&
        clip.speedPercent == 100 && clip.fps == 0 && aspect == GifAspect.SOURCE &&
        size == GifSize.SOURCE && edits == EditDocument() && clip.sourceTimesMs.firstOrNull() == 0L
}

internal fun GifDocument.canPlaySourceDirectly(): Boolean {
    val clip = video ?: return false
    return canPlayContinuousVideo() && GifVideoMath.frameCount(clip) == clip.sourceTimesMs.size
}

internal enum class GifPreviewRoute {
    NONE,
    NATIVE_SOURCE,
    SCHEDULED_CODEC,
    BITMAP_RETRIEVER,
    BITMAP_CODEC,
    GIF_MOVIE
}

internal fun GifDocument.previewRoute(
    playing: Boolean,
    nativeFailed: Boolean
): GifPreviewRoute {
    val clip = video ?: return GifPreviewRoute.NONE
    if (playing && !nativeFailed && canPlayContinuousVideo()) {
        return if (canPlaySourceDirectly()) GifPreviewRoute.NATIVE_SOURCE
        else GifPreviewRoute.SCHEDULED_CODEC
    }
    if (clip.isGif) return GifPreviewRoute.GIF_MOVIE
    if (playing && clip.sourceTimesMs.isNotEmpty() && !canPlayContinuousVideo()) {
        return GifPreviewRoute.BITMAP_CODEC
    }
    return GifPreviewRoute.BITMAP_RETRIEVER
}

/** Keep an already-failed edited-frame codec out of subsequent Play sessions for this import. */
internal fun GifPreviewRoute.afterBitmapCodecFailure(failed: Boolean): GifPreviewRoute =
    if (failed && this == GifPreviewRoute.BITMAP_CODEC) GifPreviewRoute.BITMAP_RETRIEVER else this

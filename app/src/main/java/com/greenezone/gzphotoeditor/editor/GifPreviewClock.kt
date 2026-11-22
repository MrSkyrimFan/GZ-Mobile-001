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

/** Use continuous decoding only when the source itself represents the requested output. */
internal fun GifDocument.canPlaySourceDirectly(): Boolean {
    val clip = video ?: return false
    return !clip.isGif && !clip.reverse && clip.startMs == 0L && clip.endMs == clip.durationMs &&
        clip.speedPercent == 100 && clip.fps == 0 && aspect == GifAspect.SOURCE &&
        size == GifSize.SOURCE && edits == EditDocument() && clip.sourceTimesMs.firstOrNull() == 0L &&
        GifVideoMath.frameCount(clip) == clip.sourceTimesMs.size
}

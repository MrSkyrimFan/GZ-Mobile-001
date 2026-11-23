package com.greenezone.gzphotoeditor.editor

import org.junit.Assert.*
import org.junit.Test

class GifPreviewClockTest {
    @Test fun lateDecoderCatchesUpAcrossUnequalHoldsAndLoopBoundaries() {
        val clock = GifPreviewClock(listOf(30, 120, 50))
        assertEquals(30L, clock.startOf(1))
        assertEquals(1, clock.frameAt(149))
        assertEquals(2, clock.frameAt(150))
        assertEquals(0, clock.frameAt(205))
        assertEquals(1, clock.frameAt(265))
        assertEquals(350L, clock.nextBoundary(265))
        assertEquals(200L, clock.endTime(-1))
        assertEquals(600L, clock.endTime(2))
        assertEquals(Long.MAX_VALUE, clock.endTime(0))
    }

    @Test fun routeSelectionMatchesPlayingStateEditsAndFallback() {
        val clip = GifVideoClip("content://video/route", 120,
            sourceTimesMs = listOf(0L, 40L, 80L))
        val neutral = GifDocument(video = clip)
        assertEquals(GifPreviewRoute.BITMAP_RETRIEVER,
            neutral.previewRoute(playing = false, nativeFailed = false))
        assertEquals(GifPreviewRoute.NATIVE_SOURCE,
            neutral.previewRoute(playing = true, nativeFailed = false))
        assertEquals(GifPreviewRoute.BITMAP_RETRIEVER,
            neutral.previewRoute(playing = true, nativeFailed = true))
        val sampled = neutral.copy(video = clip.copy(
            sourceTimesMs = (0L until 120L step 10L).toList()))
        assertEquals(GifPreviewRoute.SCHEDULED_CODEC,
            sampled.previewRoute(playing = true, nativeFailed = false))
        assertEquals(GifPreviewRoute.BITMAP_RETRIEVER,
            neutral.copy(video = clip.copy(reverse = true))
                .previewRoute(playing = true, nativeFailed = false))
        assertEquals(GifPreviewRoute.BITMAP_RETRIEVER,
            neutral.copy(video = clip.copy(startMs = 40L))
                .previewRoute(playing = true, nativeFailed = false))
        assertEquals(GifPreviewRoute.BITMAP_RETRIEVER,
            neutral.copy(video = clip.copy(speedPercent = 200))
                .previewRoute(playing = true, nativeFailed = false))
        assertEquals(GifPreviewRoute.BITMAP_RETRIEVER,
            neutral.copy(edits = neutral.edits.copy(quarterTurns = 1))
                .previewRoute(playing = true, nativeFailed = false))
        assertEquals(GifPreviewRoute.BITMAP_RETRIEVER,
            neutral.copy(aspect = GifAspect.SQUARE)
                .previewRoute(playing = true, nativeFailed = false))
        val importedGif = neutral.copy(video = clip.copy(isGif = true))
        assertEquals(GifPreviewRoute.GIF_MOVIE,
            importedGif.previewRoute(playing = false, nativeFailed = false))
        assertEquals(GifPreviewRoute.GIF_MOVIE,
            importedGif.previewRoute(playing = true, nativeFailed = false))
        assertEquals(GifPreviewRoute.NONE,
            GifDocument().previewRoute(playing = true, nativeFailed = false))
    }

    @Test fun nativePlaybackRequiresUnmodifiedFrameOrderTimingAndAppearance() {
        val clip = GifVideoClip("content://video/1", 120, sourceTimesMs = listOf(0L, 40L, 80L))
        val doc = GifDocument(video = clip)
        assertTrue(doc.canPlaySourceDirectly())
        assertFalse(doc.copy(video = clip.copy(isGif = true)).canPlaySourceDirectly())
        assertFalse(doc.copy(video = clip.copy(reverse = true)).canPlaySourceDirectly())
        assertFalse(doc.copy(video = clip.copy(startMs = 40L)).canPlaySourceDirectly())
        assertFalse(doc.copy(video = clip.copy(speedPercent = 200)).canPlaySourceDirectly())
        assertFalse(doc.copy(video = clip.copy(fps = 8)).canPlaySourceDirectly())
        val highFps = doc.copy(video = clip.copy(sourceTimesMs = (0L until 120L step 10L).toList()))
        assertFalse(highFps.canPlaySourceDirectly())
        assertTrue(highFps.canPlayContinuousVideo())
        assertFalse(highFps.copy(video = highFps.video!!.copy(reverse = true)).canPlayContinuousVideo())
        assertFalse(doc.copy(edits = doc.edits.copy(quarterTurns = 1)).canPlaySourceDirectly())
        assertFalse(doc.copy(aspect = GifAspect.SQUARE).canPlaySourceDirectly())
        assertFalse(doc.copy(size = GifSize.SMALL).canPlaySourceDirectly())
    }
}

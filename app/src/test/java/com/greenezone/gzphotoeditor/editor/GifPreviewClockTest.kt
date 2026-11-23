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
        assertEquals(GifPreviewRoute.BITMAP_CODEC,
            neutral.copy(video = clip.copy(reverse = true))
                .previewRoute(playing = true, nativeFailed = false))
        assertEquals(GifPreviewRoute.BITMAP_CODEC,
            neutral.copy(video = clip.copy(startMs = 40L))
                .previewRoute(playing = true, nativeFailed = false))
        assertEquals(GifPreviewRoute.BITMAP_CODEC,
            neutral.copy(video = clip.copy(speedPercent = 200))
                .previewRoute(playing = true, nativeFailed = false))
        assertEquals(GifPreviewRoute.BITMAP_CODEC,
            neutral.copy(edits = neutral.edits.copy(quarterTurns = 1))
                .previewRoute(playing = true, nativeFailed = false))
        assertEquals(GifPreviewRoute.BITMAP_CODEC,
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

    @Test fun editedCodecFailureKeepsNextPreviewOnRetrieverWithoutChangingSurfaceRoutes() {
        assertEquals(GifPreviewRoute.BITMAP_CODEC,
            GifPreviewRoute.BITMAP_CODEC.afterBitmapCodecFailure(false))
        assertEquals(GifPreviewRoute.BITMAP_RETRIEVER,
            GifPreviewRoute.BITMAP_CODEC.afterBitmapCodecFailure(true))
        assertEquals(GifPreviewRoute.NATIVE_SOURCE,
            GifPreviewRoute.NATIVE_SOURCE.afterBitmapCodecFailure(true))
        assertEquals(GifPreviewRoute.SCHEDULED_CODEC,
            GifPreviewRoute.SCHEDULED_CODEC.afterBitmapCodecFailure(true))
    }

    @Test fun unspecifiedDecoderColorMetadataFallsBackToTheSource() {
        assertEquals(1, gifColorMetadata(0, 1))
        assertEquals(2, gifColorMetadata(null, 2))
        assertEquals(4, gifColorMetadata(4, 1))
        assertNull(gifColorMetadata(0, 0))
        assertNull(gifColorMetadata(null, null))
    }

    @Test fun longFixedFpsTrimsKeepTheOppositeThumbAndBuildAValidSchedule() {
        val source = GifVideoClip("content://video/long", 6_000_000L,
            sourceTimesMs = List(144_000) { it * 1_000L / 24 })
        assertTrue(GifVideoMath.valid(source))
        assertFalse(GifVideoMath.fitsFrameBudget(source, 30))

        val right = GifVideoMath.trimWithinFrameBudget(
            source.copy(startMs = 500_000L, endMs = 1_000_000L, fps = 30),
            source.durationMs, movingEnd = true)
        assertEquals(500_000L, right.startMs)
        assertEquals(5_500_000L, right.endMs)
        assertEquals(30, right.fps)
        assertTrue(GifVideoMath.valid(right))
        assertEquals(GifVideoMath.MAX_OUTPUT_FRAMES, GifVideoMath.frameCount(right))

        val left = GifVideoMath.trimWithinFrameBudget(
            source.copy(startMs = 5_000_000L, fps = 30, reverse = true),
            0L, movingEnd = false)
        assertEquals(1_000_000L, left.startMs)
        assertEquals(source.durationMs, left.endMs)
        assertEquals(30, left.fps)
        assertTrue(left.reverse)
        assertTrue(GifVideoMath.valid(left))
        assertEquals(GifVideoMath.MAX_OUTPUT_FRAMES, GifVideoMath.frameCount(left))
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

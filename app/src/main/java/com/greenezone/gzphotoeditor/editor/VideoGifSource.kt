package com.greenezone.gzphotoeditor.editor

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.IOException
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** A bounded, user-selected section of a local video, without changing the original. */
data class GifVideoClip(
    val uri: String,
    val durationMs: Long,
    val startMs: Long = 0,
    val endMs: Long = min(durationMs, 5_000L),
    val fps: Int = 8,
    val speedPercent: Int = 100,
    val reverse: Boolean = false
)

object GifVideoMath {
    const val MAX_CLIP_MS = 12_000L
    const val MAX_OUTPUT_FRAMES = 120
    val FRAME_RATES = listOf(4, 6, 8, 10)
    val SPEEDS = listOf(50, 100, 200)

    fun valid(clip: GifVideoClip): Boolean =
        clip.uri.startsWith("content://") && clip.uri.length <= 4096 &&
        clip.durationMs in 500L..86_400_000L &&
        clip.startMs >= 0 && clip.startMs < clip.endMs &&
        clip.endMs <= clip.durationMs &&
        clip.endMs - clip.startMs in 500L..MAX_CLIP_MS &&
        clip.fps in FRAME_RATES && clip.speedPercent in SPEEDS &&
        frameCountUnvalidated(clip) in 2..MAX_OUTPUT_FRAMES

    private fun frameCountUnvalidated(clip: GifVideoClip): Int =
        ((clip.endMs - clip.startMs).toDouble() * clip.fps / 1000.0)
            .roundToInt().coerceAtLeast(2)

    fun frameCount(clip: GifVideoClip): Int {
        require(valid(clip))
        return frameCountUnvalidated(clip)
    }

    /** Every sample timestamp falls strictly inside the selected trim window. */
    fun frameTimesMs(clip: GifVideoClip): List<Long> {
        require(valid(clip))
        val count = frameCount(clip)
        val span = clip.endMs - clip.startMs
        val times = (0 until count).map { i ->
            clip.startMs + (span * i / count)
        }
        return if (clip.reverse) times.reversed() else times
    }

    fun gifDelayMs(clip: GifVideoClip): Int {
        require(valid(clip))
        val exact = 100_000f / (clip.fps * clip.speedPercent)
        return (exact / 10f).roundToInt().times(10).coerceIn(20, 1500)
    }
    fun defaultClip(uri: String, durationMs: Long): GifVideoClip =
        GifVideoClip(uri = uri, durationMs = durationMs,
            endMs = durationMs.coerceAtMost(5_000L))
}

/** Android native decoder: one resized video frame at a time; no extra library or audio reads. */
object VideoGifSource {
    fun inspect(context: Context, uri: Uri): GifVideoClip {
        require(uri.scheme == "content")
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: throw IOException("Cannot determine video duration")
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toIntOrNull() ?: 0
            if (width <= 0 || height <= 0) throw IOException("This file has no decoded video track")
            val clip = GifVideoMath.defaultClip(uri.toString(), duration)
            require(GifVideoMath.valid(clip)) { "Video must be at least half a second long" }
            return clip
        } finally { retriever.release() }
    }

    fun preview(context: Context, clip: GifVideoClip, atMs: Long, edge: Int): Bitmap {
        require(GifVideoMath.valid(clip))
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, Uri.parse(clip.uri))
            return decode(retriever, atMs.coerceIn(clip.startMs, clip.endMs - 1), edge)
        } finally { retriever.release() }
    }

    private fun decode(reader: MediaMetadataRetriever, timestampMs: Long, edge: Int): Bitmap {
        require(edge in 160..1280)
        return reader.getScaledFrameAtTime(
            timestampMs * 1000L,
            MediaMetadataRetriever.OPTION_CLOSEST,
            edge, edge
        ) ?: throw IOException("Video decoder could not read frame at " + timestampMs + " ms")
    }

    /** The retriever remains open for export; frames are never cached in a huge list. */
    fun write(
        context: Context, doc: GifDocument, out: OutputStream,
        cancel: (() -> Unit)? = null,
        onProgress: ((Int, Int) -> Unit)? = null
    ) {
        val clip = doc.video ?: throw IllegalArgumentException("Select a video first")
        require(GifMath.valid(doc))
        val times = GifVideoMath.frameTimesMs(clip)
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, Uri.parse(clip.uri))
            GifEncoder.writeFrames(out, doc, times.size, GifVideoMath.gifDelayMs(clip), cancel) { i ->
                cancel?.invoke()
                val frame = decode(retriever, times[i], min(1280, doc.size.maxEdge * 2))
                onProgress?.invoke(i + 1, times.size)
                frame
            }
        } finally { retriever.release() }
    }
}

package com.greenezone.gzphotoeditor.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Movie
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.Closeable
import java.io.IOException
import java.io.OutputStream
import kotlin.math.roundToInt

data class GifVideoClip(
    val uri: String,
    val durationMs: Long,
    val startMs: Long = 0,
    val endMs: Long = durationMs,
    val fps: Int = 0,
    val speedPercent: Int = 100,
    val reverse: Boolean = false,
    val width: Int = 480,
    val height: Int = 480,
    val sourceTimesMs: List<Long> = emptyList(),
    val isGif: Boolean = false,
    val loopCount: Int = -1
) {
    internal val schedule by lazy { GifVideoMath.schedule(this) }
}

/** Timestamps and delays are kept together so reversing variable-rate footage also
 * reverses its holds. Round cumulative time once per boundary to avoid playback drift. */
internal data class GifSchedule(val times: List<Long>, val delays: List<Int>)

object GifVideoMath {
    const val MAX_CLIP_MS = 86_400_000L
    const val MAX_OUTPUT_FRAMES = 150_000
    val FRAME_RATES = listOf(0, 4, 6, 8, 10, 12, 15, 20, 24, 30)
    val SPEEDS = listOf(50, 100, 200)

    fun valid(clip: GifVideoClip): Boolean =
        clip.uri.startsWith("content://") && clip.uri.length <= 4096 &&
        clip.durationMs in 1..MAX_CLIP_MS && clip.startMs in 0 until clip.endMs &&
        clip.endMs <= clip.durationMs && clip.fps in FRAME_RATES && clip.speedPercent in SPEEDS &&
        clip.width in 1..65535 && clip.height in 1..65535 &&
        clip.sourceTimesMs.size <= MAX_OUTPUT_FRAMES && clip.loopCount in -1..65535

    internal fun schedule(clip: GifVideoClip): GifSchedule {
        require(valid(clip))
        val span = clip.endMs - clip.startMs
        val source = clip.sourceTimesMs
        val times = ArrayList<Long>()
        val delays = ArrayList<Int>()
        var tick = 0L
        var current = clip.startMs
        fun accept(next: Long, last: Boolean = false) {
            if (next <= current) return
            val boundary = Math.round((next - clip.startMs) * 10.0 / clip.speedPercent)
            // Widely used GIF players reinterpret sub-20ms delays as 100ms.
            // Sample high-rate motion within that limit, retaining the complete duration.
            val delay = boundary - tick
            if (!last && delay < 2) return
            if (last && delay < 2 && delays.isNotEmpty()) {
                delays[delays.lastIndex] += (delay * 10).toInt()
            } else {
                times.add(current)
                delays.add((delay.coerceAtLeast(2) * 10).toInt())
            }
            tick = boundary
            current = next
        }
        if (clip.fps == 0 && source.isNotEmpty()) {
            var i = source.binarySearch(clip.startMs).let { if (it >= 0) it + 1 else -it - 1 }
            while (i < source.size && source[i] < clip.endMs) accept(source[i++])
        } else {
            val fps = if (clip.fps == 0) 24 else clip.fps
            val count = (span * fps / 1000.0).roundToInt().coerceIn(1, MAX_OUTPUT_FRAMES)
            for (i in 1 until count) accept(clip.startMs + span * i / count)
        }
        accept(clip.endMs, last = true)
        return if (clip.reverse) GifSchedule(times.asReversed(), delays.asReversed())
            else GifSchedule(times, delays)
    }

    fun frameCount(clip: GifVideoClip) = clip.schedule.times.size
    fun frameTimesMs(clip: GifVideoClip): List<Long> = clip.schedule.times
    fun frameDelaysMs(clip: GifVideoClip): List<Int> = clip.schedule.delays
    fun frameDelayMs(clip: GifVideoClip, index: Int): Int = clip.schedule.delays[index]
    fun gifDelayMs(clip: GifVideoClip): Int = frameDelayMs(clip, 0)
    fun defaultClip(uri: String, durationMs: Long) = GifVideoClip(uri, durationMs)
}

interface GifFrameReader : Closeable {
    fun frame(atMs: Long, edge: Int): Bitmap
}

object VideoGifSource {
    private fun isGif(context: Context, uri: Uri): Boolean =
        context.contentResolver.openInputStream(uri)?.buffered()?.use {
            val header = ByteArray(6)
            java.io.DataInputStream(it).readFully(header)
            String(header, Charsets.US_ASCII) in setOf("GIF87a", "GIF89a")
        } ?: throw IOException("Cannot open source")

    fun document(context: Context, uri: Uri): GifDocument {
        val clip = inspect(context, uri)
        return GifDocument(video = clip, forever = clip.loopCount == 0,
            repeatCount = clip.loopCount.takeIf { it > 0 })
    }

    fun inspect(context: Context, uri: Uri): GifVideoClip {
        require(uri.scheme == "content")
        if (isGif(context, uri)) {
            return context.contentResolver.openInputStream(uri)!!.buffered().use {
                GifSourceMetadata.read(it, uri.toString())
            }
        }
        val reader = MediaMetadataRetriever()
        try {
            reader.setDataSource(context, uri)
            fun meta(key: Int) = reader.extractMetadata(key)?.toLongOrNull() ?: 0L
            val duration = meta(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val w = meta(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt()
            val h = meta(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt()
            require(w > 0 && h > 0 && duration > 0) { "This file has no readable video track" }
            val sideways = meta(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION) % 180 != 0L
            val times = sampleTimes(context, uri, duration)
            val clip = GifVideoClip(uri.toString(), duration, width = if (sideways) h else w,
                height = if (sideways) w else h, sourceTimesMs = times)
            require(GifVideoMath.valid(clip)) { "Video dimensions or duration are unsupported" }
            return clip
        } finally { reader.release() }
    }

    private fun sampleTimes(context: Context, uri: Uri, duration: Long): List<Long> {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(context, uri, null)
            val track = (0 until extractor.trackCount).firstOrNull { index ->
                extractor.getTrackFormat(index).getString(MediaFormat.KEY_MIME)?.startsWith("video/") == true
            } ?: throw IOException("No video track")
            extractor.selectTrack(track)
            val times = ArrayList<Long>()
            while (extractor.sampleTime >= 0L) {
                val time = (extractor.sampleTime + 500L) / 1000L
                if (time < duration) times.add(time)
                require(times.size <= GifVideoMath.MAX_OUTPUT_FRAMES) { "Source exceeds 150,000 frames" }
                if (!extractor.advance()) break
            }
            require(times.isNotEmpty()) { "The video has no readable frames" }
            // Encoded packet order may differ from presentation order with B frames.
            times.sort()
            return times.distinct()
        } finally { extractor.release() }
    }

    @Suppress("DEPRECATION")
    fun open(context: Context, clip: GifVideoClip, batchForExport: Boolean = false): GifFrameReader {
        require(GifVideoMath.valid(clip))
        val uri = Uri.parse(clip.uri)
        if (clip.isGif) {
            val movie = context.contentResolver.openInputStream(uri)?.buffered()?.use { Movie.decodeStream(GifSourceMetadata.indexedStream(it)) }
                ?: throw IOException("Android cannot decode this GIF")
            return object : GifFrameReader {
                override fun frame(atMs: Long, edge: Int): Bitmap {
                    val scale = minOf(1f, edge.toFloat() / maxOf(clip.width, clip.height))
                    val bmp = Bitmap.createBitmap((clip.width * scale).roundToInt().coerceAtLeast(1),
                        (clip.height * scale).roundToInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bmp)
                    canvas.scale(bmp.width.toFloat() / clip.width, bmp.height.toFloat() / clip.height)
                    val t = atMs.coerceIn(0, clip.durationMs - 1)
                    val boundary = clip.sourceTimesMs.binarySearch(t)
                    val index = if (boundary >= 0) boundary else (-boundary - 2).coerceAtLeast(0)
                    movie.setTime(GifSourceMetadata.decoderTime(index))
                    movie.draw(canvas, 0f, 0f)
                    return bmp
                }
                override fun close() = Unit
            }
        }
        val reader = MediaMetadataRetriever()
        try { reader.setDataSource(context, uri) } catch (e: Throwable) { reader.release(); throw e }
        return object : GifFrameReader {
            // A timestamp seek can decode an entire GOP. Read consecutive original-size
            // frames together, also for reverse output, within a fixed bitmap budget.
            private val cache = mutableMapOf<Int, Bitmap>()
            private val params = MediaMetadataRetriever.BitmapParams().apply {
                preferredConfig = Bitmap.Config.ARGB_8888
            }
            private val batch = (24L * 1024 * 1024 / (4L * clip.width * clip.height))
                .coerceIn(1, 12).toInt()
            private var batching = batchForExport && batch > 1 && clip.fps == 0 &&
                clip.sourceTimesMs.isNotEmpty() &&
                reader.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)?.toIntOrNull() ==
                    clip.sourceTimesMs.size

            private fun readBatch(index: Int): Bitmap? {
                cache.remove(index)?.let { return it }
                cache.values.forEach { it.recycle() }
                cache.clear()
                val start = if (clip.reverse) (index - batch + 1).coerceAtLeast(0) else index
                val count = minOf(batch, clip.sourceTimesMs.size - start)
                val frames = try {
                    reader.getFramesAtIndex(start, count, params)
                } catch (_: RuntimeException) { emptyList() }
                if (frames.size != count || frames.any { it.width != clip.width || it.height != clip.height }) {
                    frames.forEach { it.recycle() }
                    batching = false
                    return null
                }
                frames.forEachIndexed { offset, bitmap -> cache[start + offset] = bitmap }
                return cache.remove(index)
            }

            override fun frame(atMs: Long, edge: Int): Bitmap {
                val t = atMs.coerceIn(0L, clip.durationMs - 1)
                val times = clip.sourceTimesMs
                val index = if (times.isEmpty()) -1 else times.binarySearch(t)
                val floor = if (index >= 0) index else (-index - 2).coerceAtLeast(0)
                if (batching && edge >= maxOf(clip.width, clip.height)) readBatch(floor)?.let { return it }
                val sample = if (times.isEmpty()) t else times[floor]
                val scale = minOf(1.0, edge.toDouble() / maxOf(clip.width, clip.height))
                val w = (clip.width * scale).roundToInt().coerceAtLeast(1)
                val h = (clip.height * scale).roundToInt().coerceAtLeast(1)
                val bitmap = if (android.os.Build.VERSION.SDK_INT >= 30)
                    reader.getScaledFrameAtTime(sample * 1000, MediaMetadataRetriever.OPTION_CLOSEST, w, h, params)
                    else reader.getScaledFrameAtTime(sample * 1000, MediaMetadataRetriever.OPTION_CLOSEST, w, h)
                return bitmap ?: throw IOException("Cannot decode video frame at ${sample}ms")
            }
            override fun close() {
                cache.values.forEach { it.recycle() }
                cache.clear()
                reader.release()
            }
        }
    }

    fun preview(context: Context, clip: GifVideoClip, atMs: Long, edge: Int): Bitmap =
        open(context, clip).use { it.frame(atMs, edge) }

    fun write(
        context: Context, doc: GifDocument, out: OutputStream,
        cancel: (() -> Unit)? = null,
        onProgress: ((Int, Int) -> Unit)? = null
    ) {
        val clip = requireNotNull(doc.video)
        require(GifMath.valid(doc))
        if (clip.isGif && clip.startMs == 0L && clip.endMs == clip.durationMs &&
            !clip.reverse && clip.speedPercent == 100 && clip.fps == 0 &&
            doc.edits == EditDocument() && doc.size == GifSize.SOURCE && doc.aspect == GifAspect.SOURCE &&
            doc.loopCount == clip.loopCount) {
            context.contentResolver.openInputStream(Uri.parse(clip.uri))?.use { input ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    cancel?.invoke()
                    val n = input.read(buffer)
                    if (n < 0) break
                    out.write(buffer, 0, n)
                }
            } ?: throw IOException("Cannot open original GIF")
            out.flush()
            val count = GifVideoMath.frameCount(clip)
            onProgress?.invoke(count, count)
            return
        }
        val times = GifVideoMath.frameTimesMs(clip)
        val delays = GifVideoMath.frameDelaysMs(clip)
        val (w, h) = GifMath.dimensions(doc)
        open(context, clip, batchForExport = true).use { reader ->
            GifEncoder.writeFrames(out, doc, times.size, delays.first(), cancel,
                delayAt = { delays[it] }) { index ->
                val frame = reader.frame(times[index], maxOf(w,h))
                onProgress?.invoke(index + 1, times.size)
                frame
            }
        }
    }
}

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

    /** Avoid secretly reducing requested FPS on long footage. */
    fun fitsFrameBudget(clip: GifVideoClip, fps: Int = clip.fps): Boolean {
        if (fps !in FRAME_RATES || clip.endMs <= clip.startMs) return false
        if (fps == 0 && clip.sourceTimesMs.isNotEmpty()) return true
        val effective = if (fps == 0) 24 else fps
        return (clip.endMs - clip.startMs).toDouble() * effective / 1000.0 <= MAX_OUTPUT_FRAMES
    }

    fun valid(clip: GifVideoClip): Boolean =
        clip.uri.startsWith("content://") && clip.uri.length <= 4096 &&
        clip.durationMs in 1..MAX_CLIP_MS && clip.startMs in 0 until clip.endMs &&
        clip.endMs <= clip.durationMs && clip.fps in FRAME_RATES && clip.speedPercent in SPEEDS &&
        clip.width in 1..65535 && clip.height in 1..65535 &&
        clip.sourceTimesMs.size <= MAX_OUTPUT_FRAMES && clip.loopCount in -1..65535 &&
        fitsFrameBudget(clip)

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
            val count = (span * fps / 1000.0).roundToInt().coerceAtLeast(1)
            require(count <= MAX_OUTPUT_FRAMES) {
                "Clip is too long for this FPS. Trim the clip or lower FPS."
            }
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
            val containerDuration = meta(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val w = meta(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt()
            val h = meta(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt()
            require(w > 0 && h > 0) { "This file has no readable video track" }
            val sideways = meta(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION) % 180 != 0L
            val (duration, times) = withVideoTrack(context, uri) { extractor, format ->
                val duration = durationMs(format) ?: containerDuration
                require(duration > 0) { "This video has no readable duration" }
                duration to sampleTimes(extractor, duration)
            }
            val clip = GifVideoClip(uri.toString(), duration, width = if (sideways) h else w,
                height = if (sideways) w else h, sourceTimesMs = times)
            require(GifVideoMath.valid(clip)) { "Video dimensions or duration are unsupported" }
            return clip
        } finally { reader.release() }
    }

    // MediaMetadataRetriever reports the longest track in the container, including audio.
    private fun durationMs(format: MediaFormat): Long? {
        if (!format.containsKey(MediaFormat.KEY_DURATION)) return null
        return ((format.getLong(MediaFormat.KEY_DURATION) + 500L) / 1000L).takeIf { it > 0L }
    }

    internal fun visualDurationMs(context: Context, uri: Uri): Long? =
        withVideoTrack(context, uri) { _, format -> durationMs(format) }

    private inline fun <T> withVideoTrack(context: Context, uri: Uri,
        read: (MediaExtractor, MediaFormat) -> T): T {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(context, uri, null)
            val track = (0 until extractor.trackCount).firstOrNull { index ->
                extractor.getTrackFormat(index).getString(MediaFormat.KEY_MIME)?.startsWith("video/") == true
            } ?: throw IOException("No video track")
            extractor.selectTrack(track)
            return read(extractor, extractor.getTrackFormat(track))
        } finally { extractor.release() }
    }

    private fun sampleTimes(extractor: MediaExtractor, duration: Long): List<Long> {
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
    }

    @Suppress("DEPRECATION")
    fun open(context: Context, clip: GifVideoClip, batchFrames: Boolean = false,
             allowScaledBatches: Boolean = false): GifFrameReader {
        require(GifVideoMath.valid(clip))
        val uri = Uri.parse(clip.uri)
        if (clip.isGif) {
            val movie = context.contentResolver.openInputStream(uri)?.buffered()?.use { Movie.decodeStream(GifSourceMetadata.indexedStream(it)) }
                ?: throw IOException("Android cannot decode this GIF")
            return object : GifFrameReader {
                private val pending = mutableMapOf<Int, Bitmap>()
                private var previousEdge = 0

                private fun draw(index: Int, width: Int, height: Int): Bitmap {
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.scale(width.toFloat() / clip.width, height.toFloat() / clip.height)
                    movie.setTime(GifSourceMetadata.decoderTime(index))
                    movie.draw(canvas, 0f, 0f)
                    return bitmap
                }

                override fun frame(atMs: Long, edge: Int): Bitmap {
                    val t = atMs.coerceIn(0, clip.durationMs - 1)
                    val boundary = clip.sourceTimesMs.binarySearch(t)
                    val index = if (boundary >= 0) boundary else (-boundary - 2).coerceAtLeast(0)
                    if (previousEdge == edge) pending.remove(index)?.let { return it }
                    clearPending()
                    previousEdge = edge
                    val scale = minOf(1f, edge.toFloat() / maxOf(clip.width, clip.height))
                    val width = (clip.width * scale).roundToInt().coerceAtLeast(1)
                    val height = (clip.height * scale).roundToInt().coerceAtLeast(1)
                    if (batchFrames && clip.reverse && clip.fps == 0) {
                        // GIF disposal is sequential. Composite a small window forwards once,
                        // then hand its pixels to the caller in reverse display order.
                        val window = (4L * 1024 * 1024 / (4L * width * height)).coerceIn(1, 8).toInt()
                        for (sample in (index - window + 1).coerceAtLeast(0) until index) {
                            pending[sample] = draw(sample, width, height)
                        }
                    }
                    return draw(index, width, height)
                }

                private fun clearPending() {
                    pending.values.forEach { it.recycle() }
                    pending.clear()
                }
                override fun close() = clearPending()
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
            private val budget = 24L * 1024 * 1024
            private val originalBytes = 4L * clip.width * clip.height
            private var cachedWidth = 0
            private var cachedHeight = 0
            private var batching = batchFrames && clip.fps == 0 && clip.sourceTimesMs.isNotEmpty() &&
                reader.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)?.toIntOrNull() ==
                    clip.sourceTimesMs.size

            private fun readAtTime(sample: Long, width: Int, height: Int): Bitmap {
                val bitmap = if (android.os.Build.VERSION.SDK_INT >= 30)
                    reader.getScaledFrameAtTime(sample * 1000, MediaMetadataRetriever.OPTION_CLOSEST, width, height, params)
                    else reader.getScaledFrameAtTime(sample * 1000, MediaMetadataRetriever.OPTION_CLOSEST, width, height)
                return bitmap ?: throw IOException("Cannot decode video frame at ${sample}ms")
            }

            private fun clearCache() {
                cache.values.forEach { it.recycle() }
                cache.clear()
            }

            private fun readBatch(index: Int, width: Int, height: Int, countLimit: Int): Bitmap? {
                if (width == cachedWidth && height == cachedHeight) cache.remove(index)?.let { return it }
                clearCache()
                cachedWidth = width
                cachedHeight = height
                val start = if (clip.reverse) (index - countLimit + 1).coerceAtLeast(0) else index
                val count = minOf(countLimit, clip.sourceTimesMs.size - start)
                val frames = try {
                    reader.getFramesAtIndex(start, count, params)
                } catch (_: RuntimeException) { emptyList() }
                val scaled = width != clip.width || height != clip.height
                val reserve = if (scaled) 4L * width * height else 0L
                if (frames.size != count || frames.any { it.width != clip.width || it.height != clip.height } ||
                    frames.sumOf { it.allocationByteCount.toLong() } + reserve > budget) {
                    frames.forEach { it.recycle() }
                    batching = false
                    return null
                }
                try {
                    frames.forEachIndexed { offset, bitmap ->
                        cache[start + offset] = if (scaled) {
                            Bitmap.createScaledBitmap(bitmap, width, height, true).also { bitmap.recycle() }
                        } else bitmap
                    }
                } catch (failed: Throwable) {
                    clearCache()
                    frames.forEach { if (!it.isRecycled) it.recycle() }
                    throw failed
                }
                if (scaled) {
                    // Verify the PTS anchor of each batch against the ordinary seek. Some
                    // providers report frame counts yet return differently indexed frames.
                    val reference = readAtTime(clip.sourceTimesMs[index], width, height)
                    if (!sameSample(cache.getValue(index), reference)) {
                        clearCache()
                        batching = false
                        return reference
                    }
                    reference.recycle()
                }
                return cache.remove(index)
            }

            override fun frame(atMs: Long, edge: Int): Bitmap {
                val t = atMs.coerceIn(0L, clip.durationMs - 1)
                val times = clip.sourceTimesMs
                val index = if (times.isEmpty()) -1 else times.binarySearch(t)
                val floor = if (index >= 0) index else (-index - 2).coerceAtLeast(0)
                val scale = minOf(1.0, edge.toDouble() / maxOf(clip.width, clip.height))
                val w = (clip.width * scale).roundToInt().coerceAtLeast(1)
                val h = (clip.height * scale).roundToInt().coerceAtLeast(1)
                if (batching && (scale == 1.0 || allowScaledBatches)) {
                    // Full decoded buffers and one scaled buffer coexist during reduction.
                    val reserve = if (scale < 1.0) 4L * w * h else 0L
                    val count = ((budget - reserve) / originalBytes).coerceAtMost(12).toInt()
                    if (count > 1) readBatch(floor, w, h, count)?.let { return it }
                }
                return readAtTime(if (times.isEmpty()) t else times[floor], w, h)
            }
            override fun close() {
                clearCache()
                reader.release()
            }
        }
    }

    private fun sameSample(frame: Bitmap, reference: Bitmap): Boolean {
        if (frame.width != reference.width || frame.height != reference.height) return false
        var difference = 0
        for (row in 0 until 8) for (column in 0 until 8) {
            val x = (2 * column + 1) * frame.width / 16
            val y = (2 * row + 1) * frame.height / 16
            val a = frame.getPixel(x, y)
            val b = reference.getPixel(x, y)
            for (shift in 0..16 step 8)
                difference += kotlin.math.abs((a ushr shift and 255) - (b ushr shift and 255))
        }
        // The platform's fused rotation/scale can round differently from Bitmap filtering.
        return difference <= 64 * 3 * 4
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
        writeVideoFrames(context, doc, out, allowScaledBatches = true, cancel = cancel, onProgress = onProgress)
    }

    internal fun writeVideoFrames(context: Context, doc: GifDocument, out: OutputStream,
        allowScaledBatches: Boolean, cancel: (() -> Unit)? = null,
        onProgress: ((Int, Int) -> Unit)? = null) {
        val clip = requireNotNull(doc.video)
        val times = GifVideoMath.frameTimesMs(clip)
        val delays = GifVideoMath.frameDelaysMs(clip)
        val (w, h) = GifMath.dimensions(doc)
        open(context, clip, batchFrames = true, allowScaledBatches = allowScaledBatches).use { reader ->
            GifEncoder.writeFrames(out, doc, times.size, delays.first(), cancel,
                delayAt = { delays[it] }) { index ->
                val frame = reader.frame(times[index], maxOf(w,h))
                onProgress?.invoke(index + 1, times.size)
                frame
            }
        }
    }
}

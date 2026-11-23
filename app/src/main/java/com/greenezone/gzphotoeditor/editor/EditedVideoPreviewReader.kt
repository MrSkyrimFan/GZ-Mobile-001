package com.greenezone.gzphotoeditor.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.media.Image
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.SystemClock
import java.io.IOException
import java.util.zip.Deflater
import java.util.zip.Inflater
import kotlin.math.roundToInt

/** Android uses zero for unspecified output color metadata. */
internal fun gifColorMetadata(output: Int?, source: Int?): Int? =
    output?.takeIf { it > 0 } ?: source?.takeIf { it > 0 }

private fun requiresAndroidColorConversion(standard: Int?, transfer: Int?): Boolean =
    standard == MediaFormat.COLOR_STANDARD_BT2020 ||
        transfer == MediaFormat.COLOR_TRANSFER_HLG ||
        transfer == MediaFormat.COLOR_TRANSFER_ST2084

/**
 * Bitmap decoder for edited SDR video. Forward reads decode packets in order; reverse
 * reads decode bounded windows forward and consume the selected pictures backwards.
 * The caller owns returned bitmaps. Export applies filtered downscaling.
 */
internal class EditedVideoPreviewReader private constructor(
    private val clip: GifVideoClip,
    private val extractor: MediaExtractor,
    private val decoder: MediaCodec,
    private val trackFormat: MediaFormat,
    private val rotation: Int,
    private val prefetchReverseFrames: Boolean,
    private val filterDownscaling: Boolean,
    private val reversePreviewWindow: ReversePreviewWindow?,
    private val checkActive: () -> Unit
) : GifFrameReader {
    private val times = clip.sourceTimesMs
    private val info = MediaCodec.BufferInfo()
    private val firstTrimmedIndex = indexAtOrBefore(clip.startMs)
    private val localWindow = ReversePreviewWindow()
    private val reverseFirstTime = if (reversePreviewWindow != null && clip.reverse)
        GifVideoMath.frameTimesMs(clip).firstOrNull() else null
    private var sampleAxes: SampleAxes? = null
    private var reverseStorage: ReverseStorage? = null
    private var inputEnded = false
    private var lastIndex = -1
    private var started = false

    override fun frame(atMs: Long, edge: Int): Bitmap {
        checkActive()
        val target = indexAtOrBefore(atMs)
        val shared = reversePreviewWindow?.takeIf { clip.reverse }
        val window = shared ?: localWindow
        window.setEdge(edge)
        val scale = minOf(1.0, edge.toDouble() / maxOf(clip.width, clip.height))
        val width = (clip.width * scale).roundToInt().coerceAtLeast(1)
        val height = (clip.height * scale).roundToInt().coerceAtLeast(1)
        window.picture(target, consume = shared == null)?.let {
            return storage(width, height).render(it, checkActive)
        }
        if (shared == null) window.clear()
        // A paused first reverse picture must already decode through the same GOP.
        // Keep that pass's pictures for Play, independent of the current grade.
        val prefetch = clip.reverse && (prefetchReverseFrames || atMs == reverseFirstTime)
        val first = if (prefetch) {
            // Export can produce much larger pictures. Bound the scan by the number of
            // packed planes that fit even when noisy footage does not compress.
            val window = if (filterDownscaling)
                (REVERSE_CACHE_BYTES / (3L * width * height) + 1).coerceAtMost(MAX_REVERSE_PICTURES.toLong()).toInt()
                else MAX_REVERSE_PICTURES
            (target - window + 1).coerceIn(firstTrimmedIndex.coerceAtMost(target), target)
        } else target
        val packed = if (first < target || shared != null && prefetch) storage(width, height) else null
        if (!started || lastIndex < 0 || first <= lastIndex || clip.reverse ||
            times[first] - times[lastIndex] > 1_000L) {
            if (started) decoder.flush()
            extractor.seekTo(times[first] * 1_000L, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            started = true
            inputEnded = false
            lastIndex = -1
        }
        var lastOutput = SystemClock.uptimeMillis()
        while (true) {
            checkActive()
            feedAvailableInput()
            val output = decoder.dequeueOutputBuffer(info, 3_000)
            if (output < 0) {
                if (SystemClock.uptimeMillis() - lastOutput > 2_500L) {
                    throw IOException("Edited video decoder stopped producing frames")
                }
                continue
            }
            lastOutput = SystemClock.uptimeMillis()
            val end = info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
            var retainedIndex = -1
            var retainedColor: PreviewColor? = null
            try {
                if (info.size > 0 && info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0) {
                    val pts = (info.presentationTimeUs + 500L) / 1_000L
                    val index = times.binarySearch(pts)
                    if (index >= 0) lastIndex = index
                    if (pts > times[target]) throw IOException("Video decoder missed the requested frame")
                    if (index in first..target && !window.contains(index)) {
                        val image = decoder.getOutputImage(output)
                            ?: throw IOException("Video decoder cannot supply editable pictures")
                        image.use {
                            val picture = samplePicture(it, decoder.getOutputFormat(output), width, height)
                            if (index == target && (shared == null || !prefetch)) {
                                return picture.toBitmap(checkActive)
                            }
                            requireNotNull(packed).sample(picture, checkActive)
                            retainedIndex = index
                            retainedColor = picture.color
                        }
                    }
                }
            } finally {
                decoder.releaseOutputBuffer(output, false)
            }
            retainedColor?.let { color ->
                val storage = requireNotNull(packed)
                val picture = storage.compress(color, checkActive)
                window.retain(retainedIndex, picture)
                if (retainedIndex == target) return storage.render(picture, checkActive)
            }
            if (end) throw IOException("Video ended before the requested frame")
        }
    }

    private fun indexAtOrBefore(timeMs: Long): Int {
        val found = times.binarySearch(timeMs)
        return if (found >= 0) found else (-found - 2).coerceAtLeast(0)
    }

    private fun storage(width: Int, height: Int): ReverseStorage {
        val existing = reverseStorage
        if (existing != null && existing.width == width && existing.height == height) return existing
        existing?.close()
        return ReverseStorage(width, height).also { reverseStorage = it }
    }

    private fun feedAvailableInput() {
        if (inputEnded) return
        // B-frame reordering needs packets ahead of the output frame. Refill the
        // available codec slots without waiting on input while output is ready.
        repeat(8) {
            checkActive()
            val input = decoder.dequeueInputBuffer(0)
            if (input < 0) return
            val buffer = requireNotNull(decoder.getInputBuffer(input))
            val size = extractor.readSampleData(buffer, 0)
            if (size < 0) {
                decoder.queueInputBuffer(input, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                inputEnded = true
                return
            }
            decoder.queueInputBuffer(input, 0, size, extractor.sampleTime, 0)
            extractor.advance()
        }
    }

    private fun samplePicture(image: Image, format: MediaFormat, width: Int, height: Int): SamplePicture {
        val planes = image.planes
        if (image.format != ImageFormat.YUV_420_888 || planes.size != 3) {
            throw IOException("Video decoder returned an unsupported pixel format")
        }
        val crop = image.cropRect
        if (crop.width() <= 0 || crop.height() <= 0) throw IOException("Invalid video picture bounds")
        fun colorMetadata(key: String) = gifColorMetadata(format.number(key), trackFormat.number(key))
        val standard = colorMetadata(MediaFormat.KEY_COLOR_STANDARD)
        if (requiresAndroidColorConversion(standard, colorMetadata(MediaFormat.KEY_COLOR_TRANSFER))) {
            throw IOException("Use Android's image conversion for wide-gamut or HDR video")
        }
        val fullRange = colorMetadata(MediaFormat.KEY_COLOR_RANGE) == MediaFormat.COLOR_RANGE_FULL
        val hd = standard == MediaFormat.COLOR_STANDARD_BT709 ||
            (standard == null && maxOf(clip.width, clip.height) >= 1280)
        val axes = sampleAxes?.takeIf { it.width == width && it.height == height && it.crop == crop }
            ?: SampleAxes(width, height, crop, rotation, filterDownscaling).also { sampleAxes = it }
        return SamplePicture(Plane(planes[0]), Plane(planes[1]), Plane(planes[2]),
            axes, PreviewColor(hd, fullRange))
    }

    private class SampleAxes(
        val width: Int,
        val height: Int,
        val crop: Rect,
        rotation: Int,
        filterDownscaling: Boolean
    ) {
        private val transposed = rotation == 90 || rotation == 270
        private val sourceXs = sampleCoordinates(
            if (transposed) height else width, crop.left, crop.width(), rotation == 180 || rotation == 270)
        private val sourceYs = sampleCoordinates(
            if (transposed) width else height, crop.top, crop.height(), rotation == 90 || rotation == 180)

        private fun sampleCoordinates(count: Int, start: Int, span: Int, reverse: Boolean): IntArray =
            IntArray(count) { position ->
                val fraction = (position + .5) / count
                start + ((if (reverse) 1 - fraction else fraction) * span).toInt()
            }

        private val sourceColumns = if (transposed) height else width
        private val sourceRows = if (transposed) width else height
        private val filtered = filterDownscaling && (sourceColumns < crop.width() || sourceRows < crop.height())
        private val xWeights = if (filtered) filterCoordinates(sourceColumns, crop.left, crop.width(),
            rotation == 180 || rotation == 270) else emptyArray()
        private val yWeights = if (filtered) filterCoordinates(sourceRows, crop.top, crop.height(),
            rotation == 90 || rotation == 180) else emptyArray()

        private fun filterCoordinates(count: Int, start: Int, span: Int, reverse: Boolean): Array<FilterTaps> =
            Array(count) { position ->
                val fraction = (position + .5) / count
                val coordinate = (start + (if (reverse) 1 - fraction else fraction) * span - .5)
                    .coerceIn(start.toDouble(), (start + span - 1).toDouble())
                val low = coordinate.toInt()
                FilterTaps(low, (low + 1).coerceAtMost(start + span - 1),
                    ((coordinate - low) * 256).roundToInt())
            }

        fun sample(y: Plane, u: Plane, v: Plane, row: Int, column: Int): Int {
            val xIndex = if (transposed) row else column
            val yIndex = if (transposed) column else row
            if (filtered) {
                val x = xWeights[xIndex]
                val yy = yWeights[yIndex]
                return y.bilinear(x, yy, 0) or (u.bilinear(x, yy, 1) shl 8) or
                    (v.bilinear(x, yy, 1) shl 16)
            }
            val x = sourceXs[xIndex]
            val yy = sourceYs[yIndex]
            return y.read(x, yy) or (u.read(x / 2, yy / 2) shl 8) or
                (v.read(x / 2, yy / 2) shl 16)
        }
    }

    private class SamplePicture(
        private val y: Plane,
        private val u: Plane,
        private val v: Plane,
        val axes: SampleAxes,
        val color: PreviewColor
    ) {
        fun toBitmap(checkActive: () -> Unit): Bitmap =
            previewBitmap(axes.width, axes.height, checkActive) { row, column, _ ->
                val samples = axes.sample(y, u, v, row, column)
                color.pixel(samples and 255, (samples shr 8) and 255, samples shr 16)
            }

        fun copySamplesTo(samples: ByteArray, checkActive: () -> Unit) {
            val pitch = axes.width * axes.height
            for (row in 0 until axes.height) {
                if (row % 32 == 0) checkActive()
                for (column in 0 until axes.width) {
                    val sample = axes.sample(y, u, v, row, column)
                    val index = row * axes.width + column
                    samples[index] = sample.toByte()
                    samples[pitch + index] = (sample shr 8).toByte()
                    samples[2 * pitch + index] = (sample shr 16).toByte()
                }
            }
        }
    }

    internal class PreviewColor(hd: Boolean, fullRange: Boolean) {
        private val uToBlue = if (hd) 1.8556 else 1.772
        private val vToRed = if (hd) 1.5748 else 1.402
        private val uToGreen = if (hd) -.1873 else -.344136
        private val vToGreen = if (hd) -.4681 else -.714136
        private val chromaScale = if (fullRange) 1.0 else 255.0 / 224
        private val lumaScale = if (fullRange) 1.0 else 255.0 / 219
        private val lumaOffset = if (fullRange) 0 else 16

        fun pixel(y: Int, u: Int, v: Int): Int {
            val luma = (y - lumaOffset) * lumaScale
            val blueDelta = (u - 128) * chromaScale
            val redDelta = (v - 128) * chromaScale
            val red = channel(luma + vToRed * redDelta)
            val green = channel(luma + uToGreen * blueDelta + vToGreen * redDelta)
            val blue = channel(luma + uToBlue * blueDelta)
            return -0x1000000 or (red shl 16) or (green shl 8) or blue
        }

        private fun channel(value: Double) = value.roundToInt().coerceIn(0, 255)
    }

    internal data class ReversePicture(val samples: ByteArray, val color: PreviewColor)

    /** Packed pixels only. Preview owns this across seek/play readers under its reader lock. */
    internal class ReversePreviewWindow {
        private val pictures = linkedMapOf<Int, ReversePicture>()
        private var bytes = 0
        private var edge = 0

        internal fun setEdge(requested: Int) {
            if (edge == requested) return
            clear()
            edge = requested
        }

        internal fun contains(index: Int): Boolean = pictures.containsKey(index)

        internal fun picture(index: Int, consume: Boolean): ReversePicture? =
            if (consume) pictures.remove(index)?.also { bytes -= it.samples.size }
            else pictures[index]

        internal fun retain(index: Int, picture: ReversePicture) {
            pictures.put(index, picture)?.let { bytes -= it.samples.size }
            bytes += picture.samples.size
            val oldest = pictures.values.iterator()
            while (bytes > REVERSE_CACHE_BYTES || pictures.size > MAX_REVERSE_PICTURES) {
                bytes -= oldest.next().samples.size
                oldest.remove()
            }
        }

        internal fun clear() {
            pictures.clear()
            bytes = 0
        }
    }

    /**
     * Retain exact source samples at the preview dimensions and render color only when needed.
     * Lossless compression retains longer GOPs without another seek or a larger bitmap budget.
     * Noisy frames that do not compress are stored as raw planes under the same byte limit.
     */
    private class ReverseStorage(val width: Int, val height: Int) : AutoCloseable {
        private val pitch = width * height
        private val raw = ByteArray(3 * pitch)
        private val compressed = ByteArray(raw.size)
        private val deflater = Deflater(Deflater.BEST_SPEED)
        private val inflater = Inflater()

        fun sample(picture: SamplePicture, checkActive: () -> Unit) {
            picture.copySamplesTo(raw, checkActive)
        }

        fun compress(color: PreviewColor, checkActive: () -> Unit): ReversePicture {
            deflater.reset()
            deflater.setInput(raw)
            deflater.finish()
            val length = deflater.deflate(compressed)
            checkActive()
            val samples = if (deflater.finished() && length < raw.size) compressed.copyOf(length)
                else raw.copyOf()
            return ReversePicture(samples, color)
        }

        fun render(picture: ReversePicture, checkActive: () -> Unit): Bitmap {
            val samples = if (picture.samples.size == raw.size) picture.samples else {
                inflater.reset()
                inflater.setInput(picture.samples)
                if (inflater.inflate(raw) != raw.size || !inflater.finished()) {
                    throw IOException("Cannot restore the cached reverse preview picture")
                }
                raw
            }
            return previewBitmap(width, height, checkActive) { _, _, index ->
                picture.color.pixel(samples[index].toInt() and 255,
                    samples[pitch + index].toInt() and 255, samples[2 * pitch + index].toInt() and 255)
            }
        }

        override fun close() {
            try { deflater.end() } finally { inflater.end() }
        }
    }

    private data class FilterTaps(val low: Int, val high: Int, val highWeight: Int)

    private class Plane(plane: Image.Plane) {
        private val buffer = plane.buffer
        private val origin = buffer.position()
        private val rowStride = plane.rowStride
        private val pixelStride = plane.pixelStride

        fun read(x: Int, y: Int) = buffer.get(origin + y * rowStride + x * pixelStride).toInt() and 255

        fun bilinear(x: FilterTaps, y: FilterTaps, subsampleShift: Int): Int {
            val left = x.low shr subsampleShift
            val right = x.high shr subsampleShift
            val top = y.low shr subsampleShift
            val bottom = y.high shr subsampleShift
            val horizontal = if (left == right) 0 else x.highWeight
            val vertical = if (top == bottom) 0 else y.highWeight
            val first = read(left, top)
            if (horizontal == 0 && vertical == 0) return first
            val upper = if (horizontal == 0) first * 256
                else first * (256 - horizontal) + read(right, top) * horizontal
            if (vertical == 0) return (upper + 128) shr 8
            val lower = read(left, bottom) * (256 - horizontal) +
                (if (horizontal == 0) 0 else read(right, bottom) * horizontal)
            return (upper * (256 - vertical) + lower * vertical + 32768) shr 16
        }
    }

    override fun close() {
        localWindow.clear()
        try { reverseStorage?.close() } finally {
            try { decoder.release() } finally { extractor.release() }
        }
    }

    companion object {
        private const val MAX_REVERSE_PICTURES = 96
        private const val REVERSE_CACHE_BYTES = 16 * 1024 * 1024

        private inline fun previewBitmap(
            width: Int,
            height: Int,
            checkActive: () -> Unit,
            sourcePixel: (row: Int, column: Int, index: Int) -> Int
        ): Bitmap {
            val pixels = IntArray(width * height)
            for (row in 0 until height) {
                if (row % 32 == 0) checkActive()
                for (column in 0 until width) {
                    val index = row * width + column
                    pixels[index] = sourcePixel(row, column, index)
                }
            }
            return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
        }

        private fun MediaFormat.number(key: String): Int? =
            if (containsKey(key)) getInteger(key) else null

        fun open(
            context: Context,
            clip: GifVideoClip,
            prefetchReverseFrames: Boolean = false,
            filterDownscaling: Boolean = false,
            reversePreviewWindow: ReversePreviewWindow? = null,
            checkActive: () -> Unit
        ): EditedVideoPreviewReader {
            if (clip.isGif || clip.sourceTimesMs.isEmpty()) throw IOException("Video timestamps unavailable")
            val extractor = MediaExtractor()
            try {
                extractor.setDataSource(context, Uri.parse(clip.uri), null)
                checkActive()
                val track = (0 until extractor.trackCount).firstOrNull {
                    extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("video/") == true
                } ?: throw IOException("No video track")
                val format = extractor.getTrackFormat(track)
                val transfer = format.number(MediaFormat.KEY_COLOR_TRANSFER)
                val standard = format.number(MediaFormat.KEY_COLOR_STANDARD)
                if (requiresAndroidColorConversion(standard, transfer)) {
                    throw IOException("Use Android's image conversion for wide-gamut or HDR video")
                }
                val rotation = (format.number(MediaFormat.KEY_ROTATION) ?: 0).mod(360)
                format.setInteger(MediaFormat.KEY_ROTATION, 0)
                format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
                extractor.selectTrack(track)
                val decoder = MediaCodec.createDecoderByType(requireNotNull(format.getString(MediaFormat.KEY_MIME)))
                try {
                    decoder.configure(format, null, null, 0)
                    decoder.start()
                    return EditedVideoPreviewReader(clip, extractor, decoder, format, rotation,
                        prefetchReverseFrames, filterDownscaling, reversePreviewWindow, checkActive)
                } catch (error: Throwable) {
                    decoder.release()
                    throw error
                }
            } catch (error: Throwable) {
                extractor.release()
                throw error
            }
        }
    }
}

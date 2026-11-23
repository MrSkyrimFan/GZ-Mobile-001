package com.greenezone.gzphotoeditor.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.Image
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.SystemClock
import java.io.IOException
import kotlin.math.roundToInt

/** Android uses zero for unspecified output color metadata. */
internal fun gifColorMetadata(output: Int?, source: Int?): Int? =
    output?.takeIf { it > 0 } ?: source?.takeIf { it > 0 }

private fun requiresAndroidColorConversion(standard: Int?, transfer: Int?): Boolean =
    standard == MediaFormat.COLOR_STANDARD_BT2020 ||
        transfer == MediaFormat.COLOR_TRANSFER_HLG ||
        transfer == MediaFormat.COLOR_TRANSFER_ST2084

/**
 * Bitmap preview for edited SDR video. Forward play decodes packets in order. Reverse
 * decodes bounded windows forward and consumes the selected pictures backwards.
 * The caller owns returned bitmaps. Neither this reader nor its scaling is used by export.
 */
internal class EditedVideoPreviewReader private constructor(
    private val clip: GifVideoClip,
    private val extractor: MediaExtractor,
    private val decoder: MediaCodec,
    private val trackFormat: MediaFormat,
    private val rotation: Int,
    private val checkActive: () -> Unit
) : GifFrameReader {
    private val times = clip.sourceTimesMs
    private val info = MediaCodec.BufferInfo()
    private val pending = mutableMapOf<Int, Bitmap>()
    private var inputEnded = false
    private var lastIndex = -1
    private var started = false
    private var windowEdge = 0

    override fun frame(atMs: Long, edge: Int): Bitmap {
        checkActive()
        val found = times.binarySearch(atMs)
        val target = if (found >= 0) found else (-found - 2).coerceAtLeast(0)
        if (edge == windowEdge) pending.remove(target)?.let { return it }
        clearWindow()
        windowEdge = edge

        val scale = minOf(1.0, edge.toDouble() / maxOf(clip.width, clip.height))
        val width = (clip.width * scale).roundToInt().coerceAtLeast(1)
        val height = (clip.height * scale).roundToInt().coerceAtLeast(1)
        // Larger reverse windows avoid repeatedly decoding the same long GOP while
        // the presentation clock is running. Bound by both memory and frame count.
        val window = (16L * 1024 * 1024 / (4L * width * height)).coerceIn(1, 32).toInt()
        val first = if (clip.reverse) (target - window + 1).coerceAtLeast(0) else target
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
            feedInput()
            val output = decoder.dequeueOutputBuffer(info, 3_000)
            if (output < 0) {
                if (SystemClock.uptimeMillis() - lastOutput > 2_500L) {
                    throw IOException("Edited video decoder stopped producing frames")
                }
                continue
            }
            lastOutput = SystemClock.uptimeMillis()
            val end = info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
            try {
                if (info.size > 0 && info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0) {
                    val pts = (info.presentationTimeUs + 500L) / 1_000L
                    val index = times.binarySearch(pts)
                    if (index >= 0) lastIndex = index
                    if (pts > times[target]) throw IOException("Video decoder missed the requested frame")
                    if (index in first..target) {
                        val bitmap = decoder.getOutputImage(output)?.use {
                            toBitmap(it, decoder.getOutputFormat(output), width, height)
                        } ?: throw IOException("Video decoder cannot supply editable pictures")
                        if (index == target) return bitmap
                        pending.put(index, bitmap)?.recycle()
                    }
                }
            } finally {
                decoder.releaseOutputBuffer(output, false)
            }
            if (end) throw IOException("Video ended before the requested frame")
        }
    }

    private fun feedInput() {
        if (inputEnded) return
        val input = decoder.dequeueInputBuffer(2_000)
        if (input < 0) return
        val buffer = requireNotNull(decoder.getInputBuffer(input))
        val size = extractor.readSampleData(buffer, 0)
        if (size < 0) {
            decoder.queueInputBuffer(input, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            inputEnded = true
        } else {
            decoder.queueInputBuffer(input, 0, size, extractor.sampleTime, 0)
            extractor.advance()
        }
    }

    private fun toBitmap(image: Image, format: MediaFormat, width: Int, height: Int): Bitmap {
        if (image.format != ImageFormat.YUV_420_888 || image.planes.size != 3) {
            throw IOException("Video decoder returned an unsupported pixel format")
        }
        val crop = image.cropRect
        if (crop.width() <= 0 || crop.height() <= 0) throw IOException("Invalid video picture bounds")
        val y = Plane(image.planes[0])
        val u = Plane(image.planes[1])
        val v = Plane(image.planes[2])
        fun colorMetadata(key: String) = gifColorMetadata(format.number(key), trackFormat.number(key))
        val standard = colorMetadata(MediaFormat.KEY_COLOR_STANDARD)
        if (requiresAndroidColorConversion(standard, colorMetadata(MediaFormat.KEY_COLOR_TRANSFER))) {
            throw IOException("Use Android's image conversion for wide-gamut or HDR video")
        }
        val fullRange = colorMetadata(MediaFormat.KEY_COLOR_RANGE) == MediaFormat.COLOR_RANGE_FULL
        val hd = standard == MediaFormat.COLOR_STANDARD_BT709 ||
            (standard == null && maxOf(clip.width, clip.height) >= 1280)
        val uToBlue = if (hd) 1.8556 else 1.772
        val vToRed = if (hd) 1.5748 else 1.402
        val uToGreen = if (hd) -.1873 else -.344136
        val vToGreen = if (hd) -.4681 else -.714136
        val chromaScale = if (fullRange) 1.0 else 255.0 / 224
        val lumaScale = if (fullRange) 1.0 else 255.0 / 219
        val lumaOffset = if (fullRange) 0 else 16
        val pixels = IntArray(width * height)
        for (row in 0 until height) {
            if (row % 32 == 0) checkActive()
            for (column in 0 until width) {
                val x = (column + .5) / width
                val yy = (row + .5) / height
                val sx = when (rotation) { 90 -> yy; 180 -> 1 - x; 270 -> 1 - yy; else -> x }
                val sy = when (rotation) { 90 -> 1 - x; 180 -> 1 - yy; 270 -> x; else -> yy }
                val sourceX = crop.left + (sx * crop.width()).toInt()
                val sourceY = crop.top + (sy * crop.height()).toInt()
                val luma = (y.read(sourceX, sourceY) - lumaOffset) * lumaScale
                val blueDelta = (u.read(sourceX / 2, sourceY / 2) - 128) * chromaScale
                val redDelta = (v.read(sourceX / 2, sourceY / 2) - 128) * chromaScale
                val red = channel(luma + vToRed * redDelta)
                val green = channel(luma + uToGreen * blueDelta + vToGreen * redDelta)
                val blue = channel(luma + uToBlue * blueDelta)
                pixels[row * width + column] = -0x1000000 or (red shl 16) or (green shl 8) or blue
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    private class Plane(plane: Image.Plane) {
        private val buffer = plane.buffer
        private val origin = buffer.position()
        private val rowStride = plane.rowStride
        private val pixelStride = plane.pixelStride

        fun read(x: Int, y: Int) = buffer.get(origin + y * rowStride + x * pixelStride).toInt() and 255
    }

    private fun channel(value: Double) = value.roundToInt().coerceIn(0, 255)

    private fun clearWindow() {
        pending.values.forEach { it.recycle() }
        pending.clear()
    }

    override fun close() {
        clearWindow()
        try { decoder.release() } finally { extractor.release() }
    }

    companion object {
        private fun MediaFormat.number(key: String): Int? =
            if (containsKey(key)) getInteger(key) else null

        fun open(context: Context, clip: GifVideoClip, checkActive: () -> Unit): EditedVideoPreviewReader {
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
                    return EditedVideoPreviewReader(clip, extractor, decoder, format, rotation, checkActive)
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

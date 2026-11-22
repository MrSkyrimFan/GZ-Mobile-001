package com.greenezone.gzphotoeditor.editor

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.SystemClock
import android.view.Surface
import java.io.IOException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive

/** Decode in packet order, but present only the source PTS selected by the GIF schedule. */
internal suspend fun playScheduledVideo(context: Context, clip: GifVideoClip, surface: Surface,
    startFrame: Int, loopCount: Int, onFrame: suspend (Int) -> Unit) {
    val times = GifVideoMath.frameTimesMs(clip)
    val clock = GifPreviewClock(GifVideoMath.frameDelaysMs(clip))
    val job = currentCoroutineContext()
    val extractor = MediaExtractor()
    try {
        extractor.setDataSource(context, Uri.parse(clip.uri), null)
        val track = (0 until extractor.trackCount).firstOrNull {
            extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("video/") == true
        } ?: throw IOException("Missing video track")
        val format = extractor.getTrackFormat(track)
        val transfer = if (format.containsKey(MediaFormat.KEY_COLOR_TRANSFER))
            format.getInteger(MediaFormat.KEY_COLOR_TRANSFER) else 0
        if (transfer == MediaFormat.COLOR_TRANSFER_HLG || transfer == MediaFormat.COLOR_TRANSFER_ST2084)
            throw IOException("Use the bitmap preview for HDR video")
        extractor.selectTrack(track)
        val decoder = MediaCodec.createDecoderByType(requireNotNull(format.getString(MediaFormat.KEY_MIME)))
        try {
            decoder.configure(format, surface, null, 0)
            decoder.start()
            val info = MediaCodec.BufferInfo()
            var index = startFrame.coerceIn(times.indices)
            var origin: Long? = null
            var passStart = 0L
            var completed = 0
            while (true) {
                extractor.seekTo(times[index] * 1000, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
                var inputEnded = false
                var lastOutput = SystemClock.uptimeMillis()
                while (index < times.size) {
                    job.ensureActive()
                    if (!inputEnded) {
                        val input = decoder.dequeueInputBuffer(2_000)
                        if (input >= 0) {
                            val bytes = requireNotNull(decoder.getInputBuffer(input))
                            val size = extractor.readSampleData(bytes, 0)
                            if (size < 0) {
                                decoder.queueInputBuffer(input, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                inputEnded = true
                            } else {
                                decoder.queueInputBuffer(input, 0, size, extractor.sampleTime, 0)
                                extractor.advance()
                            }
                        }
                    }

                    val output = decoder.dequeueOutputBuffer(info, 2_000)
                    if (output < 0) {
                        if (SystemClock.uptimeMillis() - lastOutput > 5_000)
                            throw IOException("Video decoder stalled")
                        continue
                    }
                    if (info.size > 0 && info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0) {
                        val ptsMs = (info.presentationTimeUs + 500L) / 1000L
                        val target = times[index]
                        if (ptsMs > target) throw IOException("Decoder missed the selected frame")
                        if (ptsMs == target) {
                            val whenMs = passStart + clock.startOf(index)
                            val base = origin ?: (SystemClock.uptimeMillis() - whenMs).also { origin = it }
                            delay((base + whenMs - SystemClock.uptimeMillis()).coerceAtLeast(0))
                            decoder.releaseOutputBuffer(output, true)
                            onFrame(index++)
                        } else {
                            decoder.releaseOutputBuffer(output, false)
                        }
                    } else {
                        decoder.releaseOutputBuffer(output, false)
                    }
                    lastOutput = SystemClock.uptimeMillis()
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0 && index < times.size)
                        throw IOException("Video ended before the selected frame")
                }
                passStart += clock.endTime(-1)
                delay((requireNotNull(origin) + passStart - SystemClock.uptimeMillis()).coerceAtLeast(0))
                if (loopCount != 0 && completed >= loopCount) return
                completed++
                index = 0
                decoder.flush()
            }
        } finally {
            decoder.release()
        }
    } finally {
        extractor.release()
    }
}

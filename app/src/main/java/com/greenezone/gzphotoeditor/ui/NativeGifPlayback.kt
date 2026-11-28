package com.greenezone.gzphotoeditor.ui

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView
import com.greenezone.gzphotoeditor.editor.GifVideoClip
import com.greenezone.gzphotoeditor.editor.GifVideoMath
import kotlinx.coroutines.delay

@Composable
internal fun NativeGifPlayback(
    clip: GifVideoClip,
    startFrame: Int,
    loopCount: Int,
    onFrame: (Int) -> Unit,
    onEnd: (Int) -> Unit,
    onFailed: (Int) -> Unit,
    modifier: Modifier,
    onDisposed: () -> Unit
) {
    val context = LocalContext.current
    val view = remember(clip.uri) { VideoView(context) }
    val times = GifVideoMath.frameTimesMs(clip)
    val initialFrame = remember(view) { startFrame.coerceIn(times.indices) }
    var current by remember(view) { mutableIntStateOf(initialFrame) }
    var started by remember(view) { mutableStateOf(false) }
    val reportFrame by rememberUpdatedState(onFrame)
    val reportEnd by rememberUpdatedState(onEnd)
    val reportFailure by rememberUpdatedState(onFailed)

    DisposableEffect(view) {
        var completedLoops = 0
        var activePlayer: MediaPlayer? = null
        view.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE)
        view.setOnErrorListener { _, _, _ -> reportFailure(current); true }
        view.setOnPreparedListener { player ->
            activePlayer = player
            if (player.duration.toLong() > clip.durationMs) {
                // This player finishes with the longest track. Render the visual timeline
                // instead when a silent audio tail would extend the last GIF frame.
                reportFailure(initialFrame)
                return@setOnPreparedListener
            }
            player.setVolume(0f, 0f)
            player.isLooping = loopCount == 0
            if (initialFrame == 0) {
                view.start()
                started = true
            } else {
                player.setOnSeekCompleteListener {
                    player.setOnSeekCompleteListener(null)
                    view.start()
                    started = true
                }
                player.seekTo(times[initialFrame], MediaPlayer.SEEK_CLOSEST)
            }
        }
        view.setOnCompletionListener {
            if (completedLoops < loopCount) {
                completedLoops++
                view.start()
            } else {
                started = false
                reportEnd(times.lastIndex)
            }
        }
        view.setVideoURI(Uri.parse(clip.uri))
        onDispose {
            activePlayer?.setOnSeekCompleteListener(null)
            view.setOnPreparedListener(null)
            view.setOnCompletionListener(null)
            view.setOnErrorListener(null)
            view.stopPlayback()
            onDisposed()
        }
    }
    LaunchedEffect(view, started) {
        if (started) {
            while (true) {
                val found = times.binarySearch(view.currentPosition.toLong())
                val index = if (found >= 0) found else (-found - 2).coerceIn(times.indices)
                if (index != current) {
                    current = index
                    reportFrame(index)
                }
                delay(33)
            }
        }
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        AndroidView(factory = { view }, modifier = Modifier.aspectRatio(clip.width.toFloat() / clip.height)
            .testTag("gif-native-video").semantics { contentDescription = "GIF preview, frame ${current + 1}" })
    }
}

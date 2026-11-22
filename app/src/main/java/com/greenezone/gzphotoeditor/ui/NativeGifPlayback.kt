package com.greenezone.gzphotoeditor.ui

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.greenezone.gzphotoeditor.editor.GifVideoClip
import kotlinx.coroutines.delay

/**
 * Genuine Android video surface. MetadataRetriever is for export/thumbnail selection only.
 * This player owns only the media instance, never writes to or changes the source URI.
 */
@Composable
fun NativeGifPlayback(
    clip: GifVideoClip,
    playing: Boolean,
    forever: Boolean,
    onPosition: (Long) -> Unit,
    onEnded: () -> Unit,
    onError: (String) -> Unit,
    onReady: () -> Unit,
    onPlayer: (VideoView?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val positionCallback by rememberUpdatedState(onPosition)
    val endedCallback by rememberUpdatedState(onEnded)
    val errorCallback by rememberUpdatedState(onError)
    val readyCallback by rememberUpdatedState(onReady)
    val activeClip by rememberUpdatedState(clip)
    val activePlayback by rememberUpdatedState(playing)
    var prepared by remember(clip.uri) { mutableStateOf(false) }
    var media by remember(clip.uri) { mutableStateOf<MediaPlayer?>(null) }
    val view = remember(clip.uri) { VideoView(context) }

    DisposableEffect(view) {
        view.setOnPreparedListener { player ->
            media = player
            player.setVolume(0f, 0f) // GIF preview is intentionally silent.
            player.isLooping = false // Loop inside user-selected trim interval, not whole source.
            prepared = true
            view.seekTo(activeClip.startMs.toInt())
            readyCallback()
            if (activePlayback && !activeClip.reverse) view.start() else view.pause()
        }
        view.setOnErrorListener { _, what, extra ->
            prepared = false
            errorCallback("Android video playback failed ($what/$extra)")
            true
        }
        view.setOnCompletionListener {
            view.pause()
            endedCallback()
        }
        view.setAudioFocusRequest(android.media.AudioManager.AUDIOFOCUS_NONE)
        view.setVideoURI(Uri.parse(clip.uri))
        onPlayer(view)
        onDispose {
            onPlayer(null)
            media = null
            prepared = false
            try { view.stopPlayback() } catch (_: Exception) {}
        }
    }
    AndroidView(factory = { view }, modifier = modifier.fillMaxSize())

    LaunchedEffect(view, clip.startMs, clip.endMs, clip.speedPercent, clip.reverse, playing, prepared) {
        if (!prepared) return@LaunchedEffect
        val start = clip.startMs.toInt()
        val end = clip.endMs.toInt()
        val current = view.currentPosition
        if (current < start || current >= end) view.seekTo(start)
        if (clip.reverse || !playing) {
            if (view.isPlaying) view.pause()
        } else {
            try {
                val player = media
                if (player != null) {
                    val speed = clip.speedPercent / 100f
                    if (player.playbackParams.speed != speed) {
                        player.playbackParams = player.playbackParams.setSpeed(speed)
                    }
                }
            } catch (error: Exception) {
                errorCallback("Playback speed not supported by this video: " +
                    (error.message ?: "codec error"))
            }
            if (!view.isPlaying) view.start()
        }
    }
    LaunchedEffect(view, clip.startMs, clip.endMs, clip.reverse, playing, forever, prepared) {
        if (!prepared || !playing || clip.reverse) return@LaunchedEffect
        while (true) {
            delay(60)
            val pos = view.currentPosition.toLong()
            positionCallback(pos)
            if (pos >= clip.endMs - 35L) {
                view.pause()
                if (forever) {
                    view.seekTo(clip.startMs.toInt())
                    view.start()
                    positionCallback(clip.startMs)
                } else {
                    endedCallback()
                    break
                }
            }
        }
    }
}

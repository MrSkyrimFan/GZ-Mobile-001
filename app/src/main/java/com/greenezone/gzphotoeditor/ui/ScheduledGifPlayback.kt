package com.greenezone.gzphotoeditor.ui

import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
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
import com.greenezone.gzphotoeditor.editor.playScheduledVideo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun ScheduledGifPlayback(clip: GifVideoClip, startFrame: Int, loopCount: Int,
    onFrame: (Int) -> Unit, onEnd: (Int) -> Unit, onFailed: (Int) -> Unit, modifier: Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val view = remember(clip.uri) { TextureView(context) }
    var current by remember(view) { mutableIntStateOf(startFrame) }
    val reportFrame by rememberUpdatedState(onFrame)
    val reportEnd by rememberUpdatedState(onEnd)
    val reportFailure by rememberUpdatedState(onFailed)

    DisposableEffect(view, clip, loopCount) {
        var playback: Job? = null
        var disposed = false
        val listener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
                if (disposed) return
                val first = current
                val surface = Surface(texture)
                playback = scope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            playScheduledVideo(context, clip, surface, first, loopCount) { frame ->
                                withContext(Dispatchers.Main.immediate) {
                                    current = frame
                                    reportFrame(frame)
                                }
                            }
                        }
                        reportEnd(GifVideoMath.frameCount(clip) - 1)
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        reportFailure(current)
                    }
                }.also { it.invokeOnCompletion { surface.release() } }
            }
            override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
                val running = playback ?: return true
                playback = null
                running.cancel()
                // TextureView transfers ownership to us when returning false. Release only
                // after the decoder stops touching the surface, including during screen teardown.
                running.invokeOnCompletion { texture.release() }
                return false
            }
            override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) = Unit
            override fun onSurfaceTextureUpdated(texture: SurfaceTexture) = Unit
        }
        view.surfaceTextureListener = listener
        view.surfaceTexture?.let { listener.onSurfaceTextureAvailable(it, view.width, view.height) }
        onDispose {
            disposed = true
            playback?.cancel()
        }
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        AndroidView(factory = { view }, modifier = Modifier.aspectRatio(clip.width.toFloat() / clip.height)
            .testTag("gif-scheduled-video").semantics { contentDescription = "GIF preview, frame ${current + 1}" })
    }
}

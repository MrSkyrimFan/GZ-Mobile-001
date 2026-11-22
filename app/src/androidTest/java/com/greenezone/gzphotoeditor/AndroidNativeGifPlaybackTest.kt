package com.greenezone.gzphotoeditor

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.GifVideoMath
import com.greenezone.gzphotoeditor.editor.VideoGifSource
import com.greenezone.gzphotoeditor.ui.NativeGifPlayback
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class AndroidNativeGifPlaybackTest {
    @get:Rule val rule = createComposeRule()

    @Test fun nativePlayerActuallyPlaysAndPausesAnMp4() {
        val instrument = InstrumentationRegistry.getInstrumentation()
        val context = instrument.targetContext
        val resolver = context.contentResolver
        val meta = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, "GZ_gif_native_playback_2s.mp4")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, meta)
        assertNotNull(uri)
        try {
            instrument.context.assets.open("gif_sample_2sec_h264.mp4").use { input ->
                resolver.openOutputStream(uri!!,"wt")!!.use { input.copyTo(it) }
            }
            resolver.update(uri!!, ContentValues().apply {
                put(MediaStore.Video.Media.IS_PENDING, 0)
            }, null, null)
            val clip = VideoGifSource.inspect(context, uri)
            assertTrue(GifVideoMath.valid(clip))
            val ready = AtomicBoolean(false)
            val progress = AtomicLong(0)
            val ended = AtomicBoolean(false)
            val error = java.util.concurrent.atomic.AtomicReference<String?>(null)
            rule.setContent {
                var playing by remember { mutableStateOf(false) }
                Column {
                    NativeGifPlayback(
                        clip = clip,
                        playing = playing,
                        forever = false,
                        onPosition = { progress.set(it) },
                        onEnded = { ended.set(true); playing = false },
                        onError = { error.set(it); playing = false },
                        onReady = { ready.set(true) },
                        onPlayer = {}
                    )
                    Button(onClick = { playing = !playing }) {
                        Text(if (playing) "Pause" else "Play")
                    }
                }
            }
            rule.waitUntil(15_000) { ready.get() || error.get() != null }
            assertNull("A real video player should prepare successfully", error.get())
            rule.onNodeWithText("Play").performClick()
            rule.waitUntil(15_000) {
                progress.get() >= 250L || ended.get() || error.get() != null
            }
            assertNull("Native video preview must not error", error.get())
            assertTrue("Actual video time must advance", progress.get() >= 250L)
            if (!ended.get()) {
                rule.onNodeWithText("Pause").performClick()
            }
        } finally {
            if (uri != null) resolver.delete(uri, null, null)
        }
    }
}

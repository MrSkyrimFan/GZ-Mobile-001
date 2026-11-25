package com.greenezone.gzphotoeditor

import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.GifStore
import com.greenezone.gzphotoeditor.editor.VideoGifSource
import com.greenezone.gzphotoeditor.ui.GifMakerScreenContent
import com.greenezone.gzphotoeditor.ui.GifWorkspaceSession
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.OutputStream
import java.nio.file.Files

/** Real Back/save flows: source-supported GIF consistency coverage, not a reported phone exit failure. */
@RunWith(AndroidJUnit4::class)
class AndroidGifDepartureContractTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val target get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val resolver get() = target.contentResolver
    private val media = mutableListOf<Uri>()
    @Volatile private var current = GifDocument()
    @Volatile private var retained = GifWorkspaceSession()
    @Volatile private var departures = 0

    @Test fun dirtyPhotoBackCancelsThenDiscardsOnlyTheUnsavedBranch() = isolated { context ->
        val red = photo("gz-departure-red.png", Color.RED)
        val blue = photo("gz-departure-blue.png", Color.BLUE)
        val original = GifDocument(frames = listOf(red.toString(), blue.toString()))
        val originalBytes = listOf(bytes(red), bytes(blue))
        mount(context.filesDir, original)
        rule.onNodeWithText("Timing").performScrollTo().performClick()
        rule.onNodeWithText("Play once").performScrollTo().performClick()
        rule.waitUntil(15_000) { !current.forever }
        val edited = current

        back()
        assertEquals("Dirty Back must wait for a disposition", 0, departures)
        rule.onNodeWithText("Discard", substring = false).assertIsDisplayed()
        rule.onNodeWithText("Cancel", substring = false).performClick()
        rule.runOnIdle {
            assertEquals(edited, current)
            assertEquals(0, departures)
        }

        back()
        rule.onNodeWithText("Discard", substring = false).performClick()
        rule.waitUntil(15_000) { departures == 1 }
        rule.runOnIdle {
            assertEquals(original, current)
            assertTrue("Discard must not retain the abandoned redo branch", retained.history.redo.isEmpty())
            assertFalse("Discarded edit cannot remain as current undo content",
                retained.history.undo.any { it.document == edited })
        }
        assertArrayEquals(originalBytes[0], bytes(red))
        assertArrayEquals(originalBytes[1], bytes(blue))
        assertTrue(GifStore.names(context).isEmpty())
    }

    @Test fun dirtyVideoBackSavesCapturedRecipeBeforeOneDeparture() = isolated { context ->
        val source = publish(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            "gz-departure-video.mp4", "video/mp4") { output ->
            InstrumentationRegistry.getInstrumentation().context.assets
                .open("gif_sample_2sec_h264.mp4").use { it.copyTo(output) }
        }
        val originalBytes = bytes(source)
        val original = VideoGifSource.document(context, source)
        mount(context.filesDir, original)
        rule.onNodeWithText("Reverse").performClick()
        rule.waitUntil(15_000) { current.video?.reverse == true }
        val edited = current

        back()
        assertEquals("Dirty Back must not navigate before saving", 0, departures)
        rule.onNodeWithText("Save", substring = false).performClick()
        rule.onNode(hasSetTextAction()).performTextReplacement("Departure video")
        rule.onNodeWithText("Save", substring = false).performClick()
        rule.waitUntil(15_000) { departures == 1 }
        assertEquals(edited, GifStore.load(context, "Departure video"))
        rule.runOnIdle {
            assertEquals(edited, current)
            assertEquals(1, departures)
        }
        assertArrayEquals(originalBytes, bytes(source))
    }

    private fun back() {
        rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    private fun mount(folder: File, initial: GifDocument) {
        current = initial
        departures = 0
        retained = GifWorkspaceSession()
        rule.setContent {
            val base = LocalContext.current
            val context = remember(base, folder) {
                object : ContextWrapper(base) { override fun getFilesDir(): File = folder }
            }
            CompositionLocalProvider(LocalContext provides context) {
                MaterialTheme(colorScheme = darkColorScheme()) {
                    var document by remember { mutableStateOf(initial) }
                    var session by remember { mutableStateOf(GifWorkspaceSession()) }
                    GifMakerScreenContent(document,
                        onDocument = { document = it; current = it },
                        onBack = { departures++ }, onSwitch = { departures++ },
                        session = session, onSession = { session = it; retained = it })
                }
            }
        }
        rule.waitForIdle()
    }

    private fun photo(name: String, color: Int): Uri =
        publish(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, name, "image/png") { output ->
            val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
            try {
                bitmap.eraseColor(color)
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
            } finally { bitmap.recycle() }
        }

    private fun publish(collection: Uri, name: String, mime: String, write: (OutputStream) -> Unit): Uri {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = requireNotNull(resolver.insert(collection, values))
        media.add(uri)
        requireNotNull(resolver.openOutputStream(uri)).use(write)
        resolver.update(uri, ContentValues().apply {
            put(MediaStore.MediaColumns.IS_PENDING, 0)
        }, null, null)
        return uri
    }

    private fun bytes(uri: Uri): ByteArray =
        requireNotNull(resolver.openInputStream(uri)).use { it.readBytes() }

    private fun isolated(test: (Context) -> Unit) {
        val folder = Files.createTempDirectory(target.cacheDir.toPath(), "gz-gif-departure-").toFile()
        val context = object : ContextWrapper(target) { override fun getFilesDir(): File = folder }
        try { test(context) } finally {
            rule.runOnIdle { rule.activity.setContentView(android.widget.FrameLayout(rule.activity)) }
            media.forEach { resolver.delete(it, null, null) }
            folder.deleteRecursively()
        }
    }
}

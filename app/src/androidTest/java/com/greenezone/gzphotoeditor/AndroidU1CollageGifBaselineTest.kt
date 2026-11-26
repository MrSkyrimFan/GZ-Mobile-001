package com.greenezone.gzphotoeditor

import android.content.ContentValues
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.CollageDocument
import com.greenezone.gzphotoeditor.editor.CollagePhoto
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.GifEncoder
import com.greenezone.gzphotoeditor.editor.VideoGifSource
import com.greenezone.gzphotoeditor.ui.CollageWorkspaceScreen
import com.greenezone.gzphotoeditor.ui.GifMakerScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.file.Files

/** Real Options entry points against the qualified app before the U1 command change. */
@U1TargetedCheck
@RunWith(AndroidJUnit4::class)
class AndroidU1CollageGifBaselineTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test fun loadedCollageOptionsExposeDistinctDeleteAndResetCurrentWork() =
        withCollageTestImage(rule.activity) { source ->
            val mounted = mutableStateOf(true)
            val storage = Files.createTempDirectory(rule.activity.cacheDir.toPath(), "u1-collage-options-").toFile()
            try {
                rule.setContent {
                    if (mounted.value) {
                        IsolatedWorkspace(storage) {
                            var document by remember {
                                mutableStateOf(CollageDocument(listOf(CollagePhoto(source.toString()))))
                            }
                            CollageWorkspaceScreen(document, { document = it }, {})
                        }
                    }
                }
                rule.waitUntil(15_000) {
                    rule.onAllNodesWithContentDescription("Photo collage preview")
                        .fetchSemanticsNodes().isNotEmpty()
                }
                assertSharedCurrentWorkCommands()
            } finally {
                unmount(mounted)
                storage.deleteRecursively()
            }
        }

    @Test fun importedGifOptionsExposeDistinctDeleteAndResetCurrentWork() =
        withCollageTestImage(rule.activity) { photo ->
            val source = animatedGif(photo)
            val mounted = mutableStateOf(true)
            val storage = Files.createTempDirectory(rule.activity.cacheDir.toPath(), "u1-gif-options-").toFile()
            var contentSet = false
            try {
                val document = VideoGifSource.document(rule.activity, source)
                assertEquals(source.toString(), document.video?.uri)
                assertTrue("Fixture must reach the existing-GIF route", document.video?.isGif == true)
                rule.setContent {
                    if (mounted.value) {
                        IsolatedWorkspace(storage) {
                            var current by remember { mutableStateOf(document) }
                            GifMakerScreen(current, { current = it }, {}, {})
                        }
                    }
                }
                contentSet = true
                assertSharedCurrentWorkCommands()
            } finally {
                if (contentSet) unmount(mounted)
                rule.activity.contentResolver.delete(source, null, null)
                storage.deleteRecursively()
            }
        }

    private fun assertSharedCurrentWorkCommands() {
        rule.onNodeWithTag("workspace-options").assertIsDisplayed().performTouchInput { click() }
        rule.onNode(hasText("Open project…") and hasClickAction()).assertExists()
        val deleteCount = rule.onAllNodes(hasText("Delete") and hasClickAction()).fetchSemanticsNodes().size
        val resetCount = rule.onAllNodes(hasText("Reset all edits", substring = true) and hasClickAction())
            .fetchSemanticsNodes().size
        assertEquals("Options must expose separate Delete and Reset all edits actions",
            listOf(1, 1), listOf(deleteCount, resetCount))
    }

    private fun unmount(mounted: androidx.compose.runtime.MutableState<Boolean>) {
        rule.runOnIdle { mounted.value = false }
        rule.waitForIdle()
    }

    @androidx.compose.runtime.Composable
    private fun IsolatedWorkspace(storage: File, content: @androidx.compose.runtime.Composable () -> Unit) {
        val base = LocalContext.current
        val context = remember(base, storage) {
            object : ContextWrapper(base) {
                override fun getFilesDir(): File = storage
                override fun getCacheDir(): File = storage
            }
        }
        CompositionLocalProvider(LocalContext provides context) { MaterialTheme(content = content) }
    }

    private fun animatedGif(photo: Uri): Uri {
        val resolver = rule.activity.contentResolver
        val source = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "gz-u1-options-" + System.nanoTime() + ".gif")
                put(MediaStore.Images.Media.MIME_TYPE, "image/gif")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        try {
            val frames = GifDocument(frames = listOf(photo.toString(), photo.toString()))
            requireNotNull(resolver.openOutputStream(source, "wt")).use { output ->
                GifEncoder.write(output, frames) { index ->
                    Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888).apply {
                        eraseColor(if (index == 0) Color.RED else Color.BLUE)
                    }
                }
            }
            resolver.update(source, ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }, null, null)
            return source
        } catch (failure: Throwable) {
            resolver.delete(source, null, null)
            throw failure
        }
    }
}

package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.view.KeyEvent
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.CollagePhoto
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.ui.PhotoEditorScreen
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidBackNavigationTest {
    @get:Rule val rule = createComposeRule()

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val resolver get() = instrumentation.targetContext.contentResolver

    private fun createPhoto(label: String): Uri {
        val uri = requireNotNull(resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$label.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        ))
        val width = 96
        val height = 64
        val pixels = IntArray(width * height) { index ->
            if (index % width < width / 2) Color.rgb(24, 72, 120)
            else Color.rgb(232, 184, 72)
        }
        val bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
        try {
            resolver.openOutputStream(uri, "wt")!!.use {
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
        } finally {
            bitmap.recycle()
        }
        resolver.update(uri, ContentValues().apply {
            put(MediaStore.Images.Media.IS_PENDING, 0)
        }, null, null)
        return uri
    }

    private fun pressSystemBack() {
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        rule.waitForIdle()
    }

    private fun withLoadedRegular(test: (Uri, ByteArray) -> Unit) {
        val uri = createPhoto("gz-back-regular-" + System.nanoTime())
        val original = resolver.openInputStream(uri)!!.use { it.readBytes() }
        val picker = object : ActivityResultRegistryOwner {
            override val activityResultRegistry = object : ActivityResultRegistry() {
                override fun <I, O> onLaunch(
                    requestCode: Int,
                    contract: ActivityResultContract<I, O>,
                    input: I,
                    options: ActivityOptionsCompat?
                ) {
                    check(contract is ActivityResultContracts.PickVisualMedia)
                    dispatchResult(requestCode, Activity.RESULT_OK, Intent().setData(uri))
                }
            }
        }
        try {
            rule.setContent {
                CompositionLocalProvider(LocalActivityResultRegistryOwner provides picker) {
                    MaterialTheme(colorScheme = darkColorScheme()) {
                        PhotoEditorScreen()
                    }
                }
            }
            rule.onNodeWithText("Open media").performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithTag("photo-tools").fetchSemanticsNodes().isNotEmpty()
            }
            test(uri, original)
        } finally {
            resolver.delete(uri, null, null)
        }
    }

    private fun mountTile(
        uri: Uri,
        onDone: (CollagePhoto) -> Unit,
        onCancel: () -> Unit
    ) {
        rule.setContent {
            var editing by remember { mutableStateOf(true) }
            MaterialTheme(colorScheme = darkColorScheme()) {
                if (editing) {
                    PhotoEditorScreen(
                        initialCollagePhoto = CollagePhoto(uri.toString()),
                        onDoneCollagePhoto = {
                            onDone(it)
                            editing = false
                        },
                        onCancelCollagePhoto = {
                            onCancel()
                            editing = false
                        }
                    )
                } else {
                    Text("Tile host")
                }
            }
        }
        rule.waitUntil(15_000) {
            rule.onAllNodesWithTag("photo-tools").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test fun loadedRegularProGifBackRestoresProThenLoadedRegular() = withLoadedRegular { _, _ ->
        rule.onNodeWithContentDescription("Choose editing workspace").performClick()
        rule.onNodeWithText("Photo editing Pro").performClick()
        rule.onNodeWithText("Photo Pro ▾").assertIsDisplayed()

        rule.onNodeWithContentDescription("Choose editing workspace").performClick()
        rule.onNodeWithText("GIF maker").performClick()
        rule.onNodeWithText("GIF ▾").assertIsDisplayed()

        pressSystemBack()
        rule.onNodeWithText("Photo Pro ▾").assertIsDisplayed()

        pressSystemBack()
        rule.onNodeWithText("Photo ▾").assertIsDisplayed()
        rule.onNodeWithTag("photo-tools").assertIsDisplayed()
    }

    @Test fun cleanLoadedRegularBackReturnsToEmptyStart() = withLoadedRegular { uri, original ->
        pressSystemBack()
        rule.onNodeWithText("Open media").assertIsDisplayed()
        assertArrayEquals(original, resolver.openInputStream(uri)!!.use { it.readBytes() })
    }

    @Test fun dirtyLoadedRegularBackCanStayOrDiscardWithoutChangingSource() =
        withLoadedRegular { uri, original ->
            rule.onNodeWithText("Golden").performClick()
            pressSystemBack()
            rule.onNodeWithText("Keep your photo edits?").assertIsDisplayed()

            rule.onNodeWithText("Keep editing").performClick()
            rule.onNodeWithTag("photo-tools").assertIsDisplayed()

            pressSystemBack()
            rule.onNodeWithText("Discard").performClick()
            rule.onNodeWithText("Open media").assertIsDisplayed()
            assertArrayEquals(original, resolver.openInputStream(uri)!!.use { it.readBytes() })
        }

    @Test fun cleanTileBackCancelsWithoutApplying() {
        val uri = createPhoto("gz-back-clean-tile-" + System.nanoTime())
        var doneCount = 0
        var cancelCount = 0
        try {
            mountTile(uri, onDone = { doneCount++ }, onCancel = { cancelCount++ })
            pressSystemBack()
            rule.onNodeWithText("Tile host").assertIsDisplayed()
            rule.runOnIdle {
                assertEquals(0, doneCount)
                assertEquals(1, cancelCount)
            }
        } finally {
            resolver.delete(uri, null, null)
        }
    }

    @Test fun dirtyTileBackDiscardsWithoutApplying() {
        val uri = createPhoto("gz-back-discard-tile-" + System.nanoTime())
        var doneCount = 0
        var cancelCount = 0
        try {
            mountTile(uri, onDone = { doneCount++ }, onCancel = { cancelCount++ })
            rule.onNodeWithText("Golden").performClick()
            pressSystemBack()
            rule.onNodeWithText("Apply tile changes?").assertIsDisplayed()
            rule.onNodeWithText("Discard").performClick()
            rule.onNodeWithText("Tile host").assertIsDisplayed()
            rule.runOnIdle {
                assertEquals(0, doneCount)
                assertEquals(1, cancelCount)
            }
        } finally {
            resolver.delete(uri, null, null)
        }
    }

    @Test fun dirtyTileBackAppliesExactlyOnce() {
        val uri = createPhoto("gz-back-apply-tile-" + System.nanoTime())
        var applied: CollagePhoto? = null
        var doneCount = 0
        var cancelCount = 0
        try {
            mountTile(uri, onDone = {
                doneCount++
                applied = it
            }, onCancel = { cancelCount++ })
            rule.onNodeWithText("Golden").performClick()
            pressSystemBack()
            rule.onNodeWithText("Apply").performClick()
            rule.onNodeWithText("Tile host").assertIsDisplayed()
            rule.runOnIdle {
                assertEquals(1, doneCount)
                assertEquals(0, cancelCount)
                assertNotNull(applied)
                assertNotEquals(EditDocument(), applied!!.edits)
            }
        } finally {
            resolver.delete(uri, null, null)
        }
    }
}

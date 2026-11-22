package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.ProjectStore
import com.greenezone.gzphotoeditor.ui.PhotoEditorScreen
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidRegularToolsTest {
    @get:Rule val rule = createComposeRule()

    private fun withRegularPhoto(destination: Uri? = null, removeSourceAfterOpen: Boolean = false,
        test: () -> Unit) {
        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "gz-regular-tools.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        var removed = false
        try {
            val size = 256
            val pixels = IntArray(size * size) { if (it % size < size / 2) Color.BLACK else Color.WHITE }
            val source = Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
            try {
                resolver.openOutputStream(uri)!!.use { check(source.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            } finally { source.recycle() }
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            val picker = object : ActivityResultRegistryOwner {
                override val activityResultRegistry = object : ActivityResultRegistry() {
                    override fun <I, O> onLaunch(requestCode: Int, contract: ActivityResultContract<I, O>,
                        input: I, options: ActivityOptionsCompat?) {
                        val result = when (contract) {
                            is ActivityResultContracts.PickVisualMedia -> uri
                            is ActivityResultContracts.StartActivityForResult -> requireNotNull(destination)
                            else -> error("Unexpected activity contract")
                        }
                        dispatchResult(requestCode, Activity.RESULT_OK, Intent().setData(result))
                    }
                }
            }
            rule.setContent {
                CompositionLocalProvider(LocalActivityResultRegistryOwner provides picker) {
                    MaterialTheme(colorScheme = darkColorScheme()) { PhotoEditorScreen() }
                }
            }
            rule.onNodeWithText("Open media").performClick()
            rule.waitUntil(15_000) { rule.onAllNodesWithTag("photo-tools").fetchSemanticsNodes().isNotEmpty() }
            if (removeSourceAfterOpen) {
                assertEquals(1, resolver.delete(uri, null, null))
                removed = true
            }
            test()
        } finally {
            if (!removed) resolver.delete(uri, null, null)
        }
    }

    @Test fun regularCaptionSurvivesProjectReopenAndPngExport() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val resolver = context.contentResolver
        val output = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "gz-exported-caption.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        val name = "Caption " + System.nanoTime()
        try {
            withRegularPhoto(output) {
                rule.onNodeWithTag("photo-tools").performScrollToNode(hasText("Text"))
                rule.onNodeWithText("Text").performClick()
                rule.onNodeWithText("+ Add text").performClick()
                rule.onNode(hasSetTextAction()).performTextInput("Phone test caption")
                rule.onNodeWithText("Apply").performClick()
                rule.onNodeWithText("Text 1").assertIsDisplayed()
                rule.onNodeWithText("Edit words").performClick()
                rule.onNodeWithText("Phone test caption").assertIsDisplayed()
                rule.onNodeWithText("Cancel").performClick()
                rule.onNodeWithTag("photo-tools").performScrollToNode(hasText("More"))
                rule.onNodeWithText("More").performClick()
                val actions = rule.onNodeWithTag("photo-actions")
                actions.performScrollToNode(hasText("Save project"))
                rule.onNodeWithText("Save project").performClick()
                rule.onNode(hasSetTextAction()).performTextInput(name)
                rule.onNodeWithText("Save").performClick()
                actions.performScrollToNode(hasText("Open projects"))
                rule.onNodeWithText("Open projects").performClick()
                rule.onNodeWithText(name).performClick()
                rule.onNodeWithText("Open project").performClick()
                rule.waitUntil(15_000) { rule.onAllNodesWithText("Project restored").fetchSemanticsNodes().isNotEmpty() }
                val saved = ProjectStore.load(context).single { it.name == name }
                assertEquals("Phone test caption", saved.document.creative.text.single().content)

                rule.onNodeWithText("Export").performClick()
                rule.onNodeWithText("JPEG · JPG  ▾").performClick()
                rule.onNodeWithText("PNG · Lossless").performClick()
                rule.onNodeWithText("Save PNG").performClick()
                rule.waitUntil(15_000) {
                    rule.onAllNodesWithText("Saved 256 × 256", substring = true).fetchSemanticsNodes().isNotEmpty()
                }
                val exported = resolver.openInputStream(output)!!.use { requireNotNull(BitmapFactory.decodeStream(it)) }
                val original = resolver.openInputStream(saved.image)!!.use { requireNotNull(BitmapFactory.decodeStream(it)) }
                try {
                    assertEquals(original.width, exported.width)
                    assertEquals(original.height, exported.height)
                    var changed = 0
                    for (y in 0 until original.height) for (x in 0 until original.width) {
                        if (exported.getPixel(x, y) != original.getPixel(x, y)) changed++
                    }
                    assertTrue("Expected caption pixels in the exported PNG", changed > 20)
                    assertEquals(Color.BLACK, original.getPixel(90, 128))
                    assertEquals(Color.BLACK, exported.getPixel(0, 0))
                } finally {
                    original.recycle()
                    exported.recycle()
                }
            }
        } finally {
            ProjectStore.delete(context, name)
            resolver.delete(output, null, null)
        }
    }

    @Test fun failedSourceDecodePreservesExistingPhotoDestination() {
        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val destination = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "gz-existing-destination.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        val existing = byteArrayOf(0x38, 0x27, 0x16)
        try {
            resolver.openOutputStream(destination, "wt")!!.use { it.write(existing) }
            withRegularPhoto(destination, removeSourceAfterOpen = true) {
                rule.onNodeWithText("Export").performClick()
                rule.onNodeWithText("Save JPG").performClick()
                rule.waitUntil(15_000) {
                    rule.onAllNodesWithText("Export failed", substring = true).fetchSemanticsNodes().isNotEmpty()
                }
                assertArrayEquals(existing, resolver.openInputStream(destination)!!.use { it.readBytes() })
            }
        } finally { resolver.delete(destination, null, null) }
    }

    @Test fun blurShowsRealPixelsDuringTheStrokeWithTheControlsHidden() = withRegularPhoto {
        rule.onNodeWithTag("photo-tools").performScrollToNode(hasText("Blur & cover"))
        rule.onNodeWithText("Blur & cover").performClick()
        rule.onNodeWithText("+ Blur brush").performClick()
        val canvas = rule.onNodeWithContentDescription("Photo editing canvas. Tap to hide or show tools.")
        canvas.performClick()
        rule.onNodeWithTag("photo-tools").assertDoesNotExist()

        fun sample(): Int {
            val image = canvas.captureToImage().asAndroidBitmap()
            val inset = (minOf(image.width, image.height) * .012f).toInt()
            return image.getPixel(image.width / 2 + inset, image.height / 2)
        }
        assertTrue(Color.red(sample()) > 240)
        canvas.performTouchInput {
            down(center - Offset(width * .16f, 0f))
            moveTo(center - Offset(width * .07f, 0f))
            moveTo(center + Offset(width * .09f, 0f))
        }
        try {
            rule.waitUntil(15_000) {
                val color = sample()
                Color.red(color) in 120..225 && Color.red(color) == Color.green(color) &&
                    Color.green(color) == Color.blue(color)
            }
        } finally { canvas.performTouchInput { up() } }
        rule.onNodeWithTag("photo-tools").assertDoesNotExist()
    }
}

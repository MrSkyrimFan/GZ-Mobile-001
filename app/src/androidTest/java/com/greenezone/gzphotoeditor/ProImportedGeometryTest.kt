package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.BrushPoint
import com.greenezone.gzphotoeditor.editor.ProDocument
import com.greenezone.gzphotoeditor.editor.ProHitMath
import com.greenezone.gzphotoeditor.editor.ProLayer
import com.greenezone.gzphotoeditor.editor.ProMath
import com.greenezone.gzphotoeditor.editor.ProRaster
import com.greenezone.gzphotoeditor.editor.ProStore
import com.greenezone.gzphotoeditor.ui.ProWorkspaceScreen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ProImportedGeometryTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val resolver get() = context.contentResolver
    @Volatile private lateinit var current: ProDocument

    private fun createPhoto(width: Int, height: Int, color: Int, rotate: Boolean = false): Uri {
        val kind = if (rotate) "jpeg" else "png"
        val temporary = File.createTempFile("gz-pro-frame-", ".$kind", context.cacheDir)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        try {
            bitmap.eraseColor(color)
            temporary.outputStream().use {
                val format = if (rotate) Bitmap.CompressFormat.JPEG else Bitmap.CompressFormat.PNG
                assertTrue(bitmap.compress(format, 95, it))
            }
            if (rotate) {
                ExifInterface(temporary.path).apply {
                    setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90.toString())
                    saveAttributes()
                }
            }
            val photo = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, temporary.name)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/$kind")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }))
            try {
                requireNotNull(resolver.openOutputStream(photo, "wt")).use { output ->
                    temporary.inputStream().use { it.copyTo(output) }
                }
                resolver.update(photo, ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }, null, null)
                return photo
            } catch (failed: Throwable) {
                resolver.delete(photo, null, null)
                throw failed
            }
        } finally {
            bitmap.recycle()
            temporary.delete()
        }
    }

    private fun picker(added: Uri, replaced: Uri): ActivityResultRegistryOwner = object : ActivityResultRegistryOwner {
        override val activityResultRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int, contract: ActivityResultContract<I, O>, input: I, options: ActivityOptionsCompat?
            ) {
                val uri = when (contract) {
                    is ActivityResultContracts.PickMultipleVisualMedia -> added
                    is ActivityResultContracts.PickVisualMedia -> replaced
                    else -> error("Unexpected photo picker: $contract")
                }
                dispatchResult(requestCode, Activity.RESULT_OK, Intent().setData(uri))
            }
        }
    }

    @Test fun directPanoramaAndExifRotatedReplacementShareRenderAndHitGeometry() {
        val backing = createPhoto(64, 64, Color.GREEN)
        val wide = createPhoto(4000, 100, Color.RED)
        val tall = createPhoto(4000, 100, Color.BLUE, rotate = true)
        val name = "Pro frame " + System.nanoTime().toString().takeLast(10)
        try {
            val initial = ProDocument(listOf(ProLayer(backing.toString(), name = "Green backdrop")))
            current = initial
            val registry = picker(wide, tall)
            rule.setContent {
                var document by remember { mutableStateOf(initial) }
                CompositionLocalProvider(LocalActivityResultRegistryOwner provides registry) {
                    MaterialTheme {
                        ProWorkspaceScreen(document, { current = it; document = it }, {}, {})
                    }
                }
            }
            rule.onNodeWithText("+ Add layer").performClick()
            rule.onNodeWithText("Photo or image").performClick()
            rule.waitUntil(15_000) { current.layers.size == 2 }
            val imported = current
            assertEquals(40f, imported.layers.last().aspect, .0001f)
            assertNotNull(imported.layers.last().sourcePlacement)
            assertEquals(1, ProHitMath.pick(imported, .5f, .5f, 400, 400))
            assertEquals(0, ProHitMath.pick(imported, .5f, .55f, 400, 400))
            val source = requireNotNull(ProMath.canvasToImageNormalized(imported.layers.last(), 400, 400,
                BrushPoint(.5f, .5075f)))
            assertEquals(.8f, source.y, .01f)
            val widePixels = ProRaster.render(resolver, imported, 400, 400, false)
            try {
                assertEquals(Color.RED, widePixels.getPixel(200, 200))
                assertEquals(Color.GREEN, widePixels.getPixel(200, 220))
            } finally { widePixels.recycle() }

            rule.onNodeWithText("Replace image").performScrollTo().performClick()
            rule.onNodeWithText("Unsaved Pro changes").assertIsDisplayed()
            rule.onNodeWithText("Save").performClick()
            rule.onNode(hasSetTextAction()).performTextInput(name)
            rule.onNodeWithText("Save").performClick()
            rule.waitUntil(15_000) { current.layers.last().uri == tall.toString() }
            assertEquals(imported, ProStore.load(context, name))
            val replaced = current
            assertEquals(.025f, replaced.layers.last().aspect, .0001f)
            assertEquals(1, ProHitMath.pick(replaced, .5f, .5f, 400, 400))
            assertEquals(0, ProHitMath.pick(replaced, .55f, .5f, 400, 400))

            rule.onNodeWithText("Options").performClick()
            rule.onNodeWithText("Save project").performClick()
            rule.waitUntil(15_000) { ProStore.load(context, name) == replaced }
            rule.onAllNodesWithText("Save Pro project").assertCountEquals(0)
            val reopened = requireNotNull(ProStore.load(context, name))
            assertEquals(replaced, reopened)
            val tallPixels = ProRaster.render(resolver, reopened, 400, 400, false)
            try {
                assertTrue(Color.blue(tallPixels.getPixel(200, 200)) > 245)
                assertEquals(Color.GREEN, tallPixels.getPixel(220, 200))
            } finally { tallPixels.recycle() }
        } finally {
            ProStore.delete(context, name)
            for (uri in listOf(backing, wide, tall)) resolver.delete(uri, null, null)
        }
    }
}

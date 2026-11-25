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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.CollageDocument
import com.greenezone.gzphotoeditor.editor.CollageLayout
import com.greenezone.gzphotoeditor.editor.CollagePhoto
import com.greenezone.gzphotoeditor.editor.CollageQuality
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.ImageExportFormat
import com.greenezone.gzphotoeditor.editor.ExportWorkspace
import com.greenezone.gzphotoeditor.editor.PendingExportJournal
import com.greenezone.gzphotoeditor.editor.PrivacyEdit
import com.greenezone.gzphotoeditor.editor.PrivacyKind
import com.greenezone.gzphotoeditor.ui.CollageWorkspaceScreen
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class CollageExportUiTest {
    @get:Rule val rule = createComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val resolver = context.contentResolver
    private val journal = PendingExportJournal(context.filesDir)
    private val temporaryMedia = mutableListOf<Uri>()

    private fun document(name: String, mime: String): Uri {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        return requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
            .also { temporaryMedia.add(it) }
    }

    @After fun removeTestMedia() {
        journal.read(ExportWorkspace.COLLAGE)?.let { journal.discard(it) }
        temporaryMedia.forEach { resolver.delete(it, null, null) }
    }

    private fun source(index: Int, color: Int): Uri {
        val uri = document("gz-collage-$index.png", "image/png")
        val image = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        try {
            image.eraseColor(color)
            requireNotNull(resolver.openOutputStream(uri, "wt")).use {
                assertTrue(image.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
        } finally { image.recycle() }
        return uri
    }

    private fun chooseDestination(destination: Uri, format: ImageExportFormat, cancelFirst: Boolean = false) =
        object : ActivityResultRegistryOwner {
            override val activityResultRegistry = object : ActivityResultRegistry() {
                private var launches = 0

                override fun <I, O> onLaunch(requestCode: Int, contract: ActivityResultContract<I, O>,
                    input: I, options: ActivityOptionsCompat?) {
                    check(contract is ActivityResultContracts.StartActivityForResult && input is Intent)
                    assertEquals(Intent.ACTION_CREATE_DOCUMENT, input.action)
                    assertEquals(format.mime, input.type)
                    launches++
                    if (cancelFirst && launches == 1) dispatchResult(requestCode, Activity.RESULT_CANCELED, null)
                    else dispatchResult(requestCode, Activity.RESULT_OK, Intent().setData(destination))
                }
            }
        }

    private fun verifyExport(format: ImageExportFormat, columns: Int, rows: Int) {
        val colors = List(columns * rows) { i -> Color.rgb((23 + i * 11) % 256,
            (37 + i * 29) % 256, (181 + i * 13) % 256) }
        val photos = colors.mapIndexed { i, color ->
            val edits = if (i == 0) EditDocument(privacy = listOf(PrivacyEdit(
                kind = PrivacyKind.BLACK_BAR, width = .5f, height = .5f))) else EditDocument()
            CollagePhoto(source(i, color).toString(), edits)
        }
        val collage = CollageDocument(photos, CollageLayout(quality = CollageQuality.STANDARD))
        val destination = document("gz-collage-export.${format.extension}", format.mime)
        val chooser = chooseDestination(destination, format)
        rule.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides chooser) {
                CollageWorkspaceScreen(collage, {}, {})
            }
        }
        rule.onNodeWithText("Options").performClick()
        rule.onNodeWithText("Export image…").performClick()
        if (format == ImageExportFormat.PNG) {
            rule.onNodeWithText("JPEG · JPG  ▾").performClick()
            rule.onNodeWithText("PNG · Lossless").performClick()
        }
        rule.onNodeWithText("Save " + format.extension.uppercase()).performClick()
        rule.waitUntil(30_000) {
            rule.onAllNodesWithText("Collage saved", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        val info = BitmapFactory.Options()
        val output = requireNotNull(resolver.openInputStream(destination)).use {
            requireNotNull(BitmapFactory.decodeStream(it, null, info))
        }
        try {
            assertEquals(format.mime, info.outMimeType)
            assertEquals(output.width, output.height)
            assertTrue("Expected full export, not the 960px preview", output.width in 961..1600)
            val tolerance = if (format == ImageExportFormat.JPEG) 4 else 0
            for (i in colors.indices) {
                val cx = ((i % columns + .5f) * output.width / columns).toInt()
                val cy = ((i / columns + .5f) * output.height / rows).toInt()
                val actual = output.getPixel(cx, cy)
                val expected = if (i == 0) Color.BLACK else colors[i]
                assertTrue("Tile $i in $format: expected $expected, got $actual",
                    abs(Color.red(expected) - Color.red(actual)) <= tolerance &&
                    abs(Color.green(expected) - Color.green(actual)) <= tolerance &&
                    abs(Color.blue(expected) - Color.blue(actual)) <= tolerance)
            }
        } finally { output.recycle() }
        val original = requireNotNull(resolver.openInputStream(Uri.parse(photos[0].uri))).use {
            requireNotNull(BitmapFactory.decodeStream(it))
        }
        try {
            assertEquals(colors[0], original.getPixel(original.width / 2, original.height / 2))
        } finally { original.recycle() }
    }

    @Test fun missingTilePreservesExistingCollageDestination() {
        val missing = document("gz-invalid-tile.png", "image/png")
        val photos = listOf(CollagePhoto(source(0, Color.BLUE).toString()), CollagePhoto(missing.toString()))
        val destination = source(7, Color.MAGENTA)
        val before = resolver.openInputStream(destination)!!.use { it.readBytes() }
        val chooser = chooseDestination(destination, ImageExportFormat.JPEG)
        rule.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides chooser) {
                CollageWorkspaceScreen(CollageDocument(photos), {}, {})
            }
        }
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText("Photo 2 is inaccessible", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Options").performClick()
        rule.onNodeWithText("Export image…").performClick()
        rule.onNodeWithText("Save JPG").performClick()
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText("Export failed", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        assertArrayEquals(before, resolver.openInputStream(destination)!!.use { it.readBytes() })
    }

    @Test fun cancelingCollagePickerKeepsDestinationAndAllowsAnotherExport() {
        val photos = listOf(CollagePhoto(source(0, Color.BLUE).toString()),
            CollagePhoto(source(1, Color.GREEN).toString()))
        val destination = source(7, Color.MAGENTA)
        val before = resolver.openInputStream(destination)!!.use { it.readBytes() }
        val chooser = chooseDestination(destination, ImageExportFormat.PNG, cancelFirst = true)
        rule.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides chooser) {
                CollageWorkspaceScreen(CollageDocument(photos), {}, {})
            }
        }
        rule.onNodeWithText("Options").performClick()
        rule.onNodeWithText("Export image…").performClick()
        rule.onNodeWithText("JPEG · JPG  ▾").performClick()
        rule.onNodeWithText("PNG · Lossless").performClick()
        rule.onNodeWithText("Save PNG").performClick()
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText("Collage export canceled").fetchSemanticsNodes().isNotEmpty()
        }
        assertArrayEquals(before, resolver.openInputStream(destination)!!.use { it.readBytes() })
        assertNull(journal.read(ExportWorkspace.COLLAGE))

        rule.onNodeWithText("Options").performClick()
        rule.onNodeWithText("Export image…").performClick()
        rule.onNodeWithText("Save PNG").performClick()
        rule.waitUntil(30_000) {
            rule.onAllNodesWithText("Collage saved", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        val output = requireNotNull(resolver.openInputStream(destination)).use {
            requireNotNull(BitmapFactory.decodeStream(it))
        }
        try {
            assertTrue(output.width > 32)
        } finally { output.recycle() }
        assertNull(journal.read(ExportWorkspace.COLLAGE))
    }

    @Test fun savePngKeepsTwentyIndependentTilesAndTheSourcePhoto() {
        verifyExport(ImageExportFormat.PNG, columns = 5, rows = 4)
    }

    @Test fun saveJpegKeepsBothTilesAndTheSourcePhoto() {
        verifyExport(ImageExportFormat.JPEG, columns = 2, rows = 1)
    }
}

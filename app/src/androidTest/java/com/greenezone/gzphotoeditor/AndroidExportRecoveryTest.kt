package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performScrollToNode
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.ExportWorkspace
import com.greenezone.gzphotoeditor.editor.PendingExportJournal
import com.greenezone.gzphotoeditor.editor.PendingExportPhase
import com.greenezone.gzphotoeditor.ui.PhotoEditorScreen
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.performTextInput
import com.greenezone.gzphotoeditor.editor.CollageDocument
import com.greenezone.gzphotoeditor.editor.CollageLayout
import com.greenezone.gzphotoeditor.editor.CollagePhoto
import com.greenezone.gzphotoeditor.editor.CollageQuality
import com.greenezone.gzphotoeditor.editor.CollageStore
import com.greenezone.gzphotoeditor.editor.CollageTemplate
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.PendingImageRecipe
import com.greenezone.gzphotoeditor.editor.PrivacyEdit
import com.greenezone.gzphotoeditor.editor.PrivacyKind
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidExportRecoveryTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val journal get() = PendingExportJournal(context.filesDir)
    private val resolver get() = context.contentResolver

    private class DelayedPicker(
        private val source: Uri,
        pickingRequestCode: Int? = null
    ) : ActivityResultRegistryOwner {
        var exportRequestCode: Int? = pickingRequestCode
            private set
        var launches = 0
            private set

        override val activityResultRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(requestCode: Int, contract: ActivityResultContract<I, O>,
                input: I, options: ActivityOptionsCompat?) {
                when (contract) {
                    is ActivityResultContracts.PickVisualMedia ->
                        dispatchResult(requestCode, Activity.RESULT_OK, Intent().setData(source))
                    is ActivityResultContracts.StartActivityForResult -> {
                        val intent = input as Intent
                        check(intent.action == Intent.ACTION_CREATE_DOCUMENT)
                        check(intent.type == "image/png")
                        exportRequestCode = requestCode
                        launches++
                    }
                    else -> error("Unexpected activity contract")
                }
            }
        }

        fun deliver(resultCode: Int, destination: Uri?) {
            activityResultRegistry.dispatchResult(requireNotNull(exportRequestCode), resultCode,
                destination?.let { Intent().setData(it) })
        }
    }

    @Composable
    private fun Workspace(registry: DelayedPicker) {
        CompositionLocalProvider(LocalActivityResultRegistryOwner provides registry) {
            MaterialTheme(colorScheme = darkColorScheme()) { PhotoEditorScreen() }
        }
    }

    @Test fun delayedDestinationWritesTheMirroredPhotoAfterActivityRecreationOnlyOnce() {
        withDelayedExport { registry, _, destination, duplicateDestination ->
            rule.runOnIdle {
                registry.deliver(Activity.RESULT_OK, destination)
                registry.deliver(Activity.RESULT_OK, duplicateDestination)
            }
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Saved 256 × 128", substring = true).fetchSemanticsNodes().isNotEmpty()
            }
            val actual = resolver.openInputStream(destination)!!.use {
                requireNotNull(BitmapFactory.decodeStream(it))
            }
            try {
                assertEquals(256, actual.width)
                assertEquals(128, actual.height)
                assertEquals(Color.BLUE, actual.getPixel(30, 62))
                assertEquals(Color.RED, actual.getPixel(226, 62))
            } finally { actual.recycle() }
            assertArrayEquals(SENTINEL, resolver.openInputStream(duplicateDestination)!!.use { it.readBytes() })
            assertNull(journal.read(ExportWorkspace.PHOTO))

            rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
            rule.onNodeWithText("Unsaved changes").assertIsDisplayed()
            rule.onNodeWithText("Discard").performClick()
            rule.onNodeWithText("Choose Options → New photo… to begin.").assertIsDisplayed()
            rule.onNodeWithContentDescription("Choose editing workspace").performClick()
            rule.onNodeWithText("Photo editing Pro").performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Open a photo or create a canvas from Options.")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
            rule.onNodeWithText("Choose Options → New photo… to begin.").assertIsDisplayed()
            rule.waitForIdle()
            rule.onNodeWithTag("photo-tools").assertDoesNotExist()
        }
    }

    @Test fun canceledDestinationClearsTheRestoredRequestAndPreservesExistingBytes() {
        withDelayedExport { registry, _, destination, _ ->
            rule.runOnIdle { registry.deliver(Activity.RESULT_CANCELED, null) }
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Export canceled").fetchSemanticsNodes().isNotEmpty()
            }
            assertArrayEquals(SENTINEL, resolver.openInputStream(destination)!!.use { it.readBytes() })
            assertNull(journal.read(ExportWorkspace.PHOTO))
        }
    }

    @Test fun delayedCollageDestinationRestoresBothTilesAndWritesPngOnceAfterRecreation() {
        journal.read(ExportWorkspace.COLLAGE)?.let { journal.discard(it) }
        val first = createMedia("recovery-collage-left.png")
        val second = createMedia("recovery-collage-right.png")
        val destination = createMedia("recovery-collage-export.png")
        val duplicate = createMedia("recovery-collage-unrelated.png")
        val id = System.nanoTime()
        val originalName = "Recovery source $id"
        val restoredName = "Recovery result $id"
        val saved = CollageDocument(
            photos = listOf(
                CollagePhoto(first.toString(), EditDocument(privacy = listOf(
                    PrivacyEdit(kind = PrivacyKind.BLACK_BAR, width = .5f, height = .5f)
                )), id = "recovery-left"),
                CollagePhoto(second.toString(), EditDocument(flipVertical = true), id = "recovery-right")
            ),
            layout = CollageLayout(template = CollageTemplate.SIDE_BY_SIDE, margin = 0f, gap = 0f,
                cornerRadius = 0f, border = 0f, quality = CollageQuality.STANDARD)
        )
        try {
            writeSource(first, Bitmap.createBitmap(IntArray(96 * 192) { Color.RED },
                96, 192, Bitmap.Config.ARGB_8888))
            writeSource(second, Bitmap.createBitmap(IntArray(96 * 192) { index ->
                if (index / 96 < 96) Color.BLUE else Color.GREEN
            }, 96, 192, Bitmap.Config.ARGB_8888))
            listOf(destination, duplicate).forEach { uri ->
                resolver.openOutputStream(uri)!!.use { it.write(SENTINEL) }
            }
            check(CollageStore.save(context, originalName, saved))

            val registry = DelayedPicker(first)
            rule.setContent { Workspace(registry) }
            rule.onNodeWithContentDescription("Choose editing workspace").performClick()
            rule.onNodeWithText("Collage maker").performClick()
            rule.onNodeWithText("Options").performClick()
            rule.onNodeWithText("Open project…").performClick()
            rule.waitUntil(15_000) { rule.onAllNodesWithText(originalName).fetchSemanticsNodes().isNotEmpty() }
            rule.onNodeWithText(originalName).performClick()
            rule.onNodeWithText("Open").performClick()
            waitForCollagePreview()
            rule.onNodeWithText("Options").performClick()
            rule.onNodeWithText("Export image…").performClick()
            rule.onNodeWithText("JPEG · JPG  ▾").performClick()
            rule.onNodeWithText("PNG · Lossless").performClick()
            rule.onNodeWithText("Save PNG").performClick()
            rule.runOnIdle { assertEquals(1, registry.launches) }
            val pending = requireNotNull(journal.read(ExportWorkspace.COLLAGE))
            assertEquals(PendingExportPhase.PICKING, pending.phase)
            val encoded = requireNotNull(PendingImageRecipe.decode(pending.recipe))
            assertEquals(saved, CollageStore.decode(encoded.payload))

            val restoredPicker = recreateWithPendingPicker(registry, first)
            assertEquals(pending.id, journal.read(ExportWorkspace.COLLAGE)?.id)
            waitForCollagePreview()
            rule.runOnIdle {
                restoredPicker.deliver(Activity.RESULT_OK, destination)
                restoredPicker.deliver(Activity.RESULT_OK, duplicate)
            }
            rule.waitUntil(30_000) {
                rule.onAllNodesWithText("Collage saved", substring = true).fetchSemanticsNodes().isNotEmpty()
            }
            val options = BitmapFactory.Options()
            val output = resolver.openInputStream(destination)!!.use {
                requireNotNull(BitmapFactory.decodeStream(it, null, options))
            }
            try {
                assertEquals("image/png", options.outMimeType)
                val side = output.width
                assertEquals(side, output.height)
                assertTrue("Expected export resolution above preview size", side in 961..1600)
                assertEquals(Color.BLACK, output.getPixel(side / 4, side / 2))
                assertEquals(Color.RED, output.getPixel(side / 4, side / 8))
                assertEquals(Color.GREEN, output.getPixel(3 * side / 4, side / 4))
                assertEquals(Color.BLUE, output.getPixel(3 * side / 4, 3 * side / 4))
            } finally { output.recycle() }
            assertArrayEquals(SENTINEL, resolver.openInputStream(duplicate)!!.use { it.readBytes() })
            assertNull(journal.read(ExportWorkspace.COLLAGE))

            // Save the restored editor itself. Export pixels alone only prove the journal recipe.
            rule.onNodeWithText("Options").performClick()
            rule.onNodeWithText("Save project as…").performClick()
            rule.onNode(hasSetTextAction()).performTextInput(restoredName)
            rule.onNodeWithText("Save").performClick()
            rule.waitUntil(15_000) { CollageStore.load(context, restoredName) != null }
            assertEquals(saved, CollageStore.load(context, restoredName))
        } finally {
            journal.read(ExportWorkspace.COLLAGE)?.let { journal.discard(it) }
            CollageStore.delete(context, originalName)
            CollageStore.delete(context, restoredName)
            listOf(first, second, destination, duplicate).forEach { resolver.delete(it, null, null) }
        }
    }

    private fun waitForCollagePreview() {
        rule.waitUntil(15_000) {
            rule.onAllNodesWithContentDescription("Photo collage preview").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun withDelayedExport(
        test: (DelayedPicker, Uri, Uri, Uri) -> Unit
    ) {
        journal.read(ExportWorkspace.PHOTO)?.let { journal.discard(it) }
        val source = createMedia("recovery-source.png")
        val destination = createMedia("recovery-export.png")
        val duplicate = createMedia("recovery-unrelated.png")
        try {
            val original = Bitmap.createBitmap(IntArray(256 * 128) { index ->
                if (index % 256 < 128) Color.RED else Color.BLUE
            }, 256, 128, Bitmap.Config.ARGB_8888)
            writeSource(source, original)
            listOf(destination, duplicate).forEach { uri ->
                resolver.openOutputStream(uri)!!.use { it.write(SENTINEL) }
            }
            val registry = DelayedPicker(source)
            rule.setContent { Workspace(registry) }
            rule.onNodeWithText("Options").performClick()
            rule.onNodeWithText("New photo…").performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithTag("photo-tools").fetchSemanticsNodes().isNotEmpty()
            }
            rule.onNodeWithTag("photo-tools").performScrollToNode(hasText("Crop"))
            rule.onNodeWithText("Crop").performClick()
            rule.onNodeWithTag("crop-transforms").performScrollToNode(hasText("Flip horizontal"))
            rule.onNodeWithText("Flip horizontal").performClick()
            rule.onNodeWithText("Options").performClick()
            rule.onNodeWithText("Export image…").performClick()
            rule.onNodeWithText("JPEG · JPG  ▾").performClick()
            rule.onNodeWithText("PNG · Lossless").performClick()
            rule.onNodeWithText("Save PNG").performClick()
            rule.runOnIdle { assertEquals(1, registry.launches) }
            val pending = requireNotNull(journal.read(ExportWorkspace.PHOTO))
            assertEquals(PendingExportPhase.PICKING, pending.phase)

            val restoredPicker = recreateWithPendingPicker(registry, source)
            assertEquals(pending.id, journal.read(ExportWorkspace.PHOTO)?.id)
            test(restoredPicker, source, destination, duplicate)
        } finally {
            journal.read(ExportWorkspace.PHOTO)?.let { journal.discard(it) }
            listOf(source, destination, duplicate).forEach { resolver.delete(it, null, null) }
        }
    }

    private fun recreateWithPendingPicker(registry: DelayedPicker, source: Uri): DelayedPicker {
        val oldActivity = rule.activity
        val registryState = Bundle()
        val externalRequestCode = rule.runOnIdle {
            registry.activityResultRegistry.onSaveInstanceState(registryState)
            requireNotNull(registry.exportRequestCode)
        }
        rule.activityRule.scenario.recreate()
        val restoredPicker = DelayedPicker(source, externalRequestCode)
        rule.activityRule.scenario.onActivity { activity ->
            assertNotSame(oldActivity, activity)
            restoredPicker.activityResultRegistry.onRestoreInstanceState(registryState)
            activity.setContent { Workspace(restoredPicker) }
        }
        rule.waitForIdle()
        rule.runOnIdle { assertEquals(0, restoredPicker.launches) }
        return restoredPicker
    }

    private fun writeSource(uri: Uri, bitmap: Bitmap) {
        try {
            resolver.openOutputStream(uri)!!.use {
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
        } finally { bitmap.recycle() }
        resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
    }

    private fun createMedia(name: String): Uri = requireNotNull(
        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        })
    )

    private companion object {
        val SENTINEL = byteArrayOf(12, 34, 56, 78)
    }
}

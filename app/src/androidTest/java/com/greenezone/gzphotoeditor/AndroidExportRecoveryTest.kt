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
import androidx.compose.ui.test.performClick
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
            try {
                resolver.openOutputStream(source)!!.use {
                    check(original.compress(Bitmap.CompressFormat.PNG, 100, it))
                }
            } finally { original.recycle() }
            resolver.update(source,
                ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            listOf(destination, duplicate).forEach { uri ->
                resolver.openOutputStream(uri)!!.use { it.write(SENTINEL) }
            }
            val registry = DelayedPicker(source)
            rule.setContent { Workspace(registry) }
            rule.onNodeWithText("Open media").performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithTag("photo-tools").fetchSemanticsNodes().isNotEmpty()
            }
            rule.onNodeWithTag("photo-tools").performScrollToNode(hasText("Crop"))
            rule.onNodeWithText("Crop").performClick()
            rule.onNodeWithTag("crop-transforms").performScrollToNode(hasText("Flip horizontal"))
            rule.onNodeWithText("Flip horizontal").performClick()
            rule.onNodeWithText("Export").performClick()
            rule.onNodeWithText("JPEG · JPG  ▾").performClick()
            rule.onNodeWithText("PNG · Lossless").performClick()
            rule.onNodeWithText("Save PNG").performClick()
            rule.runOnIdle { assertEquals(1, registry.launches) }
            val pending = requireNotNull(journal.read(ExportWorkspace.PHOTO))
            assertEquals(PendingExportPhase.PICKING, pending.phase)

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
            assertEquals(pending.id, journal.read(ExportWorkspace.PHOTO)?.id)
            test(restoredPicker, source, destination, duplicate)
        } finally {
            journal.read(ExportWorkspace.PHOTO)?.let { journal.discard(it) }
            listOf(source, destination, duplicate).forEach { resolver.delete(it, null, null) }
        }
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

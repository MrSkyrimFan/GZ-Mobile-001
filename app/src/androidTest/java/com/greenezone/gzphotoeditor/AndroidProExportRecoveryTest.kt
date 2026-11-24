package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.ExportWorkspace
import com.greenezone.gzphotoeditor.editor.PendingExportJournal
import com.greenezone.gzphotoeditor.editor.PendingExportPhase
import com.greenezone.gzphotoeditor.editor.ProDocument
import com.greenezone.gzphotoeditor.editor.ProLayer
import com.greenezone.gzphotoeditor.editor.ProPixelSize
import com.greenezone.gzphotoeditor.ui.ProWorkspaceScreen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidProExportRecoveryTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val resolver get() = context.contentResolver
    private val journal get() = PendingExportJournal(context.filesDir)

    private class DelayedDestination : ActivityResultRegistryOwner {
        private var requestCode: Int? = null
        var launches = 0
            private set

        override val activityResultRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int, contract: ActivityResultContract<I, O>, input: I, options: ActivityOptionsCompat?
            ) {
                check(contract is ActivityResultContracts.StartActivityForResult)
                val intent = input as Intent
                assertEquals(Intent.ACTION_CREATE_DOCUMENT, intent.action)
                assertEquals("image/png", intent.type)
                assertEquals("GZEditorPro_Pro.png", intent.getStringExtra(Intent.EXTRA_TITLE))
                this@DelayedDestination.requestCode = requestCode
                launches++
            }
        }

        fun deliver(result: Int, uri: Uri?) = activityResultRegistry.dispatchResult(
            requireNotNull(requestCode), result, uri?.let { Intent().setData(it) }
        )
    }

    @Composable
    private fun ProWorkspace(registry: DelayedDestination, document: ProDocument) {
        CompositionLocalProvider(LocalActivityResultRegistryOwner provides registry) {
            MaterialTheme { ProWorkspaceScreen(document, {}, {}, {}) }
        }
    }

    private fun waitForNotice(message: String) {
        rule.waitUntil(30_000) {
            rule.onAllNodesWithText(message, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test fun delayedProDestinationWritesOriginalSnapshotOnceAfterActivityRecreation() =
        withRecreatedWorkspace { picker, _, destination, duplicate ->
            rule.runOnIdle {
                picker.deliver(Activity.RESULT_OK, destination)
                picker.deliver(Activity.RESULT_OK, duplicate)
            }
            waitForNotice("Exported 96 × 96.")
            val png = requireNotNull(resolver.openInputStream(destination)?.use { BitmapFactory.decodeStream(it) })
            try {
                assertEquals(96, png.width)
                assertEquals(96, png.height)
                assertEquals(Color.RED, png.getPixel(48, 48))
            } finally { png.recycle() }
            assertArrayEquals(SENTINEL, bytes(duplicate))
            assertNull(journal.read(ExportWorkspace.PRO))
        }

    @Test fun canceledDestinationClearsAfterRecreationAndKeepsExistingBytes() =
        withRecreatedWorkspace { picker, _, destination, _ ->
            rule.runOnIdle { picker.deliver(Activity.RESULT_CANCELED, null) }
            waitForNotice("Export canceled.")
            assertArrayEquals(SENTINEL, bytes(destination))
            assertNull(journal.read(ExportWorkspace.PRO))
        }

    @Test fun originalSourceCannotBeOverwrittenAfterRecreation() =
        withRecreatedWorkspace { picker, source, _, _ ->
            val original = bytes(source)
            rule.runOnIdle { picker.deliver(Activity.RESULT_OK, source) }
            waitForNotice("Export cannot overwrite an original image.")
            rule.waitUntil(15_000) { journal.read(ExportWorkspace.PRO)?.phase == PendingExportPhase.RETRY }
            assertArrayEquals(original, bytes(source))
        }

    private fun withRecreatedWorkspace(test: (DelayedDestination, Uri, Uri, Uri) -> Unit) {
        journal.read(ExportWorkspace.PRO)?.let { journal.discard(it) }
        val source = createMedia("gz-pro-recover-source.png", Color.RED)
        val otherProject = createMedia("gz-pro-recover-other.png", Color.BLUE)
        val destination = createMedia("gz-pro-recover-output.png")
        val duplicate = createMedia("gz-pro-recover-duplicate.png")
        val picker = DelayedDestination()
        fun document(uri: Uri) = ProDocument(
            layers = listOf(ProLayer(uri.toString(), name = "Project source")),
            canvasSize = ProPixelSize(96, 96)
        )
        try {
            rule.setContent { ProWorkspace(picker, document(source)) }
            rule.waitUntil(15_000) {
                rule.onAllNodesWithContentDescription("Pro editable composition").fetchSemanticsNodes().isNotEmpty()
            }
            rule.onNodeWithText("File ▾").performClick()
            rule.onNodeWithText("Export image").performClick()
            rule.onNodeWithText("JPEG · JPG  ▾").performClick()
            rule.onNodeWithText("PNG · Lossless").performClick()
            rule.onNodeWithText("Save PNG").performClick()
            rule.waitUntil(15_000) { picker.launches == 1 }
            val pending = requireNotNull(journal.read(ExportWorkspace.PRO))
            assertEquals(PendingExportPhase.PICKING, pending.phase)

            val oldActivity = rule.activity
            rule.activityRule.scenario.recreate()
            rule.activityRule.scenario.onActivity { activity ->
                assertNotSame(oldActivity, activity)
                activity.setContent { ProWorkspace(picker, document(otherProject)) }
            }
            waitForNotice("Resume export")
            assertEquals(1, picker.launches)
            assertEquals(pending.id, journal.read(ExportWorkspace.PRO)?.id)
            test(picker, source, destination, duplicate)
        } finally {
            journal.read(ExportWorkspace.PRO)?.let { journal.discard(it) }
            for (uri in listOf(source, otherProject, destination, duplicate)) resolver.delete(uri, null, null)
        }
    }

    private fun bytes(uri: Uri) = requireNotNull(resolver.openInputStream(uri)).use { it.readBytes() }

    private fun createMedia(name: String, color: Int? = null): Uri {
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        requireNotNull(resolver.openOutputStream(uri, "wt")).use { stream ->
            if (color == null) stream.write(SENTINEL) else {
                val bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
                try {
                    bitmap.eraseColor(color)
                    assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream))
                } finally { bitmap.recycle() }
            }
        }
        if (color != null) resolver.update(uri, ContentValues().apply {
            put(MediaStore.Images.Media.IS_PENDING, 0)
        }, null, null)
        return uri
    }

    private companion object {
        val SENTINEL = byteArrayOf(18, 52, 86, 120)
    }
}

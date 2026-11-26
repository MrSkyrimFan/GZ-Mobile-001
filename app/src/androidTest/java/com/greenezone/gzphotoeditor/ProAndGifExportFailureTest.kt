package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.ExportWorkspace
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.PendingExportJournal
import com.greenezone.gzphotoeditor.editor.ProDocument
import com.greenezone.gzphotoeditor.editor.ProLayer
import com.greenezone.gzphotoeditor.ui.GifMakerScreen
import com.greenezone.gzphotoeditor.ui.ProWorkspaceScreen
import org.junit.Assert.assertArrayEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProAndGifExportFailureTest {
    @get:Rule val rule = createComposeRule()

    private fun verify(
        workspace: ExportWorkspace,
        content: @Composable (Uri) -> Unit,
        previewError: String,
        failure: String,
        export: () -> Unit
    ) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val resolver = context.contentResolver
        val journal = PendingExportJournal(context.filesDir)
        journal.read(workspace)?.let { journal.discard(it) }
        fun create(name: String): Uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        val source = create("gz-unreadable-export-source.png")
        try {
            val output = create("gz-existing-document.png")
            try {
                val previous = byteArrayOf(42, 78, 12, 61, 127)
                resolver.openOutputStream(output, "wt")!!.use { it.write(previous) }
                val owner = object : ActivityResultRegistryOwner {
                    override val activityResultRegistry = object : ActivityResultRegistry() {
                        override fun <I, O> onLaunch(requestCode: Int, contract: ActivityResultContract<I, O>,
                            input: I, options: ActivityOptionsCompat?) {
                            check(contract is ActivityResultContracts.StartActivityForResult)
                            dispatchResult(requestCode, Activity.RESULT_OK, Intent().setData(output))
                        }
                    }
                }
                rule.setContent {
                    CompositionLocalProvider(LocalActivityResultRegistryOwner provides owner) { content(source) }
                }
                rule.waitUntil(15_000) {
                    rule.onAllNodesWithText(previewError, substring = true).fetchSemanticsNodes().isNotEmpty()
                }
                export()
                rule.waitUntil(30_000) {
                    rule.onAllNodesWithText(failure, substring = true).fetchSemanticsNodes().isNotEmpty()
                }
                assertArrayEquals(previous, resolver.openInputStream(output)!!.use { it.readBytes() })
            } finally { resolver.delete(output, null, null) }
        } finally {
            journal.read(workspace)?.let { journal.discard(it) }
            resolver.delete(source, null, null)
        }
    }

    @U1FinalProbe
    @Test fun unreadableProLayerPreservesDestination() = verify(
        workspace = ExportWorkspace.PRO,
        content = { source -> ProWorkspaceScreen(ProDocument(listOf(ProLayer(uri = source.toString()))), {}, {}, {}) },
        previewError = "Source for image layer 1 is unavailable",
        failure = "Export failed"
    ) {
        rule.onNodeWithText("Options").performClick()
        rule.onNodeWithText("Export image…").performClick()
        rule.onNodeWithText("Save JPG").performClick()
    }

    @Test fun unreadablePhotoSequenceFramePreservesDestination() = verify(
        workspace = ExportWorkspace.GIF,
        content = { source -> GifMakerScreen(GifDocument(frames = listOf(source.toString(), source.toString())),
            {}, {}, {}) },
        previewError = "Photo 1 unavailable",
        failure = "GIF export failed"
    ) {
        rule.onNodeWithText("Options").performClick()
        rule.onNodeWithText("Export GIF…").performClick()
    }
}


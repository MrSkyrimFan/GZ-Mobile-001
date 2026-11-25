package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.*
import com.greenezone.gzphotoeditor.ui.PhotoEditorScreen
import com.greenezone.gzphotoeditor.ui.PhotoExportController
import com.greenezone.gzphotoeditor.ui.PhotoExportRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.file.Files
import java.util.Base64

@RunWith(AndroidJUnit4::class)
class AndroidRegularSaveBudgetTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val resolver get() = context.contentResolver

    private fun command(label: String) {
        rule.onNodeWithText("Options", substring = false).performClick()
        rule.onNodeWithText(label, substring = false).performClick()
    }

    // Both float strings are long, but their alternating distance also exceeds
    // Regular Draw's real .000008f squared-distance filter.
    private fun points(): List<BrushPoint> = List(500) { index ->
        val a = .00012345678f
        val b = .0034567893f
        if (index % 2 == 0) BrushPoint(a, b) else BrushPoint(b, a)
    }

    private fun drawing(count: Int): EditDocument {
        val samples = points()
        return EditDocument(creative = CreativeDocument(
            strokes = List(count) { InkStroke(samples) }
        ))
    }

    private fun key(name: String): String = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(ProjectStore.normalizedName(name).toByteArray(Charsets.UTF_8))

    private fun stored(name: String): String? =
        context.getSharedPreferences("gzphotoeditor.projects.v1", Context.MODE_PRIVATE)
            .getString(key(name), null)

    private fun assertReachableDenseInput(document: EditDocument) {
        val stroke = document.creative.strokes.first()
        assertEquals(500, stroke.points.size)
        assertTrue(document.creative.strokes.size <= CreativeRaster.MAX_INK)
        stroke.points.zipWithNext().forEach { (a, b) ->
            val dx = a.x - b.x
            val dy = a.y - b.y
            assertTrue(dx * dx + dy * dy >= .000008f)
        }
        val coordinates = stroke.points.joinToString(";") { it.x.toString() + "," + it.y }
        assertTrue(coordinates.length.toLong() * document.creative.strokes.size > 2_000_000L)
    }

    @Test fun oversizedReplacementKeepsTheHealthyRecipeAndSmallerDrawingsStillReopen() {
        val name = "Reg budget " + System.nanoTime().toString().takeLast(10)
        val uri = Uri.parse("content://gz-budget/source")
        val healthy = EditDocument(grade = Grade(exposure = .5f))
        val dense = drawing(160)
        assertReachableDenseInput(dense)
        try {
            assertTrue(ProjectStore.save(context, name, uri, healthy))
            val before = requireNotNull(stored(name))
            assertFalse("Unreadable replacement was reported saved",
                ProjectStore.save(context, name, uri, dense))
            assertEquals("Rejected replacement changed the existing bytes", before, stored(name))
            val reopened = ProjectStore.load(context).single { it.name == name }
            assertEquals(uri, reopened.image)
            assertEquals(healthy, reopened.document)

            val readable = drawing(20)
            assertTrue(ProjectStore.save(context, name, uri, readable))
            assertEquals(readable, ProjectStore.load(context).single { it.name == name }.document)
        } finally {
            ProjectStore.delete(context, name)
        }
    }

    @Test fun oversizedExportIsRejectedBeforePickerAndAReadableRequestStillRestores() {
        val root = Files.createTempDirectory(context.cacheDir.toPath(), "regular-export-budget-").toFile()
        val journal = PendingExportJournal(root)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        val notices = mutableListOf<String>()
        val controller = PhotoExportController(context, scope, journal, false, notices::add)
        val source = Uri.parse("content://gz-budget/source")
        try {
            controller.request(source, drawing(160))
            assertNull("An unreadable recipe reached the picker", controller.launchId)
            assertNull(controller.pending)
            assertNull(journal.read(ExportWorkspace.PHOTO))
            assertTrue(notices.any { it.startsWith("Could not prepare export") })

            val readable = drawing(20)
            controller.request(source, readable)
            val pending = requireNotNull(journal.read(ExportWorkspace.PHOTO))
            assertEquals(pending.id, controller.launchId)
            val restored = requireNotNull(PhotoExportRequest.decode(pending))
            assertEquals(readable, restored.edits)
            assertEquals(source, restored.source)
        } finally {
            scope.cancel()
            root.deleteRecursively()
        }
    }

    // Count the exact ink-only wire representation without building a second
    // multi-megabyte String in the API 29 test process.
    private fun nearBudgetDrawing(): EditDocument {
        val samples = points()
        val pairLength = samples.first().let { (it.x.toString() + "," + it.y).length }
        assertTrue(samples.all { (it.x.toString() + "," + it.y).length == pairLength })
        val target = 2_000_000 - 700
        var length = EditDocumentCodec.encode(EditDocument()).length + 2 // Three-digit stroke count.
        val strokes = mutableListOf<InkStroke>()
        repeat(160) { index ->
            val stroke = InkStroke(samples)
            val prefix = "\ncreative.ink." + index + "."
            val fields = prefix + "color=" + stroke.color +
                prefix + "opacity=" + stroke.opacity +
                prefix + "width=" + stroke.thickness +
                prefix + "erase=" + stroke.erase +
                prefix + "points="
            val count = minOf(500, (target - length - fields.length + 1) / (pairLength + 1))
            if (count > 0) {
                strokes.add(stroke.copy(points = if (count == samples.size) samples else samples.take(count)))
                length += fields.length + count * (pairLength + 1) - 1
            }
        }
        assertTrue(strokes.size in 100..160)
        assertTrue(length in (target - 30)..target)
        val captionSize = EditDocumentCodec.encode(EditDocument(creative = CreativeDocument(
            text = listOf(TextOverlay(content = "Z".repeat(500)))
        ))).length - EditDocumentCodec.encode(EditDocument()).length
        assertTrue("The UI caption must cross the reader budget", length + captionSize > 2_000_000)
        return EditDocument(creative = CreativeDocument(strokes = strokes))
    }

    @Test fun failedSaveBeforeCloseAndReplacementKeepsTheSameDirtyDraft() {
        val name = "Reg UI budget " + System.nanoTime().toString().takeLast(9)
        val source = createPhoto("regular-budget-source", Color.RED)
        val replacement = createPhoto("regular-budget-replacement", Color.BLUE)
        val mounted = mutableStateOf(true)
        val journal = PendingExportJournal(context.filesDir)
        journal.read(ExportWorkspace.PHOTO)?.let(journal::discard)
        val original = nearBudgetDrawing()
        try {
            assertTrue("The below-budget starting project must be readable",
                ProjectStore.save(context, name, source, original))
            val healthy = requireNotNull(stored(name))
            assertTrue(healthy.length - healthy.indexOf('\n') - 1 < 2_000_000)
            var opens = 0
            val picker = object : ActivityResultRegistryOwner {
                override val activityResultRegistry = object : ActivityResultRegistry() {
                    override fun <I, O> onLaunch(requestCode: Int, contract: ActivityResultContract<I, O>,
                        input: I, options: ActivityOptionsCompat?) {
                        check(contract is ActivityResultContracts.PickVisualMedia)
                        val result = if (opens++ == 0) source else replacement
                        dispatchResult(requestCode, Activity.RESULT_OK, Intent().setData(result))
                    }
                }
            }
            rule.setContent {
                if (mounted.value) {
                    CompositionLocalProvider(LocalActivityResultRegistryOwner provides picker) {
                        MaterialTheme(colorScheme = darkColorScheme()) { PhotoEditorScreen() }
                    }
                }
            }
            command("New photo…")
            waitForPhoto()
            command("Open project…")
            rule.onNodeWithText(name).performClick()
            rule.onNodeWithText("Open project").performClick()
            rule.waitUntil(20_000) {
                rule.onAllNodesWithText("Project restored").fetchSemanticsNodes().isNotEmpty()
            }
            rule.onNodeWithTag("photo-tools").performScrollToNode(hasText("Text"))
            rule.onNodeWithText("Text").performClick()
            rule.onNodeWithText("+ Add text").performClick()
            rule.onNode(hasSetTextAction()).performTextInput("Z".repeat(500))
            rule.onNodeWithText("Apply").performClick()
            rule.onNodeWithText("Text 1").assertIsDisplayed()
            rule.onNodeWithContentDescription("Undo").assertIsEnabled()

            // Real Back asks to save, then a failed Save must not close the photo.
            rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
            rule.onNodeWithText("Unsaved changes").assertIsDisplayed()
            rule.onNodeWithText("Save", substring = false).performClick()
            rejectSaveAndKeepEditing(name, healthy)

            // The departure guard now runs before the picker. A failed Save must not launch it.
            command("New photo…")
            rule.onNodeWithText("Unsaved changes").assertIsDisplayed()
            rule.onNodeWithText("Save", substring = false).performClick()
            rejectSaveAndKeepEditing(name, healthy)
            assertEquals("Failed Save must not launch the replacement picker", 1, opens)
            rule.onNodeWithText("Before").performClick()
            val image = rule.onNodeWithContentDescription(
                "Photo editing canvas. Tap to hide or show tools."
            ).captureToImage().asAndroidBitmap()
            try {
                assertEquals("The replacement image displaced the dirty source",
                    Color.RED, image.getPixel(image.width / 2, image.height / 2))
            } finally { image.recycle() }
            rule.onNodeWithText("Edited").performClick()
            rule.onNodeWithText("Text 1").assertIsDisplayed()

            // Dirty status is still present after both failed attempts.
            rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
            rule.onNodeWithText("Unsaved changes").assertIsDisplayed()
            rule.onNodeWithText("Cancel").performClick()
            assertEquals(healthy, stored(name))
            // Release the live editor and its decoded stroke graph before
            // independently reopening the retained project on the small API 29 heap.
            rule.runOnIdle { mounted.value = false }
            rule.waitForIdle()
            assertEquals(original, ProjectStore.load(context).single { it.name == name }.document)
        } finally {
            rule.runOnIdle { mounted.value = false }
            rule.waitForIdle()
            ProjectStore.delete(context, name)
            journal.read(ExportWorkspace.PHOTO)?.let(journal::discard)
            resolver.delete(source, null, null)
            resolver.delete(replacement, null, null)
        }
    }

    private fun rejectSaveAndKeepEditing(name: String, healthy: String) {
        rule.waitUntil(20_000) {
            rule.onAllNodesWithText("Project was not saved").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Project was not saved").assertIsDisplayed()
        rule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        rule.onNodeWithText("Could not save project", substring = true).assertIsDisplayed()
        assertEquals(healthy, stored(name))
        rule.onNodeWithText("Cancel").performClick()
        rule.onNodeWithText("Text 1").assertIsDisplayed()
        rule.onNodeWithContentDescription("Undo").assertIsEnabled()
    }

    private fun waitForPhoto() {
        rule.waitUntil(15_000) {
            rule.onAllNodesWithTag("photo-tools").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun createPhoto(label: String, color: Int): Uri {
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, label + ".png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
        try {
            resolver.openOutputStream(uri, "wt")!!.use {
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
            resolver.update(uri, ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }, null, null)
            return uri
        } finally { bitmap.recycle() }
    }
}

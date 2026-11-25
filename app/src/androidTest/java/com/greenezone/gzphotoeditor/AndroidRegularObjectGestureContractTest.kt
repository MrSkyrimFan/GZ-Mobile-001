package com.greenezone.gzphotoeditor

import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.*
import com.greenezone.gzphotoeditor.ui.PhotoEditorScreen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.file.Files
import kotlin.math.min

/** Real pointer sequences cross Compose frames, cancellation, and native save/reload. */
@RunWith(AndroidJUnit4::class)
class AndroidRegularObjectGestureContractTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private class IsolatedContext(base: Context) : ContextWrapper(base) {
        private val suffix = ".b1-gesture-" + System.nanoTime()
        private val directory = Files.createTempDirectory(base.cacheDir.toPath(), "b1-gesture-").toFile()
        private val preferences = mutableSetOf<String>()
        override fun getFilesDir(): File = directory
        override fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
            preferences += name + suffix
            return baseContext.getSharedPreferences(name + suffix, mode)
        }
        fun close() {
            preferences.forEach { baseContext.deleteSharedPreferences(it) }
            directory.deleteRecursively()
        }
    }

    private data class Fixture(val context: IsolatedContext, val source: Uri, val original: ByteArray)

    private fun withPhoto(document: EditDocument, test: (Fixture) -> Unit) {
        val context = IsolatedContext(rule.activity)
        val resolver = context.contentResolver
        val source = requireNotNull(resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "gz-b1-gesture-" + System.nanoTime() + ".png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        ))
        val mounted = mutableStateOf(true)
        var contentSet = false
        try {
            val bitmap = sourceBitmap()
            try {
                resolver.openOutputStream(source, "wt")!!.use {
                    assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
                }
            } finally { bitmap.recycle() }
            resolver.update(source, ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }, null, null)
            val original = resolver.openInputStream(source)!!.use { it.readBytes() }
            rule.setContent {
                CompositionLocalProvider(LocalContext provides context) {
                    MaterialTheme(colorScheme = darkColorScheme()) {
                        if (mounted.value) PhotoEditorScreen(
                            initialCollagePhoto = CollagePhoto(
                                source.toString(), edits = document, id = "b1-held-object"
                            ),
                            onDoneCollagePhoto = { error("A canceled gesture must not apply the tile") },
                            onCancelCollagePhoto = { error("Back must cancel the held gesture first") }
                        )
                    }
                }
            }
            contentSet = true
            rule.waitUntil(15_000) {
                rule.onAllNodesWithTag("photo-tools").fetchSemanticsNodes().isNotEmpty() &&
                    rule.onAllNodesWithText("Processing…").fetchSemanticsNodes().isEmpty()
            }
            test(Fixture(context, source, original))
            assertArrayEquals(original, resolver.openInputStream(source)!!.use { it.readBytes() })
        } finally {
            if (contentSet) {
                rule.runOnIdle { mounted.value = false }
                rule.waitForIdle()
            }
            resolver.delete(source, null, null)
            context.close()
        }
    }

    private fun sourceBitmap(): Bitmap = Bitmap.createBitmap(256, 128, Bitmap.Config.ARGB_8888).apply {
        val pixels = IntArray(width * height) { index ->
            val x = index % width
            val y = index / width
            android.graphics.Color.rgb(x, y * 2, (x + y) % 256)
        }
        setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun textDocument() = EditDocument(creative = CreativeDocument(
        text = listOf(TextOverlay(content = "Held text", width = .5f, height = .5f))
    ))

    private fun tab(name: String) {
        rule.onNodeWithTag("photo-tools").performScrollToNode(hasText(name))
        rule.onNodeWithText(name, substring = false).performClick()
    }

    private fun canvas() = rule.onNodeWithContentDescription(
        "Photo editing canvas. Tap to hide or show tools."
    )

    private data class Fitted(val left: Float, val top: Float, val width: Float, val height: Float) {
        fun at(x: Float, y: Float) = Offset(left + x * width, top + y * height)
    }

    private fun fitted(node: SemanticsNodeInteraction, crop: Boolean = false): Fitted {
        val size = node.fetchSemanticsNode().size
        val inset = if (crop) 16f * rule.activity.resources.displayMetrics.density else 0f
        val scale = min((size.width - inset * 2f) / 256f, (size.height - inset * 2f) / 128f)
        val width = 256f * scale
        val height = 128f * scale
        return Fitted((size.width - width) / 2f, (size.height - height) / 2f, width, height)
    }

    private fun back() {
        rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    private fun save(fixture: Fixture): EditDocument {
        val name = "Held " + System.nanoTime()
        rule.onNodeWithText("Options", substring = false).performClick()
        rule.onNodeWithText("Save photo project as…", substring = false).performClick()
        rule.onNode(hasSetTextAction()).performTextReplacement(name)
        rule.onNodeWithText("Save", substring = false).performClick()
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText("Save photo project", substring = false).fetchSemanticsNodes().isEmpty()
        }
        val saved = requireNotNull(ProjectStore.loadNamed(fixture.context, name))
        assertEquals(fixture.source, saved.content.image)
        return saved.content.document
    }

    private fun assertPixelsEqual(expected: EditDocument, actual: EditDocument) {
        val source = sourceBitmap()
        val wanted = ImagePipeline.render(source, expected)
        val observed = ImagePipeline.render(source, actual)
        try {
            assertEquals(wanted.width, observed.width)
            assertEquals(wanted.height, observed.height)
            val expectedPixels = IntArray(wanted.width * wanted.height)
            val actualPixels = IntArray(observed.width * observed.height)
            wanted.getPixels(expectedPixels, 0, wanted.width, 0, 0, wanted.width, wanted.height)
            observed.getPixels(actualPixels, 0, observed.width, 0, 0, observed.width, observed.height)
            assertArrayEquals(expectedPixels, actualPixels)
        } finally {
            if (wanted !== source) wanted.recycle()
            if (observed !== source && observed !== wanted) observed.recycle()
            source.recycle()
        }
    }

    private fun assertCanceled(fixture: Fixture, original: EditDocument) {
        rule.onNodeWithContentDescription("Undo").assertIsNotEnabled()
        rule.onNodeWithContentDescription("Redo").assertIsNotEnabled()
        rule.onAllNodesWithText("Apply tile changes?").assertCountEquals(0)
        val saved = save(fixture)
        assertEquals(original, saved)
        assertPixelsEqual(original, saved)
    }

    private fun canceledByBack(document: EditDocument, tool: String, crop: Boolean = false) {
        withPhoto(document) { fixture ->
            tab(tool)
            val node = when {
                crop -> rule.onNodeWithTag("crop-frame")
                tool == "Blur & cover" -> canvas()
                else -> rule.onNodeWithTag("editable-object")
            }
            val frame = fitted(node, crop)
            val start = if (crop) frame.at(.8f, .5f) else frame.at(.5f, .5f)
            val held = if (crop) frame.at(.65f, .5f) else frame.at(.65f, .55f)
            val late = if (crop) frame.at(.55f, .5f) else frame.at(.75f, .6f)
            node.performTouchInput { down(start); moveTo(held) }
            rule.waitForIdle()
            rule.mainClock.advanceTimeBy(80)
            back()
            node.performTouchInput { moveTo(late); up() }
            rule.waitForIdle()
            assertCanceled(fixture, document)
        }
    }

    @Test fun heldTextBackThenLateMoveAndUpRestoresExactDocumentWithoutHistory() =
        canceledByBack(textDocument(), "Text")

    @Test fun heldShapeBackThenLateMoveAndUpRestoresExactDocumentWithoutHistory() =
        canceledByBack(EditDocument(creative = CreativeDocument(shapes = listOf(
            ShapeOverlay(width = .5f, height = .5f, filled = true)
        ))), "Shapes")

    @Test fun heldPrivacyRegionBackThenLateMoveAndUpRestoresExactDocumentWithoutHistory() =
        canceledByBack(EditDocument(privacy = listOf(
            PrivacyEdit(kind = PrivacyKind.BLACK_BAR, width = .5f, height = .5f)
        )), "Blur & cover")

    @Test fun heldCropBackThenLateMoveAndUpRestoresExactDocumentWithoutHistory() =
        canceledByBack(EditDocument(
            crop = CropRatio.FREE, cropFrame = CropFrame(.2f, .2f, .8f, .8f)
        ), "Crop", crop = true)

    @Test fun heldTextFontActionCancelsWithoutCommittingHalfDrag() {
        val document = textDocument()
        withPhoto(document) { fixture ->
            tab("Text")
            // Inject through the persistent image node; hit testing routes to the object overlay.
            val node = canvas()
            val frame = fitted(node)
            node.performTouchInput { down(frame.at(.5f, .5f)); moveTo(frame.at(.65f, .55f)) }
            rule.waitForIdle()
            rule.onNodeWithText("Classic", substring = false).performClick()
            node.performTouchInput { moveTo(frame.at(.75f, .6f)); up() }
            rule.waitForIdle()
            assertCanceled(fixture, document)
        }
    }

    @Test fun heldTextActivityStopCancelsBeforeLateMoveAndUp() {
        val document = textDocument()
        withPhoto(document) { fixture ->
            tab("Text")
            val node = canvas()
            val frame = fitted(node)
            node.performTouchInput { down(frame.at(.5f, .5f)); moveTo(frame.at(.65f, .55f)) }
            rule.waitForIdle()
            rule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
            rule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
            rule.waitForIdle()
            node.performTouchInput { moveTo(frame.at(.75f, .6f)); up() }
            rule.waitForIdle()
            assertCanceled(fixture, document)
        }
    }

    @Test fun completedTextDragCreatesOneCheckpointAndNativeSavePreservesCoordinates() {
        val document = textDocument()
        withPhoto(document) { fixture ->
            tab("Text")
            val node = rule.onNodeWithTag("editable-object")
            val frame = fitted(node)
            node.performTouchInput { down(frame.at(.5f, .5f)); moveTo(frame.at(.65f, .55f)) }
            rule.waitForIdle()
            node.performTouchInput { up() }
            rule.waitForIdle()
            rule.onNodeWithContentDescription("Undo").assertIsEnabled()
            val moved = save(fixture)
            assertEquals(.65f, moved.creative.text.single().x, .005f)
            assertEquals(.55f, moved.creative.text.single().y, .005f)
            assertEquals(document.creative.text.single().width, moved.creative.text.single().width, 0f)
            assertEquals(document.creative.text.single().height, moved.creative.text.single().height, 0f)
            assertNotEquals(document, moved)
            rule.onNodeWithContentDescription("Undo").performClick()
            rule.onNodeWithContentDescription("Undo").assertIsNotEnabled()
            rule.onNodeWithContentDescription("Redo").assertIsEnabled()
            val undone = save(fixture)
            assertEquals(document, undone)
            assertPixelsEqual(document, undone)
            rule.onNodeWithContentDescription("Redo").performClick()
            rule.onNodeWithContentDescription("Redo").assertIsNotEnabled()
            assertEquals(moved, save(fixture))
        }
    }
}

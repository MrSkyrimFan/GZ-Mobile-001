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
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.*
import com.greenezone.gzphotoeditor.ui.CollageWorkspaceScreen
import com.greenezone.gzphotoeditor.ui.PhotoEditorScreen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.min

/** Real controls, pointer input, document history, named stores and decoded PNGs. */
@RunWith(AndroidJUnit4::class)
class AndroidRegularEraseInteractionTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val resolver get() = context.contentResolver
    private val baseColor = Color.rgb(20, 70, 110)
    private val names = mutableListOf<String>()
    private var destination: Uri? = null
    private var observedCollage = CollageDocument()
    private var activePhotoName: String? = null
    private val limitNotice = "Erasing would exceed the drawing limit. Your drawing is unchanged."

    private fun media(label: String): Uri = requireNotNull(resolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, label + System.nanoTime() + ".png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    ))

    private fun fixture(
        initial: EditDocument = EditDocument(), collage: Boolean = false,
        run: (String) -> Unit
    ) {
        val source = media("gz-erase-source-")
        val output = media("gz-erase-output-")
        destination = output
        val mounted = mutableStateOf(true)
        val projectName = "Erase fixture " + System.nanoTime().toString().takeLast(10)
        names += projectName
        val parent = mutableStateOf(CollageDocument())
        var contentSet = false
        try {
            val bitmap = Bitmap.createBitmap(256, 128, Bitmap.Config.ARGB_8888)
            try {
                bitmap.eraseColor(baseColor)
                resolver.openOutputStream(source, "wt")!!.use {
                    assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
                }
            } finally { bitmap.recycle() }
            resolver.update(source, ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }, null, null)
            val sourceBytes = resolver.openInputStream(source)!!.use { it.readBytes() }
            assertTrue(ProjectStore.save(context, projectName, source, initial))
            // Same source URI, independent editable documents and stable tile IDs.
            val neighbor = CollagePhoto(source.toString(), EditDocument(creative = CreativeDocument(
                strokes = listOf(InkStroke(listOf(BrushPoint(.1f, .75f), BrushPoint(.9f, .75f)),
                    color = Color.YELLOW, thickness = .05f))
            )), id = "erase-neighbor")
            parent.value = CollageDocument(listOf(
                CollagePhoto(source.toString(), initial, id = "erase-selected"), neighbor
            ), CollageLayout(template = CollageTemplate.STACKED, gap = 0f, margin = 0f,
                cornerRadius = 0f, quality = CollageQuality.STANDARD))
            observedCollage = parent.value
            val picker = object : ActivityResultRegistryOwner {
                override val activityResultRegistry = object : ActivityResultRegistry() {
                    override fun <I, O> onLaunch(requestCode: Int, contract: ActivityResultContract<I, O>,
                        input: I, options: ActivityOptionsCompat?) {
                        val result = when (contract) {
                            is ActivityResultContracts.PickVisualMedia -> source
                            is ActivityResultContracts.StartActivityForResult -> {
                                val intent = input as Intent
                                assertEquals(Intent.ACTION_CREATE_DOCUMENT, intent.action)
                                assertEquals("image/png", intent.type)
                                output
                            }
                            else -> error("Unexpected picker contract")
                        }
                        dispatchResult(requestCode, Activity.RESULT_OK, Intent().setData(result))
                    }
                }
            }
            rule.setContent {
                CompositionLocalProvider(LocalActivityResultRegistryOwner provides picker) {
                    MaterialTheme(colorScheme = darkColorScheme()) {
                        if (mounted.value) {
                            if (collage) CollageWorkspaceScreen(parent.value, {
                                parent.value = it
                                observedCollage = it
                            }, {}) else PhotoEditorScreen()
                        }
                    }
                }
            }
            contentSet = true
            if (collage) {
                waitForCollage()
                editSelectedTile()
            } else {
                command("New photo…")
                waitForPhoto()
                reopenPhoto(projectName)
            }
            run(projectName)
            assertArrayEquals("Editing changed the source file", sourceBytes,
                resolver.openInputStream(source)!!.use { it.readBytes() })
        } finally {
            if (contentSet) {
                rule.runOnIdle { mounted.value = false }
                rule.waitForIdle()
            }
            names.forEach { ProjectStore.delete(context, it); CollageStore.delete(context, it) }
            val journal = PendingExportJournal(context.filesDir)
            listOf(ExportWorkspace.PHOTO, ExportWorkspace.COLLAGE).forEach { workspace ->
                journal.read(workspace)?.let(journal::discard)
            }
            resolver.delete(source, null, null)
            resolver.delete(output, null, null)
        }
    }

    private fun waitForPhoto() = rule.waitUntil(15_000) {
        rule.onAllNodesWithTag("photo-tools").fetchSemanticsNodes().isNotEmpty()
    }

    private fun waitForCollage() = rule.waitUntil(15_000) {
        rule.onAllNodesWithContentDescription("Photo collage preview").fetchSemanticsNodes().isNotEmpty()
    }

    private fun canvas() = rule.onNodeWithContentDescription(
        "Photo editing canvas. Tap to hide or show tools."
    )

    private fun ensureToolsVisible() {
        if (rule.onAllNodesWithTag("photo-tools").fetchSemanticsNodes().isEmpty()) canvas().performClick()
        rule.onNodeWithTag("photo-tools").assertIsDisplayed()
    }

    private fun command(label: String) {
        rule.onNodeWithText("Options", substring = false).performClick()
        rule.onNodeWithText(label, substring = false).performClick()
    }

    private fun tab(name: String) {
        ensureToolsVisible()
        rule.onNodeWithTag("photo-tools").performScrollToNode(hasText(name))
        rule.onNodeWithText(name, substring = false).performClick()
    }

    private fun ink(erase: Boolean, thickness: Float = .05f) {
        tab("Draw")
        rule.onNodeWithText(if (erase) "Eraser" else "Brush", substring = false).performClick()
        rule.onNodeWithContentDescription("Adjust Brush thickness")
            .performSemanticsAction(SemanticsActions.SetProgress) { it(thickness) }
        canvas().performClick()
        rule.onNodeWithTag("photo-tools").assertDoesNotExist()
    }

    private data class Fitted(val left: Float, val top: Float, val width: Float, val height: Float) {
        fun at(x: Float, y: Float) = Offset(left + width * x, top + height * y)
    }

    private fun fitted(node: SemanticsNodeInteraction): Fitted {
        val size = node.fetchSemanticsNode().size
        val scale = min(size.width / 256f, size.height / 128f)
        val width = scale * 256f
        val height = scale * 128f
        return Fitted((size.width - width) / 2f, (size.height - height) / 2f, width, height)
    }

    private fun savePhoto(name: String): SavedProject {
        ensureToolsVisible()
        if (activePhotoName == name) {
            command("Save project")
        } else {
            command("Save project as…")
            rule.onNode(hasSetTextAction()).performTextInput(name)
            rule.onNodeWithText("Save", substring = false).performClick()
        }
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText("Save editable project").fetchSemanticsNodes().isEmpty() &&
                rule.onAllNodesWithText("Processing…", substring = false).fetchSemanticsNodes().isEmpty()
        }
        activePhotoName = name
        return ProjectStore.load(context).single { it.name == name }
    }

    private fun reopenPhoto(name: String) {
        ensureToolsVisible()
        command("Open project…")
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText(name, substring = false).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText(name, substring = false).performClick()
        rule.onNodeWithText("Open project", substring = false).performClick()
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText("Project restored").fetchSemanticsNodes().isNotEmpty()
        }
        activePhotoName = name
    }

    private fun exportPng(collage: Boolean = false, inspect: (Bitmap) -> Unit) {
        command("Export image…")
        if (rule.onAllNodesWithText("JPEG · JPG", substring = true).fetchSemanticsNodes().isNotEmpty()) {
            rule.onNodeWithText("JPEG · JPG", substring = true).performClick()
            rule.onNodeWithText("PNG · Lossless", substring = false).performClick()
        }
        rule.onNodeWithText("Save PNG", substring = false).performClick()
        rule.waitUntil(30_000) {
            rule.onAllNodesWithText(if (collage) "Collage saved" else "Saved ", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        val output = resolver.openInputStream(requireNotNull(destination))!!.use {
            requireNotNull(BitmapFactory.decodeStream(it))
        }
        try { inspect(output) } finally { output.recycle() }
    }

    private fun capacityDocument(count: Int): EditDocument {
        val strokes = mutableListOf<InkStroke>()
        for (y in listOf(.28f, .45f)) {
            strokes += InkStroke(listOf(BrushPoint(.15f, y), BrushPoint(.35f, y), BrushPoint(.55f, y)),
                color = Color.WHITE, thickness = .04f)
        }
        repeat(count - 3) { index ->
            val y = .80f + index * .0005f
            strokes += InkStroke(listOf(BrushPoint(.05f, y), BrushPoint(.08f, y)),
                color = Color.GREEN, thickness = .002f)
        }
        strokes += InkStroke(listOf(BrushPoint(.7f, .68f), BrushPoint(.9f, .68f)),
            color = Color.RED, thickness = .06f)
        return EditDocument(creative = CreativeDocument(strokes = strokes))
    }

    private fun assertCapacityPixels(bitmap: Bitmap) {
        assertEquals(256, bitmap.width)
        assertEquals(128, bitmap.height)
        assertEquals("Rejected erase changed the first stroke", Color.WHITE, bitmap.getPixel(90, 36))
        assertEquals("Unrelated last stroke was omitted", Color.RED, bitmap.getPixel(205, 87))
    }

    @Test fun firstEraseAtCapacityKeepsUntouchedLastStrokeHistoryReopenAndPng() {
        val original = capacityDocument(160)
        fixture(original) { name ->
            ink(erase = true, thickness = .04f)
            val target = canvas()
            val image = fitted(target)
            target.performTouchInput {
                down(image.at(.35f, .28f))
                moveTo(image.at(.39f, .30f))
                up()
            }
            rule.waitForIdle()
            val noticeShown = rule.onAllNodesWithText(limitNotice).fetchSemanticsNodes().isNotEmpty()
            ensureToolsVisible()
            val saved = savePhoto(name)
            assertEquals("Erasing dropped the last unrelated stroke", original.creative.strokes.last(),
                saved.document.creative.strokes.last())
            assertEquals("A refused gesture must preserve the complete document", original, saved.document)
            assertTrue("A capacity refusal needs visible feedback", noticeShown)
            rule.onNodeWithContentDescription("Undo").assertIsNotEnabled()
            reopenPhoto(name)
            exportPng(inspect = ::assertCapacityPixels)
        }
    }

    @Test fun laterCapacityRefusalRestoresEarlierEraseAndKeepsDirtyTextAndUndo() {
        val original = capacityDocument(159)
        fixture(original) { name ->
            tab("Text")
            rule.onNodeWithText("+ Add text").performClick()
            rule.onNode(hasSetTextAction()).performTextInput("Keep this draft")
            rule.onNodeWithText("Apply", substring = false).performClick()
            ink(erase = true, thickness = .04f)
            val target = canvas()
            val image = fitted(target)
            target.performTouchInput {
                down(image.at(.35f, .28f))
                moveTo(image.at(.35f, .34f))
            }
            // The first split fits (159 -> 160). A later split must roll back
            // this whole gesture, rather than preserving a partially erased draft.
            rule.waitForIdle()
            target.performTouchInput { moveTo(image.at(.35f, .45f)); up() }
            rule.waitForIdle()
            val noticeShown = rule.onAllNodesWithText(limitNotice).fetchSemanticsNodes().isNotEmpty()
            ensureToolsVisible()
            rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
            rule.onNodeWithText("Unsaved changes").assertIsDisplayed()
            rule.onNodeWithText("Cancel", substring = false).performClick()
            val saved = savePhoto(name)
            assertEquals("Later overflow retained a partial erase", original.creative.strokes,
                saved.document.creative.strokes)
            assertEquals("Keep this draft", saved.document.creative.text.single().content)
            assertTrue("A later capacity refusal needs visible feedback", noticeShown)
            rule.onNodeWithContentDescription("Undo").performClick()
            assertEquals("Rejected erase inserted an Undo checkpoint", original, savePhoto(name).document)
            rule.onNodeWithContentDescription("Redo").performClick()
            assertEquals(saved.document, savePhoto(name).document)
            reopenPhoto(name)
            // Caption is central; the first stroke and last red sentinel remain clear.
            exportPng(inspect = ::assertCapacityPixels)
        }
    }

    private fun drawLongLine() {
        ink(erase = false)
        val target = canvas()
        val image = fitted(target)
        target.performTouchInput {
            down(image.at(.1f, .5f))
            for (i in 1..1000) moveTo(image.at(.1f + .8f * i / 1000f, .5f))
            up()
        }
        rule.waitForIdle()
        ensureToolsVisible()
    }

    private fun eraseLongLine(cancelGesture: Boolean = false) {
        ink(erase = true)
        val target = canvas()
        val image = fitted(target)
        target.performTouchInput {
            down(image.at(.3f, .35f))
            for (i in 1..100) moveTo(image.at(.3f, .35f + .3f * i / 100f))
            if (!cancelGesture) up()
        }
        rule.waitForIdle()
        if (cancelGesture) {
            target.performTouchInput { cancel() }
            rule.waitForIdle()
        }
        ensureToolsVisible()
    }

    private fun assertErasedPixels(bitmap: Bitmap, collage: Boolean = false) {
        val y = if (collage) bitmap.height / 4 else bitmap.height / 2
        assertEquals("The visible middle was not erased", baseColor,
            bitmap.getPixel((bitmap.width * .3f).toInt(), y))
        assertEquals("Erase removed the unrelated left section", Color.WHITE,
            bitmap.getPixel((bitmap.width * .18f).toInt(), y))
        assertEquals("Erase removed the unrelated right section", Color.WHITE,
            bitmap.getPixel((bitmap.width * .7f).toInt(), y))
    }

    private fun waitForVisibleErasedGap() {
        rule.waitUntil(15_000) {
            val node = canvas()
            val image = fitted(node)
            val bitmap = node.captureToImage().asAndroidBitmap()
            try {
                val probe = image.at(.3f, .5f)
                val left = image.at(.18f, .5f)
                val right = image.at(.7f, .5f)
                bitmap.getPixel(probe.x.toInt(), probe.y.toInt()) == baseColor &&
                    bitmap.getPixel(left.x.toInt(), left.y.toInt()) == Color.WHITE &&
                    bitmap.getPixel(right.x.toInt(), right.y.toInt()) == Color.WHITE
            } finally { bitmap.recycle() }
        }
    }

    @Test fun longRegularLineCanBeErasedUndoneReopenedAndExported() = fixture { name ->
        drawLongLine()
        val before = savePhoto(name)
        assertEquals(500, before.document.creative.strokes.single().points.size)
        eraseLongLine()
        waitForVisibleErasedGap()
        val after = savePhoto(name)
        assertNotEquals("Committed Eraser left the long stroke unchanged", before.document, after.document)
        assertTrue(after.document.creative.strokes.size in 1..160)
        assertTrue(after.document.creative.strokes.all { it.points.size <= 500 })
        rule.onNodeWithContentDescription("Undo").performClick()
        assertEquals(before.document, savePhoto(name).document)
        rule.onNodeWithContentDescription("Redo").performClick()
        assertEquals(after.document, savePhoto(name).document)
        reopenPhoto(name)
        exportPng { bitmap ->
            assertEquals(256, bitmap.width)
            assertEquals(128, bitmap.height)
            assertErasedPixels(bitmap)
        }
    }

    private fun editSelectedTile() {
        rule.onNodeWithText("Photos", substring = false).performClick()
        rule.onNodeWithText("Photo 1", substring = false).performClick()
        rule.onNodeWithTag("collage-tile-actions").performScrollToNode(hasText("Edit selected"))
        rule.onNodeWithText("Edit selected", substring = false).performClick()
        waitForPhoto()
    }

    private fun finishTile() {
        ensureToolsVisible()
        rule.onNodeWithText("Done", substring = false).performClick()
        waitForCollage()
    }

    private fun saveAndReopenCollage(expected: CollageDocument) {
        val name = "Erase parent " + System.nanoTime().toString().takeLast(10)
        names += name
        command("Save project as…")
        rule.onNode(hasSetTextAction()).performTextInput(name)
        rule.onNodeWithText("Save", substring = false).performClick()
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText("Collage project saved").fetchSemanticsNodes().isNotEmpty()
        }
        assertEquals(expected, CollageStore.load(context, name))
        command("Open project…")
        rule.onNodeWithText(name, substring = false).performClick()
        rule.onNodeWithText("Open", substring = false).performClick()
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText("Saved collage opened").fetchSemanticsNodes().isNotEmpty()
        }
        rule.runOnIdle { assertEquals(expected, observedCollage) }
    }

    @Test fun selectedTileCapacityRefusalPreservesParentHistoryReopenAndExport() {
        fixture(capacityDocument(160), collage = true) {
            val originalParent = rule.runOnIdle { observedCollage }
            ink(erase = true, thickness = .04f)
            val target = canvas()
            val image = fitted(target)
            target.performTouchInput {
                down(image.at(.35f, .28f))
                moveTo(image.at(.39f, .30f))
                up()
            }
            rule.waitForIdle()
            val noticeShown = rule.onAllNodesWithText(limitNotice).fetchSemanticsNodes().isNotEmpty()
            finishTile()
            rule.runOnIdle {
                assertEquals("Rejected tile erase changed the selected document or its sibling",
                    originalParent, observedCollage)
            }
            assertTrue("Tile capacity refusal needs visible feedback", noticeShown)
            rule.onNodeWithContentDescription("Undo").assertIsNotEnabled()
            saveAndReopenCollage(originalParent)
            exportPng(collage = true) { bitmap ->
                assertEquals(1600, bitmap.width)
                assertEquals(1600, bitmap.height)
                assertEquals("Rejected tile erase changed the first stroke", Color.WHITE,
                    bitmap.getPixel((bitmap.width * .35f).toInt(), (bitmap.height * .14f).toInt()))
                assertEquals("Selected tile lost its unrelated red stroke", Color.RED,
                    bitmap.getPixel((bitmap.width * .8f).toInt(), (bitmap.height * .34f).toInt()))
                assertEquals("Sibling acquired selected-tile drawing", baseColor,
                    bitmap.getPixel(bitmap.width / 2, bitmap.height * 3 / 4))
                assertEquals("Sibling's independent yellow stroke disappeared", Color.YELLOW,
                    bitmap.getPixel(bitmap.width / 2, bitmap.height * 7 / 8))
            }
        }
    }

    @Test fun selectedTileEraseAndCanceledGesturesKeepSiblingThroughParentReopenAndPng() = fixture(collage = true) {
        val originalParent = rule.runOnIdle { observedCollage }
        drawLongLine()
        finishTile()
        val healthy = rule.runOnIdle { observedCollage }
        assertEquals(originalParent.photos[1], healthy.photos[1])
        assertEquals(500, healthy.photos[0].edits.creative.strokes.single().points.size)

        editSelectedTile()
        ink(erase = false)
        val target = canvas()
        val image = fitted(target)
        target.performTouchInput { down(image.at(.6f, .2f)); moveTo(image.at(.8f, .2f)) }
        rule.waitForIdle()
        target.performTouchInput { cancel() }
        rule.waitForIdle()
        ensureToolsVisible()
        eraseLongLine(cancelGesture = true)
        finishTile()
        rule.runOnIdle { assertEquals("Tile cancellation changed the parent", healthy, observedCollage) }
        rule.onNodeWithContentDescription("Undo").performClick()
        rule.runOnIdle { assertEquals("Canceled tile gesture inserted parent history", originalParent, observedCollage) }
        rule.onNodeWithContentDescription("Redo").performClick()
        rule.runOnIdle { assertEquals(healthy, observedCollage) }

        editSelectedTile()
        eraseLongLine()
        waitForVisibleErasedGap()
        finishTile()
        val erased = rule.runOnIdle { observedCollage }
        assertEquals(healthy.photos[1], erased.photos[1])
        assertEquals("erase-selected", erased.photos[0].id)
        assertNotEquals(healthy.photos[0].edits, erased.photos[0].edits)
        rule.onNodeWithContentDescription("Undo").performClick()
        rule.runOnIdle { assertEquals(healthy, observedCollage) }
        rule.onNodeWithContentDescription("Redo").performClick()
        rule.runOnIdle { assertEquals(erased, observedCollage) }

        saveAndReopenCollage(erased)
        exportPng(collage = true) { bitmap ->
            assertEquals(1600, bitmap.width)
            assertEquals(1600, bitmap.height)
            assertErasedPixels(bitmap, collage = true)
            assertEquals("Duplicate-source neighbor acquired the selected tile's stroke", baseColor,
                bitmap.getPixel(bitmap.width / 2, bitmap.height * 3 / 4))
            assertEquals("Neighbor's independent yellow line disappeared", Color.YELLOW,
                bitmap.getPixel(bitmap.width / 2, bitmap.height * 7 / 8))
        }
    }
}

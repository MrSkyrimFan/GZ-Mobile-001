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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.*
import com.greenezone.gzphotoeditor.ui.PhotoEditorScreen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import kotlin.math.min

/** UI regressions for a new cover's first crop and complete Regular/selected-tile gestures. */
@RunWith(AndroidJUnit4::class)
class AndroidRegularGestureCustodyTest {
    @get:Rule val rule = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val resolver get() = context.contentResolver
    private val names = mutableListOf<String>()
    private var output: Uri? = null
    private var applied: CollagePhoto? = null
    private var activePhotoName: String? = null

    private fun media(name: String): Uri = requireNotNull(
        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        })
    )

    private fun photo(tile: Boolean = false, run: (Uri) -> Unit) {
        val source = media("gz-gesture-source-" + System.nanoTime() + ".png")
        val destination = media("gz-gesture-result-" + System.nanoTime() + ".png")
        output = destination
        val mounted = mutableStateOf(true)
        var contentSet = false
        try {
            val bitmap = PrivacyCoverFixture.source()
            try {
                resolver.openOutputStream(source)!!.use {
                    assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
                }
            } finally { bitmap.recycle() }
            resolver.update(source, ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }, null, null)
            val originalBytes = resolver.openInputStream(source)!!.use { it.readBytes() }
            val picker = object : ActivityResultRegistryOwner {
                override val activityResultRegistry = object : ActivityResultRegistry() {
                    override fun <I, O> onLaunch(
                        requestCode: Int, contract: ActivityResultContract<I, O>,
                        input: I, options: ActivityOptionsCompat?
                    ) {
                        val uri = when (contract) {
                            is ActivityResultContracts.PickVisualMedia -> source
                            is ActivityResultContracts.StartActivityForResult -> {
                                val intent = input as Intent
                                assertEquals(Intent.ACTION_CREATE_DOCUMENT, intent.action)
                                assertEquals("image/png", intent.type)
                                destination
                            }
                            else -> error("Unexpected picker contract")
                        }
                        dispatchResult(requestCode, Activity.RESULT_OK, Intent().setData(uri))
                    }
                }
            }
            rule.setContent {
                CompositionLocalProvider(LocalActivityResultRegistryOwner provides picker) {
                    MaterialTheme(colorScheme = darkColorScheme()) {
                        if (mounted.value) PhotoEditorScreen(
                            initialCollagePhoto = if (tile) CollagePhoto(
                                source.toString(), id = "gesture-tile"
                            ) else null,
                            onDoneCollagePhoto = if (tile) ({ applied = it }) else null
                        )
                    }
                }
            }
            contentSet = true
            if (!tile) command("New photo…")
            rule.waitUntil(15_000) {
                rule.onAllNodesWithTag("photo-tools").fetchSemanticsNodes().isNotEmpty()
            }
            run(source)
            assertArrayEquals(originalBytes, resolver.openInputStream(source)!!.use { it.readBytes() })
        } finally {
            if (contentSet) {
                rule.runOnIdle { mounted.value = false }
                rule.waitForIdle()
            }
            names.forEach { ProjectStore.delete(context, it); CollageStore.delete(context, it) }
            resolver.delete(source, null, null)
            resolver.delete(destination, null, null)
        }
    }

    private fun command(label: String) {
        rule.onNodeWithText("Options", substring = false).performClick()
        rule.onNodeWithText(label, substring = false).performClick()
    }

    private fun tab(name: String) {
        rule.onNodeWithTag("photo-tools").performScrollToNode(hasText(name))
        rule.onNodeWithText(name).performClick()
    }

    private fun canvas() = rule.onNodeWithContentDescription(
        "Photo editing canvas. Tap to hide or show tools."
    )

    private fun save(name: String = "Gesture " + System.nanoTime()): SavedProject {
        if (name !in names) names += name
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

    private fun reopen(name: String) {
        command("Open project…")
        rule.onNodeWithText(name, substring = false).performClick()
        rule.onNodeWithText("Open project").performClick()
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText("Project restored").fetchSemanticsNodes().isNotEmpty()
        }
        activePhotoName = name
    }

    private fun exportPng(inspect: (Bitmap) -> Unit) {
        command("Export image…")
        if (rule.onAllNodesWithText("JPEG · JPG", substring = true).fetchSemanticsNodes().isNotEmpty()) {
            rule.onNodeWithText("JPEG · JPG", substring = true).performClick()
            rule.onNodeWithText("PNG · Lossless").performClick()
        }
        rule.onNodeWithText("Save PNG").performClick()
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText("Saved ", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        val bitmap = resolver.openInputStream(requireNotNull(output))!!.use {
            requireNotNull(BitmapFactory.decodeStream(it))
        }
        try { inspect(bitmap) } finally { bitmap.recycle() }
    }

    private data class Fitted(val left: Float, val top: Float, val width: Float, val height: Float) {
        fun at(x: Float, y: Float) = Offset(left + width * x, top + height * y)
    }

    private fun fitted(node: SemanticsNodeInteraction, crop: Boolean = false): Fitted {
        val size = node.fetchSemanticsNode().size
        val inset = if (crop) 16f * context.resources.displayMetrics.density else 0f
        val scale = min((size.width - 2f * inset) / 256f, (size.height - 2f * inset) / 128f)
        val width = scale * 256f
        val height = scale * 128f
        return Fitted((size.width - width) / 2f, (size.height - height) / 2f, width, height)
    }

    private fun freshCover() {
        tab("Blur & cover")
        rule.onNodeWithTag("privacy-edits").performScrollToNode(hasText("+ Black bar"))
        rule.onNodeWithText("+ Black bar").performClick()
        rule.onNodeWithText("Position X").performClick()
        rule.onNodeWithContentDescription("Adjust Position X")
            .performSemanticsAction(SemanticsActions.SetProgress) { it(.25f) }
        // Deliberately no Flip, Rotate, preset crop or direct anchoring helper.
        tab("Crop")
        rule.waitUntil(15_000) {
            rule.onAllNodesWithTag("crop-frame").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun cropLeftHalf(held: Boolean) {
        val overlay = rule.onNodeWithTag("crop-frame")
        val image = fitted(overlay, crop = true)
        overlay.performTouchInput {
            down(image.at(1f, .5f))
            moveTo(image.at(.75f, .5f))
            if (!held) {
                moveTo(image.at(.5f, .5f))
                up()
            }
        }
        if (held) {
            // Separate dispatches and real Compose frames force the preview stamp
            // comparison to run while this pointer remains down.
            rule.waitForIdle()
            rule.mainClock.advanceTimeBy(160)
            rule.waitForIdle()
            overlay.assertIsDisplayed()
            overlay.performTouchInput { moveTo(image.at(.625f, .5f)) }
            rule.waitForIdle()
            rule.mainClock.advanceTimeBy(160)
            overlay.performTouchInput { moveTo(image.at(.5f, .5f)); up() }
        }
    }

    private fun assertProtectedHalf(document: EditDocument) {
        val frame = requireNotNull(document.cropFrame) { "The first crop gesture snapped back" }
        assertEquals(.5f, frame.right, .003f)
        assertNotNull("Fresh cover has no source geometry", document.privacy.single().authoringGeometry)
        val source = PrivacyCoverFixture.source()
        try {
            val rendered = ImagePipeline.render(source, document)
            try {
                val bytes = ByteArrayOutputStream().use {
                    assertTrue(rendered.compress(Bitmap.CompressFormat.PNG, 100, it))
                    it.toByteArray()
                }
                val png = requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                try { assertProtectedHalfPixels(png) } finally { png.recycle() }
            } finally { if (rendered !== source) rendered.recycle() }
        } finally { source.recycle() }
    }

    private fun assertProtectedHalfPixels(png: Bitmap) {
        assertEquals(128, png.width)
        assertEquals(128, png.height)
        PrivacyCoverFixture.assertBox(png, 61, 61, 67, 67, Color.BLACK)
        assertEquals(PrivacyCoverFixture.base, png.getPixel(5, 15))
    }

    @Test fun freshCoverHeldCropRetainsGesturePixelsHistoryAndNamedReopen() = photo {
        freshCover()
        cropLeftHalf(held = true)
        val saved = save()
        assertProtectedHalf(saved.document)
        rule.onNodeWithContentDescription("Undo").performClick()
        val undone = save(saved.name)
        assertNull(undone.document.cropFrame)
        assertEquals(.25f, undone.document.privacy.single().centerX, .0001f)
        rule.onNodeWithContentDescription("Redo").performClick()
        val redone = save(saved.name)
        assertEquals(saved.document, redone.document)
        reopen(saved.name)
        exportPng(::assertProtectedHalfPixels)
    }

    @Test fun freshCoverQuickCropInSelectedTileSurvivesParentSaveAndReopen() = photo(tile = true) { uri ->
        freshCover()
        cropLeftHalf(held = false)
        rule.onNodeWithText("Done").performClick()
        val tile = rule.runOnIdle { requireNotNull(applied) }
        assertProtectedHalf(tile.edits)
        assertEquals("gesture-tile", tile.id)
        val untouched = CollagePhoto(uri.toString(), id = "independent-neighbor")
        val collage = CollageDocument(photos = listOf(tile, untouched))
        val name = "Crop tile " + System.nanoTime()
        names += name
        assertTrue(CollageStore.save(context, name, collage))
        val reopened = requireNotNull(CollageStore.load(context, name))
        assertEquals(untouched, reopened.photos[1])
        assertProtectedHalf(reopened.photos[0].edits)
    }

    private fun drawing() {
        tab("Draw")
        rule.onNodeWithContentDescription("Adjust Brush thickness")
            .performSemanticsAction(SemanticsActions.SetProgress) { it(.05f) }
        canvas().performClick()
        rule.onNodeWithTag("photo-tools").assertDoesNotExist()
    }

    private fun longStroke() {
        val target = canvas()
        val image = fitted(target)
        target.performTouchInput {
            down(image.at(.2f, .45f))
            for (i in 1..610) {
                moveTo(image.at(.2f + i * .0006f, if (i % 2 == 0) .45f else .55f))
            }
            moveTo(image.at(.78f, .65f))
            moveTo(image.at(.87f, .65f))
            moveTo(image.at(.87f, .75f))
            up()
        }
        rule.waitForIdle()
        canvas().performClick()
        rule.onNodeWithTag("photo-tools").assertIsDisplayed()
    }

    private fun assertCompleteStroke(document: EditDocument) {
        val stroke = document.creative.strokes.single()
        assertTrue(stroke.points.size <= 500)
        assertEquals(.87f, stroke.points.last().x, .003f)
        assertEquals(.75f, stroke.points.last().y, .003f)
        assertTrue("The late corner was lost", stroke.points.any {
            kotlin.math.abs(it.x - .87f) < .003f && kotlin.math.abs(it.y - .65f) < .003f
        })
    }

    @Test fun regularDrawKeepsLateBendsAndEndpointInHistoryReopenAndPng() = photo {
        drawing()
        longStroke()
        val saved = save()
        assertCompleteStroke(saved.document)
        rule.onNodeWithContentDescription("Undo").performClick()
        assertTrue(save(saved.name).document.creative.strokes.isEmpty())
        rule.onNodeWithContentDescription("Redo").performClick()
        assertEquals(saved.document, save(saved.name).document)
        reopen(saved.name)
        exportPng { png ->
            assertEquals(256, png.width)
            assertEquals(128, png.height)
            assertEquals(Color.WHITE, png.getPixel(223, 96))
            assertEquals(Color.WHITE, png.getPixel(223, 84))
        }
    }

    @Test fun selectedTileDrawRetainsTheSameCompleteGesture() = photo(tile = true) {
        drawing()
        longStroke()
        rule.onNodeWithText("Done").performClick()
        val tile = rule.runOnIdle { requireNotNull(applied) }
        assertCompleteStroke(tile.edits)
        assertEquals("gesture-tile", tile.id)
        val decoded = requireNotNull(EditDocumentCodec.decode(EditDocumentCodec.encode(tile.edits)))
        assertEquals(tile.edits, decoded)
    }

    @Test fun canceledRegularDrawingAndErasingRestoreDocumentAndHistory() = photo {
        drawing()
        val target = canvas()
        val image = fitted(target)
        target.performTouchInput {
            down(image.at(.2f, .5f))
            moveTo(image.at(.4f, .5f))
        }
        rule.waitForIdle()
        target.performTouchInput { cancel() }
        rule.waitForIdle()
        canvas().performClick()
        rule.onNodeWithContentDescription("Undo").assertIsNotEnabled()
        assertTrue(save().document.creative.strokes.isEmpty())

        drawing()
        target.performTouchInput {
            down(image.at(.2f, .5f))
            for (i in 1..80) moveTo(image.at(.2f + .006f * i, .5f))
            up()
        }
        rule.waitForIdle()
        canvas().performClick()
        val healthy = save()
        tab("Draw")
        rule.onNodeWithText("Eraser", substring = false).performClick()
        canvas().performClick()
        target.performTouchInput {
            down(image.at(.3f, .5f))
            moveTo(image.at(.5f, .5f))
        }
        rule.waitForIdle()
        target.performTouchInput { cancel() }
        rule.waitForIdle()
        canvas().performClick()
        assertEquals(healthy.document, save(healthy.name).document)
        rule.onNodeWithContentDescription("Undo").performClick()
        assertTrue("Canceled erase inserted an Undo checkpoint",
            save(healthy.name).document.creative.strokes.isEmpty())
        rule.onNodeWithContentDescription("Redo").performClick()
        assertEquals(healthy.document, save(healthy.name).document)
    }
}

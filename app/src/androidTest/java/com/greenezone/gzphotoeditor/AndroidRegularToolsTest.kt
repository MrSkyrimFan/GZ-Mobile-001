package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.ContentValues
import android.content.Context
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.CollagePhoto
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.Grade
import com.greenezone.gzphotoeditor.editor.LocalMask
import com.greenezone.gzphotoeditor.editor.MaskKind
import com.greenezone.gzphotoeditor.editor.PhotoHistogram
import com.greenezone.gzphotoeditor.editor.ProjectStore
import com.greenezone.gzphotoeditor.editor.ExportWorkspace
import com.greenezone.gzphotoeditor.editor.PendingExportJournal
import com.greenezone.gzphotoeditor.ui.PhotoEditorScreen
import com.greenezone.gzphotoeditor.editor.TextOverlay
import com.greenezone.gzphotoeditor.editor.CreativeDocument
import com.greenezone.gzphotoeditor.editor.CreativeRaster
import com.greenezone.gzphotoeditor.editor.GeometryMath
import com.greenezone.gzphotoeditor.editor.ImagePipeline
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Base64
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class AndroidRegularToolsTest {
    @get:Rule val rule = createComposeRule()

    private fun withRegularPhoto(
        destination: Uri? = null,
        removeSourceAfterOpen: Boolean = false,
        tileEdits: EditDocument? = null,
        onTileDone: ((CollagePhoto) -> Unit)? = null,
        sourceBitmap: (() -> Bitmap)? = null,
        test: (Uri) -> Unit
    ) {
        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "gz-regular-tools.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        var removed = false
        try {
            val size = 256
            val pixels = IntArray(size * size) { if (it % size < size / 2) Color.BLACK else Color.WHITE }
            val source = sourceBitmap?.invoke()
                ?: Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
            try {
                resolver.openOutputStream(uri)!!.use { check(source.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            } finally { source.recycle() }
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            val picker = object : ActivityResultRegistryOwner {
                override val activityResultRegistry = object : ActivityResultRegistry() {
                    override fun <I, O> onLaunch(requestCode: Int, contract: ActivityResultContract<I, O>,
                        input: I, options: ActivityOptionsCompat?) {
                        val result = when (contract) {
                            is ActivityResultContracts.PickVisualMedia -> uri
                            is ActivityResultContracts.StartActivityForResult -> requireNotNull(destination)
                            else -> error("Unexpected activity contract")
                        }
                        dispatchResult(requestCode, Activity.RESULT_OK, Intent().setData(result))
                    }
                }
            }
            rule.setContent {
                CompositionLocalProvider(LocalActivityResultRegistryOwner provides picker) {
                    MaterialTheme(colorScheme = darkColorScheme()) {
                        PhotoEditorScreen(
                            initialCollagePhoto = tileEdits?.let {
                                CollagePhoto(uri.toString(), it, id = "legacy-mask-tile")
                            },
                            onDoneCollagePhoto = onTileDone
                        )
                    }
                }
            }
            if (tileEdits == null) rule.onNodeWithText("Open media").performClick()
            rule.waitUntil(15_000) { rule.onAllNodesWithTag("photo-tools").fetchSemanticsNodes().isNotEmpty() }
            if (removeSourceAfterOpen) {
                assertEquals(1, resolver.delete(uri, null, null))
                removed = true
            }
            test(uri)
        } finally {
            if (!removed) resolver.delete(uri, null, null)
        }
    }

    private fun assertNoMasksInPhotoTools() {
        val tools = rule.onNodeWithTag("photo-tools")
        tools.performScrollToNode(hasText("Blur & cover"))
        rule.onNodeWithText("Blur & cover").assertIsDisplayed()
        tools.performScrollToNode(hasText("Text"))
        rule.onNodeWithText("Text").assertIsDisplayed()
        // Search the entire lazy row. A semantics query alone misses offscreen tools.
        val masks = runCatching { tools.performScrollToNode(hasText("Masks")) }
        assertTrue("Regular still exposes Masks", masks.exceptionOrNull() is AssertionError)
    }

    private fun historicalMaskedPhoto() = EditDocument(masks = listOf(
        LocalMask(kind = MaskKind.RADIAL, centerX = .68f, centerY = .5f,
            radius = .2f, feather = .3f, exposure = -1f)
    ))

    private fun seedLegacyRegularProject(context: Context, name: String, uri: Uri) {
        val recipe = """
            mask.0.exposure=-1.0
            mask.0.feather=0.3
            mask.0.kind=RADIAL
            mask.0.radius=0.2
            mask.0.x=0.68
            mask.0.y=0.5
            mask.count=1
            version=1
        """.trimIndent()
        val encoder = Base64.getUrlEncoder().withoutPadding()
        val key = encoder.encodeToString(name.toByteArray())
        val source = encoder.encodeToString(uri.toString().toByteArray())
        val store = context.getSharedPreferences("gzphotoeditor.projects.v1", Context.MODE_PRIVATE)
        assertTrue(store.edit().putString(key, source + "\n" + recipe).commit())
    }

    @Test fun photoHistogramCountsVisibleChannelsAndLuminance() {
        val pixels = intArrayOf(
            Color.TRANSPARENT, Color.BLACK, Color.WHITE, Color.RED,
            Color.GREEN, Color.BLUE, Color.argb(128, 255, 0, 0), Color.rgb(128, 128, 128)
        )
        val bitmap = Bitmap.createBitmap(pixels, 4, 2, Bitmap.Config.ARGB_8888)
        try {
            val histogram = PhotoHistogram.fromPreview(bitmap) {}
            assertEquals(7, histogram.visiblePixels)
            assertEquals(1, histogram.blackPixels)
            assertEquals(1, histogram.whitePixels)
            assertEquals(3, histogram.red[0])
            assertEquals(3, histogram.red[255])
            assertEquals(4, histogram.green[0])
            assertEquals(2, histogram.green[255])
            assertEquals(4, histogram.blue[0])
            assertEquals(2, histogram.blue[255])
            for (bins in listOf(histogram.red, histogram.green, histogram.blue, histogram.luminance)) {
                assertEquals(7, bins.sum())
            }
            assertEquals(1, histogram.luminance[18])
            assertEquals(2, histogram.luminance[54])
            assertEquals(1, histogram.luminance[128])
            assertEquals(1, histogram.luminance[182])
        } finally {
            bitmap.recycle()
        }
    }

    @Test fun regularHistogramFollowsExposureAndBeforeView() = withRegularPhoto {
        fun openHistogram() {
            rule.onNodeWithTag("photo-tools").performScrollToNode(hasText("More"))
            rule.onNodeWithText("More").performClick()
            rule.onNodeWithTag("photo-actions").performScrollToNode(hasText("Histogram"))
            rule.onNodeWithText("Histogram").performClick()
        }
        fun expectClipping(white: Double) {
            val label = String.format(Locale.getDefault(), "Black %.1f%%  ·  White %.1f%%", 50.0, white)
            rule.waitUntil(10_000) { rule.onAllNodesWithText(label).fetchSemanticsNodes().isNotEmpty() }
            rule.onNodeWithText(label).assertIsDisplayed()
        }
        openHistogram()
        expectClipping(50.0)
        rule.onNodeWithContentDescription("RGB photo histogram").assertIsDisplayed()
        rule.onNodeWithText("Luminance").performClick()
        rule.onNodeWithContentDescription("Luminance photo histogram").assertIsDisplayed()
        rule.onNodeWithText("Close").performClick()

        rule.onNodeWithTag("photo-tools").performScrollToNode(hasText("Light"))
        rule.onNodeWithText("Light").performClick()
        rule.onNodeWithContentDescription("Adjust Exposure").performTouchInput {
            swipe(center, Offset(width * .18f, center.y), 350)
        }
        openHistogram()
        rule.onNodeWithText("Edited preview").assertIsDisplayed()
        expectClipping(0.0)
        rule.onNodeWithText("Close").performClick()

        rule.onNodeWithText("Before").performClick()
        openHistogram()
        rule.onNodeWithText("Before preview").assertIsDisplayed()
        expectClipping(50.0)
        rule.onNodeWithText("Close").performClick()
    }

    @Test fun regularOmitsMaskAuthoringAndPreservesOldProjectAppearanceAndRecipe() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val oldName = "Old mask " + System.nanoTime()
        val copyName = "Mask copy " + System.nanoTime()
        val historical = historicalMaskedPhoto()
        try {
            withRegularPhoto { uri ->
                val tools = rule.onNodeWithTag("photo-tools")
                val canvas = rule.onNodeWithContentDescription(
                    "Photo editing canvas. Tap to hide or show tools."
                )
                fun sampleRightOfCenter(): Int {
                    val capture = canvas.captureToImage().asAndroidBitmap()
                    return try {
                        val side = minOf(capture.width, capture.height)
                        val x = (capture.width - side) / 2 + side * 68 / 100
                        Color.red(capture.getPixel(x, capture.height / 2))
                    } finally {
                        capture.recycle()
                    }
                }

                assertTrue(sampleRightOfCenter() > 240)
                seedLegacyRegularProject(context, oldName, uri)
                val legacy = ProjectStore.load(context).single { it.name == oldName }
                assertEquals(historical.masks, legacy.document.masks)
                tools.performScrollToNode(hasText("More"))
                rule.onNodeWithText("More").performClick()
                val actions = rule.onNodeWithTag("photo-actions")
                actions.performScrollToNode(hasText("Open projects"))
                rule.onNodeWithText("Open projects").performClick()
                rule.onNodeWithText(oldName).performClick()
                rule.onNodeWithText("Open project").performClick()
                rule.waitUntil(15_000) {
                    rule.onAllNodesWithText("Project restored").fetchSemanticsNodes().isNotEmpty()
                }
                rule.waitUntil(15_000) { sampleRightOfCenter() in 100..160 }
                assertNoMasksInPhotoTools()
                tools.performScrollToNode(hasText("More"))
                rule.onNodeWithText("More").performClick()
                actions.performScrollToNode(hasText("Save project"))
                rule.onNodeWithText("Save project").performClick()
                rule.onNode(hasSetTextAction()).performTextInput(copyName)
                rule.onNodeWithText("Save").performClick()
                rule.onNodeWithText("Project saved on your phone").assertIsDisplayed()
                assertEquals(legacy.document, ProjectStore.load(context).single { it.name == copyName }.document)
            }
        } finally {
            ProjectStore.delete(context, oldName)
            ProjectStore.delete(context, copyName)
        }
    }

    @Test fun selectedCollageTileOmitsMaskAuthoringAndKeepsHistoricalRecipe() {
        val historical = historicalMaskedPhoto()
        var applied: CollagePhoto? = null
        withRegularPhoto(tileEdits = historical, onTileDone = { applied = it }) { uri ->
            assertNoMasksInPhotoTools()
            rule.onNodeWithText("Done").performClick()
            rule.runOnIdle { assertEquals(CollagePhoto(uri.toString(), historical, id = "legacy-mask-tile"), applied) }
        }
    }

    @Test fun regularCaptionSurvivesProjectReopenAndPngExport() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val resolver = context.contentResolver
        val output = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "gz-exported-caption.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        val name = "Caption " + System.nanoTime()
        try {
            withRegularPhoto(output) {
                rule.onNodeWithTag("photo-tools").performScrollToNode(hasText("Text"))
                rule.onNodeWithText("Text").performClick()
                rule.onNodeWithText("+ Add text").performClick()
                rule.onNode(hasSetTextAction()).performTextInput("Phone test caption")
                rule.onNodeWithText("Apply").performClick()
                rule.onNodeWithText("Text 1").assertIsDisplayed()
                rule.onNodeWithText("Edit words").performClick()
                rule.onNodeWithText("Phone test caption").assertIsDisplayed()
                rule.onNodeWithText("Cancel").performClick()
                rule.onNodeWithTag("photo-tools").performScrollToNode(hasText("More"))
                rule.onNodeWithText("More").performClick()
                val actions = rule.onNodeWithTag("photo-actions")
                actions.performScrollToNode(hasText("Save project"))
                rule.onNodeWithText("Save project").performClick()
                rule.onNode(hasSetTextAction()).performTextInput(name)
                rule.onNodeWithText("Save").performClick()
                actions.performScrollToNode(hasText("Open projects"))
                rule.onNodeWithText("Open projects").performClick()
                rule.onNodeWithText(name).performClick()
                rule.onNodeWithText("Open project").performClick()
                rule.waitUntil(15_000) { rule.onAllNodesWithText("Project restored").fetchSemanticsNodes().isNotEmpty() }
                val saved = ProjectStore.load(context).single { it.name == name }
                assertEquals("Phone test caption", saved.document.creative.text.single().content)

                rule.onNodeWithText("Export").performClick()
                rule.onNodeWithText("Output: 256 × 256 pixels").assertIsDisplayed()
                rule.onNodeWithText("JPEG · JPG  ▾").performClick()
                rule.onNodeWithText("PNG · Lossless").performClick()
                rule.onNodeWithText("Save PNG").performClick()
                rule.waitUntil(15_000) {
                    rule.onAllNodesWithText("Saved 256 × 256", substring = true).fetchSemanticsNodes().isNotEmpty()
                }
                val exported = resolver.openInputStream(output)!!.use { requireNotNull(BitmapFactory.decodeStream(it)) }
                val original = resolver.openInputStream(saved.image)!!.use { requireNotNull(BitmapFactory.decodeStream(it)) }
                try {
                    assertEquals(original.width, exported.width)
                    assertEquals(original.height, exported.height)
                    var changed = 0
                    for (y in 0 until original.height) for (x in 0 until original.width) {
                        if (exported.getPixel(x, y) != original.getPixel(x, y)) changed++
                    }
                    assertTrue("Expected caption pixels in the exported PNG", changed > 20)
                    assertEquals(Color.BLACK, original.getPixel(90, 128))
                    assertEquals(Color.BLACK, exported.getPixel(0, 0))
                } finally {
                    original.recycle()
                    exported.recycle()
                }
            }
        } finally {
            ProjectStore.delete(context, name)
            resolver.delete(output, null, null)
        }
    }

    @Test fun mirroredPrivacyCoverSurvivesUndoRedoNamedReopenAndPngExport() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val resolver = context.contentResolver
        val name = "Cover " + System.nanoTime()
        val output = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "gz-exported-cover.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        try {
            withRegularPhoto(output, sourceBitmap = PrivacyCoverFixture::source) {
                val tools = rule.onNodeWithTag("photo-tools")
                tools.performScrollToNode(hasText("Blur & cover"))
                rule.onNodeWithText("Blur & cover").performClick()
                rule.onNodeWithTag("privacy-edits").performScrollToNode(hasText("+ Black bar"))
                rule.onNodeWithText("+ Black bar").performClick()
                rule.onNodeWithText("Position X").performClick()
                rule.onNodeWithContentDescription("Adjust Position X")
                    .performSemanticsAction(SemanticsActions.SetProgress) { it(.25f) }

                tools.performScrollToNode(hasText("Crop"))
                rule.onNodeWithText("Crop").performClick()
                rule.onNodeWithTag("crop-transforms").performScrollToNode(hasText("Flip horizontal"))
                rule.onNodeWithText("Flip horizontal").performClick()
                rule.onNodeWithContentDescription("Undo").performClick()
                rule.onNodeWithContentDescription("Redo").performClick()

                tools.performScrollToNode(hasText("More"))
                rule.onNodeWithText("More").performClick()
                val actions = rule.onNodeWithTag("photo-actions")
                actions.performScrollToNode(hasText("Save project"))
                rule.onNodeWithText("Save project").performClick()
                rule.onNode(hasSetTextAction()).performTextInput(name)
                rule.onNodeWithText("Save").performClick()
                actions.performScrollToNode(hasText("Open projects"))
                rule.onNodeWithText("Open projects").performClick()
                rule.onNodeWithText(name).performClick()
                rule.onNodeWithText("Open project").performClick()
                rule.waitUntil(15_000) {
                    rule.onAllNodesWithText("Project restored").fetchSemanticsNodes().isNotEmpty()
                }

                rule.onNodeWithText("Export").performClick()
                rule.onNodeWithText("JPEG · JPG  ▾").performClick()
                rule.onNodeWithText("PNG · Lossless").performClick()
                rule.onNodeWithText("Save PNG").performClick()
                rule.waitUntil(15_000) {
                    rule.onAllNodesWithText("Saved 256 × 128", substring = true)
                        .fetchSemanticsNodes().isNotEmpty()
                }
                val actual = resolver.openInputStream(output)!!.use {
                    requireNotNull(BitmapFactory.decodeStream(it))
                }
                try {
                    PrivacyCoverFixture.assertMirror(actual)
                } finally {
                    actual.recycle()
                }
            }
        } finally {
            ProjectStore.delete(context, name)
            resolver.delete(output, null, null)
        }
    }

    @Test fun regularProtectsDirtyDraftAndConfirmsNamedDeletion() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "Regular B " + System.nanoTime()
        val original = EditDocument(grade = Grade(exposure = -.3f))
        try {
            withRegularPhoto { source ->
                assertTrue(ProjectStore.save(context, name, source, original))
                rule.onNodeWithTag("photo-tools").performScrollToNode(hasText("Text"))
                rule.onNodeWithText("Text").performClick()
                rule.onNodeWithText("+ Add text").performClick()
                rule.onNode(hasSetTextAction()).performTextInput("Retain this draft")
                rule.onNodeWithText("Apply").performClick()

                fun openLibrary() {
                    rule.onNodeWithTag("photo-tools").performScrollToNode(hasText("More"))
                    rule.onNodeWithText("More").performClick()
                    rule.onNodeWithTag("photo-actions").performScrollToNode(hasText("Open projects"))
                    rule.onNodeWithText("Open projects").performClick()
                    rule.onNodeWithText(name).performClick()
                }
                openLibrary()
                rule.onNodeWithText("Open project").performClick()
                rule.onNodeWithText("Keep your photo edits?").assertIsDisplayed()
                rule.onNodeWithText("Keep editing").performClick()
                rule.onNodeWithText("Export").assertIsDisplayed()
                rule.onNodeWithTag("photo-tools").performScrollToNode(hasText("Text"))
                rule.onNodeWithText("Text").performClick()
                rule.onNodeWithText("Text 1").assertIsDisplayed()

                openLibrary()
                rule.onNodeWithText("Delete", substring = false).performClick()
                rule.onNodeWithText("Delete saved project?").assertIsDisplayed()
                rule.onNodeWithText("Cancel").performClick()
                assertEquals(original, ProjectStore.load(context).single { it.name == name }.document)
                rule.onNodeWithText("Delete", substring = false).performClick()
                rule.onNodeWithText("Delete project").performClick()
                assertTrue(ProjectStore.load(context).none { it.name == name })
                assertNotNull(context.contentResolver.openInputStream(source)?.use { it.read() })
                rule.onNodeWithText("Close").performClick()
                rule.onNodeWithTag("photo-tools").performScrollToNode(hasText("Text"))
                rule.onNodeWithText("Text").performClick()
                rule.onNodeWithText("Text 1").assertIsDisplayed()
            }
        } finally {
            ProjectStore.delete(context, name)
        }
    }

    @Test fun letterSpacingKeepsAnEmojiWholeThroughPngDecode() {
        val caption = TextOverlay(content = "😀", size = .16f)
        fun render(spacing: Float): IntArray {
            val raster = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
            try {
                CreativeRaster.drawInto(raster, CreativeDocument(
                    text = listOf(caption.copy(letterSpacing = spacing))
                ))
                val png = java.io.ByteArrayOutputStream()
                assertTrue(raster.compress(Bitmap.CompressFormat.PNG, 100, png))
                val bytes = png.toByteArray()
                val output = requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                try {
                    return IntArray(256 * 256).also { pixels ->
                        output.getPixels(pixels, 0, 256, 0, 0, 256, 256)
                    }
                } finally {
                    output.recycle()
                }
            } finally {
                raster.recycle()
            }
        }
        val unspaced = render(0f)
        assertTrue(unspaced.any { Color.alpha(it) != 0 })
        assertArrayEquals(unspaced, render(.25f))
    }

    @Test fun regularExportSizingUsesTheDecoderBudget() {
        assertEquals(2243 to 1496, ImagePipeline.decodeSize(
            6000, 4000, ImagePipeline.EXPORT_MAX_EDGE, GeometryMath.safeExportPixels(128)))
        assertEquals(3886 to 2591, ImagePipeline.decodeSize(
            6000, 4000, ImagePipeline.EXPORT_MAX_EDGE, GeometryMath.safeExportPixels(256)))
        assertEquals(256 to 256, ImagePipeline.decodeSize(
            256, 256, ImagePipeline.EXPORT_MAX_EDGE, GeometryMath.safeExportPixels(128)))
    }

    @Test fun failedSourceDecodePreservesExistingPhotoDestination() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val resolver = context.contentResolver
        val destination = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "gz-existing-destination.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        val existing = byteArrayOf(0x38, 0x27, 0x16)
        try {
            resolver.openOutputStream(destination, "wt")!!.use { it.write(existing) }
            withRegularPhoto(destination, removeSourceAfterOpen = true) {
                rule.onNodeWithText("Export").performClick()
                rule.onNodeWithText("Save JPG").performClick()
                rule.waitUntil(15_000) {
                    rule.onAllNodesWithText("Export failed", substring = true).fetchSemanticsNodes().isNotEmpty()
                }
                assertArrayEquals(existing, resolver.openInputStream(destination)!!.use { it.readBytes() })
            }
        } finally {
            val journal = PendingExportJournal(context.filesDir)
            journal.read(ExportWorkspace.PHOTO)?.let { journal.discard(it) }
            resolver.delete(destination, null, null)
        }
    }

    @Test fun blurShowsRealPixelsDuringTheStrokeWithTheControlsHidden() = withRegularPhoto {
        rule.onNodeWithTag("photo-tools").performScrollToNode(hasText("Blur & cover"))
        rule.onNodeWithText("Blur & cover").performClick()
        rule.onNodeWithText("+ Blur brush").performClick()
        val canvas = rule.onNodeWithContentDescription("Photo editing canvas. Tap to hide or show tools.")
        canvas.performClick()
        rule.onNodeWithTag("photo-tools").assertDoesNotExist()

        fun sample(): Int {
            val image = canvas.captureToImage().asAndroidBitmap()
            val inset = (minOf(image.width, image.height) * .012f).toInt()
            return image.getPixel(image.width / 2 + inset, image.height / 2)
        }
        fun holdStroke() {
            canvas.performTouchInput {
                down(center - Offset(width * .16f, 0f))
                moveTo(center - Offset(width * .07f, 0f))
                moveTo(center + Offset(width * .09f, 0f))
            }
        }

        fun expectBlurred() {
            rule.waitUntil(15_000) {
                val color = sample()
                Color.red(color) in 120..225 && Color.red(color) == Color.green(color) &&
                    Color.green(color) == Color.blue(color)
            }
        }

        assertTrue(Color.red(sample()) > 240)
        holdStroke()
        try {
            expectBlurred()
        } finally {
            canvas.performTouchInput { cancel() }
        }
        rule.waitUntil(15_000) { Color.red(sample()) > 240 }

        holdStroke()
        try {
            expectBlurred()
        } finally {
            canvas.performTouchInput { up() }
        }
        expectBlurred()
        rule.onNodeWithTag("photo-tools").assertDoesNotExist()
    }
}

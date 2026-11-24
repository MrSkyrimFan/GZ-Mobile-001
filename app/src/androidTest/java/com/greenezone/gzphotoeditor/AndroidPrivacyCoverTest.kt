package com.greenezone.gzphotoeditor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.BrushPoint
import com.greenezone.gzphotoeditor.editor.BrushStroke
import com.greenezone.gzphotoeditor.editor.CollageDocument
import com.greenezone.gzphotoeditor.editor.CollageLayout
import com.greenezone.gzphotoeditor.editor.CollagePhoto
import com.greenezone.gzphotoeditor.editor.CollageRaster
import com.greenezone.gzphotoeditor.editor.CollageStore
import com.greenezone.gzphotoeditor.editor.CollageTemplate
import com.greenezone.gzphotoeditor.editor.CropFrame
import com.greenezone.gzphotoeditor.editor.CropMath
import com.greenezone.gzphotoeditor.editor.CropRatio
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.EditDocumentCodec
import com.greenezone.gzphotoeditor.editor.ImagePipeline
import com.greenezone.gzphotoeditor.editor.PrivacyEdit
import com.greenezone.gzphotoeditor.editor.PrivacyKind
import com.greenezone.gzphotoeditor.editor.PrivacyMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream

/** Matches the asymmetric REG-01 audit fixture and its independent Pillow checks. */
internal object PrivacyCoverFixture {
    const val width = 256
    const val height = 128
    val base = Color.rgb(31, 87, 139)
    val gold = Color.rgb(249, 178, 25)

    fun pixel(x: Int, y: Int, covered: Boolean = false): Int = when {
        covered && x in 6..121 && y in 53..74 -> Color.BLACK
        x in 56..71 && y in 59..68 -> Color.MAGENTA
        x in 211..226 && y in 17..31 -> gold
        else -> base
    }

    fun source(): Bitmap = Bitmap.createBitmap(IntArray(width * height) {
        pixel(it % width, it / width)
    }, width, height, Bitmap.Config.ARGB_8888)

    fun assertMirror(actual: Bitmap) {
        assertEquals(width, actual.width)
        assertEquals(height, actual.height)
        assertBox(actual, 187, 61, 196, 67, Color.BLACK)
        assertBox(actual, 32, 19, 42, 28, gold)
        assertBox(actual, 60, 62, 68, 67, base)
        assertBox(actual, 230, 10, 237, 17, base)
    }

    fun assertBox(bitmap: Bitmap, left: Int, top: Int, right: Int, bottom: Int, color: Int) {
        for (y in top until bottom) for (x in left until right) {
            assertEquals("Pixel at ($x, $y)", color, bitmap.getPixel(x, y))
        }
    }
}

@RunWith(AndroidJUnit4::class)
class AndroidPrivacyCoverTest {
    private val legacy = EditDocument(privacy = listOf(
        PrivacyEdit(kind = PrivacyKind.BLACK_BAR, centerX = .25f)
    ))

    private fun transition(from: EditDocument, to: EditDocument): EditDocument =
        PrivacyMath.retainAuthoringGeometry(from, to)

    private fun render(document: EditDocument, inspect: (Bitmap) -> Unit) {
        val source = PrivacyCoverFixture.source()
        try {
            val bitmap = ImagePipeline.render(source, document)
            try {
                inspect(bitmap)
            } finally {
                if (bitmap !== source) bitmap.recycle()
            }
        } finally {
            source.recycle()
        }
    }

    @Test fun versionOneCoverRetainsOldPlacementThenFollowsBothFlipsAndQuarterTurns() {
        val versionOne = EditDocumentCodec.encode(legacy)
        assertTrue(versionOne.lineSequence().any { it == "version=1" })
        val olderProject = requireNotNull(EditDocumentCodec.decode(versionOne))
        assertNull(olderProject.privacy.single().authoringGeometry)
        val mirrored = transition(olderProject, olderProject.copy(mirrored = true))
        val versionTwo = EditDocumentCodec.encode(mirrored)
        assertTrue(versionTwo.lineSequence().any { it == "version=2" })
        assertNull(EditDocumentCodec.decode(versionTwo.lineSequence()
            .filterNot { it.startsWith("privacy.0.geometry.rotation=") }.joinToString("\n")))
        val reopened = requireNotNull(EditDocumentCodec.decode(versionTwo))
        assertEquals(mirrored, reopened)
        render(reopened, PrivacyCoverFixture::assertMirror)

        val horizontalRoundTrip = transition(reopened, reopened.copy(mirrored = false))
        listOf(olderProject, horizontalRoundTrip).forEach { document ->
            render(document) { actual ->
                assertMapping(actual, 256, 128) { x, y -> x to y }
            }
        }
        render(transition(olderProject, olderProject.copy(flipVertical = true))) { actual ->
            assertMapping(actual, 256, 128) { x, y -> x to (127 - y) }
        }
        render(transition(olderProject, olderProject.copy(quarterTurns = 1))) { actual ->
            assertMapping(actual, 128, 256) { x, y -> y to (127 - x) }
        }
        render(transition(olderProject, olderProject.copy(quarterTurns = 3))) { actual ->
            assertMapping(actual, 128, 256) { x, y -> (255 - y) to x }
        }
    }

    @Test fun offCenterCropRotatesInPlaceAndUncroppingRecoversProtectedSource() {
        val cropped = transition(legacy, legacy.copy(crop = CropRatio.FREE,
            cropFrame = CropFrame(.125f, .125f, .625f, .875f)))
        render(cropped) { actual ->
            assertMapping(actual, 128, 96) { x, y -> (32 + x) to (16 + y) }
        }
        val clockwise = transition(cropped, CropMath.rotate(cropped, 256, 128, clockwise = true))
        render(clockwise) { actual ->
            assertMapping(actual, 96, 128) { x, y -> (32 + y) to (111 - x) }
        }
        val counter = transition(cropped, CropMath.rotate(cropped, 256, 128, clockwise = false))
        render(counter) { actual ->
            assertMapping(actual, 96, 128) { x, y -> (159 - y) to (16 + x) }
        }
        val cleared = transition(clockwise, clockwise.copy(crop = CropRatio.ORIGINAL, cropFrame = null))
        render(cleared) { actual ->
            assertMapping(actual, 128, 256) { x, y -> y to (127 - x) }
        }

        val portrait = cropped.copy(crop = CropRatio.TALL, cropFrameRatioLocked = true)
        val landscape = CropMath.rotate(portrait, 256, 128, clockwise = true)
        assertEquals(CropRatio.CLASSIC, landscape.crop)
        assertTrue(landscape.cropFrameRatioLocked)
        render(transition(portrait, landscape)) { actual ->
            assertMapping(actual, 96, 64) { x, y -> (64 + y) to (111 - x) }
        }
    }

    @Test fun brushAndSolidCoverKeepTheProtectedInteriorWhenStraightening() {
        val brush = PrivacyEdit(kind = PrivacyKind.BLACK_BRUSH, brushSize = .055f,
            feather = .01f, strokes = listOf(BrushStroke(listOf(BrushPoint(.25f, .5f)))))
        listOf(legacy, legacy.copy(privacy = listOf(brush))).forEach { before ->
            val first = transition(before, before.copy(straightenDegrees = 5f))
            val after = transition(first, before.copy(straightenDegrees = 10f))
            assertEquals(first.privacy.single().authoringGeometry, after.privacy.single().authoringGeometry)
            render(after) { actual ->
                // Rotating the source point (64, 64) about the center by 10° and
                // translating into the centered safe crop puts it near (57, 26).
                val x = actual.width / 2 - 63
                val y = actual.height / 2 - 11
                PrivacyCoverFixture.assertBox(actual, x - 1, y - 1, x + 2, y + 2, Color.BLACK)
            }
        }
    }

    @Test fun savedCollageKeepsMirroredCoverOnOnlyTheSelectedTileInDecodedPng() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val mirror = transition(legacy, legacy.copy(mirrored = true))
        val document = CollageDocument(
            photos = listOf(CollagePhoto("content://audit/left", mirror), CollagePhoto("content://audit/right")),
            layout = CollageLayout(template = CollageTemplate.SIDE_BY_SIDE, margin = 0f, gap = 0f,
                cornerRadius = 0f, border = 0f)
        )
        val name = "REG-01 cover " + System.nanoTime()
        try {
            assertTrue(CollageStore.save(context, name, document))
            val reopened = requireNotNull(CollageStore.load(context, name))
            val rendered = CollageRaster.renderWithLoader(reopened, 512, 128, preview = false) { _, _, _ ->
                PrivacyCoverFixture.source()
            }
            val bytes = try {
                ByteArrayOutputStream().use {
                    assertTrue(rendered.compress(Bitmap.CompressFormat.PNG, 100, it))
                    it.toByteArray()
                }
            } finally { rendered.recycle() }
            val actual = requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            try {
                assertEquals(512, actual.width)
                assertEquals(128, actual.height)
                PrivacyCoverFixture.assertBox(actual, 187, 61, 196, 67, Color.BLACK)
                PrivacyCoverFixture.assertBox(actual, 32, 19, 42, 28, PrivacyCoverFixture.gold)
                PrivacyCoverFixture.assertBox(actual, 60, 62, 68, 67, PrivacyCoverFixture.base)
                PrivacyCoverFixture.assertBox(actual, 256 + 60, 62, 256 + 68, 67, Color.MAGENTA)
            } finally { actual.recycle() }
        } finally { CollageStore.delete(context, name) }
    }

    private fun assertMapping(actual: Bitmap, width: Int, height: Int, source: (Int, Int) -> Pair<Int, Int>) {
        assertEquals(width, actual.width)
        assertEquals(height, actual.height)
        for (y in 0 until height) for (x in 0 until width) {
            val (sourceX, sourceY) = source(x, y)
            assertEquals("Pixel at ($x, $y)", PrivacyCoverFixture.pixel(sourceX, sourceY, covered = true),
                actual.getPixel(x, y))
        }
    }
}

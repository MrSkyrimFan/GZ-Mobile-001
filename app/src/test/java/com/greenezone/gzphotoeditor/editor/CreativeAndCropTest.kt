package com.greenezone.gzphotoeditor.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class CreativeAndCropTest {
    @Test fun aspectCropIsCenteredAndRespectsImagePixels() {
        val f = CropMath.initial(1920, 1080, CropRatio.PORTRAIT)
        val pixels = CropMath.pixelBounds(1920, 1080, f)
        assertTrue(abs(pixels[2].toFloat() / pixels[3] - .8f) < .002f)
        assertTrue(pixels[0] > 0)
        assertEquals(0, pixels[1])
        assertEquals(CropFrame(0f, 0f, 1f, 1f),
            CropMath.initial(1920, 1080, CropRatio.ORIGINAL))
    }

    @Test fun aFortyDpCropKeepsCornersEdgesAndItsInteriorDistinct() {
        val crop = CropFrame(0f, 0f, 1f, 1f)
        fun hit(x: Float, y: Float) = CropMath.hitTest(crop, x, y, 40f, 40f, 32f)
        assertEquals(CropHandle.TOP_LEFT, hit(0f, 0f))
        assertEquals(CropHandle.TOP, hit(20f, 0f))
        assertEquals(CropHandle.TOP, hit(20f, -24f))
        assertEquals(CropHandle.BOTTOM, hit(20f, 40f))
        assertEquals(CropHandle.LEFT, hit(0f, 20f))
        assertEquals(CropHandle.RIGHT, hit(40f, 20f))
        assertEquals(CropHandle.MOVE, hit(20f, 20f))
    }

    @Test fun lockedPanoramaRemainsSquareWhenSavedAndCanStillShrink() {
        val initial = CropMath.initial(4000, 100, CropRatio.SQUARE)
        val document = EditDocument(crop = CropRatio.SQUARE, cropFrame = initial,
            cropFrameRatioLocked = true)
        val reopened = EditDocumentCodec.decode(EditDocumentCodec.encode(document))!!
        assertEquals(document, reopened)
        assertTrue(CropMath.pixelBounds(4000, 100, reopened.cropFrame!!, reopened.crop)
            .contentEquals(intArrayOf(1950, 0, 100, 100)))

        val shrunk = CropMath.drag(initial, CropHandle.RIGHT, -.01f, .06f, 4000, 100, 1f)
        val bounds = CropMath.pixelBounds(4000, 100, shrunk, CropRatio.SQUARE)
        assertTrue(bounds[2] in 55..65)
        assertEquals(bounds[2], bounds[3])
        assertEquals(initial.left, shrunk.left, .00001f)
    }

    @Test fun lockedCropSnapsRoundingInwardWithoutForcingAnOlderArbitraryFrame() {
        val frame = CropFrame(.2f, .1f, .8f, .9f)
        val old = EditDocument(crop = CropRatio.PORTRAIT, cropFrame = frame)
        val reopened = EditDocumentCodec.decode(EditDocumentCodec.encode(old))!!
        assertEquals(old, reopened)
        assertTrue(CropMath.pixelBounds(1000, 500, reopened.cropFrame!!)
            .contentEquals(intArrayOf(200, 50, 600, 400)))
        assertTrue(CropMath.pixelBounds(1000, 501, frame, CropRatio.PORTRAIT)
            .contentEquals(intArrayOf(340, 50, 320, 400)))
    }

    @Test fun legacyCropGuideUsesTheSameOffCenterPositionAsTheRenderer() {
        val legacy = EditDocument(crop = CropRatio.SQUARE, cropCenterX = 1f)
        val guide = CropMath.displayFrame(200, 100, legacy)
        assertEquals(CropFrame(.5f, 0f, 1f, 1f), guide)
        assertTrue(CropMath.pixelBounds(200, 100, guide).contentEquals(
            CropMath.presetBounds(200, 100, legacy.crop, legacy.cropCenterX, legacy.cropCenterY)
        ))
    }

    @Test fun cropHandlesMaintainAspectAndStayWithinFrame() {
        val start = CropMath.initial(1200, 900, CropRatio.SQUARE)
        val next = CropMath.drag(start, CropHandle.BOTTOM_RIGHT, -.10f, -.06f,
            1200, 900, 1f)
        assertTrue(next.left >= 0f && next.top >= 0f)
        assertTrue(next.right <= 1f && next.bottom <= 1f)
        assertTrue(next.width < start.width)
        val a = CropMath.pixelBounds(1200, 900, next)
        assertTrue(abs(a[2] - a[3]) <= 2)
        val moved = CropMath.drag(next, CropHandle.MOVE, 100f, -100f,
            1200, 900, 1f)
        assertTrue(moved.left >= 0f && moved.top >= 0f)
        assertTrue(moved.right <= 1f && moved.bottom <= 1f)
    }

    @Test fun freeformCropCanResizeSidesIndependently() {
        val original = CropFrame(.1f, .1f, .9f, .9f)
        val next = CropMath.drag(original, CropHandle.TOP_LEFT, .15f, 0f,
            1200, 900, null)
        assertTrue(next.width < original.width)
        assertEquals(original.height, next.height, .00001f)
        assertEquals(original.bottom, next.bottom, .00001f)
    }

    @Test fun cropPixelBoundsNeverHaveZeroDimensions() {
        val degenerate = CropFrame(Float.NaN, -.3f, Float.POSITIVE_INFINITY, 2f)
        val pixel = CropMath.pixelBounds(31, 20, degenerate)
        assertTrue(pixel[2] > 0 && pixel[3] > 0)
        assertTrue(pixel[0] >= 0 && pixel[1] >= 0)
        assertTrue(pixel[0] + pixel[2] <= 31)
        assertTrue(pixel[1] + pixel[3] <= 20)
    }

    @Test fun unicodeTextFontShapeDrawingAndCropSurviveCodec() {
        val source = EditDocument(
            crop = CropRatio.FREE,
            cropFrame = CropFrame(.12f, .13f, .78f, .92f),
            creative = CreativeDocument(
                text = listOf(TextOverlay(content = "Hello = world\nCafé ✓",
                    x = .44f, y = .3f, width = .67f, height = .21f,
                    font = TextFont.SCRIPT, size = .11f, bold = true,
                    italic = true, opacity = .65f, outline = .5f,
                    outlineColor = 0xFF111111.toInt(),
                    letterSpacing = .12f, align = TextAlignment.LEFT)),
                strokes = listOf(InkStroke(points = listOf(
                    BrushPoint(.1f, .2f), BrushPoint(.5f, .6f),
                    BrushPoint(.85f, .4f)
                ), color = 0xFFFF0000.toInt(), thickness = .023f, opacity = .77f)),
                shapes = listOf(ShapeOverlay(kind = ShapeKind.STAR,
                    rotation = 28f, color = 0xFF00FF00.toInt(),
                    width = .54f, height = .36f, filled = true))
            )
        )
        val encoded = EditDocumentCodec.encode(source)
        assertEquals(source, EditDocumentCodec.decode(encoded))
        assertEquals(encoded, EditDocumentCodec.encode(source))
    }

    @Test fun oldProjectsWithoutCreativeFieldsKeepOriginalValues() {
        val legacy = EditDocumentCodec.decode(
            "version=1\ncontrol.Exposure=0.3\ncropRatio=PORTRAIT\nprivacy.count=0"
        )!!
        assertEquals(.3f, legacy.grade.exposure, .0001f)
        assertEquals(CropRatio.PORTRAIT, legacy.crop)
        assertNull(legacy.cropFrame)
        assertEquals(CreativeDocument(), legacy.creative)
    }

    @Test fun editingAndErasingLeavesSurvivingVectorSegments() {
        val ink = InkStroke(
            points = (0..20).map { BrushPoint(it / 20f, .5f) },
            color = 0xFF00FF00.toInt()
        )
        val erased = InkMath.erase(listOf(ink), .5f, .5f, .08f)
        assertEquals(2, erased.size)
        assertTrue(erased.all { it.points.isNotEmpty() })
        assertTrue(erased.sumOf { it.points.size } < ink.points.size)
        assertEquals(ink.color, erased.first().color)
    }

    @Test fun socialSizesAreExplicitAndReasonable() {
        assertEquals(1080, SocialPreset.INSTAGRAM_PORTRAIT.width)
        assertEquals(1350, SocialPreset.INSTAGRAM_PORTRAIT.height)
        assertEquals(1080, SocialPreset.TIKTOK_PHOTO.width)
        assertEquals(1920, SocialPreset.TIKTOK_PHOTO.height)
        assertEquals(3840, SocialPreset.YOUTUBE_THUMBNAIL_4K.width)
        assertEquals(2160, SocialPreset.YOUTUBE_THUMBNAIL_4K.height)
        assertTrue(SocialPreset.entries.size >= 12)
        assertTrue(SocialPreset.entries.filter { it != SocialPreset.ORIGINAL }
            .all { it.width > 0 && it.height > 0 && it.width.toLong() * it.height <= 24_000_000L })
    }

    @Test fun exportFormatChoicesHaveActualMimesAndExtensions() {
        assertEquals(7, ImageExportFormat.entries.size)
        assertEquals("image/tiff", ImageExportFormat.TIFF.mime)
        assertEquals("image/bmp", ImageExportFormat.BMP.mime)
        assertEquals("application/pdf", ImageExportFormat.PDF.mime)
        assertEquals("webp", ImageExportFormat.WEBP_LOSSLESS.extension)
        assertTrue(ImageExportFormat.entries.all {
            it.mime.contains('/') && it.extension.isNotBlank()
        })
    }
}

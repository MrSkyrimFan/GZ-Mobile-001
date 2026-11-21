package com.greenezone.gzphotoeditor.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductionWaveMathTest {
    private fun pixel(r: Int, g: Int = r, b: Int = r) =
        (0xFF shl 24) or (r shl 16) or (g shl 8) or b

    @Test fun brushMaskPaintsOnlyAlongStrokeAndPreservesAlpha() {
        val brush = LocalMask(
            kind = MaskKind.BRUSH, radius = .07f, feather = .2f, exposure = .85f,
            strokes = listOf(BrushStroke(listOf(
                BrushPoint(.1f, .3f), BrushPoint(.5f, .3f), BrushPoint(.9f, .3f)
            )))
        )
        val mask = MaskMath.prepare(listOf(brush))
        assertTrue(MaskMath.coverage(brush, 50, 30, 100, 100) > .95f)
        assertTrue(MaskMath.coverage(brush, 50, 70, 100, 100) < .01f)
        val base = 0x7F555555
        val enhanced = MaskMath.adjust(base, mask, 50, 30, 100, 100)
        assertTrue((enhanced and 255) > (base and 255))
        assertEquals(base ushr 24, enhanced ushr 24)
        assertEquals(base, MaskMath.adjust(base, mask, 50, 70, 100, 100))
    }

    @Test fun brushSupportsSinglePointAndSkipsInvalidCoordinates() {
        val mask = LocalMask(kind = MaskKind.BRUSH, radius = .06f,
            strokes = listOf(BrushStroke(listOf(BrushPoint(.5f, .5f)))))
        assertTrue(MaskMath.coverage(mask, 50, 50, 101, 101) > .99f)
        val invalid = mask.copy(strokes = listOf(BrushStroke(
            listOf(BrushPoint(Float.NaN, .5f), BrushPoint(1.4f, .5f))
        )))
        assertEquals(0f, MaskMath.coverage(invalid, 50, 50, 101, 101), 0f)
    }

    @Test fun detailNoiseReductionSmoothsSpecklesWithoutChangingAlpha() {
        val rows = Array(5) { IntArray(5) { pixel(100) } }
        rows[2][2] = 0x7F787878
        assertEquals(0x7F787878, DetailMath.denoise(rows, 2, 0f))
        val result = DetailMath.denoise(rows, 2, 1f)
        assertTrue((result and 255) < 120)
        assertTrue((result and 255) > 100)
        assertEquals(0x7F, result ushr 24)
    }

    @Test fun noiseReducerResistsStrongEdges() {
        val rows = Array(5) { y -> IntArray(5) { x ->
            if (x < 2) pixel(8) else pixel(245)
        } }
        val edge = DetailMath.denoise(rows, 1, 1f)
        assertTrue((edge and 255) < 25)
        val bright = DetailMath.denoise(rows, 3, 1f)
        assertTrue((bright and 255) > 220)
    }

    @Test fun sharpeningThresholdGatesLowContrastNoise() {
        val rows = Array(5) { IntArray(5) { pixel(120) } }
        rows[2][2] = pixel(122)
        val loose = Grade(sharpen = 1f, sharpenThreshold = 0f)
        val strict = Grade(sharpen = 1f, sharpenThreshold = 1f)
        val unrestricted = DetailMath.detailDelta(rows, 2, loose)
        val guarded = DetailMath.detailDelta(rows, 2, strict)
        assertTrue(unrestricted > 0f)
        assertEquals(0f, guarded, 0.00001f)
    }

    @Test fun acceleratedDetailMathMatchesFiveRowReferenceAtEveryPixel() {
        val width = 17
        val grade = Grade(sharpen = .76f, texture = .28f, clarity = .4f, sharpenThreshold = .07f)
        val rows = Array(5) { row ->
            IntArray(width) { x ->
                val value = (20 + (x * x * 3 + row * 41) % 220).coerceIn(0, 255)
                pixel(value, (value + 40).coerceAtMost(255), (value / 2))
            }
        }
        val luma = Array(5) { row -> FloatArray(width) { x -> DetailMath.luminance(rows[row][x]) } }
        val v3 = FloatArray(width) { x -> luma[1][x] + luma[2][x] + luma[3][x] }
        val v5 = FloatArray(width) { x -> v3[x] + luma[0][x] + luma[4][x] }
        var h3 = v3[0] * 2f + v3[1]
        var h5 = v5[0] * 3f + v5[1] + v5[2]
        for (x in 0 until width) {
            val reference = DetailMath.detailDelta(rows, x, grade)
            val accelerated = DetailMath.detailDeltaFromAverages(
                luma[2][x], h3 / 9f, h5 / 25f, grade
            )
            assertEquals("detail x=$x", reference, accelerated, .000015f)
            h3 += v3[minOf(width - 1, x + 2)] - v3[maxOf(0, x - 1)]
            h5 += v5[minOf(width - 1, x + 3)] - v5[maxOf(0, x - 2)]
        }
    }

    @Test fun cropOriginRespectsUserCenterAndBounds() {
        assertEquals(0 to 50, GeometryMath.cropOrigin(150, 100, 100, 50, 0f, 1f))
        assertEquals(25 to 25, GeometryMath.cropOrigin(150, 100, 100, 50, .5f, .5f))
        assertEquals(50 to 0, GeometryMath.cropOrigin(150, 100, 100, 50, 1f, 0f))
        assertEquals(25 to 25, GeometryMath.cropOrigin(150, 100, 100, 50, Float.NaN, Float.NaN))
    }

    @Test fun straightenAutoCropNeverExtendsPastOriginalImage() {
        assertEquals(1200 to 800, GeometryMath.safeStraightenCrop(1200, 800, 0f))
        for (angle in listOf(-15f, -8f, 5f, 11f, 15f)) {
            val (w, h) = GeometryMath.safeStraightenCrop(1200, 800, angle)
            assertTrue(w in 1..1200)
            assertTrue(h in 1..800)
            assertTrue(w < 1200 && h < 800)
            assertTrue(kotlin.math.abs(w.toFloat() / h - 1.5f) < .01f)
        }
    }

    @Test fun dynamicExportBudgetIsBoundedAndMonotonic() {
        val amounts = listOf(64, 128, 192, 256, 384, 512, 1024)
        val limits = amounts.map(GeometryMath::safeExportPixels)
        assertTrue(limits.all { it in 2_000_000L..24_000_000L })
        assertEquals(limits.sorted(), limits)
        assertEquals(24_000_000L, limits.last())
    }

    @Test fun fullEditableProjectIsRoundTrippable() {
        val blue = ControlCatalog.mixer.getValue(ColorBand.BLUE)[0]
        val source = EditDocument(
            grade = blue.write(Grade(exposure = .34f, denoise = .56f,
                texture = -.2f, sharpenThreshold = .12f,
                masterCurve = CurvePoints().change(2, .83f)), .25f),
            quarterTurns = 3, mirrored = true, flipVertical = true,
            crop = CropRatio.WIDE, cropCenterX = .18f, cropCenterY = .76f,
            straightenDegrees = -6.25f,
            masks = listOf(
                LocalMask(kind = MaskKind.BRUSH, exposure = .5f, radius = .07f,
                    strokes = listOf(BrushStroke(listOf(
                        BrushPoint(.15f, .36f), BrushPoint(.35f, .44f)
                    )))),
                LocalMask(kind = MaskKind.LINEAR, contrast = -.3f)
            )
        )
        val encoded = EditDocumentCodec.encode(source)
        val decoded = EditDocumentCodec.decode(encoded)
        assertEquals(source, decoded)
        assertEquals(encoded, EditDocumentCodec.encode(source))
        assertTrue(encoded.contains("mask.0.stroke.0"))
    }

    @Test fun invalidAndOversizedProjectDataFailsClosed() {
        assertNull(EditDocumentCodec.decode("version=999\nrotation=2"))
        assertNull(EditDocumentCodec.decode("version=1" + "X".repeat(510_000)))
        val read = EditDocumentCodec.decode(
            "version=1\nrotation=999\ncropX=NaN\nstraighten=9999\n" +
                "mask.count=9999\ncontrol.0=Infinity\ncropRatio=DOES_NOT_EXIST"
        )!!
        assertEquals(3, read.quarterTurns)
        assertEquals(0f, read.grade.exposure, 0f)
        assertEquals(.5f, read.cropCenterX, 0f)
        assertEquals(15f, read.straightenDegrees, 0f)
        assertEquals(CropRatio.ORIGINAL, read.crop)
    }

    @Test fun autoToneIsConservativeOnFlatOrAlreadyBalancedImages() {
        assertEquals(0f, AutoEnhanceMath.autoTone(IntArray(64)).exposure, 0f)
        val flat = IntArray(64).also { it[20] = 1000 }
        assertEquals(0f, AutoEnhanceMath.autoTone(flat).exposure, 0f)
        val balanced = IntArray(64) { if (it in 8..55) 30 else 0 }
        val grade = AutoEnhanceMath.autoTone(balanced)
        assertTrue(grade.exposure in -.45f.. .45f)
        assertTrue(grade.contrast in -.28f.. .35f)
    }

    @Test fun neutralDenoiseAndBrushAreNotAccidentalNoOps() {
        val mask = LocalMask(kind = MaskKind.BRUSH, exposure = .7f,
            strokes = listOf(BrushStroke(listOf(BrushPoint(.5f, .5f)))))
        assertFalse(MaskMath.prepare(listOf(mask)).isEmpty())
        assertTrue(ControlCatalog.all.any { it.name == "Noise reduction" })
        assertTrue(ControlCatalog.all.any { it.name == "Sharpen threshold" })
        assertTrue(ControlCatalog.all.any { it.name == "Texture" })
        assertEquals(56, ControlCatalog.all.size)
    }
}

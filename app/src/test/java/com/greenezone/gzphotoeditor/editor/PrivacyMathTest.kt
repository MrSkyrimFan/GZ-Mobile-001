package com.greenezone.gzphotoeditor.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyMathTest {
    @Test fun blackBarCoordinatesStayInsideImage() {
        val mark = PrivacyEdit(kind = PrivacyKind.BLACK_BAR, centerX = .95f,
            centerY = .05f, width = .4f, height = .3f)
        val b = PrivacyMath.bounds(mark, 100, 80)
        assertTrue(b.left in 0..100 && b.right in 0..100)
        assertTrue(b.top in 0..80 && b.bottom in 0..80)
        assertEquals(100, b.right)
        assertEquals(0, b.top)
    }

    @Test fun solidRectHasFullCoverageWithoutFeather() {
        val mark = PrivacyEdit(kind = PrivacyKind.BLACK_BAR,
            centerX = .5f, centerY = .5f, width = .4f, height = .2f,
            feather = .8f)
        assertEquals(1f, PrivacyMath.rectCoverage(mark, 50, 50, 100, 100), .00001f)
        assertEquals(1f, PrivacyMath.rectCoverage(mark, 30, 45, 100, 100), .00001f)
        assertEquals(0f, PrivacyMath.rectCoverage(mark, 0, 0, 100, 100), .00001f)
    }

    @Test fun blurRectangleFeatherSmoothlyFallsOff() {
        val mark = PrivacyEdit(kind = PrivacyKind.BLUR, centerX = .5f,
            centerY = .5f, width = .6f, height = .6f, feather = .30f)
        val center = PrivacyMath.rectCoverage(mark, 50, 50, 100, 100)
        val edge = PrivacyMath.rectCoverage(mark, 21, 50, 100, 100)
        val outside = PrivacyMath.rectCoverage(mark, 5, 50, 100, 100)
        assertEquals(1f, center, .00001f)
        assertTrue(edge in 0f..1f)
        assertTrue(edge < center)
        assertEquals(0f, outside, .00001f)
    }

    @Test fun brushBoundsFollowPaintedStrokeAndClip() {
        val mark = PrivacyEdit(kind = PrivacyKind.BLACK_BRUSH,
            brushSize = .08f, strokes = listOf(BrushStroke(listOf(
                BrushPoint(.10f, .20f), BrushPoint(.35f, .28f)
            ))))
        val b = PrivacyMath.bounds(mark, 100, 100)
        assertTrue(b.left <= 10)
        assertTrue(b.right >= 35)
        assertTrue(b.top <= 20)
        assertTrue(b.bottom >= 28)
        assertTrue(b.right < 60)
    }

    @Test fun emptyStrokeProducesEmptyBounds() {
        val empty = PrivacyEdit(kind = PrivacyKind.BLUR_BRUSH)
        assertTrue(PrivacyMath.bounds(empty, 100, 100).empty)
        assertTrue(PrivacyKind.BLUR_BRUSH.usesBrush)
        assertTrue(PrivacyKind.BLACK_BRUSH.usesBrush)
        assertFalse(PrivacyKind.BLACK_BAR.usesBrush)
    }

    @Test fun privacyEditsRoundTripThroughSavedProjectCodec() {
        val source = EditDocument(
            grade = Grade(exposure = .25f),
            privacy = listOf(
                PrivacyEdit(kind = PrivacyKind.BLUR, centerX = .22f, centerY = .39f,
                    width = .30f, height = .22f, strength = .75f),
                PrivacyEdit(kind = PrivacyKind.BLACK_BAR, centerX = .8f, centerY = .4f,
                    width = .2f, height = .14f),
                PrivacyEdit(kind = PrivacyKind.BLUR_BRUSH, brushSize = .045f,
                    strokes = listOf(BrushStroke(listOf(
                        BrushPoint(.1f, .2f), BrushPoint(.32f, .4f)
                    ))))
            )
        )
        val encoded = EditDocumentCodec.encode(source)
        assertTrue(encoded.contains("privacy.count=3"))
        val decoded = EditDocumentCodec.decode(encoded)!!
        assertEquals(source, decoded)
        assertEquals(encoded, EditDocumentCodec.encode(decoded))
    }

    @Test fun legacyProjectWithoutPrivacyStillLoads() {
        val doc = EditDocumentCodec.decode("version=1\nrotation=1\ncontrol.Exposure=0.3")!!
        assertTrue(doc.privacy.isEmpty())
        assertEquals(1, doc.quarterTurns)
        assertEquals(.3f, doc.grade.exposure, .00001f)
    }

    @Test fun maliciousOversizedPrivacyCountRemainsBounded() {
        val invalid = EditDocumentCodec.decode(
            "version=1\nprivacy.count=999999\nprivacy.0.kind=BLACK_BAR" +
                "\nprivacy.0.width=NaN\nprivacy.0.height=Infinity"
        )!!
        assertTrue(invalid.privacy.size <= PrivacyRaster.MAX_EDITS)
        assertTrue(invalid.privacy[0].width.isFinite())
        assertTrue(invalid.privacy[0].height.isFinite())
    }
}

package com.greenezone.gzphotoeditor.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import kotlin.math.pow

class CurvesAndMasksTest {
    private val gray = 0xFF686868.toInt()

    @Test fun identityCurvesHaveNoLutAndPreservePixel() {
        val prepared = CurveMath.prepare(Grade())
        assertTrue(prepared.isNeutral)
        assertEquals(0xD02A8CE1.toInt(), prepared.apply(0xD02A8CE1.toInt()))
        assertEquals(0f, CurveMath.evaluate(CurvePoints(), 0f), 0f)
        assertEquals(1f, CurveMath.evaluate(CurvePoints(), 1f), 0f)
        assertEquals(.5f, CurveMath.evaluate(CurvePoints(), .5f), .0001f)
    }

    @Test fun masterCurveBrightensMidtonesWithoutTouchingAlpha() {
        val modified = CurvePoints().change(2, .85f)
        val grade = Grade(masterCurve = modified)
        val pixel = 0xA0646464.toInt()
        val changed = ToneMath.adjust(pixel, grade, 3, 4, 10, 10)
        assertTrue(((changed ushr 16) and 255) > ((pixel ushr 16) and 255))
        assertEquals(pixel ushr 24, changed ushr 24)
        assertEquals(0xFF000000.toInt(), ToneMath.adjust(0xFF000000.toInt(), grade, 0, 0, 5, 5))
    }

    @Test fun redChannelCurveDoesNotChangeGreenOrBlue() {
        val grade = Grade(redCurve = CurvePoints().change(2, .8f))
        val source = 0xFF777777.toInt()
        val result = ToneMath.adjust(source, grade, 0, 0, 1, 1)
        assertNotEquals((source ushr 16) and 255, (result ushr 16) and 255)
        assertEquals((source ushr 8) and 255, (result ushr 8) and 255)
        assertEquals(source and 255, result and 255)
    }

    @Test fun channelSwitchesPreserveOtherCurves() {
        val a = CurveMath.put(Grade(), CurveChannel.RED, CurvePoints().change(1, .5f))
        val b = CurveMath.put(a, CurveChannel.BLUE, CurvePoints().change(3, .45f))
        assertEquals(.5f, CurveMath.get(b, CurveChannel.RED).values[1], .00001f)
        assertEquals(.45f, CurveMath.get(b, CurveChannel.BLUE).values[3], .00001f)
        assertEquals(CurveMath.identity, CurveMath.get(b, CurveChannel.GREEN))
    }

    @Test fun smoothCurveStaysWithinRangeWithoutOvershoot() {
        val points = CurvePoints(listOf(.1f, .12f, .5f, .74f, .99f))
        var previous = -1f
        for (i in 0..1024) {
            val value = CurveMath.evaluate(points, i / 1024f)
            assertTrue(value in 0f..1f)
            assertTrue(value + .00001f >= previous)
            previous = value
        }
    }

    @Test fun radialCoverageCentersOnRegion() {
        val mask = LocalMask(kind = MaskKind.RADIAL, centerX = .5f, centerY = .5f, radius = .25f)
        assertTrue(MaskMath.coverage(mask, 50, 50, 101, 101) > .99f)
        assertEquals(0f, MaskMath.coverage(mask, 0, 0, 101, 101), 0f)
    }

    @Test fun linearGradientFadesFromTopToBottom() {
        val mask = LocalMask(kind = MaskKind.LINEAR, centerX = .5f, centerY = .5f, radius = .22f)
        assertTrue(MaskMath.coverage(mask, 50, 1, 101, 101) > .99f)
        assertTrue(MaskMath.coverage(mask, 50, 50, 101, 101) > .48f)
        assertEquals(0f, MaskMath.coverage(mask, 50, 99, 101, 101), 0f)
    }

    @Test fun localExposureChangesOnlyCoveredRegionAndKeepsAlpha() {
        val mask = LocalMask(exposure = 1f, radius = .3f)
        val prepared = MaskMath.prepare(listOf(mask))
        val source = 0x7F414141
        val center = MaskMath.adjust(source, prepared, 50, 50, 101, 101)
        val edge = MaskMath.adjust(source, prepared, 0, 0, 101, 101)
        assertTrue((center and 255) > (source and 255))
        assertEquals(source, edge)
        assertEquals(source ushr 24, center ushr 24)
    }

    @Test fun neutralMaskHasNoProcessingAndMaskListIsBounded() {
        assertTrue(MaskMath.prepare(listOf(LocalMask())).isEmpty())
        val many = List(8) { LocalMask(exposure = .3f) }
        assertEquals(4, MaskMath.prepare(many).size)
        assertEquals(gray, MaskMath.adjust(gray, emptyList(), 50, 50, 101, 101))
    }

    @Test fun gammaLookupMatchesAnalyticalPowerWithinTwoLevels() {
        val samples = listOf(-.8f, -.3f, .35f, .9f)
        for (gamma in samples) {
            val prepared = ToneMath.prepare(Grade(gamma = gamma))
            val exponent = 2.0.pow((-gamma * .6f).toDouble())
            for (input in 0..255) {
                val argb = 0xAF000000.toInt() or (input shl 16) or (input shl 8) or input
                val actual = ToneMath.adjustPrepared(argb, prepared, 1, 1, 3, 3)
                val expected = ((input / 255.0).pow(exponent) * 255.0 + .5).toInt()
                val observed = actual and 255
                assertTrue("gamma=$gamma input=$input output=$observed expected=$expected",
                    kotlin.math.abs(observed - expected) <= 2)
                assertEquals(0xAF, actual ushr 24)
            }
        }
    }

    @Test fun maskAndCurvesBothParticipateInDocumentHistory() {
        val original = EditDocument()
        val changed = original.copy(
            grade = Grade(masterCurve = CurvePoints().change(2, .7f)),
            masks = listOf(LocalMask(kind = MaskKind.RADIAL, exposure = .6f))
        )
        val history = EditHistory().checkpoint(original, changed)
        val (previous, undone) = history.undo(changed)!!
        assertEquals(original, previous)
        assertEquals(changed, undone.redo(previous)!!.first)
        assertFalse(changed.masks.isEmpty())
    }
}

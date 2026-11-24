package com.greenezone.gzphotoeditor.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class InkMathGeometryTest {
    private fun line(y: Float = .5f, thickness: Float = .012f) =
        InkStroke(listOf(BrushPoint(.1f, y), BrushPoint(.9f, y)),
            color = 0xFF3471AD.toInt(), thickness = thickness, opacity = .65f)

    @Test fun noHitAndTangentLeaveTheExactDocumentUntouched() {
        val input = listOf(line())
        val miss = InkMath.eraseBounded(input, .5f, .8f, .05f)
        assertFalse(miss.capacityExceeded)
        assertSame(input, miss.strokes)
        val tangent = listOf(line(y = .5f + .05f + .006f))
        for (previous in listOf<BrushPoint?>(null, BrushPoint(.2f, .5f))) {
            val result = InkMath.eraseBounded(tangent, .7f, .5f, .05f, previous = previous)
            assertSame("A tangent must not manufacture a history change", tangent, result.strokes)
            assertFalse(result.capacityExceeded)
        }
    }

    @Test fun retainedRoundCapsStayOutsideTheErasedDisk() {
        val input = line(thickness = .08f)
        val result = InkMath.eraseBounded(listOf(input), .5f, .5f, .05f)
        assertFalse(result.capacityExceeded)
        assertEquals(2, result.strokes.size)
        val left = result.strokes[0].points.last().x
        val right = result.strokes[1].points.first().x
        assertEquals(.41f, left, .00001f)
        assertEquals(.59f, right, .00001f)
        assertTrue(left + input.thickness / 2 <= .45001f)
        assertTrue(right - input.thickness / 2 >= .54999f)
    }

    @Test fun shortEdgeUnitsMatchLandscapePortraitAndSquareRendering() {
        for (aspect in listOf(.5f, 1f, 2f)) {
            val scaleX = maxOf(1f, aspect)
            val horizontal = InkMath.eraseBounded(listOf(line()), .5f, .5f, .05f, aspect)
            assertEquals(2, horizontal.strokes.size)
            assertEquals(.056f, (.5f - horizontal.strokes[0].points.last().x) * scaleX, .00001f)
            assertEquals(.056f, (horizontal.strokes[1].points.first().x - .5f) * scaleX, .00001f)
            val vertical = InkStroke(listOf(BrushPoint(.5f, .1f), BrushPoint(.5f, .9f)))
            val erased = InkMath.eraseBounded(listOf(vertical), .5f, .5f, .05f, aspect)
            val scaleY = maxOf(1f, 1f / aspect)
            assertEquals(2, erased.strokes.size)
            assertEquals(.056f, (.5f - erased.strokes[0].points.last().y) * scaleY, .00001f)
            assertEquals(.056f, (erased.strokes[1].points.first().y - .5f) * scaleY, .00001f)
        }
    }

    @Test fun fastSweepCutsItsMiddleEvenWhenBothPointerEndpointsMiss() {
        val input = listOf(line())
        val firstOnly = InkMath.eraseBounded(input, .5f, .2f, .03f)
        val lastOnly = InkMath.eraseBounded(input, .5f, .8f, .03f)
        assertSame(input, firstOnly.strokes)
        assertSame(input, lastOnly.strokes)
        val swept = InkMath.eraseBounded(input, .5f, .8f, .03f,
            previous = BrushPoint(.5f, .2f))
        assertEquals(2, swept.strokes.size)
        assertEquals(.464f, swept.strokes[0].points.last().x, .00001f)
        assertEquals(.536f, swept.strokes[1].points.first().x, .00001f)
    }

    @Test fun diagonalSweepAndItsReverseHaveTheSameVisibleFragments() {
        val input = listOf(line())
        for (aspect in listOf(.5625f, 1f, 2f)) {
            val forward = InkMath.eraseBounded(input, .8f, .8f, .04f, aspect,
                BrushPoint(.2f, .2f))
            val reverse = InkMath.eraseBounded(input, .2f, .2f, .04f, aspect,
                BrushPoint(.8f, .8f))
            assertEquals(2, forward.strokes.size)
            assertEquals(2, reverse.strokes.size)
            for (index in 0..1) {
                assertEquals(forward.strokes[index].points.size, reverse.strokes[index].points.size)
                for (point in forward.strokes[index].points.indices) {
                    assertEquals(forward.strokes[index].points[point].x,
                        reverse.strokes[index].points[point].x, .00001f)
                    assertEquals(forward.strokes[index].points[point].y,
                        reverse.strokes[index].points[point].y, .00001f)
                }
            }
        }
    }

    @Test fun zeroLengthSweepAndDuplicatePolylinePointsAreWellDefined() {
        val p = BrushPoint(.5f, .5f)
        val stroke = line().copy(points = listOf(BrushPoint(.1f, .5f), p, p,
            BrushPoint(.9f, .5f)))
        val disk = InkMath.eraseBounded(listOf(stroke), p.x, p.y, .05f)
        val stationary = InkMath.eraseBounded(listOf(stroke), p.x, p.y, .05f, previous = p)
        assertEquals(disk, stationary)
        assertEquals(2, disk.strokes.size)
        assertTrue(disk.strokes.all { it.points.distinct().size == it.points.size })
    }

    @Test fun singletonAndFullyCoveredStrokesAreRemovedWithoutEmptyFragments() {
        val dot = InkStroke(listOf(BrushPoint(.5f, .5f)), thickness = .04f)
        val miss = InkMath.eraseBounded(listOf(dot), .8f, .8f, .05f)
        assertEquals(listOf(dot), miss.strokes)
        val short = InkStroke(listOf(BrushPoint(.49f, .5f), BrushPoint(.51f, .5f)))
        val hit = InkMath.eraseBounded(listOf(dot, short), .5f, .5f, .05f)
        assertEquals(emptyList<InkStroke>(), hit.strokes)
        assertFalse(hit.capacityExceeded)
    }

    @Test fun repeatingTheSameEraseIsANoOpAndCannotJoinTheGap() {
        val first = InkMath.eraseBounded(listOf(line()), .5f, .5f, .05f)
        val repeated = InkMath.eraseBounded(first.strokes, .5f, .5f, .05f)
        assertSame(first.strokes, repeated.strokes)
        assertFalse(repeated.capacityExceeded)
        assertTrue(repeated.strokes[0].points.last().x < repeated.strokes[1].points.first().x)
    }

    @Test fun laterOverflowIsReportedWithoutDiscardingAnyInputStroke() {
        val last = InkStroke(listOf(BrushPoint(.7f, .8f), BrushPoint(.9f, .8f)), color = 33)
        val original = listOf(line(), last)
        val first = InkMath.eraseBounded(original, .3f, .5f, .025f, maxStrokes = 3)
        assertFalse(first.capacityExceeded)
        assertEquals(3, first.strokes.size)
        val second = InkMath.eraseBounded(first.strokes, .7f, .5f, .025f, maxStrokes = 3)
        assertTrue(second.capacityExceeded)
        assertSame("Gesture owner receives intact input for a full rollback", first.strokes, second.strokes)
        assertEquals(last, second.strokes.last())
    }

    @Test fun fullRemovalAtCapacityAndLaterRemovedStrokesStillPermitTheResult() {
        val dot = InkStroke(listOf(BrushPoint(.5f, .5f)), color = 47)
        // The first stroke splits into two, but the later dot is removed, so the
        // final result fits. Do not reject by counting unprocessed inputs.
        val fits = InkMath.eraseBounded(listOf(line(), dot), .5f, .5f, .05f, maxStrokes = 2)
        assertFalse(fits.capacityExceeded)
        assertEquals(2, fits.strokes.size)
        val full = List(160) { dot }
        val removed = InkMath.eraseBounded(full, .5f, .5f, .05f)
        assertFalse(removed.capacityExceeded)
        assertTrue(removed.strokes.isEmpty())
    }

    @Test fun invalidOverBudgetInputIsNeverSilentlyRewritten() {
        val tooMany = List(161) { line() }
        val rejected = InkMath.eraseBounded(tooMany, .5f, .5f, .05f)
        assertTrue(rejected.capacityExceeded)
        assertSame(tooMany, rejected.strokes)
        val tooManyPoints = listOf(line().copy(points = List(501) { BrushPoint(.5f, .5f) }))
        val pointRejection = InkMath.eraseBounded(tooManyPoints, .5f, .5f, .05f)
        assertTrue(pointRejection.capacityExceeded)
        assertSame(tooManyPoints, pointRejection.strokes)
    }

    @Test fun fragmentAttributesAndPointBudgetSurviveRepeatedCuts() {
        val source = InkStroke((0..499).map { BrushPoint(.05f + .9f * it / 499f, .5f) },
            color = 0xFF716355.toInt(), thickness = .008f, opacity = .42f, erase = true)
        var result = listOf(source)
        for (x in listOf(.2f, .4f, .6f, .8f)) {
            val cut = InkMath.eraseBounded(result, x, .5f, .012f)
            assertFalse(cut.capacityExceeded)
            result = cut.strokes
        }
        assertEquals(5, result.size)
        result.forEach {
            assertEquals(source.color, it.color)
            assertEquals(source.thickness, it.thickness)
            assertEquals(source.opacity, it.opacity)
            assertEquals(source.erase, it.erase)
            assertTrue(it.points.isNotEmpty() && it.points.size <= 500)
        }
    }
}

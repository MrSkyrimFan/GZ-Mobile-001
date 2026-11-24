package com.greenezone.gzphotoeditor.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InkMathRegressionTest {
    @Test fun simplifiedLongStrokeCanBeErasedBetweenItsStoredVertices() {
        var points = emptyList<BrushPoint>()
        for (index in 0..1000) {
            points = ProStrokeSampling.append(
                points, BrushPoint(.1f + .8f * index / 1000f, .5f), 1f
            )
        }
        assertEquals(500, points.size)
        assertTrue(points[0].x < .3f && points[1].x > .3f)
        val source = InkStroke(points, color = 0xFF2477AA.toInt(), thickness = .012f,
            opacity = .7f)
        val erased = InkMath.erase(listOf(source), .3f, .5f, .05f)
        assertEquals("A visible segment must be cut even without a nearby vertex", 2, erased.size)
        assertTrue(erased[0].points.last().x < .25f)
        assertTrue(erased[1].points.first().x > .35f)
        assertEquals(points.first(), erased.first().points.first())
        assertEquals(points.last(), erased.last().points.last())
        erased.forEach {
            assertEquals(source.color, it.color)
            assertEquals(source.thickness, it.thickness)
            assertEquals(source.opacity, it.opacity)
            assertTrue(it.points.size <= 500)
        }
    }

    @Test fun splittingAtTheStrokeLimitNeverOmitsUnrelatedArtwork() {
        val first = InkStroke(listOf(BrushPoint(.1f, .1f), BrushPoint(.2f, .1f),
            BrushPoint(.3f, .1f)), color = 1)
        val untouched = (1..159).map { index ->
            InkStroke(listOf(BrushPoint(.6f, .5f + index * .001f),
                BrushPoint(.8f, .5f + index * .001f)), color = 1000 + index)
        }
        val original = listOf(first) + untouched
        assertEquals(160, original.size)
        val erased = InkMath.erase(original, .2f, .1f, .04f)
        assertTrue("The accepted result must fit the renderer and saved recipe", erased.size <= 160)
        assertTrue("The final unrelated stroke must survive", untouched.last() in erased)
        assertEquals("A capacity rejection must preserve the complete input", original, erased)
    }
}

package com.greenezone.gzphotoeditor.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProStrokeSamplingTest {
    @Test fun longContinuousPathKeepsItsLateBendAndEndpointWithinTheRecipeBudget() {
        val first = BrushPoint(.12f, .32f)
        val bend = BrushPoint(.8f, .32f)
        val tail = BrushPoint(.8f, .75f)
        var points = listOf(first)
        repeat(700) { index ->
            points = ProStrokeSampling.append(points,
                BrushPoint(first.x + (bend.x - first.x) * ((index + 1) / 700f), first.y), 1f)
        }
        repeat(180) { index ->
            points = ProStrokeSampling.append(points,
                BrushPoint(bend.x, bend.y + (tail.y - bend.y) * ((index + 1) / 180f)), 1f)
        }
        assertEquals(ProStrokeSampling.MAX_POINTS, points.size)
        assertEquals(first, points.first())
        assertEquals(tail, points.last())
        assertTrue(points.contains(bend))
        val recipe = EditDocument(creative = CreativeDocument(strokes = listOf(InkStroke(points))))
        assertEquals(recipe, EditDocumentCodec.decode(EditDocumentCodec.encode(recipe)))
    }

    @Test fun lassoBudgetRetainsTheCompleteClosedContourAndArea() {
        val radius = .3
        val count = 620
        val first = BrushPoint(.8f, .5f)
        var points = listOf(first)
        for (i in 1..count) {
            val angle = 2 * kotlin.math.PI * i / count
            points = ProStrokeSampling.append(points,
                BrushPoint((.5 + radius * kotlin.math.cos(angle)).toFloat(),
                    (.5 + radius * kotlin.math.sin(angle)).toFloat()), 1f, 450)
            assertTrue(points.size <= 450)
        }
        assertEquals(first, points.first())
        assertEquals(first.x, points.last().x, .00001f)
        assertEquals(first.y, points.last().y, .00001f)
        var twiceArea = 0.0
        for (i in points.indices) {
            val a = points[i]
            val b = points[(i + 1) % points.size]
            twiceArea += a.x.toDouble() * b.y - b.x.toDouble() * a.y
        }
        assertEquals(kotlin.math.PI * radius * radius, kotlin.math.abs(twiceArea) / 2, .0005)
        // The former prefix truncation loses the final quadrant and roughly 9% of area.
        assertTrue(points.any { it.x > .7f && it.y < .3f })
    }
}

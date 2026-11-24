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
}

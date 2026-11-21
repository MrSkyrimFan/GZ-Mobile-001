package com.greenezone.gzphotoeditor.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeometryTest {
    @Test fun cropRatioNamesAreUnique() {
        assertEquals(9, CropRatio.entries.size)
        assertEquals(CropRatio.entries.size, CropRatio.entries.map { it.label }.toSet().size)
        assertEquals(null, CropRatio.ORIGINAL.ratio)
        assertEquals(1f, CropRatio.SQUARE.ratio!!, 0f)
        assertTrue(CropRatio.STORY.ratio!! < 1f)
        assertTrue(CropRatio.CINEMA.ratio!! > 2f)
    }

    @Test fun rotateAndFlipsPreserveIndependentGrade() {
        val d = EditDocument(grade = Grade(contrast = .2f))
        val altered = d.copy(quarterTurns = 3, mirrored = true, flipVertical = true, crop = CropRatio.TALL)
        assertEquals(.2f, altered.grade.contrast, 0f)
        assertEquals(3, altered.quarterTurns)
        assertTrue(altered.mirrored)
        assertTrue(altered.flipVertical)
        assertEquals(CropRatio.TALL, altered.crop)
    }
}

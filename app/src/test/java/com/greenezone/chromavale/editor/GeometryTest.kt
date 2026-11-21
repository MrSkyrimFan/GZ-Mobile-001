package com.greenezone.chromavale.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class GeometryTest {
    @Test fun cropRatioNamesAreUnique() {
        assertEquals(CropRatio.values().size, CropRatio.values().map { it.label }.toSet().size)
        assertEquals(null, CropRatio.ORIGINAL.ratio)
        assertEquals(1f, CropRatio.SQUARE.ratio!!, 0f)
    }

    @Test fun quarterTurnsPreserveIndependentGrade() {
        val d = EditDocument(grade = Grade(contrast = .2f))
        val rotated = d.copy(quarterTurns = (d.quarterTurns + 1) % 4)
        assertEquals(.2f, rotated.grade.contrast, 0f)
        assertEquals(1, rotated.quarterTurns)
    }
}

package com.greenezone.chromavale.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToneMathTest {
    private val grey = 0xFF707070.toInt()
    @Test fun neutralIsBitIdentical() {
        for (argb in listOf(0x00000000, 0xFFFFFFFF.toInt(), 0xFFEE44CC.toInt(), 0x80223344.toInt())) {
            assertEquals(argb, ToneMath.adjust(argb, Grade(), 1, 1, 3, 3))
        }
    }
    @Test fun exposureBrightensWithoutChangingAlpha() {
        val result = ToneMath.adjust(grey, Grade(exposure = 1f), 1, 1, 3, 3)
        assertTrue((result and 255) > (grey and 255))
        assertEquals(grey ushr 24, result ushr 24)
    }
    @Test fun saturationMinusOneProducesNeutralRgb() {
        val result = ToneMath.adjust(0xFFEE3388.toInt(), Grade(saturation = -1f), 3, 3, 10, 10)
        assertEquals((result ushr 16) and 255, (result ushr 8) and 255)
        assertEquals((result ushr 8) and 255, result and 255)
    }
    @Test fun vignetteIsSpatialAndDeterministic() {
        val grade = Grade(vignette = .8f)
        val mid = ToneMath.adjust(grey, grade, 50, 50, 101, 101)
        val edge = ToneMath.adjust(grey, grade, 0, 0, 101, 101)
        assertTrue((edge and 255) < (mid and 255))
        assertEquals(edge, ToneMath.adjust(grey, grade, 0, 0, 101, 101))
    }
    @Test fun allOriginalLooksHaveDifferentGrades() {
        assertEquals(7, Looks.all.size)
        assertEquals(Looks.all.size, Looks.all.map { it.grade }.toSet().size)
    }
    @Test fun controlCatalogHasUniqueControls() {
        val all = ControlCatalog.groups.values.flatten()
        assertEquals(20, all.size)
        assertEquals(20, all.map { it.name }.toSet().size)
        assertTrue(all.all { it.min <= 0f && it.max >= 0f })
        assertTrue(all.all { it.read(Grade()) == 0f })
    }
    @Test fun undoRedoRemainsCoherent() {
        val initial = EditDocument()
        val modified = initial.copy(grade = Grade(exposure = .4f))
        val history = EditHistory().checkpoint(initial, modified)
        val previous = history.undo(modified)!!
        assertEquals(initial, previous.first)
        val next = previous.second.redo(previous.first)!!
        assertEquals(modified, next.first)
        assertFalse(next.second.undo.isEmpty())
        assertTrue(next.second.redo.isEmpty())
        assertEquals(history, history.checkpoint(modified, modified))
    }
    @Test fun neutralPresetIsActuallyNeutral() {
        assertEquals(Grade(), Looks.all.first().grade)
        assertNotEquals(Looks.all.first().grade, Looks.all.last().grade)
    }
}

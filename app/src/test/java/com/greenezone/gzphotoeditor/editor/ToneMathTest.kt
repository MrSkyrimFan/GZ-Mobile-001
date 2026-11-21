package com.greenezone.gzphotoeditor.editor

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
        assertEquals(12, Looks.all.size)
        assertEquals(Looks.all.size, Looks.all.map { it.grade }.toSet().size)
    }

    @Test fun allFiftyThreeControlsHaveUniqueNamesAndNeutralZeros() {
        val all = ControlCatalog.all
        assertEquals(56, all.size)
        assertEquals(56, all.map { it.name }.toSet().size)
        assertEquals(8, ColorBand.entries.size)
        assertEquals(24, ControlCatalog.mixer.values.sumOf { it.size })
        assertTrue(all.all { it.min <= 0f && it.max >= 0f })
        assertTrue(all.all { it.read(Grade()) == 0f })
    }

    @Test fun individualMixerControlsAreIndependent() {
        val control = ControlCatalog.mixer.getValue(ColorBand.BLUE)[1]
        val modified = control.write(Grade(), -.75f)
        assertEquals(-.75f, control.read(modified), 0f)
        assertEquals(ColorMix(), modified.colorMix[ColorBand.RED.ordinal])
        assertEquals(Grade().exposure, modified.exposure, 0f)
    }

    @Test fun mixerDoesNotTintGreyscalePixels() {
        val mix = List(ColorBand.entries.size) { ColorMix(hue = .7f, saturation = .5f, luminance = .6f) }
        assertEquals(grey, ToneMath.adjust(grey, Grade(colorMix = mix), 10, 10, 20, 20))
    }

    @Test fun redMixerSaturationChangesRedPixel() {
        val mix = Grade().colorMix.toMutableList()
        mix[ColorBand.RED.ordinal] = ColorMix(saturation = -.9f)
        val source = 0xFFDF3542.toInt()
        assertNotEquals(source, ToneMath.adjust(source, Grade(colorMix = mix), 2, 2, 10, 10))
    }

    @Test fun shadowGradeAffectsDarksMuchMoreThanHighlights() {
        val grade = Grade(shadowRed = .8f)
        val dark = 0xFF252525.toInt()
        val bright = 0xFFE2E2E2.toInt()
        val darkResult = ToneMath.adjust(dark, grade, 1, 1, 2, 2)
        val brightResult = ToneMath.adjust(bright, grade, 1, 1, 2, 2)
        assertTrue(((darkResult ushr 16) and 255) > ((dark ushr 16) and 255))
        assertEquals(bright, brightResult)
    }

    @Test fun preparedMatchesStandalone() {
        val g = Grade(exposure = .15f, gamma = -.15f, temperature = .25f, shadowBlue = .2f)
        val original = 0xDFA14F95.toInt()
        val p = ToneMath.prepare(g)
        assertEquals(
            ToneMath.adjust(original, g, 8, 17, 39, 51, .12f),
            ToneMath.adjustPrepared(original, p, 8, 17, 39, 51, .12f)
        )
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

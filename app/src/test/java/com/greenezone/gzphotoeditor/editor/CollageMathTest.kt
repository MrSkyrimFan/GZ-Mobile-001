package com.greenezone.gzphotoeditor.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class CollageMathTest {
    private val templates = CollageTemplate.entries
    private fun overlap(a: CollageRect, b: CollageRect): Float =
        maxOf(0f, minOf(a.right, b.right) - maxOf(a.left, b.left)) *
            maxOf(0f, minOf(a.bottom, b.bottom) - maxOf(a.top, b.top))

    @Test fun allSevenTemplatesLayOutEveryPhotoCountWithoutOverlaps() {
        assertEquals(7, templates.size)
        for (template in templates) {
            for (count in 1..20) {
                val layout = CollageLayout(template = template)
                val cells = CollageMath.frames(count, layout)
                assertEquals("template=$template count=$count", count, cells.size)
                for ((index, r) in cells.withIndex()) {
                    assertTrue("$template count=$count cell=$index", r.left >= 0f &&
                        r.top >= 0f && r.right <= 1f && r.bottom <= 1f)
                    assertTrue("$template count=$count cell=$index",
                        r.width > 0f && r.height > 0f)
                    for (other in index + 1 until cells.size) {
                        assertTrue("overlap $template count=$count",
                            overlap(r, cells[other]) < .000001f)
                    }
                }
            }
        }
    }

    @Test fun userFacingCellsAreInsideFrameAndSelectable() {
        for (count in listOf(2, 3, 7, 12, 20)) {
            for (template in templates) {
                val layout = CollageLayout(
                    template = template, gap = .016f, margin = .03f)
                val cells = CollageMath.innerFrames(count, layout)
                assertEquals(count, cells.size)
                cells.forEachIndexed { index, cell ->
                    assertTrue(cell.left >= 0f && cell.top >= 0f)
                    assertTrue(cell.right <= 1f && cell.bottom <= 1f)
                    assertTrue(cell.width > 0f && cell.height > 0f)
                    assertEquals(index, CollageMath.select(
                        (cell.left + cell.right) / 2f,
                        (cell.top + cell.bottom) / 2f, count, layout))
                }
            }
        }
    }

    @Test fun deviceAwarePixelBudgetNeverExceedsLimit() {
        for (aspect in CollageAspect.entries) {
            for (quality in CollageQuality.entries) {
                val layout = CollageLayout(aspect = aspect, quality = quality)
                val size = CollageMath.outputSize(layout, quality.maxEdge, 3_000_000)
                assertTrue(size.first > 0 && size.second > 0)
                assertTrue(size.first.toLong() * size.second <= 3_000_000L)
                assertTrue(maxOf(size.first, size.second) <= quality.maxEdge)
                assertTrue(
                    abs(size.first / size.second.toFloat() - aspect.ratio) < .003f
                )
            }
        }
    }

    @Test fun moveMaintainsEveryTileSpecificEditDocument() {
        val input = (0 until 20).map {
            CollagePhoto("content://gz.example/$it",
                EditDocument(grade = Grade(exposure = it / 30f)))
        }
        val moved = CollageMath.move(input, 0, 19)
        assertEquals(20, moved.size)
        assertEquals(input.first(), moved.last())
        assertEquals(input[1], moved.first())
        assertEquals(input[18], moved[17])
        assertEquals(input, CollageMath.move(input, -1, 5))
        assertEquals(input, CollageMath.move(input, 8, 25))
    }

    @Test fun photoCountAndOutOfBoundsTapFailClosed() {
        assertFalse(CollageMath.validCount(0))
        assertTrue(CollageMath.validCount(1))
        assertTrue(CollageMath.validCount(20))
        assertFalse(CollageMath.validCount(21))
        assertEquals(-1, CollageMath.select(-.2f, .5f, 4, CollageLayout()))
        assertEquals(-1, CollageMath.select(.5f, .5f, 0, CollageLayout()))
    }

    @Test fun maximumGapCannotCollapseTwentyVeryThinSideBySidePhotos() {
        val layout = CollageLayout(
            template = CollageTemplate.SIDE_BY_SIDE, margin = .10f, gap = .08f
        )
        val frames = CollageMath.innerFrames(20, layout)
        assertEquals(20, frames.size)
        assertTrue(frames.all { it.width > .0001f && it.height > 0f })
    }

    @Test fun collageUndoRedoIsBoundedAndPreservesIndependentTileEdits() {
        val first = CollageDocument(photos = listOf(
            CollagePhoto("content://example/one",
                EditDocument(grade = Grade(exposure = .2f)))
        ))
        val second = first.copy(photos = first.photos + CollagePhoto(
            "content://example/two", EditDocument(grade = Grade(contrast = .4f))
        ))
        val after = second.copy(layout = second.layout.copy(gap = .04f))
        val original = CollageHistory()
        val h1 = original.checkpoint(first, second)
        val h2 = h1.checkpoint(second, after)
        val back = h2.undo(after)!!
        assertEquals(second, back.first)
        val previous = back.second.undo(back.first)!!
        assertEquals(first, previous.first)
        val replay = previous.second.redo(previous.first)!!
        assertEquals(second, replay.first)
        assertEquals(first.photos[0].edits, replay.first.photos[0].edits)
        assertEquals(second.photos[1].edits, replay.first.photos[1].edits)
        val changed = replay.second.checkpoint(replay.first, after)
        assertTrue(changed.redo.isEmpty())
        assertEquals(2, changed.undo.size)
        var history = CollageHistory()
        for (n in 0..40) {
            val a = CollageDocument(layout = CollageLayout(gap = n / 1000f))
            val b = a.copy(layout = a.layout.copy(gap = (n + 1) / 1000f))
            history = history.checkpoint(a, b)
        }
        assertEquals(35, history.undo.size)
    }

    @Test fun gridTwoPhotoLayoutAndSideBySideAreDifferent() {
        val layout = CollageLayout(template = CollageTemplate.SIDE_BY_SIDE)
        val cells = CollageMath.frames(2, layout)
        assertEquals(.5f, cells[0].right, .00001f)
        assertEquals(.5f, cells[1].left, .00001f)
        assertEquals(1f, cells[0].height, .00001f)
    }
}

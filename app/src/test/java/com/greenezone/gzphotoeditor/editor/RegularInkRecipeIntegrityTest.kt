package com.greenezone.gzphotoeditor.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class RegularInkRecipeIntegrityTest {
    private fun stroke(index: Int) = InkStroke(
        points = listOf(BrushPoint(.2f, .3f), BrushPoint(.8f, .3f)),
        color = 0xFF000000.toInt() or index
    )

    @Test fun overCapacityInkCannotBeReportedAsACompleteReadableRecipe() {
        val invalid = EditDocument(creative = CreativeDocument(
            strokes = (0..160).map(::stroke)))
        assertThrows(EditDocumentCodec.RecipeTooLarge::class.java) {
            EditDocumentCodec.encodeBounded(invalid)
        }
    }

    @Test fun denseInkUsesTheLegacyFloatGrammarAndRawTokenLimit() {
        val legacyTokens = listOf(
            "", "0,1", " .5 , +.25F ", "-0.0,1e-3", "0x1.0p-2,2.5e-1",
            "0.4999999850988388,0.5000000298023224", "1e-100,1.0D",
            "NaN,.25", "Infinity,.25", "-.1,.5", "1.01,.5", "bad,.2",
            "0,0,0", ".2", ",.2", ".2,"
        )
        val random = kotlin.random.Random(25)
        val storedPoints = List(500) { index ->
            val digits = index % 9 + 1
            val left = random.nextInt(1_000_000_000).toString().padStart(9, '0').take(digits)
            val right = random.nextInt(1_000_000_000).toString().padStart(9, '0').take(digits)
            "0.$left,0.$right"
        }
        val points = (legacyTokens + storedPoints + "1,1" + "").joinToString(";")
        val expected = points.split(';').take(500).mapNotNull { pair ->
            val coordinates = pair.split(',')
            if (coordinates.size != 2) return@mapNotNull null
            val x = coordinates[0].toFloatOrNull() ?: return@mapNotNull null
            val y = coordinates[1].toFloatOrNull() ?: return@mapNotNull null
            if (x.isFinite() && y.isFinite() && x in 0f..1f && y in 0f..1f) BrushPoint(x, y) else null
        }
        val recipe = "version=1\ncreative.ink.count=1\ncreative.ink.0.points=$points"
        val reopened = requireNotNull(EditDocumentCodec.decode(recipe))
        assertEquals(expected, reopened.creative.strokes.single().points)
        assertEquals(reopened, EditDocumentCodec.decode(EditDocumentCodec.encodeBounded(reopened)))
    }

    @Test fun theSupportedStrokeBoundaryStillRoundTripsEveryStroke() {
        val valid = EditDocument(creative = CreativeDocument(
            strokes = (0 until 160).map(::stroke)))
        val reopened = EditDocumentCodec.decode(EditDocumentCodec.encodeBounded(valid))
        assertEquals(valid, reopened)
        assertEquals(valid.creative.strokes.last(), reopened!!.creative.strokes.last())
    }
}

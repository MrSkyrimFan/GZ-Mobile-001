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

    @Test fun theSupportedStrokeBoundaryStillRoundTripsEveryStroke() {
        val valid = EditDocument(creative = CreativeDocument(
            strokes = (0 until 160).map(::stroke)))
        val reopened = EditDocumentCodec.decode(EditDocumentCodec.encodeBounded(valid))
        assertEquals(valid, reopened)
        assertEquals(valid.creative.strokes.last(), reopened!!.creative.strokes.last())
    }
}

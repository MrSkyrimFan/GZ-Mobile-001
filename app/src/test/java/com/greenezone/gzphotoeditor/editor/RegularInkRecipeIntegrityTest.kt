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

    @Test fun canonicalInkCoordinatesRoundTripBitForBitAcrossFloatExponents() {
        val random = kotlin.random.Random(57)
        val points = buildList {
            add(BrushPoint(0f, Float.fromBits(Int.MIN_VALUE)))
            add(BrushPoint(Float.MIN_VALUE, 1f))
            for (exponent in 0..126) {
                val base = exponent shl 23
                add(BrushPoint(Float.fromBits(base), Float.fromBits(base or 0x7f_ffff)))
                repeat(2) {
                    val x = Float.fromBits(base or random.nextInt(1 shl 23))
                    val y = Float.fromBits(base or random.nextInt(1 shl 23))
                    add(BrushPoint(x, y))
                }
            }
        }
        val document = EditDocument(creative = CreativeDocument(
            strokes = listOf(InkStroke(points = points, color = 0xff336699.toInt()))))
        val reopened = requireNotNull(EditDocumentCodec.decode(EditDocumentCodec.encodeBounded(document)))
        assertEquals(document, reopened)
        val restoredPoints = reopened.creative.strokes.single().points
        points.forEachIndexed { index, expected ->
            assertEquals("point $index x", expected.x.toRawBits(), restoredPoints[index].x.toRawBits())
            assertEquals("point $index y", expected.y.toRawBits(), restoredPoints[index].y.toRawBits())
        }
    }

    @Test fun legacyInkCoordinatesPreserveExactRoundingAndSignedZero() {
        // The two short decimals adjacent to ties round differently through Double.
        val expectedBits = listOf(
            "0.49999998509883880615234374" to 0x3eff_ffff,
            "0.49999998509883880615234375" to 0x3f00_0000,
            "0.5000000298023223876953125" to 0x3f00_0000,
            "0.5000000298023224" to 0x3f00_0001,
            "0.5000000894069671" to 0x3f00_0001,
            "0.5000000894069671630859375" to 0x3f00_0002,
            "7.006492321624085e-46" to 0,
            "7.006492321624086e-46" to 1,
            "-7.006492321624085E-46" to Int.MIN_VALUE,
            "-0.0" to Int.MIN_VALUE,
            "1.000000059604644775390625" to 0x3f80_0000
        )
        val legacyForms = listOf(
            " 0x1.0p-3 ", " +.125F ", "0.125D", "004.00e-2", "1.e-24", "0.01e+1", "1E-22",
            "1E-23", "0.9007199254740992", "0.9007199254740993", "0.0000000009",
            "0.000000000000000000000001E+18", "1e-999999999999999999999", "0e999999999999999999999"
        )
        val valid = expectedBits + legacyForms.map { it to it.toFloat().toRawBits() }
        val invalid = listOf(
            "0.3x", "0.25e-", "0..25", "1e+", "1e999999999999999999999", "NaN", "-Infinity",
            "1.000000059604644775390626", "-7.006492321624086E-46"
        )
        val tokens = valid.map { (coordinate, _) -> "$coordinate,$coordinate" } +
            invalid.flatMap { listOf("$it,.25", ".25,$it") }
        val points = decodeSingleInkStroke(tokens).points
        assertEquals(valid.size, points.size)
        valid.forEachIndexed { index, (coordinate, bits) ->
            assertEquals("$coordinate x", bits, points[index].x.toRawBits())
            assertEquals("$coordinate y", bits, points[index].y.toRawBits())
        }
    }

    @Test fun theFiveHundredthRawInkPointCountsEmptyAndMalformedPredecessors() {
        val tokens = List(499) { index -> if (index % 2 == 0) "" else "bad,.2" } +
            listOf(".25,.75", "1,1", "")
        assertEquals(listOf(BrushPoint(.25f, .75f)), decodeSingleInkStroke(tokens).points)
    }

    private fun decodeSingleInkStroke(tokens: List<String>): InkStroke {
        val points = tokens.joinToString(";")
        val recipe = "version=1\ncreative.ink.count=1\ncreative.ink.0.points=$points"
        return requireNotNull(EditDocumentCodec.decode(recipe)).creative.strokes.single()
    }
}

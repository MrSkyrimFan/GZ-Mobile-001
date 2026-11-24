package com.greenezone.gzphotoeditor

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.ColorMix
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.Grade
import com.greenezone.gzphotoeditor.editor.ImagePipeline
import com.greenezone.gzphotoeditor.editor.Looks
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** The uncached Android raster is the pixel oracle, including real Bitmap alpha handling. */
@RunWith(AndroidJUnit4::class)
class AndroidGifDetailColorCacheTest {
    @Test fun detailGradesMatchUncachedPixelsAcrossGradientAlphaAndRepeatedColorFrames() {
        val frames = listOf(
            bitmap(47, 31) { x, y ->
                val alpha = intArrayOf(0, 40, 127, 128, 200, 255)[(x + y * 3) % 6]
                Color.argb(alpha, x * 255 / 46, y * 255 / 30, (x * 37 + y * 61) and 255)
            },
            bitmap(43, 29) { x, y ->
                val level = intArrayOf(8, 72, 128, 184, 248)[(x / 2 + y / 3) % 5]
                Color.argb(if ((x + y) % 4 == 0) 96 else 255, level, 255 - level, level)
            },
            bitmap(41, 33) { x, y ->
                // Many exact ARGB keys and collision/eviction opportunities; no random seed.
                (y * 41 + x) * -1640531527
            }
        )
        val mixed = Grade(exposure = -.2f, contrast = .25f, highlights = -.3f,
            shadows = .4f, gamma = .2f, hue = .17f, saturation = .2f,
            temperature = .3f, red = .25f, blue = -.2f,
            sharpen = .8f, sharpenThreshold = .2f, texture = .55f,
            clarity = .35f, denoise = .6f)
        val grades = listOf(
            // Preserve the real B&W recipe used by the portrait GIF qualification case.
            Looks.all.first { it.name == "B&W" }.grade.copy(exposure = .28f),
            mixed,
            mixed.copy(sharpen = -.65f, texture = -.7f, clarity = -.45f,
                denoise = .3f, shadowRed = .2f, midGreen = -.3f,
                highlightBlue = .25f,
                colorMix = mixed.colorMix.mapIndexed { index, _ ->
                    if (index % 2 == 0) ColorMix(hue = .15f, saturation = -.2f, luminance = .1f)
                    else ColorMix()
                }),
            Grade(denoise = .8f, exposure = .2f, contrast = .2f)
        )
        try {
            for ((gradeIndex, grade) in grades.withIndex()) {
                val cache = requireNotNull(ImagePipeline.GifColors.forGrade(grade)) {
                    "A coordinate-independent detail grade must support the exact cache"
                }
                for ((frameIndex, source) in frames.withIndex()) {
                    assertCachedRaster(source, EditDocument(grade = grade), cache,
                        "grade=$gradeIndex frame=$frameIndex")
                }
            }
        } finally {
            frames.forEach(Bitmap::recycle)
        }
    }

    @Test fun equalSourceColorsWithDifferentNeighborhoodsStayDistinctAcrossCacheReuse() {
        val centerColor = Color.rgb(128, 128, 128)
        val grade = Grade(sharpen = .65f, clarity = .55f, texture = .4f)
        val document = EditDocument(grade = grade)
        val cache = requireNotNull(ImagePipeline.GifColors.forGrade(grade))
        val frames = listOf(32, 224, 128).map { surround ->
            bitmap(17, 13) { x, y ->
                if (x == 8 && y == 6) centerColor else Color.rgb(surround, surround, surround)
            }
        }
        try {
            val centerResults = mutableSetOf<Int>()
            // Revisit the first frame after two changed neighborhoods using the same cache.
            // An ARGB-only key incorrectly reuses its first center result for the other frames.
            for (frameIndex in listOf(0, 1, 2, 0)) {
                val source = frames[frameIndex]
                assertEquals(centerColor, source.getPixel(8, 6))
                val expected = ImagePipeline.render(source, document)
                val actual = ImagePipeline.render(source, document, cache, null)
                try {
                    centerResults.add(expected.getPixel(8, 6))
                    assertBitmapEquals("changed neighborhood frame=$frameIndex", expected, actual)
                } finally {
                    if (expected !== source) expected.recycle()
                    if (actual !== source) actual.recycle()
                }
            }
            assertEquals("The oracle must exercise three different detail results for the same ARGB",
                3, centerResults.size)
            assertNotNull(ImagePipeline.GifColors.forGrade(grade.copy(denoise = .5f)))
            assertNull("Vignette depends on pixel position",
                ImagePipeline.GifColors.forGrade(grade.copy(vignette = .4f)))
            assertNull("Grain depends on pixel position",
                ImagePipeline.GifColors.forGrade(grade.copy(grain = .4f)))
            assertTrue(frames.all { !it.isRecycled })
        } finally {
            frames.forEach(Bitmap::recycle)
        }
    }

    private fun bitmap(width: Int, height: Int, pixel: (Int, Int) -> Int): Bitmap =
        Bitmap.createBitmap(IntArray(width * height) { index -> pixel(index % width, index / width) },
            width, height, Bitmap.Config.ARGB_8888)

    private fun pixels(bitmap: Bitmap): IntArray = IntArray(bitmap.width * bitmap.height).also {
        bitmap.getPixels(it, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    }

    private fun assertCachedRaster(source: Bitmap, document: EditDocument,
                                   cache: ImagePipeline.GifColors, label: String) {
        val original = pixels(source)
        val expected = ImagePipeline.render(source, document)
        val actual = ImagePipeline.render(source, document, cache, null)
        try {
            assertBitmapEquals(label, expected, actual)
            assertArrayEquals("$label must not alter its source", original, pixels(source))
        } finally {
            if (expected !== source) expected.recycle()
            if (actual !== source) actual.recycle()
        }
    }

    private fun assertBitmapEquals(label: String, expected: Bitmap, actual: Bitmap) {
        assertEquals("$label width", expected.width, actual.width)
        assertEquals("$label height", expected.height, actual.height)
        assertArrayEquals("$label pixels", pixels(expected), pixels(actual))
    }
}

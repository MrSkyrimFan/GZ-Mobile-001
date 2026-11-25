package com.greenezone.gzphotoeditor.editor

import org.junit.Assert.*
import org.junit.Test

/** Exact color memo behavior; the existing Android reader tests retain the uncached scalar oracle. */
class GifYuvColorCacheTest {
    private fun packed(y: Int, u: Int, v: Int): Int = y or (u shl 8) or (v shl 16)
    private fun exact(sample: Int, color: EditedVideoPreviewReader.PreviewColor): Int =
        color.pixel(sample and 255, (sample ushr 8) and 255, (sample ushr 16) and 255)

    @Test fun repeatedSamplesReuseExactArgbAcrossFreshEquivalentColorObjects() {
        val cache = EditedVideoPreviewReader.YuvColorCache()
        val sample = packed(128, 98, 169)
        val expected = exact(sample, EditedVideoPreviewReader.PreviewColor(hd = true, fullRange = false))
        repeat(100) {
            // Decoder metadata creates fresh PreviewColor objects on successive frames.
            val color = EditedVideoPreviewReader.PreviewColor(hd = true, fullRange = false)
            assertEquals(expected, cache.pixel(sample, color))
        }
        assertEquals(1L, cache.misses)
        assertEquals(99L, cache.hits)
        assertEquals(32 * 1024, cache.storageBytes)
        val independentReader = EditedVideoPreviewReader.YuvColorCache()
        assertEquals(expected, independentReader.pixel(sample,
            EditedVideoPreviewReader.PreviewColor(hd = true, fullRange = false)))
        assertEquals("A different reader must not inherit entries", 1L, independentReader.misses)
        assertEquals(0L, independentReader.hits)
        assertEquals(99L, cache.hits)
    }

    @Test fun moreThanCapacityReplacementsAlwaysRecomputeTheExactOriginalConversion() {
        val cache = EditedVideoPreviewReader.YuvColorCache()
        val color = EditedVideoPreviewReader.PreviewColor(hd = false, fullRange = false)
        // Odd multiplication gives distinct 24-bit keys here; more than twice the cache capacity.
        val samples = IntArray(8193) { (it * 104_729) and 0x00ffffff }
        for (sample in samples) assertEquals(exact(sample, color), cache.pixel(sample, color))
        val firstPassMisses = cache.misses
        assertEquals(samples.size.toLong(), firstPassMisses)
        for (index in samples.indices.reversed()) {
            val sample = samples[index]
            assertEquals("An evicted/colliding key must not reuse another color",
                exact(sample, color), cache.pixel(sample, color))
        }
        assertTrue("Revisiting more entries than fit must recompute evicted colors", cache.misses > firstPassMisses)
        assertEquals("Replacements must not grow retained storage", 32 * 1024, cache.storageBytes)
        val sample = samples[0]
        val hitsBefore = cache.hits
        assertEquals(exact(sample, color), cache.pixel(sample, color))
        assertEquals(hitsBefore + 1L, cache.hits)
    }

    @Test fun everyMatrixAndRangeIdentityKeepsItsOwnExactColorWhenMetadataChanges() {
        val cache = EditedVideoPreviewReader.YuvColorCache()
        val sample = packed(128, 98, 169)
        val matrices = listOf(
            EditedVideoPreviewReader.PreviewColor(hd = false, fullRange = false),
            EditedVideoPreviewReader.PreviewColor(hd = false, fullRange = true),
            EditedVideoPreviewReader.PreviewColor(hd = true, fullRange = false),
            EditedVideoPreviewReader.PreviewColor(hd = true, fullRange = true)
        )
        assertEquals("The fixture must distinguish every matrix/range pair",
            4, matrices.map { exact(sample, it) }.toSet().size)
        repeat(20) { round ->
            for (index in matrices.indices) {
                val color = matrices[(index + round) % matrices.size]
                assertEquals(exact(sample, color), cache.pixel(sample, color))
                assertEquals(exact(sample, color), cache.pixel(sample, color))
            }
        }
        assertTrue(cache.hits >= 80L)
        assertTrue("All four color identities need an original conversion", cache.misses >= 4L)
        assertEquals(32 * 1024, cache.storageBytes)
    }

    @Test fun channelExtremesAndZeroKeyRetainOpaqueExactArgb() {
        val cache = EditedVideoPreviewReader.YuvColorCache()
        val levels = intArrayOf(0, 1, 15, 16, 128, 235, 254, 255)
        for (hd in listOf(false, true)) for (fullRange in listOf(false, true)) {
            val color = EditedVideoPreviewReader.PreviewColor(hd, fullRange)
            for (y in levels) for (u in levels) for (v in levels) {
                val sample = packed(y, u, v)
                val actual = cache.pixel(sample, color)
                assertEquals(exact(sample, color), actual)
                assertEquals("Memoization must preserve opaque ARGB", 255, actual ushr 24)
            }
        }
        assertEquals(32 * 1024, cache.storageBytes)
    }
}

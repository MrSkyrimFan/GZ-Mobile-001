package com.greenezone.gzphotoeditor

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.EditedVideoPreviewReader
import com.greenezone.gzphotoeditor.editor.VideoGifSource
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Compare every ARGB pixel against the pre-optimization scalar reader, not only patches. */
@RunWith(AndroidJUnit4::class)
class AndroidGifBulkRowEquivalenceTest {
    private val instrument = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrument.targetContext

    @Test fun originalPortraitRowsPreserveEveryPixelAndBoundTheirStorage() =
        compare("gif_portrait_motion_fixture.mp4", 1920, false, 16 * 1024)

    @Test fun rotatedBFramesPreserveEveryPixelInBothDirections() {
        compare("gif_phone_60fps_bframes.mp4", 1280, false, 16 * 1024)
        compare("gif_phone_60fps_bframes.mp4", 1280, true, 16 * 1024)
    }

    @Test fun correctedAspectAndFilteredReductionRetainExactSamples() {
        compare("gif_aspect_anamorphic.mp4", 1920, false, 32 * 1024)
        compare("gif_portrait_motion_fixture.mp4", 320, false, 16 * 1024)
    }

    private fun compare(asset: String, edge: Int, reverse: Boolean, rowBudget: Int) {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "gz-bulk-equivalence-" + asset)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = requireNotNull(resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values))
        try {
            requireNotNull(resolver.openOutputStream(uri)).use { output ->
                instrument.context.assets.open(asset).use { it.copyTo(output) }
            }
            resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
            val clip = requireNotNull(VideoGifSource.document(context, uri).video).copy(reverse = reverse)
            val indices = if (reverse) listOf(clip.sourceTimesMs.lastIndex,
                clip.sourceTimesMs.lastIndex - 1, clip.sourceTimesMs.size / 2, 0)
                else listOf(0, clip.sourceTimesMs.size / 2, clip.sourceTimesMs.lastIndex)
            val selected = indices.distinct().map { clip.sourceTimesMs[it] }
            var firstStorage = -1
            EditedVideoPreviewReader.open(context, clip, prefetchReverseFrames = reverse,
                filterDownscaling = true, bulkSampleRows = false) {}.use { scalar ->
                EditedVideoPreviewReader.open(context, clip, prefetchReverseFrames = reverse,
                    filterDownscaling = true, bulkSampleRows = true) {}.use { bulk ->
                    for (time in selected) {
                        val expected = scalar.frame(time, edge)
                        val actual = bulk.frame(time, edge)
                        try {
                            assertEquals(expected.width to expected.height, actual.width to actual.height)
                            assertArrayEquals("Exact decoded ARGB at " + asset + ":" + time + " reverse=" + reverse,
                                pixels(expected), pixels(actual))
                            val storage = bulk.sampleRowStorageBytes
                            assertTrue("Row cache must be allocated", storage > 0)
                            assertTrue("Row cache bytes " + storage + " exceed " + rowBudget, storage < rowBudget)
                            if (firstStorage < 0) firstStorage = storage else assertEquals(firstStorage, storage)
                            Log.i("GzGifBulkRows", "asset=" + asset + " timeMs=" + time + " reverse=" + reverse +
                                " canvas=" + actual.width + "x" + actual.height + " rowBytes=" + storage +
                                " exactArgbPixels=" + actual.width * actual.height)
                        } finally {
                            expected.recycle()
                            actual.recycle()
                        }
                    }
                }
            }
        } finally { resolver.delete(uri, null, null) }
    }

    private fun pixels(bitmap: Bitmap) = IntArray(bitmap.width * bitmap.height).also {
        bitmap.getPixels(it, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    }
}

package com.greenezone.gzphotoeditor

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Movie
import android.graphics.Rect
import android.media.MediaExtractor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.EditedVideoPreviewReader
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.GifMath
import com.greenezone.gzphotoeditor.editor.GifSize
import com.greenezone.gzphotoeditor.editor.GifStore
import com.greenezone.gzphotoeditor.editor.GifVideoMath
import com.greenezone.gzphotoeditor.editor.VideoGifSource
import com.greenezone.gzphotoeditor.ui.VideoGifScreen
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class AndroidGifAspectTest {
    @get:Rule val rule = createComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val resolver = context.contentResolver
    private val published = mutableListOf<Uri>()

    @Test fun squareAndAnamorphicSourcesProduceTheSameRoundMarkerAndDisplayCanvas() {
        try {
            val square = loadFixture("gif_aspect_square.mp4")
            val anamorphic = loadFixture("gif_aspect_anamorphic.mp4")
            assertEquals(256, square.video!!.width)
            assertEquals(180, anamorphic.video!!.width)
            assertEquals(144, anamorphic.video!!.height)

            for (document in listOf(square, anamorphic)) {
                val clip = requireNotNull(document.video)
                assertEquals(256 to 144, GifMath.dimensions(document))
                val edit = document.copy(size = GifSize.SMALL)
                assertEquals(320 to 180, GifMath.dimensions(edit))
                val expectedDimensions = 256 to 144
                VideoGifSource.open(context, clip).use { decoder ->
                    assertRaster(decoder.frame(0, 480), expectedDimensions)
                }
                EditedVideoPreviewReader.open(context, clip) {}.use { decoder ->
                    assertRaster(decoder.frame(0, 480), expectedDimensions)
                }
                val savedRecipe = GifStore.encode(edit)
                assertEquals(edit, GifStore.decode(savedRecipe))

                val exported = ByteArrayOutputStream().also { VideoGifSource.write(context, edit, it) }.toByteArray()
                val independent = requireNotNull(Movie.decodeByteArray(exported, 0, exported.size))
                assertEquals(320, independent.width())
                assertEquals(180, independent.height())
                val image = Bitmap.createBitmap(320, 180, Bitmap.Config.ARGB_8888)
                try {
                    val centers = listOf(0, independent.duration() / 2, independent.duration() - 1).map { time ->
                        independent.setTime(time)
                        independent.draw(Canvas(image), 0f, 0f)
                        val marker = marker(image)
                        assertTrue("Round GIF marker at $time ms: $marker", abs(marker.width() - marker.height()) <= 3)
                        assertTrue("Expected marker size at $time ms: $marker", marker.height() in 65..75)
                        marker.centerX()
                    }
                    assertTrue("Expected moving marker: $centers",
                        centers[0] + 25 < centers[1] && centers[1] + 25 < centers[2])
                } finally {
                    image.recycle()
                }

                val reopened = VideoGifSource.document(context,
                    publish("gz-aspect-reopened.gif", "image/gif", exported))
                assertEquals(320 to 180, GifMath.dimensions(reopened))
                assertEquals(24, GifVideoMath.frameCount(reopened.video!!))
                VideoGifSource.open(context, reopened.video!!).use { decoder ->
                    assertRaster(decoder.frame(0, 480), 320 to 180)
                }
            }

            val older = JSONObject(GifStore.encode(anamorphic.copy(size = GifSize.SMALL))).apply {
                put("version", 4)
                getJSONObject("video").remove("displayWidth")
                getJSONObject("video").remove("displayHeight")
            }
            assertEquals(320 to 256, GifMath.dimensions(requireNotNull(GifStore.decode(older.toString()))))
        } finally {
            clearMedia()
        }
    }

    @Test fun nativeSquarePlaybackAndCorrectedAnamorphicPlaybackKeepVisibleMotion() {
        try {
            val square = loadFixture("gif_aspect_square.mp4")
            val anamorphic = loadFixture("gif_aspect_anamorphic.mp4")
            val source = mutableStateOf(square)
            rule.setContent {
                val active = source.value
                key(active.video!!.uri) {
                    VideoGifScreen(active, { source.value = it }, {}, {}, {})
                }
            }
            waitFrame(1)
            assertRoundPreviewMarker()
            rule.onNodeWithText("Play").performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithTag("gif-native-video").fetchSemanticsNodes().isNotEmpty()
            }
            rule.waitUntil(15_000) { rule.onAllNodesWithText("Play").fetchSemanticsNodes().isNotEmpty() }

            rule.runOnIdle { source.value = anamorphic }
            waitFrame(1)
            val first = assertRoundPreviewMarker()
            rule.onNodeWithText("Play").performClick()
            rule.waitUntil(15_000) { rule.onAllNodesWithText("Play").fetchSemanticsNodes().isNotEmpty() }
            waitFrame(24)
            val last = assertRoundPreviewMarker()
            assertTrue("Expected anamorphic marker to move across the canvas", last.centerX() > first.centerX() + 25)
        } finally {
            clearMedia()
        }
    }

    @Test fun absentOptionalAspectAndTrackRotationHaveDeterministicGeometry() {
        try {
            val square = fixtureBytes("gif_aspect_square.mp4")
            "free".toByteArray(Charsets.US_ASCII).copyInto(square, atomTag(square, "pasp"))
            val fallback = loadFixture("gz-square-without-pasp.mp4", square)
            assertEquals(256 to 144, GifMath.dimensions(fallback))
            assertFalse(fallback.video!!.hasCorrectedGeometry)

            val sideways = fixtureBytes("gif_aspect_anamorphic.mp4")
            val header = atomTag(sideways, "tkhd") + 4
            assertEquals(0, sideways[header].toInt())
            val transform = ByteBuffer.wrap(sideways).apply { position(header + 40) }
            // ISO BMFF track presentation matrix with a 90-degree rotation.
            intArrayOf(0, 1 shl 16, 0, -(1 shl 16), 0, 0, 144 shl 16, 0, 1 shl 30)
                .forEach { transform.putInt(it) }
            val rotated = loadFixture("gz-anamorphic-rotated.mp4", sideways)
            assertEquals(144 to 256, GifMath.dimensions(rotated))
            assertEquals(180 to 320, GifMath.dimensions(rotated.copy(size = GifSize.SMALL)))
            val clip = requireNotNull(rotated.video)
            VideoGifSource.open(context, clip).use { reader ->
                assertRaster(reader.frame(0, 480), 144 to 256)
            }
            EditedVideoPreviewReader.open(context, clip) {}.use { reader ->
                assertRaster(reader.frame(0, 480), 144 to 256)
            }
            // Export filters the decoded planes before executing the track rotation.
            // This exercises the transposed row scan at a smaller output size.
            EditedVideoPreviewReader.open(context, clip, filterDownscaling = true) {}.use { reader ->
                assertRaster(reader.frame(0, 128), 72 to 128)
            }
        } finally {
            clearMedia()
        }
    }

    private fun atomTag(bytes: ByteArray, type: String): Int {
        val name = type.toByteArray(Charsets.US_ASCII)
        return (4..bytes.size - name.size).single { start ->
            name.indices.all { offset -> bytes[start + offset] == name[offset] }
        }
    }

    private fun fixtureBytes(asset: String): ByteArray =
        instrumentation.context.assets.open(asset).use { it.readBytes() }

    private fun loadFixture(asset: String, bytes: ByteArray = fixtureBytes(asset)): GifDocument {
        val uri = publish(asset, "video/mp4", bytes)
        val probe = MediaExtractor()
        try {
            probe.setDataSource(context, uri, null)
            val format = probe.getTrackFormat(0)
            Log.i("GzGifAspect", "API ${Build.VERSION.SDK_INT}, $asset track: $format")
        } finally {
            probe.release()
        }
        val result = VideoGifSource.document(context, uri)
        Log.i("GzGifAspect", "$asset display raster: ${GifMath.dimensions(result)}")
        return result
    }

    private fun waitFrame(frame: Int) {
        rule.waitUntil(15_000) {
            rule.onAllNodes(hasTestTag("gif-frame") and hasContentDescription("GIF preview, frame $frame"))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun assertRoundPreviewMarker(): Rect {
        val image = rule.onNodeWithTag("gif-frame").captureToImage().asAndroidBitmap()
        val bounds = marker(image)
        assertTrue("Round preview marker: $bounds",
            abs(bounds.width() - bounds.height()) <= maxOf(3, bounds.height() / 12))
        return bounds
    }

    private fun assertRaster(bitmap: Bitmap, expected: Pair<Int, Int>) {
        try {
            assertEquals(expected, bitmap.width to bitmap.height)
            val bounds = marker(bitmap)
            assertTrue("Round decoded marker: $bounds", abs(bounds.width() - bounds.height()) <= 3)
        } finally {
            bitmap.recycle()
        }
    }

    private fun marker(bitmap: Bitmap): Rect {
        val row = IntArray(bitmap.width)
        var left = bitmap.width
        var right = -1
        var top = bitmap.height
        var bottom = -1
        for (y in 0 until bitmap.height) {
            bitmap.getPixels(row, 0, row.size, 0, y, row.size, 1)
            for ((x, pixel) in row.withIndex()) {
                val red = pixel ushr 16 and 255
                val green = pixel ushr 8 and 255
                val blue = pixel and 255
                if (red + green + blue <= 450) continue
                left = minOf(left, x)
                right = maxOf(right, x)
                top = minOf(top, y)
                bottom = maxOf(bottom, y)
            }
        }
        assertTrue("Expected a visible marker", left <= right && top <= bottom)
        return Rect(left, top, right + 1, bottom + 1)
    }

    private fun publish(name: String, mime: String, bytes: ByteArray): Uri {
        val collection = if (mime.startsWith("video/")) MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val uri = requireNotNull(resolver.insert(collection, ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }))
        published.add(uri)
        requireNotNull(resolver.openOutputStream(uri, "wt")).use { it.write(bytes) }
        resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
        return uri
    }

    private fun clearMedia() {
        published.forEach { resolver.delete(it, null, null) }
    }
}

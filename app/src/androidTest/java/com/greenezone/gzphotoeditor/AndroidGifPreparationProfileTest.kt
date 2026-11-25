package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.ContentValues
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Movie
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.*
import com.greenezone.gzphotoeditor.ui.GifMakerScreen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.OutputStream
import java.nio.file.Files
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicLong

/** Diagnostic workload bound only. This does not replace the existing 6 s / 15 s acceptance gates. */
@RunWith(AndroidJUnit4::class)
class AndroidGifPreparationProfileTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val instrument = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrument.targetContext
    private val resolver get() = context.contentResolver
    private val media = mutableListOf<Uri>()
    @Volatile private var current = GifDocument()

    @Test fun originalSizeNeutralPortraitTrimMeasuresPreparationBeforeDestinationPicker() {
        val folder = Files.createTempDirectory(context.cacheDir.toPath(), "gz-gif-default-profile-").toFile()
        val source = publish(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            "gz-default-profile.mp4", "video/mp4") { output ->
            instrument.context.assets.open("gif_portrait_motion_fixture.mp4").use { it.copyTo(output) }
        }
        val destination = publish(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "gz-default-profile.gif", "image/gif") {}
        val originalHash = hash(source)
        val opened = VideoGifSource.document(context, source)
        val original = requireNotNull(opened.video)
        assertEquals(1080 to 1920, original.width to original.height)
        assertEquals(150, original.sourceTimesMs.size)
        val pickerAt = AtomicLong(0)
        val picker = object : ActivityResultRegistryOwner {
            override val activityResultRegistry = object : ActivityResultRegistry() {
                override fun <I, O> onLaunch(requestCode: Int, contract: ActivityResultContract<I, O>,
                    input: I, options: ActivityOptionsCompat?) {
                    check(contract is ActivityResultContracts.StartActivityForResult)
                    val intent = input as Intent
                    check(intent.action == Intent.ACTION_CREATE_DOCUMENT && intent.type == "image/gif")
                    check(pickerAt.compareAndSet(0, SystemClock.elapsedRealtimeNanos()))
                    dispatchResult(requestCode, Activity.RESULT_OK, Intent().setData(destination))
                }
            }
        }
        try {
            rule.setContent {
                val base = LocalContext.current
                val isolated = remember(base) {
                    object : ContextWrapper(base) {
                        override fun getFilesDir() = File(folder, "files").also { it.mkdirs() }
                        override fun getCacheDir() = File(folder, "cache").also { it.mkdirs() }
                    }
                }
                CompositionLocalProvider(LocalContext provides isolated, LocalActivityResultRegistryOwner provides picker) {
                    MaterialTheme(colorScheme = darkColorScheme()) {
                        var document by remember { mutableStateOf(opened) }
                        current = document
                        GifMakerScreen(document, { document = it; current = it }, {}, {})
                    }
                }
            }
            rule.waitUntil(15_000) {
                rule.onAllNodesWithContentDescription("GIF preview, frame 1").fetchSemanticsNodes().isNotEmpty()
            }
            rule.onNodeWithContentDescription("Trim start and end").performScrollTo().performTouchInput {
                swipe(Offset(width * .96f, center.y), Offset(width * .50f, center.y), 450)
            }
            val recipe = rule.runOnIdle { current }
            val clip = requireNotNull(recipe.video)
            assertEquals(0L, clip.startMs)
            assertTrue("The actual end-thumb trim must be approximately 2.5 s: " + clip.endMs,
                clip.endMs in 2_000L..3_000L)
            assertEquals(GifSize.SOURCE, recipe.size)
            assertEquals(GifAspect.SOURCE, recipe.aspect)
            assertEquals(EditDocument(), recipe.edits)
            assertEquals(0, clip.fps)
            assertEquals(100, clip.speedPercent)
            assertFalse(clip.reverse)
            assertEquals(1080 to 1920, GifMath.dimensions(recipe))
            val expectedTimes = GifVideoMath.frameTimesMs(clip)
            val expectedDelays = GifVideoMath.frameDelaysMs(clip)
            // Support the unchanged application's visible action for baseline measurement,
            // and the B1 authoritative Options route without touching encoder settings.
            val options = rule.onAllNodesWithText("Options").fetchSemanticsNodes().isNotEmpty()
            if (options) rule.onNodeWithText("Options").performClick()
            val began = SystemClock.elapsedRealtimeNanos()
            if (options) rule.onNodeWithText("Export GIF…").performClick()
            else rule.onNodeWithText("Save GIF").performClick()
            try {
                rule.waitUntil(360_000) { pickerAt.get() > 0 }
            } finally {
                Log.i("GzGifDefaultProfile", "device=" + Build.MODEL + " api=" + Build.VERSION.SDK_INT +
                    " source=1080x1920 sourceFrames=150 trimStartMs=" + clip.startMs + " trimEndMs=" + clip.endMs +
                    " canvas=1080x1920 fps=source speed=100 reverse=false neutral=true scheduledFrames=" + expectedTimes.size +
                    " preparationToPickerMs=" + if (pickerAt.get() > 0) (pickerAt.get() - began) / 1_000_000 else -1)
            }
            rule.waitUntil(30_000) {
                rule.onAllNodesWithText("GIF saved", substring = false).fetchSemanticsNodes().isNotEmpty()
            }
            val completedMs = (SystemClock.elapsedRealtimeNanos() - began) / 1_000_000
            val metadata = requireNotNull(resolver.openInputStream(destination)).use {
                GifSourceMetadata.read(it, destination.toString())
            }
            assertEquals(1080 to 1920, metadata.width to metadata.height)
            assertEquals(expectedTimes.size, metadata.sourceTimesMs.size)
            assertEquals(expectedDelays, GifVideoMath.frameDelaysMs(metadata))
            assertEquals(expectedDelays.sum().toLong(), metadata.durationMs)
            assertEquals(recipe.loopCount, metadata.loopCount)
            val decoded = requireNotNull(resolver.openInputStream(destination)).use { Movie.decodeStream(it) }
            assertNotNull("Independent Android GIF decoder must open the completed output", decoded)
            val movie = requireNotNull(decoded)
            val raster = Bitmap.createBitmap(metadata.width, metadata.height, Bitmap.Config.ARGB_8888)
            try {
                VideoGifSource.open(context, clip).use { reader ->
                    for (index in listOf(0, expectedTimes.size / 2, expectedTimes.lastIndex).distinct()) {
                        raster.eraseColor(Color.TRANSPARENT)
                        movie.setTime(metadata.sourceTimesMs[index].toInt())
                        movie.draw(Canvas(raster), 0f, 0f)
                        val reference = reader.frame(expectedTimes[index], 1920)
                        try {
                            var error = 0L
                            var samples = 0
                            for (row in 0 until 24) for (column in 0 until 16) {
                                val x = (2 * column + 1) * raster.width / 32
                                val y = (2 * row + 1) * raster.height / 48
                                val a = raster.getPixel(x, y)
                                val b = reference.getPixel(x, y)
                                for (shift in 0..16 step 8) {
                                    error += kotlin.math.abs((a ushr shift and 255) - (b ushr shift and 255))
                                    samples++
                                }
                            }
                            val averageError = error.toDouble() / samples
                            Log.i("GzGifDefaultProfile", "decodedFrame=" + index + " meanChannelError=" + averageError)
                            assertTrue("Default output diverged from independently sought source at frame " + index +
                                ": mean channel error=" + averageError, averageError <= 24.0)
                        } finally { reference.recycle() }
                    }
                }
            } finally { raster.recycle() }
            assertArrayEquals(originalHash, hash(source))
            Log.i("GzGifDefaultProfile", "completionMs=" + completedMs +
                " encodedDurationMs=" + metadata.durationMs + " verifiedFrames=" + metadata.sourceTimesMs.size)
        } finally {
            rule.runOnIdle { rule.activity.setContentView(android.widget.FrameLayout(rule.activity)) }
            media.forEach { resolver.delete(it, null, null) }
            folder.deleteRecursively()
        }
    }

    private fun hash(uri: Uri): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        requireNotNull(resolver.openInputStream(uri)).use { input ->
            val buffer = ByteArray(65_536)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest()
    }

    private fun publish(collection: Uri, name: String, mime: String, write: (OutputStream) -> Unit): Uri {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = requireNotNull(resolver.insert(collection, values))
        media.add(uri)
        requireNotNull(resolver.openOutputStream(uri)).use(write)
        resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
        return uri
    }
}

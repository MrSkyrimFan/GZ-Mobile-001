package com.greenezone.gzphotoeditor

import android.app.ActivityManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorSpace
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Debug
import android.os.Process
import android.os.SystemClock
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.*
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.Base64
import java.util.Random
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Numeric reference measurements on the existing app; the bridge and its acknowledgement do not exist here. */
@U1BaselineProbe
@RunWith(AndroidJUnit4::class)
class AndroidU1NativeCustodyBaselineTest {
    @Test fun measureNativeNamedStoresAndLosslessDerivative() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val root = Files.createTempDirectory(target.cacheDir.toPath(), "u1-native-baseline-").toFile()
        val context = object : ContextWrapper(target) {
            override fun getFilesDir() = File(root, "files").apply { mkdirs() }
            override fun getDataDir() = File(root, "data").apply { mkdirs() }
        }
        try {
            val memory = target.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            Log.i(TAG, "environment pid=${Process.myPid()} memoryClassMiB=${memory.memoryClass} " +
                "managedMaxBytes=${Runtime.getRuntime().maxMemory()} samplePeriodMs=$SAMPLE_MS " +
                "processStart=instrumentation rasterLoader=localFixtureFiles")
            measureNamedRegular(context)
            val pro = realisticPro()
            measureNamedPro(context, pro)
            measureDerivative(root, pro)
            measureHistoryReference(root, pro)
        } finally {
            root.deleteRecursively()
        }
    }

    private fun measureNamedRegular(context: Context) {
        val name = "U1 Regular near ceiling"
        val image = Uri.parse("content://gz-u1-baseline/regular")
        val document = denseRecipe(1_950_000)
        val saved = measure("regular_named_save") {
            ProjectStore.saveNamed(context, ProjectSaveIntent.Create(name), image, document)
        }
        Log.i(TAG, "regular_result outcome=$saved")
        val ref = (saved as ProjectSaveResult.Saved).project
        val bytes = requireNotNull(regularProjectPayloadFile(context, name)).length()
        Log.i(TAG, "regular_capacity recordBytes=$bytes strokes=${document.creative.strokes.size}")
        assertTrue("Fixture must approach the existing 2M-character limit", bytes in 1_900_000L..2_000_100L)
        val reopened = requireNotNull(measure("regular_selected_open") { ProjectStore.loadNamed(context, ref) })
        assertEquals(ref, reopened.project)
        assertEquals(image, reopened.content.image)
        assertEquals(document, reopened.content.document)
    }

    private fun measureNamedPro(context: Context, raster: ProDocument) {
        val name = "U1 Pro near envelope"
        val layers = raster.layers.toMutableList().apply {
            this[2] = this[2].copy(edits = denseRecipe(1_480_000))
            this[3] = this[2].copy(name = "Second drawing", edits = denseRecipe(1_300_000))
        }
        val document = raster.copy(layers = layers)
        val reset = document.copy(layers = layers.mapIndexed { index, layer ->
            if (index == 0) layer.copy(opacity = .91f) else layer
        })
        assertTrue(ProMath.valid(document))
        assertTrue(ProMath.valid(reset))
        val saved = measure("pro_named_save_with_reset") {
            ProStore.saveNamed(context, ProjectSaveIntent.Create(name), document, reset)
        }
        Log.i(TAG, "pro_result outcome=$saved")
        val ref = (saved as ProjectSaveResult.Saved).project
        val key = Base64.getUrlEncoder().withoutPadding().encodeToString(name.toByteArray(Charsets.UTF_8))
        val bytes = File(context.filesDir, "gz_pro_projects/$key.gzpro").length()
        Log.i(TAG, "pro_capacity recordBytes=$bytes layers=${document.layers.size} reset=distinct")
        assertTrue("Fixture must approach the existing 6M-byte document plus reset envelope",
            bytes in 5_400_000L..6_000_512L)
        val reopened = requireNotNull(measure("pro_selected_open_with_reset") { ProStore.loadNamed(context, name) })
        assertEquals(ref, reopened.project)
        assertEquals(document, reopened.content.document)
        assertEquals(reset, reopened.content.resetDocument)
    }

    private fun denseRecipe(targetChars: Int): EditDocument {
        val a = .00012345678f
        val b = .0034567893f
        val samples = List(500) { index -> if (index % 2 == 0) BrushPoint(a, b) else BrushPoint(b, a) }
        val pairLength = "$a,$b".length
        assertEquals(pairLength, "$b,$a".length)
        val base = EditDocument(grade = Grade(exposure = .31f))
        var length = EditDocumentCodec.encode(base).length + 2
        val strokes = mutableListOf<InkStroke>()
        repeat(CreativeRaster.MAX_INK) { index ->
            val stroke = InkStroke(samples)
            val prefix = "\ncreative.ink.$index."
            val fields = prefix + "color=" + stroke.color +
                prefix + "opacity=" + stroke.opacity +
                prefix + "width=" + stroke.thickness +
                prefix + "erase=" + stroke.erase + prefix + "points="
            val count = minOf(samples.size, (targetChars - length - fields.length + 1) / (pairLength + 1))
            if (count > 0) {
                strokes += stroke.copy(points = if (count == samples.size) samples else samples.take(count))
                length += fields.length + count * (pairLength + 1) - 1
            }
        }
        assertTrue("reachable dense recipe length=$length target=$targetChars",
            length in (targetChars - pairLength - 2)..targetChars)
        return base.copy(creative = CreativeDocument(strokes = strokes))
    }

    private fun realisticPro(): ProDocument {
        val size = ProPixelSize(WIDTH, HEIGHT)
        val first = ProLayer("content://gz-u1-baseline/source-a", name = "Texture A", scale = .86f,
            aspect = size.aspect, sourceSize = size, edits = EditDocument(grade = Grade(exposure = .08f)))
        val second = ProLayer("content://gz-u1-baseline/source-b", name = "Texture B", scale = .66f,
            x = .58f, y = .56f, opacity = .82f, rotationDegrees = 7f, aspect = size.aspect,
            sourceSize = size, edits = EditDocument(grade = Grade(exposure = -.14f)))
        val draw = ProLayer("", name = "Brush", kind = ProLayerKind.DRAWING,
            edits = EditDocument(creative = CreativeDocument(strokes = listOf(
                InkStroke(listOf(BrushPoint(.25f, .33f), BrushPoint(.35f, .42f)))
            ))))
        val grade = ProLayer("", name = "Masked grade", kind = ProLayerKind.ADJUSTMENT,
            mask = ProSelection(ProSelectionKind.RECTANGLE),
            edits = EditDocument(grade = Grade(exposure = .16f)))
        val shapes = List(8) { index ->
            ProLayer("", name = "Shape $index", kind = ProLayerKind.SHAPE, x = .24f + index * .055f,
                y = .4f, scale = .2f, edits = EditDocument(creative = CreativeDocument(
                    shapes = listOf(ShapeOverlay(kind = ShapeKind.RECTANGLE, color = 0xff2268d4.toInt()))
                )))
        }
        return ProDocument(listOf(first, second, draw, grade) + shapes,
            background = ProBackground.TRANSPARENT, canvasSize = size)
    }

    private fun measureDerivative(root: File, document: ProDocument) {
        val sources = mapOf(
            document.layers[0].uri to File(root, "source-a.png"),
            document.layers[1].uri to File(root, "source-b.png")
        )
        sources.values.forEachIndexed { index, path ->
            val source = noiseBitmap(610L + index)
            try {
                FileOutputStream(path).use { output -> check(source.compress(Bitmap.CompressFormat.PNG, 100, output)) }
            } finally {
                source.recycle()
            }
        }
        val bitmap = measure("pro_render_12_layers_1600x1200") {
            ProRaster.renderWithLoader(document, WIDTH, HEIGHT, preview = false) { layer, edge, pixels ->
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireNotNull(sources[layer.uri]))) { decoder, info, _ ->
                    val (width, height) = ImagePipeline.decodeSize(info.size.width, info.size.height, edge, pixels)
                    decoder.setTargetSize(width, height)
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.setTargetColorSpace(ColorSpace.get(ColorSpace.Named.SRGB))
                }
            }
        }
        try {
            assertEquals(0, Color.alpha(bitmap.getPixel(0, 0)))
            assertEquals(255, Color.alpha(bitmap.getPixel(WIDTH / 2, HEIGHT / 2)))
            val png = File(root, "opaque-and-transparent.png")
            measure("lossless_png_compress_and_fsync") {
                FileOutputStream(png).use { output ->
                    check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
                    output.fd.sync()
                }
            }
            val decoded = measure("lossless_png_verify_decode") {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(png)) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.setTargetColorSpace(ColorSpace.get(ColorSpace.Named.SRGB))
                }
            }
            try {
                Log.i(TAG, "derivative_capacity width=${bitmap.width} height=${bitmap.height} " +
                    "bitmapBytes=${bitmap.allocationByteCount} pngBytes=${png.length()} " +
                    "fixtureSourceBytes=${sources.values.sumOf { it.length() }} alpha=${decoded.hasAlpha()}")
                assertEquals(WIDTH, decoded.width)
                assertEquals(HEIGHT, decoded.height)
                assertTrue(decoded.hasAlpha())
                assertEquals(bitmap.getPixel(0, 0), decoded.getPixel(0, 0))
                assertEquals(bitmap.getPixel(WIDTH / 2, HEIGHT / 2), decoded.getPixel(WIDTH / 2, HEIGHT / 2))
            } finally {
                decoded.recycle()
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun noiseBitmap(seed: Long): Bitmap {
        val random = Random(seed)
        val row = IntArray(WIDTH)
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        for (y in 0 until HEIGHT) {
            for (x in 0 until WIDTH) row[x] = random.nextInt() or 0xff000000.toInt()
            bitmap.setPixels(row, 0, WIDTH, 0, y, WIDTH, 1)
        }
        return bitmap
    }

    private fun measureHistoryReference(root: File, original: ProDocument) {
        val files = (0 until 35).map { File(root, "reference-$it") }
        measure("reference_35_small_native_revisions_encode_and_fsync") {
            files.forEachIndexed { index, file ->
                val layers = original.layers.toMutableList().apply {
                    this[0] = this[0].copy(x = .3f + index / 100f)
                }
                val bytes = ProStore.encodeProject(original.copy(layers = layers), original)
                FileOutputStream(file).use { output ->
                    output.write(bytes)
                    output.fd.sync()
                }
            }
        }
        val latest = requireNotNull(measure("reference_one_native_revision_open") {
            ProStore.decodeProject(files.last().readText())
        })
        assertEquals(.3f + 34 / 100f, latest.document.layers.first().x, 0f)
        assertEquals(original, latest.resetDocument)
        Log.i(TAG, "history_reference revisions=${files.size} bytes=${files.sumOf { it.length() }} " +
            "latestBytes=${files.last().length()} actualRootPublication=false")
    }

    private fun managed(): Long = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }

    private fun rss(): String = try {
        File("/proc/self/status").useLines { lines ->
            lines.filter { it.startsWith("VmRSS:") || it.startsWith("VmHWM:") }
                .joinToString(",") { it.trim().replace(Regex("\\s+"), "") }
                .ifEmpty { "unavailable" }
        }
    } catch (_: Exception) {
        "unavailable"
    }

    private fun <T> measure(phase: String, operation: () -> T): T {
        val startManaged = managed()
        val startNative = Debug.getNativeHeapAllocatedSize()
        val startRss = rss()
        val running = AtomicBoolean(true)
        var peakManaged = startManaged
        var peakNative = startNative
        val sampler = thread(name = "u1-baseline-heap") {
            while (running.get()) {
                peakManaged = maxOf(peakManaged, managed())
                peakNative = maxOf(peakNative, Debug.getNativeHeapAllocatedSize())
                SystemClock.sleep(SAMPLE_MS)
            }
        }
        Log.i(TAG, "begin phase=$phase")
        var outcome = "failed"
        val start = SystemClock.elapsedRealtimeNanos()
        try {
            return operation().also { outcome = "completed" }
        } finally {
            val elapsedUs = (SystemClock.elapsedRealtimeNanos() - start) / 1_000L
            val endManaged = managed()
            val endNative = Debug.getNativeHeapAllocatedSize()
            running.set(false)
            sampler.join()
            Log.i(TAG, "phase=$phase outcome=$outcome elapsedUs=$elapsedUs " +
                "managedBefore=$startManaged managedPeakObserved=${maxOf(peakManaged, endManaged)} " +
                "managedAfter=$endManaged nativeBefore=$startNative " +
                "nativePeakObserved=${maxOf(peakNative, endNative)} nativeAfter=$endNative " +
                "rssBefore=$startRss rssAfter=${rss()}")
        }
    }

    private companion object {
        const val TAG = "GzU1NativeBaseline"
        const val WIDTH = 1600
        const val HEIGHT = 1200
        const val SAMPLE_MS = 12L
    }
}

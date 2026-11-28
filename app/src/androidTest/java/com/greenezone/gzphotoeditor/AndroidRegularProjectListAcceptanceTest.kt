package com.greenezone.gzphotoeditor

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Debug
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.BrushPoint
import com.greenezone.gzphotoeditor.editor.CreativeDocument
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.EditDocumentCodec
import com.greenezone.gzphotoeditor.editor.Grade
import com.greenezone.gzphotoeditor.editor.InkStroke
import com.greenezone.gzphotoeditor.editor.NamedProjectIdentity
import com.greenezone.gzphotoeditor.editor.NamedProjectRef
import com.greenezone.gzphotoeditor.editor.ProjectKind
import com.greenezone.gzphotoeditor.editor.ProjectStore
import com.greenezone.gzphotoeditor.editor.SavedPhotoProjectMetadata
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.LockSupport
import kotlin.concurrent.thread
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Regular R1.1 store-only timings, matched to the legacy checkpoint's dense fixture. */
@RunWith(AndroidJUnit4::class)
class AndroidRegularProjectListAcceptanceTest {
    private companion object {
        const val TAG = "GzR11Acceptance"
        const val MIB = 1_048_576L
        const val MILLIS = 1_000_000L
        const val RECIPE_TARGET = 1_990_000
        const val SLOTS = 12
    }

    private data class Entry(
        val project: NamedProjectRef,
        val source: Uri,
        val exposure: Float,
        val pointCount: Int,
        val rawBytes: Long
    )

    private data class Sample<T>(
        val phase: String, val data: T, val elapsedNs: Long, val observedGrowth: Long
    )

    private data class OpenEvidence(
        val decodes: Int,
        val project: NamedProjectRef?,
        val source: Uri?,
        val exposure: Float?,
        val pointCount: Int?
    )

    private val runtime = Runtime.getRuntime()

    @Test fun twelveLegacyRecipesMigrateAndTheReadyIndexDoesNoListDecodes() =
        withRegularProjectFixture { fixture ->
            val context = fixture.context()
            val sources = mutableListOf<Uri>()
            try {
                val entries = seedTwelve(fixture, context, sources)
                val legacyBytes = fixture.legacyXml.length()
                Log.i(TAG, "fixture=matched_twelve rawXmlBytes=" + legacyBytes +
                    " recipes=" + entries.size + " javaMaxBytes=" + runtime.maxMemory() +
                    " pid=" + android.os.Process.myPid())
                val expectedMax = when (Build.VERSION.SDK_INT) {
                    29 -> 256L * MIB
                    35 -> 192L * MIB
                    else -> null
                }
                expectedMax?.let { assertEquals("Named qualification heap", it, runtime.maxMemory()) }

                requestGcOutsideTiming()
                var migrationDecodes = 0
                assertNotEquals("Migration must be measured off main", Looper.getMainLooper(), Looper.myLooper())
                val migration = measure("initial_migration") {
                    ProjectStore.listMetadata(context, onDecode = { migrationDecodes++ })
                }
                Log.i(TAG, "phase=initial_migration rows=" + migration.data?.size + " decodes=" + migrationDecodes)
                assertMetadata(entries, migration.data)

                requestGcOutsideTiming()
                // Cutover is durable. This is the first independent read of an
                // existing index in the same PID, with an OS-warm page cache.
                val ready = fixture.context()
                val lists = (0..3).map { iteration ->
                    val phase = if (iteration == 0) "existing_index_first" else "existing_index_warm_" + iteration
                    var decodes = 0
                    val list = measure(phase) { ProjectStore.listMetadata(ready, onDecode = { decodes++ }) }
                    Log.i(TAG, "phase=" + phase + " rows=" + list.data?.size + " decodes=" + decodes +
                        " legacyPreferenceAccesses=" + fixture.legacyPreferenceAccesses.get())
                    Triple(list, decodes, fixture.legacyPreferenceAccesses.get())
                }

                val selectedEntries = listOf(entries.last(), entries.first(), entries[5], entries.last())
                val selections = selectedEntries.mapIndexed { index, entry ->
                    val phase = if (index == 0) "selected_first" else "selected_warm_" + index
                    measureOpen(ready, entry, phase)
                }

                // Collect the bounded sample set before deciding the numerical gates.
                assertEquals(SLOTS, migrationDecodes)
                assertBudget(migration, 12_000, 112)
                lists.forEach { (sample, decodes, preferenceAccesses) ->
                    assertMetadata(entries, sample.data)
                    assertEquals(sample.phase, 0, decodes)
                    assertEquals(sample.phase, 0, preferenceAccesses)
                    assertBudget(sample, 150, 8)
                }
                selections.zip(selectedEntries).forEach { (sample, entry) ->
                    assertEquals(sample.phase, 1, sample.data.decodes)
                    assertEquals(sample.phase, entry.project, sample.data.project)
                    assertEquals(sample.phase, entry.source, sample.data.source)
                    assertEquals(sample.phase, entry.exposure, sample.data.exposure)
                    assertEquals(sample.phase, entry.pointCount, sample.data.pointCount)
                    assertBudget(sample, 650, 64)
                }
                val warmMedian = selections.drop(1).map { it.elapsedNs }.sorted()[1]
                Log.i(TAG, "phase=selected_warm_median elapsedNs=" + warmMedian)
                assertTrue("Warm selected median exceeded 300 ms: " + warmMedian, warmMedian <= 300 * MILLIS)
                assertEquals(0, fixture.legacyPreferenceAccesses.get())
            } finally {
                sources.forEach { context.contentResolver.delete(it, null, null) }
            }
        }

    private fun measureOpen(context: Context, entry: Entry, phase: String): Sample<OpenEvidence> {
        var decodes = 0
        val probe = EditDocumentCodec.InkProbe(phase)
        var throughReadCpu = 0L
        var openCpu = 0L
        var processAllocated = 0L
        val sample = measure(phase) {
            val allocationBefore = Debug.getRuntimeStat("art.gc.bytes-allocated")?.toLongOrNull()
            val cpuStart = Debug.threadCpuTimeNanos()
            EditDocumentCodec.selectedInkProbe.set(probe)
            try {
                ProjectStore.loadNamed(context, entry.project, onDecode = {
                    decodes++
                    throughReadCpu = Debug.threadCpuTimeNanos() - cpuStart
                })
            } finally {
                EditDocumentCodec.selectedInkProbe.remove()
                openCpu = Debug.threadCpuTimeNanos() - cpuStart
                val allocationAfter = Debug.getRuntimeStat("art.gc.bytes-allocated")?.toLongOrNull()
                processAllocated = if (allocationAfter != null && allocationBefore != null)
                    allocationAfter - allocationBefore else -1L
            }
        }
        Log.i("GzR11Probe", "phase=" + probe.phase + " openCpuNs=" + openCpu +
            " throughReadCpuNs=" + throughReadCpu + " fieldsCpuNs=" + probe.fieldsCpuNs +
            " recipeChars=" + probe.fieldsChars + " inkCpuNs=" + probe.inkCpuNs +
            " fast=" + probe.fastCount + " fallback=" + probe.fallbackCount +
            " fallbackSamples=" + probe.firstFallbacks + " wholeProcessAllocated=" + processAllocated)
        val loaded = sample.data
        Log.i(TAG, "phase=" + phase + " name=" + entry.project.name + " decodes=" + decodes +
            " opened=" + (loaded != null))
        // Keep the identity and exact fixture markers, not the selected graph.
        val evidence = OpenEvidence(decodes, loaded?.project, loaded?.content?.image,
            loaded?.content?.document?.grade?.exposure,
            loaded?.content?.document?.creative?.strokes?.sumOf { it.points.size })
        return Sample(sample.phase, evidence, sample.elapsedNs, sample.observedGrowth)
    }

    private fun assertMetadata(expected: List<Entry>, actual: List<SavedPhotoProjectMetadata>?) {
        val rows = requireNotNull(actual)
        assertEquals(expected.map { it.project }, rows.map { it.project })
        assertEquals(expected.map { it.rawBytes }, rows.map { it.byteCount })
        assertTrue(rows.all { it.readable })
    }

    private fun assertBudget(sample: Sample<*>, maxMs: Long, maxMiB: Long) {
        assertTrue(sample.phase + " took " + sample.elapsedNs + " ns",
            sample.elapsedNs <= maxMs * MILLIS)
        assertTrue(sample.phase + " managed heap increase " + sample.observedGrowth + " bytes",
            sample.observedGrowth <= maxMiB * MIB)
    }

    private fun seedTwelve(
        fixture: RegularProjectTestFixture, context: Context, sources: MutableList<Uri>
    ): List<Entry> {
        val entries = mutableListOf<Entry>()
        writeRegularLegacyXml(fixture.legacyXml) { emit ->
            for (index in SLOTS downTo 1) {
                val name = "R1.1 " + index.toString().padStart(2, '0')
                val source = source(context, index, sources)
                val exposure = (index - 7) * .05f
                val document = nearBudgetDrawing(exposure)
                val recipe = EditDocumentCodec.encodeBounded(document)
                assertTrue(recipe.length in (RECIPE_TARGET - 30)..RECIPE_TARGET)
                val raw = regularLegacyValue(source, recipe)
                emit(name, raw)
                val bytes = raw.toByteArray(Charsets.UTF_8)
                val project = NamedProjectRef(ProjectKind.PHOTO,
                    NamedProjectIdentity.reserve(context, ProjectKind.PHOTO, name, false),
                    name, NamedProjectIdentity.version(bytes))
                entries += Entry(project, source, exposure, document.creative.strokes.sumOf { it.points.size },
                    bytes.size.toLong())
            }
        }
        assertEquals(SLOTS, entries.map { it.project.id }.toSet().size)
        assertEquals(SLOTS, entries.map { it.project.version }.toSet().size)
        assertEquals(SLOTS, entries.map { it.source }.toSet().size)
        return entries.sortedBy { it.project.name }
    }

    private fun source(context: Context, index: Int, sources: MutableList<Uri>): Uri {
        val resolver = context.contentResolver
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "gz-r11-ready-" + UUID.randomUUID() + ".png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        sources += uri
        val bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        try {
            bitmap.eraseColor(Color.rgb(35 + index * 8, 105, 155))
            requireNotNull(resolver.openOutputStream(uri, "wt")).use {
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
        } finally {
            bitmap.recycle()
        }
        assertEquals(1, resolver.update(uri, ContentValues().apply {
            put(MediaStore.Images.Media.IS_PENDING, 0)
        }, null, null))
        return uri
    }

    // The same 500-point stroke shape, grade markers and recipe target as the
    // legacy measurements in the test-only baseline commit.
    private fun nearBudgetDrawing(exposure: Float): EditDocument {
        val document = EditDocument(grade = Grade(exposure = exposure))
        val a = .00012345678f
        val b = .0034567893f
        val samples = List(500) { if (it % 2 == 0) BrushPoint(a, b) else BrushPoint(b, a) }
        val pairLength = (samples.first().x.toString() + "," + samples.first().y).length
        val strokes = mutableListOf<InkStroke>()
        var estimated = EditDocumentCodec.encode(document).length + 2
        repeat(160) { index ->
            val stroke = InkStroke(samples)
            val prefix = "\ncreative.ink." + index + "."
            val fields = prefix + "color=" + stroke.color +
                prefix + "opacity=" + stroke.opacity +
                prefix + "width=" + stroke.thickness +
                prefix + "erase=" + stroke.erase +
                prefix + "points="
            val count = minOf(samples.size, (RECIPE_TARGET - estimated - fields.length + 1) / (pairLength + 1))
            if (count > 0) {
                strokes += stroke.copy(points = if (count == samples.size) samples else samples.take(count))
                estimated += fields.length + count * (pairLength + 1) - 1
            }
        }
        assertTrue(strokes.size in 100..160)
        return document.copy(creative = CreativeDocument(strokes = strokes))
    }

    private fun heapUsed(): Long = runtime.totalMemory() - runtime.freeMemory()

    private fun requestGcOutsideTiming() {
        runtime.gc()
        SystemClock.sleep(40)
    }

    private fun <T> measure(phase: String, work: () -> T): Sample<T> {
        Log.i(TAG, "phase=" + phase + " measurement=start")
        val before = heapUsed()
        val peak = AtomicLong(before)
        val samples = AtomicLong(0)
        val running = AtomicBoolean(true)
        val allocated = Debug.getRuntimeStat("art.gc.bytes-allocated")?.toLongOrNull()
        val sampler = thread(isDaemon = true, name = "r11-heap-sample") {
            while (running.get()) {
                peak.updateAndGet { maxOf(it, heapUsed()) }
                samples.incrementAndGet()
                LockSupport.parkNanos(2_000_000)
            }
        }
        val start = SystemClock.elapsedRealtimeNanos()
        try {
            val result = work()
            val elapsed = SystemClock.elapsedRealtimeNanos() - start
            val after = heapUsed()
            val high = maxOf(peak.get(), after)
            val totalAllocated = Debug.getRuntimeStat("art.gc.bytes-allocated")?.toLongOrNull()
            val allocatedDelta = if (allocated != null && totalAllocated != null) totalAllocated - allocated else null
            Log.i(TAG, "phase=" + phase + " elapsedNs=" + elapsed + " managedBeforeBytes=" + before +
                " managedObservedPeakBytes=" + high + " managedEndBytes=" + after +
                " heapSamples=" + samples.get() + " javaMaxBytes=" + runtime.maxMemory() +
                " artAllocatedDeltaBytes=" + allocatedDelta)
            return Sample(phase, result, elapsed, high - before)
        } finally {
            running.set(false)
            sampler.interrupt()
            sampler.join(2_000)
        }
    }
}

package com.greenezone.gzphotoeditor

import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Debug
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.util.Xml
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.BrushPoint
import com.greenezone.gzphotoeditor.editor.CreativeDocument
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.EditDocumentCodec
import com.greenezone.gzphotoeditor.editor.Grade
import com.greenezone.gzphotoeditor.editor.InkStroke
import com.greenezone.gzphotoeditor.editor.NamedProject
import com.greenezone.gzphotoeditor.editor.NamedProjectIdentity
import com.greenezone.gzphotoeditor.editor.ProjectKind
import com.greenezone.gzphotoeditor.editor.ProjectStore
import com.greenezone.gzphotoeditor.editor.SavedProject
import java.io.File
import java.nio.file.Files
import java.util.Base64
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.LockSupport
import kotlin.concurrent.thread
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Measurements of the legacy Regular store before R1.1 migration. Fresh names
 * isolate the real SharedPreferences XML and cache in this instrumentation PID.
 * The XML was just generated, so these are preference-cache-cold timings.
 */
@RunWith(AndroidJUnit4::class)
class AndroidRegularProjectListBaselineTest {
    private companion object {
        const val TAG = "GzR11Baseline"
        const val PREFS = "gzphotoeditor.projects.v1"
        const val RECIPE_TARGET = 1_990_000
        const val SLOTS = 12
    }

    private val target = InstrumentationRegistry.getInstrumentation().targetContext
    private val runtime = Runtime.getRuntime()

    private data class FixtureEntry(
        val name: String,
        val source: Uri,
        val exposure: Float,
        val points: Int,
        val chars: Int,
        val id: String,
        val version: String
    )

    private data class ProjectSummary(
        val name: String,
        val source: Uri,
        val exposure: Float,
        val points: Int
    )

    private data class NamedSummary(
        val kind: ProjectKind,
        val name: String,
        val id: String,
        val version: String,
        val content: ProjectSummary
    )

    private inner class FixtureStorage(val files: File) {
        private val preferences = mutableListOf<String>()
        private val sources = mutableListOf<Uri>()

        fun freshPreferences(): Pair<Context, File> {
            val name = "gz-r11-baseline-" + UUID.randomUUID()
            preferences += name
            val directory = File(target.applicationInfo.dataDir, "shared_prefs")
            check(directory.isDirectory || directory.mkdirs())
            val context = object : ContextWrapper(target) {
                override fun getFilesDir(): File = files

                override fun getSharedPreferences(nameFromCaller: String, mode: Int): SharedPreferences =
                    super.getSharedPreferences(if (nameFromCaller == PREFS) name else nameFromCaller, mode)
            }
            return context to File(directory, "$name.xml")
        }

        fun source(index: Int): Uri {
            val resolver = target.contentResolver
            val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "gz-r11-" + UUID.randomUUID() + ".png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }))
            sources += uri
            val bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
            try {
                bitmap.eraseColor(Color.rgb(35 + index * 8, 105, 155))
                requireNotNull(resolver.openOutputStream(uri, "wt")).use {
                    check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
                }
            } finally {
                bitmap.recycle()
            }
            check(resolver.update(uri, ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }, null, null) == 1)
            return uri
        }

        fun close() {
            preferences.forEach { target.deleteSharedPreferences(it) }
            sources.forEach { target.contentResolver.delete(it, null, null) }
            files.deleteRecursively()
        }
    }

    @Test fun legacyTwelveProjectListingAndSelectedOpenBaseline() = withStorage { storage ->
        val (context, xml) = storage.freshPreferences()
        val entries = seedTwelve(storage, context, xml)
        assertEquals(SLOTS, entries.map { it.id }.toSet().size)
        assertEquals(SLOTS, entries.map { it.version }.toSet().size)
        assertEquals(SLOTS, entries.map { it.source }.toSet().size)

        // The actual preferences cache is keyed by name, so this second real
        // XML file has the same records and a separately cold cache. No call
        // to getSharedPreferences has occurred for either name.
        val (selectedFirstContext, selectedFirstXml) = storage.freshPreferences()
        xml.copyTo(selectedFirstXml)
        requestGcOutsideTiming()
        Log.i(TAG, "fixture=twelve_healthy count=${entries.size} rawXmlBytes=${xml.length()}" +
            " recipeCharsByName=${entries.joinToString(",") { it.name + ":" + it.chars }}" +
            " javaMaxBytes=${runtime.maxMemory()} pid=${android.os.Process.myPid()}" +
            " coldDefinition=fresh_preferences_name_same_pid_warm_os_page_cache")

        val selected = entries.last()
        val selectedFirst = readNamed("cache_cold_selected_first", selectedFirstContext, selected.name)
        val rawSelected = readRawCache("selected_first_cache", selectedFirstContext)
        target.deleteSharedPreferences(selectedFirstXml.nameWithoutExtension)
        requestGcOutsideTiming()

        var materialized = readList("cache_cold_list", context)
        val rawList = readRawCache("list_cache", context)

        // Both cold paths and their raw cache measurements are captured before
        // enforcing any correctness checks that could abort the benchmark.
        val problems = mutableListOf<Throwable>()
        checkEvidence(problems, "cache_cold_selected_first") {
            assertNamed(selected, selectedFirst.getOrThrow())
        }
        checkEvidence(problems, "selected_first_cache") {
            assertEquals(SLOTS, rawSelected.getOrThrow())
        }
        checkEvidence(problems, "list_cache") {
            assertEquals(SLOTS, rawList.getOrThrow())
        }
        var healthyList = checkEvidence(problems, "cache_cold_list") {
            assertMaterialized(entries, materialized.getOrThrow())
        }
        repeat(3) { iteration ->
            val phase = "cache_warm_list_" + (iteration + 1)
            if (healthyList) {
                val previous = materialized.getOrThrow()
                materialized = readList(phase, context)
                // Keep the prior result alive through the refresh, as the live
                // composable does while computing its replacement list.
                assertEquals(entries.last().source, previous.last().image)
                healthyList = checkEvidence(problems, phase) {
                    assertMaterialized(entries, materialized.getOrThrow())
                }
            } else {
                Log.i(TAG, "phase=$phase outcome=skipped prerequisite=verified_previous_list")
            }
        }

        listOf(entries.first(), entries[5], entries.last()).forEachIndexed { index, entry ->
            val phase = "cache_warm_selected_open_" + (index + 1)
            if (rawList.getOrNull() == SLOTS) {
                val reopened = readNamed(phase, context, entry.name)
                checkEvidence(problems, phase) { assertNamed(entry, reopened.getOrThrow()) }
            } else {
                Log.i(TAG, "phase=$phase outcome=skipped prerequisite=warm_raw_preferences_cache")
            }
        }

        requestGcOutsideTiming()
        val finalList = materialized.getOrNull()
        Log.i(TAG, "phase=list_retained_after_requested_gc" +
            " managedUsedBytes=${heapUsed()} javaMaxBytes=${runtime.maxMemory()}" +
            " decodedProjectsHeld=${finalList?.size}")
        if (healthyList) assertMaterialized(entries, requireNotNull(finalList))
        if (problems.isNotEmpty()) {
            val failure = AssertionError("Legacy baseline observed " + problems.size +
                " correctness failures. All independent evidence was logged as " + TAG + ".")
            problems.forEach(failure::addSuppressed)
            throw failure
        }
    }

    @Test fun separateLegacyFaultLaneDistinguishesCorruptRecipeFromUnavailableSource() =
        withStorage { storage ->
            val (context, xml) = storage.freshPreferences()
            val healthy = storage.source(0)
            val missing = Uri.parse("content://com.greenezone.gzphotoeditor.r11.missing/source")
            val validRecipe = EditDocumentCodec.encodeBounded(EditDocument(grade = Grade(exposure = .2f)))
            writePreferences(xml) { emit ->
                emit("Readable", envelope(healthy, validRecipe))
                emit("Unavailable", envelope(missing, validRecipe))
                emit("Corrupt", envelope(healthy, "version=unsupported-r11-fixture"))
            }

            val listed = ProjectStore.load(context)
            assertEquals(listOf("Readable", "Unavailable"), listed.map { it.name })
            assertNull(ProjectStore.loadNamed(context, "Corrupt"))
            val unavailable = requireNotNull(ProjectStore.loadNamed(context, "Unavailable"))
            assertEquals(missing, unavailable.content.image)
            assertEquals(healthy, requireNotNull(ProjectStore.loadNamed(context, "Readable")).content.image)
            assertNull(runCatching { context.contentResolver.openInputStream(missing)?.use { it.read() } }
                .getOrNull())
            Log.i(TAG, "fixture=separate_legacy_fault_lane rawXmlBytes=${xml.length()}" +
                " corruptOmittedByLegacyList=true unavailableRecipeListed=true mediaOpened=false")
            assertEquals(3, readRawCache("fault_lane_cache", context).getOrThrow())
        }

    private fun readList(phase: String, context: Context): Result<List<SavedProject>> {
        val result = runCatching { measure(phase) { ProjectStore.load(context) } }
        result.onSuccess { Log.i(TAG, "phase=$phase outcome=returned decodedCount=${it.size}") }
            .onFailure { Log.i(TAG, "phase=$phase outcome=threw error=${it.javaClass.simpleName}") }
        return result
    }

    private fun readNamed(phase: String, context: Context, name: String): Result<NamedSummary?> {
        val result = runCatching {
            measure(phase) { ProjectStore.loadNamed(context, name) }?.let(::summarize)
        }
        result.onSuccess {
            val outcome = if (it == null) "null" else "returned"
            Log.i(TAG, "phase=$phase outcome=$outcome name=$name decodedCount=${if (it == null) 0 else 1}")
        }.onFailure {
            Log.i(TAG, "phase=$phase outcome=threw name=$name error=${it.javaClass.simpleName}")
        }
        return result
    }

    private fun checkEvidence(problems: MutableList<Throwable>, phase: String, check: () -> Unit): Boolean {
        return runCatching(check).onFailure { cause ->
            Log.i(TAG, "phase=$phase correctness=failed reason=${cause.message}")
            problems += AssertionError(phase, cause)
        }.isSuccess
    }

    private fun withStorage(block: (FixtureStorage) -> Unit) {
        val storage = FixtureStorage(Files.createTempDirectory(target.cacheDir.toPath(),
            "r11-baseline-files-").toFile())
        try {
            block(storage)
        } finally {
            storage.close()
        }
    }

    private fun seedTwelve(storage: FixtureStorage, context: Context, xml: File): List<FixtureEntry> {
        val entries = mutableListOf<FixtureEntry>()
        // Write one record at a time. Do not open SharedPreferences to seed or
        // inspect it before the cold sample. Reverse write order to verify sort.
        writePreferences(xml) { emit ->
            for (index in SLOTS downTo 1) {
                val name = "R1.1 " + index.toString().padStart(2, '0')
                val uri = storage.source(index)
                val exposure = (index - 7) * .05f
                val document = nearBudgetDrawing(exposure)
                val recipe = EditDocumentCodec.encodeBounded(document)
                assertTrue(recipe.length in (RECIPE_TARGET - 30)..RECIPE_TARGET)
                val payload = envelope(uri, recipe)
                emit(name, payload)
                val id = NamedProjectIdentity.reserve(context, ProjectKind.PHOTO, name,
                    payloadExists = false)
                entries += FixtureEntry(name, uri, exposure, document.creative.strokes.sumOf { it.points.size },
                    recipe.length, id, NamedProjectIdentity.version(payload.toByteArray(Charsets.UTF_8)))
            }
        }
        return entries.sortedBy { it.name }
    }

    private fun writePreferences(file: File, records: ((String, String) -> Unit) -> Unit) {
        file.outputStream().buffered().use { output ->
            val serializer = Xml.newSerializer()
            serializer.setOutput(output, "UTF-8")
            serializer.startDocument("UTF-8", true)
            serializer.startTag(null, "map")
            records { name, value ->
                serializer.startTag(null, "string")
                serializer.attribute(null, "name", base64(ProjectStore.normalizedName(name)))
                serializer.text(value)
                serializer.endTag(null, "string")
            }
            serializer.endTag(null, "map")
            serializer.endDocument()
            serializer.flush()
        }
    }

    private fun base64(value: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray(Charsets.UTF_8))

    private fun envelope(source: Uri, recipe: String): String = base64(source.toString()) + "\n" + recipe

    // Match the dense ink representation in AndroidRegularSaveBudgetTest.
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

    private fun assertMaterialized(entries: List<FixtureEntry>, actual: List<SavedProject>) {
        assertEquals("Expected all twelve full recipe graphs", entries.map { it.name }, actual.map { it.name })
        entries.zip(actual).forEach { (entry, project) -> assertProject(entry, summarize(project)) }
    }

    private fun summarize(named: NamedProject<SavedProject>): NamedSummary = NamedSummary(
        named.project.kind, named.project.name, named.project.id, named.project.version,
        summarize(named.content)
    )

    private fun summarize(project: SavedProject): ProjectSummary = ProjectSummary(
        project.name, project.image, project.document.grade.exposure,
        project.document.creative.strokes.sumOf { it.points.size }
    )

    private fun assertNamed(entry: FixtureEntry, actual: NamedSummary?) {
        val named = requireNotNull(actual) { "Named project unavailable: " + entry.name }
        assertEquals(ProjectKind.PHOTO, named.kind)
        assertEquals(entry.name, named.name)
        assertEquals(entry.id, named.id)
        assertEquals(entry.version, named.version)
        assertProject(entry, named.content)
    }

    private fun assertProject(entry: FixtureEntry, project: ProjectSummary) {
        assertEquals(entry.name, project.name)
        assertEquals(entry.source, project.source)
        assertEquals(entry.exposure, project.exposure, 0f)
        assertEquals(entry.points, project.points)
    }

    private fun readRawCache(phase: String, context: Context): Result<Int> = runCatching {
        // .all is intentionally outside timing and heap measurements. Its small
        // shallow map copy exposes the actual resident String lengths.
        val all = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).all
        val rawChars = all.values.sumOf { (it as String).length.toLong() }
        Log.i(TAG, "phase=$phase outcome=returned cachedEntries=${all.size} cachedRawStringChars=$rawChars")
        all.size
    }.onFailure {
        Log.i(TAG, "phase=$phase outcome=threw error=${it.javaClass.simpleName}")
    }

    private fun heapUsed(): Long = runtime.totalMemory() - runtime.freeMemory()

    private fun requestGcOutsideTiming() {
        runtime.gc()
        SystemClock.sleep(40)
    }

    private fun <T> measure(phase: String, work: () -> T): T {
        Log.i(TAG, "phase=$phase measurement=start")
        val before = heapUsed()
        val peak = AtomicLong(before)
        val samples = AtomicLong(0)
        val running = AtomicBoolean(true)
        val allocated = Debug.getRuntimeStat("art.gc.bytes-allocated")?.toLongOrNull()
        val sampler = thread(isDaemon = true, name = "r11-heap-sample") {
            while (running.get()) {
                val used = heapUsed()
                peak.updateAndGet { maxOf(it, used) }
                samples.incrementAndGet()
                LockSupport.parkNanos(2_000_000)
            }
        }
        val start = SystemClock.elapsedRealtimeNanos()
        try {
            return work()
        } finally {
            val elapsed = SystemClock.elapsedRealtimeNanos() - start
            val after = heapUsed()
            running.set(false)
            sampler.interrupt()
            sampler.join(2_000)
            val totalAllocated = Debug.getRuntimeStat("art.gc.bytes-allocated")?.toLongOrNull()
            val allocatedDelta = if (allocated != null && totalAllocated != null) totalAllocated - allocated else null
            Log.i(TAG, "phase=$phase elapsedNs=$elapsed managedBeforeBytes=$before" +
                " managedObservedPeakBytes=${maxOf(peak.get(), after)} managedEndBytes=$after" +
                " heapSamples=${samples.get()} javaMaxBytes=${runtime.maxMemory()}" +
                " artAllocatedDeltaBytes=$allocatedDelta")
        }
    }
}

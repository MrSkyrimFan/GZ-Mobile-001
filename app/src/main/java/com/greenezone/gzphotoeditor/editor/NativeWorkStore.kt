package com.greenezone.gzphotoeditor.editor

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Debug
import android.os.SystemClock
import java.io.File
import java.io.FileOutputStream
import java.io.FilterOutputStream
import java.io.IOException
import java.util.IdentityHashMap
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** Only the verified catalog publishes working roots. Immutable file preparation has no route effects. */
internal class NativeWorkStore(context: Context) {
    private val context = context
    private val files = NativeWorkFiles(this.context)
    private val directoryKey = files.root.absolutePath

    fun hasRecoveryRecord(): Boolean = catalogFiles().any(File::exists)

    /** Exact handles still protected in this store. Instrumentation uses this after operations settle. */
    internal suspend fun outstandingStageLeasesForTest(): Int =
        withContext(Dispatchers.IO) { mutex.withLock { stages[directoryKey]?.size ?: 0 } }

    suspend fun recover(): NativeWorkResult<NativeWorkCatalog> = locked {
        val current = readCatalog()
        if (coldFolders.add(directoryKey)) {
            // No editor in this process can hold an open lease before its first recovery.
            if (stages[directoryKey].isNullOrEmpty()) {
                runCatching { collect(current, cold = true) }
                runCatching { files.deleteStageFilesOnly() }
            }
        }
        current.public
    }

    suspend fun load(ref: NativeWorkRef): NativeWorkResult<NativeWorkLoaded> = locked {
        val budget = Budget(10_000L, 128L * 1024 * 1024)
        val current = readCatalog()
        val row = (listOfNotNull(current.active) + current.drafts)
            .singleOrNull { it.summary.ref == ref } ?: throw StaleWork()
        val state = NativeWorkCodec(files, budget::check).decode(row.manifest)
        require(row.summary == summary(ref, state))
        budget.check()
        NativeWorkLoaded(ref, state)
    }

    suspend fun loadNamed(
        manifest: NativeManifestRef, namedRef: NamedProjectRef
    ): NativeWorkResult<NativeWorkState> = locked {
        val budget = Budget(10_000L, 128L * 1024 * 1024)
        val state = NativeWorkCodec(files, budget::check).decode(manifest)
        val photo = state.mode == NativeWorkMode.REGULAR
        val currentManifest = if (photo) {
            val selected = ProjectStore.loadNamed(context, namedRef) ?: throw StaleWork()
            selected.content.linkedWork
        } else {
            val selected = ProStore.loadNamed(context, namedRef.name) ?: throw StaleWork()
            if (selected.project != namedRef) throw StaleWork()
            selected.content.linkedWork
        }
        if (currentManifest != manifest) throw StaleWork()
        val regular = state.regular?.let {
            it.copy(ownerId = newId(), savedRef = if (photo) namedRef else it.savedRef,
                clean = if (photo || state.link != null) it.document else it.clean)
        }
        val pro = state.pro?.let { old ->
            val owner = newId()
            fun target(saved: ProSaveTarget): ProSaveTarget =
                saved.copy(nativeOwnerId = "$owner/${saved.workId}")
            fun entry(value: ProHistoryEntry): ProHistoryEntry = value.copy(saveTarget = target(value.saveTarget))
            val active = target(old.saveTarget).let {
                if (photo) it else it.copy(name = namedRef.name, savedRef = namedRef)
            }
            val history = ProHistory(old.history.undo.map(::entry), old.history.redo.map(::entry))
            old.copy(ownerId = owner, saveTarget = active,
                history = if (photo) history else history.bindSavedProject(active),
                baselines = if (!photo || state.link != null) old.baselines + (active.workId to
                    ProWorkBaseline(old.baselines[active.workId]?.entryDocument ?: old.entry ?: old.document,
                        old.document)) else old.baselines)
        }
        budget.check()
        state.copy(id = newId(), regular = regular, pro = pro)
    }

    suspend fun prepare(state: NativeWorkState): NativeWorkResult<NativeWorkPrepared> {
        var undelivered: NativeWorkPrepared? = null
        try {
            val result = locked {
                val budget = Budget(10_000L, 128L * 1024 * 1024)
                val current = readCatalog()
                val prepared = try {
                    NativeWorkCodec(files, budget::check).encode(state)
                } catch (full: NativeWorkLimit) {
                    collect(current, cold = false)
                    NativeWorkCodec(files, budget::check).encode(state)
                }
                budget.check()
                undelivered = prepared
                protect(prepared, "m:" + prepared.manifest.sha)
                prepared
            }
            // A completed IO block can still lose its result to prompt cancellation
            // before this caller resumes. Keep ownership until that handoff succeeds.
            if (result is NativeWorkResult.Ok) undelivered = null
            return result
        } finally {
            undelivered?.let { prepared ->
                withContext(NonCancellable) { release(prepared) }
            }
        }
    }

    /**
     * A single verified publication replaces active work and parks a displaced root. The
     * compare-and-set revision includes blank routes and removals, so stale intent cannot win.
     */
    suspend fun acknowledge(
        expectedCatalogRevision: Long,
        active: NativeWorkPrepared?,
        displaced: NativeWorkPrepared? = null,
        resume: NativeWorkRef? = null,
        route: NativeWorkMode? = active?.state?.mode
    ): NativeWorkResult<NativeWorkCatalog> = locked {
        val budget = Budget(10_000L, 128L * 1024 * 1024)
        val current = readCatalog()
        if (current.revision != expectedCatalogRevision) throw StaleWork()
        if (resume != null && current.drafts.none { it.summary.ref == resume }) throw StaleWork()
        require(active == null || route == active.state.mode)
        require(active == null || displaced == null || active.state.id != displaced.state.id)
        val revision = nextRevision(current.revision)
        val activeRow = active?.let { row(it, revision, current.retired) }
        val displacedRow = displaced?.let { row(it, revision, current.retired) }
        val drafts = current.drafts.filter { entry ->
            entry.summary.ref != resume && entry.summary.ref.id != activeRow?.summary?.ref?.id &&
                entry.summary.ref.id != displacedRow?.summary?.ref?.id
        } + listOfNotNull(displacedRow)
        if (drafts.size > NativeWorkFiles.MAX_DRAFTS) throw NativeWorkLimit()
        val next = Catalog(revision, activeRow, drafts, current.retired, route)
        budget.check()
        publish(next)
        active?.let { staged().remove(it) }
        displaced?.let { staged().remove(it) }
        next.public
    }

    suspend fun delete(
        expectedCatalogRevision: Long, expected: NativeWorkRef, clearRoute: Boolean = false
    ): NativeWorkResult<NativeWorkCatalog> = locked {
        val current = readCatalog()
        val row = current.active?.takeIf { it.summary.ref == expected } ?: throw StaleWork()
        if (current.revision != expectedCatalogRevision) throw StaleWork()
        val retired = current.retired + row.owners + expected.id
        val next = current.copy(revision = nextRevision(current.revision), active = null, retired = retired,
            route = if (clearRoute) null else current.route)
        publish(next)
        next.public
    }

    suspend fun saveNamed(intent: ProjectSaveIntent, prepared: NativeWorkPrepared): ProjectSaveResult =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val result = guarded {
                    val state = prepared.state
                    val source = NativeWorkCodec(files).open(prepared.manifest)
                    require(source.getString("id") == state.id && source.getString("mode") == state.mode.name)
                    verifyReachable(source)
                    state.pro?.document?.let { document ->
                        ProRaster.validateLinkedGeometry(document, NativeWorkCodec(files)::finishRecipe)
                    }
                    when (state.mode) {
                        NativeWorkMode.REGULAR -> {
                            val photo = requireNotNull(state.regular)
                            if (state.link == null)
                                ProjectStore.saveNamed(context, intent, photo.uri, photo.document)
                            else ProjectStore.saveLinkedNamed(context, intent, photo.uri, photo.document, prepared.manifest)
                        }
                        NativeWorkMode.PRO -> {
                            val pro = requireNotNull(state.pro)
                            if (state.link == null)
                                ProStore.saveNamed(context, intent, pro.document, pro.reset)
                            else ProStore.saveLinkedNamed(context, intent, pro.document, pro.reset, prepared.manifest)
                        }
                    }
                }
                when (result) {
                    is NativeWorkResult.Ok -> result.value
                    is NativeWorkResult.Failed -> ProjectSaveResult.Rejected(when (result.reason) {
                        NativeWorkFailure.STALE -> ProjectSaveFailure.TARGET_CHANGED
                        NativeWorkFailure.INVALID -> ProjectSaveFailure.INVALID_DOCUMENT
                        NativeWorkFailure.LIMIT -> ProjectSaveFailure.TOO_LARGE
                        NativeWorkFailure.UNAVAILABLE, NativeWorkFailure.STORAGE -> ProjectSaveFailure.STORAGE_FAILURE
                    })
                }
            }
        }

    suspend fun stageDerivative(document: ProDocument): NativeWorkResult<NativeDerivativeRef> =
        withContext(Dispatchers.IO) {
            val job = currentCoroutineContext()
            guarded {
                val budget = Budget(6_000L, 64L * 1024 * 1024, 64L * 1024 * 1024)
                val size = ProMath.intendedCanvasSize(document)
                NativeWorkCodec.checkBridgeSize(size)
                val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                if (size.pixelCount > GeometryMath.safeExportPixels(manager.memoryClass)) throw NativeWorkLimit()
                val rendering = Any()
                val parent = mutex.withLock {
                    val codec = NativeWorkCodec(files)
                    val owned = codec.pro(document)
                    protect(rendering, "r:" + owned.sha)
                    owned
                }
                var temporary: File? = null
                try {
                    val stage = files.newDerivativeStage().also { temporary = it }
                    val bitmap = ProRaster.renderOutput(context, document, size, preview = false) {
                        job.ensureActive()
                        budget.check()
                    }
                    val frame: ProPixelSize
                    val alpha: Boolean
                    val samples: List<Int>
                    try {
                        frame = ProPixelSize(bitmap.width, bitmap.height)
                        NativeWorkCodec.checkBridgeSize(frame)
                        alpha = bitmap.hasAlpha()
                        samples = samples(bitmap)
                        FileOutputStream(stage).use { output ->
                            val bounded = BoundedPngStream(output)
                            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, bounded))
                                throw IOException("Bridge PNG compression failed")
                            output.fd.sync()
                        }
                        budget.check()
                    } finally {
                        bitmap.recycle()
                    }
                    val decoded = BitmapFactory.decodeFile(stage.path, BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }) ?: throw NativeWorkUnavailable()
                    try {
                        if (decoded.width != frame.width || decoded.height != frame.height ||
                            samples(decoded) != samples || (decoded.hasAlpha() && !alpha))
                            throw NativeWorkUnavailable()
                        budget.check()
                    } finally {
                        decoded.recycle()
                    }
                    val bytes = stage.length()
                    val sha = NativeWorkFiles.digest(stage, NativeWorkFiles.MAX_DERIVATIVE_BYTES)
                    job.ensureActive()
                    mutex.withLock {
                        budget.check()
                        val output = files.derivative(sha)
                        if (!output.exists()) {
                            try { files.requireCapacity(bytes) } catch (_: NativeWorkLimit) {
                                collect(readCatalog(), cold = false)
                                files.requireCapacity(bytes)
                            }
                        }
                        files.publish(stage, output, bytes, sha, NativeWorkFiles.MAX_DERIVATIVE_BYTES)
                        NativeDerivativeRef(files.derivativeUri(sha), frame, sha, parent, bytes, alpha)
                            .also { protect(it, "d:" + sha, "r:" + parent.sha) }
                    }
                } finally {
                    temporary?.delete()
                    withContext(NonCancellable) { mutex.withLock { staged().remove(rendering) } }
                }
            }
        }

    suspend fun stageFinish(
        parent: ProDocument, derivative: NativeDerivativeRef, recipe: EditDocument
    ): NativeWorkResult<ProLinkedFinishRef> = locked {
        if (parent.linkedFinishes.size >= ProMath.MAX_LINKED_FINISHES) throw NativeWorkLimit()
        val codec = NativeWorkCodec(files)
        if (codec.pro(parent) != derivative.parent) throw StaleWork()
        codec.verifyDerivative(derivative)
        val shape = PhotoGeometryPlan(derivative.size.width, derivative.size.height, PhotoGeometry.from(recipe))
        val output = ProPixelSize(shape.width, shape.height)
        NativeWorkCodec.checkBridgeSize(output)
        val revision = codec.regular(recipe)
        ProLinkedFinishRef(revision.sha, derivative.size, output, derivative.parent.sha, derivative.sha)
            .also { protect(it, "r:" + revision.sha, "r:" + derivative.parent.sha, "d:" + derivative.sha) }
    }

    /** Release only the original staged handle. Published roots and other handles still retain their data. */
    suspend fun release(prepared: NativeWorkPrepared) = releaseStage(prepared)
    suspend fun release(derivative: NativeDerivativeRef) = releaseStage(derivative)
    suspend fun release(finish: ProLinkedFinishRef) = releaseStage(finish)

    private suspend fun releaseStage(token: Any) {
        withContext(NonCancellable + Dispatchers.IO) { mutex.withLock { staged().remove(token) } }
    }

    private data class Row(
        val summary: NativeWorkSummary, val manifest: NativeManifestRef, val owners: Set<String>
    )

    private data class Catalog(
        val revision: Long = 0,
        val active: Row? = null,
        val drafts: List<Row> = emptyList(),
        val retired: Set<String> = emptySet(),
        val route: NativeWorkMode? = null
    ) {
        val public: NativeWorkCatalog get() =
            NativeWorkCatalog(revision, active?.summary, drafts.map(Row::summary), retired, route)
    }

    private fun readCatalog(): Catalog {
        if (!hasRecoveryRecord()) return Catalog()
        val result = NamedProjectIdentity.readPayload(files.catalog, NativeWorkFiles.MAX_INDEX_BYTES) { input, _ ->
            decodeCatalog(JSONObject(input.bufferedReader(Charsets.UTF_8).use { it.readText() }))
        }
        return result?.content ?: if (!hasRecoveryRecord()) Catalog() else throw NativeWorkUnavailable()
    }

    private suspend fun publish(catalog: Catalog) {
        val body = JSONObject().put("version", 1).put("revision", catalog.revision)
            .put("drafts", JSONArray(catalog.drafts.map(::encodeRow)))
            .put("retired", JSONArray(catalog.retired.sorted())).apply {
                catalog.active?.let { put("active", encodeRow(it)) }
                catalog.route?.let { put("route", it.name) }
            }.toString().toByteArray(Charsets.UTF_8)
        if (body.size > NativeWorkFiles.MAX_INDEX_BYTES) throw NativeWorkLimit()
        currentCoroutineContext().ensureActive()
        withContext(NonCancellable) {
            NativeWorkFiles.mkdir(files.root)
            NamedProjectIdentity.publish(files.catalog, body, NativeWorkFiles.MAX_INDEX_BYTES) { path, expected, max ->
                NamedProjectIdentity.verify(path, expected, max)
                NativeWorkFiles.syncDirectory(files.root)
            }
        }
    }

    private fun encodeRow(row: Row): JSONObject = JSONObject().put("id", row.summary.ref.id)
        .put("revision", row.summary.ref.revision).put("mode", row.summary.mode.name)
        .put("manifest", row.manifest.sha)
        .put("owners", JSONArray(row.owners.sorted()))
        .put("modes", JSONArray(row.summary.recoverableModes.map { it.name }.sorted()))

    private fun decodeRow(json: JSONObject, catalogRevision: Long): Row {
        val ref = NativeWorkRef(NativeWorkCodec.checkedKey(json.getString("id")),
            json.getLong("revision").also { require(it in 1..catalogRevision) })
        val mode = NativeWorkMode.valueOf(json.getString("mode"))
        val modes = strings(json.getJSONArray("modes")).map(NativeWorkMode::valueOf).toSet()
        require(mode in modes)
        return Row(NativeWorkSummary(ref, mode, modes),
            NativeManifestRef(NativeWorkCodec.checkedSha(json.getString("manifest"))),
            strings(json.getJSONArray("owners")).map(NativeWorkCodec::checkedKey).toSet())
    }

    private fun decodeCatalog(json: JSONObject): Catalog {
        require(json.getInt("version") == 1)
        val revision = NativeWorkCodec.validRevision(json.getLong("revision"))
        val active = json.optJSONObject("active")?.let { decodeRow(it, revision) }
        val drafts = json.getJSONArray("drafts")
        require(drafts.length() <= NativeWorkFiles.MAX_DRAFTS)
        val list = List(drafts.length()) { decodeRow(drafts.getJSONObject(it), revision) }
        val owners = (list + listOfNotNull(active)).map { it.summary.ref.id }
        require(owners.toSet().size == owners.size)
        val retired = strings(json.getJSONArray("retired")).map(NativeWorkCodec::checkedKey).toSet()
        val route = NativeWorkCodec.optionalKey(json, "route")?.let(NativeWorkMode::valueOf)
        require(active == null || route == active.summary.mode)
        return Catalog(revision, active, list, retired, route)
    }

    private fun catalogFiles(): List<File> = listOf("", ".bak", ".new", ".save-pending",
        ".save-pending.bak", ".save-pending.new", ".save-previous", ".save-previous.bak").map {
        File(files.catalog.path + it)
    }

    private fun row(prepared: NativeWorkPrepared, revision: Long, retired: Set<String>): Row {
        val state = prepared.state
        val body = NativeWorkCodec(files).open(prepared.manifest)
        require(body.getString("id") == state.id && body.getString("mode") == state.mode.name)
        val owners = ownerKeys(body)
        if (state.id in retired || owners.any { it in retired }) throw StaleWork()
        verifyReachable(body)
        return Row(summary(NativeWorkRef(state.id, revision), state), prepared.manifest, owners)
    }

    private fun summary(ref: NativeWorkRef, state: NativeWorkState) = NativeWorkSummary(ref, state.mode,
        buildSet {
            if (state.regular != null) add(NativeWorkMode.REGULAR)
            if (state.pro != null) add(NativeWorkMode.PRO)
        })

    private fun ownerKeys(body: JSONObject): Set<String> = buildSet {
        body.optJSONObject("regular")?.let { add(it.getString("owner")) }
        body.optJSONObject("pro")?.let { pro ->
            val owner = pro.getString("owner")
            add(owner)
            fun target(raw: JSONObject) {
                add(raw.optString("owner").ifEmpty { "$owner/${raw.getLong("workId")}" })
            }
            target(pro.getJSONObject("target"))
            val history = pro.getJSONObject("history")
            listOf("undo", "redo").forEach { key ->
                val entries = history.getJSONArray(key)
                for (index in 0 until entries.length()) target(entries.getJSONObject(index).getJSONObject("target"))
            }
        }
    }

    private fun verifyReachable(body: JSONObject) {
        strings(body.getJSONArray("records")).forEach { sha ->
            val file = files.record(sha)
            files.verify(file, file.length(), sha, NativeWorkFiles.MAX_PRO_BYTES)
        }
        strings(body.getJSONArray("derivatives")).forEach { sha ->
            val file = files.derivative(sha)
            files.verify(file, file.length(), sha, NativeWorkFiles.MAX_DERIVATIVE_BYTES)
        }
    }

    private fun collect(catalog: Catalog, cold: Boolean) {
        // An independently retryable export may still reference a linked source or finish.
        // Leave it and its assets alone even when Delete removed its editing root.
        val exportDirectory = File(context.filesDir, "pending-exports")
        if (listOf(ExportWorkspace.PHOTO, ExportWorkspace.PRO).any { mode ->
                listOf(".json", ".json.bak", ".json.new").any { suffix ->
                    File(exportDirectory, mode.name + suffix).exists()
                }
            }) return
        val namedPhoto = ProjectStore.namedManifests(context) ?: return
        val namedPro = ProStore.namedManifests(context) ?: return
        val staged = staged().values.flatten().toSet()
        val manifests = (namedPhoto + namedPro + listOfNotNull(catalog.active?.manifest) +
            catalog.drafts.map(Row::manifest) + staged.filter { it.startsWith("m:") }
                .map { NativeManifestRef(it.substring(2)) }).toSet()
        val codec = NativeWorkCodec(files)
        val records = staged.filter { it.startsWith("r:") }.mapTo(mutableSetOf()) { it.substring(2) }
        val derivatives = staged.filter { it.startsWith("d:") }.mapTo(mutableSetOf()) { it.substring(2) }
        manifests.forEach { ref ->
            val body = codec.open(ref)
            records += strings(body.getJSONArray("records")).map(NativeWorkCodec::checkedSha)
            derivatives += strings(body.getJSONArray("derivatives")).map(NativeWorkCodec::checkedSha)
        }
        files.collect(manifests.mapTo(mutableSetOf(), NativeManifestRef::sha), records, derivatives, cold)
    }

    private fun staged(): IdentityHashMap<Any, Set<String>> =
        stages.getOrPut(directoryKey) { IdentityHashMap() }

    private fun protect(token: Any, vararg assets: String) {
        staged()[token] = assets.toSet()
    }

    private fun nextRevision(revision: Long): Long =
        if (revision == Long.MAX_VALUE) throw NativeWorkLimit() else revision + 1

    private fun strings(array: JSONArray): List<String> = List(array.length()) { array.getString(it) }

    private suspend fun <T> locked(operation: suspend () -> T): NativeWorkResult<T> =
        withContext(Dispatchers.IO) { mutex.withLock { guarded(operation) } }

    private suspend fun <T> guarded(operation: suspend () -> T): NativeWorkResult<T> = try {
        NativeWorkResult.Ok(operation())
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: StaleWork) {
        NativeWorkResult.Failed(NativeWorkFailure.STALE)
    } catch (_: NativeWorkLimit) {
        NativeWorkResult.Failed(NativeWorkFailure.LIMIT)
    } catch (_: OutOfMemoryError) {
        NativeWorkResult.Failed(NativeWorkFailure.LIMIT)
    } catch (_: EditDocumentCodec.RecipeTooLarge) {
        NativeWorkResult.Failed(NativeWorkFailure.LIMIT)
    } catch (_: ProStore.ProjectTooLarge) {
        NativeWorkResult.Failed(NativeWorkFailure.LIMIT)
    } catch (_: NativeWorkUnavailable) {
        NativeWorkResult.Failed(NativeWorkFailure.UNAVAILABLE)
    } catch (_: JSONException) {
        NativeWorkResult.Failed(NativeWorkFailure.UNAVAILABLE)
    } catch (_: IllegalArgumentException) {
        NativeWorkResult.Failed(NativeWorkFailure.INVALID)
    } catch (_: Exception) {
        NativeWorkResult.Failed(NativeWorkFailure.STORAGE)
    }

    private class StaleWork : IOException()

    private class Budget(
        private val milliseconds: Long,
        private val managedBytes: Long,
        private val nativeBytes: Long? = null
    ) {
        private val time = SystemClock.elapsedRealtime()
        private val managed = managedUse()
        private val native = nativeBytes?.let { Debug.getNativeHeapAllocatedSize() }

        fun check() {
            if (SystemClock.elapsedRealtime() - time > milliseconds || managedUse() - managed > managedBytes)
                throw NativeWorkLimit()
            if (native != null && nativeBytes != null && Debug.getNativeHeapAllocatedSize() - native > nativeBytes)
                throw NativeWorkLimit()
        }
    }

    private class BoundedPngStream(output: FileOutputStream) : FilterOutputStream(output) {
        private var count = 0L
        private fun reserve(size: Int) {
            count += size
            if (count > NativeWorkFiles.MAX_DERIVATIVE_BYTES) throw NativeWorkLimit()
        }
        override fun write(value: Int) { reserve(1); out.write(value) }
        override fun write(buffer: ByteArray, offset: Int, size: Int) {
            reserve(size)
            out.write(buffer, offset, size)
        }
    }

    companion object {
        private val mutex = Mutex()
        private val coldFolders = mutableSetOf<String>()
        private val stages = mutableMapOf<String, IdentityHashMap<Any, Set<String>>>()

        private fun newId() = UUID.randomUUID().toString()
        private fun managedUse() = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }

        private fun samples(image: Bitmap): List<Int> = listOf(
            image.getPixel(0, 0), image.getPixel(image.width - 1, 0),
            image.getPixel(image.width / 2, image.height / 2),
            image.getPixel(0, image.height - 1), image.getPixel(image.width - 1, image.height - 1)
        )

        fun readLinkedRegular(context: Context, name: String, manifest: NativeManifestRef): SavedProject? =
            NativeWorkCodec(NativeWorkFiles(context)).linkedRegular(name, manifest)

        fun readLinkedPro(context: Context, manifest: NativeManifestRef): ProStore.SavedProject? =
            NativeWorkCodec(NativeWorkFiles(context)).linkedPro(manifest)
    }
}

package com.greenezone.gzphotoeditor

import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Process
import android.os.SystemClock
import android.provider.MediaStore
import android.view.KeyEvent
import android.util.Log
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.*
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The external runner inserts a force-stop between seed and reopen. */
@RunWith(AndroidJUnit4::class)
class AndroidU1ProcessRecoveryTest {
    @get:Rule val compose = createEmptyComposeRule()

    private val target: Context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val receipt: File get() = File(target.filesDir, "u1-process-test-receipt.json")

    @Test fun verifyNativeRecovery() {
        when (val phase = InstrumentationRegistry.getArguments().getString("u1ProcessPhase")) {
            null, "ordinary" -> runBlocking { verifyIsolatedStore() }
            "seed" -> runBlocking { seedForProcessLoss() }
            "reopen" -> reopenInSeparateProcess()
            else -> error("Unknown u1ProcessPhase: $phase")
        }
    }

    private suspend fun verifyIsolatedStore() {
        val root = Files.createTempDirectory(target.cacheDir.toPath(), "u1-process-ordinary-").toFile()
        val context = object : ContextWrapper(target) {
            override fun getFilesDir() = root
            override fun getDataDir() = root
        }
        try {
            val store = NativeWorkStore(context)
            val draft = NativeWorkState("standalone-regular", NativeWorkMode.REGULAR,
                regular("standalone-owner", Uri.parse("content://gz-u1-process/local"), 3L,
                    EditDocument(grade = Grade(exposure = .34f))), null, null)
            val initial = ok(store.recover())
            val prepared = ok(store.prepare(draft))
            val committed = ok(store.acknowledge(initial.revision, prepared))
            val ref = requireNotNull(committed.active).ref
            assertEquals(draft, ok(NativeWorkStore(context).load(ref)).state)
            val deleted = ok(store.delete(committed.revision, ref))
            assertNull(deleted.active)
            assertEquals(NativeWorkMode.REGULAR, deleted.route)
            assertTrue("standalone-owner" in deleted.retiredOwnerIds)
            assertEquals(NativeWorkResult.Failed(NativeWorkFailure.STALE),
                store.acknowledge(deleted.revision, prepared))
        } finally {
            root.deleteRecursively()
        }
    }

    private suspend fun seedForProcessLoss() {
        val store = NativeWorkStore(target)
        assertFalse("The explicit seed phase needs a fresh disposable app install", store.hasRecoveryRecord())
        assertFalse(receipt.exists())
        val first = createOriginal("base", 320, 240, Color.rgb(196, 51, 44))
        val second = createOriginal("overlay", 180, 120, Color.rgb(36, 151, 105))
        val original = makePro(first, second)
        val reset = requireNotNull(ProImageGeometry.reset(original))
        val retiredDoc = original.copy(layers = listOf(original.layers.first().copy(name = RETIRED_LAYER)))
        val retired = NativeWorkState(RETIRED_ROOT, NativeWorkMode.PRO, null,
            pro(RETIRED_OWNER, retiredDoc, requireNotNull(ProImageGeometry.reset(retiredDoc))), null)
        val initial = ok(store.recover())
        val retiredPrepared = ok(store.prepare(retired))
        val retiredCatalog = ok(store.acknowledge(initial.revision, retiredPrepared))
        val retiredRef = requireNotNull(retiredCatalog.active).ref

        // The old Pro export deliberately keeps a valid retry recipe. Its native recovery
        // owner is retired, so it cannot drive the editing route or load this layer.
        val journal = PendingExportJournal(target.filesDir)
        val settings = ExportSettings()
        val pending = journal.begin(ExportWorkspace.PRO, settings.format.mime, "u1-old-retry",
            PendingProExportRecipe(retiredDoc, requireNotNull(retired.pro?.reset),
                settings, ProPixelSize(320, 240)).encode(), "$RETIRED_OWNER/7")
        val retryable = requireNotNull(journal.failed(pending))
        val afterDelete = ok(store.delete(retiredCatalog.revision, retiredRef))
        assertTrue("$RETIRED_OWNER/7" in afterDelete.retiredOwnerIds)
        assertEquals(NativeWorkResult.Failed(NativeWorkFailure.STALE),
            store.acknowledge(afterDelete.revision, retiredPrepared))

        val firstBridge = ok(store.stageDerivative(original))
        val firstFinish = ok(store.stageFinish(original, firstBridge,
            EditDocument(quarterTurns = 1, crop = CropRatio.PORTRAIT, cropCenterX = .21f,
                grade = Grade(temperature = .15f))))
        val linkedPro = original.copy(linkedFinishes = listOf(firstFinish))
        val derivative = ok(store.stageDerivative(linkedPro))
        assertEquals(firstFinish.output, derivative.size)
        assertTrue("The first finish must alter the composition geometry", derivative.size != firstBridge.size)
        assertEquals(derivative.sha, bytesAt(derivative.uri))

        val undo = (1..35).map { index -> EditDocument(grade = Grade(exposure = index / 100f)) }
        val activeRecipe = EditDocument(crop = CropRatio.SQUARE, cropCenterY = .74f,
            grade = Grade(exposure = .44f, saturation = .19f))
        val clean = undo[9]
        val capturedRegular = regular(ACTIVE_REGULAR_OWNER, derivative.uri, 36L, activeRecipe).copy(
            sourceId = derivative.sha, sourceRevision = 4L, clean = clean, history = EditHistory(undo))
        val activePro = pro(ACTIVE_PRO_OWNER, linkedPro, reset).let { state ->
            val previous = state.saveTarget
            state.copy(history = ProHistory(undo = listOf(
                ProHistoryEntry(reset, reset, 0, previous),
                ProHistoryEntry(original, reset, 1, previous)
            )))
        }
        val base = NativeWorkState(ACTIVE_ROOT, NativeWorkMode.REGULAR, capturedRegular, activePro,
            NativeWorkLink(derivative = derivative, regularInput = derivative.size))
        val photoSave = ok(store.prepare(base.copy(regular = capturedRegular.copy(document = clean))))
        val photoRef = saved(store.saveNamed(ProjectSaveIntent.Create(PHOTO_SAVE), photoSave))
        val proSave = ok(store.prepare(base.copy(mode = NativeWorkMode.PRO)))
        val proRef = saved(store.saveNamed(ProjectSaveIntent.Create(PRO_SAVE), proSave))
        val active = ok(store.prepare(base.copy(
            regular = capturedRegular.copy(savedRef = photoRef),
            pro = activePro.copy(saveTarget = activePro.saveTarget.copy(name = proRef.name, savedRef = proRef),
                baselines = mapOf(7L to ProWorkBaseline(reset, linkedPro)))
        )))
        val parkedDoc = original.copy(layers = listOf(original.layers.last().copy(name = PARKED_LAYER)))
        val parked = ok(store.prepare(NativeWorkState(PARKED_ROOT, NativeWorkMode.PRO, null,
            pro(PARKED_OWNER, parkedDoc, requireNotNull(ProImageGeometry.reset(parkedDoc))), null)))
        val stored = ok(store.acknowledge(afterDelete.revision, active, displaced = parked))
        val activeRef = requireNotNull(stored.active).ref
        val draftRef = stored.drafts.single().ref
        assertEquals(active.state, ok(store.load(activeRef)).state)
        val output = JSONObject().put("seedPid", Process.myPid()).put("catalog", stored.revision)
            .put("active", refJson(activeRef)).put("draft", refJson(draftRef)).put("retired", refJson(retiredRef))
            .put("state", fingerprint(active.state)).put("draftState", fingerprint(parked.state))
            .put("photoNamed", namedJson(photoRef, photoSave.manifest))
            .put("proNamed", namedJson(proRef, proSave.manifest))
            .put("firstUri", first.toString()).put("firstSha", bytesAt(first))
            .put("secondUri", second.toString()).put("secondSha", bytesAt(second))
            .put("retry", retryable.id)
        FileOutputStream(receipt).use { file ->
            file.write(output.toString().toByteArray(Charsets.UTF_8))
            file.fd.sync()
        }
        Log.i(TAG, "seed pid=${Process.myPid()} rootRevision=${activeRef.revision} " +
            "draftRevision=${draftRef.revision} undo=${undo.size} retainedProLayers=${linkedPro.layers.size}")
    }

    private fun reopenInSeparateProcess() {
        assertTrue("Run u1ProcessPhase=seed before the external force-stop", receipt.isFile)
        val expected = JSONObject(receipt.readText(Charsets.UTF_8))
        val seedPid = expected.getInt("seedPid")
        val pid = Process.myPid()
        assertNotEquals("Process recovery requires a different PID", seedPid, pid)

        val before = managedHeap()
        val sampling = AtomicBoolean(true)
        var peak = before
        val sampler = thread(name = "u1-process-heap") {
            while (sampling.get()) {
                peak = maxOf(peak, managedHeap())
                SystemClock.sleep(12)
            }
        }
        val start = SystemClock.elapsedRealtime()
        var elapsed = 0L
        val scenario = try {
            ActivityScenario.launch(MainActivity::class.java).also { opened ->
                try {
                    waitContent("Photo editing canvas. Tap to hide or show tools.")
                    elapsed = SystemClock.elapsedRealtime() - start
                } catch (failure: Throwable) {
                    opened.close()
                    throw failure
                }
            }
        } finally {
            sampling.set(false)
            sampler.join()
        }
        scenario.use { activity ->
            activity.onActivity { assertEquals(pid, Process.myPid()) }
            val peakIncrease = maxOf(peak, managedHeap()) - before
            Log.i(TAG, "reopen seedPid=$seedPid pid=$pid route=REGULAR coldHostMs=$elapsed " +
                "managedIncrease=$peakIncrease")
            assertTrue("Actual selected host recovery exceeded ten seconds: $elapsed ms", elapsed <= 10_000)
            assertTrue("Actual selected host recovery exceeded 128 MiB: $peakIncrease bytes",
                peakIncrease <= 128L * 1024L * 1024L)
            compose.onNodeWithText("Photo ▾", substring = false).assertIsDisplayed()
            compose.onNodeWithContentDescription("Undo", substring = false).assertIsEnabled()
            compose.onAllNodesWithText("Bring current work", substring = false).assertCountEquals(0)
            compose.onAllNodesWithText(RETIRED_LAYER, substring = false).assertCountEquals(0)

            val store = NativeWorkStore(target)
            val restored = runBlocking { verifyExactRecovery(store, expected) }

            // One completed native edit is driven from the recovered UI. The two retained
            // source layers and their visibility controls must be individually available.
            compose.onNodeWithContentDescription("Choose editing workspace", substring = false)
                .performTouchInput { click() }
            tapText("Photo editing Pro")
            tapText("Bring current work")
            waitText("Photo Pro ▾")
            dismissRetainedExportDialog()
            tapText("Stack (3)")
            compose.onNodeWithText(BASE_LAYER, substring = false).assertIsDisplayed()
            compose.onNodeWithText(OVERLAY_LAYER, substring = false).assertIsDisplayed()
            compose.onNodeWithText(PAINT_LAYER, substring = false).assertIsDisplayed()
            val hide = "Hide $OVERLAY_LAYER"
            waitContent(hide)
            compose.onNodeWithContentDescription(hide, substring = false).performTouchInput { click() }
            waitContent("Show $OVERLAY_LAYER")

            val updated = awaitHiddenOverlay(store)
            assertEquals(restored.id, updated.id)
            val originalPro = requireNotNull(restored.pro)
            val changedPro = requireNotNull(updated.pro)
            assertEquals(originalPro.document.layers.map { it.nativePlacementId },
                changedPro.document.layers.map { it.nativePlacementId })
            assertTrue(changedPro.document.layers.first { it.name == BASE_LAYER }.visible)
            assertFalse(changedPro.document.layers.first { it.name == OVERLAY_LAYER }.visible)
            assertEquals(2, changedPro.document.linkedFinishes.size)
            assertEquals(35, requireNotNull(updated.regular).history.undo.size)
            assertEquals(refFrom(expected.getJSONObject("draft")),
                runBlocking { ok(store.recover()).drafts.single().ref })
            assertEquals(expected.getString("firstSha"), bytesAt(Uri.parse(expected.getString("firstUri"))))
            assertEquals(expected.getString("secondSha"), bytesAt(Uri.parse(expected.getString("secondUri"))))
            assertEquals(expected.getString("retry"),
                PendingExportJournal(target.filesDir).read(ExportWorkspace.PRO)?.id)
            Log.i(TAG, "proof pid=$pid nativeLayerEdit=true linkedFinishes=2 undo=35 parked=1 named=2 retired=true")

            tapText("Done")
            verifyNamedUiOpens(store, expected, restored, pid)
        }
    }

    private fun verifyNamedUiOpens(
        store: NativeWorkStore, expected: JSONObject, original: NativeWorkState, pid: Int
    ) {
        val photoRef = namedFrom(expected.getJSONObject("photoNamed"), ProjectKind.PHOTO)
        val proRef = namedFrom(expected.getJSONObject("proNamed"), ProjectKind.PRO)
        val savedPhoto = requireNotNull(original.regular)
        val savedPro = requireNotNull(original.pro).document

        // Open the actual Regular named row from a blank workspace in the new process.
        chooseWorkspace("Photo editing")
        tapText("Discard")
        waitText("Photo ▾")
        waitText("Choose Options → New photo… to begin.")
        tapText("Options")
        tapText("Open project…")
        tapText(PHOTO_SAVE)
        tapText("Open project")
        waitContent("Photo editing canvas. Tap to hide or show tools.")
        compose.onNodeWithContentDescription("Undo", substring = false).assertIsEnabled()
        val openedPhoto = awaitActiveWork(store, "The Regular named Open did not publish its linked root") {
            it.mode == NativeWorkMode.REGULAR && it.regular?.savedRef == photoRef && it.id != original.id
        }
        assertEquals(savedPhoto.clean, openedPhoto.regular?.document)
        assertEquals(savedPhoto.clean, openedPhoto.regular?.clean)
        assertEquals(35, openedPhoto.regular?.history?.undo?.size)
        assertEquals(savedPro, openedPhoto.pro?.document)

        chooseWorkspace("Photo editing Pro")
        tapText("Bring current work")
        waitText("Photo Pro ▾")
        dismissRetainedExportDialog()
        val photoPro = awaitActiveWork(store, "The Regular named Open did not return to editable Pro layers") {
            it.mode == NativeWorkMode.PRO && it.id == openedPhoto.id
        }
        assertEquals(savedPro.layers, photoPro.pro?.document?.layers)
        assertEquals(2, photoPro.pro?.document?.linkedFinishes?.size)
        hideNamedNativeOverlay(store, photoPro)

        // The Pro chooser opens the independently saved version, with the hidden layer visible again.
        tapText("Options")
        tapText("Open project…")
        waitText("Saved Pro projects")
        tapText(PRO_SAVE)
        tapText("Discard")
        val openedPro = awaitActiveWork(store, "The Pro named Open did not publish its linked root") {
            it.mode == NativeWorkMode.PRO && it.pro?.saveTarget?.savedRef == proRef &&
                it.id != original.id && it.id != openedPhoto.id
        }
        assertEquals(savedPro, openedPro.pro?.document)
        assertEquals(savedPhoto.document, openedPro.regular?.document)
        assertEquals(savedPhoto.document, openedPro.regular?.clean)
        hideNamedNativeOverlay(store, openedPro)

        assertEquals(photoRef, ProjectStore.loadNamed(target, photoRef)?.project)
        assertEquals(proRef, ProStore.loadNamed(target, PRO_SAVE)?.project)
        val parked = refFrom(expected.getJSONObject("draft"))
        assertTrue(runBlocking { ok(store.recover()).drafts.any { it.ref == parked } })
        assertEquals(expected.getString("retry"),
            PendingExportJournal(target.filesDir).read(ExportWorkspace.PRO)?.id)
        assertEquals(pid, Process.myPid())
        Log.i(TAG, "named-ui pid=$pid regularOpen=true proOpen=true nativeEdits=2 namedVersionsRetained=true")
    }

    private fun hideNamedNativeOverlay(store: NativeWorkStore, opened: NativeWorkState) {
        tapText("Stack (3)")
        compose.onNodeWithText(BASE_LAYER, substring = false).assertIsDisplayed()
        compose.onNodeWithText(OVERLAY_LAYER, substring = false).assertIsDisplayed()
        compose.onNodeWithText(PAINT_LAYER, substring = false).assertIsDisplayed()
        val hide = "Hide $OVERLAY_LAYER"
        waitContent(hide)
        compose.onNodeWithContentDescription(hide, substring = false).performTouchInput { click() }
        waitContent("Show $OVERLAY_LAYER")
        tapText(BASE_LAYER)
        waitText("$BASE_LAYER · Image")
        val before = requireNotNull(opened.pro).document
        val selected = before.layers.indexOfFirst { it.name == BASE_LAYER }
        val changed = awaitActiveWork(store, "The layer edit after named Open was not acknowledged for ${opened.id}") {
            it.mode == NativeWorkMode.PRO && it.id == opened.id && it.pro?.selectedLayer == selected &&
                it.pro?.document?.layers?.any { layer -> layer.name == OVERLAY_LAYER && !layer.visible } == true
        }
        val after = requireNotNull(changed.pro).document
        val expected = before.layers.map { layer ->
            if (layer.name == OVERLAY_LAYER) layer.copy(visible = false) else layer
        }
        assertEquals(expected, after.layers)
        assertEquals(before.linkedFinishes, after.linkedFinishes)
    }

    private fun chooseWorkspace(label: String) {
        compose.onNodeWithContentDescription("Choose editing workspace", substring = false)
            .performTouchInput { click() }
        tapText(label)
    }

    private fun dismissRetainedExportDialog() {
        waitText("Resume export")
        // Back closes the independent retry surface while leaving its record intact.
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Resume export", substring = false).fetchSemanticsNodes().isEmpty()
        }
    }

    private suspend fun verifyExactRecovery(store: NativeWorkStore, expected: JSONObject): NativeWorkState {
        val catalog = ok(store.recover())
        val selected = requireNotNull(catalog.active)
        val draft = catalog.drafts.single()
        assertEquals(expected.getLong("catalog"), catalog.revision)
        assertEquals(NativeWorkMode.REGULAR, catalog.route)
        assertEquals(refFrom(expected.getJSONObject("active")), selected.ref)
        assertEquals(refFrom(expected.getJSONObject("draft")), draft.ref)
        assertEquals(setOf(NativeWorkMode.REGULAR, NativeWorkMode.PRO), selected.recoverableModes)
        assertTrue(RETIRED_ROOT in catalog.retiredOwnerIds && "$RETIRED_OWNER/7" in catalog.retiredOwnerIds)
        assertEquals(NativeWorkResult.Failed(NativeWorkFailure.STALE),
            store.load(refFrom(expected.getJSONObject("retired"))))
        val current = ok(store.load(selected.ref)).state
        assertEquals(expected.getString("state"), fingerprint(current))
        assertEquals(expected.getString("draftState"), fingerprint(ok(store.load(draft.ref)).state))
        val derivative = requireNotNull(current.link?.derivative)
        assertEquals(derivative.sha, bytesAt(derivative.uri))
        assertEquals(35, requireNotNull(current.regular).history.undo.size)
        assertNotEquals(current.regular?.clean, current.regular?.document)

        val photo = expected.getJSONObject("photoNamed")
        val photoRef = namedFrom(photo, ProjectKind.PHOTO)
        val photoRecord = requireNotNull(ProjectStore.loadNamed(target, photoRef))
        val photoManifest = requireNotNull(photoRecord.content.linkedWork)
        assertEquals(photo.getString("manifest"), photoManifest.sha)
        val photoState = ok(store.loadNamed(photoManifest, photoRef))
        assertNotEquals(current.id, photoState.id)
        assertEquals(photoRef, photoState.regular?.savedRef)
        assertEquals(current.regular?.clean, photoState.regular?.document)
        val photoCompanion = requireNotNull(photoState.pro)
        assertEquals(current.pro?.document, photoCompanion.document)
        assertEquals(photoCompanion.document, photoCompanion.baselines[photoCompanion.saveTarget.workId]?.cleanDocument)
        assertNull(photoCompanion.saveTarget.savedRef)

        val pro = expected.getJSONObject("proNamed")
        val proRef = namedFrom(pro, ProjectKind.PRO)
        val proRecord = requireNotNull(ProStore.loadNamed(target, PRO_SAVE))
        assertEquals(proRef, proRecord.project)
        val proManifest = requireNotNull(proRecord.content.linkedWork)
        assertEquals(pro.getString("manifest"), proManifest.sha)
        val proState = ok(store.loadNamed(proManifest, proRef))
        assertNotEquals(current.id, proState.id)
        assertEquals(proRef, proState.pro?.saveTarget?.savedRef)
        assertEquals(current.pro?.document, proState.pro?.document)
        val proCompanion = requireNotNull(proState.regular)
        assertEquals(current.regular?.document, proCompanion.document)
        assertEquals(proCompanion.document, proCompanion.clean)
        assertNull(proCompanion.savedRef)
        val retry = requireNotNull(PendingExportJournal(target.filesDir).read(ExportWorkspace.PRO))
        assertEquals(expected.getString("retry"), retry.id)
        assertEquals(PendingExportPhase.RETRY, retry.phase)
        return current
    }

    private fun awaitHiddenOverlay(store: NativeWorkStore): NativeWorkState =
        awaitActiveWork(store, "The recovered Pro layer edit did not reach the acknowledged native root") {
            it.mode == NativeWorkMode.PRO &&
                it.pro?.document?.layers?.any { layer -> layer.name == OVERLAY_LAYER && !layer.visible } == true
        }

    private fun awaitActiveWork(
        store: NativeWorkStore, missing: String, matches: (NativeWorkState) -> Boolean
    ): NativeWorkState {
        var matched: NativeWorkState? = null
        var observed: NativeWorkState? = null
        try {
            // Discard only readies a Compose effect. Keep its clock moving while
            // waiting for the exact durable root from the action.
            compose.waitUntil(12_000L) {
                val current = runBlocking {
                    val catalog = ok(store.recover())
                    catalog.active?.let { selected ->
                        when (val loaded = store.load(selected.ref)) {
                            is NativeWorkResult.Ok -> loaded.value.state
                            is NativeWorkResult.Failed -> {
                                // The host may acknowledge the layer edit after this poll read the catalog.
                                // Retry that exact revision race using the next authoritative catalog.
                                if (loaded.reason != NativeWorkFailure.STALE) error("Native custody failed: ${loaded.reason}")
                                null
                            }
                        }
                    }
                }
                observed = current
                if (current != null && matches(current)) matched = current
                matched != null
            }
        } finally {
            if (matched == null) {
                val last = observed
                val pro = last?.pro
                val layer = pro?.document?.layers?.firstOrNull { it.name == OVERLAY_LAYER }
                Log.i(TAG, "pending: $missing, mode=${last?.mode}, root=${last?.id}, " +
                    "named=${pro?.saveTarget?.savedRef?.name}, revision=${pro?.contentRevision}, " +
                    "overlayVisible=${layer?.visible}")
            }
        }
        return requireNotNull(matched)
    }

    private fun waitText(label: String) {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText(label, substring = false).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitContent(label: String) {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithContentDescription(label, substring = false).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun tapText(label: String) {
        waitText(label)
        compose.onNodeWithText(label, substring = false).performTouchInput { click() }
    }

    private fun regular(owner: String, uri: Uri, revision: Long, document: EditDocument) = NativeRegularState(
        owner, owner, 0, revision, uri, document, EditDocument(), EditDocument(), EditDocument(),
        EditHistory(), null
    )

    private fun pro(owner: String, document: ProDocument, reset: ProDocument) = NativeProState(
        owner, document, true, 8L, 2L, document.layers.lastIndex.coerceAtMost(1),
        ProSaveTarget(7L, nativeOwnerId = "$owner/7"), 8L, reset, reset,
        mapOf(7L to ProWorkBaseline(reset)), ProHistory()
    )

    private fun makePro(first: Uri, second: Uri): ProDocument {
        val firstSize = ProPixelSize(320, 240)
        val base = ProImageGeometry.recordImport(ProLayer(first.toString(), name = BASE_LAYER, scale = .9f),
            firstSize, placementId = "process-base-placement")
        val overlay = ProImageGeometry.recordImport(ProLayer(second.toString(), name = OVERLAY_LAYER,
            scale = .61f, x = .61f, y = .54f, opacity = .8f),
            ProPixelSize(180, 120), placementId = "process-overlay-placement")
        val paint = ProLayer("", name = PAINT_LAYER, kind = ProLayerKind.DRAWING,
            edits = EditDocument(creative = CreativeDocument(strokes = listOf(
                InkStroke(listOf(BrushPoint(.22f, .35f), BrushPoint(.69f, .62f)), color = Color.YELLOW)
            ))))
        val document = ProDocument(listOf(base, overlay, paint), background = ProBackground.TRANSPARENT,
            canvasSize = firstSize)
        return document.copy(sourceCanvasSetup = ProCanvasSetup.from(document))
    }

    private fun createOriginal(name: String, width: Int, height: Int, argb: Int): Uri {
        val resolver = target.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "gz-u1-$name.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/GzU1Process")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { color = argb }
            canvas.drawRect(9f, 7f, width - 12f, height - 10f, paint)
            paint.color = Color.argb(132, 81, 96, 206)
            canvas.drawRect(0f, height / 3f, width / 3f, height - 1f, paint)
            requireNotNull(resolver.openOutputStream(uri, "w")).use {
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
            assertEquals(1, resolver.update(uri, ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }, null, null))
            return uri
        } catch (failure: Throwable) {
            resolver.delete(uri, null, null)
            throw failure
        } finally {
            bitmap.recycle()
        }
    }

    private fun bytesAt(uri: Uri) =
        requireNotNull(target.contentResolver.openInputStream(uri)).use { sha(it.readBytes()) }

    private fun fingerprint(state: NativeWorkState): String {
        fun edit(doc: EditDocument) = sha(EditDocumentCodec.encodeBounded(doc))
        fun pro(doc: ProDocument) =
            if (doc.layers.isEmpty()) "empty:${doc.canvasAspectRatio}" else sha(ProStore.encode(doc))
        val root = JSONObject().put("id", state.id).put("mode", state.mode.name)
        state.regular?.let {
            root.put("regular", JSONObject().put("owner", it.ownerId).put("source", it.sourceId)
                .put("sourceRevision", it.sourceRevision).put("contentRevision", it.contentRevision)
                .put("uri", it.uri.toString()).put("document", edit(it.document)).put("entry", edit(it.entry))
                .put("clean", edit(it.clean)).put("reset", edit(it.reset))
                .put("undo", JSONArray(it.history.undo.map(::edit))).put("redo", JSONArray(it.history.redo.map(::edit)))
                .put("saved", it.savedRef?.version.orEmpty()))
        }
        state.pro?.let {
            root.put("pro", JSONObject().put("owner", it.ownerId).put("contentRevision", it.contentRevision)
                .put("sourceRevision", it.sourceRevision).put("document", pro(it.document))
                .put("reset", it.reset?.let(::pro).orEmpty()).put("entry", it.entry?.let(::pro).orEmpty())
                .put("selected", it.selectedLayer).put("target", it.saveTarget.savedRef?.version.orEmpty())
                .put("undo", JSONArray(it.history.undo.map { entry -> pro(entry.document) }))
                .put("redo", JSONArray(it.history.redo.map { entry -> pro(entry.document) })))
        }
        state.link?.let {
            root.put("link", JSONObject().put("regularParent", it.regularOrigin?.sha.orEmpty())
                .put("proParent", it.proOrigin?.sha.orEmpty()).put("derivative", it.derivative?.sha.orEmpty())
                .put("width", it.derivative?.size?.width ?: 0).put("height", it.derivative?.size?.height ?: 0))
        }
        return sha(root.toString())
    }

    private fun refJson(ref: NativeWorkRef) = JSONObject().put("id", ref.id).put("revision", ref.revision)
    private fun refFrom(json: JSONObject) = NativeWorkRef(json.getString("id"), json.getLong("revision"))
    private fun namedJson(ref: NamedProjectRef, manifest: NativeManifestRef) = JSONObject()
        .put("id", ref.id).put("name", ref.name).put("version", ref.version).put("manifest", manifest.sha)
    private fun namedFrom(json: JSONObject, kind: ProjectKind) =
        NamedProjectRef(kind, json.getString("id"), json.getString("name"), json.getString("version"))

    private fun sha(bytes: ByteArray) = NamedProjectIdentity.version(bytes)
    private fun sha(text: String) = sha(text.toByteArray(Charsets.UTF_8))
    private fun managedHeap() = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }

    private fun <T> ok(result: NativeWorkResult<T>): T = when (result) {
        is NativeWorkResult.Ok -> result.value
        is NativeWorkResult.Failed -> error("Native custody failed: ${result.reason}")
    }

    private fun saved(result: ProjectSaveResult): NamedProjectRef = when (result) {
        is ProjectSaveResult.Saved -> result.project
        else -> error("Named custody failed: $result")
    }

    private companion object {
        const val TAG = "GzU1Process"
        const val PHOTO_SAVE = "U1 Process Regular"
        const val PRO_SAVE = "U1 Process Pro"
        const val ACTIVE_ROOT = "process-linked-active"
        const val ACTIVE_REGULAR_OWNER = "process-regular-owner"
        const val ACTIVE_PRO_OWNER = "process-pro-owner"
        const val PARKED_ROOT = "process-parked"
        const val PARKED_OWNER = "process-parked-owner"
        const val RETIRED_ROOT = "process-retired"
        const val RETIRED_OWNER = "process-retired-owner"
        const val BASE_LAYER = "Source red"
        const val OVERLAY_LAYER = "Source green"
        const val PAINT_LAYER = "Native brush"
        const val PARKED_LAYER = "Parked paint"
        const val RETIRED_LAYER = "Must never restore"
    }
}

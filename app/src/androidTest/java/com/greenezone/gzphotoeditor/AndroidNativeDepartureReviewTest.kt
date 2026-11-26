package com.greenezone.gzphotoeditor

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.*
import com.greenezone.gzphotoeditor.ui.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/** Regressions for the bounded post-integration native departure review. */
@RunWith(AndroidJUnit4::class)
class AndroidNativeDepartureReviewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val originalPro = ProDocument(listOf(
        ProLayer("", name = "Review adjustment", kind = ProLayerKind.ADJUSTMENT)))
    private val editedPro get() = originalPro.copy(background = ProBackground.TRANSPARENT)
    @Volatile private var currentPro = ProDocument()
    @Volatile private var retainedPro = ProWorkspaceSession()
    @Volatile private var departures = 0

    private fun gif(delay: Int = 100) = GifDocument(
        frames = listOf("content://gz-review/first", "content://gz-review/second"), delayMs = delay)

    private fun saved(result: ProjectSaveResult): NamedProjectRef {
        assertTrue(result.toString(), result is ProjectSaveResult.Saved)
        return (result as ProjectSaveResult.Saved).project
    }

    private fun isolated(block: (Context, AtomicBoolean) -> Unit) {
        val folder = Files.createTempDirectory(rule.activity.cacheDir.toPath(), "native-review-").toFile()
        val cache = File(folder, "cache").apply { mkdirs() }
        val unavailable = AtomicBoolean(false)
        val context = object : ContextWrapper(rule.activity) {
            override fun getFilesDir(): File {
                if (unavailable.get()) throw IOException("Injected temporary project storage failure")
                return folder
            }
            override fun getCacheDir(): File = cache
        }
        try { block(context, unavailable) }
        finally {
            unavailable.set(false)
            rule.runOnIdle { rule.activity.setContentView(android.widget.FrameLayout(rule.activity)) }
            folder.deleteRecursively()
        }
    }

    private fun mountPro(context: Context) {
        departures = 0
        rule.setContent {
            var document by remember { mutableStateOf(editedPro) }
            var session by remember { mutableStateOf(ProWorkspaceSession(
                resetDocument = originalPro, entryDocument = originalPro,
                baselines = mapOf(0L to ProWorkBaseline(originalPro)))) }
            currentPro = document
            retainedPro = session
            CompositionLocalProvider(LocalContext provides context) {
                MaterialTheme {
                    ProWorkspaceScreenContent(document, { document = it },
                        { departures++ }, { departures++ }, session, { session = it })
                }
            }
        }
        // Establish that the initial asynchronous journal read has finished.
        // The tests then create a record which is absent from its cached UI result.
        rule.onNodeWithText("Options").performClick()
        rule.waitUntil(15_000) {
            rule.onAllNodes(hasText("Export image…") and isEnabled()).fetchSemanticsNodes().isNotEmpty()
        }
        rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
    }

    private fun pendingPro(context: Context, owner: String): PendingExport =
        PendingExportJournal(context.filesDir).begin(ExportWorkspace.PRO, "image/png",
            "review.png", "independent captured export recipe", owner)

    private fun deleteCurrentPro() {
        rule.onNodeWithText("Options").performClick()
        rule.onNodeWithText("Delete", substring = false).performClick()
        rule.onNodeWithText("Delete", substring = false).performClick()
    }

    private fun waitForProDelete() {
        // The document callback can precede the SideEffect that publishes session
        // ownership. Observe the completed UI operation before reading retainedPro.
        rule.waitUntil(15_000) {
            currentPro == ProDocument() &&
                rule.onAllNodesWithText("Delete current work?").fetchSemanticsNodes().isEmpty() &&
                rule.onAllNodes(hasContentDescription("Choose editing workspace") and isEnabled())
                    .fetchSemanticsNodes().isNotEmpty()
        }
        rule.waitForIdle()
    }

    @Test fun deleteCurrentProRevokesFreshOwnedRecoveryAndKeepsItsExportRecipe() = isolated { context, _ ->
        mountPro(context)
        val owner = retainedPro.nativeOwnerKey()
        val pending = pendingPro(context, owner)
        deleteCurrentPro()
        waitForProDelete()
        val retained = requireNotNull(PendingExportJournal(context.filesDir).read(ExportWorkspace.PRO))
        assertEquals(pending.id, retained.id)
        assertEquals(pending.recipe, retained.recipe)
        assertFalse(retained.restoreNativeDraft)
        assertNotEquals(owner, retainedPro.nativeOwnerKey())
        assertTrue(retainedPro.history.undo.isEmpty())
        assertEquals(0, departures)
    }

    @Test fun ordinaryProDiscardReadsFreshRecoveryBeforeItNavigates() = isolated { context, _ ->
        mountPro(context)
        val pending = pendingPro(context, retainedPro.nativeOwnerKey())
        rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.onNodeWithText("Discard").performClick()
        rule.waitUntil(15_000) { departures == 1 }
        // Navigation follows the document callback, but currentPro is mirrored by
        // the parent's next composition. Observe that committed composition.
        rule.runOnIdle {
            assertEquals(1, departures)
            assertEquals(originalPro, currentPro)
        }
        val retained = requireNotNull(PendingExportJournal(context.filesDir).read(ExportWorkspace.PRO))
        assertEquals(pending.recipe, retained.recipe)
        assertFalse(retained.restoreNativeDraft)
    }

    @Test fun deletingAnotherProWorkOwnerDoesNotRevokeRetainedExport() = isolated { context, _ ->
        mountPro(context)
        val pending = pendingPro(context, "different-native-work-owner")
        deleteCurrentPro()
        waitForProDelete()
        val retained = requireNotNull(PendingExportJournal(context.filesDir).read(ExportWorkspace.PRO))
        assertEquals(pending, retained)
        assertTrue(retained.restoreNativeDraft)
    }

    @Test fun failedProRecoveryDispositionKeepsWorkUntilExplicitRetry() = isolated { context, _ ->
        mountPro(context)
        val pending = pendingPro(context, retainedPro.nativeOwnerKey())
        val blocked = File(context.filesDir, "pending-exports/native-resolutions.json")
        assertTrue(blocked.mkdirs())
        File(blocked, "obstruction").writeText("test-only")
        deleteCurrentPro()
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText("Could not record Delete. Your Pro work remains open.")
                .fetchSemanticsNodes().isNotEmpty()
        }
        assertEquals(editedPro, currentPro)
        assertEquals(0, departures)
        assertTrue(blocked.deleteRecursively())
        assertTrue(requireNotNull(PendingExportJournal(context.filesDir).read(ExportWorkspace.PRO)).restoreNativeDraft)
        rule.onNodeWithText("Delete", substring = false).performClick()
        waitForProDelete()
        val retained = requireNotNull(PendingExportJournal(context.filesDir).read(ExportWorkspace.PRO))
        assertEquals(pending.id, retained.id)
        assertFalse(retained.restoreNativeDraft)
    }

    @Test fun gifCollisionCancelInvalidatesDepartureAndLateOrUnrelatedSavesCannotReviveIt() = isolated { context, _ ->
        val original = gif()
        val edited = gif(200)
        saved(GifStore.saveNamed(context, ProjectSaveIntent.Create("Taken"), original))
        val custody = GifDocumentCustody(original, GifWorkspaceSession())
        custody.record(edited)
        val gate = GifDepartureGate(custody)
        var continued = 0
        gate.request { continued++ }
        val canceledToken = gate.saveToken()
        gate.beginSave()
        rule.setContent {
            var capture by remember { mutableStateOf<GifSaveCapture?>(
                GifSaveCapture(canceledToken, edited, null, false)) }
            CompositionLocalProvider(LocalContext provides context) {
                MaterialTheme {
                    if (capture != null) {
                        val request = requireNotNull(capture)
                        GifProjectSaveDialog(request,
                            onSaved = { project ->
                                custody.saved(request.token, project, request.document)
                                capture = null
                                gate.saved(request.token)
                            },
                            onDismiss = { capture = null; gate.cancel() })
                    } else TextButton(onClick = {
                        capture = GifSaveCapture(custody.token(), edited, null, false)
                    }) { Text("Save again") }
                }
            }
        }
        rule.onNode(hasSetTextAction()).performTextReplacement("Taken")
        rule.onNodeWithText("Save", substring = false).performClick()
        rule.waitUntil(15_000) { rule.onAllNodesWithText("Replace GIF project?").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Cancel", substring = false).performClick()
        rule.onNodeWithText("Save again").assertIsDisplayed()
        rule.runOnIdle {
            assertFalse(gate.pending)
            gate.saved(canceledToken) // A late acknowledgement cannot resume the canceled request.
            assertEquals(0, continued)
        }
        rule.onNodeWithText("Save again").performClick()
        rule.onNode(hasSetTextAction()).performTextReplacement("Independent")
        rule.onNodeWithText("Save", substring = false).performClick()
        rule.waitUntil(15_000) { GifStore.loadNamed(context, "Independent") != null }
        rule.waitForIdle()
        assertEquals(0, continued)
        assertEquals(original, GifStore.loadNamed(context, "Taken")!!.content)
        assertEquals(edited, GifStore.loadNamed(context, "Independent")!!.content)
    }

    @Test fun failedGifReplaceRetriesCapturedIdentityInsteadOfResolvingNameAgain() = isolated { context, unavailable ->
        val original = saved(GifStore.saveNamed(context, ProjectSaveIntent.Create("Target"), gif()))
        val capture = GifSaveCapture(WorkspaceOperationToken("review-owner", 1), gif(200), null, false)
        var saveCallbacks = 0
        rule.setContent {
            CompositionLocalProvider(LocalContext provides context) {
                MaterialTheme { GifProjectSaveDialog(capture, { saveCallbacks++ }, {}) }
            }
        }
        rule.onNode(hasSetTextAction()).performTextReplacement("Target")
        rule.onNodeWithText("Save", substring = false).performClick()
        rule.waitUntil(15_000) { rule.onAllNodesWithText("Replace GIF project?").fetchSemanticsNodes().isNotEmpty() }
        unavailable.set(true)
        rule.onNodeWithText("Replace", substring = false).performClick()
        rule.waitUntil(15_000) { rule.onAllNodesWithText("GIF project not saved").fetchSemanticsNodes().isNotEmpty() }
        unavailable.set(false)
        assertEquals(original, GifStore.loadNamed(context, "Target")!!.project)
        // A different successful write between failure and Retry must not be adopted
        // as the old replacement's new target.
        val intervening = saved(GifStore.saveNamed(context, ProjectSaveIntent.Update(original), gif(300)))
        rule.onNodeWithText("Retry", substring = false).performClick()
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText("This saved project changed.", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        assertEquals(0, saveCallbacks)
        assertEquals(intervening, GifStore.loadNamed(context, "Target")!!.project)
        assertEquals(gif(300), GifStore.loadNamed(context, "Target")!!.content)
    }

    @Test fun recoveredGifDisplayNameCannotAdoptARecreatedSavedProject() = isolated { context, _ ->
        val old = saved(GifStore.saveNamed(context, ProjectSaveIntent.Create("Reused name"), gif()))
        val recovered = gif(200)
        val journal = PendingExportJournal(context.filesDir)
        val pending = journal.begin(ExportWorkspace.GIF, "image/gif", "old.gif",
            PendingGifRecipe(recovered, old.name).encode(), nativeOwnerId = "original-native-owner")
        assertTrue(GifStore.deleteNamed(context, old))
        val replacement = saved(GifStore.saveNamed(context, ProjectSaveIntent.Create(old.name), gif(300)))
        assertNotEquals(old.id, replacement.id)
        var current = GifDocument()
        var retained = GifWorkspaceSession()
        rule.setContent {
            var document by remember { mutableStateOf(GifDocument()) }
            var session by remember { mutableStateOf(GifWorkspaceSession()) }
            CompositionLocalProvider(LocalContext provides context) {
                MaterialTheme {
                    GifMakerScreenContent(document, { document = it; current = it }, {}, {},
                        session, { session = it; retained = it })
                }
            }
        }
        rule.waitUntil(15_000) { current == recovered && retained.identity != null }
        rule.runOnIdle {
            assertNull(retained.identity!!.project)
            assertNull(retained.identity!!.savedDocument)
            assertEquals(GifDocument(), retained.identity!!.entryDocument)
            assertEquals(pending.nativeOwnerId, retained.identity!!.ownerId)
        }
        assertEquals(replacement, GifStore.loadNamed(context, old.name)!!.project)
        assertEquals(gif(300), GifStore.loadNamed(context, old.name)!!.content)
        assertEquals(pending.recipe, journal.read(ExportWorkspace.GIF)!!.recipe)
    }

    @Test fun gifSaveRefreshesAliasesAcrossUndoWithoutChangingIndependentTargetsOrEntries() = isolated { context, _ ->
        val entry = gif()
        val first = saved(GifStore.saveNamed(context, ProjectSaveIntent.Create("Shared project"), entry))
        val independentRef = saved(GifStore.saveNamed(context, ProjectSaveIntent.Create("Independent"), gif(400)))
        val firstOwner = GifDocumentIdentity(entryDocument = entry, project = first, savedDocument = entry)
        val alias = GifDocumentIdentity(entryDocument = gif(150), project = first, savedDocument = entry)
        val independent = GifDocumentIdentity(entryDocument = gif(400), project = independentRef, savedDocument = gif(400))
        val custody = GifDocumentCustody(entry, GifWorkspaceSession(identity = alias,
            identities = listOf(firstOwner, alias, independent).associateBy { it.ownerId }))
        val edited = gif(200)
        custody.record(edited)
        val token = custody.token()
        val updated = saved(GifStore.saveNamed(context, ProjectSaveIntent.Update(first), edited))
        assertTrue(custody.saved(token, updated, edited))
        custody.restore(GifProjectSnapshot(entry, first.name, GifWorkspaceRoute.PHOTOS, firstOwner))
        assertEquals(updated, custody.identity.project)
        assertEquals(edited, custody.identity.baseline)
        assertEquals(entry, custody.identity.entryDocument)
        assertEquals(entry, custody.current)
        assertTrue("Undo to an older named revision must be dirty against the latest saved bytes", custody.dirty)
        custody.restore(GifProjectSnapshot(gif(400), independentRef.name, GifWorkspaceRoute.PHOTOS, independent))
        assertEquals(independentRef, custody.identity.project)
        assertEquals(gif(400), custody.identity.baseline)
        assertFalse(custody.dirty)
        assertEquals(updated, GifStore.loadNamed(context, first.name)!!.project)
    }
    private fun mountRecoveredGif(context: Context, check: (GifDocument, GifWorkspaceSession) -> Unit) {
        var current = GifDocument()
        var retained = GifWorkspaceSession()
        rule.setContent {
            var document by remember { mutableStateOf(GifDocument()) }
            var session by remember { mutableStateOf(GifWorkspaceSession()) }
            CompositionLocalProvider(LocalContext provides context) {
                MaterialTheme {
                    GifMakerScreenContent(document, { document = it; current = it }, {}, {},
                        session, { session = it; retained = it })
                }
            }
        }
        rule.waitUntil(15_000) { retained.identity != null }
        rule.runOnIdle { check(current, retained) }
    }

    @Test fun currentGifSaveReferenceRoundTripsAndRestoresItsExactNamedBaseline() = isolated { context, _ ->
        val baseline = gif()
        val project = saved(GifStore.saveNamed(context, ProjectSaveIntent.Create("Current target"), baseline))
        val edited = gif(250)
        val recipe = PendingGifRecipe(edited, project.name, project)
        val decoded = requireNotNull(PendingGifRecipe.decode(recipe.encode()))
        assertEquals(recipe, decoded)
        assertEquals(project, decoded.savedProject)
        val journal = PendingExportJournal(context.filesDir)
        val pending = journal.begin(ExportWorkspace.GIF, "image/gif", "current.gif",
            recipe.encode(), nativeOwnerId = "current-native-owner")
        mountRecoveredGif(context) { current, retained ->
            assertEquals(edited, current)
            assertEquals(project, retained.identity!!.project)
            assertEquals(project.name, retained.savedName)
            assertEquals(baseline, retained.identity!!.savedDocument)
            assertEquals(baseline, retained.identity!!.entryDocument)
            assertEquals(pending.nativeOwnerId, retained.identity!!.ownerId)
            assertNotEquals("Unsaved recovered edits must remain distinct from the named baseline",
                current, retained.identity!!.baseline)
            val nextExport = PendingGifRecipe(current, retained.savedName, retained.identity!!.project)
            assertEquals(project, PendingGifRecipe.decode(nextExport.encode())!!.savedProject)
            assertEquals(project.name, PendingGifRecipe.decode(nextExport.encode())!!.savedName)
        }
        assertEquals(project, GifStore.loadNamed(context, project.name)!!.project)
        assertEquals(baseline, GifStore.loadNamed(context, project.name)!!.content)
        assertEquals(pending.recipe, journal.read(ExportWorkspace.GIF)!!.recipe)
    }

    @Test fun staleGifSaveVersionKeepsRecoveryUnnamedWithoutChangingTheNewerProject() = isolated { context, _ ->
        val old = saved(GifStore.saveNamed(context, ProjectSaveIntent.Create("Changed target"), gif()))
        val recovered = gif(250)
        val recipe = PendingGifRecipe(recovered, old.name, old)
        val journal = PendingExportJournal(context.filesDir)
        val pending = journal.begin(ExportWorkspace.GIF, "image/gif", "stale.gif",
            recipe.encode(), nativeOwnerId = "stale-native-owner")
        val updated = saved(GifStore.saveNamed(context, ProjectSaveIntent.Update(old), gif(350)))
        assertEquals(old.id, updated.id)
        assertNotEquals(old.version, updated.version)
        mountRecoveredGif(context) { current, retained ->
            assertEquals(recovered, current)
            assertNull(retained.identity!!.project)
            assertNull(retained.identity!!.savedDocument)
            assertNull(retained.savedName)
            assertEquals(GifDocument(), retained.identity!!.entryDocument)
            assertEquals(pending.nativeOwnerId, retained.identity!!.ownerId)
        }
        assertEquals(updated, GifStore.loadNamed(context, old.name)!!.project)
        assertEquals(gif(350), GifStore.loadNamed(context, old.name)!!.content)
        assertEquals(pending.recipe, journal.read(ExportWorkspace.GIF)!!.recipe)
    }

    @Test fun cachedSuppressedGifRequestStillRejectsUnreadableLiveDisposition() = isolated { context, _ ->
        val journal = PendingExportJournal(context.filesDir)
        val owner = "resolved-native-owner"
        val pending = journal.begin(ExportWorkspace.GIF, "image/gif", "retained.gif",
            PendingGifRecipe(gif(), null).encode(), nativeOwnerId = owner)
        assertNotNull(journal.suppressNativeRestore(pending, owner))
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        try {
            val operation = GifExportOperation(context, scope, false, null, {}, { _, _ -> })
            assertFalse(requireNotNull(operation.request).restoreNativeDraft)
            val resolution = File(context.filesDir, "pending-exports/native-resolutions.json")
            assertTrue(resolution.delete())
            assertTrue(resolution.mkdir())
            File(resolution, "obstruction").writeText("test-only")
            assertFalse("A cached suppressed flag cannot approve an unreadable live disposition",
                operation.suppressNativeRestore(owner))
            assertEquals(pending.id, operation.request!!.id)
            assertEquals(pending.recipe, operation.request!!.recipe)
        } finally { scope.cancel() }
    }

}

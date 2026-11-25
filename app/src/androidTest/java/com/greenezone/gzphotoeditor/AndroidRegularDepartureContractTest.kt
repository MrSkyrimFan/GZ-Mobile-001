package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.*
import com.greenezone.gzphotoeditor.ui.PhotoEditorScreen
import com.greenezone.gzphotoeditor.ui.WorkspaceMode
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/** Delivers native results and IO completions after their originating user intent was canceled. */
@RunWith(AndroidJUnit4::class)
class AndroidRegularDepartureContractTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private class SaveGate {
        val armed = AtomicBoolean(false)
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        fun failNextCommit() { armed.set(true) }
        fun verifyPublication() {
            if (!armed.compareAndSet(true, false)) return
            started.countDown()
            check(release.await(10, TimeUnit.SECONDS)) { "Test did not release the intercepted save" }
            throw IOException("Test refused the project index before acknowledging Save")
        }
    }

    private class IsolatedContext(base: Context, val gate: SaveGate) : ContextWrapper(base) {
        private val suffix = ".b1-departure-" + System.nanoTime()
        private val directory = Files.createTempDirectory(base.cacheDir.toPath(), "b1-departure-").toFile()
        private val preferences = mutableSetOf<String>()
        override fun getDataDir(): File = directory
        override fun getFilesDir(): File = directory
        override fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
            preferences += name + suffix
            return baseContext.getSharedPreferences(name + suffix, mode)
        }
        fun close() {
            preferences.forEach { baseContext.deleteSharedPreferences(it) }
            directory.deleteRecursively()
        }
    }

    private class Picker : ActivityResultRegistryOwner {
        val requests = mutableListOf<Int>()
        override val activityResultRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int, contract: ActivityResultContract<I, O>,
                input: I, options: ActivityOptionsCompat?
            ) {
                check(contract is ActivityResultContracts.PickVisualMedia) {
                    "Unexpected activity-result contract: " + contract.javaClass.name
                }
                requests += requestCode
            }
        }
        fun deliver(index: Int, uri: Uri?) {
            activityResultRegistry.dispatchResult(
                requests[index],
                if (uri == null) Activity.RESULT_CANCELED else Activity.RESULT_OK,
                uri?.let { Intent().setData(it) }
            )
        }
    }

    private data class Fixture(
        val context: IsolatedContext, val picker: Picker, val sources: List<Uri>
    )

    private fun withEditor(
        tile: Boolean = false,
        onChildWorkspace: (WorkspaceMode, CollagePhoto?) -> Unit = { _, _ -> },
        test: (Fixture) -> Unit
    ) {
        val gate = SaveGate()
        val context = IsolatedContext(rule.activity, gate)
        val resolver = context.contentResolver
        val sources = mutableListOf<Uri>()
        val mounted = mutableStateOf(true)
        var contentSet = false
        try {
            listOf(Color.RED, Color.BLUE, Color.GREEN).forEach { color ->
                val source = requireNotNull(resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, "gz-b1-departure-" + System.nanoTime() + ".png")
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                ))
                sources += source
                val bitmap = Bitmap.createBitmap(256, 128, Bitmap.Config.ARGB_8888).apply {
                    eraseColor(color)
                }
                try {
                    resolver.openOutputStream(source, "wt")!!.use {
                        assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
                    }
                } finally { bitmap.recycle() }
                resolver.update(source, ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }, null, null)
            }
            val original = sources.map { resolver.openInputStream(it)!!.use { input -> input.readBytes() } }
            val picker = Picker()
            rule.setContent {
                CompositionLocalProvider(
                    LocalContext provides context,
                    LocalActivityResultRegistryOwner provides picker
                ) {
                    MaterialTheme(colorScheme = darkColorScheme()) {
                        if (mounted.value) PhotoEditorScreen(
                            initialCollagePhoto = if (tile) CollagePhoto(
                                sources[0].toString(), id = "b1-child-mode"
                            ) else null,
                            onDoneCollagePhoto = if (tile) ({ error("Use the parent workspace callback") }) else null,
                            onRequestCollageWorkspace = if (tile) onChildWorkspace else null,
                            savePhotoProject = { saveContext, intent, source, document ->
                                ProjectStore.saveNamed(saveContext, intent, source, document) { file, bytes, maxBytes ->
                                    NamedProjectIdentity.verify(file, bytes, maxBytes)
                                    gate.verifyPublication()
                                }
                            }
                        )
                    }
                }
            }
            contentSet = true
            if (tile) waitPhoto()
            test(Fixture(context, picker, sources))
            sources.forEachIndexed { index, uri ->
                assertArrayEquals(original[index], resolver.openInputStream(uri)!!.use { it.readBytes() })
            }
        } finally {
            gate.release.countDown()
            if (contentSet) {
                rule.runOnIdle { mounted.value = false }
                rule.waitForIdle()
            }
            sources.forEach { resolver.delete(it, null, null) }
            context.close()
        }
    }

    private fun options(command: String) {
        rule.onNodeWithText("Options", substring = false).performClick()
        rule.onNodeWithText(command, substring = false).performClick()
    }

    private fun waitPhoto() {
        rule.waitUntil(15_000) {
            rule.onAllNodesWithTag("photo-tools").fetchSemanticsNodes().isNotEmpty() &&
                rule.onAllNodesWithText("Processing…", substring = false).fetchSemanticsNodes().isEmpty()
        }
    }

    private fun launchPhoto(fixture: Fixture): Int {
        val next = fixture.picker.requests.size
        options("New photo…")
        rule.waitUntil(5_000) { fixture.picker.requests.size == next + 1 }
        return next
    }

    private fun deliver(fixture: Fixture, request: Int, uri: Uri?) {
        rule.runOnIdle { fixture.picker.deliver(request, uri) }
        rule.waitForIdle()
    }

    private fun openPhoto(fixture: Fixture, source: Int = 0) {
        val request = launchPhoto(fixture)
        deliver(fixture, request, fixture.sources[source])
        waitPhoto()
    }

    private fun workspace(label: String) {
        rule.onNodeWithContentDescription("Choose editing workspace").performClick()
        rule.onNodeWithText(label, substring = false).performClick()
        rule.waitForIdle()
    }

    private fun flip() {
        rule.onNodeWithTag("photo-tools").performScrollToNode(hasText("Crop"))
        rule.onNodeWithText("Crop", substring = false).performClick()
        rule.onNodeWithTag("crop-transforms").performScrollToNode(hasText("Flip horizontal"))
        rule.onNodeWithText("Flip horizontal", substring = false).performClick()
        rule.onNodeWithContentDescription("Undo").assertIsEnabled()
    }

    private fun back() {
        rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    private fun saveAs(fixture: Fixture, name: String, tile: Boolean = false): NamedProject<SavedProject> {
        options(if (tile) "Save photo project as…" else "Save project as…")
        rule.onNode(hasSetTextAction()).performTextReplacement(name)
        rule.onNodeWithText("Save", substring = false).performClick()
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText(
                if (tile) "Save photo project" else "Save editable project", substring = false
            ).fetchSemanticsNodes().isEmpty()
        }
        return requireNotNull(ProjectStore.loadNamed(fixture.context, name))
    }

    @Test fun canceledAndDuplicatePickerResultsCannotReplaceOrCancelANewerImport() {
        withEditor { fixture ->
            openPhoto(fixture)
            val old = launchPhoto(fixture)
            deliver(fixture, old, null)
            val current = launchPhoto(fixture)
            // Both a late success and a late cancellation belong to the old operation.
            deliver(fixture, old, fixture.sources[2])
            deliver(fixture, old, null)
            deliver(fixture, current, fixture.sources[1])
            waitPhoto()
            val accepted = saveAs(fixture, "Accepted picker")
            assertEquals(fixture.sources[1], accepted.content.image)
            assertEquals(EditDocument(), accepted.content.document)
            // A duplicate result must not create a replacement intent after the first is consumed.
            deliver(fixture, current, fixture.sources[2])
            deliver(fixture, current, null)
            rule.onAllNodesWithText("Unsaved changes").assertCountEquals(0)
            val afterDuplicate = saveAs(fixture, "After duplicate")
            assertEquals(accepted.content.image, afterDuplicate.content.image)
            assertEquals(accepted.content.document, afterDuplicate.content.document)
            rule.onNodeWithContentDescription("Undo").assertIsNotEnabled()
        }
    }

    @Test fun discardedReplacementCanceledInPickerPreservesDirtyPhotoAndItsHistory() {
        withEditor { fixture ->
            openPhoto(fixture)
            flip()
            options("New photo…")
            rule.onNodeWithText("Unsaved changes").assertIsDisplayed()
            rule.onNodeWithText("Discard", substring = false).performClick()
            rule.waitUntil(5_000) { fixture.picker.requests.size == 2 }
            deliver(fixture, 1, null)
            rule.onNodeWithContentDescription("Undo").assertIsEnabled()
            val retained = saveAs(fixture, "Canceled replacement")
            assertEquals(fixture.sources[0], retained.content.image)
            assertTrue(retained.content.document.mirrored)
            rule.onNodeWithContentDescription("Undo").performClick()
            rule.onNodeWithContentDescription("Undo").assertIsNotEnabled()
            rule.onNodeWithContentDescription("Redo").assertIsEnabled()
            assertEquals(EditDocument(), saveAs(fixture, "Replacement undo").content.document)
        }
    }

    @Test fun dirtyRegularBackResolvesBeforeReturningToTheActualPreviousWorkspace() {
        withEditor { fixture ->
            // Three distinct routes make GIF the actual previous workspace when Photo opens.
            workspace("Collage maker")
            workspace("GIF maker")
            workspace("Photo editing")
            openPhoto(fixture)
            val baseline = saveAs(fixture, "Before prior route")
            flip()
            back()
            rule.onNodeWithText("Unsaved changes").assertIsDisplayed()
            rule.onNodeWithText("Cancel", substring = false).performClick()
            rule.onNodeWithTag("photo-tools").assertIsDisplayed()
            rule.onNodeWithContentDescription("Undo").assertIsEnabled()
            back()
            rule.onNodeWithText("Discard", substring = false).performClick()
            rule.onNodeWithText("GIF ▾", substring = false).assertIsDisplayed()
            workspace("Photo editing")
            waitPhoto()
            rule.onNodeWithContentDescription("Undo").assertIsNotEnabled()
            rule.onNodeWithContentDescription("Redo").assertIsNotEnabled()
            val returned = saveAs(fixture, "Returned baseline")
            assertEquals(baseline.content.image, returned.content.image)
            assertEquals(baseline.content.document, returned.content.document)
        }
    }

    @Test fun dirtyRegularModeCancelKeepsHistoryAndUndoToBaselineLeavesWithoutPrompt() {
        withEditor { fixture ->
            openPhoto(fixture)
            val baseline = saveAs(fixture, "Mode baseline")
            flip()
            workspace("GIF maker")
            rule.onNodeWithText("Unsaved changes").assertIsDisplayed()
            rule.onNodeWithText("Cancel", substring = false).performClick()
            rule.onNodeWithContentDescription("Undo").assertIsEnabled()
            rule.onNodeWithContentDescription("Undo").performClick()
            rule.onNodeWithContentDescription("Undo").assertIsNotEnabled()
            rule.onNodeWithContentDescription("Redo").assertIsEnabled()
            workspace("GIF maker")
            rule.onAllNodesWithText("Unsaved changes").assertCountEquals(0)
            rule.onNodeWithText("GIF ▾", substring = false).assertIsDisplayed()
            workspace("Photo editing")
            waitPhoto()
            rule.onNodeWithContentDescription("Redo").assertIsEnabled()
            assertEquals(baseline.content.document, saveAs(fixture, "Clean mode return").content.document)
        }
    }

    @Test fun standaloneTileSaveDoesNotBypassApplyWhenChoosingPhotoInTheChild() {
        val requests = mutableListOf<Pair<WorkspaceMode, CollagePhoto?>>()
        withEditor(tile = true, onChildWorkspace = { mode, photo -> requests += mode to photo }) { fixture ->
            flip()
            val native = saveAs(fixture, "Standalone tile", tile = true)
            assertTrue(native.content.document.mirrored)
            workspace("Photo editing")
            rule.onNodeWithText("Apply tile changes?").assertIsDisplayed()
            rule.onNode(hasText("Cancel", substring = false) and hasAnyAncestor(isDialog())).performClick()
            assertTrue(requests.isEmpty())
            rule.onNodeWithContentDescription("Undo").assertIsEnabled()
            workspace("Photo editing")
            rule.onNodeWithText("Apply to tile", substring = false).performClick()
            rule.runOnIdle {
                assertEquals(1, requests.size)
                assertEquals(WorkspaceMode.PHOTO, requests.single().first)
                val proposed = requireNotNull(requests.single().second)
                assertEquals("b1-child-mode", proposed.id)
                assertEquals(fixture.sources[0].toString(), proposed.uri)
                assertEquals(native.content.document, proposed.edits)
            }
        }
    }

    @Test fun cancelDuringFailedCreatePreventsLateSaveDialogAndKeepsDirtyDocument() {
        withEditor { fixture ->
            openPhoto(fixture)
            flip()
            back()
            rule.onNodeWithText("Save", substring = false).performClick()
            rule.onNode(hasSetTextAction()).performTextReplacement("Canceled IO")
            fixture.context.gate.failNextCommit()
            rule.onNodeWithText("Save", substring = false).performClick()
            rule.waitUntil(5_000) { fixture.context.gate.started.count == 0L }
            rule.onNodeWithText("Cancel", substring = false).performClick()
            fixture.context.gate.release.countDown()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Processing…", substring = false).fetchSemanticsNodes().isEmpty()
            }
            rule.waitForIdle()
            rule.onAllNodesWithText("Save editable project", substring = false).assertCountEquals(0)
            rule.onAllNodesWithText("Replace saved project?", substring = false).assertCountEquals(0)
            rule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
            assertNull(ProjectStore.loadNamed(fixture.context, "Canceled IO"))
            rule.onNodeWithContentDescription("Undo").assertIsEnabled()
            back()
            rule.onNodeWithText("Unsaved changes").assertIsDisplayed()
            rule.onNodeWithText("Cancel", substring = false).performClick()
            val retained = saveAs(fixture, "After canceled IO")
            assertTrue(retained.content.document.mirrored)
            assertEquals(fixture.sources[0], retained.content.image)
        }
    }

    @Test fun failedNamedUpdateHasTypedRetryAndCancelPreservesTheExistingProject() {
        withEditor { fixture ->
            openPhoto(fixture)
            val original = saveAs(fixture, "Stable target")
            flip()
            // Return a storage refusal from the real ProjectStore commit path.
            fixture.context.gate.release.countDown()
            fixture.context.gate.failNextCommit()
            options("Save project")
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Project was not saved", substring = false).fetchSemanticsNodes().isNotEmpty()
            }
            rule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
            rule.onNodeWithText("Retry", substring = false).assertIsEnabled()
            assertEquals(original, ProjectStore.loadNamed(fixture.context, "Stable target"))
            rule.onNodeWithText("Cancel", substring = false).performClick()
            back()
            rule.onNodeWithText("Unsaved changes").assertIsDisplayed()
            rule.onNodeWithText("Cancel", substring = false).performClick()
            // A second failure followed by Retry must update the original identity, not create another save.
            fixture.context.gate.failNextCommit()
            options("Save project")
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Project was not saved", substring = false).fetchSemanticsNodes().isNotEmpty()
            }
            rule.onNodeWithText("Retry", substring = false).performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Project was not saved", substring = false).fetchSemanticsNodes().isEmpty() &&
                    rule.onAllNodesWithText("Processing…", substring = false).fetchSemanticsNodes().isEmpty()
            }
            val updated = requireNotNull(ProjectStore.loadNamed(fixture.context, "Stable target"))
            assertEquals(original.project.id, updated.project.id)
            assertNotEquals(original.project.version, updated.project.version)
            assertEquals(original.content.image, updated.content.image)
            assertTrue(updated.content.document.mirrored)
            assertEquals(1, ProjectStore.load(fixture.context).size)
            rule.onNodeWithContentDescription("Undo").assertIsEnabled()
            back()
            rule.onAllNodesWithText("Unsaved changes").assertCountEquals(0)
            rule.onAllNodesWithTag("photo-tools").assertCountEquals(0)
        }
    }

    @Test fun staleProjectChooserCannotOpenARecreatedNameOrDiscardTheCurrentPhoto() {
        withEditor { fixture ->
            openPhoto(fixture)
            val name = "Scanned project"
            val previous = saveAs(fixture, name)
            flip()
            options("Open project…")
            rule.onNodeWithText("Discard", substring = false).performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText(name, substring = false).fetchSemanticsNodes().isNotEmpty()
            }
            assertTrue(ProjectStore.deleteNamed(fixture.context, previous.project))
            assertTrue(ProjectStore.save(fixture.context, name, fixture.sources[1], EditDocument()))
            val recreated = requireNotNull(ProjectStore.loadNamed(fixture.context, name))
            assertNotEquals(previous.project.id, recreated.project.id)

            rule.onNodeWithText(name, substring = false).performClick()
            rule.onNodeWithText("Open project", substring = false).performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Could not open that project. Your photo is unchanged.", substring = false)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            rule.onNodeWithContentDescription("Undo").assertIsEnabled()
            val retained = saveAs(fixture, "Retained after stale Open")
            assertEquals(fixture.sources[0], retained.content.image)
            assertTrue(retained.content.document.mirrored)
            assertEquals(recreated, ProjectStore.loadNamed(fixture.context, name))
        }
    }
}

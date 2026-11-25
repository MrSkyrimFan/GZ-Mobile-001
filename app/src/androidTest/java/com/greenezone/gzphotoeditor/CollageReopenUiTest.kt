package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.*
import com.greenezone.gzphotoeditor.ui.CollageWorkspaceScreen
import java.io.File
import java.nio.file.Files
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CollageReopenUiTest {
    @get:Rule val rule = createComposeRule()

    @Test fun duplicateUriSelectionSurvivesMovementAndUndo() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val resolver = target.contentResolver
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "gz-tile-selection.png")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }))
        val source = Bitmap.createBitmap(12, 12, Bitmap.Config.ARGB_8888)
        try {
            source.eraseColor(Color.RED)
            requireNotNull(resolver.openOutputStream(uri, "wt")).use {
                assertTrue(source.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
            val first = CollagePhoto(uri.toString(), EditDocument(grade = Grade(exposure = -.2f)))
            val second = CollagePhoto(uri.toString(), EditDocument(grade = Grade(exposure = .2f)))
            var observed = CollageDocument(listOf(first, second))
            rule.setContent {
                var current by remember { mutableStateOf(observed) }
                CollageWorkspaceScreen(current, { current = it; observed = it }, {})
            }
            rule.waitUntil(15_000) {
                rule.onAllNodesWithContentDescription("Photo collage preview").fetchSemanticsNodes().isNotEmpty()
            }
            rule.onNodeWithText("Photos", substring = false).performClick()
            val actions = rule.onNodeWithTag("collage-tile-actions")
            actions.performScrollToNode(hasText("Move right"))
            rule.onNodeWithText("Move right").performClick()
            rule.runOnIdle { assertEquals(listOf(second, first), observed.photos) }
            rule.onNodeWithContentDescription("Undo").performClick()
            rule.runOnIdle { assertEquals(listOf(first, second), observed.photos) }

            actions.performScrollToNode(hasText("Remove"))
            rule.onNodeWithText("Remove").performClick()
            rule.runOnIdle { assertEquals(listOf(second), observed.photos) }
        } finally {
            source.recycle()
            resolver.delete(uri, null, null)
        }
    }

    @Test fun missingSavedTileClearsPreviousPreviewAndRelinksWithItsEdits() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = Files.createTempDirectory(target.cacheDir.toPath(), "collage-relink-").toFile()
        val storage = object : ContextWrapper(target) { override fun getFilesDir(): File = folder }
        val resolver = target.contentResolver
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "gz-collage-relink.png")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }))
        val bitmap = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888)
        try {
            bitmap.eraseColor(Color.RED)
            requireNotNull(resolver.openOutputStream(uri, "wt")).use {
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
            val keptEdits = EditDocument(grade = Grade(exposure = -.3f))
            val valid = CollagePhoto(uri.toString())
            val missing = CollagePhoto("content://gz.test.unavailable/missing", keptEdits)
            assertTrue(CollageStore.save(storage, "Intact A", CollageDocument(listOf(valid, valid.copy(
                id = "second-A"
            )))))
            assertTrue(CollageStore.save(storage, "Missing B", CollageDocument(listOf(missing, valid))))
            val picker = object : ActivityResultRegistryOwner {
                override val activityResultRegistry = object : ActivityResultRegistry() {
                    override fun <I, O> onLaunch(
                        requestCode: Int, contract: ActivityResultContract<I, O>, input: I,
                        options: ActivityOptionsCompat?
                    ) {
                        dispatchResult(requestCode, Activity.RESULT_OK, Intent().setData(uri))
                    }
                }
            }
            var observed = CollageDocument()
            rule.setContent {
                val host = LocalContext.current
                val context = remember(host) {
                    object : ContextWrapper(host) { override fun getFilesDir(): File = folder }
                }
                var current by remember { mutableStateOf(CollageDocument()) }
                CompositionLocalProvider(
                    LocalContext provides context, LocalActivityResultRegistryOwner provides picker
                ) {
                    CollageWorkspaceScreen(current, { current = it; observed = it }, {})
                }
            }
            rule.onNodeWithText("Options").performClick()
            rule.onNodeWithText("Open project…").performClick()
            rule.waitUntil(10_000) { rule.onAllNodesWithText("Intact A").fetchSemanticsNodes().isNotEmpty() }
            rule.onNodeWithText("Intact A").performClick()
            rule.onNodeWithText("Open").performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithContentDescription("Photo collage preview").fetchSemanticsNodes().isNotEmpty()
            }
            rule.onNodeWithText("Photos", substring = false).performClick()
            val actions = rule.onNodeWithTag("collage-tile-actions")
            rule.onNodeWithText("Options").performClick()
            rule.onNodeWithText("Open project…").performClick()
            rule.waitUntil(10_000) { rule.onAllNodesWithText("Missing B").fetchSemanticsNodes().isNotEmpty() }
            rule.onNodeWithText("Missing B").performClick()
            rule.onNodeWithText("Open").performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Photo 1 is inaccessible.", substring = true).fetchSemanticsNodes().isNotEmpty()
            }
            rule.onNodeWithContentDescription("Photo collage preview").assertDoesNotExist()
            actions.performScrollToNode(hasText("Relink source"))
            rule.onNodeWithText("Relink source").performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Confirm original source").fetchSemanticsNodes().isNotEmpty()
            }
            rule.runOnIdle {
                assertEquals("Relink must remain provisional until confirmation", missing, observed.photos.first())
                assertEquals(valid, observed.photos.last())
            }
            rule.onNode(hasText("Use this source", substring = false) and hasAnyAncestor(isDialog())).performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithContentDescription("Photo collage preview").fetchSemanticsNodes().isNotEmpty()
            }
            rule.runOnIdle {
                assertEquals(missing.copy(uri = uri.toString()), observed.photos.first())
                assertEquals(valid, observed.photos.last())
            }
        } finally {
            bitmap.recycle()
            resolver.delete(uri, null, null)
            folder.deleteRecursively()
        }
    }

    @Test fun failedAtomicCollageWritePreservesPublishedProject() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = Files.createTempDirectory(target.cacheDir.toPath(), "collage-atomic-").toFile()
        val storage = object : ContextWrapper(target) { override fun getFilesDir(): File = folder }
        val previous = CollageDocument(listOf(CollagePhoto("content://gz.test/first"),
            CollagePhoto("content://gz.test/second")))
        try {
            assertTrue(CollageStore.save(storage, "Retain previous", previous))
            val published = File(folder, "gz_collages").listFiles()!!.single { it.extension == "gzc" }
            val before = published.readBytes()
            val previousIdentity = requireNotNull(CollageStore.loadNamed(storage, "Retain previous")).project
            val obstacle = File(published.path + ".new")
            assertTrue(obstacle.mkdir())
            File(obstacle, "blocker").writeText("test")
            val replacement = previous.copy(layout = previous.layout.copy(gap = .05f))
            assertFalse(CollageStore.save(storage, "Retain previous", replacement))
            assertArrayEquals(before, published.readBytes())
            assertEquals(previous, CollageStore.load(storage, "Retain previous"))
            val reopened = requireNotNull(CollageStore.loadNamed(storage, "Retain previous"))
            assertEquals(previousIdentity, reopened.project)
            assertEquals(previous, reopened.content)
            assertFalse("An unchanged healthy payload must not remain hidden behind a failed transaction",
                NamedProjectIdentity.hasPendingPublication(published))
            assertTrue("A failed save must not delete an unrelated staging obstacle", obstacle.isDirectory)
            assertEquals("test", File(obstacle, "blocker").readText())
        } finally {
            folder.deleteRecursively()
        }
    }

    @Test fun emptyStagingDirectoryRejectsSaveWithoutHidingOrMutatingHealthyProject() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = Files.createTempDirectory(target.cacheDir.toPath(), "collage-empty-staging-").toFile()
        val storage = object : ContextWrapper(target) { override fun getFilesDir(): File = folder }
        val previous = CollageDocument(listOf(
            CollagePhoto("content://gz.test/empty-stage-first"),
            CollagePhoto("content://gz.test/empty-stage-second")
        ))
        try {
            assertTrue(CollageStore.save(storage, "Empty staging", previous))
            val published = File(folder, "gz_collages").listFiles()!!.single { it.extension == "gzc" }
            val before = published.readBytes()
            val original = requireNotNull(CollageStore.loadNamed(storage, "Empty staging"))
            val obstacle = File(published.path + ".new")
            assertTrue(obstacle.mkdir())
            assertTrue(requireNotNull(obstacle.listFiles()).isEmpty())
            val replacement = previous.copy(layout = previous.layout.copy(gap = .05f))

            // Both save APIs must refuse an empty non-file staging occupant.
            assertFalse(CollageStore.save(storage, "Empty staging", replacement))
            assertEquals(ProjectSaveResult.Rejected(ProjectSaveFailure.STORAGE_FAILURE),
                CollageStore.saveNamed(storage, ProjectSaveIntent.Update(original.project), replacement))
            assertArrayEquals(before, published.readBytes())
            assertTrue("Save deleted the empty staging directory", obstacle.isDirectory)
            assertFalse(NamedProjectIdentity.hasPendingPublication(published))

            // Legacy and identity-aware reads must still expose the healthy
            // project without treating the directory as disposable AtomicFile debris.
            assertEquals(previous, CollageStore.load(storage, "Empty staging"))
            assertTrue("Legacy read deleted the staging directory", obstacle.isDirectory)
            assertEquals(original, CollageStore.loadNamed(storage, "Empty staging"))
            assertTrue("Named read deleted the staging directory", obstacle.isDirectory)
            assertTrue(requireNotNull(obstacle.listFiles()).isEmpty())
            assertArrayEquals(before, published.readBytes())
        } finally {
            folder.deleteRecursively()
        }
    }

    @Test fun emptyWorkspaceReopensSavedTilesInTheExistingExportSlot() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = Files.createTempDirectory(target.cacheDir.toPath(), "collage-reopen-").toFile()
        val storage = object : ContextWrapper(target) { override fun getFilesDir(): File = folder }
        val resolver = target.contentResolver
        val sources = mutableListOf<Uri>()
        fun photo(index: Int, color: Int): CollagePhoto {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "gz-reopen-$index.png")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
            sources.add(uri)
            val bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
            try {
                bitmap.eraseColor(color)
                requireNotNull(resolver.openOutputStream(uri, "wt")).use {
                    assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
                }
            } finally { bitmap.recycle() }
            return CollagePhoto(uri.toString())
        }
        try {
            val first = photo(1, Color.RED).copy(edits = EditDocument(grade = Grade(exposure = -.3f)))
            val saved = CollageDocument(listOf(first, photo(2, Color.BLUE)),
                CollageLayout(template = CollageTemplate.STACKED))
            assertTrue(CollageStore.save(storage, "Two tile portrait", saved))
            var restored: CollageDocument? = null
            rule.setContent {
                val host = LocalContext.current
                val context = remember(host) {
                    object : ContextWrapper(host) { override fun getFilesDir(): File = folder }
                }
                var current by remember { mutableStateOf(CollageDocument()) }
                CompositionLocalProvider(LocalContext provides context) {
                    CollageWorkspaceScreen(current, { current = it; restored = it }, {})
                }
            }
            val options = rule.onNodeWithTag("workspace-options")
            options.assertIsDisplayed()
            val originalSlot = options.fetchSemanticsNode().boundsInRoot
            options.performClick()
            rule.onNodeWithText("Add photos…").assertIsDisplayed()
            rule.onNodeWithText("Open project…").performClick()
            rule.waitUntil(10_000) { rule.onAllNodesWithText("Two tile portrait").fetchSemanticsNodes().isNotEmpty() }
            rule.onNodeWithText("Two tile portrait").performClick()
            rule.onNodeWithText("Open").performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithContentDescription("Photo collage preview").fetchSemanticsNodes().isNotEmpty()
            }
            rule.runOnIdle { assertEquals(saved, restored) }
            assertEquals(originalSlot, options.fetchSemanticsNode().boundsInRoot)
            options.performClick()
            val export = rule.onNode(hasText("Export image…") and hasClickAction())
            export.assertIsDisplayed()
            export.performClick()
            rule.onNodeWithText("Export image").assertIsDisplayed()
        } finally {
            sources.forEach { resolver.delete(it, null, null) }
            folder.deleteRecursively()
        }
    }
}

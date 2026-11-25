package com.greenezone.gzphotoeditor

import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.CollageDocument
import com.greenezone.gzphotoeditor.editor.CollagePhoto
import com.greenezone.gzphotoeditor.editor.CollageStore
import com.greenezone.gzphotoeditor.editor.CollageTemplate
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.Grade
import com.greenezone.gzphotoeditor.editor.Looks
import com.greenezone.gzphotoeditor.editor.NamedProject
import com.greenezone.gzphotoeditor.ui.PhotoEditorScreen
import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidCollageNestedDepartureTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private val projectName = "Nested departure fixture"
    private val undo get() = rule.onNodeWithContentDescription("Undo")
    private val redo get() = rule.onNodeWithContentDescription("Redo")

    private data class SavedFixture(
        val context: Context,
        val project: NamedProject<CollageDocument>,
        val file: File,
        val bytes: ByteArray
    ) {
        val layoutOnly: CollageDocument
            get() = project.content.copy(
                layout = project.content.layout.copy(template = CollageTemplate.STACKED)
            )
    }

    @Test fun parentCancelKeepsUnappliedChildAndPriorParentEdit() = withNamedCollage { fixture ->
        val editedPixel = editTileAndRequestDeparture()
        dialogAction("Cancel")
        assertCanceledThenSaveParentOnly(fixture, editedPixel)
    }

    @Test fun failedParentSaveThenCancelKeepsUnappliedChildAndHealthySavedBytes() =
        withNamedCollage { fixture ->
            val editedPixel = editTileAndRequestDeparture()
            // An empty non-file staging occupant blocks the real store publication.
            val obstacle = File(fixture.file.path + ".new")
            assertTrue(obstacle.mkdir())
            try {
                dialogAction("Save")
                waitForText("Collage not saved")
                assertTrue(obstacle.isDirectory)
                assertArrayEquals(fixture.bytes, fixture.file.readBytes())
                dialogAction("Cancel")
            } finally {
                if (obstacle.isDirectory) obstacle.delete()
            }
            assertCanceledThenSaveParentOnly(fixture, editedPixel)
        }

    @Test fun successfulParentSaveAppliesOnlySelectedTileAndDepartsWithOneCheckpoint() =
        withNamedCollage { fixture ->
            editTileAndRequestDeparture()
            dialogAction("Save")
            waitForText("GIF ▾")
            rule.onNodeWithTag("photo-tools").assertDoesNotExist()

            val expectedPhotos = fixture.layoutOnly.photos.toMutableList()
            val look = Looks.all.single { it.name == "Golden" }.grade
            expectedPhotos[1] = expectedPhotos[1].copy(edits = EditDocument(grade = look))
            assertPublishedUpdate(fixture, fixture.layoutOnly.copy(photos = expectedPhotos))

            rule.runOnIdle { rule.activity.onBackPressedDispatcher.onBackPressed() }
            waitForText("Collage ▾")
            undo.assertIsEnabled().performClick() // Revert the single applied tile edit.
            undo.assertIsEnabled().performClick() // Revert the earlier layout edit.
            undo.assertIsNotEnabled()
            redo.assertIsEnabled().performClick()
            redo.assertIsEnabled().performClick()
            redo.assertIsNotEnabled()
        }

    private fun editTileAndRequestDeparture(): Int {
        rule.onNodeWithText("Stacked").performClick()
        undo.assertIsEnabled()
        rule.onNodeWithTag("collage-sections").performScrollToNode(hasText("Photos"))
        rule.onNodeWithText("Photos").performClick()
        rule.onNodeWithText("Photo 2").performClick()
        rule.onNodeWithTag("collage-tile-actions").performScrollToNode(hasText("Edit selected"))
        rule.onNodeWithText("Edit selected").performClick()
        rule.waitUntil(15_000) {
            rule.onAllNodesWithTag("photo-tools").fetchSemanticsNodes().isNotEmpty()
        }

        val originalPixel = photoPixel()
        rule.onNodeWithText("Golden").performClick()
        undo.assertIsEnabled()
        rule.waitUntil(15_000) { photoPixel() != originalPixel }
        val editedPixel = photoPixel()

        chooseWorkspace("GIF maker")
        waitForText("Apply tile changes?")
        dialogAction("Apply to tile")
        waitForText("Keep your collage edits?")
        return editedPixel
    }

    private fun assertCanceledThenSaveParentOnly(fixture: SavedFixture, editedPixel: Int) {
        rule.onNodeWithTag("photo-tools").assertIsDisplayed()
        rule.onNodeWithText("GIF ▾").assertDoesNotExist()
        assertEquals("Cancellation lost the unapplied tile edit", editedPixel, photoPixel())
        undo.assertIsEnabled()
        assertEquals(fixture.project, CollageStore.loadNamed(fixture.context, projectName))
        assertArrayEquals(fixture.bytes, fixture.file.readBytes())

        // Cancel the child separately. Saving now proves the canceled parent decision
        // neither applied the child's edit nor discarded the parent's dirty layout.
        rule.onNodeWithText("Cancel").performClick()
        waitForText("Collage ▾")
        undo.assertIsEnabled()
        command("Save project")
        waitForText("Save collage changes?")
        dialogAction("Save")
        rule.waitUntil(15_000) {
            CollageStore.loadNamed(fixture.context, projectName)?.content == fixture.layoutOnly
        }
        assertPublishedUpdate(fixture, fixture.layoutOnly)

        undo.assertIsEnabled().performClick()
        undo.assertIsNotEnabled()
        redo.assertIsEnabled().performClick()
        redo.assertIsNotEnabled()
    }

    private fun assertPublishedUpdate(fixture: SavedFixture, expected: CollageDocument) {
        val saved = requireNotNull(CollageStore.loadNamed(fixture.context, projectName))
        assertEquals(expected, saved.content)
        assertEquals(fixture.project.project.id, saved.project.id)
        assertNotEquals(fixture.project.project.version, saved.project.version)
    }

    private fun withNamedCollage(check: (SavedFixture) -> Unit) {
        val activity = rule.activity
        val folder = Files.createTempDirectory(activity.cacheDir.toPath(), "collage-nested-").toFile()
        val storage = object : ContextWrapper(activity) {
            override fun getFilesDir(): File = folder
        }
        val resolver = activity.contentResolver
        val mounted = mutableStateOf(true)
        var source: Uri? = null
        var contentSet = false
        try {
            val uri = requireNotNull(resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "gz-collage-nested.png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            )).also { source = it }
            val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
            try {
                bitmap.eraseColor(Color.rgb(65, 105, 155))
                requireNotNull(resolver.openOutputStream(uri, "wt")).use {
                    assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
                }
            } finally {
                bitmap.recycle()
            }
            resolver.update(uri, ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }, null, null)
            val sourceBytes = requireNotNull(resolver.openInputStream(uri)).use { it.readBytes() }

            val parent = CollageDocument(listOf(
                CollagePhoto(uri.toString(), EditDocument(grade = Grade(exposure = -.25f))),
                CollagePhoto(uri.toString())
            ))
            assertTrue(CollageStore.save(storage, projectName, parent))
            val saved = requireNotNull(CollageStore.loadNamed(storage, projectName))
            assertEquals(parent, saved.content)
            val file = requireNotNull(File(folder, "gz_collages").listFiles())
                .single { it.extension == "gzc" }
            val fixture = SavedFixture(storage, saved, file, file.readBytes())

            rule.setContent {
                if (mounted.value) {
                    val host = LocalContext.current
                    val wrapped = remember(host) {
                        object : ContextWrapper(host) { override fun getFilesDir(): File = folder }
                    }
                    CompositionLocalProvider(LocalContext provides wrapped) {
                        PhotoEditorScreen()
                    }
                }
            }
            contentSet = true
            chooseWorkspace("Collage maker")
            waitForText("Collage ▾")
            command("Open project…")
            waitForText(projectName)
            rule.onNodeWithText(projectName).performClick()
            dialogAction("Open")
            rule.waitUntil(15_000) {
                rule.onAllNodesWithContentDescription("Photo collage preview")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            undo.assertIsNotEnabled()
            check(fixture)
            assertArrayEquals(sourceBytes,
                requireNotNull(resolver.openInputStream(uri)).use { it.readBytes() })
        } finally {
            try {
                if (contentSet) {
                    rule.runOnIdle { mounted.value = false }
                    rule.waitForIdle()
                }
            } finally {
                source?.let { resolver.delete(it, null, null) }
                folder.deleteRecursively()
            }
        }
    }

    private fun photoPixel(): Int {
        val frame = rule.onNodeWithContentDescription(
            "Photo editing canvas. Tap to hide or show tools."
        ).captureToImage().asAndroidBitmap()
        return try {
            frame.getPixel(frame.width / 2, frame.height / 2)
        } finally {
            frame.recycle()
        }
    }

    private fun chooseWorkspace(label: String) {
        rule.onNodeWithContentDescription("Choose editing workspace").performClick()
        rule.onNodeWithText(label).performClick()
    }

    private fun command(label: String) {
        rule.onNodeWithTag("workspace-options").performClick()
        rule.onNodeWithText(label).performClick()
    }

    private fun dialogAction(label: String) {
        rule.onNode(hasText(label, substring = false) and hasAnyAncestor(isDialog())).performClick()
    }

    private fun waitForText(label: String) {
        rule.waitUntil(15_000) { rule.onAllNodesWithText(label).fetchSemanticsNodes().isNotEmpty() }
    }
}

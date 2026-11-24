package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.GifStore
import com.greenezone.gzphotoeditor.editor.VideoGifSource
import com.greenezone.gzphotoeditor.ui.GifMakerScreen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.OutputStream
import java.nio.file.Files
import java.util.Base64

@RunWith(AndroidJUnit4::class)
class AndroidGifProjectIdentityTest {
    @get:Rule val rule = createComposeRule()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val target = instrumentation.targetContext
    private val resolver = target.contentResolver
    private var workspaceGeneration by mutableIntStateOf(0)
    @Volatile private var current = GifDocument()

    @Test fun videoRecipesSaveToTheOpenedProjectAndReopenIndependently() = withProjects { storage ->
        val firstUri = video("gif_sample_2sec_h264.mp4")
        val secondUri = video("gif_portrait_motion_fixture.mp4")
        val first = VideoGifSource.document(target, firstUri)
        val second = VideoGifSource.document(target, secondUri)
        assertTrue(GifStore.save(storage, "Beta video", second))
        mount(storage.filesDir, first, chooseVideo = { firstUri })
        waitVideo(firstUri, first.video!!.reverse)

        videoSave()
        submitName("Alpha video")
        assertEquals(first, GifStore.load(storage, "Alpha video"))

        videoOpen("Beta video")
        waitVideo(secondUri, second.video!!.reverse)
        rule.onNodeWithText("Reverse").performClick()
        waitVideo(secondUri, !second.video!!.reverse)
        val editedSecond = current
        videoSave()
        val suggestedName = draftName()
        submitName()
        assertEquals("Saving B must leave A's source and recipe intact",
            first, GifStore.load(storage, "Alpha video"))
        assertEquals(editedSecond, GifStore.load(storage, "Beta video"))
        assertEquals("Beta video", suggestedName)

        videoSave()
        rule.onNode(hasSetTextAction()).performTextReplacement("  Alpha   video  ")
        rule.onNodeWithText("Save").performClick()
        rule.onNodeWithText("Replace GIF project?").assertIsDisplayed()
        rule.onNodeWithText("Cancel").performClick()
        rule.onNodeWithText("Cancel").performClick()
        assertEquals(first, GifStore.load(storage, "Alpha video"))
        assertEquals(editedSecond, GifStore.load(storage, "Beta video"))

        videoFile("Choose video")
        waitVideo(firstUri, first.video!!.reverse)
        videoSave()
        assertEquals("", draftName())
        rule.onNodeWithText("Cancel").performClick()
        rule.onNodeWithText("Undo").performClick()
        waitVideo(secondUri, editedSecond.video!!.reverse)
        videoSave()
        assertEquals("Beta video", draftName())
        rule.onNodeWithText("Cancel").performClick()
        rule.onNodeWithText("Redo").performClick()
        waitVideo(firstUri, first.video!!.reverse)
        videoSave()
        assertEquals("", draftName())
        rule.onNodeWithText("Cancel").performClick()

        restartWorkspace()
        videoOpen("Alpha video")
        waitVideo(firstUri, first.video!!.reverse)
        assertEquals(first, current)
        videoOpen("Beta video")
        waitVideo(secondUri, editedSecond.video!!.reverse)
        assertEquals(editedSecond, current)
    }

    @Test fun photoRecipesKeepTheirPixelsAndTimingAcrossSaveAndFreshOpen() = withProjects { storage ->
        val red = photo("gz-project-red.png", Color.RED)
        val blue = photo("gz-project-blue.png", Color.BLUE)
        val first = GifDocument(frames = listOf(red.toString(), blue.toString()), delayMs = 350)
        val second = first.copy(frames = first.frames.reversed(), delayMs = 650)
        assertTrue(GifStore.save(storage, "Beta photos", second))
        mount(storage.filesDir, first)
        waitPhotoColor(Color.RED)

        photoSave()
        submitName("Alpha photos")
        assertEquals(first, GifStore.load(storage, "Alpha photos"))
        rule.onNodeWithText("Photos").performScrollTo().performClick()
        photoOpen("Beta photos")
        waitPhotoColor(Color.BLUE)

        rule.onNodeWithText("Timing").performScrollTo().performClick()
        rule.onNodeWithContentDescription("Photo GIF frame duration").performSemanticsAction(
            SemanticsActions.SetProgress
        ) { it(720f) }
        rule.waitUntil(15_000) { current.delayMs == 720 }
        val editedSecond = current
        photoSave()
        val suggestedName = draftName()
        submitName()
        assertEquals("Saving B must leave A's photo order and timing intact",
            first, GifStore.load(storage, "Alpha photos"))
        assertEquals(editedSecond, GifStore.load(storage, "Beta photos"))
        assertEquals("Beta photos", suggestedName)

        restartWorkspace()
        videoOpen("Alpha photos")
        waitPhotoColor(Color.RED)
        assertEquals(first, current)
        photoOpen("Beta photos")
        waitPhotoColor(Color.BLUE)
        assertEquals(editedSecond, current)
    }

    @Test fun undoingTheFirstVideoImportLeavesRedoAvailableUntilAnotherImport() = withProjects { storage ->
        val first = video("gif_sample_2sec_h264.mp4")
        val second = video("gif_portrait_motion_fixture.mp4")
        var source = first
        mount(storage.filesDir, chooseVideo = { source })

        rule.onNodeWithText("Choose video").performClick()
        waitVideo(first, false)
        rule.onNodeWithText("Undo").performClick()
        rule.onNodeWithText("Select one video to turn into a GIF").assertIsDisplayed()
        rule.onNodeWithText("Redo").assertIsEnabled().performClick()
        waitVideo(first, false)
        rule.onNodeWithText("Undo").performClick()
        rule.runOnIdle { source = second }
        rule.onNodeWithText("Choose video").performClick()
        waitVideo(second, false)
        rule.onNodeWithText("Redo").assertIsNotEnabled()
    }

    @Test fun deletingNamedBackupOnlyProjectReclaimsCapacityWithoutLosingCurrentEdits() = withProjects { storage ->
        val red = photo("gz-quota-red.png", Color.RED)
        val blue = photo("gz-quota-blue.png", Color.BLUE)
        val recipe = GifDocument(frames = listOf(red.toString(), blue.toString()))
        val protected = (1..11).associate { slot ->
            "Slot $slot" to recipe.copy(delayMs = 350 + 10 * slot)
        }
        for ((name, document) in protected) assertTrue(GifStore.save(storage, name, document))
        val obsoleteName = "Obsolete GIF"
        assertTrue(GifStore.save(storage, obsoleteName, recipe))
        mount(storage.filesDir)

        rule.onNodeWithText("Open saved GIF project").performClick()
        rule.onNodeWithContentDescription("Delete $obsoleteName").performScrollTo().performClick()
        rule.onNodeWithText("Delete GIF project?").assertIsDisplayed()
        rule.onNodeWithText("Cancel").performClick()
        rule.onNodeWithText(obsoleteName).performScrollTo().performClick()
        waitPhotoColor(Color.RED)
        rule.onNodeWithText("Timing").performScrollTo().performClick()
        rule.onNodeWithText("Play once").performClick()
        rule.waitUntil(15_000) { !current.forever }
        val working = current

        photoSave()
        rule.onNode(hasSetTextAction()).performTextReplacement("New GIF recipe")
        rule.onNodeWithText("Save").performClick()
        rule.onNodeWithText("The saved GIF project list is full", substring = true).assertIsDisplayed()
        rule.onNodeWithText("Cancel").performClick()

        val key = Base64.getUrlEncoder().withoutPadding().encodeToString(obsoleteName.toByteArray(Charsets.UTF_8))
        val base = File(storage.filesDir, "gz_gif_projects/$key.gzgif")
        val backup = File(base.path + ".bak")
        val blocked = File(base.path + ".new")
        assertTrue(base.renameTo(backup))
        assertTrue(blocked.mkdir())
        rule.onNodeWithText("Photos").performScrollTo().performClick()
        rule.onNodeWithText("Open saved").performScrollTo().performClick()
        fun deleteObsolete() {
            rule.onNodeWithContentDescription("Delete $obsoleteName").performScrollTo().performClick()
            rule.onNodeWithText("Delete").performClick()
        }
        deleteObsolete()
        rule.onNodeWithText("Could not delete this GIF project.").assertIsDisplayed()
        assertTrue(backup.isFile)
        assertTrue(blocked.delete())

        deleteObsolete()
        rule.onNodeWithText(obsoleteName).assertDoesNotExist()
        rule.onNodeWithText("Close").performClick()
        assertFalse(backup.exists())
        assertNull(GifStore.load(storage, obsoleteName))
        assertEquals(working, current)
        waitPhotoColor(Color.RED)
        photoSave()
        assertEquals("", draftName())
        submitName("New GIF recipe")

        assertEquals(working, GifStore.load(storage, "New GIF recipe"))
        for ((name, doc) in protected) assertEquals(name, doc, GifStore.load(storage, name))
        assertEquals(12, GifStore.names(storage).size)
        restartWorkspace()
        videoOpen("New GIF recipe")
        waitPhotoColor(Color.RED)
        assertEquals(working, current)
    }

    private val media = mutableListOf<Uri>()

    private fun withProjects(test: (Context) -> Unit) {
        val folder = Files.createTempDirectory(target.cacheDir.toPath(), "gz-gif-identities-").toFile()
        try {
            test(isolate(target, folder))
        } finally {
            media.forEach { resolver.delete(it, null, null) }
            folder.deleteRecursively()
        }
    }

    private fun isolate(base: Context, folder: File): Context =
        object : ContextWrapper(base) { override fun getFilesDir(): File = folder }

    private fun mount(
        folder: File,
        initial: GifDocument = GifDocument(),
        chooseVideo: () -> Uri? = { null }
    ) {
        current = initial
        val owner = object : ActivityResultRegistryOwner {
            override val activityResultRegistry = object : ActivityResultRegistry() {
                override fun <I, O> onLaunch(
                    requestCode: Int,
                    contract: ActivityResultContract<I, O>,
                    input: I,
                    options: ActivityOptionsCompat?
                ) {
                    check(contract is ActivityResultContracts.PickVisualMedia)
                    val chosen = requireNotNull(chooseVideo())
                    dispatchResult(requestCode, Activity.RESULT_OK, Intent().setData(chosen))
                }
            }
        }
        rule.setContent {
            val base = LocalContext.current
            val isolated = remember(base) { isolate(base, folder) }
            CompositionLocalProvider(
                LocalContext provides isolated,
                LocalActivityResultRegistryOwner provides owner
            ) {
                key(workspaceGeneration) {
                    var document by remember {
                        mutableStateOf(if (workspaceGeneration == 0) initial else GifDocument())
                    }
                    GifMakerScreen(document, { document = it; current = it }, {}, {})
                }
            }
        }
    }

    private fun restartWorkspace() {
        rule.runOnIdle {
            current = GifDocument()
            workspaceGeneration++
        }
        rule.onNodeWithText("Select one video to turn into a GIF").assertIsDisplayed()
    }

    private fun video(asset: String): Uri = publish(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, asset, "video/mp4"
    ) { out -> instrumentation.context.assets.open(asset).use { it.copyTo(out) } }

    private fun photo(name: String, marker: Int): Uri = publish(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, name, "image/png"
    ) { out ->
        val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        try {
            bitmap.eraseColor(marker)
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out))
        } finally {
            bitmap.recycle()
        }
    }

    private fun publish(collection: Uri, name: String, mime: String, write: (OutputStream) -> Unit): Uri {
        val metadata = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = requireNotNull(resolver.insert(collection, metadata))
        media.add(uri)
        requireNotNull(resolver.openOutputStream(uri)).use(write)
        resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
        return uri
    }

    private fun awaitText(text: String) {
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun videoFile(item: String) {
        rule.onNodeWithText("File ▾").performClick()
        rule.onNodeWithText(item).performClick()
    }

    private fun videoOpen(name: String) {
        if (current.video == null) rule.onNodeWithText("Open saved GIF project").performClick()
        else videoFile("Open saved GIF project")
        rule.onNodeWithText(name).performScrollTo().performClick()
    }

    private fun videoSave() = videoFile("Save project")

    private fun photoOpen(name: String) {
        rule.onNodeWithText("Open saved").performScrollTo().performClick()
        rule.onNodeWithText(name).performScrollTo().performClick()
    }

    private fun photoSave() {
        rule.onNodeWithText("Projects").performScrollTo().performClick()
        rule.onNodeWithText("Save project").performScrollTo().performClick()
    }

    private fun draftName(): String =
        rule.onNode(hasSetTextAction()).fetchSemanticsNode().config[SemanticsProperties.EditableText].text

    private fun submitName(name: String? = null) {
        if (name != null) rule.onNode(hasSetTextAction()).performTextReplacement(name)
        rule.onNodeWithText("Save").performClick()
        rule.waitUntil(15_000) { rule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isEmpty() }
    }

    private fun waitVideo(uri: Uri, reverse: Boolean) {
        rule.waitUntil(15_000) {
            val active = current.video
            active != null && active.uri == uri.toString() && active.reverse == reverse
        }
        awaitText("Play")
        rule.waitUntil(15_000) {
            rule.onAllNodesWithContentDescription("GIF preview, frame 1").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitPhotoColor(expected: Int) {
        val preview = "Animated GIF frame preview"
        rule.waitUntil(15_000) {
            if (rule.onAllNodesWithContentDescription(preview).fetchSemanticsNodes().isEmpty()) {
                false
            } else {
                val image = rule.onNodeWithContentDescription(preview).captureToImage().asAndroidBitmap()
                image.getPixel(image.width / 2, image.height / 2) == expected
            }
        }
    }
}

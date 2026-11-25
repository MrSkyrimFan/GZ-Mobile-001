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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.CollageDocument
import com.greenezone.gzphotoeditor.editor.CollagePhoto
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.ProjectSaveIntent
import com.greenezone.gzphotoeditor.editor.ProjectSaveResult
import com.greenezone.gzphotoeditor.editor.ProjectStore
import com.greenezone.gzphotoeditor.ui.CollageWorkspaceScreenContent
import com.greenezone.gzphotoeditor.ui.CollageWorkspaceSession
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

/** Actual picker routes plus the named publication boundary used by nested photo saves. */
@RunWith(AndroidJUnit4::class)
class AndroidCollagePickerOwnershipTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val target get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val resolver get() = target.contentResolver
    private val media = mutableListOf<Uri>()
    @Volatile private var current = CollageDocument()

    @Test fun oldAddPhotosResultCannotConsumeTheNextPickerOrAddTwice() = isolated { storage ->
        val original = photo("gz-collage-picker-original.png", Color.RED)
        val obsolete = photo("gz-collage-picker-obsolete.png", Color.GREEN)
        val chosen = photo("gz-collage-picker-chosen.png", Color.BLUE)
        val originals = media.associateWith(::bytes)
        val initial = CollageDocument(photos = listOf(CollagePhoto(original.toString())))
        val picker = DelayedPicker()
        mount(storage.filesDir, initial, picker)

        option("Add photos…")
        awaitLaunches(picker, 1)
        deliver(picker, picker.codes[0], null)
        assertEquals(initial, current)
        option("Add photos…")
        awaitLaunches(picker, 2)
        assertNotEquals("Each logical picker owns a distinct registration", picker.codes[0], picker.codes[1])
        deliver(picker, picker.codes[0], obsolete)
        deliver(picker, picker.codes[0], null)
        assertEquals("Old success and cancellation must leave B pending", initial, current)

        deliver(picker, picker.codes[1], chosen)
        rule.waitUntil(15_000) { current.photos.size == 2 }
        assertEquals(listOf(original.toString(), chosen.toString()), current.photos.map { it.uri })
        val completed = current
        deliver(picker, picker.codes[1], obsolete)
        assertEquals("Duplicate completion must not import another photo", completed, current)
        rule.onNodeWithContentDescription("Undo").performClick()
        rule.runOnIdle { assertEquals(initial, current) }
        originals.forEach { (uri, expected) -> assertArrayEquals(expected, bytes(uri)) }
    }

    @Test fun oldReplacementResultCannotConsumeTheNextTileChoiceOrResetItsEdits() = isolated { storage ->
        val original = photo("gz-collage-tile-original.png", Color.RED)
        val second = photo("gz-collage-tile-second.png", Color.YELLOW)
        val obsolete = photo("gz-collage-tile-obsolete.png", Color.GREEN)
        val chosen = photo("gz-collage-tile-chosen.png", Color.BLUE)
        val originals = media.associateWith(::bytes)
        val initial = CollageDocument(photos = listOf(
            CollagePhoto(original.toString(), EditDocument(mirrored = true)),
            CollagePhoto(second.toString(), EditDocument(quarterTurns = 1))))
        val picker = DelayedPicker()
        mount(storage.filesDir, initial, picker)
        rule.onNodeWithText("Photos", substring = false).performScrollTo().performClick()
        replaceSelected()
        awaitLaunches(picker, 1)
        deliver(picker, picker.codes[0], null)
        assertEquals(initial, current)

        replaceSelected()
        awaitLaunches(picker, 2)
        assertNotEquals(picker.codes[0], picker.codes[1])
        deliver(picker, picker.codes[0], obsolete)
        deliver(picker, picker.codes[0], null)
        assertEquals("A late result cannot reset B's tile edits", initial, current)
        deliver(picker, picker.codes[1], chosen)
        rule.waitUntil(15_000) { current.photos[0].uri == chosen.toString() }
        assertEquals(initial.photos[0].id, current.photos[0].id)
        assertEquals(EditDocument(), current.photos[0].edits)
        assertEquals("Other tiles remain independent", initial.photos[1], current.photos[1])
        val completed = current
        deliver(picker, picker.codes[1], obsolete)
        assertEquals(completed, current)
        rule.onNodeWithContentDescription("Undo").performClick()
        rule.runOnIdle { assertEquals("Undo restores the complete old tile", initial, current) }
        originals.forEach { (uri, expected) -> assertArrayEquals(expected, bytes(uri)) }
    }

    @Test fun failedPhotoPublicationReadbackRestoresThePreviousNamedRecipeAndReference() = isolated { storage ->
        val source = photo("gz-photo-publication-source.png", Color.RED)
        val sourceBytes = bytes(source)
        val original = EditDocument()
        val saved = ProjectStore.saveNamed(storage, ProjectSaveIntent.Create("Publication"),
            source, original) as ProjectSaveResult.Saved
        val prefs = storage.preferences
        val previousPayload = prefs.delegate.all
        prefs.failAfterNextCommit(blockRollback = false)

        val rejected = ProjectStore.saveNamed(storage, ProjectSaveIntent.Update(saved.project),
            source, original.copy(mirrored = true))
        assertTrue("Post-publication readback failure must be reported", rejected is ProjectSaveResult.Rejected)
        assertEquals(previousPayload, prefs.delegate.all)
        val reloaded = requireNotNull(ProjectStore.loadNamed(storage.freshContext(), "Publication"))
        assertEquals(original, reloaded.content.document)
        assertEquals(saved.project, reloaded.project)
        val retry = ProjectStore.saveNamed(storage, ProjectSaveIntent.Update(saved.project),
            source, original.copy(mirrored = true)) as ProjectSaveResult.Saved
        assertEquals(saved.project.id, retry.project.id)
        assertNotEquals(saved.project.version, retry.project.version)
        assertArrayEquals(sourceBytes, bytes(source))
    }

    @Test fun failedPhotoRollbackIsRecoveredBeforeAFreshReaderCanExposeRejectedBytes() = isolated { storage ->
        val source = photo("gz-photo-recovery-source.png", Color.BLUE)
        val original = EditDocument(quarterTurns = 1)
        val saved = ProjectStore.saveNamed(storage, ProjectSaveIntent.Create("Recoverable"),
            source, original) as ProjectSaveResult.Saved
        val prefs = storage.preferences
        val previousPayload = prefs.delegate.all
        prefs.failAfterNextCommit(blockRollback = true)

        val rejected = ProjectStore.saveNamed(storage, ProjectSaveIntent.Update(saved.project),
            source, original.copy(mirrored = true))
        assertTrue(rejected is ProjectSaveResult.Rejected)
        assertNotEquals("The injected failure leaves the rejected bytes in the backing preferences",
            previousPayload, prefs.delegate.all)
        assertNull("A blocked rollback must not expose rejected content", ProjectStore.loadNamed(storage, "Recoverable"))
        assertTrue(ProjectStore.load(storage).isEmpty())

        val fresh = storage.freshContext()
        val recovered = requireNotNull(ProjectStore.loadNamed(fresh, "Recoverable"))
        assertEquals(original, recovered.content.document)
        assertEquals(saved.project, recovered.project)
        assertEquals(previousPayload, prefs.delegate.all)
        assertEquals(listOf(recovered.content), ProjectStore.load(fresh))
    }

    private class FaultPreferences(val delegate: SharedPreferences) : SharedPreferences by delegate {
        private var armed = false
        private var fired = false
        private var blockRollback = false
        private var failRead = false
        fun failAfterNextCommit(blockRollback: Boolean) {
            armed = true
            fired = false
            this.blockRollback = blockRollback
        }
        override fun getString(key: String?, defValue: String?): String? {
            if (failRead) {
                failRead = false
                throw IOException("Injected readback failure after successful publication")
            }
            return delegate.getString(key, defValue)
        }
        override fun edit(): SharedPreferences.Editor {
            val raw = delegate.edit()
            return object : SharedPreferences.Editor by raw {
                override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                    raw.putString(key, value)
                    return this
                }
                override fun remove(key: String?): SharedPreferences.Editor {
                    raw.remove(key)
                    return this
                }
                override fun commit(): Boolean {
                    if (fired && blockRollback) return false
                    val result = raw.commit()
                    if (armed) {
                        check(result)
                        armed = false
                        fired = true
                        failRead = true
                    }
                    return result
                }
            }
        }
    }

    private class Storage(
        private val base: Context, private val folder: File,
        val preferences: FaultPreferences
    ) : ContextWrapper(base) {
        override fun getFilesDir(): File = folder
        override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences = preferences
        fun freshContext(): Context = object : ContextWrapper(base) {
            override fun getFilesDir(): File = folder
            override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences = preferences.delegate
        }
    }

    private class DelayedPicker : ActivityResultRegistryOwner {
        val codes = CopyOnWriteArrayList<Int>()
        override val activityResultRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int, contract: ActivityResultContract<I, O>, input: I, options: ActivityOptionsCompat?
            ) {
                check(contract is ActivityResultContracts.PickMultipleVisualMedia ||
                    contract is ActivityResultContracts.PickVisualMedia)
                codes += requestCode
            }
        }
        fun deliver(code: Int, uri: Uri?) {
            activityResultRegistry.dispatchResult(code,
                if (uri == null) Activity.RESULT_CANCELED else Activity.RESULT_OK,
                uri?.let { Intent().setData(it) })
        }
    }

    private fun mount(folder: File, initial: CollageDocument, picker: DelayedPicker) {
        current = initial
        rule.setContent {
            val base = LocalContext.current
            val context = remember(base, folder) {
                object : ContextWrapper(base) { override fun getFilesDir(): File = folder }
            }
            CompositionLocalProvider(LocalContext provides context, LocalActivityResultRegistryOwner provides picker) {
                MaterialTheme(colorScheme = darkColorScheme()) {
                    var document by remember { mutableStateOf(initial) }
                    var session by remember { mutableStateOf(CollageWorkspaceSession()) }
                    CollageWorkspaceScreenContent(document,
                        onDocument = { document = it; current = it }, onBack = {}, onSwitch = {},
                        session = session, onSession = { session = it })
                }
            }
        }
        rule.waitForIdle()
    }

    private fun option(label: String) {
        rule.onNodeWithText("Options").performClick()
        rule.onNodeWithText(label).performClick()
    }
    private fun replaceSelected() {
        rule.onNodeWithText("Replace", substring = false).performScrollTo().performClick()
        rule.onNodeWithText("Choose photo").performClick()
    }
    private fun awaitLaunches(picker: DelayedPicker, count: Int) {
        rule.waitUntil(15_000) { picker.codes.size == count }
    }
    private fun deliver(picker: DelayedPicker, code: Int, uri: Uri?) {
        rule.runOnIdle { picker.deliver(code, uri) }
        rule.waitForIdle()
    }

    private fun photo(name: String, color: Int): Uri {
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }))
        media += uri
        val image = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        try {
            image.eraseColor(color)
            requireNotNull(resolver.openOutputStream(uri)).use {
                check(image.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
        } finally { image.recycle() }
        resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
        return uri
    }
    private fun bytes(uri: Uri) = requireNotNull(resolver.openInputStream(uri)).use { it.readBytes() }

    private fun isolated(test: (Storage) -> Unit) {
        val folder = Files.createTempDirectory(target.cacheDir.toPath(), "gz-collage-picker-").toFile()
        val preferenceName = "gz-collage-picker-" + UUID.randomUUID().toString()
        val storage = Storage(target, folder,
            FaultPreferences(target.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)))
        try { test(storage) } finally {
            rule.runOnIdle { rule.activity.setContentView(android.widget.FrameLayout(rule.activity)) }
            media.forEach { resolver.delete(it, null, null) }
            target.deleteSharedPreferences(preferenceName)
            folder.deleteRecursively()
        }
    }
}

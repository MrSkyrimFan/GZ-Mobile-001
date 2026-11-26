package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.*
import com.greenezone.gzphotoeditor.ui.ProWorkspaceScreenContent
import com.greenezone.gzphotoeditor.ui.ProWorkspaceSession
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidU1ReviewProShortcutsTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val resolver get() = context.contentResolver
    private val canvas get() = rule.onNodeWithTag("pro-canvas")
    private lateinit var document: ProDocument
    private lateinit var session: ProWorkspaceSession

    @Test fun layersMaskShortcutEditsTheNativeTargetFromFinishedOutput() = withLinkedProject { initial, name ->
        tapText("Select / mask", scroll = true)
        waitForPreview()
        rule.onNodeWithText("Apply layer mask").assertIsNotEnabled()
        assertState(initial, undo = 0, redo = 0)

        canvas.performTouchInput {
            val radius = minOf(width, height) * .20f
            swipe(center - Offset(radius, radius), center + Offset(radius, radius))
        }
        assertState(initial, undo = 0, redo = 0)
        rule.onNodeWithText("Apply layer mask").performScrollTo().assertIsEnabled()
        tapText("Apply layer mask")
        val edited = rule.runOnIdle {
            val mask = requireNotNull(document.layers[1].mask)
            assertEquals(.40f, mask.width, .04f)
            assertEquals(.40f, mask.height, .04f)
            initial.copy(layers = listOf(initial.layers[0], initial.layers[1].copy(mask = mask)))
        }
        assertHistory(initial, edited)
        saveAndReopen(name, edited)
        export { image ->
            assertEquals(0, Color.alpha(image.getPixel(12, 32)))
            assertEquals(255, Color.alpha(image.getPixel(31, 32)))
            assertEquals(0, Color.alpha(image.getPixel(52, 32)))
        }
    }

    @Test fun layersRetouchShortcutEditsTheNativeTargetFromFinishedOutput() = withLinkedProject { initial, name ->
        tapText("Clone / repair", scroll = true)
        waitForPreview()
        rule.onNodeWithText("Reset source").assertIsNotEnabled()
        assertState(initial, undo = 0, redo = 0)

        canvas.performTouchInput { longClick(center + Offset(minOf(width, height) * .22f, 0f)) }
        rule.onNodeWithText("Reset source").assertIsEnabled()
        assertState(initial, undo = 0, redo = 0)
        canvas.performTouchInput { longClick(center - Offset(minOf(width, height) * .22f, 0f)) }
        val edited = rule.runOnIdle {
            val mark = document.layers[1].retouches.single()
            assertEquals(.72f, mark.sourceX, .035f)
            assertEquals(.28f, mark.targetX, .035f)
            assertEquals(.50f, mark.sourceY, .035f)
            assertEquals(.50f, mark.targetY, .035f)
            initial.copy(layers = listOf(initial.layers[0], initial.layers[1].copy(retouches = listOf(mark))))
        }
        assertHistory(initial, edited)
        saveAndReopen(name, edited)
        export { image ->
            // Regular mirrors the output. Native red on the right remains on the finished left.
            assertEquals(Color.RED, image.getPixel(18, 32))
            assertEquals(Color.BLUE, image.getPixel(55, 32))
            val repaired = image.getPixel(46, 32)
            assertTrue("The mirrored destination must acquire red from the source", Color.red(repaired) > 40)
            assertTrue("The neighboring blue material must differ from the repair", Color.blue(repaired) < 215)
        }
    }

    private fun assertState(expected: ProDocument, undo: Int, redo: Int) = rule.runOnIdle {
        assertEquals(expected, document)
        assertEquals(1, session.selectedLayer)
        assertEquals(undo, session.history.undo.size)
        assertEquals(redo, session.history.redo.size)
    }

    private fun assertHistory(initial: ProDocument, edited: ProDocument) {
        assertState(edited, undo = 1, redo = 0)
        rule.onNodeWithContentDescription("Undo", substring = false)
            .assertIsEnabled().performTouchInput { click() }
        assertState(initial, undo = 0, redo = 1)
        rule.onNodeWithContentDescription("Redo", substring = false)
            .assertIsEnabled().performTouchInput { click() }
        assertState(edited, undo = 1, redo = 0)
    }

    private fun saveAndReopen(name: String, expected: ProDocument) {
        tapText("Options")
        tapText("Save project")
        rule.onNode(hasSetTextAction()).performTextReplacement(name)
        tapText("Save")
        waitText("Editable Pro project saved")

        val saved = requireNotNull(ProStore.loadNamed(context, name))
        assertEquals(expected, saved.content.document)
        assertNotNull(saved.content.linkedWork)

        tapText("Options")
        tapText("Open project…")
        waitText(name)
        tapText(name, scroll = true)
        waitText("Pro project opened")
        rule.runOnIdle { assertEquals(expected, document) }
        rule.onNodeWithContentDescription("Pro composition preview", substring = false).assertIsDisplayed()
    }

    private fun export(checkPixels: (Bitmap) -> Unit) {
        tapText("Options")
        tapText("Export image…")
        tapText("JPEG · JPG  ▾")
        tapText("PNG · Lossless")
        tapText("Save PNG")
        waitText("Exported 64 × 64.", substring = true)
        val bitmap = requireNotNull(resolver.openInputStream(output)?.use { BitmapFactory.decodeStream(it) })
        try {
            assertEquals(64, bitmap.width)
            assertEquals(64, bitmap.height)
            checkPixels(bitmap)
        } finally {
            bitmap.recycle()
        }
    }

    private lateinit var output: Uri

    private fun withLinkedProject(check: (ProDocument, String) -> Unit) = runBlocking {
        val name = "U1 linked " + System.nanoTime().toString().takeLast(12)
        val store = NativeWorkStore(context)
        val journal = PendingExportJournal(context.filesDir)
        assertNull("This fixture needs a free Pro export slot", journal.read(ExportWorkspace.PRO))
        val source = mediaRow("gz-u1-shortcut-source")
        try {
            output = mediaRow("gz-u1-shortcut-output")
            try {
                val size = ProPixelSize(64, 64)
                val pixels = IntArray(64 * 64) { index -> if (index % 64 < 32) Color.BLUE else Color.RED }
                val bitmap = Bitmap.createBitmap(pixels, 64, 64, Bitmap.Config.ARGB_8888)
                try {
                    requireNotNull(resolver.openOutputStream(source)).use {
                        assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
                    }
                } finally {
                    bitmap.recycle()
                }
                assertEquals(1, resolver.update(source, ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }, null, null))
                val originalBytes = sourceBytes(source)
                val target = ProImageGeometry.recordImport(
                    ProLayer(source.toString(), name = "Shortcut target"), size, placementId = "shortcut-target")
                val sibling = ProImageGeometry.recordImport(
                    ProLayer(source.toString(), name = "Untouched sibling", visible = false),
                    size, placementId = "shortcut-sibling")
                val base = ProDocument(listOf(sibling, target),
                    canvasSize = size, background = ProBackground.TRANSPARENT)
                val derivative = ok(store.stageDerivative(base))
                try {
                    val mirror = EditDocument(mirrored = true)
                    val finish = ok(store.stageFinish(base, derivative, mirror))
                    try {
                        val initial = base.copy(linkedFinishes = listOf(finish))
                        val regular = NativeRegularState("shortcut-regular", derivative.sha, 0, 1,
                            derivative.uri, mirror, EditDocument(), mirror, EditDocument(), EditHistory(), null)
                        val link = NativeWorkLink(derivative = derivative, regularInput = size)
                        open(initial, store, regular, link)
                        rule.onNodeWithContentDescription("Pro composition preview", substring = false)
                            .assertIsDisplayed()
                        waitForPreview()
                        check(initial, name)
                        assertArrayEquals(originalBytes, sourceBytes(source))
                    } finally {
                        store.release(finish)
                    }
                } finally {
                    store.release(derivative)
                }
            } finally {
                journal.read(ExportWorkspace.PRO)?.let { journal.discard(it) }
                ProStore.delete(context, name)
                resolver.delete(output, null, null)
            }
        } finally {
            resolver.delete(source, null, null)
        }
        Unit
    }

    private fun open(
        initial: ProDocument,
        store: NativeWorkStore,
        regular: NativeRegularState,
        link: NativeWorkLink
    ) {
        document = initial
        val initialSession = ProWorkspaceSession(
            resetDocument = initial, selectedLayer = 1, documentEstablished = true)
        session = initialSession
        rule.setContent {
            var current by remember { mutableStateOf(initial) }
            var currentSession by remember { mutableStateOf(initialSession) }
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides outputPicker()) {
                MaterialTheme {
                    ProWorkspaceScreenContent(
                        document = current, onDocument = { current = it; document = it },
                        onBack = {}, onSwitch = {},
                        session = currentSession, onSession = { currentSession = it; session = it },
                        saveCapturedProject = { intent, captured, state ->
                            val pro = NativeProState(state.ownerId, captured, state.documentEstablished,
                                state.contentRevision, state.sourceRevision, state.selectedLayer, state.saveTarget,
                                state.nextWorkId, state.entryDocument, state.resetDocument, state.baselines,
                                state.history)
                            val prepared = ok(store.prepare(NativeWorkState("shortcut-root",
                                NativeWorkMode.PRO, regular, pro, link)))
                            try { store.saveNamed(intent, prepared) }
                            finally { store.release(prepared) }
                        },
                        prepareOpenedNamedProject = { saved ->
                            val opened = ok(store.loadNamed(requireNotNull(saved.content.linkedWork), saved.project))
                            assertEquals(saved.content.document, opened.pro?.document)
                            assertEquals(regular.document, opened.regular?.document)
                            assertEquals(link.derivative, opened.link?.derivative)
                            true
                        }
                    )
                }
            }
        }
    }

    private fun waitForPreview() {
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText("Preparing preview…", substring = false).fetchSemanticsNodes().isEmpty()
        }
    }

    private fun waitText(label: String, substring: Boolean = false) {
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText(label, substring = substring).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun tapText(label: String, scroll: Boolean = false) {
        val node = rule.onNodeWithText(label, substring = false)
        if (scroll) node.performScrollTo()
        node.assertIsDisplayed().performTouchInput { click(center) }
    }

    private fun sourceBytes(uri: Uri) = requireNotNull(resolver.openInputStream(uri)).use { it.readBytes() }

    private fun mediaRow(label: String): Uri =
        requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, label + "-" + System.nanoTime() + ".png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }))

    private fun outputPicker(): ActivityResultRegistryOwner = object : ActivityResultRegistryOwner {
        override val activityResultRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(requestCode: Int, contract: ActivityResultContract<I, O>, input: I,
                options: ActivityOptionsCompat?) {
                check(contract is ActivityResultContracts.StartActivityForResult)
                assertEquals(Intent.ACTION_CREATE_DOCUMENT, (input as Intent).action)
                dispatchResult(requestCode, Activity.RESULT_OK, Intent().setData(output))
            }
        }
    }

    private fun <T> ok(result: NativeWorkResult<T>): T = when (result) {
        is NativeWorkResult.Ok -> result.value
        is NativeWorkResult.Failed -> error("Native shortcut fixture failed: " + result.reason)
    }
}

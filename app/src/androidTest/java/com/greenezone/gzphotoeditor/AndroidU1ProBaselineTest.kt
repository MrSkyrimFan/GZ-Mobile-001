package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.compose.ui.graphics.asAndroidBitmap
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.CreativeDocument
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.ExportWorkspace
import com.greenezone.gzphotoeditor.editor.NamedProjectRef
import com.greenezone.gzphotoeditor.editor.NativeWorkResult
import com.greenezone.gzphotoeditor.editor.NativeWorkStore
import com.greenezone.gzphotoeditor.editor.PendingExportJournal
import com.greenezone.gzphotoeditor.editor.ProBackground
import com.greenezone.gzphotoeditor.editor.ProBlend
import com.greenezone.gzphotoeditor.editor.ProDocument
import com.greenezone.gzphotoeditor.editor.ProLayer
import com.greenezone.gzphotoeditor.editor.ProLayerKind
import com.greenezone.gzphotoeditor.editor.ProPixelEffect
import com.greenezone.gzphotoeditor.editor.ProPixelSize
import com.greenezone.gzphotoeditor.editor.ProStore
import com.greenezone.gzphotoeditor.editor.ShapeOverlay
import com.greenezone.gzphotoeditor.ui.ProWorkspaceScreenContent
import com.greenezone.gzphotoeditor.ui.ProWorkspaceSession
import com.greenezone.gzphotoeditor.ui.saved
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import kotlin.math.abs
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Actual pointer coverage for the owner-reported Pro paths. */
@RunWith(AndroidJUnit4::class)
class AndroidU1ProBaselineTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val resolver get() = context.contentResolver
    private lateinit var document: ProDocument
    private lateinit var session: ProWorkspaceSession

    @Test fun transparentBottomLayerAcceptsBlendThroughTheActualPanel() {
        val selected = shape("Selected bottom")
        val initial = ProDocument(listOf(selected), background = ProBackground.TRANSPARENT)
        open(initial)
        chooseMultiply()
        rule.runOnIdle {
            assertEquals(0, session.selectedLayer)
            assertEquals("A transparent backdrop must not block selecting Multiply",
                selected.copy(blend = ProBlend.MULTIPLY), document.layers.single())
            assertEquals(1, session.history.undo.size)
        }
    }

    @Test fun hiddenLayerAcceptsBlendThroughTheActualPanel() {
        val selected = shape("Selected hidden").copy(visible = false)
        open(ProDocument(listOf(selected), background = ProBackground.WHITE))
        chooseMultiply()
        rule.runOnIdle {
            assertEquals(0, session.selectedLayer)
            assertEquals("A hidden layer must retain the selected blend for its next preview",
                selected.copy(blend = ProBlend.MULTIPLY), document.layers.single())
            assertEquals(1, session.history.undo.size)
        }
    }

    @Test fun optionsDeleteClearsWorkingSessionWhileKeepingNamedSaveAndSource() {
        val name = "U1 delete " + System.nanoTime().toString().takeLast(12)
        val uri = photo()
        val layer = ProLayer(uri.toString(), name = "Imported photo", sourceSize = ProPixelSize(16, 16))
        val original = ProDocument(listOf(layer))
        val saved = original.copy(layers = listOf(layer.copy(opacity = .55f)))
        val sourceBytes = sourceBytes(uri)
        try {
            assertTrue(ProStore.save(context, name, saved))
            open(original)
            tapText("Hide", scroll = true)
            rule.runOnIdle { assertFalse(document.layers.single().visible) }

            tapText("Options")
            tapText("Delete")
            rule.onNodeWithText("Delete current work?", substring = false).assertIsDisplayed()
            rule.onNode(hasText("Delete", substring = false) and hasAnyAncestor(isDialog()))
                .performTouchInput { click(center) }
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Delete current work?", substring = false)
                    .fetchSemanticsNodes().isEmpty()
            }

            assertEquals(saved, ProStore.load(context, name))
            assertArrayEquals(sourceBytes, sourceBytes(uri))
            rule.runOnIdle {
                assertEquals("Options Delete must clear the working media association",
                    ProDocument(), document)
                assertTrue(session.history.undo.isEmpty())
                assertTrue(session.history.redo.isEmpty())
            }
        } finally {
            ProStore.delete(context, name)
            resolver.delete(uri, null, null)
        }
    }

    @Test fun allSixteenModesAcceptRealTapsWithoutRetargetingOrDuplicateHistory() {
        val lowerUri = photo(Color.rgb(173, 56, 118))
        val topUri = photo(Color.rgb(67, 188, 220))
        try {
            val lower = ProLayer(lowerUri.toString(), name = "Lower image")
            val top = ProLayer(topUri.toString(), name = "Selected upper image")
            open(ProDocument(listOf(lower, top), background = ProBackground.TRANSPARENT), selected = 1)
            tool("Blend")
            rule.onNodeWithText("Selected upper image · Image").assertIsDisplayed()
            tapText("Blend mode: Normal", scroll = true)
            rule.onNodeWithTag("pro-blend-menu").performTouchInput { swipeUp() }
            rule.runOnIdle {
                assertEquals(ProBlend.NORMAL, document.layers[1].blend)
                assertTrue(session.history.undo.isEmpty())
            }
            tapText("Normal", scroll = true)

            val order = ProBlend.entries.drop(1) + ProBlend.NORMAL
            var current = ProBlend.NORMAL
            order.forEachIndexed { index, mode ->
                chooseMode(current, mode)
                rule.runOnIdle {
                    assertEquals(lower, document.layers[0])
                    assertEquals(top.copy(blend = mode), document.layers[1])
                    assertEquals(1, session.selectedLayer)
                    assertEquals(index + 1, session.history.undo.size)
                }
                if (mode == ProBlend.SCREEN) {
                    rule.waitUntil(15_000) { canvasCenterMatches(Color.rgb(195, 203, 236)) }
                }
                current = mode
            }
            chooseMode(ProBlend.NORMAL, ProBlend.NORMAL)
            rule.runOnIdle { assertEquals(order.size, session.history.undo.size) }
            rule.onNodeWithContentDescription("Undo").performTouchInput { click(center) }
            rule.runOnIdle { assertEquals(ProBlend.LUMINOSITY, document.layers[1].blend) }
            rule.onNodeWithContentDescription("Redo").performTouchInput { click(center) }
            rule.runOnIdle { assertEquals(ProBlend.NORMAL, document.layers[1].blend) }

            rule.onNodeWithTag("pro-canvas").performTouchInput { click(center) }
            rule.onNodeWithTag("workspace-tools").assertDoesNotExist()
            rule.onNodeWithTag("pro-canvas").performTouchInput { click(center) }
            rule.onNodeWithTag("workspace-tools").assertIsDisplayed()
            chooseMode(ProBlend.NORMAL, ProBlend.DIFFERENCE)
            rule.runOnIdle { assertEquals(ProBlend.DIFFERENCE, document.layers[1].blend) }
        } finally {
            resolver.delete(topUri, null, null)
            resolver.delete(lowerUri, null, null)
        }
    }

    @Test fun busySaveRejectsQueuedBlendChangesAndNativeReopenExportsSelectedMode() {
        val lowerUri = photo(Color.rgb(173, 56, 118))
        val topUri = photo(Color.rgb(67, 188, 220))
        val destination = photo(Color.GREEN)
        val journal = PendingExportJournal(context.filesDir)
        journal.read(ExportWorkspace.PRO)?.let { journal.discard(it) }
        val name = "U1 blend " + System.nanoTime().toString().takeLast(12)
        try {
            val lower = ProLayer(lowerUri.toString(), name = "Backdrop", sourceSize = ProPixelSize(16, 16))
            val top = ProLayer(topUri.toString(), name = "Blend target", sourceSize = ProPixelSize(16, 16))
            val initial = ProDocument(listOf(lower, top), canvasSize = ProPixelSize(32, 32))
            assertTrue(ProStore.save(context, name, initial))
            val saved = requireNotNull(ProStore.loadNamed(context, name)).project
            val originalSource = sourceBytes(topUri)
            open(initial, selected = 1, saved = saved, registry = exportDestination(destination))
            chooseMultiply()
            val expected = initial.copy(layers = listOf(lower, top.copy(blend = ProBlend.MULTIPLY)))
            tapText("Blend mode: Multiply", scroll = true)
            val callback = requireNotNull(rule.onNodeWithText("Screen", substring = false)
                .performScrollTo().fetchSemanticsNode().config[SemanticsActions.OnClick].action)
            tapText("Multiply", scroll = true)

            synchronized(ProStore) {
                tapText("Options")
                tapText("Save project")
                rule.onNodeWithText("Opening images…", substring = false).assertIsDisplayed()
                rule.onNodeWithText("Blend mode: Multiply", substring = false)
                    .performScrollTo().assertIsNotEnabled().performTouchInput { click(center) }
                rule.runOnIdle {
                    callback()
                    assertEquals(expected, document)
                    assertEquals(1, session.selectedLayer)
                    assertEquals(1, session.history.undo.size)
                }
            }
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Editable Pro project saved", substring = false)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            val receipt = requireNotNull(ProStore.loadNamed(context, name))
            assertEquals(saved.id, receipt.project.id)
            assertNotEquals(saved.version, receipt.project.version)
            assertEquals(expected, receipt.content.document)

            tapText("Options")
            tapText("Open project…")
            rule.waitUntil(15_000) { rule.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty() }
            tapText(name, scroll = true)
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Pro project opened", substring = false)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            rule.runOnIdle { assertEquals(ProBlend.MULTIPLY, document.layers[1].blend) }
            tapText("Options")
            tapText("Export image…")
            tapText("JPEG · JPG  ▾")
            tapText("PNG · Lossless")
            tapText("Save PNG")
            rule.waitUntil(30_000) {
                rule.onAllNodesWithText("Exported 32 × 32.", substring = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            val decoded = requireNotNull(resolver.openInputStream(destination)?.use { BitmapFactory.decodeStream(it) })
            try {
                assertEquals(32, decoded.width)
                assertEquals(32, decoded.height)
                assertTrue(nearColor(Color.rgb(45, 41, 102), decoded.getPixel(16, 16)))
            } finally { decoded.recycle() }
            assertArrayEquals(originalSource, sourceBytes(topUri))
        } finally {
            journal.read(ExportWorkspace.PRO)?.let { journal.discard(it) }
            ProStore.delete(context, name)
            for (uri in listOf(lowerUri, topUri, destination)) resolver.delete(uri, null, null)
        }
    }

    @Test fun paintVectorAndMaskedAdjustmentExposeTheCurrentLayerTargets() {
        val vector = shape("Original shape")
        open(ProDocument(listOf(vector)))
        tool("Paint")
        tapText("Add paint layer", scroll = true)
        rule.runOnIdle {
            assertEquals(ProLayerKind.DRAWING, document.layers.last().kind)
            assertEquals(1, session.selectedLayer)
        }
        rule.onNodeWithTag("pro-canvas").performTouchInput {
            val distance = minOf(width, height) * .2f
            swipe(center - Offset(distance, 0f), center + Offset(distance, 0f))
        }
        val painted = rule.runOnIdle {
            assertEquals(vector, document.layers.first())
            assertEquals(1, document.layers.last().edits.creative.strokes.size)
            document.layers.last()
        }

        tool("Layers")
        tapText("+ Add layer", scroll = true)
        tapText("Shape layer")
        val shapeLayer = rule.runOnIdle {
            assertEquals(ProLayerKind.SHAPE, document.layers.last().kind)
            assertEquals(1, document.layers.last().edits.creative.shapes.size)
            document.layers.last()
        }

        tool("Layers")
        tapText("+ Add layer", scroll = true)
        tapText("Adjustment layer")
        rule.runOnIdle {
            assertEquals(ProLayerKind.ADJUSTMENT, document.layers.last().kind)
            assertEquals(ProPixelEffect.NONE, document.layers.last().effect)
            assertEquals(EditDocument(), document.layers.last().edits)
        }
        tool("Layer effects")
        tapText("Black & white", scroll = true)
        tool("Select & mask")
        rule.onNodeWithTag("pro-canvas").performTouchInput {
            val distance = minOf(width, height) * .15f
            swipe(center - Offset(distance, distance), center + Offset(distance, distance))
        }
        tapText("Apply layer mask", scroll = true)
        rule.runOnIdle {
            assertEquals(listOf(vector, painted, shapeLayer), document.layers.dropLast(1))
            assertEquals(ProPixelEffect.GRAYSCALE, document.layers.last().effect)
            assertNotNull(document.layers.last().mask)
        }
    }

    @Test fun linkedRegularFinishKeepsItsArtboardWhenAnotherAspectIsTapped() {
        val initial = ProDocument(listOf(shape("Bridge shape")), canvasSize = ProPixelSize(48, 48))
        val store = NativeWorkStore(context)
        val linked = runBlocking {
            val derivative = when (val staged = store.stageDerivative(initial)) {
                is NativeWorkResult.Ok -> staged.value
                is NativeWorkResult.Failed -> error("Could not stage the bridge fixture: " + staged.reason)
            }
            val finish = when (val staged = store.stageFinish(initial, derivative, EditDocument(mirrored = true))) {
                is NativeWorkResult.Ok -> staged.value
                is NativeWorkResult.Failed -> error("Could not stage the Regular finish: " + staged.reason)
            }
            initial.copy(linkedFinishes = listOf(finish))
        }
        open(linked)
        tool("Canvas")
        tapText("Portrait 4:5", scroll = true)
        rule.onNodeWithText("Canvas proportions are kept with linked Regular edits.").assertIsDisplayed()
        rule.runOnIdle {
            assertEquals(linked, document)
            assertTrue(session.history.undo.isEmpty())
            assertTrue(session.history.redo.isEmpty())
        }
        tapText("Black", scroll = true)
        rule.runOnIdle {
            assertEquals(linked.copy(background = ProBackground.BLACK), document)
            assertEquals(1, session.history.undo.size)
        }
    }

    private fun exportDestination(destination: Uri): ActivityResultRegistryOwner = object : ActivityResultRegistryOwner {
        override val activityResultRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(requestCode: Int, contract: ActivityResultContract<I, O>, input: I,
                options: ActivityOptionsCompat?) {
                check(contract is ActivityResultContracts.StartActivityForResult)
                assertEquals(Intent.ACTION_CREATE_DOCUMENT, (input as Intent).action)
                dispatchResult(requestCode, Activity.RESULT_OK, Intent().setData(destination))
            }
        }
    }

    private fun canvasCenterMatches(expected: Int): Boolean {
        val image = rule.onNodeWithTag("pro-canvas").captureToImage().asAndroidBitmap()
        return try {
            nearColor(expected, image.getPixel(image.width / 2, image.height / 2))
        } finally { image.recycle() }
    }

    private fun nearColor(expected: Int, actual: Int) =
        abs(Color.red(expected) - Color.red(actual)) <= 3 &&
            abs(Color.green(expected) - Color.green(actual)) <= 3 &&
            abs(Color.blue(expected) - Color.blue(actual)) <= 3

    private fun shape(name: String) = ProLayer(
        uri = "",
        name = name,
        kind = ProLayerKind.SHAPE,
        edits = EditDocument(creative = CreativeDocument(
            shapes = listOf(ShapeOverlay(color = Color.BLUE, filled = true))
        ))
    )

    private fun open(initial: ProDocument, selected: Int = 0, saved: NamedProjectRef? = null,
        registry: ActivityResultRegistryOwner? = null) {
        document = initial
        val initialSession = ProWorkspaceSession(resetDocument = initial, selectedLayer = selected)
        session = if (saved == null) initialSession else initialSession.saved(initial, saved)
        rule.setContent {
            var current by remember { mutableStateOf(initial) }
            var currentSession by remember { mutableStateOf(session) }
            val picker = registry ?: requireNotNull(LocalActivityResultRegistryOwner.current)
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides picker) {
                MaterialTheme(colorScheme = darkColorScheme()) {
                    ProWorkspaceScreenContent(
                        document = current,
                        onDocument = { current = it; document = it },
                        onBack = {},
                        onSwitch = {},
                        session = currentSession,
                        onSession = { currentSession = it; session = it }
                    )
                }
            }
        }
    }

    private fun chooseMultiply() {
        tool("Blend")
        chooseMode(ProBlend.NORMAL, ProBlend.MULTIPLY)
    }

    private fun tool(name: String) {
        rule.onNodeWithTag("workspace-tools").performTouchInput { click(center) }
        rule.onNode(hasText(name, substring = false) and hasAnyAncestor(isDialog()))
            .performScrollTo().performTouchInput { click(center) }
    }

    private fun chooseMode(current: ProBlend, next: ProBlend) {
        tapText("Blend mode: " + current.label, scroll = true)
        tapText(next.label, scroll = true)
        rule.onNodeWithText("Blend mode: " + next.label, substring = false).assertIsDisplayed()
    }

    private fun tapText(label: String, scroll: Boolean = false) {
        val node = rule.onNodeWithText(label, substring = false)
        if (scroll) node.performScrollTo()
        node.assertIsDisplayed().performTouchInput { click(center) }
    }

    private fun sourceBytes(uri: Uri) =
        requireNotNull(resolver.openInputStream(uri)).use { it.readBytes() }

    private fun photo(color: Int = Color.GREEN): Uri {
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "gz-u1-delete-" + System.nanoTime() + ".png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        val bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        try {
            bitmap.eraseColor(color)
            requireNotNull(resolver.openOutputStream(uri)).use {
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
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
}

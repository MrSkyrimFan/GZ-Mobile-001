package com.greenezone.gzphotoeditor

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.CreativeDocument
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.ProBackground
import com.greenezone.gzphotoeditor.editor.ProBlend
import com.greenezone.gzphotoeditor.editor.ProDocument
import com.greenezone.gzphotoeditor.editor.ProLayer
import com.greenezone.gzphotoeditor.editor.ProLayerKind
import com.greenezone.gzphotoeditor.editor.ProPixelSize
import com.greenezone.gzphotoeditor.editor.ProStore
import com.greenezone.gzphotoeditor.editor.ShapeOverlay
import com.greenezone.gzphotoeditor.ui.ProWorkspaceScreenContent
import com.greenezone.gzphotoeditor.ui.ProWorkspaceSession
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Reproduces owner-reported paths on the unmodified Pro workspace. */
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
            tapText("Delete current work…")
            rule.onNodeWithText("Delete current Pro work?", substring = false).assertIsDisplayed()
            tapText("Delete work")
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Delete current Pro work?", substring = false)
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

    private fun shape(name: String) = ProLayer(
        uri = "",
        name = name,
        kind = ProLayerKind.SHAPE,
        edits = EditDocument(creative = CreativeDocument(
            shapes = listOf(ShapeOverlay(color = Color.BLUE, filled = true))
        ))
    )

    private fun open(initial: ProDocument) {
        document = initial
        session = ProWorkspaceSession(resetDocument = initial)
        rule.setContent {
            var current by remember { mutableStateOf(initial) }
            var currentSession by remember { mutableStateOf(session) }
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

    private fun chooseMultiply() {
        tapText("Tools ▾")
        tapText("Blend", scroll = true)
        tapText("Multiply", scroll = true)
    }

    private fun tapText(label: String, scroll: Boolean = false) {
        val node = rule.onNodeWithText(label, substring = false)
        if (scroll) node.performScrollTo()
        node.assertIsDisplayed().performTouchInput { click(center) }
    }

    private fun sourceBytes(uri: Uri) =
        requireNotNull(resolver.openInputStream(uri)).use { it.readBytes() }

    private fun photo(): Uri {
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "gz-u1-delete-" + System.nanoTime() + ".png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }))
        val bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        try {
            bitmap.eraseColor(Color.GREEN)
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

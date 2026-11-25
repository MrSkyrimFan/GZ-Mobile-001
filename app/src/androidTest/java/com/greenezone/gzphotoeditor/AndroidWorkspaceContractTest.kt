package com.greenezone.gzphotoeditor

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.CollageDocument
import com.greenezone.gzphotoeditor.editor.CollagePhoto
import com.greenezone.gzphotoeditor.editor.CollageTemplate
import com.greenezone.gzphotoeditor.ui.CollageWorkspaceScreenContent
import com.greenezone.gzphotoeditor.ui.CollageWorkspaceSession
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Owner F01/F02: exercise the real parent canvas and Back dispatcher. */
@RunWith(AndroidJUnit4::class)
class AndroidWorkspaceContractTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private lateinit var current: CollageDocument
    private var departures = 0

    private fun withCollage(check: (CollageDocument, File, ByteArray) -> Unit) {
        val source = File.createTempFile("gz-collage-contract-", ".png", rule.activity.cacheDir)
        val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        try {
            bitmap.eraseColor(Color.rgb(40, 110, 180))
            source.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        } finally { bitmap.recycle() }
        val initial = CollageDocument(photos = listOf(
            CollagePhoto(Uri.fromFile(source).toString()), CollagePhoto(Uri.fromFile(source).toString())
        ))
        val bytes = source.readBytes()
        try {
            rule.setContent {
                var document by remember { mutableStateOf(initial) }
                var session by remember { mutableStateOf(CollageWorkspaceSession()) }
                current = document
                MaterialTheme {
                    CollageWorkspaceScreenContent(document, { document = it },
                        { departures++ }, { departures++ }, session, { session = it })
                }
            }
            ready()
            check(initial, source, bytes)
        } finally {
            rule.runOnIdle { rule.activity.setContentView(android.widget.FrameLayout(rule.activity)) }
            source.delete()
        }
    }

    private fun ready() {
        rule.waitUntil(15_000) {
            rule.onAllNodesWithContentDescription("Photo collage preview").fetchSemanticsNodes().isNotEmpty() &&
                rule.onAllNodesWithText(" Updating…").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test fun singleFreeCanvasTapHidesThenRevealsWithoutEditingOrTouchingSource() = withCollage { initial, source, bytes ->
        rule.onNodeWithContentDescription("Photo collage preview").performTouchInput { click(center) }
        rule.onNodeWithText("GZ Editor Pro").assertDoesNotExist()
        rule.runOnIdle { assertEquals(initial, current) }
        rule.onNodeWithContentDescription("Photo collage preview").performTouchInput { click(center) }
        rule.onNodeWithText("GZ Editor Pro").assertIsDisplayed()
        rule.runOnIdle { assertEquals(initial, current) }
        assertArrayEquals(bytes, source.readBytes())
    }

    @Test fun dirtyCollageBackWaitsForCancelThenDiscardsOnlyWorkingChanges() = withCollage { initial, source, bytes ->
        val replacement = CollageTemplate.entries.first { it != initial.layout.template }
        rule.onNodeWithText(replacement.label).performScrollTo().performClick()
        ready()
        rule.runOnIdle {
            assertNotEquals(initial, current)
            rule.activity.onBackPressedDispatcher.onBackPressed()
        }
        rule.runOnIdle { assertEquals("Dirty collage left without a decision", 0, departures) }
        rule.onNodeWithText("Cancel").performClick()
        rule.runOnIdle {
            assertNotEquals(initial, current)
            assertEquals(0, departures)
            rule.activity.onBackPressedDispatcher.onBackPressed()
        }
        rule.onNodeWithText("Discard").performClick()
        rule.runOnIdle {
            assertEquals(initial, current)
            assertEquals(1, departures)
        }
        assertArrayEquals(bytes, source.readBytes())
    }
}

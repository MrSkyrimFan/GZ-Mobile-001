package com.greenezone.gzphotoeditor

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.CollageDocument
import com.greenezone.gzphotoeditor.editor.CollageLayout
import com.greenezone.gzphotoeditor.editor.CollagePhoto
import com.greenezone.gzphotoeditor.editor.CollageTemplate
import com.greenezone.gzphotoeditor.ui.CollageSection
import com.greenezone.gzphotoeditor.ui.CollageWorkspaceScreenContent
import com.greenezone.gzphotoeditor.ui.CollageWorkspaceSession
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidCollagePointerOwnershipTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test fun boundaryTapUsesDownTileAndMarginTapKeepsSelection() = withCollage {
        val preview = rule.onNodeWithContentDescription("Photo collage preview")
        val canvas = rule.onNodeWithContentDescription("Collage canvas. Tap to hide or show tools.")
        assertSelected(second = true)

        preview.performTouchInput {
            down(center - Offset(2f, 0f))
            moveTo(center + Offset(2f, 0f))
            up()
        }
        rule.onNodeWithTag("workspace-header").assertDoesNotExist()
        canvas.performClick()
        assertSelected(second = false)

        preview.performTouchInput {
            click(center + Offset(minOf(width, height) * .24f, 0f))
        }
        rule.onNodeWithTag("workspace-header").assertDoesNotExist()
        preview.performTouchInput {
            click(center - Offset(minOf(width, height) * .47f, 0f))
        }
        rule.onNodeWithTag("workspace-header").assertIsDisplayed()
        assertSelected(second = true)

        preview.performTouchInput {
            click(center + Offset(minOf(width, height) * .24f, 0f))
        }
        rule.onNodeWithTag("workspace-header").assertDoesNotExist()
        preview.performTouchInput {
            click(center + Offset(minOf(width, height) * .24f, 0f))
        }
        rule.onNodeWithTag("workspace-header").assertIsDisplayed()
        assertSelected(second = true)
    }

    @Test fun dragSecondPointerAndCancellationLeaveChromeAndSelectionIntact() = withCollage {
        val preview = rule.onNodeWithContentDescription("Photo collage preview")
        val header = rule.onNodeWithTag("workspace-header")
        preview.performTouchInput {
            val shift = Offset(minOf(width, height) * .24f, 0f)
            down(center + shift)
            moveTo(center - shift)
            up()
        }
        header.assertIsDisplayed()
        assertSelected(second = true)

        preview.performTouchInput {
            val shift = Offset(minOf(width, height) * .24f, 0f)
            down(0, center - shift)
            down(1, center + shift)
            up(1)
            up(0)
        }
        header.assertIsDisplayed()
        assertSelected(second = true)

        preview.performTouchInput {
            down(center - Offset(minOf(width, height) * .24f, 0f))
            cancel()
        }
        header.assertIsDisplayed()
        assertSelected(second = true)
    }

    private fun assertSelected(second: Boolean) {
        rule.onNodeWithTag("collage-tile-actions").performScrollToNode(hasText("Move left"))
        val moveLeft = rule.onNodeWithText("Move left")
        if (second) moveLeft.assertIsEnabled() else moveLeft.assertIsNotEnabled()
    }

    private fun withCollage(check: () -> Unit) = withCollageTestImage(rule.activity) { image ->
        val source = image.toString()
        val initial = CollageDocument(
            photos = listOf(CollagePhoto(source), CollagePhoto(source)),
            layout = CollageLayout(template = CollageTemplate.SIDE_BY_SIDE, gap = 0f, margin = .08f)
        )
        val resolver = rule.activity.contentResolver
        val originalBytes = requireNotNull(resolver.openInputStream(image)).use { it.readBytes() }
        val mounted = mutableStateOf(true)
        var observed = initial
        rule.setContent {
            if (mounted.value) {
                var document by remember { mutableStateOf(initial) }
                val session = remember {
                    CollageWorkspaceSession(selectedPhoto = 1, section = CollageSection.TILES)
                }
                MaterialTheme {
                    CollageWorkspaceScreenContent(
                        document, { document = it; observed = it }, {}, {},
                        session, {}
                    )
                }
            }
        }
        try {
            rule.waitUntil(15_000) {
                rule.onAllNodesWithContentDescription("Photo collage preview")
                    .fetchSemanticsNodes().isNotEmpty() &&
                    rule.onAllNodesWithText(" Updating…").fetchSemanticsNodes().isEmpty()
            }
            check()
            rule.onNodeWithContentDescription("Undo").assertIsNotEnabled()
            rule.onNodeWithContentDescription("Redo").assertIsNotEnabled()
            rule.runOnIdle { assertEquals(initial, observed) }
            assertArrayEquals(originalBytes, requireNotNull(resolver.openInputStream(image)).use { it.readBytes() })
        } finally {
            rule.runOnIdle { mounted.value = false }
            rule.waitForIdle()
        }
    }
}

internal fun withCollageTestImage(context: Context, check: (Uri) -> Unit) {
    val resolver = context.contentResolver
    val image = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "gz-collage-gesture-" + System.nanoTime() + ".png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }))
    try {
        val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        try {
            bitmap.eraseColor(Color.rgb(36, 92, 143))
            requireNotNull(resolver.openOutputStream(image, "wt")).use {
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
        } finally { bitmap.recycle() }
        resolver.update(image, ContentValues().apply {
            put(MediaStore.Images.Media.IS_PENDING, 0)
        }, null, null)
        check(image)
    } finally {
        resolver.delete(image, null, null)
    }
}

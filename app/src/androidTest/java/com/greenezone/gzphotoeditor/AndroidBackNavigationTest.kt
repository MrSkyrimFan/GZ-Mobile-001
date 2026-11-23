package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.CollageDocument
import com.greenezone.gzphotoeditor.editor.CollagePhoto
import com.greenezone.gzphotoeditor.editor.CropRatio
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.ImagePipeline
import com.greenezone.gzphotoeditor.editor.Looks
import com.greenezone.gzphotoeditor.editor.ProjectStore
import com.greenezone.gzphotoeditor.editor.ProBackground
import com.greenezone.gzphotoeditor.editor.ProDocument
import com.greenezone.gzphotoeditor.editor.ProLayer
import com.greenezone.gzphotoeditor.editor.ProMath
import com.greenezone.gzphotoeditor.editor.ProStore
import com.greenezone.gzphotoeditor.ui.CollageWorkspaceScreen
import com.greenezone.gzphotoeditor.ui.CollageWorkspaceScreenContent
import com.greenezone.gzphotoeditor.ui.CollageWorkspaceSession
import com.greenezone.gzphotoeditor.ui.GifMakerScreenContent
import com.greenezone.gzphotoeditor.ui.GifWorkspaceRoute
import com.greenezone.gzphotoeditor.ui.GifWorkspaceSession
import com.greenezone.gzphotoeditor.ui.PhotoEditorScreen
import com.greenezone.gzphotoeditor.ui.ProViewport
import com.greenezone.gzphotoeditor.ui.ProViewportSemanticsKey
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidBackNavigationTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val resolver get() = instrumentation.targetContext.contentResolver

    private fun createPhoto(
        label: String,
        width: Int = 96,
        height: Int = 64,
        colorAt: ((Int, Int) -> Int)? = null
    ): Uri {
        val uri = requireNotNull(resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$label.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        ))
        val pixels = IntArray(width * height) { index ->
            val x = index % width
            val y = index / width
            colorAt?.invoke(x, y) ?: if (x < width / 2) Color.rgb(24, 72, 120)
            else Color.rgb(232, 184, 72)
        }
        val bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
        try {
            resolver.openOutputStream(uri, "wt")!!.use {
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
            resolver.update(uri, ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }, null, null)
            return uri
        } catch (failure: Throwable) {
            resolver.delete(uri, null, null)
            throw failure
        } finally {
            bitmap.recycle()
        }
    }

    private fun createVideo(label: String): Uri {
        val uri = requireNotNull(resolver.insert(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, "$label.mp4")
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        ))
        try {
            resolver.openOutputStream(uri, "wt")!!.use { output ->
                instrumentation.context.assets.open("gif_phone_60fps_bframes.mp4").use {
                    it.copyTo(output)
                }
            }
            resolver.update(uri, ContentValues().apply {
                put(MediaStore.Video.Media.IS_PENDING, 0)
            }, null, null)
            return uri
        } catch (failure: Throwable) {
            resolver.delete(uri, null, null)
            throw failure
        }
    }

    private fun visibleGifFrameLabel(): String {
        return rule.onAllNodesWithText("Frame ", substring = true)
            .fetchSemanticsNodes()
            .flatMap { node ->
                if (SemanticsProperties.Text in node.config) {
                    node.config[SemanticsProperties.Text].map { it.text }
                } else emptyList()
            }
            .single { it.startsWith("Frame ") && " of " in it }
    }

    private fun pressBackThroughDispatcher() {
        rule.runOnIdle {
            rule.activity.onBackPressedDispatcher.onBackPressed()
        }
        rule.waitForIdle()
    }

    private fun waitForTextOrTimeout(text: String, timeoutMillis: Long): Boolean = try {
        rule.waitUntil(timeoutMillis) {
            rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
        true
    } catch (_: ComposeTimeoutException) {
        false
    }

    private fun singlePhotoPicker(uri: Uri): ActivityResultRegistryOwner =
        object : ActivityResultRegistryOwner {
            override val activityResultRegistry = object : ActivityResultRegistry() {
                override fun <I, O> onLaunch(
                    requestCode: Int,
                    contract: ActivityResultContract<I, O>,
                    input: I,
                    options: ActivityOptionsCompat?
                ) {
                    check(contract is ActivityResultContracts.PickVisualMedia)
                    dispatchResult(requestCode, Activity.RESULT_OK, Intent().setData(uri))
                }
            }
        }

    private fun withLoadedRegular(
        width: Int = 96,
        height: Int = 64,
        colorAt: ((Int, Int) -> Int)? = null,
        test: (Uri, ByteArray) -> Unit
    ) {
        val uri = createPhoto("gz-back-regular-" + System.nanoTime(), width, height, colorAt)
        val mounted = mutableStateOf(true)
        var contentSet = false
        try {
            val original = resolver.openInputStream(uri)!!.use { it.readBytes() }
            val picker = singlePhotoPicker(uri)
            rule.setContent {
                CompositionLocalProvider(LocalActivityResultRegistryOwner provides picker) {
                    MaterialTheme(colorScheme = darkColorScheme()) {
                        if (mounted.value) PhotoEditorScreen()
                    }
                }
            }
            contentSet = true
            rule.onNodeWithText("Open media").performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithTag("photo-tools").fetchSemanticsNodes().isNotEmpty()
            }
            test(uri, original)
        } finally {
            if (contentSet) {
                rule.runOnIdle { mounted.value = false }
                rule.waitForIdle()
            }
            resolver.delete(uri, null, null)
        }
    }

    private fun requestProHandoff() {
        rule.onNodeWithContentDescription("Choose editing workspace").performClick()
        rule.onNodeWithText("Photo editing Pro").performClick()
        rule.onNodeWithText("Continue editing in Photo Pro?").assertIsDisplayed()
    }

    private fun acceptProHandoff() {
        rule.onNodeWithText("Yes, continue").performClick()
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText("Photo Pro ▾").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun photoSample(node: SemanticsNodeInteraction): Int {
        val frame = node.captureToImage().asAndroidBitmap()
        val fittedWidth = minOf(frame.width, frame.height * 3 / 2)
        val sampleX = (frame.width - fittedWidth) / 2 + fittedWidth * 3 / 4
        return frame.getPixel(sampleX, frame.height / 2)
    }

    private fun pixelDistance(left: Int, right: Int): Int =
        kotlin.math.abs(Color.red(left) - Color.red(right)) +
            kotlin.math.abs(Color.green(left) - Color.green(right)) +
            kotlin.math.abs(Color.blue(left) - Color.blue(right))

    private fun mountTile(
        uri: Uri,
        onDone: (CollagePhoto) -> Unit,
        onCancel: () -> Unit,
        replacementUri: Uri = uri
    ) {
        val picker = singlePhotoPicker(replacementUri)
        rule.setContent {
            var editing by remember { mutableStateOf(true) }
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides picker) {
                MaterialTheme(colorScheme = darkColorScheme()) {
                    if (editing) {
                        PhotoEditorScreen(
                            initialCollagePhoto = CollagePhoto(uri.toString()),
                            onDoneCollagePhoto = {
                                onDone(it)
                                editing = false
                            },
                            onCancelCollagePhoto = {
                                onCancel()
                                editing = false
                            }
                        )
                    } else {
                        Text("Tile host")
                    }
                }
            }
        }
        rule.waitUntil(15_000) {
            rule.onAllNodesWithTag("photo-tools").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun withSelectedCollageTile(
        initial: CollageDocument,
        replacementUri: Uri? = null,
        onDocument: (CollageDocument) -> Unit,
        test: () -> Unit
    ) {
        val picker = singlePhotoPicker(
            replacementUri ?: Uri.parse(initial.photos[1].uri)
        )
        val mounted = mutableStateOf(true)
        var contentSet = false
        try {
            rule.setContent {
                var document by remember { mutableStateOf(initial) }
                CompositionLocalProvider(LocalActivityResultRegistryOwner provides picker) {
                    MaterialTheme(colorScheme = darkColorScheme()) {
                        if (mounted.value) {
                            CollageWorkspaceScreen(
                                document = document,
                                onDocument = {
                                    document = it
                                    onDocument(it)
                                },
                                onBack = {}
                            )
                        }
                    }
                }
            }
            contentSet = true
            rule.onNodeWithTag("collage-sections").performScrollToNode(hasText("Photos"))
            rule.onNodeWithText("Photos").performClick()
            rule.onNodeWithText("Photo 2").performClick()
            rule.onNodeWithTag("collage-tile-actions")
                .performScrollToNode(hasText("Edit selected"))
            rule.onNodeWithText("Edit selected").performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithTag("photo-tools").fetchSemanticsNodes().isNotEmpty()
            }
            if (replacementUri != null) {
                rule.onNodeWithText("Open media").performClick()
                rule.waitUntil(15_000) {
                    rule.onAllNodesWithText("Media opened").fetchSemanticsNodes().isNotEmpty()
                }
            }
            test()
        } finally {
            if (contentSet) {
                rule.runOnIdle { mounted.value = false }
                rule.waitForIdle()
            }
        }
    }

    private fun withCollageFixtures(test: (Uri, Uri, Uri) -> Unit) {
        val created = mutableListOf<Uri>()
        try {
            val entry = createPhoto("gz-back-parent-entry-" + System.nanoTime())
                .also { created.add(it) }
            val neighbor = createPhoto("gz-back-parent-neighbor-" + System.nanoTime())
                .also { created.add(it) }
            val replacement = createPhoto("gz-back-parent-replacement-" + System.nanoTime())
                .also { created.add(it) }
            test(entry, neighbor, replacement)
        } finally {
            created.forEach { resolver.delete(it, null, null) }
        }
    }

    @Test fun loadedRegularProGifBackRestoresProThenLoadedRegular() = withLoadedRegular { _, _ ->
        rule.onNodeWithText("Golden").performClick()
        rule.onNodeWithContentDescription("Undo").assertIsEnabled()
        requestProHandoff()
        acceptProHandoff()
        rule.onNodeWithText("Photo Pro ▾").assertIsDisplayed()

        rule.onNodeWithText("+ Add layer").performClick()
        rule.onNodeWithText("Shape").performClick()
        rule.onNodeWithText("Shape 2 · Shape").assertIsDisplayed()
        rule.onNodeWithText("Tools ▾").performClick()
        rule.onNodeWithText("Transform").performClick()
        rule.onNodeWithText("Transform ▴").assertIsDisplayed()
        rule.onNodeWithText("Undo").assertIsEnabled()
        rule.waitUntil(15_000) {
            rule.onAllNodesWithContentDescription("Pro editable composition")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        val canvas = rule.onNodeWithTag("pro-canvas")
        val defaultViewport = ProViewport()
        canvas.performTouchInput {
            val radius = minOf(width, height) * .13f
            down(0, center - Offset(radius, 0f))
            down(1, center + Offset(radius, 0f))
            updatePointerTo(0, center - Offset(2 * radius, 0f))
            updatePointerTo(1, center + Offset(2 * radius, 0f))
            move()
            up(0)
            up(1)
        }
        rule.waitUntil(15_000) {
            canvas.fetchSemanticsNode().config[ProViewportSemanticsKey] !=
                defaultViewport
        }
        val proViewport =
            canvas.fetchSemanticsNode().config[ProViewportSemanticsKey]

        rule.onNodeWithContentDescription("Choose editing workspace").performClick()
        rule.onNodeWithText("GIF maker").performClick()
        rule.onNodeWithText("GIF ▾").assertIsDisplayed()

        pressBackThroughDispatcher()
        rule.onNodeWithText("Photo Pro ▾").assertIsDisplayed()
        rule.onNodeWithText("Shape 2 · Shape").assertIsDisplayed()
        rule.onNodeWithText("Transform ▴").assertIsDisplayed()
        rule.onNodeWithText("Undo").assertIsEnabled()
        assertEquals(
            proViewport,
            rule.onNodeWithTag("pro-canvas").fetchSemanticsNode()
                .config[ProViewportSemanticsKey]
        )
        rule.onNodeWithText("Undo").performClick()
        rule.onNodeWithText("Stack (1)").assertIsDisplayed()
        rule.onNodeWithText("Redo").performClick()
        rule.onNodeWithText("Stack (2)").assertIsDisplayed()
        rule.onNodeWithText("Stack (2)").performClick()
        rule.onNodeWithText("Shape 2").assertIsDisplayed()
        rule.onNodeWithText("Done").performClick()
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText("Layer stack").fetchSemanticsNodes().isEmpty()
        }
        rule.onNodeWithText("Photo Pro ▾").assertIsDisplayed()

        pressBackThroughDispatcher()
        rule.onNodeWithText("Photo ▾").assertIsDisplayed()
        rule.onNodeWithTag("photo-tools").assertIsDisplayed()
        rule.onNodeWithContentDescription("Undo").assertIsEnabled()
    }

    @Test fun videoGifBackRestoresTabPausedPlayheadAndHistoryOwner() {
        val video = createVideo("gz-back-gif-" + System.nanoTime())
        val picker = singlePhotoPicker(video)
        val mounted = mutableStateOf(true)
        try {
            rule.setContent {
                CompositionLocalProvider(LocalActivityResultRegistryOwner provides picker) {
                    MaterialTheme(colorScheme = darkColorScheme()) {
                        if (mounted.value) PhotoEditorScreen()
                    }
                }
            }
            rule.onNodeWithContentDescription("Choose editing workspace").performClick()
            rule.onNodeWithText("Photo editing Pro").performClick()
            rule.onNodeWithContentDescription("Choose editing workspace").performClick()
            rule.onNodeWithText("GIF maker").performClick()
            rule.onNodeWithText("Choose video").performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Play").fetchSemanticsNodes().isNotEmpty()
            }
            rule.onNodeWithText("Reverse").performClick()
            rule.onNodeWithText("Undo").assertIsEnabled()

            rule.onNodeWithContentDescription("Scrub GIF frames")
                .performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
                    check(setProgress(5f))
                }
            rule.waitUntil(15_000) { !visibleGifFrameLabel().startsWith("Frame 1 ") }
            val playhead = visibleGifFrameLabel()

            rule.onNodeWithText("Playback").performClick()
            rule.onNodeWithText("Source timing").assertIsDisplayed()

            rule.onNodeWithContentDescription("Choose editing workspace").performClick()
            rule.onNodeWithText("Photo editing Pro").performClick()
            rule.onNodeWithText("Photo Pro ▾").assertIsDisplayed()

            pressBackThroughDispatcher()
            rule.onNodeWithText("GIF ▾").assertIsDisplayed()
            rule.onNodeWithText("Source timing").assertIsDisplayed()
            rule.onNodeWithText("Play").assertIsDisplayed()
            rule.onNodeWithText("Undo").assertIsEnabled()
            rule.onNodeWithText("Timeline").performClick()
            assertEquals(playhead, visibleGifFrameLabel())

            rule.onNodeWithText("Playback").performClick()
            rule.onNodeWithText("Play").performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("Pause").fetchSemanticsNodes().isNotEmpty()
            }
            rule.onNodeWithContentDescription("Choose editing workspace").performClick()
            rule.onNodeWithText("Photo editing Pro").performClick()
            rule.onNodeWithText("Photo Pro ▾").assertIsDisplayed()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithText("GIF maker").fetchSemanticsNodes().isEmpty()
            }
            pressBackThroughDispatcher()
            rule.onNodeWithText("GIF ▾").assertIsDisplayed()
            rule.onNodeWithText("Source timing").assertIsDisplayed()
            rule.onNodeWithText("Play").assertIsDisplayed()

            rule.onNodeWithText("Reverse").assertIsSelected()
            rule.onNodeWithText("Undo").performClick()
            rule.onNodeWithText("Reverse").assertIsNotSelected()
            rule.onNodeWithText("Redo").performClick()
            rule.onNodeWithText("Reverse").assertIsSelected()
        } finally {
            rule.runOnIdle { mounted.value = false }
            rule.waitForIdle()
            resolver.delete(video, null, null)
        }
    }

    @Test fun collageBackRestoresSelectedTileSectionAndUndoOwner() =
        withCollageFixtures { entry, neighbor, _ ->
            val initial = CollageDocument(photos = listOf(
                CollagePhoto(entry.toString()),
                CollagePhoto(neighbor.toString())
            ))
            val mounted = mutableStateOf(true)
            var latest = initial
            var contentSet = false
            try {
                rule.setContent {
                    var document by remember { mutableStateOf(initial) }
                    var session by remember { mutableStateOf(CollageWorkspaceSession()) }
                    var collageVisible by remember { mutableStateOf(true) }
                    MaterialTheme(colorScheme = darkColorScheme()) {
                        if (mounted.value) {
                            if (collageVisible) {
                                CollageWorkspaceScreenContent(
                                    document = document,
                                    onDocument = {
                                        document = it
                                        latest = it
                                    },
                                    onBack = { collageVisible = false },
                                    onSwitch = { _ -> collageVisible = false },
                                    session = session,
                                    onSession = { session = it }
                                )
                            } else {
                                BackHandler { collageVisible = true }
                                Text("Other workspace")
                            }
                        }
                    }
                }
                contentSet = true
                rule.waitUntil(15_000) {
                    rule.onAllNodesWithContentDescription("Photo collage preview")
                        .fetchSemanticsNodes()
                        .isNotEmpty()
                }
                rule.onNodeWithTag("collage-sections")
                    .performScrollToNode(hasText("Photos"))
                rule.onNodeWithText("Photos").performClick()
                rule.onNodeWithText("Photo 2").performClick()
                rule.onNodeWithTag("collage-sections")
                    .performScrollToNode(hasText("Spacing"))
                rule.onNodeWithText("Spacing").performClick()
                rule.onNodeWithText("Outside margin").performClick()
                rule.onNodeWithContentDescription("Adjust Outside margin")
                    .performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
                        check(setProgress(.06f))
                    }
                rule.waitUntil(15_000) {
                    rule.onAllNodesWithText("Updating", substring = true)
                        .fetchSemanticsNodes()
                        .isEmpty()
                }

                pressBackThroughDispatcher()
                rule.onNodeWithText("Other workspace").assertIsDisplayed()
                pressBackThroughDispatcher()
                rule.onNodeWithContentDescription("Adjust Outside margin").assertIsDisplayed()
                rule.onNodeWithText("↶").assertIsEnabled()
                rule.onNodeWithText("↶").performClick()
                rule.runOnIdle {
                    assertEquals(initial.layout.margin, latest.layout.margin)
                }

                rule.onNodeWithTag("collage-sections")
                    .performScrollToNode(hasText("Photos"))
                rule.onNodeWithText("Photos").performClick()
                rule.onNodeWithText("Edit selected").performClick()
                rule.waitUntil(15_000) {
                    rule.onAllNodesWithTag("photo-tools").fetchSemanticsNodes().isNotEmpty()
                }
                rule.onNodeWithText("Golden").performClick()
                rule.onNodeWithText("Done").performClick()
                rule.runOnIdle {
                    assertEquals(EditDocument(), latest.photos[0].edits)
                    assertNotEquals(EditDocument(), latest.photos[1].edits)
                }
            } finally {
                if (contentSet) {
                    rule.runOnIdle { mounted.value = false }
                    rule.waitForIdle()
                }
            }
        }

    @Test fun photoGifBackRestoresToolFrameAndExactTimingUndo() =
        withCollageFixtures { entry, neighbor, _ ->
            val initial = GifDocument(
                frames = listOf(entry.toString(), neighbor.toString()),
                delayMs = 350
            )
            val mounted = mutableStateOf(true)
            var latest = initial
            var contentSet = false
            try {
                rule.setContent {
                    var document by remember { mutableStateOf(initial) }
                    var session by remember {
                        mutableStateOf(
                            GifWorkspaceSession(route = GifWorkspaceRoute.PHOTOS)
                        )
                    }
                    var gifVisible by remember { mutableStateOf(true) }
                    MaterialTheme(colorScheme = darkColorScheme()) {
                        if (mounted.value) {
                            if (gifVisible) {
                                GifMakerScreenContent(
                                    document = document,
                                    onDocument = {
                                        document = it
                                        latest = it
                                    },
                                    onBack = { gifVisible = false },
                                    onSwitch = { _ -> gifVisible = false },
                                    session = session,
                                    onSession = { session = it }
                                )
                            } else {
                                BackHandler { gifVisible = true }
                                Text("Other workspace")
                            }
                        }
                    }
                }
                contentSet = true
                rule.onNodeWithText("Photos").performClick()
                rule.onNodeWithText("Photo 2").performClick()
                rule.onNodeWithText("Timing").performClick()
                rule.onNodeWithContentDescription("Photo GIF frame duration")
                    .performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
                        check(setProgress(800f))
                    }
                rule.onNodeWithText("Frame duration: 800 ms").assertIsDisplayed()

                pressBackThroughDispatcher()
                rule.onNodeWithText("Other workspace").assertIsDisplayed()
                pressBackThroughDispatcher()
                rule.onNodeWithText("Frame duration: 800 ms").assertIsDisplayed()
                rule.onNodeWithText("Undo").assertIsEnabled()
                rule.onNodeWithText("Undo").performClick()
                rule.onNodeWithText("Frame duration: 350 ms").assertIsDisplayed()

                rule.onNodeWithText("Photos").performClick()
                rule.onNodeWithText("Remove photo").performClick()
                rule.runOnIdle {
                    assertEquals(listOf(entry.toString()), latest.frames)
                }
            } finally {
                if (contentSet) {
                    rule.runOnIdle { mounted.value = false }
                    rule.waitForIdle()
                }
            }
        }

    @Test fun emptyProVisitWithoutRegularPhotoDoesNotSuppressLaterHandoff() {
        val uri = createPhoto("gz-back-pro-seed-" + System.nanoTime())
        val picker = singlePhotoPicker(uri)
        val mounted = mutableStateOf(true)
        var contentSet = false
        try {
            rule.setContent {
                CompositionLocalProvider(LocalActivityResultRegistryOwner provides picker) {
                    MaterialTheme(colorScheme = darkColorScheme()) {
                        if (mounted.value) PhotoEditorScreen()
                    }
                }
            }
            contentSet = true

            rule.onNodeWithContentDescription("Choose editing workspace").performClick()
            rule.onNodeWithText("Photo editing Pro").performClick()
            rule.onNodeWithText("Add first photo").assertIsDisplayed()
            pressBackThroughDispatcher()
            rule.onNodeWithText("Photo ▾").assertIsDisplayed()

            rule.onNodeWithText("Open media").performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithTag("photo-tools").fetchSemanticsNodes().isNotEmpty()
            }
            requestProHandoff()
            acceptProHandoff()
            rule.onNodeWithText("Original photo", substring = true).assertIsDisplayed()
        } finally {
            if (contentSet) {
                rule.runOnIdle { mounted.value = false }
                rule.waitForIdle()
            }
            resolver.delete(uri, null, null)
        }
    }

    @Test fun declinedHandoffOpensBlankProAndExplicitRegularSwitchRestoresTheDraft() =
        withLoadedRegular { uri, original ->
            rule.onNodeWithText("Golden").performClick()
            requestProHandoff()
            rule.onNodeWithText("Cancel").performClick()
            rule.onNodeWithText("Continue editing in Photo Pro?").assertDoesNotExist()
            rule.onNodeWithTag("photo-tools").assertIsDisplayed()

            requestProHandoff()
            rule.onNodeWithText("No, open blank").performClick()
            rule.onNodeWithText("Select an image to edit").assertIsDisplayed()
            rule.onNodeWithText("Original photo", substring = true).assertDoesNotExist()
            rule.onNodeWithContentDescription("Choose editing workspace").performClick()
            rule.onNodeWithText("Photo editing").performClick()
            rule.onNodeWithTag("photo-tools").assertIsDisplayed()
            rule.onNodeWithContentDescription("Undo").assertIsEnabled()
            pressBackThroughDispatcher()
            rule.onNodeWithText("Keep your photo edits?").assertIsDisplayed()
            rule.onNodeWithText("Keep editing").performClick()
            assertArrayEquals(original, resolver.openInputStream(uri)!!.use { it.readBytes() })
        }

    @Test fun acceptedHandoffRendersTheCurrentEditedPhotoInPro() = withLoadedRegular { uri, original ->
        val regular = rule.onNodeWithContentDescription("Photo editing canvas", substring = true)
        val before = photoSample(regular)
        rule.onNodeWithText("Golden").performClick()
        rule.waitUntil(15_000) { pixelDistance(photoSample(regular), before) > 16 }
        val edited = photoSample(regular)
        requestProHandoff()
        acceptProHandoff()
        rule.onNodeWithText("Original photo", substring = true).assertIsDisplayed()
        rule.waitUntil(15_000) {
            val preview = rule.onAllNodesWithContentDescription("Pro editable composition")
            preview.fetchSemanticsNodes().isNotEmpty() && pixelDistance(photoSample(preview[0]), edited) <= 8
        }
        pressBackThroughDispatcher()
        rule.onNodeWithTag("photo-tools").assertIsDisplayed()
        rule.waitUntil(15_000) { pixelDistance(photoSample(regular), edited) <= 8 }
        rule.onNodeWithContentDescription("Undo").performClick()
        rule.waitUntil(15_000) { pixelDistance(photoSample(regular), before) <= 8 }
        assertArrayEquals(original, resolver.openInputStream(uri)!!.use { it.readBytes() })
    }

    @Test fun offCenterCropAndLookSurviveRegularProHandoffAndProjectSave() =
        withLoadedRegular(width = 120, height = 80, colorAt = { x, y ->
            val column = x / 20
            val row = y / 20
            Color.rgb(32 + 34 * column, 32 + 52 * row, 28 + 33 * ((column + row * 2) % 6))
        }) { uri, sourceBytes ->
            val context = instrumentation.targetContext
            val name = "crop-handoff-" + System.nanoTime().toString().takeLast(10)
            val original = ImagePipeline.decode(resolver, uri, 120, 9_600)
            // An 80x80 centered square starts at x=20. The drag below moves it 12px right.
            // Build the expected crop directly from source pixels, independently of CropMath.
            val cropped = Bitmap.createBitmap(original, 32, 0, 80, 80)
            val look = Looks.all.single { it.name == "Golden" }.grade
            val expected = ImagePipeline.render(cropped, EditDocument(grade = look))
            val probes = listOf(.15f, .4f, .65f, .9f)
            fun matchesEditedCrop(node: SemanticsNodeInteraction): Boolean {
                val shown = node.captureToImage().asAndroidBitmap()
                val edge = minOf(shown.width, shown.height)
                val left = (shown.width - edge) / 2
                val top = (shown.height - edge) / 2
                return try {
                    probes.all { y ->
                        probes.all { x ->
                            val actual = shown.getPixel(left + (x * edge).toInt(), top + (y * edge).toInt())
                            val reference = expected.getPixel((x * 80).toInt(), (y * 80).toInt())
                            pixelDistance(actual, reference) <= 12
                        }
                    }
                } finally {
                    shown.recycle()
                }
            }

            try {
                val tools = rule.onNodeWithTag("photo-tools")
                tools.performScrollToNode(hasText("Crop"))
                rule.onNodeWithText("Crop").performClick()
                rule.onNodeWithText("1:1").performScrollTo().performClick()
                rule.waitUntil(15_000) { rule.onAllNodesWithTag("crop-frame").fetchSemanticsNodes().isNotEmpty() }
                val gutter = with(rule.density) { 16.dp.roundToPx() }
                rule.onNodeWithTag("crop-frame").performTouchInput {
                    val fittedWidth = minOf(
                        (width - 2 * gutter).toFloat(), (height - 2 * gutter) * (120f / 80f)
                    )
                    down(center)
                    moveTo(center + Offset(fittedWidth / 10f, 0f))
                    up()
                }
                tools.performScrollToNode(hasText("Looks"))
                rule.onNodeWithText("Looks").performClick()
                rule.onNodeWithText("Golden").performClick()
                val regular = rule.onNodeWithContentDescription("Photo editing canvas", substring = true)
                // Exclude chrome from the full-photo pixel grid. Reopen it to choose Pro.
                regular.performSemanticsAction(SemanticsActions.OnClick)
                rule.waitUntil(15_000) { matchesEditedCrop(regular) }
                regular.performSemanticsAction(SemanticsActions.OnClick)

                requestProHandoff()
                acceptProHandoff()
                rule.waitUntil(15_000) {
                    val previews = rule.onAllNodesWithContentDescription("Pro editable composition")
                    previews.fetchSemanticsNodes().isNotEmpty() && matchesEditedCrop(previews[0])
                }
                rule.onNodeWithText("File ▾").performClick()
                rule.onNodeWithText("Save project").performClick()
                rule.onNode(hasSetTextAction()).performTextInput(name)
                rule.onNodeWithText("Save", ignoreCase = true).performClick()
                rule.waitUntil(15_000) { ProStore.names(context).contains(name) }
                val saved = requireNotNull(ProStore.load(context, name))
                val layer = saved.layers.single()
                assertEquals(uri.toString(), layer.uri)
                assertEquals(1f, saved.canvasAspectRatio, .001f)
                assertEquals(1f, layer.aspect, .001f)
                assertEquals(CropRatio.SQUARE, layer.edits.crop)
                assertTrue(layer.edits.cropFrameRatioLocked)
                assertEquals(look, layer.edits.grade)
                val frame = requireNotNull(layer.edits.cropFrame)
                assertEquals(32f / 120f, frame.left, .01f)
                assertEquals(112f / 120f, frame.right, .01f)
                assertEquals(0f, frame.top, .001f)
                assertEquals(1f, frame.bottom, .001f)

                pressBackThroughDispatcher()
                rule.onNodeWithTag("photo-tools").assertIsDisplayed()
                regular.performSemanticsAction(SemanticsActions.OnClick)
                rule.waitUntil(15_000) { matchesEditedCrop(regular) }
                assertArrayEquals(sourceBytes, resolver.openInputStream(uri)!!.use { it.readBytes() })
            } finally {
                ProStore.delete(context, name)
                if (expected !== cropped) expected.recycle()
                cropped.recycle()
                original.recycle()
            }
        }

    @Test fun portraitCropTopAndBottomEdgesStayReachableWithEditorControlsVisible() =
        withLoadedRegular(width = 64, height = 120) { uri, original ->
            rule.onNodeWithTag("photo-tools").performScrollToNode(hasText("Crop"))
            rule.onNodeWithText("Crop").performClick()
            rule.waitUntil(15_000) { rule.onAllNodesWithTag("crop-frame").fetchSemanticsNodes().isNotEmpty() }
            val overlay = rule.onNodeWithTag("crop-frame")
            val frame = overlay.fetchSemanticsNode().boundsInRoot
            val gutter = with(rule.density) { 16.dp.roundToPx() }
            val topControl = rule.onNodeWithText("Open media").fetchSemanticsNode().boundsInRoot
            val bottomControls = rule.onNodeWithTag("photo-tools").fetchSemanticsNode().boundsInRoot
            assertTrue(frame.top >= topControl.bottom - 1f)
            assertTrue(frame.bottom <= bottomControls.top + 1f)

            fun dragEdge(top: Boolean) {
                overlay.performTouchInput {
                    val fittedHeight = minOf(
                        (height - 2 * gutter).toFloat(), (width - 2 * gutter) * (120f / 64f)
                    )
                    val inset = (height - fittedHeight) / 2f
                    val direction = if (top) 1f else -1f
                    val border = if (top) inset else height - inset
                    val start = Offset(center.x, border - direction * gutter * .75f)
                    down(start)
                    moveTo(start + Offset(0f, direction * fittedHeight / 10f))
                    up()
                }
            }
            val undo = rule.onNodeWithContentDescription("Undo")
            undo.assertIsNotEnabled()
            dragEdge(top = true)
            undo.assertIsEnabled()
            dragEdge(top = false)
            undo.performClick()
            undo.assertIsEnabled()
            undo.performClick()
            undo.assertIsNotEnabled()
            rule.onNodeWithTag("photo-tools").assertIsDisplayed()
            assertArrayEquals(original, resolver.openInputStream(uri)!!.use { it.readBytes() })
        }

    @Test fun sourceMatchedProCanvasSurvivesAProjectRoundTrip() {
        val original = ProDocument(
            layers = listOf(ProLayer(uri = "content://fixture/original", aspect = 1.5f)),
            background = ProBackground.TRANSPARENT,
            sourceAspectRatio = 1.5f
        )
        val restored = requireNotNull(ProStore.decode(ProStore.encode(original)))
        assertEquals(original, restored)
        assertEquals(96 to 64, ProMath.dimensions(restored, 96, 24_000_000L))
    }

    @Test fun cleanLoadedRegularBackReturnsToEmptyStart() = withLoadedRegular { uri, original ->
        pressBackThroughDispatcher()
        rule.onNodeWithText("Create collage").assertIsDisplayed()
        rule.onNodeWithTag("photo-tools").assertDoesNotExist()
        assertArrayEquals(original, resolver.openInputStream(uri)!!.use { it.readBytes() })
    }

    @Test fun dirtyLoadedRegularBackCanStayOrDiscardWithoutChangingSource() =
        withLoadedRegular { uri, original ->
            rule.onNodeWithText("Golden").performClick()
            rule.waitUntil(15_000) {
                try {
                    rule.onNodeWithContentDescription("Undo").assertIsEnabled()
                    true
                } catch (_: AssertionError) {
                    false
                }
            }
            rule.onNodeWithContentDescription("Undo").assertIsEnabled()
            pressBackThroughDispatcher()
            rule.onNodeWithText("Keep your photo edits?").assertIsDisplayed()

            rule.onNodeWithText("Keep editing").performClick()
            rule.onNodeWithTag("photo-tools").assertIsDisplayed()

            pressBackThroughDispatcher()
            rule.onNodeWithText("Discard").performClick()
            rule.onNodeWithText("Create collage").assertIsDisplayed()
            rule.onNodeWithTag("photo-tools").assertDoesNotExist()
            assertArrayEquals(original, resolver.openInputStream(uri)!!.use { it.readBytes() })
        }

    @Test fun cleanTileBackCancelsWithoutApplying() {
        val uri = createPhoto("gz-back-clean-tile-" + System.nanoTime())
        var doneCount = 0
        var cancelCount = 0
        try {
            mountTile(uri, onDone = { doneCount++ }, onCancel = { cancelCount++ })
            pressBackThroughDispatcher()
            rule.onNodeWithText("Tile host").assertIsDisplayed()
            rule.runOnIdle {
                assertEquals(0, doneCount)
                assertEquals(1, cancelCount)
            }
        } finally {
            resolver.delete(uri, null, null)
        }
    }

    @Test fun dirtyTileBackDiscardsWithoutApplying() {
        val uri = createPhoto("gz-back-discard-tile-" + System.nanoTime())
        var doneCount = 0
        var cancelCount = 0
        try {
            mountTile(uri, onDone = { doneCount++ }, onCancel = { cancelCount++ })
            rule.onNodeWithText("Golden").performClick()
            pressBackThroughDispatcher()
            rule.onNodeWithText("Apply tile changes?").assertIsDisplayed()
            rule.onNodeWithText("Discard").performClick()
            rule.onNodeWithText("Tile host").assertIsDisplayed()
            rule.runOnIdle {
                assertEquals(0, doneCount)
                assertEquals(1, cancelCount)
            }
        } finally {
            resolver.delete(uri, null, null)
        }
    }

    @Test fun dirtyTileBackAppliesExactlyOnce() {
        val uri = createPhoto("gz-back-apply-tile-" + System.nanoTime())
        var applied: CollagePhoto? = null
        var doneCount = 0
        var cancelCount = 0
        try {
            mountTile(uri, onDone = {
                doneCount++
                applied = it
            }, onCancel = { cancelCount++ })
            rule.onNodeWithText("Golden").performClick()
            pressBackThroughDispatcher()
            rule.onNodeWithText("Apply").performClick()
            rule.onNodeWithText("Tile host").assertIsDisplayed()
            rule.runOnIdle {
                assertEquals(1, doneCount)
                assertEquals(0, cancelCount)
                assertNotNull(applied)
                assertNotEquals(EditDocument(), applied!!.edits)
            }
        } finally {
            resolver.delete(uri, null, null)
        }
    }

    @Test fun savingStandaloneProjectDoesNotCleanActualTileDraft() =
        withCollageFixtures { entry, neighbor, _ ->
            val projectName = "Tile" + System.nanoTime()
            val initial = CollageDocument(photos = listOf(
                CollagePhoto(entry.toString()),
                CollagePhoto(neighbor.toString())
            ))
            var committed = initial
            var commitCount = 0
            try {
                withSelectedCollageTile(
                    initial = initial,
                    onDocument = {
                        committed = it
                        commitCount++
                    }
                ) {
                    rule.onNodeWithText("Golden").performClick()
                    rule.onNodeWithTag("photo-tools").performScrollToNode(hasText("More"))
                    rule.onNodeWithText("More").performClick()
                    rule.onNodeWithTag("photo-actions")
                        .performScrollToNode(hasText("Save project"))
                    rule.onNodeWithText("Save project").performClick()
                    rule.onNode(hasSetTextAction()).performTextInput(projectName)
                    rule.onNodeWithText("Save").performClick()
                    rule.onNodeWithText("Project saved on your phone").assertIsDisplayed()

                    // Project naming can leave the system IME owning the first Back.
                    // Wait for the application prompt before deciding the IME consumed it.
                    pressBackThroughDispatcher()
                    if (!waitForTextOrTimeout("Apply tile changes?", 5_000)) {
                        pressBackThroughDispatcher()
                    }
                    rule.onNodeWithText("Apply tile changes?").assertIsDisplayed()
                    rule.onNodeWithText("Discard").performClick()
                    rule.onNodeWithText("Collage ▾").assertIsDisplayed()
                    rule.runOnIdle {
                        assertEquals(initial, committed)
                        assertEquals(0, commitCount)
                    }
                }
            } finally {
                ProjectStore.delete(instrumentation.targetContext, projectName)
            }
        }

    @Test fun replacedTileDiscardLeavesActualCollageUnchanged() =
        withCollageFixtures { entry, neighbor, replacement ->
            val initial = CollageDocument(photos = listOf(
                CollagePhoto(entry.toString()),
                CollagePhoto(neighbor.toString())
            ))
            var committed = initial
            var commitCount = 0
            withSelectedCollageTile(
                initial = initial,
                replacementUri = replacement,
                onDocument = {
                    committed = it
                    commitCount++
                }
            ) {
                pressBackThroughDispatcher()
                rule.onNodeWithText("Apply tile changes?").assertIsDisplayed()
                rule.onNodeWithText("Discard").performClick()
                rule.onNodeWithText("Collage ▾").assertIsDisplayed()
                rule.runOnIdle {
                    assertEquals(initial, committed)
                    assertEquals(0, commitCount)
                }
            }
        }

    @Test fun replacedTileKeepThenApplyChangesOnlySelectedActualTile() =
        withCollageFixtures { entry, neighbor, replacement ->
            val initial = CollageDocument(photos = listOf(
                CollagePhoto(entry.toString()),
                CollagePhoto(neighbor.toString())
            ))
            var committed = initial
            var commitCount = 0
            withSelectedCollageTile(
                initial = initial,
                replacementUri = replacement,
                onDocument = {
                    committed = it
                    commitCount++
                }
            ) {
                pressBackThroughDispatcher()
                rule.onNodeWithText("Apply tile changes?").assertIsDisplayed()
                rule.onNodeWithText("Keep editing").performClick()
                rule.onNodeWithTag("photo-tools").assertIsDisplayed()

                pressBackThroughDispatcher()
                rule.onNodeWithText("Apply tile changes?").assertIsDisplayed()
                rule.onNodeWithText("Apply").performClick()
                rule.onNodeWithText("Collage ▾").assertIsDisplayed()
                rule.runOnIdle {
                    assertEquals(1, commitCount)
                    assertEquals(initial.photos[0], committed.photos[0])
                    assertEquals(replacement.toString(), committed.photos[1].uri)
                    assertEquals(initial.layout, committed.layout)
                }
            }
        }

    @Test fun visibleDoneCommitsOnlySelectedActualTile() =
        withCollageFixtures { entry, neighbor, replacement ->
            val initial = CollageDocument(photos = listOf(
                CollagePhoto(entry.toString()),
                CollagePhoto(neighbor.toString())
            ))
            var committed = initial
            var commitCount = 0
            withSelectedCollageTile(
                initial = initial,
                replacementUri = replacement,
                onDocument = {
                    committed = it
                    commitCount++
                }
            ) {
                rule.onNodeWithText("Done").performClick()
                rule.onNodeWithText("Collage ▾").assertIsDisplayed()
                rule.runOnIdle {
                    assertEquals(1, commitCount)
                    assertEquals(initial.photos[0], committed.photos[0])
                    assertEquals(replacement.toString(), committed.photos[1].uri)
                    assertEquals(initial.layout, committed.layout)
                }
            }
        }

}

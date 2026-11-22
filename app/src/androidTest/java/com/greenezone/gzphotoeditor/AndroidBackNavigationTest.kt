package com.greenezone.gzphotoeditor

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.view.KeyEvent
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
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.CollageDocument
import com.greenezone.gzphotoeditor.editor.CollagePhoto
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.ProjectStore
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidBackNavigationTest {
    @get:Rule val rule = createComposeRule()

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val resolver get() = instrumentation.targetContext.contentResolver

    private fun createPhoto(label: String): Uri {
        val uri = requireNotNull(resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$label.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        ))
        val width = 96
        val height = 64
        val pixels = IntArray(width * height) { index ->
            if (index % width < width / 2) Color.rgb(24, 72, 120)
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

    private fun pressSystemBack() {
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        rule.waitForIdle()
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

    private fun withLoadedRegular(test: (Uri, ByteArray) -> Unit) {
        val uri = createPhoto("gz-back-regular-" + System.nanoTime())
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
        rule.onNodeWithContentDescription("Choose editing workspace").performClick()
        rule.onNodeWithText("Photo editing Pro").performClick()
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

        pressSystemBack()
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

        pressSystemBack()
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

            pressSystemBack()
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
            pressSystemBack()
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

                pressSystemBack()
                rule.onNodeWithText("Other workspace").assertIsDisplayed()
                pressSystemBack()
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

                pressSystemBack()
                rule.onNodeWithText("Other workspace").assertIsDisplayed()
                pressSystemBack()
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

    @Test fun emptyNoOpProVisitStillAllowsLaterRegularSeed() {
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
            pressSystemBack()
            rule.onNodeWithText("Photo ▾").assertIsDisplayed()

            rule.onNodeWithText("Open media").performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithTag("photo-tools").fetchSemanticsNodes().isNotEmpty()
            }
            rule.onNodeWithContentDescription("Choose editing workspace").performClick()
            rule.onNodeWithText("Photo editing Pro").performClick()
            rule.onNodeWithText("Original photo", substring = true).assertIsDisplayed()
        } finally {
            if (contentSet) {
                rule.runOnIdle { mounted.value = false }
                rule.waitForIdle()
            }
            resolver.delete(uri, null, null)
        }
    }

    @Test fun intentionalEmptyProHistoryIsNotReseededFromRegular() {
        val uri = createPhoto("gz-back-pro-established-" + System.nanoTime())
        val picker = singlePhotoPicker(uri)
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
            rule.onNodeWithText("Or start with a blank canvas").performClick()
            rule.onNodeWithText("+ Add layer").performClick()
            rule.onNodeWithText("Shape").performClick()
            rule.onNodeWithText("Shape 2 · Shape").assertIsDisplayed()

            rule.onNodeWithText("Tools ▾").performClick()
            rule.onNodeWithText("Canvas").performClick()
            rule.onNodeWithText("New project").performScrollTo().performClick()
            rule.onNodeWithText("Start new").performClick()
            rule.onNodeWithText("Add first photo").assertIsDisplayed()
            rule.onNodeWithText("Undo").assertIsEnabled()

            rule.onNodeWithContentDescription("Choose editing workspace").performClick()
            rule.onNodeWithText("Photo editing").performClick()
            rule.onNodeWithText("Open media").performClick()
            rule.waitUntil(15_000) {
                rule.onAllNodesWithTag("photo-tools").fetchSemanticsNodes().isNotEmpty()
            }

            rule.onNodeWithContentDescription("Choose editing workspace").performClick()
            rule.onNodeWithText("Photo editing Pro").performClick()
            rule.onNodeWithText("Original photo", substring = true).assertDoesNotExist()
            rule.onNodeWithText("Add first photo").assertIsDisplayed()
            rule.onNodeWithText("Undo").performClick()
            rule.onNodeWithText("Stack (2)").assertIsDisplayed()
            rule.onNodeWithText("Stack (2)").performClick()
            rule.onNodeWithText("Shape 2").assertIsDisplayed()
            rule.onNodeWithText("Done").performClick()
        } finally {
            rule.runOnIdle { mounted.value = false }
            rule.waitForIdle()
            resolver.delete(uri, null, null)
        }
    }

    @Test fun cleanLoadedRegularBackReturnsToEmptyStart() = withLoadedRegular { uri, original ->
        pressSystemBack()
        rule.onNodeWithText("Create collage").assertIsDisplayed()
        rule.onNodeWithTag("photo-tools").assertDoesNotExist()
        assertArrayEquals(original, resolver.openInputStream(uri)!!.use { it.readBytes() })
    }

    @Test fun dirtyLoadedRegularBackCanStayOrDiscardWithoutChangingSource() =
        withLoadedRegular { uri, original ->
            rule.onNodeWithText("Golden").performClick()
            pressSystemBack()
            rule.onNodeWithText("Keep your photo edits?").assertIsDisplayed()

            rule.onNodeWithText("Keep editing").performClick()
            rule.onNodeWithTag("photo-tools").assertIsDisplayed()

            pressSystemBack()
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
            pressSystemBack()
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
            pressSystemBack()
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
            pressSystemBack()
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
                    Espresso.closeSoftKeyboard()
                    rule.waitForIdle()

                    pressSystemBack()
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
                pressSystemBack()
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
                pressSystemBack()
                rule.onNodeWithText("Apply tile changes?").assertIsDisplayed()
                rule.onNodeWithText("Keep editing").performClick()
                rule.onNodeWithTag("photo-tools").assertIsDisplayed()

                pressSystemBack()
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

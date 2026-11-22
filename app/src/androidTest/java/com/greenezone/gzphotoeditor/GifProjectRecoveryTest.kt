package com.greenezone.gzphotoeditor

import android.content.ContextWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.GifDocument
import com.greenezone.gzphotoeditor.editor.GifStore
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.file.Files
import java.util.Base64

@RunWith(AndroidJUnit4::class)
class GifProjectRecoveryTest {
    @Test fun recoverAndResaveAnInterruptedProjectAtCapacity() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val isolated = Files.createTempDirectory(target.cacheDir.toPath(), "gif-project-").toFile()
        val context = object : ContextWrapper(target) {
            override fun getFilesDir(): File = isolated
        }
        val original = GifDocument(frames = listOf("content://qa/red", "content://qa/blue"))
        val edited = original.copy(delayMs = 600, frames = original.frames.reversed())
        try {
            for (slot in 1..12) assertTrue(GifStore.save(context, "Slot $slot", original))
            val id = Base64.getUrlEncoder().withoutPadding().encodeToString("Slot 1".toByteArray())
            val base = File(isolated, "gz_gif_projects/$id.gzgif")
            val backup = File(base.path + ".bak")
            assertTrue(base.renameTo(backup))
            assertTrue(GifStore.names(context).contains("Slot 1"))
            assertFalse(GifStore.save(context, "Extra", original))
            assertEquals(original, GifStore.load(context, "Slot 1"))
            assertTrue(base.renameTo(backup))
            assertTrue(GifStore.save(context, "Slot 1", edited))
            assertEquals(edited, GifStore.load(context, "Slot 1"))
            assertEquals(12, GifStore.names(context).size)
        } finally { isolated.deleteRecursively() }
    }
}

package com.greenezone.gzphotoeditor

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Base64

@RunWith(AndroidJUnit4::class)
class AndroidProAtomicStoreTest {
    @Test fun twoProjectSavesRemainReadableAndLatestWins() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "qa-atomic-pro-write"
        val first = ProDocument(listOf(ProLayer(uri = "content://qa/example", name = "First")))
        val second = first.copy(layers = listOf(first.layers[0].copy(
            name = "After edit", x = .75f, opacity = .6f)))
        try {
            assertTrue(ProStore.save(context, name, first))
            assertEquals(first, ProStore.load(context, name))
            assertTrue(ProStore.save(context, name, second))
            assertEquals(second, ProStore.load(context, name))
            assertTrue(ProStore.names(context).contains(name))
        } finally { ProStore.delete(context, name) }
    }

    @Test fun recoverProjectFromLegacyBackupAfterInterruptedWrite() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "qa-backup-recovery"
        val project = ProDocument(listOf(ProLayer(
            uri = "content://qa/project", name = "Recover me",
            kind = ProLayerKind.IMAGE)))
        val id = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(name.toByteArray(StandardCharsets.UTF_8))
        val dir = File(context.filesDir, "gz_pro_projects")
        val base = File(dir, "$id.gzpro")
        val backup = File(dir, "$id.gzpro.bak")
        try {
            assertTrue(ProStore.save(context, name, project))
            backup.writeBytes(base.readBytes())
            assertTrue(base.delete())
            assertTrue(ProStore.names(context).contains(name))
            assertEquals(project, ProStore.load(context, name))
        } finally {
            ProStore.delete(context, name)
            backup.delete()
            base.delete()
        }
    }

    @Test fun invalidProjectDataFailsSafelyWithoutAffectingOtherProjects() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "qa-invalid-bounds"
        val otherName = "qa-untouched"
        val project = ProDocument(listOf(ProLayer(uri = "content://qa/existing", name = "Valid")))
        val id = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(name.toByteArray(StandardCharsets.UTF_8))
        val file = File(File(context.filesDir, "gz_pro_projects"), "$id.gzpro")
        try {
            assertTrue(ProStore.save(context, otherName, project))
            assertTrue(ProStore.save(context, name, project))
            file.writeText("x".repeat(3_000_050), Charsets.UTF_8)
            assertNull(ProStore.load(context, name))
            assertEquals(project, ProStore.load(context, otherName))
        } finally {
            ProStore.delete(context, name)
            ProStore.delete(context, otherName)
        }
    }
}

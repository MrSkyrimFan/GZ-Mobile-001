package com.greenezone.gzphotoeditor

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.ExportWorkspace
import com.greenezone.gzphotoeditor.editor.PendingExportJournal
import com.greenezone.gzphotoeditor.editor.PendingExportPhase
import com.greenezone.gzphotoeditor.editor.NamedProjectIdentity
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.fail
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.file.Files

@RunWith(AndroidJUnit4::class)
class AndroidPendingExportJournalTest {
    @Test fun destinationIsClaimedOnlyOnceAndInterruptedCopyRequiresNewPicker() = withJournal { journal, _ ->
        val ready = journal.begin(ExportWorkspace.PHOTO, "image/png", "edited.png", "original photo recipe")
        val destination = Uri.parse("content://export.test/first")
        val preparing = requireNotNull(journal.claim(ready, destination))
        assertEquals(PendingExportPhase.PREPARING, preparing.phase)
        assertEquals(destination.toString(), preparing.destination)
        assertNull(journal.claim(ready, Uri.parse("content://export.test/duplicate")))
        assertFalse(journal.cancelPicker(ready))

        journal.copying(preparing)
        val stored = requireNotNull(journal.read(ExportWorkspace.PHOTO))
        assertEquals(PendingExportPhase.COPYING, stored.phase)
        val failed = requireNotNull(journal.failed(stored))
        assertEquals(PendingExportPhase.DESTINATION_UNCERTAIN, failed.phase)

        val retry = journal.retry(failed)
        assertNotEquals(ready.id, retry.id)
        assertEquals(ready.recipe, retry.recipe)
        assertEquals(ready.mime, retry.mime)
        assertEquals(ready.filename, retry.filename)
        assertNull(retry.destination)
        assertNull(journal.claim(ready, destination))
        assertTrue(journal.cancelPicker(retry))
        assertNull(journal.read(ExportWorkspace.PHOTO))
    }

    @Test fun unclaimedFailureRemainsRetryableWithoutDestinationAndColdRouteReadsBackup() =
        withJournal { journal, directory ->
            val regular = journal.begin(ExportWorkspace.PHOTO, "image/png", "photo.png", "regular")
            val collage = journal.begin(ExportWorkspace.COLLAGE, "image/png", "layout.png", "collage")
            val failed = requireNotNull(journal.failed(collage))
            assertEquals(PendingExportPhase.RETRY, failed.phase)
            assertNull(failed.destination)

            val pendingDirectory = directory.resolve("pending-exports")
            val regularFile = pendingDirectory.resolve("PHOTO.json")
            val collageFile = pendingDirectory.resolve("COLLAGE.json")
            val backup = pendingDirectory.resolve("COLLAGE.json.bak")
            val timestamp = System.currentTimeMillis()
            assertTrue(regularFile.setLastModified(timestamp - 30_000L))
            assertTrue(collageFile.renameTo(backup))
            assertTrue(backup.setLastModified(timestamp - 10_000L))
            // The authoritative backup wins even if its ignored base file looks newer.
            collageFile.writeText("incomplete primary")
            assertEquals(ExportWorkspace.COLLAGE, journal.latestWorkspace())
            assertEquals(failed, journal.read(ExportWorkspace.COLLAGE))

            assertTrue(journal.discard(failed))
            assertEquals(ExportWorkspace.PHOTO, journal.latestWorkspace())
            assertTrue(journal.cancelPicker(regular))
            assertNull(journal.latestWorkspace())
        }

    @Test fun failedNativeDispositionRestoresPreviousPermissionsBeforeFreshRead() =
        withJournal { journal, directory ->
            val gif = journal.begin(ExportWorkspace.GIF, "image/gif", "loop.gif", "gif recipe", "gif-owner")
            journal.suppressNativeRestore(gif, "gif-owner")
            val photo = journal.begin(ExportWorkspace.PHOTO, "image/png", "photo.png", "unsaved photo", "photo-owner")
            val pro = journal.begin(ExportWorkspace.PRO, "image/png", "pro.png", "unsaved pro", "pro-owner")
            val destination = Uri.parse("content://export.test/uncertain")
            val preparing = requireNotNull(journal.claim(pro, destination))
            journal.copying(preparing)
            val oldBytes = directory.resolve("pending-exports/native-resolutions.json").readBytes()
            val failing = PendingExportJournal(directory) { file, bytes, bound ->
                NamedProjectIdentity.verify(file, bytes, bound)
                throw IOException("Injected failure after publication readback")
            }
            try {
                failing.suppressNativeRestores(listOf(
                    ExportWorkspace.PHOTO to "photo-owner", ExportWorkspace.PRO to "pro-owner"))
                fail("The injected verification failure must reject the disposition")
            } catch (_: IOException) { }
            assertArrayEquals(oldBytes, directory.resolve("pending-exports/native-resolutions.json").readBytes())
            val fresh = PendingExportJournal(directory)
            assertFalse(requireNotNull(fresh.read(ExportWorkspace.GIF)).restoreNativeDraft)
            assertEquals(photo, fresh.read(ExportWorkspace.PHOTO))
            val retainedPro = requireNotNull(fresh.read(ExportWorkspace.PRO))
            assertTrue(retainedPro.restoreNativeDraft)
            assertEquals(pro.recipe, retainedPro.recipe)
            assertEquals(destination.toString(), retainedPro.destination)
            assertEquals(PendingExportPhase.COPYING, retainedPro.phase)
        }

    @Test fun nativeDispositionRejectsWrongOwnerAndSurvivesIndependentExportRetry() =
        withJournal { journal, directory ->
            val photo = journal.begin(ExportWorkspace.PHOTO, "image/png", "photo.png", "captured recipe", "owner-a")
            assertNull(journal.suppressNativeRestore(photo, "owner-b"))
            assertTrue(requireNotNull(journal.read(ExportWorkspace.PHOTO)).restoreNativeDraft)
            val resolved = requireNotNull(journal.suppressNativeRestore(photo, "owner-a"))
            assertFalse(resolved.restoreNativeDraft)
            val fresh = PendingExportJournal(directory)
            val retry = fresh.retry(resolved)
            assertNotEquals(photo.id, retry.id)
            assertEquals(photo.recipe, retry.recipe)
            assertEquals("owner-a", retry.nativeOwnerId)
            assertFalse(retry.restoreNativeDraft)
            assertFalse(requireNotNull(PendingExportJournal(directory).read(ExportWorkspace.PHOTO)).restoreNativeDraft)
        }

    private fun withJournal(test: (PendingExportJournal, java.io.File) -> Unit) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = Files.createTempDirectory(context.cacheDir.toPath(), "export-journal-test").toFile()
        try {
            test(PendingExportJournal(directory), directory)
        } finally { directory.deleteRecursively() }
    }
}

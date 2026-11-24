package com.greenezone.gzphotoeditor

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.greenezone.gzphotoeditor.editor.ExportWorkspace
import com.greenezone.gzphotoeditor.editor.PendingExportJournal
import com.greenezone.gzphotoeditor.editor.PendingExportPhase
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

    private fun withJournal(test: (PendingExportJournal, java.io.File) -> Unit) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = Files.createTempDirectory(context.cacheDir.toPath(), "export-journal-test").toFile()
        try {
            test(PendingExportJournal(directory), directory)
        } finally { directory.deleteRecursively() }
    }
}

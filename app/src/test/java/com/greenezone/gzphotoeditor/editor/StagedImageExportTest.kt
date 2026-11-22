package com.greenezone.gzphotoeditor.editor

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.CancellationException

class StagedImageExportTest {
    @get:Rule val folder = TemporaryFolder()

    @Test fun incompleteEncodingNeverOpensTheDestination() {
        val cache = folder.newFolder()
        var opened = 0
        val open = { opened++; ByteArrayOutputStream() }
        assertThrows(IOException::class.java) {
            writeStagedImage(cache, open) { it.write(12); false }
        }
        assertThrows(IOException::class.java) {
            writeStagedImage(cache, open) { it.write(13); throw IOException("Encoder failure") }
        }
        assertThrows(IOException::class.java) {
            writeStagedImage(cache, open) { true }
        }
        assertEquals(0, opened)
        assertTrue(cache.listFiles()!!.isEmpty())
    }

    @Test fun successfulEncodingOnlyNeedsAWriteStreamAndCopiesAllBytes() {
        val cache = folder.newFolder()
        val data = ByteArray(180_000) { (it % 251).toByte() }
        val output = ByteArrayOutputStream()
        var finished = false
        writeStagedImage(cache, openDestination = { check(finished); output }) {
            it.write(data)
            finished = true
            true
        }
        assertArrayEquals(data, output.toByteArray())
        assertTrue(cache.listFiles()!!.isEmpty())
    }

    @Test fun cancelledEncoderCannotOpenTheDestinationEvenAfterProducingBytes() {
        val cache = folder.newFolder()
        var cancelled = false
        var opened = false
        assertThrows(CancellationException::class.java) {
            writeStagedImage(cache, openDestination = {
                opened = true
                ByteArrayOutputStream()
            }, checkActive = {
                if (cancelled) throw CancellationException("Stopped during encoding")
            }) {
                it.write(byteArrayOf(1, 2, 3))
                cancelled = true
                true
            }
        }
        assertEquals(false, opened)
        assertTrue(cache.listFiles()!!.isEmpty())
    }

    @Test fun cancellationDuringFinalCopyStillWritesEveryByteAndClosesTheDestinationOnce() {
        val cache = folder.newFolder()
        val data = ByteArray(170_000) { (it % 127).toByte() }
        var opens = 0
        var closes = 0
        var cancelled = false
        val output = object : ByteArrayOutputStream() {
            override fun write(bytes: ByteArray, offset: Int, length: Int) {
                super.write(bytes, offset, length)
                cancelled = true
            }

            override fun close() { closes++; super.close() }
        }
        writeStagedImage(cache, openDestination = { opens++; output }, checkActive = {
            if (cancelled) throw CancellationException("Stopped after the first write")
        }) { it.write(data); true }
        assertTrue(cancelled)
        assertEquals(1, opens)
        assertEquals(1, closes)
        assertArrayEquals(data, output.toByteArray())
        assertTrue(cache.listFiles()!!.isEmpty())
    }
}

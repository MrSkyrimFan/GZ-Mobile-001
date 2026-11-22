package com.greenezone.gzphotoeditor.editor

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.IOException

class GifExportGuardTest {
    @Test fun storageReservationFailsClosedBelowSafetyMargin() {
        assertEquals(0L, GifExportGuard.maxTemporaryBytes(0))
        assertEquals(0L, GifExportGuard.maxTemporaryBytes(GifExportGuard.SAFETY_MARGIN_BYTES))
        assertEquals(2L * 1024 * 1024,
            GifExportGuard.maxTemporaryBytes(GifExportGuard.SAFETY_MARGIN_BYTES + 2L * 1024 * 1024))
        assertEquals(GifExportGuard.MAX_TEMP_BYTES,
            GifExportGuard.maxTemporaryBytes(Long.MAX_VALUE))
    }

    @Test fun byteQuotaRejectsOverflowWithoutWritingPartialForbiddenChunk() {
        val out = ByteArrayOutputStream()
        GifExportGuard.limit(out, 6).use { stream ->
            stream.write(byteArrayOf(10, 20), 0, 2)
            stream.write(30)
            stream.write(byteArrayOf(40, 50, 60), 0, 3)
            assertArrayEquals(byteArrayOf(10,20,30,40,50,60), out.toByteArray())
            try {
                stream.write(70)
                fail("Exceeded byte ceiling without error")
            } catch (expected: IOException) {
                assertTrue(expected.message!!.contains("Reduce video duration"))
            }
            assertEquals(6, out.size())
        }
    }

    @Test fun oversizedArrayWriteIsRejectedBeforeAnyBytesAreCopied() {
        val out = ByteArrayOutputStream()
        GifExportGuard.limit(out, 2).use { stream ->
            try {
                stream.write(byteArrayOf(1, 2, 3), 0, 3)
                fail("Oversized write was accepted")
            } catch (_: IOException) {}
            assertEquals(0, out.size())
            stream.write(byteArrayOf(1,2),0,2)
        }
        assertArrayEquals(byteArrayOf(1,2), out.toByteArray())
    }

    @Test fun malformedArraySliceCannotBypassQuota() {
        val out = ByteArrayOutputStream()
        GifExportGuard.limit(out, 4).use { stream ->
            try {
                stream.write(byteArrayOf(1,2,3), 2, 2)
                fail("Invalid slice accepted")
            } catch (_: IndexOutOfBoundsException) {}
            stream.write(byteArrayOf(3,4),0,2)
        }
        assertArrayEquals(byteArrayOf(3,4),out.toByteArray())
    }
}

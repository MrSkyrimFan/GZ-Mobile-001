package com.greenezone.gzphotoeditor

import android.content.ContextWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.MessageDigest
import java.util.Base64
import org.json.JSONObject

@RunWith(AndroidJUnit4::class)
class AndroidProAtomicStoreTest {
    private fun denseDrawing(strokeCount: Int = 160): ProDocument {
        val points = List(500) { index ->
            val a = 0.00012345678f
            val b = 0.00045678912f
            if (index % 2 == 0) BrushPoint(a, b) else BrushPoint(b, a)
        }
        val edits = EditDocument(creative = CreativeDocument(strokes = List(strokeCount) { InkStroke(points) }))
        return ProDocument(listOf(ProLayer("", edits = edits, name = "Dense QA ink", kind = ProLayerKind.DRAWING)))
    }

    private fun coordinateText(drawing: ProDocument): String =
        drawing.layers.single().edits.creative.strokes.first().points.joinToString(";") { "${it.x},${it.y}" }

    private fun writeLegacyDrawing(file: File, drawing: ProDocument) {
        val envelope = JSONObject(file.readText())
        val layer = envelope.getJSONArray("layers").getJSONObject(0)
        val strokes = drawing.layers.single().edits.creative.strokes
        val stroke = strokes.first()
        val neutral = layer.getString("edits")
        val recipe = neutral.replace("creative.ink.count=0", "creative.ink.count=${strokes.size}")
        assertNotEquals(neutral, recipe)
        val placeholder = "gz-audit-legacy-recipe"
        layer.put("edits", placeholder)
        val outside = envelope.toString()
        val field = JSONObject.quote(placeholder)
        val coordinates = coordinateText(drawing)
        assertTrue(coordinates.length * strokes.size > 2_000_000)
        file.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(outside.substringBefore(field))
            writer.write(JSONObject.quote(recipe).dropLast(1))
            repeat(strokes.size) { index ->
                val key = "creative.ink.$index."
                writer.write("\\n${key}color=${stroke.color}\\n${key}opacity=${stroke.opacity}")
                writer.write("\\n${key}width=${stroke.thickness}\\n${key}erase=${stroke.erase}")
                writer.write("\\n${key}points=")
                writer.write(coordinates)
            }
            writer.write('"'.code)
            writer.write(outside.substringAfter(field))
        }
    }

    private fun fileSha256(file: File): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val chunk = ByteArray(8192)
            while (true) {
                val read = input.read(chunk)
                if (read < 0) break
                digest.update(chunk, 0, read)
            }
        }
        return digest.digest()
    }

    private fun projectFile(root: File, name: String): File {
        val id = Base64.getUrlEncoder().withoutPadding().encodeToString(name.toByteArray(Charsets.UTF_8))
        return File(root, "gz_pro_projects/$id.gzpro")
    }

    @Test fun oversizedCurrentOrResetRecipeCannotReplaceAHealthyNamedProject() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "Dense guard " + System.nanoTime().toString().takeLast(10)
        val healthy = ProDocument(listOf(ProLayer("", name = "Healthy", kind = ProLayerKind.DRAWING)))
        val reset = healthy.copy(layers = listOf(healthy.layers.single().copy(name = "Prior baseline")))
        val dense = denseDrawing()
        val file = projectFile(context.filesDir, name)
        try {
            assertTrue(ProMath.valid(dense))
            val strokes = dense.layers.single().edits.creative.strokes
            assertTrue(coordinateText(dense).length * strokes.size > 2_000_000)
            assertTrue(ProStore.save(context, name, healthy, reset))
            val priorBytes = file.readBytes()

            assertFalse("Oversized current recipe was reported saved", ProStore.save(context, name, dense, reset))
            assertArrayEquals(priorBytes, file.readBytes())
            assertFalse("Oversized reset recipe was reported saved", ProStore.save(context, name, healthy, dense))
            assertArrayEquals(priorBytes, file.readBytes())
            val recovered = requireNotNull(ProStore.loadProject(context, name))
            assertEquals(healthy, recovered.document)
            assertEquals(reset, recovered.resetDocument)
        } finally {
            ProStore.delete(context, name)
        }
    }

    @Test fun loadRecoversAnOlderOversizedRecipeWithinTheExistingProjectBudget() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "Legacy ink " + System.nanoTime().toString().takeLast(10)
        val dense = denseDrawing(strokeCount = 154)
        val skeleton = dense.copy(layers = listOf(dense.layers.single().copy(edits = EditDocument())))
        val file = projectFile(context.filesDir, name)
        try {
            assertTrue(ProStore.save(context, name, skeleton))
            writeLegacyDrawing(file, dense)
            assertTrue(file.length() < 3_000_000)
            assertTrue(file.length() <= Runtime.getRuntime().maxMemory() / 8L)
            val before = fileSha256(file)

            assertEquals(dense, ProStore.load(context, name))
            assertArrayEquals(before, fileSha256(file))
        } finally {
            ProStore.delete(context, name)
        }
    }

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
            // Stream oversize input rather than allocating a multi-MB String
            // inside the deliberately small API29 instrumentation heap.
            file.outputStream().buffered().use { out ->
                val chunk = ByteArray(4096) { 'x'.code.toByte() }
                repeat(1024) { out.write(chunk) }
            }
            assertNull(ProStore.load(context, name))
            assertEquals(project, ProStore.load(context, otherName))
        } finally {
            ProStore.delete(context, name)
            ProStore.delete(context, otherName)
        }
    }

    @Test fun backupOnlyProjectCanBeOverwrittenWhenAllTwelveSlotsAreOccupied() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val isolated = Files.createTempDirectory(target.cacheDir.toPath(), "pro-slot-").toFile()
        val context = object : ContextWrapper(target) {
            override fun getFilesDir(): File = isolated
        }
        val project = ProDocument(listOf(ProLayer(uri = "content://qa/image")))
        val edited = project.copy(layers = listOf(project.layers.single().copy(name = "New edit")))
        try {
            for (slot in 1..12) assertTrue(ProStore.save(context, "Slot $slot", project))
            val id = Base64.getUrlEncoder().withoutPadding().encodeToString("Slot 1".toByteArray())
            val file = File(isolated, "gz_pro_projects/$id.gzpro")
            assertTrue(file.renameTo(File(file.path + ".bak")))
            assertFalse(ProStore.save(context, "Extra", project))
            assertTrue(ProStore.save(context, "Slot 1", edited))
            assertEquals(edited, ProStore.load(context, "Slot 1"))
            assertEquals(12, ProStore.names(context).size)
        } finally {
            isolated.deleteRecursively()
        }
    }
}


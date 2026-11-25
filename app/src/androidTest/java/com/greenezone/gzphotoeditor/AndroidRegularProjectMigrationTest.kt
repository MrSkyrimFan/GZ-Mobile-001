package com.greenezone.gzphotoeditor

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.net.Uri
import android.util.Xml
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.EditDocument
import com.greenezone.gzphotoeditor.editor.EditDocumentCodec
import com.greenezone.gzphotoeditor.editor.Grade
import com.greenezone.gzphotoeditor.editor.NamedProjectIdentity
import com.greenezone.gzphotoeditor.editor.NamedProjectRef
import com.greenezone.gzphotoeditor.editor.PhotoProjectFiles
import com.greenezone.gzphotoeditor.editor.ProjectKind
import com.greenezone.gzphotoeditor.editor.ProjectSaveFailure
import com.greenezone.gzphotoeditor.editor.ProjectSaveIntent
import com.greenezone.gzphotoeditor.editor.ProjectSaveResult
import com.greenezone.gzphotoeditor.editor.ProjectStore
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.Base64
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

internal const val REGULAR_LEGACY_PREFERENCES = "gzphotoeditor.projects.v1"

/** Test-only byte oracle from the sole committed generation, without decoding a recipe. */
internal fun regularProjectPayloadFile(context: Context, name: String): File? {
    val index = PhotoProjectFiles.indexFile(context)
    if (!index.isFile) return null
    val rows = JSONObject(index.readText()).getJSONArray("rows")
    val row = (0 until rows.length()).map(rows::getJSONObject).firstOrNull { it.getString("name") == name }
        ?: return null
    if (row.isNull("generation")) return null
    return PhotoProjectFiles.payloadFile(context, row.getString("generation"))
}

internal class RegularProjectTestFixture(private val base: Context, root: File) {
    private val files = File(root, "files").apply { check(mkdir()) }
    private val data = File(root, "data").apply { check(mkdir()) }
    private val preferences = File(data, "shared_prefs").apply { check(mkdir()) }
    val legacyXml = File(preferences, REGULAR_LEGACY_PREFERENCES + ".xml")
    val legacyBackup = File(legacyXml.path + ".bak")
    val pendingSave = File(files, "gz-photo-project-save.pending")
    val legacyPreferenceAccesses = AtomicInteger()

    fun context(): Context = object : ContextWrapper(base) {
        override fun getFilesDir(): File = files
        override fun getDataDir(): File = data

        override fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
            if (name == REGULAR_LEGACY_PREFERENCES) {
                legacyPreferenceAccesses.incrementAndGet()
                error("Opening the legacy SharedPreferences would materialize its whole map")
            }
            return super.getSharedPreferences(name, mode)
        }
    }
}

internal fun withRegularProjectFixture(block: (RegularProjectTestFixture) -> Unit) {
    val base = InstrumentationRegistry.getInstrumentation().targetContext
    val root = Files.createTempDirectory(base.cacheDir.toPath(), "regular-r11-fixture-").toFile()
    try {
        val fixture = RegularProjectTestFixture(base, root)
        block(fixture)
        assertEquals(0, fixture.legacyPreferenceAccesses.get())
    } finally {
        root.deleteRecursively()
    }
}

internal fun regularLegacyKey(value: String): String =
    Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray(Charsets.UTF_8))

internal fun regularLegacyValue(source: Uri, recipe: String): String =
    regularLegacyKey(source.toString()) + "\n" + recipe

internal fun writeRegularLegacyXml(file: File, records: ((String, String) -> Unit) -> Unit) {
    file.outputStream().buffered().use { output ->
        val xml = Xml.newSerializer()
        xml.setOutput(output, "UTF-8")
        xml.startDocument("UTF-8", true)
        xml.startTag(null, "map")
        records { name, raw ->
            xml.startTag(null, "string")
            xml.attribute(null, "name", regularLegacyKey(name))
            xml.text(raw)
            xml.endTag(null, "string")
        }
        xml.endTag(null, "map")
        xml.endDocument()
        xml.flush()
    }
}

@RunWith(AndroidJUnit4::class)
class AndroidRegularProjectMigrationTest {
    @Test fun legacyBackupAndPendingRollbackKeepTheHealthyVersionAndExposeDamagedRows() =
        withRegularProjectFixture { fixture ->
            val context = fixture.context()
            val alphaSource = Uri.parse("content://r11-fixture/alpha")
            val absentSource = Uri.parse("content://r11-fixture-missing/photo")
            val alphaDoc = EditDocument(grade = Grade(exposure = .2f))
            val alpha = raw(alphaSource, alphaDoc)
            val interrupted = raw(alphaSource, alphaDoc.copy(grade = Grade(exposure = .7f)))
            val zeta = raw(alphaSource, EditDocument())
            val corrupt = regularLegacyValue(alphaSource, "version=unsupported-fixture")
            val unavailable = raw(absentSource, EditDocument(grade = Grade(exposure = -.1f)))
            val alphaId = NamedProjectIdentity.reserve(context, ProjectKind.PHOTO, "Alpha", false)
            writeRegularLegacyXml(fixture.legacyBackup) { emit ->
                emit("zeta", zeta)
                emit("Unavailable", unavailable)
                emit("Corrupt", corrupt)
                emit("Alpha", interrupted)
            }
            // Mirror the legacy preferences rule: the interrupted main copy
            // loses to its atomic backup, then the B1 pending save rolls back.
            writeRegularLegacyXml(fixture.legacyXml) { emit -> emit("Decoy", zeta) }
            fixture.pendingSave.writeText(JSONObject().put("version", 1)
                .put("key", regularLegacyKey("Alpha")).put("previous", alpha).toString())

            val rows = requireNotNull(ProjectStore.listMetadata(fixture.context()))
            assertEquals(listOf("Alpha", "Corrupt", "Unavailable", "zeta"), rows.map { it.project.name })
            val alphaRow = rows[0]
            assertEquals(alphaId, alphaRow.project.id)
            assertEquals(version(alpha), alphaRow.project.version)
            assertEquals(alpha.toByteArray().size.toLong(), alphaRow.byteCount)
            assertTrue(alphaRow.readable)
            assertArrayEquals(alpha.toByteArray(), requireNotNull(regularProjectPayloadFile(context, "Alpha")).readBytes())
            assertEquals(alphaDoc, ProjectStore.loadNamed(fixture.context(), alphaRow.project)?.content?.document)
            assertFalse(rows[1].readable)
            assertNull(ProjectStore.loadNamed(fixture.context(), rows[1].project))
            assertTrue(rows[2].readable)
            assertEquals(absentSource, ProjectStore.loadNamed(fixture.context(), rows[2].project)?.content?.image)
            assertNull(runCatching { context.contentResolver.openInputStream(absentSource)?.use { it.read() } }.getOrNull())
            assertTrue(rows[3].readable)
            assertEquals(zeta, requireNotNull(regularProjectPayloadFile(context, "zeta")).readText())
            assertFalse(fixture.pendingSave.exists())
        }

    @Test fun failedCutoverKeepsLegacyBytesAndCanRetryWithTheSameIdentity() =
        withRegularProjectFixture { fixture ->
            val context = fixture.context()
            val uri = Uri.parse("content://r11-fixture/retained")
            val document = EditDocument(grade = Grade(exposure = .3f))
            val original = raw(uri, document)
            writeRegularLegacyXml(fixture.legacyXml) { emit -> emit("Protected", original) }
            val legacyBytes = fixture.legacyXml.readBytes()
            val legacyId = NamedProjectIdentity.reserve(context, ProjectKind.PHOTO, "Protected", false)

            var verified = false
            val failed = ProjectStore.listMetadata(context, verifyIndex = { index, expected, limit ->
                NamedProjectIdentity.verify(index, expected, limit)
                verified = true
                throw IOException("Entry generations exist but the cutover must not be acknowledged")
            })
            assertTrue(verified)
            assertNull(failed)
            assertArrayEquals(legacyBytes, fixture.legacyXml.readBytes())
            assertFalse(PhotoProjectFiles.indexFile(context).exists())

            val retried = fixture.context()
            val row = requireNotNull(ProjectStore.listMetadata(retried)).single()
            assertEquals(legacyId, row.project.id)
            assertEquals(version(original), row.project.version)
            assertEquals(document, ProjectStore.loadNamed(retried, row.project)?.content?.document)
            assertArrayEquals(original.toByteArray(),
                requireNotNull(regularProjectPayloadFile(retried, "Protected")).readBytes())
        }

    @Test fun interruptedIndexRollbackAndUnreferencedRecordsNeverResurrectDeletedProject() =
        withRegularProjectFixture { fixture ->
            val context = fixture.context()
            val uri = Uri.parse("content://r11-fixture/pinned")
            val document = EditDocument(grade = Grade(exposure = .1f))
            val legacy = raw(uri, document)
            writeRegularLegacyXml(fixture.legacyXml) { emit ->
                emit("Remove", legacy)
                emit("Retain", raw(uri, EditDocument(grade = Grade(exposure = .4f))))
            }
            val oldLegacyBytes = fixture.legacyXml.readBytes()
            val rows = requireNotNull(ProjectStore.listMetadata(context))
            val removing = rows[0].project
            val retained = rows[1].project
            val retainedFile = requireNotNull(regularProjectPayloadFile(context, retained.name))
            val retainedBytes = retainedFile.readBytes()

            val index = PhotoProjectFiles.indexFile(context)
            val oldIndexBytes = index.readBytes()
            val previous = File(index.path + ".save-previous")
            val temporarilyUnavailable = File(index.path + ".test-preserved")
            assertFalse(ProjectStore.deleteNamed(context, removing, verifyIndex = { published, expected, limit ->
                NamedProjectIdentity.verify(published, expected, limit)
                assertTrue(previous.renameTo(temporarilyUnavailable))
                throw IOException("Injected outage while restoring the preceding authoritative index")
            }))
            assertTrue(NamedProjectIdentity.hasPendingPublication(index))
            assertNull(ProjectStore.listMetadata(fixture.context()))
            assertNull(ProjectStore.loadNamed(fixture.context(), removing))

            assertTrue(temporarilyUnavailable.renameTo(previous))
            val reopened = fixture.context()
            assertEquals(rows, ProjectStore.listMetadata(reopened))
            assertArrayEquals(oldIndexBytes, index.readBytes())
            assertFalse(NamedProjectIdentity.hasPendingPublication(index))
            assertEquals(document, ProjectStore.loadNamed(reopened, removing)?.content?.document)
            assertTrue(ProjectStore.deleteNamed(reopened, removing))

            // Simulate cleanup being interrupted after the committed deletion.
            // Neither the old XML nor an unreferenced valid payload may register
            // itself as a project once an authoritative index exists.
            fixture.legacyXml.writeBytes(oldLegacyBytes)
            val orphan = PhotoProjectFiles.payloadFile(context, UUID.randomUUID().toString())
            orphan.parentFile?.mkdirs()
            orphan.writeText(legacy)
            val afterDelete = fixture.context()
            assertEquals(listOf(retained), requireNotNull(ProjectStore.listMetadata(afterDelete)).map { it.project })
            assertNull(ProjectStore.loadNamed(afterDelete, "Remove"))
            assertNull(ProjectStore.loadNamed(afterDelete, removing))
            val recreated = saved(ProjectStore.saveNamed(afterDelete, ProjectSaveIntent.Create("Remove"), uri, document))
            assertNotEquals(removing.id, recreated.id)
            assertEquals(ProjectSaveResult.Rejected(ProjectSaveFailure.TARGET_CHANGED),
                ProjectStore.saveNamed(afterDelete, ProjectSaveIntent.Update(removing), uri, document))
            assertFalse(ProjectStore.deleteNamed(afterDelete, removing))
            assertEquals(recreated, ProjectStore.loadNamed(fixture.context(), recreated)?.project)
            assertArrayEquals(retainedBytes, retainedFile.readBytes())
            assertEquals(retained, ProjectStore.loadNamed(fixture.context(), retained)?.project)
        }

    private fun raw(source: Uri, document: EditDocument): String =
        regularLegacyValue(source, EditDocumentCodec.encodeBounded(document))

    private fun version(raw: String): String =
        NamedProjectIdentity.version(raw.toByteArray(Charsets.UTF_8))

    private fun saved(result: ProjectSaveResult): NamedProjectRef {
        assertTrue(result.toString(), result is ProjectSaveResult.Saved)
        return (result as ProjectSaveResult.Saved).project
    }
}

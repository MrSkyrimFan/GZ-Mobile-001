package com.greenezone.gzphotoeditor

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.Base64

@RunWith(AndroidJUnit4::class)
class AndroidProjectIdentityContractTest {
    private data class Store(
        val kind: ProjectKind,
        val write: (ProjectSaveIntent, Int) -> ProjectSaveResult,
        val read: (String) -> NamedProjectRef?,
        val value: (String) -> Int?,
        val delete: (NamedProjectRef) -> Boolean,
        val legacy: (String, Int) -> Boolean,
        val invalid: (ProjectSaveIntent) -> ProjectSaveResult
    )

    private fun withStores(block: (Context, File, List<Store>) -> Unit) {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val root = Files.createTempDirectory(base.cacheDir.toPath(), "project-identity-").toFile()
        val prefix = "identity-" + root.name + "-"
        val context = object : ContextWrapper(base) {
            override fun getFilesDir(): File = root
            override fun getSharedPreferences(name: String, mode: Int) =
                base.getSharedPreferences(prefix + name, mode)
        }
        val uri = Uri.parse("content://gz-identity/source")
        fun photo(n: Int) = EditDocument(grade = Grade(exposure = n / 10f))
        fun collage(n: Int) = CollageDocument(listOf(CollagePhoto(uri.toString(), photo(n))))
        fun pro(n: Int) = ProDocument(listOf(ProLayer("", edits = photo(n), kind = ProLayerKind.DRAWING)))
        fun gif(n: Int) = GifDocument(frames = listOf(uri.toString(), "$uri/second"), delayMs = 100 + n * 10)
        val stores = listOf(
            Store(ProjectKind.PHOTO,
                { intent, n -> ProjectStore.saveNamed(context, intent, uri, photo(n)) },
                { ProjectStore.loadNamed(context, it)?.project },
                { ProjectStore.loadNamed(context, it)?.content?.document?.grade?.exposure?.let { value -> (value * 10).toInt() } },
                { ProjectStore.deleteNamed(context, it) },
                { name, n -> ProjectStore.save(context, name, uri, photo(n)) },
                { intent -> ProjectStore.saveNamed(context, intent, Uri.parse("file:///invalid"), photo(1)) }),
            Store(ProjectKind.COLLAGE,
                { intent, n -> CollageStore.saveNamed(context, intent, collage(n)) },
                { CollageStore.loadNamed(context, it)?.project },
                { CollageStore.loadNamed(context, it)?.content?.photos?.single()?.edits?.grade?.exposure?.let { value -> (value * 10).toInt() } },
                { CollageStore.deleteNamed(context, it) },
                { name, n -> CollageStore.save(context, name, collage(n)) },
                { intent -> CollageStore.saveNamed(context, intent, CollageDocument()) }),
            Store(ProjectKind.PRO,
                { intent, n -> ProStore.saveNamed(context, intent, pro(n), pro(0)) },
                { ProStore.loadNamed(context, it)?.project },
                { ProStore.loadNamed(context, it)?.content?.document?.layers?.single()?.edits?.grade?.exposure?.let { value -> (value * 10).toInt() } },
                { ProStore.deleteNamed(context, it) },
                { name, n -> ProStore.save(context, name, pro(n), pro(0)) },
                { intent -> ProStore.saveNamed(context, intent, ProDocument()) }),
            Store(ProjectKind.GIF,
                { intent, n -> GifStore.saveNamed(context, intent, gif(n)) },
                { GifStore.loadNamed(context, it)?.project },
                { GifStore.loadNamed(context, it)?.content?.delayMs?.let { value -> (value - 100) / 10 } },
                { GifStore.deleteNamed(context, it) },
                { name, n -> GifStore.save(context, name, gif(n)) },
                { intent -> GifStore.saveNamed(context, intent, GifDocument()) })
        )
        try { block(context, root, stores) }
        finally {
            context.getSharedPreferences("gzphotoeditor.projects.v1", Context.MODE_PRIVATE).edit().clear().commit()
            root.deleteRecursively()
        }
    }

    private fun saved(result: ProjectSaveResult): NamedProjectRef {
        assertTrue(result.toString(), result is ProjectSaveResult.Saved)
        return (result as ProjectSaveResult.Saved).project
    }

    private fun rejected(result: ProjectSaveResult, reason: ProjectSaveFailure) {
        assertEquals(ProjectSaveResult.Rejected(reason), result)
    }

    private fun key(name: String) = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(name.toByteArray(Charsets.UTF_8))

    private fun identity(root: File, kind: ProjectKind, name: String) =
        File(root, "named-project-identities/" + kind.name.lowercase() + "/" + key(name) + ".id")

    private fun payload(root: File, kind: ProjectKind, name: String): File = when (kind) {
        ProjectKind.COLLAGE -> File(root, "gz_collages/" + key(name) + ".gzc")
        ProjectKind.PRO -> File(root, "gz_pro_projects/" + key(name) + ".gzpro")
        ProjectKind.GIF -> File(root, "gz_gif_projects/" + key(name) + ".gzgif")
        ProjectKind.PHOTO -> error("Regular still uses its compatible preferences payload")
    }

    @Test fun saveAsCollisionAndReopenKeepTheIntendedIdentityAndContent() = withStores { _, _, stores ->
        stores.forEach { store ->
            val first = saved(store.write(ProjectSaveIntent.Create("  First   project  "), 1))
            assertEquals("First project", first.name)
            assertEquals(store.kind, first.kind)
            assertEquals(first, store.read(first.name))
            val copy = saved(store.write(ProjectSaveIntent.Create("Second project"), 2))
            assertNotEquals(first.id, copy.id)
            val collision = store.write(ProjectSaveIntent.Create("First project"), 3)
            assertEquals(ProjectSaveResult.Exists(first), collision)
            assertEquals(1, store.value(first.name))
            val replaced = saved(store.write(ProjectSaveIntent.Replace((collision as ProjectSaveResult.Exists).project), 3))
            assertEquals(first.id, replaced.id)
            assertNotEquals(first.version, replaced.version)
            assertEquals(3, store.value(first.name))
            assertEquals(copy, store.read(copy.name))
            assertEquals(2, store.value(copy.name))
        }
    }

    @Test fun staleSaveAndDeleteCannotTouchChangedOrRecreatedTargets() = withStores { _, _, stores ->
        stores.forEach { store ->
            val first = saved(store.write(ProjectSaveIntent.Create("Same name"), 1))
            val updated = saved(store.write(ProjectSaveIntent.Update(first), 2))
            assertEquals(first.id, updated.id)
            rejected(store.write(ProjectSaveIntent.Update(first), 3), ProjectSaveFailure.TARGET_CHANGED)
            assertFalse(store.delete(first))
            assertEquals(2, store.value(first.name))
            assertTrue(store.delete(updated))
            val recreated = saved(store.write(ProjectSaveIntent.Create("Same name"), 4))
            assertNotEquals(updated.id, recreated.id)
            rejected(store.write(ProjectSaveIntent.Replace(updated), 5), ProjectSaveFailure.TARGET_CHANGED)
            assertFalse(store.delete(updated))
            assertEquals(recreated, store.read(recreated.name))
            assertEquals(4, store.value(recreated.name))
        }
    }

    @Test fun legacyWrappersRemainCompatibleAndNeverReturnAStaleVersion() = withStores { _, _, stores ->
        stores.forEach { store ->
            assertTrue(store.legacy("Legacy", 1))
            val original = store.read("Legacy")!!
            assertTrue(store.legacy("Legacy", 2))
            val updated = store.read("Legacy")!!
            assertEquals(original.id, updated.id)
            assertNotEquals(original.version, updated.version)
            rejected(store.write(ProjectSaveIntent.Update(original), 3), ProjectSaveFailure.TARGET_CHANGED)
            assertEquals(2, store.value("Legacy"))
            assertEquals(updated, store.read("Legacy"))
        }
    }

    @Test fun twelvePayloadSlotsAreIndependentOfIdentityMetadata() = withStores { _, _, stores ->
        stores.forEach { store ->
            val projects = (1..12).map { saved(store.write(ProjectSaveIntent.Create("Slot $it"), 1)) }
            rejected(store.write(ProjectSaveIntent.Create("Slot 13"), 2), ProjectSaveFailure.FULL)
            val replacement = saved(store.write(ProjectSaveIntent.Update(projects[0]), 2))
            assertEquals(projects[0].id, replacement.id)
            assertTrue(store.delete(projects[1]))
            val next = saved(store.write(ProjectSaveIntent.Create("Slot 13"), 3))
            assertEquals(3, store.value(next.name))
            projects.drop(2).forEach { assertEquals(it, store.read(it.name)) }
        }
    }

    @Test fun invalidDocumentsAndWrongWorkspaceRefsPreserveHealthySaves() = withStores { _, _, stores ->
        stores.forEach { store ->
            val original = saved(store.write(ProjectSaveIntent.Create("Protected"), 1))
            rejected(store.invalid(ProjectSaveIntent.Update(original)), ProjectSaveFailure.INVALID_DOCUMENT)
            val otherKind = ProjectKind.entries.first { it != store.kind }
            rejected(store.write(ProjectSaveIntent.Update(original.copy(kind = otherKind)), 2),
                ProjectSaveFailure.TARGET_CHANGED)
            assertEquals(original, store.read(original.name))
            assertEquals(1, store.value(original.name))
        }
    }

    @Test fun failedIdentityReservationCannotPublishPayloadOrConsumeQuota() = withStores { _, root, stores ->
        stores.forEach { store ->
            val blocked = identity(root, store.kind, "Blocked")
            assertTrue(blocked.mkdirs())
            File(blocked, "keep").writeText("obstruction")
            rejected(store.write(ProjectSaveIntent.Create("Blocked"), 1), ProjectSaveFailure.STORAGE_FAILURE)
            assertNull(store.read("Blocked"))
            assertTrue(blocked.deleteRecursively())
            val valid = saved(store.write(ProjectSaveIntent.Create("Blocked"), 2))
            assertEquals(2, store.value(valid.name))
        }
    }

    @Test fun backupOnlyPayloadAndIdentityRestoreTogetherWithoutChangingId() = withStores { _, root, stores ->
        stores.filter { it.kind != ProjectKind.PHOTO }.forEach { store ->
            val original = saved(store.write(ProjectSaveIntent.Create("Recovered"), 1))
            val file = payload(root, store.kind, original.name)
            val idFile = identity(root, store.kind, original.name)
            assertTrue(file.renameTo(File(file.path + ".bak")))
            assertTrue(idFile.renameTo(File(idFile.path + ".bak")))
            assertEquals(original, store.read(original.name))
            val updated = saved(store.write(ProjectSaveIntent.Update(original), 2))
            assertEquals(original.id, updated.id)
            assertTrue(store.delete(updated))
            assertNull(store.read(original.name))
        }
    }

    @Test fun interruptedReservationCannotBecomeTheIdentityOfALaterCreation() = withStores { context, _, stores ->
        stores.forEach { store ->
            val reserved = NamedProjectIdentity.reserve(context, store.kind, "Interrupted", payloadExists = false)
            assertNull(store.read("Interrupted"))
            val created = saved(store.write(ProjectSaveIntent.Create("Interrupted"), 1))
            assertNotEquals(reserved, created.id)
            assertEquals(created, store.read(created.name))
        }
    }

    @Test fun failedPreferencePublicationNeverReportsSavedOrChangesHealthyBaseline() = withStores { context, _, _ ->
        val uri = Uri.parse("content://gz-identity/failed-preferences")
        val document = EditDocument(grade = Grade(exposure = .1f))
        val original = saved(ProjectStore.saveNamed(context, ProjectSaveIntent.Create("Healthy"), uri, document))
        val failing = object : ContextWrapper(context) {
            override fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
                val preferences = context.getSharedPreferences(name, mode)
                return object : SharedPreferences by preferences {
                    override fun edit(): SharedPreferences.Editor {
                        val editor = preferences.edit()
                        return object : SharedPreferences.Editor by editor {
                            override fun putString(key: String, value: String?): SharedPreferences.Editor {
                                editor.putString(key, value)
                                return this
                            }
                            override fun remove(key: String): SharedPreferences.Editor {
                                editor.remove(key)
                                return this
                            }
                            override fun commit(): Boolean = false
                        }
                    }
                }
            }
        }
        rejected(ProjectStore.saveNamed(failing, ProjectSaveIntent.Update(original), uri,
            document.copy(grade = Grade(exposure = .4f))), ProjectSaveFailure.STORAGE_FAILURE)
        assertEquals(original, ProjectStore.loadNamed(context, original.name)!!.project)
        assertEquals(document, ProjectStore.loadNamed(context, original.name)!!.content.document)
        rejected(ProjectStore.saveNamed(failing, ProjectSaveIntent.Create("Never saved"), uri, document),
            ProjectSaveFailure.STORAGE_FAILURE)
        assertNull(ProjectStore.loadNamed(context, "Never saved"))
    }

    @Test fun proLegacyTrailingSpaceAndResetBaselineSurviveIdentityAdoption() = withStores { context, root, stores ->
        val legacyName = "x".repeat(31) + " "
        val document = ProDocument(listOf(ProLayer("", name = "Current", kind = ProLayerKind.DRAWING)))
        val reset = document.copy(layers = listOf(document.layers.single().copy(name = "Entry")))
        val file = payload(root, ProjectKind.PRO, legacyName)
        assertTrue(file.parentFile!!.mkdirs())
        file.writeBytes(ProStore.encodeProject(document, reset))
        val adopted = ProStore.loadNamed(context, legacyName)!!
        assertEquals(legacyName, adopted.project.name)
        assertEquals(document, adopted.content.document)
        assertEquals(reset, adopted.content.resetDocument)
        val edited = document.copy(background = ProBackground.BLACK)
        val saved = saved(ProStore.saveNamed(context, ProjectSaveIntent.Update(adopted.project), edited, reset))
        assertEquals(adopted.project.id, saved.id)
        assertEquals(legacyName, saved.name)
        assertEquals(reset, ProStore.loadNamed(context, legacyName)!!.content.resetDocument)
        assertTrue(stores.first { it.kind == ProjectKind.PRO }.delete(saved))
    }
    @Test fun failedPostPublicationVerificationRestoresExactPreviousBytesAndIdentity() = withStores { _, root, stores ->
        stores.filter { it.kind != ProjectKind.PHOTO }.forEach { store ->
            val original = saved(store.write(ProjectSaveIntent.Create("Healthy"), 1))
            val replacement = saved(store.write(ProjectSaveIntent.Create("Candidate"), 2))
            val file = payload(root, store.kind, original.name)
            val originalBytes = file.readBytes()
            val newBytes = payload(root, store.kind, replacement.name).readBytes()
            val limit = maxOf(originalBytes.size, newBytes.size).toLong()
            var checkedPublishedBytes = false
            try {
                NamedProjectIdentity.publish(file, newBytes, limit) { published, expected, bound ->
                    NamedProjectIdentity.verify(published, expected, bound)
                    checkedPublishedBytes = true
                    throw IOException("Injected post-publication validation failure")
                }
                fail("Validation failure must not be acknowledged as a successful publication")
            } catch (_: IOException) { }
            assertTrue(checkedPublishedBytes)
            assertArrayEquals(originalBytes, file.readBytes())
            assertEquals(original, store.read(original.name))
            assertEquals(1, store.value(original.name))
            assertFalse(NamedProjectIdentity.hasPendingPublication(file))
            assertFalse(File(file.path + ".save-previous").exists())
            val retry = saved(store.write(ProjectSaveIntent.Update(original), 3))
            assertEquals(original.id, retry.id)
            assertEquals(3, store.value(original.name))
        }
    }

    @Test fun failedFirstPublicationLeavesNoReadableProjectAndLaterCreateGetsANewIdentity() = withStores { context, root, stores ->
        stores.filter { it.kind != ProjectKind.PHOTO }.forEach { store ->
            val source = saved(store.write(ProjectSaveIntent.Create("Source"), 1))
            val bytes = payload(root, store.kind, source.name).readBytes()
            val file = payload(root, store.kind, "Uncommitted")
            val reserved = NamedProjectIdentity.reserve(context, store.kind, "Uncommitted", false)
            try {
                NamedProjectIdentity.publish(file, bytes, bytes.size.toLong()) { _, _, _ ->
                    throw IOException("Injected new-project validation failure")
                }
                fail("Failed first publication must throw")
            } catch (_: IOException) { }
            assertFalse(file.exists())
            assertNull(store.read("Uncommitted"))
            assertFalse(NamedProjectIdentity.hasPendingPublication(file))
            val retry = saved(store.write(ProjectSaveIntent.Create("Uncommitted"), 2))
            assertNotEquals(reserved, retry.id)
            assertEquals(2, store.value(retry.name))
        }
    }

    @Test fun interruptedRollbackNeverExposesUnacknowledgedBytesAndLegacyLoadRecovers() = withStores { context, root, stores ->
        stores.filter { it.kind != ProjectKind.PHOTO }.forEach { store ->
            val original = saved(store.write(ProjectSaveIntent.Create("Recoverable"), 1))
            val candidate = saved(store.write(ProjectSaveIntent.Create("Candidate"), 2))
            val file = payload(root, store.kind, original.name)
            val oldBytes = file.readBytes()
            val newBytes = payload(root, store.kind, candidate.name).readBytes()
            val previous = File(file.path + ".save-previous")
            val temporarilyUnavailable = File(file.path + ".test-preserved")
            try {
                NamedProjectIdentity.publish(file, newBytes, maxOf(oldBytes.size, newBytes.size).toLong()) { _, _, _ ->
                    // Model a rollback IO interruption after the new bytes were written.
                    // Keep the original bytes intact, but unavailable to the first rollback.
                    assertTrue(previous.renameTo(temporarilyUnavailable))
                    throw IOException("Injected interrupted rollback")
                }
                fail("An unresolved rollback must not acknowledge success")
            } catch (_: IOException) { }
            assertTrue(NamedProjectIdentity.hasPendingPublication(file))
            assertNull(store.read(original.name))
            val names = when (store.kind) {
                ProjectKind.COLLAGE -> CollageStore.names(context)
                ProjectKind.PRO -> ProStore.names(context)
                ProjectKind.GIF -> GifStore.names(context)
                ProjectKind.PHOTO -> error("Not a file store")
            }
            assertTrue(names.contains(original.name))
            assertTrue(temporarilyUnavailable.renameTo(previous))
            val legacyLoaded = when (store.kind) {
                ProjectKind.COLLAGE -> CollageStore.load(context, original.name)
                ProjectKind.PRO -> ProStore.load(context, original.name)
                ProjectKind.GIF -> GifStore.load(context, original.name)
                ProjectKind.PHOTO -> error("Not a file store")
            }
            assertNotNull(legacyLoaded)
            assertArrayEquals(oldBytes, file.readBytes())
            assertEquals(original, store.read(original.name))
            assertEquals(1, store.value(original.name))
            assertFalse(NamedProjectIdentity.hasPendingPublication(file))
        }
    }

}

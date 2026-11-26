package com.greenezone.gzphotoeditor

import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.greenezone.gzphotoeditor.editor.*
import java.io.File
import java.nio.file.Files
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@U1ReviewFocused
@RunWith(AndroidJUnit4::class)
class AndroidU1ReviewLeaseTest {
    @Test fun acquisitionCanceledAtIoReturnReleasesUndeliveredLease() = runBlocking {
        withStore {
            val dispatcher = ResultGate()
            val delivered = AtomicBoolean(false)
            val task = launch(dispatcher) {
                val result = store.prepare(work("undelivered-open", .27f))
                delivered.set(true)
                if (result is NativeWorkResult.Ok) store.release(result.value)
            }
            var returnToCaller: Runnable? = null
            var manifest: File? = null
            try {
                dispatcher.take().run() // Start the caller and dispatch its preparation to real IO.
                returnToCaller = dispatcher.take() // IO produced a result, but the caller has not run.
                assertFalse("The caller has not received its return value", delivered.get())
                assertEquals("The store installed exactly one staged handle", 1, store.outstandingStageLeasesForTest())
                manifest = requireNotNull(File(files.root, "manifests").listFiles()).single()
                assertTrue("The prepared manifest exists before transfer", manifest.isFile)
            } finally {
                task.cancel()
                dispatcher.open()
                returnToCaller?.run()
                withContext(NonCancellable) { withTimeout(12_000L) { task.join() } }
            }

            assertTrue(task.isCancelled)
            assertFalse("Prompt cancellation must withhold the returned handle", delivered.get())
            assertEquals("Undelivered acquisition must release its exact handle", 0,
                store.outstandingStageLeasesForTest())
            assertTrue("Release by itself does not collect bytes", requireNotNull(manifest).isFile)
            val catalog = ok(store.recover())
            assertNull(catalog.active)
            assertTrue(catalog.drafts.isEmpty())
            assertFalse("Normal cold collection removes the unrooted manifest", manifest.exists())
        }
    }

    @Test fun cancellationReleasesOnlyItsExactHandleAndCollectionKeepsPublishedRoots() = runBlocking {
        withStore {
            val active = ok(store.prepare(work("active", .19f)))
            val parked = ok(store.prepare(work("parked", .19f)))
            val named = ok(store.prepare(work("named", .19f, linked = true)))
            val sharedBytes = files.manifest(named.manifest.sha).readBytes()
            val namedRef = (store.saveNamed(ProjectSaveIntent.Create("Lease retained"), named)
                as ProjectSaveResult.Saved).project
            val catalog = ok(store.acknowledge(0L, active, displaced = parked))
            store.release(named)
            assertEquals("Published named, active and parked roots need no live stage", 0,
                store.outstandingStageLeasesForTest())

            val state = work("abandoned", .64f)
            val acquired = CompletableDeferred<NativeWorkPrepared>()
            var duplicate: NativeWorkPrepared? = null
            val task = launch(Dispatchers.Default) {
                val held = ok(store.prepare(state))
                try {
                    acquired.complete(held)
                    awaitCancellation()
                } finally {
                    store.release(held)
                }
            }
            try {
                val held = withTimeout(12_000L) { acquired.await() }
                val sameContent = ok(store.prepare(state))
                duplicate = sameContent
                assertEquals(held.manifest, sameContent.manifest)
                assertNotSame("A shared SHA is held by two exact handles", held, sameContent)
                assertEquals(2, store.outstandingStageLeasesForTest())
                task.cancelAndJoin()
                assertEquals("Cancel releases one lease, not every holder of the SHA", 1,
                    store.outstandingStageLeasesForTest())
                store.release(held)
                assertEquals("Repeated release cannot consume the other holder", 1,
                    store.outstandingStageLeasesForTest())
                assertTrue(files.manifest(held.manifest.sha).isFile)
            } finally {
                task.cancelAndJoin()
                duplicate?.let { store.release(it) }
            }

            assertEquals(0, store.outstandingStageLeasesForTest())
            val orphan = files.manifest(requireNotNull(duplicate).manifest.sha)
            assertTrue("Exiting a lease leaves its bytes for normal collection", orphan.isFile)
            assertEquals(catalog, ok(store.recover()))
            assertFalse("Cold collection can now remove the unrooted manifest", orphan.exists())
            assertEquals(active.state, ok(store.load(requireNotNull(catalog.active).ref)).state)
            assertEquals(parked.state, ok(store.load(catalog.drafts.single().ref)).state)
            assertArrayEquals(sharedBytes, files.manifest(named.manifest.sha).readBytes())
            val openedNamed = requireNotNull(ProjectStore.loadNamed(context, namedRef))
            assertEquals(namedRef, openedNamed.project)
            assertEquals(named.manifest, openedNamed.content.linkedWork)
            assertEquals(stateFor(.19f), openedNamed.content.document)
        }
    }

    private suspend fun withStore(block: suspend FreshStore.() -> Unit) = coroutineScope {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val root = Files.createTempDirectory(target.cacheDir.toPath(), "u1-review-lease-").toFile()
        val files = File(root, "files").apply { check(mkdir()) }
        val data = File(root, "data").apply { check(mkdir()) }
        val context = object : ContextWrapper(target) {
            override fun getFilesDir() = files
            override fun getDataDir() = data
        }
        try {
            FreshStore(context).block()
        } finally {
            root.deleteRecursively()
        }
    }

    private class FreshStore(val context: Context) {
        val store = NativeWorkStore(context)
        val files = NativeWorkFiles(context)
    }

    /** Hold only the caller dispatcher while the production IO dispatcher runs normally. */
    private class ResultGate : CoroutineDispatcher() {
        private val lock = Any()
        private val waiting = LinkedBlockingQueue<Runnable>()
        private var opened = false

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            val queued = synchronized(lock) {
                if (opened) false else waiting.offer(block)
            }
            if (!queued) Dispatchers.Default.dispatch(context, block)
        }

        fun take(): Runnable = checkNotNull(waiting.poll(12L, TimeUnit.SECONDS)) {
            "Expected the caller dispatch"
        }

        fun open() {
            val remaining = mutableListOf<Runnable>()
            synchronized(lock) {
                opened = true
                waiting.drainTo(remaining)
            }
            remaining.forEach { Dispatchers.Default.dispatch(EmptyCoroutineContext, it) }
        }
    }

    private fun stateFor(exposure: Float) = EditDocument(grade = Grade(exposure = exposure))

    private fun work(id: String, exposure: Float, linked: Boolean = false): NativeWorkState {
        val owner = "$id-owner"
        val initial = EditDocument()
        val current = stateFor(exposure)
        val photo = NativeRegularState(
            ownerId = owner,
            sourceId = "$id-source",
            sourceRevision = 1L,
            contentRevision = 3L,
            uri = Uri.parse("content://gz-u1-lease/original"),
            document = current,
            entry = initial,
            clean = initial,
            reset = initial,
            history = EditHistory(undo = listOf(initial, stateFor(.12f))),
            savedRef = null
        )
        return NativeWorkState(id, NativeWorkMode.REGULAR, photo, null,
            if (linked) NativeWorkLink() else null)
    }

    private fun <T> ok(result: NativeWorkResult<T>): T = when (result) {
        is NativeWorkResult.Ok -> result.value
        is NativeWorkResult.Failed -> error("Native custody failed: ${result.reason}")
    }
}

package com.greenezone.gzphotoeditor.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkspaceNavigationTest {
    @Test fun photoProGifPopsInReverseOrder() {
        val gif = WorkspaceNavigation()
            .open(WorkspaceMode.PRO)
            .open(WorkspaceMode.GIF)

        val pro = requireNotNull(gif.commitBack())
        assertEquals(WorkspaceMode.PRO, pro.current)
        assertEquals(listOf(WorkspaceMode.PHOTO), pro.backStack)

        val photo = requireNotNull(pro.commitBack())
        assertEquals(WorkspaceMode.PHOTO, photo.current)
        assertEquals(emptyList<WorkspaceMode>(), photo.backStack)
    }

    @Test fun openingCurrentDestinationDoesNotDuplicateHistory() {
        val pro = WorkspaceNavigation().open(WorkspaceMode.PRO)
        assertEquals(pro, pro.open(WorkspaceMode.PRO))
    }

    @Test fun previewingBackDoesNotMutateCommittedHistory() {
        val gif = WorkspaceNavigation()
            .open(WorkspaceMode.PRO)
            .open(WorkspaceMode.GIF)
        val snapshot = gif.copy(backStack = gif.backStack.toList())

        assertEquals(WorkspaceMode.PRO, gif.previewBack())
        assertEquals(snapshot, gif)
    }

    @Test fun rootBackDelegatesToAndroidAndUnavailableVideoDoesNotRoute() {
        val root = WorkspaceNavigation()
        assertNull(root.previewBack())
        assertNull(root.commitBack())
        assertEquals(root, root.open(WorkspaceMode.VIDEO))
    }
}

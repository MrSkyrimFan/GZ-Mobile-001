package com.greenezone.gzphotoeditor.ui

/**
 * Small host-owned history for the existing single-activity workspace shell.
 *
 * Opening a workspace records the actual caller. Previewing Back is read-only;
 * only [commitBack] mutates the route value held by the caller.
 */
internal data class WorkspaceNavigation(
    val current: WorkspaceMode = WorkspaceMode.PHOTO,
    val backStack: List<WorkspaceMode> = emptyList()
) {
    fun open(destination: WorkspaceMode): WorkspaceNavigation {
        if (!destination.available || destination == current) return this
        return copy(current = destination, backStack = backStack + current)
    }

    fun previewBack(): WorkspaceMode? =
        backStack.lastOrNull() ?: WorkspaceMode.PHOTO.takeIf { current != WorkspaceMode.PHOTO }

    fun commitBack(): WorkspaceNavigation? {
        val destination = previewBack() ?: return null
        return WorkspaceNavigation(
            current = destination,
            backStack = if (backStack.isEmpty()) emptyList() else backStack.dropLast(1)
        )
    }
}

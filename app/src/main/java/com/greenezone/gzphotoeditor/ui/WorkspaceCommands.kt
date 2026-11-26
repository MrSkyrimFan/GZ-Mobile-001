package com.greenezone.gzphotoeditor.ui

/** Presentation only: each native workspace owns the action and its availability. */
enum class WorkspaceCommandId {
    NEW, OPEN_MEDIA, OPEN_SECONDARY_MEDIA, OPEN_PROJECT, SAVE, SAVE_AS, EXPORT, RESET, DELETE, HELP, ADD_PHOTOS
}

data class WorkspaceCommand(
    val id: WorkspaceCommandId,
    val label: String,
    val enabled: Boolean = true,
    val disabledReason: String? = null,
    val onInvoke: () -> Unit
)

/** Source variants share the first Options slot. All other positions are stable. */
internal fun WorkspaceCommandId.optionsPosition(): Int = when (this) {
    WorkspaceCommandId.NEW, WorkspaceCommandId.OPEN_MEDIA, WorkspaceCommandId.OPEN_SECONDARY_MEDIA,
    WorkspaceCommandId.ADD_PHOTOS -> 0
    WorkspaceCommandId.OPEN_PROJECT -> 1
    WorkspaceCommandId.SAVE -> 2
    WorkspaceCommandId.SAVE_AS -> 3
    WorkspaceCommandId.EXPORT -> 4
    WorkspaceCommandId.RESET -> 5
    WorkspaceCommandId.DELETE -> 6
    WorkspaceCommandId.HELP -> 7
}

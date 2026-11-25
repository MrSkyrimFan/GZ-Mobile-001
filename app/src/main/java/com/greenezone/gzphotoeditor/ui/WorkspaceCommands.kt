package com.greenezone.gzphotoeditor.ui

/** Presentation only: each native workspace owns the action and its availability. */
enum class WorkspaceCommandId {
    NEW, OPEN_MEDIA, OPEN_SECONDARY_MEDIA, OPEN_PROJECT, SAVE, SAVE_AS, EXPORT, RESET, HELP, ADD_PHOTOS
}

data class WorkspaceCommand(
    val id: WorkspaceCommandId,
    val label: String,
    val enabled: Boolean = true,
    val disabledReason: String? = null,
    val onInvoke: () -> Unit
)

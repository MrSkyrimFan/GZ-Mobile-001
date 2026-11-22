package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Distinct workflows, not a giant merged tool palette. Video intentionally ships last. */
enum class WorkspaceMode(val label: String, val shortLabel: String, val hint: String, val available: Boolean = true) {
    PHOTO("Photo editing", "Photo", "Quick edits, Looks and simple corrections"),
    PRO("Photo editing Pro", "Photo Pro", "Layers, selections, masking, retouch and painting"),
    COLLAGE("Collage maker", "Collage", "Combine and independently edit up to 20 photos"),
    GIF("GIF maker", "GIF", "Pick one video, trim, play and save an animated GIF"),
    VIDEO("Video editing", "Video", "Coming soon · planned last", false)
}

@Composable
fun WorkspaceModeMenu(
    current: WorkspaceMode,
    enabled: Boolean = true,
    onChoose: (WorkspaceMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(
            enabled = enabled,
            onClick = { expanded = true },
            modifier = Modifier.semantics { contentDescription = "Choose editing workspace" }
        ) {
            Text(current.shortLabel + " ▾", fontSize = 12.sp, color = EditorMint,
                fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            WorkspaceMode.entries.forEach { mode ->
                DropdownMenuItem(
                    enabled = mode.available && enabled,
                    onClick = {
                        expanded = false
                        onChoose(mode)
                    },
                    text = {
                        Column {
                            Text(mode.label + if (!mode.available) " · Coming soon" else "",
                                color = if (mode.available) EditorWhite else EditorMuted,
                                fontSize = 14.sp,
                                fontWeight = if (mode == current) FontWeight.Bold else FontWeight.Normal)
                            Text(mode.hint, color = EditorMuted, fontSize = 11.sp)
                        }
                    }
                )
            }
        }
    }
}

package com.greenezone.gzphotoeditor.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * One presentation for all native editors. The host controls visibility and the
 * transient Options state; this component never changes a document or route.
 * Options stays at the trailing edge beside the wrapping brand. Workspace and
 * history have a deliberate second row; major commands never scroll horizontally.
 */
@Composable
fun EditorWorkspaceHeader(
    workspace: WorkspaceMode,
    commands: List<WorkspaceCommand>,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onChooseWorkspace: (WorkspaceMode) -> Unit,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    workspaceSelectionEnabled: Boolean = true
) {
    require(commands.map { it.id }.distinct().size == commands.size) {
        "A workspace command must have exactly one Options entry."
    }
    BackHandler(enabled = menuExpanded) { onMenuExpandedChange(false) }
    Column(modifier.fillMaxWidth().testTag("workspace-header")) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Text("GZ Editor Pro", color = EditorWhite, fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 10.dp))
            Box {
                TextButton(
                    onClick = { onMenuExpandedChange(true) },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("workspace-options")
                ) { Text("Options", color = EditorMint, fontWeight = FontWeight.Bold) }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { onMenuExpandedChange(false) }
                ) {
                    commands.forEach { command ->
                        DropdownMenuItem(
                            modifier = Modifier.heightIn(min = 48.dp)
                                .testTag("workspace-command-" + command.id.name.lowercase()),
                            enabled = enabled && command.enabled,
                            onClick = {
                                onMenuExpandedChange(false)
                                command.onInvoke()
                            },
                            text = {
                                Column {
                                    Text(command.label)
                                    if (!command.enabled && command.disabledReason != null) {
                                        Text(command.disabledReason, fontSize = 12.sp)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack, enabled = enabled,
                        modifier = Modifier.size(48.dp).semantics { contentDescription = "Back" }
                    ) { Text("‹", color = EditorWhite, fontSize = 28.sp) }
                }
                WorkspaceModeMenu(workspace, enabled && workspaceSelectionEnabled, onChooseWorkspace)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onUndo, enabled = enabled && canUndo,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = EditorWhite,
                        disabledContentColor = EditorMuted.copy(alpha = .38f)),
                    modifier = Modifier.size(48.dp).semantics { contentDescription = "Undo" }
                ) { HistoryArrow(redo = false) }
                IconButton(
                    onClick = onRedo, enabled = enabled && canRedo,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = EditorWhite,
                        disabledContentColor = EditorMuted.copy(alpha = .38f)),
                    modifier = Modifier.size(48.dp).semantics { contentDescription = "Redo" }
                ) { HistoryArrow(redo = true) }
            }
        }
    }
}

@Composable
private fun HistoryArrow(redo: Boolean) {
    val arrowColor = LocalContentColor.current
    Canvas(Modifier.size(26.dp)) {
        fun x(value: Float): Float = size.width * if (redo) 1f - value else value
        val arrow = Path().apply {
            moveTo(x(.15f), size.height * .36f)
            lineTo(x(.42f), size.height * .12f)
            moveTo(x(.15f), size.height * .36f)
            lineTo(x(.43f), size.height * .57f)
            moveTo(x(.15f), size.height * .36f)
            cubicTo(x(.89f), size.height * .12f, x(.97f), size.height * .83f,
                x(.45f), size.height * .86f)
        }
        drawPath(arrow, arrowColor, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

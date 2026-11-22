package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import java.util.Locale

private val palette = listOf(
    "White" to -1, "Black" to 0xFF000000.toInt(), "Mint" to 0xFF96E5C2.toInt(),
    "Red" to 0xFFFF5555.toInt(), "Blue" to 0xFF368FFF.toInt(),
    "Yellow" to 0xFFFFD95E.toInt(), "Purple" to 0xFFA878FF.toInt()
)

@Composable
internal fun ProColorPalette(value: Int, onColor: (Int) -> Unit) {
    var custom by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { custom = true }) { Text("Mix color") }
        palette.forEach { (name, color) ->
            Box(Modifier.size(44.dp).clickable { onColor(color) }.semantics {
                contentDescription = if (color == value) "$name, selected" else name
            }, contentAlignment = Alignment.Center) {
                Box(Modifier.size(28.dp).border(if (color == value) 3.dp else 1.dp,
                    if (color == value) EditorMint else EditorMuted, CircleShape)
                    .padding(3.dp).background(Color(color), CircleShape))
            }
        }
    }
    if (custom) {
        fun hex(number: Int) = String.format(Locale.US, "%06X", number and 0xffffff)
        var color by remember { mutableIntStateOf(value) }
        var entry by remember { mutableStateOf(hex(value)) }
        val valid = entry.length == 6 && entry.toIntOrNull(16) != null
        AlertDialog(onDismissRequest = { custom = false }, title = { Text("Mix color") }, text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Box(Modifier.fillMaxWidth().height(32.dp).background(Color(color)))
                OutlinedTextField(value = entry, onValueChange = {
                    entry = it.trim().removePrefix("#").take(6)
                    if (entry.length == 6) entry.toIntOrNull(16)?.let { rgb -> color = rgb or -0x1000000 }
                }, singleLine = true, label = { Text("Hex RGB") }, isError = !valid)
                listOf("Red" to 16, "Green" to 8, "Blue" to 0).forEach { (name, shift) ->
                    val channel = (color ushr shift) and 255
                    Text("$name · $channel", fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                    Slider(value = channel.toFloat(), valueRange = 0f..255f,
                        modifier = Modifier.semantics { contentDescription = name },
                        onValueChange = { raw ->
                            color = (color and (255 shl shift).inv()) or (raw.roundToInt() shl shift)
                            entry = hex(color)
                        })
                }
            }
        }, confirmButton = {
            TextButton(enabled = valid, onClick = { onColor(color); custom = false }) { Text("Use color") }
        }, dismissButton = { TextButton(onClick = { custom = false }) { Text("Cancel") } })
    }
}

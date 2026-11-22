package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private class HelpTopic(val title: String, vararg val steps: String)

private val proTopics = listOf(
    HelpTopic("Edit with the canvas clear",
        "Choose your tool and its settings. Tap the canvas once to hide the panels. Your active tool stays ready.",
        "Drag with one finger to paint, move an object or draw a selection. Pinch with two fingers to zoom and pan around details.",
        "Tap the canvas again to restore the panels. Open File and choose Fit canvas to return to the whole composition."),
    HelpTopic("Build and arrange a layer stack",
        "Add a photo as the first layer, or start with a blank canvas for drawing. Add a separate layer for each object you want to change independently.",
        "Select the layer you want to work on. In Layers, hold an object on the canvas to select it. If objects overlap, select the one you need in the stack.",
        "Move a layer above another to cover it, or behind to reveal the upper layer. Use Hide and Show to compare. Duplicate a layer before trying a different treatment."),
    HelpTopic("Position and align an object",
        "Select an image, text or shape layer and open Transform. Drag to move it. Use the X and Y controls for a small positioning change.",
        "Adjust Size or Rotate to place the object. Center on canvas returns its center to the middle. Pinching the canvas changes the view you work in."),
    HelpTopic("Cut out part of a photo",
        "Select the image layer, then choose Select & mask. Drag from one corner to the opposite corner for a rectangle or ellipse. For Freehand, trace the area with one continuous drag.",
        "Choose Apply layer mask. To make a window through the layer, choose Invert mask. The layer below shows through the hidden area. Increase Feather to soften the edge.",
        "Remove mask brings the whole layer back. To try another outline, draw it and apply it to replace the current mask."),
    HelpTopic("Correct color in one area",
        "Open Tone & color. To grade one photo, select its image layer. To grade a composite, add an adjustment layer above the elements you want to affect.",
        "Start with Light for exposure, highlights and shadows. Use Mix to target a single hue family. In Curves, choose RGB or a color channel, select a tonal point and move its slider.",
        "To limit the grade to a face or sky, select the adjustment layer, draw a selection in Select & mask, and apply it. Invert mask swaps which area receives the grade."),
    HelpTopic("Clone a detail or soften a blemish",
        "Select the image that contains the detail and open Clone & repair. Pinch to zoom in. Set a radius slightly wider than the spot.",
        "In Clone stamp, hold on clean texture to sample it, then hold on the area you want to replace. Choose nearby texture with similar light and grain.",
        "Use Soft repair and a lower strength for a gentler result. Undo last repair removes the most recent mark. Reset source lets you pick a different sample."),
    HelpTopic("Paint, erase and add lettering",
        "Add a brush layer. Choose size, opacity and color. Hold on the canvas to sample a color, or enter a hex color. Drag to paint. Eraser removes strokes from that drawing layer. The photo underneath stays intact.",
        "Add a text layer to write a caption. Edit its words, font, color and size, then position it with Transform. Add a shape layer to make an arrow, outline or filled shape."),
    HelpTopic("Blend a texture or apply an effect",
        "Place a texture photo above your image and open Blend. Multiply darkens the composite, Screen lightens it, and Soft light gives a gentler contrast effect.",
        "Use Opacity to control the strength. Use a separate Layer effects adjustment for black and white, invert, threshold or posterize. Its position in the stack determines which layers it affects."),
    HelpTopic("Save a project and export a copy",
        "Save the Pro project to keep layers, masks and adjustments editable. Keep the original photos accessible on the device. Use Open to resume.",
        "Export writes a flattened image. Choose PNG for a transparent canvas. For an exact platform size choose its preset, then choose Fit for the whole composition or Fill to crop its edges.",
        "If an image source becomes unavailable, select the affected layer and use Replace image to reconnect it.")
)

private val gifTopics = listOf(
    HelpTopic("Convert a video as it was shot",
        "Choose a video in GIF maker. Leave its original framing, timing and neutral adjustments in place to convert it directly. Press Play to review it, then Save GIF and pick a destination.",
        "GIF holds up to 256 colors per frame, stores delays in hundredths of a second, and has no audio. A smooth sky or subtle skin tones can look different from the source video."),
    HelpTopic("Trim, reverse and preview",
        "Use the trim controls to choose the part you want. Scrub through the result to check the first and last visible frames.",
        "Turn on Reverse to start at the end of the selected clip and work backward. Press Play to check the motion. Turn Reverse off to restore forward playback.",
        "For a shorter or slower action, change the trim or speed. Frame rate controls how often a new picture is sampled. At a lower frame rate, quick movements become less smooth."),
    HelpTopic("Repeat or play once",
        "Choose the repeat setting in Playback. Play once is useful when the final frame should hold. Use looping for motion you want to repeat.",
        "For a smoother loop, scrub the start and end until the subject has a similar position at both ends. Preview a complete cycle before saving."),
    HelpTopic("Touch up an entire clip",
        "Open Adjust and choose Light to recover shadows or pull down bright highlights. Use Color for white balance and saturation. The edit applies across the clip.",
        "For a selective color change, open Mix, pick the color and adjust Hue, Saturation or Luminance. Use Curves for more control over shadows, midtones or highlights.",
        "Play through both bright and dark frames to check the result. Use the control’s Reset to return it to neutral."),
    HelpTopic("Add a look or a caption",
        "Choose a Look to apply a preset to the clip, then inspect several frames. Use Undo to take back a change.",
        "Open Caption, enter your words, and adjust placement and size. Scrub the clip to check that the caption stays readable over changing backgrounds."),
    HelpTopic("Resize, rotate or crop the frame",
        "Keep the original aspect and size to preserve the source framing. To make a square or portrait GIF, choose that aspect in Canvas.",
        "Fit keeps the whole image inside the frame. Fill crops the edges to cover it. Check faces and captions near the borders. Use rotation or mirroring only when you want to change orientation."),
    HelpTopic("Edit a GIF you already have",
        "Choose the option to edit an existing GIF and select the animated file. Preview its frames, then change timing, direction, color or caption as needed.",
        "Save GIF writes an animated copy. Save a project as well if you want to revisit your adjustments later. Keep the source file available."),
    HelpTopic("Reopen a photo sequence",
        "Open a saved GIF project and choose the photo sequence. In Photos, select a frame and use Earlier or Later to change its place.",
        "Timing sets the duration of each photo. Use Play to check the order, then save the project to keep it editable or export an animated GIF."),
    HelpTopic("Reduce file size or finish a large export",
        "Duration, frame rate and dimensions drive GIF file size. Trim unused footage first. For a smaller file, choose a smaller output size or lower frame rate and check the preview.",
        "If export asks for a smaller job, keep your project and choose which setting to reduce. Save to a writable destination with room for the finished animation.")
)

private val photoTopics = listOf(
    HelpTopic("Work on the photo at full size",
        "Open a photo and choose a tool from the bottom row. Tap the image to hide the controls and tap again to restore them.",
        "Pinch to inspect detail. Fit returns to the whole photo. Use Before to compare the original with your edits."),
    HelpTopic("Balance light and color",
        "Start in Light. Adjust Exposure for the whole photo, Highlights for bright regions, and Shadows for dark regions.",
        "In Color, use Temperature and Tint to correct a color cast. Mix changes one color family. Curves lets you change shadows, midtones and highlights in RGB or a single color channel."),
    HelpTopic("Retouch a local area",
        "Open Masks to select a radial, linear or brush region. Set its position or draw over your subject, then adjust exposure, contrast or saturation inside it.",
        "Use Blur & cover to obscure a face or detail. Position the region or paint it on. Inspect the entire area before exporting."),
    HelpTopic("Save a look, project or finished image",
        "More → Save my look stores the grade for another photo. Save project keeps this photo’s editable settings. Keep the original photo on your device.",
        "Export creates a new image. For an exact platform size choose its preset. Fit preserves the whole photo and Fill crops the edges to cover the frame.")
)

@Composable
internal fun ProHelpDialog(onDismiss: () -> Unit) = HelpDialog("Photo Pro help", proTopics, onDismiss)

@Composable
internal fun GifHelpDialog(onDismiss: () -> Unit) = HelpDialog("GIF maker help", gifTopics, onDismiss)

@Composable
internal fun PhotoHelpDialog(onDismiss: () -> Unit) = HelpDialog("Photo editing help", photoTopics, onDismiss)

@Composable
private fun HelpDialog(title: String, topics: List<HelpTopic>, onDismiss: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var selection by rememberSaveable { mutableStateOf<String?>(null) }
    val current = topics.find { it.title == selection }
    val back = { if (current == null) onDismiss() else selection = null }
    AlertDialog(
        onDismissRequest = back,
        title = { Text(current?.title ?: title, color = EditorWhite) },
        text = {
            key(selection) {
                Column(Modifier.heightIn(max = 410.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (current == null) {
                        OutlinedTextField(value = query, onValueChange = { query = it },
                            singleLine = true, label = { Text("Search help") },
                            modifier = Modifier.fillMaxWidth())
                        val matches = topics.filter { topic ->
                            topic.title.contains(query.trim(), true) ||
                                topic.steps.any { it.contains(query.trim(), true) }
                        }
                        matches.forEach { topic ->
                            TextButton(onClick = { selection = topic.title }) {
                                Text(topic.title, color = EditorMint, modifier = Modifier.fillMaxWidth())
                            }
                        }
                        if (matches.isEmpty()) Text("No matching topics. Try a tool name.",
                            color = EditorMuted)
                    } else {
                        current.steps.forEachIndexed { i, step ->
                            Text("Step ${i + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                color = EditorMint)
                            Text(step, color = EditorWhite, fontSize = 15.sp, lineHeight = 21.sp)
                        }
                    }
                }
            }
        },
        dismissButton = if (current == null) null else ({ TextButton(onClick = { selection = null }) {
            Text("All topics", color = EditorWhite)
        } }),
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close", color = EditorMint) } }
    )
}

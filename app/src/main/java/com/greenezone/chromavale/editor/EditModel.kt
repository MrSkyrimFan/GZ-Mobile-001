package com.greenezone.chromavale.editor

/** Every adjustment is neutral at 0. An edit is data, never an overwritten photo. */
data class Grade(
    val exposure: Float = 0f,
    val contrast: Float = 0f,
    val highlights: Float = 0f,
    val shadows: Float = 0f,
    val whites: Float = 0f,
    val blacks: Float = 0f,
    val gamma: Float = 0f,
    val saturation: Float = 0f,
    val vibrance: Float = 0f,
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val hue: Float = 0f,
    val red: Float = 0f,
    val green: Float = 0f,
    val blue: Float = 0f,
    val fade: Float = 0f,
    val clarity: Float = 0f,
    val sharpen: Float = 0f,
    val vignette: Float = 0f,
    val grain: Float = 0f
)

enum class CropRatio(val label: String, val ratio: Float?) {
    ORIGINAL("Original", null),
    SQUARE("1:1", 1f),
    PORTRAIT("4:5", 4f / 5f),
    CLASSIC("3:2", 3f / 2f),
    WIDE("16:9", 16f / 9f)
}

data class EditDocument(
    val grade: Grade = Grade(),
    val quarterTurns: Int = 0,
    val mirrored: Boolean = false,
    val crop: CropRatio = CropRatio.ORIGINAL
)

data class Look(val name: String, val grade: Grade)

object Looks {
    val all: List<Look> = listOf(
        Look("Natural", Grade()),
        Look("Golden", Grade(temperature = .34f, contrast = .13f, vibrance = .22f, highlights = -.14f)),
        Look("Cinema", Grade(contrast = .2f, highlights = -.22f, shadows = .11f, temperature = -.08f, fade = .12f, vignette = .18f)),
        Look("B&W", Grade(saturation = -1f, contrast = .2f, clarity = .16f)),
        Look("Soft Film", Grade(contrast = -.12f, fade = .4f, grain = .18f, saturation = -.09f)),
        Look("Punch", Grade(contrast = .26f, vibrance = .3f, blacks = -.17f, sharpen = .14f)),
        Look("Cool Air", Grade(temperature = -.29f, tint = .09f, saturation = -.05f, highlights = -.12f))
    )
}

/** Pure snapshot history; one entry per completed slider gesture or toolbar action. */
data class EditHistory(
    val undo: List<EditDocument> = emptyList(),
    val redo: List<EditDocument> = emptyList()
) {
    fun checkpoint(before: EditDocument, after: EditDocument): EditHistory {
        if (before == after) return this
        return copy(undo = (undo + before).takeLast(40), redo = emptyList())
    }

    fun undo(current: EditDocument): Pair<EditDocument, EditHistory>? {
        if (undo.isEmpty()) return null
        return undo.last() to copy(undo = undo.dropLast(1), redo = (redo + current).takeLast(40))
    }

    fun redo(current: EditDocument): Pair<EditDocument, EditHistory>? {
        if (redo.isEmpty()) return null
        return redo.last() to copy(undo = (undo + current).takeLast(40), redo = redo.dropLast(1))
    }
}

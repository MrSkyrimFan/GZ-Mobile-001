package com.greenezone.gzphotoeditor.editor

/** All edits remain parameters. Source image bytes are never overwritten. */
data class ColorMix(
    val hue: Float = 0f,
    val saturation: Float = 0f,
    val luminance: Float = 0f
)

enum class ColorBand(val label: String, val centerHue: Float) {
    RED("Red", 0f),
    ORANGE("Orange", 30f),
    YELLOW("Yellow", 60f),
    GREEN("Green", 120f),
    AQUA("Aqua", 180f),
    BLUE("Blue", 240f),
    PURPLE("Purple", 285f),
    MAGENTA("Magenta", 330f)
}

/** A neutral grade is the default for every slider. */
/** Five fixed x-coordinates, editable y-values. Endpoints are editable as well. */
data class CurvePoints(val values: List<Float> = listOf(0f, .25f, .5f, .75f, 1f)) {
    init { require(values.size == 5 && values.all { it.isFinite() && it in 0f..1f }) }
    fun change(index: Int, value: Float): CurvePoints {
        require(index in 0..4)
        return CurvePoints(values.toMutableList().also { it[index] = value.coerceIn(0f, 1f) })
    }
}

enum class CurveChannel(val title: String) {
    MASTER("RGB"), RED("Red"), GREEN("Green"), BLUE("Blue")
}

enum class MaskKind(val title: String) { RADIAL("Radial"), LINEAR("Linear"), BRUSH("Brush") }

data class BrushPoint(val x: Float, val y: Float)
data class BrushStroke(val points: List<BrushPoint>)


/** Local geometry coordinates are proportional to the final cropped/rotated image. */
data class LocalMask(
    val kind: MaskKind = MaskKind.RADIAL,
    val centerX: Float = .5f,
    val centerY: Float = .5f,
    val radius: Float = .35f,
    val feather: Float = .35f,
    val exposure: Float = 0f,
    val saturation: Float = 0f,
    val contrast: Float = 0f,
    val strokes: List<BrushStroke> = emptyList()
)

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
    val texture: Float = 0f,
    val denoise: Float = 0f,
    val sharpenThreshold: Float = 0f,
    val vignette: Float = 0f,
    val grain: Float = 0f,
    val shadowRed: Float = 0f,
    val shadowGreen: Float = 0f,
    val shadowBlue: Float = 0f,
    val midRed: Float = 0f,
    val midGreen: Float = 0f,
    val midBlue: Float = 0f,
    val highlightRed: Float = 0f,
    val highlightGreen: Float = 0f,
    val highlightBlue: Float = 0f,
    val colorMix: List<ColorMix> = List(ColorBand.entries.size) { ColorMix() },
    val masterCurve: CurvePoints = CurvePoints(),
    val redCurve: CurvePoints = CurvePoints(),
    val greenCurve: CurvePoints = CurvePoints(),
    val blueCurve: CurvePoints = CurvePoints()
)

enum class CropRatio(val label: String, val ratio: Float?) {
    ORIGINAL("Original", null),
    SQUARE("1:1", 1f),
    PORTRAIT("4:5", 4f / 5f),
    TALL("2:3", 2f / 3f),
    STORY("9:16", 9f / 16f),
    CLASSIC("3:2", 3f / 2f),
    PHOTO("4:3", 4f / 3f),
    WIDE("16:9", 16f / 9f),
    CINEMA("21:9", 21f / 9f)
}

data class EditDocument(
    val grade: Grade = Grade(),
    val quarterTurns: Int = 0,
    val mirrored: Boolean = false,
    val flipVertical: Boolean = false,
    val crop: CropRatio = CropRatio.ORIGINAL,
    val cropCenterX: Float = .5f,
    val cropCenterY: Float = .5f,
    val straightenDegrees: Float = 0f,
    val masks: List<LocalMask> = emptyList()
)

data class Look(val name: String, val grade: Grade)

object Looks {
    val all: List<Look> = listOf(
        Look("Natural", Grade()),
        Look("Golden", Grade(temperature = .34f, contrast = .13f, vibrance = .22f, highlights = -.14f)),
        Look("Cinema", Grade(contrast = .20f, highlights = -.22f, shadows = .11f, temperature = -.08f, fade = .12f, vignette = .18f)),
        Look("B&W", Grade(saturation = -1f, contrast = .20f, clarity = .16f)),
        Look("Soft Film", Grade(contrast = -.12f, fade = .40f, grain = .18f, saturation = -.09f)),
        Look("Punch", Grade(contrast = .26f, vibrance = .30f, blacks = -.17f, sharpen = .14f)),
        Look("Cool Air", Grade(temperature = -.29f, tint = .09f, saturation = -.05f, highlights = -.12f)),
        Look("Matte", Grade(fade = .45f, contrast = -.13f, blacks = .08f, grain = .14f)),
        Look("Warm Portrait", Grade(temperature = .19f, shadows = .13f, highlights = -.15f, contrast = -.06f)),
        Look("Twilight", Grade(shadows = .12f, highlights = -.14f, shadowBlue = .22f, highlightRed = .12f, vignette = .18f)),
        Look("High Key", Grade(exposure = .38f, highlights = -.18f, shadows = .18f, contrast = -.12f)),
        Look("Analog", Grade(fade = .24f, grain = .28f, temperature = .14f, saturation = -.16f, vignette = .22f))
    )
}

/** Forty completed gestures/actions of undo history, including restored custom looks. */
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

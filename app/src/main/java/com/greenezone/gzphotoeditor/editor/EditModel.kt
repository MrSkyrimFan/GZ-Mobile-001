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

enum class PrivacyKind(val title: String) {
    BLUR("Blur"), PIXELATE("Pixelate"), BLACK_BAR("Black bar"),
    COLOR_COVER("Color cover"), BLUR_BRUSH("Blur brush"), BLACK_BRUSH("Black brush");
    val usesBrush: Boolean get() = this == BLUR_BRUSH || this == BLACK_BRUSH
}

data class PrivacyEdit(
    val kind: PrivacyKind = PrivacyKind.BLUR,
    val centerX: Float = .5f,
    val centerY: Float = .5f,
    val width: Float = .45f,
    val height: Float = .16f,
    val strength: Float = .65f,
    val feather: Float = .22f,
    val brushSize: Float = .055f,
    val color: Int = -0x1000000,
    val strokes: List<BrushStroke> = emptyList(),
    // Older marks use the current view until its geometry changes.
    val authoringGeometry: PhotoGeometry? = null
)

data class PhotoGeometry(
    val quarterTurns: Int,
    val mirrored: Boolean,
    val flipVertical: Boolean,
    val crop: CropRatio,
    val cropCenterX: Float,
    val cropCenterY: Float,
    val straightenDegrees: Float,
    val cropFrame: CropFrame?,
    val cropFrameRatioLocked: Boolean
) {
    companion object {
        fun from(document: EditDocument) = PhotoGeometry(
            document.quarterTurns, document.mirrored, document.flipVertical,
            document.crop, document.cropCenterX, document.cropCenterY,
            document.straightenDegrees, document.cropFrame, document.cropFrameRatioLocked
        )
    }
}

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

enum class CropRatio(val label: String, val unitsWide: Int = 0, val unitsHigh: Int = 0) {
    ORIGINAL("Original"),
    SQUARE("1:1", 1, 1),
    PORTRAIT("4:5", 4, 5),
    TALL("2:3", 2, 3),
    STORY("9:16", 9, 16),
    CLASSIC("3:2", 3, 2),
    PHOTO("4:3", 4, 3),
    WIDE("16:9", 16, 9),
    CINEMA("21:9", 7, 3),
    FREE("Freeform");

    val ratio: Float? = if (unitsWide > 0) unitsWide.toFloat() / unitsHigh else null
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
    val masks: List<LocalMask> = emptyList(),
    val privacy: List<PrivacyEdit> = emptyList(),
    val cropFrame: CropFrame? = null,
    val creative: CreativeDocument = CreativeDocument(),
    // Explicit old crop frames are authoritative even when accompanied by a preset name.
    val cropFrameRatioLocked: Boolean = false
)

data class Look(val name: String, val grade: Grade)

object Looks {
    val all: List<Look> = listOf(
        Look("Natural", Grade(vibrance = .08f, highlights = -.07f, shadows = .05f)),
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
        Look("Analog", Grade(fade = .24f, grain = .28f, temperature = .14f, saturation = -.16f, vignette = .22f)),
        Look("Clean Studio", Grade(whites = .14f, highlights = -.12f, shadows = .09f, vibrance = .06f)),
        Look("Moody", Grade(exposure = -.18f, contrast = .22f, blacks = -.20f, saturation = -.14f, vignette = .16f)),
        Look("Rose", Grade(tint = .17f, temperature = .10f, fade = .13f, contrast = -.04f)),
        Look("Forest", Grade(green = .12f, temperature = -.09f, highlights = -.16f, contrast = .10f)),
        Look("Coastal", Grade(blue = .12f, temperature = -.18f, vibrance = .12f, highlights = -.08f)),
        Look("Desert", Grade(temperature = .28f, tint = .05f, blacks = -.06f, saturation = -.06f)),
        Look("Sunset", Grade(temperature = .38f, highlightRed = .20f, shadowBlue = .09f, contrast = .10f)),
        Look("Dawn", Grade(exposure = .12f, temperature = .16f, highlights = -.21f, shadows = .16f)),
        Look("Noir", Grade(saturation = -1f, contrast = .36f, blacks = -.21f, clarity = .18f)),
        Look("Silver", Grade(saturation = -1f, highlights = .11f, shadows = .12f, contrast = -.05f)),
        Look("Carbon", Grade(saturation = -1f, exposure = -.16f, whites = -.08f, contrast = .24f)),
        Look("Sepia", Grade(saturation = -.63f, temperature = .37f, tint = -.07f, fade = .18f)),
        Look("Vintage", Grade(saturation = -.23f, fade = .32f, grain = .20f, vignette = .16f)),
        Look("Polaroid", Grade(exposure = .11f, fade = .28f, temperature = .18f, grain = .14f)),
        Look("Pastel", Grade(contrast = -.20f, saturation = -.18f, exposure = .12f, fade = .16f)),
        Look("Editorial", Grade(contrast = .12f, highlights = -.15f, whites = .10f, saturation = -.06f)),
        Look("Fashion", Grade(exposure = .09f, shadows = .12f, sharpen = .16f, contrast = .13f)),
        Look("Portrait Glow", Grade(clarity = -.25f, shadows = .12f, temperature = .11f, highlights = -.08f)),
        Look("Skin Soft", Grade(texture = -.27f, clarity = -.12f, denoise = .18f, exposure = .04f)),
        Look("Nightfall", Grade(exposure = -.22f, shadows = .22f, highlightBlue = .16f, vignette = .19f)),
        Look("Neon", Grade(contrast = .25f, vibrance = .35f, tint = .08f, shadowBlue = .20f)),
        Look("Urban", Grade(contrast = .20f, saturation = -.17f, temperature = -.08f, clarity = .18f)),
        Look("Drama", Grade(contrast = .29f, blacks = -.19f, highlights = -.27f, clarity = .22f)),
        Look("Minimal", Grade(saturation = -.25f, contrast = -.09f, whites = .11f, fade = .06f)),
        Look("Bright", Grade(exposure = .22f, shadows = .18f, highlights = -.16f, whites = .11f)),
        Look("Soft Warm", Grade(temperature = .14f, contrast = -.14f, texture = -.14f, fade = .10f)),
        Look("Icy", Grade(temperature = -.38f, blue = .10f, contrast = .12f, highlights = .08f)),
        Look("Copper", Grade(temperature = .27f, shadowRed = .13f, highlightGreen = -.12f, vignette = .11f)),
        Look("Olive", Grade(green = .08f, temperature = .12f, tint = -.22f, saturation = -.08f)),
        Look("Cobalt", Grade(temperature = -.24f, blue = .15f, shadowBlue = .22f, contrast = .15f)),
        Look("Pearl", Grade(exposure = .18f, contrast = -.17f, whites = .15f, saturation = -.11f)),
        Look("Autumn", Grade(temperature = .27f, vibrance = .16f, green = -.08f, shadowRed = .13f)),
        Look("Spring", Grade(vibrance = .25f, shadows = .13f, temperature = .08f, tint = -.08f)),
        Look("Winter", Grade(temperature = -.20f, exposure = .11f, contrast = .14f, saturation = -.12f)),
        Look("Summer", Grade(temperature = .22f, vibrance = .23f, highlights = -.13f, contrast = .09f)),
        Look("Fine Art", Grade(saturation = -.32f, contrast = .13f, shadows = .14f, grain = .07f)),
        Look("Soft Portrait", Grade(shadows = .18f, contrast = -.16f, texture = -.16f, tint = .05f)),
        Look("Amber Film", Grade(temperature = .32f, fade = .18f, grain = .16f, saturation = -.13f))
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

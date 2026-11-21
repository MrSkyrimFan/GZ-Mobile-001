package com.greenezone.chromavale.editor

data class Control(
    val name: String,
    val min: Float,
    val max: Float,
    val read: (Grade) -> Float,
    val write: (Grade, Float) -> Grade
)

enum class Panel(val title: String) { LIGHT("Light"), COLOR("Color"), DETAIL("Detail"), FILM("Film") }

object ControlCatalog {
    val groups: Map<Panel, List<Control>> = mapOf(
        Panel.LIGHT to listOf(
            Control("Exposure", -2f, 2f, { it.exposure }) { g, v -> g.copy(exposure = v) },
            Control("Contrast", -1f, 1f, { it.contrast }) { g, v -> g.copy(contrast = v) },
            Control("Highlights", -1f, 1f, { it.highlights }) { g, v -> g.copy(highlights = v) },
            Control("Shadows", -1f, 1f, { it.shadows }) { g, v -> g.copy(shadows = v) },
            Control("Whites", -1f, 1f, { it.whites }) { g, v -> g.copy(whites = v) },
            Control("Blacks", -1f, 1f, { it.blacks }) { g, v -> g.copy(blacks = v) },
            Control("Gamma", -1f, 1f, { it.gamma }) { g, v -> g.copy(gamma = v) }
        ),
        Panel.COLOR to listOf(
            Control("Saturation", -1f, 1f, { it.saturation }) { g, v -> g.copy(saturation = v) },
            Control("Vibrance", -1f, 1f, { it.vibrance }) { g, v -> g.copy(vibrance = v) },
            Control("Temperature", -1f, 1f, { it.temperature }) { g, v -> g.copy(temperature = v) },
            Control("Tint", -1f, 1f, { it.tint }) { g, v -> g.copy(tint = v) },
            Control("Hue", -1f, 1f, { it.hue }) { g, v -> g.copy(hue = v) },
            Control("Red balance", -1f, 1f, { it.red }) { g, v -> g.copy(red = v) },
            Control("Green balance", -1f, 1f, { it.green }) { g, v -> g.copy(green = v) },
            Control("Blue balance", -1f, 1f, { it.blue }) { g, v -> g.copy(blue = v) }
        ),
        Panel.DETAIL to listOf(
            Control("Clarity", -1f, 1f, { it.clarity }) { g, v -> g.copy(clarity = v) },
            Control("Sharpen", 0f, 1f, { it.sharpen }) { g, v -> g.copy(sharpen = v) }
        ),
        Panel.FILM to listOf(
            Control("Fade", 0f, 1f, { it.fade }) { g, v -> g.copy(fade = v) },
            Control("Vignette", 0f, 1f, { it.vignette }) { g, v -> g.copy(vignette = v) },
            Control("Grain", 0f, 1f, { it.grain }) { g, v -> g.copy(grain = v) }
        )
    )
}

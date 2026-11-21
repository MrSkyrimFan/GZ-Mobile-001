package com.greenezone.gzphotoeditor.editor

data class Control(
    val name: String,
    val min: Float,
    val max: Float,
    val read: (Grade) -> Float,
    val write: (Grade, Float) -> Grade
)

enum class Panel(val title: String) {
    LIGHT("Light"), COLOR("Color"), DETAIL("Detail"), FILM("Film"),
    GRADE("Color grade"), HSL("Color mixer")
}

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
        ),
        Panel.GRADE to listOf(
            Control("Shadow red", -1f, 1f, { it.shadowRed }) { g, v -> g.copy(shadowRed = v) },
            Control("Shadow green", -1f, 1f, { it.shadowGreen }) { g, v -> g.copy(shadowGreen = v) },
            Control("Shadow blue", -1f, 1f, { it.shadowBlue }) { g, v -> g.copy(shadowBlue = v) },
            Control("Midtone red", -1f, 1f, { it.midRed }) { g, v -> g.copy(midRed = v) },
            Control("Midtone green", -1f, 1f, { it.midGreen }) { g, v -> g.copy(midGreen = v) },
            Control("Midtone blue", -1f, 1f, { it.midBlue }) { g, v -> g.copy(midBlue = v) },
            Control("Highlight red", -1f, 1f, { it.highlightRed }) { g, v -> g.copy(highlightRed = v) },
            Control("Highlight green", -1f, 1f, { it.highlightGreen }) { g, v -> g.copy(highlightGreen = v) },
            Control("Highlight blue", -1f, 1f, { it.highlightBlue }) { g, v -> g.copy(highlightBlue = v) }
        ),
        Panel.HSL to emptyList()
    )

    val mixer: Map<ColorBand, List<Control>> = ColorBand.entries.associateWith { band ->
        val i = band.ordinal
        listOf(
            Control(band.label + " hue", -1f, 1f, { it.colorMix[i].hue }) { g, v ->
                val values = g.colorMix.toMutableList()
                values[i] = values[i].copy(hue = v)
                g.copy(colorMix = values)
            },
            Control(band.label + " saturation", -1f, 1f, { it.colorMix[i].saturation }) { g, v ->
                val values = g.colorMix.toMutableList()
                values[i] = values[i].copy(saturation = v)
                g.copy(colorMix = values)
            },
            Control(band.label + " luminance", -1f, 1f, { it.colorMix[i].luminance }) { g, v ->
                val values = g.colorMix.toMutableList()
                values[i] = values[i].copy(luminance = v)
                g.copy(colorMix = values)
            }
        )
    }

    val all: List<Control> = groups.values.flatten() + mixer.values.flatten()
}

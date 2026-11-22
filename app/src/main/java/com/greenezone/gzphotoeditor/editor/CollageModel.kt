package com.greenezone.gzphotoeditor.editor

import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** A collage is a set of distinct, independently editable photo documents. */
data class CollagePhoto(
    val uri: String,
    val edits: EditDocument = EditDocument()
)

enum class CollageTemplate(val label: String) {
    GRID("Grid"),
    SIDE_BY_SIDE("Side by side"),
    STACKED("Stacked"),
    FEATURED("Featured"),
    FILMSTRIP("Filmstrip"),
    MAGAZINE("Magazine"),
    MOSAIC("Mosaic")
}

enum class CollageAspect(val label: String, val ratio: Float) {
    SQUARE("1:1 Square", 1f),
    PORTRAIT("4:5 Portrait", .8f),
    STORY("9:16 Story", 9f / 16f),
    CLASSIC("3:2 Classic", 1.5f),
    LANDSCAPE("16:9 Landscape", 16f / 9f),
    PHOTO("4:3 Photo", 4f / 3f)
}

enum class CollageBackground(val label: String, val argb: Int) {
    BLACK("Black", -0x1000000),
    WHITE("White", -1),
    CHARCOAL("Charcoal", 0xFF242A31.toInt()),
    CREAM("Cream", 0xFFF5EEE1.toInt()),
    GREEN("Sage", 0xFF89B4A2.toInt()),
    PINK("Blush", 0xFFE6A9B8.toInt()),
    TRANSPARENT("Transparent", 0)
}

enum class CollageCellMode(val label: String) {
    FILL("Fill tiles"), FIT("Fit whole photos")
}

enum class CollageQuality(val label: String, val maxEdge: Int) {
    STANDARD("Standard · 1600", 1600),
    HIGH("High · 2600", 2600),
    ULTRA("Ultra · 4096", 4096)
}

data class CollageLayout(
    val template: CollageTemplate = CollageTemplate.GRID,
    val aspect: CollageAspect = CollageAspect.SQUARE,
    val gap: Float = .012f,
    val margin: Float = .025f,
    val cornerRadius: Float = .015f,
    val border: Float = 0f,
    val background: CollageBackground = CollageBackground.BLACK,
    val cellMode: CollageCellMode = CollageCellMode.FILL,
    val quality: CollageQuality = CollageQuality.HIGH
)

data class CollageDocument(
    val photos: List<CollagePhoto> = emptyList(),
    val layout: CollageLayout = CollageLayout()
)

data class CollageRect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    fun contains(x: Float, y: Float): Boolean =
        x >= left && x <= right && y >= top && y <= bottom
}

object CollageMath {
    const val MAX_PHOTOS = 20

    fun validCount(count: Int): Boolean = count in 1..MAX_PHOTOS

    private fun rows(count: Int, columns: Int): List<CollageRect> {
        val cols = columns.coerceIn(1, count)
        val rowCount = (count + cols - 1) / cols
        val rects = ArrayList<CollageRect>(count)
        for (row in 0 until rowCount) {
            val used = minOf(cols, count - row * cols)
            for (col in 0 until used) {
                rects.add(CollageRect(
                    col.toFloat() / used,
                    row.toFloat() / rowCount,
                    (col + 1f) / used,
                    (row + 1f) / rowCount
                ))
            }
        }
        return rects
    }

    private fun region(rect: CollageRect, x: Float, y: Float, w: Float, h: Float): CollageRect =
        CollageRect(x + rect.left * w, y + rect.top * h,
            x + rect.right * w, y + rect.bottom * h)

    /** Normalized, nonoverlapping outer cells, independent of display density. */
    fun frames(count: Int, layout: CollageLayout): List<CollageRect> {
        require(validCount(count)) { "Collages need between 1 and 20 photos" }
        val t = layout.template
        if (count == 1) return listOf(CollageRect(0f, 0f, 1f, 1f))
        return when (t) {
            CollageTemplate.SIDE_BY_SIDE -> rows(count, count)
            CollageTemplate.STACKED -> rows(count, 1)
            CollageTemplate.GRID -> {
                val bestColumns = ceil(sqrt(count * layout.aspect.ratio.toDouble()))
                    .toInt().coerceIn(1, count)
                rows(count, bestColumns)
            }
            CollageTemplate.FEATURED -> {
                val primary = CollageRect(0f, 0f, .59f, 1f)
                val rightColumns = ceil(sqrt((count - 1) * .41 *
                    layout.aspect.ratio.toDouble())).toInt().coerceIn(1, count - 1)
                listOf(primary) + rows(count - 1, rightColumns).map {
                    region(it, .59f, 0f, .41f, 1f)
                }
            }
            CollageTemplate.FILMSTRIP -> {
                val bottomRows = if (count > 8) 2 else 1
                val first = CollageRect(0f, 0f, 1f, .64f)
                val others = rows(count - 1, (count - 1 + bottomRows - 1) / bottomRows)
                listOf(first) + others.map { region(it, 0f, .64f, 1f, .36f) }
            }
            CollageTemplate.MAGAZINE -> {
                val upper = listOf(
                    CollageRect(0f, 0f, .5f, .53f),
                    CollageRect(.5f, 0f, 1f, .53f)
                ).take(count)
                if (count == 2) upper.map {
                    CollageRect(it.left, 0f, it.right, 1f)
                } else {
                    val bottomColumns = ceil(sqrt((count - 2) * layout.aspect.ratio.toDouble()))
                        .toInt().coerceIn(1, count - 2)
                    upper + rows(count - 2, bottomColumns).map {
                        region(it, 0f, .53f, 1f, .47f)
                    }
                }
            }
            CollageTemplate.MOSAIC -> {
                val strips = ArrayList<Int>()
                var remaining = count
                var step = 0
                while (remaining > 0) {
                    val take = minOf(remaining, when (step % 3) {
                        0 -> 2
                        1 -> 3
                        else -> 1
                    })
                    strips.add(take)
                    remaining -= take
                    step++
                }
                val cells = ArrayList<CollageRect>(count)
                var index = 0
                for (row in strips.indices) {
                    val cols = strips[row]
                    for (i in 0 until cols) {
                        cells.add(CollageRect(
                            i.toFloat() / cols,
                            row.toFloat() / strips.size,
                            (i + 1f) / cols,
                            (row + 1f) / strips.size
                        ))
                        index++
                    }
                }
                cells
            }
        }
    }

    /** Inner cells account for the actual canvas margins and gutters. */
    fun innerFrames(count: Int, layout: CollageLayout): List<CollageRect> {
        val outer = frames(count, layout)
        val margin = layout.margin.coerceIn(0f, .10f)
        val gap = layout.gap.coerceIn(0f, .08f)
        return outer.map { rect ->
            val contentWidth = 1f - 2f * margin
            val localW = rect.width * contentWidth
            val localH = rect.height * contentWidth
            // For 20 adjacent cells, huge gaps must not collapse a tile to zero pixels.
            val gapX = minOf(gap / 2f, localW * .24f)
            val gapY = minOf(gap / 2f, localH * .24f)
            val x0 = margin + rect.left * contentWidth +
                if (rect.left == 0f) 0f else gapX
            val x1 = margin + rect.right * contentWidth -
                if (rect.right == 1f) 0f else gapX
            val y0 = margin + rect.top * contentWidth +
                if (rect.top == 0f) 0f else gapY
            val y1 = margin + rect.bottom * contentWidth -
                if (rect.bottom == 1f) 0f else gapY
            CollageRect(x0.coerceIn(0f, 1f), y0.coerceIn(0f, 1f),
                x1.coerceIn(x0, 1f), y1.coerceIn(y0, 1f))
        }
    }

    fun select(x: Float, y: Float, count: Int, layout: CollageLayout): Int =
        if (!validCount(count)) -1 else innerFrames(count, layout).indexOfFirst {
            it.contains(x, y)
        }

    /** Same limits apply to preview and export, with device-specific pixel caps. */
    fun outputSize(layout: CollageLayout, maxEdge: Int, maxPixels: Long): Pair<Int, Int> {
        require(maxEdge >= 16 && maxPixels >= 256)
        val ratio = layout.aspect.ratio.toDouble()
        var height = if (ratio >= 1.0) maxEdge.toDouble() / ratio else maxEdge.toDouble()
        var width = height * ratio
        val scale = minOf(1.0, sqrt(maxPixels.toDouble() / (width * height)))
        width *= scale
        height *= scale
        return width.roundToInt().coerceAtLeast(1) to height.roundToInt().coerceAtLeast(1)
    }

    fun move(photos: List<CollagePhoto>, from: Int, to: Int): List<CollagePhoto> {
        if (from !in photos.indices || to !in photos.indices || from == to) return photos
        return photos.toMutableList().apply {
            add(to, removeAt(from))
        }
    }
}

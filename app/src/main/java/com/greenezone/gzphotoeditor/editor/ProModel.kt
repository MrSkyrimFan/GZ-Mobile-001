package com.greenezone.gzphotoeditor.editor

/** Original bitmap files and editable vector layers coexist in one ordered stack. */
enum class ProLayerKind(val label: String) {
    IMAGE("Image"), TEXT("Text"), SHAPE("Shape"), DRAWING("Brush"), ADJUSTMENT("Adjustment")
}

enum class ProBlend(val label: String) {
    NORMAL("Normal"), MULTIPLY("Multiply"), SCREEN("Screen"), OVERLAY("Overlay")
}
enum class ProAspect(val label: String, val ratio: Float) {
    SQUARE("Square 1:1", 1f), PORTRAIT("Portrait 4:5", .8f),
    STORY("Story 9:16", 9f / 16f), LANDSCAPE("Wide 16:9", 16f / 9f)
}
enum class ProBackground(val label: String, val argb: Int) {
    BLACK("Black", -0x1000000), WHITE("White", -1),
    TRANSPARENT("Transparent", 0)
}
data class ProLayer(
    val uri: String,
    val edits: EditDocument = EditDocument(),
    val name: String = "Image",
    val x: Float = .5f,
    val y: Float = .5f,
    val scale: Float = 1f,
    val opacity: Float = 1f,
    val visible: Boolean = true,
    val blend: ProBlend = ProBlend.NORMAL,
    val kind: ProLayerKind = ProLayerKind.IMAGE,
    val aspect: Float = 1f,
    val rotationDegrees: Float = 0f,
    val mask: ProSelection? = null,
    val retouches: List<ProRetouchMark> = emptyList(),
    val effect: ProPixelEffect = ProPixelEffect.NONE,
    val effectAmount: Float = .5f
)
data class ProDocument(
    val layers: List<ProLayer> = emptyList(),
    val aspect: ProAspect = ProAspect.SQUARE,
    val background: ProBackground = ProBackground.WHITE
)
data class ProHistory(
    val undo: List<ProDocument> = emptyList(),
    val redo: List<ProDocument> = emptyList()
) {
    fun checkpoint(before: ProDocument, after: ProDocument): ProHistory =
        if (before == after) this else copy(undo = (undo + before).takeLast(35), redo = emptyList())
    fun undo(now: ProDocument): Pair<ProDocument, ProHistory>? =
        undo.lastOrNull()?.let { it to copy(undo = undo.dropLast(1), redo = (redo + now).takeLast(35)) }
    fun redo(now: ProDocument): Pair<ProDocument, ProHistory>? =
        redo.lastOrNull()?.let { it to copy(redo = redo.dropLast(1), undo = (undo + now).takeLast(35)) }
}
data class ProRect(val left: Float, val top: Float, val right: Float, val bottom: Float)

object ProMath {
    const val MAX_LAYERS = 12
    fun valid(document: ProDocument): Boolean =
        document.layers.size in 1..MAX_LAYERS &&
            document.layers.all { item ->
                val sourceValid = when (item.kind) {
                    ProLayerKind.IMAGE -> item.uri.startsWith("content://") && item.uri.length <= 4096
                    ProLayerKind.TEXT -> item.uri.isEmpty() &&
                        item.edits.creative.text.size == 1 &&
                        item.edits.creative.text[0].content.length <= 500
                    ProLayerKind.SHAPE -> item.uri.isEmpty() && item.edits.creative.shapes.size == 1
                    ProLayerKind.DRAWING -> item.uri.isEmpty() &&
                        item.edits.creative.strokes.size <= CreativeRaster.MAX_INK &&
                        item.edits.creative.strokes.all { it.points.size <= 500 }
                    ProLayerKind.ADJUSTMENT -> item.uri.isEmpty()
                }
                sourceValid && item.name.isNotBlank() && item.name.length <= 42 &&
                    item.x.isFinite() && item.y.isFinite() && item.x in 0f..1f &&
                    item.y in 0f..1f && item.scale.isFinite() &&
                    item.scale in .1f..2f && item.opacity.isFinite() && item.opacity in 0f..1f &&
                    item.aspect.isFinite() && item.aspect in .1f..10f &&
                    item.rotationDegrees.isFinite() && item.rotationDegrees in -180f..180f &&
                    (item.mask == null || (item.kind == ProLayerKind.IMAGE &&
                        ProToolMath.valid(item.mask))) &&
                    item.retouches.size <= ProToolMath.MAX_RETOUCH &&
                    (item.retouches.isEmpty() || item.kind == ProLayerKind.IMAGE) &&
                    item.retouches.all(ProToolMath::valid) &&
                    item.effectAmount.isFinite() && item.effectAmount in 0f..1f &&
                    (item.effect == ProPixelEffect.NONE ||
                        item.kind == ProLayerKind.ADJUSTMENT)
            }

    fun dimensions(aspect: ProAspect, maxEdge: Int, maxPixels: Long): Pair<Int, Int> {
        require(maxEdge in 64..5600 && maxPixels in 1024L..24_000_000L)
        val w = if (aspect.ratio >= 1f) maxEdge else (maxEdge * aspect.ratio).toInt()
        val h = if (aspect.ratio >= 1f) (maxEdge / aspect.ratio).toInt() else maxEdge
        val k = kotlin.math.min(1.0, kotlin.math.sqrt(maxPixels.toDouble() / (w.toLong() * h)))
        return ((w * k).toInt().coerceAtLeast(1)) to ((h * k).toInt().coerceAtLeast(1))
    }

    fun bounds(layer: ProLayer, canvasW: Int, canvasH: Int, imageW: Int, imageH: Int): ProRect {
        require(canvasW > 0 && canvasH > 0 && imageW > 0 && imageH > 0)
        val fit = kotlin.math.min(canvasW.toFloat() / imageW, canvasH.toFloat() / imageH)
        val halfW = imageW * fit * layer.scale.coerceIn(.1f, 2f) / 2f
        val halfH = imageH * fit * layer.scale.coerceIn(.1f, 2f) / 2f
        val x = layer.x.coerceIn(0f, 1f) * canvasW
        val y = layer.y.coerceIn(0f, 1f) * canvasH
        return ProRect(x - halfW, y - halfH, x + halfW, y + halfH)
    }

    fun insert(document: ProDocument, layer: ProLayer): ProDocument? =
        if (document.layers.size >= MAX_LAYERS) null
        else document.copy(layers = document.layers + layer)

    /** Index zero is the bottommost layer; final element is the topmost. */
    fun move(layers: List<ProLayer>, from: Int, to: Int): List<ProLayer> {
        if (from !in layers.indices || to !in layers.indices || from == to) return layers
        return layers.toMutableList().apply { add(to, removeAt(from)) }
    }
}

package com.greenezone.gzphotoeditor.editor

enum class GifSize(val label: String, val maxEdge: Int) {
    SOURCE("Original size", 4096),
    SMALL("Small · 320 px", 320),
    MEDIUM("Medium · 480 px", 480),
    LARGE("Large · 640 px", 640)
}
enum class GifFit(val label: String) { FIT("Fit whole photo"), FILL("Fill frame") }
enum class GifAspect(val label: String, val ratio: Float) {
    SOURCE("Original aspect", 0f), SQUARE("Square", 1f), PORTRAIT("Portrait", .8f), LANDSCAPE("Landscape", 16f / 9f)
}
data class GifDocument(
    val frames: List<String> = emptyList(),
    val delayMs: Int = 350,
    val size: GifSize = if (frames.isEmpty()) GifSize.SOURCE else GifSize.MEDIUM,
    val fit: GifFit = GifFit.FIT,
    val aspect: GifAspect = if (frames.isEmpty()) GifAspect.SOURCE else GifAspect.SQUARE,
    val forever: Boolean = frames.isNotEmpty(),
    val video: GifVideoClip? = null,
    val edits: EditDocument = EditDocument(),
    val repeatCount: Int? = null
) {
    val loopCount: Int get() = if (forever) 0 else repeatCount ?: -1
}
data class GifUndoStack<T>(
    val undo: List<T> = emptyList(),
    val redo: List<T> = emptyList()
) {
    fun checkpoint(before: T, after: T): GifUndoStack<T> =
        if (before == after) this else copy(undo = (undo + before).takeLast(30), redo = emptyList())

    fun undo(now: T): Pair<T, GifUndoStack<T>>? =
        undo.lastOrNull()?.let { it to copy(undo = undo.dropLast(1), redo = (redo + now).takeLast(30)) }

    fun redo(now: T): Pair<T, GifUndoStack<T>>? =
        redo.lastOrNull()?.let { it to copy(redo = redo.dropLast(1), undo = (undo + now).takeLast(30)) }
}

typealias GifHistory = GifUndoStack<GifDocument>

object GifMath {
    const val MAX_FRAMES = 20
    fun valid(doc: GifDocument): Boolean =
        if (doc.video != null) doc.frames.isEmpty() && GifVideoMath.valid(doc.video)
        else doc.frames.size in 2..MAX_FRAMES && doc.delayMs in 100..1500 &&
            doc.frames.all { it.startsWith("content://") && it.length <= 4096 }
    fun dimensions(doc: GifDocument, maxEdge: Int? = null): Pair<Int, Int> {
        var width = doc.video?.width ?: 480
        var height = doc.video?.height ?: 480
        if (doc.edits.quarterTurns % 2 != 0) {
            val swapped = width
            width = height
            height = swapped
        }
        val sourceEdge = maxOf(width, height)
        val edge = (if (doc.size == GifSize.SOURCE) sourceEdge else doc.size.maxEdge)
            .coerceAtMost(maxEdge ?: 65535)
        val ratio = if (doc.aspect == GifAspect.SOURCE) width.toDouble() / height
            else doc.aspect.ratio.toDouble()
        return if (ratio >= 1) edge to Math.round(edge / ratio).toInt().coerceAtLeast(1)
            else Math.round(edge * ratio).toInt().coerceAtLeast(1) to edge
    }
    fun move(frames: List<String>, from: Int, to: Int): List<String> {
        if (from !in frames.indices || to !in frames.indices || from == to) return frames
        return frames.toMutableList().apply { add(to, removeAt(from)) }
    }
}

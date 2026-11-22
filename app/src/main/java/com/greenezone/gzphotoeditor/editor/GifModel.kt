package com.greenezone.gzphotoeditor.editor

enum class GifSize(val label: String, val maxEdge: Int) {
    SMALL("Small · 320 px", 320),
    MEDIUM("Medium · 480 px", 480),
    LARGE("Large · 640 px", 640)
}
enum class GifFit(val label: String) { FIT("Fit whole photo"), FILL("Fill frame") }
enum class GifAspect(val label: String, val ratio: Float) {
    SQUARE("Square", 1f), PORTRAIT("Portrait", .8f), LANDSCAPE("Landscape", 16f / 9f)
}
data class GifDocument(
    val frames: List<String> = emptyList(),
    val delayMs: Int = 350,
    val size: GifSize = GifSize.MEDIUM,
    val fit: GifFit = GifFit.FIT,
    val aspect: GifAspect = GifAspect.SQUARE,
    val forever: Boolean = true,
    val video: GifVideoClip? = null
)
data class GifHistory(val undo: List<GifDocument> = emptyList(),
                      val redo: List<GifDocument> = emptyList()) {
    fun checkpoint(before: GifDocument, after: GifDocument): GifHistory =
        if (before == after) this else copy(undo = (undo + before).takeLast(30), redo = emptyList())
    fun undo(now: GifDocument): Pair<GifDocument, GifHistory>? =
        undo.lastOrNull()?.let { it to copy(undo = undo.dropLast(1), redo = (redo + now).takeLast(30)) }
    fun redo(now: GifDocument): Pair<GifDocument, GifHistory>? =
        redo.lastOrNull()?.let { it to copy(redo = redo.dropLast(1), undo = (undo + now).takeLast(30)) }
}
object GifMath {
    const val MAX_FRAMES = 20
    fun valid(doc: GifDocument): Boolean =
        if (doc.video != null) doc.frames.isEmpty() && GifVideoMath.valid(doc.video)
        else doc.frames.size in 2..MAX_FRAMES && doc.delayMs in 100..1500 &&
            doc.frames.all { it.startsWith("content://") && it.length <= 4096 }
    fun dimensions(doc: GifDocument): Pair<Int, Int> {
        val edge = doc.size.maxEdge
        return if (doc.aspect.ratio >= 1f) edge to (edge / doc.aspect.ratio).toInt()
        else (edge * doc.aspect.ratio).toInt() to edge
    }
    fun move(frames: List<String>, from: Int, to: Int): List<String> {
        if (from !in frames.indices || to !in frames.indices || from == to) return frames
        return frames.toMutableList().apply { add(to, removeAt(from)) }
    }
}

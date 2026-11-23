package com.greenezone.gzphotoeditor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.greenezone.gzphotoeditor.editor.BrushPoint
import com.greenezone.gzphotoeditor.editor.CropFrame
import com.greenezone.gzphotoeditor.editor.CropHandle
import com.greenezone.gzphotoeditor.editor.CropMath
import com.greenezone.gzphotoeditor.editor.InkStroke
import kotlin.math.abs
import kotlin.math.min

private data class ImageSpace(val left: Float, val top: Float, val w: Float, val h: Float) {
    fun point(p: Offset): BrushPoint? {
        if (w <= 0f || h <= 0f) return null
        val x = (p.x - left) / w
        val y = (p.y - top) / h
        return if (x in 0f..1f && y in 0f..1f) BrushPoint(x, y) else null
    }
    fun clamp(p: Offset): BrushPoint =
        BrushPoint(((p.x - left) / w).coerceIn(0f, 1f),
            ((p.y - top) / h).coerceIn(0f, 1f))
}

private fun imageSpace(
    outerWidth: Float, outerHeight: Float, imageW: Int, imageH: Int, inset: Float = 0f
): ImageSpace {
    val width = (outerWidth - inset * 2).coerceAtLeast(0f)
    val height = (outerHeight - inset * 2).coerceAtLeast(0f)
    val scale = min(width / imageW, height / imageH)
    val w = imageW * scale
    val h = imageH * scale
    return ImageSpace((outerWidth - w) / 2f, (outerHeight - h) / 2f, w, h)
}

@Composable
fun CropFrameOverlay(
    imageWidth: Int,
    imageHeight: Int,
    frame: CropFrame,
    aspectRatio: Float?,
    showGuides: Boolean,
    onChange: (CropFrame) -> Unit,
    onFinished: () -> Unit,
    onCanceled: () -> Unit,
    onTap: () -> Unit,
    imageInset: Dp = 0.dp
) {
    val latestFrame by rememberUpdatedState(frame)
    val change by rememberUpdatedState(onChange)
    val finished by rememberUpdatedState(onFinished)
    val canceled by rememberUpdatedState(onCanceled)
    val tapped by rememberUpdatedState(onTap)
    Canvas(
        Modifier.fillMaxSize()
            .testTag("crop-frame")
            .pointerInput(aspectRatio, imageWidth, imageHeight, imageInset) {
                val radius = 32.dp.toPx()
                val insetPixels = imageInset.roundToPx().toFloat()
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val startSize = size
                    val img = imageSpace(startSize.width.toFloat(), startSize.height.toFloat(),
                        imageWidth, imageHeight, insetPixels)
                    val start = CropMath.sanitize(latestFrame)
                    val handle = CropMath.hitTest(start, down.position.x - img.left,
                        down.position.y - img.top, img.w, img.h, radius)
                    down.consume()
                    var moved = false
                    var changed = false
                    var completed = false
                    try {
                        do {
                            val event = awaitPointerEvent()
                            val pointer = event.changes.singleOrNull() ?: return@awaitEachGesture
                            if (size != startSize || pointer.id != down.id || pointer.isConsumed)
                                return@awaitEachGesture
                            val delta = pointer.position - down.position
                            moved = moved || delta.getDistance() > viewConfiguration.touchSlop
                            if (moved && handle != null) {
                                val next = CropMath.drag(start, handle, delta.x / img.w, delta.y / img.h,
                                    imageWidth, imageHeight, aspectRatio)
                                if (changed || next != start) {
                                    changed = true
                                    change(next)
                                }
                            }
                            pointer.consume()
                        } while (pointer.pressed)
                        completed = true
                        if (changed) finished()
                        else if (!moved && (handle == null || handle == CropHandle.MOVE)) tapped()
                    } finally {
                        if (!completed && changed) canceled()
                    }
                }
            }
    ) {
        if (!showGuides) return@Canvas
        val space = imageSpace(size.width, size.height, imageWidth, imageHeight,
            imageInset.roundToPx().toFloat())
        val f = CropMath.sanitize(frame)
        val l = space.left + f.left * space.w
        val t = space.top + f.top * space.h
        val r = space.left + f.right * space.w
        val b = space.top + f.bottom * space.h
        val shade = Color.Black.copy(alpha = .49f)
        drawRect(shade, Offset(space.left, space.top), Size(space.w, t - space.top))
        drawRect(shade, Offset(space.left, b), Size(space.w, space.top + space.h - b))
        drawRect(shade, Offset(space.left, t), Size(l - space.left, b - t))
        drawRect(shade, Offset(r, t), Size(space.left + space.w - r, b - t))
        drawRect(Color.White, Offset(l, t), Size(r - l, b - t), style = Stroke(1.5.dp.toPx()))
        for (n in 1..2) {
            val xx = l + (r - l) * n / 3f
            val yy = t + (b - t) * n / 3f
            drawLine(Color.White.copy(alpha = .58f), Offset(xx, t), Offset(xx, b), 1.dp.toPx())
            drawLine(Color.White.copy(alpha = .58f), Offset(l, yy), Offset(r, yy), 1.dp.toPx())
        }
        val handles = listOf(
            Offset(l, t), Offset(r, t), Offset(l, b), Offset(r, b),
            Offset((l + r) / 2f, t), Offset((l + r) / 2f, b),
            Offset(l, (t + b) / 2f), Offset(r, (t + b) / 2f)
        )
        for (p in handles) {
            drawCircle(Color.White, radius = 6.dp.toPx(), center = p)
            drawCircle(Color(0xFF253A37), radius = 2.5.dp.toPx(), center = p)
        }
    }
}

@Composable
fun EditableObjectOverlay(
    imageWidth: Int,
    imageHeight: Int,
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    showGuides: Boolean,
    onChanged: (Float, Float, Float, Float) -> Unit,
    onFinished: () -> Unit,
    onTap: () -> Unit
) {
    val latest by rememberUpdatedState(floatArrayOf(centerX, centerY, width, height))
    val change by rememberUpdatedState(onChanged)
    val finish by rememberUpdatedState(onFinished)
    val tap by rememberUpdatedState(onTap)
    Canvas(
        Modifier.fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onTap = { tap() }) }
            .pointerInput(imageWidth, imageHeight) {
                val img = imageSpace(size.width.toFloat(), size.height.toFloat(), imageWidth, imageHeight)
                var corner = false
                var active = false
                detectDragGestures(
                    onDragStart = { pos ->
                        val p = img.point(pos)
                        val f = latest
                        val cx = f[0]; val cy = f[1]
                        val w = f[2]; val h = f[3]
                        val dx = p?.x ?: -1f
                        val dy = p?.y ?: -1f
                        corner = abs(dx - (cx + w / 2f)) * img.w < 38f &&
                            abs(dy - (cy + h / 2f)) * img.h < 38f
                        active = p != null && (corner ||
                            (dx in cx - w / 2f..cx + w / 2f &&
                                dy in cy - h / 2f..cy + h / 2f))
                    },
                    onDragEnd = { if (active) finish(); active = false },
                    onDragCancel = { if (active) finish(); active = false },
                    onDrag = { event, delta ->
                        if (!active) return@detectDragGestures
                        val f = latest
                        val dx = delta.x / img.w
                        val dy = delta.y / img.h
                        if (corner) {
                            change(f[0], f[1], (f[2] + 2f * dx).coerceIn(.05f, 1f),
                                (f[3] + 2f * dy).coerceIn(.04f, 1f))
                        } else {
                            change((f[0] + dx).coerceIn(0f, 1f),
                                (f[1] + dy).coerceIn(0f, 1f), f[2], f[3])
                        }
                        event.consume()
                    }
                )
            }
    ) {
        if (!showGuides) return@Canvas
        val img = imageSpace(size.width, size.height, imageWidth, imageHeight)
        val w = width * img.w
        val h = height * img.h
        val left = img.left + centerX * img.w - w / 2f
        val top = img.top + centerY * img.h - h / 2f
        drawRect(Color(0xFF9CE9CB), Offset(left, top), Size(w, h), style = Stroke(2f))
        drawCircle(Color(0xFF9CE9CB), 10f, Offset(left + w, top + h))
        drawCircle(Color.Black, 4f, Offset(left + w, top + h))
    }
}

@Composable
fun DrawTouchOverlay(
    imageWidth: Int,
    imageHeight: Int,
    ink: InkStroke? = null,
    onStart: (BrushPoint) -> Unit,
    onMove: (BrushPoint) -> Unit,
    onFinish: () -> Unit,
    onTap: () -> Unit
) {
    val start by rememberUpdatedState(onStart)
    val move by rememberUpdatedState(onMove)
    val finish by rememberUpdatedState(onFinish)
    val tap by rememberUpdatedState(onTap)
    Canvas(
        Modifier.fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onTap = { tap() }) }
            .pointerInput(imageWidth, imageHeight) {
                val img = imageSpace(size.width.toFloat(), size.height.toFloat(), imageWidth, imageHeight)
                var drawing = false
                detectDragGestures(
                    onDragStart = { p ->
                        drawing = false
                        img.point(p)?.let { drawing = true; start(it) }
                    },
                    onDragEnd = { if (drawing) finish(); drawing = false },
                    onDragCancel = { if (drawing) finish(); drawing = false },
                    onDrag = { event, _ ->
                        if (drawing) {
                            move(img.clamp(event.position))
                            event.consume()
                        }
                    }
                )
            }
    ) {
        val stroke = ink
        if (stroke != null && stroke.points.isNotEmpty()) {
            val space = imageSpace(size.width, size.height, imageWidth, imageHeight)
            val path = Path()
            stroke.points.forEachIndexed { index, point ->
                val x = space.left + point.x * space.w
                val y = space.top + point.y * space.h
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            val color = Color(stroke.color).copy(alpha = stroke.opacity.coerceIn(0f, 1f))
            val sizePx = stroke.thickness * min(space.w, space.h)
            if (stroke.points.size == 1) {
                drawCircle(color, sizePx / 2f,
                    Offset(space.left + stroke.points[0].x * space.w,
                        space.top + stroke.points[0].y * space.h))
            } else {
                drawPath(path, color, style = Stroke(sizePx,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round))
            }
        }
    }
}

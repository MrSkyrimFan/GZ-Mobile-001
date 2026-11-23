package com.greenezone.gzphotoeditor.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import com.greenezone.gzphotoeditor.editor.*
import kotlin.math.min

internal data class ProViewport(val zoom: Float = 1f, val x: Float = .5f, val y: Float = .5f)

internal val ProViewportSemanticsKey =
    SemanticsPropertyKey<ProViewport>("ProViewport")
private var SemanticsPropertyReceiver.proViewportForTesting by ProViewportSemanticsKey

private class CanvasFrame(
    width: Float, height: Float, imageWidth: Int, imageHeight: Int, view: ProViewport
) {
    val scale = min(width / imageWidth, height / imageHeight) * view.zoom
    val w = imageWidth * scale
    val h = imageHeight * scale
    val left = width / 2 - view.x * w
    val top = height / 2 - view.y * h
    fun point(position: Offset) = BrushPoint((position.x - left) / w, (position.y - top) / h)
}

internal data class ProCanvasActions(
    val toggleControls: () -> Unit,
    val hold: (BrushPoint) -> Unit,
    val dragStart: (BrushPoint) -> Unit,
    val drag: (BrushPoint, BrushPoint) -> Unit,
    val dragEnd: (cancel: Boolean) -> Unit,
    val changeView: (ProViewport) -> Unit
)

/** The bitmap, guides, hit targets and brush share the same document-to-screen transform. */
@Composable
internal fun ProCanvasSurface(
    bitmap: Bitmap?,
    artboardSize: Pair<Int, Int>,
    background: Int,
    section: ProSection,
    layer: ProLayer?,
    view: ProViewport,
    controlsVisible: Boolean,
    enabled: Boolean,
    draft: ProSelection?,
    source: BrushPoint?,
    actions: ProCanvasActions,
    modifier: Modifier = Modifier
) {
    val events by rememberUpdatedState(actions)
    val currentSize by rememberUpdatedState(artboardSize)
    val currentView by rememberUpdatedState(view)
    val bitmapPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG) }
    val toggleLabel = if (controlsVisible) "Hide controls" else "Show controls"
    val (width, height) = artboardSize
    Canvas(modifier.testTag("pro-canvas").semantics {
        contentDescription = if (enabled) "Pro editable composition" else "Pro composition preview"
        proViewportForTesting = view
        if (enabled) onClick(label = toggleLabel) { events.toggleControls(); true }
    }.pointerInput(section, enabled) {
        if (!enabled) return@pointerInput
        awaitEachGesture {
            val down = awaitFirstDown()
            val (documentW, documentH) = currentSize
            val screenW = size.width.toFloat()
            val screenH = size.height.toFloat()
            var viewport = currentView
            val initial = CanvasFrame(screenW, screenH, documentW, documentH, viewport)
            val start = initial.point(down.position)
            val startsInside = start.x in 0f..1f && start.y in 0f..1f
            var navigating = false
            var dragging = false
            down.consume()
            try {
                do {
                    val event = awaitPointerEvent()
                    if (event.changes.size > 1) {
                        navigating = true
                        if (dragging) {
                            events.dragEnd(true)
                            dragging = false
                        }
                        if (event.changes.count { it.previousPressed && it.pressed } >= 2) {
                            val centroid = event.calculateCentroid()
                            val previous = centroid - event.calculatePan()
                            val oldFrame = CanvasFrame(screenW, screenH, documentW, documentH, viewport)
                            val anchor = oldFrame.point(previous)
                            val zoom = (viewport.zoom * event.calculateZoom()).coerceIn(1f, 12f)
                            val frame = CanvasFrame(screenW, screenH, documentW, documentH, ProViewport(zoom))
                            fun center(anchorValue: Float, pos: Float, screen: Float, extent: Float): Float {
                                val margin = (.5f * screen / extent).coerceAtMost(.5f)
                                return (anchorValue - (pos - screen / 2) / extent)
                                    .coerceIn(margin, 1f - margin)
                            }
                            viewport = ProViewport(zoom,
                                center(anchor.x, centroid.x, screenW, frame.w),
                                center(anchor.y, centroid.y, screenH, frame.h))
                            events.changeView(viewport)
                        }
                    } else if (!navigating) {
                        val change = event.changes.first()
                        val moved = (change.position - down.position).getDistance()
                        if (!dragging && moved > viewConfiguration.touchSlop) {
                            dragging = true
                            if (startsInside) events.dragStart(start)
                        }
                        if (dragging && startsInside) {
                            val point = initial.point(change.position)
                            events.drag(start, BrushPoint(point.x.coerceIn(0f, 1f),
                                point.y.coerceIn(0f, 1f)))
                        } else if (!dragging && !change.pressed) {
                            if (change.uptimeMillis - down.uptimeMillis >= viewConfiguration.longPressTimeoutMillis) {
                                if (startsInside) events.hold(start)
                            } else events.toggleControls()
                        }
                    }
                    event.changes.forEach { it.consume() }
                } while (event.changes.any { it.pressed })
                if (dragging && startsInside) {
                    dragging = false
                    events.dragEnd(false)
                }
            } finally {
                if (dragging && startsInside) events.dragEnd(true)
            }
        }
    }) {
        val frame = CanvasFrame(size.width, size.height, width, height, view)
        val canvas = drawContext.canvas.nativeCanvas
        val save = canvas.save()
        try {
            canvas.translate(frame.left, frame.top)
            canvas.scale(frame.scale, frame.scale)
            canvas.clipRect(0, 0, width, height)
            if (bitmap == null) {
                canvas.drawColor(background)
            } else {
                canvas.drawBitmap(bitmap, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), bitmapPaint)
            }
            val guide = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2.dp.toPx() / frame.scale
                color = 0xFF96E5C2.toInt()
            }
            if ((section == ProSection.LAYERS || section == ProSection.MOVE) && layer?.visible == true)
                drawLayerBounds(canvas, layer, width, height, guide)
            if (section == ProSection.SELECT && layer?.visible == true && layer.opacity > 0f) {
                layer?.mask?.let { canvas.drawPath(ProToolMath.projectedPath(it, layer, width, height), guide) }
                if (draft != null && ProToolMath.valid(draft)) {
                    guide.color = android.graphics.Color.YELLOW
                    canvas.drawPath(ProToolMath.path(draft, width, height), guide)
                }
            }
            if (section == ProSection.RETOUCH && source != null) {
                val x = source.x * width
                val y = source.y * height
                val r = 8.dp.toPx() / frame.scale
                guide.color = android.graphics.Color.YELLOW
                canvas.drawCircle(x, y, r, guide)
                canvas.drawLine(x - r * 1.5f, y, x + r * 1.5f, y, guide)
                canvas.drawLine(x, y - r * 1.5f, x, y + r * 1.5f, guide)
            }
        } finally { canvas.restoreToCount(save) }
    }
}

private fun drawLayerBounds(canvas: Canvas, layer: ProLayer, w: Int, h: Int, paint: Paint) {
    val cx = layer.x * w
    val cy = layer.y * h
    val rect = when (layer.kind) {
        ProLayerKind.IMAGE -> ProMath.bounds(layer, w, h)
            .let { RectF(it.left, it.top, it.right, it.bottom) }
        ProLayerKind.TEXT, ProLayerKind.SHAPE -> {
            val text = layer.edits.creative.text.firstOrNull()
            val shape = layer.edits.creative.shapes.firstOrNull()
            val halfW = ((text?.width ?: shape?.width ?: return) * layer.scale).coerceAtMost(1f) * w / 2
            val halfH = ((text?.height ?: shape?.height ?: return) * layer.scale).coerceAtMost(1f) * h / 2
            RectF(cx - halfW, cy - halfH, cx + halfW, cy + halfH)
        }
        else -> return
    }
    val save = canvas.save()
    canvas.rotate(layer.rotationDegrees, cx, cy)
    canvas.drawRect(rect, paint)
    canvas.restoreToCount(save)
}

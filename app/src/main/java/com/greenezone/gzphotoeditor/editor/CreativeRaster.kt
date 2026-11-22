package com.greenezone.gzphotoeditor.editor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

object CreativeRaster {
    const val MAX_TEXT = 32
    const val MAX_SHAPES = 64
    const val MAX_INK = 160

    fun isEmpty(creative: CreativeDocument): Boolean =
        creative.text.isEmpty() && creative.strokes.isEmpty() && creative.shapes.isEmpty()

    /** Mutates ONLY a bitmap owned by the caller. Never call on the imported source. */
    fun drawInto(target: Bitmap, creative: CreativeDocument, cancel: (() -> Unit)? = null) {
        if (isEmpty(creative)) return
        require(target.isMutable) { "Compositing requires a caller-owned mutable bitmap" }
        val canvas = Canvas(target)
        val shorter = min(target.width, target.height).toFloat().coerceAtLeast(1f)
        for (item in creative.shapes.take(MAX_SHAPES)) {
            cancel?.invoke()
            drawShape(canvas, item, target.width, target.height, shorter)
        }
        for (item in creative.strokes.take(MAX_INK)) {
            cancel?.invoke()
            drawStroke(canvas, item, target.width, target.height, shorter)
        }
        for (item in creative.text.take(MAX_TEXT)) {
            cancel?.invoke()
            drawText(canvas, item, target.width, target.height, shorter)
        }
    }

    private fun paint(color: Int, alpha: Float): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color or -0x1000000
        this.alpha = (255f * alpha.coerceIn(0f, 1f)).roundToInt()
        isDither = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private fun drawStroke(c: Canvas, ink: InkStroke, w: Int, h: Int, short: Float) {
        if (ink.points.isEmpty()) return
        val path = Path()
        ink.points.take(2000).forEachIndexed { index, p ->
            val x = p.x.coerceIn(0f, 1f) * w
            val y = p.y.coerceIn(0f, 1f) * h
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        val brush = paint(ink.color, ink.opacity).apply {
            style = Paint.Style.STROKE
            strokeWidth = ink.thickness.coerceIn(.0015f, .16f) * short
        }
        if (ink.points.size == 1) {
            c.drawCircle(ink.points[0].x * w, ink.points[0].y * h,
                brush.strokeWidth / 2f, brush)
        } else c.drawPath(path, brush)
    }

    private fun drawShape(c: Canvas, shape: ShapeOverlay, w: Int, h: Int, short: Float) {
        val halfW = shape.width.coerceIn(.018f, 1f) * w / 2f
        val halfH = shape.height.coerceIn(.018f, 1f) * h / 2f
        val brush = paint(shape.color, shape.opacity).apply {
            style = if (shape.filled && shape.kind != ShapeKind.LINE &&
                shape.kind != ShapeKind.ARROW) Paint.Style.FILL else Paint.Style.STROKE
            strokeWidth = shape.thickness.coerceIn(.002f, .10f) * short
        }
        c.save()
        c.translate(shape.x.coerceIn(0f, 1f) * w, shape.y.coerceIn(0f, 1f) * h)
        c.rotate(shape.rotation.coerceIn(-180f, 180f))
        when (shape.kind) {
            ShapeKind.RECTANGLE -> c.drawRect(-halfW, -halfH, halfW, halfH, brush)
            ShapeKind.ELLIPSE -> c.drawOval(-halfW, -halfH, halfW, halfH, brush)
            ShapeKind.LINE -> c.drawLine(-halfW, 0f, halfW, 0f, brush)
            ShapeKind.ARROW -> {
                c.drawLine(-halfW, 0f, halfW, 0f, brush)
                val point = minOf(halfH, halfW * .55f)
                c.drawLine(halfW, 0f, halfW - point, -point * .6f, brush)
                c.drawLine(halfW, 0f, halfW - point, point * .6f, brush)
            }
            ShapeKind.TRIANGLE -> {
                val path = Path().apply {
                    moveTo(0f, -halfH)
                    lineTo(halfW, halfH)
                    lineTo(-halfW, halfH)
                    close()
                }
                c.drawPath(path, brush)
            }
            ShapeKind.STAR -> {
                val path = Path()
                for (index in 0..9) {
                    val angle = -PI / 2 + index * PI / 5
                    val r = if (index % 2 == 0) 1.0 else .45
                    val x = (cos(angle) * halfW * r).toFloat()
                    val y = (sin(angle) * halfH * r).toFloat()
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                c.drawPath(path, brush)
            }
        }
        c.restore()
    }

    private fun lines(content: String, textPaint: Paint, maxWidth: Float): List<String> {
        val wrapped = ArrayList<String>()
        val cleaned = content.take(500)
        for (paragraph in cleaned.split('\n')) {
            if (paragraph.isEmpty()) { wrapped.add(""); continue }
            var line = ""
            for (word in paragraph.split(' ')) {
                val test = if (line.isEmpty()) word else line + " " + word
                if (line.isNotEmpty() && textPaint.measureText(test) > maxWidth) {
                    wrapped.add(line)
                    line = word
                } else line = test
                // Long unbroken words may need splitting rather than overflowing.
                while (line.length > 1 && textPaint.measureText(line) > maxWidth) {
                    val fit = textPaint.breakText(line, true, maxWidth, null)
                        .coerceIn(1, line.length - 1)
                    wrapped.add(line.substring(0, fit))
                    line = line.substring(fit)
                }
            }
            wrapped.add(line)
        }
        return wrapped.take(36)
    }

    private fun drawSpaced(
        canvas: Canvas,
        value: String,
        x: Float,
        y: Float,
        paint: Paint,
        spacing: Float
    ) {
        if (spacing <= .0001f || value.length <= 1) {
            canvas.drawText(value, x, y, paint)
            return
        }
        val width = paint.measureText(value) + (value.length - 1) * spacing
        val start = when (paint.textAlign) {
            Paint.Align.LEFT -> x
            Paint.Align.RIGHT -> x - width
            else -> x - width / 2f
        }
        val p = Paint(paint).apply { textAlign = Paint.Align.LEFT }
        var cursor = start
        for (char in value) {
            val part = char.toString()
            canvas.drawText(part, cursor, y, p)
            cursor += p.measureText(part) + spacing
        }
    }

    private fun drawText(canvas: Canvas, layer: TextOverlay, w: Int, h: Int, short: Float) {
        if (layer.content.isBlank()) return
        val bw = layer.width.coerceIn(.07f, 1f) * w
        val bh = layer.height.coerceIn(.04f, 1f) * h
        val size = layer.size.coerceIn(.012f, .24f) * short
        val face = Typeface.create(
            layer.font.family, when {
                layer.bold && layer.italic -> Typeface.BOLD_ITALIC
                layer.bold -> Typeface.BOLD
                layer.italic -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }
        )
        val brush = paint(layer.color, layer.opacity).apply {
            typeface = face
            textSize = size
            style = Paint.Style.FILL
            textAlign = when (layer.align) {
                TextAlignment.LEFT -> Paint.Align.LEFT
                TextAlignment.CENTER -> Paint.Align.CENTER
                TextAlignment.RIGHT -> Paint.Align.RIGHT
            }
            if (layer.shadow > 0f) {
                setShadowLayer(layer.shadow.coerceIn(0f, 1f) * short * .036f,
                    short * .006f, short * .008f, 0xCC000000.toInt())
            }
        }
        val rowHeight = size * 1.21f
        val rows = lines(layer.content, brush, bw * .94f)
        if (rows.isEmpty()) return
        val contentHeight = rows.size * rowHeight
        val fit = minOf(1f, bh / contentHeight.coerceAtLeast(1f))
        canvas.save()
        canvas.translate(layer.x.coerceIn(0f, 1f) * w, layer.y.coerceIn(0f, 1f) * h)
        canvas.rotate(layer.rotation.coerceIn(-180f, 180f))
        canvas.scale(fit, fit)
        val startY = -((rows.size - 1) * rowHeight) / 2f - (brush.ascent() + brush.descent()) / 2f
        val drawX = when (layer.align) {
            TextAlignment.LEFT -> -bw / 2f
            TextAlignment.CENTER -> 0f
            TextAlignment.RIGHT -> bw / 2f
        }
        val outline = layer.outline.coerceIn(0f, 1f)
        for ((i, row) in rows.withIndex()) {
            val y = startY + i * rowHeight
            if (outline > 0f) {
                val edge = Paint(brush).apply {
                    clearShadowLayer()
                    color = layer.outlineColor or -0x1000000
                    style = Paint.Style.STROKE
                    strokeJoin = Paint.Join.ROUND
                    strokeWidth = outline * short * .014f
                }
                drawSpaced(canvas, row, drawX, y, edge,
                    layer.letterSpacing.coerceIn(0f, .3f) * size)
            }
            drawSpaced(canvas, row, drawX, y, brush,
                layer.letterSpacing.coerceIn(0f, .3f) * size)
        }
        canvas.restore()
    }
}

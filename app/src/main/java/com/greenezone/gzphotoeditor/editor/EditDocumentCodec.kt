package com.greenezone.gzphotoeditor.editor

import java.nio.charset.StandardCharsets
import java.util.Base64

/**
 * Versioned, deterministic, bounded pure-Kotlin project format.
 * No external files, source photos, network identifiers or executable data.
 */
object EditDocumentCodec {
    private const val LEGACY_VERSION = "1"
    private const val ANCHORED_PRIVACY_VERSION = "2"
    private const val MAX_STROKES = 40
    private const val MAX_BRUSH_POINTS = 640
    private const val MAX_INK_POINTS = 500
    private const val MAX_INK_DECIMAL_CHARS = 18
    private const val MAX_EXACT_DOUBLE_INTEGER = 9_007_199_254_740_992L
    // In 10^22 = 2^22 * 5^22, the odd factor fits a double significand.
    private val inkDecimalDivisors = doubleArrayOf(
        1e0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7, 1e8, 1e9, 1e10, 1e11,
        1e12, 1e13, 1e14, 1e15, 1e16, 1e17, 1e18, 1e19, 1e20, 1e21, 1e22
    )
    internal const val MAX_ENCODED_CHARS = 2_000_000

    internal class RecipeTooLarge : IllegalArgumentException("Edit recipe is too large")

    private class RecipeFields(private val limit: Int) {
        private val values = sortedMapOf<String, String>()
        private var lengthWithTrailingNewline = 0L

        operator fun set(key: String, value: String) {
            val previous = values.put(key, value)
            lengthWithTrailingNewline += if (previous == null) {
                key.length.toLong() + value.length + 2
            } else {
                (value.length - previous.length).toLong()
            }
            if (lengthWithTrailingNewline - 1 > limit) throw RecipeTooLarge()
        }

        fun encode(): String {
            // Keys, enums, numbers and flags are ASCII. Free text is encoded in Base64.
            // Allocate the exact byte count instead of growing a large UTF-16 buffer.
            val bytes = ByteArray((lengthWithTrailingNewline - 1).toInt())
            var offset = 0
            for ((key, value) in values) {
                if (offset != 0) bytes[offset++] = '\n'.code.toByte()
                for (char in key) bytes[offset++] = char.code.toByte()
                bytes[offset++] = '='.code.toByte()
                for (char in value) bytes[offset++] = char.code.toByte()
            }
            return String(bytes, StandardCharsets.US_ASCII)
        }
    }

    fun encode(document: EditDocument): String = encodeBounded(document, Int.MAX_VALUE)

    internal fun encodeBounded(document: EditDocument, maxChars: Int = MAX_ENCODED_CHARS): String {
        // Never report a successful save of only a prefix of the active drawing.
        // UI transactions enforce this too; persistence is the final boundary.
        if (document.creative.strokes.size > CreativeRaster.MAX_INK) throw RecipeTooLarge()
        val fields = RecipeFields(maxChars)
        val privacy = document.privacy.take(PrivacyRaster.MAX_EDITS)
        fields["version"] = if (privacy.any { it.authoringGeometry != null })
            ANCHORED_PRIVACY_VERSION else LEGACY_VERSION
        ControlCatalog.all.forEach { control ->
            fields["control." + control.name] = control.read(document.grade).toString()
        }
        for (channel in CurveChannel.entries) {
            fields["curve." + channel.name] = CurveMath.get(document.grade, channel).values.joinToString(",")
        }
        fields["rotation"] = document.quarterTurns.toString()
        fields["horizontalFlip"] = document.mirrored.toString()
        fields["verticalFlip"] = document.flipVertical.toString()
        fields["cropRatio"] = document.crop.name
        fields["cropX"] = document.cropCenterX.toString()
        fields["cropY"] = document.cropCenterY.toString()
        fields["straighten"] = document.straightenDegrees.toString()
        val masks = document.masks.take(4)
        fields["mask.count"] = masks.size.toString()
        for ((index, mask) in masks.withIndex()) {
            val prefix = "mask.$index."
            fields[prefix + "kind"] = mask.kind.name
            fields[prefix + "x"] = mask.centerX.toString()
            fields[prefix + "y"] = mask.centerY.toString()
            fields[prefix + "radius"] = mask.radius.toString()
            fields[prefix + "feather"] = mask.feather.toString()
            fields[prefix + "exposure"] = mask.exposure.toString()
            fields[prefix + "contrast"] = mask.contrast.toString()
            fields[prefix + "saturation"] = mask.saturation.toString()
            val strokes = mask.strokes.take(MAX_STROKES)
            fields[prefix + "stroke.count"] = strokes.size.toString()
            strokes.forEachIndexed { strokeIndex, stroke ->
                fields[prefix + "stroke.$strokeIndex"] =
                    stroke.points.take(MAX_BRUSH_POINTS).joinToString(";") { "${it.x},${it.y}" }
            }
        }
        fields["privacy.count"] = privacy.size.toString()
        for ((index, mark) in privacy.withIndex()) {
            val prefix = "privacy.$index."
            fields[prefix + "kind"] = mark.kind.name
            fields[prefix + "x"] = mark.centerX.toString()
            fields[prefix + "y"] = mark.centerY.toString()
            fields[prefix + "width"] = mark.width.toString()
            fields[prefix + "height"] = mark.height.toString()
            fields[prefix + "strength"] = mark.strength.toString()
            fields[prefix + "feather"] = mark.feather.toString()
            fields[prefix + "brushSize"] = mark.brushSize.toString()
            fields[prefix + "color"] = mark.color.toString()
            mark.authoringGeometry?.let { geometry ->
                val anchor = prefix + "geometry."
                fields[anchor + "rotation"] = geometry.quarterTurns.toString()
                fields[anchor + "horizontalFlip"] = geometry.mirrored.toString()
                fields[anchor + "verticalFlip"] = geometry.flipVertical.toString()
                fields[anchor + "cropRatio"] = geometry.crop.name
                fields[anchor + "cropX"] = geometry.cropCenterX.toString()
                fields[anchor + "cropY"] = geometry.cropCenterY.toString()
                fields[anchor + "straighten"] = geometry.straightenDegrees.toString()
                geometry.cropFrame?.let { frame ->
                    fields[anchor + "frame"] = listOf(frame.left, frame.top, frame.right, frame.bottom)
                        .joinToString(",")
                    if (geometry.cropFrameRatioLocked) fields[anchor + "frame.locked"] = "true"
                }
            }
            val strokes = mark.strokes.take(MAX_STROKES)
            fields[prefix + "stroke.count"] = strokes.size.toString()
            strokes.forEachIndexed { i, stroke ->
                fields[prefix + "stroke.$i"] =
                    stroke.points.take(MAX_BRUSH_POINTS).joinToString(";") { "${it.x},${it.y}" }
            }
        }
        document.cropFrame?.let { f ->
            fields["crop.frame"] = listOf(f.left, f.top, f.right, f.bottom).joinToString(",")
            if (document.cropFrameRatioLocked) fields["crop.frame.locked"] = "true"
        }
        val creative = document.creative
        fields["creative.text.count"] = creative.text.take(CreativeRaster.MAX_TEXT).size.toString()
        creative.text.take(CreativeRaster.MAX_TEXT).forEachIndexed { i, layer ->
            val pre = "creative.text." + i + "."
            fields[pre + "content"] = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(layer.content.take(500).toByteArray(StandardCharsets.UTF_8))
            fields[pre + "font"] = layer.font.name
            fields[pre + "align"] = layer.align.name
            fields[pre + "x"] = layer.x.toString(); fields[pre + "y"] = layer.y.toString()
            fields[pre + "w"] = layer.width.toString(); fields[pre + "h"] = layer.height.toString()
            fields[pre + "rotation"] = layer.rotation.toString()
            fields[pre + "size"] = layer.size.toString()
            fields[pre + "color"] = layer.color.toString()
            fields[pre + "opacity"] = layer.opacity.toString()
            fields[pre + "bold"] = layer.bold.toString()
            fields[pre + "italic"] = layer.italic.toString()
            fields[pre + "outline"] = layer.outline.toString()
            fields[pre + "outlineColor"] = layer.outlineColor.toString()
            fields[pre + "shadow"] = layer.shadow.toString()
            fields[pre + "spacing"] = layer.letterSpacing.toString()
        }
        fields["creative.shape.count"] = creative.shapes.take(CreativeRaster.MAX_SHAPES).size.toString()
        creative.shapes.take(CreativeRaster.MAX_SHAPES).forEachIndexed { i, shape ->
            val pre = "creative.shape." + i + "."
            fields[pre + "kind"] = shape.kind.name
            fields[pre + "x"] = shape.x.toString(); fields[pre + "y"] = shape.y.toString()
            fields[pre + "w"] = shape.width.toString(); fields[pre + "h"] = shape.height.toString()
            fields[pre + "rotation"] = shape.rotation.toString()
            fields[pre + "color"] = shape.color.toString()
            fields[pre + "opacity"] = shape.opacity.toString()
            fields[pre + "thickness"] = shape.thickness.toString()
            fields[pre + "filled"] = shape.filled.toString()
        }
        fields["creative.ink.count"] = creative.strokes.take(CreativeRaster.MAX_INK).size.toString()
        creative.strokes.take(CreativeRaster.MAX_INK).forEachIndexed { i, stroke ->
            val pre = "creative.ink." + i + "."
            fields[pre + "color"] = stroke.color.toString()
            fields[pre + "opacity"] = stroke.opacity.toString()
            fields[pre + "width"] = stroke.thickness.toString()
            fields[pre + "erase"] = stroke.erase.toString()
            fields[pre + "points"] = stroke.points.take(MAX_INK_POINTS).joinToString(";") {
                it.x.toString() + "," + it.y.toString()
            }
        }
        return fields.encode()
    }

    fun decode(content: String): EditDocument? = decode(content, MAX_ENCODED_CHARS)

    // Older Pro files could contain a larger recipe within their bounded project envelope.
    internal fun recoverProRecipe(content: String, projectLimit: Int): EditDocument? =
        decode(content, projectLimit)

    // Disposable selected-Open instrumentation. This source is diagnostic only.
    internal class InkProbe(val phase: String, val baselineParser: Boolean = false) {
        var recipeCopyCpuNs = 0L
        var boundedCount = 0
        var fieldsCpuNs = 0L
        var fieldsChars = 0
        var inkCpuNs = 0L
        var fastCount = 0
        var fallbackCount = 0
        val firstFallbacks = ArrayList<String>(8)
    }

    internal val selectedInkProbe = ThreadLocal<InkProbe>()
    internal val heldInkParserForRun = ThreadLocal<Boolean>()

    private fun decodeInkPoints(value: String): List<BrushPoint> {
        val probe = selectedInkProbe.get()
        val cpuStart = if (probe != null) android.os.Debug.threadCpuTimeNanos() else 0L
        val points = ArrayList<BrushPoint>()
        var start = 0
        var tokens = 0
        while (start < value.length && tokens < MAX_INK_POINTS) {
            val separator = value.indexOf(';', start)
            val end = if (separator == -1) value.length else separator
            decodeInkPoint(value, start, end)?.let { points.add(it) }
            start = end + 1
            tokens++
        }
        if (probe != null) probe.inkCpuNs += android.os.Debug.threadCpuTimeNanos() - cpuStart
        return points
    }

    private fun decodeInkPoint(value: String, start: Int, end: Int): BrushPoint? {
        var comma = -1
        for (index in start until end) {
            if (value[index] == ',') {
                if (comma != -1) return null
                comma = index
            }
        }
        if (comma <= start || comma == end - 1) return null
        val x = inkCoordinate(value, start, comma)
        val y = inkCoordinate(value, comma + 1, end)
        return if (x.isFinite() && y.isFinite() && x in 0f..1f && y in 0f..1f) BrushPoint(x, y) else null
    }

    private fun inkFallback(value: String, start: Int, end: Int): Float {
        val token = value.substring(start, end)
        selectedInkProbe.get()?.let { probe ->
            probe.fallbackCount++
            if (probe.firstFallbacks.size < 8) probe.firstFallbacks.add(token)
        }
        return token.toFloatOrNull() ?: Float.NaN
    }

    private fun heldInkCoordinate(value: String, start: Int, end: Int): Float {
        val digits = end - start - 2
        if (digits !in 1..9 || value[start] != '0' || value[start + 1] != '.')
            return inkFallback(value, start, end)
        var numerator = 0
        for (index in start + 2 until end) {
            val digit = value[index] - '0'
            if (digit !in 0..9) return inkFallback(value, start, end)
            numerator = numerator * 10 + digit
        }
        // With at most nine decimal places, any nonzero gap to a float
        // midpoint exceeds half a double ULP there (5^9 < 2^29). Conversion
        // therefore rounds as direct float parsing does.
        selectedInkProbe.get()?.let { it.fastCount++ }
        return (numerator / inkDecimalDivisors[digits]).toFloat()
    }

    private fun inkCoordinate(value: String, start: Int, end: Int): Float {
        val probe = selectedInkProbe.get()
        if (probe?.baselineParser ?: (heldInkParserForRun.get() == true))
            return heldInkCoordinate(value, start, end)
        val digits = end - start - 2
        if (digits in 1..9 && value[start] == '0' && value[start + 1] == '.') {
            var numerator = 0
            for (index in start + 2 until end) {
                val digit = value[index] - '0'
                if (digit !in 0..9) return extendedInkCoordinate(value, start, end)
                numerator = numerator * 10 + digit
            }
            // With at most nine decimal places, any nonzero gap to a float
            // midpoint exceeds half a double ULP there (5^9 < 2^29).
            probe?.let { it.fastCount++ }
            return (numerator / inkDecimalDivisors[digits]).toFloat()
        }
        return extendedInkCoordinate(value, start, end)
    }

    private fun extendedInkCoordinate(value: String, start: Int, end: Int): Float {
        val coordinate = boundedInkDecimal(value, start, end)
        if (!coordinate.isNaN()) {
            selectedInkProbe.get()?.let { it.boundedCount++ }
            return coordinate
        }
        return inkFallback(value, start, end)
    }

    private fun boundedInkDecimal(value: String, start: Int, end: Int): Float {
        // Eighteen characters bound the coefficient accumulation to fewer than 10^18.
        if (end - start > MAX_INK_DECIMAL_CHARS) return Float.NaN
        var index = start
        val negative = value[index] == '-'
        if (negative || value[index] == '+') index++
        var coefficient = 0L
        var digits = 0
        var decimalPoint = -1
        while (index < end) {
            val char = value[index]
            val digit = char - '0'
            if (digit in 0..9) {
                coefficient = coefficient * 10 + digit
                digits++
            } else if (char == '.' && decimalPoint == -1) {
                decimalPoint = index
            } else break
            index++
        }
        if (digits == 0 || coefficient > MAX_EXACT_DOUBLE_INTEGER) return Float.NaN
        var scale = if (decimalPoint == -1) 0 else index - decimalPoint - 1
        if (index < end) {
            if (value[index] != 'e' && value[index] != 'E') return Float.NaN
            index++
            if (index == end) return Float.NaN
            val negativeExponent = value[index] == '-'
            if (negativeExponent || value[index] == '+') index++
            if (end - index !in 1..2) return Float.NaN
            var exponent = 0
            while (index < end) {
                val digit = value[index++] - '0'
                if (digit !in 0..9) return Float.NaN
                exponent = exponent * 10 + digit
            }
            scale += if (negativeExponent) exponent else -exponent
        }
        if (scale !in inkDecimalDivisors.indices) return Float.NaN

        // The operands are exact doubles, and nonzero quotients stay in the
        // normal float range. A rounded double can only obscure direct float
        // rounding if it lands on a float midpoint. The 29 extra significand
        // bits identify that case, which goes through the direct parser.
        val magnitude = coefficient / inkDecimalDivisors[scale]
        if ((magnitude.toRawBits() and 0x1fffffffL) == 0x10000000L) return Float.NaN
        val rounded = magnitude.toFloat()
        return if (negative) -rounded else rounded
    }

    private fun decode(content: String, limit: Int): EditDocument? {
        if (content.length > limit) return null
        val probe = selectedInkProbe.get()
        val fieldsStart = if (probe != null) android.os.Debug.threadCpuTimeNanos() else 0L
        val fields = content.lineSequence().take(6000).mapNotNull { line ->
            val index = line.indexOf('=')
            if (index <= 0) null else line.substring(0, index) to line.substring(index + 1)
        }.toMap()
        if (probe != null) {
            probe.fieldsChars = content.length
            probe.fieldsCpuNs = android.os.Debug.threadCpuTimeNanos() - fieldsStart
        }
        val version = fields["version"]
        if (version != LEGACY_VERSION && version != ANCHORED_PRIVACY_VERSION) return null
        fun numeric(key: String, default: Float, min: Float, max: Float): Float {
            val parsed = fields[key]?.toFloatOrNull()
            return if (parsed != null && parsed.isFinite()) parsed.coerceIn(min, max) else default
        }
        fun count(key: String, max: Int): Int =
            (fields[key]?.toIntOrNull() ?: 0).coerceIn(0, max)
        var grade = Grade()
        ControlCatalog.all.forEach { control ->
            val value = numeric("control." + control.name, 0f, control.min, control.max)
            grade = control.write(grade, value)
        }
        for (channel in CurveChannel.entries) {
            val points = fields["curve." + channel.name]?.split(',') ?: continue
            if (points.size != 5) continue
            val values = points.mapNotNull { it.toFloatOrNull() }
            if (values.size == 5 && values.all { it.isFinite() && it in 0f..1f }) {
                grade = CurveMath.put(grade, channel, CurvePoints(values))
            }
        }
        val masks = ArrayList<LocalMask>()
        for (i in 0 until count("mask.count", 4)) {
            val prefix = "mask.$i."
            val kind = MaskKind.entries.firstOrNull { it.name == fields[prefix + "kind"] }
                ?: continue
            val defaultRadius = if (kind == MaskKind.BRUSH) .04f else .35f
            val strokes = ArrayList<BrushStroke>()
            for (j in 0 until count(prefix + "stroke.count", MAX_STROKES)) {
                val pairs = fields[prefix + "stroke.$j"]?.split(';')?.take(MAX_BRUSH_POINTS) ?: continue
                val points = pairs.mapNotNull { pair ->
                    val coords = pair.split(',')
                    if (coords.size != 2) return@mapNotNull null
                    val x = coords[0].toFloatOrNull()
                    val y = coords[1].toFloatOrNull()
                    if (x == null || y == null || !x.isFinite() || !y.isFinite() ||
                        x !in 0f..1f || y !in 0f..1f
                    ) null else BrushPoint(x, y)
                }
                if (points.isNotEmpty()) strokes.add(BrushStroke(points))
            }
            masks.add(LocalMask(
                kind = kind,
                centerX = numeric(prefix + "x", .5f, 0f, 1f),
                centerY = numeric(prefix + "y", .5f, 0f, 1f),
                radius = numeric(prefix + "radius", defaultRadius, .005f, .8f),
                feather = numeric(prefix + "feather", .35f, .01f, 1f),
                exposure = numeric(prefix + "exposure", 0f, -1.5f, 1.5f),
                contrast = numeric(prefix + "contrast", 0f, -1f, 1f),
                saturation = numeric(prefix + "saturation", 0f, -1f, 1f),
                strokes = strokes
            ))
        }
        fun cropFrame(key: String): CropFrame? {
            val parts = fields[key]?.split(',') ?: return null
            if (parts.size != 4) return null
            val values = parts.mapNotNull { it.toFloatOrNull() }
            if (values.size != 4 || values.any { !it.isFinite() }) return null
            return CropMath.sanitize(CropFrame(values[0], values[1], values[2], values[3]))
        }
        fun privacyGeometry(prefix: String): PhotoGeometry? {
            val anchor = prefix + "geometry."
            val rotation = fields[anchor + "rotation"]?.toIntOrNull()?.takeIf { it in 0..3 } ?: return null
            val crop = CropRatio.entries.firstOrNull { it.name == fields[anchor + "cropRatio"] }
                ?: return null
            val frame = cropFrame(anchor + "frame")
            if (fields.containsKey(anchor + "frame") && frame == null) return null
            val flipX = fields[anchor + "horizontalFlip"]?.toBooleanStrictOrNull() ?: return null
            val flipY = fields[anchor + "verticalFlip"]?.toBooleanStrictOrNull() ?: return null
            val centerX = numeric(anchor + "cropX", Float.NaN, 0f, 1f)
            val centerY = numeric(anchor + "cropY", Float.NaN, 0f, 1f)
            val straighten = numeric(anchor + "straighten", Float.NaN, -15f, 15f)
            if (!centerX.isFinite() || !centerY.isFinite() || !straighten.isFinite()) return null
            return PhotoGeometry(rotation, flipX, flipY, crop, centerX, centerY, straighten, frame,
                frame != null && fields[anchor + "frame.locked"] == "true")
        }
        val privacy = ArrayList<PrivacyEdit>()
        for (i in 0 until count("privacy.count", PrivacyRaster.MAX_EDITS)) {
            val prefix = "privacy.$i."
            val kind = PrivacyKind.entries.firstOrNull { it.name == fields[prefix + "kind"] }
                ?: continue
            val strokes = ArrayList<BrushStroke>()
            for (j in 0 until count(prefix + "stroke.count", MAX_STROKES)) {
                val pairs = fields[prefix + "stroke.$j"]?.split(';')?.take(MAX_BRUSH_POINTS) ?: continue
                val points = pairs.mapNotNull { pair ->
                    val coords = pair.split(',')
                    if (coords.size != 2) return@mapNotNull null
                    val x = coords[0].toFloatOrNull()
                    val y = coords[1].toFloatOrNull()
                    if (x == null || y == null || !x.isFinite() || !y.isFinite() ||
                        x !in 0f..1f || y !in 0f..1f
                    ) null else BrushPoint(x, y)
                }
                if (points.isNotEmpty()) strokes.add(BrushStroke(points))
            }
            val hasAnchorFields = fields.keys.any { it.startsWith(prefix + "geometry.") }
            val authoringGeometry = if (version == ANCHORED_PRIVACY_VERSION && hasAnchorFields)
                privacyGeometry(prefix) ?: return null
            else null
            privacy.add(PrivacyEdit(
                kind = kind,
                centerX = numeric(prefix + "x", .5f, 0f, 1f),
                centerY = numeric(prefix + "y", .5f, 0f, 1f),
                width = numeric(prefix + "width", .45f, .02f, 1f),
                height = numeric(prefix + "height", .16f, .02f, 1f),
                strength = numeric(prefix + "strength", .65f, 0f, 1f),
                feather = numeric(prefix + "feather", .22f, 0f, .8f),
                brushSize = numeric(prefix + "brushSize", .055f, .005f, .20f),
                color = (fields[prefix + "color"]?.toIntOrNull() ?: -0x1000000) or -0x1000000,
                strokes = strokes,
                authoringGeometry = authoringGeometry
            ))
        }
        if (version == ANCHORED_PRIVACY_VERSION && privacy.none { it.authoringGeometry != null }) return null
        val frame = cropFrame("crop.frame")
        fun color(key: String, fallback: Int): Int = fields[key]?.toIntOrNull() ?: fallback
        val texts = ArrayList<TextOverlay>()
        for (i in 0 until count("creative.text.count", CreativeRaster.MAX_TEXT)) {
            val pre = "creative.text." + i + "."
            val title = try {
                String(Base64.getUrlDecoder().decode(fields[pre + "content"] ?: ""),
                    StandardCharsets.UTF_8).take(500)
            } catch (_: IllegalArgumentException) { "" }
            if (title.isBlank()) continue
            texts.add(TextOverlay(
                content = title,
                font = TextFont.entries.firstOrNull { it.name == fields[pre + "font"] } ?: TextFont.SANS,
                align = TextAlignment.entries.firstOrNull { it.name == fields[pre + "align"] } ?: TextAlignment.CENTER,
                x = numeric(pre + "x", .5f, 0f, 1f),
                y = numeric(pre + "y", .5f, 0f, 1f),
                width = numeric(pre + "w", .7f, .07f, 1f),
                height = numeric(pre + "h", .24f, .04f, 1f),
                rotation = numeric(pre + "rotation", 0f, -180f, 180f),
                size = numeric(pre + "size", .075f, .012f, .24f),
                color = color(pre + "color", -1),
                opacity = numeric(pre + "opacity", 1f, 0f, 1f),
                bold = fields[pre + "bold"] == "true",
                italic = fields[pre + "italic"] == "true",
                outline = numeric(pre + "outline", 0f, 0f, 1f),
                outlineColor = color(pre + "outlineColor", -0x1000000),
                shadow = numeric(pre + "shadow", 0f, 0f, 1f),
                letterSpacing = numeric(pre + "spacing", 0f, 0f, .3f)
            ))
        }
        val shapes = ArrayList<ShapeOverlay>()
        for (i in 0 until count("creative.shape.count", CreativeRaster.MAX_SHAPES)) {
            val pre = "creative.shape." + i + "."
            val kind = ShapeKind.entries.firstOrNull { it.name == fields[pre + "kind"] }
                ?: continue
            shapes.add(ShapeOverlay(
                kind = kind,
                x = numeric(pre + "x", .5f, 0f, 1f),
                y = numeric(pre + "y", .5f, 0f, 1f),
                width = numeric(pre + "w", .38f, .018f, 1f),
                height = numeric(pre + "h", .25f, .018f, 1f),
                rotation = numeric(pre + "rotation", 0f, -180f, 180f),
                color = color(pre + "color", -1),
                opacity = numeric(pre + "opacity", 1f, 0f, 1f),
                thickness = numeric(pre + "thickness", .009f, .002f, .10f),
                filled = fields[pre + "filled"] == "true"
            ))
        }
        val ink = ArrayList<InkStroke>()
        for (i in 0 until count("creative.ink.count", CreativeRaster.MAX_INK)) {
            val pre = "creative.ink." + i + "."
            val positions = fields[pre + "points"]?.let(::decodeInkPoints) ?: continue
            if (positions.isEmpty()) continue
            ink.add(InkStroke(
                points = positions,
                color = color(pre + "color", -1),
                thickness = numeric(pre + "width", .012f, .0015f, .16f),
                opacity = numeric(pre + "opacity", 1f, 0f, 1f),
                erase = fields[pre + "erase"] == "true"
            ))
        }

        return EditDocument(
            grade = grade,
            quarterTurns = (fields["rotation"]?.toIntOrNull() ?: 0).mod(4),
            mirrored = fields["horizontalFlip"] == "true",
            flipVertical = fields["verticalFlip"] == "true",
            crop = CropRatio.entries.firstOrNull { it.name == fields["cropRatio"] }
                ?: CropRatio.ORIGINAL,
            cropCenterX = numeric("cropX", .5f, 0f, 1f),
            cropCenterY = numeric("cropY", .5f, 0f, 1f),
            straightenDegrees = numeric("straighten", 0f, -15f, 15f),
            masks = masks,
            privacy = privacy,
            cropFrame = frame,
            creative = CreativeDocument(texts, ink, shapes),
            cropFrameRatioLocked = frame != null && fields["crop.frame.locked"] == "true"
        )
    }
}

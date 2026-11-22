package com.greenezone.gzphotoeditor.editor

import java.nio.charset.StandardCharsets
import java.util.Base64

/**
 * Versioned, deterministic, bounded pure-Kotlin project format.
 * No external files, source photos, network identifiers or executable data.
 */
object EditDocumentCodec {
    private const val VERSION = "1"
    private const val MAX_STROKES = 40
    private const val MAX_POINTS = 500

    fun encode(document: EditDocument): String {
        val fields = sortedMapOf<String, String>()
        fields["version"] = VERSION
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
                    stroke.points.take(MAX_POINTS).joinToString(";") { "${it.x},${it.y}" }
            }
        }
        val privacy = document.privacy.take(PrivacyRaster.MAX_EDITS)
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
            val strokes = mark.strokes.take(MAX_STROKES)
            fields[prefix + "stroke.count"] = strokes.size.toString()
            strokes.forEachIndexed { i, stroke ->
                fields[prefix + "stroke.$i"] =
                    stroke.points.take(MAX_POINTS).joinToString(";") { "${it.x},${it.y}" }
            }
        }
        document.cropFrame?.let { f ->
            fields["crop.frame"] = listOf(f.left, f.top, f.right, f.bottom).joinToString(",")
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
            fields[pre + "points"] = stroke.points.take(MAX_POINTS).joinToString(";") {
                it.x.toString() + "," + it.y.toString()
            }
        }
        return fields.entries.joinToString("\n") { "${it.key}=${it.value}" }
    }

    fun decode(content: String): EditDocument? {
        if (content.length > 2_000_000) return null
        val fields = content.lineSequence().take(6000).mapNotNull { line ->
            val index = line.indexOf('=')
            if (index <= 0) null else line.substring(0, index) to line.substring(index + 1)
        }.toMap()
        if (fields["version"] != VERSION) return null
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
                val pairs = fields[prefix + "stroke.$j"]?.split(';')?.take(MAX_POINTS) ?: continue
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
        val privacy = ArrayList<PrivacyEdit>()
        for (i in 0 until count("privacy.count", PrivacyRaster.MAX_EDITS)) {
            val prefix = "privacy.$i."
            val kind = PrivacyKind.entries.firstOrNull { it.name == fields[prefix + "kind"] }
                ?: continue
            val strokes = ArrayList<BrushStroke>()
            for (j in 0 until count(prefix + "stroke.count", MAX_STROKES)) {
                val pairs = fields[prefix + "stroke.$j"]?.split(';')?.take(MAX_POINTS) ?: continue
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
                strokes = strokes
            ))
        }
        val frame = fields["crop.frame"]?.split(',')?.let { parts ->
            if (parts.size != 4) null else {
                val values = parts.mapNotNull { it.toFloatOrNull() }
                if (values.size != 4 || values.any { !it.isFinite() }) null
                else CropMath.sanitize(CropFrame(values[0], values[1], values[2], values[3]))
            }
        }
        fun color(key: String, fallback: Int): Int = fields[key]?.toIntOrNull() ?: fallback
        fun points(key: String): List<BrushPoint> =
            fields[key]?.split(';')?.take(MAX_POINTS)?.mapNotNull { pair ->
                val pieces = pair.split(',')
                if (pieces.size != 2) return@mapNotNull null
                val x = pieces[0].toFloatOrNull()
                val y = pieces[1].toFloatOrNull()
                if (x == null || y == null || !x.isFinite() || !y.isFinite() ||
                    x !in 0f..1f || y !in 0f..1f) null else BrushPoint(x, y)
            } ?: emptyList()

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
            val positions = points(pre + "points")
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
            creative = CreativeDocument(texts, ink, shapes)
        )
    }
}

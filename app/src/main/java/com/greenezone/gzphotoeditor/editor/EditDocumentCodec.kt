package com.greenezone.gzphotoeditor.editor

/**
 * Versioned, deterministic, bounded pure-Kotlin project format.
 * No external files, source photos, network identifiers or executable data.
 */
object EditDocumentCodec {
    private const val VERSION = "1"
    private const val MAX_STROKES = 40
    private const val MAX_POINTS = 400

    fun encode(document: EditDocument): String {
        val fields = sortedMapOf<String, String>()
        fields["version"] = VERSION
        ControlCatalog.all.forEachIndexed { index, control ->
            fields["control.$index"] = control.read(document.grade).toString()
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
        return fields.entries.joinToString("\n") { "${it.key}=${it.value}" }
    }

    fun decode(content: String): EditDocument? {
        if (content.length > 500_000) return null
        val fields = content.lineSequence().take(600).mapNotNull { line ->
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
        ControlCatalog.all.forEachIndexed { index, control ->
            val value = numeric("control.$index", 0f, control.min, control.max)
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
            masks = masks
        )
    }
}

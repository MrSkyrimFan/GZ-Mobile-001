package com.greenezone.gzphotoeditor.editor

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ColorSpace
import android.graphics.ImageDecoder
import android.net.Uri
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** Pure geometry and conservative device-memory budget calculations. */
object GeometryMath {
    /** Centered rectangle inside a rotated image, excluding transparent corners. */
    fun safeStraightenCrop(width: Int, height: Int, angle: Float): Pair<Int, Int> {
        require(width > 0 && height > 0)
        if (abs(angle) < .001f) return width to height
        val radians = abs(angle.toDouble()) * PI / 180.0
        val c = abs(cos(radians))
        val s = abs(sin(radians))
        val aspect = width.toDouble() / height
        val maxHeight = min(
            width / (aspect * c + s),
            height / (aspect * s + c)
        ).coerceAtLeast(1.0)
        return ((maxHeight * aspect).toInt().coerceIn(1, width)) to
            (maxHeight.toInt().coerceIn(1, height))
    }

    fun cropOrigin(fullWidth: Int, fullHeight: Int, w: Int, h: Int, x: Float, y: Float): Pair<Int, Int> {
        require(w > 0 && h > 0 && fullWidth >= w && fullHeight >= h)
        val roomX = fullWidth - w
        val roomY = fullHeight - h
        val cx = if (x.isFinite()) x.coerceIn(0f, 1f) else .5f
        val cy = if (y.isFinite()) y.coerceIn(0f, 1f) else .5f
        return (roomX * cx).roundToInt().coerceIn(0, roomX) to
            (roomY * cy).roundToInt().coerceIn(0, roomY)
    }

    /** Allows extra decode/render/transform/canvas buffers and app overhead. */
    fun safeExportPixels(memoryClassMB: Int): Long {
        val available = (memoryClassMB - 64).coerceAtLeast(40).toLong() * 1024 * 1024
        return (available / 20L).coerceIn(2_000_000L, 24_000_000L)
    }
}

/**
 * All bitmap work is software sRGB, caller runs on Dispatchers.Default/IO.
 * No network and no writes to the source URI.
 */
object ImagePipeline {
    const val PREVIEW_MAX_EDGE = 1400
    const val MAX_EXPORT_PIXELS = 24_000_000L

    fun decode(resolver: ContentResolver, uri: Uri, maxEdge: Int, maxPixels: Long): Bitmap {
        require(maxEdge > 0 && maxPixels > 0) { "Invalid image decode budget" }
        val src = ImageDecoder.createSource(resolver, uri)
        return ImageDecoder.decodeBitmap(src) { decoder, info, _ ->
            val width = info.size.width.toLong()
            val height = info.size.height.toLong()
            require(width > 0 && height > 0) { "Invalid image dimensions" }
            val sideScale = min(1.0, maxEdge.toDouble() / maxOf(width, height))
            val pixelScale = min(1.0, sqrt(maxPixels.toDouble() / (width * height)))
            val scale = min(sideScale, pixelScale)
            if (scale < 1.0) {
                decoder.setTargetSize(
                    maxOf(1, (width * scale).roundToInt()),
                    maxOf(1, (height * scale).roundToInt())
                )
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.setTargetColorSpace(ColorSpace.get(ColorSpace.Named.SRGB))
        }
    }

    fun centerCropSize(width: Int, height: Int, ratio: CropRatio): Pair<Int, Int> =
        CropMath.presetSize(width, height, ratio)

    /**
     * Each result owns its new cropped/rotated bitmaps; borrowed input is never recycled.
     * Straightening produces a transparent-cornered intermediate, then a conservative
     * centered safe crop before the regular aspect/position crop.
     */
    fun geometry(input: Bitmap, document: EditDocument): Bitmap =
        geometry(input, PhotoGeometryPlan(input.width, input.height, PhotoGeometry.from(document)))

    private fun geometry(input: Bitmap, plan: PhotoGeometryPlan): Bitmap {
        var working = input
        try {
            if (!plan.orientation.isIdentity) {
                working = Bitmap.createBitmap(
                    input, 0, 0, input.width, input.height, plan.orientation, true
                )
            }
            if (!plan.straighten.isIdentity) {
                val spun = Bitmap.createBitmap(
                    working, 0, 0, working.width, working.height, plan.straighten, true
                )
                if (working !== input && working !== spun) working.recycle()
                working = spun
                if (plan.safeWidth < working.width || plan.safeHeight < working.height) {
                    val centered = Bitmap.createBitmap(working, plan.safeLeft, plan.safeTop,
                        plan.safeWidth, plan.safeHeight)
                    if (working !== input && working !== centered) working.recycle()
                    working = centered
                }
            }
            val (originX, originY, cropWidth, cropHeight) = plan.crop
            if (cropWidth < working.width || cropHeight < working.height) {
                val crop = Bitmap.createBitmap(working, originX, originY, cropWidth, cropHeight)
                if (working !== input && working !== crop) working.recycle()
                working = crop
            }
            return working
        } catch (error: Throwable) {
            if (working !== input && !working.isRecycled) working.recycle()
            throw error
        }
    }

    /**
     * Five sliding source rows and one rendered row. Does not allocate frame-sized
     * pixel IntArrays. Detail, curves and masks share one final-resolution render.
     */
    fun render(input: Bitmap, document: EditDocument, cancelCheck: (() -> Unit)? = null): Bitmap {
        val view = PhotoGeometry.from(document)
        val plan = PhotoGeometryPlan(input.width, input.height, view)
        val source = geometry(input, plan)
        val projections = HashMap<PhotoGeometry, PrivacyProjection>()
        fun project(mark: PrivacyEdit): PrivacyProjection? {
            val anchor = mark.authoringGeometry?.takeUnless { it == view } ?: return null
            return projections.getOrPut(anchor) {
                plan.projectFrom(PhotoGeometryPlan(input.width, input.height, anchor))
            }
        }
        var output: Bitmap? = null
        var returnedGeometry = false
        try {
            fun composite(image: Bitmap, owned: Boolean): Bitmap {
                if (CreativeRaster.isEmpty(document.creative)) return image
                val target = if (owned && image.isMutable) image
                    else image.copy(Bitmap.Config.ARGB_8888, true)
                        ?: throw IllegalStateException("Cannot allocate drawing layer")
                try {
                    CreativeRaster.drawInto(target, document.creative, cancelCheck)
                } catch (error: Throwable) {
                    if (target !== image && !target.isRecycled) target.recycle()
                    throw error
                }
                return target
            }
            val prepared = ToneMath.prepare(document.grade)
            val masks = MaskMath.prepare(document.masks)
            if (prepared.isNeutral && masks.isEmpty()) {
                if (document.privacy.isEmpty() && CreativeRaster.isEmpty(document.creative)) {
                    returnedGeometry = true
                    return source
                }
                val photo = if (document.privacy.isEmpty()) source
                    else PrivacyRaster.applyProjected(source, document.privacy, cancelCheck, ::project)
                try {
                    return composite(photo, photo !== source)
                } catch (error: Throwable) {
                    if (photo !== source && !photo.isRecycled) photo.recycle()
                    throw error
                }
            }

            val width = source.width
            val height = source.height
            val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            result.setHasAlpha(source.hasAlpha())
            output = result
            val rows = Array(5) { IntArray(width) }
            val rendered = IntArray(width)
            val hasDenoise = document.grade.denoise > 0f
            val hasDetail = document.grade.sharpen != 0f || document.grade.texture != 0f ||
                document.grade.clarity != 0f
            val lightRows = if (hasDetail) Array(5) { FloatArray(width) } else null
            val vertical3 = if (hasDetail) FloatArray(width) else null
            val vertical5 = if (hasDetail) FloatArray(width) else null

            fun readRow(index: Int, row: Int) {
                source.getPixels(rows[index], 0, width, 0, row.coerceIn(0, height - 1), width, 1)
                val lights = lightRows?.get(index)
                if (lights != null) for (x in 0 until width) {
                    lights[x] = DetailMath.luminance(rows[index][x])
                }
            }
            for (i in 0..4) readRow(i, i - 2)

            for (y in 0 until height) {
                if (y % 16 == 0) cancelCheck?.invoke()
                var horizontal3 = 0f
                var horizontal5 = 0f
                if (lightRows != null && vertical3 != null && vertical5 != null) {
                    for (x in 0 until width) {
                        vertical3[x] = lightRows[1][x] + lightRows[2][x] + lightRows[3][x]
                        vertical5[x] = vertical3[x] + lightRows[0][x] + lightRows[4][x]
                    }
                    horizontal3 = vertical3[0] * 2f + vertical3[min(1, width - 1)]
                    horizontal5 = vertical5[0] * 3f + vertical5[min(1, width - 1)] +
                        vertical5[min(2, width - 1)]
                }
                for (x in 0 until width) {
                    val sourcePixel = if (hasDenoise) {
                        DetailMath.denoise(rows, x, document.grade.denoise)
                    } else rows[2][x]
                    val detail = if (lightRows != null) {
                        DetailMath.detailDeltaFromAverages(
                            lightRows[2][x], horizontal3 / 9f, horizontal5 / 25f, document.grade
                        )
                    } else 0f
                    val base = ToneMath.adjustPrepared(sourcePixel, prepared, x, y, width, height, detail)
                    rendered[x] = MaskMath.adjust(base, masks, x, y, width, height)
                    if (vertical3 != null && vertical5 != null) {
                        horizontal3 += vertical3[min(width - 1, x + 2)] -
                            vertical3[maxOf(0, x - 1)]
                        horizontal5 += vertical5[min(width - 1, x + 3)] -
                            vertical5[maxOf(0, x - 2)]
                    }
                }
                result.setPixels(rendered, 0, width, 0, y, width, 1)
                if (y < height - 1) {
                    val reusable = rows[0]
                    for (i in 0..3) rows[i] = rows[i + 1]
                    rows[4] = reusable
                    if (lightRows != null) {
                        val old = lightRows[0]
                        for (i in 0..3) lightRows[i] = lightRows[i + 1]
                        lightRows[4] = old
                    }
                    readRow(4, min(y + 3, height - 1))
                }
            }
            if (document.privacy.isNotEmpty()) {
                val privacyResult = PrivacyRaster.applyProjected(result, document.privacy, cancelCheck, ::project)
                output = privacyResult
                result.recycle()
            }
            val finalPhoto = composite(output ?: result, true)
            output = null
            return finalPhoto
        } catch (error: Throwable) {
            if (output != null && !output.isRecycled) output.recycle()
            throw error
        } finally {
            if (!returnedGeometry && source !== input && !source.isRecycled) source.recycle()
        }
    }

    /** Histogram reads at most about 12,000 sampled pixels. Run off UI thread. */
    fun luminanceHistogram(image: Bitmap, buckets: Int = 64): IntArray {
        require(buckets > 1)
        val counts = IntArray(buckets)
        val total = image.width.toLong() * image.height
        val stride = maxOf(1, sqrt(total / 12000.0).roundToInt())
        for (y in 0 until image.height step stride) {
            for (x in 0 until image.width step stride) {
                val light = DetailMath.luminance(image.getPixel(x, y))
                counts[(light * (buckets - 1)).roundToInt().coerceIn(0, buckets - 1)]++
            }
        }
        return counts
    }

    /**
     * Only needed when converting transparency to JPEG. JPEG has no alpha.
     * Caller owns and recycles returned temporary.
     */
    fun compositeWhite(source: Bitmap): Bitmap {
        val opaque = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        opaque.setHasAlpha(false)
        val canvas = android.graphics.Canvas(opaque)
        canvas.drawColor(android.graphics.Color.WHITE)
        canvas.drawBitmap(source, 0f, 0f, null)
        return opaque
    }
}

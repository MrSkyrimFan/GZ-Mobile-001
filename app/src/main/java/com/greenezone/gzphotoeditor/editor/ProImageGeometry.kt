package com.greenezone.gzphotoeditor.editor

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.CancellationException
import kotlin.math.abs

/** The oriented media dimensions and the committed recipe define the image frame. */
internal object ProImageGeometry {
    private class HeaderRead(val size: ProPixelSize) : RuntimeException()

    fun sourceSize(resolver: ContentResolver, uri: Uri): ProPixelSize {
        require(uri.scheme == "content")
        try {
            // ImageDecoder reports oriented dimensions and closes its decoder when
            // the listener exits. Stop before allocating a bitmap for metadata.
            ImagePipeline.decode(resolver, uri, 1, 1) { width, height ->
                throw HeaderRead(ProPixelSize(width, height))
            }.recycle()
        } catch (header: HeaderRead) {
            return header.size
        }
        error("Image dimensions unavailable")
    }

    fun editedSize(source: ProPixelSize, edits: EditDocument): ProPixelSize {
        val plan = PhotoGeometryPlan(source.width, source.height, PhotoGeometry.from(edits))
        return ProPixelSize(plan.width, plan.height)
    }

    fun bind(layer: ProLayer, source: ProPixelSize): ProLayer {
        val dimensions = editedSize(source, layer.edits)
        require(ProMath.validSourceAspect(dimensions.aspect)) { "Image proportions exceed Pro's supported range" }
        return layer.copy(aspect = dimensions.aspect, sourceSize = source)
    }

    fun recover(resolver: ContentResolver, project: ProStore.SavedProject): ProStore.SavedProject {
        val sources = mutableMapOf<String, ProPixelSize?>()
        fun recoverDocument(document: ProDocument): ProDocument {
            val layers = document.layers.map { layer ->
                if (layer.kind != ProLayerKind.IMAGE || layer.sourceSize != null) return@map layer
                if (!sources.containsKey(layer.uri)) {
                    sources[layer.uri] = try {
                        sourceSize(resolver, Uri.parse(layer.uri))
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        null
                    }
                }
                val size = sources[layer.uri] ?: return@map layer
                val edited = editedSize(size, layer.edits)
                if (ProMath.validSourceAspect(edited.aspect)) layer.copy(aspect = edited.aspect, sourceSize = size)
                else layer
            }
            val reference = layers.firstOrNull { it.kind == ProLayerKind.IMAGE }
            val recoveredSize = reference?.sourceSize?.let { editedSize(it, reference.edits) }
            // Older handoffs stored the edited aspect. Estimate their pixel extent only
            // when the same source geometry still agrees with that stored canvas.
            val canvasSize = document.canvasSize ?: document.sourceAspectRatio?.let { expected ->
                recoveredSize?.takeIf { abs(it.aspect / expected - 1f) < .025f }
            }
            return document.copy(layers = layers, canvasSize = canvasSize)
        }
        val document = recoverDocument(project.document)
        val reset = if (project.resetDocument == project.document) document else recoverDocument(project.resetDocument)
        return ProStore.SavedProject(document, reset)
    }
}

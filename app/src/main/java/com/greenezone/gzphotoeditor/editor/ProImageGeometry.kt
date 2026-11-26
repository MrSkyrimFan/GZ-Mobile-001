package com.greenezone.gzphotoeditor.editor

import android.content.ContentResolver
import android.net.Uri
import java.util.UUID
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

    fun recordImport(
        layer: ProLayer,
        source: ProPixelSize,
        placementId: String = UUID.randomUUID().toString(),
        placement: ProSourcePlacement? = null
    ): ProLayer {
        require(layer.kind == ProLayerKind.IMAGE)
        val neutral = (placement ?: ProSourcePlacement(placementId, layer.x, layer.y, layer.scale))
            .copy(id = placementId)
        require(neutral.valid())
        return bind(layer, source).copy(sourcePlacement = neutral)
    }

    /** Keep the current imported assets, independently of the age of the entry snapshot. */
    fun reset(document: ProDocument, legacyReset: ProDocument? = null): ProDocument? {
        val setup = document.sourceCanvasSetup ?: ProCanvasSetup.from(legacyReset ?: document)
        val creation = ProDocument(aspect = setup.aspect, background = setup.background,
            sourceAspectRatio = setup.sourceAspectRatio, canvasSize = setup.canvasSize,
            sourceCanvasSetup = setup)
        val canvas = ProMath.intendedCanvasSize(creation)
        val imported = document.layers.filter { it.kind == ProLayerKind.IMAGE }
        val older = legacyReset?.layers.orEmpty().filter { it.kind == ProLayerKind.IMAGE }
        val layers = imported.mapIndexed { index, layer ->
            val source = layer.sourceSize ?: return null
            val recorded = layer.sourcePlacement ?: run {
                val previous = older.singleOrNull { it.uri == layer.uri && it.sourceSize == source }
                    ?.takeIf { imported.count { asset -> asset.uri == layer.uri } == 1 }
                previous?.sourcePlacement ?: run {
                    val neutral = layer.copy(x = .5f, y = .5f, scale = 1f)
                    val bounds = ProMath.bounds(neutral, canvas.width, canvas.height, source.width, source.height)
                    val id = UUID.nameUUIDFromBytes(
                        ("legacy-image:" + index + ":" + layer.uri).toByteArray(Charsets.UTF_8)
                    ).toString()
                    ProSourcePlacement(id, (bounds.left + bounds.right) / (2f * canvas.width),
                        (bounds.top + bounds.bottom) / (2f * canvas.height), 1f)
                }
            }
            if (!recorded.valid()) return null
            recordImport(ProLayer(uri = layer.uri, name = layer.name, x = recorded.x, y = recorded.y,
                scale = recorded.scale), source, recorded.id, recorded)
        }
        return creation.copy(layers = layers)
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
        return ProStore.SavedProject(document, reset, project.linkedWork)
    }
}

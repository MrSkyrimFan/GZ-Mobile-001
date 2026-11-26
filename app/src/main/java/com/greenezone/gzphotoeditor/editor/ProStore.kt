package com.greenezone.gzphotoeditor.editor

import android.content.Context
import android.util.AtomicFile
import android.util.JsonReader
import android.util.JsonToken
import androidx.core.util.AtomicFile as ProjectAtomicFile
import java.io.FilterInputStream
import java.io.FilterReader
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.security.MessageDigest
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Base64

/** Versioned private JSON holding photo URIs and editable layer settings, never source image bytes. */
object ProStore {
    private const val VERSION = 7
    private const val LEGACY_ASPECT_MIN = .1f
    private const val LEGACY_ASPECT_MAX = 10f
    private const val MAX_BYTES = 3_000_000
    // A named save can carry one bounded document and one bounded reset baseline.
    private const val MAX_PROJECT_BYTES = MAX_BYTES * 2 + 512
    private const val RESET_DOCUMENT = "resetDocument"
    private const val MAX_SAVED = 12
    private const val DIR = "gz_pro_projects"
    private val whitespace = Regex("\\s+")
    internal class ProjectTooLarge : IllegalArgumentException()

    private fun projectReadLimit(): Long =
        (Runtime.getRuntime().maxMemory() / 8L).coerceAtMost(MAX_PROJECT_BYTES.toLong())

    private fun ProPixelSize.toJson(): JSONObject = JSONObject().put("width", width).put("height", height)

    private fun ProCanvasSetup.toJson(): JSONObject = JSONObject()
        .put("aspect", aspect.name).put("background", background.name).apply {
            sourceAspectRatio?.let { put("sourceAspectRatio", it.toDouble()) }
            canvasSize?.let { put("canvasSize", it.toJson()) }
        }

    private fun JSONObject.canvasSetup(): ProCanvasSetup? = optJSONObject("sourceCanvasSetup")?.let {
        ProCanvasSetup(value(it.getString("aspect"), ProAspect.SQUARE),
            value(it.getString("background"), ProBackground.WHITE),
            if (it.has("sourceAspectRatio")) it.getDouble("sourceAspectRatio").toFloat().also { v ->
                require(ProMath.validSourceAspect(v))
            } else null, it.pixelSize("canvasSize"))
    }

    private fun JSONObject.pixelSize(key: String): ProPixelSize? = optJSONObject(key)?.let {
        ProPixelSize(it.getInt("width"), it.getInt("height"))
    }

    fun encode(document: ProDocument): String {
        require(ProMath.valid(document))
        val layers = JSONArray()
        var recipeChars = 0
        document.layers.forEach { layer ->
            layers.put(JSONObject().apply {
                put("uri", layer.uri); put("name", layer.name)
                put("x", layer.x.toDouble()); put("y", layer.y.toDouble())
                put("scale", layer.scale.toDouble()); put("opacity", layer.opacity.toDouble())
                put("visible", layer.visible); put("blend", layer.blend.name)
                put("kind", layer.kind.name); put("aspect", layer.aspect.toDouble())
                layer.sourceSize?.let { put("sourceSize", it.toJson()) }
                layer.sourcePlacement?.let {
                    put("sourcePlacement", JSONObject().put("id", it.id).put("x", it.x.toDouble())
                        .put("y", it.y.toDouble()).put("scale", it.scale.toDouble()))
                }
                put("rotate", layer.rotationDegrees.toDouble())
                put("effect", layer.effect.name); put("effectAmount", layer.effectAmount.toDouble())
                layer.mask?.let { mask ->
                    val obj = JSONObject().apply {
                        put("kind", mask.kind.name)
                        put("x", mask.centerX.toDouble()); put("y", mask.centerY.toDouble())
                        put("width", mask.width.toDouble()); put("height", mask.height.toDouble())
                        put("invert", mask.inverted)
                        put("feather", mask.feather.toDouble())
                        mask.anchor?.let { anchor ->
                            put("imageAnchor", JSONObject().apply {
                                put("x", anchor.x.toDouble())
                                put("y", anchor.y.toDouble())
                                put("scale", anchor.scale.toDouble())
                                put("rotation", anchor.rotation.toDouble())
                                put("canvasAspect", anchor.canvasAspect.toDouble())
                                put("imageAspect", anchor.imageAspect.toDouble())
                            })
                        }
                        put("points", JSONArray().also { arr ->
                            mask.points.forEach { point ->
                                arr.put(JSONArray().put(point.x.toDouble()).put(point.y.toDouble()))
                            }
                        })
                    }
                    put("mask", obj)
                }
                put("retouches", JSONArray().apply {
                    layer.retouches.forEach { mark ->
                        put(JSONObject().apply {
                            put("kind", mark.kind.name)
                            put("sx", mark.sourceX.toDouble()); put("sy", mark.sourceY.toDouble())
                            put("tx", mark.targetX.toDouble()); put("ty", mark.targetY.toDouble())
                            put("radius", mark.radius.toDouble())
                            put("strength", mark.strength.toDouble())
                        })
                    }
                })
                val edits = try {
                    EditDocumentCodec.encodeBounded(
                        layer.edits, minOf(EditDocumentCodec.MAX_ENCODED_CHARS, MAX_BYTES - recipeChars)
                    )
                } catch (_: EditDocumentCodec.RecipeTooLarge) {
                    throw ProjectTooLarge()
                }
                recipeChars += edits.length
                put("edits", edits)
            })
        }
        val legacyAspectRange = LEGACY_ASPECT_MIN..LEGACY_ASPECT_MAX
        val extendedAspect = document.sourceAspectRatio != null || document.layers.any { layer ->
            val anchor = layer.mask?.anchor
            layer.aspect !in legacyAspectRange ||
                (anchor != null && (anchor.canvasAspect !in legacyAspectRange ||
                    anchor.imageAspect !in legacyAspectRange))
        }
        val pixelGeometry = document.canvasSize != null || document.layers.any { it.sourceSize != null }
        return JSONObject().apply {
            put("version", when {
                pixelGeometry -> VERSION
                extendedAspect -> 6
                else -> 5
            })
            put("aspect", document.aspect.name)
            document.sourceAspectRatio?.let { put("sourceAspectRatio", it.toDouble()) }
            document.canvasSize?.let { put("canvasSize", it.toJson()) }
            document.sourceCanvasSetup?.let { put("sourceCanvasSetup", it.toJson()) }
            if (document.linkedFinishes.isNotEmpty()) {
                put("linkedFinishes", JSONArray().also { items ->
                    document.linkedFinishes.forEach { items.put(NativeWorkCodec.encodeFinish(it)) }
                })
            }
            put("background", document.background.name)
            put("layers", layers)
        }.toString().also {
            if (it.toByteArray(Charsets.UTF_8).size > MAX_BYTES) throw ProjectTooLarge()
        }
    }
    private inline fun <reified T: Enum<T>> value(name: String, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: fallback

    fun decode(data: String): ProDocument? = decode(data, recoverLegacyRecipe = false)

    private fun decode(data: String, recoverLegacyRecipe: Boolean): ProDocument? {
        if (data.length > MAX_BYTES) return null
        return try {
            decode(JSONObject(data), recoverLegacyRecipe)
        } catch (_: Exception) { null }
    }

    private fun decode(obj: JSONObject, recoverLegacyRecipe: Boolean): ProDocument? {
        return try {
            val version = obj.optInt("version", -1)
            if (version !in 1..VERSION) return null
            val array = obj.optJSONArray("layers") ?: return null
            if (array.length() !in 1..ProMath.MAX_LAYERS) return null
            fun unit(o: JSONObject, key: String, default: Double, lo: Float, hi: Float): Float {
                val raw = o.optDouble(key, default).toFloat()
                if (!raw.isFinite() || raw < lo || raw > hi) throw IllegalArgumentException(key)
                return raw
            }
            val aspectMin = if (version >= 6) ProMath.MIN_SOURCE_ASPECT else LEGACY_ASPECT_MIN
            val aspectMax = if (version >= 6) ProMath.MAX_SOURCE_ASPECT else LEGACY_ASPECT_MAX
            val layers = (0 until array.length()).map { idx ->
                val o = array.optJSONObject(idx) ?: return null
                val uri = o.optString("uri", "")
                if (uri.length > 4096) return null
                val recipe = o.optString("edits", "")
                val edits = if (recoverLegacyRecipe && recipe.length > EditDocumentCodec.MAX_ENCODED_CHARS) {
                    EditDocumentCodec.recoverProRecipe(recipe, MAX_BYTES)
                } else EditDocumentCodec.decode(recipe)
                if (edits == null) return null
                val mask = o.optJSONObject("mask")?.let { json ->
                    val raw = json.optJSONArray("points") ?: JSONArray()
                    if (raw.length() > ProToolMath.MAX_LASSO_POINTS) return null
                    val points = (0 until raw.length()).map { k ->
                        val p = raw.optJSONArray(k) ?: return null
                        if (p.length() != 2) return null
                        BrushPoint(p.optDouble(0, Double.NaN).toFloat(),
                            p.optDouble(1, Double.NaN).toFloat())
                    }
                    ProSelection(
                        kind = value(json.optString("kind", ""), ProSelectionKind.RECTANGLE),
                        centerX = unit(json, "x", .5, 0f, 1f),
                        centerY = unit(json, "y", .5, 0f, 1f),
                        width = unit(json, "width", .6, 0f, 1f),
                        height = unit(json, "height", .6, 0f, 1f),
                        points = points, inverted = json.optBoolean("invert", false),
                        feather = unit(json, "feather", 0.0, 0f, .2f),
                        anchor = if (version >= 5 && json.has("imageAnchor")) {
                            val anchor = json.getJSONObject("imageAnchor")
                            ProMaskAnchor(
                                unit(anchor, "x", Double.NaN, 0f, 1f),
                                unit(anchor, "y", Double.NaN, 0f, 1f),
                                unit(anchor, "scale", Double.NaN, .1f, 2f),
                                unit(anchor, "rotation", Double.NaN, -180f, 180f),
                                unit(anchor, "canvasAspect", Double.NaN, aspectMin, aspectMax),
                                unit(anchor, "imageAspect", Double.NaN, aspectMin, aspectMax)
                            )
                        } else null
                    ).takeIf(ProToolMath::valid) ?: return null
                }
                val rawRetouch = o.optJSONArray("retouches") ?: JSONArray()
                if (rawRetouch.length() > ProToolMath.MAX_RETOUCH) return null
                val retouches = (0 until rawRetouch.length()).map { k ->
                    val item = rawRetouch.optJSONObject(k) ?: return null
                    ProRetouchMark(
                        kind = value(item.optString("kind", ""), ProRetouchKind.CLONE),
                        sourceX = unit(item, "sx", .5, 0f, 1f),
                        sourceY = unit(item, "sy", .5, 0f, 1f),
                        targetX = unit(item, "tx", .5, 0f, 1f),
                        targetY = unit(item, "ty", .5, 0f, 1f),
                        radius = unit(item, "radius", .04, .005f, .2f),
                        strength = unit(item, "strength", .85, .1f, 1f)
                    ).takeIf(ProToolMath::valid) ?: return null
                }
                ProLayer(
                    uri = uri, edits = edits, name = o.optString("name", "").take(42),
                    x = unit(o, "x", .5, 0f, 1f), y = unit(o, "y", .5, 0f, 1f),
                    scale = unit(o, "scale", 1.0, .1f, 2f),
                    opacity = unit(o, "opacity", 1.0, 0f, 1f),
                    visible = o.optBoolean("visible", true),
                    blend = value(o.optString("blend", ""), ProBlend.NORMAL),
                    kind = value(o.optString("kind", ""), ProLayerKind.IMAGE),
                    aspect = unit(o, "aspect", 1.0, aspectMin, aspectMax),
                    rotationDegrees = unit(o, "rotate", 0.0, -180f, 180f),
                    mask = mask, retouches = retouches,
                    effect = value(o.optString("effect", ""), ProPixelEffect.NONE),
                    effectAmount = unit(o, "effectAmount", .5, 0f, 1f),
                    sourceSize = if (version >= 7) o.pixelSize("sourceSize") else null,
                    sourcePlacement = o.optJSONObject("sourcePlacement")?.let {
                        ProSourcePlacement(it.getString("id"), it.getDouble("x").toFloat(),
                            it.getDouble("y").toFloat(), it.getDouble("scale").toFloat())
                    }
                )
            }
            val sourceAspectRatio = if (version >= 6 && obj.has("sourceAspectRatio")) {
                unit(obj, "sourceAspectRatio", Double.NaN, ProMath.MIN_SOURCE_ASPECT, ProMath.MAX_SOURCE_ASPECT)
            } else null
            ProDocument(layers,
                value(obj.optString("aspect", ""), ProAspect.SQUARE),
                value(obj.optString("background", ""), ProBackground.WHITE),
                sourceAspectRatio,
                if (version >= 7) obj.pixelSize("canvasSize") else null,
                obj.canvasSetup(),
                obj.optJSONArray("linkedFinishes")?.let { items ->
                    require(items.length() <= ProMath.MAX_LINKED_FINISHES)
                    List(items.length()) { NativeWorkCodec.decodeFinish(items.getJSONObject(it)) }
                }.orEmpty())
                .takeIf { ProMath.valid(it) }
        } catch (_: Exception) { null }
    }

    data class SavedProject(
        val document: ProDocument,
        val resetDocument: ProDocument,
        val linkedWork: NativeManifestRef? = null
    )

    internal fun encodeProject(document: ProDocument, resetDocument: ProDocument?): ByteArray {
        val encoded = encode(document)
        if (resetDocument == null || resetDocument == document) return encoded.toByteArray(Charsets.UTF_8)
        val baseline = if (resetDocument.layers.isEmpty()) {
            JSONObject().apply {
                put("empty", true)
                put("aspect", resetDocument.aspect.name)
                put("background", resetDocument.background.name)
                resetDocument.sourceAspectRatio?.let { put("sourceAspectRatio", it.toDouble()) }
                resetDocument.canvasSize?.let { put("canvasSize", it.toJson()) }
                resetDocument.sourceCanvasSetup?.let { put("sourceCanvasSetup", it.toJson()) }
            }
        } else JSONObject(encode(resetDocument))
        // The optional field leaves the existing root document and codec version intact.
        return JSONObject(encoded).put(RESET_DOCUMENT, baseline).toString().toByteArray(Charsets.UTF_8)
            .also { if (it.size > MAX_PROJECT_BYTES) throw ProjectTooLarge() }
    }

    internal fun decodeProject(data: String): SavedProject? {
        if (data.length > MAX_PROJECT_BYTES) return null
        return decodeProject(JSONObject(data), data.length)
    }

    private fun decodeProject(json: JSONObject, encodedChars: Int): SavedProject? {
        if (encodedChars > MAX_PROJECT_BYTES) return null
        val reset = json.remove(RESET_DOCUMENT) as? JSONObject
        if (encodedChars > MAX_BYTES) {
            if (reset == null || json.toString().length > MAX_BYTES ||
                reset.toString().length > MAX_BYTES
            ) return null
        }
        val document = decode(json, recoverLegacyRecipe = true) ?: return null
        val baseline = reset?.let {
            if (it.optBoolean("empty")) {
                ProDocument(
                    aspect = value(it.optString("aspect"), ProAspect.SQUARE),
                    background = value(it.optString("background"), ProBackground.WHITE),
                    sourceAspectRatio = it.optDouble("sourceAspectRatio").toFloat()
                        .takeIf(ProMath::validSourceAspect),
                    canvasSize = runCatching { it.pixelSize("canvasSize") }.getOrNull(),
                    sourceCanvasSetup = it.canvasSetup()
                )
            } else decode(it, recoverLegacyRecipe = true)
        }
        // A legacy or unreadable optional baseline must never clear the user's saved work.
        return SavedProject(document, baseline ?: document)
    }

    private fun dir(context: Context): File = File(context.filesDir, DIR).also { it.mkdirs() }
    private data class ProjectPath(val name: String, val base: File) {
        val backup: File get() = File(base.path + ".bak")
        fun exists(): Boolean = base.exists() || backup.isFile || NamedProjectIdentity.hasPendingPublication(base)
    }

    private fun projectPath(context: Context, name: String): ProjectPath? {
        val legacy = name.trim().replace(whitespace, " ").take(32)
            .toByteArray(Charsets.UTF_8).toString(Charsets.UTF_8)
        val canonical = legacy.trimEnd()
        if (canonical.isBlank()) return null
        val directory = dir(context)
        fun path(storedName: String): ProjectPath {
            val id = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(storedName.toByteArray(Charsets.UTF_8))
            return ProjectPath(storedName, File(directory, "$id.gzpro"))
        }
        // Old names truncated at a space may still exist in the project list.
        if (name.length == 32 && name.endsWith(' ') && name.dropLast(1) == canonical) {
            path(name).takeIf { it.exists() }?.let { return it }
        }
        val current = path(canonical)
        return if (current.exists() || canonical == legacy) current
            else path(legacy).takeIf { it.exists() } ?: current
    }
    @Synchronized fun names(context: Context): List<String> {
        NamedProjectIdentity.recoverDirectory(dir(context), ".gzpro", projectReadLimit())
        return dir(context).listFiles()?.mapNotNull {
            if (!it.isFile) return@mapNotNull null
            // Both legacy backups and unresolved publication markers retain a slot.
            val filename = it.name.removeSuffix(".bak").removeSuffix(".save-pending")
            val stem = filename.takeIf { name -> name.endsWith(".gzpro") }
                ?.removeSuffix(".gzpro") ?: return@mapNotNull null
            try {
                String(Base64.getUrlDecoder().decode(stem), Charsets.UTF_8)
            } catch (_: Exception) { null }
        }?.distinct()?.sortedBy { it.lowercase() } ?: emptyList()
    }

    internal sealed interface SaveResult {
        data class Saved(val name: String) : SaveResult
        data class Exists(val name: String) : SaveResult
        data object TooLarge : SaveResult
        data object Failed : SaveResult
    }

    fun save(
        context: Context,
        name: String,
        document: ProDocument,
        resetDocument: ProDocument? = null
    ): Boolean = writeProject(context, name, document, resetDocument, null, true, null) is ProjectSaveResult.Saved

    internal fun saveProject(context: Context, name: String, document: ProDocument,
        resetDocument: ProDocument?, replaceName: String?): SaveResult =
        when (val result = writeProject(context, name, document, resetDocument, replaceName, false, null)) {
            is ProjectSaveResult.Saved -> SaveResult.Saved(result.project.name)
            is ProjectSaveResult.Exists -> SaveResult.Exists(result.project.name)
            is ProjectSaveResult.Rejected -> if (result.reason == ProjectSaveFailure.TOO_LARGE)
                SaveResult.TooLarge else SaveResult.Failed
        }

    fun saveNamed(context: Context, intent: ProjectSaveIntent,
        document: ProDocument, resetDocument: ProDocument? = null): ProjectSaveResult =
        writeProject(context, intent.requestedName, document, resetDocument, null, true, intent)

    internal fun saveLinkedNamed(
        context: Context, intent: ProjectSaveIntent, document: ProDocument,
        resetDocument: ProDocument?, linkedWork: NativeManifestRef
    ): ProjectSaveResult =
        writeProject(context, intent.requestedName, document, resetDocument, null, true, intent, linkedWork)

    @Synchronized private fun writeProject(context: Context, name: String, document: ProDocument,
        resetDocument: ProDocument?, replaceName: String?, replaceAny: Boolean,
        intent: ProjectSaveIntent?, linkedWork: NativeManifestRef? = null): ProjectSaveResult {
        if (!ProMath.valid(document) || (linkedWork == null && document.linkedFinishes.isNotEmpty()) ||
            (resetDocument != null && resetDocument.layers.isNotEmpty() && !ProMath.valid(resetDocument)))
            return ProjectSaveResult.Rejected(ProjectSaveFailure.INVALID_DOCUMENT)
        if (intent?.expectedProject?.kind?.let { it != ProjectKind.PRO } == true)
            return ProjectSaveResult.Rejected(ProjectSaveFailure.TARGET_CHANGED)
        return try {
            val target = projectPath(context, name)
                ?: return ProjectSaveResult.Rejected(ProjectSaveFailure.INVALID_NAME)
            NamedProjectIdentity.recoverPublication(target.base, projectReadLimit())
            val exists = target.exists()
            val current = if (exists) NamedProjectIdentity.reference(context, ProjectKind.PRO,
                target.name, NamedProjectIdentity.fileVersion(target.base, projectReadLimit())
                    ?: return ProjectSaveResult.Rejected(ProjectSaveFailure.STORAGE_FAILURE)) else null
            if (intent != null) NamedProjectIdentity.authorize(ProjectKind.PRO, intent, current)
                ?.let { return it }
            else if (current != null && !replaceAny && replaceName != target.name)
                return ProjectSaveResult.Exists(current)
            if (!exists && names(context).size >= MAX_SAVED)
                return ProjectSaveResult.Rejected(ProjectSaveFailure.FULL)
            val nativePayload = encodeProject(document, resetDocument)
            if (nativePayload.size.toLong() > projectReadLimit())
                return ProjectSaveResult.Rejected(ProjectSaveFailure.TOO_LARGE)
            val payload = linkedWork?.let(NativeWorkCodec::namedBytes) ?: nativePayload
            val identity = current?.id ?: NamedProjectIdentity.reserve(context, ProjectKind.PRO,
                target.name, payloadExists = false)
            val version = NamedProjectIdentity.publish(target.base, payload, projectReadLimit())
            ProjectSaveResult.Saved(NamedProjectRef(ProjectKind.PRO, identity, target.name, version))
        } catch (_: ProjectTooLarge) {
            ProjectSaveResult.Rejected(ProjectSaveFailure.TOO_LARGE)
        } catch (_: OutOfMemoryError) {
            ProjectSaveResult.Rejected(ProjectSaveFailure.TOO_LARGE)
        } catch (_: Exception) {
            ProjectSaveResult.Rejected(ProjectSaveFailure.STORAGE_FAILURE)
        }
    }

    @Synchronized fun loadNamed(context: Context, name: String): NamedProject<SavedProject>? = try {
        val target = projectPath(context, name)
        if (target == null) null else {
            NamedProjectIdentity.readPayload(target.base, projectReadLimit()) { input, length ->
                readProject(context, input, length)
            }?.let { loaded ->
                NamedProject(NamedProjectIdentity.reference(context, ProjectKind.PRO,
                    target.name, loaded.version), loaded.content)
            }
        }
    } catch (_: OutOfMemoryError) { null } catch (_: Exception) { null }

    /** Resolve only the selected row without decoding its editable recipe. */
    @Synchronized fun deletionTarget(context: Context, name: String): NamedProjectRef? {
        return try {
            val target = projectPath(context, name) ?: return null
            readDeletionTarget(context, target)
        } catch (_: Exception) { null }
    }

    private fun readDeletionTarget(context: Context, target: ProjectPath): NamedProjectRef? {
        val base = target.base
        val stage = File(base.path + ".new")
        // Recovery can remove transaction files. A non-regular occupant is an
        // obstruction, even when it is an empty directory or a symlink.
        val transactionBases = listOf(base, File(base.path + ".save-pending"),
            File(base.path + ".save-previous"))
        for (file in transactionBases) {
            for (path in listOf(file, File(file.path + ".bak"), File(file.path + ".new"))) {
                val nio = path.toPath()
                if (!Files.notExists(nio, NOFOLLOW_LINKS) && !Files.isRegularFile(nio, NOFOLLOW_LINKS))
                    return null
            }
        }
        NamedProjectIdentity.recoverPublication(base, MAX_PROJECT_BYTES.toLong())
        if (!base.isFile && !target.backup.isFile) return null

        val version = ProjectAtomicFile(base).openRead().use { source ->
            // A failed legacy backup promotion must not leave two possible
            // versions, or a backup which could resurrect a deleted record.
            if (!Files.isRegularFile(base.toPath(), NOFOLLOW_LINKS) ||
                !Files.notExists(target.backup.toPath(), NOFOLLOW_LINKS) ||
                !Files.notExists(stage.toPath(), NOFOLLOW_LINKS) ||
                NamedProjectIdentity.hasPendingPublication(base)
            ) return null

            val length = source.channel.size()
            var remaining = length
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            // The selected on-disk length bounds IO. Empty and oversized files
            // still get exact versions without allocating for the full file.
            while (remaining > 0) {
                val count = source.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
                if (count <= 0) return null
                digest.update(buffer, 0, count)
                remaining -= count
            }
            if (source.read() != -1 || source.channel.size() != length) return null
            digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
        }
        return NamedProjectIdentity.reference(context, ProjectKind.PRO, target.name, version)
    }

    @Synchronized fun deleteNamed(context: Context, expected: NamedProjectRef): Boolean {
        if (expected.kind != ProjectKind.PRO) return false
        return try {
            val target = projectPath(context, expected.name) ?: return false
            if (readDeletionTarget(context, target) != expected) return false
            // Recovery above left one authoritative file. One unlink commits
            // deletion without removing only part of a recoverable transaction.
            Files.delete(target.base.toPath())
            if (!Files.notExists(target.base.toPath(), NOFOLLOW_LINKS) || target.exists()) return false
            NamedProjectIdentity.forget(context, ProjectKind.PRO, target.name)
            true
        } catch (_: Exception) { false }
    }

    fun load(context: Context, name: String): ProDocument? = loadProject(context, name)?.document

    @Synchronized internal fun loadProject(context: Context, name: String): SavedProject? {
        val target = projectPath(context, name) ?: return null
        val base = target.base
        try { NamedProjectIdentity.recoverPublication(base, projectReadLimit()) }
        catch (_: Exception) { return null }
        val backup = target.backup
        // Fail before allocating strings or reading hostile oversize projects.
        // On legacy backup-only recovery AtomicFile will rename the backup to base.
        val recoverable = if (backup.exists()) backup else base
        if (!recoverable.isFile || recoverable.length() > projectReadLimit()) return null
        val atomic = AtomicFile(base)
        return try {
            atomic.openRead().use { input ->
                val length = input.channel.size()
                if (length !in 1L..projectReadLimit()) return null
                readProject(context, input, length)
            }
        } catch (_: OutOfMemoryError) {
            null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Parse saved JSON through a small reader buffer. JSONTokener grows a UTF-16
     * builder around each escaped recipe and can transiently need several times
     * the file budget even though the recipe itself is readable.
     */
    private fun readProject(context: Context, input: InputStream, length: Long): SavedProject? {
        val bounded = object : FilterInputStream(input) {
            private var remaining = length
            override fun read(): Int {
                val value = input.read()
                if (remaining == 0L) {
                    if (value != -1) throw IOException("Project grew while reading")
                    return -1
                }
                if (value == -1) throw IOException("Truncated project")
                remaining--
                return value
            }
            override fun read(bytes: ByteArray, offset: Int, count: Int): Int {
                if (count == 0) return 0
                if (remaining == 0L) return read()
                val read = input.read(bytes, offset, minOf(count.toLong(), remaining).toInt())
                if (read == -1) throw IOException("Truncated project")
                remaining -= read
                return read
            }
        }
        var chars = 0
        val counted = object : FilterReader(bounded.reader(Charsets.UTF_8)) {
            override fun read(buffer: CharArray, offset: Int, count: Int): Int =
                super.read(buffer, offset, count).also { if (it > 0) chars += it }
        }
        return JsonReader(counted).use { reader ->
            val json = readProjectValue(reader, 0) as? JSONObject ?: return null
            if (reader.peek() != JsonToken.END_DOCUMENT) return null
            if (json.has(NativeWorkCodec.NAMED_TAG)) {
                val manifest = NativeWorkCodec.namedManifest(json) ?: return null
                return NativeWorkStore.readLinkedPro(context, manifest)
            }
            decodeProject(json, chars)
        }
    }

    @Synchronized
    internal fun namedManifests(context: Context): Set<NativeManifestRef>? = try {
        names(context).mapNotNullTo(mutableSetOf()) { name ->
            val path = projectPath(context, name) ?: throw IOException("Invalid Pro project path")
            if (NamedProjectIdentity.hasPendingPublication(path.base) || path.backup.exists())
                throw IOException("Unresolved Pro project publication")
            NativeWorkCodec.peekManifest(path.base)
        }
    } catch (_: Exception) { null }

    private fun readProjectValue(reader: JsonReader, depth: Int): Any {
        // Our layer/mask/reset schema is shallow; malformed input must not recurse
        // indefinitely before the existing document validator gets to inspect it.
        require(depth <= 32) { "Project JSON nesting is too deep" }
        return when (reader.peek()) {
            JsonToken.BEGIN_OBJECT -> JSONObject().apply {
                reader.beginObject()
                while (reader.hasNext()) put(reader.nextName(), readProjectValue(reader, depth + 1))
                reader.endObject()
            }
            JsonToken.BEGIN_ARRAY -> JSONArray().apply {
                reader.beginArray()
                while (reader.hasNext()) put(readProjectValue(reader, depth + 1))
                reader.endArray()
            }
            JsonToken.STRING -> reader.nextString()
            JsonToken.NUMBER -> reader.nextString().let { it.toLongOrNull() ?: it.toDouble() }
            JsonToken.BOOLEAN -> reader.nextBoolean()
            JsonToken.NULL -> { reader.nextNull(); JSONObject.NULL }
            else -> throw IllegalArgumentException("Expected project JSON value")
        }
    }

    @Synchronized fun delete(context: Context, name: String): Boolean {
        val target = projectPath(context, name) ?: return false
        val file = target.base
        try { NamedProjectIdentity.recoverPublication(file, projectReadLimit()) }
        catch (_: Exception) { return false }
        val projectFiles = listOf(file, target.backup, File(file.path + ".new"))
        if (projectFiles.none { it.exists() }) return false
        AtomicFile(file).delete()
        val deleted = projectFiles.none { it.exists() }
        if (deleted) NamedProjectIdentity.forget(context, ProjectKind.PRO, target.name)
        return deleted
    }
}

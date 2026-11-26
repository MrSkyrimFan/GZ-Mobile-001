package com.greenezone.gzphotoeditor.editor

import android.net.Uri
import java.io.File
import java.util.IdentityHashMap
import org.json.JSONArray
import org.json.JSONObject

/** Versioned custody metadata wraps unchanged Regular and Pro native record codecs. */
internal class NativeWorkCodec(
    private val files: NativeWorkFiles,
    private val checkBudget: () -> Unit = {}
) {
    private val encodedRegular = IdentityHashMap<EditDocument, NativeRevisionRef>()
    private val encodedPro = IdentityHashMap<ProDocument, NativeRevisionRef>()
    private val decodedRegular = mutableMapOf<String, EditDocument>()
    private val decodedPro = mutableMapOf<String, ProDocument>()
    private val keptRecords = linkedSetOf<String>()
    private val keptDerivatives = linkedSetOf<String>()

    fun encode(state: NativeWorkState): NativeWorkPrepared {
        require(state.id.length in 1..MAX_KEY && state.activeExists())
        val regularState = state.regular?.let(::encodeRegularState)
        val proState = state.pro?.let(::encodeProState)
        val link = state.link?.let { current ->
            val originRegular = current.regularOrigin ?: state.regular?.let { regular(it.document) }
            val originPro = current.proOrigin ?: current.derivative?.parent
                ?: state.pro?.let { pro(it.document) }
            current.copy(regularOrigin = originRegular, proOrigin = originPro)
        }
        val json = JSONObject().put("version", VERSION).put("id", state.id).put("mode", state.mode.name)
        regularState?.let { json.put("regular", it) }
        proState?.let { json.put("pro", it) }
        link?.let { json.put("link", encodeLink(it)) }
        json.put("records", JSONArray(keptRecords.toList()))
        json.put("derivatives", JSONArray(keptDerivatives.toList()))
        val bytes = json.toString().toByteArray(Charsets.UTF_8)
        val ref = NativeManifestRef(NativeWorkFiles.digest(bytes))
        files.put(files.manifest(ref.sha), bytes, NativeWorkFiles.MAX_INDEX_BYTES)
        return NativeWorkPrepared(ref, state.copy(link = link))
    }

    fun open(ref: NativeManifestRef): JSONObject {
        val file = files.manifest(ref.sha)
        if (!NativeWorkFiles.regular(file)) throw NativeWorkUnavailable()
        return JSONObject(String(files.read(file, file.length(), NativeWorkFiles.MAX_INDEX_BYTES), Charsets.UTF_8))
            .also { require(it.getInt("version") == VERSION) }
    }

    fun decode(ref: NativeManifestRef): NativeWorkState {
        val json = open(ref)
        val state = NativeWorkState(
            checkedKey(json.getString("id")), NativeWorkMode.valueOf(json.getString("mode")),
            json.optJSONObject("regular")?.let(::decodeRegularState),
            json.optJSONObject("pro")?.let(::decodeProState),
            json.optJSONObject("link")?.let(::decodeLink)
        )
        require(state.activeExists())
        return state
    }

    private fun NativeWorkState.activeExists(): Boolean =
        if (mode == NativeWorkMode.REGULAR) regular != null else pro != null

    private fun encodeRegularState(state: NativeRegularState): JSONObject = JSONObject()
        .put("owner", checkedKey(state.ownerId))
        .put("source", checkedKey(state.sourceId))
        .put("sourceRevision", validRevision(state.sourceRevision))
        .put("contentRevision", validRevision(state.contentRevision))
        .put("uri", keepSource(state.uri))
        .put("document", jsonRef(regular(state.document)))
        .put("entry", jsonRef(regular(state.entry)))
        .put("clean", jsonRef(regular(state.clean)))
        .put("reset", jsonRef(regular(state.reset)))
        .put("history", history(state.history.undo, state.history.redo, REGULAR_HISTORY) { jsonRef(regular(it)) })
        .apply { state.savedRef?.let { put("saved", namedRef(it, ProjectKind.PHOTO)) } }

    private fun decodeRegularState(json: JSONObject): NativeRegularState {
        val history = readHistory(json.getJSONObject("history"), REGULAR_HISTORY) {
            regular(readRef(it, NativeWorkMode.REGULAR))
        }
        return NativeRegularState(
            checkedKey(json.getString("owner")), checkedKey(json.getString("source")),
            validRevision(json.getLong("sourceRevision")), validRevision(json.getLong("contentRevision")),
            sourceUri(json.getString("uri")), regular(readRef(json.getJSONObject("document"), NativeWorkMode.REGULAR)),
            regular(readRef(json.getJSONObject("entry"), NativeWorkMode.REGULAR)),
            regular(readRef(json.getJSONObject("clean"), NativeWorkMode.REGULAR)),
            regular(readRef(json.getJSONObject("reset"), NativeWorkMode.REGULAR)),
            EditHistory(history.first, history.second),
            json.optJSONObject("saved")?.let { namedRef(it, ProjectKind.PHOTO) }
        )
    }

    fun linkedRegular(name: String, ref: NativeManifestRef): SavedProject? {
        val root = open(ref)
        if (root.getString("mode") != NativeWorkMode.REGULAR.name) return null
        val state = root.getJSONObject("regular")
        return SavedProject(name, sourceUri(state.getString("uri")),
            regular(readRef(state.getJSONObject("document"), NativeWorkMode.REGULAR)), ref)
    }

    fun linkedPro(ref: NativeManifestRef): ProStore.SavedProject? {
        val root = open(ref)
        if (root.getString("mode") != NativeWorkMode.PRO.name) return null
        val state = root.getJSONObject("pro")
        val doc = pro(readRef(state.getJSONObject("document"), NativeWorkMode.PRO))
        val reset = state.optJSONObject("reset")?.let { pro(readRef(it, NativeWorkMode.PRO)) } ?: doc
        if (!ProMath.valid(doc)) return null
        return ProStore.SavedProject(doc, reset, ref)
    }

    private fun encodeProState(state: NativeProState): JSONObject = JSONObject()
        .put("owner", checkedKey(state.ownerId)).put("document", jsonRef(pro(state.document)))
        .put("established", state.documentEstablished)
        .put("sourceRevision", validRevision(state.sourceRevision))
        .put("contentRevision", validRevision(state.contentRevision))
        .put("selected", state.selectedLayer)
        .put("target", encodeTarget(state.saveTarget))
        .put("nextWorkId", validRevision(state.nextWorkId))
        .put("history", history(state.history.undo, state.history.redo, NativeWorkFiles.MAX_HISTORY, ::encodeProHistory))
        .apply {
            state.entry?.let { put("entry", jsonRef(pro(it))) }
            state.reset?.let { put("reset", jsonRef(pro(it))) }
            require(state.baselines.size <= 2 * NativeWorkFiles.MAX_HISTORY + 1)
            put("baselines", JSONArray().also { baselines ->
                state.baselines.toSortedMap().forEach { (id, baseline) ->
                    baselines.put(JSONObject().put("workId", validRevision(id))
                        .put("entry", jsonRef(pro(baseline.entryDocument))).apply {
                            baseline.savedDocument?.let { put("saved", jsonRef(pro(it))) }
                        })
                }
            })
        }

    private fun decodeProState(json: JSONObject): NativeProState {
        val document = pro(readRef(json.getJSONObject("document"), NativeWorkMode.PRO))
        val history = readHistory(json.getJSONObject("history"), NativeWorkFiles.MAX_HISTORY, ::decodeProHistory)
        val baselines = json.getJSONArray("baselines")
        require(baselines.length() <= 2 * NativeWorkFiles.MAX_HISTORY + 1)
        return NativeProState(
            checkedKey(json.getString("owner")), document, json.getBoolean("established"),
            validRevision(json.getLong("contentRevision")), validRevision(json.getLong("sourceRevision")),
            selected(json.getInt("selected"), document), decodeTarget(json.getJSONObject("target")),
            validRevision(json.getLong("nextWorkId")),
            json.optJSONObject("entry")?.let { pro(readRef(it, NativeWorkMode.PRO)) },
            json.optJSONObject("reset")?.let { pro(readRef(it, NativeWorkMode.PRO)) },
            (0 until baselines.length()).associate {
                val value = baselines.getJSONObject(it)
                validRevision(value.getLong("workId")) to ProWorkBaseline(
                    pro(readRef(value.getJSONObject("entry"), NativeWorkMode.PRO)),
                    value.optJSONObject("saved")?.let { ref -> pro(readRef(ref, NativeWorkMode.PRO)) }
                )
            },
            ProHistory(history.first, history.second)
        )
    }

    private fun encodeProHistory(entry: ProHistoryEntry): JSONObject = JSONObject()
        .put("document", jsonRef(pro(entry.document)))
        .put("selected", selected(entry.selectedLayer, entry.document))
        .put("target", encodeTarget(entry.saveTarget))
        .apply { entry.resetDocument?.let { put("reset", jsonRef(pro(it))) } }

    private fun decodeProHistory(entry: JSONObject): ProHistoryEntry {
        val document = pro(readRef(entry.getJSONObject("document"), NativeWorkMode.PRO))
        return ProHistoryEntry(document, entry.optJSONObject("reset")?.let { pro(readRef(it, NativeWorkMode.PRO)) },
            selected(entry.getInt("selected"), document), decodeTarget(entry.getJSONObject("target")))
    }

    private fun selected(index: Int, doc: ProDocument): Int =
        index.coerceIn(0, doc.layers.lastIndex.coerceAtLeast(0))

    private fun encodeTarget(target: ProSaveTarget): JSONObject = JSONObject()
        .put("workId", validRevision(target.workId)).apply {
            target.name?.let { put("name", checkedKey(it)) }
            target.savedRef?.let { put("saved", namedRef(it, ProjectKind.PRO)) }
            target.nativeOwnerId?.let { put("owner", checkedKey(it)) }
        }

    private fun decodeTarget(json: JSONObject): ProSaveTarget = ProSaveTarget(
        validRevision(json.getLong("workId")), optionalKey(json, "name"),
        json.optJSONObject("saved")?.let { namedRef(it, ProjectKind.PRO) }, optionalKey(json, "owner")
    )

    private fun encodeLink(link: NativeWorkLink): JSONObject = JSONObject().apply {
        link.ownedPlacementId?.let { put("placement", checkedKey(it)) }
        link.regularOrigin?.let { put("regular", keepRef(it, NativeWorkMode.REGULAR)) }
        link.proOrigin?.let { put("pro", keepRef(it, NativeWorkMode.PRO)) }
        link.derivative?.let {
            verifyDerivative(it)
            put("derivative", JSONObject().put("size", sizeJson(it.size)).put("sha", it.sha)
                .put("parent", keepRef(it.parent, NativeWorkMode.PRO)).put("bytes", it.bytes)
                .put("alpha", it.hasAlpha))
        }
        link.regularInput?.let { put("input", sizeJson(it)) }
    }

    private fun decodeLink(json: JSONObject): NativeWorkLink = NativeWorkLink(
        optionalKey(json, "placement"),
        json.optJSONObject("regular")?.let { readRef(it, NativeWorkMode.REGULAR).also(::verifyRef) },
        json.optJSONObject("pro")?.let { readRef(it, NativeWorkMode.PRO).also(::verifyRef) },
        json.optJSONObject("derivative")?.let {
            val sha = checkedSha(it.getString("sha"))
            NativeDerivativeRef(files.derivativeUri(sha), readSize(it.getJSONObject("size")), sha,
                readRef(it.getJSONObject("parent"), NativeWorkMode.PRO).also(::verifyRef),
                it.getLong("bytes"), it.getBoolean("alpha")).also(::verifyDerivative)
        },
        json.optJSONObject("input")?.let(::readSize)
    )

    fun verifyDerivative(ref: NativeDerivativeRef) {
        checkBridgeSize(ref.size)
        require(ref.parent.mode == NativeWorkMode.PRO && files.derivativeUri(ref.sha) == ref.uri)
        files.verify(files.derivative(ref.sha), ref.bytes, ref.sha, NativeWorkFiles.MAX_DERIVATIVE_BYTES)
        keptDerivatives += ref.sha
    }

    private fun keepRef(ref: NativeRevisionRef, mode: NativeWorkMode): JSONObject {
        require(ref.mode == mode)
        if (ref.sha !in keptRecords) verifyRef(ref)
        keptRecords += ref.sha
        return jsonRef(ref)
    }

    private fun verifyRef(ref: NativeRevisionRef) =
        files.verify(files.record(ref.sha), ref.bytes, ref.sha, max(ref.mode))

    fun regular(document: EditDocument): NativeRevisionRef = encodedRegular.getOrPut(document) {
        checkBudget()
        write(NativeWorkMode.REGULAR, EditDocumentCodec.encodeBounded(document).toByteArray(Charsets.UTF_8))
    }

    fun pro(document: ProDocument): NativeRevisionRef = encodedPro.getOrPut(document) {
        checkBudget()
        document.layers.filter { it.kind == ProLayerKind.IMAGE }.forEach { keepSource(sourceUri(it.uri)) }
        document.linkedFinishes.forEach(::keepFinish)
        val encoded = if (document.layers.isEmpty()) emptyPro(document).toString() else ProStore.encode(document)
        write(NativeWorkMode.PRO, encoded.toByteArray(Charsets.UTF_8))
    }

    private fun regular(ref: NativeRevisionRef): EditDocument = decodedRegular.getOrPut(ref.sha) {
        checkBudget()
        EditDocumentCodec.decode(read(ref)) ?: throw NativeWorkUnavailable()
    }

    private fun pro(ref: NativeRevisionRef): ProDocument = decodedPro.getOrPut(ref.sha) {
        checkBudget()
        val source = read(ref)
        val value = if (source.startsWith("{\"empty\":true")) decodeEmptyPro(JSONObject(source))
            else ProStore.decode(source) ?: throw NativeWorkUnavailable()
        value.linkedFinishes.forEach(::keepFinish)
        value
    }

    private fun read(ref: NativeRevisionRef) =
        String(files.read(files.record(ref.sha), ref.bytes, max(ref.mode)), Charsets.UTF_8)

    private fun write(mode: NativeWorkMode, bytes: ByteArray): NativeRevisionRef {
        val sha = NativeWorkFiles.digest(bytes)
        if (keptRecords.add(sha)) files.put(files.record(sha), bytes, max(mode))
        return NativeRevisionRef(mode, sha, bytes.size.toLong())
    }

    private fun keepFinish(ref: ProLinkedFinishRef) {
        val file = files.record(ref.sha)
        if (keptRecords.add(ref.sha)) {
            if (!NativeWorkFiles.regular(file)) throw NativeWorkUnavailable()
            files.verify(file, file.length(), ref.sha, NativeWorkFiles.MAX_REGULAR_BYTES)
        }
        val derivative = files.derivative(ref.derivativeSha)
        if (!NativeWorkFiles.regular(derivative) || derivative.length() !in 1..NativeWorkFiles.MAX_DERIVATIVE_BYTES)
            throw NativeWorkUnavailable()
        keptDerivatives += ref.derivativeSha
        val parent = files.record(ref.proParentSha)
        if (!NativeWorkFiles.regular(parent) || parent.length() !in 1..NativeWorkFiles.MAX_PRO_BYTES)
            throw NativeWorkUnavailable()
        keptRecords += ref.proParentSha
    }

    fun finishRecipe(ref: ProLinkedFinishRef): EditDocument {
        val file = files.record(ref.sha)
        if (!NativeWorkFiles.regular(file)) throw NativeWorkUnavailable()
        return EditDocumentCodec.decode(read(NativeRevisionRef(NativeWorkMode.REGULAR, ref.sha, file.length())))
            ?: throw NativeWorkUnavailable()
    }

    private fun keepSource(uri: Uri): String {
        files.derivativeSha(uri)?.let { sha ->
            val derivative = files.derivative(sha)
            if (!NativeWorkFiles.regular(derivative) || derivative.length() !in 1..NativeWorkFiles.MAX_DERIVATIVE_BYTES)
                throw NativeWorkUnavailable()
            keptDerivatives += sha
        }
        return sourceUri(uri.toString()).toString()
    }

    private fun emptyPro(doc: ProDocument): JSONObject = JSONObject().put("empty", true)
        .put("canvas", canvasJson(ProCanvasSetup.from(doc))).apply {
            require(doc.linkedFinishes.isEmpty())
            doc.sourceCanvasSetup?.let { put("setup", canvasJson(it)) }
        }

    private fun decodeEmptyPro(json: JSONObject): ProDocument {
        val canvas = readCanvas(json.getJSONObject("canvas"))
        return ProDocument(aspect = canvas.aspect, background = canvas.background,
            sourceAspectRatio = canvas.sourceAspectRatio, canvasSize = canvas.canvasSize,
            sourceCanvasSetup = json.optJSONObject("setup")?.let(::readCanvas))
    }

    private fun canvasJson(value: ProCanvasSetup): JSONObject = JSONObject().put("aspect", value.aspect.name)
        .put("background", value.background.name).apply {
            value.sourceAspectRatio?.let { put("sourceAspect", it.toDouble()) }
            value.canvasSize?.let { put("size", sizeJson(it)) }
        }

    private fun readCanvas(json: JSONObject): ProCanvasSetup = ProCanvasSetup(
        ProAspect.valueOf(json.getString("aspect")), ProBackground.valueOf(json.getString("background")),
        if (json.has("sourceAspect")) json.getDouble("sourceAspect").toFloat().also {
            require(ProMath.validSourceAspect(it))
        } else null,
        json.optJSONObject("size")?.let(::readSize)
    )

    private fun <T> history(
        undo: List<T>, redo: List<T>, limit: Int, encode: (T) -> JSONObject
    ): JSONObject {
        if (undo.size + redo.size > limit) throw NativeWorkLimit()
        return JSONObject().put("undo", JSONArray(undo.map(encode))).put("redo", JSONArray(redo.map(encode)))
    }

    private fun <T> readHistory(
        json: JSONObject, limit: Int, decode: (JSONObject) -> T
    ): Pair<List<T>, List<T>> {
        val undo = json.getJSONArray("undo")
        val redo = json.getJSONArray("redo")
        if (undo.length() + redo.length() > limit) throw NativeWorkLimit()
        return List(undo.length()) { decode(undo.getJSONObject(it)) } to
            List(redo.length()) { decode(redo.getJSONObject(it)) }
    }

    companion object {
        private const val VERSION = 1
        private const val MAX_KEY = 200
        // EditHistory already retains forty completed Regular transactions.
        private const val REGULAR_HISTORY = 40
        const val NAMED_TAG = "gzNativeWork"
        const val NAMED_PREFIX = "{\"gzNativeWork\":1,\"manifest\":\""
        private val namedLength = NAMED_PREFIX.length + 64 + 2

        fun namedBytes(ref: NativeManifestRef): ByteArray =
            (NAMED_PREFIX + checkedSha(ref.sha) + "\"}").toByteArray(Charsets.US_ASCII)

        fun namedManifest(json: JSONObject): NativeManifestRef? =
            if (json.length() == 2 && json.optInt(NAMED_TAG) == VERSION)
                NativeManifestRef(checkedSha(json.getString("manifest")))
            else null

        fun peekManifest(file: File): NativeManifestRef? {
            if (!NativeWorkFiles.regular(file) || file.length() != namedLength.toLong()) return null
            val bytes = file.readBytes()
            if (bytes.size != namedLength) return null
            val text = String(bytes, Charsets.US_ASCII)
            return if (text.startsWith(NAMED_PREFIX)) namedManifest(JSONObject(text)) else null
        }

        fun encodeFinish(ref: ProLinkedFinishRef): JSONObject {
            checkBridgeSize(ref.input)
            checkBridgeSize(ref.output)
            return JSONObject().put("sha", checkedSha(ref.sha)).put("input", sizeJson(ref.input))
                .put("output", sizeJson(ref.output)).put("parent", checkedSha(ref.proParentSha))
                .put("derivative", checkedSha(ref.derivativeSha))
        }

        fun decodeFinish(json: JSONObject): ProLinkedFinishRef = ProLinkedFinishRef(
            checkedSha(json.getString("sha")), readSize(json.getJSONObject("input")),
            readSize(json.getJSONObject("output")), checkedSha(json.getString("parent")),
            checkedSha(json.getString("derivative"))
        ).also { checkBridgeSize(it.input); checkBridgeSize(it.output) }

        fun sizeJson(size: ProPixelSize) = JSONObject().put("width", size.width).put("height", size.height)
        fun readSize(json: JSONObject) = ProPixelSize(json.getInt("width"), json.getInt("height"))

        fun checkBridgeSize(size: ProPixelSize) {
            if (size.edge > NativeWorkFiles.MAX_DERIVATIVE_EDGE ||
                size.pixelCount > NativeWorkFiles.MAX_DERIVATIVE_PIXELS) throw NativeWorkLimit()
        }

        fun checkedSha(raw: String): String = raw.also { require(NativeWorkFiles.validSha(it)) }
        fun checkedKey(raw: String): String = raw.also { require(it.length in 1..MAX_KEY) }
        fun optionalKey(json: JSONObject, key: String): String? =
            if (json.has(key)) checkedKey(json.getString(key)) else null
        fun validRevision(value: Long): Long = value.also { require(it >= 0) }

        private fun sourceUri(value: String): Uri {
            require(value.length in 1..4096)
            return Uri.parse(value).also { require(it.scheme == "content" && !it.authority.isNullOrBlank()) }
        }

        private fun max(mode: NativeWorkMode): Long =
            if (mode == NativeWorkMode.REGULAR) NativeWorkFiles.MAX_REGULAR_BYTES else NativeWorkFiles.MAX_PRO_BYTES

        private fun jsonRef(ref: NativeRevisionRef) = JSONObject().put("sha", checkedSha(ref.sha)).put("bytes", ref.bytes)

        private fun readRef(json: JSONObject, mode: NativeWorkMode): NativeRevisionRef =
            NativeRevisionRef(mode, checkedSha(json.getString("sha")), json.getLong("bytes").also {
                require(it in 1..max(mode))
            })

        private fun namedRef(ref: NamedProjectRef, kind: ProjectKind): JSONObject {
            require(ref.kind == kind)
            return JSONObject().put("id", checkedKey(ref.id)).put("name", checkedKey(ref.name))
                .put("version", checkedSha(ref.version))
        }

        private fun namedRef(json: JSONObject, kind: ProjectKind): NamedProjectRef = NamedProjectRef(
            kind, checkedKey(json.getString("id")), checkedKey(json.getString("name")), checkedSha(json.getString("version"))
        )
    }
}

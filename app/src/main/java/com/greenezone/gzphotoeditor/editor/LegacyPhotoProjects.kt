package com.greenezone.gzphotoeditor.editor

import android.content.Context
import android.util.JsonReader
import android.util.JsonToken
import android.util.Xml
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.security.MessageDigest
import java.util.Base64
import java.util.UUID

/** One-time import of the app's own SharedPreferences XML. Source files remain untouched. */
internal object LegacyPhotoProjects {
    private const val LEGACY_XML = "gzphotoeditor.projects.v1.xml"
    private const val PENDING_FILE = "gz-photo-project-save.pending"
    // The old pending journal already bounded its UTF-8 JSON envelope at 12 MB.
    private const val MAX_PENDING_BYTES = 12_000_000L
    // Includes XML escaping overhead across twelve maximum-size legacy values.
    private const val MAX_XML_BYTES = 256L * 1024L * 1024L

    private data class Pending(val key: String, val previous: PhotoProjectFiles.Record?)
    private data class Capture(
        val staging: File,
        val count: Long,
        val version: String,
        val hasBoundedRecipe: Boolean
    )
    private data class LegacyIdentity(val ref: NamedProjectRef, val readable: Boolean)

    fun migrate(context: Context, onDecode: (() -> Unit)?): List<PhotoProjectFiles.Record> {
        val pending = recoverPending(context, onDecode)
        val source = authoritative(File(File(context.dataDir, "shared_prefs"), LEGACY_XML))
        val handler = LegacyHandler(context, pending, onDecode)
        if (source != null) {
            if (!source.isFile || source.length() !in 1L..MAX_XML_BYTES)
                throw IOException("The legacy Regular project library exceeds its import bound")
            try {
                BoundedXmlInput(source.inputStream().buffered()).use { input ->
                    Xml.parse(input, Xml.Encoding.UTF_8, handler)
                }
            } finally { handler.close() }
        }
        return handler.complete()
    }

    private fun authoritative(base: File): File? {
        val backup = File(base.path + ".bak")
        return when {
            backup.exists() -> backup
            base.exists() -> base
            else -> null
        }
    }

    /**
     * v1 restores the previous value whenever the published marker exists,
     * even if the XML already contains the attempted replacement. Model that
     * rollback in the import without writing to SharedPreferences or its cache.
     */
    private fun recoverPending(context: Context, onDecode: (() -> Unit)?): Pending? {
        val pendingFile = authoritative(File(context.filesDir, PENDING_FILE)) ?: return null
        if (!pendingFile.isFile || pendingFile.length() !in 1L..MAX_PENDING_BYTES)
            throw IOException("Invalid Regular project rollback record")
        var key: String? = null
        var version = 0
        var previous: String? = null
        var hasPrevious = false
        JsonReader(pendingFile.reader(Charsets.UTF_8)).use { reader ->
            reader.beginObject()
            while (reader.hasNext()) when (reader.nextName()) {
                "version" -> {
                    if (version != 0) throw IOException("Duplicate rollback version")
                    version = reader.nextInt()
                }
                "key" -> {
                    if (key != null || reader.peek() != JsonToken.STRING) throw IOException("Invalid rollback key")
                    key = reader.nextString()
                }
                "previous" -> {
                    if (hasPrevious) throw IOException("Duplicate rollback value")
                    hasPrevious = true
                    previous = when (reader.peek()) {
                        JsonToken.NULL -> { reader.nextNull(); null }
                        JsonToken.STRING -> reader.nextString()
                        else -> throw IOException("Invalid rollback value")
                    }
                }
                else -> throw IOException("Unexpected Regular project rollback field")
            }
            reader.endObject()
            if (reader.peek() != JsonToken.END_DOCUMENT)
                throw IOException("Unexpected trailing Regular project rollback data")
        }
        val storageKey = key?.takeIf { version == 1 && hasPrevious && nameFromKey(it) != null }
            ?: throw IOException("Invalid Regular project rollback record")
        val restored = previous?.let { value ->
            EntryWriter(context).use { writer ->
                writer.text(value)
                record(context, storageKey, writer.finish(), stringValue = true, onDecode = onDecode)
            }
        }
        return Pending(storageKey, restored)
    }

    private fun nameFromKey(key: String): String? = try {
        val name = String(Base64.getUrlDecoder().decode(key), Charsets.UTF_8)
        val canonical = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(name.toByteArray(Charsets.UTF_8))
        name.takeIf { it.isNotBlank() && canonical == key }
    } catch (_: IllegalArgumentException) { null }

    private fun identity(context: Context, key: String, version: String): LegacyIdentity {
        val name = nameFromKey(key)
        if (name != null) {
            try {
                return LegacyIdentity(NamedProjectIdentity.reference(context, ProjectKind.PHOTO, name, version), true)
            } catch (_: Exception) {
                // An unavailable original identity must not be silently overwritten.
            }
        }
        val orphan = UUID.nameUUIDFromBytes(("Regular unavailable legacy key:" + key).toByteArray(Charsets.UTF_8))
        val label = name ?: "[Unavailable " + orphan.toString().take(12) + "]"
        return LegacyIdentity(NamedProjectRef(ProjectKind.PHOTO, orphan.toString(), label, version), false)
    }

    private fun record(
        context: Context,
        key: String,
        captured: Capture,
        stringValue: Boolean,
        onDecode: (() -> Unit)?
    ): PhotoProjectFiles.Record {
        val identity = identity(context, key, captured.version)
        val generation = if (stringValue && captured.count in 1L..PhotoProjectFiles.MAX_RECORD_BYTES)
            PhotoProjectFiles.finishRecord(context, captured.staging, captured.count, captured.version)
        else null
        val row = PhotoProjectFiles.Record(identity.ref, captured.count, false, generation)
        val readable = if (generation != null && identity.readable && captured.hasBoundedRecipe) {
            try { PhotoProjectFiles.readRecord(context, row, onDecode) != null }
            catch (_: IllegalArgumentException) { false }
        } else false
        return row.copy(readable = readable)
    }

    private class LegacyHandler(
        private val context: Context,
        private val pending: Pending?,
        private val onDecode: (() -> Unit)?
    ) : DefaultHandler(), Closeable {
        private val rows = mutableListOf<PhotoProjectFiles.Record>()
        private val keys = mutableSetOf<String>()
        private var depth = 0
        private var key: String? = null
        private var writer: EntryWriter? = null
        private var stringValue = false
        private var completeRoot = false

        override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
            val element = localName.ifEmpty { qName }
            when (++depth) {
                1 -> if (element != "map") throw SAXException("Invalid Regular project preference root")
                2 -> {
                    val storageKey = attributes.getValue("", "name") ?: attributes.getValue("name")
                        ?: throw SAXException("Regular project preference has no storage key")
                    if (!keys.add(storageKey) || keys.size > PhotoProjectFiles.LIMIT + 1)
                        throw SAXException("Invalid Regular project preference occupancy")
                    key = storageKey
                    if (pending?.key != storageKey) {
                        stringValue = element == "string"
                        writer = EntryWriter(context)
                        if (!stringValue) {
                            // Non-string values used a legacy preference slot but
                            // never represented a decodable Regular recipe.
                            val value = attributes.getValue("", "value") ?: attributes.getValue("value")
                            writer?.text(element + "\n" + value.orEmpty())
                        }
                    }
                }
                3 -> stringValue = false
                else -> throw SAXException("Invalid nested Regular project preference")
            }
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            writer?.text(ch, start, length)
        }

        override fun endElement(uri: String, localName: String, qName: String) {
            if (depth == 2) {
                writer?.use { entry ->
                    rows += record(context, requireNotNull(key), entry.finish(), stringValue, onDecode)
                }
                writer = null
                key = null
            } else if (depth == 1) completeRoot = true
            depth--
        }

        override fun endDocument() {
            if (!completeRoot || depth != 0) throw SAXException("The legacy Regular project library is incomplete")
        }

        fun complete(): List<PhotoProjectFiles.Record> {
            pending?.previous?.let { rows += it }
            if (rows.size > PhotoProjectFiles.LIMIT)
                throw IOException("The legacy Regular project library exceeds its slot limit")
            // A fabricated label for an invalid key must not mask an actual
            // named save with the same label.
            val used = mutableSetOf<String>()
            val duplicates = rows.groupingBy { it.project.name }.eachCount()
            val reserved = duplicates.keys
            return rows.map { row ->
                val label = row.project.name
                if (duplicates.getValue(label) == 1 || (row.readable && used.add(label))) row
                else {
                    var display = "[" + row.project.id + "]"
                    while (display in reserved || display in used) display += "-"
                    used += display
                    row.copy(project = row.project.copy(name = display))
                }
            }
        }

        override fun close() { writer?.close() }
    }

    /** Stream decoded XML text to a bounded copy and digest every byte, including oversized values. */
    private class EntryWriter(context: Context) : Closeable {
        private val staging = PhotoProjectFiles.createStage(context)
        private val output = FileOutputStream(staging)
        private val digest = MessageDigest.getInstance("SHA-256")
        private var count = 0L
        private var uriCharacters = 0L
        private var recipeCharacters = 0L
        private var sawSeparator = false
        private val writer = OutputStreamWriter(object : OutputStream() {
            override fun write(byte: Int) {
                digest.update(byte.toByte())
                if (count++ < PhotoProjectFiles.MAX_RECORD_BYTES) output.write(byte)
            }

            override fun write(bytes: ByteArray, offset: Int, length: Int) {
                digest.update(bytes, offset, length)
                val retained = minOf(length.toLong(), (PhotoProjectFiles.MAX_RECORD_BYTES - count).coerceAtLeast(0))
                if (retained > 0) output.write(bytes, offset, retained.toInt())
                count += length
            }
        }, Charsets.UTF_8)
        private var finished = false

        private fun countCharacters(length: Int, separator: Int) {
            if (sawSeparator) recipeCharacters += length
            else if (separator < 0) uriCharacters += length
            else {
                uriCharacters += separator
                sawSeparator = true
                recipeCharacters += length - separator - 1
            }
        }

        fun text(value: String) {
            countCharacters(value.length, if (sawSeparator) -1 else value.indexOf('\n'))
            writer.write(value)
        }

        fun text(value: CharArray, offset: Int, length: Int) {
            var separator = -1
            if (!sawSeparator) {
                for (index in offset until offset + length) {
                    if (value[index] != '\n') continue
                    separator = index - offset
                    break
                }
            }
            countCharacters(length, separator)
            writer.write(value, offset, length)
        }

        fun finish(): Capture {
            writer.close()
            if (count in 1L..PhotoProjectFiles.MAX_RECORD_BYTES) output.fd.sync()
            output.close()
            finished = true
            val version = digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
            return Capture(staging, count, version,
                sawSeparator && uriCharacters > 0 && recipeCharacters <= EditDocumentCodec.MAX_ENCODED_CHARS)
        }

        override fun close() {
            try {
                if (!finished) output.close()
            } finally { staging.delete() }
        }
    }

    /**
     * The platform SAX reader supplies text in chunks. Bound incoming chunks,
     * the XML envelope, attributes and references before SAX sees them. The
     * platform preference writer emits simple tags and escaped text, not DTDs,
     * comments or CDATA sections.
     */
    private class BoundedXmlInput(source: InputStream) : FilterInputStream(source) {
        private var count = 0L
        private var markup = 0
        private var quote = 0
        private var entity = 0

        override fun read(): Int = super.read().also { if (it >= 0) checkByte(it) }

        override fun read(bytes: ByteArray, offset: Int, length: Int): Int {
            val size = super.read(bytes, offset, minOf(length, 8192))
            for (index in offset until offset + size) checkByte(bytes[index].toInt() and 255)
            return size
        }

        private fun checkByte(byte: Int) {
            if (++count > MAX_XML_BYTES) throw IOException("The legacy Regular project library exceeds its import bound")
            if (markup > 0) {
                if (++markup > 4096 || (markup == 2 && byte == '!'.code))
                    throw IOException("Unsupported Regular project XML markup")
                if (quote != 0) {
                    if (byte == quote) quote = 0
                } else when (byte) {
                    '"'.code, '\''.code -> quote = byte
                    '>'.code -> markup = 0
                }
            } else if (byte == '<'.code) markup = 1

            if (entity > 0) {
                if (++entity > 32) throw IOException("Unsupported Regular project XML reference")
                if (byte == ';'.code) entity = 0
            } else if ((markup == 0 || quote != 0) && byte == '&'.code) {
                entity = 1
            }
        }
    }
}

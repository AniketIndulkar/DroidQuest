package dev.novanest.droidquest

import dev.novanest.droidquest.content.ContentLoadState
import dev.novanest.droidquest.content.ContentSource
import dev.novanest.droidquest.content.DroidQuestContentRepository
import dev.novanest.droidquest.content.FileContentSource
import dev.novanest.droidquest.content.LoadedContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileNotFoundException

/** Shared helpers to drive the content repository from the on-disk data repository in JVM tests. */
object TestContent {

    /** Locate the sibling `data` directory that holds the `content/` tree. */
    fun dataDir(): File {
        val candidates = listOf(
            File("../../data"), File("../data"), File("data"),
            File(System.getProperty("user.dir"), "../../data"),
        )
        return candidates.map { it.canonicalFile }.firstOrNull {
            File(it, "content/generated/content-index.json").exists()
        } ?: error("Could not locate data/content directory from ${File(".").canonicalPath}")
    }

    fun source(): ContentSource = FileContentSource(dataDir())

    fun repository(source: ContentSource = source()): DroidQuestContentRepository =
        DroidQuestContentRepository(source, Dispatchers.Unconfined)

    fun load(source: ContentSource = source()): ContentLoadState = runBlocking { repository(source).load() }

    fun loaded(): LoadedContent = (load() as ContentLoadState.Success).content
}

/** Wraps a source and rewrites the bytes for one path (to force a hash mismatch). */
class CorruptingSource(private val base: ContentSource, private val path: String) : ContentSource {
    override fun readBytes(path: String): ByteArray {
        val bytes = base.readBytes(path)
        return if (path == this.path) bytes + " ".toByteArray() else bytes
    }
}

/** Wraps a source and hides one path (to force a missing-content error). */
class MissingSource(private val base: ContentSource, private val path: String) : ContentSource {
    override fun readBytes(path: String): ByteArray {
        if (path == this.path) throw FileNotFoundException(path)
        return base.readBytes(path)
    }
}

/** Wraps a source and rewrites curriculum.json to require an unsupported content API. */
class UnsupportedApiSource(private val base: ContentSource) : ContentSource {
    override fun readBytes(path: String): ByteArray {
        val bytes = base.readBytes(path)
        if (path != DroidQuestContentRepository.CURRICULUM_PATH) return bytes
        val text = String(bytes).replace(
            Regex("\"minimumAppContentApi\"\\s*:\\s*\\d+"),
            "\"minimumAppContentApi\": 99",
        )
        return text.toByteArray()
    }
}

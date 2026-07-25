package dev.novanest.droidquest.content

import android.content.Context
import java.io.File

/**
 * Abstraction over where bundled content bytes come from, so the repository can be
 * driven by Android assets in the app and by plain files in JVM unit tests.
 *
 * [path] is always relative to the content root and begins with "content/", exactly as
 * stored in content-index.json (e.g. "content/lessons/level-01-week-01-01-first-program.json").
 */
interface ContentSource {
    /** @throws java.io.FileNotFoundException when the path is not present. */
    fun readBytes(path: String): ByteArray
}

/** Reads from Android assets under a stable root directory (default "droidquest"). */
class AssetContentSource(
    private val context: Context,
    private val root: String = "droidquest",
) : ContentSource {
    override fun readBytes(path: String): ByteArray =
        context.assets.open("$root/$path").use { it.readBytes() }
}

/** Reads from a filesystem directory that contains the "content/" tree (used by tests). */
class FileContentSource(private val rootDir: File) : ContentSource {
    override fun readBytes(path: String): ByteArray {
        val f = File(rootDir, path)
        if (!f.exists()) throw java.io.FileNotFoundException(f.absolutePath)
        return f.readBytes()
    }
}

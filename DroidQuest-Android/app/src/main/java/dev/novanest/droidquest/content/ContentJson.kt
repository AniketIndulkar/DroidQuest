package dev.novanest.droidquest.content

import kotlinx.serialization.json.Json
import java.security.MessageDigest

/**
 * Shared JSON configuration for all content deserialization.
 *
 * - [ignoreUnknownKeys] keeps an older client forward-compatible with additive fields
 *   in a newer content release.
 * - The class discriminator "type" matches the Learn block and (implicitly) sealed shapes.
 */
val ContentJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = false
    classDiscriminator = "type"
    explicitNulls = false
}

/** Lowercase hex SHA-256 of the given bytes, matching the generator's hash format. */
fun sha256Hex(bytes: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    val sb = StringBuilder(digest.size * 2)
    for (b in digest) {
        val v = b.toInt() and 0xFF
        sb.append(HEX[v ushr 4])
        sb.append(HEX[v and 0x0F])
    }
    return sb.toString()
}

private const val HEX = "0123456789abcdef"

package dev.novanest.droidquest.content

/** Explicit, recoverable failure categories surfaced to the UI error screen. */
enum class ContentErrorKind { MISSING_CONTENT, MALFORMED_JSON, UNSUPPORTED_VERSION, HASH_MISMATCH, UNKNOWN }

class ContentException(
    val kind: ContentErrorKind,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

/** Loading / success / error state for the content snapshot. */
sealed interface ContentLoadState {
    data object Loading : ContentLoadState
    data class Success(val content: LoadedContent) : ContentLoadState
    data class Error(val kind: ContentErrorKind, val message: String) : ContentLoadState
}

package dev.novanest.droidquest.ui.theme

import androidx.compose.ui.graphics.Color

/** Parse a "#RRGGBB" content colour, falling back to the Android green if malformed. */
fun hexColor(hex: String?): Color {
    if (hex == null || !hex.startsWith("#") || hex.length != 7) return DQ.Green
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: IllegalArgumentException) {
        DQ.Green
    }
}

/**
 * Map an authored category/theme icon name to a known local glyph. Content stays the source
 * of truth for the name; the app owns the visual with a safe fallback so an unknown future
 * icon never breaks rendering.
 */
fun iconGlyph(icon: String?): String = when (icon) {
    "terminal" -> "⌘"        // ⌘-like command
    "android" -> "◈"
    "layers" -> "≣"
    "sync" -> "↻"
    "account-tree" -> "⑂"
    "devices" -> "▦"
    "verified" -> "✓"
    "security" -> "⛨"
    "build" -> "⚙"
    "speed" -> "◉"
    "memory" -> "▤"
    "route" -> "⌥"
    else -> "◆"              // ◆ fallback diamond
}

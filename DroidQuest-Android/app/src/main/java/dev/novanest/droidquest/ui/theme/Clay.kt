package dev.novanest.droidquest.ui.theme

import androidx.compose.foundation.background
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The design board's "clay" treatment: a diagonal light-to-dark gradient, a soft top highlight,
 * a darker lower edge and a coloured glow. Stops mirror the board's CLAY table.
 */
@Immutable
class Clay(val light: Color, val dark: Color, val glyph: Color, val glow: Color)

object ClayPalette {
    val Green = Clay(Color(0xFF7EEBAF), Color(0xFF1F9E5C), Color(0xFFEAFFF3), DQ.Green)
    val Amber = Clay(Color(0xFFFFD37A), Color(0xFFC98A1C), Color(0xFFFFF6E4), DQ.Amber)
    val Blue = Clay(Color(0xFF8FB6FF), Color(0xFF2A5FCB), Color(0xFFEAF1FF), DQ.Blue)
    val Red = Clay(Color(0xFFF08A82), Color(0xFFB3372E), Color(0xFFFFEDEB), DQ.Red)

    /** Dark orb stops used behind the brand diamond in the header. */
    val InkLight = Color(0xFF243029)
    val InkDark = Color(0xFF0D110F)
}

/** Clay stops for an accent colour; unknown content colours get a derived light/dark pair. */
fun clayFor(color: Color): Clay = when (color.copy(alpha = 1f)) {
    DQ.Green -> ClayPalette.Green
    DQ.Amber -> ClayPalette.Amber
    DQ.Blue -> ClayPalette.Blue
    DQ.Red -> ClayPalette.Red
    else -> Clay(
        light = lerp(color, Color.White, 0.35f),
        dark = lerp(color, Color.Black, 0.35f),
        glyph = lerp(color, Color.White, 0.9f),
        glow = color,
    )
}

/** Fills [shape] with the clay look and a coloured drop glow beneath it. */
fun Modifier.clay(shape: Shape, clay: Clay, glow: Dp = 8.dp): Modifier = this
    .shadow(glow, shape, clip = false, ambientColor = clay.glow, spotColor = clay.glow)
    .clip(shape)
    .background(Brush.linearGradient(listOf(clay.light, clay.dark)))
    .drawBehind {
        drawRect(
            Brush.verticalGradient(
                0f to Color.White.copy(alpha = 0.30f),
                0.22f to Color.Transparent,
                0.62f to Color.Transparent,
                1f to Color.Black.copy(alpha = 0.28f),
            ),
        )
    }

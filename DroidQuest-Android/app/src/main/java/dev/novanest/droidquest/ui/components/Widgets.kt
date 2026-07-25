package dev.novanest.droidquest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import dev.novanest.droidquest.ui.theme.DQ

/** Rounded progress track with a colored fill (design uses border-radius:100px). */
@Composable
fun ProgressBar(
    pct: Int,
    fill: Color,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    track: Color = DQ.text(0.1f),
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(100))
            .background(track)
    ) {
        Box(
            Modifier
                .fillMaxWidth(pct.coerceIn(0, 100) / 100f)
                .height(height)
                .clip(RoundedCornerShape(100))
                .background(fill)
        )
    }
}

/** The little rotated-square "diamond" glyph used across the app. */
@Composable
fun Diamond(color: Color, size: Dp, corner: Dp = 2.dp) {
    Box(
        Modifier
            .size(size)
            .rotate(45f)
            .clip(RoundedCornerShape(corner))
            .background(color)
    )
}

/** Three stars in a row, each with its own on/off color. */
@Composable
fun StarRow(colors: List<Color>, fontSize: Int, gap: Dp = 2.dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
        colors.forEach {
            Text("★", color = it, fontSize = fontSize.sp)
        }
    }
}

/** Star on/off colors for a given star count (design's starColors). */
fun starColors(stars: Int): List<Color> = listOf(
    if (stars >= 1) DQ.Amber else DQ.StarOff,
    if (stars >= 2) DQ.Amber else DQ.StarOff,
    if (stars >= 3) DQ.Amber else DQ.StarOff,
)

/** Uppercase muted section header (letter-spacing:0.5px, weight 700). */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, color: Color = DQ.text(0.5f)) {
    Text(
        text.uppercase(),
        modifier = modifier,
        color = color,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
    )
}

/** Standard card container: #1E2422, hairline border, rounded. */
fun Modifier.card(corner: Dp = 16.dp, fill: Color = DQ.Card): Modifier =
    this
        .clip(RoundedCornerShape(corner))
        .background(fill)
        .border(1.dp, DQ.Border, RoundedCornerShape(corner))

/** Toggle switch matching the settings design (44x26 track, 22 knob, 18px travel). */
@Composable
fun ToggleSwitch(on: Boolean, onToggle: () -> Unit) {
    Box(
        Modifier
            .size(44.dp, 26.dp)
            .clip(RoundedCornerShape(100))
            .background(if (on) DQ.Green else DQ.text(0.15f))
            .clickable(onClick = onToggle)
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset(x = if (on) 18.dp else 0.dp)
                .size(22.dp)
                .clip(RoundedCornerShape(100))
                .background(DQ.TextPrimary)
        )
    }
}

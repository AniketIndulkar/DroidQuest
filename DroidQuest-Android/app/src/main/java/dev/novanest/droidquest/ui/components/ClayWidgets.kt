package dev.novanest.droidquest.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import dev.novanest.droidquest.ui.theme.Clay
import dev.novanest.droidquest.ui.theme.clay
import dev.novanest.droidquest.ui.theme.clayFor

/** Embossed glyph used on clay surfaces (the board stacks hard text-shadows; one offset reads the same). */
@Composable
fun ClayGlyph(text: String, clay: Clay, fontSize: TextUnit, modifier: Modifier = Modifier, fontWeight: FontWeight = FontWeight.Black) {
    Text(
        text,
        modifier = modifier,
        color = clay.glyph,
        fontSize = fontSize,
        fontWeight = fontWeight,
        style = TextStyle(shadow = Shadow(clay.dark, Offset(0f, 3f), 2f)),
    )
}

/** Rounded-square clay tile tinted by [color]. */
@Composable
fun ClayTile(
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(size * 0.28f),
    content: @Composable BoxScope.(Clay) -> Unit,
) {
    val clay = clayFor(color)
    Box(modifier.size(size).clay(shape, clay), contentAlignment = Alignment.Center) { content(clay) }
}

/** Round clay orb (avatar, AI helper button). */
@Composable
fun ClayOrb(
    clay: Clay,
    size: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.size(size).clay(CircleShape, clay), contentAlignment = Alignment.Center, content = content)
}

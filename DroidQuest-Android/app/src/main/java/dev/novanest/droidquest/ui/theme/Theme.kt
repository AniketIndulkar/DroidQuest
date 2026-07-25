package dev.novanest.droidquest.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DroidQuestColorScheme = darkColorScheme(
    primary = DQ.Green,
    onPrimary = DQ.Ink,
    secondary = DQ.Amber,
    tertiary = DQ.Blue,
    background = DQ.ScreenBg,
    onBackground = DQ.TextPrimary,
    surface = DQ.Card,
    onSurface = DQ.TextPrimary,
)

@Composable
fun DroidQuestTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DroidQuestColorScheme,
        typography = Typography,
        content = content
    )
}

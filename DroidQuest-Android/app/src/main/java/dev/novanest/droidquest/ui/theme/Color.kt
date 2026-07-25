package dev.novanest.droidquest.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * DroidQuest palette — mapped 1:1 from the design source (DroidQuest.dc.html).
 */
object DQ {
    val ScreenBg = Color(0xFF14181A)   // #14181A phone screen background
    val Card = Color(0xFF1E2422)       // #1E2422 card / surface
    val CardAlt = Color(0xFF262E2B)    // #262E2B AI helper / raised surface
    val BadgeDim = Color(0xFF2A322F)   // #2A322F locked badge fill

    val Green = Color(0xFF3DDC84)      // #3DDC84 primary / Android green
    val Amber = Color(0xFFF2B33D)      // #F2B33D gold / daily quest
    val Blue = Color(0xFF4C8DFF)       // #4C8DFF accent blue
    val BlueLight = Color(0xFF7FADFF)  // #7FADFF light blue
    val Orange = Color(0xFFE2663C)     // #E2663C streak flame
    val Red = Color(0xFFE2574C)        // #E2574C boss red

    val Ink = Color(0xFF0B0D0C)        // #0B0D0C on-accent text (near black)
    val TextPrimary = Color(0xFFF4F2EE) // #F4F2EE primary text

    // rgba(244,242,238, a) — light text at varying opacity
    fun text(alpha: Float) = TextPrimary.copy(alpha = alpha)
    // rgba(255,255,255, a) — hairline borders / dividers
    fun white(alpha: Float) = Color.White.copy(alpha = alpha)

    val Border = white(0.06f)          // rgba(255,255,255,0.06)
    val StarOff = text(0.2f)           // rgba(244,242,238,0.2)
}

// Legacy names kept so the generated theme scaffold still resolves.
val Purple80 = DQ.Green
val PurpleGrey80 = DQ.Amber
val Pink80 = DQ.Blue
val Purple40 = DQ.Green
val PurpleGrey40 = DQ.Amber
val Pink40 = DQ.Blue

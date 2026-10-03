package dev.novanest.droidquest.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.content.model.CategoryStatus
import dev.novanest.droidquest.ui.components.ClayGlyph
import dev.novanest.droidquest.ui.components.ClayOrb
import dev.novanest.droidquest.ui.components.ClayTile
import dev.novanest.droidquest.ui.components.ProgressBar
import dev.novanest.droidquest.ui.state.DroidQuestUiState
import dev.novanest.droidquest.ui.state.DroidQuestViewModel
import dev.novanest.droidquest.ui.state.Screen
import dev.novanest.droidquest.ui.state.UiDerive
import dev.novanest.droidquest.ui.theme.ClayPalette
import dev.novanest.droidquest.ui.theme.DQ
import dev.novanest.droidquest.ui.theme.clay
import dev.novanest.droidquest.ui.theme.hexColor
import dev.novanest.droidquest.ui.theme.iconGlyph

@Composable
fun HomeScreen(vm: DroidQuestViewModel, content: LoadedContent, ui: DroidQuestUiState) {
    val progress = ui.progress
    val level = UiDerive.currentLevelNumber(content, progress)
    val stars = UiDerive.totalStars(content, progress)
    val next = UiDerive.nextNode(content, progress)
    val currentCat = next?.let { content.category(it.categoryId) } ?: content.categoriesInOrder().first()
    val catPct = UiDerive.categoryProgress(content, progress, currentCat.id).pct
    val reviewsDue = progress.reviewsDue(System.currentTimeMillis())

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // Header
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ClayOrb(ClayPalette.Green, 34.dp) {
                    Box(
                        Modifier.size(11.dp).rotate(45f).clip(RoundedCornerShape(2.dp))
                            .background(Brush.linearGradient(listOf(ClayPalette.InkLight, ClayPalette.InkDark))),
                    )
                }
                Text("DroidQuest", color = DQ.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.2.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("$stars★", color = DQ.Amber, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clip(RoundedCornerShape(100)).background(DQ.Amber.copy(alpha = 0.15f)).padding(horizontal = 10.dp, vertical = 6.dp))
                Text("Lv $level", color = DQ.BlueLight, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clip(RoundedCornerShape(100)).background(DQ.Blue.copy(alpha = 0.16f)).padding(horizontal = 10.dp, vertical = 6.dp))
            }
        }

        if (reviewsDue > 0) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(DQ.Blue.copy(alpha = 0.10f)).border(1.dp, DQ.Blue.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .clickable { vm.startDailyReview() }.padding(18.dp),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("REVIEW DUE", color = DQ.BlueLight, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Text("Strengthen $reviewsDue ${if (reviewsDue == 1) "memory" else "memories"}", color = DQ.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp))
                        Text("A short, mixed session—no penalties.", color = DQ.text(0.5f), fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))
                    }
                    Text("Review ›", color = DQ.BlueLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // XP / progress card
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(DQ.Card).border(1.dp, DQ.Green.copy(alpha = 0.15f), RoundedCornerShape(20.dp)).padding(18.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            XpHex(catPct, level)
            Column(Modifier.weight(1f)) {
                Text("${progress.totalXp} XP", color = DQ.text(0.6f), fontSize = 13.sp, modifier = Modifier.padding(bottom = 2.dp))
                Text(currentCat.title, color = DQ.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                Text("Local-first progress · streak not tracked yet", color = DQ.text(0.45f), fontSize = 12.sp)
            }
        }

        // Next up
        if (next != null) {
            val cat = content.category(next.categoryId)
            val nextProgress = UiDerive.categoryProgress(content, progress, next.categoryId)
            val cardShape = RoundedCornerShape(20.dp)
            Box(
                Modifier.fillMaxWidth().clip(cardShape)
                    .background(Brush.linearGradient(listOf(DQ.Amber.copy(alpha = 0.16f), DQ.Amber.copy(alpha = 0.05f))))
                    .border(1.dp, DQ.Amber.copy(alpha = 0.3f), cardShape)
                    .clickable { vm.openNode(next.id) },
            ) {
                Canvas(Modifier.align(Alignment.TopEnd).size(90.dp)) {
                    drawPath(
                        Path().apply { moveTo(size.width, 0f); lineTo(0f, 0f); lineTo(size.width, size.height); close() },
                        DQ.Amber.copy(alpha = 0.08f),
                    )
                }
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            QuestGem()
                            Text("NEXT UP", color = DQ.Amber, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        }
                        Text("+${next.rewards.xp} XP", color = DQ.text(0.5f), fontSize = 12.sp)
                    }
                    Text(next.title, color = DQ.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))
                    ProgressBar(nextProgress.pct, DQ.Amber, modifier = Modifier.padding(bottom = 10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${cat?.title ?: ""} · ${nextProgress.completed} of ${nextProgress.total}", color = DQ.text(0.5f), fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text("Continue ›", color = DQ.Amber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(DQ.Card).border(1.dp, DQ.Border, RoundedCornerShape(20.dp)).padding(18.dp)) {
                Text("You're all caught up on published content. New levels unlock as they publish.", color = DQ.text(0.7f), fontSize = 14.sp, lineHeight = 20.sp)
            }
        }

        // Your Journey
        Column {
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("YOUR JOURNEY", color = DQ.text(0.5f), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                Text("Full map ›", color = DQ.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { vm.goTo(Screen.MAP) })
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                content.categoriesInOrder().take(4).forEach { cat ->
                    val cp = UiDerive.categoryProgress(content, progress, cat.id)
                    val locked = cat.status == CategoryStatus.PLANNED
                    val color = hexColor(cat.theme.color)
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(DQ.Card).border(1.dp, DQ.Border, RoundedCornerShape(14.dp))
                            .clickable { vm.openCategory(cat.id) }.padding(horizontal = 14.dp, vertical = 12.dp).alpha(if (locked) 0.55f else 1f),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ClayTile(color, 36.dp, shape = RoundedCornerShape(10.dp)) { clay ->
                            ClayGlyph(iconGlyph(cat.theme.icon), clay, 16.sp)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(cat.title, color = DQ.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(if (locked) "Planned preview" else "${cp.completed}/${cp.total} done", color = DQ.text(0.45f), fontSize = 11.sp)
                        }
                        Text("${cp.starsEarned}★", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/** Pointy-top hexagon inscribed in a [w] x [h] box, offset by [dx]/[dy]. */
private fun hexPath(w: Float, h: Float, dx: Float = 0f, dy: Float = 0f): Path = Path().apply {
    moveTo(dx + w * 0.5f, dy)
    lineTo(dx + w, dy + h * 0.25f)
    lineTo(dx + w, dy + h * 0.75f)
    lineTo(dx + w * 0.5f, dy + h)
    lineTo(dx, dy + h * 0.75f)
    lineTo(dx, dy + h * 0.25f)
    close()
}

@Composable
private fun XpHex(pct: Int, level: Int) {
    Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(72.dp)) {
            val outer = hexPath(size.width, size.height)
            drawPath(outer, DQ.text(0.08f))
            clipPath(outer) {
                // Conic fill from 12 o'clock, clipped to the hexagon.
                drawArc(
                    DQ.Green, -90f, 360f * pct.coerceIn(0, 100) / 100f, useCenter = true,
                    topLeft = Offset(-size.width / 2, -size.height / 2), size = Size(size.width * 2, size.height * 2),
                )
            }
            val inset = 4.dp.toPx()
            drawPath(hexPath(size.width - inset * 2, size.height - inset * 2, inset, inset), DQ.Card)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$level", color = DQ.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Text("LEVEL", color = DQ.text(0.5f), fontSize = 7.5.sp, letterSpacing = 0.5.sp)
        }
    }
}

/** Gold clay gem with the board's slow attention pulse. */
@Composable
private fun QuestGem() {
    val pulse by rememberInfiniteTransition(label = "questPulse").animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "questPulseProgress",
    )
    val shape = RoundedCornerShape(6.dp)
    val clay = ClayPalette.Amber
    Box(
        Modifier.size(26.dp).drawBehind {
            val spread = pulse * 9.dp.toPx()
            rotate(45f) {
                drawRoundRect(
                    DQ.Amber.copy(alpha = 0.5f * (1f - pulse)),
                    topLeft = Offset(-spread / 2, -spread / 2),
                    size = Size(size.width + spread, size.height + spread),
                    cornerRadius = CornerRadius(6.dp.toPx() + spread / 2),
                    style = Stroke(spread),
                )
            }
        },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(26.dp).rotate(45f).clay(shape, clay, 6.dp), contentAlignment = Alignment.Center) {
            ClayGlyph("✦", clay, 13.sp, Modifier.rotate(-45f))
        }
    }
}

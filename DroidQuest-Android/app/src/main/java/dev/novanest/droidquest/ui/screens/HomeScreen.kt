package dev.novanest.droidquest.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.content.model.CategoryStatus
import dev.novanest.droidquest.ui.components.Diamond
import dev.novanest.droidquest.ui.components.ProgressBar
import dev.novanest.droidquest.ui.state.DroidQuestUiState
import dev.novanest.droidquest.ui.state.DroidQuestViewModel
import dev.novanest.droidquest.ui.state.Screen
import dev.novanest.droidquest.ui.state.UiDerive
import dev.novanest.droidquest.ui.theme.DQ
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

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // Header
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(34.dp).clip(CircleShape).background(DQ.Card).border(2.dp, DQ.Green, CircleShape), contentAlignment = Alignment.Center) {
                    Diamond(DQ.Green, 10.dp)
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

        // XP / progress card
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(DQ.Card).border(1.dp, DQ.Border, RoundedCornerShape(20.dp)).padding(18.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            XpRing(catPct, level)
            Column(Modifier.weight(1f)) {
                Text("${progress.totalXp} XP", color = DQ.text(0.6f), fontSize = 13.sp, modifier = Modifier.padding(bottom = 2.dp))
                Text(currentCat.title, color = DQ.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                Text("Local-first progress · streak not tracked yet", color = DQ.text(0.45f), fontSize = 12.sp)
            }
        }

        // Next up
        if (next != null) {
            val cat = content.category(next.categoryId)
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(DQ.Amber.copy(alpha = 0.10f)).border(1.dp, DQ.Amber.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .clickable { vm.openNode(next.id) }.padding(18.dp),
            ) {
                Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("NEXT UP", color = DQ.Amber, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Text("+${next.rewards.xp} XP", color = DQ.text(0.5f), fontSize = 12.sp)
                }
                Text(next.title, color = DQ.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(cat?.title ?: "", color = DQ.text(0.5f), fontSize = 12.sp)
                    Text("Continue ›", color = DQ.Amber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                        Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                            Text(iconGlyph(cat.theme.icon), color = color, fontSize = 16.sp)
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

@Composable
private fun XpRing(pct: Int, level: Int) {
    Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(72.dp)) {
            val stroke = 8.dp.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)
            drawArc(Color(0xFF2A322F), -90f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
            drawArc(DQ.Green, -90f, 360f * pct / 100f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Box(Modifier.size(56.dp).clip(CircleShape).background(DQ.Card), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$level", color = DQ.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text("LEVEL", color = DQ.text(0.5f), fontSize = 8.sp, letterSpacing = 0.5.sp)
            }
        }
    }
}

package dev.novanest.droidquest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.content.model.CategoryDto
import dev.novanest.droidquest.content.model.CategoryStatus
import dev.novanest.droidquest.content.model.RoadmapNodeType
import dev.novanest.droidquest.ui.components.Diamond
import dev.novanest.droidquest.ui.state.DroidQuestUiState
import dev.novanest.droidquest.ui.state.DroidQuestViewModel
import dev.novanest.droidquest.ui.state.UiDerive
import dev.novanest.droidquest.ui.theme.DQ
import dev.novanest.droidquest.ui.theme.hexColor

@Composable
fun QuestMapScreen(vm: DroidQuestViewModel, content: LoadedContent, ui: DroidQuestUiState) {
    val progress = ui.progress
    val publishedNodes = content.roadmap.nodes.filter { it.type != RoadmapNodeType.LEVEL_PREVIEW }
    val completedCount = publishedNodes.count { it.id in progress.completedNodeIds }
    val stars = UiDerive.totalStars(content, progress)
    val maxStars = UiDerive.maxStars(content)

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 36.dp),
    ) {
        Text("Quest Map", color = DQ.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 4.dp))
        Text("Your path from beginner to Android platform expert", color = DQ.text(0.5f), fontSize = 13.sp, modifier = Modifier.padding(bottom = 18.dp))

        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(DQ.Card).border(1.dp, DQ.Border, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StatCell("$completedCount", " / ${publishedNodes.size}", "Nodes complete", DQ.TextPrimary)
            Box(Modifier.width(1.dp).height(30.dp).background(DQ.white(0.08f)))
            StatCell("$stars", " / $maxStars", "Stars earned", DQ.Amber)
        }

        Box(Modifier.height(22.dp))

        Column {
            content.categoriesInOrder().forEachIndexed { i, cat ->
                if (i > 0) Box(Modifier.padding(start = 31.dp).width(3.dp).height(28.dp).clip(RoundedCornerShape(2.dp)).background(DQ.white(0.15f)))
                MapCategoryRow(vm, content, ui, cat)
            }
        }
    }
}

@Composable
private fun StatCell(main: String, sub: String, label: String, mainColor: Color) {
    Column {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(main, color = mainColor, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(sub, color = DQ.text(0.4f), fontSize = 13.sp)
        }
        Text(label, color = DQ.text(0.45f), fontSize = 11.sp)
    }
}

@Composable
private fun MapCategoryRow(vm: DroidQuestViewModel, content: LoadedContent, ui: DroidQuestUiState, cat: CategoryDto) {
    val color = hexColor(cat.theme.color)
    val cp = UiDerive.categoryProgress(content, ui.progress, cat.id)
    val planned = cat.status == CategoryStatus.PLANNED
    val unlocked = UiDerive.isCategoryUnlocked(content, ui.progress, cat)
    val completed = cp.total > 0 && cp.completed == cp.total
    val locked = planned || !unlocked

    val subtitle = when {
        planned -> "Planned preview · unlocks in order"
        !unlocked -> "Locked · complete the previous level"
        completed -> "Completed · ${cp.total} nodes"
        else -> "${cp.completed}/${cp.total} nodes · in progress"
    }
    val badgeBg = when {
        completed -> color
        locked -> DQ.BadgeDim
        else -> DQ.Card
    }
    val badgeBorder = if (locked) DQ.white(0.15f) else color
    val glow = if (!locked && !completed) color.copy(alpha = 0.16f) else null

    Row(
        Modifier.fillMaxWidth().clickable { vm.openCategory(cat.id) }.alpha(if (locked) 0.55f else 1f),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier.size(64.dp)
                .then(if (glow != null) Modifier.drawBehind { drawCircle(glow, size.minDimension / 2 + 2.dp.toPx(), style = Stroke(4.dp.toPx())) } else Modifier)
                .clip(CircleShape).background(badgeBg).border(2.dp, badgeBorder, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            when {
                locked -> Box(Modifier.size(16.dp, 12.dp).clip(RoundedCornerShape(2.dp)).background(DQ.text(0.35f)))
                completed -> Text("✓", color = DQ.ScreenBg, fontSize = 22.sp, fontWeight = FontWeight.Black)
                else -> Diamond(color, 14.dp, 4.dp)
            }
        }
        Column(
            Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(DQ.Card).border(1.dp, DQ.Border, RoundedCornerShape(14.dp)).padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(cat.title, color = DQ.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("${cp.starsEarned}★", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Text(subtitle, color = DQ.text(0.45f), fontSize = 11.5.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

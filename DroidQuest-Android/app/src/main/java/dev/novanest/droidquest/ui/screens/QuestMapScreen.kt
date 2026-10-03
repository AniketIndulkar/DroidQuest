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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.content.model.CategoryDto
import dev.novanest.droidquest.content.model.CategoryStatus
import dev.novanest.droidquest.content.model.RoadmapNodeType
import dev.novanest.droidquest.ui.components.ClayGlyph
import dev.novanest.droidquest.ui.state.DroidQuestUiState
import dev.novanest.droidquest.ui.state.DroidQuestViewModel
import dev.novanest.droidquest.ui.state.UiDerive
import dev.novanest.droidquest.ui.theme.DQ
import dev.novanest.droidquest.ui.theme.clay
import dev.novanest.droidquest.ui.theme.clayFor
import dev.novanest.droidquest.ui.theme.hexColor

@Composable
fun QuestMapScreen(vm: DroidQuestViewModel, content: LoadedContent, ui: DroidQuestUiState) {
    val progress = ui.progress
    val publishedNodes = content.roadmap.nodes.filter { it.type != RoadmapNodeType.LEVEL_PREVIEW }
    val completedCount = publishedNodes.count { it.id in progress.completedNodeIds }
    val stars = UiDerive.totalStars(content, progress)
    val maxStars = UiDerive.maxStars(content)
    val categories = content.categoriesInOrder()
    val currentId = categories.firstOrNull { cat ->
        val cp = UiDerive.categoryProgress(content, progress, cat.id)
        cat.status != CategoryStatus.PLANNED && UiDerive.isCategoryUnlocked(content, progress, cat) && cp.completed < cp.total
    }?.id

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .background(Brush.verticalGradient(0f to Color(0xFF0F211A), 0.3f to DQ.ScreenBg))
            .padding(top = 20.dp, bottom = 36.dp),
    ) {
        Column(Modifier.padding(horizontal = 20.dp)) {
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
        }

        Box(Modifier.height(26.dp))

        // Winding trail: a dashed spine with levels alternating left and right of it.
        Box(
            Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 8.dp).drawBehind {
                val x = size.width / 2
                drawLine(
                    DQ.text(0.16f), Offset(x, 0f), Offset(x, size.height - 32.dp.toPx()),
                    strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(9.dp.toPx(), 11.dp.toPx())),
                )
            },
        ) {
            Column {
                categories.forEachIndexed { i, cat ->
                    Box(
                        Modifier.fillMaxWidth().padding(bottom = 30.dp),
                        contentAlignment = if (i % 2 == 0) Alignment.CenterStart else Alignment.CenterEnd,
                    ) {
                        MapCategoryNode(vm, content, ui, cat, isCurrent = cat.id == currentId)
                    }
                }
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
private fun MapCategoryNode(vm: DroidQuestViewModel, content: LoadedContent, ui: DroidQuestUiState, cat: CategoryDto, isCurrent: Boolean) {
    val color = hexColor(cat.theme.color)
    val clay = clayFor(color)
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
    val badgeShape = RoundedCornerShape(18.dp)

    Column(
        Modifier.fillMaxWidth(0.78f).clickable { vm.openCategory(cat.id) }.alpha(if (locked) 0.55f else 1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (isCurrent) {
            Text(
                "YOU ARE HERE", color = Color(0xFF0D110F), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.4.sp,
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(DQ.TextPrimary).padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        when {
            completed -> Box(Modifier.size(64.dp).clay(badgeShape, clay, 10.dp), contentAlignment = Alignment.Center) {
                ClayGlyph("✓", clay, 24.sp)
            }
            locked -> Box(
                Modifier.size(64.dp).clip(badgeShape).background(DQ.BadgeDim).border(3.dp, DQ.white(0.15f), badgeShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.padding(top = 3.dp).size(18.dp, 13.dp).clip(RoundedCornerShape(2.dp)).background(DQ.text(0.35f)))
            }
            else -> Box(
                Modifier.size(64.dp)
                    .drawBehind {
                        if (isCurrent) {
                            val spread = 4.dp.toPx()
                            drawRoundRect(
                                color.copy(alpha = 0.16f), Offset(-spread / 2, -spread / 2),
                                Size(size.width + spread, size.height + spread), CornerRadius(20.dp.toPx()), style = Stroke(spread),
                            )
                        }
                    }
                    .clip(badgeShape).background(DQ.Card).border(3.dp, color, badgeShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(16.dp).rotate(45f).clay(RoundedCornerShape(4.dp), clay, 4.dp))
            }
        }
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(DQ.Card).border(1.dp, DQ.Border, RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(cat.title, color = DQ.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("${cp.starsEarned}★", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Text(subtitle, color = DQ.text(0.45f), fontSize = 10.5.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

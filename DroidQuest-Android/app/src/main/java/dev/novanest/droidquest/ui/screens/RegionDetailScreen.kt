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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.content.model.CategoryStatus
import dev.novanest.droidquest.content.model.RoadmapNodeDto
import dev.novanest.droidquest.content.model.RoadmapNodeType
import dev.novanest.droidquest.domain.NodeProgress
import dev.novanest.droidquest.domain.ProgressionPolicy
import dev.novanest.droidquest.ui.components.ProgressBar
import dev.novanest.droidquest.ui.state.DroidQuestUiState
import dev.novanest.droidquest.ui.state.DroidQuestViewModel
import dev.novanest.droidquest.ui.state.UiDerive
import dev.novanest.droidquest.ui.theme.DQ
import dev.novanest.droidquest.ui.theme.hexColor

@Composable
fun RegionDetailScreen(vm: DroidQuestViewModel, content: LoadedContent, ui: DroidQuestUiState) {
    val cat = content.category(ui.nav.categoryId) ?: return
    val color = hexColor(cat.theme.color)
    val cp = UiDerive.categoryProgress(content, ui.progress, cat.id)
    val nodes = content.roadmap.nodes.filter { it.categoryId == cat.id && it.type != RoadmapNodeType.LEVEL_PREVIEW }
    val planned = cat.status == CategoryStatus.PLANNED

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 36.dp)) {
        // Header
        Column(Modifier.fillMaxWidth().background(color.copy(alpha = 0.14f)).padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 14.dp)) {
                Text("←", color = DQ.TextPrimary, fontSize = 20.sp, modifier = Modifier.width(28.dp).clickable { vm.back() })
                Text("LEVEL ${cat.order}", color = DQ.text(0.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            }
            Text(cat.title, color = DQ.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 4.dp))
            Text(cat.description, color = DQ.text(0.6f), fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(bottom = 14.dp))
            if (!planned) {
                ProgressBar(cp.pct, color, height = 8.dp, modifier = Modifier.padding(bottom = 8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${cp.completed} / ${cp.total} nodes", color = DQ.text(0.5f), fontSize = 12.sp)
                    Text("${cp.starsEarned}★", color = DQ.Amber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text("Planned preview · Weeks ${cat.weekRange.start}–${cat.weekRange.end}", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(DQ.Border))

        // Project blurb
        Column(Modifier.padding(20.dp)) {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(DQ.Card).border(1.dp, DQ.Border, RoundedCornerShape(14.dp)).padding(14.dp)) {
                Text("PROJECT · ${cat.project.title}".uppercase(), color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp, modifier = Modifier.padding(bottom = 6.dp))
                Text(cat.project.summary, color = DQ.text(0.7f), fontSize = 13.sp, lineHeight = 19.sp)
            }
            Box(Modifier.height(18.dp))

            if (planned || nodes.isEmpty()) {
                Text("PLANNED TOPICS", color = DQ.text(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    cat.plannedTopics.forEach { topic ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DQ.Card).border(1.dp, DQ.Border, RoundedCornerShape(12.dp)).padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("○", color = DQ.text(0.35f), fontSize = 13.sp)
                            Text(topic, color = DQ.text(0.7f), fontSize = 13.sp)
                        }
                    }
                }
                Box(Modifier.height(14.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DQ.text(0.06f)).border(1.dp, DQ.text(0.1f), RoundedCornerShape(12.dp)).padding(14.dp), contentAlignment = Alignment.Center) {
                    Text("This level is a planned preview and unlocks in curriculum order.", color = DQ.text(0.5f), fontSize = 12.5.sp, lineHeight = 18.sp)
                }
            } else {
                nodes.forEachIndexed { i, node ->
                    if (i > 0) Box(Modifier.padding(start = 23.dp).width(3.dp).height(22.dp).background(DQ.white(0.1f)))
                    NodeRow(vm, content, ui, node, color)
                }
            }
        }
    }
}

@Composable
private fun NodeRow(vm: DroidQuestViewModel, content: LoadedContent, ui: DroidQuestUiState, node: RoadmapNodeDto, color: Color) {
    val prog = ProgressionPolicy.progressOf(node, ui.progress.completedNodeIds)
    val isBoss = node.type == RoadmapNodeType.BOSS
    val isCheckpoint = node.type == RoadmapNodeType.CHECKPOINT
    val shape: Shape = if (isBoss || isCheckpoint) RoundedCornerShape(14.dp) else CircleShape
    val enabled = prog != NodeProgress.LOCKED
    val badgeBg = when (prog) {
        NodeProgress.COMPLETED -> color
        NodeProgress.LOCKED -> DQ.BadgeDim
        NodeProgress.AVAILABLE -> DQ.Card
    }
    val badgeBorder = if (prog == NodeProgress.LOCKED) DQ.white(0.15f) else color
    val typeLabel = when (node.type) {
        RoadmapNodeType.BOSS -> "Boss · Level checkpoint"
        RoadmapNodeType.CHECKPOINT -> "Weekly checkpoint"
        else -> "Lesson · ${node.estimatedLearningMinutes} min"
    }

    Row(
        Modifier.fillMaxWidth().clickable(enabled = enabled) { vm.openNode(node.id) }.alpha(if (enabled) 1f else 0.5f),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(46.dp).clip(shape).background(badgeBg).border(2.dp, badgeBorder, shape), contentAlignment = Alignment.Center) {
            when (prog) {
                NodeProgress.LOCKED -> Box(Modifier.size(12.dp, 9.dp).clip(RoundedCornerShape(2.dp)).background(DQ.text(0.35f)))
                NodeProgress.COMPLETED -> Text("✓", color = DQ.ScreenBg, fontSize = 16.sp, fontWeight = FontWeight.Black)
                NodeProgress.AVAILABLE -> Text(if (isBoss || isCheckpoint) "★" else "▸", color = color, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
        }
        Column(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(DQ.Card).border(1.dp, DQ.Border, RoundedCornerShape(14.dp)).padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 3.dp)) {
                if (isBoss) Text("BOSS", color = DQ.Red, fontSize = 9.5.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp,
                    modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(DQ.Red.copy(alpha = 0.16f)).padding(horizontal = 6.dp, vertical = 2.dp))
                Text(node.title, color = DQ.TextPrimary, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
            }
            Text(typeLabel, color = DQ.text(0.45f), fontSize = 11.sp)
        }
        if (prog == NodeProgress.COMPLETED && node.rewards.stars > 0) {
            Text("${node.rewards.stars}★", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

package dev.novanest.droidquest.ui.screens

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.ui.lesson.FurtherReadingCard
import dev.novanest.droidquest.ui.state.DroidQuestUiState
import dev.novanest.droidquest.ui.state.DroidQuestViewModel
import dev.novanest.droidquest.ui.theme.DQ
import dev.novanest.droidquest.ui.theme.hexColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopicDetailScreen(vm: DroidQuestViewModel, content: LoadedContent, ui: DroidQuestUiState, openUrl: (String) -> Unit) {
    val lesson = content.lesson(ui.nav.lessonId) ?: return
    val cat = content.category(lesson.categoryId)
    val color = cat?.let { hexColor(it.theme.color) } ?: DQ.Green
    val starred = ui.progress.isStarred(lesson.id)
    val scout = lesson.revealStages.scout
    val challenge = content.challengeForLesson(lesson.id)

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 36.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("←", color = DQ.TextPrimary, fontSize = 20.sp, modifier = Modifier.clickable { vm.back() })
            Text(if (starred) "★" else "☆", color = if (starred) DQ.Amber else DQ.text(0.4f), fontSize = 20.sp, modifier = Modifier.clickable { vm.toggleStar(lesson.id) })
        }
        Text(cat?.title ?: "", color = DQ.text(0.45f), fontSize = 11.5.sp, modifier = Modifier.padding(bottom = 6.dp))
        Text(lesson.title, color = DQ.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 12.dp))

        FlowRow(Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Chip(lesson.difficulty.replaceFirstChar { it.uppercase() }, color, color.copy(alpha = 0.16f))
            Chip("~${lesson.estimatedLearningMinutes} min learn", DQ.text(0.65f), DQ.text(0.08f))
            lesson.tags.forEach { Chip(it, DQ.text(0.65f), DQ.text(0.08f)) }
        }

        // Scout
        Text("WHY THIS MATTERS", color = DQ.text(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 8.dp))
        Text(scout.purpose, color = DQ.text(0.78f), fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(bottom = 12.dp))
        ScoutLine("Real Android use", scout.realWorldUse)
        ScoutLine("You'll be able to", scout.outcome)

        Box(Modifier.padding(top = 8.dp))
        Text("WHAT YOU'LL LEARN", color = DQ.text(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 22.dp)) {
            lesson.revision.objectives.forEach { obj ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("✓", color = DQ.Green, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
                    Text(obj, color = DQ.text(0.75f), fontSize = 13.5.sp, lineHeight = 20.sp)
                }
            }
        }

        // CTA -> staged lesson
        Text(
            "Begin Lesson", color = DQ.Ink, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(DQ.Green).clickable { vm.openLesson() }.padding(15.dp),
        )

        // Challenge
        if (challenge != null) {
            Box(Modifier.padding(top = 12.dp))
            Text("PRACTICE CHALLENGE", color = DQ.text(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, modifier = Modifier.padding(top = 12.dp, bottom = 8.dp))
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(DQ.Card).border(1.dp, DQ.Border, RoundedCornerShape(14.dp)).clickable { vm.openChallenge(challenge.id) }.padding(14.dp)) {
                Text(challenge.title, color = DQ.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 3.dp))
                Text("~${challenge.estimatedMinutes} min · +${challenge.rewards.xp} XP · optional", color = DQ.text(0.5f), fontSize = 11.5.sp)
            }
        }

        // Further reading
        Text("FURTHER READING", color = DQ.text(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, modifier = Modifier.padding(top = 20.dp, bottom = 10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            lesson.revealStages.learn.furtherReading.forEach { FurtherReadingCard(it, openUrl) }
        }
    }
}

@Composable
private fun ScoutLine(label: String, text: String) {
    Column(Modifier.padding(bottom = 10.dp)) {
        Text(label, color = DQ.BlueLight, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 2.dp))
        Text(text, color = DQ.text(0.7f), fontSize = 13.5.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun Chip(text: String, color: Color, bg: Color) {
    Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clip(RoundedCornerShape(100)).background(bg).padding(horizontal = 10.dp, vertical = 5.dp))
}

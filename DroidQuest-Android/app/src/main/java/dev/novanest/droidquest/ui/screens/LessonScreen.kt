package dev.novanest.droidquest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.content.model.LessonDto
import dev.novanest.droidquest.domain.ReviewRating
import dev.novanest.droidquest.ui.lesson.CodeBlock
import dev.novanest.droidquest.ui.lesson.FurtherReadingCard
import dev.novanest.droidquest.ui.lesson.LearnBlock
import dev.novanest.droidquest.ui.state.DroidQuestUiState
import dev.novanest.droidquest.ui.state.DroidQuestViewModel
import dev.novanest.droidquest.ui.theme.DQ

@Composable
fun LessonScreen(vm: DroidQuestViewModel, content: LoadedContent, ui: DroidQuestUiState, openUrl: (String) -> Unit) {
    val lesson = content.lesson(ui.nav.lessonId) ?: return
    val rs = lesson.revealStages

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 36.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 14.dp)) {
            Text("←", color = DQ.TextPrimary, fontSize = 20.sp, modifier = Modifier.clickable { vm.back() })
            Text("LESSON", color = DQ.text(0.45f), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
        Text(lesson.title, color = DQ.TextPrimary, fontSize = 21.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 18.dp))

        // ── Scout ──
        Stage("Scout") {
            ScoutRow("Why it matters", rs.scout.purpose)
            ScoutRow("Real Android use", rs.scout.realWorldUse)
            ScoutRow("Outcome", rs.scout.outcome)
        }

        // ── Learn ──
        Stage("Learn · ~${rs.learn.estimatedMinutes} min") {
            rs.learn.sections.forEach { section ->
                Text(section.title, color = DQ.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 18.dp)) {
                    section.blocks.forEach { LearnBlock(it) }
                }
            }
        }

        // ── Further reading ──
        Stage("Further reading") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rs.learn.furtherReading.forEach { FurtherReadingCard(it, openUrl) }
            }
        }

        // ── Inspect ──
        Stage("Inspect · ${rs.inspect.title}") {
            CodeBlock(rs.inspect.language, rs.inspect.code)
            Box(Modifier.padding(top = 12.dp))
            Text("Walkthrough", color = DQ.text(0.55f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 6.dp))
            rs.inspect.walkthrough.forEachIndexed { i, step ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 3.dp)) {
                    Text("${i + 1}.", color = DQ.Blue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(step, color = DQ.text(0.78f), fontSize = 13.5.sp, lineHeight = 19.5.sp)
                }
            }
            Box(Modifier.fillMaxWidth().padding(top = 10.dp).clip(RoundedCornerShape(10.dp)).background(DQ.Green.copy(alpha = 0.08f)).border(1.dp, DQ.Green.copy(alpha = 0.3f), RoundedCornerShape(10.dp)).padding(12.dp)) {
                Column {
                    Text("Expected output", color = DQ.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                    Text(rs.inspect.expectedOutput, color = DQ.text(0.8f), fontSize = 13.sp, fontFamily = dev.novanest.droidquest.ui.theme.Mono)
                }
            }
        }

        // ── Trap Check ──
        Stage("Trap Check") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rs.trapCheck.forEach { trap ->
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DQ.Red.copy(alpha = 0.08f)).border(1.dp, DQ.Red.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).padding(13.dp)) {
                        Text("✗ ${trap.mistake}", color = DQ.Red, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 5.dp))
                        Text("Why: ${trap.why}", color = DQ.text(0.7f), fontSize = 12.5.sp, lineHeight = 18.sp, modifier = Modifier.padding(bottom = 5.dp))
                        Text("Fix: ${trap.fix}", color = DQ.Green, fontSize = 12.5.sp, lineHeight = 18.sp)
                    }
                }
            }
        }

        // ── Challenge intro ──
        Stage("Challenge") {
            Text(rs.challengeIntro.task, color = DQ.text(0.8f), fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text("Success looks like: ${rs.challengeIntro.successLooksLike}", color = DQ.text(0.6f), fontSize = 13.sp, lineHeight = 19.sp)
            content.challengeForLesson(lesson.id)?.let { challenge ->
                Text("Open practice challenge ›", color = DQ.Green, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp).clickable { vm.openChallenge(challenge.id) })
            }
        }

        // ── Recall ──
        Stage("Recall") {
            RecallList(vm, lesson)
        }

        // Practice quiz CTA
        Box(Modifier.padding(top = 8.dp))
        Text(
            "Practice This Quest", color = DQ.Ink, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(DQ.Green).clickable { vm.startQuiz(lesson.quizId) }.padding(15.dp),
        )
    }
}

@Composable
private fun Stage(label: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(bottom = 22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
            Box(Modifier.clip(RoundedCornerShape(100)).background(DQ.Green.copy(alpha = 0.16f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                Text(label, color = DQ.Green, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
            }
        }
        content()
    }
}

@Composable
private fun ScoutRow(label: String, text: String) {
    Column(Modifier.padding(bottom = 10.dp)) {
        Text(label, color = DQ.BlueLight, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 2.dp))
        Text(text, color = DQ.text(0.75f), fontSize = 13.5.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun RecallList(vm: DroidQuestViewModel, lesson: LessonDto) {
    val answers = remember(lesson.id) { mutableStateMapOf<String, String>() }
    val revealed = remember(lesson.id) { mutableStateMapOf<String, Boolean>() }
    val rated = remember(lesson.id) { mutableStateMapOf<String, ReviewRating>() }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Answer from memory before looking. Your wording does not need to match exactly.", color = DQ.text(0.55f), fontSize = 12.5.sp, lineHeight = 18.sp)
        lesson.revealStages.recall.forEachIndexed { index, item ->
            val recallId = item.id.ifBlank { "${lesson.id}-recall-${index + 1}" }
            val answer = answers[recallId].orEmpty()
            val isOpen = revealed[recallId] == true
            val rating = rated[recallId]
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DQ.Card).border(1.dp, DQ.Border, RoundedCornerShape(12.dp))
                    .padding(13.dp),
            ) {
                Text(item.prompt, color = DQ.TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                BasicTextField(
                    value = answer,
                    onValueChange = { if (!isOpen) answers[recallId] = it },
                    enabled = !isOpen,
                    textStyle = TextStyle(color = DQ.TextPrimary, fontSize = 13.sp, lineHeight = 19.sp),
                    cursorBrush = SolidColor(DQ.Green),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp).clip(RoundedCornerShape(10.dp)).background(DQ.ScreenBg).border(1.dp, DQ.white(0.08f), RoundedCornerShape(10.dp)).padding(11.dp),
                    decorationBox = { inner ->
                        if (answer.isEmpty()) Text("Write what you remember…", color = DQ.text(0.32f), fontSize = 12.5.sp)
                        inner()
                    },
                )
                if (isOpen) {
                    Text("MODEL ANSWER", color = DQ.BlueLight, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                    Text(item.answer, color = DQ.text(0.75f), fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 4.dp))
                    if (rating == null) {
                        Text("How well did you remember it?", color = DQ.text(0.5f), fontSize = 11.5.sp, modifier = Modifier.padding(top = 12.dp, bottom = 7.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RecallRating("Again", DQ.Amber, Modifier.weight(1f)) { rated[recallId] = ReviewRating.AGAIN; vm.rateRecall(lesson.id, recallId, ReviewRating.AGAIN) }
                            RecallRating("Hard", DQ.BlueLight, Modifier.weight(1f)) { rated[recallId] = ReviewRating.HARD; vm.rateRecall(lesson.id, recallId, ReviewRating.HARD) }
                            RecallRating("Good", DQ.Green, Modifier.weight(1f)) { rated[recallId] = ReviewRating.GOOD; vm.rateRecall(lesson.id, recallId, ReviewRating.GOOD) }
                            RecallRating("Easy", DQ.Green, Modifier.weight(1f)) { rated[recallId] = ReviewRating.EASY; vm.rateRecall(lesson.id, recallId, ReviewRating.EASY) }
                        }
                    } else {
                        Text("Scheduled for review · ${rating.name.lowercase()}", color = DQ.Green, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
                    }
                } else {
                    Text(
                        "Compare answer",
                        color = if (answer.isBlank()) DQ.text(0.28f) else DQ.Green,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 9.dp).clip(RoundedCornerShape(9.dp)).background(DQ.Green.copy(alpha = if (answer.isBlank()) 0.03f else 0.10f)).clickable(enabled = answer.isNotBlank()) { revealed[recallId] = true }.padding(10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RecallRating(label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.clip(RoundedCornerShape(9.dp)).background(color.copy(alpha = 0.14f)).border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(9.dp)).clickable(onClick = onClick).padding(vertical = 9.dp), contentAlignment = Alignment.Center) {
        Text(label, color = color, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
    }
}

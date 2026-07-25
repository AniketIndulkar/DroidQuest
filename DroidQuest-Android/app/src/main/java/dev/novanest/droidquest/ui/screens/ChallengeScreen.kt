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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.ui.lesson.CodeBlock
import dev.novanest.droidquest.ui.state.DroidQuestUiState
import dev.novanest.droidquest.ui.state.DroidQuestViewModel
import dev.novanest.droidquest.ui.theme.DQ

@Composable
fun ChallengeScreen(vm: DroidQuestViewModel, content: LoadedContent, ui: DroidQuestUiState) {
    val challenge = content.challenge(ui.nav.challengeId) ?: return
    val done = ui.progress.isChallengeComplete(challenge.id)
    var hintsShown by remember(challenge.id) { mutableIntStateOf(0) }
    var showSolution by remember(challenge.id) { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 36.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 14.dp)) {
            Text("←", color = DQ.TextPrimary, fontSize = 20.sp, modifier = Modifier.clickable { vm.back() })
            Text("CHALLENGE · OPTIONAL", color = DQ.text(0.45f), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
        Text(challenge.title, color = DQ.TextPrimary, fontSize = 21.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 8.dp))
        Text("~${challenge.estimatedMinutes} min practice · +${challenge.rewards.xp} XP · ${challenge.rewards.stars}★", color = DQ.text(0.5f), fontSize = 12.5.sp, modifier = Modifier.padding(bottom = 16.dp))
        Text(challenge.prompt, color = DQ.text(0.82f), fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(bottom = 20.dp))

        Section("Success criteria")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 20.dp)) {
            challenge.successCriteria.forEach {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("✓", color = DQ.Green, fontSize = 13.sp, modifier = Modifier.padding(top = 1.dp))
                    Text(it, color = DQ.text(0.75f), fontSize = 13.5.sp, lineHeight = 19.5.sp)
                }
            }
        }

        Section("Starter code")
        Box(Modifier.padding(bottom = 20.dp)) { CodeBlock(challenge.starterCode.language, challenge.starterCode.code) }

        Section("Hints")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 12.dp)) {
            challenge.hints.take(hintsShown).forEachIndexed { i, hint ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DQ.Amber.copy(alpha = 0.08f)).border(1.dp, DQ.Amber.copy(alpha = 0.25f), RoundedCornerShape(12.dp)).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💡", fontSize = 13.sp)
                    Text(hint, color = DQ.text(0.78f), fontSize = 13.sp, lineHeight = 19.sp)
                }
            }
        }
        if (hintsShown < challenge.hints.size) {
            Text("Reveal hint ${hintsShown + 1} of ${challenge.hints.size} ›", color = DQ.Amber, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { hintsShown++ }.padding(bottom = 20.dp, top = 4.dp))
        } else {
            Box(Modifier.padding(bottom = 20.dp))
        }

        Section("Solution outline")
        if (showSolution) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 20.dp)) {
                challenge.solutionOutline.forEachIndexed { i, step ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${i + 1}.", color = DQ.Blue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(step, color = DQ.text(0.75f), fontSize = 13.5.sp, lineHeight = 19.5.sp)
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DQ.Card).border(1.dp, DQ.Border, RoundedCornerShape(12.dp)).clickable { showSolution = true }.padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Reveal solution outline (try it yourself first)", color = DQ.text(0.55f), fontSize = 13.sp)
            }
            Box(Modifier.padding(bottom = 20.dp))
        }

        Section("Verification")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 24.dp)) {
            challenge.verification.forEach {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("▸", color = DQ.Green, fontSize = 13.sp, modifier = Modifier.padding(top = 1.dp))
                    Text(it, color = DQ.text(0.75f), fontSize = 13.5.sp, lineHeight = 19.5.sp)
                }
            }
        }

        if (done) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(DQ.Green.copy(alpha = 0.12f)).border(1.dp, DQ.Green.copy(alpha = 0.35f), RoundedCornerShape(14.dp)).padding(15.dp), contentAlignment = Alignment.Center) {
                Text("✓ Challenge completed", color = DQ.Green, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            }
        } else {
            Text(
                "Mark Challenge Complete", color = DQ.Ink, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(DQ.Green).clickable { vm.completeChallenge(challenge.id) }.padding(15.dp),
            )
        }
    }
}

@Composable
private fun Section(label: String) {
    Text(label.uppercase(), color = DQ.text(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 10.dp))
}

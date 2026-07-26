package dev.novanest.droidquest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.domain.ReviewRating
import dev.novanest.droidquest.ui.state.DroidQuestUiState
import dev.novanest.droidquest.ui.state.DroidQuestViewModel
import dev.novanest.droidquest.ui.theme.DQ

@Composable
fun ReviewScreen(vm: DroidQuestViewModel, content: LoadedContent, ui: DroidQuestUiState) {
    val state = ui.review ?: return
    val item = content.recallItem(state.currentRecallItemId)

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("←", color = DQ.TextPrimary, fontSize = 20.sp, modifier = Modifier.clickable { vm.exitReview() })
            Column(Modifier.weight(1f)) {
                Text("DAILY REVIEW", color = DQ.BlueLight, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                Text("${state.index.coerceAtMost(state.recallItemIds.size)} of ${state.recallItemIds.size}", color = DQ.text(0.45f), fontSize = 11.sp)
            }
        }

        if (state.isComplete || item == null) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Memory strengthened", color = DQ.Green, fontSize = 21.sp, fontWeight = FontWeight.Black)
                Text("You’re caught up for now. Come back when another idea is ready to revisit.", color = DQ.text(0.6f), fontSize = 13.5.sp, lineHeight = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
                ReviewAction("Back Home", DQ.Green) { vm.exitReview() }
            }
            return@Column
        }

        Spacer(Modifier.height(34.dp))
        Text(item.lesson.title.uppercase(), color = DQ.text(0.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
        Text(item.recall.prompt, color = DQ.TextPrimary, fontSize = 19.sp, lineHeight = 27.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 18.dp))

        BasicTextField(
            value = state.answer,
            onValueChange = vm::setReviewAnswer,
            enabled = !state.revealed,
            textStyle = TextStyle(color = DQ.TextPrimary, fontSize = 14.sp, lineHeight = 21.sp),
            cursorBrush = SolidColor(DQ.Green),
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(DQ.Card).border(1.dp, DQ.Border, RoundedCornerShape(14.dp)).padding(15.dp),
            decorationBox = { inner ->
                if (state.answer.isEmpty()) Text("Explain it from memory first…", color = DQ.text(0.35f), fontSize = 14.sp)
                inner()
            },
        )

        if (state.revealed) {
            Column(Modifier.fillMaxWidth().padding(top = 16.dp).clip(RoundedCornerShape(14.dp)).background(DQ.Blue.copy(alpha = 0.10f)).border(1.dp, DQ.Blue.copy(alpha = 0.3f), RoundedCornerShape(14.dp)).padding(15.dp)) {
                Text("MODEL ANSWER", color = DQ.BlueLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(item.recall.answer, color = DQ.TextPrimary, fontSize = 13.5.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 6.dp))
            }
        }

        Spacer(Modifier.weight(1f))
        if (!state.revealed) {
            ReviewAction("Compare Answer", if (state.answer.isBlank()) DQ.text(0.15f) else DQ.Green, enabled = state.answer.isNotBlank()) { vm.revealReviewAnswer() }
        } else {
            Text("How well did you remember the idea?", color = DQ.text(0.55f), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                RatingButton("Again", DQ.Amber, Modifier.weight(1f)) { vm.rateCurrentReview(ReviewRating.AGAIN) }
                RatingButton("Hard", DQ.BlueLight, Modifier.weight(1f)) { vm.rateCurrentReview(ReviewRating.HARD) }
                RatingButton("Good", DQ.Green, Modifier.weight(1f)) { vm.rateCurrentReview(ReviewRating.GOOD) }
                RatingButton("Easy", DQ.Green, Modifier.weight(1f)) { vm.rateCurrentReview(ReviewRating.EASY) }
            }
        }
    }
}

@Composable
private fun ReviewAction(label: String, color: androidx.compose.ui.graphics.Color, enabled: Boolean = true, onClick: () -> Unit) {
    Text(label, color = DQ.Ink, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(color).clickable(enabled = enabled, onClick = onClick).padding(15.dp))
}

@Composable
private fun RatingButton(label: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.16f)).border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

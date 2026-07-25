package dev.novanest.droidquest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.content.model.QuestionDto
import dev.novanest.droidquest.content.model.QuestionType
import dev.novanest.droidquest.domain.QuizEvaluator
import dev.novanest.droidquest.domain.UserAnswer
import dev.novanest.droidquest.ui.state.DroidQuestUiState
import dev.novanest.droidquest.ui.state.DroidQuestViewModel
import dev.novanest.droidquest.ui.state.QuizPhase
import dev.novanest.droidquest.ui.theme.DQ
import dev.novanest.droidquest.ui.theme.Mono

@Composable
fun RevisionScreen(vm: DroidQuestViewModel, content: LoadedContent, ui: DroidQuestUiState) {
    val quizState = ui.quiz ?: return
    val quiz = content.quiz(quizState.quizId) ?: return
    val total = quiz.questions.size

    Column(Modifier.fillMaxSize().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 36.dp)) {
        // Progress header
        Row(Modifier.fillMaxWidth().padding(bottom = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("✕", color = DQ.text(0.6f), fontSize = 18.sp, modifier = Modifier.clickable { vm.exitQuiz() })
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                quiz.questions.indices.forEach { i ->
                    val c = when {
                        i < quizState.index -> DQ.Green
                        i == quizState.index -> DQ.text(0.35f)
                        else -> DQ.text(0.1f)
                    }
                    Box(Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(100)).background(c))
                }
            }
            Text("${quizState.index.coerceAtMost(total)}/$total", color = DQ.Amber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        if (quizState.phase == QuizPhase.DONE) {
            QuizResult(vm, quiz, quizState, content)
            return@Column
        }

        val question = quiz.questions[quizState.index]
        val typeLabel = question.type.name.lowercase().replace('_', ' ')

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Text("${quiz.title} · $typeLabel".uppercase(), color = DQ.text(0.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text(question.prompt, color = DQ.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

            val enabled = quizState.phase == QuizPhase.QUESTION
            QuestionInput(question, vm, ui, enabled)

            if (quizState.phase == QuizPhase.FEEDBACK) {
                Spacer(Modifier.height(16.dp))
                FeedbackBox(quizState.lastCorrect == true, question.explanation)
            }
        }

        Spacer(Modifier.height(12.dp))
        val answered = isAnswered(question, ui.quiz?.answerFor(question.id) ?: UserAnswer.None)
        if (quizState.phase == QuizPhase.QUESTION) {
            PrimaryButton("Check Answer", enabled = answered, bg = if (answered) DQ.Green else DQ.text(0.15f)) { vm.submitCurrentQuestion() }
        } else {
            PrimaryButton(if (quizState.index + 1 >= total) "See Results" else "Next Question", bg = DQ.Green) { vm.nextQuestion() }
        }
    }
}

private fun isAnswered(q: QuestionDto, a: UserAnswer): Boolean = when (q.type) {
    QuestionType.MULTIPLE_CHOICE -> a is UserAnswer.Choices && a.values.isNotEmpty()
    QuestionType.ORDER_STEPS -> a is UserAnswer.Choices && a.values.isNotEmpty()
    QuestionType.MATCH_PAIRS -> a is UserAnswer.Pairs && a.map.size >= QuizEvaluator.matchLefts(q).size
    QuestionType.TRUE_FALSE -> a is UserAnswer.Bool
    else -> a is UserAnswer.Text && a.value.isNotBlank()
}

@Composable
private fun QuestionInput(q: QuestionDto, vm: DroidQuestViewModel, ui: DroidQuestUiState, enabled: Boolean) {
    when (q.type) {
        QuestionType.SINGLE_CHOICE -> SingleChoice(q, vm, ui, enabled)
        QuestionType.TRUE_FALSE -> TrueFalse(q, vm, ui, enabled)
        QuestionType.MULTIPLE_CHOICE -> MultipleChoice(q, vm, ui, enabled)
        QuestionType.ORDER_STEPS -> OrderSteps(q, vm, enabled)
        QuestionType.MATCH_PAIRS -> MatchPairs(q, vm, enabled)
        QuestionType.FILL_BLANK, QuestionType.SHORT_ANSWER, QuestionType.SPOT_BUG, QuestionType.CODE_OUTPUT ->
            TextInput(q, vm, ui, enabled)
    }
}

@Composable
private fun SingleChoice(q: QuestionDto, vm: DroidQuestViewModel, ui: DroidQuestUiState, enabled: Boolean) {
    val current = (ui.quiz?.answerFor(q.id) as? UserAnswer.Text)?.value
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        QuizEvaluator.optionLabels(q).forEach { opt ->
            OptionCard(opt, selected = current == opt, enabled = enabled) { vm.setQuizAnswer(q.id, UserAnswer.Text(opt)) }
        }
    }
}

@Composable
private fun MultipleChoice(q: QuestionDto, vm: DroidQuestViewModel, ui: DroidQuestUiState, enabled: Boolean) {
    val current = (ui.quiz?.answerFor(q.id) as? UserAnswer.Choices)?.values ?: emptyList()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        QuizEvaluator.optionLabels(q).forEach { opt ->
            val selected = opt in current
            OptionCard(opt, selected = selected, enabled = enabled, multi = true) {
                val next = if (selected) current - opt else current + opt
                vm.setQuizAnswer(q.id, UserAnswer.Choices(next))
            }
        }
    }
}

@Composable
private fun TrueFalse(q: QuestionDto, vm: DroidQuestViewModel, ui: DroidQuestUiState, enabled: Boolean) {
    val current = (ui.quiz?.answerFor(q.id) as? UserAnswer.Bool)?.value
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(true to "True", false to "False").forEach { (value, label) ->
            Box(Modifier.weight(1f)) {
                OptionCard(label, selected = current == value, enabled = enabled) { vm.setQuizAnswer(q.id, UserAnswer.Bool(value)) }
            }
        }
    }
}

@Composable
private fun TextInput(q: QuestionDto, vm: DroidQuestViewModel, ui: DroidQuestUiState, enabled: Boolean) {
    val value = (ui.quiz?.answerFor(q.id) as? UserAnswer.Text)?.value ?: ""
    val isCode = q.type == QuestionType.CODE_OUTPUT
    val placeholder = when (q.type) {
        QuestionType.CODE_OUTPUT -> "Type the exact output"
        QuestionType.FILL_BLANK -> "Type the missing text"
        else -> "Type your answer"
    }
    BasicTextField(
        value = value,
        onValueChange = { if (enabled) vm.setQuizAnswer(q.id, UserAnswer.Text(it)) },
        enabled = enabled,
        textStyle = TextStyle(color = DQ.TextPrimary, fontSize = 14.sp, fontFamily = if (isCode) Mono else null),
        cursorBrush = SolidColor(DQ.Green),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DQ.Card).border(1.dp, DQ.white(0.1f), RoundedCornerShape(12.dp)).padding(14.dp),
        decorationBox = { inner ->
            if (value.isEmpty()) Text(placeholder, color = DQ.text(0.35f), fontSize = 14.sp, fontFamily = if (isCode) Mono else null)
            inner()
        },
    )
}

@Composable
private fun OrderSteps(q: QuestionDto, vm: DroidQuestViewModel, enabled: Boolean) {
    // Accessible ordering via up/down controls (not drag-only). Working order starts shuffled.
    val working = remember(q.id) { mutableStateListOf<String>().apply { addAll(QuizEvaluator.orderedSteps(q).shuffled()) } }
    // Seed the answer so submit reads the current order even without interaction.
    androidx.compose.runtime.LaunchedEffect(q.id) { vm.setQuizAnswer(q.id, UserAnswer.Choices(working.toList())) }

    fun push() = vm.setQuizAnswer(q.id, UserAnswer.Choices(working.toList()))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        working.forEachIndexed { i, step ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DQ.Card).border(1.dp, DQ.white(0.08f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(Modifier.size(22.dp).clip(RoundedCornerShape(100)).background(DQ.Blue.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Text("${i + 1}", color = DQ.BlueLight, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
                Text(step, color = DQ.TextPrimary, fontSize = 13.sp, fontFamily = Mono, modifier = Modifier.weight(1f))
                Text("↑", color = if (i > 0 && enabled) DQ.Green else DQ.text(0.2f), fontSize = 18.sp,
                    modifier = Modifier.clickable(enabled = enabled && i > 0) { val t = working[i]; working[i] = working[i - 1]; working[i - 1] = t; push() }.padding(horizontal = 4.dp))
                Text("↓", color = if (i < working.lastIndex && enabled) DQ.Green else DQ.text(0.2f), fontSize = 18.sp,
                    modifier = Modifier.clickable(enabled = enabled && i < working.lastIndex) { val t = working[i]; working[i] = working[i + 1]; working[i + 1] = t; push() }.padding(horizontal = 4.dp))
            }
        }
    }
}

@Composable
private fun MatchPairs(q: QuestionDto, vm: DroidQuestViewModel, enabled: Boolean) {
    val lefts = remember(q.id) { QuizEvaluator.matchLefts(q) }
    val rights = remember(q.id) { QuizEvaluator.matchRights(q).shuffled() }
    val picks = remember(q.id) { mutableStateMapOf<String, String>() }

    fun push() = vm.setQuizAnswer(q.id, UserAnswer.Pairs(picks.toMap()))

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        lefts.forEach { left ->
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DQ.Card).border(1.dp, DQ.white(0.08f), RoundedCornerShape(12.dp)).padding(12.dp)) {
                Text(left, color = DQ.TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rights.forEach { right ->
                        val selected = picks[left] == right
                        Text(
                            right, color = if (selected) DQ.Ink else DQ.text(0.7f), fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, fontFamily = Mono,
                            modifier = Modifier.clip(RoundedCornerShape(100)).background(if (selected) DQ.Blue else DQ.text(0.08f))
                                .clickable(enabled = enabled) { picks[left] = right; push() }.padding(horizontal = 12.dp, vertical = 7.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionCard(text: String, selected: Boolean, enabled: Boolean, multi: Boolean = false, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (selected) DQ.Blue.copy(alpha = 0.16f) else DQ.Card)
            .border(1.5.dp, if (selected) DQ.Blue else DQ.white(0.08f), RoundedCornerShape(12.dp)).clickable(enabled = enabled, onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (multi) {
            Box(Modifier.size(18.dp).clip(RoundedCornerShape(5.dp)).background(if (selected) DQ.Blue else Color.Transparent).border(1.5.dp, if (selected) DQ.Blue else DQ.text(0.3f), RoundedCornerShape(5.dp)), contentAlignment = Alignment.Center) {
                if (selected) Text("✓", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
        Text(text, color = DQ.TextPrimary, fontSize = 14.5.sp)
    }
}

@Composable
private fun FeedbackBox(correct: Boolean, explanation: String) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(if (correct) DQ.Green.copy(alpha = 0.12f) else DQ.Red.copy(alpha = 0.12f))
            .border(1.dp, if (correct) DQ.Green.copy(alpha = 0.35f) else DQ.Red.copy(alpha = 0.35f), RoundedCornerShape(14.dp)).padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(if (correct) "Correct!" else "Not quite", color = if (correct) DQ.Green else DQ.Red, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(bottom = 6.dp))
        Text(explanation, color = DQ.text(0.7f), fontSize = 13.sp, lineHeight = 19.5.sp)
    }
}

@Composable
private fun PrimaryButton(label: String, enabled: Boolean = true, bg: Color, onClick: () -> Unit) {
    Text(
        label, color = DQ.Ink, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(bg).clickable(enabled = enabled, onClick = onClick).padding(15.dp),
    )
}

@Composable
private fun QuizResult(vm: DroidQuestViewModel, quiz: dev.novanest.droidquest.content.model.QuizDto, quizState: dev.novanest.droidquest.ui.state.QuizUiState, content: LoadedContent) {
    val score = quizState.score
    val recorded = quizState.recorded
    val passed = score?.passed == true
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(if (passed) "✓" else "✕", color = if (passed) DQ.Green else DQ.Red, fontSize = 44.sp)
        Spacer(Modifier.height(12.dp))
        Text("${score?.correct ?: 0} / ${score?.total ?: quiz.questions.size} correct", color = DQ.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(6.dp))
        Text(
            if (passed) "Passed · needed ${(quiz.passingScore * 100).toInt()}%" else "Keep going · needed ${(quiz.passingScore * 100).toInt()}%",
            color = if (passed) DQ.Green else DQ.text(0.55f), fontSize = 13.sp, fontWeight = FontWeight.Bold,
        )
        if (recorded != null && recorded.firstPass) {
            Spacer(Modifier.height(10.dp))
            Text("+${recorded.outcome.xpAwarded} XP · ${recorded.outcome.starsAwarded}★ earned", color = DQ.Amber, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        } else if (passed) {
            Spacer(Modifier.height(10.dp))
            Text("Already mastered — rewards granted earlier", color = DQ.text(0.45f), fontSize = 12.sp)
        }
        Spacer(Modifier.height(24.dp))
        PrimaryButton("Back to Map", bg = DQ.Green) { vm.exitQuiz() }
    }
}

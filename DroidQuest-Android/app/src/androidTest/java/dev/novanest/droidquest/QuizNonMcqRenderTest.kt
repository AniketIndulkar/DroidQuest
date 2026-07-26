package dev.novanest.droidquest

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.novanest.droidquest.content.AssetContentSource
import dev.novanest.droidquest.content.ContentLoadState
import dev.novanest.droidquest.content.DroidQuestContentRepository
import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.content.model.QuestionType
import dev.novanest.droidquest.ui.screens.RevisionScreen
import dev.novanest.droidquest.ui.state.DroidQuestUiState
import dev.novanest.droidquest.ui.state.DroidQuestViewModel
import dev.novanest.droidquest.ui.state.NavState
import dev.novanest.droidquest.ui.state.QuizUiState
import dev.novanest.droidquest.ui.state.QuizPhase
import dev.novanest.droidquest.ui.state.Screen
import dev.novanest.droidquest.ui.theme.DroidQuestTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Renders a non-MCQ (true/false) quiz question and asserts its dedicated controls appear. */
@RunWith(AndroidJUnit4::class)
class QuizNonMcqRenderTest {

    @get:Rule val rule = createComposeRule()

    private fun content(): LoadedContent {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val state = runBlocking { DroidQuestContentRepository(AssetContentSource(ctx)).load() }
        return (state as ContentLoadState.Success).content
    }

    @Test
    fun true_false_question_renders_true_false_controls() {
        val content = content()
        // Find a quiz + index whose question is true_false (a non-MCQ interaction).
        var quizId: String? = null
        var index = -1
        for (q in content.quizzesById.values) {
            val i = q.questions.indexOfFirst { it.type == QuestionType.TRUE_FALSE }
            if (i >= 0) { quizId = q.id; index = i; break }
        }
        requireNotNull(quizId) { "content must contain a true_false question" }

        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val vm = DroidQuestViewModel(DroidQuestContentRepository(AssetContentSource(ctx)), InMemoryProgressRepository())
        val ui = DroidQuestUiState(
            loadState = ContentLoadState.Success(content),
            nav = NavState(screen = Screen.REVISION, quizId = quizId),
            quiz = QuizUiState(quizId = quizId!!, index = index),
        )

        rule.setContent { DroidQuestTheme { RevisionScreen(vm, content, ui) } }

        rule.onNodeWithText("True").assertIsDisplayed()
        rule.onNodeWithText("False").assertIsDisplayed()
        rule.onNodeWithText("Check Answer").assertIsDisplayed()
    }

    @Test
    fun wrong_code_output_reveals_exact_expected_output_and_gentle_guidance() {
        val content = content()
        val quiz = content.quizzesById.values.first { candidate ->
            candidate.questions.any { it.type == QuestionType.CODE_OUTPUT }
        }
        val index = quiz.questions.indexOfFirst { it.type == QuestionType.CODE_OUTPUT }
        val question = quiz.questions[index]

        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val vm = DroidQuestViewModel(DroidQuestContentRepository(AssetContentSource(ctx)), InMemoryProgressRepository())
        val ui = DroidQuestUiState(
            loadState = ContentLoadState.Success(content),
            nav = NavState(screen = Screen.REVISION, quizId = quiz.id),
            quiz = QuizUiState(quizId = quiz.id, index = index, phase = QuizPhase.FEEDBACK, lastCorrect = false),
        )

        rule.setContent { DroidQuestTheme { RevisionScreen(vm, content, ui) } }

        rule.onNodeWithText("Enter what the program prints—not Kotlin code.").assertIsDisplayed()
        rule.onNodeWithText("Expected answer").assertIsDisplayed()
        rule.onNodeWithText(question.answer.toString().trim('"')).assertIsDisplayed()
        rule.onNodeWithText("Let’s learn from this one").assertIsDisplayed()
    }
}

package dev.novanest.droidquest.ui.state

import dev.novanest.droidquest.content.ContentLoadState
import dev.novanest.droidquest.domain.QuizScore
import dev.novanest.droidquest.domain.UserAnswer
import dev.novanest.droidquest.progress.LearnerProgress
import dev.novanest.droidquest.progress.QuizRecordResult

enum class Screen { HOME, MAP, REGION, TOPIC, LESSON, REVISION, CHALLENGE, SEARCH, STARRED, SETTINGS }

val Screen.isTopLevel: Boolean
    get() = this == Screen.HOME || this == Screen.MAP || this == Screen.SEARCH ||
        this == Screen.STARRED || this == Screen.SETTINGS

/** Navigation, addressed only by stable content IDs so config changes never lose place. */
data class NavState(
    val screen: Screen = Screen.HOME,
    val categoryId: String? = null,
    val nodeId: String? = null,
    val lessonId: String? = null,
    val quizId: String? = null,
    val challengeId: String? = null,
    val aiOpen: Boolean = false,
    val query: String = "",
    val tagFilter: String = "All",
    val backStack: List<NavState> = emptyList(),
)

enum class QuizPhase { QUESTION, FEEDBACK, DONE }

/** Active quiz session; survives configuration changes because it lives in the ViewModel. */
data class QuizUiState(
    val quizId: String,
    val index: Int = 0,
    val phase: QuizPhase = QuizPhase.QUESTION,
    val answers: Map<String, UserAnswer> = emptyMap(),
    val lastCorrect: Boolean? = null,
    val score: QuizScore? = null,
    val recorded: QuizRecordResult? = null,
) {
    fun answerFor(questionId: String): UserAnswer = answers[questionId] ?: UserAnswer.None
}

/** Single immutable state surface consumed by the UI. */
data class DroidQuestUiState(
    val loadState: ContentLoadState = ContentLoadState.Loading,
    val progress: LearnerProgress = LearnerProgress(),
    val nav: NavState = NavState(),
    val quiz: QuizUiState? = null,
)

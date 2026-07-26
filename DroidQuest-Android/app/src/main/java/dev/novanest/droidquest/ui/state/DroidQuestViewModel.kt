package dev.novanest.droidquest.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.novanest.droidquest.content.ContentLoadState
import dev.novanest.droidquest.content.DroidQuestContentRepository
import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.content.model.RoadmapNodeType
import dev.novanest.droidquest.domain.ProgressionPolicy
import dev.novanest.droidquest.domain.QuizEvaluator
import dev.novanest.droidquest.domain.ReviewRating
import dev.novanest.droidquest.domain.SpacedRepetitionPolicy
import dev.novanest.droidquest.domain.UserAnswer
import dev.novanest.droidquest.progress.LearnerProgress
import dev.novanest.droidquest.progress.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Lifecycle-safe application state holder. Holds no Context/View/binding. Content loads
 * asynchronously; navigation and the active quiz survive configuration changes because they
 * live here. Progression and reward decisions are delegated to the pure policy objects.
 */
class DroidQuestViewModel(
    private val contentRepo: DroidQuestContentRepository,
    private val progressRepo: ProgressRepository,
    private val nowEpochMillis: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    private val loadState = MutableStateFlow<ContentLoadState>(ContentLoadState.Loading)
    private val nav = MutableStateFlow(NavState())
    private val quiz = MutableStateFlow<QuizUiState?>(null)
    private val review = MutableStateFlow<ReviewUiState?>(null)

    val uiState: StateFlow<DroidQuestUiState> =
        combine(loadState, progressRepo.progress, nav, quiz, review) { load, progress, navState, quizState, reviewState ->
            DroidQuestUiState(load, progress, navState, quizState, reviewState)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, DroidQuestUiState())

    private val content: LoadedContent?
        get() = (loadState.value as? ContentLoadState.Success)?.content

    init {
        loadContent()
    }

    fun loadContent() {
        loadState.value = ContentLoadState.Loading
        viewModelScope.launch { loadState.value = contentRepo.load() }
    }

    // ── Top-level navigation ─────────────────────────────────────────────
    fun goTo(screen: Screen) = nav.update { it.copy(screen = screen, aiOpen = false) }

    private fun push(next: NavState) = nav.update { cur ->
        next.copy(backStack = cur.backStack + cur.copy(backStack = emptyList()))
    }

    fun back() = nav.update { cur ->
        val prev = cur.backStack.lastOrNull()
        if (prev != null) prev.copy(backStack = cur.backStack.dropLast(1)) else cur.copy(screen = Screen.HOME)
    }

    fun openCategory(categoryId: String) =
        push(nav.value.copy(screen = Screen.REGION, categoryId = categoryId, aiOpen = false))

    /** Route a roadmap node to its correct destination based on stable type/links. */
    fun openNode(nodeId: String) {
        val node = content?.roadmapNodesById?.get(nodeId) ?: return
        if (ProgressionPolicy.isPlanned(node)) return
        when (node.type) {
            RoadmapNodeType.LESSON, RoadmapNodeType.START ->
                node.lessonId?.let { openTopic(it, nodeId, node.categoryId) }
            RoadmapNodeType.CHECKPOINT, RoadmapNodeType.BOSS ->
                node.quizId?.let { startQuiz(it) }
            RoadmapNodeType.LEVEL_PREVIEW -> Unit
        }
    }

    fun openTopic(lessonId: String, nodeId: String?, categoryId: String?) =
        push(nav.value.copy(screen = Screen.TOPIC, lessonId = lessonId, nodeId = nodeId, categoryId = categoryId, aiOpen = false))

    fun openLesson() {
        val nodeId = nav.value.nodeId
        if (nodeId != null) viewModelScope.launch { progressRepo.markNodeRead(nodeId) }
        push(nav.value.copy(screen = Screen.LESSON))
    }

    fun openChallenge(challengeId: String) =
        push(nav.value.copy(screen = Screen.CHALLENGE, challengeId = challengeId, aiOpen = false))

    fun toggleAI() = nav.update { it.copy(aiOpen = !it.aiOpen) }
    fun setQuery(q: String) = nav.update { it.copy(query = q) }
    fun setTagFilter(f: String) = nav.update { it.copy(tagFilter = f) }

    // ── Stars / challenges / settings ────────────────────────────────────
    fun toggleStar(lessonId: String) = viewModelScope.launch { progressRepo.toggleStar(lessonId) }

    fun completeChallenge(challengeId: String) {
        val c = content?.challenge(challengeId) ?: return
        viewModelScope.launch { progressRepo.completeChallenge(c.id, c.rewards.xp, c.rewards.stars) }
    }

    fun setGithub(v: Boolean) = viewModelScope.launch { progressRepo.setGithubConnected(v) }
    fun setNotifications(v: Boolean) = viewModelScope.launch { progressRepo.setNotifications(v) }
    fun setSound(v: Boolean) = viewModelScope.launch { progressRepo.setSound(v) }
    fun backupNow() = viewModelScope.launch { progressRepo.setGithubConnected(true) }

    // ── Quiz session ─────────────────────────────────────────────────────
    fun startQuiz(quizId: String) {
        quiz.value = QuizUiState(quizId = quizId)
        push(nav.value.copy(screen = Screen.REVISION, quizId = quizId, aiOpen = false))
    }

    fun setQuizAnswer(questionId: String, answer: UserAnswer) {
        quiz.update { q -> q?.copy(answers = q.answers + (questionId to answer)) }
    }

    fun submitCurrentQuestion() {
        val q = quiz.value ?: return
        val quizDto = content?.quiz(q.quizId) ?: return
        val question = quizDto.questions.getOrNull(q.index) ?: return
        val correct = if (QuizEvaluator.requiresSelfAssessment(question)) null
        else QuizEvaluator.isCorrect(question, q.answerFor(question.id))
        quiz.value = q.copy(phase = QuizPhase.FEEDBACK, lastCorrect = correct)
    }

    /** Records the learner's comparison with the model answer for open-ended prose. */
    fun assessCurrentQuestion(correct: Boolean) {
        val q = quiz.value ?: return
        val quizDto = content?.quiz(q.quizId) ?: return
        val question = quizDto.questions.getOrNull(q.index) ?: return
        if (!QuizEvaluator.requiresSelfAssessment(question) || q.phase != QuizPhase.FEEDBACK) return
        quiz.value = q.copy(
            selfAssessments = q.selfAssessments + (question.id to correct),
            lastCorrect = correct,
        )
    }

    fun nextQuestion() {
        val q = quiz.value ?: return
        val quizDto = content?.quiz(q.quizId) ?: return
        val next = q.index + 1
        if (next < quizDto.questions.size) {
            quiz.value = q.copy(index = next, phase = QuizPhase.QUESTION, lastCorrect = null)
        } else {
            finishQuiz(q, quizDto)
        }
    }

    private fun finishQuiz(q: QuizUiState, quizDto: dev.novanest.droidquest.content.model.QuizDto) {
        val score = QuizEvaluator.score(quizDto, q.answers, q.selfAssessments)
        quiz.value = q.copy(phase = QuizPhase.DONE, score = score)
        val graph = content?.roadmap ?: return
        val nodeId = ProgressionPolicy.nodeCompletedByQuiz(graph, quizDto)
        viewModelScope.launch {
            val recorded = progressRepo.recordQuizResult(
                quizId = quizDto.id,
                nodeIdToComplete = nodeId,
                scoreFraction = score.fraction,
                passingScore = quizDto.passingScore,
                rewardXp = quizDto.rewards.xp,
                maxStars = quizDto.rewards.stars,
            )
            quiz.update { it?.copy(recorded = recorded) }
        }
    }

    fun exitQuiz() {
        quiz.value = null
        back()
    }

    fun retryQuiz() {
        val quizId = quiz.value?.quizId ?: return
        quiz.value = QuizUiState(quizId = quizId)
    }

    // ── Active recall / spaced repetition ───────────────────────────────
    fun rateRecall(lessonId: String, recallItemId: String, rating: ReviewRating) {
        val lesson = content?.lesson(lessonId) ?: return
        val recall = content?.recallItem(recallItemId) ?: return
        if (recall.lesson.id != lesson.id) return
        val next = SpacedRepetitionPolicy.next(
            recallItemId = recallItemId,
            previous = currentProgress.reviewState(recallItemId),
            rating = rating,
            authoredIntervalsDays = lesson.revision.reviewIntervalsDays,
            nowEpochMillis = nowEpochMillis(),
        )
        viewModelScope.launch { progressRepo.saveReviewState(next) }
    }

    fun startDailyReview() {
        val now = nowEpochMillis()
        val dueIds = currentProgress.reviewStates.values
            .asSequence()
            .filter { it.isDue(now) && content?.recallItem(it.recallItemId) != null }
            .sortedBy { it.dueAtEpochMillis }
            .take(20)
            .map { it.recallItemId }
            .toList()
        review.value = ReviewUiState(dueIds)
        push(nav.value.copy(screen = Screen.REVIEW, aiOpen = false))
    }

    fun setReviewAnswer(answer: String) = review.update { it?.copy(answer = answer) }

    fun revealReviewAnswer() = review.update { state ->
        if (state == null || state.answer.isBlank()) state else state.copy(revealed = true)
    }

    fun rateCurrentReview(rating: ReviewRating) {
        val state = review.value ?: return
        if (!state.revealed) return
        val item = content?.recallItem(state.currentRecallItemId) ?: return
        val nextState = SpacedRepetitionPolicy.next(
            recallItemId = item.id,
            previous = currentProgress.reviewState(item.id),
            rating = rating,
            authoredIntervalsDays = item.lesson.revision.reviewIntervalsDays,
            nowEpochMillis = nowEpochMillis(),
        )
        viewModelScope.launch {
            progressRepo.saveReviewState(nextState)
            review.update {
                it?.copy(index = it.index + 1, answer = "", revealed = false, lastRating = rating)
            }
        }
    }

    fun exitReview() {
        review.value = null
        back()
    }

    /** Snapshot of progress for pure/derived UI computations. */
    val currentProgress: LearnerProgress get() = uiState.value.progress

    class Factory(
        private val contentRepo: DroidQuestContentRepository,
        private val progressRepo: ProgressRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DroidQuestViewModel(contentRepo, progressRepo) as T
    }
}

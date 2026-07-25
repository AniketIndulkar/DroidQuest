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
) : ViewModel() {

    private val loadState = MutableStateFlow<ContentLoadState>(ContentLoadState.Loading)
    private val nav = MutableStateFlow(NavState())
    private val quiz = MutableStateFlow<QuizUiState?>(null)

    val uiState: StateFlow<DroidQuestUiState> =
        combine(loadState, progressRepo.progress, nav, quiz) { load, progress, navState, quizState ->
            DroidQuestUiState(load, progress, navState, quizState)
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
        val correct = QuizEvaluator.isCorrect(question, q.answerFor(question.id))
        quiz.value = q.copy(phase = QuizPhase.FEEDBACK, lastCorrect = correct)
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
        val score = QuizEvaluator.score(quizDto, q.answers)
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

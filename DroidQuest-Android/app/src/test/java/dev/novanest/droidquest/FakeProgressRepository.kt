package dev.novanest.droidquest

import dev.novanest.droidquest.domain.RewardPolicy
import dev.novanest.droidquest.domain.ReviewState
import dev.novanest.droidquest.progress.LearnerProgress
import dev.novanest.droidquest.progress.LearnerSettings
import dev.novanest.droidquest.progress.ProgressRepository
import dev.novanest.droidquest.progress.QuizRecordResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** In-memory progress repository mirroring the DataStore idempotency rules, for tests. */
class FakeProgressRepository(initial: LearnerProgress = LearnerProgress()) : ProgressRepository {
    private val state = MutableStateFlow(initial)
    override val progress: StateFlow<LearnerProgress> = state

    val current: LearnerProgress get() = state.value

    override suspend fun toggleStar(lessonId: String) = state.update {
        it.copy(starredLessonIds = if (lessonId in it.starredLessonIds) it.starredLessonIds - lessonId else it.starredLessonIds + lessonId)
    }

    override suspend fun markNodeRead(nodeId: String) = state.update { it.copy(readNodeIds = it.readNodeIds + nodeId) }

    override suspend fun recordQuizResult(
        quizId: String, nodeIdToComplete: String?, scoreFraction: Double, passingScore: Double, rewardXp: Int, maxStars: Int,
    ): QuizRecordResult {
        val cur = state.value
        val already = quizId in cur.passedQuizIds
        val outcome = RewardPolicy.evaluate(scoreFraction, passingScore, rewardXp, maxStars, already)
        val firstPass = outcome.passed && !already
        state.value = cur.copy(
            quizAttempts = cur.quizAttempts + (quizId to (cur.attempts(quizId) + 1)),
            bestQuizScore = cur.bestQuizScore + (quizId to maxOf(cur.bestScore(quizId), scoreFraction)),
            passedQuizIds = if (outcome.passed) cur.passedQuizIds + quizId else cur.passedQuizIds,
            completedNodeIds = if (firstPass && nodeIdToComplete != null) cur.completedNodeIds + nodeIdToComplete else cur.completedNodeIds,
            totalXp = cur.totalXp + outcome.xpAwarded,
            totalStars = cur.totalStars + outcome.starsAwarded,
        )
        return QuizRecordResult(outcome, if (firstPass) nodeIdToComplete else null, firstPass)
    }

    override suspend fun completeChallenge(challengeId: String, rewardXp: Int, rewardStars: Int) = state.update {
        if (challengeId in it.completedChallengeIds) it
        else it.copy(completedChallengeIds = it.completedChallengeIds + challengeId, totalXp = it.totalXp + rewardXp, totalStars = it.totalStars + rewardStars)
    }

    override suspend fun saveReviewState(state: ReviewState) = this.state.update {
        it.copy(reviewStates = it.reviewStates + (state.recallItemId to state))
    }

    override suspend fun setGithubConnected(connected: Boolean) = updateSettings { it.copy(githubConnected = connected) }
    override suspend fun setNotifications(enabled: Boolean) = updateSettings { it.copy(notifications = enabled) }
    override suspend fun setSound(enabled: Boolean) = updateSettings { it.copy(sound = enabled) }

    private fun updateSettings(f: (LearnerSettings) -> LearnerSettings) = state.update { it.copy(settings = f(it.settings)) }
    private inline fun MutableStateFlow<LearnerProgress>.update(f: (LearnerProgress) -> LearnerProgress) { value = f(value) }
}

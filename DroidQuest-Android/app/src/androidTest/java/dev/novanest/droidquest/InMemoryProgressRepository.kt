package dev.novanest.droidquest

import dev.novanest.droidquest.domain.RewardPolicy
import dev.novanest.droidquest.domain.ReviewState
import dev.novanest.droidquest.progress.LearnerProgress
import dev.novanest.droidquest.progress.ProgressRepository
import dev.novanest.droidquest.progress.QuizRecordResult
import kotlinx.coroutines.flow.MutableStateFlow

/** Minimal in-memory progress repository for instrumented render tests. */
class InMemoryProgressRepository : ProgressRepository {
    private val s = MutableStateFlow(LearnerProgress())
    override val progress = s
    override suspend fun toggleStar(lessonId: String) {}
    override suspend fun markNodeRead(nodeId: String) {}
    override suspend fun recordQuizResult(quizId: String, nodeIdToComplete: String?, scoreFraction: Double, passingScore: Double, rewardXp: Int, maxStars: Int): QuizRecordResult {
        val o = RewardPolicy.evaluate(scoreFraction, passingScore, rewardXp, maxStars, false)
        return QuizRecordResult(o, nodeIdToComplete, o.passed)
    }
    override suspend fun completeChallenge(challengeId: String, rewardXp: Int, rewardStars: Int) {}
    override suspend fun saveReviewState(state: ReviewState) {}
    override suspend fun setGithubConnected(connected: Boolean) {}
    override suspend fun setNotifications(enabled: Boolean) {}
    override suspend fun setSound(enabled: Boolean) {}
}

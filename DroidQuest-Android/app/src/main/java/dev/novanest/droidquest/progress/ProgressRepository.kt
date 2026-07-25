package dev.novanest.droidquest.progress

import kotlinx.coroutines.flow.Flow

/**
 * Learner progress persistence boundary. Content stays immutable; everything mutable about a
 * learner lives here, keyed by stable IDs. Reward awarding is idempotent.
 */
interface ProgressRepository {
    val progress: Flow<LearnerProgress>

    suspend fun toggleStar(lessonId: String)
    suspend fun markNodeRead(nodeId: String)

    /**
     * Record a quiz attempt. Rewards (xp/stars) are granted only on the first passing attempt.
     * When [nodeIdToComplete] is non-null and the attempt passes, that roadmap node is marked
     * complete, unlocking downstream nodes.
     */
    suspend fun recordQuizResult(
        quizId: String,
        nodeIdToComplete: String?,
        scoreFraction: Double,
        passingScore: Double,
        rewardXp: Int,
        maxStars: Int,
    ): QuizRecordResult

    /** Complete a challenge. Optional and idempotent — never affects roadmap progression. */
    suspend fun completeChallenge(challengeId: String, rewardXp: Int, rewardStars: Int)

    suspend fun setGithubConnected(connected: Boolean)
    suspend fun setNotifications(enabled: Boolean)
    suspend fun setSound(enabled: Boolean)
}

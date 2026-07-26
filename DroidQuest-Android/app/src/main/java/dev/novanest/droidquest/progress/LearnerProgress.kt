package dev.novanest.droidquest.progress

import dev.novanest.droidquest.domain.RewardOutcome
import dev.novanest.droidquest.domain.ReviewState

/**
 * Immutable snapshot of learner progress, stored entirely separately from curriculum content
 * and keyed only by stable content IDs.
 */
data class LearnerProgress(
    val completedNodeIds: Set<String> = emptySet(),
    val starredLessonIds: Set<String> = emptySet(),
    val completedChallengeIds: Set<String> = emptySet(),
    val passedQuizIds: Set<String> = emptySet(),
    val readNodeIds: Set<String> = emptySet(),
    val bestQuizScore: Map<String, Double> = emptyMap(),
    val quizAttempts: Map<String, Int> = emptyMap(),
    val reviewStates: Map<String, ReviewState> = emptyMap(),
    val totalXp: Int = 0,
    val totalStars: Int = 0,
    val settings: LearnerSettings = LearnerSettings(),
) {
    fun bestScore(quizId: String): Double = bestQuizScore[quizId] ?: 0.0
    fun attempts(quizId: String): Int = quizAttempts[quizId] ?: 0
    fun hasPassed(quizId: String): Boolean = quizId in passedQuizIds
    fun isStarred(lessonId: String): Boolean = lessonId in starredLessonIds
    fun isNodeComplete(nodeId: String): Boolean = nodeId in completedNodeIds
    fun isChallengeComplete(challengeId: String): Boolean = challengeId in completedChallengeIds
    fun reviewState(recallItemId: String): ReviewState? = reviewStates[recallItemId]
    fun reviewsDue(nowEpochMillis: Long): Int = reviewStates.values.count { it.isDue(nowEpochMillis) }
}

/** Streak is intentionally not fabricated: it is not tracked in this release. */
data class LearnerSettings(
    val githubConnected: Boolean = false,
    val notifications: Boolean = true,
    val sound: Boolean = true,
)

/** Result of recording a quiz attempt, so callers can surface freshly awarded rewards. */
data class QuizRecordResult(
    val outcome: RewardOutcome,
    val completedNode: String?,
    val firstPass: Boolean,
)

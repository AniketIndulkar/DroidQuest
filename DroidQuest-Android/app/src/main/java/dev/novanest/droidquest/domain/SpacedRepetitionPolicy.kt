package dev.novanest.droidquest.domain

import kotlin.math.max

enum class ReviewRating { AGAIN, HARD, GOOD, EASY }

/** Mutable learner memory state for one immutable recall item. */
data class ReviewState(
    val recallItemId: String,
    val dueAtEpochMillis: Long,
    val intervalDays: Int,
    val repetitions: Int,
    val lapses: Int,
    val lastReviewedAtEpochMillis: Long,
    val lastRating: ReviewRating,
) {
    fun isDue(nowEpochMillis: Long): Boolean = dueAtEpochMillis <= nowEpochMillis
}

/**
 * Small, transparent first scheduler. Authored intervals seed successful reviews; the policy can
 * later be replaced by FSRS without changing recall IDs or the repository contract.
 */
object SpacedRepetitionPolicy {
    private const val MINUTE_MILLIS = 60_000L
    private const val DAY_MILLIS = 86_400_000L
    private const val MAX_INTERVAL_DAYS = 365

    fun next(
        recallItemId: String,
        previous: ReviewState?,
        rating: ReviewRating,
        authoredIntervalsDays: List<Int>,
        nowEpochMillis: Long,
    ): ReviewState {
        val intervals = authoredIntervalsDays.filter { it > 0 }.distinct().sorted().ifEmpty { listOf(1, 7, 21) }
        val current = previous?.intervalDays ?: 0
        val intervalDays = when (rating) {
            ReviewRating.AGAIN -> 0
            ReviewRating.HARD -> if (current <= 1) 1 else max(1, current / 2)
            ReviewRating.GOOD -> nextSuccessfulInterval(current, intervals, skip = 0)
            ReviewRating.EASY -> nextSuccessfulInterval(current, intervals, skip = 1)
        }.coerceAtMost(MAX_INTERVAL_DAYS)
        val dueAt = if (rating == ReviewRating.AGAIN) nowEpochMillis + 10 * MINUTE_MILLIS
        else nowEpochMillis + intervalDays * DAY_MILLIS

        return ReviewState(
            recallItemId = recallItemId,
            dueAtEpochMillis = dueAt,
            intervalDays = intervalDays,
            repetitions = (previous?.repetitions ?: 0) + if (rating == ReviewRating.AGAIN) 0 else 1,
            lapses = (previous?.lapses ?: 0) + if (rating == ReviewRating.AGAIN && previous != null) 1 else 0,
            lastReviewedAtEpochMillis = nowEpochMillis,
            lastRating = rating,
        )
    }

    private fun nextSuccessfulInterval(current: Int, intervals: List<Int>, skip: Int): Int {
        val nextIndex = intervals.indexOfFirst { it > current }
        if (nextIndex >= 0) return intervals[(nextIndex + skip).coerceAtMost(intervals.lastIndex)]
        val multiplier = if (skip == 0) 2 else 3
        return max(intervals.last(), max(1, current) * multiplier)
    }
}


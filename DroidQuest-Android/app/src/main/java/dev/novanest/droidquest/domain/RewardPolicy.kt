package dev.novanest.droidquest.domain

import kotlin.math.ceil
import kotlin.math.max

/** Outcome of grading a quiz attempt, including what should be persisted this time. */
data class RewardOutcome(
    val passed: Boolean,
    /** Stars this attempt represents (for display), 0 when not passed. */
    val starsEarned: Int,
    /** XP to actually add now — non-zero only on the first passing attempt (idempotent). */
    val xpAwarded: Int,
    /** Stars to actually add now — non-zero only on the first passing attempt (idempotent). */
    val starsAwarded: Int,
)

/**
 * Reward calculation, isolated so the score-to-star mapping can change later without a data
 * migration. Awarding is idempotent: a quiz's declared rewards are granted only on its first
 * passing completion.
 */
object RewardPolicy {

    /**
     * Stars for a score. 0 when below the passing threshold; otherwise scales with the score
     * up to the quiz's declared maximum, with at least one star for any pass.
     */
    fun starsFor(scoreFraction: Double, maxStars: Int, passingScore: Double): Int {
        if (scoreFraction < passingScore) return 0
        val scaled = ceil(scoreFraction * maxStars).toInt()
        return max(1, minOf(scaled, maxStars))
    }

    /**
     * @param alreadyPassed whether this quiz was passed before (so rewards were already given).
     */
    fun evaluate(
        scoreFraction: Double,
        passingScore: Double,
        rewardXp: Int,
        maxStars: Int,
        alreadyPassed: Boolean,
    ): RewardOutcome {
        val passed = scoreFraction >= passingScore
        val stars = starsFor(scoreFraction, maxStars, passingScore)
        val firstPass = passed && !alreadyPassed
        return RewardOutcome(
            passed = passed,
            starsEarned = stars,
            xpAwarded = if (firstPass) rewardXp else 0,
            starsAwarded = if (firstPass) stars else 0,
        )
    }
}

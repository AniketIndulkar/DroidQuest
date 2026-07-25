package dev.novanest.droidquest

import dev.novanest.droidquest.domain.RewardPolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class RewardPolicyTest {

    @Test
    fun no_reward_below_passing_score() {
        val o = RewardPolicy.evaluate(scoreFraction = 0.5, passingScore = 0.8, rewardXp = 100, maxStars = 5, alreadyPassed = false)
        assertEquals(false, o.passed)
        assertEquals(0, o.xpAwarded)
        assertEquals(0, o.starsAwarded)
        assertEquals(0, o.starsEarned)
    }

    @Test
    fun first_pass_awards_declared_rewards() {
        val o = RewardPolicy.evaluate(1.0, 0.8, 100, 5, alreadyPassed = false)
        assertEquals(true, o.passed)
        assertEquals(100, o.xpAwarded)
        assertEquals(5, o.starsAwarded)
    }

    @Test
    fun rewards_are_idempotent_on_repeat_pass() {
        val o = RewardPolicy.evaluate(1.0, 0.8, 100, 5, alreadyPassed = true)
        assertEquals(true, o.passed)
        assertEquals(0, o.xpAwarded)
        assertEquals(0, o.starsAwarded)
        // Display value still reflects the score.
        assertEquals(5, o.starsEarned)
    }

    @Test
    fun stars_scale_with_score_and_never_below_one_when_passed() {
        assertEquals(0, RewardPolicy.starsFor(0.79, 5, 0.8))
        assertEquals(4, RewardPolicy.starsFor(0.8, 5, 0.8))
        assertEquals(5, RewardPolicy.starsFor(1.0, 5, 0.8))
        assertEquals(1, RewardPolicy.starsFor(0.8, 1, 0.8))
    }
}

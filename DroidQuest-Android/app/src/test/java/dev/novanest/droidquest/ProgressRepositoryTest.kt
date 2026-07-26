package dev.novanest.droidquest

import dev.novanest.droidquest.domain.ReviewRating
import dev.novanest.droidquest.domain.ReviewState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Persistence behaviour + idempotent rewards using the in-memory fake. */
class ProgressRepositoryTest {

    @Test
    fun toggle_star_persists_and_clears() = runTest {
        val repo = FakeProgressRepository()
        repo.toggleStar("lesson-a")
        assertTrue(repo.current.isStarred("lesson-a"))
        repo.toggleStar("lesson-a")
        assertFalse(repo.current.isStarred("lesson-a"))
    }

    @Test
    fun quiz_reward_awarded_once_and_node_completed_once() = runTest {
        val repo = FakeProgressRepository()
        val first = repo.recordQuizResult("quiz-x", "node-x", scoreFraction = 1.0, passingScore = 0.8, rewardXp = 100, maxStars = 5)
        assertTrue(first.firstPass)
        assertEquals(100, repo.current.totalXp)
        assertTrue(repo.current.isNodeComplete("node-x"))

        // A second passing attempt must not double-award.
        val second = repo.recordQuizResult("quiz-x", "node-x", scoreFraction = 1.0, passingScore = 0.8, rewardXp = 100, maxStars = 5)
        assertFalse(second.firstPass)
        assertEquals(100, repo.current.totalXp)
        assertEquals(2, repo.current.attempts("quiz-x"))
    }

    @Test
    fun failing_attempt_records_but_grants_nothing() = runTest {
        val repo = FakeProgressRepository()
        val r = repo.recordQuizResult("quiz-y", "node-y", scoreFraction = 0.3, passingScore = 0.8, rewardXp = 50, maxStars = 3)
        assertFalse(r.outcome.passed)
        assertEquals(0, repo.current.totalXp)
        assertFalse(repo.current.isNodeComplete("node-y"))
        assertEquals(1, repo.current.attempts("quiz-y"))
    }

    @Test
    fun challenge_completion_is_idempotent() = runTest {
        val repo = FakeProgressRepository()
        repo.completeChallenge("chal-1", 40, 2)
        repo.completeChallenge("chal-1", 40, 2)
        assertEquals(40, repo.current.totalXp)
        assertTrue(repo.current.isChallengeComplete("chal-1"))
    }

    @Test
    fun review_state_is_upserted_by_stable_recall_id() = runTest {
        val repo = FakeProgressRepository()
        val state = ReviewState("lesson-a-recall-1", 1234L, 1, 1, 0, 1000L, ReviewRating.GOOD)

        repo.saveReviewState(state)

        assertEquals(state, repo.current.reviewState("lesson-a-recall-1"))
    }
}

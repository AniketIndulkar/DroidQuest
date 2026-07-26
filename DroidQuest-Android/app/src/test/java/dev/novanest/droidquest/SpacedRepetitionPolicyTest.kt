package dev.novanest.droidquest

import dev.novanest.droidquest.domain.ReviewRating
import dev.novanest.droidquest.domain.SpacedRepetitionPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpacedRepetitionPolicyTest {
    private val now = 1_000_000_000L
    private val intervals = listOf(1, 7, 21)

    @Test
    fun good_reviews_advance_through_authored_intervals() {
        val first = SpacedRepetitionPolicy.next("recall-a", null, ReviewRating.GOOD, intervals, now)
        val second = SpacedRepetitionPolicy.next("recall-a", first, ReviewRating.GOOD, intervals, first.dueAtEpochMillis)

        assertEquals(1, first.intervalDays)
        assertEquals(7, second.intervalDays)
        assertEquals(2, second.repetitions)
    }

    @Test
    fun easy_initial_review_skips_to_second_interval() {
        val state = SpacedRepetitionPolicy.next("recall-a", null, ReviewRating.EASY, intervals, now)

        assertEquals(7, state.intervalDays)
    }

    @Test
    fun again_is_due_in_ten_minutes_and_counts_a_lapse_after_learning() {
        val learned = SpacedRepetitionPolicy.next("recall-a", null, ReviewRating.GOOD, intervals, now)
        val again = SpacedRepetitionPolicy.next("recall-a", learned, ReviewRating.AGAIN, intervals, now)

        assertEquals(now + 10 * 60_000L, again.dueAtEpochMillis)
        assertEquals(1, again.lapses)
        assertTrue(!again.isDue(now))
    }
}


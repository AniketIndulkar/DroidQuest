package dev.novanest.droidquest

import dev.novanest.droidquest.widget.LearningBite
import dev.novanest.droidquest.widget.LearningBiteMemory
import dev.novanest.droidquest.widget.LearningBitePolicy
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningBitePolicyTest {
    private val now = 1_000_000_000L
    private val factA = LearningBite.Fact("fact-a", "topic-a", "A", "Fact A", "Cue A")
    private val factB = LearningBite.Fact("fact-b", "topic-b", "B", "Fact B", "Cue B")
    private val questionA = LearningBite.Question(
        "question-a", "topic-a", "A", "Question A?", listOf("Yes", "No"), 0, "Because A.",
    )
    private val questionB = LearningBite.Question(
        "question-b", "topic-b", "B", "Question B?", listOf("Yes", "No"), 1, "Because B.",
    )

    @Test
    fun cadence_uses_two_questions_for_each_fact() {
        val items = listOf(factA, questionA)

        assertTrue(LearningBitePolicy.choose(items, emptyMap(), emptyList(), emptyList(), 0, now, Random(1)) is LearningBite.Fact)
        assertTrue(LearningBitePolicy.choose(items, emptyMap(), emptyList(), emptyList(), 1, now, Random(1)) is LearningBite.Question)
        assertTrue(LearningBitePolicy.choose(items, emptyMap(), emptyList(), emptyList(), 2, now, Random(1)) is LearningBite.Question)
    }

    @Test
    fun due_retrieval_beats_unseen_novelty() {
        val shown = LearningBitePolicy.afterShown(questionA, null, now - 2 * DAY)

        val selected = LearningBitePolicy.choose(
            listOf(questionA, questionB), mapOf(questionA.id to shown), emptyList(), emptyList(), 1, now, Random(1),
        )

        assertEquals(questionA.id, selected?.id)
    }

    @Test
    fun recent_topic_is_interleaved_when_an_alternative_exists() {
        val selected = LearningBitePolicy.choose(
            listOf(factA, factB), emptyMap(), emptyList(), listOf("topic-a"), 0, now, Random(1),
        )

        assertEquals(factB.id, selected?.id)
    }

    @Test
    fun a_miss_returns_in_ten_minutes() {
        val shown = LearningBitePolicy.afterShown(questionA, null, now)
        val missed = LearningBitePolicy.afterAnswer(shown, correct = false, nowEpochMillis = now)

        assertEquals(now + 10 * 60_000L, missed.dueAtEpochMillis)
        assertEquals(1, missed.wrongAnswers)
        assertEquals(0, missed.intervalStage)
    }

    @Test
    fun successful_retrieval_expands_from_one_to_three_days() {
        val shown = LearningBitePolicy.afterShown(questionA, null, now)
        val first = LearningBitePolicy.afterAnswer(shown, correct = true, nowEpochMillis = now)
        val second = LearningBitePolicy.afterAnswer(first, correct = true, nowEpochMillis = now)

        assertEquals(now + DAY, first.dueAtEpochMillis)
        assertEquals(now + 3 * DAY, second.dueAtEpochMillis)
        assertEquals(2, second.correctAnswers)
    }

    @Test
    fun real_curriculum_produces_facts_and_mcqs() {
        val items = dev.novanest.droidquest.widget.LearningBiteCatalog.from(TestContent.loaded())

        assertTrue(items.any { it is LearningBite.Fact })
        assertTrue(items.any { it is LearningBite.Question })
        assertTrue(items.filterIsInstance<LearningBite.Question>().all { it.options.size in 2..4 })
        assertEquals(items.size, items.map { it.id }.distinct().size)
    }

    private companion object {
        const val DAY = 86_400_000L
    }
}

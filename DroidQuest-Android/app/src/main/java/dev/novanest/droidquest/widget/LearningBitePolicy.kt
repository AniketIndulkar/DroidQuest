package dev.novanest.droidquest.widget

import kotlin.random.Random

/**
 * Selection and repetition rules for low-friction, interleaved retrieval practice.
 *
 * The widget favours due material over novelty, alternates topics when possible, and uses a
 * two-question/one-fact cadence. A miss returns quickly; successful retrievals expand outwards.
 */
object LearningBitePolicy {
    private const val MINUTE_MILLIS = 60_000L
    private const val DAY_MILLIS = 86_400_000L
    private val SUCCESS_INTERVAL_DAYS = intArrayOf(1, 3, 7, 21, 60)

    fun choose(
        items: List<LearningBite>,
        memories: Map<String, LearningBiteMemory>,
        recentItemIds: List<String>,
        recentTopicIds: List<String>,
        sequence: Int,
        nowEpochMillis: Long,
        random: Random = Random.Default,
    ): LearningBite? {
        if (items.isEmpty()) return null

        val due = items.filter { item ->
            memories[item.id]?.dueAtEpochMillis?.let { it <= nowEpochMillis } == true
        }
        val unseen = items.filter { it.id !in memories }
        val eligible = when {
            due.isNotEmpty() -> due
            unseen.isNotEmpty() -> unseen
            else -> items
        }

        // Facts orient the learner; questions do the memory work. Sequence 0 starts gently.
        val wantsQuestion = sequence % 3 != 0
        val matchingKind = eligible.filter {
            (it is LearningBite.Question) == wantsQuestion
        }
        var candidates = matchingKind.ifEmpty { eligible }

        val withoutRecentTopics = candidates.filter { it.topicId !in recentTopicIds }
        if (withoutRecentTopics.isNotEmpty()) candidates = withoutRecentTopics

        val withoutRecentItems = candidates.filter { it.id !in recentItemIds }
        if (withoutRecentItems.isNotEmpty()) candidates = withoutRecentItems

        return candidates[random.nextInt(candidates.size)]
    }

    fun afterShown(
        item: LearningBite,
        previous: LearningBiteMemory?,
        nowEpochMillis: Long,
    ): LearningBiteMemory {
        val defaultDelayDays = if (item is LearningBite.Fact) 3 else 1
        return LearningBiteMemory(
            itemId = item.id,
            topicId = item.topicId,
            dueAtEpochMillis = previous?.dueAtEpochMillis ?: nowEpochMillis + defaultDelayDays * DAY_MILLIS,
            intervalStage = previous?.intervalStage ?: 0,
            timesShown = (previous?.timesShown ?: 0) + 1,
            correctAnswers = previous?.correctAnswers ?: 0,
            wrongAnswers = previous?.wrongAnswers ?: 0,
            lastShownAtEpochMillis = nowEpochMillis,
        )
    }

    fun afterAnswer(
        previous: LearningBiteMemory,
        correct: Boolean,
        nowEpochMillis: Long,
    ): LearningBiteMemory {
        if (!correct) {
            return previous.copy(
                dueAtEpochMillis = nowEpochMillis + 10 * MINUTE_MILLIS,
                intervalStage = 0,
                wrongAnswers = previous.wrongAnswers + 1,
            )
        }

        val nextStage = (previous.intervalStage + 1).coerceAtMost(SUCCESS_INTERVAL_DAYS.size)
        val intervalDays = SUCCESS_INTERVAL_DAYS[nextStage - 1]
        return previous.copy(
            dueAtEpochMillis = nowEpochMillis + intervalDays * DAY_MILLIS,
            intervalStage = nextStage,
            correctAnswers = previous.correctAnswers + 1,
        )
    }
}

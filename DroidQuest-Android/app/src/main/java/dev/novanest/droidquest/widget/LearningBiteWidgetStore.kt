package dev.novanest.droidquest.widget

import android.content.Context
import androidx.core.content.edit

/** Lightweight widget-only state. Curriculum and the app's canonical learner progress stay separate. */
class LearningBiteWidgetStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun memories(): Map<String, LearningBiteMemory> = preferences.all.entries
        .asSequence()
        .filter { it.key.startsWith(MEMORY_PREFIX) }
        .mapNotNull { (key, value) ->
            val encoded = value as? String ?: return@mapNotNull null
            decodeMemory(key.removePrefix(MEMORY_PREFIX), encoded)?.let { it.itemId to it }
        }
        .toMap()

    fun currentItemId(appWidgetId: Int): String? = preferences.getString(currentKey(appWidgetId), null)

    fun answerIndex(appWidgetId: Int): Int = preferences.getInt(answerKey(appWidgetId), NO_ANSWER)

    fun selectNext(appWidgetId: Int, items: List<LearningBite>, nowEpochMillis: Long): LearningBite? = synchronized(LOCK) {
        val sequence = preferences.getInt(SEQUENCE_KEY, 0)
        val currentMemories = memories()
        val item = LearningBitePolicy.choose(
            items = items,
            memories = currentMemories,
            recentItemIds = recentItems(),
            recentTopicIds = recentTopics(),
            sequence = sequence,
            nowEpochMillis = nowEpochMillis,
        ) ?: return@synchronized null

        val memory = LearningBitePolicy.afterShown(item, currentMemories[item.id], nowEpochMillis)
        preferences.edit(commit = true) {
            putString(currentKey(appWidgetId), item.id)
            putInt(answerKey(appWidgetId), NO_ANSWER)
            putInt(SEQUENCE_KEY, sequence + 1)
            putString(RECENT_ITEMS_KEY, prepend(item.id, recentItems(), RECENT_ITEM_LIMIT).joinToString(SEPARATOR))
            putString(RECENT_TOPICS_KEY, prepend(item.topicId, recentTopics(), RECENT_TOPIC_LIMIT).joinToString(SEPARATOR))
            putString(MEMORY_PREFIX + item.id, encodeMemory(memory))
        }
        item
    }

    fun recordAnswer(
        appWidgetId: Int,
        question: LearningBite.Question,
        selectedIndex: Int,
        nowEpochMillis: Long,
    ): Boolean = synchronized(LOCK) {
        if (currentItemId(appWidgetId) != question.id || answerIndex(appWidgetId) != NO_ANSWER) {
            return@synchronized false
        }
        if (selectedIndex !in question.options.indices) return@synchronized false
        val previous = memories()[question.id] ?: LearningBitePolicy.afterShown(question, null, nowEpochMillis)
        val updated = LearningBitePolicy.afterAnswer(
            previous = previous,
            correct = selectedIndex == question.correctOptionIndex,
            nowEpochMillis = nowEpochMillis,
        )
        preferences.edit(commit = true) {
            putInt(answerKey(appWidgetId), selectedIndex)
            putString(MEMORY_PREFIX + question.id, encodeMemory(updated))
        }
        true
    }

    fun deleteWidget(appWidgetId: Int) {
        preferences.edit {
            remove(currentKey(appWidgetId))
            remove(answerKey(appWidgetId))
        }
    }

    private fun recentItems(): List<String> = readList(RECENT_ITEMS_KEY)
    private fun recentTopics(): List<String> = readList(RECENT_TOPICS_KEY)

    private fun readList(key: String): List<String> = preferences.getString(key, null)
        ?.split(SEPARATOR)
        ?.filter(String::isNotBlank)
        .orEmpty()

    private fun prepend(value: String, current: List<String>, limit: Int): List<String> =
        (listOf(value) + current.filterNot { it == value }).take(limit)

    private fun encodeMemory(memory: LearningBiteMemory): String = listOf(
        memory.topicId,
        memory.dueAtEpochMillis,
        memory.intervalStage,
        memory.timesShown,
        memory.correctAnswers,
        memory.wrongAnswers,
        memory.lastShownAtEpochMillis,
    ).joinToString(SEPARATOR)

    private fun decodeMemory(itemId: String, encoded: String): LearningBiteMemory? = runCatching {
        val parts = encoded.split(SEPARATOR)
        require(parts.size == 7)
        LearningBiteMemory(
            itemId = itemId,
            topicId = parts[0],
            dueAtEpochMillis = parts[1].toLong(),
            intervalStage = parts[2].toInt(),
            timesShown = parts[3].toInt(),
            correctAnswers = parts[4].toInt(),
            wrongAnswers = parts[5].toInt(),
            lastShownAtEpochMillis = parts[6].toLong(),
        )
    }.getOrNull()

    private fun currentKey(appWidgetId: Int) = "current_$appWidgetId"
    private fun answerKey(appWidgetId: Int) = "answer_$appWidgetId"

    companion object {
        const val NO_ANSWER = -1
        private val LOCK = Any()
        private const val PREFERENCES_NAME = "learning_bite_widget"
        private const val MEMORY_PREFIX = "memory_"
        private const val SEQUENCE_KEY = "sequence"
        private const val RECENT_ITEMS_KEY = "recent_items"
        private const val RECENT_TOPICS_KEY = "recent_topics"
        private const val SEPARATOR = "|"
        private const val RECENT_ITEM_LIMIT = 8
        private const val RECENT_TOPIC_LIMIT = 3
    }
}

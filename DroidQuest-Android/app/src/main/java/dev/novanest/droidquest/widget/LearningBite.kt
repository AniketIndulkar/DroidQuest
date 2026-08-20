package dev.novanest.droidquest.widget

/** Small, self-contained learning units that fit a home-screen widget. */
sealed interface LearningBite {
    val id: String
    val topicId: String
    val topicTitle: String

    data class Fact(
        override val id: String,
        override val topicId: String,
        override val topicTitle: String,
        val text: String,
        val recallCue: String,
    ) : LearningBite

    data class Question(
        override val id: String,
        override val topicId: String,
        override val topicTitle: String,
        val prompt: String,
        val options: List<String>,
        val correctOptionIndex: Int,
        val explanation: String,
    ) : LearningBite {
        val correctOption: String get() = options[correctOptionIndex]
    }
}

data class LearningBiteMemory(
    val itemId: String,
    val topicId: String,
    val dueAtEpochMillis: Long,
    val intervalStage: Int,
    val timesShown: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val lastShownAtEpochMillis: Long,
)

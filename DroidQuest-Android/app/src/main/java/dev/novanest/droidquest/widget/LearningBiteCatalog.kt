package dev.novanest.droidquest.widget

import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.content.model.QuestionType
import dev.novanest.droidquest.domain.QuizEvaluator
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive

/** Adapts the verified offline curriculum into content suitable for RemoteViews. */
object LearningBiteCatalog {
    fun from(content: LoadedContent): List<LearningBite> = buildList {
        content.lessonsById.values.sortedBy { it.id }.forEach { lesson ->
            lesson.revealStages.recall.forEachIndexed { index, recall ->
                if (recall.answer.length <= MAX_BODY_LENGTH && recall.prompt.length <= MAX_PROMPT_LENGTH) {
                    add(
                        LearningBite.Fact(
                            id = "fact:${recall.id.ifBlank { "${lesson.id}:$index" }}",
                            topicId = lesson.id,
                            topicTitle = lesson.title,
                            text = recall.answer,
                            recallCue = recall.prompt,
                        ),
                    )
                }
            }
        }

        content.quizzesById.values.sortedBy { it.id }.flatMap { it.questions }.forEach { question ->
            val lesson = content.lessonsById[question.lessonId] ?: return@forEach
            val options: List<String>
            val correctIndex: Int
            when (question.type) {
                QuestionType.SINGLE_CHOICE -> {
                    options = runCatching { QuizEvaluator.optionLabels(question) }.getOrNull() ?: return@forEach
                    val answer = runCatching { question.answer.jsonPrimitive.content }.getOrNull() ?: return@forEach
                    correctIndex = options.indexOfFirst {
                        QuizEvaluator.normalize(it) == QuizEvaluator.normalize(answer)
                    }
                }
                QuestionType.TRUE_FALSE -> {
                    options = listOf("True", "False")
                    val answer = runCatching { question.answer.jsonPrimitive.booleanOrNull }.getOrNull()
                        ?: return@forEach
                    correctIndex = if (answer) 0 else 1
                }
                else -> return@forEach
            }

            if (
                correctIndex >= 0 &&
                options.size in 2..MAX_OPTIONS &&
                options.distinctBy(QuizEvaluator::normalize).size == options.size &&
                options.all { it.length <= MAX_OPTION_LENGTH } &&
                question.prompt.length <= MAX_PROMPT_LENGTH &&
                question.explanation.length <= MAX_BODY_LENGTH
            ) {
                add(
                    LearningBite.Question(
                        id = "question:${question.id}",
                        topicId = lesson.id,
                        topicTitle = lesson.title,
                        prompt = question.prompt,
                        options = options,
                        correctOptionIndex = correctIndex,
                        explanation = question.explanation,
                    ),
                )
            }
        }
    }

    private const val MAX_OPTIONS = 4
    private const val MAX_PROMPT_LENGTH = 220
    private const val MAX_OPTION_LENGTH = 72
    private const val MAX_BODY_LENGTH = 220
}

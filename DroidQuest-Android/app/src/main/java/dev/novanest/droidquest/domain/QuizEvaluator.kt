package dev.novanest.droidquest.domain

import dev.novanest.droidquest.content.model.QuestionDto
import dev.novanest.droidquest.content.model.QuestionType
import dev.novanest.droidquest.content.model.QuizDto
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Typed learner answer at the grading boundary — never a raw String for every type. */
sealed interface UserAnswer {
    /** single_choice, fill_blank, code_output, short_answer, spot_bug */
    data class Text(val value: String) : UserAnswer
    /** true_false */
    data class Bool(val value: Boolean) : UserAnswer
    /** multiple_choice (unordered set) or order_steps (ordered sequence) */
    data class Choices(val values: List<String>) : UserAnswer
    /** match_pairs: left key -> chosen right value */
    data class Pairs(val map: Map<String, String>) : UserAnswer
    data object None : UserAnswer
}

data class QuizScore(val correct: Int, val total: Int, val fraction: Double, val passed: Boolean)

/**
 * Pure quiz grading for all nine question types. No content mutation, no correct answers
 * exposed. All comparisons go through [normalize] so trivial casing/whitespace differences
 * do not fail an otherwise correct answer.
 */
object QuizEvaluator {

    fun normalize(s: String): String =
        s.trim().lowercase().replace(WHITESPACE, " ")

    private val WHITESPACE = Regex("\\s+")

    private fun JsonElement.asText(): String = jsonPrimitive.content
    private fun JsonElement.asBool(): Boolean = jsonPrimitive.content.trim().equals("true", ignoreCase = true)
    private fun JsonArray.texts(): List<String> = map { it.jsonPrimitive.content }
    private fun JsonObject.textMap(): Map<String, String> = entries.associate { (k, v) -> k to v.jsonPrimitive.content }

    /** Option labels a question presents (already strings in the content). */
    fun optionLabels(q: QuestionDto): List<String> = q.options?.map { it.jsonPrimitive.content } ?: emptyList()

    /** For match_pairs, the left keys and the pool of right values to choose from. */
    fun matchLefts(q: QuestionDto): List<String> = q.answer.jsonObject.keys.toList()
    fun matchRights(q: QuestionDto): List<String> = q.answer.jsonObject.textMap().values.toList()
    /** For order_steps, the correct sequence (used to derive the shuffled pool). */
    fun orderedSteps(q: QuestionDto): List<String> =
        if (q.options != null) optionLabels(q) else q.answer.jsonArray.texts()

    fun isCorrect(q: QuestionDto, answer: UserAnswer): Boolean = when (q.type) {
        QuestionType.SINGLE_CHOICE ->
            answer is UserAnswer.Text && normalize(answer.value) == normalize(q.answer.asText())

        QuestionType.MULTIPLE_CHOICE -> {
            val expected = q.answer.jsonArray.texts().map(::normalize).toSet()
            answer is UserAnswer.Choices && answer.values.map(::normalize).toSet() == expected
        }

        QuestionType.TRUE_FALSE ->
            answer is UserAnswer.Bool && answer.value == q.answer.asBool()

        QuestionType.ORDER_STEPS -> {
            val expected = q.answer.jsonArray.texts().map(::normalize)
            answer is UserAnswer.Choices && answer.values.map(::normalize) == expected
        }

        QuestionType.MATCH_PAIRS -> {
            val expected = q.answer.jsonObject.textMap()
            answer is UserAnswer.Pairs &&
                expected.all { (k, v) -> answer.map[k]?.let { normalize(it) == normalize(v) } == true } &&
                answer.map.keys.containsAll(expected.keys)
        }

        QuestionType.FILL_BLANK, QuestionType.SHORT_ANSWER, QuestionType.SPOT_BUG, QuestionType.CODE_OUTPUT -> {
            val accepted = buildSet {
                add(normalize(q.answer.asText()))
                q.acceptedAnswers?.forEach { add(normalize(it)) }
            }
            answer is UserAnswer.Text && normalize(answer.value) in accepted
        }
    }

    fun score(quiz: QuizDto, answers: Map<String, UserAnswer>): QuizScore {
        val total = quiz.questions.size
        val correct = quiz.questions.count { isCorrect(it, answers[it.id] ?: UserAnswer.None) }
        val fraction = if (total == 0) 0.0 else correct.toDouble() / total
        return QuizScore(correct, total, fraction, fraction >= quiz.passingScore)
    }
}

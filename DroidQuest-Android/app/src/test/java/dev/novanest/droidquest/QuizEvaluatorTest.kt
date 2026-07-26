package dev.novanest.droidquest

import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.content.model.QuestionDto
import dev.novanest.droidquest.content.model.QuestionType
import dev.novanest.droidquest.domain.QuizEvaluator
import dev.novanest.droidquest.domain.UserAnswer
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Exhaustive evaluator coverage across all nine question types, using real content. */
class QuizEvaluatorTest {

    private val content: LoadedContent = TestContent.loaded()
    private val allQuestions = content.quizzesById.values.flatMap { it.questions }

    private fun firstOfType(type: QuestionType): QuestionDto =
        allQuestions.first { it.type == type }

    private fun correctAnswer(q: QuestionDto): UserAnswer = when (q.type) {
        QuestionType.SINGLE_CHOICE -> UserAnswer.Text(q.answer.jsonPrimitive.content)
        QuestionType.MULTIPLE_CHOICE -> UserAnswer.Choices(q.answer.jsonArray.map { it.jsonPrimitive.content })
        QuestionType.TRUE_FALSE -> UserAnswer.Bool(q.answer.jsonPrimitive.content.toBoolean())
        QuestionType.ORDER_STEPS -> UserAnswer.Choices(q.answer.jsonArray.map { it.jsonPrimitive.content })
        QuestionType.MATCH_PAIRS -> UserAnswer.Pairs(q.answer.jsonObject.mapValues { it.value.jsonPrimitive.content })
        QuestionType.FILL_BLANK, QuestionType.SHORT_ANSWER, QuestionType.SPOT_BUG, QuestionType.CODE_OUTPUT ->
            UserAnswer.Text(q.answer.jsonPrimitive.content)
    }

    @Test
    fun every_type_grades_its_correct_answer_true() {
        QuestionType.entries.forEach { type ->
            val q = firstOfType(type)
            assertTrue("correct answer failed for $type", QuizEvaluator.isCorrect(q, correctAnswer(q)))
        }
    }

    @Test
    fun single_choice_wrong_option_is_incorrect() {
        val q = firstOfType(QuestionType.SINGLE_CHOICE)
        val wrong = QuizEvaluator.optionLabels(q).first { it != q.answer.jsonPrimitive.content }
        assertFalse(QuizEvaluator.isCorrect(q, UserAnswer.Text(wrong)))
    }

    @Test
    fun multiple_choice_partial_selection_is_incorrect() {
        val q = firstOfType(QuestionType.MULTIPLE_CHOICE)
        val correct = q.answer.jsonArray.map { it.jsonPrimitive.content }
        assertFalse(QuizEvaluator.isCorrect(q, UserAnswer.Choices(correct.dropLast(1))))
    }

    @Test
    fun order_steps_reversed_is_incorrect() {
        val q = firstOfType(QuestionType.ORDER_STEPS)
        val reversed = q.answer.jsonArray.map { it.jsonPrimitive.content }.reversed()
        assertFalse(QuizEvaluator.isCorrect(q, UserAnswer.Choices(reversed)))
    }

    @Test
    fun match_pairs_swapped_is_incorrect() {
        val q = firstOfType(QuestionType.MATCH_PAIRS)
        val map = q.answer.jsonObject.mapValues { it.value.jsonPrimitive.content }.toMutableMap()
        val keys = map.keys.toList()
        val tmp = map[keys[0]]!!; map[keys[0]] = map[keys[1]]!!; map[keys[1]] = tmp
        assertFalse(QuizEvaluator.isCorrect(q, UserAnswer.Pairs(map)))
    }

    @Test
    fun true_false_flipped_is_incorrect() {
        val q = firstOfType(QuestionType.TRUE_FALSE)
        val expected = q.answer.jsonPrimitive.content.toBoolean()
        assertFalse(QuizEvaluator.isCorrect(q, UserAnswer.Bool(!expected)))
    }

    @Test
    fun fill_blank_is_case_and_space_insensitive() {
        val q = firstOfType(QuestionType.FILL_BLANK)
        val messy = "  ${q.answer.jsonPrimitive.content.uppercase()}  "
        assertTrue(QuizEvaluator.isCorrect(q, UserAnswer.Text(messy)))
    }

    @Test
    fun open_ended_questions_use_learner_self_assessment_when_scored() {
        val question = firstOfType(QuestionType.SHORT_ANSWER)
        val quiz = content.quizzesById.values.first { question in it.questions }
        val answers = quiz.questions.associate { it.id to correctAnswer(it) }
        val assessments = quiz.questions.filter(QuizEvaluator::requiresSelfAssessment).associate { it.id to true }

        val score = QuizEvaluator.score(quiz, answers, assessments)

        assertEquals(quiz.questions.size, score.correct)
    }

    @Test
    fun model_answer_preserves_multiline_code_output() {
        val question = allQuestions.first {
            it.type == QuestionType.CODE_OUTPUT && it.answer.jsonPrimitive.content.contains('\n')
        }

        assertEquals(question.answer.jsonPrimitive.content, QuizEvaluator.modelAnswer(question))
    }

    @Test
    fun scoring_uses_passing_score() {
        val quiz = content.quizzesById.values.first()
        val allCorrect = quiz.questions.associate { it.id to correctAnswer(it) }
        val assessments = quiz.questions.filter(QuizEvaluator::requiresSelfAssessment).associate { it.id to true }
        val perfect = QuizEvaluator.score(quiz, allCorrect, assessments)
        assertEquals(quiz.questions.size, perfect.correct)
        assertTrue(perfect.passed)

        val none = QuizEvaluator.score(quiz, emptyMap())
        assertEquals(0, none.correct)
        assertFalse(none.passed)
    }
}

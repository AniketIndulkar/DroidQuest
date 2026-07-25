package dev.novanest.droidquest.domain

import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.content.model.SearchDocumentDto

/** Where a search result navigates. Pure and unit-testable, independent of Compose. */
sealed interface SearchRoute {
    data class Lesson(val lessonId: String, val nodeId: String?, val categoryId: String?) : SearchRoute
    data class Quiz(val quizId: String) : SearchRoute
    data class Challenge(val challengeId: String) : SearchRoute
    data class Category(val categoryId: String) : SearchRoute
    data object None : SearchRoute
}

object SearchRouter {
    fun route(content: LoadedContent, doc: SearchDocumentDto): SearchRoute = when (doc.type) {
        "lesson" -> SearchRoute.Lesson(doc.id, nodeForLesson(content, doc.id), doc.categoryId)
        "quiz" -> SearchRoute.Quiz(doc.id)
        "challenge" -> SearchRoute.Challenge(doc.id)
        "category" -> SearchRoute.Category(doc.categoryId ?: doc.id)
        "glossary" -> {
            val related = content.glossaryEntry(doc.id)?.relatedLessonIds?.firstOrNull()
            val lesson = content.lesson(related)
            when {
                lesson != null -> SearchRoute.Lesson(lesson.id, nodeForLesson(content, lesson.id), lesson.categoryId)
                doc.categoryId != null -> SearchRoute.Category(doc.categoryId)
                else -> SearchRoute.None
            }
        }
        else -> SearchRoute.None
    }

    private fun nodeForLesson(content: LoadedContent, lessonId: String): String? =
        content.roadmap.nodes.firstOrNull { it.lessonId == lessonId }?.id
}

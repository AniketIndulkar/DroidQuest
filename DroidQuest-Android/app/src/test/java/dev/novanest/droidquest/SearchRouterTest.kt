package dev.novanest.droidquest

import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.domain.SearchRoute
import dev.novanest.droidquest.domain.SearchRouter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchRouterTest {

    private val content: LoadedContent = TestContent.loaded()
    private fun docOf(type: String) = content.search.documents.first { it.type == type }

    @Test
    fun lesson_result_routes_to_its_lesson_with_node() {
        val doc = docOf("lesson")
        val route = SearchRouter.route(content, doc)
        assertTrue(route is SearchRoute.Lesson)
        route as SearchRoute.Lesson
        assertEquals(doc.id, route.lessonId)
        // Every published lesson has a roadmap node.
        assertEquals(content.roadmap.nodes.firstOrNull { it.lessonId == doc.id }?.id, route.nodeId)
    }

    @Test
    fun quiz_result_routes_to_quiz() {
        val doc = docOf("quiz")
        assertEquals(SearchRoute.Quiz(doc.id), SearchRouter.route(content, doc))
    }

    @Test
    fun challenge_result_routes_to_challenge() {
        val doc = docOf("challenge")
        assertEquals(SearchRoute.Challenge(doc.id), SearchRouter.route(content, doc))
    }

    @Test
    fun glossary_result_routes_to_related_lesson_or_category() {
        val doc = docOf("glossary")
        val route = SearchRouter.route(content, doc)
        assertTrue(route is SearchRoute.Lesson || route is SearchRoute.Category || route is SearchRoute.None)
    }
}

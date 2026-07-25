package dev.novanest.droidquest

import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.content.model.RoadmapNodeType
import dev.novanest.droidquest.domain.NodeProgress
import dev.novanest.droidquest.domain.ProgressionPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Progression rules derived from whatever roadmap the current release ships. */
class ProgressionPolicyTest {

    private val content: LoadedContent = TestContent.loaded()
    private val graph = content.roadmap
    private val byId = graph.nodes.associateBy { it.id }
    private val firstId = graph.topologicalOrder.first()

    @Test
    fun first_node_is_available_initially() {
        val first = byId.getValue(firstId)
        assertEquals(NodeProgress.AVAILABLE, ProgressionPolicy.progressOf(first, emptySet()))
        assertEquals(firstId, ProgressionPolicy.nextAvailableNode(graph, emptySet())?.id)
    }

    @Test
    fun successor_is_locked_until_prerequisite_completed() {
        val successorId = graph.adjacency[firstId]?.firstOrNull()
        assertNotNull("first node should have a successor", successorId)
        val successor = byId.getValue(successorId!!)
        assertEquals(NodeProgress.LOCKED, ProgressionPolicy.progressOf(successor, emptySet()))
        assertEquals(NodeProgress.AVAILABLE, ProgressionPolicy.progressOf(successor, setOf(firstId)))
    }

    @Test
    fun completing_a_node_advances_the_next_available_node() {
        val successorId = graph.adjacency[firstId]?.firstOrNull()
        assertEquals(successorId, ProgressionPolicy.nextAvailableNode(graph, setOf(firstId))?.id)
    }

    @Test
    fun planned_preview_can_never_start_even_with_prerequisites_met() {
        val preview = graph.nodes.firstOrNull { it.type == RoadmapNodeType.LEVEL_PREVIEW }
        assertNotNull("release should expose at least one planned preview", preview)
        preview!!
        assertTrue(ProgressionPolicy.isPlanned(preview))
        // Even with every prerequisite completed, a preview cannot be started or completed.
        assertFalse(ProgressionPolicy.canStart(preview, preview.unlockPrerequisites.toSet()))
        assertEquals(NodeProgress.LOCKED, ProgressionPolicy.progressOf(preview, preview.unlockPrerequisites.toSet()))
    }

    @Test
    fun next_level_unlocks_only_after_its_prerequisites() {
        // The first node that has prerequisites and belongs to a later level.
        val gated = graph.topologicalOrder
            .map { byId.getValue(it) }
            .first { it.unlockPrerequisites.isNotEmpty() && it.type != RoadmapNodeType.LEVEL_PREVIEW }
        assertFalse(ProgressionPolicy.canStart(gated, emptySet()))
        assertTrue(ProgressionPolicy.canStart(gated, gated.unlockPrerequisites.toSet()))
    }

    @Test
    fun lesson_quiz_completes_its_lesson_node() {
        val lessonNode = graph.nodes.first { it.lessonId != null && it.type != RoadmapNodeType.LEVEL_PREVIEW }
        val lesson = content.lesson(lessonNode.lessonId)!!
        val quiz = content.quiz(lesson.quizId)!!
        assertEquals(lessonNode.id, ProgressionPolicy.nodeCompletedByQuiz(graph, quiz))
    }

    @Test
    fun boss_quiz_completes_its_boss_node() {
        val boss = graph.nodes.first { it.type == RoadmapNodeType.BOSS && it.quizId != null }
        val quiz = content.quiz(boss.quizId)!!
        assertEquals(boss.id, ProgressionPolicy.nodeCompletedByQuiz(graph, quiz))
    }
}

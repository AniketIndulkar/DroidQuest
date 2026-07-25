package dev.novanest.droidquest.domain

import dev.novanest.droidquest.content.model.QuizDto
import dev.novanest.droidquest.content.model.QuizKind
import dev.novanest.droidquest.content.model.RoadmapGraphDto
import dev.novanest.droidquest.content.model.RoadmapNodeDto
import dev.novanest.droidquest.content.model.RoadmapNodeType

enum class NodeProgress { LOCKED, AVAILABLE, COMPLETED }

/**
 * Pure roadmap progression. Consumes the generated roadmap graph plus the set of completed
 * node IDs and answers unlock questions. Never uses titles or array positions as identity;
 * only stable node IDs and unlockPrerequisites drive results.
 */
object ProgressionPolicy {

    /** Planned previews carry no teaching content and can never be started or completed. */
    fun isPlanned(node: RoadmapNodeDto): Boolean =
        node.type == RoadmapNodeType.LEVEL_PREVIEW ||
            node.status == dev.novanest.droidquest.content.model.NodeStatus.PLANNED

    fun prerequisitesMet(node: RoadmapNodeDto, completed: Set<String>): Boolean =
        node.unlockPrerequisites.all { it in completed }

    /** A node can be started when it is available, not already done, and its prereqs are met. */
    fun canStart(node: RoadmapNodeDto, completed: Set<String>): Boolean =
        !isPlanned(node) && node.id !in completed && prerequisitesMet(node, completed)

    fun progressOf(node: RoadmapNodeDto, completed: Set<String>): NodeProgress = when {
        node.id in completed -> NodeProgress.COMPLETED
        canStart(node, completed) -> NodeProgress.AVAILABLE
        else -> NodeProgress.LOCKED
    }

    /** Every node currently startable, in topological order. */
    fun availableNodes(graph: RoadmapGraphDto, completed: Set<String>): List<RoadmapNodeDto> {
        val byId = graph.nodes.associateBy { it.id }
        val order = graph.topologicalOrder.ifEmpty { graph.nodes.map { it.id } }
        return order.mapNotNull { byId[it] }.filter { canStart(it, completed) }
    }

    /** The next node the learner should tackle: first startable node in topological order. */
    fun nextAvailableNode(graph: RoadmapGraphDto, completed: Set<String>): RoadmapNodeDto? =
        availableNodes(graph, completed).firstOrNull()

    /**
     * Which node passing [quiz] completes.
     * - A lesson quiz completes the lesson node whose lessonId is the quiz's linked lesson.
     * - A checkpoint/boss quiz completes the node that links that quiz directly.
     */
    fun nodeCompletedByQuiz(graph: RoadmapGraphDto, quiz: QuizDto): String? {
        return if (quiz.kind == QuizKind.LESSON) {
            val lessonId = quiz.linkedLessonIds.firstOrNull()
            // A lesson node may be a START or LESSON node; both carry lessonId.
            graph.nodes.firstOrNull {
                (it.type == RoadmapNodeType.LESSON || it.type == RoadmapNodeType.START) && it.lessonId == lessonId
            }?.id ?: graph.nodes.firstOrNull { it.quizId == quiz.id }?.id
        } else {
            graph.nodes.firstOrNull { it.quizId == quiz.id }?.id
        }
    }

    /** A category (level) is unlocked when its start/preview node is startable or completed. */
    fun isCategoryUnlocked(graph: RoadmapGraphDto, categoryId: String, completed: Set<String>): Boolean {
        val cat = graph.categories.firstOrNull { it.id == categoryId } ?: return false
        val entryIds = (cat.startNodeIds + cat.previewNodeIds)
        val byId = graph.nodes.associateBy { it.id }
        return entryIds.any { id ->
            val node = byId[id] ?: return@any false
            node.id in completed || canStart(node, completed) ||
                // A published level entry is unlocked once its prerequisites are met even if
                // it is itself already available to start.
                prerequisitesMet(node, completed)
        }
    }
}

package dev.novanest.droidquest.ui.state

import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.content.model.CategoryDto
import dev.novanest.droidquest.content.model.RoadmapNodeDto
import dev.novanest.droidquest.content.model.RoadmapNodeType
import dev.novanest.droidquest.domain.ProgressionPolicy
import dev.novanest.droidquest.progress.LearnerProgress

/** Pure UI-derivation helpers over content + learner progress (no fabricated numbers). */
object UiDerive {

    fun categoryNodes(content: LoadedContent, categoryId: String): List<RoadmapNodeDto> =
        content.roadmap.nodes.filter { it.categoryId == categoryId && it.type != RoadmapNodeType.LEVEL_PREVIEW }

    data class CategoryProgress(val completed: Int, val total: Int, val starsEarned: Int, val pct: Int)

    fun categoryProgress(content: LoadedContent, progress: LearnerProgress, categoryId: String): CategoryProgress {
        val nodes = categoryNodes(content, categoryId)
        val completed = nodes.count { it.id in progress.completedNodeIds }
        val stars = nodes.filter { it.id in progress.completedNodeIds }.sumOf { it.rewards.stars }
        val pct = if (nodes.isEmpty()) 0 else Math.round(completed / nodes.size.toFloat() * 100)
        return CategoryProgress(completed, nodes.size, stars, pct)
    }

    /** The node the learner should tackle next, or null if nothing is currently available. */
    fun nextNode(content: LoadedContent, progress: LearnerProgress): RoadmapNodeDto? =
        ProgressionPolicy.nextAvailableNode(content.roadmap, progress.completedNodeIds)

    /** Current working level = 1 + number of fully-completed published categories. */
    fun currentLevelNumber(content: LoadedContent, progress: LearnerProgress): Int {
        val published = content.categoriesInOrder().filter { it.status != dev.novanest.droidquest.content.model.CategoryStatus.PLANNED }
        val fullyComplete = published.count { cat ->
            val p = categoryProgress(content, progress, cat.id)
            p.total > 0 && p.completed == p.total
        }
        val next = nextNode(content, progress)
        val nextOrder = next?.let { content.category(it.categoryId)?.order }
        return nextOrder ?: (fullyComplete + 1).coerceAtMost(content.categories.size)
    }

    fun isCategoryUnlocked(content: LoadedContent, progress: LearnerProgress, category: CategoryDto): Boolean =
        ProgressionPolicy.isCategoryUnlocked(content.roadmap, category.id, progress.completedNodeIds)

    /** Total stars earned across every completed node (matches per-category sums). */
    fun totalStars(content: LoadedContent, progress: LearnerProgress): Int =
        content.roadmap.nodes.filter { it.id in progress.completedNodeIds }.sumOf { it.rewards.stars }

    /** Maximum stars obtainable from all currently published (non-preview) nodes. */
    fun maxStars(content: LoadedContent): Int =
        content.roadmap.nodes.filter { it.type != RoadmapNodeType.LEVEL_PREVIEW }.sumOf { it.rewards.stars }
}

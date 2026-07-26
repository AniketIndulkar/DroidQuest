package dev.novanest.droidquest.content

import dev.novanest.droidquest.content.model.BadgeDto
import dev.novanest.droidquest.content.model.CategoryDto
import dev.novanest.droidquest.content.model.ChallengeDto
import dev.novanest.droidquest.content.model.ContentIndexDto
import dev.novanest.droidquest.content.model.CurriculumDto
import dev.novanest.droidquest.content.model.GlossaryDto
import dev.novanest.droidquest.content.model.GlossaryEntryDto
import dev.novanest.droidquest.content.model.IndexRecordDto
import dev.novanest.droidquest.content.model.LessonDto
import dev.novanest.droidquest.content.model.QuizDto
import dev.novanest.droidquest.content.model.RecallDto
import dev.novanest.droidquest.content.model.RoadmapGraphDto
import dev.novanest.droidquest.content.model.SearchIndexDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.serializer
import java.io.FileNotFoundException

/**
 * Immutable, verified snapshot of the bundled curriculum. Content is separate from
 * learner progress and is never mutated after loading.
 */
class LoadedContent(
    val curriculum: CurriculumDto,
    val index: ContentIndexDto,
    val categories: List<CategoryDto>,
    val lessonsById: Map<String, LessonDto>,
    val quizzesById: Map<String, QuizDto>,
    val challengesById: Map<String, ChallengeDto>,
    val badgesById: Map<String, BadgeDto>,
    val glossary: List<GlossaryEntryDto>,
    val roadmap: RoadmapGraphDto,
    val search: SearchIndexDto,
) {
    private val categoriesById = categories.associateBy { it.id }
    private val glossaryById = glossary.associateBy { it.id }
    private val challengesByLesson = challengesById.values.associateBy { it.lessonId }
    val recallItemsById: Map<String, RecallItem> = lessonsById.values.flatMap { lesson ->
        lesson.revealStages.recall.mapIndexed { index, recall ->
            val stableId = recall.id.ifBlank { "${lesson.id}-recall-${index + 1}" }
            RecallItem(stableId, lesson, recall)
        }
    }.associateBy { it.id }

    val contentVersion: String get() = curriculum.version

    fun category(id: String?): CategoryDto? = id?.let { categoriesById[it] }
    fun lesson(id: String?): LessonDto? = id?.let { lessonsById[it] }
    fun quiz(id: String?): QuizDto? = id?.let { quizzesById[it] }
    fun challenge(id: String?): ChallengeDto? = id?.let { challengesById[it] }
    fun challengeForLesson(lessonId: String?): ChallengeDto? = lessonId?.let { challengesByLesson[it] }
    fun badge(id: String?): BadgeDto? = id?.let { badgesById[it] }
    fun glossaryEntry(id: String?): GlossaryEntryDto? = id?.let { glossaryById[it] }
    fun recallItem(id: String?): RecallItem? = id?.let { recallItemsById[it] }

    val roadmapNodesById get() = roadmap.nodes.associateBy { it.id }
    fun categoriesInOrder(): List<CategoryDto> = categories.sortedBy { it.order }
}

data class RecallItem(val id: String, val lesson: LessonDto, val recall: RecallDto)

/**
 * Loads and verifies the bundled content snapshot behind a repository boundary.
 *
 * Verifies the content API contract and every indexed SHA-256 hash. All parsing runs on
 * [io]. Returns an explicit [ContentLoadState]; never silently returns empty collections.
 */
class DroidQuestContentRepository(
    private val source: ContentSource,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) {
    companion object {
        /** Highest content API this app build understands. */
        const val APP_CONTENT_API = 2

        const val INDEX_PATH = "content/generated/content-index.json"
        const val CURRICULUM_PATH = "content/curriculum.json"
        const val ROADMAP_GRAPH_PATH = "content/generated/roadmap-graph.json"
        const val SEARCH_INDEX_PATH = "content/generated/search-index.json"
    }

    suspend fun load(): ContentLoadState = withContext(io) {
        try {
            ContentLoadState.Success(loadOrThrow())
        } catch (e: ContentException) {
            ContentLoadState.Error(e.kind, e.message ?: "Content failed to load.")
        } catch (e: SerializationException) {
            ContentLoadState.Error(ContentErrorKind.MALFORMED_JSON, "Malformed content JSON: ${e.message}")
        } catch (e: FileNotFoundException) {
            ContentLoadState.Error(ContentErrorKind.MISSING_CONTENT, "Missing content file: ${e.message}")
        } catch (e: Exception) {
            ContentLoadState.Error(ContentErrorKind.UNKNOWN, e.message ?: "Unknown content error.")
        }
    }

    private inline fun <reified T> parse(path: String): T {
        val bytes = try {
            source.readBytes(path)
        } catch (e: FileNotFoundException) {
            throw ContentException(ContentErrorKind.MISSING_CONTENT, "Missing content file: $path", e)
        }
        return try {
            ContentJson.decodeFromString(ContentJson.serializersModule.serializer(), String(bytes, Charsets.UTF_8))
        } catch (e: SerializationException) {
            throw ContentException(ContentErrorKind.MALFORMED_JSON, "Malformed JSON in $path: ${e.message}", e)
        }
    }

    private inline fun <reified T> parseVerified(record: IndexRecordDto): T {
        val bytes = try {
            source.readBytes(record.path)
        } catch (e: FileNotFoundException) {
            throw ContentException(ContentErrorKind.MISSING_CONTENT, "Missing indexed file: ${record.path}", e)
        }
        val actual = sha256Hex(bytes)
        if (!actual.equals(record.sha256, ignoreCase = true)) {
            throw ContentException(
                ContentErrorKind.HASH_MISMATCH,
                "Hash mismatch for ${record.id} (${record.path}): expected ${record.sha256}, got $actual",
            )
        }
        return try {
            ContentJson.decodeFromString(ContentJson.serializersModule.serializer(), String(bytes, Charsets.UTF_8))
        } catch (e: SerializationException) {
            throw ContentException(ContentErrorKind.MALFORMED_JSON, "Malformed JSON in ${record.path}: ${e.message}", e)
        }
    }

    private fun loadOrThrow(): LoadedContent {
        val index: ContentIndexDto = parse(INDEX_PATH)
        val curriculum: CurriculumDto = parse(CURRICULUM_PATH)

        // Content API / version contract.
        if (curriculum.minimumAppContentApi > APP_CONTENT_API) {
            throw ContentException(
                ContentErrorKind.UNSUPPORTED_VERSION,
                "Content requires app content API ${curriculum.minimumAppContentApi}, " +
                    "but this build supports $APP_CONTENT_API. Update the app.",
            )
        }
        if (index.curriculumVersion != curriculum.version) {
            throw ContentException(
                ContentErrorKind.UNSUPPORTED_VERSION,
                "Content index version ${index.curriculumVersion} does not match curriculum ${curriculum.version}.",
            )
        }

        // Load every indexed record through its indexed path, verifying its hash.
        val categories = index.categories.map { parseVerified<CategoryDto>(it) }
        val lessons = index.lessons.map { parseVerified<LessonDto>(it) }
        val quizzes = index.quizzes.map { parseVerified<QuizDto>(it) }
        val challenges = index.challenges.map { parseVerified<ChallengeDto>(it) }
        val badges = index.badges.map { parseVerified<BadgeDto>(it) }
        // The raw roadmap is indexed + hashed; the enriched graph below is derived from it.
        index.roadmap.forEach { parseVerified<dev.novanest.droidquest.content.model.RoadmapGraphDto>(it) }

        // Generated aggregates (not individually hashed in the index).
        val roadmapGraph: RoadmapGraphDto = parse(ROADMAP_GRAPH_PATH)
        val searchIndex: SearchIndexDto = parse(SEARCH_INDEX_PATH)

        // Glossary via curriculum reference (entries are counted in the index but not hashed).
        val glossary = curriculum.glossaryIds.flatMap { gid ->
            parse<GlossaryDto>("content/glossary/$gid.json").entries
        }

        verifyCounts(index, categories.size, lessons.size, quizzes.size, challenges.size, badges.size, glossary.size)

        return LoadedContent(
            curriculum = curriculum,
            index = index,
            categories = categories.sortedBy { it.order },
            lessonsById = lessons.associateBy { it.id },
            quizzesById = quizzes.associateBy { it.id },
            challengesById = challenges.associateBy { it.id },
            badgesById = badges.associateBy { it.id },
            glossary = glossary,
            roadmap = roadmapGraph,
            search = searchIndex,
        )
    }

    private fun verifyCounts(
        index: ContentIndexDto,
        categories: Int, lessons: Int, quizzes: Int, challenges: Int, badges: Int, glossary: Int,
    ) {
        val c = index.counts
        val mismatches = buildList {
            if (c.categories != categories) add("categories ${c.categories}!=$categories")
            if (c.lessons != lessons) add("lessons ${c.lessons}!=$lessons")
            if (c.quizzes != quizzes) add("quizzes ${c.quizzes}!=$quizzes")
            if (c.challenges != challenges) add("challenges ${c.challenges}!=$challenges")
            if (c.badges != badges) add("badges ${c.badges}!=$badges")
            if (c.glossaryEntries != glossary) add("glossary ${c.glossaryEntries}!=$glossary")
        }
        if (mismatches.isNotEmpty()) {
            throw ContentException(
                ContentErrorKind.MISSING_CONTENT,
                "Content index counts do not match loaded records: ${mismatches.joinToString()}",
            )
        }
    }
}

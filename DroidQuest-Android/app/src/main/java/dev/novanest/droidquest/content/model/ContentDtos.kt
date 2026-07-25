package dev.novanest.droidquest.content.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement

/**
 * Versioned content DTOs mapped 1:1 from the DroidQuest data repository JSON.
 *
 * These are the deserialization boundary only. They contain no Compose types and
 * no learner progress — content is immutable curriculum data. Unknown JSON keys are
 * tolerated by the [dev.novanest.droidquest.content.ContentJson] configuration so a
 * forward-compatible content release does not crash an older client on additive fields.
 */

// ── Enums (closed sets the app switches on) ───────────────────────────────

@Serializable
enum class CategoryStatus {
    @SerialName("planned") PLANNED,
    @SerialName("in_progress") IN_PROGRESS,
    @SerialName("complete") COMPLETE,
}

@Serializable
enum class RoadmapNodeType {
    @SerialName("start") START,
    @SerialName("lesson") LESSON,
    @SerialName("checkpoint") CHECKPOINT,
    @SerialName("boss") BOSS,
    @SerialName("level_preview") LEVEL_PREVIEW,
}

@Serializable
enum class NodeStatus {
    @SerialName("available") AVAILABLE,
    @SerialName("planned") PLANNED,
}

@Serializable
enum class QuizKind {
    @SerialName("lesson") LESSON,
    @SerialName("week_checkpoint") WEEK_CHECKPOINT,
    @SerialName("level_checkpoint") LEVEL_CHECKPOINT,
    @SerialName("boss") BOSS,
}

@Serializable
enum class QuestionType {
    @SerialName("single_choice") SINGLE_CHOICE,
    @SerialName("multiple_choice") MULTIPLE_CHOICE,
    @SerialName("true_false") TRUE_FALSE,
    @SerialName("fill_blank") FILL_BLANK,
    @SerialName("order_steps") ORDER_STEPS,
    @SerialName("match_pairs") MATCH_PAIRS,
    @SerialName("code_output") CODE_OUTPUT,
    @SerialName("spot_bug") SPOT_BUG,
    @SerialName("short_answer") SHORT_ANSWER,
}

@Serializable
enum class CalloutTone {
    @SerialName("note") NOTE,
    @SerialName("remember") REMEMBER,
    @SerialName("warning") WARNING,
}

// ── Curriculum ────────────────────────────────────────────────────────────

@Serializable
data class CurriculumDto(
    val id: String,
    val title: String,
    val description: String = "",
    val version: String,
    val contentRevision: Int = 0,
    val releasedAt: String = "",
    val categoryIds: List<String> = emptyList(),
    val roadmapId: String = "",
    val glossaryIds: List<String> = emptyList(),
    val authoredWeeks: List<AuthoredWeekDto> = emptyList(),
    val minimumAppContentApi: Int = 1,
)

@Serializable
data class AuthoredWeekDto(
    val id: String,
    val number: Int,
    val categoryId: String,
    val title: String,
    val status: String,
    val lessonIds: List<String> = emptyList(),
    val checkpointQuizId: String? = null,
)

// ── Category (one curriculum level) ───────────────────────────────────────

@Serializable
data class CategoryDto(
    val id: String,
    val title: String,
    val order: Int,
    val description: String,
    val difficulty: String,
    val status: CategoryStatus,
    val weekRange: WeekRangeDto,
    val unlockPrerequisites: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val estimatedLearningMinutes: Int = 0,
    val lessonIds: List<String> = emptyList(),
    val checkpointQuizId: String? = null,
    val roadmapStartNodeId: String? = null,
    val plannedTopics: List<String> = emptyList(),
    val project: CategoryProjectDto,
    val theme: CategoryThemeDto,
    val version: String,
)

@Serializable
data class WeekRangeDto(val start: Int, val end: Int)

@Serializable
data class CategoryProjectDto(val title: String, val summary: String)

@Serializable
data class CategoryThemeDto(val color: String, val icon: String, val mapMood: String)

// ── Lesson ────────────────────────────────────────────────────────────────

@Serializable
data class LessonDto(
    val id: String,
    val title: String,
    val categoryId: String,
    val week: Int,
    val difficulty: String,
    val prerequisites: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val estimatedLearningMinutes: Int,
    val revealStages: RevealStagesDto,
    val quizId: String,
    val challengeId: String,
    val revision: RevisionDto,
    val sourceRefs: List<String> = emptyList(),
    val version: String,
)

@Serializable
data class RevealStagesDto(
    val scout: ScoutDto,
    val learn: LearnDto,
    val inspect: InspectDto,
    @SerialName("trap_check") val trapCheck: List<TrapDto>,
    @SerialName("challenge_intro") val challengeIntro: ChallengeIntroDto,
    val recall: List<RecallDto>,
)

@Serializable
data class ScoutDto(val purpose: String, val realWorldUse: String, val outcome: String)

@Serializable
data class LearnDto(
    val estimatedMinutes: Int,
    val sections: List<LearnSectionDto>,
    val furtherReading: List<FurtherReadingDto>,
)

@Serializable
data class LearnSectionDto(val id: String, val title: String, val blocks: List<LearnBlockDto>)

@Serializable
data class FurtherReadingDto(
    val title: String,
    val publisher: String,
    val resourceType: String,
    val url: String,
    val whyRead: String,
)

@Serializable
data class InspectDto(
    val title: String,
    val language: String,
    val code: String,
    val walkthrough: List<String>,
    val expectedOutput: String,
)

@Serializable
data class TrapDto(val mistake: String, val why: String, val fix: String)

@Serializable
data class ChallengeIntroDto(val task: String, val successLooksLike: String)

@Serializable
data class RecallDto(val prompt: String, val answer: String)

@Serializable
data class RevisionDto(
    val objectives: List<String>,
    val reviewIntervalsDays: List<Int>,
    val masteryThreshold: Double,
    val xp: Int,
    val starsAvailable: Int,
)

// ── Learn content blocks (polymorphic on "type") ──────────────────────────

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class LearnBlockDto {
    @Serializable
    @SerialName("paragraph")
    data class Paragraph(val text: String) : LearnBlockDto()

    @Serializable
    @SerialName("code")
    data class Code(val language: String, val code: String, val caption: String) : LearnBlockDto()

    @Serializable
    @SerialName("callout")
    data class Callout(val tone: CalloutTone, val title: String, val text: String) : LearnBlockDto()

    @Serializable
    @SerialName("flow")
    data class Flow(val title: String, val steps: List<FlowStep>) : LearnBlockDto()

    @Serializable
    @SerialName("table")
    data class Table(val title: String, val columns: List<String>, val rows: List<List<String>>) : LearnBlockDto()

    @Serializable
    @SerialName("list")
    data class Listing(val title: String, val items: List<String>) : LearnBlockDto()
}

@Serializable
data class FlowStep(val id: String, val label: String, val detail: String)

// ── Quiz ──────────────────────────────────────────────────────────────────

@Serializable
data class QuizDto(
    val id: String,
    val title: String,
    val categoryId: String,
    val linkedLessonIds: List<String> = emptyList(),
    val kind: QuizKind,
    val difficulty: String,
    val passingScore: Double,
    val questions: List<QuestionDto>,
    val rewards: RewardsDto,
    val version: String,
)

@Serializable
data class QuestionDto(
    val id: String,
    val prompt: String,
    val type: QuestionType,
    val difficulty: String,
    /** Options may be strings or richer values; kept as raw JSON at the boundary. */
    val options: List<JsonElement>? = null,
    /** Answer can be string, number, boolean, array or object — never forced to String. */
    val answer: JsonElement,
    val acceptedAnswers: List<String>? = null,
    val explanation: String,
    val lessonId: String,
)

@Serializable
data class RewardsDto(val xp: Int, val stars: Int)

// ── Challenge ─────────────────────────────────────────────────────────────

@Serializable
data class ChallengeDto(
    val id: String,
    val title: String,
    val categoryId: String,
    val lessonId: String,
    val difficulty: String,
    val estimatedMinutes: Int,
    val prompt: String,
    val successCriteria: List<String>,
    val hints: List<String>,
    val starterCode: StarterCodeDto,
    val solutionOutline: List<String>,
    val verification: List<String>,
    val rewards: RewardsDto,
    val version: String,
)

@Serializable
data class StarterCodeDto(val language: String, val code: String)

// ── Badge ─────────────────────────────────────────────────────────────────

@Serializable
data class BadgeDto(
    val id: String,
    val title: String,
    val description: String,
    val categoryId: String,
    val icon: String,
    val criteria: BadgeCriteriaDto,
    val xp: Int,
    val rarity: String,
    val version: String,
)

@Serializable
data class BadgeCriteriaDto(val type: String, val targetId: String, val minimumScore: Double)

// ── Glossary ──────────────────────────────────────────────────────────────

@Serializable
data class GlossaryDto(
    val id: String,
    val title: String,
    val version: String,
    val entries: List<GlossaryEntryDto>,
)

@Serializable
data class GlossaryEntryDto(
    val id: String,
    val term: String,
    val definition: String,
    val categoryId: String,
    val tags: List<String> = emptyList(),
    val relatedLessonIds: List<String> = emptyList(),
)

// ── Roadmap graph (generated) ─────────────────────────────────────────────

@Serializable
data class RoadmapGraphDto(
    val id: String,
    // Present in the generated graph; absent in the raw indexed roadmap file.
    val curriculumVersion: String = "",
    val nodes: List<RoadmapNodeDto>,
    val edges: List<RoadmapEdgeDto> = emptyList(),
    val adjacency: Map<String, List<String>> = emptyMap(),
    val incoming: Map<String, List<String>> = emptyMap(),
    val topologicalOrder: List<String> = emptyList(),
    val categories: List<RoadmapCategoryDto> = emptyList(),
)

@Serializable
data class RoadmapNodeDto(
    val id: String,
    val categoryId: String,
    val title: String,
    val type: RoadmapNodeType,
    val status: NodeStatus,
    val lessonId: String? = null,
    val quizId: String? = null,
    val difficulty: String,
    val estimatedLearningMinutes: Int,
    val unlockPrerequisites: List<String> = emptyList(),
    val rewards: RewardsDto,
)

@Serializable
data class RoadmapEdgeDto(val from: String, val to: String, val kind: String)

@Serializable
data class RoadmapCategoryDto(
    val id: String,
    val title: String,
    val order: Int,
    val startNodeIds: List<String> = emptyList(),
    val checkpointNodeIds: List<String> = emptyList(),
    val previewNodeIds: List<String> = emptyList(),
)

// ── Search index (generated) ──────────────────────────────────────────────

@Serializable
data class SearchIndexDto(
    val id: String,
    val curriculumVersion: String,
    val normalization: String = "",
    val documents: List<SearchDocumentDto> = emptyList(),
)

@Serializable
data class SearchDocumentDto(
    val id: String,
    val type: String,
    val title: String,
    val categoryId: String? = null,
    val lessonId: String? = null,
    val tags: List<String> = emptyList(),
    val text: String = "",
)

// ── Content index (generated: discovery + integrity) ──────────────────────

@Serializable
data class ContentIndexDto(
    val id: String,
    val curriculumVersion: String,
    val contentRevision: Int,
    val counts: ContentCountsDto,
    val categories: List<IndexRecordDto> = emptyList(),
    val lessons: List<IndexRecordDto> = emptyList(),
    val quizzes: List<IndexRecordDto> = emptyList(),
    val challenges: List<IndexRecordDto> = emptyList(),
    val badges: List<IndexRecordDto> = emptyList(),
    val roadmap: List<IndexRecordDto> = emptyList(),
)

@Serializable
data class ContentCountsDto(
    val categories: Int = 0,
    val lessons: Int = 0,
    val quizzes: Int = 0,
    val challenges: Int = 0,
    val badges: Int = 0,
    val glossaryEntries: Int = 0,
)

/** One indexed record: only the integrity fields are needed; the rest are ignored. */
@Serializable
data class IndexRecordDto(
    val id: String,
    val path: String,
    val sha256: String,
)

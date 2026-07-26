package dev.novanest.droidquest.progress

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import dev.novanest.droidquest.domain.RewardPolicy
import dev.novanest.droidquest.domain.ReviewRating
import dev.novanest.droidquest.domain.ReviewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore-backed [ProgressRepository]. Per-quiz best score and attempt counts use dynamic
 * prefixed keys; sets use string-set keys. The idempotent reward calculation runs inside the
 * atomic edit block so concurrent attempts cannot double-award.
 */
class DataStoreProgressRepository(
    private val dataStore: DataStore<Preferences>,
) : ProgressRepository {

    override val progress: Flow<LearnerProgress> = dataStore.data.map { it.toProgress() }

    override suspend fun toggleStar(lessonId: String) {
        dataStore.edit { p ->
            val cur = p[K_STARRED] ?: emptySet()
            p[K_STARRED] = if (lessonId in cur) cur - lessonId else cur + lessonId
        }
    }

    override suspend fun markNodeRead(nodeId: String) {
        dataStore.edit { p -> p[K_READ] = (p[K_READ] ?: emptySet()) + nodeId }
    }

    override suspend fun recordQuizResult(
        quizId: String,
        nodeIdToComplete: String?,
        scoreFraction: Double,
        passingScore: Double,
        rewardXp: Int,
        maxStars: Int,
    ): QuizRecordResult {
        lateinit var result: QuizRecordResult
        dataStore.edit { p ->
            val passedSet = p[K_PASSED] ?: emptySet()
            val alreadyPassed = quizId in passedSet
            val outcome = RewardPolicy.evaluate(scoreFraction, passingScore, rewardXp, maxStars, alreadyPassed)
            val firstPass = outcome.passed && !alreadyPassed

            // Attempts + best score always update.
            p[attemptsKey(quizId)] = (p[attemptsKey(quizId)] ?: 0) + 1
            val prevBest = p[bestKey(quizId)] ?: 0.0
            if (scoreFraction > prevBest) p[bestKey(quizId)] = scoreFraction

            if (outcome.passed) p[K_PASSED] = passedSet + quizId

            // Rewards + node completion apply only on the first pass (idempotent).
            if (firstPass) {
                p[K_XP] = (p[K_XP] ?: 0) + outcome.xpAwarded
                p[K_STARS] = (p[K_STARS] ?: 0) + outcome.starsAwarded
                if (nodeIdToComplete != null) {
                    p[K_COMPLETED] = (p[K_COMPLETED] ?: emptySet()) + nodeIdToComplete
                }
            }
            result = QuizRecordResult(outcome, if (firstPass) nodeIdToComplete else null, firstPass)
        }
        return result
    }

    override suspend fun completeChallenge(challengeId: String, rewardXp: Int, rewardStars: Int) {
        dataStore.edit { p ->
            val done = p[K_CHALLENGES] ?: emptySet()
            if (challengeId !in done) {
                p[K_CHALLENGES] = done + challengeId
                p[K_XP] = (p[K_XP] ?: 0) + rewardXp
                p[K_STARS] = (p[K_STARS] ?: 0) + rewardStars
            }
        }
    }

    override suspend fun saveReviewState(state: ReviewState) {
        dataStore.edit { p ->
            val current = (p[K_REVIEWS] ?: emptySet()).mapNotNull(::decodeReview).associateBy { it.recallItemId }.toMutableMap()
            current[state.recallItemId] = state
            p[K_REVIEWS] = current.values.map(::encodeReview).toSet()
        }
    }

    override suspend fun setGithubConnected(connected: Boolean) {
        dataStore.edit { it[K_GITHUB] = connected }
    }

    override suspend fun setNotifications(enabled: Boolean) {
        dataStore.edit { it[K_NOTIF] = enabled }
    }

    override suspend fun setSound(enabled: Boolean) {
        dataStore.edit { it[K_SOUND] = enabled }
    }

    private fun Preferences.toProgress(): LearnerProgress {
        val best = mutableMapOf<String, Double>()
        val attempts = mutableMapOf<String, Int>()
        asMap().forEach { (key, value) ->
            val name = key.name
            when {
                name.startsWith(BEST_PREFIX) -> best[name.removePrefix(BEST_PREFIX)] = value as Double
                name.startsWith(ATTEMPTS_PREFIX) -> attempts[name.removePrefix(ATTEMPTS_PREFIX)] = value as Int
            }
        }
        val reviews = (this[K_REVIEWS] ?: emptySet()).mapNotNull(::decodeReview).associateBy { it.recallItemId }
        return LearnerProgress(
            completedNodeIds = this[K_COMPLETED] ?: emptySet(),
            starredLessonIds = this[K_STARRED] ?: emptySet(),
            completedChallengeIds = this[K_CHALLENGES] ?: emptySet(),
            passedQuizIds = this[K_PASSED] ?: emptySet(),
            readNodeIds = this[K_READ] ?: emptySet(),
            bestQuizScore = best,
            quizAttempts = attempts,
            reviewStates = reviews,
            totalXp = this[K_XP] ?: 0,
            totalStars = this[K_STARS] ?: 0,
            settings = LearnerSettings(
                githubConnected = this[K_GITHUB] ?: false,
                notifications = this[K_NOTIF] ?: true,
                sound = this[K_SOUND] ?: true,
            ),
        )
    }

    private companion object {
        val K_COMPLETED = stringSetPreferencesKey("completed_nodes")
        val K_STARRED = stringSetPreferencesKey("starred_lessons")
        val K_CHALLENGES = stringSetPreferencesKey("completed_challenges")
        val K_PASSED = stringSetPreferencesKey("passed_quizzes")
        val K_READ = stringSetPreferencesKey("read_nodes")
        val K_REVIEWS = stringSetPreferencesKey("review_states_v1")
        val K_XP = intPreferencesKey("total_xp")
        val K_STARS = intPreferencesKey("total_stars")
        val K_GITHUB = booleanPreferencesKey("github_connected")
        val K_NOTIF = booleanPreferencesKey("notifications")
        val K_SOUND = booleanPreferencesKey("sound")

        const val BEST_PREFIX = "best_"
        const val ATTEMPTS_PREFIX = "attempts_"
        fun bestKey(quizId: String) = doublePreferencesKey("$BEST_PREFIX$quizId")
        fun attemptsKey(quizId: String) = intPreferencesKey("$ATTEMPTS_PREFIX$quizId")

        fun encodeReview(state: ReviewState): String = listOf(
            state.recallItemId,
            state.dueAtEpochMillis,
            state.intervalDays,
            state.repetitions,
            state.lapses,
            state.lastReviewedAtEpochMillis,
            state.lastRating.name,
        ).joinToString("|")

        fun decodeReview(value: String): ReviewState? = runCatching {
            val parts = value.split('|')
            require(parts.size == 7)
            ReviewState(
                recallItemId = parts[0],
                dueAtEpochMillis = parts[1].toLong(),
                intervalDays = parts[2].toInt(),
                repetitions = parts[3].toInt(),
                lapses = parts[4].toInt(),
                lastReviewedAtEpochMillis = parts[5].toLong(),
                lastRating = ReviewRating.valueOf(parts[6]),
            )
        }.getOrNull()
    }
}

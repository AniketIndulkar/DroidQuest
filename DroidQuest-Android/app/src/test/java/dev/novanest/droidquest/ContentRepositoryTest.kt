package dev.novanest.droidquest

import dev.novanest.droidquest.content.ContentErrorKind
import dev.novanest.droidquest.content.ContentLoadState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The content repository is the moving source of truth, so these tests assert structural
 * invariants derived from the bundled index rather than pinning a specific release number.
 */
class ContentRepositoryTest {

    @Test
    fun loads_successfully_with_consistent_counts() {
        val state = TestContent.load()
        assertTrue("expected Success but was $state", state is ContentLoadState.Success)
        val c = (state as ContentLoadState.Success).content

        // The curriculum and index agree on version.
        assertEquals(c.index.curriculumVersion, c.curriculum.version)
        // 12 ordered levels is a fixed curriculum invariant.
        assertEquals(12, c.categories.size)
        // Every indexed record loaded and every declared count matches what was parsed.
        val counts = c.index.counts
        assertEquals(counts.categories, c.categories.size)
        assertEquals(counts.lessons, c.lessonsById.size)
        assertEquals(counts.quizzes, c.quizzesById.size)
        assertEquals(counts.challenges, c.challengesById.size)
        assertEquals(counts.badges, c.badgesById.size)
        assertEquals(counts.glossaryEntries, c.glossary.size)
        assertTrue(c.quizzesById.values.sumOf { it.questions.size } > 0)
        assertTrue(c.roadmap.nodes.isNotEmpty())
    }

    @Test
    fun stable_id_lookup_resolves_indexed_records() {
        val c = TestContent.loaded()
        val lessonId = c.index.lessons.first().id
        val quizId = c.index.quizzes.first().id
        val categoryId = c.index.categories.first().id
        assertNotNull(c.lesson(lessonId))
        assertNotNull(c.quiz(quizId))
        assertNotNull(c.category(categoryId))
        assertNull(c.lesson("does-not-exist"))
    }

    @Test
    fun hash_mismatch_is_reported() {
        val c = TestContent.loaded()
        val path = c.index.lessons.first().path
        val state = TestContent.load(CorruptingSource(TestContent.source(), path))
        assertTrue(state is ContentLoadState.Error)
        assertEquals(ContentErrorKind.HASH_MISMATCH, (state as ContentLoadState.Error).kind)
    }

    @Test
    fun missing_file_is_reported() {
        val c = TestContent.loaded()
        val path = c.index.quizzes.first().path
        val state = TestContent.load(MissingSource(TestContent.source(), path))
        assertTrue(state is ContentLoadState.Error)
        assertEquals(ContentErrorKind.MISSING_CONTENT, (state as ContentLoadState.Error).kind)
    }

    @Test
    fun unsupported_content_api_is_reported() {
        val state = TestContent.load(UnsupportedApiSource(TestContent.source()))
        assertTrue(state is ContentLoadState.Error)
        assertEquals(ContentErrorKind.UNSUPPORTED_VERSION, (state as ContentLoadState.Error).kind)
    }
}

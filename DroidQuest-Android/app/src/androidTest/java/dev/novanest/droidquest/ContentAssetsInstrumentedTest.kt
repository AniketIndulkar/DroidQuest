package dev.novanest.droidquest

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.novanest.droidquest.content.AssetContentSource
import dev.novanest.droidquest.content.ContentLoadState
import dev.novanest.droidquest.content.DroidQuestContentRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Verifies the bundled assets load + hash-verify on-device, exactly as the app does. */
@RunWith(AndroidJUnit4::class)
class ContentAssetsInstrumentedTest {

    private fun repo(): DroidQuestContentRepository {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        return DroidQuestContentRepository(AssetContentSource(ctx))
    }

    @Test
    fun bundled_assets_load_and_counts_match_index() {
        val state = runBlocking { repo().load() }
        assertTrue("expected Success but was $state", state is ContentLoadState.Success)
        val c = (state as ContentLoadState.Success).content
        assertEquals(12, c.categories.size)
        assertEquals(c.index.counts.lessons, c.lessonsById.size)
        assertEquals(c.index.counts.quizzes, c.quizzesById.size)
        assertEquals(c.index.counts.glossaryEntries, c.glossary.size)
    }
}

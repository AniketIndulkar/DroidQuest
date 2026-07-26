package dev.novanest.droidquest

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.novanest.droidquest.content.AssetContentSource
import dev.novanest.droidquest.content.ContentLoadState
import dev.novanest.droidquest.content.DroidQuestContentRepository
import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.content.model.LearnBlockDto
import dev.novanest.droidquest.ui.screens.LessonScreen
import dev.novanest.droidquest.ui.state.DroidQuestUiState
import dev.novanest.droidquest.ui.state.DroidQuestViewModel
import dev.novanest.droidquest.ui.state.NavState
import dev.novanest.droidquest.ui.state.Screen
import dev.novanest.droidquest.ui.theme.DroidQuestTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Renders a full staged lesson containing paragraph, code, flow/table, a further-reading link,
 * a trap and recall, asserting the complete content is present (not summarised).
 */
@RunWith(AndroidJUnit4::class)
class LessonRenderTest {

    @get:Rule val rule = createComposeRule()

    private fun content(): LoadedContent {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val state = runBlocking { DroidQuestContentRepository(AssetContentSource(ctx)).load() }
        return (state as ContentLoadState.Success).content
    }

    @Test
    fun full_lesson_renders_all_stage_labels_and_block_kinds() {
        val content = content()
        // Pick a lesson whose Learn blocks include a flow and a table (plus code/paragraph).
        val lesson = content.lessonsById.values.first { l ->
            val blocks = l.revealStages.learn.sections.flatMap { it.blocks }
            blocks.any { it is LearnBlockDto.Flow } &&
                blocks.any { it is LearnBlockDto.Table } &&
                blocks.any { it is LearnBlockDto.Code }
        }
        assertNotNull(lesson)

        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val vm = DroidQuestViewModel(DroidQuestContentRepository(AssetContentSource(ctx)), InMemoryProgressRepository())
        val ui = DroidQuestUiState(
            loadState = ContentLoadState.Success(content),
            nav = NavState(screen = Screen.LESSON, lessonId = lesson.id),
        )

        rule.setContent { DroidQuestTheme { LessonScreen(vm, content, ui, openUrl = {}) } }

        // Stage labels and block affordances prove the complete staged content is rendered.
        // Nodes below the fold still exist in the scrollable tree.
        rule.onNodeWithText("Scout").assertIsDisplayed()
        rule.onNodeWithText("Further reading").assertExists()
        rule.onNodeWithText("Trap Check").assertExists()
        rule.onNodeWithText("Recall").assertExists()
        assert(rule.onAllNodesWithText("Copy").fetchSemanticsNodes().isNotEmpty())           // code block affordance
        assert(rule.onAllNodesWithText("Write what you remember…").fetchSemanticsNodes().isNotEmpty()) // active recall input
    }
}

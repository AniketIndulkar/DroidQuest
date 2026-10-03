package dev.novanest.droidquest

import dev.novanest.droidquest.ui.state.DroidQuestViewModel
import dev.novanest.droidquest.ui.state.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SystemBackTest {

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = DroidQuestViewModel(TestContent.repository(), FakeProgressRepository())

    private fun DroidQuestViewModel.screen() = uiState.value.nav.screen

    @Test
    fun back_on_bare_home_is_left_to_the_system() {
        val vm = viewModel()
        assertFalse(vm.handleSystemBack())
        assertEquals(Screen.HOME, vm.screen())
    }

    @Test
    fun back_from_a_top_level_tab_returns_home() {
        val vm = viewModel()
        vm.goTo(Screen.MAP)
        assertTrue(vm.handleSystemBack())
        assertEquals(Screen.HOME, vm.screen())
    }

    @Test
    fun back_from_a_pushed_screen_pops_to_where_it_came_from() {
        val vm = viewModel()
        vm.goTo(Screen.MAP)
        vm.openCategory(TestContent.loaded().categoriesInOrder().first().id)
        assertEquals(Screen.REGION, vm.screen())

        assertTrue(vm.handleSystemBack())
        assertEquals(Screen.MAP, vm.screen())
        assertTrue(vm.handleSystemBack())
        assertEquals(Screen.HOME, vm.screen())
        assertFalse(vm.handleSystemBack())
    }

    @Test
    fun back_closes_the_ai_bubble_before_navigating() {
        val vm = viewModel()
        vm.goTo(Screen.MAP)
        vm.toggleAI()
        assertTrue(vm.uiState.value.nav.aiOpen)

        assertTrue(vm.handleSystemBack())
        assertFalse(vm.uiState.value.nav.aiOpen)
        assertEquals(Screen.MAP, vm.screen())
    }

    @Test
    fun back_from_review_clears_the_session_and_leaves() {
        val vm = viewModel()
        vm.startDailyReview()
        assertEquals(Screen.REVIEW, vm.screen())

        assertTrue(vm.handleSystemBack())
        assertEquals(Screen.HOME, vm.screen())
        assertEquals(null, vm.uiState.value.review)
    }
}

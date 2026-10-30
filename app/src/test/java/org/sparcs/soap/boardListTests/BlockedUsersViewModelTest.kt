package org.sparcs.soap.boardListTests

import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.ara.AraBlockedUser
import org.sparcs.soap.app.features.settings.ara.BlockedUsersViewModel
import org.sparcs.soap.buddyTestSupport.error.TestError
import org.sparcs.soap.buddyTestSupport.RecordingAnalyticsService
import org.sparcs.soap.buddyTestSupport.repository.MockAraUserRepository
import org.sparcs.soap.testSupport.MainDispatcherRule

class BlockedUsersViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = MockAraUserRepository()
    private val analytics = RecordingAnalyticsService()
    private val first = AraBlockedUser(1, "a", null)
    private val second = AraBlockedUser(2, "b", null)
    private val third = AraBlockedUser(3, "c", null)

    private fun loadedViewModel(vararg users: AraBlockedUser): BlockedUsersViewModel {
        repository.fetchBlockedUsersResult = Result.success(users.toList())
        return BlockedUsersViewModel(repository, analytics).apply { load() }
    }

    @Test
    fun aFailedRefreshKeepsTheList() {
        val viewModel = loadedViewModel(first, second)

        repository.fetchBlockedUsersResult = Result.failure(TestError.TestFailure())
        viewModel.refresh()

        assertEquals(BlockedUsersViewModel.ViewState.Loaded(listOf(first, second)), viewModel.state)
        assertFalse(viewModel.isRefreshing)
    }

    @Test
    fun showsAnErrorOnlyWhenNothingLoaded() {
        repository.fetchBlockedUsersResult = Result.failure(TestError.TestFailure())
        val viewModel = BlockedUsersViewModel(repository, analytics)

        viewModel.load()
        assertTrue(viewModel.state is BlockedUsersViewModel.ViewState.Error)

        repository.fetchBlockedUsersResult = Result.success(listOf(first))
        viewModel.load()
        assertEquals(BlockedUsersViewModel.ViewState.Loaded(listOf(first)), viewModel.state)
    }

    @Test
    fun refreshPicksUpChanges() {
        val viewModel = loadedViewModel(first)

        repository.fetchBlockedUsersResult = Result.success(listOf(first, second))
        viewModel.refresh()

        assertEquals(BlockedUsersViewModel.ViewState.Loaded(listOf(first, second)), viewModel.state)
    }

    @Test
    fun logsAnUnblock() {
        val viewModel = loadedViewModel(first)

        viewModel.unblock(first)

        assertEquals(listOf("blocked_user_unblocked"), analytics.events.map { it.name })
    }

    @Test
    fun aFailedUnblockMergesThePersonBackIntoTheCurrentList() {
        val viewModel = loadedViewModel(first, second, third)
        val gate = CompletableDeferred<Unit>()
        repository.unblockGate = gate
        repository.blockResult = Result.failure(TestError.TestFailure())

        viewModel.unblock(first)
        repository.unblockGate = null
        repository.blockResult = Result.success(Unit)
        viewModel.unblock(third)

        repository.blockResult = Result.failure(TestError.TestFailure())
        gate.complete(Unit)

        assertEquals(BlockedUsersViewModel.ViewState.Loaded(listOf(first, second)), viewModel.state)
        assertTrue(viewModel.isAlertPresented)
        assertEquals(listOf("blocked_user_unblocked"), analytics.events.map { it.name })
    }
}

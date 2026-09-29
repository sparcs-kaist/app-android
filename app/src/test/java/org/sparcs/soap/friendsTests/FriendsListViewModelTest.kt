package org.sparcs.soap.friendsTests

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.otl.Friend
import org.sparcs.soap.app.domain.models.otl.FriendList
import org.sparcs.soap.app.features.friends.FriendsListViewModel
import org.sparcs.soap.app.features.friends.event.FriendsListViewEvent
import org.sparcs.soap.app.domain.enums.Event
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol
import org.sparcs.soap.buddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.buddyTestSupport.useCase.MockFriendUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

class FriendsListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var useCase: MockFriendUseCase
    private lateinit var analytics: RecordingAnalyticsService
    private lateinit var viewModel: FriendsListViewModel

    @Before
    fun setup() {
        useCase = MockFriendUseCase()
        analytics = RecordingAnalyticsService()
        viewModel = FriendsListViewModel(useCase, MockCrashlyticsService(), analytics)
    }

    @Test
    fun `initial state is loading`() {
        assertEquals(FriendsListViewModel.ViewState.Loading, viewModel.state.value)
    }

    @Test
    fun `load sorts favorites first then by name`() {
        useCase.fetchFriendsResult = Result.success(
            FriendList(
                checkedAt = null,
                friends = listOf(
                    Friend(1, "minho", isFavorite = false, hasScheduleNow = false),
                    Friend(2, "Zoe", isFavorite = true, hasScheduleNow = false),
                    Friend(3, "Ari", isFavorite = false, hasScheduleNow = true),
                    Friend(4, "Bora", isFavorite = true, hasScheduleNow = false)
                )
            )
        )

        viewModel.load()

        val state = viewModel.state.value as FriendsListViewModel.ViewState.Loaded
        assertEquals(listOf("Bora", "Zoe", "Ari", "minho"), state.friends.map { it.name })
    }

    @Test
    fun `load with no friends is empty`() {
        useCase.fetchFriendsResult = Result.success(FriendList(checkedAt = null, friends = emptyList()))
        viewModel.load()
        assertEquals(FriendsListViewModel.ViewState.Empty, viewModel.state.value)
    }

    @Test
    fun `load failure shows the error state`() {
        useCase.fetchFriendsResult = Result.failure(Exception("offline"))
        viewModel.load()
        assertTrue(viewModel.state.value is FriendsListViewModel.ViewState.Error)
    }

    @Test
    fun `my code failure falls back quietly`() {
        useCase.fetchMyCodeResult = Result.failure(Exception("404"))
        viewModel.loadMyCode()
        assertNull(viewModel.myCode.value)
        assertTrue(viewModel.isMyCodeUnavailable.value)
        assertNull(viewModel.alertState.value)
    }

    @Test
    fun `my code loads`() {
        viewModel.loadMyCode()
        assertEquals("ACD347", viewModel.myCode.value)
        assertFalse(viewModel.isMyCodeUnavailable.value)
    }

    @Test
    fun `add friend normalizes the code, reloads, and completes`() {
        var completed = false
        viewModel.addFriend(" acd347 ") { completed = true }

        assertEquals("ACD347", useCase.lastAddedCode)
        assertEquals(1, useCase.fetchFriendsCallCount)
        assertTrue(completed)
        assertFalse(viewModel.isAddingFriend.value)
        assertTrue(analytics.loggedEvents.contains(FriendsListViewEvent.FriendAdded))
    }

    @Test
    fun `add friend ignores invalid codes`() {
        var completed = false
        viewModel.addFriend("ABC") { completed = true }
        assertNull(useCase.lastAddedCode)
        assertFalse(completed)
    }

    @Test
    fun `add friend failure shows an alert and still completes`() {
        useCase.addFriendResult = Result.failure(Exception("invalid code"))
        var completed = false
        viewModel.addFriend("ACD347") { completed = true }

        assertNotNull(viewModel.alertState.value)
        assertTrue(completed)
        assertEquals(0, useCase.fetchFriendsCallCount)

        viewModel.dismissAlert()
        assertNull(viewModel.alertState.value)
    }

    @Test
    fun `toggle favorite flips the flag`() {
        viewModel.toggleFavorite(Friend(9, "Sujin", isFavorite = true, hasScheduleNow = false))
        assertEquals(9 to false, useCase.lastFavorite)
    }

    @Test
    fun `delete removes by id and reloads`() {
        viewModel.delete(Friend(5, "Jiwoo", isFavorite = false, hasScheduleNow = false))
        assertEquals(5, useCase.lastDeletedID)
        assertEquals(1, useCase.fetchFriendsCallCount)
    }

    @Test
    fun `refresh settles once the reload lands`() {
        viewModel.refresh()
        assertFalse(viewModel.isRefreshing.value)
        assertTrue(viewModel.state.value is FriendsListViewModel.ViewState.Loaded)
    }
}

private class RecordingAnalyticsService : AnalyticsServiceProtocol {
    val loggedEvents = mutableListOf<Event>()
    override fun logEvent(event: Event) {
        loggedEvents += event
    }
}

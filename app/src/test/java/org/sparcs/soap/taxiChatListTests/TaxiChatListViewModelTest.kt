package org.sparcs.soap.taxiChatListTests

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.features.taxiChatList.TaxiChatListViewModel
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.buddyTestSupport.repository.MockTaxiRoomRepository
import org.sparcs.soap.buddyTestSupport.useCase.MockUserUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

class TaxiChatListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockTaxiRoomRepository: MockTaxiRoomRepository
    private lateinit var mockUserUseCase: MockUserUseCase
    private lateinit var viewModel: TaxiChatListViewModel

    @Before
    fun setup() {
        mockTaxiRoomRepository = MockTaxiRoomRepository()
        mockUserUseCase = MockUserUseCase()
    }

    private fun createViewModel() {
        viewModel = TaxiChatListViewModel(mockTaxiRoomRepository, mockUserUseCase)
    }

    @Test
    fun `initial state is loading`() {
        createViewModel()
        assertEquals(TaxiChatListViewModel.ViewState.Loading, viewModel.state.value)
    }

    @Test
    fun `init loads taxi user from use case`() {
        val user: TaxiUser = TaxiUser.mock()
        mockUserUseCase.taxiUser = user

        createViewModel()

        assertEquals(user, viewModel.taxiUser)
    }

    @Test
    fun `fetchData success splits ongoing and done rooms`() = runTest {
        val onGoing = listOf(TaxiRoom.mock())
        val done = listOf(TaxiRoom.mock(), TaxiRoom.mock())
        mockTaxiRoomRepository.fetchMyRoomsResult = Result.success(onGoing to done)
        createViewModel()

        viewModel.fetchData()

        val state = viewModel.state.value
        assertTrue(state is TaxiChatListViewModel.ViewState.Loaded)
        state as TaxiChatListViewModel.ViewState.Loaded
        assertEquals(1, state.onGoing.size)
        assertEquals(2, state.done.size)
    }

    @Test
    fun `fetchData failure sets error state`() = runTest {
        mockTaxiRoomRepository.fetchMyRoomsResult = Result.failure(Exception("Test failure"))
        createViewModel()

        viewModel.fetchData()

        assertTrue(viewModel.state.value is TaxiChatListViewModel.ViewState.Error)
    }
}

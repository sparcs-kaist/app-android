package org.sparcs.soap.taxiListTests

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.taxi.TaxiLocation
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.taxiList.TaxiListViewModel
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.buddyTestSupport.repository.MockTaxiRoomRepository
import org.sparcs.soap.buddyTestSupport.useCase.MockTaxiLocationUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

class TaxiListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockTaxiRoomRepository: MockTaxiRoomRepository
    private lateinit var mockTaxiLocationUseCase: MockTaxiLocationUseCase
    private lateinit var viewModel: TaxiListViewModel

    @Before
    fun setup() {
        mockTaxiRoomRepository = MockTaxiRoomRepository()
        mockTaxiLocationUseCase = MockTaxiLocationUseCase()
    }

    private fun createViewModel(roomId: String? = null) {
        val savedStateHandle = SavedStateHandle(
            if (roomId != null) mapOf("roomId" to roomId) else emptyMap()
        )
        viewModel = TaxiListViewModel(
            savedStateHandle = savedStateHandle,
            taxiRoomRepository = mockTaxiRoomRepository,
            taxiLocationUseCase = mockTaxiLocationUseCase,
        )
    }

    @Test
    fun `roomId is read from saved state handle`() {
        createViewModel(roomId = "room-123")
        assertEquals("room-123", viewModel.roomId)
    }

    @Test
    fun `fetchData with rooms sets loaded state`() = runTest {
        val rooms = TaxiRoom.mockList()
        val locations: List<TaxiLocation> = TaxiLocation.mockList()
        mockTaxiRoomRepository.fetchRoomsResult = Result.success(rooms)
        mockTaxiLocationUseCase.locationsToLoad = locations

        createViewModel()

        val state = viewModel.state.value
        assertTrue(state is TaxiListViewModel.ViewState.Loaded)
        state as TaxiListViewModel.ViewState.Loaded
        assertEquals(rooms.size, state.rooms.size)
        assertEquals(locations, viewModel.locations.value)
    }

    @Test
    fun `fetchData with no rooms sets empty state`() = runTest {
        mockTaxiRoomRepository.fetchRoomsResult = Result.success(emptyList())
        mockTaxiLocationUseCase.locationsToLoad = TaxiLocation.mockList()

        createViewModel()

        assertTrue(viewModel.state.value is TaxiListViewModel.ViewState.Empty)
    }

    @Test
    fun `fetchData failure sets error state`() = runTest {
        mockTaxiRoomRepository.fetchRoomsResult = Result.failure(Exception("Test failure"))

        createViewModel()

        assertTrue(viewModel.state.value is TaxiListViewModel.ViewState.Error)
    }
}

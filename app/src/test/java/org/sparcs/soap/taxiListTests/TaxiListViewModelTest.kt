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
import org.sparcs.soap.app.domain.models.taxi.TaxiFavoriteRoute
import org.sparcs.soap.app.domain.usecases.taxi.MockTaxiFavoriteUseCase
import org.sparcs.soap.app.domain.usecases.taxi.TaxiFavoriteUseCaseProtocol
import org.sparcs.soap.app.features.taxiList.TaxiListViewModel
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

    @Test
    fun `favorites refresh independently and mutations use location and server ids`() = runTest {
        var fetches = 0
        var saved: Pair<String, String>? = null
        var deleted: String? = null
        val favorites = object : TaxiFavoriteUseCaseProtocol by MockTaxiFavoriteUseCase() {
            override suspend fun fetchFavoriteRoutes() { fetches++ }
            override suspend fun addFavoriteRoute(fromId: String, toId: String) { saved = fromId to toId }
            override suspend fun deleteFavoriteRoute(id: String) { deleted = id }
        }
        mockTaxiRoomRepository.fetchRoomsResult = Result.failure(Exception("offline"))
        viewModel = TaxiListViewModel(SavedStateHandle(), mockTaxiRoomRepository, mockTaxiLocationUseCase, favorites)
        assertEquals(1, fetches)
        val places = TaxiLocation.mockList()
        viewModel.source = places[0]
        viewModel.destination = places[1]
        viewModel.saveFavoriteRoute()
        assertEquals(places[0].id to places[1].id, saved)
        viewModel.deleteFavoriteRoute(TaxiFavoriteRoute("server-id", places[0], places[1]))
        assertEquals("server-id", deleted)
    }

    @Test
    fun `favorite mutation failure presents an alert without replacing room state`() = runTest {
        val favorites = object : TaxiFavoriteUseCaseProtocol by MockTaxiFavoriteUseCase() {
            override suspend fun addFavoriteRoute(fromId: String, toId: String) { error("offline") }
        }
        viewModel = TaxiListViewModel(SavedStateHandle(), mockTaxiRoomRepository, mockTaxiLocationUseCase, favorites)
        val before = viewModel.state.value
        val places = TaxiLocation.mockList()
        viewModel.source = places[0]
        viewModel.destination = places[1]
        viewModel.saveFavoriteRoute()
        assertTrue(viewModel.isAlertPresented)
        assertEquals("offline", viewModel.alertState?.message)
        assertEquals(before, viewModel.state.value)
    }
}

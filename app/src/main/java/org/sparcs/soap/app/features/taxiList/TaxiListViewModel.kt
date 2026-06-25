package org.sparcs.soap.app.features.taxiList

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.taxi.TaxiCreateRoom
import org.sparcs.soap.app.domain.models.taxi.TaxiFavoriteRoute
import org.sparcs.soap.app.domain.models.taxi.TaxiLocation
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.repositories.taxi.TaxiRoomRepositoryProtocol
import org.sparcs.soap.app.domain.usecases.taxi.MockTaxiFavoriteUseCase
import org.sparcs.soap.app.domain.usecases.taxi.TaxiFavoriteUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.taxi.TaxiLocationUseCaseProtocol
import org.sparcs.soap.app.shared.extensions.ceilToNextTenMinutes
import org.sparcs.soap.app.shared.extensions.toAlertState
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

interface TaxiListViewModelProtocol {

    // MARK: - ViewModel Properties
    val state: StateFlow<TaxiListViewModel.ViewState>
    val week: List<Date>
    var roomId: String?
    val rooms: StateFlow<List<TaxiRoom>>
    val locations: StateFlow<List<TaxiLocation>>
    val favoriteRoutes: StateFlow<List<TaxiFavoriteRoute>>
    var roomHasCarrier: Boolean

    // MARK: - View Properties
    var source: TaxiLocation?
    var destination: TaxiLocation?
    var selectedDate: Date?

    // MARK: - Functions
    var roomDepartureTime: Date
    var roomCapacity: Int

    var alertState: AlertState?
    var isAlertPresented: Boolean

    fun fetchData()
    suspend fun createRoom(title: String): String?
    fun toggleCarrier(roomID: String, hasCarrier: Boolean)

    fun addFavoriteRoute()
    fun deleteFavoriteRoute(id: String)
    fun selectFavoriteRoute(favoriteRoute: TaxiFavoriteRoute)
}

@HiltViewModel
class TaxiListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val taxiRoomRepository: TaxiRoomRepositoryProtocol,
    private val taxiLocationUseCase: TaxiLocationUseCaseProtocol,
    private val taxiFavoriteUseCase: TaxiFavoriteUseCaseProtocol = MockTaxiFavoriteUseCase(),
) : ViewModel(), TaxiListViewModelProtocol {

    sealed class ViewState {
        data object Loading : ViewState()
        data class Loaded(val rooms: List<TaxiRoom>, val locations: List<TaxiLocation>) : ViewState()
        data class Empty(val locations: List<TaxiLocation>) : ViewState()
        data class Error(val error: Exception) : ViewState()
    }

    // MARK: - ViewModel Properties
    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    override val week: List<Date> = (0 until 7).map {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, it) }.time
    }

    private val _locations = MutableStateFlow<List<TaxiLocation>>(emptyList())
    override val locations: StateFlow<List<TaxiLocation>> get() = _locations

    override var roomId: String? = savedStateHandle["roomId"]

    private val _rooms = MutableStateFlow<List<TaxiRoom>>(emptyList())
    override val rooms: StateFlow<List<TaxiRoom>> get() = _rooms

    override val favoriteRoutes: StateFlow<List<TaxiFavoriteRoute>> = taxiFavoriteUseCase.favoriteRoutes

    init {
        fetchData()
    }

    //MARK: - View Properties
    override var source: TaxiLocation? by mutableStateOf(null)
    override var destination: TaxiLocation? by mutableStateOf(null)
    override var selectedDate: Date? by mutableStateOf(null)

    // Room Creation
    override var roomDepartureTime: Date by mutableStateOf(Date().ceilToNextTenMinutes())
    override var roomCapacity: Int by mutableIntStateOf(4)
    override var roomHasCarrier: Boolean by mutableStateOf(false)

    override var alertState by mutableStateOf<AlertState?>(null)
    override var isAlertPresented by mutableStateOf(false)

    // MARK: - Functions
    override fun fetchData() {
        viewModelScope.launch {
            if (_rooms.value.isEmpty()) {
                _state.value = ViewState.Loading
            }
            try {
                taxiFavoriteUseCase.fetchFavoriteRoutes()
                
                val roomsDeferred = taxiRoomRepository.fetchRooms()
                taxiLocationUseCase.fetchLocations()

                _rooms.value = roomsDeferred
                _locations.value = taxiLocationUseCase.locations.value

                _state.value = if (_rooms.value.isEmpty()) {
                    ViewState.Empty(_locations.value)
                } else {
                    ViewState.Loaded(_rooms.value, _locations.value)
                }
            } catch (e: Exception) {
                _state.value = ViewState.Error(e)
            }
        }
    }

    //Safely capture values before any suspension
    override suspend fun createRoom(title: String): String? {
        try {
            val request = TaxiCreateRoom(
                title = title,
                source = source ?: return null,
                destination = destination ?: return null,
                departureTime = roomDepartureTime,
                capacity = roomCapacity
            )
            val newRoom = taxiRoomRepository.createRoom(request)
            this.roomId = newRoom.id
            return newRoom.id
        } catch (e: Exception) {
            _state.value = ViewState.Error(e)
            alertState = e.toAlertState(R.string.error_failed_to_create_taxi_room)
            isAlertPresented = true
            return null
        }
    }

    override fun toggleCarrier(roomID: String, hasCarrier: Boolean) {
        viewModelScope.launch {
            try {
                taxiRoomRepository.toggleCarrier(roomID, hasCarrier)
            } catch (e: Exception) {
                _state.value = ViewState.Error(e)
            }
        }
    }

    override fun addFavoriteRoute() {
        val from = source ?: return
        val to = destination ?: return

        if (favoriteRoutes.value.any { it.from.id == from.id && it.to.id == to.id }) {
            return
        }

        viewModelScope.launch {
            try {
                taxiFavoriteUseCase.addFavoriteRoute(from.id, to.id)
            } catch (e: Exception) {
                val message = e.message ?: ""
                if (message.contains("already exists", ignoreCase = true)) {
                    taxiFavoriteUseCase.fetchFavoriteRoutes()
                } else {
                    alertState = AlertState(
                        titleResId = R.string.error,
                        messageResId = R.string.route_already_exists
                    )
                    isAlertPresented = true
                }
            }
        }
    }

    override fun deleteFavoriteRoute(id: String) {
        viewModelScope.launch {
            try {
                taxiFavoriteUseCase.deleteFavoriteRoute(id)
            } catch (e: Exception) {
                taxiFavoriteUseCase.fetchFavoriteRoutes()
            }
        }
    }

    override fun selectFavoriteRoute(favoriteRoute: TaxiFavoriteRoute) {
        source = favoriteRoute.from
        destination = favoriteRoute.to
    }
}


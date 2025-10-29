package com.example.soap.Features.TaxiList

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.Domain.Models.Taxi.TaxiCreateRoom
import com.example.soap.Domain.Models.Taxi.TaxiLocation
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Domain.Repositories.Taxi.TaxiRoomRepositoryProtocol
import com.example.soap.Shared.Extensions.ceilToNextTenMinutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

interface TaxiListViewModelProtocol {

    // MARK: - ViewModel Properties
    val state: StateFlow<TaxiListViewModel.ViewState>
    val week: List<Date>
    val rooms:StateFlow<List<TaxiRoom>>
    val locations: StateFlow<List<TaxiLocation>>

    // MARK: - View Properties
    var source: TaxiLocation?
    var destination: TaxiLocation?
    var selectedDate: Date?

    // MARK: - Functions
    var roomDepartureTime: Date
    var roomCapacity: Int

    suspend fun fetchData()
    suspend fun createRoom(title: String)

}

@HiltViewModel
class TaxiListViewModel @Inject constructor(
    private val taxiRoomRepository: TaxiRoomRepositoryProtocol
) : ViewModel(),TaxiListViewModelProtocol {

    sealed class ViewState {
        data object Loading : ViewState()
        data class Loaded(val rooms: List<TaxiRoom>, val locations: List<TaxiLocation>) : ViewState()
        data class Empty(val locations: List<TaxiLocation>) : ViewState()
        data class Error(val message: String) : ViewState()
    }

    // MARK: - ViewModel Properties
    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    override val week: List<Date> = (0 until 7).map {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, it) }.time
    }

    private val _locations = MutableStateFlow<List<TaxiLocation>>(emptyList())
    override val locations: StateFlow<List<TaxiLocation>> get() = _locations

    private val _rooms = MutableStateFlow<List<TaxiRoom>>(emptyList())
    override val rooms: StateFlow<List<TaxiRoom>> get() = _rooms

    //MARK: - View Properties
    override var source: TaxiLocation? by mutableStateOf(null)
    override var destination: TaxiLocation? by mutableStateOf(null)
    override var selectedDate: Date? by mutableStateOf(null)

    // Room Creation
    override var roomDepartureTime: Date by mutableStateOf(Date().ceilToNextTenMinutes())
    override var roomCapacity: Int by mutableStateOf(4)

    // MARK: - Functions
    override suspend fun fetchData() {
        viewModelScope.launch {
            _state.value = ViewState.Loading
            try {
                val roomsDeferred = taxiRoomRepository.fetchRooms()
                val locationsDeferred = taxiRoomRepository.fetchLocations()

                _rooms.value = roomsDeferred
                _locations.value = locationsDeferred

                _state.value = if (_rooms.value.isEmpty()) {
                    ViewState.Empty(_locations.value)
                } else {
                    ViewState.Loaded(_rooms.value, _locations.value)
                }
            } catch (e: Exception) {
                _state.value = ViewState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }


    //Safely capture values before any suspension
    override suspend fun createRoom(title: String) {
        try {
            Log.d("TaxiListViewModel", "creating a room")
            val request = TaxiCreateRoom(
                title = title,
                source = source ?: return,
                destination = destination ?: return,
                departureTime = roomDepartureTime,
                capacity = roomCapacity
            )
            taxiRoomRepository.createRoom(request)
        } catch (e: Exception) {
            throw e
        }
    }
}


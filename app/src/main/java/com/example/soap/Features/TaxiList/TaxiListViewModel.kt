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
import com.example.soap.Domain.Repositories.TaxiRoomRepositoryProtocol
import com.example.soap.Shared.Extensions.ceilToNextTenMinutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TaxiListViewModel @Inject constructor(
    private val taxiRoomRepository: TaxiRoomRepositoryProtocol
) : ViewModel() {

    sealed class ViewState {
        object Loading : ViewState()
        data class Loaded(val rooms: List<TaxiRoom>, val locations: List<TaxiLocation>) : ViewState()
        data class Empty(val locations: List<TaxiLocation>) : ViewState()
        data class Error(val message: String) : ViewState()
    }

    // MARK: - ViewModel Properties
    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    val state: StateFlow<ViewState> = _state.asStateFlow()

    val week: List<Date> = (0 until 7).map {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, it) }.time
    }

    var rooms: List<TaxiRoom> = emptyList()
        private set

    var locations: List<TaxiLocation> = emptyList()
        private set

    //MARK: - View Properties
    var source: TaxiLocation? by mutableStateOf(null)
    var destination: TaxiLocation? by mutableStateOf(null)
    var selectedDate: Date by mutableStateOf(Date())

    // Room Creation
    var roomDepartureTime: Date by mutableStateOf(Date().ceilToNextTenMinutes())
    var roomCapacity: Int by mutableStateOf(4)


    // MARK: - Functions
    fun fetchData() {
        viewModelScope.launch {
            _state.value = ViewState.Loading
            try {
                val roomsDeferred = taxiRoomRepository.fetchRooms()
                val locationsDeferred = taxiRoomRepository.fetchLocations()

                rooms = roomsDeferred
                locations = locationsDeferred

                _state.value = if (rooms.isEmpty()) {
                    ViewState.Empty(locations)
                } else {
                    ViewState.Loaded(rooms, locations)
                }
            } catch (e: Exception) {
                _state.value = ViewState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }


    //Safely capture values before any suspension
    fun createRoom(title: String, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("TaxiListViewModel", "creating a room")
                val request = TaxiCreateRoom(
                    title = title,
                    source = source ?: return@launch,
                    destination = destination ?: return@launch,
                    departureTime = roomDepartureTime,
                    capacity = roomCapacity
                )
                taxiRoomRepository.createRoom(request)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}


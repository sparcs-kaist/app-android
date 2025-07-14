package com.example.soap.Features.TaxiList

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.soap.Domain.Models.Taxi.TaxiLocation
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar
import java.util.Date

class TaxiListViewModel(
//    private val taxiRoomRepository: TaxiRoomRepository
) : ViewModel() {

    sealed class ViewState {
        object Loading : ViewState()
        data class Loaded(val rooms: List<TaxiRoom>, val locations: List<TaxiLocation>) : ViewState()
        data class Empty(val locations: List<TaxiLocation>) : ViewState()
        data class Error(val message: String) : ViewState()
    }

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    val state: StateFlow<ViewState> = _state

    val week: List<Date> = List(7) { offset ->
        Calendar.getInstance().apply { add(Calendar.DATE, offset) }.time
    }

    var rooms: List<TaxiRoom> = emptyList()
    var locations: List<TaxiLocation> = emptyList()

    var source: TaxiLocation? by mutableStateOf(null)
    var destination: TaxiLocation? by mutableStateOf(null)
    var selectedDate: Date by mutableStateOf(Date())
    var roomDepartureTime: Date by mutableStateOf(ceilToNextTenMinutes(Date()))
    var roomCapacity: Int by mutableStateOf(4)

//    fun fetchData() {
//        viewModelScope.launch {
//            try {
//                val roomsDeferred = taxiRoomRepository.fetchRooms()
//                val locationsDeferred = taxiRoomRepository.fetchLocations()
//                rooms = roomsDeferred
//                locations = locationsDeferred
//
//                if (rooms.isEmpty()) {
//                    _state.value = ViewState.Empty(locations)
//                } else {
//                    _state.value = ViewState.Loaded(rooms, locations)
//                }
//            } catch (e: Exception) {
//                _state.value = ViewState.Error(e.localizedMessage ?: "Unknown error")
//            }
//        }
//    }
//
//    fun createRoom(title: String) {
//        viewModelScope.launch {
//            try {
//                val currentSource = source ?: return@launch
//                val currentDestination = destination ?: return@launch
//                val request = TaxiCreateRoom(
//                    title = title,
//                    source = currentSource,
//                    destination = currentDestination,
//                    departureTime = roomDepartureTime,
//                    capacity = roomCapacity
//                )
//                taxiRoomRepository.createRoom(request)
//            } catch (e: Exception) {
//            }
//        }
//    }

    companion object {
        private fun ceilToNextTenMinutes(date: Date): Date {
            val calendar = Calendar.getInstance().apply { time = date }
            val minute = calendar.get(Calendar.MINUTE)
            val nextTen = ((minute + 9) / 10 * 10) % 60
            calendar.set(Calendar.MINUTE, nextTen)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            if (nextTen == 0) calendar.add(Calendar.HOUR_OF_DAY, 1)
            return calendar.time
        }
    }
}

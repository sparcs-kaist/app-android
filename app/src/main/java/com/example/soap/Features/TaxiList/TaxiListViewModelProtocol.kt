package com.example.soap.Features.TaxiList

import com.example.soap.Domain.Models.Taxi.TaxiLocation
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.util.Date

interface TaxiListViewModelProtocol {

    sealed class ViewState {
        object Loading : ViewState()
        data class Loaded(val rooms: List<TaxiRoom>, val locations: List<TaxiLocation>) : ViewState()
        data class Empty(val locations: List<TaxiLocation>) : ViewState()
        data class Error(val message: String) : ViewState()
    }

    val state: StateFlow<ViewState>
    val week: List<LocalDate>
    val rooms: List<TaxiRoom>
    val locations: List<TaxiLocation>

    var source: TaxiLocation?
    var destination: TaxiLocation?
    var selectedDate: Date

    var roomDepartureTime: Date
    var roomCapacity: Int

    suspend fun fetchData()
    suspend fun createRoom(title: String)
}

package com.example.soap.Features.TaxiList

import com.example.soap.Domain.Models.Taxi.TaxiLocation
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

interface TaxiListViewModelProtocol {

    val state: StateFlow<TaxiListViewModel.ViewState>
    val week: List<Date>
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

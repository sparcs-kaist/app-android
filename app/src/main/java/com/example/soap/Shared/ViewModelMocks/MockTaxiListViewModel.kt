package com.example.soap.Shared.ViewModel

import com.example.soap.Domain.Models.Taxi.TaxiLocation
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Features.TaxiList.TaxiListViewModel
import com.example.soap.Features.TaxiList.TaxiListViewModelProtocol
import com.example.soap.Shared.Extensions.ceilToNextTenMinutes
import com.example.soap.Shared.Mocks.mockList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar
import java.util.Date

class MockTaxiListViewModel(initialState: TaxiListViewModel.ViewState) : TaxiListViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<TaxiListViewModel.ViewState> = _state

    override var week: List<Date> = List(7) { index ->
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, index)
        }.time
    }
    override var rooms: List<TaxiRoom> = TaxiRoom.mockList()
    override var locations: List<TaxiLocation> = TaxiLocation.mockList()

    override var source: TaxiLocation? = null
    override var destination: TaxiLocation? = null
    override var selectedDate: Date? = null

    override var roomDepartureTime: Date = Date().ceilToNextTenMinutes()
    override var roomCapacity: Int = 4

    override suspend fun fetchData() {}

    override suspend fun createRoom(title: String) {}
}

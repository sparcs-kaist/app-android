package org.sparcs.App.Shared.ViewModelMocks.Taxi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.sparcs.App.Domain.Models.Taxi.TaxiLocation
import org.sparcs.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.App.Features.TaxiList.TaxiListViewModel
import org.sparcs.App.Features.TaxiList.TaxiListViewModelProtocol
import org.sparcs.App.Shared.Extensions.ceilToNextTenMinutes
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

    private val _locations = MutableStateFlow<List<TaxiLocation>>(emptyList())
    override val locations: StateFlow<List<TaxiLocation>> get() = _locations

    override var roomId: String? = ""

    private val _rooms = MutableStateFlow<List<TaxiRoom>>(emptyList())
    override val rooms: StateFlow<List<TaxiRoom>> get() = _rooms

    override var source: TaxiLocation? = null
    override var destination: TaxiLocation? = null
    override var selectedDate: Date? = null

    override var roomDepartureTime: Date = Date().ceilToNextTenMinutes()
    override var roomCapacity: Int = 4

    override suspend fun fetchData() {}

    override suspend fun createRoom(title: String) {}
}

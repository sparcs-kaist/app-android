package org.sparcs.soap.BuddyPreviewSupport.Taxi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Helpers.LocalizedString
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiLocation
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Features.TaxiList.TaxiListViewModel
import org.sparcs.soap.App.Features.TaxiList.TaxiListViewModelProtocol
import org.sparcs.soap.App.Shared.Mocks.Taxi.mockList
import java.util.Calendar
import java.util.Date
import java.util.UUID

class PreviewTaxiListViewModel(
    initialState: TaxiListViewModel.ViewState = TaxiListViewModel.ViewState.Loaded(
        TaxiRoom.mockList(),
        TaxiLocation.mockList()
    ),
) : TaxiListViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<TaxiListViewModel.ViewState> = _state.asStateFlow()

    override val week: List<Date>
        get() {
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            return (0 until 7).map {
                val date = calendar.time
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                date
            }
        }

    override var roomId: String? by mutableStateOf(null)

    private val _rooms = MutableStateFlow(TaxiRoom.mockList())
    override val rooms: StateFlow<List<TaxiRoom>> = _rooms.asStateFlow()

    private val _locations = MutableStateFlow(
        listOf(
            TaxiLocation(
                id = UUID.randomUUID().toString(),
                title = LocalizedString(mapOf("ko" to "대전역", "en" to "Daejeon Station")),
                priority = 0.0,
                latitude = 36.3319731,
                longitude = 127.4323382
            ),
            TaxiLocation(
                id = UUID.randomUUID().toString(),
                title = LocalizedString(mapOf("ko" to "카이스트 본원", "en" to "KAIST Main Campus")),
                priority = 0.0,
                latitude = 36.3723596,
                longitude = 127.358697
            )
        )
    )
    override val locations: StateFlow<List<TaxiLocation>> = _locations.asStateFlow()

    override var source: TaxiLocation? by mutableStateOf(null)
    override var destination: TaxiLocation? by mutableStateOf(null)
    override var selectedDate: Date? by mutableStateOf(Date())

    override var roomDepartureTime: Date by mutableStateOf(Date())
    override var roomCapacity by mutableIntStateOf(4)
    override var roomHasCarrier: Boolean by mutableStateOf(false)

    override var alertState by mutableStateOf<AlertState?>(null)
    override var isAlertPresented by mutableStateOf(false)

    override fun fetchData() {}
    override suspend fun createRoom(title: String): String? {
        return null
    }

    override fun toggleCarrier(roomID: String, hasCarrier: Boolean) {}
}
package org.sparcs.App.Shared.ViewModelMocks.Taxi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.sparcs.App.Domain.Enums.Taxi.TaxiReports
import org.sparcs.App.Features.Settings.Taxi.TaxiReportListViewModel.ViewState
import org.sparcs.App.Features.Settings.Taxi.TaxiReportListViewModelProtocol

class MockTaxiReportListViewModel(initialState: ViewState): TaxiReportListViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<ViewState> = _state

    override var reports: TaxiReports = TaxiReports(emptyList(), emptyList())

    override suspend fun fetchReports() {}
}
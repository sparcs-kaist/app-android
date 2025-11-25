package com.sparcs.soap.Shared.ViewModelMocks.Taxi

import com.sparcs.soap.Domain.Enums.Taxi.TaxiReports
import com.sparcs.soap.Features.Settings.Taxi.TaxiReportListViewModel.ViewState
import com.sparcs.soap.Features.Settings.Taxi.TaxiReportListViewModelProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockTaxiReportListViewModel(initialState: ViewState): TaxiReportListViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<ViewState> = _state

    override var reports: TaxiReports = TaxiReports(emptyList(), emptyList())

    override suspend fun fetchReports() {}
}
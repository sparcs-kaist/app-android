package com.sparcs.soap.Shared.ViewModelMocks

import com.sparcs.soap.Features.Settings.Taxi.TaxiReportListViewModel.ViewState
import com.sparcs.soap.Features.Settings.Taxi.TaxiReportListViewModelProtocol
import com.sparcs.soap.Features.Settings.Taxi.TaxiReports
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockTaxiReportListViewModel(initialState: ViewState): TaxiReportListViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<ViewState> = _state

    override var reports: TaxiReports = TaxiReports(emptyList(), emptyList())

    override suspend fun fetchReports() {}
}
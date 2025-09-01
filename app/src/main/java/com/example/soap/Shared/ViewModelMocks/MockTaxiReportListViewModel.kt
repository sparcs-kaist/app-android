package com.example.soap.Shared.ViewModelMocks

import com.example.soap.Features.Settings.Taxi.TaxiReportListViewModel.ViewState
import com.example.soap.Features.Settings.Taxi.TaxiReportListViewModelProtocol
import com.example.soap.Features.Settings.Taxi.TaxiReports
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockTaxiReportListViewModel(initialState: ViewState): TaxiReportListViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<ViewState> = _state

    override var reports: TaxiReports = TaxiReports(emptyList(), emptyList())

    override suspend fun fetchReports() {}
}
package com.example.soap.Features.Settings.Taxi

import kotlinx.coroutines.flow.StateFlow

interface TaxiReportListViewModelProtocol {
    val state: StateFlow<TaxiReportListViewModel.ViewState>
    var reports: TaxiReports

    suspend fun fetchReports()
}

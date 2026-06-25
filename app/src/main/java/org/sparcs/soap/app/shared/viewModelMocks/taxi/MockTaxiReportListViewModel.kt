package org.sparcs.soap.app.shared.viewModelMocks.taxi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.sparcs.soap.app.domain.enums.taxi.TaxiReports
import org.sparcs.soap.app.features.settings.taxi.TaxiReportListViewModel.ViewState
import org.sparcs.soap.app.features.settings.taxi.TaxiReportListViewModelProtocol

class MockTaxiReportListViewModel(initialState: ViewState): TaxiReportListViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<ViewState> = _state

    override var reports: TaxiReports = TaxiReports(emptyList(), emptyList())

    override suspend fun fetchReports() {}
}
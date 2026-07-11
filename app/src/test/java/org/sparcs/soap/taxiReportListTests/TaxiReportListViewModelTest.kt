package org.sparcs.soap.taxiReportListTests

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.enums.taxi.TaxiReports
import org.sparcs.soap.app.features.settings.taxi.TaxiReportListViewModel
import org.sparcs.soap.buddyTestSupport.repository.MockTaxiReportRepository
import org.sparcs.soap.testSupport.MainDispatcherRule

class TaxiReportListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockTaxiReportRepository: MockTaxiReportRepository
    private lateinit var viewModel: TaxiReportListViewModel

    @Before
    fun setup() {
        mockTaxiReportRepository = MockTaxiReportRepository()
        viewModel = TaxiReportListViewModel(mockTaxiReportRepository)
    }

    @Test
    fun `initial state is loading`() {
        assertEquals(TaxiReportListViewModel.ViewState.Loading, viewModel.state.value)
    }

    @Test
    fun `fetchReports success sets loaded state and reports`() = runTest {
        val reports = TaxiReports(emptyList(), emptyList())
        mockTaxiReportRepository.fetchMyReportsResult = Result.success(reports)

        viewModel.fetchReports()

        assertEquals(TaxiReportListViewModel.ViewState.Loaded, viewModel.state.value)
        assertEquals(reports, viewModel.reports)
    }

    @Test
    fun `fetchReports failure sets error state`() = runTest {
        mockTaxiReportRepository.fetchMyReportsResult = Result.failure(Exception("Test failure"))

        viewModel.fetchReports()

        assertTrue(viewModel.state.value is TaxiReportListViewModel.ViewState.Error)
    }
}

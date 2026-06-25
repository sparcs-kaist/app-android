package org.sparcs.soap.app.features.settings.taxi

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.app.domain.enums.taxi.TaxiReports
import org.sparcs.soap.app.domain.repositories.taxi.TaxiReportRepositoryProtocol
import timber.log.Timber
import javax.inject.Inject


interface TaxiReportListViewModelProtocol {
    val state: StateFlow<TaxiReportListViewModel.ViewState>
    var reports: TaxiReports

    suspend fun fetchReports()
}

@HiltViewModel
class TaxiReportListViewModel @Inject constructor(
    private val taxiReportRepository: TaxiReportRepositoryProtocol,
) : ViewModel(), TaxiReportListViewModelProtocol {
    sealed class ViewState {
        data object Loading : ViewState()
        data object Loaded : ViewState()
        data class Error(val error: Exception) : ViewState()
    }

    // MARK: - Properties
    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    override var reports: TaxiReports = TaxiReports(emptyList(), emptyList())

    // MARK: - Functions
    override suspend fun fetchReports() {
        try {
            reports = taxiReportRepository.fetchMyReports()
            _state.value = ViewState.Loaded
        } catch (e: Exception) {
            _state.value = ViewState.Error(e)
            Timber.e("Failed to fetch reports: ${e.localizedMessage}")
        }
    }
}
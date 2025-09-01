package com.example.soap.Features.Settings.Taxi

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.soap.Domain.Models.Taxi.TaxiReport
import com.example.soap.Domain.Repositories.TaxiUserRepositoryProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class TaxiReports(
    val reported: List<TaxiReport>,
    val reporting: List<TaxiReport>
)

@HiltViewModel
class TaxiReportListViewModel @Inject constructor(
    private val taxiUserRepository: TaxiUserRepositoryProtocol
): ViewModel(), TaxiReportListViewModelProtocol {
    sealed class ViewState {
        data object Loading : ViewState()
        data object Loaded : ViewState()
        data class Error(val message: String) : ViewState()
    }

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    override var reports: TaxiReports = TaxiReports(emptyList(), emptyList())

    override suspend fun fetchReports() {
        try {
            reports = taxiUserRepository.fetchReports()
            _state.value = ViewState.Loaded
        } catch (e: Exception) {
            _state.value = ViewState.Error(e.localizedMessage ?: "Unknown error")
            Log.e("TaxiReportListViewModel", "Failed to fetch reports: ${e.localizedMessage}")
        }
    }

}
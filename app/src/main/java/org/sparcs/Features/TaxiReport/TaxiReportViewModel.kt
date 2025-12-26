package org.sparcs.Features.TaxiReport

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import org.sparcs.Domain.Helpers.CrashlyticsHelper
import org.sparcs.Domain.Models.Taxi.TaxiCreateReport
import org.sparcs.Domain.Models.Taxi.TaxiParticipant
import org.sparcs.Domain.Models.Taxi.TaxiReport
import org.sparcs.Domain.Models.Taxi.TaxiRoom
import org.sparcs.Domain.Repositories.Taxi.TaxiReportRepositoryProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

interface TaxiReportViewModelProtocol {
    val room: StateFlow<TaxiRoom>
    val selectedUser: StateFlow<TaxiParticipant?>
    val selectedReason: StateFlow<TaxiReport.Reason?>
    val maxEtcDetailsLength: Int
    var etcDetails: String

    fun setSelectedUser(user: TaxiParticipant?)
    fun setSelectedReason(reason: TaxiReport.Reason?)
    suspend fun createReport(roomID: String)
    fun handleException(error: Throwable)
}

@HiltViewModel
class TaxiReportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val taxiReportRepository: TaxiReportRepositoryProtocol,
    private val crashlyticsHelper: CrashlyticsHelper,
) : ViewModel(), TaxiReportViewModelProtocol {

    // MARK: - Initialiser
    private val initialRoom: TaxiRoom by lazy {
        val json = savedStateHandle.get<String>("room_json")
            ?: throw IllegalStateException("room_json is null. TaxiReportViewModel requires a room_json to initialize.")
        Gson().fromJson(json, TaxiRoom::class.java)
    }

    // MARK: - Properties
    private val _room = MutableStateFlow(initialRoom)
    override val room: StateFlow<TaxiRoom> = _room.asStateFlow()

    // MARK: - View Properties
    private val _selectedUser = MutableStateFlow<TaxiParticipant?>(null)
    override val selectedUser: StateFlow<TaxiParticipant?> get() = _selectedUser

    private val _selectedReason = MutableStateFlow<TaxiReport.Reason?>(null)
    override val selectedReason: StateFlow<TaxiReport.Reason?> get() = _selectedReason

    override var etcDetails by mutableStateOf("")
    override val maxEtcDetailsLength = 30 // Restricted by Taxi backend

    // MARK: - Functions
    override fun setSelectedUser(user: TaxiParticipant?) {
        _selectedUser.value = user
    }

    override fun setSelectedReason(reason: TaxiReport.Reason?) {
        _selectedReason.value = reason
    }

    override suspend fun createReport(roomID: String) {
        val user = _selectedUser.value
        val reason = _selectedReason.value

        if (user == null || reason == null) return

        val details = if (reason == TaxiReport.Reason.ETC_REASON) etcDetails else ""

        val requestModel = TaxiCreateReport(
            reportedID = user.id,
            reason = reason,
            etcDetails = details,
            time = Date(),
            roomID = roomID
        )

        viewModelScope.launch {
            try {
                taxiReportRepository.createReport(requestModel)
            } catch (e: Throwable) {
                Log.e("TaxiReportViewModel", "createReport: $e")
            }
        }
    }

    override fun handleException(error: Throwable) {
        Log.e("TaxiReportViewModel","failed to create a report: $error")
        crashlyticsHelper.recordException(
            error = error,
            alertMessage = "An unexpected error occurred while reporting a user. Please try again later."
        )
    }
}

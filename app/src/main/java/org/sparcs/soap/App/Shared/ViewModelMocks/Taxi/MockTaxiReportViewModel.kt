package org.sparcs.soap.App.Shared.ViewModelMocks.Taxi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiParticipant
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiReport
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Features.TaxiReport.TaxiReportViewModelProtocol
import org.sparcs.soap.App.Shared.Mocks.Taxi.mockList

class MockTaxiReportViewModel : TaxiReportViewModelProtocol {

    private val _room = MutableStateFlow(TaxiRoom.mockList()[0])
    override val room: StateFlow<TaxiRoom> = _room

    private val _selectedUser = MutableStateFlow<TaxiParticipant?>(null)
    override val selectedUser: StateFlow<TaxiParticipant?> = _selectedUser

    private val _selectedReason = MutableStateFlow<TaxiReport.Reason?>(null)
    override val selectedReason: StateFlow<TaxiReport.Reason?> = _selectedReason

    override val maxEtcDetailsLength: Int = 200
    override var etcDetails: String = ""

    override var alertState: AlertState? = null
    override var isAlertPresented: Boolean = false

    override fun setSelectedUser(user: TaxiParticipant?) {}
    override fun setSelectedReason(reason: TaxiReport.Reason?) {}
    override suspend fun createReport(roomID: String, onSuccess: () -> Unit) {}
    override fun handleException(error: Throwable) {}
}

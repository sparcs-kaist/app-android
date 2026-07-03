package org.sparcs.soap.app.shared.viewModelMocks.taxi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.domain.models.taxi.TaxiReport
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.taxiReport.TaxiReportViewModelProtocol
import org.sparcs.soap.app.shared.mocks.taxi.mockList

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
    override suspend fun createReport(roomID: String): Boolean { return false }
    override fun handleException(error: Throwable) {}
}

package com.sparcs.soap.Shared.ViewModelMocks.Taxi

import com.sparcs.soap.Domain.Models.Taxi.TaxiParticipant
import com.sparcs.soap.Domain.Models.Taxi.TaxiReport
import com.sparcs.soap.Domain.Models.Taxi.TaxiRoom
import com.sparcs.soap.Features.TaxiReport.TaxiReportViewModelProtocol
import com.sparcs.soap.Shared.Mocks.mockList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockTaxiReportViewModel : TaxiReportViewModelProtocol {

    private val _room = MutableStateFlow(TaxiRoom.mockList()[0])
    override val room: StateFlow<TaxiRoom> = _room

    private val _selectedUser = MutableStateFlow<TaxiParticipant?>(null)
    override val selectedUser: StateFlow<TaxiParticipant?> = _selectedUser

    private val _selectedReason = MutableStateFlow<TaxiReport.Reason?>(null)
    override val selectedReason: StateFlow<TaxiReport.Reason?> = _selectedReason

    override val maxEtcDetailsLength: Int = 200
    override var etcDetails: String = ""

    override fun setSelectedUser(user: TaxiParticipant?) {}
    override fun setSelectedReason(reason: TaxiReport.Reason?) {}
    override suspend fun createReport(roomID: String) {}
    override fun handleException(error: Throwable) {}
}

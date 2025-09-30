package com.example.soap.Features.TaxiReport

import com.example.soap.Domain.Models.Taxi.TaxiParticipant
import com.example.soap.Domain.Models.Taxi.TaxiReport
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import kotlinx.coroutines.flow.StateFlow

interface TaxiReportViewModelProtocol {
    val room: StateFlow<TaxiRoom>
    val selectedUser: StateFlow<TaxiParticipant?>
    val selectedReason: StateFlow<TaxiReport.Reason?>
    val maxEtcDetailsLength: Int
    var etcDetails: String

    fun setSelectedUser(user: TaxiParticipant?)
    fun setSelectedReason(reason: TaxiReport.Reason?)
    suspend fun createReport(roomID: String)
}
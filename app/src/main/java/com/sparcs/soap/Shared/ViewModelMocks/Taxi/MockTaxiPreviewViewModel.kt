package com.sparcs.soap.Shared.ViewModelMocks.Taxi

import com.sparcs.soap.Domain.Enums.Taxi.TaxiRoomBlockStatus
import com.sparcs.soap.Domain.Models.Taxi.TaxiParticipant
import com.sparcs.soap.Domain.Models.Taxi.TaxiUser
import com.sparcs.soap.Features.TaxiPreview.TaxiPreviewViewModelProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.util.GeoPoint

class MockTaxiPreviewViewModel() : TaxiPreviewViewModelProtocol {

    private val _taxiUser = MutableStateFlow(null)
    override val taxiUser: StateFlow<TaxiUser?> = _taxiUser

    private val _blockStatus = MutableStateFlow(TaxiRoomBlockStatus.Allow)
    override val blockStatus: StateFlow<TaxiRoomBlockStatus> = _blockStatus

    override fun isJoined(participants: List<TaxiParticipant>): Boolean {
        return false
    }

    override suspend fun calculateRoutePoints(
        source: GeoPoint,
        destination: GeoPoint
    ): List<GeoPoint> {
        return listOf(source, destination)
    }

    override fun joinRoom(id: String) {}
}

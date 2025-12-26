package org.sparcs.Shared.ViewModelMocks.Taxi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.util.GeoPoint
import org.sparcs.Domain.Enums.Taxi.TaxiRoomBlockStatus
import org.sparcs.Domain.Models.Taxi.TaxiParticipant
import org.sparcs.Domain.Models.Taxi.TaxiUser
import org.sparcs.Features.TaxiPreview.TaxiPreviewViewModelProtocol

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

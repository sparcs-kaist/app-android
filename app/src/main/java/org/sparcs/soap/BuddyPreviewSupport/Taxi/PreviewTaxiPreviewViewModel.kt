package org.sparcs.soap.BuddyPreviewSupport.Taxi

import com.kakao.vectormap.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.sparcs.soap.App.Domain.Enums.Taxi.TaxiRoomBlockStatus
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiParticipant
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiUser
import org.sparcs.soap.App.Features.TaxiPreview.TaxiPreviewViewModelProtocol

class PreviewTaxiPreviewViewModel() : TaxiPreviewViewModelProtocol {

    private val _taxiUser = MutableStateFlow(null)
    override val taxiUser: StateFlow<TaxiUser?> = _taxiUser

    private val _blockStatus = MutableStateFlow(TaxiRoomBlockStatus.Allow)
    override val blockStatus: StateFlow<TaxiRoomBlockStatus> = _blockStatus

    override var alertState: AlertState? = null
    override var isAlertPresented: Boolean = false

    override fun isJoined(participants: List<TaxiParticipant>): Boolean {
        return false
    }

    override suspend fun calculateRoutePoints(
        source: LatLng,
        destination: LatLng
    ): List<LatLng> {
        return listOf(source, destination)
    }

    override fun joinRoom(id: String) {}
}

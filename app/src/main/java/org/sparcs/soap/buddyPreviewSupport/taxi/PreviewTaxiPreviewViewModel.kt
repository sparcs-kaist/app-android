package org.sparcs.soap.buddyPreviewSupport.taxi

import com.kakao.vectormap.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.sparcs.soap.app.domain.enums.taxi.TaxiRoomBlockStatus
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.features.taxiPreview.TaxiPreviewViewModelProtocol

class PreviewTaxiPreviewViewModel : TaxiPreviewViewModelProtocol {

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

    override suspend fun joinRoom(id: String, onSuccess: () -> Unit) {}
}

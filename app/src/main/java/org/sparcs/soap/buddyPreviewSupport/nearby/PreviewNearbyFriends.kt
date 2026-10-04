package org.sparcs.soap.buddyPreviewSupport.nearby

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.sparcs.soap.app.domain.models.nearby.NearbyPeer
import org.sparcs.soap.app.domain.models.nearby.NearbyPeerState
import org.sparcs.soap.app.domain.nearby.BeaconSighting
import org.sparcs.soap.app.domain.nearby.NearbyBeaconSourceProtocol
import org.sparcs.soap.app.domain.nearby.NearbyBluetoothAvailability
import org.sparcs.soap.app.domain.usecases.nearby.NearbyFriendsManagerProtocol
import org.sparcs.soap.app.features.friends.addFriends.NearbyFriendsViewModel
import org.sparcs.soap.app.features.friends.addFriends.NearbyFriendsViewState
import org.sparcs.soap.app.shared.mocks.nearby.mockList

/** A view model whose discovery is simulated, for interactive previews. */
fun previewNearbyFriendsViewModel(
    availability: NearbyBluetoothAvailability = NearbyBluetoothAvailability.Available,
): NearbyFriendsViewModel = NearbyFriendsViewModel(
    initialState = NearbyFriendsViewState.Scanning(),
    manager = PreviewNearbyFriendsManager(),
    beacon = PreviewNearbyBeaconSource(availability)
)

class PreviewNearbyBeaconSource(
    private val availability: NearbyBluetoothAvailability = NearbyBluetoothAvailability.Available,
) : NearbyBeaconSourceProtocol {
    override val requiredPermissions: Array<String> = emptyArray()
    override val canAdvertise: Boolean = true
    override fun availability(): Flow<NearbyBluetoothAvailability> = flowOf(availability)
    override fun sightings(token: ByteArray): Flow<BeaconSighting> = emptyFlow()
}

/**
 * People appear one by one, a few of them send a request, and our own requests
 * are answered after a short delay.
 */
class PreviewNearbyFriendsManager : NearbyFriendsManagerProtocol {
    private val _peers = MutableStateFlow<List<NearbyPeer>>(emptyList())
    override val peers: StateFlow<List<NearbyPeer>> = _peers
    override val friendAdded: SharedFlow<String> = MutableSharedFlow()

    private var scope: kotlinx.coroutines.CoroutineScope? = null

    override suspend fun run() = coroutineScope {
        scope = this
        for (mock in NearbyPeer.mockList()) {
            if (_peers.value.any { it.id == mock.id }) continue
            delay(1_400)
            _peers.update { it + mock }
        }
        repeat(3) {
            delay(2_000)
            _peers.value.lastOrNull { it.state == NearbyPeerState.Idle }?.let { set(it.id, NearbyPeerState.Incoming) }
        }
        awaitCancellation()
    }

    override fun request(peerId: String) {
        set(peerId, NearbyPeerState.Requested)
        scope?.launch {
            delay(2_500)
            if (state(peerId) != NearbyPeerState.Requested) return@launch
            set(peerId, NearbyPeerState.Adding)
            delay(1_200)
            set(peerId, NearbyPeerState.Added)
        }
    }

    override fun cancel(peerId: String) = set(peerId, NearbyPeerState.Idle)

    override fun accept(peerId: String) {
        set(peerId, NearbyPeerState.Adding)
        scope?.launch {
            delay(1_200)
            set(peerId, NearbyPeerState.Added)
        }
    }

    override fun decline(peerId: String) = set(peerId, NearbyPeerState.Idle)

    private fun state(id: String) = _peers.value.firstOrNull { it.id == id }?.state

    private fun set(id: String, state: NearbyPeerState) =
        _peers.update { peers -> peers.map { if (it.id == id) it.copy(state = state) else it } }
}

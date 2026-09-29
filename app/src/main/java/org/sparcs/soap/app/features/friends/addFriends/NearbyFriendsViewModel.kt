package org.sparcs.soap.app.features.friends.addFriends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.sparcs.soap.BuildConfig
import org.sparcs.soap.app.domain.models.nearby.NearbyPeer
import org.sparcs.soap.app.domain.models.nearby.NearbyPeerState
import org.sparcs.soap.app.domain.models.nearby.NearbyUnavailableReason
import org.sparcs.soap.app.shared.mocks.nearby.mockList
import javax.inject.Inject

/**
 * Drives the nearby section of Add Friends.
 *
 * The BLE beacon and relay don't exist yet (see the cross-team nearby friends
 * plan), so this simulates both sides with mock data: people appear one by
 * one, a few of them send a request, and our own requests are answered after
 * a short delay. The public surface matches what the real implementation will
 * need — a lifecycle-bound [runDiscovery] plus the per-peer actions — so the
 * screen won't change when a `NearbyFriendsManager` replaces the simulation.
 *
 * Nothing here touches Bluetooth, and the app declares no Bluetooth
 * permissions yet; [grantPermission] stands in for the runtime prompt.
 */
@HiltViewModel
class NearbyFriendsViewModel(
    initialState: NearbyFriendsViewState,
    /** Previews and tests pass `false` so a fixed state stays put. */
    private val simulatesDiscovery: Boolean,
) : ViewModel() {

    @Inject
    constructor() : this(
        initialState = NearbyFriendsViewState.Unavailable(NearbyUnavailableReason.PermissionRequired),
        simulatesDiscovery = true
    )

    companion object {
        /**
         * The simulation must never reach real users, so the nearby section is
         * only shown in debug builds until the relay and BLE beacon ship.
         */
        val isFeatureEnabled: Boolean = BuildConfig.DEBUG

        private const val PEER_APPEAR_DELAY_MS = 1_400L
        private const val INCOMING_REQUEST_DELAY_MS = 2_000L
        private const val REPLY_DELAY_MS = 2_500L
        private const val ADD_DELAY_MS = 1_200L
        private const val SIMULATED_INCOMING_REQUESTS = 3
    }

    private val _viewState = MutableStateFlow(initialState)
    val viewState: StateFlow<NearbyFriendsViewState> = _viewState.asStateFlow()

    private val replyJobs = mutableMapOf<String, Job>()
    private var simulatedIncomingCount = 0

    // MARK: - Discovery

    /** Stands in for the Nearby devices permission prompt. */
    fun grantPermission() {
        _viewState.value = NearbyFriendsViewState.Scanning()
    }

    /**
     * Runs for as long as the calling coroutine lives, so collect it while the
     * screen is at least STARTED and let cancellation stop discovery.
     */
    suspend fun runDiscovery() {
        if (!simulatesDiscovery || !_viewState.value.isScanning) return

        for (mock in NearbyPeer.mockList()) {
            if (_viewState.value.peers.any { it.id == mock.id }) continue
            delay(PEER_APPEAR_DELAY_MS)
            appendPeer(mock)
        }

        // A few people tap us once the list has settled, so requests queue up.
        while (simulatedIncomingCount < SIMULATED_INCOMING_REQUESTS) {
            delay(INCOMING_REQUEST_DELAY_MS)
            simulatedIncomingCount += 1
            _viewState.value.peers.lastOrNull { it.state == NearbyPeerState.Idle }?.let {
                setState(NearbyPeerState.Incoming, it.id)
            }
        }
    }

    // MARK: - Actions

    fun tap(peer: NearbyPeer) {
        when (stateOf(peer.id)) {
            NearbyPeerState.Idle, NearbyPeerState.Failed -> request(peer)
            NearbyPeerState.Requested -> cancel(peer)
            NearbyPeerState.Incoming -> accept(peer)
            NearbyPeerState.Adding, NearbyPeerState.Added, null -> Unit
        }
    }

    fun request(peer: NearbyPeer) {
        val state = stateOf(peer.id)
        if (state != NearbyPeerState.Idle && state != NearbyPeerState.Failed) return
        setState(NearbyPeerState.Requested, peer.id)
        // Mock reply: they accept, then codes are exchanged and added.
        replaceReplyJob(peer.id) {
            delay(REPLY_DELAY_MS)
            if (stateOf(peer.id) != NearbyPeerState.Requested) return@replaceReplyJob
            setState(NearbyPeerState.Adding, peer.id)
            delay(ADD_DELAY_MS)
            if (stateOf(peer.id) == NearbyPeerState.Adding) setState(NearbyPeerState.Added, peer.id)
        }
    }

    fun cancel(peer: NearbyPeer) {
        if (stateOf(peer.id) != NearbyPeerState.Requested) return
        replyJobs.remove(peer.id)?.cancel()
        setState(NearbyPeerState.Idle, peer.id)
    }

    fun accept(peer: NearbyPeer) {
        if (stateOf(peer.id) != NearbyPeerState.Incoming) return
        setState(NearbyPeerState.Adding, peer.id)
        replaceReplyJob(peer.id) {
            delay(ADD_DELAY_MS)
            if (stateOf(peer.id) == NearbyPeerState.Adding) setState(NearbyPeerState.Added, peer.id)
        }
    }

    fun decline(peer: NearbyPeer) {
        if (stateOf(peer.id) != NearbyPeerState.Incoming) return
        setState(NearbyPeerState.Idle, peer.id)
    }

    // MARK: - Helpers

    private fun stateOf(id: String): NearbyPeerState? =
        _viewState.value.peers.firstOrNull { it.id == id }?.state

    private fun setState(state: NearbyPeerState, id: String) {
        _viewState.update { current ->
            if (current !is NearbyFriendsViewState.Scanning) return@update current
            current.copy(peers = current.peers.map { if (it.id == id) it.copy(state = state) else it })
        }
    }

    private fun appendPeer(peer: NearbyPeer) {
        _viewState.update { current ->
            if (current !is NearbyFriendsViewState.Scanning) return@update current
            current.copy(peers = current.peers + peer)
        }
    }

    private fun replaceReplyJob(id: String, block: suspend () -> Unit) {
        replyJobs.remove(id)?.cancel()
        replyJobs[id] = viewModelScope.launch { block() }
    }
}

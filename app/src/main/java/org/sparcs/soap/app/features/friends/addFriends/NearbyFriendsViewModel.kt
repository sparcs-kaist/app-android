package org.sparcs.soap.app.features.friends.addFriends

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.sparcs.soap.app.domain.models.nearby.NearbyPeer
import org.sparcs.soap.app.domain.models.nearby.NearbyPeerState
import org.sparcs.soap.app.domain.models.nearby.NearbyUnavailableReason
import org.sparcs.soap.app.domain.nearby.NearbyBeaconSourceProtocol
import org.sparcs.soap.app.domain.nearby.NearbyBluetoothAvailability
import org.sparcs.soap.app.domain.usecases.nearby.NearbyFriendsManagerProtocol
import timber.log.Timber
import javax.inject.Inject

/**
 * Drives the nearby section of Add Friends: follows Bluetooth availability and
 * the Nearby devices permission, and while both allow it runs a
 * [NearbyFriendsManagerProtocol] session, mirroring its peers into
 * [viewState].
 */
@HiltViewModel
class NearbyFriendsViewModel(
    initialState: NearbyFriendsViewState,
    private val manager: NearbyFriendsManagerProtocol,
    private val beacon: NearbyBeaconSourceProtocol,
) : ViewModel() {

    @Inject
    constructor(
        manager: NearbyFriendsManagerProtocol,
        beacon: NearbyBeaconSourceProtocol,
    ) : this(
        initialState = NearbyFriendsViewState.Unavailable(NearbyUnavailableReason.PermissionRequired),
        manager = manager,
        beacon = beacon
    )

    companion object {
        /** Bounded so a failing scan never trips Android's 5-starts-per-30-s throttle. */
        const val SCAN_RETRY_DELAY_MS = 10_000L
    }

    private val _viewState = MutableStateFlow(initialState)
    val viewState: StateFlow<NearbyFriendsViewState> = _viewState.asStateFlow()

    /** Emits a peer's ID whenever a friendship is created, to refresh the list. */
    val friendAdded: SharedFlow<String> get() = manager.friendAdded

    /** What the Allow button asks for. */
    val requiredPermissions: Array<String> get() = beacon.requiredPermissions

    /** Bumped to re-read availability after a permission prompt. */
    private val availabilityCheck = MutableStateFlow(0)
    private var isPermissionPermanentlyDenied = false

    // MARK: - Discovery

    /**
     * Runs for as long as the calling coroutine lives, so collect it while the
     * screen is at least STARTED and let cancellation stop discovery.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun runDiscovery() {
        availabilityCheck
            .flatMapLatest { beacon.availability() }
            .collectLatest { availability ->
                when (availability) {
                    NearbyBluetoothAvailability.Available -> {
                        isPermissionPermanentlyDenied = false
                        discover()
                    }

                    NearbyBluetoothAvailability.PermissionRequired -> _viewState.value =
                        NearbyFriendsViewState.Unavailable(permissionReason())

                    NearbyBluetoothAvailability.BluetoothOff -> _viewState.value =
                        NearbyFriendsViewState.Unavailable(NearbyUnavailableReason.BluetoothOff)

                    NearbyBluetoothAvailability.Unsupported -> _viewState.value =
                        NearbyFriendsViewState.Unavailable(NearbyUnavailableReason.Unsupported)
                }
            }
    }

    /**
     * Called with the permission prompt's outcome. [canAskAgain] is false once
     * the system stops showing the prompt, leaving only Settings.
     */
    fun onPermissionsResult(granted: Boolean, canAskAgain: Boolean) {
        isPermissionPermanentlyDenied = !granted && !canAskAgain
        if (!granted) _viewState.value = NearbyFriendsViewState.Unavailable(permissionReason())
        availabilityCheck.update { it + 1 }
    }

    private fun permissionReason(): NearbyUnavailableReason =
        if (isPermissionPermanentlyDenied) NearbyUnavailableReason.PermissionDenied
        else NearbyUnavailableReason.PermissionRequired

    private suspend fun discover() = coroutineScope {
        _viewState.value = NearbyFriendsViewState.Scanning(isVisibleToOthers = beacon.canAdvertise)
        launch {
            manager.peers.collect { peers ->
                _viewState.update { current ->
                    if (current is NearbyFriendsViewState.Scanning) current.copy(peers = peers) else current
                }
            }
        }
        while (true) {
            try {
                manager.run()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.w(e, "Nearby: discovery stopped; retrying")
            }
            delay(SCAN_RETRY_DELAY_MS)
        }
    }

    // MARK: - Actions

    fun tap(peer: NearbyPeer) {
        when (stateOf(peer.id)) {
            NearbyPeerState.Idle, NearbyPeerState.Declined, NearbyPeerState.Failed -> request(peer)
            NearbyPeerState.Requested -> cancel(peer)
            NearbyPeerState.Incoming -> accept(peer)
            NearbyPeerState.Adding, NearbyPeerState.Added, null -> Unit
        }
    }

    fun request(peer: NearbyPeer) = manager.request(peer.id)

    fun cancel(peer: NearbyPeer) = manager.cancel(peer.id)

    fun accept(peer: NearbyPeer) = manager.accept(peer.id)

    fun decline(peer: NearbyPeer) = manager.decline(peer.id)

    private fun stateOf(id: String): NearbyPeerState? =
        _viewState.value.peers.firstOrNull { it.id == id }?.state
}

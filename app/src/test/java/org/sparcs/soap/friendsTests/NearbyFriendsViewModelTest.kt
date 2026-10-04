package org.sparcs.soap.friendsTests

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.nearby.NearbyPeer
import org.sparcs.soap.app.domain.models.nearby.NearbyPeerState
import org.sparcs.soap.app.domain.models.nearby.NearbyUnavailableReason
import org.sparcs.soap.app.domain.nearby.BeaconSighting
import org.sparcs.soap.app.domain.nearby.NearbyBeaconSourceProtocol
import org.sparcs.soap.app.domain.nearby.NearbyBluetoothAvailability
import org.sparcs.soap.app.domain.usecases.nearby.NearbyFriendsManagerProtocol
import org.sparcs.soap.app.features.friends.addFriends.NearbyFriendsViewModel
import org.sparcs.soap.app.features.friends.addFriends.NearbyFriendsViewState
import org.sparcs.soap.app.features.friends.addFriends.incomingPeers
import org.sparcs.soap.app.features.friends.addFriends.peers
import org.sparcs.soap.app.shared.mocks.nearby.mockList
import org.sparcs.soap.testSupport.MainDispatcherRule

private class RecordingManager : NearbyFriendsManagerProtocol {
    override val peers = MutableStateFlow<List<NearbyPeer>>(emptyList())
    override val friendAdded: SharedFlow<String> = MutableSharedFlow()
    val calls = mutableListOf<String>()
    var runs = 0
    var failRun = false

    override suspend fun run() {
        runs += 1
        if (failRun) throw IllegalStateException("scan failed")
        awaitCancellation()
    }

    override fun request(peerId: String) { calls += "request:$peerId" }
    override fun cancel(peerId: String) { calls += "cancel:$peerId" }
    override fun accept(peerId: String) { calls += "accept:$peerId" }
    override fun decline(peerId: String) { calls += "decline:$peerId" }
}

private class ControllableBeacon(initial: NearbyBluetoothAvailability, override val canAdvertise: Boolean = true) :
    NearbyBeaconSourceProtocol {
    val state = MutableStateFlow(initial)
    override val requiredPermissions: Array<String> = arrayOf("scan", "advertise")
    override fun availability(): Flow<NearbyBluetoothAvailability> = state
    override fun sightings(token: ByteArray): Flow<BeaconSighting> = emptyFlow()
}

@OptIn(ExperimentalCoroutinesApi::class)
class NearbyFriendsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val peers = NearbyPeer.mockList()
    private val manager = RecordingManager()

    private fun viewModel(
        availability: NearbyBluetoothAvailability = NearbyBluetoothAvailability.Available,
        canAdvertise: Boolean = true,
        vararg states: NearbyPeerState,
    ): Pair<NearbyFriendsViewModel, ControllableBeacon> {
        val beacon = ControllableBeacon(availability, canAdvertise)
        manager.peers.value = peers.mapIndexed { index, peer ->
            peer.copy(state = states.getOrElse(index) { NearbyPeerState.Idle })
        }
        val viewModel = NearbyFriendsViewModel(
            NearbyFriendsViewState.Unavailable(NearbyUnavailableReason.PermissionRequired),
            manager,
            beacon
        )
        return viewModel to beacon
    }

    @Test
    fun `available bluetooth runs a session and mirrors its peers`() = runTest(mainDispatcherRule.testDispatcher) {
        val (viewModel, _) = viewModel()
        val job = launch { viewModel.runDiscovery() }
        runCurrent()

        assertEquals(1, manager.runs)
        assertEquals(peers.map { it.id }, viewModel.viewState.value.peers.map { it.id })

        manager.peers.value = peers.take(1)
        runCurrent()
        assertEquals(1, viewModel.viewState.value.peers.size)
        job.cancel()
    }

    @Test
    fun `availability maps to the unavailable reasons`() = runTest(mainDispatcherRule.testDispatcher) {
        val (viewModel, beacon) = viewModel(NearbyBluetoothAvailability.BluetoothOff)
        val job = launch { viewModel.runDiscovery() }
        runCurrent()
        assertEquals(
            NearbyFriendsViewState.Unavailable(NearbyUnavailableReason.BluetoothOff),
            viewModel.viewState.value
        )
        assertEquals(0, manager.runs)

        beacon.state.value = NearbyBluetoothAvailability.Unsupported
        runCurrent()
        assertEquals(
            NearbyFriendsViewState.Unavailable(NearbyUnavailableReason.Unsupported),
            viewModel.viewState.value
        )

        beacon.state.value = NearbyBluetoothAvailability.Available
        runCurrent()
        assertTrue(viewModel.viewState.value.isScanningState())
        assertEquals(1, manager.runs)
        job.cancel()
    }

    @Test
    fun `a permanently denied permission points to settings`() = runTest(mainDispatcherRule.testDispatcher) {
        val (viewModel, beacon) = viewModel(NearbyBluetoothAvailability.PermissionRequired)
        val job = launch { viewModel.runDiscovery() }
        runCurrent()
        assertEquals(
            NearbyFriendsViewState.Unavailable(NearbyUnavailableReason.PermissionRequired),
            viewModel.viewState.value
        )

        viewModel.onPermissionsResult(granted = false, canAskAgain = false)
        runCurrent()
        assertEquals(
            NearbyFriendsViewState.Unavailable(NearbyUnavailableReason.PermissionDenied),
            viewModel.viewState.value
        )

        beacon.state.value = NearbyBluetoothAvailability.Available
        viewModel.onPermissionsResult(granted = true, canAskAgain = true)
        runCurrent()
        assertTrue(viewModel.viewState.value.isScanningState())
        job.cancel()
    }

    @Test
    fun `devices that can't advertise scan only`() = runTest(mainDispatcherRule.testDispatcher) {
        val (viewModel, _) = viewModel(canAdvertise = false)
        val job = launch { viewModel.runDiscovery() }
        runCurrent()
        val state = viewModel.viewState.value as NearbyFriendsViewState.Scanning
        assertEquals(false, state.isVisibleToOthers)
        job.cancel()
    }

    @Test
    fun `a failed scan is retried after a pause, not in a loop`() = runTest(mainDispatcherRule.testDispatcher) {
        manager.failRun = true
        val (viewModel, _) = viewModel()
        val job = launch { viewModel.runDiscovery() }
        runCurrent()
        assertEquals(1, manager.runs)
        advanceTimeBy(NearbyFriendsViewModel.SCAN_RETRY_DELAY_MS - 1)
        runCurrent()
        assertEquals(1, manager.runs)
        advanceTimeBy(2)
        runCurrent()
        assertEquals(2, manager.runs)
        job.cancel()
    }

    @Test
    fun `tapping walks each state to its next step`() = runTest(mainDispatcherRule.testDispatcher) {
        val (viewModel, _) = viewModel(
            NearbyBluetoothAvailability.Available,
            true,
            NearbyPeerState.Idle,
            NearbyPeerState.Requested,
            NearbyPeerState.Incoming,
            NearbyPeerState.Adding,
            NearbyPeerState.Failed
        )
        val job = launch { viewModel.runDiscovery() }
        runCurrent()

        peers.forEach(viewModel::tap)

        assertEquals(
            listOf(
                "request:${peers[0].id}",
                "cancel:${peers[1].id}",
                "accept:${peers[2].id}",
                "request:${peers[4].id}"
            ),
            manager.calls
        )
        job.cancel()
    }

    @Test
    fun `incoming requests are listed oldest first`() = runTest(mainDispatcherRule.testDispatcher) {
        val (viewModel, _) = viewModel(
            NearbyBluetoothAvailability.Available,
            true,
            NearbyPeerState.Incoming,
            NearbyPeerState.Idle,
            NearbyPeerState.Incoming
        )
        val job = launch { viewModel.runDiscovery() }
        runCurrent()
        assertEquals(listOf(peers[0].id, peers[2].id), viewModel.viewState.value.incomingPeers.map { it.id })

        viewModel.accept(peers[0])
        viewModel.decline(peers[2])
        assertEquals(listOf("accept:${peers[0].id}", "decline:${peers[2].id}"), manager.calls)
        job.cancel()
    }

    @Test
    fun `stopping discovery cancels the session`() = runTest(mainDispatcherRule.testDispatcher) {
        val (viewModel, _) = viewModel()
        val job = launch { viewModel.runDiscovery() }
        runCurrent()
        job.cancel()
        runCurrent()
        assertTrue(job.isCancelled)
        assertEquals(1, manager.runs)
    }

    private fun NearbyFriendsViewState.isScanningState() = this is NearbyFriendsViewState.Scanning
}

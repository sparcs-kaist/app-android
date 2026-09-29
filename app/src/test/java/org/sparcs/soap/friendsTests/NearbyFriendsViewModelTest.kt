package org.sparcs.soap.friendsTests

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.nearby.NearbyPeer
import org.sparcs.soap.app.domain.models.nearby.NearbyPeerState
import org.sparcs.soap.app.domain.models.nearby.NearbyUnavailableReason
import org.sparcs.soap.app.features.friends.addFriends.NearbyFriendsViewModel
import org.sparcs.soap.app.features.friends.addFriends.NearbyFriendsViewState
import org.sparcs.soap.app.features.friends.addFriends.incomingPeers
import org.sparcs.soap.app.features.friends.addFriends.peers
import org.sparcs.soap.app.shared.mocks.nearby.mockList
import org.sparcs.soap.testSupport.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class NearbyFriendsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val peers = NearbyPeer.mockList()

    private fun viewModel(vararg states: NearbyPeerState, simulates: Boolean = false) = NearbyFriendsViewModel(
        initialState = NearbyFriendsViewState.Scanning(
            peers.mapIndexed { index, peer -> peer.copy(state = states.getOrElse(index) { NearbyPeerState.Idle }) }
        ),
        simulatesDiscovery = simulates
    )

    private fun NearbyFriendsViewModel.stateOf(index: Int) = viewState.value.peers[index].state

    @Test
    fun `granting permission starts scanning with nobody found`() {
        val viewModel = NearbyFriendsViewModel(
            NearbyFriendsViewState.Unavailable(NearbyUnavailableReason.PermissionRequired),
            simulatesDiscovery = false
        )
        viewModel.grantPermission()
        assertEquals(NearbyFriendsViewState.Scanning(), viewModel.viewState.value)
    }

    @Test
    fun `tapping walks each state to its next step`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = viewModel(
            NearbyPeerState.Idle,
            NearbyPeerState.Requested,
            NearbyPeerState.Incoming,
            NearbyPeerState.Adding,
            NearbyPeerState.Failed
        )

        peers.forEach(viewModel::tap)

        assertEquals(NearbyPeerState.Requested, viewModel.stateOf(0))
        assertEquals(NearbyPeerState.Idle, viewModel.stateOf(1))
        assertEquals(NearbyPeerState.Adding, viewModel.stateOf(2))
        assertEquals(NearbyPeerState.Adding, viewModel.stateOf(3))
        assertEquals(NearbyPeerState.Requested, viewModel.stateOf(4))
    }

    @Test
    fun `a request is answered then added`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = viewModel()
        viewModel.request(peers[0])

        advanceTimeBy(2_600)
        assertEquals(NearbyPeerState.Adding, viewModel.stateOf(0))
        advanceUntilIdle()
        assertEquals(NearbyPeerState.Added, viewModel.stateOf(0))
    }

    @Test
    fun `cancelling a request stops the mock reply`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = viewModel()
        viewModel.request(peers[0])
        viewModel.cancel(peers[0])
        advanceUntilIdle()
        assertEquals(NearbyPeerState.Idle, viewModel.stateOf(0))
    }

    @Test
    fun `accepting and declining only apply to incoming requests`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = viewModel(NearbyPeerState.Incoming, NearbyPeerState.Incoming, NearbyPeerState.Idle)
        assertEquals(listOf(peers[0].id, peers[1].id), viewModel.viewState.value.incomingPeers.map { it.id })

        viewModel.accept(peers[0])
        viewModel.decline(peers[1])
        viewModel.accept(peers[2])
        advanceUntilIdle()

        assertEquals(NearbyPeerState.Added, viewModel.stateOf(0))
        assertEquals(NearbyPeerState.Idle, viewModel.stateOf(1))
        assertEquals(NearbyPeerState.Idle, viewModel.stateOf(2))
        assertTrue(viewModel.viewState.value.incomingPeers.isEmpty())
    }

    @Test
    fun `simulated discovery finds everyone then receives three requests`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = NearbyFriendsViewModel(NearbyFriendsViewState.Scanning(), simulatesDiscovery = true)

        val discovery = launch { viewModel.runDiscovery() }
        advanceUntilIdle()
        discovery.join()

        assertEquals(peers.map { it.id }, viewModel.viewState.value.peers.map { it.id })
        assertEquals(3, viewModel.viewState.value.incomingPeers.size)

        // Restarting (e.g. back from the background) doesn't pile up more requests.
        launch { viewModel.runDiscovery() }
        advanceUntilIdle()
        assertEquals(3, viewModel.viewState.value.incomingPeers.size)
    }

    @Test
    fun `discovery does nothing until scanning`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = NearbyFriendsViewModel(
            NearbyFriendsViewState.Unavailable(NearbyUnavailableReason.BluetoothOff),
            simulatesDiscovery = true
        )
        viewModel.runDiscovery()
        assertEquals(
            NearbyFriendsViewState.Unavailable(NearbyUnavailableReason.BluetoothOff),
            viewModel.viewState.value
        )
    }
}

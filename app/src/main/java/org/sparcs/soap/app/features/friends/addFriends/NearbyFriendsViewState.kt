package org.sparcs.soap.app.features.friends.addFriends

import org.sparcs.soap.app.domain.models.nearby.NearbyPeer
import org.sparcs.soap.app.domain.models.nearby.NearbyPeerState
import org.sparcs.soap.app.domain.models.nearby.NearbyUnavailableReason

sealed interface NearbyFriendsViewState {
    data class Unavailable(val reason: NearbyUnavailableReason) : NearbyFriendsViewState

    /** Discovery is running; [peers] is empty until someone is found. */
    data class Scanning(val peers: List<NearbyPeer> = emptyList()) : NearbyFriendsViewState
}

val NearbyFriendsViewState.isScanning: Boolean
    get() = this is NearbyFriendsViewState.Scanning

val NearbyFriendsViewState.peers: List<NearbyPeer>
    get() = (this as? NearbyFriendsViewState.Scanning)?.peers.orEmpty()

/** Pending requests, oldest first; the first is the one shown up front. */
val NearbyFriendsViewState.incomingPeers: List<NearbyPeer>
    get() = peers.filter { it.state == NearbyPeerState.Incoming }

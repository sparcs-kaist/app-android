package org.sparcs.soap.app.domain.models.nearby

/** Someone discovered nearby on the Add Friends screen. */
data class NearbyPeer(
    /** The peer's BLE session token, hex-encoded. Only stable for one session. */
    val id: String,
    val name: String,
    val state: NearbyPeerState = NearbyPeerState.Idle,
) {
    companion object
}

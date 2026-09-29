package org.sparcs.soap.app.domain.models.nearby

/** Where a nearby peer is in the request/accept exchange. */
enum class NearbyPeerState {
    /** Discovered; nothing sent yet. */
    Idle,

    /** We asked to add them and are waiting for their answer. */
    Requested,

    /** They asked to add us. */
    Incoming,

    /** Both sides agreed; friend codes are being exchanged and added. */
    Adding,
    Added,

    /** The exchange or the add failed; can be retried. */
    Failed,
}

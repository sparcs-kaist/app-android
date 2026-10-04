package org.sparcs.soap.app.domain.nearby

import kotlinx.coroutines.flow.Flow

/** One reception of a Buddy beacon. */
class BeaconSighting(
    /** The 12-byte session token carried in the beacon UUID. */
    val token: ByteArray,
    val rssi: Int,
)

/** Whether nearby discovery can run right now, before anything is started. */
enum class NearbyBluetoothAvailability {
    Available,

    /** Nearby devices permission isn't granted (yet). */
    PermissionRequired,
    BluetoothOff,
    Unsupported,
}

/** Bluetooth Low Energy advertising and scanning of Buddy beacons. */
interface NearbyBeaconSourceProtocol {
    /** Runtime permissions discovery needs. */
    val requiredPermissions: Array<String>

    /** Emits the current availability, then again whenever the adapter turns on or off. */
    fun availability(): Flow<NearbyBluetoothAvailability>

    /**
     * Whether this device can advertise. Without it, discovery runs scan-only:
     * the user can find and request others, but others can't see them.
     */
    val canAdvertise: Boolean

    /**
     * Advertises [token] (when possible) and scans for other Buddy beacons until
     * collection stops. Fails if scanning can't start.
     */
    fun sightings(token: ByteArray): Flow<BeaconSighting>
}

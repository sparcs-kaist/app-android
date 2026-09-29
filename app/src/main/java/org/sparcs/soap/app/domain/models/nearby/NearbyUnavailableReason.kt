package org.sparcs.soap.app.domain.models.nearby

/** Why nearby discovery can't run. */
enum class NearbyUnavailableReason {
    /** Bluetooth permission hasn't been asked for yet. */
    PermissionRequired,

    /** The person declined Bluetooth access; only Settings can change it. */
    PermissionDenied,
    BluetoothOff,

    /** The device can't advertise or scan over Bluetooth Low Energy. */
    Unsupported,
}

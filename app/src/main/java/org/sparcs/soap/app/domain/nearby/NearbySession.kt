package org.sparcs.soap.app.domain.nearby

import java.security.KeyPair

/**
 * One Add Friends visit's ephemeral identity. Nothing here outlives the screen:
 * a new visit means a new key, so a new token and a new relay mailbox.
 */
class NearbySession(
    val keyPair: KeyPair,
    val ownerSecret: ByteArray,
) {
    val pubX963: ByteArray = NearbyCrypto.x963(keyPair.public)
    val token: ByteArray = NearbyCrypto.token(pubX963)
    val tokenHex: String = NearbyCrypto.hex(token)
    val lookupId: ByteArray = NearbyCrypto.lookupId(token)
    val presenceKey: ByteArray = NearbyCrypto.presenceKey(token)
    val beaconUuid: String = BeaconUuid.fromToken(token)

    /** Best effort: the key objects themselves can't be wiped on the JVM. */
    fun clear() {
        ownerSecret.fill(0)
        presenceKey.fill(0)
    }

    companion object {
        fun generate(): NearbySession =
            NearbySession(NearbyCrypto.generateKeyPair(), NearbyCrypto.randomBytes(32))
    }
}

/**
 * The token rides in a 128-bit service UUID: `b0dd1e01-TTTT-TTTT-TTTT-TTTTTTTTTTTT`.
 * Always built and parsed from the string form so byte order on air never matters.
 */
object BeaconUuid {
    const val PREFIX = "b0dd1e01"
    const val PREFIX_UUID = "b0dd1e01-0000-0000-0000-000000000000"
    const val MASK_UUID = "ffffffff-0000-0000-0000-000000000000"

    fun fromToken(token: ByteArray): String {
        require(token.size == NearbyCrypto.TOKEN_LENGTH) { "Token must be 12 bytes" }
        val hex = NearbyCrypto.hex(token)
        return "$PREFIX-${hex.substring(0, 4)}-${hex.substring(4, 8)}-${hex.substring(8, 12)}-${hex.substring(12, 24)}"
    }

    /** The token in a Buddy beacon UUID, or `null` for any other UUID. */
    fun token(uuid: String): ByteArray? {
        val hex = uuid.lowercase().replace("-", "")
        if (hex.length != 32 || !hex.startsWith(PREFIX) || hex.any { it !in "0123456789abcdef" }) return null
        return NearbyCrypto.fromHex(hex.substring(PREFIX.length))
    }
}

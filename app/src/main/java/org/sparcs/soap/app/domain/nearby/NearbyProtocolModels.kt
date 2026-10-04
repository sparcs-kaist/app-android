package org.sparcs.soap.app.domain.nearby

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Plaintext of a presence blob; only someone who heard the token can read it. */
@Serializable
data class PresenceCard(
    val v: Int = 1,
    /** base64url X9.63 public key; its SHA-256 prefix must equal the token. */
    val pub: String,
    val name: String,
    val platform: String = "android",
    /**
     * Random per app launch (never stored), so a phone that leaves Add Friends
     * and comes back with a new session replaces its old bubble instead of
     * showing twice. Optional: older cards don't carry it.
     */
    val device: String? = null,
)

@Serializable
enum class NearbyMessageType {
    @SerialName("request") Request,
    @SerialName("accept") Accept,
    @SerialName("confirm") Confirm,
    @SerialName("decline") Decline,
    @SerialName("cancel") Cancel,
}

/** Plaintext of a mailbox message body. */
@Serializable
data class NearbyMessageBody(
    val v: Int = 1,
    /** `null` for a type this version doesn't know, which receivers ignore. */
    val type: NearbyMessageType? = null,
    val msgId: String,
    val sentAt: Long,
    val inReplyTo: String? = null,
    val friendCode: String? = null,
)

object NearbyJson {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        coerceInputValues = true
    }

    /** Presence names are capped at 64 UTF-8 bytes without splitting a character. */
    fun truncatedName(name: String, maxBytes: Int = 64): String {
        val builder = StringBuilder()
        var bytes = 0
        var index = 0
        while (index < name.length) {
            val codePoint = name.codePointAt(index)
            val chars = Character.toChars(codePoint)
            val size = String(chars).toByteArray(Charsets.UTF_8).size
            if (bytes + size > maxBytes) break
            builder.append(chars)
            bytes += size
            index += chars.size
        }
        return builder.toString()
    }
}

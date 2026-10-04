package org.sparcs.soap.app.networking.requestDTO.nearby

/** All binary fields are base64url without padding; the relay rejects anything else. */
data class PutPresenceRequestDTO(
    val blob: String,
)

data class BatchGetPresencesRequestDTO(
    val lookupIds: List<String>,
)

data class PostNearbyMessageRequestDTO(
    val header: String,
    val body: String,
)

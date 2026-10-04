package org.sparcs.soap.app.networking.responseDTO.nearby

import org.sparcs.soap.app.domain.nearby.NearbyCrypto
import org.sparcs.soap.app.domain.repositories.nearby.RelayMessage
import org.sparcs.soap.app.domain.repositories.nearby.RelayMessagesPage

data class PresenceExpiryResponseDTO(
    val expiresAt: String,
)

data class PresenceItemDTO(
    val lookupId: String,
    val blob: String,
    val expiresAt: String,
)

data class BatchGetPresencesResponseDTO(
    val items: List<PresenceItemDTO>,
)

data class MessageCreatedResponseDTO(
    val messageId: String,
)

data class RelayMessageDTO(
    val id: String,
    val header: String,
    val body: String,
    val createdAt: String,
) {
    fun toModel(): RelayMessage = RelayMessage(
        id = id,
        header = NearbyCrypto.fromBase64Url(header),
        body = NearbyCrypto.fromBase64Url(body),
    )
}

data class MessagesPageResponseDTO(
    val messages: List<RelayMessageDTO>,
    val cursor: String,
) {
    fun toModel(): RelayMessagesPage = RelayMessagesPage(
        messages = messages.map { it.toModel() },
        cursor = cursor,
    )
}

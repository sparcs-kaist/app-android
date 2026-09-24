package org.sparcs.soap.app.networking.responseDTO.taxi

import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.taxi.TaxiChat
import org.sparcs.soap.app.shared.extensions.toDate
import java.util.Date
import java.util.UUID

data class TaxiChatDTO(
    @SerializedName("roomId")
    val roomID: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("authorId")
    val authorID: String?,

    @SerializedName("authorName")
    val authorName: String?,

    @SerializedName("authorProfileUrl")
    val authorProfileURL: String?,

    @SerializedName("authorIsWithdrew")
    val authorIsWithdrew: Boolean?,

    @SerializedName("content")
    val content: String,

    @SerializedName("time")
    val time: String,

    @SerializedName("isValid")
    val isValid: Boolean,

    @SerializedName("inOutNames")
    val inOutNames: List<String>?,

    @SerializedName("settlementMeta")
    val settlementMeta: SettlementMetaDTO?
) {
    data class SettlementMetaDTO(
        @SerializedName("total")
        val total: Int,

        @SerializedName("perPerson")
        val perPerson: Int,

        @SerializedName("participantCount")
        val participantCount: Int
    )

    fun toModel(): TaxiChat {
        val identityString = "${authorID ?: "system"}_${content}_${time}"
        val deterministicId = UUID.nameUUIDFromBytes(identityString.toByteArray())

        return TaxiChat(
            id = deterministicId,
            roomID = roomID,
            type = TaxiChat.ChatType.fromRawValue(type),
            authorID = authorID,
            authorName = authorName,
            authorProfileURL = authorProfileURL,
            authorIsWithdrew = authorIsWithdrew,
            content = content,
            time = time.toDate() ?: Date(),
            isValid = isValid,
            inOutNames = inOutNames,
            settlementMeta = settlementMeta?.takeIf { it.total > 0 && it.perPerson >= 0 && it.participantCount > 0 }?.let {
                TaxiChat.SettlementMeta(
                    total = it.total,
                    perPerson = it.perPerson,
                    participantCount = it.participantCount
                )
            } ?: if (type == "settlement") parseSettlementContent(content) else null
        )
    }
}

// Accept metadata both as a separate response field and as JSON message content.
private fun parseSettlementContent(content: String): TaxiChat.SettlementMeta? = runCatching {
    val json = JsonParser.parseString(content).asJsonObject
    fun integer(key: String): Int? = json.get(key)?.takeIf { it.isJsonPrimitive }
        ?.asJsonPrimitive?.takeIf { it.isNumber }?.asString?.toIntOrNull()
    val total = integer("total") ?: return null
    val perPerson = integer("perPerson") ?: return null
    val count = integer("participantCount") ?: return null
    if (total <= 0 || perPerson < 0 || count <= 0) return null
    TaxiChat.SettlementMeta(total, perPerson, count)
}.getOrNull()



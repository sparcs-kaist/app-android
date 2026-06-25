package org.sparcs.soap.app.networking.responseDTO.taxi

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
            settlementMeta = settlementMeta?.let {
                TaxiChat.SettlementMeta(
                    total = it.total,
                    perPerson = it.perPerson,
                    participantCount = it.participantCount
                )
            }
        )
    }
}



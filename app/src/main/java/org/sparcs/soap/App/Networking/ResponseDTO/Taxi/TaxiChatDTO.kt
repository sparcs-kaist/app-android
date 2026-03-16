package org.sparcs.soap.App.Networking.ResponseDTO.Taxi

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiChat
import org.sparcs.soap.App.Shared.Extensions.toDate
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
    val inOutNames: List<String>?
) {
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
            inOutNames = inOutNames
        )
    }
}



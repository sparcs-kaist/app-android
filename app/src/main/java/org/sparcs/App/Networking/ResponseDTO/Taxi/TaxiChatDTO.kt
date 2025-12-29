package org.sparcs.App.Networking.ResponseDTO.Taxi

import com.google.gson.annotations.SerializedName
import org.sparcs.App.Domain.Models.Taxi.TaxiChat
import org.sparcs.App.Shared.Extensions.toDate
import java.net.URL
import java.util.Date

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
        return TaxiChat(
            roomID = roomID,
            type = TaxiChat.ChatType.valueOf(type.uppercase()),
            authorID = authorID,
            authorName = authorName,
            authorProfileURL = authorProfileURL?.let { URL(it) },
            authorIsWithdrew = authorIsWithdrew,
            content = content,
            time = time.toDate() ?: Date(),
            isValid = isValid,
            inOutNames = inOutNames
        )
    }
}



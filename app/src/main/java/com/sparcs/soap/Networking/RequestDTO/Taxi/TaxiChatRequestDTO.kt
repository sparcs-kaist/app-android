package com.sparcs.soap.Networking.RequestDTO.Taxi

import com.sparcs.soap.Domain.Models.Taxi.TaxiChatRequest
import com.google.gson.annotations.SerializedName

data class TaxiChatRequestDTO(
    @SerializedName("roomId")
    val roomId: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("content")
    val content: String?
) {
    companion object {
        fun fromModel(model: TaxiChatRequest): TaxiChatRequestDTO {
            return TaxiChatRequestDTO(
                roomId = model.roomID,
                type = model.type.type,
                content = model.content
            )
        }
    }
}

package org.sparcs.App.Networking.RequestDTO.Taxi

import com.google.gson.annotations.SerializedName
import org.sparcs.App.Domain.Models.Taxi.TaxiChatRequest

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

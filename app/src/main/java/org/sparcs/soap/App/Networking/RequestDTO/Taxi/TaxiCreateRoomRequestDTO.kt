package org.sparcs.soap.App.Networking.RequestDTO.Taxi

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiCreateRoom
import org.sparcs.soap.App.Shared.Extensions.toISO8601

data class TaxiCreateRoomRequestDTO(
    @SerializedName("name")
    val name: String,

    @SerializedName("from")
    val from: String,

    @SerializedName("to")
    val to: String,

    @SerializedName("time")
    val time: String,

    @SerializedName("maxPartLength")
    val maxPartLength: Int
) {
    companion object {
        fun fromModel(model: TaxiCreateRoom): TaxiCreateRoomRequestDTO {
            return TaxiCreateRoomRequestDTO(
                name = model.title,
                from = model.source.id,
                to = model.destination.id,
                time = model.departureTime.toISO8601(),
                maxPartLength = model.capacity
            )
        }
    }
}

package com.sparcs.soap.Networking.RequestDTO.Taxi

import com.sparcs.soap.Domain.Models.Taxi.TaxiCreateRoom
import com.sparcs.soap.Shared.Extensions.toISO8601
import com.google.gson.annotations.SerializedName

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

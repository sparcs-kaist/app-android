package org.sparcs.soap.App.Networking.ResponseDTO.Taxi

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiFavoriteRoute

data class TaxiFavoriteRouteDTO(
    @SerializedName("_id")
    val id: String,

    @SerializedName("from")
    val from: TaxiLocationDTO,

    @SerializedName("to")
    val to: TaxiLocationDTO,

    @SerializedName("createdAt")
    val createdAt: String
) {
    fun toModel(): TaxiFavoriteRoute {
        return TaxiFavoriteRoute(
            id = id,
            from = from.toModel(),
            to = to.toModel()
        )
    }
}

data class TaxiFavoriteRoutesResponseDTO(
    @SerializedName("favorites")
    val favorites: List<TaxiFavoriteRouteDTO>
)

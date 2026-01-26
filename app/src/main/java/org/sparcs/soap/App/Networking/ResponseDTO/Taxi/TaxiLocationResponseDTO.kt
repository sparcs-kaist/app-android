package org.sparcs.soap.App.Networking.ResponseDTO.Taxi

import com.google.gson.annotations.SerializedName

data class TaxiLocationResponseDTO (
    @SerializedName("locations")
    val locations: List<TaxiLocationDTO>
)

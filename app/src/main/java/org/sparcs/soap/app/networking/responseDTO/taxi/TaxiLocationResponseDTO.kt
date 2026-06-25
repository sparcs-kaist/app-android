package org.sparcs.soap.app.networking.responseDTO.taxi

import com.google.gson.annotations.SerializedName

data class TaxiLocationResponseDTO (
    @SerializedName("locations")
    val locations: List<TaxiLocationDTO>
)

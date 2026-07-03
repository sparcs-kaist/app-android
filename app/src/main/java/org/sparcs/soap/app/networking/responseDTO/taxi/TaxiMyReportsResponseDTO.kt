package org.sparcs.soap.app.networking.responseDTO.taxi

import com.google.gson.annotations.SerializedName

data class TaxiMyReportsResponseDTO(
    @SerializedName("reported")
    val incoming: List<TaxiReportDTO>,

    @SerializedName("reporting")
    val outgoing: List<TaxiReportDTO>
)
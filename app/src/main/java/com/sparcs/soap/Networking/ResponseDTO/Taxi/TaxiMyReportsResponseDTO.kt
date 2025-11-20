package com.sparcs.soap.Networking.ResponseDTO.Taxi

import com.google.gson.annotations.SerializedName

data class TaxiMyReportsResponseDTO(
    @SerializedName("reported")
    val incoming: List<TaxiReportDTO>,

    @SerializedName("reporting")
    val outgoing: List<TaxiReportDTO>
)
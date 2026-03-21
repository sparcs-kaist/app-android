package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName

data class TimetableSummaryListDTO(
    @SerializedName("timetables")
    val timetables: List<TimetableSummaryDTO>
)

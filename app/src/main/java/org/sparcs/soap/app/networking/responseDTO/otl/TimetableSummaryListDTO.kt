package org.sparcs.soap.app.networking.responseDTO.otl

import com.google.gson.annotations.SerializedName

data class TimetableSummaryListDTO(
    @SerializedName("timetables")
    val timetables: List<TimetableSummaryDTO>
)

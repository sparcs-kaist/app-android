package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType
import org.sparcs.soap.App.Domain.Models.OTL.TimetableSummary


data class TimetableSummaryDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("year")
    val year: Int,

    @SerializedName("semester")
    val semester: Int,

    @SerializedName("timetableOrder")
    val timetableOrder: Int
) {
    fun toModel(): TimetableSummary {
        return TimetableSummary(
            id = id,
            title = name,
            year = year,
            semester = SemesterType.fromRawValue(semester),
        )
    }
}

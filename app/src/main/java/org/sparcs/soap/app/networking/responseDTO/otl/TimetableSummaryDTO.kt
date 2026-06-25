package org.sparcs.soap.app.networking.responseDTO.otl

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.enums.otl.SemesterType
import org.sparcs.soap.app.domain.models.otl.TimetableSummary


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

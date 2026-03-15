package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import org.sparcs.soap.App.Domain.Models.OTL.TimetableListItem

data class TimetableListItemDTO(
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
    fun toModel(): TimetableListItem {
        return TimetableListItem(
            id = id,
            name = name,
            year = year,
            semester = SemesterType.fromRawValue(semester),
            timetableOrder = timetableOrder
        )
    }
}

data class TimetableListDTO(
    @SerializedName("timetables")
    val timetables: List<TimetableListItemDTO>
)

data class TimetableDTO(
    @SerializedName("lectures")
    val lectures: List<LectureDTO>
) {
    fun toModel(): Timetable {
        return Timetable(
            lectures = lectures.map { it.toModel() }
        )
    }
}
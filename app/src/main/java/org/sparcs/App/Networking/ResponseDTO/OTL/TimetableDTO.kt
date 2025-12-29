package org.sparcs.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.App.Domain.Models.OTL.Timetable

data class TimetableDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("lectures")
    val lectures: List<LectureDTO>
) {
    fun toModel(): Timetable {
        return Timetable(
            id = id.toString(),
            lectures = lectures.map { it.toModel() }
        )
    }
}
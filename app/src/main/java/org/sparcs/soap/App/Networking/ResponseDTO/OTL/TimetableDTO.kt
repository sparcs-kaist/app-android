package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Models.OTL.Timetable


data class TimetableDTO(
    @SerializedName("lectures")
    val lectures: List<LectureDTO>
) {
    fun toModel(id: String): Timetable {
        return Timetable(
            id = id,
            lectures = lectures.map { it.toModel() }
        )
    }
}
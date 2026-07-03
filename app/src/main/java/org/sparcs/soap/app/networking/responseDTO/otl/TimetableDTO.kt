package org.sparcs.soap.app.networking.responseDTO.otl

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.otl.Timetable


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
package com.example.soap.Networking.ResponseDTO.OTL

import com.example.soap.Domain.Models.OTL.Timetable
import com.google.gson.annotations.SerializedName

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
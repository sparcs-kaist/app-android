package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Enums.OTL.LectureType
import org.sparcs.soap.App.Domain.Models.OTL.CourseLecture

data class CourseLectureDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("lectures")
    val lectures: List<LectureDTO>,

    @SerializedName("completed")
    val completed: Boolean
) {
    fun toModel(): CourseLecture {
        return CourseLecture(
            id = id,
            name = name,
            code = code,
            type = LectureType.fromString(type),
            lectures = lectures.map { it.toModel() },
            completed = completed
        )
    }
}
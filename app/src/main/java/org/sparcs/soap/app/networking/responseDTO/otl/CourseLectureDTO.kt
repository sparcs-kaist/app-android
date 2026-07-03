package org.sparcs.soap.app.networking.responseDTO.otl

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.enums.otl.LectureType
import org.sparcs.soap.app.domain.models.otl.CourseLecture

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
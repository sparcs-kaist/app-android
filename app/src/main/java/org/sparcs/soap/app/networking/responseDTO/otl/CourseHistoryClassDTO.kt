package org.sparcs.soap.app.networking.responseDTO.otl

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.otl.CourseHistoryClass


data class CourseHistoryClassDTO(
    @SerializedName("lectureId")
    val lectureID: Int,

    @SerializedName("subtitle")
    val subtitle: String,

    @SerializedName("classNo")
    val classNo: String,

    @SerializedName("professors")
    val professors: List<ProfessorDTO>,
) {
    fun toModel(): CourseHistoryClass {
        return CourseHistoryClass(
            lectureID = lectureID,
            subtitle = subtitle,
            section = classNo,
            professors = professors.map { it.toModel() }
        )
    }
}

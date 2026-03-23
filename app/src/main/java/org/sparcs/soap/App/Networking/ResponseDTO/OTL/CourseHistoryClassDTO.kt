package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Models.OTL.CourseHistoryClass


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

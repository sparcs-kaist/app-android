package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType
import org.sparcs.soap.App.Domain.Models.OTL.CourseHistory

data class CourseHistoryDTO(
    @SerializedName("year")
    val year: Int,

    @SerializedName("semester")
    val semester: Int,

    @SerializedName("classes")
    val classes: List<CourseHistoryClassDTO>,

    @SerializedName("myLectureId")
    val myLectureID: Int?
) {
    fun toModel(): CourseHistory {
        return CourseHistory(
            year = year,
            semester = SemesterType.fromRawValue(semester),
            classes = classes.map { it.toModel() },
            myLectureID = myLectureID
        )
    }
}

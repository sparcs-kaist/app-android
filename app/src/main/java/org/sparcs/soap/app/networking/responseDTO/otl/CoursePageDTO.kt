package org.sparcs.soap.app.networking.responseDTO.otl

import com.google.gson.annotations.SerializedName

data class CoursePageDTO(
    @SerializedName("courses")
    val courses: List<CourseSummaryDTO>,

    @SerializedName("totalCount")
    val totalCount: Int
)
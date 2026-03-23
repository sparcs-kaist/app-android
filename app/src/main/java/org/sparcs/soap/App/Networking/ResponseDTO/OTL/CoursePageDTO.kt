package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName

data class CoursePageDTO(
    @SerializedName("courses")
    val courses: List<CourseSummaryDTO>,

    @SerializedName("totalCount")
    val totalCount: Int
)
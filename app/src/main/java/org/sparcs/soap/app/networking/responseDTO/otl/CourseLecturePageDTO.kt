package org.sparcs.soap.app.networking.responseDTO.otl

import com.google.gson.annotations.SerializedName

data class CourseLecturePageDTO(
    @SerializedName("courses")
    val courses: List<CourseLectureDTO>,
)
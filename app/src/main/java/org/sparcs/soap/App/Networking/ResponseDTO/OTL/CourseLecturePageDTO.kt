package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName

data class CourseLecturePageDTO(
    @SerializedName("courses")
    val courses: List<CourseLectureDTO>,
)
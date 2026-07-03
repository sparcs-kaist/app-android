package org.sparcs.soap.app.networking.responseDTO.otl

import com.google.gson.annotations.SerializedName

data class SemesterListDTO(
    @SerializedName("semesters")
    val semesters: List<SemesterDTO>
)
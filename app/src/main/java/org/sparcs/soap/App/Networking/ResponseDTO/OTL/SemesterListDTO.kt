package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName

data class SemesterListDTO(
    @SerializedName("semesters")
    val semesters: List<SemesterDTO>
)
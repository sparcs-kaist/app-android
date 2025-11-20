package com.sparcs.soap.Networking.ResponseDTO.OTL

import com.sparcs.soap.Domain.Helpers.LocalizedString
import com.sparcs.soap.Domain.Models.OTL.Department
import com.google.gson.annotations.SerializedName

data class DepartmentDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("name_en")
    val enName: String,

    @SerializedName("code")
    val code: String
) {
    fun toModel(): Department = Department(
        id = id,
        name = LocalizedString(
            mapOf("ko" to name, "en" to enName)
        ),
        code = code
    )
}
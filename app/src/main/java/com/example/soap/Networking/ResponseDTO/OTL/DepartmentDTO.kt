package com.example.soap.Networking.ResponseDTO.OTL

import com.example.soap.Domain.Helpers.LocalizedString
import com.example.soap.Domain.Models.OTL.Department
import kotlinx.serialization.SerialName

data class DepartmentDTO(
    @SerialName("id")
    val id: Int,

    @SerialName("name")
    val name: String,

    @SerialName("name_en")
    val enName: String,

    @SerialName("code")
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
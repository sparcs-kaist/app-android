package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Helpers.LocalizedString
import org.sparcs.soap.App.Domain.Models.OTL.Department

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
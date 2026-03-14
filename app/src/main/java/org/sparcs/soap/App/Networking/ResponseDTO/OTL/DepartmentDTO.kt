package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Models.OTL.Department

data class DepartmentDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
) {
    fun toModel(): Department = Department(
        id = id,
        name = name
    )
}
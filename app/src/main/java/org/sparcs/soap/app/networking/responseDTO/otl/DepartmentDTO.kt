package org.sparcs.soap.app.networking.responseDTO.otl

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.otl.Department

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
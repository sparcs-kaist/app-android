package org.sparcs.soap.app.networking.responseDTO.otl

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.otl.DepartmentOption

data class DepartmentOptionListDTO(@SerializedName("departments") val departments: List<DepartmentOptionDTO>)
data class DepartmentOptionDTO(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
) {
    fun toModel() = DepartmentOption(id, name, code)
}

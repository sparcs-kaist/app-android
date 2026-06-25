package org.sparcs.soap.app.networking.responseDTO.otl

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.otl.Professor

data class ProfessorDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,
) {
    fun toModel(): Professor = Professor(
        id = id,
        name = name
    )
}
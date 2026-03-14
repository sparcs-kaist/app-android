package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Models.OTL.Professor

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
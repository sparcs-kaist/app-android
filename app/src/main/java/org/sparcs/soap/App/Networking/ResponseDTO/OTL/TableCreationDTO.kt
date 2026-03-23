package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Models.OTL.TimetableCreation

data class TableCreationDTO(
    @SerializedName("id")
    val id: Int
) {
    fun toModel(): TimetableCreation {
        return TimetableCreation(id = id)
    }
}
package org.sparcs.soap.app.networking.responseDTO.otl

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.otl.TimetableCreation

data class TableCreationDTO(
    @SerializedName("id")
    val id: Int
) {
    fun toModel(): TimetableCreation {
        return TimetableCreation(id = id)
    }
}
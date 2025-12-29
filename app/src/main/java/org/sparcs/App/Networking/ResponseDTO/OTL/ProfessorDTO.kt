package org.sparcs.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.App.Domain.Helpers.LocalizedString
import org.sparcs.App.Domain.Models.OTL.Professor

data class ProfessorDTO(
    @SerializedName("professor_id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("name_en")
    val enName: String,

    @SerializedName("review_total_weight")
    val reviewTotalWeight: Double
) {
    fun toModel(): Professor = Professor(
        id = id,
        name = LocalizedString(mapOf("ko" to name, "en" to enName)),
        reviewTotalWeight = reviewTotalWeight
    )
}
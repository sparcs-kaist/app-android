package com.example.soap.Networking.ResponseDTO.OTL

import com.example.soap.Domain.Helpers.LocalizedString
import com.example.soap.Domain.Models.OTL.Professor
import kotlinx.serialization.SerialName

data class ProfessorDTO(
    @SerialName("professor_id")
    val id: Int,

    @SerialName("name")
    val name: String,

    @SerialName("name_en")
    val enName: String,

    @SerialName("review_total_weight")
    val reviewTotalWeight: Double
) {
    fun toModel(): Professor = Professor(
        id = id,
        name = LocalizedString(mapOf("ko" to name, "en" to enName)),
        reviewTotalWeight = reviewTotalWeight
    )
}
package com.example.soap.Networking.ResponseDTO.OTL

import com.example.soap.Domain.Helpers.LocalizedString
import com.example.soap.Domain.Models.OTL.Professor
import com.google.gson.annotations.SerializedName

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
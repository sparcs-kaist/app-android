package com.sparcs.soap.Networking.ResponseDTO.Ara

import com.sparcs.soap.Domain.Helpers.LocalizedString
import com.sparcs.soap.Domain.Models.Ara.AraBoardGroup
import com.google.gson.annotations.SerializedName

data class AraBoardGroupDTO (
    @SerializedName("id")
    val id: Int,

    @SerializedName("ko_name")
    val koName: String,

    @SerializedName("en_name")
    val enName: String,

    @SerializedName("slug")
    val slug: String
) {
    fun toModel(): AraBoardGroup {
        return AraBoardGroup(
            id = id,
            name = LocalizedString(
                mapOf(
                    "ko" to koName,
                    "en" to enName
                )
            ),
            slug = slug
        )
    }

}
package com.sparcs.soap.Networking.ResponseDTO.Ara

import com.sparcs.soap.Domain.Helpers.LocalizedString
import com.sparcs.soap.Domain.Models.Ara.AraBoardTopic
import com.google.gson.annotations.SerializedName

data class AraBoardTopicDTO (
    @SerializedName("id")
    val id: Int,

    @SerializedName("ko_name")
    val koName: String,

    @SerializedName("en_name")
    val enName: String,

    @SerializedName("slug")
    val slug: String
) {
    fun toModel(): AraBoardTopic {
        return AraBoardTopic(
            id = id,
            name = LocalizedString(
                mapOf(
                    "ko" to koName,
                    "en" to enName
                )
            ),
            slug =slug
        )
    }
}

package org.sparcs.soap.App.Networking.ResponseDTO.Ara

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Helpers.LocalizedString
import org.sparcs.soap.App.Domain.Models.Ara.AraBoardGroup

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
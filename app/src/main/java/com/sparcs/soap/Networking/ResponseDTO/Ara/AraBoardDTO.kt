package com.sparcs.soap.Networking.ResponseDTO.Ara

import com.sparcs.soap.Domain.Helpers.LocalizedString
import com.sparcs.soap.Domain.Models.Ara.AraBoard
import com.google.gson.annotations.SerializedName

data class AraBoardDTO (
    @SerializedName("id")
    val id: Int,

    @SerializedName("slug")
    val slug: String,

    @SerializedName("ko_name")
    val koName: String,

    @SerializedName("en_name")
    val enName: String,

    @SerializedName("is_readonly")
    val isReadOnly: Boolean,

    @SerializedName("group")
    val group: AraBoardGroupDTO,

    @SerializedName("topics")
    val topics: List<AraBoardTopicDTO>?,

    @SerializedName("user_readable")
    val userReadable: Boolean?,

    @SerializedName("user_writable")
    val userWritable: Boolean?

) {
    fun toModel(): AraBoard {
        return AraBoard(
            id = id,
            slug = slug,
            name = LocalizedString(
                mapOf(
                    "ko" to koName,
                    "en" to enName
                )
            ),
            group = group.toModel(),
            topics = topics?.map { it.toModel() } ?: emptyList(),
            isReadOnly = isReadOnly,
            userReadable = userReadable,
            userWritable = userWritable
        )
    }

}
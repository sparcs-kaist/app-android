package org.sparcs.soap.app.networking.responseDTO.ara

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.helpers.LocalizedString
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraBoardGroup

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
    val group: AraBoardGroupDTO?,

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
            group = group?.toModel() ?: AraBoardGroup.Empty,
            topics = topics?.map { it.toModel() } ?: emptyList(),
            isReadOnly = isReadOnly,
            userReadable = userReadable,
            userWritable = userWritable
        )
    }

}
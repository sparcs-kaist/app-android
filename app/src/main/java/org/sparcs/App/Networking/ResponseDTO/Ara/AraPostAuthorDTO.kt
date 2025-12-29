package org.sparcs.App.Networking.ResponseDTO.Ara

import kotlinx.serialization.SerialName
import org.sparcs.App.Domain.Models.Ara.AraPostAuthor

data class AraPostAuthorDTO(
    @SerialName("id")
    val id: Any,

    @SerialName("username")
    val username: String,

    @SerialName("profile")
    val profile: AraPostAuthorProfileDTO,

    @SerialName("is_blocked")
    val isBlocked: Boolean?
) {

    fun toModel(): AraPostAuthor {
        return AraPostAuthor(
            id = id.toString(),
            username = username,
            profile = profile.toModel(),
            isBlocked = isBlocked
        )
    }
}


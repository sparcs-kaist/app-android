package org.sparcs.soap.app.networking.responseDTO.ara

import kotlinx.serialization.SerialName
import org.sparcs.soap.app.domain.models.ara.AraPostAuthor

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


package org.sparcs.soap.app.networking.responseDTO.ara

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.ara.AraPostAuthor

data class AraPostAuthorDTO(
    @SerializedName("id")
    val id: Any,

    @SerializedName("username")
    val username: String,

    @SerializedName("profile")
    val profile: AraPostAuthorProfileDTO,

    @SerializedName("is_blocked")
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


package com.example.soap.Networking.ResponseDTO.Ara

import com.example.soap.Domain.Models.Ara.AraPostAuthor
import kotlinx.serialization.SerialName

data class AraPostAuthorDTO(
    @SerialName("id")
    val id: String,

    @SerialName("username")
    val username: String,

    @SerialName("profile")
    val profile: AraPostAuthorProfileDTO,

    @SerialName("is_blocked")
    val isBlocked: Boolean?
) {
    fun toModel(): AraPostAuthor {
        return AraPostAuthor(
            id = id,
            username = username,
            profile = profile.toModel(),
            isBlocked = isBlocked
        )
    }
}


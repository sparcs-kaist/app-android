package com.example.soap.Networking.ResponseDTO.Ara

import com.example.soap.Domain.Models.Ara.AraPostAuthor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
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
    // id could be Int or String.

    //todo - json ~~ 돌아가는 거 보고 확인하기. 일단은 toModel만!!
    fun toModel(): AraPostAuthor {
        return AraPostAuthor(
            id = id,
            username = username,
            profile = profile.toModel(),
            isBlocked = isBlocked
        )
    }
}


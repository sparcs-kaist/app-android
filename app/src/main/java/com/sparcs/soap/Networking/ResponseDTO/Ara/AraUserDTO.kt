package com.sparcs.soap.Networking.ResponseDTO.Ara

import com.sparcs.soap.Domain.Models.Ara.AraUser
import com.sparcs.soap.Shared.Extensions.toDate
import com.google.gson.annotations.SerializedName

data class AraUserDTO(
    @SerializedName("user")
    val id: Int,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("nickname_updated_at")
    val nicknameUpdatedAt: String,

    @SerializedName("see_sexual")
    val allowNSFW: Boolean,

    @SerializedName("see_social")
    val allowPolitical: Boolean
){
    fun toModel(): AraUser {
        return AraUser(
            id = id,
            nickname = nickname,
            nicknameUpdatedAt = nicknameUpdatedAt.toDate(),
            allowNSFW = allowNSFW,
            allowPolitical = allowPolitical
        )
    }
}
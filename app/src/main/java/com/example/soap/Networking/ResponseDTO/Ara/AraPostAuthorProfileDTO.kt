package com.example.soap.Networking.ResponseDTO.Ara

import com.example.soap.Domain.Models.Ara.AraPostAuthorProfile
import com.google.gson.annotations.SerializedName
import java.net.URL

data class AraPostAuthorProfileDTO(
    @SerializedName("user")
    val id: Any,

    @SerializedName("picture")
    val profilePictureURL: String?,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("is_official")
    val isOfficial: Boolean?,

    @SerializedName("is_school_admin")
    val isSchoolAdmin: Boolean?
) {
    private val userId: String
        get() = when (id) {
            is Number -> id.toString()
            is String -> id
            else -> throw IllegalArgumentException("Unexpected type for user: $id")
        }

    fun toModel(): AraPostAuthorProfile =
        AraPostAuthorProfile(
            id = userId,
            profilePictureURL = profilePictureURL?.let { try { URL(it) } catch (e: Exception) { null } },
            nickname = nickname,
            isOfficial = isOfficial,
            isSchoolAdmin = isSchoolAdmin
        )
}

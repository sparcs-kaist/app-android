package com.sparcs.soap.Networking.ResponseDTO.Taxi

import com.google.gson.annotations.SerializedName
import com.sparcs.soap.Domain.Models.Taxi.TaxiUser
import com.sparcs.soap.Shared.Extensions.toDate
import java.net.URL
import java.util.Date

data class TaxiUserDTO(
    @SerializedName("id")
    val id: String,

    @SerializedName("oid")
    val oid: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("phoneNumber")
    val phoneNumber: String?,

    @SerializedName("badge")
    val badge: Boolean,

    @SerializedName("residence")
    val residence: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("withdraw")
    val withdraw: Boolean,

    @SerializedName("ban")
    val ban: Boolean,

    @SerializedName("agreeOnTermsOfService")
    val agreeOnTermsOfService: Boolean,

    @SerializedName("joinat")
    val joinAt: String,

    @SerializedName("profileImgUrl")
    val profileImageURL: String,

    @SerializedName("account")
    val account: String
) {
    fun toModel(): TaxiUser {
        return TaxiUser(
            id = id,
            oid = oid,
            name = name,
            nickname = nickname,
            phoneNumber = phoneNumber,
            badge = badge,
            residence = residence,
            email = email,
            withdraw = withdraw,
            ban = ban,
            agreeOnTermsOfService = agreeOnTermsOfService,
            joinedAt = joinAt.toDate() ?: Date(),
            profileImageURL = URL(profileImageURL),
            account = account
        )
    }
}

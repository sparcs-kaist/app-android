package com.example.soap.Networking.ResponseDTO.Taxi

import com.example.soap.Domain.Models.Taxi.TaxiReport
import com.example.soap.Domain.Models.Taxi.TaxiReportedUser
import com.example.soap.Shared.Extensions.toDate
import com.google.gson.annotations.SerializedName
import java.net.URL
import java.util.Date

data class TaxiReportedUserDTO(
    @SerializedName("id")
    val id: String,

    @SerializedName("_id")
    val oid: String,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("profileImageUrl")
    val profileImageURL: String,

    @SerializedName("withdraw")
    val withdraw: Boolean
) {
    fun toModel(): TaxiReportedUser {
        return TaxiReportedUser(
            id = id,
            oid = oid,
            nickname = nickname,
            profileImageUrl = if (profileImageURL.isNotBlank()) URL(profileImageURL) else null,
            withdraw = withdraw
        )
    }
}

data class TaxiReportDTO(
    @SerializedName("_id")
    val id: String,

    @SerializedName("creatorId")
    val creatorID: String,

    @SerializedName("reportedId")
    val reportedUser: TaxiReportedUserDTO,

    @SerializedName("type")
    val reason: String,

    @SerializedName("etcDetail")
    val etcDetails: String,

    @SerializedName("time")
    val time: String,

    @SerializedName("roomId")
    val roomID: String
) {
    fun toModel(): TaxiReport {
        return TaxiReport(
            id = id,
            creatorId = creatorID,
            reportedUser = reportedUser.toModel(),
            reason = TaxiReport.Reason.from(reason),
            etcDetails = etcDetails,
            time = time.toDate() ?: Date(),
            roomId = roomID
        )
    }
}
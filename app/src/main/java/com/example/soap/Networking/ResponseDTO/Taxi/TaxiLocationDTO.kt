package com.example.soap.Networking.ResponseDTO.Taxi

import com.example.soap.Domain.Helpers.LocalizedString
import com.example.soap.Domain.Models.Taxi.TaxiLocation
import com.google.gson.annotations.SerializedName

data class TaxiLocationDTO(
    @SerializedName("_id")
    val id: String,

    @SerializedName("enName")
    val enName: String,

    @SerializedName("koName")
    val koName: String,

    @SerializedName("priority")
    val priority: Double?,

    @SerializedName("isValid")
    val isValid: Boolean?,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double
) {
    fun toModel(): TaxiLocation {
        return TaxiLocation(
            id = id,
            title = LocalizedString(
                mapOf(
                    "ko" to koName,
                    "en" to enName
                )
            ),
            priority = priority,
            latitude = latitude,
            longitude = longitude
        )
    }
}

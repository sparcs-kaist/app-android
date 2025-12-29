package org.sparcs.App.Networking.ResponseDTO.Taxi

import com.google.gson.annotations.SerializedName
import org.sparcs.App.Domain.Helpers.LocalizedString
import org.sparcs.App.Domain.Models.Taxi.TaxiLocation

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

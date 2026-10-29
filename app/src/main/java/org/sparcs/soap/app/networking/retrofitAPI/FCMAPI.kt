package org.sparcs.soap.app.networking.retrofitAPI

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.networking.requestDTO.NotificationPreferenceUpdateRequestDTO
import org.sparcs.soap.app.networking.requestDTO.RegisterDeviceRequestDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FCMApi {

    @POST("notification/device_info")
    suspend fun registerDevice(
        @Body body: RegisterDeviceRequestDTO
    ): Response<Unit>

    @DELETE("notification/device_info")
    suspend fun unregisterDevice(
        @Query("device_uuid") deviceUUID: String
    ): Response<Unit>

    @GET("notification/preferences")
    suspend fun getNotificationPreferences(
        @Query("device_uuid") deviceUUID: String
    ): Response<NotificationPreferencesResponseDTO>

    @PUT("notification/preferences/{category}")
    suspend fun setNotificationPreference(
        @Path("category") category: String,
        @Body body: NotificationPreferenceUpdateRequestDTO
    ): Response<Unit>
}

data class NotificationPreferencesResponseDTO(
    @SerializedName("preferences") val preferences: List<NotificationPreferenceDTO>
)

data class NotificationPreferenceDTO(
    @SerializedName("category") val category: String,
    @SerializedName("is_enabled") val isEnabled: Boolean
)

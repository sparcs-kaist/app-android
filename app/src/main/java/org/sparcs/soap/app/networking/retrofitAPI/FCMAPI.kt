package org.sparcs.soap.app.networking.retrofitAPI

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.networking.requestDTO.NotificationPreferenceUpdateRequestDTO
import org.sparcs.soap.app.networking.requestDTO.RegisterDeviceRequestDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FCMApi {

    @POST("notifications/token")
    suspend fun updateNotificationToken(
        @Body body: NotificationTokenUpdateRequestDTO
    ): Response<Unit>
    @POST("notification/device_info")
    suspend fun registerDevice(
        @Body body: RegisterDeviceRequestDTO
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

data class NotificationTokenUpdateRequestDTO(
    @SerializedName("fcm_token") val fcmToken: String,
    @SerializedName("device_token") val deviceToken: String
)
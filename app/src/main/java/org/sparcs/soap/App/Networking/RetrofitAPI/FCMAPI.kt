package org.sparcs.soap.App.Networking.RetrofitAPI

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Networking.RequestDTO.ManageAlertRequestDTO
import org.sparcs.soap.App.Networking.RequestDTO.RegisterDeviceRequestDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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

    @POST("notification/manage_alert")
    suspend fun manageAlert(
        @Body body: ManageAlertRequestDTO
    ): Response<Unit>

    @GET("notification/manage_alert")
    suspend fun getAlertStatus(
        @Query("device_uuid") deviceUUID: String
    ): Response<List<AlertManagerResponseDTO>>
}

data class AlertManagerResponseDTO(
    @SerializedName("service_name") val serviceName: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("device_uuid") val deviceUuid: String
)

data class NotificationTokenUpdateRequestDTO(
    @SerializedName("fcm_token") val fcmToken: String,
    @SerializedName("device_token") val deviceToken: String
)
package org.sparcs.soap.App.Networking.RequestDTO

import com.google.gson.annotations.SerializedName

data class RegisterDeviceRequestDTO(
    @SerializedName("device_uuid")
    val deviceUUID: String,

    @SerializedName("fcm_token")
    val fcmToken: String,

    @SerializedName("device_name")
    val deviceName: String,

    @SerializedName("app_language")
    val language: String
)

data class ManageAlertRequestDTO(
    @SerializedName("service")
    val service: Int,

    @SerializedName("is_active")
    val isActive: Boolean
)
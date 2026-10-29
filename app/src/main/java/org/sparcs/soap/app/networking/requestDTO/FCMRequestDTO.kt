package org.sparcs.soap.app.networking.requestDTO

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

data class NotificationPreferenceUpdateRequestDTO(
    @SerializedName("device_uuid")
    val deviceUUID: String,

    @SerializedName("is_enabled")
    val isEnabled: Boolean
)
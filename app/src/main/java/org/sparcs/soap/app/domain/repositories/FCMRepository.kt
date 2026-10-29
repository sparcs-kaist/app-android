package org.sparcs.soap.app.domain.repositories

import org.sparcs.soap.app.domain.helpers.NotificationCategory
import org.sparcs.soap.app.networking.requestDTO.NotificationPreferenceUpdateRequestDTO
import org.sparcs.soap.app.networking.requestDTO.RegisterDeviceRequestDTO
import org.sparcs.soap.app.networking.retrofitAPI.FCMApi
import org.sparcs.soap.app.networking.retrofitAPI.NotificationTokenUpdateRequestDTO
import timber.log.Timber
import javax.inject.Inject

interface FCMRepositoryProtocol {

    suspend fun register(
        deviceUUID: String,
        fcmToken: String,
        deviceName: String,
        language: String,
    )

    /** Whether the device gets each kind of notification. Kinds this version doesn't know are left out. */
    suspend fun fetchNotificationPreferences(deviceUUID: String): Map<NotificationCategory, Boolean>

    suspend fun setNotificationPreference(
        deviceUUID: String,
        category: NotificationCategory,
        isEnabled: Boolean,
    )

    suspend fun updateToken(
        fcmToken: String,
        deviceToken: String,
    ): Boolean
}

class FCMRepository @Inject constructor(
    private val api: FCMApi,
) : FCMRepositoryProtocol {

    override suspend fun register(
        deviceUUID: String,
        fcmToken: String,
        deviceName: String,
        language: String,
    ) {
        val request = RegisterDeviceRequestDTO(
            deviceUUID = deviceUUID,
            fcmToken = fcmToken,
            deviceName = deviceName,
            language = language
        )

        val response = api.registerDevice(request)
        if (!response.isSuccessful) {
            throw Exception("Failed to register device: ${response.code()}")
        }
    }

    override suspend fun updateToken(fcmToken: String, deviceToken: String): Boolean {
        val response = api.updateNotificationToken(
            NotificationTokenUpdateRequestDTO(fcmToken, deviceToken)
        )
        if (!response.isSuccessful) {
            Timber.tag("FCM").e("Failed to update token: ${response.code()}")
            return false
        }
        return true
    }

    override suspend fun fetchNotificationPreferences(deviceUUID: String): Map<NotificationCategory, Boolean> {
        val response = api.getNotificationPreferences(deviceUUID)
        val body = response.body()
        if (!response.isSuccessful || body == null) {
            throw Exception("Failed to load notification preferences: ${response.code()}")
        }
        return body.preferences.mapNotNull { preference ->
            NotificationCategory.fromRawValue(preference.category)?.let { it to preference.isEnabled }
        }.toMap()
    }

    override suspend fun setNotificationPreference(
        deviceUUID: String,
        category: NotificationCategory,
        isEnabled: Boolean,
    ) {
        val response = api.setNotificationPreference(
            category.rawValue,
            NotificationPreferenceUpdateRequestDTO(deviceUUID = deviceUUID, isEnabled = isEnabled)
        )
        if (!response.isSuccessful) {
            throw Exception("Failed to change notification preference: ${response.code()}")
        }
    }
}
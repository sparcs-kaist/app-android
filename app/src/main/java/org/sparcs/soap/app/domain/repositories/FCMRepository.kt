package org.sparcs.soap.app.domain.repositories

import org.sparcs.soap.app.domain.helpers.FeatureType
import org.sparcs.soap.app.networking.requestDTO.ManageAlertRequestDTO
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

    suspend fun manage(
        deviceUUID: String,
        service: FeatureType,
        isActive: Boolean,
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

    override suspend fun manage(deviceUUID: String, service: FeatureType, isActive: Boolean) {
        val response = api.getAlertStatus(deviceUUID)

        if (response.isSuccessful) {
            val alertList = response.body() ?: emptyList()

            val existing =
                alertList.find { it.serviceName == service.name || it.serviceName == service.rawValue.toString() }

            if (existing != null) {
                if (existing.isActive == isActive) {
                    Timber.tag("FCM")
                        .d("Configuration already matches the server. Skipping request.")
                    return
                }

            }
        }

        val request = ManageAlertRequestDTO(deviceUUID, service.rawValue, isActive)
        try {
            val manageResponse = api.manageAlert(request)
            if (!manageResponse.isSuccessful) {
                if (manageResponse.code() == 500) {
                    Timber.tag("FCM").e("Server DB conflict: Data already exists.")
                } else {
                    throw Exception("Failed: ${manageResponse.code()}")
                }
            }
        } catch (e: Exception) {
            Timber.tag("FCM").e("Network Error: ${e.message}")
        }
    }
}
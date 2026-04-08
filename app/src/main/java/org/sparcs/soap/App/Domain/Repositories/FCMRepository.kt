package org.sparcs.soap.App.Domain.Repositories

import org.sparcs.soap.App.Domain.Helpers.FeatureType
import org.sparcs.soap.App.Networking.RequestDTO.ManageAlertRequestDTO
import org.sparcs.soap.App.Networking.RequestDTO.RegisterDeviceRequestDTO
import org.sparcs.soap.App.Networking.RetrofitAPI.FCMApi
import javax.inject.Inject

interface FCMRepositoryProtocol {

    suspend fun register(
        deviceUUID: String,
        fcmToken: String,
        deviceName: String,
        language: String
    )
    suspend fun manage(
        deviceUUID: String,
        service: FeatureType,
        isActive: Boolean
    )
}

class FCMRepository @Inject constructor(
    private val api: FCMApi
) : FCMRepositoryProtocol {

    override suspend fun register(
        deviceUUID: String,
        fcmToken: String,
        deviceName: String,
        language: String
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

    override suspend fun manage(
        deviceUUID: String,
        service: FeatureType,
        isActive: Boolean
    ) {
        val request = ManageAlertRequestDTO(
            deviceUUID = deviceUUID,
            service = service.rawValue,
            isActive = isActive
        )
        val response = api.manageAlert(request)
        if (!response.isSuccessful) {
            throw Exception("Failed to manage alert: ${response.code()}")
        }
    }
}
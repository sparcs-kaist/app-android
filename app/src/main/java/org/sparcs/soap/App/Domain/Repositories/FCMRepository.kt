package org.sparcs.soap.App.Domain.Repositories

import android.util.Log
import org.sparcs.soap.App.Domain.Helpers.FeatureType
import org.sparcs.soap.App.Networking.RequestDTO.ManageAlertRequestDTO
import org.sparcs.soap.App.Networking.RequestDTO.RegisterDeviceRequestDTO
import org.sparcs.soap.App.Networking.RetrofitAPI.FCMApi
import org.sparcs.soap.App.Networking.RetrofitAPI.NotificationTokenUpdateRequestDTO
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

    suspend fun updateToken(
        fcmToken: String,
        deviceToken: String
    ): Boolean
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

    override suspend fun updateToken(fcmToken: String, deviceToken: String): Boolean {
        val response = api.updateNotificationToken(
            NotificationTokenUpdateRequestDTO(fcmToken, deviceToken)
        )
        if (!response.isSuccessful) {
            Log.e("FCM", "토큰 업데이트 실패: ${response.code()}")
            return false
        }
        return true
    }

    override suspend fun manage(deviceUUID: String, service: FeatureType, isActive: Boolean) {
        val response = api.getAlertStatus(deviceUUID)

        if (response.isSuccessful) {
            val alertList = response.body() ?: emptyList()

            val existing = alertList.find { it.serviceName == service.name || it.serviceName == service.rawValue.toString() }

            if (existing != null) {
                if (existing.isActive == isActive) {
                    Log.d("FCM", "이미 서버와 동일한 설정입니다. 요청 스킵.")
                    return
                }

            }
        }

        val request = ManageAlertRequestDTO(deviceUUID, service.rawValue, isActive)
        try {
            val manageResponse = api.manageAlert(request)
            if (!manageResponse.isSuccessful) {
                if (manageResponse.code() == 500) {
                    Log.e("FCM", "서버 DB 충돌 발생: 이미 데이터가 존재함.")
                } else {
                    throw Exception("Failed: ${manageResponse.code()}")
                }
            }
        } catch (e: Exception) {
            Log.e("FCM", "Network Error: ${e.message}")
        }
    }

    // FCMRepository.kt
//    override suspend fun manage(deviceUUID: String, service: FeatureType, isActive: Boolean) {
//        val response = api.getAlertStatus(deviceUUID)
//
//        if (response.isSuccessful) {
//            val alertList = response.body() ?: emptyList()
//            val alreadyHasConfig = alertList.isNotEmpty()
//
//            if (alreadyHasConfig) {
//                Log.d("FCM", "이미 설정이 존재하여 추가 요청을 보내지 않습니다.")
//                return
//            }
//        }
//        val request = ManageAlertRequestDTO(deviceUUID, service.rawValue, isActive)
//        api.manageAlert(request)
//    }
}
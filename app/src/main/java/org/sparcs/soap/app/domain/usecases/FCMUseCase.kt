package org.sparcs.soap.app.domain.usecases

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import org.sparcs.soap.app.domain.helpers.FeatureType
import org.sparcs.soap.app.domain.repositories.FCMRepositoryProtocol
import timber.log.Timber
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

interface FCMUseCaseProtocol {
    suspend fun register(fcmToken: String)
    suspend fun manage(service: FeatureType, isActive: Boolean)
}

@Suppress("DEPRECATION")
@SuppressLint("HardwareIds")
class FCMUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val fcmRepository: FCMRepositoryProtocol,
) : FCMUseCaseProtocol {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "fcm_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val FCM_DEVICE_ID_KEY = "fcmDeviceID"
        private const val FCM_ANDROID_ID_KEY = "fcmAndroidID"
        private const val LAST_REGISTERED_TOKEN = "last_registered_token"
        private const val LAST_REGISTERED_LANG = "last_registered_lang"
    }

    private var isRegistering: Boolean = false

    override suspend fun register(fcmToken: String) {
        val currentLang = Locale.getDefault().language.takeIf { it.isNotBlank() } ?: "ko"
        
        val savedToken = prefs.getString(LAST_REGISTERED_TOKEN, null)
        val savedLang = prefs.getString(LAST_REGISTERED_LANG, null)

        if (savedToken == fcmToken && savedLang == currentLang) {
            Timber.d("FCM: Token and language already match. Skipping.")
            return
        }

        if (isRegistering) return
        isRegistering = true

        try {
            val existingUUID = getPersistentDeviceID()

            if (existingUUID == null) {
                val newUUID = UUID.randomUUID().toString()
                val currentAndroidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                
                prefs.edit {
                    putString(FCM_DEVICE_ID_KEY, newUUID)
                    putString(FCM_ANDROID_ID_KEY, currentAndroidId)
                }

                fcmRepository.register(
                    deviceUUID = newUUID,
                    fcmToken = fcmToken,
                    deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
                    language = currentLang
                )
                Timber.d("FCM: New device registered. UUID: $newUUID")
            } else {
                fcmRepository.updateToken(
                    fcmToken = fcmToken,
                    deviceToken = existingUUID
                )
                Timber.d("FCM: Existing device token updated. UUID: $existingUUID")
            }

            prefs.edit {
                putString(LAST_REGISTERED_TOKEN, fcmToken)
                putString(LAST_REGISTERED_LANG, currentLang)
            }

        } catch (e: Exception) {
            Timber.e(e, "FCM: Operation failed.")
        } finally {
            isRegistering = false
        }
    }

    private fun getPersistentDeviceID(): String? {
        val currentAndroidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val storedAndroidId = prefs.getString(FCM_ANDROID_ID_KEY, null)

        if (storedAndroidId != null && storedAndroidId != currentAndroidId) {
            Timber.d("FCM: Detected device change via backup. Ignoring restored UUID.")
            return null
        }

        return try {
            prefs.getString(FCM_DEVICE_ID_KEY, null)
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun manage(service: FeatureType, isActive: Boolean) {
        val deviceUUID = getPersistentDeviceID() ?: run {
            Timber.e("FCM Manage failed: No Device UUID found.")
            return
        }

        fcmRepository.manage(
            deviceUUID = deviceUUID,
            service = service,
            isActive = isActive
        )
    }
}

class MockFCMUseCase : FCMUseCaseProtocol {
    override suspend fun register(fcmToken: String) {}
    override suspend fun manage(service: FeatureType, isActive: Boolean) {}
}

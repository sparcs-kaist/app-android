package org.sparcs.soap.app.domain.usecases

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
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

@SuppressLint("HardwareIds")
class FCMUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val fcmRepository: FCMRepositoryProtocol,
    private val dataStore: DataStore<Preferences>,
) : FCMUseCaseProtocol {

    companion object {
        private val FCM_DEVICE_ID_KEY = stringPreferencesKey("fcmDeviceID")
        private val FCM_ANDROID_ID_KEY = stringPreferencesKey("fcmAndroidID")
        private val LAST_REGISTERED_TOKEN = stringPreferencesKey("last_registered_token")
        private val LAST_REGISTERED_LANG = stringPreferencesKey("last_registered_lang")
    }

    private var isRegistering: Boolean = false

    override suspend fun register(fcmToken: String) {
        val currentLang = Locale.getDefault().language.takeIf { it.isNotBlank() } ?: "ko"

        val prefs = dataStore.data.first()
        val savedToken = prefs[LAST_REGISTERED_TOKEN]
        val savedLang = prefs[LAST_REGISTERED_LANG]

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
                val currentAndroidId =
                    Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

                dataStore.edit { p ->
                    p[FCM_DEVICE_ID_KEY] = newUUID
                    p[FCM_ANDROID_ID_KEY] = currentAndroidId
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

            dataStore.edit { p ->
                p[LAST_REGISTERED_TOKEN] = fcmToken
                p[LAST_REGISTERED_LANG] = currentLang
            }

        } catch (e: Exception) {
            Timber.e(e, "FCM: Operation failed.")
        } finally {
            isRegistering = false
        }
    }

    private suspend fun getPersistentDeviceID(): String? {
        val currentAndroidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val prefs = dataStore.data.first()

        var storedUUID = prefs[FCM_DEVICE_ID_KEY]
        var storedAndroidId = prefs[FCM_ANDROID_ID_KEY]

        if (storedUUID == null) {
            val legacyData = readLegacyData()
            if (legacyData != null) {
                storedUUID = legacyData.uuid
                storedAndroidId = legacyData.androidId

                dataStore.edit { p ->
                    p[FCM_DEVICE_ID_KEY] = legacyData.uuid
                    legacyData.androidId?.let { p[FCM_ANDROID_ID_KEY] = it }
                    legacyData.token?.let { p[LAST_REGISTERED_TOKEN] = it }
                    legacyData.lang?.let { p[LAST_REGISTERED_LANG] = it }
                }
                Timber.d("FCM: Migrated legacy UUID: $storedUUID")
            }
        }

        if (storedAndroidId != null && storedAndroidId != currentAndroidId) {
            Timber.d("FCM: Detected device change via backup. Ignoring restored UUID.")
            return null
        }

        return storedUUID
    }

    @Suppress("DEPRECATION")
    private fun readLegacyData(): LegacyFCMData? {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val sharedPrefs = EncryptedSharedPreferences.create(
                context,
                "fcm_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            val uuid = sharedPrefs.getString("fcmDeviceID", null) ?: return null
            LegacyFCMData(
                uuid = uuid,
                androidId = sharedPrefs.getString("fcmAndroidID", null),
                token = sharedPrefs.getString("last_registered_token", null),
                lang = sharedPrefs.getString("last_registered_lang", null)
            )
        } catch (_: Exception) {
            null
        }
    }

    private data class LegacyFCMData(
        val uuid: String,
        val androidId: String?,
        val token: String?,
        val lang: String?,
    )

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

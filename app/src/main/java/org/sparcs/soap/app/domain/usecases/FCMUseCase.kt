package org.sparcs.soap.app.domain.usecases

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import org.sparcs.soap.app.domain.helpers.FeatureType
import org.sparcs.soap.app.domain.repositories.FCMRepositoryProtocol
import timber.log.Timber
import java.security.KeyStore
import java.util.Locale
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

interface FCMUseCaseProtocol {
    suspend fun register(fcmToken: String)
    suspend fun manage(service: FeatureType, isActive: Boolean)
}

@SuppressLint("HardwareIds")
class FCMUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val fcmRepository: FCMRepositoryProtocol,
) : FCMUseCaseProtocol {

    private val prefs = context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    companion object {
        private const val FCM_DEVICE_ID_KEY = "fcmDeviceID"
        private const val FCM_DEVICE_ID_BACKUP_KEY = "fcmDeviceID_plain"
        private const val FCM_ANDROID_ID_KEY = "fcmAndroidID"
        private const val AES_KEY_ALIAS = "FCMDeviceKey"
        private const val AES_MODE = "AES/GCM/NoPadding"
    }

    private var isRegistering: Boolean = false

    init {
        if (!keyStore.containsAlias(AES_KEY_ALIAS)) {
            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    AES_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            keyGenerator.generateKey()
        }
    }

    override suspend fun register(fcmToken: String) {
        val currentLang = Locale.getDefault().language.takeIf { it.isNotBlank() } ?: "ko"
        val savedToken = prefs.getString("last_registered_token", null)
        val savedLang = prefs.getString("last_registered_lang", null)

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
                saveDeviceID(newUUID)

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

                val currentAndroidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                prefs.edit {
                    if (prefs.getString(FCM_DEVICE_ID_BACKUP_KEY, null) == null) {
                        putString(FCM_DEVICE_ID_BACKUP_KEY, existingUUID)
                    }
                    if (prefs.getString(FCM_ANDROID_ID_KEY, null) == null) {
                        putString(FCM_ANDROID_ID_KEY, currentAndroidId)
                    }
                }
            }

            prefs.edit {
                putString("last_registered_token", fcmToken)
                putString("last_registered_lang", currentLang)
            }

        } catch (e: Exception) {
            Timber.e(e, "FCM: Operation failed.")
        } finally {
            isRegistering = false
        }
    }

    private fun saveDeviceID(uuid: String) {
        try {
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            val cipher = Cipher.getInstance(AES_MODE)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv
            val encrypted = cipher.doFinal(uuid.toByteArray())
            val combined = Base64.encodeToString(iv + encrypted, Base64.NO_WRAP)

            prefs.edit {
                putString(FCM_DEVICE_ID_KEY, combined)
                putString(FCM_DEVICE_ID_BACKUP_KEY, uuid)
                putString(FCM_ANDROID_ID_KEY, androidId)
            }
        } catch (e: Exception) {
            prefs.edit { putString(FCM_DEVICE_ID_BACKUP_KEY, uuid) }
        }
    }

    private fun getPersistentDeviceID(): String? {
        val currentAndroidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val storedAndroidId = prefs.getString(FCM_ANDROID_ID_KEY, null)

        if (storedAndroidId != null && storedAndroidId != currentAndroidId) {
            Timber.d("FCM: Detected device change via backup. Ignoring restored UUID.")
            return null
        }

        val encryptedStr = prefs.getString(FCM_DEVICE_ID_KEY, null)
        if (encryptedStr != null) {
            try {
                val combined = Base64.decode(encryptedStr, Base64.NO_WRAP)
                val iv = combined.sliceArray(0 until 12)
                val encrypted = combined.sliceArray(12 until combined.size)
                val cipher = Cipher.getInstance(AES_MODE)
                cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(128, iv))
                return String(cipher.doFinal(encrypted))
            } catch (e: Exception) {
                Timber.w("FCM: Decryption failed, maybe reinstalled. Trying backup...")
            }
        }

        return prefs.getString(FCM_DEVICE_ID_BACKUP_KEY, null)
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

    private fun getSecretKey(): SecretKey {
        return (keyStore.getEntry(AES_KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }


}

class MockFCMUseCase : FCMUseCaseProtocol {
    override suspend fun register(fcmToken: String) {}
    override suspend fun manage(service: FeatureType, isActive: Boolean) {}
}
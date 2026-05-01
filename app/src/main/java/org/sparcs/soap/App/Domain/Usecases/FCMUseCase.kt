package org.sparcs.soap.App.Domain.Usecases

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import org.sparcs.soap.App.Domain.Helpers.FeatureType
import org.sparcs.soap.App.Domain.Repositories.FCMRepositoryProtocol
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

class FCMUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fcmRepository: FCMRepositoryProtocol,
) : FCMUseCaseProtocol {

    private val prefs = context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    companion object {
        private const val FCM_DEVICE_ID_KEY = "fcmDeviceID"
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
    val savedToken = prefs.getString("last_registered_token", null)
    if (savedToken == fcmToken) {
        Timber.d("FCM: Token already matches. Skipping.")
        return
    }

    if (isRegistering) return

    try {
        val existingUUID = getEncryptedDeviceID()

        if (existingUUID == null) {
            val newUUID = UUID.randomUUID().toString()
            saveEncryptedDeviceID(newUUID)

            fcmRepository.register(
                deviceUUID = newUUID,
                fcmToken = fcmToken,
                deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
                language = Locale.getDefault().language.takeIf { it.isNotBlank() } ?: "ko"
            )
            Timber.d("FCM: New device registered.")
        } else {
            fcmRepository.updateToken(
                fcmToken = fcmToken,
                deviceToken = existingUUID
            )
            Timber.d("FCM: Existing device token updated.")
        }

        prefs.edit { putString("last_registered_token", fcmToken) }

    } catch (e: Exception) {
        Timber.e(e, "FCM: Operation failed.")
    } finally {
        isRegistering = false
    }
}

    override suspend fun manage(service: FeatureType, isActive: Boolean) {
        val deviceUUID = getEncryptedDeviceID() ?: run {
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

    private fun saveEncryptedDeviceID(uuid: String) {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(uuid.toByteArray())
        val combined = Base64.encodeToString(iv + encrypted, Base64.NO_WRAP)
        prefs.edit { putString(FCM_DEVICE_ID_KEY, combined) }
    }

    private fun getEncryptedDeviceID(): String? {
        val combinedStr = prefs.getString(FCM_DEVICE_ID_KEY, null) ?: return null
        return try {
            val combined = Base64.decode(combinedStr, Base64.NO_WRAP)
            val iv = combined.sliceArray(0 until 12)
            val encrypted = combined.sliceArray(12 until combined.size)
            val cipher = Cipher.getInstance(AES_MODE)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(encrypted))
        } catch (e: Exception) {
            null
        }
    }
}

class MockFCMUseCase : FCMUseCaseProtocol {
    override suspend fun register(fcmToken: String) {}
    override suspend fun manage(service: FeatureType, isActive: Boolean) {}
}
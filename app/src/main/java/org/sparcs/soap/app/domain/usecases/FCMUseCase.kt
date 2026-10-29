package org.sparcs.soap.app.domain.usecases

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.sparcs.soap.app.domain.helpers.NotificationCategory
import org.sparcs.soap.app.domain.repositories.FCMRepositoryProtocol
import timber.log.Timber
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

interface FCMUseCaseProtocol {
    suspend fun register(fcmToken: String)

    /** Stops notifications to this device for the signed-in account, so call it while still signed in. */
    suspend fun unregister()
    /** Whether this device gets each kind of notification. Kinds never turned off are on. */
    suspend fun notificationPreferences(): Map<NotificationCategory, Boolean>

    /** Turns a kind of notification on or off on this device; Buddy's backend then stops or starts sending it here. */
    suspend fun setNotifications(category: NotificationCategory, enabled: Boolean)
}

/** This device hasn't registered for notifications yet, so it has no preferences to show or change. */
class DeviceNotRegisteredException : Exception("This device isn't registered for notifications")

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

    // Registering and unregistering run one at a time, so a registration still in flight when you
    // sign out can't land after the unregister and link this device to your account again.
    private val registrationMutex = Mutex()

    // Bumped by every unregister. A registration that started before one doesn't record itself as
    // done, so the next sign-in registers this device rather than skipping it as unchanged.
    @Volatile
    private var unregisterCount = 0

    override suspend fun register(fcmToken: String): Unit = registrationMutex.withLock {
        val currentLang = Locale.getDefault().language.takeIf { it.isNotBlank() } ?: "ko"
        val unregistersBefore = unregisterCount

        val savedToken = prefs.getString(LAST_REGISTERED_TOKEN, null)
        val savedLang = prefs.getString(LAST_REGISTERED_LANG, null)

        if (savedToken == fcmToken && savedLang == currentLang) {
            Timber.d("FCM: Token and language already match. Skipping.")
            return@withLock
        }

        try {
            // The backend updates a device it already knows, so registering again also keeps the
            // token and language of a registered device current.
            val deviceUUID = getPersistentDeviceID() ?: UUID.randomUUID().toString().also { newUUID ->
                val currentAndroidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                prefs.edit {
                    putString(FCM_DEVICE_ID_KEY, newUUID)
                    putString(FCM_ANDROID_ID_KEY, currentAndroidId)
                }
            }

            fcmRepository.register(
                deviceUUID = deviceUUID,
                fcmToken = fcmToken,
                deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
                language = currentLang
            )
            Timber.d("FCM: Device registered. UUID: $deviceUUID")

            if (unregisterCount == unregistersBefore) {
                prefs.edit {
                    putString(LAST_REGISTERED_TOKEN, fcmToken)
                    putString(LAST_REGISTERED_LANG, currentLang)
                }
            }

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "FCM: Operation failed.")
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

    override suspend fun unregister() {
        // Whether or not it gets through, signing in again registers this device rather than
        // skipping it as unchanged, even if a registration in flight finishes afterwards.
        unregisterCount++
        prefs.edit {
            remove(LAST_REGISTERED_TOKEN)
            remove(LAST_REGISTERED_LANG)
        }
        registrationMutex.withLock {
            val deviceUUID = getPersistentDeviceID() ?: return@withLock
            fcmRepository.unregister(deviceUUID)
        }
    }

    // Preferences belong to the device as registered, so a device not registered yet has none.
    override suspend fun notificationPreferences(): Map<NotificationCategory, Boolean> {
        val deviceUUID = getPersistentDeviceID() ?: throw DeviceNotRegisteredException()
        return fcmRepository.fetchNotificationPreferences(deviceUUID)
    }

    override suspend fun setNotifications(category: NotificationCategory, enabled: Boolean) {
        val deviceUUID = getPersistentDeviceID() ?: throw DeviceNotRegisteredException()
        fcmRepository.setNotificationPreference(deviceUUID, category, enabled)
    }
}

class MockFCMUseCase : FCMUseCaseProtocol {
    val preferences = NotificationCategory.entries.associateWith { true }.toMutableMap()
    var setNotificationsError: Exception? = null

    override suspend fun register(fcmToken: String) {}
    override suspend fun unregister() {}
    override suspend fun notificationPreferences(): Map<NotificationCategory, Boolean> = preferences.toMap()
    override suspend fun setNotifications(category: NotificationCategory, enabled: Boolean) {
        setNotificationsError?.let { throw it }
        preferences[category] = enabled
    }
}

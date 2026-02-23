package org.sparcs.soap.App.Domain.Services
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Error.CrashContext
import org.sparcs.soap.App.Domain.Error.ErrorSource
import org.sparcs.soap.App.Domain.Error.NetworkError
import org.sparcs.soap.App.Domain.Error.SourcedError
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class CrashlyticsService @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : CrashlyticsServiceProtocol {

    private val externalScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private val FCM_DEVICE_ID_KEY = stringPreferencesKey("fcmDeviceID")
    }

    override fun recordException(error: Throwable) {
        if (error is NetworkError && !error.isRecordable) return

        externalScope.launch {
            val userID = getDeviceUUID()
            val crashlytics = FirebaseCrashlytics.getInstance()

            crashlytics.setUserId(userID)
            crashlytics.setCustomKey("id", userID)
            crashlytics.recordException(error)
        }
    }

    override fun record(error: SourcedError, context: CrashContext) {
        val throwable = error as? Throwable ?: return
        if (throwable is NetworkError && !throwable.isRecordable) return

        externalScope.launch {
            val userID = getDeviceUUID()
            val crashlytics = FirebaseCrashlytics.getInstance()

            crashlytics.setCustomKey("feature", context.feature)
            crashlytics.setCustomKey("action", context.action)
            crashlytics.setCustomKey("source", error.source.name)
            crashlytics.setCustomKey("user_id", userID)

            context.metadata.forEach { (key, value) ->
                crashlytics.setCustomKey(key, value)
            }

            crashlytics.recordException(throwable)
        }
    }

    override fun record(error: Throwable, context: CrashContext) {
        if (error is NetworkError && !error.isRecordable) return

        externalScope.launch {
            val userID = getDeviceUUID()
            val crashlytics = FirebaseCrashlytics.getInstance()

            crashlytics.setCustomKey("feature", context.feature)
            crashlytics.setCustomKey("action", context.action)
            crashlytics.setCustomKey("source", ErrorSource.Unknown.name)
            crashlytics.setCustomKey("user_id", userID)

            context.metadata.forEach { (key, value) ->
                crashlytics.setCustomKey(key, value)
            }

            crashlytics.recordException(error)
        }
    }

    private suspend fun getDeviceUUID(): String {
        val prefs = dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences())
                else throw exception
            }
            .first()

        val storedUUID = prefs[FCM_DEVICE_ID_KEY]

        return if (storedUUID != null) {
            storedUUID
        } else {
            val newUUID = UUID.randomUUID().toString()
            dataStore.edit { it[FCM_DEVICE_ID_KEY] = newUUID }
            newUUID
        }
    }
}
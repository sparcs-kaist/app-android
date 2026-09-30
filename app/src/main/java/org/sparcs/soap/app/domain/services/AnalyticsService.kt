package org.sparcs.soap.app.domain.services

import android.content.Context
import android.os.Bundle
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sparcs.soap.app.domain.enums.Event
import timber.log.Timber
import java.io.IOException
import java.util.UUID
import javax.inject.Inject


interface AnalyticsServiceProtocol {
    fun logEvent(event: Event)
}

fun AnalyticsServiceProtocol.logScreen(screenName: String, vararg extraParams: Pair<String, Any>) {
    logEvent(object : Event {
        override val source = screenName
        override val name = "screen_view"
        override val parameters = mapOf("screen_name" to source, "screen_class" to source) + extraParams.toMap()
    })
}

class AnalyticsService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
) : AnalyticsServiceProtocol {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private val FCM_DEVICE_ID_KEY = stringPreferencesKey("fcm_device_id")
    }

    override fun logEvent(event: Event) {
        scope.launch {
            try {
                val deviceId = getDeviceUUID()
                val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

                val bundle = Bundle().apply {
                    putString("source", event.source)
                    event.parameters.forEach { (key, value) ->
                        when (value) {
                            is String -> putString(key, value)
                            is Int -> putInt(key, value)
                            is Long -> putLong(key, value)
                            is Double -> putDouble(key, value)
                            is Boolean -> putBoolean(key, value)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    firebaseAnalytics.setUserId(deviceId)
                    firebaseAnalytics.logEvent(event.name, bundle)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Timber.e(error, "Could not log analytics event")
            }
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
            dataStore.edit { mutablePrefs: MutablePreferences ->
                mutablePrefs[FCM_DEVICE_ID_KEY] = newUUID
            }
            newUUID
        }
    }
}

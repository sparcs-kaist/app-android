package org.sparcs.soap.App.Domain.Services

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sparcs.soap.App.Domain.Enums.Event
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class AnalyticsService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
) : AnalyticsServiceProtocol {

    companion object {
        private val FCM_DEVICE_ID_KEY = stringPreferencesKey("fcm_device_id")
    }

    override fun logEvent(event: Event) {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

        CoroutineScope(Dispatchers.IO).launch {
            val deviceId = getDeviceUUID()

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
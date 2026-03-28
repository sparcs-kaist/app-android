package org.sparcs.soap.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "watch_data")

class WatchDataStore(private val context: Context) {
    companion object {
        private val TIMETABLE_JSON_KEY = stringPreferencesKey("timetable_json")
    }

    val timetableJsonFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TIMETABLE_JSON_KEY]
    }

    suspend fun saveTimetableJson(json: String) {
        context.dataStore.edit { preferences ->
            preferences[TIMETABLE_JSON_KEY] = json
        }
    }
}
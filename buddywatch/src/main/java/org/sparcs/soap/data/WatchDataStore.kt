package org.sparcs.soap.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.sparcs.soap.presentation.LectureViewOption

val Context.dataStore by preferencesDataStore(name = "watch_data")

class WatchDataStore(private val context: Context) {
    companion object {
        private val TIMETABLE_JSON_KEY = stringPreferencesKey("timetable_json")
        private val SEMESTER_JSON_KEY = stringPreferencesKey("semester_json")
        private val VIEW_OPTION_KEY = stringPreferencesKey("lecture_view_option")
    }

    val timetableJsonFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TIMETABLE_JSON_KEY]
    }

    val semesterJsonFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SEMESTER_JSON_KEY]
    }

    suspend fun saveTimetableJson(json: String) {
        context.dataStore.edit { preferences ->
            preferences[TIMETABLE_JSON_KEY] = json
        }
    }

    val viewOptionFlow: Flow<LectureViewOption> = context.dataStore.data.map { preferences ->
        LectureViewOption.entries.firstOrNull { it.name == preferences[VIEW_OPTION_KEY] }
            ?: LectureViewOption.UP_NEXT
    }

    suspend fun saveViewOption(option: LectureViewOption) {
        context.dataStore.edit { it[VIEW_OPTION_KEY] = option.name }
    }

    suspend fun clearTimetable() {
        context.dataStore.edit {
            it.remove(TIMETABLE_JSON_KEY)
            it.remove(SEMESTER_JSON_KEY)
        }
    }

    suspend fun saveSemesterJson(json: String) {
        context.dataStore.edit { preferences ->
            preferences[SEMESTER_JSON_KEY] = json
        }
    }
}

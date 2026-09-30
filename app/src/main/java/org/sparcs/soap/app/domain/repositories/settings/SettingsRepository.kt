package org.sparcs.soap.app.domain.repositories.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

interface SettingsRepositoryProtocol {
    val themeMode: Flow<String>
    suspend fun setThemeMode(mode: String)
}

@Singleton
class SettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SettingsRepositoryProtocol {
    private val themeModeKey = stringPreferencesKey("theme_mode")

    override val themeMode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[themeModeKey] ?: "system"
        }

    override suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { settings ->
            settings[themeModeKey] = mode
        }
    }
}
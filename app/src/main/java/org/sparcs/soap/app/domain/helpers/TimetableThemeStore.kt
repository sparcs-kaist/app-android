package org.sparcs.soap.app.domain.helpers

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** App-only theme preferences. Widgets and wearables keep their existing palettes. */
class TimetableThemeStore(context: Context) {
    private val preferences = context.getSharedPreferences("timetable_themes", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    data class State(val customThemes: List<TimetableTheme>, val selectedID: String) {
        val themes get() = TimetableTheme.builtIn + customThemes
        val selected get() = themes.firstOrNull { it.id == selectedID } ?: TimetableTheme.Default
    }

    val state: State get() {
        val custom = runCatching {
            json.decodeFromString<List<TimetableTheme>>(preferences.getString("custom", "[]")!!)
                .filter { !it.isBuiltIn && it.id.startsWith("custom.") && it.isValid }.distinctBy { it.id }
        }.getOrDefault(emptyList())
        val id = preferences.getString("selected", null) ?: TimetableTheme.Default.id
        return State(custom, id.takeIf { candidate -> (TimetableTheme.builtIn + custom).any { it.id == candidate } } ?: TimetableTheme.Default.id)
    }

    fun observe(onChange: (State) -> Unit): () -> Unit {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> onChange(state) }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        onChange(state)
        return { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun select(id: String) {
        if (state.themes.any { it.id == id }) preferences.edit().putString("selected", id).apply()
    }

    fun saveAndSelect(theme: TimetableTheme) {
        require(!theme.isBuiltIn && theme.id.startsWith("custom.") && theme.isValid)
        val saved = theme.copy(name = theme.name.trim())
        val themes = state.customThemes.toMutableList()
        val index = themes.indexOfFirst { it.id == theme.id }
        if (index < 0) themes.add(saved) else themes[index] = saved
        preferences.edit().putString("custom", json.encodeToString(themes)).putString("selected", saved.id).apply()
    }

    fun delete(id: String) {
        if (state.customThemes.none { it.id == id }) return
        val editor = preferences.edit().putString("custom", json.encodeToString(state.customThemes.filterNot { it.id == id }))
        if (state.selectedID == id) editor.remove("selected")
        editor.apply()
    }
}

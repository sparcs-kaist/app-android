package org.sparcs.soap.app.domain.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaxiFavoritesStore internal constructor(private val preferences: SharedPreferences) {
    @Inject constructor(@ApplicationContext context: Context) :
        this(context.getSharedPreferences(FILE, Context.MODE_PRIVATE))

    private val json = Json { ignoreUnknownKeys = true }

    var placeIDs: List<String>
        get() = preferences.getString(KEY_PLACES, null)
            ?.let { runCatching { json.decodeFromString<List<String>>(it) }.getOrNull() }
            .orEmpty()
        set(value) = preferences.edit { putString(KEY_PLACES, json.encodeToString(value)) }

    fun clear() = preferences.edit {
        remove(KEY_PLACES)
        remove("taxi.favoriteRoutes") // Remove routes left by older local-only versions too.
    }

    private companion object {
        const val FILE = "taxi_favorites"
        const val KEY_PLACES = "taxi.favoritePlaces"
    }
}

package org.sparcs.soap.app.domain.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaxiFavoritesStore internal constructor(private val preferences: SharedPreferences) {
    @Inject constructor(@ApplicationContext context: Context) :
        this(context.getSharedPreferences(FILE, Context.MODE_PRIVATE))

    @Serializable
    data class Route(val sourceID: String, val destinationID: String)

    private val json = Json { ignoreUnknownKeys = true }

    var placeIDs: List<String>
        get() = decode(KEY_PLACES)
        set(value) = preferences.edit { putString(KEY_PLACES, json.encodeToString(value)) }

    var routes: List<Route>
        get() = decode(KEY_ROUTES)
        set(value) = preferences.edit { putString(KEY_ROUTES, json.encodeToString(value)) }

    fun clear() = preferences.edit {
        remove(KEY_PLACES)
        remove(KEY_ROUTES)
    }

    private inline fun <reified T> decode(key: String): List<T> =
        preferences.getString(key, null)
            ?.let { runCatching { json.decodeFromString<List<T>>(it) }.getOrNull() }
            .orEmpty()

    private companion object {
        const val FILE = "taxi_favorites"
        const val KEY_PLACES = "taxi.favoritePlaces"
        const val KEY_ROUTES = "taxi.favoriteRoutes"
    }
}

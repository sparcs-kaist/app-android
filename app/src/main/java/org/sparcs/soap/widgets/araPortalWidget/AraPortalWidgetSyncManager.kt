package org.sparcs.soap.widgets.araPortalWidget

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AraPortalWidgetSyncManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    suspend fun sync(state: AraPortalUiState) {
        try {
            val manager = GlanceAppWidgetManager(context)
            manager.getGlanceIds(AraPortalWidget::class.java).forEach { id ->
                sync(id, state)
            }
            AraPortalWidget().updateAll(context)
        } catch (e: Exception) {
            Timber.tag("AraPortalWidget").e(e, "sync failed")
        }
    }

    suspend fun syncSignInRequired() {
        sync(AraPortalUiState(signInRequired = true))
    }

    suspend fun sync(glanceId: GlanceId, state: AraPortalUiState) {
        try {
            val jsonString = try {
                Json.encodeToString(state)
            } catch (e: Exception) {
                Timber.tag("AraPortalWidget").e(e, "Json serialization failed for state: $state")
                return
            }
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[stringPreferencesKey(STATE_KEY)] = jsonString
                }
            }
        } catch (e: Exception) {
            Timber.tag("AraPortalWidget").e(e, "sync failed for glanceId $glanceId")
        }
    }
}

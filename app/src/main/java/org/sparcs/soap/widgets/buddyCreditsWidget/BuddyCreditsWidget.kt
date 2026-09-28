package org.sparcs.soap.widgets.buddyCreditsWidget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import dagger.hilt.android.EntryPointAccessors
import org.sparcs.soap.app.domain.helpers.CreditSummarySnapshotStore
import org.sparcs.soap.widgets.WidgetEntryPoint
import org.sparcs.soap.widgets.theme.ui.WidgetTheme

class BuddyCreditsWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val tokenStorage = EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .tokenStorage()
        val signInRequired = tokenStorage.getAccessToken() == null
        val snapshot = if (signInRequired) null else CreditSummarySnapshotStore(context).snapshot
        provideContent {
            val prefs = currentState<Preferences>()
            val themeMode = prefs[stringPreferencesKey("theme_mode")] ?: "System"
            val transparency = prefs[floatPreferencesKey("background_transparency")] ?: 1f

            WidgetTheme(themeMode = themeMode) {
                CreditsWidgetView(
                    snapshot = snapshot,
                    signInRequired = signInRequired,
                    transparency = transparency
                )
            }
        }
    }
}

class BuddyCreditsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BuddyCreditsWidget()
}

package org.sparcs.soap.widgets.araPortalWidget

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import dagger.hilt.android.EntryPointAccessors
import org.sparcs.soap.widgets.WidgetEntryPoint
import org.sparcs.soap.widgets.araPortalWidget.ui.AraPortalWidgetContent
import org.sparcs.soap.widgets.theme.ui.WidgetTheme

class AraPortalWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(100.dp, 60.dp),   // 1 row
            DpSize(100.dp, 120.dp),  // 3 rows
            DpSize(100.dp, 210.dp),  // 5 rows
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(appContext, WidgetEntryPoint::class.java)
        val tokenStorage = entryPoint.tokenStorage()

        provideContent {
            val prefs = currentState<Preferences>()
            val state = AraPortalStateParser.parse(prefs, tokenStorage)
            val themeMode = prefs[stringPreferencesKey("theme_mode")] ?: "System"
            val transparency = prefs[floatPreferencesKey("background_transparency")] ?: 1f

            WidgetTheme(themeMode = themeMode) {
                val height = LocalSize.current.height
                val visibleCount = when {
                    height >= 210.dp -> 5
                    height >= 120.dp -> 3
                    else -> 1
                }

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(
                            GlanceTheme.colors.surface.getColor(context).copy(alpha = transparency)
                        )
                ) {
                    AraPortalWidgetContent(state = state, visibleCount = visibleCount)
                }
            }
        }
    }
}

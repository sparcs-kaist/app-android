package org.sparcs.soap.widgets.buddyTimetableWidget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dagger.hilt.android.EntryPointAccessors
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableThemeStore
import org.sparcs.soap.widgets.WIDGET_THEME_ID
import org.sparcs.soap.widgets.WidgetEntryPoint
import org.sparcs.soap.widgets.theme.ui.TimetableWidgetTheme.grayBB
import org.sparcs.soap.widgets.theme.ui.WidgetTheme
import org.sparcs.soap.widgets.themed
import org.sparcs.soap.widgets.timetableWidgetIntent

class BuddySilhouetteWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val entryPoint =
            EntryPointAccessors.fromApplication(appContext, WidgetEntryPoint::class.java)
        val tokenStorage = entryPoint.tokenStorage()
        val themes = TimetableThemeStore(appContext).state

        provideContent {
            val prefs = currentState<Preferences>()
            val state = TimetableStateParser.parse(prefs, tokenStorage)

            val timetableTheme = themes.theme(prefs[WIDGET_THEME_ID])
            val themeMode = prefs[stringPreferencesKey("theme_mode")] ?: "System"
            val transparency = prefs[floatPreferencesKey("background_transparency")] ?: 1f

            WidgetTheme(themeMode = themeMode) {
                val surface = timetableTheme.backgroundColor
                    ?: GlanceTheme.colors.background.getColor(context)
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .background(surface.copy(alpha = transparency))
                ) {
                    if (state.signInRequired) {
                        Box(
                            modifier = GlanceModifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                context.getString(R.string.login_required),
                                style = TextStyle(
                                    color = GlanceTheme.colors.onSurface
                                )
                            )
                        }
                    } else if (state.timetable == null) {
                        Box(
                            modifier = GlanceModifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    context.getString(R.string.loading_data),
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onSurface
                                    )
                                )
                                Text(
                                    context.getString(R.string.wait_moment),
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        color = GlanceTheme.colors.grayBB
                                    )
                                )
                            }
                        }
                    } else {
                        val timetable = state.timetable.themed(timetableTheme)
                        TimetableSmallWidgetView(timetable, timetableTheme)
                    }
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .clickable(onClick = actionStartActivity(timetableWidgetIntent(context)))
                    ) {}
                }
            }
        }
    }
}

class BuddySilhouetteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BuddySilhouetteWidget()
}

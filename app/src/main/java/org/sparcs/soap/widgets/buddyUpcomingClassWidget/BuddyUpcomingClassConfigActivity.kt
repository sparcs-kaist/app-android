package org.sparcs.soap.widgets.buddyUpcomingClassWidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.helpers.TimetableThemeStore
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol
import org.sparcs.soap.app.domain.services.logScreen
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.theme_dark_background
import org.sparcs.soap.app.theme.ui.theme_light_background
import org.sparcs.soap.widgets.WIDGET_THEME_ID
import org.sparcs.soap.widgets.components.WidgetPaletteRow
import org.sparcs.soap.widgets.components.WidgetPreviewStage
import org.sparcs.soap.widgets.components.WidgetTransparencyItem
import org.sparcs.soap.widgets.components.WidgetThemeItem
import org.sparcs.soap.widgets.components.WidgetSaveButton
import org.sparcs.soap.app.features.settings.components.SettingsSectionHeader
import org.sparcs.soap.app.features.settings.components.SettingsGroup
import org.sparcs.soap.widgets.ownsAppWidget
import org.sparcs.soap.widgets.updateInstalledWidgets
import javax.inject.Inject

@AndroidEntryPoint
class BuddyUpcomingClassConfigActivity : ComponentActivity() {

    @Inject lateinit var analyticsService: AnalyticsServiceProtocol

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (!ownsAppWidget(appWidgetId)) {
            finish()
            return
        }

        analyticsService.logScreen("BuddyUpcomingClassConfig")

        setContent {
            Theme {
                var selectedTheme by remember { mutableStateOf("System") }
                var transparency by remember { mutableFloatStateOf(1f) }
                val themeState = remember { TimetableThemeStore(this@BuddyUpcomingClassConfigActivity).state }
                var timetableThemeID by remember { mutableStateOf(themeState.selectedID) }

                LaunchedEffect(Unit) {
                    val manager = GlanceAppWidgetManager(this@BuddyUpcomingClassConfigActivity)
                    val glanceId = try {
                        manager.getGlanceIdBy(appWidgetId)
                    } catch (_: Exception) {
                        null
                    }

                    if (glanceId != null) {
                        val prefs = getAppWidgetState(
                            this@BuddyUpcomingClassConfigActivity,
                            PreferencesGlanceStateDefinition, glanceId
                        )
                        selectedTheme = prefs[stringPreferencesKey("theme_mode")] ?: "System"
                        transparency = prefs[floatPreferencesKey("background_transparency")] ?: 1f
                        timetableThemeID = themeState.theme(prefs[WIDGET_THEME_ID]).id
                    }
                }

                Scaffold(
                    topBar = {
                        SettingsViewNavigationBar(
                            title = stringResource(R.string.widget_settings),
                            onDismiss = { finish() },
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    }
                ) { innerPadding ->
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        LazyColumn {
                            item {
                                UpcomingClassPreviewSection(selectedTheme, transparency, themeState.theme(timetableThemeID))
                                SettingsSectionHeader(stringResource(R.string.widget_miscellaneous))
                                SettingsGroup {
                                    row { WidgetPaletteRow(themeState.themes, timetableThemeID) { timetableThemeID = it } }
                                    row { WidgetThemeItem(selectedTheme) { selectedTheme = it } }
                                    row { WidgetTransparencyItem(transparency) { transparency = it } }
                                }
                                WidgetSaveButton { saveAndFinish(selectedTheme, transparency, timetableThemeID) }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun UpcomingClassPreviewSection(
        selectedTheme: String,
        transparency: Float,
        timetableTheme: TimetableTheme,
    ) {
        val isDark =
            if (selectedTheme == "System") isSystemInDarkTheme() else selectedTheme == "Dark"
        val surfaceColor = timetableTheme.backgroundColor
            ?: if (isDark) theme_dark_background else theme_light_background
        // The widget paints the next class in its palette, so the preview shows a palette color.
        val accentColor = timetableTheme.colorFor(0)

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            WidgetPreviewStage {
                Theme(darkTheme = isDark) {
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(28.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = surfaceColor.copy(alpha = transparency),
                                    shape = RoundedCornerShape(28.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = stringResource(R.string.up_next),
                                    color = accentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = stringResource(R.string.preview_lecture_title),
                                    maxLines = 2,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = stringResource(R.string.preview_lecture_time),
                                    color = accentColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = stringResource(R.string.preview_lecture_location),
                                    maxLines = 1,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }




    private fun saveAndFinish(theme: String, transparency: Float, timetableThemeID: String) {
        if (!ownsAppWidget(appWidgetId)) {
            finish()
            return
        }
        val appContext = applicationContext
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val manager = GlanceAppWidgetManager(appContext)
                val glanceId = try {
                    manager.getGlanceIdBy(appWidgetId)
                } catch (_: Exception) {
                    null
                }
                if (glanceId != null) {
                    updateAppWidgetState(
                        appContext,
                        PreferencesGlanceStateDefinition,
                        glanceId
                    ) { prefs ->
                        prefs.toMutablePreferences().apply {
                            this[stringPreferencesKey("theme_mode")] = theme
                            this[floatPreferencesKey("background_transparency")] = transparency
                            this[WIDGET_THEME_ID] = timetableThemeID
                        }
                    }
                    BuddyUpcomingClassWidget().update(appContext, glanceId)
                } else {
                    BuddyUpcomingClassWidget().updateInstalledWidgets(appContext)
                }
            }
            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}

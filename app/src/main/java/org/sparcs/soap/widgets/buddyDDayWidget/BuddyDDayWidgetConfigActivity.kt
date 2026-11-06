package org.sparcs.soap.widgets.buddyDDayWidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
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
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol
import org.sparcs.soap.app.domain.services.logScreen
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.theme_dark_background
import org.sparcs.soap.app.theme.ui.theme_light_background
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
class BuddyDDayWidgetConfigActivity : ComponentActivity() {

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

        analyticsService.logScreen("BuddyDDayWidgetConfig")

        setContent {
            Theme {
                var selectedTheme by remember { mutableStateOf("System") }
                var transparency by remember { mutableFloatStateOf(1f) }

                LaunchedEffect(Unit) {
                    val manager = GlanceAppWidgetManager(this@BuddyDDayWidgetConfigActivity)
                    val glanceId = try {
                        manager.getGlanceIdBy(appWidgetId)
                    } catch (_: Exception) {
                        null
                    }

                    if (glanceId != null) {
                        val prefs = getAppWidgetState(
                            this@BuddyDDayWidgetConfigActivity,
                            PreferencesGlanceStateDefinition, glanceId
                        )
                        selectedTheme = prefs[stringPreferencesKey("theme_mode")] ?: "System"
                        transparency = prefs[floatPreferencesKey("background_transparency")] ?: 1f
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
                                WidgetPreviewSection(selectedTheme, transparency)
                                SettingsSectionHeader(stringResource(R.string.widget_miscellaneous))
                                SettingsGroup {
                                    row { WidgetThemeItem(selectedTheme) { selectedTheme = it } }
                                    row { WidgetTransparencyItem(transparency) { transparency = it } }
                                }
                                WidgetSaveButton { saveAndFinish(selectedTheme, transparency) }
                            }
                        }
                    }
                }
            }
        }
    }




    @Composable
    private fun WidgetPreviewSection(selectedTheme: String, transparency: Float) {
        val isDark = when (selectedTheme) {
            "Dark" -> true
            "Light" -> false
            else -> isSystemInDarkTheme()
        }

        val surfaceColor = if (isDark) theme_dark_background else theme_light_background

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            WidgetPreviewStage {
                Theme(darkTheme = isDark) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .width(160.dp)
                            .height(160.dp)
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
                                .padding(16.dp),
                            contentAlignment = Alignment.TopStart
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.d_day_widget_ends_in).uppercase(),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                )

                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "54",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "days",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Text(
                                    text = stringResource(R.string.preview_d_day_semester_label),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun saveAndFinish(theme: String, transparency: Float) {
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
                        }
                    }
                    BuddyDDayWidget().update(appContext, glanceId)
                } else {
                    BuddyDDayWidget().updateInstalledWidgets(appContext)
                }
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<DDayUpdateWorker>()
                .setConstraints(constraints)
                .addTag("d_day_one_time_sync")
                .build()

            WorkManager.getInstance(appContext).enqueueUniqueWork(
                "d_day_one_time_sync",
                ExistingWorkPolicy.REPLACE,
                request
            )

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}

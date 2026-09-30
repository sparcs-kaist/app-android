package org.sparcs.soap.widgets.buddyCreditsWidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol
import org.sparcs.soap.app.domain.services.logScreen
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.shared.formatters.creditProgress
import org.sparcs.soap.app.shared.formatters.formatGPA
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.grayBB
import org.sparcs.soap.app.theme.ui.theme_dark_background
import org.sparcs.soap.app.theme.ui.theme_light_background
import org.sparcs.soap.widgets.ownsAppWidget
import org.sparcs.soap.widgets.updateInstalledWidgets
import javax.inject.Inject

@AndroidEntryPoint
class BuddyCreditsWidgetConfigActivity : ComponentActivity() {

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

        analyticsService.logScreen("BuddyCreditsWidgetConfig")

        setContent {
            Theme {
                var selectedTheme by remember { mutableStateOf("System") }
                var transparency by remember { mutableFloatStateOf(1f) }

                LaunchedEffect(Unit) {
                    val manager = GlanceAppWidgetManager(this@BuddyCreditsWidgetConfigActivity)
                    val glanceId = try {
                        manager.getGlanceIdBy(appWidgetId)
                    } catch (_: Exception) {
                        null
                    }

                    if (glanceId != null) {
                        val prefs = getAppWidgetState(
                            this@BuddyCreditsWidgetConfigActivity,
                            PreferencesGlanceStateDefinition,
                            glanceId
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
                        LazyColumn(modifier = Modifier.padding(16.dp)) {
                            item {
                                CreditsWidgetPreviewSection(selectedTheme, transparency)

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = stringResource(R.string.widget_miscellaneous),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(8.dp)
                                )
                                WidgetThemeRow(selectedTheme) { selectedTheme = it }
                                WidgetTransparencyRow(transparency) { transparency = it }
                                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { saveAndFinish(selectedTheme, transparency) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Text(text = stringResource(R.string.save_configuration))
                                }
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
                    BuddyCreditsWidget().update(appContext, glanceId)
                } else {
                    BuddyCreditsWidget().updateInstalledWidgets(appContext)
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

@Composable
private fun CreditsWidgetPreviewSection(selectedTheme: String, transparency: Float) {
    val isDark = when (selectedTheme) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemInDarkTheme()
    }

    val surfaceColor = if (isDark) theme_dark_background else theme_light_background

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.preview),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
        )
        Theme(darkTheme = isDark) {
            Box(
                modifier = Modifier
                    .width(170.dp)
                    .height(170.dp)
                    .align(Alignment.CenterHorizontally)
                    .glassBorder(shape = RoundedCornerShape(20.dp))
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = surfaceColor.copy(alpha = transparency),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.credit_gpa_label),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = formatGPA(3.73),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = " / 4.3",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.credit_widget_progress, 96, 138),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { creditProgress(96, 138) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetThemeRow(selectedTheme: String, onThemeSelected: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val currentModeText = when (selectedTheme) {
        "Light" -> stringResource(R.string.widget_white_mode)
        "Dark" -> stringResource(R.string.widget_dark_mode)
        else -> stringResource(R.string.widget_system_default)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { showDialog = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.DarkMode,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = stringResource(R.string.theme_mode),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = currentModeText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.grayBB
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.theme_mode)) },
            text = {
                Column {
                    ThemeOptionRow(
                        stringResource(R.string.widget_system_default),
                        selectedTheme == "System"
                    ) {
                        onThemeSelected("System")
                        showDialog = false
                    }
                    ThemeOptionRow(
                        stringResource(R.string.widget_white_mode),
                        selectedTheme == "Light"
                    ) {
                        onThemeSelected("Light")
                        showDialog = false
                    }
                    ThemeOptionRow(
                        stringResource(R.string.widget_dark_mode),
                        selectedTheme == "Dark"
                    ) {
                        onThemeSelected("Dark")
                        showDialog = false
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

@Composable
private fun ThemeOptionRow(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        RadioButton(selected = isSelected, onClick = onClick)
        Text(text)
    }
}

@Composable
private fun WidgetTransparencyRow(transparency: Float, onTransparencyChange: (Float) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.transparency),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${(transparency * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.grayBB
            )
        }
        Slider(
            value = transparency,
            onValueChange = onTransparencyChange,
            valueRange = 0.0f..1.0f,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CreditsWidgetConfigPreview() {
    Theme {
        Surface {
            CreditsWidgetPreviewSection(selectedTheme = "System", transparency = 0.8f)
        }
    }
}

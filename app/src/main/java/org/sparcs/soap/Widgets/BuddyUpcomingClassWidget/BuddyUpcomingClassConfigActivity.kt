package org.sparcs.soap.Widgets.BuddyUpcomingClassWidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Features.Settings.Components.SettingsViewNavigationBar
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.App.theme.ui.theme_dark_surface
import org.sparcs.soap.App.theme.ui.theme_light_surface
import org.sparcs.soap.R

class BuddyUpcomingClassConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            Theme {
                var selectedTheme by remember { mutableStateOf("System") }
                var transparency by remember { mutableFloatStateOf(1f) }

                LaunchedEffect(Unit) {
                    val manager = GlanceAppWidgetManager(this@BuddyUpcomingClassConfigActivity)
                    val glanceId = manager.getGlanceIdBy(appWidgetId)
                    updateAppWidgetState(this@BuddyUpcomingClassConfigActivity, glanceId) { prefs ->
                        selectedTheme = prefs[stringPreferencesKey("theme_mode")] ?: "System"
                        transparency = prefs[floatPreferencesKey("background_transparency")] ?: 1f
                        prefs
                    }
                }

                Scaffold(
                    topBar = {
                        SettingsViewNavigationBar(
                            title = stringResource(R.string.widget_settings),
                            onDismiss = { finish() }
                        )
                    }
                ) { innerPadding ->
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        LazyColumn(modifier = Modifier.padding(16.dp)) {
                            item {
                                UpcomingClassPreviewSection(selectedTheme, transparency)
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = stringResource(R.string.miscellaneous),
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

    @Composable
    private fun UpcomingClassPreviewSection(selectedTheme: String, transparency: Float) {
        val isDark =
            if (selectedTheme == "System") isSystemInDarkTheme() else selectedTheme == "Dark"
        val surfaceColor = if (isDark) theme_dark_surface else theme_light_surface

        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)) {
            Text(
                text = stringResource(R.string.preview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
            )
            Theme(darkTheme = isDark) {
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(28.dp))
                        .background(surfaceColor.copy(alpha = transparency))
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = stringResource(R.string.up_next),
                            color = MaterialTheme.colorScheme.primary,
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
                            color = MaterialTheme.colorScheme.primary,
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

    @Composable
    private fun WidgetThemeRow(selectedTheme: String, onThemeSelected: (String) -> Unit) {
        var showDialog by remember { mutableStateOf(false) }
        val currentModeText = when (selectedTheme) {
            "Light" -> stringResource(R.string.white_mode)
            "Dark" -> stringResource(R.string.dark_mode)
            else -> stringResource(R.string.system_default)
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
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text(stringResource(R.string.theme_mode)) },
                text = {
                    Column {
                        ThemeOptionRow(
                            stringResource(R.string.system_default),
                            selectedTheme == "System"
                        ) { onThemeSelected("System"); showDialog = false }
                        ThemeOptionRow(
                            stringResource(R.string.white_mode),
                            selectedTheme == "Light"
                        ) { onThemeSelected("Light"); showDialog = false }
                        ThemeOptionRow(
                            stringResource(R.string.dark_mode),
                            selectedTheme == "Dark"
                        ) { onThemeSelected("Dark"); showDialog = false }
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
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)) {
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
                valueRange = 0.0f..1f,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    private fun saveAndFinish(theme: String, transparency: Float) {
        lifecycleScope.launch {
            val manager = GlanceAppWidgetManager(this@BuddyUpcomingClassConfigActivity)
            val glanceId = manager.getGlanceIdBy(appWidgetId)
            updateAppWidgetState(this@BuddyUpcomingClassConfigActivity, glanceId) { prefs ->
                prefs[stringPreferencesKey("theme_mode")] = theme
                prefs[floatPreferencesKey("background_transparency")] = transparency
            }
            BuddyUpcomingClassWidget().update(this@BuddyUpcomingClassConfigActivity, glanceId)

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}
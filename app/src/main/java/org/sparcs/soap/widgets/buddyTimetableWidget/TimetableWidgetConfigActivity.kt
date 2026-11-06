package org.sparcs.soap.widgets.buddyTimetableWidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
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
import androidx.compose.foundation.lazy.items
import org.sparcs.soap.widgets.components.WidgetChoiceRow
import org.sparcs.soap.widgets.components.WidgetChoiceDialog
import org.sparcs.soap.app.features.settings.components.SettingsNavigationItem
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.helpers.TimetableThemeStore
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableSummary
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol
import org.sparcs.soap.app.domain.services.logScreen
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.features.timetable.components.TimetableGrid
import org.sparcs.soap.app.features.timetable.components.TimetableSilhouetteView
import org.sparcs.soap.app.theme.ui.LocalTimetableTheme
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.theme_dark_background
import org.sparcs.soap.app.theme.ui.theme_light_background
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel
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
import timber.log.Timber
import javax.inject.Inject
import org.sparcs.soap.widgets.TimetableWidget as TimetableWidgetQualifier

@AndroidEntryPoint
class TimetableWidgetConfigActivity : ComponentActivity() {

    @Inject lateinit var analyticsService: AnalyticsServiceProtocol

    private val viewModel: TimetableWidgetConfigViewModel by viewModels()

    @Inject
    @TimetableWidgetQualifier
    lateinit var syncManager: TimetableWidgetSyncManager

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private val isSilhouetteWidget: Boolean
        get() = AppWidgetManager.getInstance(this).getAppWidgetInfo(appWidgetId)?.provider ==
            ComponentName(this, BuddySilhouetteWidgetReceiver::class.java)

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

        analyticsService.logScreen("TimetableWidgetConfig")

        setContent {
            Theme {
                var selectedTheme by remember { mutableStateOf("System") }
                var silhouette by remember { mutableStateOf(isSilhouetteWidget) }
                var transparency by remember { mutableFloatStateOf(1f) }
                val state by viewModel.state.collectAsState()
                val selectedTimetableId = state.selectedTimetableId
                val timetableList = state.timetableList
                val semesters = state.semesters
                val selectedSemester = state.selectedSemester
                val selectedTimetable = state.selectedTimetable
                val themeState = remember { TimetableThemeStore(this@TimetableWidgetConfigActivity).state }
                var timetableThemeID by remember { mutableStateOf(themeState.selectedID) }

                LaunchedEffect(Unit) {
                    val manager = GlanceAppWidgetManager(this@TimetableWidgetConfigActivity)
                    val glanceId = try {
                        manager.getGlanceIdBy(appWidgetId)
                    } catch (_: Exception) {
                        null
                    }

                    var savedTimetableId = -1
                    var savedSemesterYear = -1
                    var savedSemesterTypeInt = -1

                    if (glanceId != null) {
                        val prefs = getAppWidgetState(
                            this@TimetableWidgetConfigActivity,
                            PreferencesGlanceStateDefinition, glanceId
                        )
                        selectedTheme = prefs[stringPreferencesKey("theme_mode")] ?: "System"
                        silhouette = isSilhouetteWidget || (prefs[booleanPreferencesKey("silhouette")] ?: false)
                        transparency = prefs[floatPreferencesKey("background_transparency")] ?: 1f
                        savedTimetableId =
                            prefs[intPreferencesKey("selected_timetable_id")] ?: -1
                        savedSemesterYear = prefs[intPreferencesKey("selected_semester_year")] ?: -1
                        savedSemesterTypeInt =
                            prefs[intPreferencesKey("selected_semester_type_int")] ?: -1
                        timetableThemeID = themeState.theme(prefs[WIDGET_THEME_ID]).id
                    }

                    viewModel.initialize(savedTimetableId, savedSemesterYear, savedSemesterTypeInt)
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
                                WidgetPreviewSection(selectedTheme, transparency, selectedTimetable, themeState.theme(timetableThemeID), silhouette)
                                SettingsSectionHeader(stringResource(R.string.widget_timetable))
                                SettingsGroup {
                                    row { WidgetSemesterRow(selectedSemester, semesters, viewModel::selectSemester) }
                                    row { WidgetTimetableRow(selectedTimetableId, timetableList) { viewModel.selectTimetable(it) } }
                                }
                                SettingsSectionHeader(stringResource(R.string.widget_miscellaneous))
                                SettingsGroup {
                                    row { WidgetPaletteRow(themeState.themes, timetableThemeID) { timetableThemeID = it } }
                                    if (!isSilhouetteWidget) row { WidgetSilhouetteRow(silhouette) { silhouette = it } }
                                    row { WidgetThemeItem(selectedTheme) { selectedTheme = it } }
                                    row { WidgetTransparencyItem(transparency) { transparency = it } }
                                }
                                WidgetSaveButton { saveAndFinish(selectedTheme, transparency, selectedTimetableId, selectedSemester, timetableThemeID, silhouette) }
                            }
                        }
                    }
                }
            }
        }
    }







    @Composable
    private fun WidgetSemesterRow(
        selectedSemester: Semester?,
        semesters: List<Semester>,
        onSemesterSelected: (Semester) -> Unit,
    ) {
        var showDialog by remember { mutableStateOf(false) }

        SettingsNavigationItem(
            text = stringResource(R.string.semester),
            supportingText = selectedSemester?.description,
            leadingContent = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
            onClick = { showDialog = true },
        )

        if (showDialog) {
            WidgetChoiceDialog(title = stringResource(R.string.semester), onDismiss = { showDialog = false }) {
                items(semesters, key = { it.id }) { semester ->
                    WidgetChoiceRow(
                        selected = selectedSemester?.id == semester.id,
                        onClick = { onSemesterSelected(semester); showDialog = false },
                    ) { Text(semester.description) }
                }
            }
        }
    }

    @Composable
    private fun WidgetTimetableRow(
        selectedTimetableId: Int,
        timetableList: List<TimetableSummary>,
        onTimetableSelected: (Int) -> Unit,
    ) {
        var showDialog by remember { mutableStateOf(false) }
        val currentTimetable = if (selectedTimetableId == -1) {
            stringResource(R.string.main_timetable)
        } else {
            timetableList.find { it.id == selectedTimetableId }?.title
                ?: stringResource(R.string.timetable_number, selectedTimetableId)
        }

        SettingsNavigationItem(
            text = stringResource(R.string.timetable),
            supportingText = currentTimetable,
            leadingContent = { Icon(Icons.Outlined.TableChart, contentDescription = null) },
            onClick = { showDialog = true },
        )

        if (showDialog) {
            WidgetChoiceDialog(title = stringResource(R.string.timetable), onDismiss = { showDialog = false }) {
                item(key = "main") {
                    WidgetChoiceRow(
                        selected = selectedTimetableId == -1,
                        onClick = { onTimetableSelected(-1); showDialog = false },
                    ) { Text(stringResource(R.string.main_timetable)) }
                }
                items(timetableList, key = { it.id }) { timetable ->
                    WidgetChoiceRow(
                        selected = selectedTimetableId == timetable.id,
                        onClick = { onTimetableSelected(timetable.id); showDialog = false },
                    ) { Text(timetable.title) }
                }
            }
        }
    }

    private fun saveAndFinish(
        theme: String,
        transparency: Float,
        selectedTimetableId: Int,
        selectedSemester: Semester?,
        timetableThemeID: String,
        silhouette: Boolean,
    ) {
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
                            this[booleanPreferencesKey("silhouette")] = silhouette
                            this[floatPreferencesKey("background_transparency")] = transparency
                            this[WIDGET_THEME_ID] = timetableThemeID
                            this[intPreferencesKey("selected_timetable_id")] = selectedTimetableId
                            selectedSemester?.let {
                                this[intPreferencesKey("selected_semester_year")] = it.year
                                this[intPreferencesKey("selected_semester_type_int")] =
                                    it.semesterType.intValue
                            }
                        }
                    }

                    try {
                        val timetable = viewModel.loadTimetableForWidget(selectedTimetableId, selectedSemester)
                        syncManager.sync(timetable, glanceId)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to sync timetable data for widget with id $appWidgetId")
                    }
                } else {
                    TimetableWidget().updateInstalledWidgets(appContext)
                    BuddySilhouetteWidget().updateInstalledWidgets(appContext)
                }
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<TimetableUpdateWorker>()
                .setConstraints(constraints)
                .addTag("timetable_one_time_sync")
                .build()

            WorkManager.getInstance(appContext).enqueueUniqueWork(
                "timetable_one_time_sync",
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

@Composable
private fun WidgetPreviewSection(
    selectedTheme: String,
    transparency: Float,
    selectedTimetable: Timetable?,
    timetableTheme: TimetableTheme,
    silhouette: Boolean = false,
) {
    val isDark = when (selectedTheme) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemInDarkTheme()
    }

    val surfaceColor = timetableTheme.backgroundColor ?: if (isDark) {
        theme_dark_background
    } else {
        theme_light_background
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        WidgetPreviewStage {
            Theme(darkTheme = isDark) {
                // Theme() re-provides the app's timetable theme, so the widget's own choice goes back on top.
                CompositionLocalProvider(LocalTimetableTheme provides timetableTheme) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
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
                                .padding(8.dp)
                        ) {
                            val previewViewModel = remember(selectedTimetable) {
                                PreviewTimetableViewModel(selectedTimetable)
                            }
                            if (silhouette) {
                                TimetableSilhouetteView(selectedTimetable, Modifier.fillMaxSize().padding(8.dp))
                            } else {
                                TimetableGrid(viewModel = previewViewModel, onLectureSelected = {}, showDeleteDialog = {})
                            }
                        }
                    }
                }
            }
        }
    }
}

package org.sparcs.soap.Widgets.BuddyTimetableWidget

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.TableChart
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType
import org.sparcs.soap.App.Domain.Models.OTL.Semester
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import org.sparcs.soap.App.Domain.Models.OTL.TimetableSummary
import org.sparcs.soap.App.Domain.Usecases.OTL.TimetableUseCaseProtocol
import org.sparcs.soap.App.Features.Settings.Components.SettingsViewNavigationBar
import org.sparcs.soap.App.Features.Timetable.Components.TimetableGrid
import org.sparcs.soap.App.Shared.Extensions.glassBorder
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.App.theme.ui.theme_dark_background
import org.sparcs.soap.App.theme.ui.theme_light_background
import org.sparcs.soap.BuddyPreviewSupport.OTL.PreviewTimetableViewModel
import org.sparcs.soap.R
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TimetableWidgetConfigActivity : ComponentActivity() {

    @Inject
    lateinit var timetableUseCase: TimetableUseCaseProtocol

    @Inject
    @org.sparcs.soap.Widgets.TimetableWidget
    lateinit var syncManager: TimetableWidgetSyncManager

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
                var selectedTimetableId by remember { mutableIntStateOf(-1) }
                var timetableList by remember { mutableStateOf<List<TimetableSummary>>(emptyList()) }
                var semesters by remember { mutableStateOf<List<Semester>>(emptyList()) }
                var selectedSemester by remember { mutableStateOf<Semester?>(null) }
                var selectedTimetable by remember { mutableStateOf<Timetable?>(null) }

                LaunchedEffect(Unit) {
                    val manager = GlanceAppWidgetManager(this@TimetableWidgetConfigActivity)
                    val glanceId = try {
                        manager.getGlanceIdBy(appWidgetId)
                    } catch (_: Exception) {
                        null
                    }

                    var savedSemesterYear = -1
                    var savedSemesterTypeInt = -1

                    if (glanceId != null) {
                        val prefs = getAppWidgetState(
                            this@TimetableWidgetConfigActivity,
                            PreferencesGlanceStateDefinition, glanceId
                        )
                        selectedTheme = prefs[stringPreferencesKey("theme_mode")] ?: "System"
                        transparency = prefs[floatPreferencesKey("background_transparency")] ?: 1f
                        selectedTimetableId =
                            prefs[intPreferencesKey("selected_timetable_id")] ?: -1
                        savedSemesterYear = prefs[intPreferencesKey("selected_semester_year")] ?: -1
                        savedSemesterTypeInt =
                            prefs[intPreferencesKey("selected_semester_type_int")] ?: -1
                    }

                    try {
                        semesters = timetableUseCase.getSemesters().sortedDescending()
                        val current = timetableUseCase.getCurrentSemester()
                        if (savedSemesterYear != -1 && savedSemesterTypeInt != -1) {
                            val savedType = SemesterType.fromRawValue(savedSemesterTypeInt)
                            selectedSemester =
                                semesters.find { it.year == savedSemesterYear && it.semesterType == savedType }
                                    ?: current
                        } else {
                            selectedSemester = current
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to load semesters")
                    }
                }

                LaunchedEffect(selectedSemester) {
                    selectedSemester?.let {
                        try {
                            timetableList = timetableUseCase.getTimetableList(it)
                            if (selectedTimetableId != -1 && timetableList.none { t -> t.id == selectedTimetableId }) {
                                selectedTimetableId = -1
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to load timetable list for semester")
                            timetableList = emptyList()
                            selectedTimetableId = -1
                        }
                    }
                }

                LaunchedEffect(selectedTimetableId, selectedSemester) {
                    selectedSemester?.let {
                        try {
                            selectedTimetable = if (selectedTimetableId == -1) {
                                timetableUseCase.getMyTable(it)
                            } else {
                                timetableUseCase.getTable(selectedTimetableId)
                            }
                        } catch (e: Exception) {
                            selectedTimetable = null
                            Timber.e(
                                e,
                                "Failed to load selected timetable with id $selectedTimetableId"
                            )
                        }
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
                        LazyColumn(
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                        ) {
                            item {
                                WidgetPreviewSection(selectedTheme, transparency, selectedTimetable)

                                Text(
                                    text = stringResource(R.string.widget_timetable),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(8.dp)
                                )

                                WidgetSemesterRow(selectedSemester, semesters) {
                                    selectedSemester = it
                                }

                                WidgetTimetableRow(
                                    selectedTimetableId,
                                    timetableList
                                ) { selectedTimetableId = it }

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
                                    onClick = {
                                        saveAndFinish(
                                            selectedTheme,
                                            transparency,
                                            selectedTimetableId,
                                            selectedSemester
                                        )
                                    },
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
    private fun WidgetSemesterRow(
        selectedSemester: Semester?,
        semesters: List<Semester>,
        onSemesterSelected: (Semester) -> Unit,
    ) {
        var showDialog by remember { mutableStateOf(false) }
        val currentModeText = selectedSemester?.description ?: ""

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .clickable { showDialog = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = stringResource(R.string.semester),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
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
                title = { Text(stringResource(R.string.semester)) },
                text = {
                    LazyColumn {
                        items(semesters.size) { index ->
                            val semester = semesters[index]
                            ThemeOptionRow(
                                text = semester.description,
                                isSelected = selectedSemester?.id == semester.id
                            ) {
                                onSemesterSelected(semester)
                                showDialog = false
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }
    }


    @Composable
    private fun WidgetTimetableRow(
        selectedTimetableId: Int,
        timetableList: List<TimetableSummary>,
        onTimetableSelected: (Int) -> Unit,
    ) {
        var showDialog by remember { mutableStateOf(false) }
        val currentModeText = if (selectedTimetableId == -1) {
            stringResource(R.string.main_timetable)
        } else {
            timetableList.find { it.id == selectedTimetableId }?.title
                ?: stringResource(R.string.main_timetable)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .clickable { showDialog = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.TableChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = stringResource(R.string.timetable),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
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
                title = { Text(stringResource(R.string.timetable)) },
                text = {
                    LazyColumn {
                        item {
                            ThemeOptionRow(
                                text = stringResource(R.string.main_timetable),
                                isSelected = selectedTimetableId == -1
                            ) {
                                onTimetableSelected(-1)
                                showDialog = false
                            }
                        }
                        items(timetableList.size) { index ->
                            val timetable = timetableList[index]
                            ThemeOptionRow(
                                text = timetable.title,
                                isSelected = selectedTimetableId == timetable.id
                            ) {
                                onTimetableSelected(timetable.id)
                                showDialog = false
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
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
                    color = MaterialTheme.colorScheme.onSurface,
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
                            onThemeSelected("System"); showDialog = false
                        }
                        ThemeOptionRow(
                            stringResource(R.string.widget_white_mode),
                            selectedTheme == "Light"
                        ) {
                            onThemeSelected("Light"); showDialog = false
                        }
                        ThemeOptionRow(
                            stringResource(R.string.widget_dark_mode),
                            selectedTheme == "Dark"
                        ) {
                            onThemeSelected("Dark"); showDialog = false
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
                valueRange = 0.0f..1f,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    private fun saveAndFinish(
        theme: String,
        transparency: Float,
        selectedTimetableId: Int,
        selectedSemester: Semester?,
    ) {
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
                            this[intPreferencesKey("selected_timetable_id")] = selectedTimetableId
                            selectedSemester?.let {
                                this[intPreferencesKey("selected_semester_year")] = it.year
                                this[intPreferencesKey("selected_semester_type_int")] =
                                    it.semesterType.intValue
                            }
                        }
                    }

                    try {
                        val timetable = if (selectedTimetableId == -1) {
                            timetableUseCase.getMyTable(
                                selectedSemester ?: timetableUseCase.getCurrentSemester()
                            )
                        } else {
                            timetableUseCase.getTable(selectedTimetableId)
                        }
                        syncManager.sync(timetable, glanceId)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to sync timetable data for widget with id $appWidgetId")
                    }
                } else {
                    TimetableWidget().updateAll(appContext)
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
private fun WidgetPreviewSection(
    selectedTheme: String,
    transparency: Float,
    selectedTimetable: Timetable?,
) {
    val isDark = when (selectedTheme) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemInDarkTheme()
    }

    val surfaceColor = if (isDark) {
        theme_dark_background
    } else {
        theme_light_background
    }

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
                    .fillMaxWidth()
                    .height(400.dp)
                    .glassBorder(shape = RoundedCornerShape(28.dp))
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
                    TimetableGrid(
                        viewModel = previewViewModel,
                        onLectureSelected = {},
                        showDeleteDialog = {}
                    )
                }
            }
        }
    }
}
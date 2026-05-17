package org.sparcs.soap.Widgets.BuddyTimetableWidget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sparcs.soap.App.Domain.Helpers.Constants
import org.sparcs.soap.App.Domain.Helpers.TokenStorageProtocol
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import org.sparcs.soap.R
import org.sparcs.soap.Widgets.WidgetEntryPoint
import org.sparcs.soap.Widgets.theme.ui.TimetableWidgetTheme.grayBB
import org.sparcs.soap.Widgets.theme.ui.WidgetTheme
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

class TimetableWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val entryPoint =
            EntryPointAccessors.fromApplication(appContext, WidgetEntryPoint::class.java)
        val tokenStorage = entryPoint.tokenStorage()

        provideContent {
            val prefs = currentState<Preferences>()
            val state = TimetableStateParser.parse(prefs, tokenStorage)

            val themeMode = prefs[stringPreferencesKey("theme_mode")] ?: "System"
            val transparency = prefs[floatPreferencesKey("background_transparency")] ?: 1f

            WidgetTheme(themeMode = themeMode) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(
                            GlanceTheme.colors.surface.getColor(context).copy(alpha = transparency)
                        )
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
                        Column(
                            modifier = GlanceModifier.fillMaxSize()
                        ) {
                            TimetableLargeWidgetView(timetable = state.timetable)
                        }
                    }
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .clickable(onClick = actionRunCallback<RefreshTimetableAction>())

                    ) {}
                }
            }
        }
    }
}

@Singleton
class TimetableWidgetSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun sync(timetable: Timetable, glanceId: GlanceId? = null) {
        val newState = timetable.toWidgetUiState()
        syncState(newState, glanceId)
    }

    suspend fun syncSignInRequired() {
        syncState(TimetableUiState(signInRequired = true, lastUpdated = System.currentTimeMillis()))
    }
    private suspend fun syncState(state: TimetableUiState, specificGlanceId: GlanceId? = null) {
        try {
            val jsonString = Json.encodeToString(state)
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = specificGlanceId?.let { listOf(it) } ?: manager.getGlanceIds(TimetableWidget::class.java)

            glanceIds.forEach { id ->
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[stringPreferencesKey("timetable_state")] = jsonString
                    }
                }
            }
            if (specificGlanceId != null) {
                TimetableWidget().update(context, specificGlanceId)
            } else {
                TimetableWidget().updateAll(context)
            }
        } catch (_: Exception) {
            Timber.tag("WidgetSync").e("Timetable widget sync failed")
        }
    }
}

class TimetableUpdateWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val glanceManager = GlanceAppWidgetManager(applicationContext)
        val glanceIds = glanceManager.getGlanceIds(TimetableWidget::class.java)

        if (glanceIds.isEmpty()) {
            Timber.d("No installed widgets found. Stopping worker.")
            return Result.success()
        }
        val entryPoint =
            EntryPointAccessors.fromApplication(applicationContext, WidgetEntryPoint::class.java)
        val syncManager = entryPoint.timetableSyncManager()
        val tokenStorage = entryPoint.tokenStorage()
        val timetableUseCase = entryPoint.timetableUseCase()

        return try {
            if (tokenStorage.getAccessToken() == null) {
                return Result.success()
            }

            for (glanceId in glanceIds) {
                val prefs = androidx.glance.appwidget.state.getAppWidgetState(
                    applicationContext,
                    PreferencesGlanceStateDefinition,
                    glanceId
                )
                val selectedTimetableId = prefs[androidx.datastore.preferences.core.intPreferencesKey("selected_timetable_id")] ?: -1
                
                val timetable = if (selectedTimetableId == -1) {
                    val savedYear = prefs[androidx.datastore.preferences.core.intPreferencesKey("selected_semester_year")] ?: -1
                    val savedTypeInt = prefs[androidx.datastore.preferences.core.intPreferencesKey("selected_semester_type_int")] ?: -1
                    if (savedYear != -1 && savedTypeInt != -1) {
                        val savedType = org.sparcs.soap.App.Domain.Enums.OTL.SemesterType.fromRawValue(savedTypeInt)
                        timetableUseCase.getMyTable(savedYear, savedType)
                    } else {
                        val currentSemester = timetableUseCase.getCurrentSemester()
                        if (currentSemester != null) {
                            timetableUseCase.getMyTable(currentSemester.year, currentSemester.semesterType)
                        } else {
                            Timetable(id = "-1", lectures = emptyList())
                        }
                    }
                } else {
                    timetableUseCase.getTable(selectedTimetableId)
                }

                syncManager.sync(timetable, glanceId)
            }
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "TimetableUpdateWorker Error")
            return Result.success()
        }
    }
}

object TimetableStateParser {
    private val STATE_KEY = stringPreferencesKey("timetable_state")

    fun parse(prefs: Preferences, tokenStorage: TokenStorageProtocol): TimetableUiState {
        val hasRefreshToken = tokenStorage.getRefreshToken() != null
        val jsonString = prefs[STATE_KEY]
        if (!jsonString.isNullOrBlank()) {
            val decoded = try {
                Json.decodeFromString<TimetableUiState>(jsonString)
            } catch (_: Exception) {
                TimetableUiState(signInRequired = true)
            }
            if (hasRefreshToken && decoded.signInRequired) {
                return TimetableUiState(signInRequired = false, timetable = null, isLoading = true)
            }
            return decoded
        }
        return if (hasRefreshToken) {
            TimetableUiState(signInRequired = false, timetable = null, isLoading = true)
        } else {
            TimetableUiState(signInRequired = true)
        }
    }
}

class RefreshTimetableAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val tokenStorage = entryPoint.tokenStorage()
        if (tokenStorage.getAccessToken() != null && shouldEnqueueRefresh(context)) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<TimetableUpdateWorker>()
                .setConstraints(constraints)
                .addTag("one_time_sync")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "one_time_sync",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        val intent = if (tokenStorage.getAccessToken() == null) {
            context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                putExtra(EXTRA_FROM_WIDGET, true)
            }
        } else {
            Intent(Intent.ACTION_VIEW, Constants.otlShareURL.toUri())
        }

        intent?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(this)
        }
    }

    private fun shouldEnqueueRefresh(context: Context): Boolean {
        val prefs = context.getSharedPreferences(REFRESH_PREFS, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val last = prefs.getLong(KEY_LAST_REFRESH, 0L)
        if (now - last < MIN_REFRESH_INTERVAL_MS) {
            return false
        }
        prefs.edit { putLong(KEY_LAST_REFRESH, now) }
        return true
    }

    private companion object {
        private const val REFRESH_PREFS = "widget_refresh"
        private const val KEY_LAST_REFRESH = "timetable_last_refresh"
        private const val MIN_REFRESH_INTERVAL_MS = 5 * 60 * 1000L
        private const val EXTRA_FROM_WIDGET = "extra_from_widget"
    }
}

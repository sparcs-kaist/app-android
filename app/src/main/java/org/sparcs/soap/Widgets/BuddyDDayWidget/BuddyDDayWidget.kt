package org.sparcs.soap.Widgets.BuddyDDayWidget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
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
import org.sparcs.soap.App.Domain.Models.OTL.Semester
import org.sparcs.soap.Widgets.BuddyDDayWidget.UI.DDayCircularWidgetView
import org.sparcs.soap.Widgets.BuddyDDayWidget.UI.DDayErrorView
import org.sparcs.soap.Widgets.BuddyDDayWidget.UI.DDayLoadingView
import org.sparcs.soap.Widgets.BuddyDDayWidget.UI.DDayRectangleWidgetView
import org.sparcs.soap.Widgets.BuddyDDayWidget.UI.DDaySignInRequiredView
import org.sparcs.soap.Widgets.BuddyDDayWidget.UI.DDaySmallWidgetView
import org.sparcs.soap.Widgets.WidgetEntryPoint
import org.sparcs.soap.Widgets.theme.ui.WidgetTheme
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

class BuddyDDayWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(60.dp, 60.dp),   // Circular
            DpSize(110.dp, 50.dp),  // Rectangle (4x1)
            DpSize(150.dp, 130.dp), // Small (2x2)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        runCatching {
            val entryPoint = EntryPointAccessors.fromApplication(appContext, WidgetEntryPoint::class.java)
            val tokenStorage = entryPoint.tokenStorage()

            provideContent {
                val prefs = currentState<Preferences>()
                val state = DDayStateParser.parse(prefs, tokenStorage)

                val themeMode = prefs[stringPreferencesKey("theme_mode")] ?: "System"
                val transparency = prefs[floatPreferencesKey("background_transparency")] ?: 1f

                // No automatic refresh here; manual refresh only.

                WidgetTheme(themeMode = themeMode) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .background(GlanceTheme.colors.surface.getColor(context).copy(alpha = transparency))
                    ) {
                        when {
                            state.signInRequired -> DDaySignInRequiredView()
                            state.entry == null -> DDayLoadingView()
                            state.entry.type == DDayType.ERROR -> DDayErrorView()
                            else -> {
                                val size = LocalSize.current
                                val entry = state.entry
                                when {
                                    size.width >= 140.dp && size.height >= 100.dp -> DDaySmallWidgetView(entry)
                                    size.width >= 110.dp -> DDayRectangleWidgetView(entry)
                                    else -> DDayCircularWidgetView(entry)
                                }
                            }
                        }
                    }

                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .clickable(onClick = actionRunCallback<RefreshAndOpenDDayAction>()),
                        contentAlignment = Alignment.Center
                    ) {}
                }
            }
        }.onFailure { e ->
            Timber.e(e, "DDay widget provideGlance failed; falling back to error view")
            provideContent {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.surface)
                        .padding(8.dp),
                ) {
                    DDayErrorView()
                }
            }
        }
    }
}

class DDayUpdateWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(applicationContext, WidgetEntryPoint::class.java)
        val syncManager = entryPoint.dDaySyncManager()
        val tokenStorage = entryPoint.tokenStorage()
        val timetableUseCase = entryPoint.timetableUseCase()

        return try {
            if (tokenStorage.getAccessToken() == null) {
                return Result.success()
            }

            val semester = try {
                timetableUseCase.getCurrentSemester()
            } catch (_: Exception) {
                null
            }

            if (semester == null) {
                syncManager.syncError()
                return Result.success()
            }

            val entry = buildDDayEntry(applicationContext, semester)
            syncManager.sync(entry)
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "DDayUpdateWorker Error")
            Result.success()
        }
    }

    private fun buildDDayEntry(context: Context, semester: Semester): DDayWidgetEntry {
        val now = Date()
        val semesterLabel = runCatching {
            "${semester.year} ${context.getString(semester.semesterType.rawValue)}"
        }.getOrElse {
            "${semester.year} ${semester.semesterType.name}"
        }

        val begin = semester.beginDate
        val end = semester.endDate

        return if (now.before(begin)) {
            DDayWidgetEntry(
                semesterLabel = semesterLabel,
                type = DDayType.START_OF_SEMESTER,
                days = daysBetween(now, begin),
                progress = 0f,
            )
        } else {
            val totalDays = daysBetween(begin, end).coerceAtLeast(1)
            val elapsedDays = daysBetween(begin, now).coerceAtLeast(0)
            val progress = (elapsedDays.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)

            DDayWidgetEntry(
                semesterLabel = semesterLabel,
                type = DDayType.END_OF_SEMESTER,
                days = daysBetween(now, end),
                progress = progress,
            )
        }
    }

    private fun daysBetween(from: Date, to: Date): Int {
        val fromCal = Calendar.getInstance().apply {
            time = from
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val toCal = Calendar.getInstance().apply {
            time = to
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMillis = toCal.timeInMillis - fromCal.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
    }
}

@Singleton
class DDayWidgetSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun sync(entry: DDayWidgetEntry) {
        val newState = entry.toDDayWidgetUiState()
        syncState(newState)
    }

    suspend fun syncSignInRequired() {
        syncState(BuddyDDayUiState(signInRequired = true, lastUpdated = System.currentTimeMillis()))
    }
    suspend fun syncError() {
        val state = BuddyDDayUiState(
            entry = DDayWidgetEntry("", DDayType.ERROR, 0, 0f),
            signInRequired = false,
            lastUpdated = System.currentTimeMillis()
        )
        syncState(state)
    }

    private suspend fun syncState(state: BuddyDDayUiState) {
        try {
            val jsonString = Json.encodeToString(state)
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(BuddyDDayWidget::class.java)

            glanceIds.forEach { id ->
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[stringPreferencesKey("d_day_state")] = jsonString
                    }
                }
            }
            BuddyDDayWidget().updateAll(context)
        } catch (e: Exception) {
            Timber.tag("DDayWidgetSync").e(e, "sync failed")
        }
    }
}

object DDayStateParser {
    private val STATE_KEY = stringPreferencesKey("d_day_state")

    fun parse(prefs: Preferences, tokenStorage: TokenStorageProtocol): BuddyDDayUiState {
        val hasRefreshToken = tokenStorage.getRefreshToken() != null
        val jsonString = prefs[STATE_KEY]
        if (!jsonString.isNullOrBlank()) {
            val decoded = try {
                Json.decodeFromString<BuddyDDayUiState>(jsonString)
            } catch (_: Exception) {
                BuddyDDayUiState(signInRequired = true)
            }
            if (hasRefreshToken && decoded.signInRequired) {
                return BuddyDDayUiState(signInRequired = false, entry = null, isLoading = true)
            }
            return decoded
        }

        return if (hasRefreshToken) {
            BuddyDDayUiState(signInRequired = false, entry = null, isLoading = true)
        } else {
            BuddyDDayUiState(signInRequired = true)
        }
    }
}

class RefreshAndOpenDDayAction : ActionCallback {
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

            val request = OneTimeWorkRequestBuilder<DDayUpdateWorker>()
                .setConstraints(constraints)
                .addTag("d_day_one_time_sync")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "d_day_one_time_sync",
                ExistingWorkPolicy.KEEP,
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
        prefs.edit().putLong(KEY_LAST_REFRESH, now).apply()
        return true
    }

    private companion object {
        private const val REFRESH_PREFS = "widget_refresh"
        private const val KEY_LAST_REFRESH = "d_day_last_refresh"
        private const val MIN_REFRESH_INTERVAL_MS = 5 * 60 * 1000L
        private const val EXTRA_FROM_WIDGET = "extra_from_widget"
    }
}

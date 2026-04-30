package org.sparcs.soap.Widgets.BuddyUpcomingClassWidget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
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
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
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
import org.sparcs.soap.App.Domain.Models.OTL.backgroundColor
import org.sparcs.soap.App.Domain.Models.OTL.textColor
import org.sparcs.soap.Widgets.BuddyUpcomingClassWidget.UI.UpcomingClassCircularWidgetView
import org.sparcs.soap.Widgets.BuddyUpcomingClassWidget.UI.UpcomingClassRectangleWidgetView
import org.sparcs.soap.Widgets.BuddyUpcomingClassWidget.UI.UpcomingClassSmallWidgetView
import org.sparcs.soap.Widgets.WidgetEntryPoint
import org.sparcs.soap.Widgets.theme.ui.WidgetTheme
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

class BuddyUpcomingClassWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(60.dp, 60.dp),   // Circular
            DpSize(150.dp, 110.dp), // Small
            DpSize(150.dp, 50.dp),  // Rectangle
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val entryPoint =
            EntryPointAccessors.fromApplication(appContext, WidgetEntryPoint::class.java)
        val tokenStorage = entryPoint.tokenStorage()

        provideContent {
            val prefs = currentState<Preferences>()
            val state = UpcomingClassStateParser.parse(prefs, tokenStorage)

            val themeMode = prefs[stringPreferencesKey("theme_mode")] ?: "System"
            val transparency = prefs[floatPreferencesKey("background_transparency")] ?: 1f

            if (state.entry == null && !state.signInRequired) {
                val request = OneTimeWorkRequestBuilder<UpcomingClassUpdateWorker>()
                    .addTag("upcoming_one_time_sync")
                    .build()

                WorkManager.getInstance(appContext).enqueueUniqueWork(
                    "upcoming_one_time_sync",
                    ExistingWorkPolicy.KEEP,
                    request
                )
            }

            WidgetTheme(themeMode = themeMode) {
                Box(
                    modifier = GlanceModifier.fillMaxSize().background(
                        GlanceTheme.colors.surface.getColor(context).copy(alpha = transparency)
                    )
                ) {
                    val entry = state.entry ?: WidgetLectureEntry.empty(state.signInRequired)
                    val size = LocalSize.current
                    when {
                        size.width >= 110.dp && size.height < 110.dp -> {
                            UpcomingClassRectangleWidgetView(entry)
                        }

                        size.width >= 110.dp && size.height >= 110.dp -> {
                            UpcomingClassSmallWidgetView(entry)
                        }

                        else -> UpcomingClassCircularWidgetView(entry)
                    }
                }
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .clickable(
                            onClick = actionRunCallback<RefreshAndOpenAppAction>()
                        )
                ) {}
            }
        }
    }
}

class UpcomingClassUpdateWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val glanceManager = GlanceAppWidgetManager(applicationContext)
        val glanceIds = glanceManager.getGlanceIds(BuddyUpcomingClassWidget::class.java)

        if (glanceIds.isEmpty()) {
            Timber.d("No installed widgets found. Stopping worker.")
            return Result.success()
        }
        val entryPoint =
            EntryPointAccessors.fromApplication(applicationContext, WidgetEntryPoint::class.java)
        val syncManager = entryPoint.upComingSyncManager()
        val tokenStorage = entryPoint.tokenStorage()
        val timetableUseCase = entryPoint.timetableUseCase()

        return try {
            val token = tokenStorage.getAccessToken()

            if (token == null || tokenStorage.isTokenExpired()) return Result.failure()

            val timetable = timetableUseCase.getCurrentMyTable()

            val now = Calendar.getInstance()
            val dayOfWeekString = when (now.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "MON"
                Calendar.TUESDAY -> "TUE"
                Calendar.WEDNESDAY -> "WED"
                Calendar.THURSDAY -> "THU"
                Calendar.FRIDAY -> "FRI"
                else -> ""
            }

            val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

            val nextLecture = timetable.lectures
                .flatMap { lecture ->
                    lecture.classes.map { time -> lecture to time }
                }
                .filter { (_, time) -> time.day.name == dayOfWeekString }
                .filter { (_, time) -> time.end > currentMinutes }
                .minByOrNull { (_, time) -> time.begin }

            val widgetEntry = if (nextLecture != null) {
                val (lecture, time) = nextLecture
                WidgetLectureEntry(
                    title = lecture.name + lecture.subtitle,
                    classroom = time.let { "(" + it.buildingCode + ") " + it.roomName },
                    day = time.day,
                    startMinutes = time.begin,
                    durationMinutes = time.let { it.end - it.begin },
                    bgColor = "#" + Integer.toHexString(lecture.backgroundColor.toArgb())
                        .uppercase(),
                    textColor = "#" + Integer.toHexString(lecture.textColor.toArgb()).uppercase(),
                    signInRequired = false
                )
            } else {
                WidgetLectureEntry.empty(false)
            }
            syncManager.sync(widgetEntry)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

@Singleton
class UpComingWidgetSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun sync(entry: WidgetLectureEntry) {
        try {
            val newState = entry.toUpcomingWidgetUiState()
            val jsonString = Json.encodeToString(newState)
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(BuddyUpcomingClassWidget::class.java)

            glanceIds.forEach { id ->
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[stringPreferencesKey("upcoming_class_state")] = jsonString
                    }
                }
            }
            BuddyUpcomingClassWidget().updateAll(context)
        } catch (e: Exception) {
            Timber.tag("WidgetSync").e("${e.message}")
        }
    }
}

object UpcomingClassStateParser {
    private val STATE_KEY = stringPreferencesKey("upcoming_class_state")

    fun parse(prefs: Preferences, tokenStorage: TokenStorageProtocol): UpcomingClassUiState {
        val jsonString = prefs[STATE_KEY]
        if (!jsonString.isNullOrBlank()) {
            return try {
                Json.decodeFromString<UpcomingClassUiState>(jsonString)
            } catch (e: Exception) {
                UpcomingClassUiState(signInRequired = true)
            }
        }
        return if (tokenStorage.getAccessToken() != null && !tokenStorage.isTokenExpired()) {
            UpcomingClassUiState(signInRequired = false, entry = null)
        } else {
            UpcomingClassUiState(signInRequired = true)
        }
    }
}

class RefreshAndOpenAppAction : ActionCallback {
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
        if (tokenStorage.getAccessToken() != null && !tokenStorage.isTokenExpired()) {

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<UpcomingClassUpdateWorker>()
                .setConstraints(constraints)
                .addTag("upcoming_one_time_sync")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "upcoming_one_time_sync",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        val intent = if (tokenStorage.getAccessToken() == null || tokenStorage.isTokenExpired()) {
            context.packageManager.getLaunchIntentForPackage(context.packageName)
        } else {
            Intent(Intent.ACTION_VIEW, Uri.parse(Constants.otlShareURL))
        }

        intent?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(this)
        }
    }
}
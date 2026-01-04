package org.sparcs.Widgets.BuddyUpcomingClassWidget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sparcs.App.Domain.Helpers.Constants
import org.sparcs.App.Domain.Helpers.TokenStorageProtocol
import org.sparcs.App.Domain.Models.OTL.backgroundColor
import org.sparcs.App.Domain.Models.OTL.textColor
import org.sparcs.Widgets.BuddyTimetableWidget.WidgetEntryPoint
import java.util.Calendar

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
        val entryPoint = EntryPointAccessors.fromApplication(appContext, WidgetEntryPoint::class.java)
        val tokenStorage = entryPoint.tokenStorage()

        provideContent {
            val prefs = currentState<Preferences>()
            val state = UpcomingClassStateParser.parse(prefs, tokenStorage)

            if (state.entry == null && !state.signInRequired) {
                val request = OneTimeWorkRequestBuilder<UpcomingClassUpdateWorker>().build()
                WorkManager.getInstance(appContext).enqueueUniqueWork(
                    "upcoming_one_time_sync",
                    ExistingWorkPolicy.KEEP,
                    request
                )
            }

            GlanceTheme {
                Box(modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.surface)) {
                    val entry = state.entry ?: WidgetLectureEntry.empty(state.signInRequired)

                    val size = LocalSize.current
                    when {
                        size.height < 100.dp && size.width >= 110.dp -> {
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
                            onClick = actionStartActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(Constants.otlShareURL)
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        )
                ) {}
            }
        }
    }
}
class UpcomingClassUpdateWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(applicationContext, WidgetEntryPoint::class.java)
        val timetableUseCase = entryPoint.timetableUseCase()
        val tokenStorage = entryPoint.tokenStorage()

        return try {
            if (tokenStorage.getAccessToken() == null) return Result.failure()

            timetableUseCase.load()
            val timetable = timetableUseCase.selectedTimetable.filterNotNull().first()

            val now = Calendar.getInstance()
            val calendarDay = now.get(Calendar.DAY_OF_WEEK)

            val domainDayOfWeek = when (calendarDay) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
            val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

            val nextLecture = timetable.lectures
                .flatMap { lecture ->
                    lecture.classTimes.map { time -> lecture to time }
                }
                .filter { (lecture, time) -> time.day.value == domainDayOfWeek }
                .filter { (lecture, time) -> time.end > currentMinutes }
                .minByOrNull { (lecture, time) -> time.begin }

            val widgetEntry = if (nextLecture != null) {
                val (lecture, time) = nextLecture
                WidgetLectureEntry(
                    title = lecture.title.localized(),
                    classroom = time.classroomNameShort.localized(),
                    day = time.day,
                    startMinutes = time.begin,
                    durationMinutes = time.duration,
                    bgColor = "#" + Integer.toHexString(lecture.backgroundColor.toArgb()).uppercase(),
                    textColor = "#" + Integer.toHexString(lecture.textColor.toArgb()).uppercase(),
                    signInRequired = false
                )
            } else {
                WidgetLectureEntry.empty(false)
            }
            sync(widgetEntry)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun sync(entry: WidgetLectureEntry) {
        val jsonString = Json.encodeToString(UpcomingClassUiState(entry = entry))
        val manager = GlanceAppWidgetManager(applicationContext)
        val glanceIds = manager.getGlanceIds(BuddyUpcomingClassWidget::class.java)

        glanceIds.forEach { id ->
            updateAppWidgetState(applicationContext, id) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[stringPreferencesKey("upcoming_class_state")] = jsonString
                }
            }
        }
        BuddyUpcomingClassWidget().updateAll(applicationContext)
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
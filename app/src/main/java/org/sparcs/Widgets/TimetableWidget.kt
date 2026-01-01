package org.sparcs.Widgets

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
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
import androidx.glance.unit.ColorProvider
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sparcs.App.Domain.Helpers.Constants
import org.sparcs.App.Domain.Helpers.TokenStorageProtocol
import org.sparcs.App.Domain.Models.OTL.Timetable
import org.sparcs.App.Domain.Usecases.TimetableUseCaseProtocol
import javax.inject.Inject
import javax.inject.Singleton

class TimetableWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(appContext, WidgetEntryPoint::class.java)
        val tokenStorage = entryPoint.tokenStorage()

        provideContent {
            val prefs = currentState<Preferences>()
            val state = TimetableStateParser.parse(prefs, tokenStorage)

            if (state.timetable == null && !state.signInRequired) {
                val request = OneTimeWorkRequestBuilder<TimetableUpdateWorker>().build()
                WorkManager.getInstance(appContext).enqueueUniqueWork(
                    "one_time_sync",
                    ExistingWorkPolicy.KEEP,
                    request
                )
            }

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(GlanceTheme.colors.surface)
            ) {
                if (state.signInRequired) {
                    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("로그인이 필요합니다.", style = TextStyle(color = ColorProvider(Color.Black)))
                    }
                } else if (state.timetable == null) {
                    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("데이터를 가져오는 중...", style = TextStyle(color = ColorProvider(Color.Black)))
                            Text("잠시만 기다려주세요.", style = TextStyle(fontSize = 12.sp, color = ColorProvider(Color.Gray)))
                        }
                    }
                } else {
                    Column(modifier = GlanceModifier.fillMaxSize()
                    ) {
                        TimetableLargeWidgetView(timetable = state.timetable!!)
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
                    ){}
                }
            }
        }
    }
}

@Singleton
class WidgetSyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun sync(timetable: Timetable) {
        try {
            val newState = timetable.toWidgetUiState()
            val jsonString = Json.encodeToString(newState)
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(TimetableWidget::class.java)

            glanceIds.forEach { id ->
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[stringPreferencesKey("timetable_state")] = jsonString
                    }
                }
            }
            TimetableWidget().updateAll(context)
        } catch (e: Exception) {
            Log.e("WidgetSync", "${e.message}")
        }
    }
}
class TimetableUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(applicationContext, WidgetEntryPoint::class.java)
        val syncManager = entryPoint.syncManager()
        val tokenStorage = entryPoint.tokenStorage()
        val timetableUseCase = entryPoint.timetableUseCase()

        return try {
            val token = tokenStorage.getAccessToken()
            if (token == null || tokenStorage.isTokenExpired()) return Result.failure()

            timetableUseCase.load()
            Log.d("AASASD", "loaded")
            val timetable = timetableUseCase.selectedTimetable
                .filterNotNull()
                .first()

            syncManager.sync(timetable)
            Result.success()
        } catch (e: Exception) {
            Log.e("TimetableWorker", "에러 발생: ${e.message}")
            Result.retry()
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun tokenStorage(): TokenStorageProtocol
    fun syncManager(): WidgetSyncManager
    fun timetableUseCase(): TimetableUseCaseProtocol
}

object TimetableStateParser {
    private val STATE_KEY = stringPreferencesKey("timetable_state")

    fun parse(prefs: Preferences, tokenStorage: TokenStorageProtocol): TimetableUiState {
        val jsonString = prefs[STATE_KEY]
        if (!jsonString.isNullOrBlank()) {
            return try {
                Json.decodeFromString<TimetableUiState>(jsonString)
            } catch (e: Exception) {
                TimetableUiState(signInRequired = true)
            }
        }
        return if (tokenStorage.getAccessToken() != null && !tokenStorage.isTokenExpired()) {
            TimetableUiState(signInRequired = false, timetable = null)
        } else {
            TimetableUiState(signInRequired = true)
        }
    }
}
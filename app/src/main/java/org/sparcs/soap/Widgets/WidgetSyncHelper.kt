package org.sparcs.soap.Widgets

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import org.sparcs.soap.Widgets.BuddyDDayWidget.DDayUpdateWorker
import org.sparcs.soap.Widgets.BuddyDDayWidget.DDayWidgetSyncManager
import org.sparcs.soap.Widgets.BuddyTimetableWidget.TimetableUpdateWorker
import org.sparcs.soap.Widgets.BuddyTimetableWidget.TimetableWidgetSyncManager
import org.sparcs.soap.Widgets.BuddyUpcomingClassWidget.UpComingWidgetSyncManager
import org.sparcs.soap.Widgets.BuddyUpcomingClassWidget.UpcomingClassUpdateWorker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetSyncHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    @DDayWidget private val dDaySyncManager: DDayWidgetSyncManager,
    @TimetableWidget private val timetableSyncManager: TimetableWidgetSyncManager,
    @UpcomingWidget private val upComingSyncManager: UpComingWidgetSyncManager,
) {
    fun refreshAllWidgets() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val dDayRequest = OneTimeWorkRequestBuilder<DDayUpdateWorker>()
            .setConstraints(constraints)
            .addTag("d_day_one_time_sync")
            .build()

        val upcomingRequest = OneTimeWorkRequestBuilder<UpcomingClassUpdateWorker>()
            .setConstraints(constraints)
            .addTag("upcoming_one_time_sync")
            .build()

        val timetableRequest = OneTimeWorkRequestBuilder<TimetableUpdateWorker>()
            .setConstraints(constraints)
            .addTag("timetable_one_time_sync")
            .build()

        val workManager = WorkManager.getInstance(context)
        
        workManager.enqueueUniqueWork(
            "d_day_one_time_sync",
            ExistingWorkPolicy.REPLACE,
            dDayRequest
        )
        
        workManager.enqueueUniqueWork(
            "upcoming_one_time_sync",
            ExistingWorkPolicy.REPLACE,
            upcomingRequest
        )
        
        workManager.enqueueUniqueWork(
            "timetable_one_time_sync",
            ExistingWorkPolicy.REPLACE,
            timetableRequest
        )
    }

    suspend fun clearAllWidgets() {
        dDaySyncManager.syncSignInRequired()
        timetableSyncManager.syncSignInRequired()
        upComingSyncManager.syncSignInRequired()
    }
}

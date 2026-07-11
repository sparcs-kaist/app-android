package org.sparcs.soap.widgets

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import org.sparcs.soap.widgets.araPortalWidget.AraPortalUpdateWorker
import org.sparcs.soap.widgets.araPortalWidget.AraPortalWidgetSyncManager
import org.sparcs.soap.widgets.buddyDDayWidget.DDayUpdateWorker
import org.sparcs.soap.widgets.buddyTimetableWidget.TimetableUpdateWorker
import org.sparcs.soap.widgets.buddyTimetableWidget.TimetableWidgetSyncManager
import org.sparcs.soap.widgets.buddyUpcomingClassWidget.UpComingWidgetSyncManager
import org.sparcs.soap.widgets.buddyUpcomingClassWidget.UpcomingClassUpdateWorker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetSyncHelper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:TimetableWidget private val timetableSyncManager: TimetableWidgetSyncManager,
    @param:UpcomingWidget private val upComingSyncManager: UpComingWidgetSyncManager,
    private val araPortalSyncManager: AraPortalWidgetSyncManager,
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

        val araPortalRequest = OneTimeWorkRequestBuilder<AraPortalUpdateWorker>()
            .setConstraints(constraints)
            .addTag("ara_portal_one_time_sync")
            .build()

        val workManager = WorkManager.getInstance(context)

        workManager.enqueueUniqueWork(
            "ara_portal_one_time_sync",
            ExistingWorkPolicy.REPLACE,
            araPortalRequest
        )
        
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
        timetableSyncManager.syncSignInRequired()
        upComingSyncManager.syncSignInRequired()
        araPortalSyncManager.syncSignInRequired()
    }
}

package org.sparcs.soap.Widgets.BuddyDDayWidget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class BuddyDDayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BuddyDDayWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        enqueueInitialFetch(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        enqueueInitialFetch(context)
    }

    private fun enqueueInitialFetch(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<DDayUpdateWorker>()
            .setConstraints(constraints)
            .addTag("d_day_one_time_sync")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "d_day_one_time_sync",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}

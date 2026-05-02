package org.sparcs.soap.Widgets.AraPortalWidget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class AraPortalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AraPortalWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        enqueueWork(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        enqueueWork(context)
    }

    private fun enqueueWork(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeRequest = OneTimeWorkRequestBuilder<AraPortalUpdateWorker>()
            .setConstraints(constraints)
            .addTag("ara_portal_immediate_work")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "ara_portal_immediate_work",
            ExistingWorkPolicy.REPLACE,
            oneTimeRequest
        )

        val periodicRequest = PeriodicWorkRequestBuilder<AraPortalUpdateWorker>(
            30,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag("ara_portal_sync_work")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "ara_portal_sync_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )
    }
}

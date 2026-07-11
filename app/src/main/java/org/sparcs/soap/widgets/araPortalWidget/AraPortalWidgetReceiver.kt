package org.sparcs.soap.widgets.araPortalWidget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

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

        val request = OneTimeWorkRequestBuilder<AraPortalUpdateWorker>()
            .setConstraints(constraints)
            .addTag("ara_portal_one_time_sync")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "ara_portal_one_time_sync",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}

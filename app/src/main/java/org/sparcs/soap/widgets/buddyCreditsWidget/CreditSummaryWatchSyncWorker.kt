package org.sparcs.soap.widgets.buddyCreditsWidget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CancellationException
import org.sparcs.soap.widgets.WidgetEntryPoint
import timber.log.Timber

class CreditSummaryWatchSyncWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        EntryPointAccessors.fromApplication(applicationContext, WidgetEntryPoint::class.java)
            .creditSummarySyncManager().syncWatch()
        Result.success()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        Timber.e(error, "Could not sync credit summary to watch")
        Result.retry()
    }
}

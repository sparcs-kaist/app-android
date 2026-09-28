package org.sparcs.soap.widgets.buddyCreditsWidget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.sparcs.soap.app.domain.helpers.CreditSummaryPublisher
import org.sparcs.soap.app.domain.helpers.CreditSummarySnapshotStore
import org.sparcs.soap.app.domain.helpers.TokenStorageProtocol
import org.sparcs.soap.app.domain.models.otl.CreditSummarySnapshot
import org.sparcs.soap.wearable.WearableDataManager
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreditSummarySyncManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val store: CreditSummarySnapshotStore,
    private val tokenStorage: TokenStorageProtocol,
    private val wearableDataManager: WearableDataManager,
) : CreditSummaryPublisher {
    private val mutex = Mutex()
    private val watchMutex = Mutex()
    @Volatile override var revision: Long = 0
        private set

    override suspend fun publish(snapshot: CreditSummarySnapshot, expectedRevision: Long) = mutex.withLock {
        if (expectedRevision != revision || tokenStorage.getAccessToken() == null) return@withLock
        val changed = store.snapshot?.hasSameValues(snapshot) != true
        if (changed) store.save(snapshot)
        enqueueWatchSync()
        if (changed) refreshWidgets()
    }

    suspend fun clear() = mutex.withLock {
        revision++
        store.clear()
        enqueueWatchSync()
        refreshWidgets()
    }

    private fun enqueueWatchSync() {
        WorkManager.getInstance(context).enqueueUniqueWork(
            "credit_summary_watch_sync",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            OneTimeWorkRequestBuilder<CreditSummaryWatchSyncWorker>().build(),
        )
    }

    suspend fun syncWatch() = watchMutex.withLock {
        val snapshot = mutex.withLock {
            if (tokenStorage.getAccessToken() == null) null else store.snapshot
        }
        wearableDataManager.updateCreditSummary(snapshot)
    }

    private suspend fun refreshWidgets() {
        try {
            BuddyCreditsWidget().updateAll(context)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Timber.e(error, "Could not refresh credit widgets")
        }
    }
}

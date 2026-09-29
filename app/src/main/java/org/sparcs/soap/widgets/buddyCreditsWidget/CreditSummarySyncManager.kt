package org.sparcs.soap.widgets.buddyCreditsWidget

import android.content.Context
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sparcs.soap.app.domain.helpers.CreditSummaryPublisher
import org.sparcs.soap.app.domain.helpers.CreditSummarySnapshotStore
import org.sparcs.soap.app.domain.helpers.TokenStorageProtocol
import org.sparcs.soap.app.domain.models.otl.CreditSummarySnapshot
import org.sparcs.soap.wearable.WearableDataManager
import org.sparcs.soap.widgets.installedWidgetIds
import org.sparcs.soap.widgets.updateInstalledWidgets
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
        refreshWidgets()
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

    suspend fun refreshWidgets() {
        try {
            val snapshot = if (tokenStorage.getAccessToken() == null) null else store.snapshot
            val jsonString = snapshot?.let { Json.encodeToString(it) } ?: ""
            val glanceIds = installedWidgetIds(context, BuddyCreditsWidget::class.java)
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[CREDIT_SUMMARY_STATE_KEY] = jsonString
                    }
                }
            }
            BuddyCreditsWidget().updateInstalledWidgets(context)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Timber.e(error, "Could not refresh credit widgets")
        }
    }
}

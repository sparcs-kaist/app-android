package org.sparcs.soap.widgets.buddyCreditsWidget

import android.content.Context
import androidx.glance.appwidget.updateAll
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
    @Volatile override var revision: Long = 0
        private set

    override suspend fun publish(snapshot: CreditSummarySnapshot, expectedRevision: Long) = mutex.withLock {
        if (expectedRevision != revision || tokenStorage.getAccessToken() == null) return@withLock
        val changed = store.snapshot?.hasSameValues(snapshot) != true
        if (changed) store.save(snapshot)
        wearableDataManager.updateCreditSummary(snapshot)
        if (changed) refreshWidgets()
    }

    suspend fun clear() = mutex.withLock {
        revision++
        store.clear()
        wearableDataManager.updateCreditSummary(null)
        refreshWidgets()
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

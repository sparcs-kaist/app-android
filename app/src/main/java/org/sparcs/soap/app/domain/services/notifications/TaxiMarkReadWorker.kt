package org.sparcs.soap.app.domain.services.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException
import org.sparcs.soap.app.domain.repositories.taxi.TaxiChatRepositoryProtocol
import timber.log.Timber

/**
 * Marks a room read on the server, after replying from its notification. Run as work, not by the
 * reply's receiver, which has only a few seconds; failing only leaves the room's badge for now.
 */
class TaxiMarkReadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencies {
        fun taxiChatRepository(): TaxiChatRepositoryProtocol
    }

    override suspend fun doWork(): Result {
        val roomId = inputData.getString(KEY_ROOM_ID) ?: return Result.failure()
        return try {
            EntryPointAccessors.fromApplication(applicationContext, Dependencies::class.java)
                .taxiChatRepository().readChats(roomId)
            Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Timber.e(error, "[TaxiMarkReadWorker] Could not mark the room read")
            if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val KEY_ROOM_ID = "room_id"
        private const val MAX_ATTEMPTS = 3

        fun enqueue(context: Context, roomId: String) {
            val request = OneTimeWorkRequestBuilder<TaxiMarkReadWorker>()
                .setInputData(workDataOf(KEY_ROOM_ID to roomId))
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork("taxi_mark_read_$roomId", ExistingWorkPolicy.REPLACE, request)
        }
    }
}

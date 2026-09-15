package org.sparcs.soap.app.domain.helpers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

internal class TokenRefreshCoordinator(private val scope: CoroutineScope) {
    private val lock = Any()
    private var refreshJob: Deferred<Unit>? = null

    suspend fun refresh(refreshTokens: suspend () -> Unit) {
        val job = synchronized(lock) {
            refreshJob?.takeUnless { it.isCompleted }
                ?: scope.async(start = CoroutineStart.LAZY) {
                    refreshTokens()
                }.also { refreshJob = it }
        }
        job.await()
    }

    fun cancel() {
        synchronized(lock) {
            refreshJob?.cancel()
            refreshJob = null
        }
    }
}

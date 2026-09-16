package org.sparcs.soap.timetableTests

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.helpers.TokenRefreshCoordinator

@OptIn(ExperimentalCoroutinesApi::class)
class TokenRefreshCoordinatorTest {
    @Test
    fun concurrentRequestsShareOneTokenRefresh() = runTest {
        val coordinator = TokenRefreshCoordinator(backgroundScope)
        val response = CompletableDeferred<Unit>()
        var refreshCount = 0
        val requests = List(20) {
            async {
                coordinator.refresh {
                    refreshCount++
                    response.await()
                }
            }
        }

        runCurrent()
        assertEquals(1, refreshCount)
        assertTrue(requests.none { it.isCompleted })

        response.complete(Unit)
        requests.forEach { it.await() }
        assertEquals(1, refreshCount)
    }

    @Test
    fun cancellingWidgetDoesNotCancelSharedRefresh() = runTest {
        val coordinator = TokenRefreshCoordinator(backgroundScope)
        val response = CompletableDeferred<Unit>()
        var refreshed = false
        val widget = launch {
            coordinator.refresh {
                response.await()
                refreshed = true
            }
        }
        runCurrent()
        widget.cancel()
        val foreground = async { coordinator.refresh { error("Duplicate refresh") } }
        runCurrent()
        assertFalse(refreshed)

        response.complete(Unit)
        foreground.await()
        assertTrue(refreshed)
    }

    @Test
    fun failedRefreshCanBeRetried() = runTest {
        val scope = CoroutineScope(coroutineContext + SupervisorJob())
        try {
            val coordinator = TokenRefreshCoordinator(scope)
            val failure = runCatching { coordinator.refresh { error("Offline") } }
            assertTrue(failure.isFailure)
            var refreshed = false
            coordinator.refresh { refreshed = true }
            assertTrue(refreshed)
        } finally {
            scope.cancel()
        }
    }
}

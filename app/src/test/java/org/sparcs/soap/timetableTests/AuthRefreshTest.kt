package org.sparcs.soap.timetableTests

import androidx.activity.ComponentActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.sparcs.soap.app.cache.CachedTimetable
import org.sparcs.soap.app.cache.TimetableCache
import org.sparcs.soap.app.cache.TimetableCacheDAO
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.helpers.TokenStorageProtocol
import org.sparcs.soap.app.domain.services.AuthenticationServiceProtocol
import org.sparcs.soap.app.domain.usecases.AuthUseCase
import org.sparcs.soap.app.networking.responseDTO.auth.TokenResponseDTO
import org.sparcs.soap.testSupport.MainDispatcherRule
import org.sparcs.soap.widgets.WidgetSyncHelper
import org.sparcs.soap.widgets.buddyTimetableWidget.TimetableWidgetSyncManager
import org.sparcs.soap.widgets.buddyUpcomingClassWidget.UpComingWidgetSyncManager
import java.io.IOException
import java.lang.reflect.Proxy
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class AuthRefreshTest {
    @get:Rule val dispatcher = MainDispatcherRule()
    private val storage = Storage()
    private val service = Service()

    private fun useCase(): AuthUseCase {
        val context = RuntimeEnvironment.getApplication()
        val dao = object : TimetableCacheDAO {
            override suspend fun getTimetable(key: String): CachedTimetable? = null
            override suspend fun saveTimetable(timetable: CachedTimetable) = Unit
            override suspend fun invalidate(key: String) = Unit
            override suspend fun getSummaries(): List<CachedTimetable> = emptyList()
            override suspend fun clear() = Unit
        }
        return AuthUseCase(
            service, storage, unused(), unused(), unused(), unused(),
            WidgetSyncHelper(context, TimetableWidgetSyncManager(context), UpComingWidgetSyncManager(context)),
            TimetableCache(dao),
        )
    }

    @Test fun validTokenIsReusedEvenDuringRefreshCooldown() = runTest {
        val model = useCase()
        service.failure = NetworkError.NoConnection()
        assertTrue(runCatching { model.refreshAccessToken() }.isFailure)
        storage.access = "valid"
        storage.expired = false
        model.refreshAccessToken()
        assertEquals("valid", model.getValidAccessToken())
        assertEquals(1, service.calls)
        assertTrue(model.isAuthenticatedFlow.first())
    }

    @Test fun missingAccessTokenRefreshesEvenWithFutureExpiration() = runTest {
        storage.expired = false
        val model = useCase()
        assertEquals("new-access", model.getValidAccessToken())
        assertEquals(1, service.calls)
        assertEquals("new-refresh", storage.refresh)
    }

    @Test fun unreadableRefreshTokenDoesNotSignOutOrClearStorage() = runTest {
        val failure = IOException("Keystore unavailable")
        storage.readFailure = failure
        val model = useCase()
        val error = runCatching { model.refreshAccessToken() }.exceptionOrNull()
        assertTrue(error is IOException)
        assertEquals(failure.message, error?.message)
        assertEquals(0, storage.clears)
        assertEquals(0, service.calls)
        assertTrue(model.isAuthenticatedFlow.first())
    }

    @Test fun writeFailureIsNotReportedAsRefreshSuccess() = runTest {
        val failure = IOException("Storage unavailable")
        storage.saveFailure = failure
        val model = useCase()
        val error = runCatching { model.getValidAccessToken() }.exceptionOrNull()
        assertTrue(error is IOException)
        assertEquals(failure.message, error?.message)
        assertEquals("old-refresh", storage.refresh)
        assertEquals(0, storage.clears)
        assertTrue(model.isAuthenticatedFlow.first())
    }

    @Test fun networkFailureRetainsItsCauseAndSession() = runTest {
        val failure = NetworkError.NoConnection()
        service.failure = failure
        val model = useCase()
        assertSame(failure, runCatching { model.getValidAccessToken() }.exceptionOrNull())
        assertEquals(0, storage.clears)
        assertTrue(model.isAuthenticatedFlow.first())
    }

    private class Storage : TokenStorageProtocol {
        var access: String? = null
        var refresh = "old-refresh"
        var expired = true
        var clears = 0
        var readFailure: Exception? = null
        var saveFailure: Exception? = null
        override fun save(accessToken: String, refreshToken: String) {
            saveFailure?.let { throw it }
            access = accessToken
            refresh = refreshToken
            expired = false
        }
        override fun getAccessToken() = access
        override fun getRefreshToken(): String? = null
        override fun hasStoredRefreshToken() = true
        override fun readRefreshToken(): String {
            readFailure?.let { throw it }
            return refresh
        }
        override fun isTokenExpired() = expired
        override fun getTokenExpirationDate(): Date? = null
        override fun clearTokens() { clears++ }
    }

    private class Service : AuthenticationServiceProtocol {
        var calls = 0
        var failure: Exception? = null
        override suspend fun authenticate(activity: ComponentActivity): Nothing = error("Unexpected sign in")
        override suspend fun refreshAccessToken(refreshToken: String): TokenResponseDTO {
            calls++
            failure?.let { throw it }
            return TokenResponseDTO("new-access", "new-refresh")
        }
    }

    private inline fun <reified T> unused(): T = Proxy.newProxyInstance(
        T::class.java.classLoader, arrayOf(T::class.java),
    ) { _, method, _ -> error("Unexpected call: ${method.name}") } as T
}

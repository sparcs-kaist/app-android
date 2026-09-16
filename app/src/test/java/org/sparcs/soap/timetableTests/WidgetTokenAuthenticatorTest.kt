package org.sparcs.soap.timetableTests

import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.usecases.AuthUseCaseProtocol
import org.sparcs.soap.app.networking.TokenAuthenticator
import org.sparcs.soap.buddyTestSupport.useCase.MockAuthUseCase
import javax.inject.Provider

class WidgetTokenAuthenticatorTest {
    @Test
    fun rejectedAccessTokenIsRefreshedEvenBeforeLocalExpiry() {
        val auth = TestAuthUseCase("rejected-token")
        val authenticator = TokenAuthenticator(Provider { auth })

        val request = authenticator.authenticate(null, unauthorizedResponse("rejected-token"))

        assertEquals(1, auth.refreshCount)
        assertEquals("Bearer refreshed-token", request?.header("Authorization"))
    }

    @Test
    fun responseUsingOldTokenReusesAlreadyRefreshedToken() {
        val auth = TestAuthUseCase("current-token")
        val authenticator = TokenAuthenticator(Provider { auth })

        val request = authenticator.authenticate(null, unauthorizedResponse("old-token"))

        assertEquals(0, auth.refreshCount)
        assertEquals("Bearer current-token", request?.header("Authorization"))
    }

    private fun unauthorizedResponse(token: String): Response = Response.Builder()
        .request(
            Request.Builder()
                .url("https://example.invalid/timetable")
                .header("Authorization", "Bearer $token")
                .build()
        )
        .protocol(Protocol.HTTP_1_1)
        .code(401)
        .message("Unauthorized")
        .build()

    private class TestAuthUseCase(private var token: String) : AuthUseCaseProtocol by MockAuthUseCase() {
        var refreshCount = 0
            private set

        override fun getAccessToken(): String = token
        override suspend fun getValidAccessToken(): String = token

        override suspend fun refreshAccessToken(force: Boolean) {
            assertTrue(force)
            refreshCount++
            token = "refreshed-token"
        }
    }
}

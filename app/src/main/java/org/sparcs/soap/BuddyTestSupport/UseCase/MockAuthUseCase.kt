package org.sparcs.soap.BuddyTestSupport.UseCase

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.sparcs.soap.App.Domain.Usecases.AuthUseCaseProtocol

class MockAuthUseCase : AuthUseCaseProtocol {
    private var signOutResult: Result<Unit> = Result.success(Unit)
    private var signOutCallCount = 0

    override val isAuthenticatedFlow: Flow<Boolean>
        get() = flowOf(true)

    override suspend fun signIn(activity: Activity) {}

    override suspend fun signOut() {
        signOutCallCount += 1
        signOutResult.getOrThrow()
    }

    override fun getAccessToken(): String? = null

    override suspend fun getValidAccessToken(): String = ""

    override suspend fun refreshAccessToken(force: Boolean) {}
}
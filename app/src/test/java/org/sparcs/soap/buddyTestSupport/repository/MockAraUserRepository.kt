package org.sparcs.soap.buddyTestSupport.repository

import kotlinx.coroutines.CompletableDeferred
import org.sparcs.soap.app.domain.models.ara.AraBlockedUser
import org.sparcs.soap.app.domain.models.ara.AraUser
import org.sparcs.soap.app.domain.repositories.ara.AraUserRepositoryProtocol
import org.sparcs.soap.app.networking.responseDTO.ara.AraSignInResponseDTO

class MockAraUserRepository : AraUserRepositoryProtocol {
    var blockResult: Result<Unit> = Result.success(Unit)
    var fetchBlockedUsersResult: Result<List<AraBlockedUser>> = Result.success(emptyList())

    val blockedUserIds = mutableListOf<Int>()
    val unblockedUserIds = mutableListOf<Int>()
    var unblockGate: CompletableDeferred<Unit>? = null

    override suspend fun register(ssoInfo: String): AraSignInResponseDTO = throw IllegalStateException("TestError.notConfigured")
    override suspend fun agreeTOS(userID: Int) {}
    override suspend fun fetchUser(): AraUser = throw IllegalStateException("TestError.notConfigured")
    override suspend fun updateMe(id: Int, params: Map<String, Any>) {}

    override suspend fun blockUser(userID: Int) {
        blockResult.getOrThrow()
        blockedUserIds += userID
    }

    override suspend fun unblockUser(userID: Int) {
        unblockGate?.await()
        blockResult.getOrThrow()
        unblockedUserIds += userID
    }

    override suspend fun fetchBlockedUsers(): List<AraBlockedUser> = fetchBlockedUsersResult.getOrThrow()
}

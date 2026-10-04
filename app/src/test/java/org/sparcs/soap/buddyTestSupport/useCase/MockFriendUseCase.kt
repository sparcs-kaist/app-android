package org.sparcs.soap.buddyTestSupport.useCase

import org.sparcs.soap.app.domain.models.otl.Friend
import org.sparcs.soap.app.domain.models.otl.FriendList
import org.sparcs.soap.app.domain.usecases.otl.FriendUseCaseProtocol
import org.sparcs.soap.app.shared.mocks.otl.mockList

class MockFriendUseCase : FriendUseCaseProtocol {

    var fetchFriendsResult: Result<FriendList> =
        Result.success(FriendList(checkedAt = null, friends = Friend.mockList()))
    var addFriendResult: Result<Unit> = Result.success(Unit)
    var deleteFriendResult: Result<Unit> = Result.success(Unit)
    var setFavoriteResult: Result<Unit> = Result.success(Unit)
    var fetchMyCodeResult: Result<String> = Result.success("ACD347")

    var fetchFriendsCallCount = 0
    var fetchMyCodeCallCount = 0
    var lastAddedCode: String? = null
    var lastDeletedID: Int? = null
    var lastFavorite: Pair<Int, Boolean>? = null

    override suspend fun fetchFriends(): FriendList {
        fetchFriendsCallCount += 1
        return fetchFriendsResult.getOrThrow()
    }

    override suspend fun addFriend(code: String) {
        lastAddedCode = code
        addFriendResult.getOrThrow()
    }

    override suspend fun deleteFriend(id: Int) {
        lastDeletedID = id
        deleteFriendResult.getOrThrow()
    }

    override suspend fun setFavorite(id: Int, isFavorite: Boolean) {
        lastFavorite = id to isFavorite
        setFavoriteResult.getOrThrow()
    }

    override suspend fun fetchMyCode(): String {
        fetchMyCodeCallCount += 1
        return fetchMyCodeResult.getOrThrow()
    }
}

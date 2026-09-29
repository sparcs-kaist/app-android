package org.sparcs.soap.app.domain.usecases.otl

import kotlinx.coroutines.CancellationException
import org.sparcs.soap.app.domain.error.CrashContext
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.error.otl.FriendUseCaseError
import org.sparcs.soap.app.domain.models.otl.FriendList
import org.sparcs.soap.app.domain.repositories.otl.OTLFriendRepositoryProtocol
import org.sparcs.soap.app.domain.services.CrashlyticsServiceProtocol
import javax.inject.Inject

interface FriendUseCaseProtocol {
    suspend fun fetchFriends(): FriendList
    suspend fun addFriend(code: String)
    suspend fun deleteFriend(id: Int)
    suspend fun setFavorite(id: Int, isFavorite: Boolean)
    suspend fun fetchMyCode(): String
}

class FriendUseCase @Inject constructor(
    private val friendRepository: OTLFriendRepositoryProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol? = null,
) : FriendUseCaseProtocol {

    // MARK: - Properties
    private val feature: String = "Friend"

    // MARK: - Functions
    override suspend fun fetchFriends(): FriendList = execute("fetchFriends") {
        friendRepository.fetchFriends()
    }

    override suspend fun addFriend(code: String) = execute("addFriend") {
        friendRepository.addFriend(code)
    }

    override suspend fun deleteFriend(id: Int) = execute("deleteFriend", "friendID" to id.toString()) {
        friendRepository.deleteFriend(id)
    }

    override suspend fun setFavorite(id: Int, isFavorite: Boolean) = execute(
        "setFavorite",
        "friendID" to id.toString(),
        "isFavorite" to isFavorite.toString()
    ) {
        friendRepository.setFavorite(id, isFavorite)
    }

    override suspend fun fetchMyCode(): String = execute("fetchMyCode") {
        friendRepository.fetchMyCode()
    }

    // MARK: - Private Helper
    private suspend fun <T> execute(
        operation: String,
        vararg metadata: Pair<String, String>,
        action: suspend () -> T,
    ): T {
        val context = CrashContext(
            feature = feature,
            action = operation,
            metadata = mapOf(*metadata)
        )
        return try {
            action()
        } catch (e: CancellationException) {
            throw e
        } catch (networkError: NetworkError) {
            crashlyticsService?.record(networkError as Throwable, context)
            throw networkError
        } catch (e: Exception) {
            val mappedError = FriendUseCaseError.Unknown(underlying = e)
            crashlyticsService?.record(mappedError as Throwable, context)
            throw mappedError
        }
    }
}

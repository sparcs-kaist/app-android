package org.sparcs.soap.App.Domain.Usecases.Feed

import okhttp3.MultipartBody
import org.sparcs.soap.App.Domain.Error.CrashContext
import org.sparcs.soap.App.Domain.Error.Feed.FeedCommentUseCaseError
import org.sparcs.soap.App.Domain.Error.Feed.FeedProfileUseCaseError
import org.sparcs.soap.App.Domain.Error.NetworkError
import org.sparcs.soap.App.Domain.Repositories.Feed.FeedProfileRepositoryProtocol
import org.sparcs.soap.App.Domain.Services.CrashlyticsServiceProtocol
import javax.inject.Inject
import javax.inject.Singleton

interface FeedProfileUseCaseProtocol {

    @Throws(Exception::class)
    suspend fun updateNickname(nickname: String)

    @Throws(Exception::class)
    suspend fun updateProfileImage(imagePart: MultipartBody.Part?)
}

@Singleton
class FeedProfileUseCase @Inject constructor(
    private val feedProfileRepository: FeedProfileRepositoryProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol?
) : FeedProfileUseCaseProtocol {

    private val feature = "FeedProfile"

    override suspend fun updateNickname(nickname: String) {
        val context = CrashContext(feature = feature, metadata = mapOf("nickname" to nickname))

        execute(context) {
            feedProfileRepository.updateNickname(nickname)
        }
    }

    // MARK: - Functions
    override suspend fun updateProfileImage(imagePart: MultipartBody.Part?) {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf("resetProfileImage" to (imagePart == null).toString())
        )

        execute(context) {
            if (imagePart != null) {
                feedProfileRepository.setProfileImage(imagePart)
            } else {
                feedProfileRepository.removeProfileImage()
            }
        }
    }

    // MARK: - Private
    private suspend inline fun <T> execute(
        context: CrashContext,
        crossinline operation: suspend () -> T
    ): T {
        return try {
            operation()
        } catch (e: Exception) {
            when (e) {
                is NetworkError.ServerError -> {
                    if (e.code == 400) {
                        throw FeedProfileUseCaseError.NicknameReserved
                    } else if (e.code == 409) {
                        throw FeedProfileUseCaseError.NicknameConflict
                    }
                    crashlyticsService?.record(error = e as Throwable, context = context)
                    throw e
                }

                is NetworkError -> {
                    crashlyticsService?.record(error = e as Throwable, context = context)
                    throw e
                }

                else -> {
                    val mappedError = FeedCommentUseCaseError.Unknown(e)
                    crashlyticsService?.record(error = mappedError as Throwable, context = context)
                    throw mappedError
                }
            }
        }
    }
}
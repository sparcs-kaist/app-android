package org.sparcs.soap.App.Domain.Usecases.Feed

import org.sparcs.soap.App.Domain.Enums.Feed.FeedPostPhotoItem
import org.sparcs.soap.App.Domain.Error.CrashContext
import org.sparcs.soap.App.Domain.Error.Feed.FeedImageUseCaseError
import org.sparcs.soap.App.Domain.Error.NetworkError
import org.sparcs.soap.App.Domain.Models.Feed.FeedImage
import org.sparcs.soap.App.Domain.Repositories.Feed.FeedImageRepository
import org.sparcs.soap.App.Domain.Services.CrashlyticsServiceProtocol
import javax.inject.Inject

interface FeedImageUseCaseProtocol {
    suspend fun uploadPostImage(item: FeedPostPhotoItem): FeedImage
}
class FeedImageUseCase @Inject constructor(
    private val feedImageRepository: FeedImageRepository,
    private val crashlyticsService: CrashlyticsServiceProtocol?
) : FeedImageUseCaseProtocol {
    // MARK: - Properties
    private val feature: String = "FeedImage"

    // MARK: - Functions
    override suspend fun uploadPostImage(item: FeedPostPhotoItem): FeedImage {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "hasSpoiler" to "${item.spoiler}",
                "hasDescription" to if (item.description.isEmpty()) "false" else "true"
            )
        )

        return execute(context) {
            feedImageRepository.uploadPostImage(item)
        }
    }

    // MARK: - Private
    private suspend fun <T> execute(
        context: CrashContext,
        operation: suspend () -> T
    ): T {
        return try {
            operation()
        } catch (e: Exception) {
            when (e) {
                is NetworkError -> {
                    crashlyticsService?.record(error = e as Throwable, context = context)
                    throw e
                }

                else -> {
                    val mappedError = FeedImageUseCaseError.Unknown(e)
                    crashlyticsService?.record(error = mappedError as Throwable, context = context)
                    throw mappedError
                }
            }
        }
    }
}
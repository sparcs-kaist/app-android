package org.sparcs.soap.app.domain.usecases.feed

import org.sparcs.soap.app.domain.enums.feed.FeedPostPhotoItem
import org.sparcs.soap.app.domain.error.CrashContext
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.error.feed.FeedImageUseCaseError
import org.sparcs.soap.app.domain.models.feed.FeedImage
import org.sparcs.soap.app.domain.repositories.feed.FeedImageRepository
import org.sparcs.soap.app.domain.services.CrashlyticsServiceProtocol
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
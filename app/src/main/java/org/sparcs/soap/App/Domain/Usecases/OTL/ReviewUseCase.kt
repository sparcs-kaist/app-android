package org.sparcs.soap.App.Domain.Usecases.OTL

import org.sparcs.soap.App.Domain.Error.CrashContext
import org.sparcs.soap.App.Domain.Error.NetworkError
import org.sparcs.soap.App.Domain.Error.OTL.ReviewUseCaseError
import org.sparcs.soap.App.Domain.Models.OTL.LectureHistory
import org.sparcs.soap.App.Domain.Models.OTL.LectureReview
import org.sparcs.soap.App.Domain.Models.OTL.LectureReviewPage
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLReviewRepositoryProtocol
import org.sparcs.soap.App.Domain.Services.CrashlyticsServiceProtocol
import javax.inject.Inject

interface ReviewUseCaseProtocol {
    suspend fun fetchReviews(
        courseID: Int,
        professorID: Int?,
        offset: Int,
        limit: Int,
    ): LectureReviewPage

    suspend fun writeReview(lectureID: Int, content: String, grade: Int, load: Int, speech: Int)
    suspend fun updateReview(reviewID: Int, content: String, grade: Int, load: Int, speech: Int)
    suspend fun likeReview(reviewID: Int, like: Boolean)
    suspend fun fetchLectureHistory(userID: Int): List<LectureHistory>
    suspend fun getWrittenReviews(): List<LectureReview>
}

class ReviewUseCase @Inject constructor(
    private val otlReviewRepository: OTLReviewRepositoryProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol? = null,
) : ReviewUseCaseProtocol {

    // MARK: - Properties
    private val feature: String = "Review"

    // MARK: - Functions
    override suspend fun fetchReviews(
        courseID: Int,
        professorID: Int?,
        offset: Int,
        limit: Int,
    ): LectureReviewPage {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "courseID" to courseID.toString(),
                "professorID" to (professorID?.toString() ?: "null"),
                "offset" to offset.toString(),
                "limit" to limit.toString()
            )
        )

        return execute(context) {
            otlReviewRepository.fetchReviews(
                courseID = courseID,
                professorID = professorID,
                offset = offset,
                limit = limit
            )
        }
    }

    override suspend fun writeReview(
        lectureID: Int,
        content: String,
        grade: Int,
        load: Int,
        speech: Int,
    ) {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "lectureID" to lectureID.toString(),
                "grade" to grade.toString(),
                "load" to load.toString(),
                "speech" to speech.toString()
            )
        )

        execute(context) {
            otlReviewRepository.writeReview(lectureID, content, grade, load, speech)
        }
    }

    override suspend fun updateReview(
        reviewID: Int,
        content: String,
        grade: Int,
        load: Int,
        speech: Int,
    ) {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "reviewID" to reviewID.toString(),
                "contentLength" to content.length.toString()
            )
        )
        execute(context) {
            otlReviewRepository.updateReview(reviewID, content, grade, load, speech)
        }
    }

    override suspend fun likeReview(reviewID: Int, like: Boolean) {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf(
                "reviewID" to reviewID.toString(),
                "likeState" to like.toString()
            )
        )
        execute(context) {
            otlReviewRepository.likeReview(reviewID, like)
        }
    }

    override suspend fun fetchLectureHistory(userID: Int): List<LectureHistory> {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf("userID" to userID.toString())
        )
        return execute(context) {
            otlReviewRepository.fetchLectureHistory(userID)
        }
    }

    override suspend fun getWrittenReviews(): List<LectureReview> {
        val context = CrashContext(
            feature = feature,
            metadata = emptyMap()
        )
        return execute(context) {
            otlReviewRepository.getWrittenReviews()
        }
    }

    // MARK: - Private Helper
    private suspend fun <T> execute(
        context: CrashContext,
        operation: suspend () -> T,
    ): T {
        return try {
            operation()
        } catch (networkError: NetworkError) {
            crashlyticsService?.record(networkError as Throwable, context)
            throw networkError
        } catch (e: Exception) {
            val mappedError = ReviewUseCaseError.Unknown(underlying = e)
            crashlyticsService?.record(mappedError as Throwable, context)
            throw mappedError
        }
    }
}
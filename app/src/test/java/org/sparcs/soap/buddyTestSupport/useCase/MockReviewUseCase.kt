package org.sparcs.soap.buddyTestSupport.useCase

import org.sparcs.soap.app.domain.models.otl.LectureHistory
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.domain.models.otl.LectureReviewPage
import org.sparcs.soap.app.domain.usecases.otl.ReviewUseCaseProtocol
import org.sparcs.soap.app.shared.mocks.otl.mock

class MockReviewUseCase : ReviewUseCaseProtocol {

    var fetchReviewsResult: Result<LectureReviewPage> = Result.success(LectureReviewPage.mock())
    var writtenReviewsResult: Result<List<LectureReview>> = Result.success(emptyList())
    var likeReviewResult: Result<Unit> = Result.success(Unit)
    var lectureHistoryResult: Result<List<LectureHistory>> = Result.success(emptyList())

    var likeReviewCallCount = 0
    var lastLikedReviewId: Int? = null
    var lastLikeValue: Boolean? = null

    override suspend fun fetchReviews(
        courseID: Int,
        professorID: Int?,
        offset: Int,
        limit: Int,
    ): LectureReviewPage = fetchReviewsResult.getOrThrow()

    override suspend fun writeReview(lectureID: Int, content: String, grade: Int, load: Int, speech: Int) {}

    override suspend fun updateReview(reviewID: Int, content: String, grade: Int, load: Int, speech: Int) {}

    override suspend fun likeReview(reviewID: Int, like: Boolean) {
        likeReviewCallCount += 1
        lastLikedReviewId = reviewID
        lastLikeValue = like
        likeReviewResult.getOrThrow()
    }

    override suspend fun fetchLectureHistory(userID: Int): List<LectureHistory> =
        lectureHistoryResult.getOrThrow()

    override suspend fun getWrittenReviews(): List<LectureReview> = writtenReviewsResult.getOrThrow()
}

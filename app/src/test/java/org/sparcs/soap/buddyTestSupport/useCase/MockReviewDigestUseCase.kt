package org.sparcs.soap.buddyTestSupport.useCase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigestProgress
import org.sparcs.soap.app.domain.usecases.reviewDigest.ReviewDigestUseCaseProtocol

class MockReviewDigestUseCase : ReviewDigestUseCaseProtocol {
    var available = true
    var updates: List<LectureReviewDigestProgress> = emptyList()
    var failure: Exception? = null
    var stream: (suspend () -> Unit)? = null
    var streamCallCount = 0

    override suspend fun isAvailable(): Boolean = available

    override fun streamDigest(reviews: List<LectureReview>): Flow<LectureReviewDigestProgress> = flow {
        streamCallCount += 1
        updates.forEach { emit(it) }
        stream?.invoke()
        failure?.let { throw it }
    }
}

package org.sparcs.soap.buddyTestSupport.useCase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.sparcs.soap.app.domain.usecases.summarization.ReviewSummaryUseCaseProtocol

class MockReviewSummaryUseCase : ReviewSummaryUseCaseProtocol {
    var available = false
    var chunks: List<String> = emptyList()
    var failure: Exception? = null
    var streamCallCount = 0

    override suspend fun isAvailable(): Boolean = available

    override fun streamSummary(review: String): Flow<String> = flow {
        streamCallCount += 1
        chunks.forEach { emit(it) }
        failure?.let { throw it }
    }
}

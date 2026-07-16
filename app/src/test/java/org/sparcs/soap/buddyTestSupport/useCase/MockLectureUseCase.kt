package org.sparcs.soap.buddyTestSupport.useCase

import org.sparcs.soap.app.domain.models.otl.CourseLecture
import org.sparcs.soap.app.domain.models.otl.LectureSearchRequest
import org.sparcs.soap.app.domain.usecases.otl.LectureUseCaseProtocol

class MockLectureUseCase : LectureUseCaseProtocol {

    var searchLectureResult: Result<List<CourseLecture>> = Result.success(emptyList())
    var searchLectureCallCount = 0
    var lastRequest: LectureSearchRequest? = null

    override suspend fun searchLecture(request: LectureSearchRequest): List<CourseLecture> {
        searchLectureCallCount += 1
        lastRequest = request
        return searchLectureResult.getOrThrow()
    }
}

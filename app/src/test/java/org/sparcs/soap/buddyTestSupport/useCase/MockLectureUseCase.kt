package org.sparcs.soap.buddyTestSupport.useCase

import org.sparcs.soap.app.domain.models.otl.DepartmentOption
import org.sparcs.soap.app.domain.models.otl.CourseLecture
import org.sparcs.soap.app.domain.models.otl.LectureSearchRequest
import org.sparcs.soap.app.domain.usecases.otl.LectureUseCaseProtocol
import org.sparcs.soap.app.domain.models.otl.OTLUserLectureHistory

class MockLectureUseCase : LectureUseCaseProtocol {
    override suspend fun fetchDepartmentOptions() = emptyList<DepartmentOption>()
    var historyResult = Result.success(OTLUserLectureHistory(emptyList(), 0, 0, 0))
    override suspend fun fetchUserLectureHistory(userID: Int): OTLUserLectureHistory = historyResult.getOrThrow()

    var searchLectureResult: Result<List<CourseLecture>> = Result.success(emptyList())
    var searchLectureCallCount = 0
    var lastRequest: LectureSearchRequest? = null

    override suspend fun searchLecture(request: LectureSearchRequest): List<CourseLecture> {
        searchLectureCallCount += 1
        lastRequest = request
        return searchLectureResult.getOrThrow()
    }
}

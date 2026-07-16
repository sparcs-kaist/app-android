package org.sparcs.soap.buddyTestSupport.useCase

import org.sparcs.soap.app.domain.models.otl.Course
import org.sparcs.soap.app.domain.models.otl.CourseSearchRequest
import org.sparcs.soap.app.domain.models.otl.CourseSummary
import org.sparcs.soap.app.domain.usecases.otl.CourseUseCaseProtocol
import org.sparcs.soap.app.shared.mocks.otl.mock

class MockCourseUseCase : CourseUseCaseProtocol {

    var getCourseResult: Result<Course> = Result.success(Course.mock())
    var searchCourseResult: Result<List<CourseSummary>> = Result.success(emptyList())

    var getCourseCallCount = 0

    override suspend fun searchCourse(request: CourseSearchRequest): List<CourseSummary> =
        searchCourseResult.getOrThrow()

    override suspend fun getCourse(courseID: Int): Course {
        getCourseCallCount += 1
        return getCourseResult.getOrThrow()
    }
}

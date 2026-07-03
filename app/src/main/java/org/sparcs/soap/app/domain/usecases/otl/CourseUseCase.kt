package org.sparcs.soap.app.domain.usecases.otl

import org.sparcs.soap.app.domain.error.CrashContext
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.error.otl.CourseUseCaseError
import org.sparcs.soap.app.domain.models.otl.Course
import org.sparcs.soap.app.domain.models.otl.CourseSearchRequest
import org.sparcs.soap.app.domain.models.otl.CourseSummary
import org.sparcs.soap.app.domain.repositories.otl.OTLCourseRepositoryProtocol
import org.sparcs.soap.app.domain.services.CrashlyticsServiceProtocol
import javax.inject.Inject

interface CourseUseCaseProtocol {
    suspend fun searchCourse(request: CourseSearchRequest): List<CourseSummary>
    suspend fun getCourse(courseID: Int): Course
}

class CourseUseCase @Inject constructor(
    private val otlCourseRepository: OTLCourseRepositoryProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol?
) : CourseUseCaseProtocol {

    // MARK: - Properties
    private val feature: String = "Course"

    // MARK: - Functions
    override suspend fun searchCourse(request: CourseSearchRequest): List<CourseSummary> {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf("keyword" to request.toString())
        )
        return execute(context) {
            otlCourseRepository.searchCourse(request)
        }
    }

    override suspend fun getCourse(courseID: Int): Course {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf("courseID" to courseID.toString())
        )
        return execute(context) {
            otlCourseRepository.getCourse(courseID)
        }
    }

    // MARK: - Private Helper
    private suspend fun <T> execute(
        context: CrashContext,
        operation: suspend () -> T
    ): T {
        return try {
            operation()
        } catch (networkError: NetworkError) {
            crashlyticsService?.record(networkError as Throwable, context)
            throw networkError
        } catch (e: Exception) {
            val mappedError = CourseUseCaseError.Unknown(underlying = e)
            crashlyticsService?.record(mappedError as Throwable, context)
            throw mappedError
        }
    }
}
package org.sparcs.soap.app.domain.usecases.otl

import org.sparcs.soap.app.domain.error.CrashContext
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.error.otl.LectureUseCaseError
import org.sparcs.soap.app.domain.models.otl.CourseLecture
import org.sparcs.soap.app.domain.models.otl.LectureSearchRequest
import org.sparcs.soap.app.domain.repositories.otl.OTLLectureRepositoryProtocol
import org.sparcs.soap.app.domain.services.CrashlyticsServiceProtocol
import javax.inject.Inject

interface LectureUseCaseProtocol {
    suspend fun searchLecture(request: LectureSearchRequest): List<CourseLecture>
}

class LectureUseCase @Inject constructor(
    private val otlLectureRepository: OTLLectureRepositoryProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol?
) : LectureUseCaseProtocol {

    // MARK: - Properties
    private val feature: String = "Lecture"

    // MARK: - Functions
    override suspend fun searchLecture(request: LectureSearchRequest): List<CourseLecture> {
        val context = CrashContext(
            feature = feature,
            metadata = mapOf("keyword" to request.toString())
        )
        return execute(context) {
            otlLectureRepository.searchLectures(request)
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
            val mappedError = LectureUseCaseError.Unknown(underlying = e)
            crashlyticsService?.record(mappedError as Throwable, context)
            throw mappedError
        }
    }
}
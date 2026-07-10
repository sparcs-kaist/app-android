package org.sparcs.soap.app.domain.repositories.otl

import com.google.gson.Gson
import org.sparcs.soap.app.domain.models.otl.Course
import org.sparcs.soap.app.domain.models.otl.CourseSearchRequest
import org.sparcs.soap.app.domain.models.otl.CourseSummary
import org.sparcs.soap.app.networking.requestDTO.otl.CourseSearchRequestDTO
import org.sparcs.soap.app.networking.responseDTO.safeApiCall
import org.sparcs.soap.app.networking.retrofitAPI.otl.OTLCourseApi
import javax.inject.Inject

interface OTLCourseRepositoryProtocol {
    suspend fun searchCourse(request: CourseSearchRequest): List<CourseSummary>
    suspend fun getCourse(courseId: Int): Course
}


class OTLCourseRepository @Inject constructor(
    private val api: OTLCourseApi,
    private val gson: Gson = Gson(),
) : OTLCourseRepositoryProtocol {

    override suspend fun searchCourse(request: CourseSearchRequest): List<CourseSummary> = safeApiCall(gson) {
        val dto = CourseSearchRequestDTO.fromModel(request)
        val response = api.searchCourse(
            name = dto.keyword,
            offset = dto.offset,
            limit = dto.limit,
            type = dto.type,
            department = dto.department,
            level = dto.level,
            term = dto.term
        )

        response.courses.map { it.toModel() }
    }

    override suspend fun getCourse(courseId: Int): Course = safeApiCall(gson) {
        api.fetchCourse(courseId).toModel()
    }
}
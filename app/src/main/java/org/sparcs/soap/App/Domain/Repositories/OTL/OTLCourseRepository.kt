package org.sparcs.soap.App.Domain.Repositories.OTL

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Models.OTL.Course
import org.sparcs.soap.App.Domain.Models.OTL.CourseSearchRequest
import org.sparcs.soap.App.Domain.Models.OTL.CourseSummary
import org.sparcs.soap.App.Networking.ResponseDTO.safeApiCall
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.OTLCourseApi
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
        val response = api.searchCourse(
            name = request.keyword,
            offset = request.offset,
            limit = request.limit
        )

        response.courses.map { it.toModel() }
    }

    override suspend fun getCourse(courseId: Int): Course = safeApiCall(gson) {
        api.fetchCourse(courseId).toModel()
    }
}
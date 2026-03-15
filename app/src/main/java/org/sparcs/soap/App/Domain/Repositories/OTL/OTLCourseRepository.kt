package org.sparcs.soap.App.Domain.Repositories.OTL

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Models.OTL.Course
import org.sparcs.soap.App.Domain.Models.OTL.SearchCourse
import org.sparcs.soap.App.Networking.ResponseDTO.safeApiCall
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.OTLCourseApi
import org.sparcs.soap.App.Shared.Mocks.mock
import javax.inject.Inject

interface OTLCourseRepositoryProtocol {
    suspend fun searchCourse(name: String, offset: Int, limit: Int): List<SearchCourse>

    suspend fun getCourseDetail(courseId: Int): Course
}


class OTLCourseRepository @Inject constructor(
    private val api: OTLCourseApi,
    private val gson: Gson = Gson(),
) : OTLCourseRepositoryProtocol {

    override suspend fun searchCourse(name: String, offset: Int, limit: Int): List<SearchCourse> = safeApiCall(gson) {
        api.searchCourse(name, offset, limit)
    }.courses.map { it.toModel() }

    override suspend fun getCourseDetail(courseId: Int): Course = safeApiCall(gson) {
        api.fetchCourseDetail(courseId).toModel()
    }
}

class FakeOTLCourseRepository : OTLCourseRepositoryProtocol {
    override suspend fun searchCourse(name: String, offset: Int, limit: Int): List<SearchCourse> {
        return emptyList()
    }

    override suspend fun getCourseDetail(courseId: Int): Course {
        return Course.mock()
    }
}
package org.sparcs.soap.App.Domain.Repositories.OTL

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Enums.OTL.LectureType
import org.sparcs.soap.App.Domain.Models.OTL.Course
import org.sparcs.soap.App.Domain.Models.OTL.Department
import org.sparcs.soap.App.Domain.Models.OTL.SearchCourse
import org.sparcs.soap.App.Networking.ResponseDTO.safeApiCall
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.OTLCourseApi
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
        return Course(
            id = courseId,
            name = "Fake Course",
            code = "FAKE101",
            type = LectureType.ETC,
            department = Department(
                id = 0,
                name = "Fake Department",
            ),
            summary = "This is a fake course for testing purposes.",
            classDuration = 2,
            expDuration = 1,
            credit = 3,
            creditAu = 0
        )
    }
}
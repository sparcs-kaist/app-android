package org.sparcs.soap.app.domain.repositories.otl

import com.google.gson.Gson
import org.sparcs.soap.app.domain.models.otl.CourseLecture
import org.sparcs.soap.app.domain.models.otl.OTLUser
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.networking.requestDTO.otl.InterestedDepartmentsRequestDTO
import org.sparcs.soap.app.networking.responseDTO.safeApiCall
import org.sparcs.soap.app.networking.retrofitAPI.otl.OTLUserApi
import javax.inject.Inject

interface OTLUserRepositoryProtocol {
    suspend fun fetchWishlist(userID: Int, semester: Semester): List<CourseLecture>
    suspend fun updateWishlist(userID: Int, lectureID: Int, isWishlisted: Boolean)
    suspend fun updateInterestedDepartments(userID: Int, departmentIDs: List<Int>)
    suspend fun register(ssoInfo: String)
    suspend fun fetchUser(): OTLUser
}

class OTLUserRepository @Inject constructor(
    private val api: OTLUserApi,
    private val gson: Gson = Gson(),
) : OTLUserRepositoryProtocol {

    override suspend fun fetchWishlist(userID: Int, semester: Semester): List<CourseLecture> = safeApiCall(gson) {
        api.fetchWishlist(userID, semester.year, semester.semesterType.intValue).courses.map { dto ->
            val course = dto.toModel()
            course.copy(lectures = course.lectures.map { it.copy(grade = 0.0, load = 0.0, speech = 0.0) })
        }
    }

    override suspend fun updateWishlist(userID: Int, lectureID: Int, isWishlisted: Boolean) = safeApiCall(gson) {
        api.updateWishlist(userID, mapOf("lectureId" to lectureID, "mode" to if (isWishlisted) "add" else "delete"))
    }

    override suspend fun updateInterestedDepartments(userID: Int, departmentIDs: List<Int>) = safeApiCall(gson) {
        api.updateInterestedDepartments(userID, InterestedDepartmentsRequestDTO(departmentIDs))
    }

    override suspend fun register(ssoInfo: String) = safeApiCall(gson) {
        api.register(mapOf("sso_info" to ssoInfo))
    }

    override suspend fun fetchUser(): OTLUser = safeApiCall(gson) {
        val response = api.fetchUserInfo()
        response.body() ?: throw Exception("Empty response")
    }.toModel()
}

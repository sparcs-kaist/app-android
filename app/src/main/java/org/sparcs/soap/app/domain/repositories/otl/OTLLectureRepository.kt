package org.sparcs.soap.app.domain.repositories.otl

import com.google.gson.Gson
import org.sparcs.soap.app.domain.models.otl.CourseLecture
import org.sparcs.soap.app.domain.models.otl.LectureSearchRequest
import org.sparcs.soap.app.networking.requestDTO.otl.LectureSearchRequestDTO
import org.sparcs.soap.app.networking.responseDTO.safeApiCall
import org.sparcs.soap.app.networking.retrofitAPI.otl.OTLLectureApi
import javax.inject.Inject

interface OTLLectureRepositoryProtocol {
    suspend fun searchLectures(request: LectureSearchRequest): List<CourseLecture>
}

class OTLLectureRepository @Inject constructor(
    private val api: OTLLectureApi,
    private val gson: Gson = Gson(),
) : OTLLectureRepositoryProtocol {

    override suspend fun searchLectures(request: LectureSearchRequest): List<CourseLecture> = safeApiCall(gson) {
        val dto = LectureSearchRequestDTO.fromModel(request)
        api.searchLecture(
            year = dto.year,
            semester = dto.semester,
            keyword = dto.keyword,
            type = dto.type,
            department = dto.department,
            level = dto.level,
            term = dto.term,
            limit = dto.limit,
            offset = dto.offset
        ).courses.map { it.toModel() }
    }
}
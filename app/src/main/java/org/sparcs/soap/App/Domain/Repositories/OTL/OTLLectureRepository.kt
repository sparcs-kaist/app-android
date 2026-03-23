package org.sparcs.soap.App.Domain.Repositories.OTL

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Models.OTL.CourseLecture
import org.sparcs.soap.App.Domain.Models.OTL.LectureSearchRequest
import org.sparcs.soap.App.Networking.RequestDTO.OTL.LectureSearchRequestDTO
import org.sparcs.soap.App.Networking.ResponseDTO.safeApiCall
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.OTLLectureApi
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
            limit = dto.limit,
            offset = dto.offset
        ).courses.map { it.toModel() }
    }
}
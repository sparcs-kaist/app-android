package com.example.soap.Domain.Repositories.OTL

import com.example.soap.Domain.Models.OTL.Lecture
import com.example.soap.Domain.Models.OTL.LectureSearchRequest
import com.example.soap.Networking.RequestDTO.OTL.LectureSearchRequestDTO
import com.example.soap.Networking.RetrofitAPI.OTL.OTLLectureApi
import javax.inject.Inject

interface OTLLectureRepositoryProtocol {
    suspend fun searchLectures(request: LectureSearchRequest): List<Lecture>
}

class OTLLectureRepository @Inject constructor(
    private val api: OTLLectureApi
) : OTLLectureRepositoryProtocol {

    override suspend fun searchLectures(request: LectureSearchRequest): List<Lecture> {
        val dto = LectureSearchRequestDTO.fromModel(request)
        val response = api.searchLecture(
            year = dto.year,
            semester = dto.semester,
            keyword = dto.keyword,
            type = dto.type,
            department = dto.department,
            level = dto.level,
            limit = dto.limit,
            offset = dto.offset
        )

        return response.map { it.toModel() }
    }
}
package com.example.soap.Domain.Repositories.OTL

import com.example.soap.Domain.Models.OTL.Lecture
import com.example.soap.Domain.Models.OTL.LectureReview
import com.example.soap.Domain.Models.OTL.LectureSearchRequest
import com.example.soap.Networking.RequestDTO.OTL.LectureSearchRequestDTO
import com.example.soap.Networking.RequestDTO.OTL.WriteReviewRequest
import com.example.soap.Networking.RetrofitAPI.OTL.OTLLectureApi
import javax.inject.Inject

interface OTLLectureRepositoryProtocol {
    suspend fun searchLectures(request: LectureSearchRequest): List<Lecture>
    suspend fun fetchLectures(lectureID: Int): List<LectureReview>
    suspend fun writeReview(
        lectureID: Int,
        content: String,
        grade: Int,
        load: Int,
        speech: Int
    ): LectureReview
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

    override suspend fun fetchLectures(lectureID: Int): List<LectureReview> {
        return api.fetchReviews(lectureID)
            .map { it.toModel() }
    }

    override suspend fun writeReview(
        lectureID: Int,
        content: String,
        grade: Int,
        load: Int,
        speech: Int
    ): LectureReview {
        val request = WriteReviewRequest(
            lecture = lectureID,
            content = content,
            grade = grade,
            load = load,
            speech = speech
        )

        val response = api.writeReview(request)
        return response.toModel()
    }
}

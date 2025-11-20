package com.sparcs.soap.Domain.Repositories.OTL

import com.sparcs.soap.Domain.Models.OTL.Lecture
import com.sparcs.soap.Domain.Models.OTL.LectureReview
import com.sparcs.soap.Domain.Models.OTL.LectureSearchRequest
import com.sparcs.soap.Networking.RequestDTO.OTL.LectureSearchRequestDTO
import com.sparcs.soap.Networking.RequestDTO.OTL.WriteReviewRequest
import com.sparcs.soap.Networking.RetrofitAPI.OTL.OTLLectureApi
import com.sparcs.soap.Shared.Mocks.mock
import com.sparcs.soap.Shared.Mocks.mockList
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

class FakeOTLLectureRepository : OTLLectureRepositoryProtocol {
    override suspend fun searchLectures(request: LectureSearchRequest): List<Lecture> { return Lecture.mockList() }
    override suspend fun fetchLectures(lectureID: Int): List<LectureReview> { return LectureReview.mockList() }
    override suspend fun writeReview(
        lectureID: Int,
        content: String,
        grade: Int,
        load: Int,
        speech: Int
    ): LectureReview { return LectureReview.mock() }
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
            content = content,
            grade = grade,
            load = load,
            speech = speech
        )

        val response = api.writeReview(lectureID = lectureID, request)
        return response.toModel()
    }
}

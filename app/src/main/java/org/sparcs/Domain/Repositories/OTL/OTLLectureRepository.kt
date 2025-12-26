package org.sparcs.Domain.Repositories.OTL

import com.google.gson.Gson
import org.sparcs.Domain.Models.OTL.Lecture
import org.sparcs.Domain.Models.OTL.LectureReview
import org.sparcs.Domain.Models.OTL.LectureSearchRequest
import org.sparcs.Networking.RequestDTO.OTL.LectureSearchRequestDTO
import org.sparcs.Networking.RequestDTO.OTL.WriteReviewRequest
import org.sparcs.Networking.ResponseDTO.handleApiError
import org.sparcs.Networking.RetrofitAPI.OTL.OTLLectureApi
import org.sparcs.Shared.Mocks.mock
import org.sparcs.Shared.Mocks.mockList
import javax.inject.Inject

interface OTLLectureRepositoryProtocol {
    suspend fun searchLectures(request: LectureSearchRequest): List<Lecture>
    suspend fun fetchLectures(lectureID: Int): List<LectureReview>
    suspend fun writeReview(
        lectureID: Int,
        content: String,
        grade: Int,
        load: Int,
        speech: Int,
    ): LectureReview
}


class OTLLectureRepository @Inject constructor(
    private val api: OTLLectureApi,
    private val gson: Gson = Gson(),
) : OTLLectureRepositoryProtocol {

    override suspend fun searchLectures(request: LectureSearchRequest): List<Lecture> {
        try {
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
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun fetchLectures(lectureID: Int): List<LectureReview> = try {
        api.fetchReviews(lectureID).map { it.toModel() }
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun writeReview(
        lectureID: Int,
        content: String,
        grade: Int,
        load: Int,
        speech: Int,
    ): LectureReview {
        try {
            val request = WriteReviewRequest(
                lectureID = lectureID,
                content = content,
                grade = grade,
                load = load,
                speech = speech
            )

            val response = api.writeReview(request)
            return response.toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }
}

class FakeOTLLectureRepository : OTLLectureRepositoryProtocol {
    override suspend fun searchLectures(request: LectureSearchRequest): List<Lecture> {
        return Lecture.mockList()
    }

    override suspend fun fetchLectures(lectureID: Int): List<LectureReview> {
        return LectureReview.mockList()
    }

    override suspend fun writeReview(
        lectureID: Int,
        content: String,
        grade: Int,
        load: Int,
        speech: Int,
    ): LectureReview {
        return LectureReview.mock()
    }
}
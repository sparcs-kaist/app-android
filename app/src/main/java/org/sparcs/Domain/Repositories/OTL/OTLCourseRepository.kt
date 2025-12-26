package org.sparcs.Domain.Repositories.OTL

import com.google.gson.Gson
import org.sparcs.Domain.Models.OTL.Course
import org.sparcs.Domain.Models.OTL.LectureReview
import org.sparcs.Networking.ResponseDTO.handleApiError
import org.sparcs.Networking.RetrofitAPI.OTL.OTLCourseApi
import javax.inject.Inject

interface OTLCourseRepositoryProtocol {
    suspend fun searchCourse(name: String, offset: Int, limit: Int): List<Course>
    suspend fun fetchReviews(courseId: Int, offset: Int, limit: Int): List<LectureReview>
    suspend fun likeReview(reviewId: Int)
    suspend fun unlikeReview(reviewId: Int)
}


class OTLCourseRepository @Inject constructor(
    private val api: OTLCourseApi,
    private val gson: Gson = Gson(),
) : OTLCourseRepositoryProtocol {

    override suspend fun searchCourse(name: String, offset: Int, limit: Int): List<Course> {
        try {
            val response = api.searchCourse(name, offset, limit)
            return response.map { it.toModel() }
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun fetchReviews(courseId: Int, offset: Int, limit: Int): List<LectureReview> {
        try {
            val response = api.fetchReviews(courseId, offset, limit)
            return response.map { it.toModel() }

        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun likeReview(reviewId: Int) = try {
        api.likeReview(reviewId)
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun unlikeReview(reviewId: Int) = try {
        api.unlikeReview(reviewId)
    } catch (e: Exception) {
        handleApiError(gson, e)
    }
}

class FakeOTLCourseRepository : OTLCourseRepositoryProtocol {
    override suspend fun searchCourse(name: String, offset: Int, limit: Int): List<Course> {
        return emptyList()
    }

    override suspend fun fetchReviews(courseId: Int, offset: Int, limit: Int): List<LectureReview> {
        return emptyList()
    }

    override suspend fun likeReview(reviewId: Int) {}
    override suspend fun unlikeReview(reviewId: Int) {}
}
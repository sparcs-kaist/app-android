package org.sparcs.soap.App.Domain.Repositories.OTL

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Models.OTL.Course
import org.sparcs.soap.App.Domain.Models.OTL.LectureReview
import org.sparcs.soap.App.Networking.ResponseDTO.safeApiCall
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.OTLCourseApi
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

    override suspend fun searchCourse(name: String, offset: Int, limit: Int): List<Course> = safeApiCall(gson) {
        api.searchCourse(name, offset, limit)
    }.map { it.toModel() }

    override suspend fun fetchReviews(courseId: Int, offset: Int, limit: Int): List<LectureReview> = safeApiCall(gson) {
        api.fetchReviews(courseId, offset, limit)
    }.map { it.toModel() }

    override suspend fun likeReview(reviewId: Int) = safeApiCall(gson) {
        api.likeReview(reviewId)
    }

    override suspend fun unlikeReview(reviewId: Int) = safeApiCall(gson) {
        api.unlikeReview(reviewId)
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
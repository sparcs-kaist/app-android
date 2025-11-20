package com.sparcs.soap.Domain.Repositories.OTL

import com.sparcs.soap.Domain.Models.OTL.Course
import com.sparcs.soap.Domain.Models.OTL.LectureReview
import com.sparcs.soap.Networking.RetrofitAPI.OTL.OTLCourseApi
import javax.inject.Inject

interface OTLCourseRepositoryProtocol {
    suspend fun searchCourse(name: String, offset: Int, limit: Int): List<Course>
    suspend fun fetchReviews(courseId: Int, offset: Int, limit: Int): List<LectureReview>
    suspend fun likeReview(reviewId: Int)
    suspend fun unlikeReview(reviewId: Int)
}

class FakeOTLCourseRepository: OTLCourseRepositoryProtocol {
    override suspend fun searchCourse(name: String, offset: Int, limit: Int): List<Course> { return emptyList() }
    override suspend fun fetchReviews(courseId: Int, offset: Int, limit: Int): List<LectureReview> { return emptyList() }
    override suspend fun likeReview(reviewId: Int) {}
    override suspend fun unlikeReview(reviewId: Int) {}
}

class OTLCourseRepository @Inject constructor(
    private val api: OTLCourseApi
) : OTLCourseRepositoryProtocol {

    override suspend fun searchCourse(name: String, offset: Int, limit: Int): List<Course> {
        val response = api.searchCourse(name, offset, limit)
        return response.map { it.toModel() }
    }

    override suspend fun fetchReviews(courseId: Int, offset: Int, limit: Int): List<LectureReview> {
        val response = api.fetchReviews(courseId, offset, limit)
        return response.map { it.toModel() }
    }

    override suspend fun likeReview(reviewId: Int) {
        val response = api.likeReview(reviewId)
        if (!response.isSuccessful) {
            throw Exception("Failed to like review: ${response.code()}")
        }
    }

    override suspend fun unlikeReview(reviewId: Int) {
        val response = api.unlikeReview(reviewId)
        if (!response.isSuccessful) {
            throw Exception("Failed to unlike review: ${response.code()}")
        }
    }
}
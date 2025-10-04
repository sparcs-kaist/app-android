package com.example.soap.Domain.Repositories.OTL

import com.example.soap.Domain.Models.OTL.Course
import com.example.soap.Domain.Models.OTL.CourseReview
import com.example.soap.Networking.RetrofitAPI.OTL.OTLCourseApi
import javax.inject.Inject

interface OTLCourseRepositoryProtocol {
    suspend fun searchCourse(name: String, offset: Int, limit: Int): List<Course>
    suspend fun getCourseReview(courseId: Int, offset: Int, limit: Int): List<CourseReview>
    suspend fun likeReview(reviewId: Int)
    suspend fun unlikeReview(reviewId: Int)
}

class OTLCourseRepository @Inject constructor(
    private val api: OTLCourseApi
) : OTLCourseRepositoryProtocol {

    override suspend fun searchCourse(name: String, offset: Int, limit: Int): List<Course> {
        val response = api.searchCourse(name, offset, limit)
        return response.map { it.toModel() }
    }

    override suspend fun getCourseReview(courseId: Int, offset: Int, limit: Int): List<CourseReview> {
        val response = api.getCourseReview(courseId, offset, limit)
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
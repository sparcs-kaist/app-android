package com.example.soap.Networking.RetrofitAPI.Feed

import com.example.soap.Networking.RequestDTO.Feed.FeedPostRequestDTO
import com.example.soap.Networking.ResponseDTO.Feed.FeedPostDTO
import com.example.soap.Networking.ResponseDTO.Feed.FeedPostPageDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface FeedPostApi {

    @GET("/posts")
    suspend fun fetchPosts(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int
    ): FeedPostPageDTO

    @POST("/posts")
    suspend fun writePost(
        @Body request: FeedPostRequestDTO
    ): FeedPostDTO

    @DELETE("/posts/{postId}")
    suspend fun deletePost(
        @Path("postId") postId: String
    ): Response<Unit>

    @POST("/posts/{postId}/vote")
    suspend fun vote(
        @Path("postId") postId: String,
        @Body body:  Map<String, String>
    ): Response<Unit>

    @DELETE("/posts/{postId}/vote")
    suspend fun deleteVote(
        @Path("postId") postId: String
    ): Response<Unit>
}
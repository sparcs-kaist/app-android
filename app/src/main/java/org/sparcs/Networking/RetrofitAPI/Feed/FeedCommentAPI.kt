package org.sparcs.Networking.RetrofitAPI.Feed

import org.sparcs.Networking.RequestDTO.Feed.FeedCommentRequestDTO
import org.sparcs.Networking.ResponseDTO.Feed.FeedCommentDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FeedCommentApi {

    @GET("posts/{postID}/comments")
    suspend fun fetchComments(
        @Path("postID") postID: String
    ): List<FeedCommentDTO>

    @POST("posts/{postID}/comments")
    suspend fun writeComment(
        @Path("postID") postID: String,
        @Body request: FeedCommentRequestDTO
    ): FeedCommentDTO

    @POST("comments/{commentID}/replies")
    suspend fun writeReply(
        @Path("commentID") commentID: String,
        @Body request: FeedCommentRequestDTO
    ): FeedCommentDTO

    @DELETE("comments/{commentID}")
    suspend fun deleteComment(
        @Path("commentID") commentID: String
    )

    @POST("comments/{commentID}/vote")
    suspend fun vote(
        @Path("commentID") commentID: String,
        @Body vote: Map<String, String>
    )

    @DELETE("comments/{commentID}/vote")
    suspend fun deleteVote(
        @Path("commentID") commentID: String
    )

    @POST("comments/{commentID}/report")
    suspend fun reportComment(
        @Path("commentID") commentID: String,
        @Body report: Map<String, String> //reason, detail
    )
}
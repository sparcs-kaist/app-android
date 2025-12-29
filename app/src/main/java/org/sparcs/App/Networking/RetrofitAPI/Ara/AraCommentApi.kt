package org.sparcs.App.Networking.RetrofitAPI.Ara

import org.sparcs.App.Networking.ResponseDTO.Ara.AraPostCommentDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AraCommentApi {

    @POST("comments/{id}/vote_positive/")
    suspend fun upVoteComment(@Path("id") commentID: Int)

    @POST("comments/{id}/vote_negative/")
    suspend fun downVoteComment(@Path("id") commentID: Int)

    @POST("comments/{id}/vote_cancel/")
    suspend fun cancelVote(@Path("id") commentID: Int)

    @POST("comments/")
    suspend fun writeComment(@Body body: CommentPostRequest): AraPostCommentDTO

    @POST("comments/")
    suspend fun writeThreadedComment(@Body body: ThreadedCommentPostRequest): AraPostCommentDTO

    @DELETE("comments/{id}/")
    suspend fun deleteComment(@Path("id") commentID: Int)

    @PATCH("comments/{id}/")
    suspend fun editComment(@Path("id") commentID: Int, @Body body: CommentPatchRequest): AraPostCommentDTO

    @POST("reports/")
    suspend fun reportComment(@Body body: CommentReportRequest)
}

data class CommentPostRequest(
    val parent_article: Int,
    val content: String,
    val name_type: Int = 2
)

data class ThreadedCommentPostRequest(
    val parent_comment: Int,
    val content: String,
    val name_type: Int = 2
)

data class CommentPatchRequest(
    val content: String,
    val name_type: Int = 2,
    val is_mine: Boolean = true
)

data class CommentReportRequest(
    val parent_comment: Int,
    val type: String,
    val content: String
)

package com.example.soap.Networking.RetrofitAPI.Ara

import com.example.soap.Domain.Enums.AraContentReportType
import com.example.soap.Networking.RequestDTO.AraPostRequestDTO
import com.example.soap.Networking.ResponseDTO.Ara.AraAttachmentDTO
import com.example.soap.Networking.ResponseDTO.Ara.AraBoardDTO
import com.example.soap.Networking.ResponseDTO.Ara.AraPostDTO
import com.example.soap.Networking.ResponseDTO.Ara.AraPostPageDTO
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

sealed class AraBoardTarget{

    data object FetchBoards : AraBoardTarget()

    data class FetchPosts(
        val type: PostListType,
        val page: Int,
        val pageSize: Int,
        val searchKeyword: String? = null
    ) : AraBoardTarget()

    data class FetchPost(
        val origin: PostOrigin?,
        val postID: Int
    ) : AraBoardTarget()

    data class UploadImage(val imageData: ByteArray) : AraBoardTarget()

    data class WritePost(val request: AraPostRequestDTO) : AraBoardTarget()

    data class Upvote(val postID: Int) : AraBoardTarget()
    data class DownVote(val postID: Int) : AraBoardTarget()
    data class CancelVote(val postID: Int) : AraBoardTarget()
    data class Report(val postID: Int, val type: AraContentReportType) : AraBoardTarget()
    data class Delete(val postID: Int) : AraBoardTarget()

    sealed class PostListType {
        data class Board(val boardID: Int) : PostListType()
        data class User(val userID: Int) : PostListType()
    }

    sealed class PostOrigin {
        data object All : PostOrigin()
        data object Board : PostOrigin()
        data class Topic(val topicID: String) : PostOrigin()
        data object None : PostOrigin()
    }
}

interface AraBoardApi {

    @GET("boards/")
    suspend fun fetchBoards(): List<AraBoardDTO>

    @GET("articles/")
    suspend fun fetchPosts(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        @Query("parent_board") parentBoard: Int? = null,
        @Query("created_by") createdBy: Int? = null,
        @Query("parent_topic") searchKeyword: String? = null
    ): AraPostPageDTO

    @GET("articles/{id}/")
    suspend fun fetchPost(
        @Path("id") postID: Int,
        @Query("from_view") fromView: String? = null,
        @Query("topic_id") topicId: String? = null,
        @Query("current") current: Int? = null
    ): AraPostDTO

    @Multipart
    @POST("attachments/")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): AraAttachmentDTO

    @POST("articles/")
    suspend fun writePost(
        @Body request: AraPostRequestDTO
    )

    @POST("articles/{id}/vote_positive/")
    suspend fun upVote(@Path("id") postID: Int)

    @POST("articles/{id}/vote_negative/")
    suspend fun downVote(@Path("id") postID: Int)

    @POST("articles/{id}/vote_cancel/")
    suspend fun cancelVote(@Path("id") postID: Int)

    @POST("reports/")
    suspend fun report(
        @Body request: Map<String, Any>
    )

    @DELETE("articles/{id}/")
    suspend fun delete(@Path("id") postID: Int)
}


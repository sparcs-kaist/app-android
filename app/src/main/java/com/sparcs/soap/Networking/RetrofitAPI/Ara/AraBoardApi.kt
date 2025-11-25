package com.sparcs.soap.Networking.RetrofitAPI.Ara

import com.sparcs.soap.Domain.Repositories.Ara.PostReportRequest
import com.sparcs.soap.Networking.RequestDTO.Ara.AraPostRequestDTO
import com.sparcs.soap.Networking.ResponseDTO.Ara.AraAttachmentDTO
import com.sparcs.soap.Networking.ResponseDTO.Ara.AraBoardDTO
import com.sparcs.soap.Networking.ResponseDTO.Ara.AraBookmarkDTO
import com.sparcs.soap.Networking.ResponseDTO.Ara.AraPostDTO
import com.sparcs.soap.Networking.ResponseDTO.Ara.AraPostPageDTO
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AraBoardApi {

    @GET("boards/")
    suspend fun fetchBoards(): List<AraBoardDTO>

    @GET("articles/")
    suspend fun fetchPosts(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        @Query("parent_board") parentBoard: Int? = null,
        @Query("created_by") createdBy: Int? = null,
        @Query("main_search__contains") searchKeyword: String? = null
    ): AraPostPageDTO

    @GET("articles/{id}/")
    suspend fun fetchPost(
        @Path("id") postID: Int,
        @Query("from_view") fromView: String? = null,
        @Query("topic_id") topicId: String? = null,
        @Query("current") current: Int? = null
    ): AraPostDTO

    @GET("scraps/")
    suspend fun fetchBookmarks(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int
    ): AraBookmarkDTO

    @POST("scraps/")
    suspend fun addBookmark(@Body body: Map<String, Int>)

    @DELETE("scraps/{scrapId}/")
    suspend fun removeBookmark(@Path("scrapId") scrapId: Int)

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
    suspend fun report(@Body request: PostReportRequest)

    @DELETE("articles/{id}/")
    suspend fun delete(@Path("id") postID: Int): Response<Unit>
}


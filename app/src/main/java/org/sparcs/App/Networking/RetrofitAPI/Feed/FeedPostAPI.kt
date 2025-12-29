package org.sparcs.App.Networking.RetrofitAPI.Feed

import org.sparcs.App.Networking.RequestDTO.Feed.FeedPostRequestDTO
import org.sparcs.App.Networking.ResponseDTO.Feed.FeedPostDTO
import org.sparcs.App.Networking.ResponseDTO.Feed.FeedPostPageDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface FeedPostApi {

    @GET("posts")
    suspend fun fetchPosts(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int
    ): FeedPostPageDTO

    @GET("posts/{postID}")
    suspend fun fetchPost(
        @Path("postID") postId: String
    ): FeedPostDTO

    @POST("posts")
    suspend fun writePost(
        @Body request: FeedPostRequestDTO
    ): FeedPostDTO

    @DELETE("posts/{postID}")
    suspend fun deletePost(
        @Path("postID") postId: String
    )

    @POST("posts/{postID}/vote")
    suspend fun vote(
        @Path("postID") postId: String,
        @Body body:  Map<String, String>
    )

    @DELETE("posts/{postID}/vote")
    suspend fun deleteVote(
        @Path("postID") postId: String
    )

    @POST("posts/{postID}/report")
    suspend fun reportPost(
        @Path("postID") postID: String,
        @Body body: Map<String, String> //reason, detail
    )
}
package org.sparcs.soap.App.Domain.Repositories.Feed

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Enums.Feed.FeedReportType
import org.sparcs.soap.App.Domain.Enums.Feed.FeedVoteType
import org.sparcs.soap.App.Domain.Models.Feed.FeedCreatePost
import org.sparcs.soap.App.Domain.Models.Feed.FeedPost
import org.sparcs.soap.App.Domain.Models.Feed.FeedPostPage
import org.sparcs.soap.App.Networking.RequestDTO.Feed.FeedPostRequestDTO
import org.sparcs.soap.App.Networking.ResponseDTO.handleApiError
import org.sparcs.soap.App.Networking.RetrofitAPI.Feed.FeedPostApi
import javax.inject.Inject

interface FeedPostRepositoryProtocol {
    suspend fun fetchPosts(cursor: String?, page: Int): FeedPostPage
    suspend fun fetchPost(postID: String): FeedPost
    suspend fun writePost(request: FeedCreatePost)
    suspend fun deletePost(postID: String)
    suspend fun vote(postID: String, type: FeedVoteType)
    suspend fun deleteVote(postID: String)
    suspend fun reportPost(postID: String, reason: FeedReportType, detail: String)
}

class FeedPostRepository @Inject constructor(
    private val api: FeedPostApi,
    private val gson: Gson = Gson(),
) : FeedPostRepositoryProtocol {

    override suspend fun fetchPosts(cursor: String?, page: Int): FeedPostPage = try {
        api.fetchPosts(cursor, page).toModel()
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun fetchPost(postID: String): FeedPost = try {
        api.fetchPost(postID).toModel()
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun writePost(request: FeedCreatePost) {
        try {
            val dto = FeedPostRequestDTO.fromModel(request)
            api.writePost(dto)
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun deletePost(postID: String) {
        try {
           api.deletePost(postID)
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun vote(postID: String, type: FeedVoteType) = try {
        api.vote(postID, mapOf("vote" to type.name))
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun deleteVote(postID: String) = try {
        api.deleteVote(postID)
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun reportPost(postID: String, reason: FeedReportType, detail: String) = try {
        api.reportPost(postID, mapOf("reason" to reason.name, "detail" to detail))
    } catch (e: Exception) {
        handleApiError(gson, e)
    }
}
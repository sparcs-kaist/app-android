package org.sparcs.soap.app.domain.repositories.feed

import com.google.gson.Gson
import org.sparcs.soap.app.domain.enums.feed.FeedReportType
import org.sparcs.soap.app.domain.enums.feed.FeedVoteType
import org.sparcs.soap.app.domain.models.feed.FeedCreatePost
import org.sparcs.soap.app.domain.models.feed.FeedPost
import org.sparcs.soap.app.domain.models.feed.FeedPostPage
import org.sparcs.soap.app.networking.requestDTO.feed.FeedPostRequestDTO
import org.sparcs.soap.app.networking.responseDTO.safeApiCall
import org.sparcs.soap.app.networking.retrofitAPI.feed.FeedPostApi
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

    override suspend fun fetchPosts(cursor: String?, page: Int): FeedPostPage = safeApiCall(gson) {
        api.fetchPosts(cursor, page)
    }.toModel()

    override suspend fun fetchPost(postID: String): FeedPost = safeApiCall(gson) {
        api.fetchPost(postID)
    }.toModel()

    override suspend fun writePost(request: FeedCreatePost) = safeApiCall(gson) {
        val dto = FeedPostRequestDTO.fromModel(request)
        api.writePost(dto)
    }

    override suspend fun deletePost(postID: String) = safeApiCall(gson) {
        api.deletePost(postID)
    }

    override suspend fun vote(postID: String, type: FeedVoteType) = safeApiCall(gson) {
        api.vote(postID, mapOf("vote" to type.name))
    }

    override suspend fun deleteVote(postID: String) = safeApiCall(gson) {
        api.deleteVote(postID)
    }

    override suspend fun reportPost(postID: String, reason: FeedReportType, detail: String) = safeApiCall(gson) {
        api.reportPost(postID, mapOf("reason" to reason.name, "detail" to detail))
    }
}